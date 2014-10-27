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
package org.wso2.carbon.analytics.datasource.rdbms.h2;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.ChunkedDataInput;
import org.wso2.carbon.analytics.datasource.core.ChunkedDataOutput;
import org.wso2.carbon.analytics.datasource.core.ChunkedStream;
import org.wso2.carbon.analytics.datasource.core.ChunkedStream.DataChunk;
import org.wso2.carbon.analytics.datasource.core.FileSystem;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.rdbms.common.RDBMSAnalyticsDSConstants;
import org.wso2.carbon.analytics.datasource.rdbms.common.RDBMSUtils;

/**
 * H2 {@link FileSystem} implementation.
 */
public class H2FileSystem implements FileSystem {
    
    private DataSource dataSource;
    
    public H2FileSystem(DataSource dataSource) throws AnalyticsDataSourceException {
        this.dataSource = dataSource;
        this.init();
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
    
    private void init() throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
            conn = this.getConnection();
            Set<String> tables = this.getTablesInDB(conn);
            if (!this.checkPathTableExists(tables)) {
                this.createPathTable(conn);
                this.createParentPathIndex(conn);
            }
            if (!this.checkDataTableExists(tables)) {
                this.createDataTable(conn);
            }
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in initializing H2 File System: " + 
                    e.getMessage());
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    protected boolean checkPathTableExists(Set<String> tables) {
        return tables.contains(RDBMSAnalyticsDSConstants.FS_PATH_TABLE);
    }
    
    protected boolean checkDataTableExists(Set<String> tables) {
        return tables.contains(RDBMSAnalyticsDSConstants.FS_DATA_TABLE);
    }
    
    protected Set<String> getTablesInDB(Connection conn) throws SQLException {
        DatabaseMetaData dmd = conn.getMetaData();
        ResultSet rs = dmd.getTables(null, null, "%", null);
        Set<String> result = new HashSet<String>();
        while (rs.next()) {
            result.add(rs.getString(3));
        }
        rs.close();
        return result;
    }
    
    protected void createPathTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(this.generatePathCreateTableSQL());
        stmt.close();
    }
    
    protected void createParentPathIndex(Connection conn) throws SQLException {
        String sql = this.generateParentPathAddIndexSQL();
        if (sql != null) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
        }
    }
    
    protected void createDataTable(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(this.generateDataCreateTableSQL());
        stmt.close();
    }
    
    protected String generatePathCreateTableSQL() {
        return "CREATE TABLE " + RDBMSAnalyticsDSConstants.FS_PATH_TABLE + " (path VARCHAR(256), is_directory BOOLEAN, "
                + "length BIGINT, parent_path VARCHAR(256), PRIMARY KEY(path), FOREIGN KEY (parent_path) "
                + "REFERENCES FS_PATH(path) ON DELETE CASCADE)";
    }
    
    protected String generateDataCreateTableSQL() {
        return "CREATE TABLE " + RDBMSAnalyticsDSConstants.FS_DATA_TABLE + " (path VARCHAR(256), "
                + "sequence BIGINT, data VARBINARY(" + RDBMSAnalyticsDSConstants.FS_DATA_CHUNK_SIZE + "), "
                + "PRIMARY KEY (path,sequence), FOREIGN KEY (path) "
                + "REFERENCES FS_PATH(path) ON DELETE CASCADE)";
    }
    
    protected String generateParentPathAddIndexSQL() {
        return "CREATE INDEX index_parent_id ON " + RDBMSAnalyticsDSConstants.FS_PATH_TABLE + "(parent_path)";
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
        PreparedStatement stmt = conn.prepareStatement(this.generateDeletePathSQL());
        stmt.setString(1, path);
        stmt.executeUpdate();
        RDBMSUtils.cleanupConnection(null, stmt, null);
    }
    
    protected String generateDeletePathSQL() {
        return "DELETE FROM " + RDBMSAnalyticsDSConstants.FS_PATH_TABLE + " WHERE path = ?";
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
        PreparedStatement stmt = conn.prepareStatement(this.generateSelectPathSQL());
        stmt.setString(1, path);
        ResultSet rs = stmt.executeQuery();
        boolean result = rs.next();
        RDBMSUtils.cleanupConnection(rs, stmt, null);
        return result;
    }
    
    protected String generateSelectPathSQL() {
        return "SELECT * FROM " + RDBMSAnalyticsDSConstants.FS_PATH_TABLE + " WHERE path = ?";
    }
    
    protected String generateListFilesSQL() {
        return "SELECT path FROM " + RDBMSAnalyticsDSConstants.FS_PATH_TABLE + " WHERE parent_path = ?";
    }

    @Override
    public List<String> list(String path) throws AnalyticsDataSourceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(this.generateListFilesSQL());
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
        PreparedStatement stmt = conn.prepareStatement(this.generateInsertPathSQL());
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
    
    protected String generateInsertPathSQL() {
        return "INSERT INTO " + RDBMSAnalyticsDSConstants.FS_PATH_TABLE + " (path,is_directory,length,parent_path) VALUES (?,?,?,?)";
    }
    
    protected String generateGetFileLengthSQL() {
        return "SELECT length FROM " + RDBMSAnalyticsDSConstants.FS_PATH_TABLE + " WHERE path = ?";
    }

    @Override
    public long length(String path) throws AnalyticsDataSourceException {
        Connection conn = null;
        try {
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
        conn = this.getConnection();
        stmt = conn.prepareStatement(this.generateGetFileLengthSQL());
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
            stmt = conn.prepareStatement(this.generateSetLengthSQL());
            stmt.setLong(1, length);
            stmt.setString(2, path);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in file delete: " + e.getMessage());
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    protected String generateSetLengthSQL() {
        return "UPDATE " + RDBMSAnalyticsDSConstants.FS_PATH_TABLE + " SET length = ? WHERE path = ?";
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
            stmt = conn.prepareStatement(this.generateReadDataChunkSQL());
            stmt.setString(1, path);
            stmt.setLong(2, n);
            rs = stmt.executeQuery();
            if (rs.next()) {
                return this.inputStreamToByteArray(rs.getBinaryStream(1));
            } else {
                return RDBMSAnalyticsDSConstants.FS_EMPTY_DATA_CHUNK;
            }
        } catch (SQLException e) {
            throw new AnalyticsDataSourceException("Error in file read chunk: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }
    
    protected String generateReadDataChunkSQL() {
        return "SELECT data FROM " + RDBMSAnalyticsDSConstants.FS_DATA_TABLE + " WHERE path = ? AND sequence = ?";
    }
    
    protected void writeChunks(String path, List<DataChunk> chunks) throws AnalyticsDataSourceException {
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection(false);
            stmt = conn.prepareStatement(this.generateWriteDataChunkSQL());
            for (DataChunk chunk : chunks) {
                this.populateStatementWithDataChunk(stmt, path, chunk);
                stmt.addBatch();
            }
            stmt.executeBatch();
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsDataSourceException("Error in writing file data chunks: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
    
    protected void populateStatementWithDataChunk(PreparedStatement stmt, String path, 
            DataChunk chunk) throws SQLException {
        stmt.setString(1, path);
        stmt.setLong(2, chunk.getChunkNumber());
        stmt.setBinaryStream(3, new ByteArrayInputStream(chunk.getData()));
    }

    protected String generateWriteDataChunkSQL() {
        return "INSERT INTO " + RDBMSAnalyticsDSConstants.FS_DATA_TABLE + " (path,sequence,data) VALUES (?,?,?) "
                + "ON DUPLICATE KEY UPDATE path=VALUES(path), sequence=VALUES(sequence), data=VALUES(data)";
    }
    
    public class H2DataStream extends ChunkedStream {
        
        private String path;
        
        public H2DataStream(String path) {
            super(RDBMSAnalyticsDSConstants.FS_DATA_CHUNK_SIZE);
            this.path = path;
        }

        public H2DataStream(int chunkSize) {
            super(chunkSize);
        }
        
        public String getPath() {
            return path;
        }

        @Override
        public long length() throws AnalyticsDataSourceException {
            return H2FileSystem.this.length(this.getPath());
        }

        @Override
        public DataChunk readChunk(long n) throws AnalyticsDataSourceException {
            return new DataChunk(n, H2FileSystem.this.readChunkData(this.getPath(), n));
        }

        @Override
        public void setLength(long length) throws AnalyticsDataSourceException {
            H2FileSystem.this.setLength(this.getPath(), length);
        }

        @Override
        public void writeChunks(List<DataChunk> chunks) throws AnalyticsDataSourceException {
            H2FileSystem.this.writeChunks(this.getPath(), chunks);
        }
        
    }

    @Override
    public DataInput createInput(String path)
            throws AnalyticsDataSourceException {
        return new ChunkedDataInput(new H2DataStream(path));
    }

    @Override
    public DataOutput createOutput(String path)
            throws AnalyticsDataSourceException {
        this.createFile(path);
        return new ChunkedDataOutput(new H2DataStream(path));
    }

}
