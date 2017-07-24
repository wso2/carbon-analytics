/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.udf;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.SparkTestBase;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.internal.SparkAnalyticsExecutor;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;

import java.util.List;

/**
 * This class tests the default UDFs added to the spark SQL.
 */
public class DefaultSparkTest extends SparkTestBase {

    @Test public void testStringConcatUDF() throws AnalyticsException {
        System.out.println(testString("START : String concat UDF tester"));
        final int INFO_MESSAGES = 5;
        final int ERROR_MESSAGES = 5;

        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = generateRecords(1, "Log", System.currentTimeMillis(), ERROR_MESSAGES, INFO_MESSAGES);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log USING CarbonAnalytics " + "OPTIONS" + "(tableName \"Log\","
                        + "schema \"log_level STRING, message STRING, tenant INTEGER\"" + ")");
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log where log_level = concat('IN','FO')");
        Assert.assertEquals(result.getRows().size(), INFO_MESSAGES);

        result = ex.executeQuery(1, "SELECT * FROM Log where log_level = concat('ER','ROR')");
        Assert.assertEquals(result.getRows().size(), ERROR_MESSAGES);
        this.service.deleteTable(1, "Log");

        System.out.println(testString("END: String concat UDF tester"));
    }

    @Test public void testTimeNow() throws AnalyticsException, InterruptedException {
        System.out.println(testString("START : now() UDF tester"));
        final int INFO_MESSAGES = 5;
        final int ERROR_MESSAGES = 5;

        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = generateRecords(1, "Log", System.currentTimeMillis(), ERROR_MESSAGES, INFO_MESSAGES);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        ex.executeQuery(1, "CREATE TEMPORARY TABLE Log USING CarbonAnalytics " + "OPTIONS" + "(tableName \"Log\","
                        + "schema \"log_level STRING, message STRING, tenant INTEGER, _timestamp LONG\"" + ")");

        // setting a time difference between the insertion of records and search.
        Thread.sleep(2000);
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log where _timestamp < now('')");
        Assert.assertEquals(result.getRows().size(), INFO_MESSAGES + ERROR_MESSAGES);

        System.out.println(testString("END: now() UDF tester"));
    }
}
