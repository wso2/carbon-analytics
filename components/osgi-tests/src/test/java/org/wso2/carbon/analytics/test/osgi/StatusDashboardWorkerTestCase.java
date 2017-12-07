/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.analytics.test.osgi;

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
import org.wso2.carbon.datasource.core.beans.DataSourceMetadata;
import org.wso2.carbon.datasource.core.exception.DataSourceException;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;
import org.wso2.msf4j.MicroservicesRegistry;

import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.carbonDistribution;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

/**
 * Test class for test the org.wso2.carbon.status.dashboard.core module.
 */
@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class StatusDashboardWorkerTestCase {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SiddhiMetricsAPITestcase.class);

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    @Inject
    private DataSourceManagementService dataSourceManagementService;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private MicroservicesRegistry microservicesRegistry;

    @Inject
    private SiddhiAppRuntimeService siddhiAppRuntimeService;

    @Inject
    private EventStreamService eventStreamService;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[] {
                copyOSGiLibBundle(maven().artifactId("h2").groupId("com.h2database").version("1.4.195")),
                CarbonDistributionOption.carbonDistribution(
                        maven().groupId("org.wso2.carbon.analytics")
                                .artifactId("org.wso2.carbon.analytics.test.distribution")
                                .type("zip").versionAsInProject()),
                copyOSGiLibBundle(maven()
                        .artifactId("org.wso2.carbon.status.dashboard.core")
                        .groupId("org.wso2.carbon.analytics")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven()
                        .artifactId("org.wso2.carbon.analytics.permissions")
                        .groupId("org.wso2.carbon.analytics-common")
                        .versionAsInProject()),
                copyDSConfigFile()
        };
    }
    private static Option copyDSConfigFile() {
        return copyFile(Paths.get("src", "test", "resources", "conf", "deployment.yaml"),
                Paths.get("conf", "default", "deployment.yaml"));
    }

    @Test
    public void testDashboardCoreApis() throws Exception {
        URI baseURI = URI.create(String.format("https://%s:%d", "localhost", 9443));
        String path = "/monitoring/apis/workers";
        String contentType = "application/json";
        String method = "POST";
        logger.info("Add a worker");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        HTTPResponseMessage httpResponseMessage = TestUtil
                .sendHRequest("{\"port\":9443\n , \"host\":\"localhost\"}", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        logger.info(httpResponseMessage.getMessage());
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        method = "GET";
        logger.info("/monitoring/apis/workers");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        method = "GET";
        logger.info("/monitoring/apis/workers/roles");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/config";
        method = "GET";
        logger.info("Get dashboard configs");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/system-details";
        method = "GET";
        logger.info("Get worker general details");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        testValidSiddhiAPPDeployment();
        TestUtil.waitForAppDeployment(siddhiAppRuntimeService, eventStreamService, "CoreTestApp", Duration.TEN_SECONDS);

        path = "/monitoring/apis/workers/localhost_9443/history";
        method = "GET";
        logger.info("Get worker history");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/history?more=true";
        method = "GET";
        logger.info("Get worker history");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/history";
        method = "GET";
        logger.info("Get siddhi app history");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp";
        method = "GET";
        logger.info("Get siddhi app text");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/statistics";
        method = "PUT";
        logger.info("Get siddhi app statistics");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("{\"statsEnable\":" + true + "}", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/components";
        method = "GET";
        logger.info("Get siddhi app components list");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps";
        method = "GET";
        logger.info("Get siddhi app all list");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/components/streams/cseEventStream/history";
        method = "GET";
        logger.info("Get siddhi app history");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/ha-status";
        method = "GET";
        logger.info("Get worker HA status");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp";
        method = "GET";
        logger.info("Get siddhi app");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp";
        method = "GET";
        logger.info("Get siddhi app text");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443";
        method = "DELETE";
        logger.info("Delete worker workers");
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, path, Duration.TEN_SECONDS);
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

    }


    public void testValidSiddhiAPPDeployment() throws Exception {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('CoreTestApp')" +
                "@app:statistics(reporter = 'jdbc', interval = '2' )" +
                " " +
                "define stream cseEventStream (symbol string, price float, volume int);" +
                "define stream cseEventStream2 (symbol string, price float, volume int);" +
                "" +
                "@info(name = 'query1') " +
                "from cseEventStream[70 > price] " +
                "select * " +
                "insert into outputStream ;" +
                "" +
                "@info(name = 'query2') " +
                "from cseEventStream[volume > 90] " +
                "select * " +
                "insert into outputStream ;";

        logger.info("Deploying valid Siddhi App through REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");

        Thread.sleep(10000);
    }
}
