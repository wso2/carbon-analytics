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

import java.io.IOException;
import java.util.List;

import javax.naming.NamingException;

import junit.framework.Assert;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.spark.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.spark.core.AnalyticsExecutionContext;
import org.wso2.carbon.analytics.spark.core.AnalyticsQueryResult;

/**
 * This class represents tests related to Spark SQL based analytics.
 */
public class AnalyticsSparkSQLTest {

    private AnalyticsDataService service;
    
    @BeforeSuite
    public void setup() throws NamingException, AnalyticsException, IOException {
        AnalyticsRecordStore ars = H2FileDBAnalyticsRecordStoreTest.cleanupAndCreateARS();
        AnalyticsFileSystem afs = H2FileDBAnalyticsFileSystemTest.cleanupAndCreateAFS();
        this.service = new AnalyticsDataServiceImpl(ars, afs, 5);
    }
    
    @AfterSuite
    public void done() throws NamingException, AnalyticsException, IOException {
        this.service.destroy();
    }
    
    @Test
    public void testExecutionContextInit() {
        AnalyticsServiceHolder.setAnalyticsDataService(this.service);
        AnalyticsExecutionContext.init();
    }
    
    @Test (dependsOnMethods = "testExecutionContextInit")
    public void testExecutionSelectQuery() throws AnalyticsException {
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.insert(records);
        AnalyticsExecutionContext.executeQuery(1, "define table Log server_name STRING, "
                + "ip STRING, tenant INTEGER, sequence LONG, summary STRING");
        AnalyticsQueryResult result = AnalyticsExecutionContext.executeQuery(1, "SELECT ip FROM Log");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        result = AnalyticsExecutionContext.executeQuery(1, "SELECT * FROM Log");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        this.service.deleteTable(1, "Log");
        AnalyticsExecutionContext.stop();
    }
    
}
