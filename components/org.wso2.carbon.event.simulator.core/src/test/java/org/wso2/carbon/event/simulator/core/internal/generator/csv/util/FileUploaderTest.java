/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.simulator.core.internal.generator.csv.util;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileLimitExceededException;
import org.wso2.carbon.event.simulator.core.exception.InvalidFileException;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.msf4j.formparam.FileInfo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * FileUploaderTest verifies the functionality of uploading CSv files
 */
public class FileUploaderTest {
    private static File testDir = Paths.get("target", "FileUploaderTest").toFile();
    private static String sampleOrderedCSVFile = Paths.get("src", "test", "resources", "files",
            "sample(ordered).csv").toAbsolutePath().toString();
    private static String sampleORDEREDcsv = Paths.get("src", "test", "resources", "files",
            "SAMPLE(ORDERED).csv").toString();
    private static String sampleTextFile = Paths.get("src", "test", "resources", "files",
            "sample.txt").toString();

    @BeforeMethod
    public void setUp() throws Exception {
        File file = new File(testDir, "tempCSVFolder");
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }
        file.mkdirs();
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        deleteFile(FilenameUtils.getName(sampleOrderedCSVFile));
        EventSimulatorDataHolder.getInstance().setMaximumFileSize(200);
    }

    @Test
    public void testUploadValidCSV() throws Exception {
        File csvFile = new File(Paths.get(testDir.toString(), "tempCSVFolder", FilenameUtils.getName
                (sampleOrderedCSVFile)).toString());
        uploadFile(sampleOrderedCSVFile);
        Assert.assertTrue(csvFile.exists());

    }

    @Test (dependsOnMethods = "testUploadValidCSV")
    public void testDeleteCSVFIle() throws Exception {
        uploadFile(sampleOrderedCSVFile);
        boolean deleted = FileUploader.getFileUploaderInstance().deleteFile(FilenameUtils.getName(sampleOrderedCSVFile),
                FilenameUtils.concat(testDir.toString(), "tempCSVFolder"));
        Assert.assertTrue(deleted);
        Assert.assertFalse(new File(Paths.get(testDir.toString(), "tempCSVFolder", FilenameUtils
                .getName(sampleOrderedCSVFile)).toString()).exists());
    }

    @Test(expectedExceptions = FileAlreadyExistsException.class, dependsOnMethods = "testDeleteCSVFIle")
    public void testCSVFileAlreadyExists() throws Exception {
        uploadFile(sampleOrderedCSVFile);
        uploadFile(sampleOrderedCSVFile);
    }

    @Test(expectedExceptions = InvalidFileException.class, dependsOnMethods = "testCSVFileAlreadyExists")
    public void testUploadInvalidFileType() throws Exception {
        uploadFile(sampleTextFile);
    }

    @Test(expectedExceptions = FileLimitExceededException.class, dependsOnMethods = "testUploadInvalidFileType")
    public void testValidateFileSize() throws Exception {
        EventSimulatorDataHolder.getInstance().setMaximumFileSize(60);
        uploadFile(sampleOrderedCSVFile);
    }

    @Test (dependsOnMethods = "testValidateFileSize")
    public void testDeleteFileNotExist() throws Exception {
        boolean deleted = FileUploader.getFileUploaderInstance()
                .deleteFile(FilenameUtils.getName(sampleOrderedCSVFile), FilenameUtils.concat(testDir.toString(),
                        "tempCSVFolder"));
        Assert.assertFalse(deleted);
    }

    @Test (dependsOnMethods = "testDeleteFileNotExist")
    public void testCaseSensitiveFileUpload() throws Exception {
        String OSName = System.getProperty("os.name");
        System.out.println(OSName);
        try {
            uploadFile(sampleOrderedCSVFile);
            uploadFile(sampleORDEREDcsv);
            Assert.assertTrue(new File(Paths.get(testDir.toString(), "tempCSVFolder", FilenameUtils.getName
                    (sampleOrderedCSVFile)).toString()).exists());
            Assert.assertTrue(new File(Paths.get(testDir.toString(), "tempCSVFolder", FilenameUtils.getName
                    (sampleORDEREDcsv)).toString()).exists());
        } catch (FileAlreadyExistsException ex) {
            if (!"Mac OS X".equals(OSName)) {
                throw ex;
            }
        }
    }

    private void deleteFile(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(Paths.get(testDir.toString(), "tempCSVFolder", fileName).toString()));
        } catch (IOException e) {
            Assert.assertTrue(false);
        }
    }

    private void uploadFile(String filePath) throws Exception {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFileName(FilenameUtils.getName(filePath));
        FileUploader.getFileUploaderInstance().uploadFile(fileInfo, FileUtils.openInputStream(new
                File(filePath)), FilenameUtils.concat(testDir.toString(), "tempCSVFolder"));
    }
}
