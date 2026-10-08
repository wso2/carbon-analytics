/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
package org.wso2.carbon.si.management.icp.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.si.management.icp.impl.ICPReporterService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.ws.rs.core.Response;

public class UtilsTest {

    private Path temporaryDirectory;
    private Path carbonHome;
    private Path logDirectory;
    private String previousCarbonHome;

    @BeforeMethod
    public void setUp() throws IOException {
        temporaryDirectory = Files.createTempDirectory("icp-log-test");
        carbonHome = temporaryDirectory.resolve("carbon-home");
        logDirectory = Utils.getLogDirectoryPath(carbonHome.toString());
        Files.createDirectories(logDirectory);
        previousCarbonHome = System.getProperty(Constants.ENV_CARBON_HOME);
        System.setProperty(Constants.ENV_CARBON_HOME, carbonHome.toString());
    }

    @AfterMethod
    public void tearDown() throws IOException {
        if (previousCarbonHome == null) {
            System.clearProperty(Constants.ENV_CARBON_HOME);
        } else {
            System.setProperty(Constants.ENV_CARBON_HOME, previousCarbonHome);
        }
        FileUtils.deleteDirectory(temporaryDirectory.toFile());
    }

    @Test
    public void testResolveLogFilePath() throws IOException {
        Path logFile = createFile(logDirectory.resolve("carbon.log"));

        Path resolvedPath = Utils.resolveLogFilePath(carbonHome.toString(), "carbon.log");

        Assert.assertEquals(resolvedPath, logFile.toRealPath());
    }

    @Test
    public void testResolveNestedLogFilePath() throws IOException {
        Path logFile = createFile(logDirectory.resolve("archive/carbon.log"));

        Path resolvedPath = Utils.resolveLogFilePath(carbonHome.toString(), "archive/carbon.log");

        Assert.assertEquals(resolvedPath, logFile.toRealPath());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectAbsolutePath() throws IOException {
        Path outsideFile = createFile(temporaryDirectory.resolve("outside.txt"));

        Utils.resolveLogFilePath(carbonHome.toString(), outsideFile.toString());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectPathTraversal() throws IOException {
        createFile(temporaryDirectory.resolve("outside.txt"));

        Utils.resolveLogFilePath(carbonHome.toString(), "../../../../outside.txt");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectSymlinkOutsideLogDirectory() throws IOException {
        Path outsideFile = createFile(temporaryDirectory.resolve("outside.txt"));
        Files.createSymbolicLink(logDirectory.resolve("linked.log"), outsideFile);

        Utils.resolveLogFilePath(carbonHome.toString(), "linked.log");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectSymlinkInsideLogDirectory() throws IOException {
        Path logFile = createFile(logDirectory.resolve("carbon.log"));
        Files.createSymbolicLink(logDirectory.resolve("linked.log"), logFile.getFileName());

        Utils.resolveLogFilePath(carbonHome.toString(), "linked.log");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectDanglingSymlink() throws IOException {
        Files.createSymbolicLink(logDirectory.resolve("linked.log"), logDirectory.resolve("missing.log"));

        Utils.resolveLogFilePath(carbonHome.toString(), "linked.log");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectSymlinkLoop() throws IOException {
        Files.createSymbolicLink(logDirectory.resolve("linked.log"), logDirectory.resolve("linked.log"));

        Utils.resolveLogFilePath(carbonHome.toString(), "linked.log");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testRejectDirectory() throws IOException {
        Files.createDirectories(logDirectory.resolve("archive"));

        Utils.resolveLogFilePath(carbonHome.toString(), "archive");
    }

    @Test
    public void testLogFileListExcludesSymlinksAndDirectories() throws IOException {
        createFile(logDirectory.resolve("carbon.log"));
        Path outsideFile = createFile(temporaryDirectory.resolve("outside.txt"));
        Files.createSymbolicLink(logDirectory.resolve("linked.log"), outsideFile);
        Files.createDirectories(logDirectory.resolve("directory.log"));

        JsonObject response = Utils.getLogFileList();
        JsonArray logFiles = response.getAsJsonArray(Constants.LIST);

        Assert.assertEquals(logFiles.size(), 1);
        Assert.assertEquals(logFiles.get(0).getAsJsonObject().get("FileName").getAsString(), "carbon.log");
    }

    @Test
    public void testGetLogsSupportsNestedFileWithoutLogExtension() throws IOException {
        createFile(logDirectory.resolve("archive/custom-output.txt"));

        Response response = new ICPReporterService().getLogs(null, "archive/custom-output.txt");

        Assert.assertEquals(response.getStatus(), Response.Status.OK.getStatusCode());
        try (InputStream inputStream = (InputStream) response.getEntity()) {
            Assert.assertEquals(IOUtils.toString(inputStream, StandardCharsets.UTF_8), "test");
        }
    }

    @Test
    public void testGetLogsRejectsTraversal() {
        Response response = new ICPReporterService().getLogs(null, "../../../../outside.txt");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogsRejectsSymlink() throws IOException {
        Path outsideFile = createFile(temporaryDirectory.resolve("outside.txt"));
        Files.createSymbolicLink(logDirectory.resolve("linked.log"), outsideFile);

        Response response = new ICPReporterService().getLogs(null, "linked.log");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogsRejectsDirectory() throws IOException {
        Files.createDirectories(logDirectory.resolve("archive"));

        Response response = new ICPReporterService().getLogs(null, "archive");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testGetLogsReturnsNotFoundForMissingFile() {
        Response response = new ICPReporterService().getLogs(null, "missing.log");

        Assert.assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testGetLogsRejectsEmptyFileName() {
        Response response = new ICPReporterService().getLogs(null, " ");

        Assert.assertEquals(response.getStatus(), Response.Status.BAD_REQUEST.getStatusCode());
    }

    private Path createFile(Path path) throws IOException {
        Files.createDirectories(path.getParent());
        return Files.write(path, "test".getBytes(StandardCharsets.UTF_8));
    }
}
