package org.wso2.carbon.bam.gadgetgenwizard.internal;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="gadgetgenwizard.component" immediate="true"
 * @scr.reference name="config.context.service" interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1" policy="dynamic" bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="server.configuration" interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1" policy="dynamic" bind="setServerConfiguration" unbind="unsetServerConfiguration"
 * @scr.reference name="ndatasource.core" interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1" policy="dynamic" bind="setDataSourceService" unbind="unsetDataSourceService"

 */

public class GadgetGenWizardServiceComponent {

    private static final Log log = LogFactory.getLog(GadgetGenWizardServiceComponent.class);

    protected void activate(ComponentContext ctx) {

    }

    protected void setConfigurationContextService(ConfigurationContextService ccService) {
        GGWUtils.setConfigurationContextService(ccService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService ccService) {
        GGWUtils.setConfigurationContextService(null);
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
       GGWUtils.setDataSourceService(null);
    }

    protected void unsetDataSourceService(DataSourceService dataSourceService) {
       GGWUtils.setDataSourceService(dataSourceService);
    }

    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        GGWUtils.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        GGWUtils.setServerConfiguration(null);
    }

}
