package org.wso2.carbon.event.execution.manager.admin.dto.configuration;

public class AttributeMappingDTO {
    private String fromAttribute;
    private String toAttribute;
    private String attributeType;

    public String getFromAttribute() {
        return fromAttribute;
    }

    public void setFromAttribute(String fromAttribute) {
        this.fromAttribute = fromAttribute;
    }

    public String getToAttribute() {
        return toAttribute;
    }

    public void setToAttribute(String toAttribute) {
        this.toAttribute = toAttribute;
    }

    public String getAttributeType() {
        return attributeType;
    }

    public void setAttributeType(String attributeType) {
        this.attributeType = attributeType;
    }
}
