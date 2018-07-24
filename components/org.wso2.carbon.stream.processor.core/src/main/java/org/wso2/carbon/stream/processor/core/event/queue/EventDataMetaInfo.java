package org.wso2.carbon.stream.processor.core.event.queue;

import org.wso2.siddhi.query.api.definition.Attribute;

public class EventDataMetaInfo {
    int eventSize;
    Attribute.Type attributeType;

    public EventDataMetaInfo(int eventSize, Attribute.Type attributeType) {
        this.eventSize = eventSize;
        this.attributeType = attributeType;
    }

    public int getEventSize() {
        return eventSize;
    }

    public void setEventSize(int eventSize) {
        this.eventSize = eventSize;
    }

    public Attribute.Type getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(Attribute.Type attributeType) {
        this.attributeType = attributeType;
    }
}
