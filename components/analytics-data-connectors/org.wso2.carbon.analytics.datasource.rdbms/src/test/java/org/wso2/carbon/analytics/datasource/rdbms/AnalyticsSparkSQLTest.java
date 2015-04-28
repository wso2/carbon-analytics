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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.*;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.internal.SparkAnalyticsExecutor;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;

import javax.naming.NamingException;

import java.io.IOException;
import java.util.List;

/**
 * This class represents tests related to Spark SQL based analytics.
 */
public class AnalyticsSparkSQLTest {

    private AnalyticsDataService service;
    
    @BeforeClass
    public void setup() throws NamingException, AnalyticsException, IOException {
        GenericUtils.clearGlobalCustomDataSourceRepo();
        System.setProperty(GenericUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf1");
        AnalyticsServiceHolder.setHazelcastInstance(null);
        AnalyticsServiceHolder.setAnalyticsClusterManager(new AnalyticsClusterManagerImpl());
        System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
        this.service = ServiceHolder.getAnalyticsDataService();
        ServiceHolder.setAnalyticskExecutor(new SparkAnalyticsExecutor("localhost", 0));
    }
    
    @AfterClass
    public void done() throws NamingException, AnalyticsException, IOException {
        ServiceHolder.getAnalyticskExecutor().stop();        
        this.service.destroy();
        System.clearProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP);
    }
    
    @Test
    public void testExecutionSelectQuery() throws AnalyticsException {
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        ex.executeQuery(1, "define table Log (server_name STRING, "
                + "ip STRING, tenant INTEGER, sequence LONG, summary STRING)");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT ip FROM Log");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        result = ex.executeQuery(1, "SELECT * FROM Log");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        this.service.deleteTable(1, "Log");
    }
    
    @Test
    public void testExecutionInsertQuery() throws AnalyticsException {
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 1000, -1, -1);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        this.service.deleteTable(1, "Log2");
        this.service.deleteTable(1, "Log3");
        ex.executeQuery(1, "define table Log (server_name STRING, "
                + "ip STRING, tenant INTEGER, sequence LONG, summary STRING, log STRING)");
        long start = System.currentTimeMillis();
        ex.executeQuery(1, "define table Log2 (server_name STRING, "
                + "ip STRING, tenant INTEGER, sequence LONG, summary STRING, log STRING, primary key(ip, log))");
        ex.executeQuery(1, "define table Log3 (server_name STRING, "
                + "ip STRING, tenant INTEGER, sequence LONG, summary STRING, log STRING)");
        long end = System.currentTimeMillis();
        System.out.println("* Spark SQL define table time: " + (end - start) + " ms.");
        ex.executeQuery(1, "INSERT INTO Log2 SELECT * FROM Log");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log2");
        Assert.assertEquals(result.getRows().size(), 1000);
        /* with the given composite primary key, it should just update the next insert */
        start = System.currentTimeMillis();
        ex.executeQuery(1, "INSERT INTO Log2 SELECT * FROM Log");
        end = System.currentTimeMillis();
        System.out.println("* Spark SQL insert/update table time: " + (end - start) + " ms.");
        result = ex.executeQuery(1, "SELECT * FROM Log2");
        Assert.assertEquals(result.getRows().size(), 1000);
        /* insert to a table without a primary key */
        ex.executeQuery(1, "INSERT INTO Log3 SELECT * FROM Log");
        result = ex.executeQuery(1, "SELECT * FROM Log3");
        Assert.assertEquals(result.getRows().size(), 1000);
        ex.executeQuery(1, "INSERT INTO Log3 SELECT * FROM Log");
        result = ex.executeQuery(1, "SELECT * FROM Log3");
        Assert.assertEquals(result.getRows().size(), 2000);
        
        this.service.deleteTable(1, "Log");
        this.service.deleteTable(1, "Log2");
        this.service.deleteTable(1, "Log3");
    }
    
    @Test
    public void testExecutionTableAliasQuery() throws AnalyticsException {
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        this.service.deleteTable(1, "ESBLogs");
        this.service.deleteTable(1, "ESBLogsBackup");
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "org_wso2_logs_esb_100", 0, 1200, -1, -1);
        this.service.deleteTable(1, "org_wso2_logs_esb_100");
        this.service.createTable(1, "org_wso2_logs_esb_100");
        this.service.put(records);
        ex.executeQuery(1, "define table org_wso2_logs_esb_100 (server_name STRING, "
                + "ip STRING, tenant INTEGER, sequence LONG, summary STRING, log STRING) as ESBLogs");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM ESBLogs");
        ex.executeQuery(1, "define table ESBLogsBackup (server_name STRING, "
                + "ip STRING, tenant INTEGER, sequence LONG, summary STRING, log STRING)");
        ex.executeQuery(1, "INSERT INTO ESBLogsBackup SELECT * FROM ESBLogs");
        result = ex.executeQuery(1, "SELECT * FROM ESBLogsBackup");
        Assert.assertEquals(result.getRows().size(), 1200);        
        this.service.deleteTable(1, "ESBLogs");
        this.service.deleteTable(1, "ESBLogsBackup");
        this.service.deleteTable(1, "org_wso2_logs_esb_100");
    }
    
}
