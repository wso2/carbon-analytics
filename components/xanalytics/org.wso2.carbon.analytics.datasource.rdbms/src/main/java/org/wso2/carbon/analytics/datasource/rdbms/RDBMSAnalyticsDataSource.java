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
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.DirectAnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Abstract RDBMS database backed implementation of {@link AnalyticsDataSource}.
 */
public class RDBMSAnalyticsDataSource extends DirectAnalyticsDataSource {

    private static final String RDBMS_QUERY_CONFIG_FILE = "rdbms-query-config.xml";

    private static final String ANALYTICS_CONF_DIR = "analytics";

    private static final Log log = LogFactory.getLog(RDBMSAnalyticsDataSource.class);
    
    private static final String ANALYTICS_USER_TABLE_PREFIX = "ANX";

    private static final String RECORD_IDS_PLACEHOLDER = "{{RECORD_IDS}}";

    private static final String TABLE_NAME_PLACEHOLDER = "{{TABLE_NAME}}";

    private DataSource dataSource;
    
    private Map<String, String> properties;
    
    private QueryConfigurationEntry queryConfigurationEntry;
    
    public RDBMSAnalyticsDataSource() throws AnalyticsException {
    }
    
    public RDBMSAnalyticsDataSource(QueryConfigurationEntry queryConfigurationEntry) {
        this.queryConfigurationEntry = queryConfigurationEntry;
    }
    
    @Override
    public void init(Map<String, String> properties)
            throws AnalyticsException {
        this.properties = properties;
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
        if (this.queryConfigurationEntry == null) {
            this.queryConfigurationEntry = lookupCurrentQueryConfigurationEntry();
        }
        /* create the system tables */
        this.checkAndCreateSystemTables();
    }
    
    private String lookupDatabaseType() throws AnalyticsException {
        Connection conn = null;
        try {
            conn = this.getConnection();
            DatabaseMetaData dmd = conn.getMetaData();
            return dmd.getDatabaseProductName();
        } catch (SQLException e) {
            throw new AnalyticsException("Error in looking up database type: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private QueryConfiguration loadQueryConfiguration() throws AnalyticsException {
        try {
            File confFile = new File(CarbonUtils.getCarbonConfigDirPath() + 
                    File.separator + ANALYTICS_CONF_DIR + File.separator + RDBMS_QUERY_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException("Cannot initalize RDBMS analytics data source, "
                        + "the query configuration file cannot be found at: " + confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(QueryConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (QueryConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsException(
                    "Error in processing RDBMS query configuration: " + e.getMessage(), e);
        }
    }
    
    private QueryConfigurationEntry lookupCurrentQueryConfigurationEntry() throws AnalyticsException {
        String dbType = this.lookupDatabaseType();
        if (log.isDebugEnabled()) {
            log.debug("Loaded RDBMS Analytics Database Type: " + dbType);
        }
        QueryConfiguration qcon = this.loadQueryConfiguration();
        for (QueryConfigurationEntry entry : qcon.getDatabases()) {
            if (entry.getDatabaseName().equalsIgnoreCase(dbType)) {
                return entry;
            }
        }
        throw new AnalyticsException("Cannot find a database section in the RDBMS "
                + "query configuration for the database: " + dbType);
    }
    
    public QueryConfigurationEntry getQueryConfiguration() {
        return queryConfigurationEntry;
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
    
    private String[] getRecordTableInitQueries(long tableCategoryId, String tableName) {
        String[] queries = this.getQueryConfiguration().getRecordTableInitQueries();
        String[] result = new String[queries.length];
        for (int i = 0; i < queries.length; i++) {
            result[i] = this.translateQueryWithTableInfo(queries[i], tableCategoryId, tableName);
        }
        return result;
    }
    
    private String[] getRecordTableDeleteQueries(long tableCategoryId, String tableName) {
        String[] queries = this.getQueryConfiguration().getRecordTableDeleteQueries();
        String[] result = new String[queries.length];
        for (int i = 0; i < queries.length; i++) {
            result[i] = this.translateQueryWithTableInfo(queries[i], tableCategoryId, tableName);
        }
        return result;
    }
    
    public Map<String, String> getProperties() {
        return properties;
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
    
    private String calculateRecordIdentity(Record record) {
        return this.generateTargetTableName(record.getTenantId(), record.getTableName());
    }

    @Override
    public void update(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            Map<String, List<Record>> recordBatches = this.generateRecordBatches(records);
            for (List<Record> batch : recordBatches.values()) {
                this.deleteRecordsSimilar(conn, batch);
            }
            for (List<Record> batch : recordBatches.values()) {
                this.addRecordsSimilar(conn, batch);
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsException("Error in updating records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private Map<String, List<Record>> generateRecordBatches(List<Record> records) {
        /* if the records have identities (unique table category and name) as the following
         * "ABABABCCAACBDABCABCDBAC", the job of this method is to make it like the following,
         * {"AAAAAAAA", "BBBBBBB", "CCCCCC", "DD" } */
        Map<String, List<Record>> recordBatches = new HashMap<String, List<Record>>();
        List<Record> recordBatch;
        for (Record record : records) {
            recordBatch = recordBatches.get(this.calculateRecordIdentity(record));
            if (recordBatch == null) {
                recordBatch = new ArrayList<Record>();
                recordBatches.put(this.calculateRecordIdentity(record), recordBatch);
            }
            recordBatch.add(record);
        }
        return recordBatches;
    }
    
    @Override
    public void insert(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {        
        if (records.size() == 0) {
            return;
        }
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            Map<String, List<Record>> recordBatches = this.generateRecordBatches(records);
            for (List<Record> batch : recordBatches.values()) {
                this.addRecordsSimilar(conn, batch);
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw new AnalyticsException("Error in adding records: " + e.getMessage(), e);
        } catch (AnalyticsException e) {
            RDBMSUtils.rollbackConnection(conn);
            throw e;
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private void addRecordsSimilar(Connection conn, 
            List<Record> records) throws SQLException, 
            AnalyticsException, AnalyticsTableNotAvailableException {
        Record firstRecord = records.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        String query = this.getRecordInsertSQL(tenantId, tableName);
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(query);
            for (Record record : records) {
                stmt.setString(1, record.getId());
                stmt.setLong(2, record.getTimestamp());
                stmt.setBlob(3, new ByteArrayInputStream(GenericUtils.encodeRecordValues(record.getValues())));
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw e;
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    private String getRecordInsertSQL(long tableCategoryId, String tableName) {
    	String query = this.getQueryConfiguration().getRecordInsertQuery();
    	return translateQueryWithTableInfo(query, tableCategoryId, tableName);
    }

    @Override
    public List<Record> getRecords(int tenantId, String tableName, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(this.getRecordRetrievalQuery(tenantId, tableName));
            if (timeFrom == -1) {
                timeFrom = Long.MIN_VALUE;
            }
            if (timeTo == -1) {
                timeTo = Long.MAX_VALUE;
            }
            if (recordsFrom == -1) {
                recordsFrom = 0;
            }
            if (recordsCount == -1) {
                recordsCount = Integer.MAX_VALUE;
            }
            stmt.setLong(1, timeFrom);
            stmt.setLong(2, timeTo);
            stmt.setInt(3, this.adjustRecordsFromForProvider(recordsFrom));
            stmt.setInt(4, this.adjustRecordsCountForProvider(recordsFrom, recordsCount));            
            rs = stmt.executeQuery();
            List<Record> result = this.processRecordResultSet(tenantId, tableName, rs, columns);
            return result;
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in retrieving records: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }
    
    private int adjustRecordsFromForProvider(int recordsFrom) {
        if (!this.getQueryConfiguration().isPaginationFirstZeroIndexed()) {
            recordsFrom++;
        }
        if (!this.getQueryConfiguration().isPaginationFirstInclusive()) {
            recordsFrom++;
        }
        return recordsFrom;
    }
    
    private int adjustRecordsCountForProvider(int recordsFrom, int recordsCount) {
        if (!this.getQueryConfiguration().isPaginationSecondLength() && recordsCount != Integer.MAX_VALUE) {
            if (!this.getQueryConfiguration().isPaginationSecondZeroIndexed()) {
                recordsCount++;
            }
            if (!this.getQueryConfiguration().isPaginationSecondInclusive()) {
                recordsCount++;
            }
        }
        return recordsCount;
    }
    
    private List<Record> processRecordResultSet(int tenantId, String tableName, ResultSet rs, 
            List<String> columns) throws SQLException, AnalyticsException {
        List<Record> result = new ArrayList<Record>();
        Record record;
        Blob blob;
        Map<String, Object> values;
        Set<String> colSet = null;
        if (columns != null && columns.size() > 0) {
            colSet = new HashSet<String>(columns);
        }
        while (rs.next()) {
            blob = rs.getBlob(3);
            values = GenericUtils.decodeRecordValues(blob.getBytes(1, (int) blob.length()), colSet);
            record = new Record(rs.getString(1), tenantId, tableName, values, rs.getLong(2));
            result.add(record);            
        }
        return result;
    }

    @Override
    public List<Record> getRecords(int tenantId, String tableName, List<String> columns,
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        String recordGetSQL = this.generateGetRecordRetrievalWithIdQuery(tenantId, tableName, ids.size());
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(recordGetSQL);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            rs = stmt.executeQuery();
            List<Record> result = this.processRecordResultSet(tenantId, tableName, rs, columns);
            return result;
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in retrieving records: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }
    
    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String sql = this.getRecordDeletionQuery(tenantId, tableName);
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            if (timeFrom == -1) {
                timeFrom = Long.MIN_VALUE;
            }
            if (timeTo == -1) {
                timeTo = Long.MAX_VALUE;
            }
            stmt.setLong(1, timeFrom);
            stmt.setLong(2, timeTo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in deleting records: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, conn);
        }
    }
        
    @Override
    public void delete(int tenantId, String tableName, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (ids.size() == 0) {
            return;
        }
        Connection conn = null;
        try {
            conn = this.getConnection();
            this.delete(conn, tenantId, tableName, ids);
        } catch (SQLException e) {
            throw new AnalyticsException("Error in deleting records: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private void deleteRecordsSimilar(Connection conn, List<Record> records) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (records.size() == 0) {
            return;
        }
        Record firstRecord = records.get(0);
        int tenantId = firstRecord.getTenantId();
        String tableName = firstRecord.getTableName();
        List<String> ids = new ArrayList<String>(records.size());
        for (Record record : records) {
            ids.add(record.getId());
        }
        this.delete(conn, tenantId, tableName, ids);
    }
    
    private void delete(Connection conn, int tenantId, String tableName, 
            List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        String sql = this.generateRecordDeletionRecordsWithIdsQuery(tenantId, tableName, ids.size());
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            } else {
                throw new AnalyticsException("Error in deleting records: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    private String generateTablePrefix(long tableCategoryId) {
        return RDBMSAnalyticsDataSource.ANALYTICS_USER_TABLE_PREFIX + "_" + tableCategoryId + "_";
    }
    
    private String generateTargetTableName(long tableCategoryId, String tableName) {
        return this.normalizeTableName(this.generateTablePrefix(tableCategoryId) + tableName);
    }
    
    private String translateQueryWithTableInfo(String query, long tableCategoryId, String tableName) {
        return query.replace(TABLE_NAME_PLACEHOLDER, this.generateTargetTableName(tableCategoryId, tableName));
    }
    
    private String translateQueryWithRecordIdsInfo(String query, int recordCount) {
        return query.replace(RECORD_IDS_PLACEHOLDER, this.getDynamicSQLParams(recordCount));
    }
    
    private String getRecordRetrievalQuery(long tableCategoryId, String tableName) {
        String query = this.getQueryConfiguration().getRecordRetrievalQuery();
        return this.translateQueryWithTableInfo(query, tableCategoryId, tableName);
    }
    
    private String generateGetRecordRetrievalWithIdQuery(long tableCategoryId, String tableName, int recordCount) {
        String query = this.getQueryConfiguration().getRecordRetrievalWithIdsQuery();
        query = this.translateQueryWithTableInfo(query, tableCategoryId, tableName);
        query = this.translateQueryWithRecordIdsInfo(query, recordCount);
        return query;
    }
    
    private String generateRecordDeletionRecordsWithIdsQuery(long tableCategoryId, String tableName, int recordCount) {
        String query = this.getQueryConfiguration().getRecordDeletionWithIdsQuery();
        query = this.translateQueryWithTableInfo(query, tableCategoryId, tableName);
        query = this.translateQueryWithRecordIdsInfo(query, recordCount);
        return query;
    }
    
    private String getDynamicSQLParams(int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) {
            if (i == 0) {
                builder.append("?");
            } else {
                builder.append(",?");
            }
        }
        return builder.toString();
    }
    
    private String getRecordDeletionQuery(long tableCategoryId, String tableName) {
        String query = this.getQueryConfiguration().getRecordDeletionQuery();
        return this.translateQueryWithTableInfo(query, tableCategoryId, tableName);
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            String[] tableInitQueries = this.getRecordTableDeleteQueries(tenantId, tableName);
            for (String query : tableInitQueries) {
                query = this.translateQueryWithTableInfo(query, tenantId, tableName);
                this.executeUpdate(conn, query);
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            if (this.tableExists(tenantId, tableName)) {
                throw new AnalyticsException("Error in deleting table: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }

    @Override
    public AnalyticsFileSystem getFileSystem() throws IOException {
        return new RDBMSFileSystem(this.getQueryConfiguration(), this.getDataSource());
    }
    
    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        Connection conn = null;
        try {
            conn = this.getConnection(false);
            String[] tableInitQueries = this.getRecordTableInitQueries(tenantId, tableName);
            for (String query : tableInitQueries) {
                this.executeUpdate(conn, query);
            }
            conn.commit();
        } catch (SQLException e) {
            RDBMSUtils.rollbackConnection(conn);
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsException("Error in creating table: " + e.getMessage(), e);
            }
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private void executeUpdate(Connection conn, String query) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query);
        } finally {
            RDBMSUtils.cleanupConnection(null, stmt, null);
        }
    }
    
    @SuppressWarnings("resource")
    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        tableName = this.normalizeTableName(tableName);
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            DatabaseMetaData dbm = conn.getMetaData();
            String prefix = this.normalizeTableName(this.generateTablePrefix(tenantId));
            String srcTable;
            rs = dbm.getTables(null, null, "%", null);
            while (rs.next()) {
                srcTable = rs.getString("TABLE_NAME");
                if (srcTable.startsWith(prefix)) {
                    srcTable = srcTable.substring(prefix.length());
                    srcTable = this.normalizeTableName(srcTable);
                }
                if (tableName.equals(srcTable)) {
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new AnalyticsException("Error in checking table existence: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, null, conn);
        }
    }

    private String normalizeTableName(String tableName) {
        return tableName.toUpperCase();
    }
    
    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        List<String> result = new ArrayList<String>();
        Connection conn = null;
        ResultSet rs = null;
        String tableName;
        String prefix = this.normalizeTableName(this.generateTablePrefix(tenantId));
        try {
            conn = this.getConnection();
            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, "%", null);
            while (rs.next()) {
                tableName = rs.getString("TABLE_NAME");
                if (tableName.startsWith(prefix)) {
                    tableName = tableName.substring(prefix.length());
                    tableName = this.normalizeTableName(tableName);
                    result.add(tableName);
                }
            }
            return result;
        } catch (SQLException e) {
            throw new AnalyticsException("Error in listing tables: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, null, conn);
        }
    }

    private String getRecordCountQuery(long tableCategoryId, String tableName) {
        String query = this.getQueryConfiguration().getRecordCountQuery();
        return this.translateQueryWithTableInfo(query, tableCategoryId, tableName);
    }
    
    private String printableTableName(long tableCategoryId, String tableName) {
        return "[" + tableCategoryId + ":" + tableName + "]";
    }
    
    @Override
    public long getRecordCount(int tenantId, String tableName) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        String recordCountQuery = this.getRecordCountQuery(tenantId, tableName);
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = this.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(recordCountQuery);
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new AnalyticsException("Record count not available for " + 
                        printableTableName(tenantId, tableName));
            }
        } catch (SQLException e) {
            if (!this.tableExists(tenantId, tableName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            }
            throw new AnalyticsException("Error in retrieving record count: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(rs, stmt, conn);
        }
    }

}
