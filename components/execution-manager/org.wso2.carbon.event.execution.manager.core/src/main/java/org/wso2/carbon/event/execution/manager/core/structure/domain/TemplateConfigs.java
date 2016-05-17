package org.wso2.carbon.event.execution.manager.core.structure.domain;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class TemplateConfigs {
    private List<TemplateConfig> templateConfig;

    public List<TemplateConfig> getTemplateConfig() {
        return templateConfig;
    }

    public void setTemplateConfig(List<TemplateConfig> templateConfig) {
        this.templateConfig = templateConfig;
    }
}
