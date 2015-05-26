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
     * 2.a create temp table using CA, schema inline  - NOT WORKING
     * 2.b create temp table using CA, schema inline  - NOT WORKING
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
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, summary STRING\"" +
                           ")");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT ip FROM Log");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        result = ex.executeQuery(1, "SELECT * FROM Log");
        Assert.assertEquals(result.getRows().size(), 10);
        System.out.println(result);
        this.service.deleteTable(1, "Log");
        System.out.println(testString("end : create temp table test"));
    }

    @Test
    public void testExecutionInsertQuery() throws AnalyticsException {
        System.out.println(testString("start : insert table test "));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 1000, -1, -1);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        this.service.deleteTable(1, "Log2");
        this.service.deleteTable(1, "Log3");
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"" +
                           ")");
        long start = System.currentTimeMillis();
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log2 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log2\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"," +
                           "primaryKeys \"ip, log\""+
                           ")");
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log3 USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"Log3\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"" +
                           ")");
        long end = System.currentTimeMillis();
        System.out.println("* Spark SQL define table time: " + (end - start) + " ms.");
        ex.executeQuery(1, "INSERT INTO TABLE Log2 SELECT * FROM Log");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log2");
        Assert.assertEquals(result.getRows().size(), 1000);
        /* with the given composite primary key, it should just update the next insert */
        start = System.currentTimeMillis();
        ex.executeQuery(1, "INSERT INTO TABLE Log2 SELECT * FROM Log");
        end = System.currentTimeMillis();
        System.out.println("* Spark SQL insert/update table time: " + (end - start) + " ms.");
        result = ex.executeQuery(1, "SELECT * FROM Log2");
        Assert.assertEquals(result.getRows().size(), 1000);
        /* insert to a table without a primary key */
        ex.executeQuery(1, "INSERT INTO TABLE Log3 SELECT * FROM Log");
        result = ex.executeQuery(1, "SELECT * FROM Log3");
        Assert.assertEquals(result.getRows().size(), 1000);
        ex.executeQuery(1, "INSERT INTO TABLE Log3 SELECT * FROM Log");
        result = ex.executeQuery(1, "SELECT * FROM Log3");
        Assert.assertEquals(result.getRows().size(), 2000);

        this.service.deleteTable(1, "Log");
        this.service.deleteTable(1, "Log2");
        this.service.deleteTable(1, "Log3");
        System.out.println(testString("end : insert table test "));
    }

    @Test
    public void testMultiTenantQueryExecution() throws AnalyticsException{
        System.out.println(testString("start : multi tenancy test "));
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        String commonTableName = "log";
        List<Record> records1 = AnalyticsRecordStoreTest.generateRecords(-1234, commonTableName, 0, 10, -1, -1);
        List<Record> records2 = AnalyticsRecordStoreTest.generateRecords(2, commonTableName, 0, 2000, -1, -1);

        this.service.deleteTable(-1234, commonTableName);
        this.service.deleteTable(2, commonTableName);
        this.service.createTable(-1234, commonTableName);
        this.service.createTable(2, commonTableName);
        this.service.put(records1);
        this.service.put(records2);

        //test supertenant queries
        ex.executeQuery(-1234, "CREATE TEMPORARY TABLE log USING CarbonAnalytics " +
                           "OPTIONS" +
                           "(tableName \"log\"," +
                           "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"" +
                           ")");
        AnalyticsQueryResult result = ex.executeQuery(-1234, "SELECT * FROM log");
        Assert.assertEquals(result.getRows().size(), 10);

        //test tenant queries
        ex.executeQuery(2, "CREATE TEMPORARY TABLE log USING CarbonAnalytics " +
                               "OPTIONS" +
                               "(tableName \"log\"," +
                               "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"" +
                               ")");
        result = ex.executeQuery(2, "SELECT * FROM log");
        Assert.assertEquals(result.getRows().size(), 2000);

        //test <table>.<column name> queries
        ex.executeQuery(-1234, "CREATE TEMPORARY TABLE log2 USING CarbonAnalytics " +
                               "OPTIONS" +
                               "(tableName \"log\"," +
                               "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"" +
                               ")");
        result = ex.executeQuery(-1234, "SELECT log.ip FROM log UNION SELECT log2.ip FROM log2");
        Assert.assertEquals(result.getRows().size(), 1);

        //test single letter table queries ex; table name = "t"
        ex.executeQuery(-1234, "CREATE TEMPORARY TABLE t USING CarbonAnalytics " +
                               "OPTIONS" +
                               "(tableName \"log\"," +
                               "schema \"server_name STRING, ip STRING, tenant INTEGER, sequence LONG, log STRING\"" +
                               ")");
        result = ex.executeQuery(-1234, "SELECT t.ip FROM t");
        Assert.assertEquals(result.getRows().size(), 10);

        //test queries with table names and tables alias
        result = ex.executeQuery(-1234, "select * from ( select * from log ) t1 full outer join " +
                                        "( select * from log2 ) t2 on t1.ip = t2.ip");
        Assert.assertEquals(result.getRows().size(), 10*10);

        this.service.deleteTable(-1234, "log");
        this.service.deleteTable(-1234, "log2");
        this.service.deleteTable(-1234, "t");
        this.service.deleteTable(-1234, "t1");
        this.service.deleteTable(-1234, "t2");
        this.service.deleteTable(2, "log");
        System.out.println(testString("end : multi tenancy test "));
    }

//    @Test
//    public void testCreateTableQuerySchemaInLine() throws AnalyticsException {
//        System.out.println(testString("start : create temp table test"));
//        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
//        List<Record> records = AnalyticsRecordStoreTest.generateRecords(1, "Log", 0, 10, -1, -1);
//        this.service.deleteTable(1, "Log");
//        this.service.createTable(1, "Log");
//        this.service.put(records);
//        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log (server_name STRING, ip STRING, tenant INTEGER, sequence LONG, summary STRING) " +
//                           "USING CarbonAnalytics " +
//                           "OPTIONS" +
//                           "(tableName \"Log\"" +
//                           ")");
//        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT ip FROM Log");
//        Assert.assertEquals(result.getRows().size(), 10);
//        System.out.println(result);
//        result = ex.executeQuery(1, "SELECT * FROM Log");
//        Assert.assertEquals(result.getRows().size(), 10);
//        System.out.println(result);
//        this.service.deleteTable(1, "Log");
//        System.out.println(testString("end : create temp table test"));
//    }


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