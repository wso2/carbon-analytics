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
import org.wso2.carbon.kernel.CarbonServerInfo;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

import javax.inject.Inject;

/**
 * OSGI Tests for siddhi-editor.
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

    private Option copySampleFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "editor", "samples", "ReceiveAndCount.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "editor", "deployment", "ReceiveAndCount.siddhi"));
    }

    private Option copySiddhiAppFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "editor", "siddhi-apps", "TestSiddhiApp.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "editor", "deployment", "workspace",
                "TestSiddhiApp.siddhi"));
    }

    @Configuration
    public Option[] createConfiguration() {
        return new Option[]{
                copySiddhiAppFileOption(),
                copySampleFileOption(),
                carbonDistribution(Paths.get("target", "wso2das-" +
                        System.getProperty("carbon.analytic.version")), "editor")/*,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testListingDirectoriesInPath() throws Exception {
        String path = "/editor/workspace/list?path=";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Listing existing directories under the given path.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testListingDirectoriesInInvalidPath() throws Exception {
        String path = "/editor/workspace/list?path=invalidPath";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Listing existing directories under the given path.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getMessage(), "Internal Server Error");
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, fullPath, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(tmp, baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(tmp, baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);

        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getMessage(), "Internal Server Error");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testExportingAFile() throws Exception {
        String path = "/editor/workspace/export";
        String contentType = "text/plain";
        String method = "POST";
        String config = "@App:name(\"SiddhiApp\")\n" +
                "define stream FooStream (symbol string, price float, volume long);\n" +
                "@source(type='inMemory', topic='symbol', @map(type='passThrough'))" +
                "define stream BarStream (symbol string, price float, volume long);\n" +
                "from FooStream\n" +
                "select symbol, price, volume\n" +
                "insert into BarStream;\n";

        String userdir = System.getProperty("user.dir");
        String location = Paths.get(userdir, "deployment").toString();
        String encodedLocation = Base64.getEncoder().encodeToString(location.getBytes());
        String encodedConfig = Base64.getEncoder().encodeToString(config.getBytes());
        String encodedConfigName = Base64.getEncoder().encodeToString("SiddhiApp.siddhi".getBytes());
        String body = String.format("location=%s&configName=%s&config=%s", encodedLocation, encodedConfigName,
                encodedConfig);

        logger.info("Exporting a siddhi app.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
    }

    @Test
    public void testFailureInExportingAFileWithInvalidConfigs() throws Exception {
        String path = "/editor/workspace/export";
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
        String body = String.format("location=%s&configName=%s&config=%s", "", configName, config);

        logger.info("Trying to export a file which is not possible to export.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(httpResponseMessage.getMessage(), "Internal Server Error");
    }

    @Test
    public void testReadingSample() throws Exception {
        String path = "/editor/workspace/read/sample";
        String contentType = "text/plain";
        String method = "POST";
        String body = "wso2/editor/deployment/ReceiveAndCount.siddhi";

        logger.info("Reading a sample.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
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
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(body, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getMessage(), "Internal Server Error");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testImportingAFile() throws Exception {
        String path = "/editor/workspace/import";
        String contentType = "text/plain";
        String method = "POST";
        String userdir = System.getProperty("user.dir");
        Path sourceFilePath = Paths.get(userdir, "deployment", "ReceiveAndCount.siddhi");
        String sourceFile = sourceFilePath.toString();

        logger.info("Importing a siddhi application from file system.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(sourceFile, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testImportingAnInvalidFile() throws Exception {
        String path = "/editor/workspace/import";
        String contentType = "text/plain";
        String method = "POST";
        String userdir = System.getProperty("user.dir");
        Path sourceFilePath = Paths.get(userdir, "invalid.directory", "ReceiveAndCount.siddhi");
        String sourceFile = sourceFilePath.toString();

        logger.info("Importing a siddhi application from file system.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(sourceFile, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getMessage(), "Internal Server Error");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testImportingANotExistingFile() throws Exception {
        String path = "/editor/workspace/import";
        String contentType = "text/plain";
        String method = "POST";
        String userdir = System.getProperty("user.dir");
        Path sourceFilePath = Paths.get(userdir, "deployment", "siddhi-files", "TestInvalidSiddhiApp.siddhi");
        String sourceFile = sourceFilePath.toString();

        logger.info("Importing a siddhi application from file system.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest(sourceFile, baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test
    public void testGettingMetadata() throws Exception {
        String path = "/editor/metadata";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Loading metadata.");
        HTTPResponseMessage httpResponseMessage = TestUtil.sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getMessage(), "OK");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    //TODO : tests for {siddhiApp}/..
}
