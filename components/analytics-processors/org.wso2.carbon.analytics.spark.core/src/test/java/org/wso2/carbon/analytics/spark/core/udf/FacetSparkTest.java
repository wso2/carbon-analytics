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

public class FacetSparkTest extends SparkTestBase {

    /**
     * Tests the persistence of facets in the record store.
     *
     * @throws AnalyticsException thrown when service fails to perform operations of Analytics Service.
     */
    @Test
    public void testFacetPersistence() throws AnalyticsException {
        System.out.println(testString("START : Facet Persistence tester"));
        final int INFO_MESSAGES = 10;
        final int ERROR_MESSAGES = 0;
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records =
                generateRecords(1, "Log", System.currentTimeMillis(),
                        ERROR_MESSAGES, INFO_MESSAGES);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        ex.executeQuery(1,
                "CREATE TEMPORARY TABLE Log USING CarbonAnalytics "
                        + "OPTIONS"
                        + "(tableName \"Log\","
                        + " schema \"log_level STRING, message STRING, tenant INTEGER\""
                        + ")");

        ex.executeQuery(1,
                "CREATE TEMPORARY TABLE facetTest USING CarbonAnalytics "
                        + "OPTIONS"
                        + "(tableName \"facetTest\","
                        + " schema \"log_level STRING, message STRING, tenant INTEGER, composite FACET -i\""
                        + ")");

        ex.executeQuery(1, "INSERT INTO TABLE facetTest SELECT log_level,message,tenant," +
                " facet2(tenant,log_level) from Log");

        // testing the facet persistence
        AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * from facetTest where composite = '1,INFO'");
        Assert.assertEquals(result.getRows().size(), INFO_MESSAGES);
        this.service.deleteTable(1, "Log");
        this.service.deleteTable(1, "facetTest");
        System.out.println(testString("END: Facet Persistence tester"));
    }

    /**
     * tests if the Facet UDFs fails if given the wrong number of parameters.
     *
     * @throws AnalyticsException thrown when service fails to perform operations of Analytics Service.
     */
    @Test (expectedExceptions = ClassCastException.class)
    public void testFacetUDFWrongNumberOfParameters() throws AnalyticsException {
        System.out.println(testString("START : Facet UDF fail tester"));
        final int INFO_MESSAGES = 10;
        final int ERROR_MESSAGES = 0;
        SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records =
                generateRecords(1, "Log", System.currentTimeMillis(),
                        ERROR_MESSAGES, INFO_MESSAGES);
        this.service.deleteTable(1, "Log");
        this.service.createTable(1, "Log");
        this.service.put(records);
        ex.executeQuery(1,
                "CREATE TEMPORARY TABLE Log USING CarbonAnalytics "
                        + "OPTIONS"
                        + "(tableName \"Log\","
                        + "schema \"log_level STRING, message STRING, tenant INTEGER, composite FACET -i\""
                        + ")");

        // for a single facet
        ex.executeQuery(1, "SELECT facet3(tenant,log_level) from Log");
        this.service.deleteTable(1, "Log");
        System.out.println(testString("END: Facet UDF fail tester"));

    }
}
