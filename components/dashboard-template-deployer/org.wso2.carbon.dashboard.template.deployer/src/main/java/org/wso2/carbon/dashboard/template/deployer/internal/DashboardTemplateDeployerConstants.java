package org.wso2.carbon.dashboard.template.deployer.internal;

import org.wso2.carbon.registry.core.RegistryConstants;

public class DashboardTemplateDeployerConstants {

    private DashboardTemplateDeployerConstants() {
    }

    public static final String ARTIFACT_TYPE = "dashboard";

    public static final String CONFIG_TAG = "config";

    public static final String PROPERTIES_TAG = "properties";

    public static final String PROPERTY_TAG = "property";

    public static final String CONTENT_TAG = "content";

    public static final String NAME_ATTRIBUTE = "name";

    public static final String DASHBOARD_ID = "dashboardId";

    public static final String DASHBOARDS_RESOURCE_PATH = RegistryConstants.PATH_SEPARATOR +
            "ues" + RegistryConstants.PATH_SEPARATOR + "dashboards" + RegistryConstants.PATH_SEPARATOR;


    public static final String ARTIFACT_DASHBOARD_ID_MAPPING_PATH = "repository" + RegistryConstants.PATH_SEPARATOR + "components" + RegistryConstants.PATH_SEPARATOR +
            "org.wso2.carbon.dashboard.template.deployer" + RegistryConstants.PATH_SEPARATOR + "artifact.dashboard.id.mapping";
}
