package org.wso2.carbon.analytics.dashboard.admin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dashboard.admin.DashboardAdminService;

/**
 * Declarative service for the Analytics Dashboard Admin service component
 *
 * @scr.component name="dashboard.component" immediate="true"
 */
public class DashboardAdminComponent {

    private static final Log logger = LogFactory.getLog(DashboardAdminComponent.class);

    protected void activate(ComponentContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Activating Analytics DashboardAdminComponent module.");
        }
        BundleContext bundleContext = ctx.getBundleContext();
        bundleContext.registerService(DashboardAdminService.class, new DashboardAdminService(), null);
    }


}
