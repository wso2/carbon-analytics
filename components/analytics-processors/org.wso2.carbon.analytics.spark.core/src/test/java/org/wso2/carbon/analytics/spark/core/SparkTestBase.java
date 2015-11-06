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

package org.wso2.carbon.analytics.spark.core;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.internal.SparkAnalyticsExecutor;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This is the test base for testing custom UDFs supported by DAS.
 */

public abstract class SparkTestBase {

    protected AnalyticsDataService service;

    @BeforeClass public void setup() throws NamingException, AnalyticsException, IOException {
        GenericUtils.clearGlobalCustomDataSourceRepo();
        System.setProperty(GenericUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf1");
        AnalyticsServiceHolder.setHazelcastInstance(null);
        AnalyticsServiceHolder.setAnalyticsClusterManager(new AnalyticsClusterManagerImpl());
        System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
        this.service = ServiceHolder.getAnalyticsDataService();
        ServiceHolder.setAnalyticskExecutor(new SparkAnalyticsExecutor("localhost", 0));
        ServiceHolder.getAnalyticskExecutor().initializeSparkServer();
    }

    @AfterClass public void done() throws NamingException, AnalyticsException, IOException {
        ServiceHolder.getAnalyticskExecutor().stop();
        this.service.destroy();
        System.clearProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP);
    }

    /**
     * Provides a way to format the test messages.
     *
     * @param str the message to print.
     * @return formattedString which would be printed.
     */
    protected String testString(String str) {
        return "\n************** " + str.toUpperCase() + " **************\n";
    }

    /**
     * Generates a given number of dummy log records.
     *
     * @param tenantId      tenant ID for the records.
     * @param tableName     table Name for the records.
     * @param time          timestamp of the event.
     * @param errorMessages number of error messages required.
     * @param infoMessages  number of info messages required.
     * @return a list of Records with the specified details.
     */
    protected List<Record> generateRecords(int tenantId, String tableName, long time, int errorMessages,
            int infoMessages) {
        List<Record> result = new ArrayList<Record>();

        for (int i = 0; i < errorMessages; i++) {
            result.add(generateRecord(tenantId, tableName, "ERROR", "[ERROR] /get failed for tenant:" + tenantId, time,
                    true));
        }
        for (int i = 0; i < infoMessages; i++) {
            result.add(generateRecord(tenantId, tableName, "INFO", "[INFO] the request success for tenant:" + tenantId,
                    time, true));
        }
        return result;
    }

    /**
     * Generates a given number of dummy log records with facets.
     *
     * @param tenantId      tenant ID for the records.
     * @param tableName     table Name for the records.
     * @param time          timestamp of the event.
     * @param errorMessages number of error messages required.
     * @param infoMessages  number of info messages required.
     * @return a list of Records with the specified details.
     */
    protected List<Record> generateRecordsWithFacets(int tenantId, String tableName, long time, int errorMessages,
            int infoMessages) {
        List<Record> result = new ArrayList<Record>();

        for (int i = 0; i < errorMessages; i++) {
            result.add(
                    generateRecordWithFacets(tenantId, tableName, "ERROR", "[ERROR] /get failed for tenant:" + tenantId,
                            time, true));
        }
        for (int i = 0; i < infoMessages; i++) {
            result.add(generateRecordWithFacets(tenantId, tableName, "INFO",
                    "[INFO] the request success for tenant:" + tenantId, time, true));
        }
        return result;
    }

    /**
     * generates an random record.
     *
     * @param tenantId          tenant ID for the records.
     * @param tableName         table Name for the records.
     * @param logLevel          of the log ( INFO, ERROR, WARN).
     * @param message           log message.
     * @param time              timestamp of the message.
     * @param generateRecordIds should the record ID get generated or not.
     * @return a Record with the specified details.
     */
    protected Record generateRecord(int tenantId, String tableName, String logLevel, String message, long time,
            boolean generateRecordIds) {

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("log_level", logLevel);
        values.put("tenant", tenantId);
        values.put("message", "Important syslog with tenant ID: " + tenantId + "and message:" +
                message);
        long timeTmp;
        if (time != -1) {
            timeTmp = time;
        } else {
            timeTmp = System.currentTimeMillis();
        }
        return new Record(generateRecordIds ? GenericUtils.generateRecordID() : null, tenantId, tableName, values,
                timeTmp);
    }

    /**
     * generates an random record with facets.
     *
     * @param tenantId
     * @param tableName
     * @param logLevel          of the log ( INFO, ERROR, WARN).
     * @param message           log message.
     * @param time              timestamp of the message.
     * @param generateRecordIds should the record ID get generated or not.
     * @return a Record with the specified details.
     */
    protected Record generateRecordWithFacets(int tenantId, String tableName, String logLevel, String message,
            long time, boolean generateRecordIds) {

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("log_level", logLevel);
        values.put("tenant", tenantId);
        values.put("message", "Important syslog with tenant ID: " + tenantId + "and message:" +
                message);
        values.put("composite", "[" + logLevel + "," + tenantId + "]");

        long timeTmp;
        if (time != -1) {
            timeTmp = time;
        } else {
            timeTmp = System.currentTimeMillis();
        }
        return new Record(generateRecordIds ? GenericUtils.generateRecordID() : null, tenantId, tableName, values,
                timeTmp);
    }

}
