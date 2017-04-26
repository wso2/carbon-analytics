package org.wso2.carbon.event.simulator.core.internal.util;

import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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

    @BeforeClass
    public void setUp() throws Exception {
        new File(testDir, "tempConfigFolder").mkdirs();
    }

    @Test
    public void testUploadSimulationConfig() throws Exception {
        SimulationConfigUploader.getConfigUploader().uploadSimulationConfig(readFile(sampleSimulationCSV), Paths.get
                ("target", "FileUploaderTest", "tempConfigFolder").toString());
        String fileName = Paths.get("target", "FileUploaderTest", "tempConfigFolder",
                FilenameUtils.getName(sampleSimulationCSV)).toString();
        Assert.assertTrue(new File(fileName).exists());
        Assert.assertEquals(readFile(fileName), readFile(sampleSimulationCSV));
    }

    @Test
    public void testGetSimulationConfig() throws Exception {
        String simulationConfigRetrieved = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                (FilenameUtils.getBaseName(sampleSimulationCSV), FilenameUtils.getPath(sampleSimulationCSV));
        String simulationConfigExpected = readFile(sampleSimulationCSV);
        Assert.assertEquals(simulationConfigRetrieved, simulationConfigExpected);
    }

    @Test(dependsOnMethods = {"testUploadSimulationConfig"})
    public void testDeleteConfig() throws Exception {
        SimulationConfigUploader.getConfigUploader().deleteSimulationConfig(
                FilenameUtils.getBaseName(sampleSimulationCSV), Paths.get
                        ("target", "FileUploaderTest", "tempConfigFolder").toString());
        String fileName = Paths.get
                ("target", "FileUploaderTest", "tempConfigFolder",
                        FilenameUtils.getBaseName(sampleSimulationCSV)).toString();
        Assert.assertFalse(new File(fileName).exists());
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
