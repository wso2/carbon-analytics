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

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.rdbms.RDBMSAnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.rdbms.RDBMSQueryConfigurationEntry;

/**
 * H2 implementation of analytics data source tests.
 */
public class H2MemDBAnalyticsRecordStoreTest extends AnalyticsRecordStoreTest {
    
    private DataSource dataSource;
    
    private AnalyticsRecordStore ars;
        
    @BeforeClass
    public void setup() throws NamingException, AnalyticsException {
        this.dataSource = this.createDataSource("jdbc:h2:mem:bam_test_ars_db", "wso2carbon", "wso2carbon");
        new InitialContext().bind("DSRS", this.dataSource);
        this.ars = new RDBMSAnalyticsRecordStore(this.generateQueryConfiguration());
        Map<String, String> props = new HashMap<String, String>();
        props.put("datasource", "DSRS");
        this.ars.init(props);
        this.init("H2InMemoryDBAnalyticsDataSource", ars);
    }
    
    public AnalyticsRecordStore getARS() {
        return this.ars;
    }
    
    @AfterClass
    public void destroy() throws AnalyticsException {
        this.cleanup();
        try {
            new InitialContext().unbind("DSRS");
        } catch (NamingException ignore) { }
        if (this.dataSource != null) {
            this.dataSource.close(true);
        }
    }
    
    private DataSource createDataSource(String url, String username, String password) {
        PoolProperties pps = new PoolProperties();
        pps.setDriverClassName("org.h2.Driver");
        pps.setUrl(url);
        pps.setUsername(username);
        pps.setPassword(password);
        pps.setDefaultAutoCommit(false);
        return new DataSource(pps);
    }
    
    private RDBMSQueryConfigurationEntry generateQueryConfiguration() {
        RDBMSQueryConfigurationEntry conf = new RDBMSQueryConfigurationEntry();
        String[] recordTableInitQueries = new String[2];
        recordTableInitQueries[0] = "CREATE TABLE {{TABLE_NAME}} (record_id VARCHAR(50), timestamp BIGINT, data BLOB, PRIMARY KEY(record_id))";
        recordTableInitQueries[1] = "CREATE INDEX {{TABLE_NAME}}_TIMESTAMP ON {{TABLE_NAME}} (timestamp)";
        String[] recordTableDeleteQueries = new String[2];
        recordTableDeleteQueries[0] = "DROP TABLE IF EXISTS {{TABLE_NAME}}";
        recordTableDeleteQueries[1] = "DROP INDEX IF EXISTS {{TABLE_NAME}}_TIMESTAMP";        
        conf.setRecordTableInitQueries(recordTableInitQueries);
        conf.setRecordTableDeleteQueries(recordTableDeleteQueries);
        conf.setRecordInsertQuery("INSERT INTO {{TABLE_NAME}} (timestamp, data, record_id) VALUES (?, ?, ?)");
        conf.setRecordUpdateQuery("UPDATE {{TABLE_NAME}} SET timestamp = ?, data = ? WHERE record_id = ?");
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
