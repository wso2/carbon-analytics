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
package org.wso2.carbon.analytics.datasource.rdbms.mysql;

import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceTest;

/**
 * MySQL implementation of analytics data source tests.
 */
public class MySQLInnoDBAnalyticsDataSourceTest extends AnalyticsDataSourceTest {

    @BeforeSuite
    @Parameters({"url", "username", "password"})
    public void setup(String url, String username, 
            String password) throws NamingException, AnalyticsDataSourceException {
        this.initDS(url, username, password);
        MySQLAnalyticsDataSource ads = new MySQLAnalyticsDataSource();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("datasource", "DS");
        props.put("storage_engine", "InnoDB");
        ads.init(props);
        this.init("MySQLInnoDBAnalyticsDataSource", ads);
    }
    
    private void initDS(String url, String username, String password) throws NamingException {
        PoolProperties pps = new PoolProperties();
        pps.setDriverClassName("com.mysql.jdbc.Driver");
        pps.setUrl(url);
        pps.setUsername(username);
        pps.setPassword(password);
        DataSource dsx = new DataSource(pps);
        new InitialContext().bind("DS", dsx);
    }
    
}
