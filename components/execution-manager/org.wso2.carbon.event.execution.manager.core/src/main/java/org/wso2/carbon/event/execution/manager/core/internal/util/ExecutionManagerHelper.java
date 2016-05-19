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
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.execution.manager.core.exception.ExecutionManagerException;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.AttributeMapping;
import org.wso2.carbon.event.execution.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Artifact;
import org.wso2.carbon.event.execution.manager.core.structure.domain.ExecutionManagerTemplate;
import org.wso2.carbon.event.execution.manager.core.structure.domain.StreamMapping;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
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
    private static final String EXECUTION_PLAN_NAME_ANNOTATION = "@Plan:name";
    private static final String IMPORT_ANNOTATION = "@Import";
    private static final String EXPORT_ANNOTATION = "@Export";
    private static final String DEFINE_STREAM = "define stream ";
    private static final String FROM = "from ";
    private static final String SELECT = "select ";
    private static final String AS = "as ";
    private static final String INSERT_INTO = "insert into ";
    private static final String DELIMETER = "-";

    /**
     * To avoid instantiating
     */
    private ExecutionManagerHelper() {
    }

    /**
     * Load All domains templates available in the file directory
     */
    public static Map<String, ExecutionManagerTemplate> loadDomains() {
        //Get domain template folder and load all the domain template files
        File folder = new File(ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH);
        Map<String, ExecutionManagerTemplate> domains = new HashMap<>();

        File[] files = folder.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isFile() && fileEntry.getName().endsWith("xml")) {
                    ExecutionManagerTemplate executionManagerTemplate = unmarshalDomain(fileEntry);
                    if (executionManagerTemplate != null) {
                        try {
                            validateTemplateDomainConfig(executionManagerTemplate);
                        } catch (ExecutionManagerException e) {
                            //In case an invalid template configuration is found, this loader logs
                            // an error message and aborts loading that particular template domain config.
                            //However, this will load all the valid template domain configurations.
                            log.error("Invalid Template Domain configuration file found: " + fileEntry.getName(), e);
                        }
                        domains.put(executionManagerTemplate.getDomain(), executionManagerTemplate);
                    } else {
                        log.error("Invalid Template Domain configuration file found: " + fileEntry.getName());
                    }

                }
            }
        }

        return domains;
    }

    /**
     * Unmarshalling ExecutionManagerTemplate object by given file
     *
     * @param fileEntry file for unmarshalling
     * @return templateDomain object
     */
    private static ExecutionManagerTemplate unmarshalDomain(File fileEntry) {
        ExecutionManagerTemplate executionManagerTemplate = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ExecutionManagerTemplate.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            executionManagerTemplate = (ExecutionManagerTemplate) jaxbUnmarshaller.unmarshal(fileEntry);

        } catch (JAXBException e) {
            log.error("JAXB Exception when unmarshalling domain template file at "
                    + fileEntry.getPath(), e);
        }

        return executionManagerTemplate;

    }

    /**
     * Provide template configurations available in the given registry and given path
     *
     * @param path where configurations are stored
     * @return available configurations
     */
    public static ScenarioConfiguration getConfiguration(String path) {


        ScenarioConfiguration scenarioConfiguration = null;
        try {
            Registry registry = ExecutionManagerValueHolder.getRegistryService().getConfigSystemRegistry(PrivilegedCarbonContext
                                                                                                                 .getThreadLocalCarbonContext().getTenantId());

            if (registry.resourceExists(path)) {
                Resource configFile = registry.get(path);
                if (configFile != null) {
                    scenarioConfiguration = unmarshalConfiguration(configFile.getContent());
                }
            }
        } catch (RegistryException e) {
            log.error("Registry exception occurred when accessing files at "
                    + ExecutionManagerConstants.TEMPLATE_CONFIG_PATH, e);
        }

        return scenarioConfiguration;
    }

    /**
     * Unmarshalling ExecutionManagerTemplate object by given file content object
     *
     * @param configFileContent file for unmarshalling
     * @return templateConfiguration object
     */
    private static ScenarioConfiguration unmarshalConfiguration(Object configFileContent) {
        ScenarioConfiguration scenarioConfiguration = null;
        try {

            StringReader reader = new StringReader(new String((byte[]) configFileContent));
            JAXBContext jaxbContext = JAXBContext.newInstance(ScenarioConfiguration.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            scenarioConfiguration = (ScenarioConfiguration) jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            log.error("JAXB Exception occurred when unmarshalling configuration ", e);
        }

        return scenarioConfiguration;
    }


    /**
     * Checks whether the present Template Domain configuration has valid content.
     * Validity criteria - It should have at least one Template configuration.
     *
     * @param executionManagerTemplate ExecutionManagerTemplate object which needs to be validated.
     * @throws ExecutionManagerException
     */
    public static void validateTemplateDomainConfig(ExecutionManagerTemplate executionManagerTemplate)
            throws ExecutionManagerException {
        if (executionManagerTemplate.getScenarios() == null ||
            executionManagerTemplate.getScenarios().getScenario() == null ||
            executionManagerTemplate.getScenarios().getScenario().isEmpty()) {
            //It is required to have at least one ScenarioConfiguration.
            //Having only a set of common artifacts is not a valid use case.
            throw new ExecutionManagerException("There are no template configurations in the domain " + executionManagerTemplate.getDomain());
        }
    }


    /**
     * Deploy the artifacts given in the template domain configuration
     *
     * @param executionManagerTemplate template domain object, containing the templates.
     * @param configuration configuration object, containing the parameters.
     */
    public static void deployArtifacts(ScenarioConfiguration configuration,
                                       ExecutionManagerTemplate executionManagerTemplate)
            throws ExecutionManagerException {
        //make sure common artifacts are deployed
        if (executionManagerTemplate.getCommonArtifacts() != null) {
            for (Artifact artifact : executionManagerTemplate.getCommonArtifacts().getArtifact()) {
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
        for (Scenario scenario : executionManagerTemplate.getScenarios().getScenario()) {
            for (Template template : scenario.getTemplates().getTemplate()) {
                if (scenario.getName().equals(configuration.getScenario())) {     //todo: check whether this check is necessary
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
    private static String updateArtifactParameters(ScenarioConfiguration config, String script) {
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
     * @param executionManagerTemplate ExecutionManagerTemplate object, containing the StreamMappings element
     * @return List of Stream IDs
     */
    public static List<String> getStreamIDsInMappings(ScenarioConfiguration configuration,
                                                      ExecutionManagerTemplate executionManagerTemplate) {
        List<String> streamIdList = new ArrayList<>();
        for (Scenario scenario : executionManagerTemplate.getScenarios().getScenario()){
            if (configuration.getName().equals(scenario.getName())) {  //todo: check whether this check is necessary
                if(scenario.getStreamMappings() != null && scenario.getStreamMappings().getStreamMapping() != null
                        && !scenario.getStreamMappings().getStreamMapping().isEmpty()) {    //if no stream mappings present, should return null
                    for (StreamMapping streamMapping: scenario.getStreamMappings().getStreamMapping()) {
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

    /**
     * Returns the execution plan which selects from the "fromStream" and inserts into the "toStream"
     *
     *
     * @param streamMapping object which specifies "fromStream", "toStream" and how attributes needs to be mapped.
     * @param templateConfigName
     *@param templateConfigFrom @return execution plan as a deploy-ready String.
     */
    public static String generateExecutionPlan(
            org.wso2.carbon.event.execution.manager.core.structure.configuration.StreamMapping streamMapping,
            String templateConfigName, String templateConfigFrom) {
        String fromStreamId = streamMapping.getFrom();
        String toStreamId = streamMapping.getTo();

        String fromStreamName = fromStreamId.split(EventStreamConstants.STREAM_DEFINITION_DELIMITER)[0];
        String toStreamName = toStreamId.split(EventStreamConstants.STREAM_DEFINITION_DELIMITER)[0];

        StreamDefinition fromStreamDefinition = ExecutionManagerValueHolder.getEventStreamService().getStreamDefinition(fromStreamId);
        StreamDefinition toStreamDefinition = ExecutionManagerValueHolder.getEventStreamService().getStreamDefinition(toStreamId);

        //@Plan:name() statement
        String planNameStatement = EXECUTION_PLAN_NAME_ANNOTATION + "('" + templateConfigFrom
                + DELIMETER + templateConfigName + DELIMETER + fromStreamName + "Mapping')";


        //@Import...define stream statements      //todo use common method
        String importStatement = IMPORT_ANNOTATION + "('" + fromStreamId + "')";
        StringBuilder fromStreamDefBuilder = new StringBuilder(DEFINE_STREAM + fromStreamName + " (");
        for (Attribute metaAttribute : fromStreamDefinition.getMetaData()) {
            fromStreamDefBuilder.append(EventStreamConstants.META_PREFIX + metaAttribute.getName()
                                        + " " + metaAttribute.getType() + ", ");
        }
        for (Attribute corrAttribute : fromStreamDefinition.getCorrelationData()) {
            fromStreamDefBuilder.append(EventStreamConstants.CORRELATION_PREFIX + corrAttribute.getName()
                                        + " " + corrAttribute.getType() + ", ");
        }
        for (Attribute payloadAttribute : fromStreamDefinition.getPayloadData()) {
            fromStreamDefBuilder.append(payloadAttribute.getName() + " " + payloadAttribute.getType() + ", ");
        }

        fromStreamDefBuilder.delete(fromStreamDefBuilder.length()-2, fromStreamDefBuilder.length()-1);
        fromStreamDefBuilder.append(");");
        String fromStreamDefinitionStr = fromStreamDefBuilder.toString();
        importStatement += fromStreamDefinitionStr;


        //@Export...define stream statements
        String exportStatement = EXPORT_ANNOTATION + "('" + toStreamId + "')";
        StringBuilder toStreamDefBuilder = new StringBuilder(DEFINE_STREAM + toStreamName + " (");
        for (Attribute metaAttribute : toStreamDefinition.getMetaData()) {
            toStreamDefBuilder.append(EventStreamConstants.META_PREFIX + metaAttribute.getName()
                                        + " " + metaAttribute.getType() + ", ");
        }
        for (Attribute corrAttribute : toStreamDefinition.getCorrelationData()) {
            toStreamDefBuilder.append(EventStreamConstants.CORRELATION_PREFIX + corrAttribute.getName()
                                        + " " + corrAttribute.getType() + ", ");
        }
        for (Attribute payloadAttribute : toStreamDefinition.getPayloadData()) {
            toStreamDefBuilder.append(payloadAttribute.getName() + " " + payloadAttribute.getType() + ", ");
        }

        toStreamDefBuilder.delete(toStreamDefBuilder.length()-2, toStreamDefBuilder.length()-1);
        toStreamDefBuilder.append(");");
        String toStreamDefinitionStr = toStreamDefBuilder.toString();
        exportStatement += toStreamDefinitionStr;


        //select ... insert into query
        StringBuilder queryBuilder = new StringBuilder(FROM + fromStreamName + " " + SELECT);
        for (AttributeMapping attributeMapping: streamMapping.getAttributeMappings().getAttributeMapping()) {
            queryBuilder.append(attributeMapping.getFrom() + AS + attributeMapping.getTo() + ", ");
        }
        queryBuilder.deleteCharAt(queryBuilder.length() - 2);
        queryBuilder.append(INSERT_INTO + toStreamName + ";");
        String query = queryBuilder.toString();

        return planNameStatement + importStatement + exportStatement + query;
    }


    public static void saveToRegistry(ScenarioConfiguration configuration)
            throws ExecutionManagerException {
        try {
            Registry registry = ExecutionManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            StringWriter fileContent = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(ScenarioConfiguration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, ExecutionManagerConstants.DEFAULT_CHARSET);

            jaxbMarshaller.marshal(configuration, fileContent);
            Resource resource = registry.newResource();
            resource.setContent(fileContent.toString());
            String resourceCollectionPath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                                            + "/" + configuration.getDomain();

            String resourcePath = resourceCollectionPath + "/"
                                  + configuration.getName() + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;

            //Collection directory will be created if it is not exist in the registry
            if (!registry.resourceExists(resourceCollectionPath)) {
                registry.put(resourceCollectionPath, registry.newCollection());
            }

            if (registry.resourceExists(resourcePath)) {
                registry.delete(resourcePath);
            }
            resource.setMediaType("application/xml");
            registry.put(resourcePath, resource);
        } catch (RegistryException e) {
            throw new ExecutionManagerException("Registry exception occurred when creating " + configuration.getName()
                                                + " configurations", e);

        } catch (JAXBException e) {
            throw new ExecutionManagerException("JAXB Exception when marshalling file at " + configuration.getName()
                                                + " configurations", e);
        }
    }

    /**
     * Returns a Template Configuration from the registry.
     *
     * @param templateConfigName Name of the Template Configuration.
     * @param templateConfigFrom Name of the Template Domain which contains the Template Configuration.
     * @return
     */
    public static ScenarioConfiguration getConfigurationFromRegistry(String templateConfigName,
                                                                     String templateConfigFrom)
            throws ExecutionManagerException {
        try {
            Registry registry = ExecutionManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());
            String resourceCollectionPath = ExecutionManagerConstants.TEMPLATE_CONFIG_PATH
                                            + "/" + templateConfigFrom;

            String resourcePath = resourceCollectionPath + "/"
                                  + templateConfigName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION;
            Resource resource = registry.get(resourcePath);

            JAXBContext jaxbContext = JAXBContext.newInstance(ScenarioConfiguration.class);
        }
        catch (RegistryException e) {
            throw new ExecutionManagerException("Registry exception occurred when trying to access configuration registry for tenant domain: "
                                                + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), e);
        } catch (JAXBException e) {
            throw new ExecutionManagerException("JAXB exception occurred when trying to access configuration registry for tenant domain: "
                                                + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), e);
        }
        return null; //todo: remove after completing impl
    }
}
