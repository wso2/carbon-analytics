package util;

import org.wso2.siddhi.core.event.Event;

public class EventData {
    private String executionPlanName;
    private String streamName;
    private Event event;

    public EventData(String executionPlanName, String streamName, Event event) {
        this.executionPlanName = executionPlanName;
        this.streamName = streamName;
        this.event = event;
    }

    public String getExecutionPlanName() {
        return executionPlanName;
    }

    public void setExecutionPlanName(String executionPlanName) {
        this.executionPlanName = executionPlanName;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

}
