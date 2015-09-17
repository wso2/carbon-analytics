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

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import com.google.common.collect.Maps;

/**
 * Utility methods for RDBMS based operations for analytics data source.
 */
public class RDBMSUtils {
    
    private static final String VERSION = "Version";

    private static final String DATABASE_PRODUCT_NAME = "Database Product Name";

    private static final Log log = LogFactory.getLog(RDBMSUtils.class);
    
    private static final String RDBMS_QUERY_CONFIG_FILE = "rdbms-config.xml";
    
    private static RDBMSConfigurationMapper mapper;
    
    public static Map<String, Object> lookupDatabaseInfo(DataSource ds) throws AnalyticsException {
        Connection conn = null;
        try {
            conn = ds.getConnection();
            DatabaseMetaData dmd = conn.getMetaData();
            Map<String, Object> result = new HashMap<String, Object>();
            result.put(DATABASE_PRODUCT_NAME, dmd.getDatabaseProductName());
            result.put(VERSION, Double.parseDouble(dmd.getDatabaseMajorVersion() + "." + dmd.getDatabaseMinorVersion()));
            return result;
        } catch (SQLException e) {
            throw new AnalyticsException("Error in looking up database type: " + e.getMessage(), e);
        } finally {
            RDBMSUtils.cleanupConnection(null, null, conn);
        }
    }
    
    private static RDBMSConfigurationMapper loadRDBMSConfigurationMapper() throws AnalyticsException {
        if (mapper == null) {
            RDBMSQueryConfiguration config = loadQueryConfiguration();
            mapper = new RDBMSConfigurationMapper(config);
        }
        return mapper;
    }
        
    public static RDBMSQueryConfigurationEntry lookupCurrentQueryConfigurationEntry(
            DataSource ds, String category) throws AnalyticsException {
        Map<String, Object> dbInfo = lookupDatabaseInfo(ds);
        RDBMSConfigurationMapper mapper = loadRDBMSConfigurationMapper();
        RDBMSQueryConfigurationEntry entry = mapper.lookupEntry((String) dbInfo.get(DATABASE_PRODUCT_NAME), 
                (double) dbInfo.get(VERSION), category);
        if (entry != null) {
            return entry;
        } else {
            dbInfo.put("Category", category);
            throw new AnalyticsException("Cannot find a database section in the RDBMS "
                    + "configuration for the database: " + dbInfo);
        }
    }
    
    public static RDBMSQueryConfiguration loadQueryConfiguration() throws AnalyticsException {
        try {
            File confFile = new File(GenericUtils.getAnalyticsConfDirectory() + 
                    File.separator + AnalyticsDataSourceConstants.ANALYTICS_CONF_DIR + File.separator + RDBMS_QUERY_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException("Cannot initalize RDBMS analytics data source, "
                        + "the query configuration file cannot be found at: " + confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(RDBMSQueryConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            RDBMSQueryConfiguration conf = (RDBMSQueryConfiguration) unmarshaller.unmarshal(confFile);
            validateRDBMSQueryConfiguration(conf);
            return conf;
        } catch (JAXBException e) {
            throw new AnalyticsException(
                    "Error in processing RDBMS query configuration: " + e.getMessage(), e);
        }
    }
    
    private static void validateRDBMSQueryConfiguration(RDBMSQueryConfiguration conf) throws AnalyticsException {
        for (RDBMSQueryConfigurationEntry entry : conf.getDatabases()) {
            if (isEmpty(entry.getRecordMergeQuery()) && (isEmpty(entry.getRecordInsertQuery()) || 
                    isEmpty(entry.getRecordUpdateQuery()))) {
                throw new AnalyticsException("RDBMS configuration database entry: " + 
                        entry.getDatabaseName() + ", either record merge query or both insert/update queries must be provided");
            }
        }
    }
    
    private static boolean isEmpty(String field) {
        return (field == null || field.trim().length() == 0);
    }
    
    public static void cleanupConnection(ResultSet rs, Statement stmt, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ignore) { /* ignore */ }
        }
    }
    
    public static void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException ignore) { /* ignore */ }
        }
    }
    
    public static void executeAllUpdateQueries(Connection conn, Map<String, Object[]> queries) throws AnalyticsException {
        StringBuilder messages = new StringBuilder();
        PreparedStatement stmt = null;
        for (Map.Entry<String, Object[]> entry : queries.entrySet()) {
            try {
                stmt = conn.prepareStatement(entry.getKey());
                for (int i = 0; i < entry.getValue().length; i++) {
                    stmt.setObject(i + 1, entry.getValue()[i]);
                }
                stmt.execute();
            } catch (SQLException e) {
                messages.append(e.getMessage() + "\n");
            } finally {
                RDBMSUtils.cleanupConnection(null, stmt, null);
            }
        }
        String exs = messages.toString();
        if (exs.length() > 0) {
            if (log.isDebugEnabled()) {
                log.debug("executeAllUpdateQueries exceptions: [" + exs + "]");
            }
            throw new AnalyticsException("Error in executing SQL queries: " + exs);
        }
    }
    
    public static Map<String, Object[]> generateNoParamQueryMap(String[] queries) {
        Map<String, Object[]> result = new LinkedHashMap<String, Object[]>(queries.length);
        for (String query : queries) {
            result.put(query, new Object[0]);
        }
        return result;
    }

    /**
     * RDBMS configuration mapping class to be used to lookup matching configuration entry with a data source.
     */
    private static class RDBMSConfigurationMapper {
        
        private List<Map.Entry<Pattern, RDBMSQueryConfigurationEntry>> entries = 
                new ArrayList<Map.Entry<Pattern, RDBMSQueryConfigurationEntry>>();
        
        public RDBMSConfigurationMapper(RDBMSQueryConfiguration config) {
            for (RDBMSQueryConfigurationEntry entry : config.getDatabases()) {
                this.entries.add(Maps.immutableEntry(Pattern.compile(entry.getDatabaseName().toLowerCase()), entry));
            }
        }
        
        private boolean checkVersion(RDBMSQueryConfigurationEntry entry, double version) {
            double minVersion = entry.getMinVersion();
            double maxVersion = entry.getMaxVersion();
            if (minVersion != 0 && version < minVersion) {
                return false;
            }
            if (maxVersion != 0 && version > maxVersion) {
                return false;
            }
            return true;
        }
        
        private List<RDBMSQueryConfigurationEntry> extractMatchingConfigEntries(String dbName) {
            List<RDBMSQueryConfigurationEntry> result = new ArrayList<RDBMSQueryConfigurationEntry>();
            for (Map.Entry<Pattern, RDBMSQueryConfigurationEntry> entry : this.entries) {
                if (entry.getKey().matcher(dbName).find()) {
                    result.add(entry.getValue());
                }
            }
            return result;
        }
        
        public RDBMSQueryConfigurationEntry lookupEntry(String dbName, double version, String category) {
            List<RDBMSQueryConfigurationEntry> dbResults = this.extractMatchingConfigEntries(dbName.toLowerCase());
            if (dbResults == null || dbResults.isEmpty()) {
                return null;
            }
            List<RDBMSQueryConfigurationEntry> versionResults = new ArrayList<RDBMSQueryConfigurationEntry>();
            for (RDBMSQueryConfigurationEntry entry : dbResults) {
                if (this.checkVersion(entry, version)) {
                    versionResults.add(entry);
                }
            }
            if (versionResults.isEmpty()) {
                return null;
            }
            if (category == null) {
                return versionResults.get(0);
            } else {
                RDBMSQueryConfigurationEntry defaultResult = null;
                /* to put the top most empty category entry as the default one */
                boolean defaultSet = false;
                for (RDBMSQueryConfigurationEntry entry : versionResults) {
                    if (category.equalsIgnoreCase(entry.getCategory())) {
                        return entry;
                    } else if (!defaultSet && entry.getCategory() == null) {
                        defaultResult = entry;
                        defaultSet = true;
                    }
                }
                return defaultResult;
            }
        }
        
    }
        
}
