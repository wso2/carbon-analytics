package org.wso2.carbon.event.execution.manager.core.structure.configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="streamMapping")
public class StreamMapping {

    @XmlAttribute(name="from")
    private String from;

    @XmlAttribute(name="to")
    private String to;

    private AttributeMappings attributeMappings;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public AttributeMappings getAttributeMappings() {
        return attributeMappings;
    }

    public void setAttributeMappings(AttributeMappings attributeMappings) {
        this.attributeMappings = attributeMappings;
    }
}
