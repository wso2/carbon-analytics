package org.wso2.carbon.dashboard.template.deployer.internal;

import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;

public class DashboardTemplateDeployerException extends TemplateDeploymentException {

    public DashboardTemplateDeployerException(String message) {
        super(message);
    }

    public DashboardTemplateDeployerException(String message, Exception cause) {
        super(message, cause);
    }
}
