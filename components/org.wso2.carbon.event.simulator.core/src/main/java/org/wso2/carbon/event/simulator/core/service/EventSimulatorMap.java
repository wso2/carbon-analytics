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
    private final Map<String, Map<EventSimulator, String>> activeSimulatorMap = new ConcurrentHashMap<>();
    private final Map<String, Map<Resources.ResourceType, String>> inActiveSimulatorMap = new
            ConcurrentHashMap<>();

    private EventSimulatorMap() {
    }

    public static EventSimulatorMap getInstance() {
        return instance;
    }

    public Map<String, Map<EventSimulator, String>> getActiveSimulatorMap() {
        return activeSimulatorMap;
    }

    public Map<String, Map<Resources.ResourceType, String>> getInActiveSimulatorMap() {
        return inActiveSimulatorMap;
    }


    public void retryInActiveSimulatorDeployment() {
        inActiveSimulatorMap.forEach((simulationName, resourceData) -> {
            try {
                String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                        (simulationName, (Paths.get(Utils.getCarbonHome().toString(),
                                EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                if (!simulationConfig.isEmpty()) {
                    EventSimulator eventSimulator = new EventSimulator(simulationName, simulationConfig);
                    inActiveSimulatorMap.remove(simulationName);
                    activeSimulatorMap.put(simulationName,
                            Collections.singletonMap(eventSimulator, simulationConfig));
                    log.info("Successfully deployed simulation '" + simulationName + "'.");
                }
            } catch (ResourceNotFoundException e) {
                if (!getResourceTypeForInActiveSimulator(simulationName).equals(e
                        .getResourceType()) || !getResourceNameForInActiveSimulator(simulationName)
                        .equals(e.getResourceName())) {
                    inActiveSimulatorMap.remove(simulationName);
                    inActiveSimulatorMap.put(simulationName, Collections
                            .singletonMap(e.getResourceType(), e.getResourceName()));
                    log.error(e.getMessage(), e);
                }
            } catch (FileOperationsException | InvalidConfigException | InsufficientAttributesException e) {
                log.error(e.getMessage(), e);
            }
        });
    }


    public void retrySimulatorDeployment() {
//        use activeSimulations List to ensure that we don't revalidate an inactive simulation that got activated
        List<String> activeSimulations = new ArrayList<>();
        inActiveSimulatorMap.forEach((simulationName, resourceData) -> {
            try {
                String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                        (simulationName, (Paths.get(Utils.getCarbonHome().toString(),
                                EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                if (!simulationConfig.isEmpty()) {
                    EventSimulator eventSimulator = new EventSimulator(simulationName, simulationConfig);
                    inActiveSimulatorMap.remove(simulationName);
                    activeSimulatorMap.put(simulationName,
                            Collections.singletonMap(eventSimulator, simulationConfig));
                    activeSimulations.add(simulationName);
                    log.info("Successfully deployed simulation '" + simulationName + "'.");
                }
            } catch (ResourceNotFoundException e) {
                if (!getResourceTypeForInActiveSimulator(simulationName).equals(e.getResourceType())
                        || !getResourceNameForInActiveSimulator(simulationName).equals(e.getResourceName())) {
                    inActiveSimulatorMap.remove(simulationName);
                    inActiveSimulatorMap.put(simulationName,
                            Collections.singletonMap(e.getResourceType(), e.getResourceName()));
                    log.error(e.getMessage(), e);
                }
            } catch (FileOperationsException | InvalidConfigException | InsufficientAttributesException e) {
                inActiveSimulatorMap.remove(simulationName);
                log.error("Error occurred when deploying simulation '" + simulationName + "'.", e);
            }
        });
        activeSimulatorMap.forEach((simulationName, simulatorData) -> {
            try {
                if (!activeSimulations.contains(simulationName)) {
                    EventSimulator.validateSimulationConfig((String) simulatorData.values().toArray()[0]);
                }
            } catch (ResourceNotFoundException e) {
                getActiveSimulator(simulationName).stop();
                activeSimulatorMap.remove(simulationName);
                inActiveSimulatorMap.put(simulationName,
                        Collections.singletonMap(e.getResourceType(), e.getResourceName()));
                log.error(e.getResourceTypeString() + " '" + e.getResourceName() + "' required for simulation '" +
                        simulationName + "' cannot be found. ", e);
                log.info("Undeploy simulation '" + simulationName + "'.");
            } catch (InvalidConfigException | InsufficientAttributesException e) {
                getActiveSimulator(simulationName).stop();
                activeSimulatorMap.remove(simulationName);
                log.info("Simulation configuration of simulator '" + simulationName + "' is no longer valid. " +
                        "Undeploy simulation '" + simulationName + "'.", e);
            }
        });
    }

    public EventSimulator getActiveSimulator(String simulationName) {
        if (activeSimulatorMap.containsKey(simulationName)) {
            return ((EventSimulator) activeSimulatorMap.get(simulationName).keySet().toArray()[0]);
        } else {
            return null;
        }
    }

    public Resources.ResourceType getResourceTypeForInActiveSimulator(String simulationName) {
        if (inActiveSimulatorMap.containsKey(simulationName)) {
            return (Resources.ResourceType) inActiveSimulatorMap.get(simulationName).keySet().toArray()[0];
        } else {
            return null;
        }
    }

    public String getResourceNameForInActiveSimulator(String simulationName) {
        if (inActiveSimulatorMap.containsKey(simulationName)) {
            return (String) inActiveSimulatorMap.get(simulationName).values().toArray()[0];
        } else {
            return null;
        }
    }

    public boolean containsActiveSimulator(String simulationName) {
        return activeSimulatorMap.containsKey(simulationName);
    }

    public boolean containsInActiveSimulator(String simulationName) {
        return inActiveSimulatorMap.containsKey(simulationName);
    }

    public void stopActiveSimulation(String simulationName) {
        if (containsActiveSimulator(simulationName)) {
            getActiveSimulator(simulationName).stop();
        }
    }

    public void stopAllActiveSimulations() {
        activeSimulatorMap.forEach((simulationName, simulatorData) -> getActiveSimulator(simulationName).stop());
    }

    public void deleteActiveSimulation(String simulationName) {
        if (activeSimulatorMap.containsKey(simulationName)) {
            activeSimulatorMap.remove(simulationName);
        }
    }

    public void deleteInActiveSimulation(String simulationName) {
        if (inActiveSimulatorMap.containsKey(simulationName)) {
            inActiveSimulatorMap.remove(simulationName);
        }
    }
}

