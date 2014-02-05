package org.wso2.carbon.event.formatter.core.internal.config;

import org.wso2.carbon.databridge.commons.AttributeType;

/**
 * This class contains properties of inputs and outputs
 */
public class EventOutputProperty {

    /**
     * Name of the property
     */
    private String name;

    /**
     * Value of the property
     */
    private String valueOf;

    /**
     * Type of the property
     */
    private AttributeType type;

    public EventOutputProperty(String name, String valueOf) {
        this.name = name;
        this.valueOf = valueOf;
    }

    public EventOutputProperty(String name, String valueOf, AttributeType type) {
        this.name = name;
        this.valueOf = valueOf;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValueOf() {
        return valueOf;
    }

    public void setValueOf(String valueOf) {
        this.valueOf = valueOf;
    }

    public AttributeType getType() {
        return type;
    }

    public void setType(AttributeType type) {
        this.type = type;
    }
}
