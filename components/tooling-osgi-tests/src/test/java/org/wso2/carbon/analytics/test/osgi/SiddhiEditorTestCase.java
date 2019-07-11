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

import static org.wso2.carbon.container.options.CarbonDistributionOption.carbonDistribution;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;

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
import org.wso2.carbon.kernel.CarbonServerInfo;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * OSGI Tests for siddhi-server.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class SiddhiEditorTestCase {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SiddhiEditorTestCase.class);

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private URI baseURI = URI.create(String.format("http://%s:%d", "localhost", 9390));

    @Inject
    private CarbonServerInfo carbonServerInfo;

    private Option copyImportingFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "server", "samples", "ReceiveAndCount.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "server", "deployment", "ReceiveAndCount.siddhi"));
    }

    private Option copySampleFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "server", "samples", "ReceiveAndCount.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("samples"));
    }

    private Option copySiddhiAppFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "server", "siddhi-apps", "TestSiddhiApp.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "server", "deployment", "workspace",
                "TestSiddhiApp.siddhi"));
    }

    @Configuration
    public Option[] createConfiguration() {
        logger.info("Running - "+ this.getClass().getName());
        return new Option[]{
                copySiddhiAppFileOption(),
                //copySampleFileOption(),
                copyImportingFileOption(),
                carbonDistribution(Paths.get("target", "wso2-streaming-integrator-tooling-" +
                        System.getProperty("carbon.analytic.version")), "server")/*,
                CarbonDistributionOption.debug(5005)*/
                };
    }

    @Test
    public void testSiddhiAppValidation() throws Exception {
        String path = "/editor/validator";
        String contentType = "text/plain";
        String method = "POST";
        String body = "{\"siddhiApp\":\"@App:name('TestSiddhiApp')\n" +
                "define stream FooStream (symbol string, price float, volume long);\n" +
                "@source(type='inMemory', topic='symbol', @map(type='passThrough'))" +
                "Define stream BarStream (symbol string, price float, volume long);\n" +
                "from FooStream\nselect symbol, price, volume\n" +
                "insert into BarStream;\"," +
                "\"missingStreams\":[]," +
                "\"missingInnerStreams\":[]}";

        logger.info("Validating a siddhi app.");
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testListingDirectories() throws Exception {
        String path = "/editor/workspace/root";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Listing the directories in the given root directory.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testListingFilesInPath() throws Exception {
        String path = "/editor/workspace/listFilesInPath?path=";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Listing files existed in the given path.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testListingFilesInDirectory() throws Exception {
        String path = "/editor/workspace/listFilesInPath?path=";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Listing files existed in the given directory.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testCheckingWhetherFileExists() throws Exception {
        String path = "/editor/workspace/exists?path=%s";
        String encodedPath = new String(Base64.getEncoder().encode("logs".getBytes()));
        String fullPath = String.format(path, encodedPath);
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Checking whether the given file exists.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, fullPath, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testSavingASiddhiApp() throws Exception {
        String path = "/editor/workspace/write";
        String contentType = "text/plain";
        String method = "POST";
        String config = "@App:name(\"SiddhiApp\")\n" +
                "define stream FooStream (symbol string, price float, volume long);\n" +
                "@source(type='inMemory', topic='symbol', @map(type='passThrough'))" +
                "define stream BarStream (symbol string, price float, volume long);\n" +
                "from FooStream\n" +
                "select symbol, price, volume\n" +
                "insert into BarStream;\n";

        String encodedConfig = Base64.getEncoder().encodeToString(config.getBytes());
        String encodedConfigName = Base64.getEncoder().encodeToString("SiddhiApp.siddhi".getBytes());
        String tmp = String.format("location=%s&configName=%s&config=%s", "location", encodedConfigName, encodedConfig);

        logger.info("Saving a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest(tmp, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = "testSiddhiAppValidation")
    public void testStartingASiddhiApp() throws Exception {
        String path = "/editor/TestSiddhiApp/start";
        String contentType = "text/plain";
        String method = "GET";
        logger.info("Starting a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testStartingASiddhiApp"})
    public void testStoppingASiddhiApp() throws Exception {
        String path = "/editor/TestSiddhiApp/stop";
        String contentType = "text/plain";
        String method = "GET";
        logger.info("Stopping a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testStoppingASiddhiApp"})
    public void testDebuggingASiddhiApp() throws Exception {
        String path = "/editor/TestSiddhiApp/debug";
        String contentType = "text/plain";
        String method = "GET";
        logger.info("Debugging a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testDebuggingASiddhiApp"})
    public void testSteppingOverWhenDebugging() throws Exception {
        String path = "/editor/TestSiddhiApp/next";
        String contentType = "text/plain";
        String method = "GET";
        logger.info("Stepping over when debugging a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testDebuggingASiddhiApp"})
    public void testResumingWhenDebugging() throws Exception {
        String path = "/editor/TestSiddhiApp/play";
        String contentType = "text/plain";
        String method = "GET";
        logger.info("Resuming when debugging a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testDebuggingASiddhiApp"})
    public void testGettingStateOfASiddhiApp() throws Exception {
        String path = "/editor/TestSiddhiApp/state";
        String contentType = "text/plain";
        String method = "GET";
        logger.info("Resuming when debugging a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        String content = httpResponseMessage.getSuccessContent().toString();
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(content, JsonObject.class);
        Map queries = gson.fromJson(jsonObject.getAsJsonObject("queryState").toString(), Map.class);
        if (queries != null) {
            String requestURL;
            for (Object entry : queries.entrySet()) {
                String queryID = ((Map.Entry) entry).getKey().toString();
                requestURL = "/editor/TestSiddhiApp/" + queryID + "/state";
                httpResponseMessage = sendHRequest("", baseURI, requestURL, contentType, method,
                        true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
                Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
                Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
                Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
            }
        }
    }

    @Test(dependsOnMethods = {"testDebuggingASiddhiApp"})
    public void testGettingStateOfAStreamInASiddhiApp() throws Exception {
        String path = "/editor/TestSiddhiApp/BarStream/state";
        String contentType = "text/plain";
        String method = "GET";
        logger.info("Resuming when debugging a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testGettingStateOfAQueryInASiddhiApp() throws Exception {
        String path = "/editor/TestSiddhiApp/BarStream/send";
        String contentType = "text/plain";
        String method = "POST";
        String body = "[\"WSO2\", 10.5, 100]";
        logger.info("Resuming when debugging a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");

    }

    @Test
    public void testSavingASiddhiAppWhenConfigsNotEncoded() throws Exception {
        String path = "/editor/workspace/write";
        String contentType = "text/plain";
        String method = "POST";
        String config = "@App:name(\"SiddhiApp\")\n" +
                "define stream FooStream (symbol string, price float, volume long);\n" +
                "@source(type='inMemory', topic='symbol', @map(type='passThrough'))" +
                "define stream BarStream (symbol string, price float, volume long);\n" +
                "from FooStream\n" +
                "select symbol, price, volume\n" +
                "insert into BarStream;\n";

        String configName = "SiddhiApp.siddhi";
        String tmp = String.format("location=%s&configName=%s&config=%s", "location", configName, config);

        logger.info("Saving a siddhi application.");
        HTTPResponseMessage httpResponseMessage = sendHRequest(tmp, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getMessage(), "Internal Server Error");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testSavingASiddhiApp"})
    public void testCheckingWhetherFileExistsInWorkspace() throws Exception {
        String path = "/editor/workspace/exists/workspace";
        String contentType = "text/plain";
        String method = "POST";
        String encodedBody = Base64.getEncoder().encodeToString("SiddhiApp.siddhi".getBytes());
        String body = String.format("configName=%s", encodedBody);

        logger.info("Checking whether the given siddhi app is existed in the workspace.");
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = {"testSavingASiddhiApp"})
    public void testCheckingWhetherInvalidFileExistsInWorkspace() throws Exception {
        String path = "/editor/workspace/exists/workspace";
        String contentType = "text/plain";
        String method = "POST";
        String body = String.format("configName=%s", "SiddhiApp.siddhi");

        logger.info("Checking whether the given siddhi app is existed in the workspace.");
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getMessage(), "Internal Server Error");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testReadingSample() throws Exception {
        String path = "/editor/workspace/read/sample";
        String contentType = "text/plain";
        String method = "POST";
        String body = "artifacts/1001/ReceiveAndCount.siddhi";

        logger.info("Reading a sample.");
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testReadingInvalidSample() throws Exception {
        String path = "/editor/workspace/read/sample";
        String contentType = "text/plain";
        String method = "POST";
        String body = "wso2/editor/directoryNotExisted/ReceiveAndCount.siddhi";

        logger.info("Reading a sample.");
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getMessage(), "Internal Server Error");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testGettingMetadata() throws Exception {
        String path = "/editor/metadata";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Loading metadata.");
        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    private HTTPResponseMessage sendHRequest(String body, URI baseURI, String path, String contentType,
                                             String methodType, Boolean auth, String userName, String password) {
        TestUtil testUtil = new TestUtil(baseURI, path, auth, false, methodType,
                contentType, userName, password);
        testUtil.addBodyContent(body);
        return testUtil.getResponse();
    }

    //TODO : tests for {siddhiApp}/..
}
