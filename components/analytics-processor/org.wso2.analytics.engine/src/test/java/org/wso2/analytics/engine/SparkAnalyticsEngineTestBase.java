/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.analytics.engine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.analytics.data.commons.AnalyticsEngine;
import org.wso2.analytics.data.commons.AnalyticsEngineQueryResult;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.utils.AnalyticsCommonUtils;
import org.wso2.analytics.engine.core.SparkAnalyticsEngine;
import org.wso2.analytics.engine.exceptions.AnalyticsExecutionException;

public class SparkAnalyticsEngineTestBase {
    private static final Log log = LogFactory.getLog(SparkAnalyticsEngineTestBase.class);

    protected static String sparkConfFilePath = "src/test/resources/conf/spark-defaults.conf";
    protected AnalyticsEngine analyticsEngine;

    @BeforeClass
    public void setup() {
        System.setProperty(AnalyticsCommonUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf");
        this.analyticsEngine = new SparkAnalyticsEngine(sparkConfFilePath);
    }

    /*@Test
    public void getVersionTest() {
        log.info("================== Testing Spark Connectivity through getVersion method =====================");
        String sparkVersion = this.analyticsEngine.getVersion();
        Assert.assertNotNull(sparkVersion);
        log.info("Spark getVersion result: " + sparkVersion);
    }

    @Test(dependsOnMethods = "getVersionTest")
    public void simpleQueryExecutionTest() throws AnalyticsException {
        log.info("================== Testing Simple SPARK SQL query execution =====================");
        AnalyticsEngineQueryResult analyticsEngineQueryResult = this.analyticsEngine.executeQuery("SELECT 1+2;");
        String result = analyticsEngineQueryResult.getRows().get(0).toString();
        Assert.assertEquals(result, "[3]");
    }
*/
    @Test
    public void creatingTablesTest() throws AnalyticsException {
        log.info("================== Creating temporary table Test =====================");
        this.analyticsEngine.executeQuery("CREATE TEMPORARY VIEW person using CarbonAnalytics options (tableName \"PERSON\", schema \"name STRING\");");
//        this.analyticsEngine.executeQuery("INSERT INTO person select 'sachith';");
        AnalyticsEngineQueryResult analyticsEngineQueryResult = this.analyticsEngine.executeQuery("SELECT * FROM person");
        Assert.assertEquals(analyticsEngineQueryResult.getRows().size(), 2);
    }
/*
    @Test(dependsOnMethods = "simpleQueryExecutionTest", expectedExceptions = AnalyticsExecutionException.class)
    public void accessingUnrecognizedTableTest() throws AnalyticsException {
        log.info("================== Accessing an unavailable view Test =====================");
        this.analyticsEngine.executeQuery("SELECT * FROM unrecog");
    }*/
}
