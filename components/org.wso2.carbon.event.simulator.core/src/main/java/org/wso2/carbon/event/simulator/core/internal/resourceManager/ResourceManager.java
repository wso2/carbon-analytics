package org.wso2.carbon.event.simulator.core.internal.resourceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ResourceManager class defines methods used to manage resources required for event simulation
 */
public class ResourceManager {
    //    resourceDependencyMap holds <resourceNames, ArrayList<simulationNames>>
    private final ConcurrentHashMap<String, ArrayList<String>> resourceDependencyMap = new ConcurrentHashMap<>();

    public ResourceManager() {
    }

    /**
     * add() is used to add a simulation to the resourceDependencyMap
     *
     * @param resourceName name of resource
     * @param simulationName name of simulation
     * */
    public void add(String resourceName, String simulationName) {
        if (resourceDependencyMap.containsKey(resourceName)) {
            resourceDependencyMap.get(resourceName).add(simulationName);
        } else {
            resourceDependencyMap.put(resourceName, new ArrayList<>(Collections.singletonList(simulationName)));
        }
    }

    /**
     * contains() is used to check whether there is a simulation that depends on the specified resource
     *
     * @param resourceName name of stream
     * @return true if there are any simulations depending on the resource, else return false
     * */
    public boolean contains(String resourceName) {
        return resourceDependencyMap.containsKey(resourceName);
    }

    /**
     * get() is used to retrieve the names of simulations that require on the resource
     *
     * @param resourceName name of resource
     * @return if there are any simulation that require the resource, return a list of names of simulation that depend
     * on the resource, else return null
     * */
    public List<String> get(String resourceName) {
        if (resourceDependencyMap.containsKey(resourceName)) {
            return resourceDependencyMap.get(resourceName);
        } else {
            return null;
        }
    }

    /**
     * remove() removes a resource dependency entry
     *
     * @param resourceName name of resource whose dependencies are being removed
     * */
    public void remove(String resourceName) {
        if (resourceDependencyMap.containsKey(resourceName)) {
            resourceDependencyMap.remove(resourceName);
        }
    }

}
