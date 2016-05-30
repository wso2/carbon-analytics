package org.wso2.carbon.application.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.application.deployer.GadgetTemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;

/**
 * @scr.component name="TemplateDeployer.gadget.component" immediate="true"
 */
public class GadgetTemplateDeployerDS {
    private static Log log = LogFactory.getLog(GadgetTemplateDeployerDS.class);


    protected void activate(ComponentContext context) {
        try {
            GadgetTemplateDeployer templateDeployer = new GadgetTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);

        } catch (RuntimeException e) {
            log.error("Couldn't register GadgetTemplateDeployer service", e);
        }
    }
}
