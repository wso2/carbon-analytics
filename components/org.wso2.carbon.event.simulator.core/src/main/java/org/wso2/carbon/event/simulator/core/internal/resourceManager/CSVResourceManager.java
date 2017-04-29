package org.wso2.carbon.event.simulator.core.internal.resourceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CSVResourceManager manages simulation dependencies on csv files
 */
public class CSVResourceManager implements ResourceManager {
    private static CSVResourceManager instance = new CSVResourceManager();
    //    csvFileDependencyMap holds <csvFileNames, ArrayList<simulationNames>>
    private final ConcurrentHashMap<String, ArrayList<String>> csvFileDependencyMap = new ConcurrentHashMap<>();

    private CSVResourceManager() {}

    /**
     * add() is used to add a simulation to the csvFileDependencyMap
     *
     * @param resourceName name of csv file
     * @param simulationName name of simulation
     * */
    @Override
    public void add(String resourceName, String simulationName) {
        if (csvFileDependencyMap.containsKey(resourceName)) {
            csvFileDependencyMap.get(resourceName).add(simulationName);
        } else {
            csvFileDependencyMap.put(resourceName, new ArrayList<>(Collections.singletonList(simulationName)));
        }
    }

    /**
     * contains() is used to check whether there is a simulation that depends on the specified csv file
     *
     * @param resourceName name of stream
     * @return true if there are any simulations depending on the csv file, else return false
     * */
    @Override
    public boolean contains(String resourceName) {
        return csvFileDependencyMap.containsKey(resourceName);
    }

    /**
     * get() is used to retrieve the names of simulations that require on the csv file
     *
     * @param resourceName name of csv file
     * @return if there are any simulation that require the csv file, return a list of names of simulation that depend
     * on the csv file, else return null
     * */
    @Override
    public List<String> get(String resourceName) {
        if (csvFileDependencyMap.containsKey(resourceName)) {
            return csvFileDependencyMap.get(resourceName);
        } else {
            return null;
        }
    }

    /**
     * remove() removes a resource dependency entry
     *
     * @param resourceName name of csv file whose dependencies are being removed
     * */
    @Override
    public void remove(String resourceName) {
        if (csvFileDependencyMap.containsKey(resourceName)) {
            csvFileDependencyMap.remove(resourceName);
        }
    }

    public static CSVResourceManager getInstance() {
        return instance;
    }
}
