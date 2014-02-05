package org.wso2.carbon.event.stream.manager.admin.internal;

/**
 * This class contains properties of inputs and outputs
 */
public class EventStreamAttributeDto {

    /**
     * Name of the attribute
     */
    private String attributeName;

    /**
     * Type of the attribute
     */
    private String attributeType;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }
}
