package org.wso2.carbon.event.simulator.core.service.bean;

import org.wso2.carbon.event.simulator.core.service.EventSimulator;

/**
 * ActiveSimulatorData holds details about the active simulator
 */
public class ActiveSimulatorData {
    private EventSimulator eventSimulator;
    private String simulationConfig;

    public ActiveSimulatorData(EventSimulator eventSimulator, String simulationConfig) {
        this.eventSimulator = eventSimulator;
        this.simulationConfig = simulationConfig;
    }

    public EventSimulator getEventSimulator() {
        return eventSimulator;
    }

    public void setEventSimulator(EventSimulator eventSimulator) {
        this.eventSimulator = eventSimulator;
    }

    public String getSimulationConfig() {
        return simulationConfig;
    }

    public void setSimulationConfig(String simulationConfig) {
        this.simulationConfig = simulationConfig;
    }
}
