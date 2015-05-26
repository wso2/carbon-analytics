/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.internal;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.List;

public class AnalyticsSparkExecutorTest {


    private AnalyticsDataService service;

    /**
     * TEST PLAN
     * <p/>
     * 1.a create temp table using CarbonAnalytics, schema in the options - happy
     * 1.b create temp table using CarbonAnalytics, schema in the options - happy
     * <p/>
     * 2.a create temp table using CA, schema inline  - happy
     * 2.b create temp table using CA, schema inline  - happy
     * <p/>
     * 3. select queries
     * <p/>
     * 4. insert queries
     * <p/>
     * 5. working multi tenant
     * <p/>
     * 6.a super tenant can access other tenants' tables
     * 6.b other tenants can only access their tables
     * 6.c other tenants can NOT access other tables
     *
     * @throws AnalyticsException
     */
    @Test
    public void testCreateTableQuery() throws AnalyticsException {
        System.out.println(testString("start : create temp table test"));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 10, -1, -1);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        ex.executeQuery(1, "CREATE TEMPORARY TABLE logs USING CarbonAnalytics " +
                           "OPTIONS ( " +
                           "tenantId \"1\", " +
                           "tableName \"Log\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, summary STRING\"," +
                           ")");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT ip FROM logs");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        result = ex.executeQuery(1, "SELECT * FROM logs");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        this.service.deleteTable(1, "Log");
        System.out.println(testString("end : create temp table test"));
    }


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

    private String testString(String str) {
        return "\n************** " + str.toUpperCase() + " **************\n";
    }


}