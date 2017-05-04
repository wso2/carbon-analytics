package org.wso2.carbon.event.simulator.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.stream.processor.common.Resources;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.utils.Utils;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventSimulatorMap holds the simulators available
 */
public class EventSimulatorMap {
    private static final Logger log = LoggerFactory.getLogger(EventSimulatorMap.class);
    private static final EventSimulatorMap instance = new EventSimulatorMap();
    private final Map<String, Map<EventSimulator, String>> deployedSimulatorMap = new ConcurrentHashMap<>();
    private final Map<String, Map<Resources.ResourceType, String>> undeployedSimulatorMap = new
            ConcurrentHashMap<>();

    private EventSimulatorMap() {
    }

    public static EventSimulatorMap getInstance() {
        return instance;
    }

    public Map<String, Map<EventSimulator, String>> getDeployedSimulatorMap() {
        return deployedSimulatorMap;
    }

    public Map<String, Map<Resources.ResourceType, String>> getUndeployedSimulatorMap() {
        return undeployedSimulatorMap;
    }


    public void retryUndeployedSimulatorDeployment() {
        undeployedSimulatorMap.forEach((simulationName, resourceData) -> {
            try {
                String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                        (simulationName, (Paths.get(Utils.getCarbonHome().toString(),
                                EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                if (!simulationConfig.isEmpty()) {
                    EventSimulator eventSimulator = new EventSimulator(simulationName, simulationConfig);
                    undeployedSimulatorMap.remove(simulationName);
                    deployedSimulatorMap.put(simulationName,
                            Collections.singletonMap(eventSimulator, simulationConfig));
                    log.info("Successfully deployed simulation '" + simulationName + "'.");
                }
            } catch (ResourceNotFoundException e) {
                if (!getResourceTypeForUndeployedSimulator(simulationName).equals(e
                        .getResourceType()) || !getResourceNameForUndeployedSimulator(simulationName)
                        .equals(e.getResourceName())) {
                    undeployedSimulatorMap.remove(simulationName);
                    undeployedSimulatorMap.put(simulationName, Collections
                            .singletonMap(e.getResourceType(), e.getResourceName()));
                    log.error(e.getMessage(), e);
                }
            } catch (FileOperationsException | InvalidConfigException | InsufficientAttributesException e) {
                log.error(e.getMessage(), e);
            }
        });
    }


    public void retrySimulatorDeployment() {
//        use deployedSimulations List to ensure that we don't revalidate an undeployed simulation that got deployed
        List<String> deployedSimulations = new ArrayList<>();
        undeployedSimulatorMap.forEach((simulationName, resourceData) -> {
            try {
                String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                        (simulationName, (Paths.get(Utils.getCarbonHome().toString(),
                                EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                if (!simulationConfig.isEmpty()) {
                    EventSimulator eventSimulator = new EventSimulator(simulationName, simulationConfig);
                    undeployedSimulatorMap.remove(simulationName);
                    deployedSimulatorMap.put(simulationName,
                            Collections.singletonMap(eventSimulator, simulationConfig));
                    deployedSimulations.add(simulationName);
                    log.info("Successfully deployed simulation '" + simulationName + "'.");
                }
            } catch (ResourceNotFoundException e) {
                if (!getResourceTypeForUndeployedSimulator(simulationName).equals(e.getResourceType())
                        || !getResourceNameForUndeployedSimulator(simulationName).equals(e.getResourceName())) {
                    undeployedSimulatorMap.remove(simulationName);
                    undeployedSimulatorMap.put(simulationName,
                            Collections.singletonMap(e.getResourceType(), e.getResourceName()));
                    log.error(e.getMessage(), e);
                }
            } catch (FileOperationsException | InvalidConfigException | InsufficientAttributesException e) {
                undeployedSimulatorMap.remove(simulationName);
                log.error("Error occurred when deploying simulation '" + simulationName + "'.", e);
            }
        });
        deployedSimulatorMap.forEach((simulationName, simulatorData) -> {
            try {
                if (!deployedSimulations.contains(simulationName)) {
                    EventSimulator.validateSimulationConfig((String) simulatorData.values().toArray()[0]);
                }
            } catch (ResourceNotFoundException e) {
                getDeployedSimulator(simulationName).stop();
                deployedSimulatorMap.remove(simulationName);
                undeployedSimulatorMap.put(simulationName,
                        Collections.singletonMap(e.getResourceType(), e.getResourceName()));
                log.error(e.getResourceTypeString() + " '" + e.getResourceName() + "' required for simulation '" +
                        simulationName + "' cannot be found. ", e);
                log.info("Undeploy simulation '" + simulationName + "'.");
            } catch (InvalidConfigException | InsufficientAttributesException e) {
                getDeployedSimulator(simulationName).stop();
                deployedSimulatorMap.remove(simulationName);
                log.info("Simulation configuration of simulator '" + simulationName + "' is no longer valid. " +
                        "Undeploy simulation '" + simulationName + "'.", e);
            }
        });
    }

    public EventSimulator getDeployedSimulator(String simulationName) {
        if (deployedSimulatorMap.containsKey(simulationName)) {
            return ((EventSimulator) deployedSimulatorMap.get(simulationName).keySet().toArray()[0]);
        } else {
            return null;
        }
    }

    public Resources.ResourceType getResourceTypeForUndeployedSimulator(String simulationName) {
        if (undeployedSimulatorMap.containsKey(simulationName)) {
            return (Resources.ResourceType) undeployedSimulatorMap.get(simulationName).keySet().toArray()[0];
        } else {
            return null;
        }
    }

    public String getResourceNameForUndeployedSimulator(String simulationName) {
        if (undeployedSimulatorMap.containsKey(simulationName)) {
            return (String) undeployedSimulatorMap.get(simulationName).values().toArray()[0];
        } else {
            return null;
        }
    }

    public boolean containsDeployedSimulator(String simulationName) {
        return deployedSimulatorMap.containsKey(simulationName);
    }

    public boolean containsUndeployedSimulator(String simulationName) {
        return undeployedSimulatorMap.containsKey(simulationName);
    }

    public void stopDeployedSimulation(String simulationName) {
        if (containsDeployedSimulator(simulationName)) {
            getDeployedSimulator(simulationName).stop();
        }
    }

    public void stopAllDeployedSimulations() {
        deployedSimulatorMap.forEach((simulationName, simulatorData) -> getDeployedSimulator(simulationName).stop());
    }

    public void deleteDeployedSimulation(String simulationName) {
        if (deployedSimulatorMap.containsKey(simulationName)) {
            deployedSimulatorMap.remove(simulationName);
        }
    }

    public void deleteUndeployedSimulation(String simulationName) {
        if (undeployedSimulatorMap.containsKey(simulationName)) {
            undeployedSimulatorMap.remove(simulationName);
        }
    }
}

