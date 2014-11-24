/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.datasource.rdbms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedDataInput;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedDataOutput;
import org.wso2.carbon.analytics.datasource.core.ChunkedStream;
import org.wso2.carbon.analytics.datasource.core.ChunkedStream.DataChunk;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.rdbms.RDBMSUtils;

/**
 * RDBMS {@link FileSystem} implementation.
 */
public class RDBMSFileSystem implements FileSystem {
    
    private final byte[] FS_EMPTY_DATA_CHUNK;
    
    private QueryConfiguration queryConfiguration;
    
    private DataSource dataSource;
    
    private static final Log log = LogFactory.getLog(RDBMSFileSystem.class);
    
    public RDBMSFileSystem(QueryConfiguration queryConfiguration, 
            DataSource dataSource) throws AnalyticsDataSourceException {
        this.queryConfiguration = queryConfiguration;
        this.dataSource = dataSource;
        this.FS_EMPTY_DATA_CHUNK = new byte[this.getQueryConfiguration().getFsDataChunkSize()];
    }
    
    public QueryConfiguration getQueryConfiguration() {
        return queryConfiguration;
    }
    
    public DataSource getDataSource() {
        return dataSource;
    }
    
    private Connection getConnection() throws SQLException {
        return this.getConnection(true);
    }
    
    private Connection getConnection(boolean autoCommit) throws SQLException {
        Connection conn = this.getDataSource().getConnection();
        conn.setAutoCommit(autoCommit);
        return conn;
    }
    
    @Override
    public void copy(String srcPath, String destPath) throws AnalyticsDataSourceException {
        try {
            DataInput input = this.createInput(srcPath);
            if (!this.exists(destPath)) {
                this.createFile(destPath);
            }
            DataOutput output = this.createOutput(destPath);
            byte[] buff = new byte[1024];
            int i;
            while ((i = input.read(buff, 0, buff.length)) > 0) {
                output.write(buff, 0, i);
            }
            output.close();
            input.close();
        } catch (AnalyticsDataSourceException e) {
            throw new AnalyticsDataSourceException("Error in file copy "
                    + "[" + srcPath + " -> " + destPath + "]: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String path) throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection();
            this.deleteImpl(conn, path);
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in file delete: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    protected void deleteImpl(Connection conn, String path) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement(this.getDeletePathQuery());
        stmt.setString(1, path);
        stmt.executeUpdate();
        RDBMSUtils.cleanupConnection(null, stmt, null);
    }
    
    protected String getDeletePathQuery() {
        return this.getQueryConfiguration().getFsDeletePathQuery();
    }

    @Override
    public boolean exists(String path) throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection();
            return this.existsImpl(conn, path);
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in file exists: " + e.getMessage());
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private boolean existsImpl(Connection conn, String path) throws SQLException {
        if (path == null) {
            return true;
        }
        PreparedStatement stmt = conn.prepareStatement(this.getSelectPathQuery());
        stmt.setString(1, path);
        ResultSet rs = stmt.executeQuery();
        boolean result = rs.next();
        RDBMSUtils.cleanupConnection(rs, stmt, null);
        return result;
    }
    
    protected String getSelectPathQuery() {
        return this.getQueryConfiguration().getFsPathRetrievalQuery();
    }
    
    protected String getListFilesQuery() {
        return this.getQueryConfiguration().getFsListFilesQuery();
    }

    @Override
    public List<String> list(String path) throws AnalyticsDataSourceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(this.getListFilesQuery());
            stmt.setString(1, path);
            rs = stmt.executeQuery();
            List<String> result = new ArrayList<String>();
            while (rs.next()) {
                result.add(rs.getString(1));
            }
            return result;
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in file exists: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    @Override
    public void sync(String path) throws AnalyticsDataSourceException {
        /* nothing to do */
    }

    @Override
    public void mkdir(String path) throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            this.createFileImpl(conn, path, true);
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in mkdir: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private void createFileImpl(Connection conn, String path, boolean isDir) throws SQLException {
        String parentPath = GenericUtils.getParentPath(path);
        if (!this.existsImpl(conn, parentPath)) {
            this.createFileImpl(conn, parentPath, true);
        } else if (this.existsImpl(conn, path)) {
            return;
        }
        PreparedStatement stmt = conn.prepareStatement(this.getInsertPathQuery());
        stmt.setString(1, path);
        stmt.setBoolean(2, isDir);
        stmt.setLong(3, 0);
        stmt.setString(4, parentPath);
        try {
            stmt.executeUpdate();
        } catch (SQLException e) {
            /* if this exception is because someone else already added the directory, we can ignore it */
            if (!this.existsImpl(conn, path)) {
                throw e;
            }
        }
        RDBMSUtils.cleanupConnection(null, stmt, null);
    }
    
    protected void createFile(String path) throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            this.createFileImpl(conn, path, false);
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in creating file: " + e.getMessage());
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    protected String getInsertPathQuery() {
        return this.getQueryConfiguration().getFsInsertPathQuery();
    }
    
    protected String getFileLengthQuery() {
        return this.getQueryConfiguration().getFsFileLengthRetrievalQuery();
    }

    @Override
    public long length(String path) throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection();
            return this.lengthImpl(conn, path);
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in file length: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    protected long lengthImpl(Connection conn, String path) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        stmt = conn.prepareStatement(this.getFileLengthQuery());
        stmt.setString(1, path);
        rs = stmt.executeQuery();
        long result = -1;
        if (rs.next()) {
            result = rs.getLong(1);
        }
        RDBMSUtils.cleanupConnection(rs, stmt, null);
        return result;
    }
    
    protected void setLength(String path, long length) throws AnalyticsDataSourceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(this.getSetLengthQuery());
            stmt.setLong(1, length);
            stmt.setString(2, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in file delete: " + e.getMessage());
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    protected String getSetLengthQuery() {
        return this.getQueryConfiguration().getFsSetFileLengthQuery();
    }
    
    private byte[] inputStreamToByteArray(InputStream in) throws AnalyticsDataSourceException {
        byte[] buff = new byte[256];
        int i;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            while ((i = in.read(buff)) > 0) {
                out.write(buff, 0, i);
            }
            out.close();
            in.close();
            return out.toByteArray();
        } catch (IOException e) {
            throw new AnalyticsDataSourceException("Error in converting input stream -> byte[]: " + e.getMessage(), e);
        }
    }
    
    protected byte[] readChunkData(String path, long n) throws AnalyticsDataSourceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(this.getReadDataChunkQuery());
            stmt.setString(1, path);
            stmt.setLong(2, n);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return this.inputStreamToByteArray(rs.getBinaryStream(1));
            } else {
                return FS_EMPTY_DATA_CHUNK;
            }
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in file read chunk: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }
    
    private String getReadDataChunkQuery() {
        return this.getQueryConfiguration().getFsReadDataChunkQuery();
    }
    
    private void writeChunks(String path, List<DataChunk> chunks) throws AnalyticsDataSourceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection(false);
            stmt = conn.prepareStatement(this.getWriteDataChunkQuery());
            for (DataChunk chunk : chunks) {
                this.populateStatementWithDataChunk(stmt, path, chunk);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            RDBMSUtils.rollbackConnection(conn);
            /* this is maybe because we are updating some data already in the file with a seek operation,
             * and the given write chunk query is not an insert or update, so lets insert sequentially
             * and check, if an error comes, a separate update statement will be executed and checked */
            if (log.isDebugEnabled()) {
                log.debug("Chunk batch write failed: " + e.getMessage() + 
                        ", falling back to sequential insert/update..");
            }
            RDBMSUtils.cleanupConnection(null, stmt, null);
            stmt = null;
            this.writeChunksSequentially(conn, path, chunks);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    private void writeChunksSequentially(Connection conn, String path, 
        List<DataChunk> chunks) throws AnalyticsDataSourceException {
        PreparedStatement stmt = null;
        String query;
        for (DataChunk chunk : chunks) {
            try {
                stmt = conn.prepareStatement(this.getQueryConfiguration().getFsWriteDataChunkQuery());
                this.populateStatementWithDataChunk(stmt, path, chunk);
                stmt.execute();
            } catch (SQLException e) {
                /* maybe the chunk is already there, lets try the update */
                try {
                    query = this.getQueryConfiguration().getFsUpdateDataChunkQuery();
                    if (query == null) {
                        throw new AnalyticsDataSourceException("A required property 'FsUpdateDataChunkQuery' "
                                + "for the current analytics data source is not specified");
                    }
                    stmt = conn.prepareStatement(query);
                    this.populateStatementWithDataChunkUpdate(stmt, path, chunk);
                    stmt.execute();
                } catch (SQLException e1) {
                    throw new AnalyticsDataSourceException("Error in updating data chunk: " + e1.getMessage(), e1);
                }
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        log.error("Error closing statement: " + e.getMessage(), e);
                    }
                }
            }
        }
    }
    
    private void populateStatementWithDataChunk(PreparedStatement stmt, String path, 
            DataChunk chunk) throws SQLException {
        stmt.setString(1, path);
        stmt.setLong(2, chunk.getChunkNumber());
        stmt.setBinaryStream(3, new ByteArrayInputStream(chunk.getData()));
    }
    
    private void populateStatementWithDataChunkUpdate(PreparedStatement stmt, String path, 
            DataChunk chunk) throws SQLException {
        stmt.setBinaryStream(1, new ByteArrayInputStream(chunk.getData()));
        stmt.setString(2, path);
        stmt.setLong(3, chunk.getChunkNumber());
    }

    private String getWriteDataChunkQuery() {
        return this.getQueryConfiguration().getFsWriteDataChunkQuery();
    }
    
    /**
     * RDBMS implementation of {@link ChunkedStream}.
     */
    public class RDBMSDataStream extends ChunkedStream {
        
        private String path;
        
        public RDBMSDataStream(String path) {
            super(getQueryConfiguration().getFsDataChunkSize());
            this.path = path;
        }

        public RDBMSDataStream(int chunkSize) {
            super(chunkSize);
        }
        
        public String getPath() {
            return path;
        }

        @Override
        public long length() throws AnalyticsDataSourceException {
            return RDBMSFileSystem.this.length(this.getPath());
        }

        @Override
        public DataChunk readChunk(long n) throws AnalyticsDataSourceException {
            return new DataChunk(n, RDBMSFileSystem.this.readChunkData(this.getPath(), n));
        }

        @Override
        public void setLength(long length) throws AnalyticsDataSourceException {
            RDBMSFileSystem.this.setLength(this.getPath(), length);
        }

        @Override
        public void writeChunks(List<DataChunk> chunks) throws AnalyticsDataSourceException {
            RDBMSFileSystem.this.writeChunks(this.getPath(), chunks);
        }
        
    }

    @Override
    public DataInput createInput(String path)
            throws AnalyticsDataSourceException {
        return new ChunkedDataInput(new RDBMSDataStream(path));
    }

    @Override
    public DataOutput createOutput(String path)
            throws AnalyticsDataSourceException {
        this.createFile(path);
        return new ChunkedDataOutput(new RDBMSDataStream(path));
    }

}