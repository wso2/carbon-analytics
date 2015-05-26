/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.analytics.dashboard;

import org.wso2.carbon.registry.core.RegistryConstants;

/**
 * Constants related to dashboards
 */
public class DashboardConstants {

    public static final String APP_NAME = "portal";

    public static final String DASHBOARD_ARTIFACT_TYPE = "dashboards/dashboard";

    public static final String DASHBOARD_EXTENSION = ".json";

    public static  final String DASHBOARDS_RESOURCE_PATH = RegistryConstants.PATH_SEPARATOR +
            "ues" + RegistryConstants.PATH_SEPARATOR +  "dashboards" + RegistryConstants.PATH_SEPARATOR;
}
