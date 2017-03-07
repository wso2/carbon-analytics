/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public static final int ENTITY_EXPANSION_LIMIT = 0;
}
