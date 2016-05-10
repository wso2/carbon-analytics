package org.wso2.carbon.analytics.spark.core.util;

import java.util.ArrayList;

public class PublishingPayload {
    private String payload;

    private ArrayList<PublishingPayloadEvent> events = new ArrayList<>();

    public PublishingPayloadEvent getEvent(int index) {
        return events.get(index);
    }

    public boolean addEvent(PublishingPayloadEvent publishingPayloadEvent) {
        return events.add(publishingPayloadEvent);
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public ArrayList<PublishingPayloadEvent> getEvents() {
        return events;
    }

    public void setEvents(ArrayList<PublishingPayloadEvent> events) {
        this.events = events;
    }

}
