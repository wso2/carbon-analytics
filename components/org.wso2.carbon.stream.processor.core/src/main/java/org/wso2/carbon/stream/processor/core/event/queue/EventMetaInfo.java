package org.wso2.carbon.stream.processor.core.event.queue;

import org.wso2.siddhi.query.api.definition.Attribute;

public class EventMetaInfo {
    int eventSize;
    Attribute.Type[] attributeTypeOrder;

    public EventMetaInfo(int eventSize, Attribute.Type[] attributeType){
        this.eventSize = eventSize;
        this.attributeTypeOrder = attributeType;
    }
    public int getEventSize() {
        return eventSize;
    }

    public void setEventSize(int eventSize) {
        this.eventSize = eventSize;
    }

    public Attribute.Type[] getAttributeTypeOrder() {
        return attributeTypeOrder;
    }

    public void setAttributeTypeOrder(Attribute.Type[] attributeTypeOrder) {
        this.attributeTypeOrder = attributeTypeOrder;
    }
}
