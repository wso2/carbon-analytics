package org.wso2.carbon.event.simulator.core.internal.resourceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExecutionPlanResourceManager manages simulation dependencies on execution plans
 */
public class ExecutionPlanResourceManager implements ResourceManager {
    private static ExecutionPlanResourceManager instance = new ExecutionPlanResourceManager();
//    executionPlanDependencyMap holds <executionPlanName, ArrayList<simulationNames>>
    private final ConcurrentHashMap<String, ArrayList<String>> executionPlanDependencyMap = new ConcurrentHashMap<>();

    private ExecutionPlanResourceManager(){}

    /**
     * add() is used to add a simulation to the executionPlanDependencyMap
     *
     * @param resourceName name of execution plan
     * @param simulationName name of simulation
     * */
    @Override
    public void add(String resourceName, String simulationName) {
        if (executionPlanDependencyMap.containsKey(resourceName)) {
            executionPlanDependencyMap.get(resourceName).add(simulationName);
        } else {
            executionPlanDependencyMap.put(resourceName, new ArrayList<>(Collections.singletonList(simulationName)));
        }
    }

    /**
     * contains() is used to check whether there is a simulation that depends on the specified execution plan
     *
     * @param resourceName name of stream
     * @return true if there are any simulations depending on the execution plan, else return false
     * */
    @Override
    public boolean contains(String resourceName) {
        return executionPlanDependencyMap.containsKey(resourceName);
    }

    /**
     * get() is used to retrieve the names of simulations that require on the execution plan
     *
     * @param resourceName name of execution plan
     * @return if there are any simulation that require the execution plan, return a list of names of simulation that
     * depend on the csv file, else return null
     * */
    @Override
    public List<String> get(String resourceName) {
        if (executionPlanDependencyMap.containsKey(resourceName)) {
            return executionPlanDependencyMap.get(resourceName);
        } else {
            return null;
        }
    }

    /**
     * remove() removes a resource dependency entry
     *
     * @param resourceName name of execution plan whose dependencies are being removed
     * */
    @Override
    public void remove(String resourceName) {
        if (executionPlanDependencyMap.containsKey(resourceName)) {
            executionPlanDependencyMap.remove(resourceName);
        }
    }

    public static ExecutionPlanResourceManager getInstance() {
        return instance;
    }
}
