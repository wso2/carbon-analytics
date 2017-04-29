package org.wso2.carbon.event.simulator.core.internal.resourceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StreamResourceManager manages simulation dependencies on streams
 */
public class StreamResourceManager implements ResourceManager{
    private static StreamResourceManager instance = new StreamResourceManager();
    //    streamDependencyMap holds <streamName, ArrayList<simulationNames>>
    private final ConcurrentHashMap<String, ArrayList<String>> streamDependencyMap = new ConcurrentHashMap<>();

    private StreamResourceManager() {}

    /**
     * add() is used to add a simulation to the streamDependencyMap
     *
     * @param resourceName name of stream
     * @param simulationName name of simulation
     * */
    @Override
    public void add(String resourceName, String simulationName) {
        if (streamDependencyMap.containsKey(resourceName)) {
            streamDependencyMap.get(resourceName).add(simulationName);
        } else {
            streamDependencyMap.put(resourceName, new ArrayList<>(Collections.singletonList(simulationName)));
        }
    }

    /**
     * contains() is used to check whether there is a simulation that depends on the specified stream
     *
     * @param resourceName name of stream
     * @return true if there are any simulations depending on the stream, else return false
     * */
    @Override
    public boolean contains(String resourceName) {
        return streamDependencyMap.containsKey(resourceName);
    }

    /**
     * get() is used to retrieve the names of simulations that require on the stream
     *
     * @param resourceName name of stream
     * @return if there are any simulation that require the stream, return a list of names of simulation that
     * depend on the csv file, else return null
     * */
    @Override
    public List<String> get(String resourceName) {
        if (streamDependencyMap.containsKey(resourceName)) {
            return streamDependencyMap.get(resourceName);
        } else {
            return null;
        }
    }

    /**
     * remove() removes a resource dependency entry
     *
     * @param resourceName name of stream whose dependencies are being removed
     * */
    @Override
    public void remove(String resourceName) {
        if (streamDependencyMap.containsKey(resourceName)) {
            streamDependencyMap.remove(resourceName);
        }
    }

    public static StreamResourceManager getInstance() {
        return instance;
    }
}
