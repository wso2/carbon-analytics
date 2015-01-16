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

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.rdbms.QueryConfigurationEntry;
import org.wso2.carbon.analytics.datasource.rdbms.RDBMSAnalyticsRecordStore;

/**
 * H2 implementation of analytics data source tests.
 */
public class H2MemDBAnalyticsRecordStoreTest extends AnalyticsRecordStoreTest {

    @BeforeSuite
    public void setup() throws NamingException, AnalyticsException {
        this.initDS("jdbc:h2:mem:bam_test_db", "wso2carbon", "wso2carbon");
        RDBMSAnalyticsRecordStore ars = new RDBMSAnalyticsRecordStore(this.generateQueryConfiguration());
        Map<String, String> props = new HashMap<String, String>();
        props.put("datasource", "DS");
        ars.init(props);
        this.init("H2MemDBAnalyticsDataSource", ars);
    }
    
    private void initDS(String url, String username, String password) throws NamingException {
        PoolProperties pps = new PoolProperties();
        pps.setDriverClassName("org.h2.Driver");
        pps.setUrl(url);
        pps.setUsername(username);
        pps.setPassword(password);
        DataSource dsx = new DataSource(pps);
        new InitialContext().bind("DS", dsx);
    }
    
    private QueryConfigurationEntry generateQueryConfiguration() {
        QueryConfigurationEntry conf = new QueryConfigurationEntry();
        String[] recordTableInitQueries = new String[2];
        recordTableInitQueries[0] = "CREATE TABLE {{TABLE_NAME}} (record_id VARCHAR(50), timestamp BIGINT, data BLOB, PRIMARY KEY(record_id))";
        recordTableInitQueries[1] = "CREATE INDEX {{TABLE_NAME}}_TIMESTAMP ON {{TABLE_NAME}} (timestamp)";
        String[] recordTableDeleteQueries = new String[2];
        recordTableDeleteQueries[0] = "DROP TABLE IF EXISTS {{TABLE_NAME}}";
        recordTableDeleteQueries[1] = "DROP INDEX IF EXISTS {{TABLE_NAME}}_TIMESTAMP";        
        conf.setRecordTableInitQueries(recordTableInitQueries);
        conf.setRecordTableDeleteQueries(recordTableDeleteQueries);
        conf.setRecordInsertQuery("INSERT INTO {{TABLE_NAME}} (record_id, timestamp, data) VALUES (?, ?, ?)");
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
