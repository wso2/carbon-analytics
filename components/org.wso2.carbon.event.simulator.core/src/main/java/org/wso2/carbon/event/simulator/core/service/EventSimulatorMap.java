package org.wso2.carbon.event.simulator.core.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.event.simulator.core.service.bean.ActiveSimulatorData;
import org.wso2.carbon.event.simulator.core.service.bean.ResourceDependencyData;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.utils.Utils;

import java.nio.file.Paths;
import java.util.HashMap;

/**
 * EventSimulatorMap holds the simulators available
 */
public class EventSimulatorMap {
    private static final Logger log = LoggerFactory.getLogger(EventSimulatorMap.class);
    private static final EventSimulatorMap instance = new EventSimulatorMap();
    private final HashMap<String, ActiveSimulatorData> activeSimulatorMap = new HashMap<>();
    private final HashMap<String, ResourceDependencyData> inActiveSimulatorMap = new HashMap<>();

    private EventSimulatorMap() {
    }

    public static EventSimulatorMap getInstance() {
        return instance;
    }

    public HashMap<String, ActiveSimulatorData> getActiveSimulatorMap() {
        return activeSimulatorMap;
    }

    public HashMap<String, ResourceDependencyData> getInActiveSimulatorMap() {
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
                    activeSimulatorMap.put(simulationName, new ActiveSimulatorData(eventSimulator, simulationConfig));
                    log.info("Changed status of simulation '" + simulationName + "' from inactive to active.");
                }
            } catch (ResourceNotFoundException e) {
                /*
                 * check whether the resource missing is the same as previous. if not, update the entry in
                 * inactiveSimulation map.
                 * This check avoids logging errors if the same resource is missing in every retry
                 * */
                ResourceDependencyData newDependency =
                        new ResourceDependencyData(e.getResourceType(), e.getResourceName());
                if (!resourceData.equals(newDependency)) {
                    inActiveSimulatorMap.put(simulationName, newDependency);
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
    public void checkValidityOfActiveSimAfterDependency() {
        activeSimulatorMap.forEach((simulationName, simulatorData) -> {
            try {
                EventSimulator.validateSimulationConfig(simulatorData.getSimulationConfig());
            } catch (ResourceNotFoundException e) {
                simulatorData.getEventSimulator().stop();
                activeSimulatorMap.remove(simulationName);
                inActiveSimulatorMap.put(simulationName, new ResourceDependencyData(e.getResourceType(),
                        e.getResourceName()));
                log.error(e.getMessage(), e);
                log.info("Changed status of simulation '" + simulationName + "' from active to inactive.");
            } catch (InvalidConfigException | InsufficientAttributesException e) {
                simulatorData.getEventSimulator().stop();
                activeSimulatorMap.remove(simulationName);
                log.info("Simulation configuration of active simulation '" + simulationName + "' is no longer valid. "
                        , e);
            }
        });
    }


    /**
     * retrySimulatorDeployment() revalidates active simulations and retries inactive simulations
     * */
    public void checkValidityAfterDependency() {
        checkValidityOfActiveSimAfterDependency();
        retryInActiveSimulatorDeployment();
    }


    /**
     * stopAllActiveSimulations() stops all active simulations
     * */
    public void stopAllActiveSimulations() {
        activeSimulatorMap.forEach((simulationName, simulatorData) -> simulatorData.getEventSimulator().stop());
    }
}

