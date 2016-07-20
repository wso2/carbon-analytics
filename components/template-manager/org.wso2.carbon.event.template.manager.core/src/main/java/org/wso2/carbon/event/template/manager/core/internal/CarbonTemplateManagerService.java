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
package org.wso2.carbon.event.template.manager.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.template.manager.core.TemplateManagerService;
import org.wso2.carbon.event.template.manager.core.exception.TemplateManagerException;
import org.wso2.carbon.event.template.manager.core.internal.ds.TemplateManagerValueHolder;
import org.wso2.carbon.event.template.manager.core.internal.util.TemplateManagerConstants;
import org.wso2.carbon.event.template.manager.core.internal.util.TemplateManagerHelper;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping;
import org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMappings;
import org.wso2.carbon.event.template.manager.core.structure.domain.Artifact;
import org.wso2.carbon.event.template.manager.core.structure.domain.Domain;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.template.manager.core.structure.domain.Template;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;

import javax.script.ScriptEngine;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Class consist of the implementations of interface TemplateManagerService
 */
public class CarbonTemplateManagerService implements TemplateManagerService {
    private static final Log log = LogFactory.getLog(CarbonTemplateManagerService.class);

    private Map<String, Domain> domains;

    public CarbonTemplateManagerService() throws TemplateManagerException {
        domains = new HashMap<>();
        domains = TemplateManagerHelper.loadDomains();
    }


    @Override
    public List<String> saveConfiguration(ScenarioConfiguration configuration)
            throws TemplateManagerException {
        try {
            Registry registry = TemplateManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            String resourceCollectionPath = TemplateManagerConstants.TEMPLATE_CONFIG_PATH
                                            + "/" + configuration.getDomain();
            String resourcePath = resourceCollectionPath + "/"
                                  + configuration.getName() + TemplateManagerConstants.CONFIG_FILE_EXTENSION;
            if (registry.resourceExists(resourcePath)) {
                throw new TemplateManagerException("Could not edit the Scenario because another scenario with same name '"
                                                   + configuration.getName() + "' already exists.");
            }
            return processSaveConfiguration(configuration);
        }  catch (RegistryException e) {
            throw new TemplateManagerException("Could not load the registry for Tenant: " +
                                               PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true), e);
        }
    }

    @Override
    public List<String> editConfiguration(ScenarioConfiguration configuration)
            throws TemplateManagerException {
        return processSaveConfiguration(configuration);
    }


    @Override
    public void saveStreamMapping(List<StreamMapping> streamMappingList,
                                  String scenarioConfigName, String domainName)
            throws TemplateManagerException {
        try {
            //save to registry
            String resourceCollectionPath = TemplateManagerConstants.TEMPLATE_CONFIG_PATH
                                            + "/" + domainName;

            String resourcePath = resourceCollectionPath + "/"
                                  + scenarioConfigName + TemplateManagerConstants.CONFIG_FILE_EXTENSION;

            ScenarioConfiguration scenarioConfiguration = TemplateManagerHelper.getConfiguration(resourcePath);

            StreamMappings streamMappings = new StreamMappings();
            streamMappings.setStreamMapping(streamMappingList);

            scenarioConfiguration.setStreamMappings(streamMappings);
            TemplateManagerHelper.saveToRegistry(scenarioConfiguration);

            //deploy execution plan
            String planName = TemplateManagerHelper.getStreamMappingPlanId(domainName, scenarioConfigName);
            String executionPlan = TemplateManagerHelper.generateExecutionPlan(streamMappingList, planName);

            DeployableTemplate deployableTemplate = new DeployableTemplate();
            deployableTemplate.setArtifact(executionPlan);
            deployableTemplate.setConfiguration(scenarioConfiguration);
            deployableTemplate.setArtifactId(planName);

            TemplateDeployer deployer = TemplateManagerValueHolder.getTemplateDeployers().get(TemplateManagerConstants.DEPLOYER_TYPE_REALTIME);
            if (deployer != null) {
                deployer.deployArtifact(deployableTemplate);
            } else {
                throw new TemplateManagerException("A deployer doesn't exist for template type " + TemplateManagerConstants.DEPLOYER_TYPE_REALTIME);
            }

        } catch (TemplateDeploymentException e) {
            throw new TemplateManagerException("Failed to deploy stream-mapping-execution plan, hence event flow will " +
                    "not be complete for Template Configuration: " + scenarioConfigName + " in domain: " + domainName, e);
        }
    }



    @Override
    public Collection<Domain> getAllDomains() {
        return domains.values();
    }

    @Override
    public Collection<ScenarioConfiguration> getConfigurations(String domainName)
            throws TemplateManagerException {
        Collection<ScenarioConfiguration> scenarioConfigurations = new ArrayList<ScenarioConfiguration>();

        String domainFilePath = TemplateManagerConstants.TEMPLATE_CONFIG_PATH
                + "/" + domainName;
        try {
            Registry registry = TemplateManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            if (registry.resourceExists(domainFilePath)) {
                Resource resource = registry.get(domainFilePath);
                //All the resources of collection will be loaded
                if (resource instanceof org.wso2.carbon.registry.core.Collection) {
                    loadConfigurations(((org.wso2.carbon.registry.core.Collection) resource).getChildren(),
                                       scenarioConfigurations);
                }
            }
        } catch (RegistryException e) {
            throw new TemplateManagerException("Registry exception occurred when accessing files at "
                    + TemplateManagerConstants.TEMPLATE_CONFIG_PATH, e);
        }
        return scenarioConfigurations;

    }

    /**
     * Load all the configurations of given list of file paths
     *
     * @param filePaths              where configuration files are located
     * @param scenarioConfigurations ScenarioConfiguration collection which needs to be loaded
     */
    private void loadConfigurations(String[] filePaths, Collection<ScenarioConfiguration> scenarioConfigurations)
            throws TemplateManagerException {
        for (String filePath : filePaths) {
            scenarioConfigurations.add(TemplateManagerHelper.getConfiguration(filePath));
        }
    }

    @Override
    public Domain getDomain(String domainName) {
        return domains.get(domainName);
    }

    @Override
    public ScenarioConfiguration getConfiguration(String domainName, String configName)
            throws TemplateManagerException {
        return TemplateManagerHelper.getConfiguration(TemplateManagerConstants.TEMPLATE_CONFIG_PATH
                + "/" + domainName
                + "/" + configName
                + TemplateManagerConstants.CONFIG_FILE_EXTENSION);
    }

    @Override
    public void deleteConfiguration(String domainName, String configName) throws TemplateManagerException {
        /*
            First try to delete from registry if any exception occur, it will be logged.
            Then try to un deploy execution plan and log errors occur.
            So even one operation failed other operation will be executed
         */
        ScenarioConfiguration scenarioConfig = null;
        try {
            // need to distinguish the type to delegate to the pluggable deployer.
            scenarioConfig = TemplateManagerHelper.getConfiguration(TemplateManagerConstants.TEMPLATE_CONFIG_PATH
                    + RegistryConstants.PATH_SEPARATOR + domainName
                    + RegistryConstants.PATH_SEPARATOR + configName
                    + TemplateManagerConstants.CONFIG_FILE_EXTENSION);

            Registry registry = TemplateManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            registry.delete(TemplateManagerConstants.TEMPLATE_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR
                    + domainName + RegistryConstants.PATH_SEPARATOR + configName + TemplateManagerConstants.CONFIG_FILE_EXTENSION);
        } catch (RegistryException e) {
            log.error("Configuration exception when deleting registry configuration file "
                    + configName + " of Domain " + domainName, e);           //todo: propagate to UI
        }

        try {
            Domain domain = getDomain(domainName);

            for (Scenario scenario : domain.getScenarios().getScenario()) {
                if (scenarioConfig.getScenario().equals(scenario.getType())) {
                    Map<String,Integer> artifactTypeCountingMap = new HashMap<>();
                    for (Template template : scenario.getTemplates().getTemplate()) {
                        String artifactType = template.getType();
                        Integer artifactCount = artifactTypeCountingMap.get(artifactType);
                        if (artifactCount == null) {
                            artifactCount = 1;  //Count starts with one, instead of zero for user-friendliness.
                        } else {
                            artifactCount++;
                        }
                        String artifactId = TemplateManagerHelper.getTemplatedArtifactId(domainName, scenario.getType(), configName, artifactType, artifactCount);
                        TemplateManagerHelper.unDeployExistingArtifact(artifactId, template.getType());
                        artifactTypeCountingMap.put(artifactType, artifactCount);
                    }

                    //undeploy stream-mapping execution plan
                    String streamMappingPlanName = TemplateManagerHelper.getStreamMappingPlanId(domainName, scenarioConfig.getName());
                    TemplateManagerHelper.unDeployExistingArtifact(streamMappingPlanName, TemplateManagerConstants.DEPLOYER_TYPE_REALTIME);
                    break;
                }
            }

            //If this was the last scenario configuration left, then delete all the common artifacts.
            if (getConfigurations(domainName).isEmpty() && domain.getCommonArtifacts() != null) {
                Map<String,Integer> artifactTypeCountingMap = new HashMap<>();
                for (Artifact artifact: domain.getCommonArtifacts().getArtifact()) {
                    String artifactType = artifact.getType();
                    Integer artifactCount = artifactTypeCountingMap.get(artifact.getType());
                    if (artifactCount == null) {
                        artifactCount = 1;  //Count starts with one, instead of zero for user-friendliness.
                    } else {
                        artifactCount++;
                    }
                    String artifactId = TemplateManagerHelper.getCommonArtifactId(domainName, artifactType, artifactCount);
                    TemplateManagerHelper.unDeployExistingArtifact(artifactId, artifactType);
                    artifactTypeCountingMap.put(artifactType, artifactCount);
                }
            }
        } catch (TemplateDeploymentException e) {
            log.error("Configuration exception when un deploying script "
                    + configName + " of Domain " + domainName, e);
        }
    }

    private List<String> processSaveConfiguration(ScenarioConfiguration configuration) throws TemplateManagerException {
        try {
            Domain domain = domains.get(configuration.getDomain());
            TemplateManagerHelper.saveToRegistry(configuration);
            // JavaScript ScriptEngine
            ScriptEngine scriptEngine = TemplateManagerHelper.createJavaScriptEngine(domain);
            TemplateManagerHelper.deployArtifacts(configuration, domain, scriptEngine);
            //If StreamMappings element is present in the Domain, then need to return those Stream IDs,
            //so the caller (the UI) can prompt the user to map these streams to his own streams.
            return TemplateManagerHelper.getStreamIDsToBeMapped(configuration, getDomain(configuration.getDomain()), scriptEngine);
        } catch (TemplateDeploymentException e) {
            TemplateManagerHelper.deleteConfigWithoutUndeploy(configuration.getDomain(), configuration.getName());
            throw new TemplateManagerException("Failed to save Scenario: " + configuration.getName() + ", for Domain: "
                                               + configuration.getDomain(), e);
        }
    }
}