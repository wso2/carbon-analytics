package org.wso2.carbon.event.simulator.core.internal.util;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for saving, modifying and deleting the simulation configurations
 */
public class SimulationConfigUploader {
    private static final Logger log = Logger.getLogger(SimulationConfigUploader.class);
    private static final SimulationConfigUploader configUploader =
            new SimulationConfigUploader(SimulationConfigStore.getSimulationConfigStore());
    /**
     * SimulationConfigStore object which holds details of uploaded simulation configurations
     */
    private SimulationConfigStore simulationConfigStore;

    private SimulationConfigUploader(SimulationConfigStore simulationConfigStore) {
        this.simulationConfigStore = simulationConfigStore;
    }

    /**
     * getConfigUploader() returns Singleton configUploader object
     *
     * @return configUploader
     */
    public static SimulationConfigUploader getConfigUploader() {
        return configUploader;
    }

    /**
     * Method to upload a simulation configuration.
     *
     * @param simulationConfig simulation configuration being uploaded
     * @throws FileAlreadyExistsException if the file exists in 'deployment/simulator/simulationConfigs' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'deployment/simulator/simulationConfigs' directory
     */
    public void uploadSimulationConfig(String simulationConfig, String destination) throws FileAlreadyExistsException,
            FileOperationsException, InvalidConfigException {
        String simulationName = getSimulationName(simulationConfig);
        if (!simulationConfigStore.checkExists(simulationName)) {
            if (log.isDebugEnabled()) {
                log.debug("Initialize a File writer for simulation configuration '" + simulationName + "'.");
            }
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(destination, (simulationName +
                    ".json")))) {
                writer.write(simulationConfig);
                simulationConfigStore.addSimulationConfig(simulationName);
                if (log.isDebugEnabled()) {
                    log.debug("Successfully uploaded simulation configuration '" + simulationName + "' to " +
                            "directory '" + destination + "'");
                }
            } catch (IOException e) {
                log.error("Error occurred while copying the file '" + simulationName + "' to " +
                        "directory '" + destination + "'. ", e);
                throw new FileOperationsException("Error occurred while copying the file '" + simulationName +
                        "' to directory '" + Paths.get(destination).toString() + "'. ", e);
            }
        } else {
            log.error("Simulation configuration '" + simulationName + "' already exists in directory '" +
                    destination + "'");
            throw new FileAlreadyExistsException("Simulation configuration '" + simulationName + "'" +
                    " already exists in directory '" + destination + "'");
        }
    }

    /**
     * Method to delete an uploaded simulation configuration.
     *
     * @param simulationName name of simulation to be deleted
     * @throws FileOperationsException if an IOException occurs while deleting file
     */
    public boolean deleteSimulationConfig(String simulationName, String destination) throws FileOperationsException {
        try {
            if (simulationConfigStore.checkExists(simulationName)) {
                simulationConfigStore.removeSimulationConfig(simulationName, destination);
                if (log.isDebugEnabled()) {
                    log.debug("Deleted simulation configuration '" + simulationName + "' from directory '" +
                            destination + "'");
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            log.error("Error occurred while deleting the simulation configuration '" + simulationName +
                    "' from directory '" + destination + "'", e);
            throw new FileOperationsException("Error occurred while deleting the simulation configuration '" +
                    simulationName + "' from directory '" + destination + "'", e);
        }
    }

    /**
     * getSimulationConfig() is used to retrieve an uploaded simulation configuration
     *
     * @param simulationName name of simulation to be retrieved
     * @param destination where the simulation configuration is stored
     * @return simulation configuration
     * @throws FileOperationsException is an error occurs when reading simulation configuration file
     * */
    public String getSimulationConfig(String simulationName, String destination) throws FileOperationsException {
        try {
            if (simulationConfigStore.checkExists(simulationName)) {
                return new String(Files.readAllBytes(Paths.get(destination, (simulationName + ".json"))),
                        "UTF-8");
            } else {
                return null;
            }
        } catch (IOException e) {
            log.error("Error occurred while reading the simulation configuration '" +
                    simulationName + "' from directory '" + destination + "'", e);
            throw new FileOperationsException("Error occurred while reading the simulation configuration '" +
                    simulationName + "' from directory '" + destination + "'", e);
        }
    }

    /**
     * getSimulationName() is used to retrieve the simulation name of a simulation configuration
     *
     * @param simulationConfig simulation configuration
     * @return simulation configuration name
     * @throws InvalidConfigException if the simulation configuration doesnt contain a simulation name
     * */
    public String getSimulationName(String simulationConfig) throws InvalidConfigException {
        JSONObject configuration = new JSONObject(simulationConfig);
        if (configuration.has(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)
                && !configuration.isNull(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)) {
            if (checkAvailability(configuration.getJSONObject(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES),
                    EventSimulatorConstants.EVENT_SIMULATION_NAME)) {
                return configuration.getJSONObject(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)
                        .getString(EventSimulatorConstants.EVENT_SIMULATION_NAME);
            } else {
                throw new InvalidConfigException("Simulation name is required for event simulation. Invalid " +
                        "simulation configuration provided : " + configuration.toString());
            }
        } else {
            throw new InvalidConfigException("Simulation properties are required for event simulation. Invalid " +
                    "simulation configuration provided : " + configuration.toString());
        }
    }
}
