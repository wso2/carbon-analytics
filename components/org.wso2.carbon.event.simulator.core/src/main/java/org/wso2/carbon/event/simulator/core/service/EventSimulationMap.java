package org.wso2.carbon.event.simulator.core.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * EventSimulationMap holds the simulators available
 */
public class EventSimulationMap {
    private static final Map<String, EventSimulator> simulatorMap = new ConcurrentHashMap<>();

    public static Map<String, EventSimulator> getSimulatorMap() {
        return simulatorMap;
    }
}

