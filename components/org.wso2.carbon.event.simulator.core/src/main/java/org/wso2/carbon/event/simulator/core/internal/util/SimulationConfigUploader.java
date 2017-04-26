package org.wso2.carbon.event.simulator.core.internal.util;

import static org.wso2.carbon.event.simulator.core.internal.util.CommonOperations.checkAvailability;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.event.simulator.core.exception.FileAlreadyExistsException;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This class is responsible for saving, modifying and deleting the simulation configurations
 */
public class SimulationConfigUploader {
    private static final Logger log = Logger.getLogger(SimulationConfigUploader.class);
    private static final SimulationConfigUploader configUploader =
            new SimulationConfigUploader();

    private SimulationConfigUploader() {}

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
     * @param destination destination where the simulation configuration must be stored
     * @throws FileOperationsException    if an IOException occurs while copying uploaded stream to
     *                                    'deployment/simulationConfigs' directory
     */
    public void uploadSimulationConfig(String simulationConfig, String destination) throws FileOperationsException,
            InvalidConfigException {
        String simulationName = getSimulationName(simulationConfig);
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(destination, (simulationName + "." +
                        EventSimulatorConstants.SIMULATION_FILE_EXTENSION)))) {
            writer.write(simulationConfig);
            if (log.isDebugEnabled()) {
                log.debug("Successfully uploaded simulation configuration '" + simulationName + "'.");
            }
        } catch (IOException e) {
            log.error("Error occurred while copying the file '" + simulationName + "'.", e);
            throw new FileOperationsException("Error occurred while copying the file '" + simulationName +
                    "'. ", e);
        }
    }

    /**
     * Method to delete an uploaded simulation configuration.
     *
     * @param simulationName name of simulation to be deleted
     * @param destination location where the simulation is saved
     * @throws FileOperationsException if an IOException occurs while deleting file
     */
    public void deleteSimulationConfig(String simulationName, String destination) throws FileOperationsException {
        try {
            Files.deleteIfExists(Paths.get(destination, simulationName +  "." +
                    EventSimulatorConstants.SIMULATION_FILE_EXTENSION));
            if (log.isDebugEnabled()) {
                log.debug("Deleted simulation configuration '" + simulationName + "'.");
            }
        } catch (IOException e) {
            log.error("Error occurred while deleting the simulation configuration '" + simulationName +
                    "'.", e);
            throw new FileOperationsException("Error occurred while deleting the simulation configuration '" +
                    simulationName + "'.'", e);
        }
    }

    /**
     * getSimulationConfig() is used to retrieve an uploaded simulation configuration
     *
     * @param simulationName name of simulation to be retrieved
     * @param destination    where the simulation configuration is stored
     * @return simulation configuration
     * @throws FileOperationsException is an error occurs when reading simulation configuration file
     */
    public String getSimulationConfig(String simulationName, String destination) throws FileOperationsException {
        try {
            return new String(Files.readAllBytes(Paths.get(destination, (simulationName +  "." +
                    EventSimulatorConstants.SIMULATION_FILE_EXTENSION))),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Error occurred while reading the simulation configuration '" +
                    simulationName + "'.", e);
            throw new FileOperationsException("Error occurred while reading the simulation configuration '" +
                    simulationName + "'.", e);
        }
    }

    /**
     * getSimulationName() is used to retrieve the simulation name of a simulation configuration
     *
     * @param simulationConfig simulation configuration
     * @return simulation configuration name
     * @throws InvalidConfigException if the simulation configuration doesnt contain a simulation name
     */
    public String getSimulationName(String simulationConfig) throws InvalidConfigException {
        try {
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
        } catch (JSONException e) {
            throw new InvalidConfigException("Invalid simulation configuration provided : " + simulationConfig, e);
        }
    }
}
