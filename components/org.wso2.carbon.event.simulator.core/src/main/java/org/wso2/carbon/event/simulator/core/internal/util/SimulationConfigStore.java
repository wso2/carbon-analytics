package org.wso2.carbon.event.simulator.core.internal.util;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.SimulatorInitializationException;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;

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
    private static final SimulationConfigStore simulationConfigStore = new SimulationConfigStore
            (EventSimulatorDataHolder.getInstance().getDirectoryDestination());
    /**
     * Concurrent list that holds names of simulations
     */
    private final List<String> simulationNamesList = Collections.synchronizedList(new ArrayList<>());

    private SimulationConfigStore(String destination) {
        try {
            boolean dirCreated = new File(Paths.get(destination, EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)
                    .toString()).mkdirs();
            if (dirCreated && log.isDebugEnabled()) {
                log.debug("Successfully created directory '" + Paths.get(destination,
                        EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS).toString() + "'");
            }
//            load the names of files available in tmp/simulationConfigs
            List<File> fileNames = Files.walk(Paths.get(destination,
                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS))
                    .filter(Files::isRegularFile)
                    .filter(file -> FilenameUtils.isExtension(file.toString(), "json"))
                    .map(Path::toFile).collect(Collectors.toList());
            if (log.isDebugEnabled()) {
                log.debug("Retrieved files in directory '" + Paths.get(destination,
                        EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS).toString() + "'");
            }
            for (File file : fileNames) {
                simulationNamesList.add(FilenameUtils.getBaseName(file.getName()));
            }
        } catch (IOException e) {
            throw new SimulatorInitializationException("Error occurred when loading simulation configuration names " +
                    "available in directory '" + Paths.get(destination,
                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS).toString() + "'", e);
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
     *                     delete the simulation config
     */
    public void removeSimulationConfig(String simulationName, String destination) throws IOException {
        Files.deleteIfExists(Paths.get(destination, simulationName + ".json"));
        simulationNamesList.remove(simulationName);
    }

    /**
     * Method to check whether the simulation configuration  already exists in directory
     *
     * @param simulationName name of the simulation
     * @return true if exist false if not exist
     */
    public boolean checkExists(String simulationName) {
        return simulationNamesList.contains(simulationName);
    }
}
