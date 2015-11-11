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

import com.datastax.driver.core.*;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedDataInput;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedDataOutput;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedStream;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.ndatasource.common.DataSourceException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Cassandra implementation of {@link AnalyticsFileSystem}.
 */
public class CassandraAnalyticsFileSystem implements AnalyticsFileSystem {
        
    private Session session;
    
    private PreparedStatement fsDataInsertStmt;
    
    private PreparedStatement fsDataSelectStmt;
    
    private String ksName;

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String dsName = CassandraUtils.extractDataSourceName(properties);
        this.ksName = CassandraUtils.extractAFSKSName(properties);
        String ksCreateQuery = CassandraUtils.generateCreateKeyspaceQuery(this.ksName, properties);
        try {
            Cluster cluster = (Cluster) GenericUtils.loadGlobalDataSource(dsName);
            if (cluster == null) {
                throw new AnalyticsException("Error establishing connection to Cassandra instance: Invalid datasource configuration");
            }
            this.session = cluster.connect();
            if (session == null) {
                throw new AnalyticsException("Error establishing connection to Cassandra instance: Failed to initialize " +
                        "client from Datasource");
            }
            this.session.execute(ksCreateQuery);
            this.session.execute("CREATE TABLE IF NOT EXISTS " + this.ksName + 
                    ".PATH (path VARCHAR, child VARCHAR, length BIGINT, PRIMARY KEY (path, child))");
            this.session.execute("CREATE TABLE IF NOT EXISTS " + this.ksName + 
                    ".DATA (path VARCHAR, sequence BIGINT, data BLOB, PRIMARY KEY (path, sequence))");
            this.fsDataInsertStmt = this.session.prepare("INSERT INTO " + this.ksName + ".DATA (path, sequence, data) VALUES (?, ?, ?)");
            this.fsDataSelectStmt = this.session.prepare("SELECT data FROM " + this.ksName + ".DATA WHERE path = ? and sequence = ?");
        } catch (DataSourceException e) {
            throw new AnalyticsException("Error establishing connection to Cassandra instance for Analytics File System:" + e.getMessage(), e);
        }
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
        this.session.execute("DELETE FROM " + this.ksName + ".PATH WHERE path = ? and child = ?", 
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
        ResultSet rs = this.session.execute("SELECT child from " + this.ksName + ".PATH WHERE path = ? and child = ?",
                (Object[]) CassandraUtils.splitParentChild(path));
        return rs.iterator().hasNext();
    }

    @Override
    public long length(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        ResultSet rs = this.session.execute("SELECT length from " + this.ksName + ".PATH WHERE path = ? and child = ?",
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
        this.session.execute("UPDATE " + this.ksName + ".PATH SET length = ? WHERE path = ? and child = ?",
                length, parentChild[0], parentChild[1]);
    }

    @Override
    public List<String> list(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        ResultSet rs = this.session.execute("SELECT child from " + this.ksName + ".PATH WHERE path = ?", path);
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
        this.session.execute("INSERT INTO " + this.ksName + ".PATH (path, child) VALUES (?, ?)", 
                (Object[]) CassandraUtils.splitParentChild(path));
    }

    @Override
    public void renameFileInDirectory(String dirPath, String nameFrom, String nameTo) throws IOException {
        dirPath = GenericUtils.normalizePath(dirPath);
        this.session.execute("INSERT INTO " + this.ksName + ".PATH (path, child, length) VALUES (?, ?, ?)", 
                dirPath, nameTo, this.length(dirPath + "/" + nameFrom));
        this.session.execute("DELETE FROM " + this.ksName + ".PATH WHERE path = ? and child = ?",
                dirPath, nameFrom);
        ResultSet rs = this.session.execute("SELECT sequence, data FROM " + this.ksName + ".DATA WHERE path = ?", 
                dirPath + "/" + nameFrom);
        Iterator<Row> itr = rs.iterator();
        Row row;
        BatchStatement stmt = new BatchStatement();
        while (itr.hasNext()) {
            row = itr.next();
            stmt.add(this.fsDataInsertStmt.bind(dirPath + "/" + nameTo, row.getLong(0), row.getBytes(1)));
        }
        session.execute(stmt);
        this.session.execute("DELETE FROM " + this.ksName + ".DATA WHERE path = ?", dirPath + "/" + nameFrom);
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
            ResultSet rs = session.execute(fsDataSelectStmt.bind(this.path, index));
            Row row = rs.one();
            if (row == null) {
                throw new IOException("The data chunk for path: " + this.path + 
                        " at sequence: " + index + " does not exist.");
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
            BatchStatement stmt = new BatchStatement();
            for (DataChunk chunk : chunks) {
                stmt.add(fsDataInsertStmt.bind(this.path, chunk.getChunkNumber(), ByteBuffer.wrap(chunk.getData())));
            }
            session.execute(stmt);
        }
        
    }

}
