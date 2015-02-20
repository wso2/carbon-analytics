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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;

/**
 * H2 implementation of analytics record store tests.
 */
public class H2FileDBAnalyticsRecordStoreTest extends AnalyticsRecordStoreTest {

    @BeforeSuite
    public void setup() throws NamingException, AnalyticsException, IOException {
        AnalyticsRecordStore ars = cleanupAndCreateARS();
        this.init("H2FileDBAnalyticsDataSource", ars);
    }
    
    public static AnalyticsRecordStore cleanupAndCreateARS() throws NamingException, AnalyticsException {
        String dbPath = System.getProperty("java.io.tmpdir") + File.separator + "bam_test_db";
        deleteFile(dbPath + ".mv.db");
        deleteFile(dbPath + ".trace.db");
        initDS("jdbc:h2:" + dbPath, "wso2carbon", "wso2carbon");
        AnalyticsRecordStore ars = new RDBMSAnalyticsRecordStore(generateQueryConfiguration());
        Map<String, String> props = new HashMap<String, String>();
        props.put("datasource", "DSRS");
        ars.init(props);
        return ars;
    }
    
    private static void deleteFile(String path) {
        new File(path).delete();
    }
    
    private static void initDS(String url, String username, String password) throws NamingException {
        PoolProperties pps = new PoolProperties();
        pps.setDriverClassName("org.h2.Driver");
        pps.setUrl(url);
        pps.setRemoveAbandonedTimeout(10);
        pps.setRemoveAbandoned(true);
        pps.setLogAbandoned(true);
        pps.setUsername(username);
        pps.setPassword(password);
        pps.setDefaultAutoCommit(false);
        DataSource dsx = new DataSource(pps);
        new InitialContext().bind("DSRS", dsx);
    }
    
    private static RDBMSQueryConfigurationEntry generateQueryConfiguration() {
        RDBMSQueryConfigurationEntry conf = new RDBMSQueryConfigurationEntry();
        String[] recordTableInitQueries = new String[2];
        recordTableInitQueries[0] = "CREATE TABLE {{TABLE_NAME}} (record_id VARCHAR(50), timestamp BIGINT, data BLOB, PRIMARY KEY(record_id))";
        recordTableInitQueries[1] = "CREATE INDEX {{TABLE_NAME}}_TIMESTAMP ON {{TABLE_NAME}} (timestamp)";
        String[] recordTableDeleteQueries = new String[2];
        recordTableDeleteQueries[0] = "DROP TABLE IF EXISTS {{TABLE_NAME}}";
        recordTableDeleteQueries[1] = "DROP INDEX IF EXISTS {{TABLE_NAME}}_TIMESTAMP";        
        conf.setRecordTableInitQueries(recordTableInitQueries);
        conf.setRecordTableDeleteQueries(recordTableDeleteQueries);
        conf.setRecordMergeQuery("MERGE INTO {{TABLE_NAME}} (timestamp, data, record_id) KEY (record_id) VALUES (?, ?, ?)");
        conf.setRecordRetrievalQuery("SELECT record_id, timestamp, data FROM {{TABLE_NAME}} WHERE timestamp >= ? AND timestamp < ? LIMIT ?,?");
        conf.setRecordRetrievalWithIdsQuery("SELECT record_id, timestamp, data FROM {{TABLE_NAME}} WHERE record_id IN ({{RECORD_IDS}})");
        conf.setRecordDeletionWithIdsQuery("DELETE FROM {{TABLE_NAME}} WHERE record_id IN ({{RECORD_IDS}})");
        conf.setRecordDeletionQuery("DELETE FROM {{TABLE_NAME}} WHERE timestamp >= ? AND timestamp < ?");
        conf.setRecordCountQuery("SELECT COUNT(*) FROM {{TABLE_NAME}}");
        conf.setPaginationFirstZeroIndexed(true);
        conf.setPaginationFirstInclusive(true);
        conf.setPaginationSecondLength(true);
        return conf;
    }
    
}
