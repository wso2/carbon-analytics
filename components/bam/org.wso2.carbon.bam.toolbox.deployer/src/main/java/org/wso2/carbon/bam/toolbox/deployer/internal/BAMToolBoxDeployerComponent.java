/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.carbon.bam.toolbox.deployer.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.toolbox.deployer.BAMToolBoxDeployerConstants;
import org.wso2.carbon.bam.toolbox.deployer.BasicToolBox;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.core.ServerStartupHandler;
import org.wso2.carbon.ndatasource.core.DataSourceService;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @scr.component name="org.wso2.carbon.bam.toolbox.deployer" immediate="true"
 * @scr.reference name="server.configuration"
 * interface="org.wso2.carbon.base.api.ServerConfigurationService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setServerConfiguration"
 * unbind="unsetServerConfiguration"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setConfigurationContextService"
 * unbind="unsetConfigurationContextService"
 * @scr.reference name="user.realm.delegating"
 * interface="org.wso2.carbon.user.core.UserRealm"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setUserRealm"
 * unbind="unsetUserRealm"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setRealmService"
 * unbind="unsetRealmService"
 * @scr.reference name="datasources.service"
 * interface="org.wso2.carbon.ndatasource.core.DataSourceService"
 * cardinality="1..1"
 * policy="dynamic"
 * bind="setDataSourceService"
 * unbind="unsetDataSourceService"
 */

public class BAMToolBoxDeployerComponent {
    private static final Log log = LogFactory.getLog(BAMToolBoxDeployerComponent.class);

    protected void activate(ComponentContext context) {
        loadAvailableToolBoxes();
        BundleContext bundleContext = context.getBundleContext();
        bundleContext.registerService(ServerStartupHandler.class.getName(),
                new ServerStartUpInspector(), null);
        log.info("Successfully Started BAM Toolbox Deployer");
    }


    private void loadAvailableToolBoxes() {
        String toolboxPropLocation = CarbonUtils.getCarbonHome() + File.separator +
                BAMToolBoxDeployerConstants.BAM_TOOLBOX_HOME + File.separator + BAMToolBoxDeployerConstants.BAM_DEFAULT_TOOLBOX_PROP_FILE;
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(toolboxPropLocation));
            String varNames = props.getProperty(BAMToolBoxDeployerConstants.TOOLBOXES_VAR_NAME);
            if (null != varNames && !varNames.trim().equals("")) {
                int toolBoxId = 1;
                String[] varNameList = varNames.trim().split(",");
                for (String aToolVar : varNameList) {
                    aToolVar = aToolVar.trim();
                    String defaultProp = props.getProperty(BAMToolBoxDeployerConstants.TOOLBOXES_PREFIX +
                            "." + aToolVar + "." + BAMToolBoxDeployerConstants.TOOLBOXES_DEFAULT_SUFFIX);
                    if (null != defaultProp && defaultProp.equalsIgnoreCase("true")) {
                        String displayName = props.getProperty(BAMToolBoxDeployerConstants.TOOLBOXES_PREFIX +
                                "." + aToolVar + "." + BAMToolBoxDeployerConstants.TOOLBOXES_NAME_SUFFIX);
                        String location = props.getProperty(BAMToolBoxDeployerConstants.TOOLBOXES_PREFIX + "." +
                                aToolVar + "." + BAMToolBoxDeployerConstants.TOOLBOXES_LOCATION_SUFFIX);
                        String desc = props.getProperty(BAMToolBoxDeployerConstants.TOOLBOXES_PREFIX + "." +
                                aToolVar + "." + BAMToolBoxDeployerConstants.TOOLBOXES_DESC_SUFFIX);
                        if (((null != location && !location.equals("")) && (null != displayName && !displayName.equals("")))) {
                            if (location.indexOf(BAMToolBoxDeployerConstants.CARBON_HOME) == 0) {
                                location = location.substring(BAMToolBoxDeployerConstants.CARBON_HOME.length());
                                location = CarbonUtils.getCarbonHome() + location;
                                location = location.replace("/", File.separator);
                            }
                            BasicToolBox toolBox = new BasicToolBox(toolBoxId, location, displayName, desc);
                            BasicToolBox.addToAvailableToolBox(toolBox);
                            toolBoxId++;
                        } else {
                            log.warn("Location | Name of toolbox is not specified for " + aToolVar);
                        }
                    } else {
                        log.warn("Toolbox for reference " + aToolVar + " is not marked as default");
                    }
                }
            } else {
                log.warn("No references found for property " + BAMToolBoxDeployerConstants.TOOLBOXES_VAR_NAME + ".\n " +
                        "No default toolboxes will be populated");
            }
        } catch (IOException e) {
            log.warn("No " + BAMToolBoxDeployerConstants.BAM_DEFAULT_TOOLBOX_PROP_FILE
                    + " file found! There won't be default toolboxes..");
        }

    }

    protected void setConfigurationContextService(ConfigurationContextService contextService) {
        ServiceHolder.setConfigurationContextService(contextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService contextService) {
        ServiceHolder.setConfigurationContextService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceHolder.setRegistryService(null);
    }

    protected void setUserRealm(UserRealm userRealm) {
        ServiceHolder.setUserRealm(userRealm);
    }

    protected void unsetUserRealm(UserRealm userRealm) {
        ServiceHolder.setUserRealm(userRealm);

    }

    protected void setRealmService(RealmService realmService) {
        ServiceHolder.setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceHolder.setRealmService(null);
    }


    protected void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        ServiceHolder.setServerConfiguration(serverConfiguration);
    }

    protected void unsetServerConfiguration(ServerConfigurationService serverConfiguration) {
        ServiceHolder.setConfigurationContextService(null);
    }

    protected void setDataSourceService(DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Carbon Data Sources Service");
        }
        ServiceHolder.setDataSourceService(dataSourceService);
    }

    protected void unsetDataSourceService(
            DataSourceService dataSourceService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Carbon Data Sources Service");
        }
        ServiceHolder.setDataSourceService(null);
    }
}