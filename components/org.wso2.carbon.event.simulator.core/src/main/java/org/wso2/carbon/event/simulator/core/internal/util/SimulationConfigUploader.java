package org.wso2.carbon.event.simulator.core.internal.util;


import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;

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

    private SimulationConfigUploader(SimulationConfigStore simulationConfigStore) {
        EventSimulatorDataHolder.getInstance().setSimulationConfigStore(simulationConfigStore);
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
     * @param simulationConfig simulation configuration being uploaded
     * @throws FileAlreadyExistsException if the file exists in 'tmp/simulationConfigs' directory
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'tmp/simulationConfigs' directory
     */
    public void uploadSimulationConfig(String simulationConfig) throws FileAlreadyExistsException,
            FileOperationsException, InvalidConfigException {
        /**
         * check whether the simulation already exists.
         * if so log it exists.
         * else, add the file
         * */

        JSONObject configuration = new JSONObject(simulationConfig);
        if (configuration.has(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)
                && !configuration.isNull(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)) {
            if (checkAvailability(configuration.getJSONObject(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES),
                    EventSimulatorConstants.EVENT_SIMULATION_NAME)) {
                String simulationName = configuration.getJSONObject(EventSimulatorConstants.EVENT_SIMULATION_PROPERTIES)
                        .getString(EventSimulatorConstants.EVENT_SIMULATION_NAME);
                if (!EventSimulatorDataHolder.getInstance().getSimulationConfigStore().checkExists(simulationName)) {
                    if (log.isDebugEnabled()) {
                    log.debug("Initialize a File writer for simulation configuration '" + simulationName + "'.");
                    }
                    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(System.getProperty("java.io.tmpdir"),
                        EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS, simulationName))) {
                        writer.write(simulationConfig);
                        EventSimulatorDataHolder.getInstance().getSimulationConfigStore()
                                .addSimulationConfig(simulationName);
                        if (log.isDebugEnabled()) {
                            log.debug("Successfully uploaded simulation configuration '" + simulationName + "' to " +
                                    "directory " + (Paths.get(System.getProperty("java.io.tmpdir"),
                                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                        }
                    } catch (IOException e) {
                        log.error("Error occurred while copying the file '" + simulationName + "' to " +
                                "location '" + Paths.get(System.getProperty("java.io.tmpdir"),
                                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS, simulationName).toString() +
                                "'. ", e);
                        throw new FileOperationsException("Error occurred while copying the file '" + simulationName +
                            "' to location '" + Paths.get(System.getProperty("java.io.tmpdir"), EventSimulatorConstants
                            .DIRECTORY_NAME, simulationName).toString() + "'. ", e);
                    }
                } else {
                    log.error("Simulation configuration '" + simulationName + "' already exists in " +
                            (Paths.get(System.getProperty("java.io.tmpdir"),
                                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                    throw new FileAlreadyExistsException("Simulation configuration '" + simulationName + "'" +
                            " already exists in " + (Paths.get(System.getProperty("java.io.tmpdir"),
                                EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                }
            } else {
                throw new InvalidConfigException("Simulation name is required for event simulation. Invalid " +
                        "simulation configuration provided : " + configuration.toString());
            }
        } else {
            throw new InvalidConfigException("Simulation properties are required for event simulation. Invalid " +
                    "simulation configuration provided : " + configuration.toString());
        }
    }

    /**
     * Method to delete an uploaded simulation configuration.
     *
     * @param simulationName name of simulation to be deleted
     * @throws FileOperationsException if an IOException occurs while deleting file
     */
    public boolean deleteSimulationConfig(String simulationName) throws FileOperationsException {
        try {
            if (EventSimulatorDataHolder.getInstance().getSimulationConfigStore().checkExists(simulationName)) {
                EventSimulatorDataHolder.getInstance().getSimulationConfigStore().
                        removeSimulationConfig(simulationName);
                Files.deleteIfExists(Paths.get(System.getProperty("java.io.tmpdir"),
                        EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS, simulationName));
                if (log.isDebugEnabled()) {
                    log.debug("Deleted simulation configuration '" + simulationName + "'");
                }
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            log.error("Error occurred while deleting the simulation configuration '" +
                    simulationName + "'. ", e);
            throw new FileOperationsException("Error occurred while deleting the simulation configuration '" +
                    simulationName + "'. ", e);
        }
    }

    public String getSimulationConfig(String simulationName) throws FileOperationsException {
        try {
            if (EventSimulatorDataHolder.getInstance().getSimulationConfigStore().checkExists(simulationName)) {
                return new String(Files.readAllBytes(Paths.get(System.getProperty("java.io.tmpdir"),
                        EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS, simulationName)), "UTF-8");
            } else {
                return null;
            }
        } catch (IOException e) {
            log.error("Error occurred while reading the simulation configuration '" +
                    simulationName + "'. ", e);
            throw new FileOperationsException("Error occurred while reading the simulation configuration '" +
                    simulationName + "'. ", e);
        }
    }
}
