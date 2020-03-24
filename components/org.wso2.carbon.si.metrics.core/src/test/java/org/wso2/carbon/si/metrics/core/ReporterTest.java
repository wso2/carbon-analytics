/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.si.metrics.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.Metrics;
import org.wso2.carbon.si.metrics.core.util.TestConstants;
import org.wso2.carbon.si.metrics.core.util.TestUtils;

/**
 * Test Cases for Reporters.
 */

public class ReporterTest {

    private static final Logger logger = LoggerFactory.getLogger(ReporterTest.class);
    protected static Metrics metrics;
    protected static MetricManagementService metricManagementService;

    @BeforeSuite
    protected static void init() throws ConfigurationException {
        metrics = new Metrics(TestUtils.getConfigProvider(TestConstants.PROMETHEUS_CONFIG_FILE_NAME));
        metrics.activate();
        metricManagementService = metrics.getMetricManagementService();
    }

    @AfterSuite
    protected static void destroy() throws Exception {
        logger.info("Deactivating Metrics");
        metrics.deactivate();
    }

    @BeforeClass
    private void stopReporters() {
        metricManagementService.stopReporters();
    }

    @Test
    public void testInvalidReporter() {
        try {
            metricManagementService.startReporter("INVALID");
            Assert.fail("The reporter should not be started");
        } catch (Exception e) {
            Assert.assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testPrometheusReporter() throws InterruptedException {
        metricManagementService.startReporter(TestConstants.PROMETHEUS_REPORTER_NAME);
        Assert.assertTrue(metricManagementService.isReporterRunning(TestConstants.PROMETHEUS_REPORTER_NAME));
        metricManagementService.report();
        Thread.sleep(10000);
        metricManagementService.stopReporter(TestConstants.PROMETHEUS_REPORTER_NAME);
        Assert.assertFalse(metricManagementService.isReporterRunning(TestConstants.PROMETHEUS_REPORTER_NAME));
    }
}
