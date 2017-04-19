package org.wso2.carbon.event.simulator.core.internal.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This  class is responsible for keeping a list of simulation configurations available in the system
 */
public class SimulationConfigStore {
    private static final Logger log = LoggerFactory.getLogger(SimulationConfigStore.class);
    private static final SimulationConfigStore simulationConfigStore = new SimulationConfigStore();
    /**
     * Concurrent list that holds names of simulations
     */
    private final List<String> simulationNamesList = Collections.synchronizedList(new ArrayList<>());

    private SimulationConfigStore() {
        try {
            /*
             * if the 'tmp' directory doesn't already have a directory called 'simulationConfigs', create new directory
             * load all the names of simulation configurations in 'tmp/simulationConfigs' to fileNameList
             * */
            boolean dirCreated = new File(Paths.get(System.getProperty("java.io.tmpdir"),
                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS).toString()).mkdirs();
            if (dirCreated && log.isDebugEnabled()) {
                log.debug("Successfully created directory 'tmp/simulationConfigs' ");
            }
//            create a list of files with '.csv' extension
            List<File> simulationNames = Files.walk(Paths.get(System.getProperty("java.io.tmpdir"),
                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".txt"))
                    .map(Path::toFile).collect(Collectors.toList());
            if (log.isDebugEnabled()) {
                log.debug("Retrieved files in temp directory " + Paths.get(System.getProperty("java.io.tmpdir"),
                        EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS).toString());
            }
//            add each file in list of CSV files to fileNames
            for (File file : simulationNames) {
                simulationNamesList.add(file.getName());
            }
        } catch (IOException e) {
            throw new SimulatorInitializationException("Error occurred when loading simulation configuration names " +
                    "to simulationNamesList ", e);
        }
    }
    /**
     * Method to return Singleton Object of SimulationConfigStore
     *
     * @return simulationConfigStore
     */
    public static SimulationConfigStore getSimulationConfigStore() {
        return simulationConfigStore;
    }


    /**
     * Method to add simulation configurations into in memory
     *
     * @param simulationName name of simulation configuration uploaded
     */
    public void addSimulationConfig(String simulationName) {
        simulationNamesList.add(simulationName);
    }

    /**
     * Method to remove the file from in memory
     *
     * @param simulationName name of simulation being removes
     * @throws IOException it throws IOException if anything occurred while
     *                     delete the simulation config from temp directory and in memory
     */
    public void removeSimulationConfig(String simulationName) throws IOException {
        // delete the file from directory
        Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"),
                EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS, simulationName));
        //delete the file from in memory
        simulationNamesList.remove(simulationName);
    }

    /**
     * Method to check whether the simulation configuration  already exists in directory
     *
     * @param simulationName name of the simulation
     * @return true if exist false if not exist
     */
    public Boolean checkExists(String simulationName) {
        return simulationNamesList.contains(simulationName);
    }
}
