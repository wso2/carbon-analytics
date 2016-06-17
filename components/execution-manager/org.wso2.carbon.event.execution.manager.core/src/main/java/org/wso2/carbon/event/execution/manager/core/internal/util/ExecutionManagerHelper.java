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
import org.wso2.carbon.event.execution.manager.core.structure.domain.Domain;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.execution.manager.core.structure.domain.StreamMapping;
import org.wso2.carbon.event.execution.manager.core.structure.domain.Template;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
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
    private static final String DEFINE_STREAM = "define stream ";
    private static final String FROM = "from ";
    private static final String SELECT = "select ";
    private static final String AS = " as ";
    private static final String INSERT_INTO = "insert into ";

    private static enum DefineStreamTypes {IMPORT, EXPORT};

    /**
     * To avoid instantiating
     */
    private ExecutionManagerHelper() {
    }

    /**
     * Load All domains templates available in the file directory
     */
    public static Map<String, Domain> loadDomains() {
        //Get domain template folder and load all the domain template files
        File folder = new File(ExecutionManagerConstants.TEMPLATE_DOMAIN_PATH);
        Map<String, Domain> domains = new HashMap<>();

        File[] files = folder.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isFile() && fileEntry.getName().endsWith("xml")) {
                    Domain domain = unmarshalDomain(fileEntry);
                    if (domain != null) {
                        try {
                            validateTemplateDomainConfig(domain);
                        } catch (ExecutionManagerException e) {
                            //In case an invalid template configuration is found, this loader logs
                            // an error message and aborts loading that particular template domain config.
                            //However, this will load all the valid template domain configurations.
                            log.error("Invalid Template Domain configuration file found: " + fileEntry.getName(), e);
                        }
                        domains.put(domain.getName(), domain);
                    } else {
                        log.error("Invalid Template Domain configuration file found: " + fileEntry.getName());
                    }

                }
            }
        }

        return domains;
    }

    /**
     * Unmarshalling Domain object by given file
     *
     * @param fileEntry file for unmarshalling
     * @return templateDomain object
     */
    private static Domain unmarshalDomain(File fileEntry) {
        Domain domain = null;

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Domain.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            domain = (Domain) jaxbUnmarshaller.unmarshal(fileEntry);

        } catch (JAXBException e) {
            log.error("JAXB Exception when unmarshalling domain template file at "
                    + fileEntry.getPath(), e);
        }

        return domain;

    }

    /**
     * Unmarshalling Domain object by given file content object
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
     * @param domain Domain object which needs to be validated.
     * @throws ExecutionManagerException
     */
    public static void validateTemplateDomainConfig(Domain domain)
            throws ExecutionManagerException {
        if (domain.getScenarios() == null ||
            domain.getScenarios().getScenario() == null ||
            domain.getScenarios().getScenario().isEmpty()) {
            //It is required to have at least one ScenarioConfiguration.
            //Having only a set of common artifacts is not a valid use case.
            throw new ExecutionManagerException("There are no template configurations in the domain " + domain.getName());
        }
    }


    /**
     * Deploy the artifacts given in the template domain configuration
     *
     * @param domain template domain object, containing the templates.
     * @param configuration scenario configuration object, containing the parameters.
     */
    public static void deployArtifacts(ScenarioConfiguration configuration,
                                       Domain domain)
            throws TemplateDeploymentException {
        //make sure common artifacts are deployed
        if (domain.getCommonArtifacts() != null) {
            Map<String,Integer> artifactTypeCountingMap = new HashMap<>();
            for (Artifact artifact : domain.getCommonArtifacts().getArtifact()) {
                    String artifactType = artifact.getType();
                    Integer artifactCount = artifactTypeCountingMap.get(artifactType);
                    if (artifactCount == null) {
                        artifactCount = 1;  //Count starts with one, instead of zero for user-friendliness.
                    } else {
                        artifactCount++;
                    }
                    String artifactId = ExecutionManagerHelper.getCommonArtifactId(domain.getName(), artifactType, artifactCount);

                    DeployableTemplate deployableTemplate = new DeployableTemplate();
                    deployableTemplate.setArtifact(artifact.getValue());
                    deployableTemplate.setConfiguration(configuration);
                    deployableTemplate.setArtifactId(artifactId);
                    TemplateDeployer deployer = ExecutionManagerValueHolder.getTemplateDeployers().get(artifact.getType());
                    if (deployer != null) {
                        deployer.deployIfNotDoneAlready(deployableTemplate);
                        artifactTypeCountingMap.put(artifactType, artifactCount);
                    } else {
                        throw new TemplateDeploymentException("A deployer doesn't exist for template type " + artifact.getType());
                    }
            }
        }
        //now, deploy templated artifacts
        for (Scenario scenario : domain.getScenarios().getScenario()) {
            if (scenario.getType().equals(configuration.getScenario())) {
                Map<String,Integer> artifactTypeCountingMap = new HashMap<>();
                for (Template template : scenario.getTemplates().getTemplate()) {
                    String artifactType = template.getType();
                    Integer artifactCount = artifactTypeCountingMap.get(artifactType);
                    if (artifactCount == null) {
                        artifactCount = 1;  //Count starts with one, instead of zero for user-friendliness.
                    } else {
                        artifactCount++;
                    }
                    String artifactId = ExecutionManagerHelper.getTemplatedArtifactId(domain.getName(),
                                                                                      scenario.getType(), configuration.getName(), artifactType, artifactCount);
                    TemplateDeployer deployer = ExecutionManagerValueHolder.getTemplateDeployers().get(template.getType());
                    if (deployer != null) {
                            DeployableTemplate deployableTemplate = new DeployableTemplate();
                            String updatedScript = updateArtifactParameters(configuration, template.getValue());
                            deployableTemplate.setArtifact(updatedScript);
                            deployableTemplate.setConfiguration(configuration);
                            deployableTemplate.setArtifactId(artifactId);
                            deployer.deployArtifact(deployableTemplate);
                            artifactTypeCountingMap.put(artifactType, artifactCount);
                    } else {
                        throw new TemplateDeploymentException("A deployer doesn't exist for template type " + template.getType());
                    }
                }
                break;
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
     * @param artifactId Artifact ID
     * @param type       type of the artifact e.g Realtime, batch
     * @throws TemplateDeploymentException
     */
    public static void unDeployExistingArtifact(String artifactId, String type)
            throws TemplateDeploymentException {
        TemplateDeployer deployer = ExecutionManagerValueHolder.getTemplateDeployers().get(type);
        deployer.undeployArtifact(artifactId);
    }


    /**
     * Returns the list of Stream IDs(with their template symbols replaced by user-parameters) given in StreamMappings element.
     *
     * @param configuration Scenario configuration, specified by the user, containing parameter values.
     * @param domain Domain object, containing the StreamMappings element
     * @return List of Stream IDs
     */
    public static List<String> getStreamIDsToBeMapped(ScenarioConfiguration configuration,
                                                      Domain domain) {
        List<String> streamIdList = new ArrayList<>();
        for (Scenario scenario : domain.getScenarios().getScenario()){
            if (configuration.getScenario().equals(scenario.getType())) {
                if(scenario.getStreamMappings() != null && scenario.getStreamMappings().getStreamMapping() != null
                        && !scenario.getStreamMappings().getStreamMapping().isEmpty()) {
                        //empty check is required because, if no stream mappings present, we should return null
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
                break;
            }
        }
        return null;
    }

    /**
     * Returns the execution plan which selects from the "fromStream" and inserts into the "toStream"
     *
     *
     * @param streamMappingList object which specifies "fromStream", "toStream" and how attributes needs to be mapped.
     * @return execution plan as a deploy-ready String.
     */
    public static String generateExecutionPlan(
            List<org.wso2.carbon.event.execution.manager.core.structure.configuration.StreamMapping> streamMappingList,
            String planName)
            throws ExecutionManagerException {
        //@Plan:name() statement
        String planNameStatement = EXECUTION_PLAN_NAME_ANNOTATION + "('" + planName + "') \n\n";

        StringBuilder importStatementBuilder = new StringBuilder(); //for building "@Import..define stream.." statements
        StringBuilder exportStatementBuilder = new StringBuilder(); //for building "@Export..define stream.." statements
        StringBuilder queryBuilder = new StringBuilder();           //for building "select ... insert into..." query

        for (org.wso2.carbon.event.execution.manager.core.structure.configuration.StreamMapping streamMapping: streamMappingList) {
            String fromStreamId = streamMapping.getFrom();
            String toStreamId = streamMapping.getTo();
            String internalFromStreamId = getInternalStreamId(fromStreamId);
            String internalToStreamId = getInternalStreamId(toStreamId);

            importStatementBuilder.append(generateDefineStreamStatements(DefineStreamTypes.IMPORT, fromStreamId, internalFromStreamId)).append("\n\n");
            exportStatementBuilder.append(generateDefineStreamStatements(DefineStreamTypes.EXPORT, toStreamId, internalToStreamId)).append("\n\n");

            queryBuilder.append(FROM).append(internalFromStreamId).append(" \n").append(SELECT);
            for (AttributeMapping attributeMapping: streamMapping.getAttributeMappings().getAttributeMapping()) {
                queryBuilder.append(attributeMapping.getFrom()).append(AS).append(attributeMapping.getTo()).append(", ");
            }
            queryBuilder.deleteCharAt(queryBuilder.length() - 2);
            queryBuilder.append("\n").append(INSERT_INTO).append(internalToStreamId).append(";\n\n");
        }

        return planNameStatement + importStatementBuilder.toString() + exportStatementBuilder.toString() + queryBuilder.toString();
    }

    /**
     * Returns a StreamId which can be used as an internal Stream ID in a Siddhi Query.
     * @param streamId Stream ID (as in CEP)
     * @return internal Stream ID.
     */
    private static String getInternalStreamId(String streamId) {
        String internalStreamId = streamId.replace(EventStreamConstants.STREAM_DEFINITION_DELIMITER, "__");
        return internalStreamId.replace(".", "_");
    }

    private static String generateDefineStreamStatements(DefineStreamTypes type, String streamId, String internalStreamId)
            throws ExecutionManagerException {
        try {
            StreamDefinition streamDefinition = ExecutionManagerValueHolder.getEventStreamService().getStreamDefinition(streamId);
            if (streamDefinition == null) {
                throw new ExecutionManagerException("No stream has being deployed with Stream ID: " + streamId);
            }

            String statement = "@" + type.toString() + "('" + streamId + "')\n";
            StringBuilder streamDefBuilder = new StringBuilder(DEFINE_STREAM + internalStreamId + " (");
            if (streamDefinition.getMetaData() != null) {
                for (Attribute metaAttribute : streamDefinition.getMetaData()) {
                    streamDefBuilder.append(ExecutionManagerConstants.META_PREFIX).append(metaAttribute.getName()).append(" ").append(metaAttribute.getType()).append(", ");
                }
            }
            if (streamDefinition.getCorrelationData() != null) {
                for (Attribute corrAttribute : streamDefinition.getCorrelationData()) {
                    streamDefBuilder.append(ExecutionManagerConstants.CORRELATION_PREFIX).append(corrAttribute.getName()).append(" ").append(corrAttribute.getType()).append(", ");
                }
            }
            if (streamDefinition.getPayloadData() != null) {
                for (Attribute payloadAttribute : streamDefinition.getPayloadData()) {
                    streamDefBuilder.append(payloadAttribute.getName()).append(" ").append(payloadAttribute.getType()).append(", ");
                }
            }

            streamDefBuilder.delete(streamDefBuilder.length() - 2, streamDefBuilder.length() - 1);
            streamDefBuilder.append(");");
            String toStreamDefinitionStr = streamDefBuilder.toString();
            statement += toStreamDefinitionStr;
            return statement;
        } catch (EventStreamConfigurationException e) {
            throw new ExecutionManagerException("Failed to get stream definition for Stream ID: " + streamId, e);
        }
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
        } catch (JAXBException e) {
            throw new ExecutionManagerException("Could not marshall Scenario: " + configuration.getName() + ", for Domain: "
                                                + configuration.getDomain() + ". Could not save to registry.", e);
        } catch (RegistryException e) {
            throw new ExecutionManagerException("Could not save Scenario: " + configuration.getName() + ", for Domain: "
                                                + configuration.getDomain() + ", to the registry.", e);
        }
    }


    /**
     * Deletes a scenario config from the registry. This does not undeploy any artifact.
     * If some error occurred in the deployment process, this can be used for "rolling back" the config saved in the registry.
     *
     * @param domainName domain name
     * @param configName config name
     * @throws RegistryException
     */
    public static void deleteConfigWithoutUndeploy(String domainName, String configName)
            throws ExecutionManagerException {
        try {
        Registry registry = ExecutionManagerValueHolder.getRegistryService()
                .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

        registry.delete(ExecutionManagerConstants.TEMPLATE_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR
                        + domainName + RegistryConstants.PATH_SEPARATOR + configName + ExecutionManagerConstants.CONFIG_FILE_EXTENSION);
        } catch (RegistryException e) {
            throw new ExecutionManagerException("Failed to delete scenario from the registry. Scenario name: " + configName
                                                + ", Domain name: " + domainName, e);
        }
    }


    /**
     * Provide template configurations available in the given registry and given path
     *
     * @param path where configurations are stored
     * @return available configurations
     */
    public static ScenarioConfiguration getConfiguration(String path)
            throws ExecutionManagerException {

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
            throw new ExecutionManagerException("Registry exception occurred when accessing files at "
                                                + ExecutionManagerConstants.TEMPLATE_CONFIG_PATH, e);
        }

        return scenarioConfiguration;
    }


    /**
     * Returns the ID of a templated artifact.
     * @param domainName
     * @param scenarioName
     * @param scenarioConfigName
     * @param artifactType
     * @param sequenceNumber
     * @return
     */
    public static String getTemplatedArtifactId(String domainName, String scenarioName,
                                                String scenarioConfigName, String artifactType,
                                                int sequenceNumber) {
        return domainName + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR
               + scenarioName + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + scenarioConfigName
               + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + artifactType + sequenceNumber;
    }

    /**
     * Returns the ID of a Stream Mapping execution plan.
     * @param domainName
     * @param scenarioConfigName
     * @return
     */
    public static String getStreamMappingPlanId(String domainName, String scenarioConfigName) {
        return domainName + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + scenarioConfigName
                          + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + ExecutionManagerConstants.STREAM_MAPPING_PLAN_SUFFIX;
    }

    /**
     * Returns the ID of a common artifact.
     * @param domainName
     * @param artifactType
     * @param sequenceNumber
     * @return
     */
    public static String getCommonArtifactId(String domainName, String artifactType,
                                                int sequenceNumber) {
        return domainName + ExecutionManagerConstants.CONFIG_NAME_SEPARATOR + artifactType + sequenceNumber;
    }
}
