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

import org.wso2.carbon.si.metrics.prometheus.reporter.config.PrometheusMetricsConfig;
import org.wso2.carbon.si.metrics.prometheus.reporter.config.PrometheusReporterConfig;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.config.ConfigurationException;

/**
 * Test Cases for {@link PrometheusMetricsConfig}.
 */
public class MetricsConfigTest {

    private static PrometheusMetricsConfig prometheusMetricsConfig;

    @BeforeClass
    private void load() throws ConfigurationException {
        prometheusMetricsConfig = TestUtils.getConfigProvider("metrics-prometheus.yaml")
                .getConfigurationObject(PrometheusMetricsConfig.class);
    }

    @Test
    public void testPrometheusConfigLoad() {
        PrometheusReporterConfig config = prometheusMetricsConfig.getReporting().getPrometheus().iterator().next();
        Assert.assertEquals(config.getName(), "prometheus");
        Assert.assertTrue(config.isEnabled());
        Assert.assertEquals(config.getServerURL(), "http://localhost:9005");
    }
}
