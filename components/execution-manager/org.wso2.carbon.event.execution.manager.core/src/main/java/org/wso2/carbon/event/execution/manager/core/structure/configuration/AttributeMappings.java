package org.wso2.carbon.event.execution.manager.core.structure.configuration;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class AttributeMappings {
    private List<AttributeMapping> attributeMapping;

    public List<AttributeMapping> getAttributeMapping() {
        return attributeMapping;
    }

    public void setAttributeMapping(List<AttributeMapping> attributeMapping) {
        this.attributeMapping = attributeMapping;
    }
}
