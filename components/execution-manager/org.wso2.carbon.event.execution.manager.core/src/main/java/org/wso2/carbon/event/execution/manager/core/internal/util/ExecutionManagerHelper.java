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
package org.wso2.carbon.event.execution.manager.core.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.TemplateConfiguration;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Artifact;
import org.wso2.carbon.event.execution.manager.core.structure.domain.StreamMapping;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateConfig;
import org.wso2.carbon.event.execution.manager.core.structure.domain.TemplateDomain;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class consist of the helper methods which are required to deal with domain templates stored in the file directory,
 * configurations stored as resources in the registry, deploy execution plans and deploy streams
 */
public class ExecutionManagerHelper {

    private static final Log log = LogFactory.getLog(ExecutionManagerHelper.class);

    /**
     * To avoid instantiating
     */
    private ExecutionManagerHelper() {
    }

    /**
     * Load All domains templates available in the file directory
     */
    public static Map<String, TemplateDomain> loadDomains() {
        //Get domain template folder and load all the domain template files
        File folder = new File(ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH);
        Map<String, TemplateDomain> domains = new HashMap<>();

        File[] files = folder.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isFile() && fileEntry.getName().endsWith("xml")) {
                    TemplateDomain templateDomain = unmarshalDomain(fileEntry);
                    domains.put(templateDomain.getName(), templateDomain);
                }
            }
        }

        return domains;
    }

    /**
     * Unmarshalling TemplateDomain object by given file
     *
     * @param fileEntry file for unmarshalling
     * @return templateDomain object
     */
    private static TemplateDomain unmarshalDomain(File fileEntry) {
        TemplateDomain templateDomain = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(TemplateDomain.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            templateDomain = (TemplateDomain) jaxbUnmarshaller.unmarshal(fileEntry);

        } catch (JAXBException e) {
            log.error("JAXB Exception when unmarshalling domain template file at "
                    + fileEntry.getPath(), e);
        }

        return templateDomain;

    }

    /**
     * Provide template configurations available in the given registry and given path
     *
     * @param path where configurations are stored
     * @return available configurations
     */
    public static TemplateConfiguration getConfiguration(String path) {


        TemplateConfiguration templateConfiguration = null;
        try {
            Registry registry = ExecutionManagerValueHolder.getRegistryService().getConfigSystemRegistry(PrivilegedCarbonContext
                                                                                                                 .getThreadLocalCarbonContext().getTenantId());

            if (registry.resourceExists(path)) {
                Resource configFile = registry.get(path);
                if (configFile != null) {
                    templateConfiguration = unmarshalConfiguration(configFile.getContent());
                }
            }
        } catch (RegistryException e) {
            log.error("Registry exception occurred when accessing files at "
                    + ExecutionManagerConstants.TEMPLATE_CONFIG_PATH, e);
        }

        return templateConfiguration;
    }

    /**
     * Unmarshalling TemplateDomain object by given file content object
     *
     * @param configFileContent file for unmarshalling
     * @return templateConfiguration object
     */
    private static TemplateConfiguration unmarshalConfiguration(Object configFileContent) {
        TemplateConfiguration templateConfiguration = null;
        try {

            StringReader reader = new StringReader(new String((byte[]) configFileContent));
            JAXBContext jaxbContext = JAXBContext.newInstance(TemplateConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            templateConfiguration = (TemplateConfiguration) jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            log.error("JAXB Exception occurred when unmarshalling configuration ", e);
        }

        return templateConfiguration;
    }


    /**
     * Checks whether the present Template Domain configuration has valid content.
     * For example, it should have at least one Template configuration.
     *
     * @param configuration Object containing configuration parameters. Required for logging purpose.
     * @param templateDomain TemplateDomain object which needs to be validated.
     * @throws ExecutionManagerException
     */
    public static void validateTemplateDomainConfig(TemplateConfiguration configuration,
                                                    TemplateDomain templateDomain)
            throws ExecutionManagerException {
        if (templateDomain == null) {
            throw new ExecutionManagerException("The " + configuration.getFrom() + " domain of"
                                                + configuration.getName() + " configuration" + " is not available in the domain list.");
        }
        if (templateDomain.getTemplateConfigs() == null ||
                   templateDomain.getTemplateConfigs().getTemplateConfig() == null ||
                   templateDomain.getTemplateConfigs().getTemplateConfig().isEmpty()) {
            //It is required to have at least one TemplateConfiguration.
            //Having only a set of common artifacts is not a valid use case.
            throw new ExecutionManagerException("There are no templates in the domain " + configuration.getFrom()
                                                + " of " + configuration.getName() + " configuration");
        }
    }

    /**
     * Deploy given configurations template Execution Plans
     *
     * @param configuration configuration object
     */
    public static void deployArtifacts(TemplateConfiguration configuration,
                                       TemplateDomain templateDomain)
            throws ExecutionManagerException {
        //make sure common artifacts are deployed
        if (templateDomain.getCommonArtifacts() != null) {
            for (Artifact artifact : templateDomain.getCommonArtifacts().getArtifact()) {
                try {
                    DeployableTemplate deployableTemplate = new DeployableTemplate();
                    deployableTemplate.setArtifact(artifact.getValue());
                    deployableTemplate.setConfiguration(configuration);
                    TemplateDeployer deployer = ExecutionManagerValueHolder.getTemplateDeployers().get(artifact.getType());
                    deployer.deployArtifact(deployableTemplate);
                } catch (TemplateDeploymentException e) {
                    log.error("Error when trying to deploy the artifact " + configuration.getName(), e);
                    throw new ExecutionManagerException(e);
                }

            }
        }

        //now, deploy templated artifacts
        for (TemplateConfig templateConfig : templateDomain.getTemplateConfigs().getTemplateConfig()) {
            for (Template template : templateConfig.getTemplates().getTemplate()) {
                if (templateConfig.getName().equals(configuration.getType())) {
                    TemplateDeployer deployer = ExecutionManagerValueHolder.getTemplateDeployers().get(template.getType());
                    if (deployer != null) {
                        try {
                            DeployableTemplate deployableTemplate = new DeployableTemplate();
                            String updatedScript = updateArtifactParameters(configuration, template.getValue());
                            deployableTemplate.setArtifact(updatedScript);
                            deployableTemplate.setConfiguration(configuration);
                            deployer.deployArtifact(deployableTemplate);
                        } catch (TemplateDeploymentException e) {
                            log.error("Error when trying to deploy the artifact " + configuration.getName(), e);
                            throw new ExecutionManagerException(e);
                        }
                        break;
                    } else {
                        throw new ExecutionManagerException("A deployer doesn't exist for template type " + template.getType());
                    }
                }
            }
        }
    }


    /**
     * Update given script by replacing undefined parameter values with configured parameter values
     *
     * @param config configurations which consists of parameters which will replace
     * @param script script which needs to be updated
     * @return updated execution plan
     */
    private static String updateArtifactParameters(TemplateConfiguration config, String script) {
        String updatedScript = script;
        //Script parameters will be replaced with given configuration parameters
        if (config.getParameterMap() != null && script != null) {
            for (Map.Entry parameterMapEntry : config.getParameterMap().entrySet()) {
                updatedScript = updatedScript.replaceAll(ExecutionManagerConstants.REGEX_NAME_VALUE
                                                         + parameterMapEntry.getKey().toString(), parameterMapEntry.getValue().toString());
            }
        }
        return updatedScript;
    }

    /**
     * Check weather given execution plan is already exists and un deploy it
     *
     * @param scriptName name of the execution script
     * @param type       type of the execution script
     * @throws TemplateDeploymentException
     */
    public static void unDeployExistingArtifact(String scriptName, String type)    //todo: script name is yet to be decided.
            throws TemplateDeploymentException {
        TemplateDeployer deployer = ExecutionManagerValueHolder.getTemplateDeployers().get(type);
        deployer.undeployArtifact(scriptName);
    }


    /**
     * Returns the list of Stream IDs(with their template symbols replaced by user-parameters) given in StreamMappings element.
     *
     * @param configuration Template configuration, specified by the user, containing parameter values.
     * @param templateDomain TemplateDomain object, containing the StreamMappings element
     * @return List of Stream IDs
     */
    public static List<String> getStreamIDsInMappings(TemplateConfiguration configuration,
                                                      TemplateDomain templateDomain) {
        List<String> streamIdList = new ArrayList<>();
        for (TemplateConfig templateConfig: templateDomain.getTemplateConfigs().getTemplateConfig()){
            if (configuration.getName().equals(templateConfig.getName())) {
                if(templateConfig.getStreamMappings() != null && templateConfig.getStreamMappings().getStreamMapping() != null
                        && !templateConfig.getStreamMappings().getStreamMapping().isEmpty()) {    //if no stream mappings present, should return null
                    for (StreamMapping streamMapping: templateConfig.getStreamMappings().getStreamMapping()) {
                        String toStream = streamMapping.getTo();
                        for (Map.Entry entry: configuration.getParameterMap().entrySet()) {
                            toStream = toStream.replaceAll(ExecutionManagerConstants.REGEX_NAME_VALUE
                                                           + entry.getKey().toString(), entry.getValue().toString());
                        }
                        streamIdList.add(toStream);
                    }
                    return streamIdList;
                }
            }
        }
        return null;
    }
}
