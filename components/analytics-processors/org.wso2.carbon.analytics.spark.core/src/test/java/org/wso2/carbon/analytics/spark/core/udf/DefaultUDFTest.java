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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.internal.SparkAnalyticsExecutor;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;

/**
 * This class tests the default UDFs added to the spark SQL
 */
public class DefaultUDFTest {
	private AnalyticsDataService service;

	@Test
	public void testStringConcatUDF() throws AnalyticsException {
		System.out.println(testString("START : String concat UDF tester"));
		final int INFO_MESSAGES = 5;
		final int ERROR_MESSAGES = 5;

		SparkAnalyticsExecutor ex = ServiceHolder.getAnalyticskExecutor();
        List<Record> records = generateRecords(1, "Log", System.currentTimeMillis(), -1, ERROR_MESSAGES, INFO_MESSAGES);
        this.service.deleteTable(1, "Log");
		this.service.createTable(1, "Log");
		this.service.put(records);
		ex.executeQuery(1,
		                "CREATE TEMPORARY TABLE Log USING CarbonAnalytics "
		                        + "OPTIONS"
		                        + "(tableName \"Log\","
		                        + "schema \"log_level STRING, message STRING, tenant INTEGER\""
		                        + ")");
		AnalyticsQueryResult result = ex.executeQuery(1, "SELECT * FROM Log where log_level = concat('IN','FO')");
		Assert.assertEquals(result.getRows().size(), INFO_MESSAGES);

		result = ex.executeQuery(1, "SELECT * FROM Log where log_level = concat('ER','ROR')");
		Assert.assertEquals(result.getRows().size(), ERROR_MESSAGES);
		this.service.deleteTable(1, "Log");

		System.out.println(testString("END: String concat UDF tester"));
	}

	@BeforeClass
	public void setup() throws NamingException, AnalyticsException, IOException {
		GenericUtils.clearGlobalCustomDataSourceRepo();
		System.setProperty(GenericUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP,
				"src/test/resources/conf1");
		AnalyticsServiceHolder.setHazelcastInstance(null);
		AnalyticsServiceHolder.setAnalyticsClusterManager(new AnalyticsClusterManagerImpl());
		System.setProperty(AnalyticsServiceHolder.FORCE_INDEXING_ENV_PROP, Boolean.TRUE.toString());
		this.service = ServiceHolder.getAnalyticsDataService();
		ServiceHolder.setAnalyticskExecutor(new SparkAnalyticsExecutor("localhost", 0));
		ServiceHolder.getAnalyticskExecutor().initializeSparkServer();
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

	/**
	 * Generates a given number of dummy log records
	 * @param tenantId
	 * @param tableName
	 * @param time
	 * @param timeOffset
	 * @param errorMessages => number of error messages required
	 * @param infoMessages  => number of info messages required
	 * @return
	 */
	private List<Record> generateRecords(int tenantId, String tableName, long time, int timeOffset,int errorMessages,int infoMessages) {
		List<Record> result = new ArrayList<Record>();

        for (int i = 0; i < errorMessages; i++) {
            result.add(generateRecord(tenantId, tableName, "ERROR", "[ERROR] /get failed for tenant:" + tenantId,
                    time, timeOffset, true));
        }
        for (int i = 0; i < infoMessages; i++) {
            result.add(generateRecord(tenantId, tableName, "INFO", "[INFO] the request success for tenant:" + tenantId,
                    time, timeOffset, true));
        }
        return result;
	}
	/**
	 * generates an random record
	 *
	 * @param tenantId
	 * @param tableName
	 * @param logLevel
	 * @param message
	 * @param time
	 * @param timeOffset
	 * @param generateRecordIds
	 * @return
	 */
	private Record generateRecord(int tenantId, String tableName, String logLevel, String message,
	                              long time, int timeOffset, boolean generateRecordIds) {

		Map<String, Object> values = new HashMap<String, Object>();
		values.put("log_level", logLevel);
		values.put("tenant", tenantId);
		values.put("message", "Important syslog with tenant ID: " + tenantId + "and message:" +
		                      message);
		long timeTmp;
		if (time != -1) {
			timeTmp = time;
			time += timeOffset;
		} else {
			timeTmp = System.currentTimeMillis();
		}
		return new Record(generateRecordIds ? GenericUtils.generateRecordID() : null, tenantId,
		                  tableName, values, timeTmp);
	}


}
