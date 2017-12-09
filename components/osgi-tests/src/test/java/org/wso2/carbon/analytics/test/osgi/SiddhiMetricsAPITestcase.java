/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.test.osgi;

import com.google.gson.Gson;
import org.awaitility.Duration;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.test.osgi.util.HTTPResponseMessage;
import org.wso2.carbon.analytics.test.osgi.util.TestUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.datasource.core.api.DataSourceManagementService;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.metrics.core.MetricManagementService;
import org.wso2.carbon.metrics.core.MetricService;
import org.wso2.carbon.siddhi.store.api.rest.ApiResponseMessage;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;
import org.wso2.carbon.stream.processor.statistics.bean.WorkerMetrics;
import org.wso2.carbon.stream.processor.statistics.bean.WorkerStatistics;
import org.wso2.msf4j.MicroservicesRegistry;

import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Paths;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.wso2.carbon.container.options.CarbonDistributionOption.carbonDistribution;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

/**
 * SiddhiAsAPI OSGI Tests.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SiddhiMetricsAPITestcase {

    private static final org.apache.log4j.Logger log =
            org.apache.log4j.Logger.getLogger(SiddhiMetricsAPITestcase.class);
    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private static final String CARBON_YAML_FILENAME = "deployment.yaml";
    private static final String APP_NAME = "MetricsTestApp";
    private static final String SIDDHI_EXTENSION = ".siddhi";

    private Gson gson = new Gson();

    @Inject
    private MicroservicesRegistry microservicesRegistry;

    @Inject
    private SiddhiAppRuntimeService siddhiAppRuntimeService;

    @Inject
    private EventStreamService eventStreamService;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Configuration
    public Option[] createConfiguration() {
        log.info("Running - " + this.getClass().getName());
        return new Option[]{
                copyCarbonYAMLOption(),
                copySiddhiFileOption(),
                carbonDistribution(Paths.get("target", "wso2das-" + System.getProperty("carbon.analytic.version")),
                        "worker")
        };
    }

    /**
     * Replace the existing deployment.yaml file with populated deployment.yaml file.
     */
    private Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "conf", "metrics", CARBON_YAML_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "worker", CARBON_YAML_FILENAME));
    }

    /**
     * Copy Siddhi file to deployment directory in runtime.
     */
    private Option copySiddhiFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "deployment", "siddhi-files",
                APP_NAME + SIDDHI_EXTENSION);
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "worker", "deployment", "siddhi-files",
                APP_NAME + SIDDHI_EXTENSION));
    }

    //Server is started with statistics enabled from the deployment.yaml. So we need to test re-enabling.
    @Test
    public void testReEnableMetricsFirstTime() throws Exception {
        enableMetrics();
    }

    //Disable statistics for further testing
    @Test(dependsOnMethods = "testReEnableMetricsFirstTime")
    public void testDisableMetricsForFirstTime() throws Exception {
        disableMetrics();
    }

    @Test(dependsOnMethods = "testDisableMetricsForFirstTime")
    public void testEnableMetrics() throws Exception {
        HTTPResponseMessage httpResponseMessage = switchMetricsAndGetResponse(true);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        ApiResponseMessage msg = gson.fromJson((String) httpResponseMessage.getSuccessContent(), ApiResponseMessage
                .class);
        Assert.assertEquals(msg.getMessage(), "Successfully enabled the metrics.");
    }

    private HTTPResponseMessage switchMetricsAndGetResponse(boolean enableStats) {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/statistics";
        String contentType = "application/json";
        String method = "PUT";
        TestUtil.waitForAppDeployment(siddhiAppRuntimeService, eventStreamService, APP_NAME, Duration.TEN_SECONDS);
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        return TestUtil.sendHRequest("{\"statsEnable\":" + enableStats + "}", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
    }

    @Test(dependsOnMethods = "testEnableMetrics")
    public void testGetRealTimeStatistics() throws Exception {
        HTTPResponseMessage httpResponseMessage = getRealTimeStatsAndReturnResponse();
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        WorkerStatistics workerStatistics =
                gson.fromJson(httpResponseMessage.getSuccessContent().toString(), WorkerStatistics.class);
        WorkerMetrics workerMetrics = workerStatistics.getWorkerMetrics();
        Assert.assertTrue(workerMetrics.getLoadAverage() > 0);
        Assert.assertTrue(workerMetrics.getProcessCPU() > 0);
        Assert.assertTrue(workerMetrics.getSystemCPU() > 0);
        Assert.assertTrue(workerMetrics.getTotalMemory() > 0);
        Assert.assertTrue(workerStatistics.getClusterID().equalsIgnoreCase("Non Clusters"));
        Assert.assertTrue(workerStatistics.getRunningStatus().equalsIgnoreCase("Reachable"));
        Assert.assertTrue(workerStatistics.isStatsEnabled());
        Assert.assertTrue(!workerStatistics.isInSync());
        Assert.assertTrue(workerStatistics.getOsName() != null);
        Assert.assertTrue(workerStatistics.getLastSnapshotTime() != null);
        Assert.assertTrue(workerStatistics.getLastSyncTime() != null);
    }

    @Test(dependsOnMethods = "testGetRealTimeStatistics")
    public void testReEnableMetrics() throws Exception {
        enableMetrics();
    }

    private void enableMetrics() throws InterruptedException {
        HTTPResponseMessage httpResponseMessage = switchMetricsAndGetResponse(true);
        Thread.sleep(100);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        ApiResponseMessage msg = gson.fromJson((String) httpResponseMessage.getSuccessContent(), ApiResponseMessage
                .class);
        Assert.assertEquals(msg.getMessage(), "Metrics are enabled already.");
    }

    @Test(dependsOnMethods = "testReEnableMetrics")
    public void testSystemDetails() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/system-details";
        String method = "GET";
        String contentType = "application/json";
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(" ", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = "testSystemDetails")
    public void testDisableMetrics() throws Exception {
        disableMetrics();
    }

    private void disableMetrics() {
        HTTPResponseMessage httpResponseMessage = switchMetricsAndGetResponse(false);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        ApiResponseMessage msg = gson.fromJson((String) httpResponseMessage.getSuccessContent(), ApiResponseMessage
                .class);
        Assert.assertEquals(msg.getMessage(), "Sucessfully disabled the metrics.");
    }

    @Test(dependsOnMethods = "testDisableMetrics")
    public void testReDisableMetrics() throws Exception {
        HTTPResponseMessage httpResponseMessage = switchMetricsAndGetResponse(false);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        ApiResponseMessage msg = gson.fromJson((String) httpResponseMessage.getSuccessContent(), ApiResponseMessage
                .class);
        Assert.assertEquals(msg.getMessage(), "Metrics are disabled already.");
    }

    @Test(dependsOnMethods = "testReDisableMetrics")
    public void testGetRealTimeStatisticsAfterDisableStats() throws Exception {
        HTTPResponseMessage httpResponseMessage = getRealTimeStatsAndReturnResponse();
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        ApiResponseMessage msg = gson.fromJson((String) httpResponseMessage.getSuccessContent(), ApiResponseMessage
                .class);
        Assert.assertTrue(msg.getMessage().equals("WSO2 Carbon metrics is not enabled.") ||
                msg.getMessage().equals("MX reporter has been disabled at WSO2 carbon metrics."));
    }

    private HTTPResponseMessage getRealTimeStatsAndReturnResponse() {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/statistics";
        String method = "GET";
        String contentType = "application/json";
        return TestUtil.sendHRequest(" ", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
    }


}
