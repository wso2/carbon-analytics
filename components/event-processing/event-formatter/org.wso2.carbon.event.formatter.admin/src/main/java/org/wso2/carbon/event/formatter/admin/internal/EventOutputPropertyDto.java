package org.wso2.carbon.event.formatter.admin.internal;

/**
 * This class contains properties of inputs and outputs
 */
public class EventOutputPropertyDto {

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
    private String type;

    public EventOutputPropertyDto() {

    }

    public EventOutputPropertyDto(String name, String valueOf) {
        this.name = name;
        this.valueOf = valueOf;
    }

    public EventOutputPropertyDto(String name, String valueOf, String type) {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
