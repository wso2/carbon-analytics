package org.wso2.carbon.gadget.template.deployer.internal;

import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;

public class GadgetTemplateDeployerException extends TemplateDeploymentException {

    public GadgetTemplateDeployerException(String message) {
        super(message);
    }

    public GadgetTemplateDeployerException(String message, Exception cause) {
        super(message, cause);
    }
}
