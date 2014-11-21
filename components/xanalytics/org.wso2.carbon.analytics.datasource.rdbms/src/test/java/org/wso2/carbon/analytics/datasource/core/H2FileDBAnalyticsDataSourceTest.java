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
package org.wso2.carbon.analytics.datasource.core;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.wso2.carbon.analytics.datasource.rdbms.QueryConfiguration;
import org.wso2.carbon.analytics.datasource.rdbms.RDBMSAnalyticsDataSource;

/**
 * H2 implementation of analytics data source tests.
 */
public class H2FileDBAnalyticsDataSourceTest extends AnalyticsDataSourceTest {

    @BeforeSuite
    @Parameters({"h2.url", "h2.username", "h2.password"})
    public void setup(String url, String username, 
            String password) throws NamingException, AnalyticsDataSourceException {
        this.initDS(url, username, password);
        RDBMSAnalyticsDataSource ads = new RDBMSAnalyticsDataSource(this.generateQueryConfiguration());
        Map<String, String> props = new HashMap<String, String>();
        props.put("datasource", "DS");
        ads.init(props);
        this.init("H2FileDBAnalyticsDataSource", ads);
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
    
    private QueryConfiguration generateQueryConfiguration() {
        QueryConfiguration conf = new QueryConfiguration();
        String[] initQueries = new String[3];
        initQueries[0] = "CREATE TABLE AN_TABLE_RECORD (record_id VARCHAR(50), table_name VARCHAR(256), timestamp BIGINT, data BLOB, PRIMARY KEY(record_id))";
        initQueries[1] = "CREATE INDEX AN_TABLE_RECORD_TABLE_NAME ON AN_TABLE_RECORD(table_name)";
        initQueries[2] = "CREATE INDEX AN_TABLE_RECORD_TIMESTAMP ON AN_TABLE_RECORD(timestamp)";
        conf.setInitQueries(initQueries);
        conf.setSystemTablesCheckQuery("SELECT record_id FROM AN_TABLE_RECORD WHERE record_id = '0'");
        conf.setRecordInsertQuery("INSERT INTO AN_TABLE_RECORD (record_id, table_name, timestamp, data) VALUES (?, ?, ?, ?)");
        conf.setRecordRetrievalQuery("SELECT record_id, timestamp, data FROM AN_TABLE_RECORD WHERE table_name = ? AND timestamp >= ? AND timestamp < ? LIMIT ?,?");
        conf.setRecordRetrievalWithIdsQuery("SELECT record_id, timestamp, data FROM AN_TABLE_RECORD WHERE table_name = ? AND record_id IN (:record_ids)");
        conf.setRecordDeletionWithIdsQuery("DELETE FROM AN_TABLE_RECORD WHERE table_name = ? AND record_id IN (:record_ids)");
        conf.setRecordDeletionQuery("DELETE FROM AN_TABLE_RECORD WHERE table_name = ? AND timestamp >= ? AND timestamp < ?");
        conf.setPaginationFirstZeroIndexed(true);
        conf.setPaginationFirstInclusive(true);
        conf.setPaginationSecondLength(true);
        return conf;
    }
    
}
