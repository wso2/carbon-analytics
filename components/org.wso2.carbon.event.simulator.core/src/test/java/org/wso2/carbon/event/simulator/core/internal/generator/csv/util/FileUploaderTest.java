package org.wso2.carbon.event.simulator.core.internal.generator.csv.util;


import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileLimitExceededException;
import org.wso2.carbon.event.simulator.core.exception.InvalidFileException;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;

import java.io.File;
import java.nio.file.Paths;

/**
 * FileUploaderTest verifies the functionality of uploading CSv files
 */
//todo save and read files from test/resources
public class FileUploaderTest {
    private static File testDir = Paths.get("target", "FileUploaderTest").toFile();
    private static File testSampleDirStructure = Paths.get("target", "FileUploaderTest",
            "testSampleDirStructure").toFile();
    private static String sampleOrderedCSVFile = Paths.get("src", "test", "resources",
            "sample(ordered).csv").toString();
    private static String sampleUnOrderedCSVFile = Paths.get("src", "test", "resources",
            "sample(unordered).csv").toString();
    private static String sampleInsufficientAttributesCSVFile = Paths.get("src", "test", "resources",
            "sample(insufficientAttributes).csv").toString();
    private static String sampleNoTimestampCSVFile = Paths.get("src", "test", "resources",
            "sample(noTimestamp).csv").toString();

    private final String DIRECTORY_LOCATION = "/home/ruwini/abc";
    private final String DIRECTORY_CSV_FILES = "csvFiles";
    private final String VALID_SOURCE_LOCATION = "/home/ruwini/csvFiles/foostream5.csv";
    private final String INVALID_SOURCE_LOCATION = "/home/ruwini/foo.csv";
    private final String INVALID_FILE_TYPE = "/home/ruwini/database.sql";
    private final String FILE_SIZE_EXCEED = "/home/ruwini/csvFiles/foostream2.csv";

//    @BeforeClass
//    public void setUp() throws Exception {
//        EventSimulatorDataHolder.getInstance().setDirectoryDestination(DIRECTORY_LOCATION);
//        EventSimulatorDataHolder.getInstance().setMaximumFileSize(200);
//        File file = new File(DIRECTORY_LOCATION);
//        if (file.exists()) {
//            FileUtils.forceDelete(file);
//        }
//    }
//
//    @AfterClass
//    public void tearDown() throws Exception {
//        EventSimulatorDataHolder.getInstance().setDirectoryDestination(null);
//        EventSimulatorDataHolder.getInstance().setMaximumFileSize(-1);
//        File file = new File(DIRECTORY_LOCATION);
//        if (file.exists()) {
//            FileUtils.forceDelete(file);
//        }
//    }
//
//    @Test
//    public void testUploadValidCSV() throws Exception {
//        FileUploader.getFileUploaderInstance().uploadFile(VALID_SOURCE_LOCATION,
//                FilenameUtils.concat(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES));
//        Assert.assertTrue(new File(Paths.get(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES, FilenameUtils
//                .getName(VALID_SOURCE_LOCATION)).toString()).exists());
//
//    }
//
//    @Test(expectedExceptions = FileAlreadyExistsException.class, dependsOnMethods = {"testUploadValidCSV"})
//    public void testCSVFileAlreadyExists() throws Exception {
//        FileUploader.getFileUploaderInstance().uploadFile(VALID_SOURCE_LOCATION,
//                FilenameUtils.concat(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES));
//
//    }
//
//    @Test(expectedExceptions = InvalidFileException.class)
//    public void testUploadInvalidFileType() throws Exception {
//        FileUploader.getFileUploaderInstance().uploadFile(INVALID_FILE_TYPE,
//                FilenameUtils.concat(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES));
//
//    }
//
//    @Test(expectedExceptions = FileLimitExceededException.class)
//    public void testValidateFileSize() throws Exception {
//        FileUploader.getFileUploaderInstance().uploadFile(FILE_SIZE_EXCEED,
//                FilenameUtils.concat(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES));
//    }
//
//    @Test(expectedExceptions = InvalidFileException.class)
//    public void testInvalidSourceLocation() throws Exception {
//        FileUploader.getFileUploaderInstance().uploadFile(INVALID_SOURCE_LOCATION,
//                FilenameUtils.concat(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES));
//    }
//
//    @Test(dependsOnMethods = {"testUploadValidCSV"})
//    public void testDeleteCSVFIle() throws Exception {
//        boolean deleted = FileUploader.getFileUploaderInstance().deleteFile(FilenameUtils.getName
//                        (VALID_SOURCE_LOCATION),
//                FilenameUtils.concat(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES));
//        Assert.assertTrue(deleted);
//        Assert.assertFalse(new File(Paths.get(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES, FilenameUtils
//                .getName(VALID_SOURCE_LOCATION)).toString()).exists());
//    }
//
//    @Test
//    public void testDeleteFileNotExist() throws Exception {
//        boolean deleted = FileUploader.getFileUploaderInstance().deleteFile(FilenameUtils.getName
//                        (VALID_SOURCE_LOCATION),
//                FilenameUtils.concat(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES));
//        Assert.assertFalse(deleted);
//        Assert.assertTrue(new File(Paths.get(DIRECTORY_LOCATION, DIRECTORY_CSV_FILES, FilenameUtils
//                .getName(VALID_SOURCE_LOCATION)).toString()).exists());
//    }
}