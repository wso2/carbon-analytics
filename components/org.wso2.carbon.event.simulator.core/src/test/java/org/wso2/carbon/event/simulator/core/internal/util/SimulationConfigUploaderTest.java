package org.wso2.carbon.event.simulator.core.internal.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * SimulationConfigUploaderTest the functionality of simulation configuration uploading
 */
public class SimulationConfigUploaderTest {
    private static File testDir = Paths.get("target", "FileUploaderTest").toFile();
    private static String sampleSimulationCSV = Paths.get("src", "test", "resources", "simulationConfigs",
            "sampleSimulationCSV.json").toString();
    private static String malformedJsonFile = Paths.get("src", "test", "resources", "simulationConfigs",
            "malformedJSON.json").toString();
    private static String withoutSimulationName = Paths.get("src", "test", "resources",
            "simulationConfigs", "simulationConfig(noSimulationName).json").toString();
    private static String tempConfigFolder = Paths.get("target", "FileUploaderTest", "tempConfigFolder")
            .toString();

    @BeforeClass
    public void setUp() throws Exception {
        File file = new File(testDir, "tempConfigFolder");
        if (file.exists()) {
            FileUtils.deleteDirectory(file);
        }
        file.mkdirs();
    }

    @Test
    public void testUploadSimulationConfig() throws Exception {
        File fileName = Paths.get("target", "FileUploaderTest", "tempConfigFolder",
                FilenameUtils.getName(sampleSimulationCSV)).toFile();
        deleteFile(sampleSimulationCSV, tempConfigFolder);
        Assert.assertFalse(fileName.exists());
//        upload a valid config JSON file
        uploadConfig(sampleSimulationCSV, tempConfigFolder);
        Assert.assertTrue(fileName.exists());
        Assert.assertEquals(readFile(fileName.getPath()), readFile(sampleSimulationCSV));
    }


    @Test(expectedExceptions = InvalidConfigException.class)
    public void testNOSimulationName() throws Exception {
        uploadConfig(withoutSimulationName, tempConfigFolder);

    }

    @Test(expectedExceptions = InvalidConfigException.class)
    public void testMalformedJson() throws Exception {
        uploadConfig(malformedJsonFile, tempConfigFolder);

    }

    @Test(dependsOnMethods = {"testUploadSimulationConfig"})
    public void testDeleteValidConfig() throws Exception {
        Assert.assertTrue(deleteFile(sampleSimulationCSV, tempConfigFolder));
    }

    @Test
    public void testGetSimulationConfig() throws Exception {
        String simulationConfigRetrieved = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                (FilenameUtils.getBaseName(sampleSimulationCSV), FilenameUtils.getPath(sampleSimulationCSV));
        String simulationConfigExpected = readFile(sampleSimulationCSV);
        Assert.assertEquals(simulationConfigRetrieved, simulationConfigExpected);
    }

    private void uploadConfig(String fileName, String destination) throws IOException, FileOperationsException,
            InvalidConfigException, FileAlreadyExistsException {
        SimulationConfigUploader.getConfigUploader().uploadSimulationConfig(readFile(fileName), destination);
    }

    private boolean deleteFile(String fileName, String destination) throws FileOperationsException {
        return SimulationConfigUploader.getConfigUploader().deleteSimulationConfig(
                FilenameUtils.getBaseName(fileName), destination);
    }

    private String readFile(String fileName) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        }
    }
}
