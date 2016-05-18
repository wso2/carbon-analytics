package org.wso2.carbon.event.execution.manager.core.structure.domain;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "executionManagerTemplate")
public class ExecutionManagerTemplate {
    private String domain;
    private String description;
    private CommonArtifacts commonArtifacts;
    private Scenarios scenarios;

    @XmlAttribute
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
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
