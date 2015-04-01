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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedDataInput;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedDataOutput;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedStream;
import org.wso2.carbon.analytics.datasource.core.fs.ChunkedStream.DataChunk;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RDBMS {@link AnalyticsFileSystem} implementation.
 */
public class RDBMSAnalyticsFileSystem implements AnalyticsFileSystem {
    
    /** One time set empty data chunk value */
    private byte[] FS_EMPTY_DATA_CHUNK;
    
    private RDBMSQueryConfigurationEntry rdbmsQueryConfigurationEntry;
    
    private DataSource dataSource;
    
    private static final Log log = LogFactory.getLog(RDBMSAnalyticsFileSystem.class);

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String dsName = properties.get(RDBMSAnalyticsDSConstants.DATASOURCE);
        if (dsName == null) {
            throw new AnalyticsException("The property '" + 
                    RDBMSAnalyticsDSConstants.DATASOURCE + "' is required");
        }
        try {
            this.dataSource = (DataSource) InitialContext.doLookup(dsName);
        } catch (NamingException e) {
            throw new AnalyticsException("Error in looking up data source: " + 
                    e.getMessage(), e);
        }
        if (this.rdbmsQueryConfigurationEntry == null) {
            this.rdbmsQueryConfigurationEntry = RDBMSUtils.lookupCurrentQueryConfigurationEntry(this.dataSource);
        }
        this.FS_EMPTY_DATA_CHUNK = new byte[this.getQueryConfiguration().getFsDataChunkSize()];
        /* create the system tables */
        this.checkAndCreateSystemTables();
    }
    
    public RDBMSAnalyticsFileSystem() {
        this.rdbmsQueryConfigurationEntry = null;
    }
    
    public RDBMSAnalyticsFileSystem(RDBMSQueryConfigurationEntry rDBMSQueryConfigurationEntry) throws IOException {
        this.rdbmsQueryConfigurationEntry = rDBMSQueryConfigurationEntry;
    }
    
    private void checkAndCreateSystemTables() throws AnalyticsException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            Statement stmt;
            if (!this.checkSystemTables(conn)) {
                for (String query : this.getFsTableInitSQLQueries()) {
                    stmt = conn.createStatement();
                    stmt.executeUpdate(query);
                    stmt.close();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsException("Error in creating system tables: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private boolean checkSystemTables(Connection conn) {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute(this.getSystemTableCheckQuery());
            return true;
        } catch (SQLException ignore) {
            RDBMSUtils.cleanupConnection(null, stmt, null);
            return false;
        }
    }
    
    private String[] getFsTableInitSQLQueries() {
        return this.getQueryConfiguration().getFsTableInitQueries();
    }
    
    private String getSystemTableCheckQuery() {
        return this.getQueryConfiguration().getFsTablesCheckQuery();
    }
    
    public RDBMSQueryConfigurationEntry getQueryConfiguration() {
        return rdbmsQueryConfigurationEntry;
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
    public void delete(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        Connection conn = null;
        try {
            conn = this.getConnection();
            this.deleteImpl(conn, path);
        } catch (SQLException e) {
            throw new IOException("Error in file delete: " + path + ": " + e.getMessage(), e);
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
    public boolean exists(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        Connection conn = null;
        try {
            conn = this.getConnection();
            return this.existsImpl(conn, path);
        } catch (SQLException e) {
            throw new IOException("Error in file exists: " + path + ": " + e.getMessage());
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
    public List<String> list(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
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
                result.add(rs.getString(1).substring(path.length() + 1));
            }
            return result;
        } catch (SQLException e) {
            throw new IOException("Error in file exists: " + path + ": " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }

    @Override
    public void sync(String path) throws IOException {
        /* nothing to do here, since the data is already sync'ed when flush/close is called,
         * this is guaranteed since, before sync is called, the users will call close */
    }

    @Override
    public void mkdir(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            this.createFileImpl(conn, path, true);
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new IOException("Error in mkdir: " + path + ": " + e.getMessage(), e);
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
    
    protected void createFile(String path) throws IOException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            this.createFileImpl(conn, path, false);
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new IOException("Error in creating file: " + path + ": " + e.getMessage(), e);
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
    public long length(String path) throws IOException {
        path = GenericUtils.normalizePath(path);
        Connection conn = null;
        try {
            conn = this.getConnection();
            return this.lengthImpl(conn, path);
        } catch (SQLException e) {
            throw new IOException("Error in file length: " + path + ": " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }

    @Override
    public void destroy() throws IOException {
        /* do nothing */
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
    
    protected void setLength(String path, long length) throws IOException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(this.getSetLengthQuery());
            stmt.setLong(1, length);
            stmt.setString(2, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IOException("Error in file delete: " + path + ": " + e.getMessage());
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    protected String getSetLengthQuery() {
        return this.getQueryConfiguration().getFsSetFileLengthQuery();
    }
    
    private byte[] inputStreamToByteArray(InputStream in) throws IOException {
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
            throw new IOException("Error in converting input stream -> byte[]: " + e.getMessage(), e);
        }
    }
    
    protected byte[] readChunkData(String path, long n) throws IOException {
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
            throw new IOException("Error in file read chunk: " + path + ": " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }
    
    private String getReadDataChunkQuery() {
        return this.getQueryConfiguration().getFsReadDataChunkQuery();
    }
    
    private void writeChunks(String path, List<DataChunk> chunks) throws IOException {
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
        List<DataChunk> chunks) throws IOException {
        PreparedStatement stmt = null;
        String query;
        for (DataChunk chunk : chunks) {
            try {
                stmt = conn.prepareStatement(this.getQueryConfiguration().getFsWriteDataChunkQuery());
                this.populateStatementWithDataChunk(stmt, path, chunk);
                stmt.execute();
                conn.commit();
            } catch (SQLException e) {
                /* maybe the chunk is already there, lets try the update */
                try {
                    query = this.getQueryConfiguration().getFsUpdateDataChunkQuery();
                    if (query == null) {
                        throw new IOException("A required property 'FsUpdateDataChunkQuery' "
                                + "for the current analytics data source is not specified");
                    }
                    stmt = conn.prepareStatement(query);
                    this.populateStatementWithDataChunkUpdate(stmt, path, chunk);
                    stmt.execute();
                    conn.commit();
                } catch (SQLException e1) {
                    throw new IOException("Error in updating data chunk: " + path + ": " + e1.getMessage(), e1);
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
        
        public RDBMSDataStream(String path) throws IOException {
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
        public long length() throws IOException {
            return RDBMSAnalyticsFileSystem.this.length(this.getPath());
        }

        @Override
        public DataChunk readChunk(long n) throws IOException {
            return new DataChunk(n, RDBMSAnalyticsFileSystem.this.readChunkData(this.getPath(), n));
        }

        @Override
        public void setLength(long length) throws IOException {
            RDBMSAnalyticsFileSystem.this.setLength(this.getPath(), length);
        }

        @Override
        public void writeChunks(List<DataChunk> chunks) throws IOException {
            RDBMSAnalyticsFileSystem.this.writeChunks(this.getPath(), chunks);
        }
        
    }

    @Override
    public DataInput createInput(String path)
            throws IOException {
        path = GenericUtils.normalizePath(path);
        return new ChunkedDataInput(new RDBMSDataStream(path));
    }

    @Override
    public OutputStream createOutput(String path)
            throws IOException {
        path = GenericUtils.normalizePath(path);
        this.createFile(path);
        return new ChunkedDataOutput(new RDBMSDataStream(path));
    }

}