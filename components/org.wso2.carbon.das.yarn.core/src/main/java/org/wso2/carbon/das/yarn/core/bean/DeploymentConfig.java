package org.wso2.carbon.das.yarn.core.bean;

import org.wso2.carbon.config.annotation.Configuration;

@Configuration(namespace = "deployment.config",description = "")
public class DeploymentConfig {
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
