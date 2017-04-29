package org.wso2.carbon.event.simulator.core.internal.resourceManager;

import java.util.List;

/**
 * ResourceManager interface defines methods used by all resource manager
 */
public interface ResourceManager {

    void add(String resourceName, String simulationName);

    boolean contains(String resourceName);

    List<String> get(String resourceName);

    void remove(String resourceName);

}
