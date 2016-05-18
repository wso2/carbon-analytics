/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.execution.manager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.ExecutionManagerService;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerConstants;
import org.wso2.carbon.event.execution.manager.core.internal.util.ExecutionManagerHelper;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.StreamMapping;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.TemplateConfiguration;
import org.wso2.carbon.event.execution.manager.core.structure.domain.ExecutionManagerTemplate;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class consist of the implementations of interface ExecutionManagerService
 */
public class CarbonExecutionManagerService implements ExecutionManagerService {
    private static final Log log = LogFactory.getLog(CarbonExecutionManagerService.class);

    private Map<String, ExecutionManagerTemplate> domains;

    public CarbonExecutionManagerService() throws ExecutionManagerException {

        domains = new HashMap<>();

        domains = ExecutionManagerHelper.loadDomains();
    }


    @Override
    public List<String> saveConfiguration(TemplateConfiguration configuration)
            throws ExecutionManagerException {
        ExecutionManagerTemplate executionManagerTemplate = domains.get(configuration.getDomain());
        ExecutionManagerHelper.deployArtifacts(configuration, executionManagerTemplate);

        ExecutionManagerHelper.saveToRegistry(configuration);//todo: TBD on whether saving conditionally, based on getStreamIDsInMappings output.

        //If StreamMappings element is present in the ExecutionManagerTemplate, then need to return those Stream IDs,
        //so the caller (the UI) can prompt the user to map these streams to his own streams.
        return ExecutionManagerHelper.getStreamIDsInMappings(configuration, getDomain(configuration.getDomain()));
    }


    @Override
    public void saveConfigurationWithStreamMapping(StreamMapping streamMapping
            , String templateConfigName, String templateConfigFrom)
            throws ExecutionManagerException {
        try {
            //deploy execution plan
            String executionPlan = ExecutionManagerHelper.generateExecutionPlan(streamMapping, templateConfigName, templateConfigFrom);
            DeployableTemplate deployableTemplate = new DeployableTemplate();
            deployableTemplate.setArtifact(executionPlan);

            TemplateDeployer deployer = ExecutionManagerValueHolder.getTemplateDeployers().get("realtime"); //todo: check name;
            deployer.deployArtifact(deployableTemplate);


            //save to registry
            TemplateConfiguration templateConfiguration = ExecutionManagerHelper.getConfigurationFromRegistry(templateConfigName, templateConfigFrom);
        } catch (TemplateDeploymentException e) {
            throw new ExecutionManagerException("Failed to deploy execution plan, hence event flow will " +
                    "not be complete for Template Configuration: " + templateConfigName + " in domain: " + templateConfigFrom, e);
        }
    }



    @Override
    public Collection<ExecutionManagerTemplate> getAllDomains() {
        return domains.values();
    }

    @Override
    public Collection<TemplateConfiguration> getConfigurations(String domainName) {
        Collection<TemplateConfiguration> templateConfigurations = new ArrayList<TemplateConfiguration>();

        String domainFilePath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                + "/" + domainName;
        try {
            Registry registry = ExecutionManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            if (registry.resourceExists(domainFilePath)) {
                Resource resource = registry.get(domainFilePath);
                //All the resources of collection will be loaded
                if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                    loadConfigurations(((org.wso2.carbon.registry.core.Collection) resource).getChildren(),
                            templateConfigurations);
                }
            }
        } catch (RegistryException e) {
            log.error("Registry exception occurred when accessing files at "
                    + ExecutionManagerConstants.TEMPLATE_CONFIG_PATH, e);
        }
        return templateConfigurations;

    }

    /**
     * Load all the configurations of given list of file paths
     *
     * @param filePaths              where configuration files are located
     * @param templateConfigurations TemplateConfiguration collection which needs to be loaded
     */
    private void loadConfigurations(String[] filePaths, Collection<TemplateConfiguration> templateConfigurations) {
        for (String filePath : filePaths) {
            templateConfigurations.add(ExecutionManagerHelper.getConfiguration(filePath));
        }
    }

    @Override
    public ExecutionManagerTemplate getDomain(String domainName) {
        return domains.get(domainName);
    }

    @Override
    public TemplateConfiguration getConfiguration(String domainName, String configName) {
        return ExecutionManagerHelper.getConfiguration(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                + "/" + domainName
                + "/" + configName
                + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
    }

    @Override
    public void deleteConfiguration(String domainName, String configName) throws ExecutionManagerException {
        /*
            First try to delete from registry if any exception occur, it will be logged.
            Then try to un deploy execution plan and log errors occur.
            So even one operation failed other operation will be executed
         */
        TemplateConfiguration templateConfig = null;
        try {
            // need to distinguish the type to delegate to the pluggable deployer.
            templateConfig = ExecutionManagerHelper.getConfiguration(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                    + RegistryConstants.PATH_SEPARATOR + domainName
                    + RegistryConstants.PATH_SEPARATOR + configName
                    + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);

            Registry registry = ExecutionManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            registry.delete(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR
                    + domainName + RegistryConstants.PATH_SEPARATOR + configName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
        } catch (RegistryException e) {
            log.error("Configuration exception when deleting registry configuration file "
                    + configName + " of Domain " + domainName, e);
        }

        try {

            ExecutionManagerTemplate executionManagerTemplate = getDomain(templateConfig.getDomain());
            for (Scenario scenario : executionManagerTemplate.getScenarios().getScenario()) {
                if (templateConfig.getScenario().equals(scenario.getName())) {
                    for (Template template : scenario.getTemplates().getTemplate()) {
                        ExecutionManagerHelper.unDeployExistingArtifact(domainName
                                                                        + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + configName, template.getType());
                        //todo: scriptName is yet to be decided
                    }
                    break;
                }
            }
        } catch (TemplateDeploymentException e) {
            log.error("Configuration exception when un deploying script "
                    + configName + " of Domain " + domainName, e);

        }
    }

}