package org.wso2.carbon.event.simulator.core.internal.bean;


/**
 * SimulationPropertiesDTO class is used to create simulation configuration objects.
 */
public class SimulationPropertiesDTO {

    private String simulationName;
    private long timeInterval;
    private int noOfEventsRequired;
    private long startTimestamp;
    private long endTimestamp;

    public String getSimulationName() {
        return simulationName;
    }

    public void setSimulationName(String simulationName) {
        this.simulationName = simulationName;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void setTimeInterval(long timeInterval) {
        this.timeInterval = timeInterval;
    }

    public int getNoOfEventsRequired() {
        return noOfEventsRequired;
    }

    public void setNoOfEventsRequired(int noOfEventsRequired) {
        this.noOfEventsRequired = noOfEventsRequired;
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

}
