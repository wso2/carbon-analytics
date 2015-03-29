package org.wso2.carbon.analytics.dashboard.admin;

import org.wso2.carbon.registry.core.RegistryConstants;

public interface DashboardConstants {

    /**
     * Relative Registry locations for dataViews and dashboards.
     */
    public static  final String DATAVIEWS_DIR = RegistryConstants.PATH_SEPARATOR +
            "repository" + RegistryConstants.PATH_SEPARATOR +  "components" + RegistryConstants.PATH_SEPARATOR +
            "org.wso2.carbon.analytics.dataviews" + RegistryConstants.PATH_SEPARATOR;

    public static  final String DASHBOARDS_DIR = RegistryConstants.PATH_SEPARATOR +
            "repository" + RegistryConstants.PATH_SEPARATOR +  "components" + RegistryConstants.PATH_SEPARATOR +
            "org.wso2.carbon.analytics.dashboards" + RegistryConstants.PATH_SEPARATOR;


}
