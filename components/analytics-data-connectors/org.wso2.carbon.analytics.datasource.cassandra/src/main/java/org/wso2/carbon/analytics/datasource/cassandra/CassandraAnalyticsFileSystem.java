/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.datasource.cassandra;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedDataInput;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedDataOutput;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedStream;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

/**
 * This class represents the Cassandra implementation of {@link AnalyticsFileSystem}.
 */
public class CassandraAnalyticsFileSystem implements AnalyticsFileSystem {
    
    private static final String CASSANDRA_SERVERS = "servers";
    
    private Session session;
    
    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String servers = properties.get(CASSANDRA_SERVERS);
        if (servers == null) {
            throw new AnalyticsException("The Cassandra connector property '" + CASSANDRA_SERVERS + "' is mandatory");
        }
        Cluster cluster = Cluster.builder().addContactPoints(servers.split(",")).build();
        this.session = cluster.connect();
        this.session.execute("CREATE KEYSPACE IF NOT EXISTS ANX WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':3};");
        this.session.execute("DROP TABLE ANX.PATH");
        this.session.execute("DROP TABLE ANX.DATA");
        this.session.execute("CREATE TABLE IF NOT EXISTS ANX.PATH (path VARCHAR, child VARCHAR, length BIGINT, PRIMARY KEY (path, child))");
        this.session.execute("CREATE TABLE IF NOT EXISTS ANX.DATA (path VARCHAR, sequence BIGINT, data BLOB, PRIMARY KEY (path, sequence))");
    }
    
    @Override
    public DataInput createInput(String path) throws IOException {
        return new ChunkedDataInput(new CassandraDataStream(path));
    }

    @Override
    public OutputStream createOutput(String path) throws IOException {
        this.createFile(path);
        return new ChunkedDataOutput(new CassandraDataStream(path));
    }

    @Override
    public void delete(String path) throws IOException {
        if (path.equals("/")) {
            return;
        }
        path = GenericUtils.normalizePath(path);
        List<String> children = this.list(path);
        for (String child : children) {
            this.delete(path + "/" + child);
        }
        this.session.execute("DELETE FROM ANX.PATH WHERE path = ? and child = ?", 
                (Object[]) CassandraUtils.splitParentChild(path));
    }

    @Override
    public void destroy() throws IOException {
        if (this.session != null) {
            this.session.close();
        }
    }

    @Override
    public boolean exists(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        ResultSet rs = this.session.execute("SELECT child from ANX.PATH WHERE path = ? and child = ?",
                (Object[]) CassandraUtils.splitParentChild(path));
        return rs.iterator().hasNext();
    }

    @Override
    public long length(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        ResultSet rs = this.session.execute("SELECT length from ANX.PATH WHERE path = ? and child = ?",
                (Object[]) CassandraUtils.splitParentChild(path));
        Row row = rs.one();
        if (row == null) {
            return 0;
        } else {
            return row.getLong(0);
        }
    }
    
    public void setLength(String path, long length) throws IOException {
        path = GenericUtils.normalizePath(path);
        String[] parentChild = CassandraUtils.splitParentChild(path);
        this.session.execute("UPDATE ANX.PATH SET length = ? WHERE path = ? and child = ?",
                length, parentChild[0], parentChild[1]);
    }

    @Override
    public List<String> list(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        ResultSet rs = this.session.execute("SELECT child from ANX.PATH WHERE path = ?", path);
        List<String> result = new ArrayList<String>();
        for (Row row : rs.all()) {
            result.add(row.getString(0));
        }
        return result;
    }

    @Override
    public void mkdir(String path) throws IOException {
        this.createFile(path);
    }
    
    private void createFile(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        String parentPath = GenericUtils.getParentPath(path);
        if (parentPath != null && !this.exists(parentPath)) {
            this.createFile(parentPath);
        }
        this.session.execute("INSERT INTO ANX.PATH (path, child) VALUES (?, ?)", 
                (Object[]) CassandraUtils.splitParentChild(path));
    }

    @Override
    public void renameFileInDirectory(String dirPath, String nameFrom, String nameTo) throws IOException {
        dirPath = GenericUtils.normalizePath(dirPath);
        this.session.execute("INSERT INTO ANX.PATH (path, child, length) VALUES (?, ?, ?)", 
                dirPath, nameTo, this.length(dirPath + "/" + nameFrom));
        this.session.execute("DELETE FROM ANX.PATH WHERE path = ? and child = ?",
                dirPath, nameFrom);
        ResultSet rs = this.session.execute("SELECT sequence, data FROM ANX.DATA WHERE path = ?", 
                dirPath + "/" + nameFrom);
        Iterator<Row> itr = rs.iterator();
        Row row;
        PreparedStatement ps = session.prepare("INSERT INTO ANX.DATA (path, sequence, data) VALUES (?, ?, ?)");
        BatchStatement stmt = new BatchStatement();
        while (itr.hasNext()) {
            row = itr.next();
            stmt.add(ps.bind(dirPath + "/" + nameTo, row.getLong(0), row.getBytes(1)));
        }
        session.execute(stmt);
        this.session.execute("DELETE FROM ANX.DATA WHERE path = ?", dirPath + "/" + nameFrom);
    }

    @Override
    public void sync(String path) throws IOException {
        /* nothing to do */
    }
    
    /**
     * Cassandra implementation of {@link ChunkedStream}.
     */
    private class CassandraDataStream extends ChunkedStream {

        private static final int DATA_CHUNK_SIZE = 10240;
        
        private String path;

        public CassandraDataStream(String path) {
            super(DATA_CHUNK_SIZE);
            this.path = path;
        }

        @Override
        public long length() throws IOException {
            return CassandraAnalyticsFileSystem.this.length(this.path);
        }

        @Override
        public DataChunk readChunk(long index) throws IOException {
            ResultSet rs = session.execute("SELECT data FROM ANX.DATA WHERE path = ? and sequence = ?", 
                    this.path, index);
            Row row = rs.one();
            if (row == null) {
                return null;
            }
            ByteBuffer byteBuffer = row.getBytes(0);
            byte[] data = new byte[byteBuffer.remaining()];
            byteBuffer.get(data);
            return new DataChunk(index, data);
        }

        @Override
        public void setLength(long length) throws IOException {
            CassandraAnalyticsFileSystem.this.setLength(this.path, length);
        }

        @Override
        public void writeChunks(List<DataChunk> chunks) throws IOException {
            PreparedStatement ps = session.prepare("INSERT INTO ANX.DATA (path, sequence, data) VALUES (?, ?, ?)");
            BatchStatement stmt = new BatchStatement();
            for (DataChunk chunk : chunks) {
                stmt.add(ps.bind(this.path, chunk.getChunkNumber(), ByteBuffer.wrap(chunk.getData())));
            }
            session.execute(stmt);
        }
        
    }

}
