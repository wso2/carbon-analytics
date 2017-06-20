package util;

import org.wso2.siddhi.core.event.Event;

public class EventData {
    private String siddhiAppName;
    private String streamName;
    private Event event;

    public EventData(String siddhiAppName, String streamName, Event event) {
        this.siddhiAppName = siddhiAppName;
        this.streamName = streamName;
        this.event = event;
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public void setSiddhiAppName(String siddhiAppName) {
        this.siddhiAppName = siddhiAppName;
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
