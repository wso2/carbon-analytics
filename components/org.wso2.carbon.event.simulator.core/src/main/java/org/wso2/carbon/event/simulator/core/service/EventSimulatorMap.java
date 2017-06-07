package org.wso2.carbon.event.simulator.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.utils.Utils;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventSimulatorMap holds the simulators available
 */
public class EventSimulatorMap {
    private static final Logger log = LoggerFactory.getLogger(EventSimulatorMap.class);
    private static final EventSimulatorMap instance = new EventSimulatorMap();
    private final Map<String, Map<EventSimulator, String>> activeSimulatorMap = new ConcurrentHashMap<>();
    private final Map<String, Map<ResourceNotFoundException.ResourceType, String>> inActiveSimulatorMap = new
            ConcurrentHashMap<>();

    private EventSimulatorMap() {
    }

    public static EventSimulatorMap getInstance() {
        return instance;
    }

    public Map<String, Map<EventSimulator, String>> getActiveSimulatorMap() {
        return activeSimulatorMap;
    }

    public Map<String, Map<ResourceNotFoundException.ResourceType, String>> getInActiveSimulatorMap() {
        return inActiveSimulatorMap;
    }


    /**
     * retryInActiveSimulatorDeployment() retries to create simulator objects from inactive simulation
     * configurations which resulted in ResourceNotFoundException
     * */
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
                    log.info("Changed status of simulation '" + simulationName + "' from inactive to active.");
                }
            } catch (ResourceNotFoundException e) {
                /*
                 * check whether the resource missing is the same as previous. if not, update the entry in
                 * inactiveSimulation map.
                 * This check avoids logging errors if the same resource is missing in every retry
                 * */
                if (!getResourceTypeForInActiveSimulator(simulationName).equals(e.getResourceType())
                        || !getResourceNameForInActiveSimulator(simulationName).equals(e.getResourceName())) {
                    inActiveSimulatorMap.put(simulationName,
                            Collections.singletonMap(e.getResourceType(), e.getResourceName()));
                    log.error(e.getMessage(), e);
                }
            } catch (FileOperationsException | InvalidConfigException | InsufficientAttributesException e) {
                inActiveSimulatorMap.remove(simulationName);
                log.error(e.getMessage(), e);
            }
        });
    }

    /**
     * retryActiveSimulatorDeployment() validates whether the active simulation configurations are still
     * valid.
     * */
    public void retryActiveSimulatorDeployment() {
        activeSimulatorMap.forEach((simulationName, simulatorData) -> {
            try {
                EventSimulator.validateSimulationConfig((String) simulatorData.values().toArray()[0]);
            } catch (ResourceNotFoundException e) {
                stopActiveSimulation(simulationName);
                activeSimulatorMap.remove(simulationName);
                inActiveSimulatorMap.put(simulationName,
                        Collections.singletonMap(e.getResourceType(), e.getResourceName()));
                log.error(e.getMessage(), e);
                log.info("Changed status of simulation '" + simulationName + "' from active to inactive.");
            } catch (InvalidConfigException | InsufficientAttributesException e) {
                stopActiveSimulation(simulationName);
                activeSimulatorMap.remove(simulationName);
                log.info("Simulation configuration of active simulation '" + simulationName + "' is no longer valid. "
                        , e);
            }
        });
    }


    /**
     * retrySimulatorDeployment() revalidates active simulations and retries inactive simulations
     * */
    public void retrySimulatorDeployment() {
        retryActiveSimulatorDeployment();
        retryInActiveSimulatorDeployment();
    }

    /**
     * getActiveSimulator() retrieves a simulator object
     *
     * @param simulationName name of simulator
     * @return simulator object
     * */
    public EventSimulator getActiveSimulator(String simulationName) {
        if (activeSimulatorMap.containsKey(simulationName)) {
            return ((EventSimulator) activeSimulatorMap.get(simulationName).keySet().toArray()[0]);
        } else {
            return null;
        }
    }

    /**
     * getResourceTypeForInActiveSimulator() retrieves the resource type which is required by the inactive simulation
     *
     * @param simulationName name of inactive simulation
     * @return resource type
     * */
    public ResourceNotFoundException.ResourceType getResourceTypeForInActiveSimulator(String simulationName) {
        if (inActiveSimulatorMap.containsKey(simulationName)) {
            return (ResourceNotFoundException.ResourceType)
                    inActiveSimulatorMap.get(simulationName).keySet().toArray()[0];
        } else {
            return null;
        }
    }

    /**
     * getResourceNameForInActiveSimulator() retrieves the name of resource required by the inactive simulation
     *
     * @param simulationName name of inactive simulation
     * @return resource name
     * */
    public String getResourceNameForInActiveSimulator(String simulationName) {
        if (inActiveSimulatorMap.containsKey(simulationName)) {
            return (String) inActiveSimulatorMap.get(simulationName).values().toArray()[0];
        } else {
            return null;
        }
    }

    /**
     * containsActiveSimulator() checks whether an active simulation exists
     *
     * @param simulationName name of simulation
     * @return true if an active simulation exists, else false
     * */
    public boolean containsActiveSimulator(String simulationName) {
        return activeSimulatorMap.containsKey(simulationName);
    }

    /**
     * containsInActiveSimulator() checks whether an inactive simulation exists
     *
     * @param simulationName name of simulation
     * @return true if an inactive simulation exists, else false
     * */
    public boolean containsInActiveSimulator(String simulationName) {
        return inActiveSimulatorMap.containsKey(simulationName);
    }

    /**
     * stopActiveSimulation() stops a simulation
     *
     * @param simulationName name os simulation
     * */
    public void stopActiveSimulation(String simulationName) {
        if (containsActiveSimulator(simulationName)) {
            getActiveSimulator(simulationName).stop();
        }
    }

    /**
     * stopAllActiveSimulations() stops all active simulations
     * */
    public void stopAllActiveSimulations() {
        activeSimulatorMap.forEach((simulationName, simulatorData) -> getActiveSimulator(simulationName).stop());
    }

    /**
     * deleteActiveSimulation() deletes an active simulation
     *
     * @param simulationName name of simulation being deleted
     * */
    public void deleteActiveSimulation(String simulationName) {
        if (activeSimulatorMap.containsKey(simulationName)) {
            activeSimulatorMap.remove(simulationName);
        }
    }

    /**
     * deleteInActiveSimulation() deletes an inactive simulation
     *
     * @param simulationName name of simulation being deleted
     * */
    public void deleteInActiveSimulation(String simulationName) {
        if (inActiveSimulatorMap.containsKey(simulationName)) {
            inActiveSimulatorMap.remove(simulationName);
        }
    }
}

