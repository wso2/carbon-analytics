/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.mongo;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import es.eci.carbon.analytics.datasource.mongo.MongoAnalyticsRecordStore;

/**
 * Cassandra analytics record store tests.
 */
public class MongoAnalyticsRecordStoreTest extends AnalyticsRecordStoreTest {

    @BeforeClass
    public void setup() throws NamingException, AnalyticsException, IOException {
        GenericUtils.clearGlobalCustomDataSourceRepo();
        System.setProperty(GenericUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf1");        
        AnalyticsRecordStore ars = new MongoAnalyticsRecordStore();
        Map<String, String> props = new HashMap<String, String>();
        props.put("datasource", "WSO2_ANALYTICS_DS_MONGO");
        props.put("database", "ARS_TEST");
        ars.init(props);
        this.init("MongoAnalyticsDataSource", ars);
    }
    
    @AfterClass
    public void destroy() throws AnalyticsException {
        this.cleanup();
    }
    
}
