package org.wso2.carbon.event.execution.manager.core.structure.domain;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="scenario")
public class Scenario {

    @XmlAttribute(name="name")
    private String name;

    private String description;

    private Templates templates;

    private StreamMappings streamMappings;

    private Parameters parameters;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Templates getTemplates() {
        return templates;
    }

    public void setTemplates(Templates templates) {
        this.templates = templates;
    }

    public StreamMappings getStreamMappings() {
        return streamMappings;
    }

    public void setStreamMappings(StreamMappings streamMappings) {
        this.streamMappings = streamMappings;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
    }
}
