package org.wso2.carbon.event.execution.manager.core.structure.domain;


import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "templateDomain")
public class TemplateDomain {
    @XmlAttribute
    private String name;
    private String description;
    private CommonArtifacts commonArtifacts;
    private TemplateConfigs templateConfigs;

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

    public TemplateConfigs getTemplateConfigs() {
        return templateConfigs;
    }

    @XmlElement
    public void setTemplateConfigs(TemplateConfigs templateConfigs) {
        this.templateConfigs = templateConfigs;
    }
}
