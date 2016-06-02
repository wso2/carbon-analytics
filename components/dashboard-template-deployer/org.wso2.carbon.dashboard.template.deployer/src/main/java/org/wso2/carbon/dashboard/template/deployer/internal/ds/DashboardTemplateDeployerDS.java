package org.wso2.carbon.dashboard.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.dashboard.template.deployer.DashboardTemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;

/**
 * @scr.component name="TemplateDeployer.dashboard.component" immediate="true"
 */
public class DashboardTemplateDeployerDS {
    private static final Log log = LogFactory.getLog(DashboardTemplateDeployerDS.class);

    protected void activate(ComponentContext context) {
        try {
            DashboardTemplateDeployer templateDeployer = new DashboardTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);
        } catch (RuntimeException e) {
            log.error("Couldn't register DashboardTemplateDeployer service", e);
        }
    }
}
