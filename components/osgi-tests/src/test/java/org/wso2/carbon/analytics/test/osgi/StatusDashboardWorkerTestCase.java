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
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;
import org.wso2.msf4j.MicroservicesRegistry;

import javax.inject.Inject;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SiddhiMetricsAPITestcase.class);
    private static final String CARBON_YAML_FILENAME = "deployment.yaml";
    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

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
        log.info("Running - " + this.getClass().getName());
        return new Option[]{
                copyOSGiLibBundle(maven().artifactId("h2").groupId("com.h2database").version("1.4.195")),
                carbonDistribution(
                        Paths.get("target", "wso2das-" + System.getProperty("carbon.analytic.version")),
                        "worker"),
                copyOSGiLibBundle(maven()
                        .artifactId("org.wso2.carbon.status.dashboard.core")
                        .groupId("org.wso2.carbon.analytics")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven()
                        .artifactId("org.wso2.carbon.analytics.permissions")
                        .groupId("org.wso2.carbon.analytics-common")
                        .versionAsInProject()),
                copyCarbonYAMLOption(),
                copyPermissionDB()
        };
    }

    private Option copyPermissionDB() {
        String basedir = System.getProperty("basedir");
        Path carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "carbon-context", "carbon.yml");
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "database", "PERMISSION_DB.h2.db");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "worker", "database", "PERMISSION_DB.h2.db"));
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
                "conf", CARBON_YAML_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "worker", CARBON_YAML_FILENAME));
    }

    @Test
    public void testDashboardCoreApis() throws Exception {
        URI baseURI = URI.create(String.format("https://%s:%d", "localhost", 9443));
        String path = "/monitoring/apis/workers";
        String contentType = "application/json";
        String method = "POST";
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, "/monitoring/apis/workers",
                Duration.FIVE_SECONDS);
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, "/statistics", Duration.FIVE_SECONDS);
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, "/system-details", Duration.FIVE_SECONDS);
        TestUtil.waitForMicroServiceDeployment(microservicesRegistry, "/siddhi-apps", Duration.FIVE_SECONDS);
        HTTPResponseMessage httpResponseMessage = TestUtil
                .sendHRequest("{\"port\":9443\n , \"host\":\"localhost\"}", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/config";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/system-details";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        testValidSiddhiAPPDeployment();
        TestUtil.waitForAppDeployment(siddhiAppRuntimeService, eventStreamService, "CoreTestApp", Duration.TEN_SECONDS);

        path = "/monitoring/apis/workers/localhost_9443/history";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/history?period=60000";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/history?more=true";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/history?more=true&period=60000";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        path = "/monitoring/apis/workers/localhost_9443/history?more=true";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/history";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/statistics";
        method = "PUT";
        httpResponseMessage = TestUtil
                .sendHRequest("{\"statsEnable\":" + true + "}", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/components";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp/components/streams/cseEventStream/history";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/ha-status";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443/siddhi-apps/CoreTestApp";
        method = "GET";
        httpResponseMessage = TestUtil
                .sendHRequest("", baseURI, path, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);

        path = "/monitoring/apis/workers/localhost_9443";
        method = "DELETE";
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

        log.info("Deploying valid Siddhi App through REST API");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        TestUtil.waitForAppDeployment(siddhiAppRuntimeService, eventStreamService, "CoreTestApp", Duration.TEN_SECONDS);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }
}
