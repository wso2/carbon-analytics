package org.wso2.carbon.analytics.spark.core.util;

public class PublishingPayloadEvent {
    private int eventIndex;
    private String attribute;
    
    public PublishingPayloadEvent() {
    }

    public PublishingPayloadEvent(int eventIndex, String attribute) {
        this.eventIndex = eventIndex;
        this.attribute = attribute;
    }

    public int getEventIndex() {
        return eventIndex;
    }

    public void setEventIndex(int eventIndex) {
        this.eventIndex = eventIndex;
    }

    public String getAttribute() {
        return attribute;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    @Override
    public String toString() {
        return "event- " + eventIndex + " , attribute- " + attribute;
    }
}
