package org.wso2.carbon.event.template.manager.core.structure.domain;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "domain")
public class Domain {
    private String name;
    private String description;
    private CommonArtifacts commonArtifacts;
    private Scenarios scenarios;

    @XmlAttribute
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** Getter.
     * @return the commonArtifacts
     */
    public CommonArtifacts getCommonArtifacts() {
        return commonArtifacts;
    }

    /** Setter.
     * @param commonArtifacts the commonArtifacts to set
     */
    @XmlElement
    public void setCommonArtifacts(CommonArtifacts commonArtifacts) {
        this.commonArtifacts = commonArtifacts;
    }

    public Scenarios getScenarios() {
        return scenarios;
    }

    @XmlElement
    public void setScenarios(Scenarios scenarios) {
        this.scenarios = scenarios;
    }
}
