package org.wso2.carbon.analytics.dashboard.admin.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dashboard.admin.DashboardAdminService;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;

/**
 * Declarative service for the Analytics Dashboard Admin service component
 *
 * @scr.component name="dashboard.component" immediate="true"
 * @scr.reference name="analytics.component" interface="org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
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

    protected void setAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.setAnalyticsDataService(secureAnalyticsDataService);
    }

    protected void unsetAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        ServiceHolder.setAnalyticsDataService(null);
    }
}
