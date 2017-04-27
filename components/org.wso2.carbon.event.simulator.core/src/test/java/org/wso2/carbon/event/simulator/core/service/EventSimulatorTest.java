package org.wso2.carbon.event.simulator.core.service;


import org.apache.commons.io.FilenameUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.siddhi.query.api.definition.Attribute;

import util.StreamProcessorUtil;

import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * EventSimulatorTest vrifys the functionality of event simulator
 */
public class EventSimulatorTest {
    private static String csvFileDirectory = Paths.get("src", "test", "resources", "files").toString();
    private static String sampleNoTimestampCSVFile = Paths.get("src", "test", "resources", "files",
            "sample(noTimestamp).csv").toString();
    private static String sampleSimulationCSV = Paths.get("src", "test", "resources", "simulationConfigs",
            "sampleSimulationCSV.json").toString();
    private static final String simulationName = FilenameUtils.getBaseName(sampleSimulationCSV);
    private static final String simulationDestination = FilenameUtils.getPath(sampleSimulationCSV);
    private final SimulationConfigUploader simulationConfigUploader = SimulationConfigUploader.getConfigUploader();
    private String validaSimulationProperties = "{\n" +
            "  \"properties\": {\n" +
            "    \"simulationName\": \"sampleSimulationCSV\",\n" +
            "    \"startTimestamp\": \"1488615136957\",\n" +
            "    \"endTimestamp\": \"1488615143957\",\n" +
            "    \"timeInterval\": \"1000\"\n" +
            "  }}";

    @BeforeClass
    public void setUp() throws Exception {
        EventSimulatorDataHolder.getInstance().setCsvFileDirectory(csvFileDirectory);
//        fileUploader.uploadFile();
        EventSimulatorDataHolder.getInstance().setEventStreamService(new StreamProcessorUtil(0));
        StreamProcessorUtil streamProcessorUtil = (StreamProcessorUtil) EventSimulatorDataHolder.getInstance()
                .getEventStreamService();
        streamProcessorUtil.addStreamAttributes("TestExecutionPlan", "FooStream",
                new ArrayList<Attribute>() {
                    {
                        add(new Attribute("symbol", Attribute.Type.STRING));
                        add(new Attribute("price", Attribute.Type.FLOAT));
                        add(new Attribute("volume", Attribute.Type.LONG));

                    }
                });
    }

    public void testValidateSimulationProperties() throws Exception {
        EventSimulator.validateSimulationConfig(validaSimulationProperties);
    }

    public void testCreateSimulator() throws Exception {
        EventSimulator simulator = new EventSimulator(simulationName, simulationConfigUploader.getSimulationConfig
                (simulationName, simulationDestination));
        Assert.assertTrue(simulator.getStatus().equals(EventSimulator.Status.STOP));
    }
}
