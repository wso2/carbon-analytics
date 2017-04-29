package org.wso2.carbon.event.simulator.core.internal.resourceManager;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.util.EventSimulatorConstants;
import org.wso2.carbon.event.simulator.core.internal.util.SimulationConfigUploader;
import org.wso2.carbon.event.simulator.core.service.EventSimulationMap;
import org.wso2.carbon.event.simulator.core.service.EventSimulator;
import org.wso2.carbon.stream.processor.common.ExecutionPlanDeployerReceiver;
import org.wso2.carbon.stream.processor.common.Resources;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.utils.Utils;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

/**
 * ResourceDependencyResolver manages the resource dependencies a simulation has
 */
public class ResourceDependencyResolver implements ExecutionPlanDeployerReceiver {
    private final Logger log = LoggerFactory.getLogger(ResourceDependencyResolver.class);
    private static ResourceDependencyResolver instance = new ResourceDependencyResolver();
    private HashMap<Resources.ResourceType, ResourceManager> resourceManagers = new HashMap<>();

    private ResourceDependencyResolver() {}

    public void resolveResourceDependency(Resources.ResourceType resourceType, String resourceName){
        /**
         * if there are any simulations waiting for a resource of type resourceType, retrieve the list of simulation
         * names and try to initialize simulators
         * */
        if (resourceManagers.containsKey(resourceType)) {
            ResourceManager resourceManager = resourceManagers.get(resourceType);
            if (resourceManager.contains(resourceName)) {
                List<String> simulationNames = resourceManager.get(resourceName);
                resourceManager.remove(resourceName);
                simulationNames.forEach(simulationName -> {
                    try {
                        String simulationConfig = SimulationConfigUploader.getConfigUploader().getSimulationConfig
                            (simulationName, (Paths.get(Utils.getCarbonHome().toString(),
                                    EventSimulatorConstants.DIRECTORY_DEPLOYMENT,
                                    EventSimulatorConstants.DIRECTORY_SIMULATION_CONFIGS)).toString());
                        if (!simulationConfig.isEmpty()) {
                            EventSimulator simulator = new EventSimulator(simulationName, simulationConfig);
                            EventSimulationMap.getSimulatorMap().put(simulationName, simulator);
                            log.info("Successfully deployed simulation '" + simulationName + "'.");
                        }
                    } catch (ResourceNotFoundException e) {
                        addResourceDependency(resourceType, resourceName, simulationName);
                    } catch (FileOperationsException | InsufficientAttributesException | InvalidConfigException e) {
//                        do nothing
                    }
                });
            }
        }
    }

    public void addResourceDependency(Resources.ResourceType resourceType, String resourceName, String simulationName) {
        if (resourceManagers.containsKey(resourceType)) {
            ResourceManager resourceManager = resourceManagers.get(resourceType);
            resourceManager.add(resourceName, simulationName);
        } else {
            ResourceManager resourceManager = new ResourceManager();
            resourceManager.add(resourceName, simulationName);
            resourceManagers.put(resourceType, resourceManager);
        }
    }

    public static ResourceDependencyResolver getInstance() {
        return instance;
    }

    /**
     * This bind method will be called when a class is registered against ExecutionPlanDeployerReceiver interface of
     * stream processor
     */
    @Reference(
            name = "resource.dependency.resolver",
            service = ExecutionPlanDeployerReceiver.class,
            cardinality = ReferenceCardinality.OPTIONAL,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "stopExecutionPlanDeployerReceiver"
    )
    protected void executionPlanDeployerReceiver(ExecutionPlanDeployerReceiver executionPlanDeployerReceiver) {
    }

    /**
     * This is the unbind method which gets called at the un-registration of eventStream OSGi service.
     */
    protected void stopExecutionPlanDeployerReceiver(ExecutionPlanDeployerReceiver executionPlanDeployerReceiver) {
        if (log.isDebugEnabled()) {
            log.info("@Reference(unbind) ExecutionPlanDeployerReceiver");
        }
    }
}
