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
import org.apache.log4j.Logger;
import org.awaitility.Duration;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.osgi.framework.BundleContext;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.test.osgi.util.HTTPResponseMessage;
import org.wso2.carbon.analytics.test.osgi.util.TestUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.carbon.stream.processor.common.EventStreamService;
import org.wso2.carbon.stream.processor.core.DeploymentMode;
import org.wso2.carbon.stream.processor.core.NodeInfo;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;
import org.wso2.carbon.stream.processor.core.model.HAStateSyncObject;
import org.wso2.carbon.stream.processor.core.model.OutputSyncTimestampCollection;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import static org.awaitility.Awaitility.await;
import static org.wso2.carbon.container.options.CarbonDistributionOption.carbonDistribution;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class HAActiveNodeTestcase {

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";

    @Inject
    private SiddhiAppRuntimeService siddhiAppRuntimeService;

    @Inject
    private EventStreamService eventStreamService;

    @Inject
    protected BundleContext bundleContext;

    @Inject
    private NodeInfo nodeInfo;

    @Inject
    private CarbonServerInfo carbonServerInfo;

    private static final Logger log = Logger.getLogger(HAActiveNodeTestcase.class);
    private static final String CARBON_YAML_FILENAME = "deployment.yaml";

    /**
     * Replace the existing deployment.yaml file with populated deployment.yaml file.
     */
    private Option copyCarbonYAMLOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "conf", "ha", CARBON_YAML_FILENAME);
        return copyFile(carbonYmlFilePath, Paths.get("conf", "worker", CARBON_YAML_FILENAME));
    }

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{
                copyCarbonYAMLOption(),
                carbonDistribution(Paths.get("target", "wso2das-" +
                        System.getProperty("carbon.analytic.version")), "worker")
        };
    }

    @Test
    public void testActiveNodeWithLiveSyncOff() throws InterruptedException {
        await().atMost(1, TimeUnit.MINUTES).until(() -> nodeInfo.getMode() == DeploymentMode.MINIMUM_HA);
        Assert.assertEquals(nodeInfo.isActiveNode(), true);
        Assert.assertEquals(nodeInfo.getNodeId(), "wso2-sp");
        Assert.assertEquals(nodeInfo.getGroupId(), "sp");

        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/siddhi-apps";
        String contentType = "text/plain";
        String method = "POST";
        String body = "@App:name('TestApp')\n" +
                "define stream FooStream (symbol string, price float, volume long);\n" +
                "\n" +
                "@source(type='inMemory', topic='symbol', @map(type='passThrough'))" +
                "define stream BarStream (symbol string, price float, volume long);\n" +

                "@sink(type='inMemory', topic='option_value') \n" +
                "define stream OutputStream (symbol string, price float, volume long);\n" +

                "@store(type='rdbms', jdbc.url='jdbc:h2:./database/hadb;" +
                "AUTO_SERVER=TRUE', " +
                "username='wso2carbon', password='wso2carbon', jdbc.driver.name='org.h2.Driver' ) \n" +
                "define table OutputTable (symbol string, price float, volume long);\n" +

                "from FooStream\n" +
                "select symbol, price, volume\n" +
                "insert into BarStream;" +

                "from BarStream\n" +
                "select symbol, price, volume\n" +
                "insert into OutputStream;" +

                "from BarStream\n" +
                "select symbol, price, volume\n" +
                "insert into OutputTable;";

        log.info("Deploying Siddhi app to Active Node");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        TestUtil.waitForAppDeployment(siddhiAppRuntimeService, eventStreamService, "TestApp",
                Duration.TEN_SECONDS);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = "testActiveNodeWithLiveSyncOff")
    public void testActiveNodeOutputSyncCollection() {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/ha/outputSyncTimestamps";
        String contentType = "text/plain";
        String method = "GET";

        log.info("Calling active node outputSyncTimestamp");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        OutputSyncTimestampCollection outputSyncCollection = new Gson().fromJson((String) httpResponseMessage.
                        getSuccessContent(), OutputSyncTimestampCollection.class);
        Assert.assertTrue(outputSyncCollection != null);
    }

    @Test(dependsOnMethods = "testActiveNodeOutputSyncCollection")
    public void testActiveNodeGetStateOfSingleSiddhiApp() {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/ha/state/TestApp";
        String contentType = "text/plain";
        String method = "GET";

        log.info("Calling active node to get state of SiddhiApp");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        HAStateSyncObject haStateSyncObject = new Gson().fromJson((String) httpResponseMessage.getSuccessContent(),
                HAStateSyncObject.class);
        Assert.assertTrue(haStateSyncObject != null);
    }

    @Test(dependsOnMethods = "testActiveNodeGetStateOfSingleSiddhiApp")
    public void testActiveNodeGetState() throws InterruptedException {
        URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9090));
        String path = "/ha/state";
        String contentType = "text/plain";
        String method = "GET";

        log.info("Calling active node to get state of all SiddhiApps");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        HAStateSyncObject haStateSyncObject = new Gson().fromJson((String) httpResponseMessage.getSuccessContent(),
                HAStateSyncObject.class);
        Assert.assertTrue(haStateSyncObject != null);
    }
}