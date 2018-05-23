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

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.inject.Inject;

import static org.wso2.carbon.container.options.CarbonDistributionOption.carbonDistribution;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SimulatorAPITestcase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SimulatorAPITestcase.class);

    private static final int HTTP_PORT = 9390;
    private static final String HOSTNAME = "localhost";
    private URI baseURI = URI.create(String.format("http://%s:%d", HOSTNAME, HTTP_PORT));

    private final String DEFAULT_USER_NAME = "admin";
    private final String DEFAULT_PASSWORD = "admin";

    private static final String VALID_SINGLE_EVENT_CONFIG = "{\n" +
            "  \"streamName\": \"CountStream\",\n" +
            "  \"siddhiAppName\": \"ReceiveAndCount\",\n" +
            "  \"data\": ['Customer1', 26, 'USA', 15 ]\n" +
            "}";
    private static final String IN_VALID_SINGLE_EVENT_CONFIG = "{\n" +
            "  \"streamName\": \"CountStream\",\n" +
            "  \"data\": ['Customer1', 26, 'USA', 15 ]\n" +
            "}";

    private static final String VALID_FEED_EVENT_CONFIG = "{\"properties\":{\"simulationName\":\"FeedSimulation\"," +
            "\"startTimestamp\":\"\",\"endTimestamp\":\"\",\"noOfEvents\":\"\",\"description\":\"\"," +
            "\"timeInterval\":\"1000\"},\"sources\":[{\"siddhiAppName\":\"ReceiveAndCount\",\"streamName\":" +
            "\"CountStream\",\"timestampInterval\":\"1000\",\"simulationType\":\"CSV_SIMULATION\",\"fileName\":" +
            "\"sampleCSV.csv\",\"delimiter\":\",\",\"isOrdered\":true,\"indices\":\"0,1,2,3\"}]}";

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    protected BundleContext bundleContext;

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{
                carbonDistribution(Paths.get("target", "wso2das-" +
                        System.getProperty("carbon.analytic.version")), "editor"),
                copySiddhiFileOption(),
                copyCSVFileOption(),
                //CarbonDistributionOption.debug(5005)
        };
    }

    private Option copySiddhiFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "editor", "siddhi-apps", "ReceiveAndCount.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "editor", "deployment", "workspace",
                "ReceiveAndCount.siddhi"));
    }

    private Option copyCSVFileOption() {
        Path csvFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        csvFilePath = Paths.get(basedir, "src", "test", "resources",
                "files", "sampleCSV.csv");
        return copyFile(csvFilePath, Paths.get("wso2", "editor",
                "sampleCSV.csv"));
    }

    @Test
    public void testSingleAPI() throws Exception {
        String path = "/simulation/single";
        String contentType = "text/plain";
        String method = "POST";
        logger.info("Simulation REST API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(VALID_SINGLE_EVENT_CONFIG, baseURI, path, contentType,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testSingleAPI"})
    public void testInvalidEventConfigSingleAPI() throws Exception {
        String path = "/simulation/single";
        String contentType = "text/plain";
        String method = "POST";
        logger.info("Simulation REST API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(IN_VALID_SINGLE_EVENT_CONFIG, baseURI, path, contentType,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    //todo:remove thread sleep after fixing deployment success callback
    @Test(dependsOnMethods = {"testSingleAPI"})
    public void testaddFilesApi() throws Exception {
        String fileUploadPath = "/simulation/files";
        String method = "POST";
        String contentType = "multipart/form-data";
        File file = new File("sampleCSV.csv");
        logger.info("Uploading Simulation CSV file");
        TestUtil testUtil = new TestUtil(baseURI, fileUploadPath, true, true,
                method, contentType, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        testUtil.addFilePart("file", file);
        HTTPResponseMessage httpResponseMessage = testUtil.getResponse();
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Thread.sleep(10000);
    }

    //todo:remove thread sleep after fixing deployment success callback
    @Test(dependsOnMethods = {"testaddFilesApi"})
    public void testAddingFeedConfApi() throws Exception {
        String simlationPath = "/simulation/feed/";
        String simulationContentType = "text/plain";
        String method = "POST";
        logger.info("Uploading Simulation Configuration through API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(VALID_FEED_EVENT_CONFIG, baseURI, simlationPath,
                simulationContentType, method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 201);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Thread.sleep(10000);
    }

    @Test(dependsOnMethods = {"testAddingFeedConfApi"})
    public void testReAddingFeedConfApi() throws Exception {
        String simlationPath = "/simulation/feed/";
        String simulationContentType = "text/plain";
        String method = "POST";
        logger.info("Uploading same Simulation Configuration again through API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(VALID_FEED_EVENT_CONFIG, baseURI, simlationPath,
                simulationContentType, method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 409);
        logger.info(httpResponseMessage.getErrorContent());
    }

    //todo:remove thread sleep after fixing deployment success callback
    @Test(dependsOnMethods = {"testReAddingFeedConfApi"})
    public void testRunFeedApi() throws Exception {
        String path = "/simulation/feed/FeedSimulation/?action=run";
        String method = "POST";
        String contentType = null;
        logger.info("Simulation starting");
        HTTPResponseMessage httpResponseMessage = sendHRequest(VALID_SINGLE_EVENT_CONFIG, baseURI, path, contentType,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Thread.sleep(10000);
    }

    @Test(dependsOnMethods = {"testRunFeedApi"})
    public void testPauseFeedApi() throws Exception {
        String method = "POST";
        String path = "/simulation/feed/FeedSimulation/?action=pause";
        logger.info("Pause Simulation Configuration API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(VALID_SINGLE_EVENT_CONFIG, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testPauseFeedApi"})
    public void testResumeFeedApi() throws Exception {
        String method = "POST";
        String path = "/simulation/feed/FeedSimulation/?action=resume";
        logger.info("Resume Simulation Configuration API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(VALID_SINGLE_EVENT_CONFIG, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testResumeFeedApi"})
    public void testStopFeedApi() throws Exception {
        String method = "POST";
        String path = "/simulation/feed/FeedSimulation/?action=stop";
        logger.info("Stop Simulation Configuration API");
        HTTPResponseMessage httpResponseMessage = sendHRequest(VALID_SINGLE_EVENT_CONFIG, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testStopFeedApi"})
    public void testGetFilesApi() throws Exception {
        String path = "/simulation/files";
        String method = "GET";
        logger.info("Get names of all uploaded CSV files");
        HTTPResponseMessage httpResponseMessage = sendHRequest(null, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        logger.info(httpResponseMessage.getSuccessContent());
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testGetFilesApi"})
    public void testGetFeedConfApi() throws Exception {
        String path = "/simulation/feed/";
        String method = "GET";
        logger.info("GET Feed configurations");
        HTTPResponseMessage httpResponseMessage = sendHRequest(null, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        logger.info(httpResponseMessage.getSuccessContent());
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        logger.info(httpResponseMessage.getSuccessContent());
    }

    @Test(dependsOnMethods = {"testGetFeedConfApi"})
    public void testGetSpecificFeedConfApi() throws Exception {
        String path = "/simulation/feed/FeedSimulation";
        String method = "GET";
        logger.info("GET specific feed configuration");
        HTTPResponseMessage httpResponseMessage = sendHRequest(null, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        logger.info(httpResponseMessage.getSuccessContent());
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        logger.info(httpResponseMessage.getSuccessContent());
    }

    @Test(dependsOnMethods = {"testGetSpecificFeedConfApi"})
    public void testDeleteFeedConfApi() throws Exception {
        String path = "/simulation/feed/FeedSimulation";
        String method = "DELETE";
        logger.info("Delete Feed configuration");
        HTTPResponseMessage httpResponseMessage = sendHRequest(null, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        logger.info(httpResponseMessage.getSuccessContent());
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testGetFilesApi"})
    public void testDeleteUnavailableFeedConf() throws Exception {
        String path = "/simulation/feed/FeedSimulation2";
        String method = "DELETE";
        logger.info("Delete Feed configuration");
        HTTPResponseMessage httpResponseMessage = sendHRequest(null, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 404);
        logger.info(httpResponseMessage.getErrorContent());
    }

    @Test(dependsOnMethods = {"testDeleteUnavailableFeedConf"})
    public void testGetUnavailableFeedConf() throws Exception {
        String path = "/simulation/feed/";
        String method = "GET";
        logger.info("GET unavailable Feed configurations");
        HTTPResponseMessage httpResponseMessage = sendHRequest(null, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    @Test(dependsOnMethods = {"testGetUnavailableFeedConf"})
    public void testGetSpecificUnavailableFeedConf() throws Exception {
        String path = "/simulation/feed/FeedSimulation";
        String method = "GET";
        logger.info("GET specific unavailable feed configuration");
        HTTPResponseMessage httpResponseMessage = sendHRequest(null, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        logger.info(httpResponseMessage.getSuccessContent());
    }

    @Test(dependsOnMethods = {"testDeleteUnavailableFeedConf"})
    public void testDeleteFilesApi() throws Exception {
        String path = "/simulation/files/sampleCSV.csv";
        String method = "DELETE";
        logger.info("Trying to delete unavailable CSV file");
        HTTPResponseMessage httpResponseMessage = sendHRequest(null, baseURI, path, null,
                method, true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        logger.info(httpResponseMessage.getSuccessContent().toString());
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
    }

    private HTTPResponseMessage sendHRequest(String body, URI baseURI, String path, String contentType,
                                             String methodType, Boolean auth, String userName, String password) {
        TestUtil testUtil = new TestUtil(baseURI, path, auth, true, methodType,
                contentType, userName, password);
        testUtil.addBodyContent(body);
        return testUtil.getResponse();
    }
}
