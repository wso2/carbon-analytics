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
package org.wso2.carbon.event.template.manager.core.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.template.manager.core.exception.TemplateManagerException;
import org.wso2.carbon.event.template.manager.core.internal.ds.TemplateManagerValueHolder;
import org.wso2.carbon.event.template.manager.core.structure.configuration.AttributeMapping;
import org.wso2.carbon.event.template.manager.core.structure.configuration.ScenarioConfiguration;
import org.wso2.carbon.event.template.manager.core.structure.domain.Artifact;
import org.wso2.carbon.event.template.manager.core.structure.domain.Domain;
import org.wso2.carbon.event.template.manager.core.structure.domain.Scenario;
import org.wso2.carbon.event.template.manager.core.structure.domain.Script;
import org.wso2.carbon.event.template.manager.core.structure.domain.StreamMapping;
import org.wso2.carbon.event.template.manager.core.structure.domain.Template;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class consist of the helper methods which are required to deal with domain templates stored in the file directory,
 * configurations stored as resources in the registry, deploy execution plans and deploy streams
 */
public class TemplateManagerHelper {

    private static final Log log = LogFactory.getLog(TemplateManagerHelper.class);
    private static final String EXECUTION_PLAN_NAME_ANNOTATION = "@Plan:name";
    private static final String DEFINE_STREAM = "define stream ";
    private static final String FROM = "from ";
    private static final String SELECT = "select ";
    private static final String AS = " as ";
    private static final String INSERT_INTO = "insert into ";

    private static enum DefineStreamTypes {IMPORT, EXPORT}

    ;

    /**
     * To avoid instantiating
     */
    private TemplateManagerHelper() {
    }

    /**
     * Load All domains templates available in the file directory
     */
    public static Map<String, Domain> loadDomains() {
        //Get domain template folder and load all the domain template files
        File folder = new File(TemplateManagerConstants.TEMPLATE_DOMAIN_PATH);
        Map<String, Domain> domains = new HashMap<>();

        File[] files = folder.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                if (fileEntry.isFile() && fileEntry.getName().endsWith("xml")) {
                    Domain domain = unmarshalDomain(fileEntry);
                    if (domain != null) {
                        try {
                            validateTemplateDomainConfig(domain);
                        } catch (TemplateManagerException e) {
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
     * @throws TemplateManagerException
     */
    public static void validateTemplateDomainConfig(Domain domain)
            throws TemplateManagerException {
        if (domain.getScenarios() == null ||
                domain.getScenarios().getScenario() == null ||
                domain.getScenarios().getScenario().isEmpty()) {
            //It is required to have at least one ScenarioConfiguration.
            //Having only a set of common artifacts is not a valid use case.
            throw new TemplateManagerException("There are no template configurations in the domain " + domain.getName());
        }
    }


    /**
     * Deploy the artifacts given in the template domain configuration
     *
     * @param domain        template domain object, containing the templates.
     * @param configuration scenario configuration object, containing the parameters.
     */
    public static void deployArtifacts(ScenarioConfiguration configuration,
                                       Domain domain, ScriptEngine scriptEngine)
            throws TemplateDeploymentException {
        //make sure common artifacts are deployed
        if (domain.getCommonArtifacts() != null) {
            Map<String, Integer> artifactTypeCountingMap = new HashMap<>();
            for (Artifact artifact : domain.getCommonArtifacts().getArtifact()) {
                String artifactType = artifact.getType();
                Integer artifactCount = artifactTypeCountingMap.get(artifactType);
                if (artifactCount == null) {
                    artifactCount = 1;  //Count starts with one, instead of zero for user-friendliness.
                } else {
                    artifactCount++;
                }
                String artifactId = TemplateManagerHelper.getCommonArtifactId(domain.getName(), artifactType, artifactCount);

                DeployableTemplate deployableTemplate = new DeployableTemplate();
                deployableTemplate.setArtifact(artifact.getValue());
                deployableTemplate.setConfiguration(configuration);
                deployableTemplate.setArtifactId(artifactId);
                TemplateDeployer deployer = TemplateManagerValueHolder.getTemplateDeployers().get(artifact.getType());
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
                Map<String, Integer> artifactTypeCountingMap = new HashMap<>();
                for (Template template : scenario.getTemplates().getTemplate()) {
                    String artifactType = template.getType();
                    Integer artifactCount = artifactTypeCountingMap.get(artifactType);
                    if (artifactCount == null) {
                        artifactCount = 1;  //Count starts with one, instead of zero for user-friendliness.
                    } else {
                        artifactCount++;
                    }
                    String artifactId = TemplateManagerHelper.getTemplatedArtifactId(domain.getName(),
                            scenario.getType(), configuration.getName(), artifactType, artifactCount);
                    TemplateDeployer deployer = TemplateManagerValueHolder.getTemplateDeployers().get(template.getType());
                    if (deployer != null) {
                        DeployableTemplate deployableTemplate = new DeployableTemplate();
                        String updatedScript = updateArtifactParameters(configuration, template.getValue(), scriptEngine);
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
     * Create a JavaScript engine packed with given scripts. If two scripts have methods with same name,
     * later method will override the previous method.
     *
     * @param domain
     * @return JavaScript engine
     * @throws TemplateDeploymentException if there are any errors in JavaScript evaluation
     */
    public static ScriptEngine createJavaScriptEngine(Domain domain) throws TemplateDeploymentException {

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(TemplateManagerConstants.JAVASCRIPT_ENGINE_NAME);

        if (scriptEngine == null) {
            // Exception will be thrown later, only if function calls are used in the template
            log.warn("JavaScript engine is not available. Function calls in the templates cannot be evaluated");
        } else {
            if (domain != null && domain.getScripts() != null && domain.getScripts().getScript() != null) {
                Path scriptDirectory = Paths.get(TemplateManagerConstants.TEMPLATE_SCRIPT_PATH);
                if (Files.exists(scriptDirectory, LinkOption.NOFOLLOW_LINKS) && Files.isDirectory(scriptDirectory)) {
                    for (Script script : domain.getScripts().getScript()) {
                        String src = script.getSrc();
                        String content = script.getContent();
                        if (src != null) {
                            // Evaluate JavaScript file
                            Path scriptFile = scriptDirectory.resolve(src).normalize();
                            if (Files.exists(scriptFile, LinkOption.NOFOLLOW_LINKS) && Files.isReadable(scriptFile)) {
                                if (!scriptFile.startsWith(scriptDirectory)) {
                                    // The script file is not in the permitted directory
                                    throw new TemplateDeploymentException("Script file " + scriptFile.toAbsolutePath() + " is not in the permitted directory " + scriptDirectory.toAbsolutePath());
                                }
                                try {
                                    scriptEngine.eval(Files.newBufferedReader(scriptFile, Charset.defaultCharset()));
                                } catch (ScriptException e) {
                                    throw new TemplateDeploymentException("Error in JavaScript " + scriptFile.toAbsolutePath() + ": " + e.getMessage(), e);
                                } catch (IOException e) {
                                    throw new TemplateDeploymentException("Error in reading JavaScript file: " + scriptFile.toAbsolutePath());
                                }
                            } else {
                                throw new TemplateDeploymentException("JavaScript file not exist at: " + scriptFile.toAbsolutePath() + " or not readable.");
                            }
                        }
                        if (content != null) {
                            // Evaluate JavaScript content
                            try {
                                scriptEngine.eval(content);
                            } catch (ScriptException e) {
                                throw new TemplateDeploymentException("JavaScript declared in " + domain.getName() + " has error", e);
                            }
                        }
                    }
                } else {
                    log.warn("Script directory not found at: " + scriptDirectory.toAbsolutePath());
                }
            }
        }

        return scriptEngine;
    }

    /**
     * This method searches for expressions matching {@link TemplateManagerConstants#TEMPLATE_SCRIPT_REGEX}, evaluate them and
     * replace them by the result of those expressions. If user make any syntax errors in expression and if it does
     * not match with REGEX pattern, they will not be considered as an expression to evaluate.
     * For example, ${toID('My Temperature')} will execute the 'toID' method and replace the value.
     * ${toID('My Temperature)} will not be considered as an expression since the closing single quote is missing.
     *
     * @param content      actual template content to be checked
     * @param scriptEngine JavaScript engine with pre-loaded scripts
     * @return updated content
     * @throws TemplateDeploymentException if there are any errors in JavaScript function evaluation
     */
    private static String replaceScriptExpressions(String content, ScriptEngine scriptEngine) throws TemplateDeploymentException {
        StringBuffer buffer = new StringBuffer();
        Pattern pattern = Pattern.compile(TemplateManagerConstants.TEMPLATE_SCRIPT_REGEX);
        Matcher matcher = pattern.matcher(content);
        final int scriptEvaluatorPrefixLength = TemplateManagerConstants.SCRIPT_EVALUATOR_PREFIX.length();
        final int scriptEvaluatorSuffixLength = TemplateManagerConstants.SCRIPT_EVALUATOR_SUFFIX.length();
        if (scriptEngine == null && matcher.find()) {   // Do not alter the order of conditions
            // If script engine is not available and at lest one function call is used, throw the exception
            throw new TemplateDeploymentException("JavaScript engine is not available in the current JRE to evaluate the function calls given in the template.");
        }
        while (matcher.find()) {
            String expression = matcher.group();
            int expressionLength = expression.length();
            String scriptToEvaluate = expression.substring(scriptEvaluatorPrefixLength, expressionLength - scriptEvaluatorSuffixLength);
            String result = "";     // Empty string, if there is no output
            try {
                Object output = scriptEngine.eval(scriptToEvaluate);
                if (output != null) {
                    result = output.toString();
                }
            } catch (ScriptException e) {
                throw new TemplateDeploymentException("Error in evaluating JavaScript expression: " + expression, e);
            }
            matcher.appendReplacement(buffer, result);
        }
        matcher.appendTail(buffer); // Append the remaining text

        return buffer.toString();
    }

    /**
     * Update given script by replacing undefined parameter values with configured parameter values
     *
     * @param config       configurations which consists of parameters which will replace
     * @param script       script which needs to be updated
     * @param scriptEngine JavaScript engine with pre-loaded scripts
     * @return updated execution plan
     * @throws TemplateDeploymentException if there are any errors in JavaScript function evaluation
     */
    private static String updateArtifactParameters(ScenarioConfiguration config, String script, ScriptEngine scriptEngine)
            throws TemplateDeploymentException {
        String updatedScript = script;
        //Script parameters will be replaced with given configuration parameters
        if (config.getParameterMap() != null && script != null) {
            for (Map.Entry parameterMapEntry : config.getParameterMap().entrySet()) {
                updatedScript = updatedScript.replaceAll(TemplateManagerConstants.REGEX_NAME_VALUE
                        + parameterMapEntry.getKey().toString(), parameterMapEntry.getValue().toString());
            }
        }

        return replaceScriptExpressions(updatedScript, scriptEngine);
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
        TemplateDeployer deployer = TemplateManagerValueHolder.getTemplateDeployers().get(type);
        deployer.undeployArtifact(artifactId);
    }


    /**
     * Returns the list of Stream IDs(with their template symbols replaced by user-parameters) given in StreamMappings element.
     *
     * @param configuration Scenario configuration, specified by the user, containing parameter values.
     * @param domain        Domain object, containing the StreamMappings element
     * @param scriptEngine  JavaScript engine with pre-loaded scripts
     * @return List of Stream IDs
     * @throws TemplateDeploymentException if there are any errors in JavaScript function evaluation
     */
    public static List<String> getStreamIDsToBeMapped(ScenarioConfiguration configuration,
                                                      Domain domain, ScriptEngine scriptEngine) throws TemplateDeploymentException {
        List<String> streamIdList = new ArrayList<>();
        for (Scenario scenario : domain.getScenarios().getScenario()) {
            if (configuration.getScenario().equals(scenario.getType())) {
                if (scenario.getStreamMappings() != null && scenario.getStreamMappings().getStreamMapping() != null
                        && !scenario.getStreamMappings().getStreamMapping().isEmpty()) {
                    //empty check is required because, if no stream mappings present, we should return null
                    for (StreamMapping streamMapping : scenario.getStreamMappings().getStreamMapping()) {
                        String toStream = streamMapping.getTo();
                        for (Map.Entry entry : configuration.getParameterMap().entrySet()) {
                            toStream = toStream.replaceAll(TemplateManagerConstants.REGEX_NAME_VALUE
                                    + entry.getKey().toString(), entry.getValue().toString());
                        }
                        streamIdList.add(replaceScriptExpressions(toStream, scriptEngine));
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
     * @param streamMappingList object which specifies "fromStream", "toStream" and how attributes needs to be mapped.
     * @return execution plan as a deploy-ready String.
     */
    public static String generateExecutionPlan(
            List<org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping> streamMappingList,
            String planName)
            throws TemplateManagerException {
        //@Plan:name() statement
        String planNameStatement = EXECUTION_PLAN_NAME_ANNOTATION + "('" + planName + "') \n\n";

        StringBuilder importStatementBuilder = new StringBuilder(); //for building "@Import..define stream.." statements
        StringBuilder exportStatementBuilder = new StringBuilder(); //for building "@Export..define stream.." statements
        StringBuilder queryBuilder = new StringBuilder();           //for building "select ... insert into..." query

        for (org.wso2.carbon.event.template.manager.core.structure.configuration.StreamMapping streamMapping : streamMappingList) {
            String fromStreamId = streamMapping.getFrom();
            String toStreamId = streamMapping.getTo();
            String internalFromStreamId = getInternalStreamId(fromStreamId);
            String internalToStreamId = getInternalStreamId(toStreamId);

            importStatementBuilder.append(generateDefineStreamStatements(DefineStreamTypes.IMPORT, fromStreamId, internalFromStreamId)).append("\n\n");
            exportStatementBuilder.append(generateDefineStreamStatements(DefineStreamTypes.EXPORT, toStreamId, internalToStreamId)).append("\n\n");

            queryBuilder.append(FROM).append(internalFromStreamId).append(" \n").append(SELECT);
            for (AttributeMapping attributeMapping : streamMapping.getAttributeMappings().getAttributeMapping()) {
                queryBuilder.append(attributeMapping.getFrom()).append(AS).append(attributeMapping.getTo()).append(", ");
            }
            queryBuilder.deleteCharAt(queryBuilder.length() - 2);
            queryBuilder.append("\n").append(INSERT_INTO).append(internalToStreamId).append(";\n\n");
        }

        return planNameStatement + importStatementBuilder.toString() + exportStatementBuilder.toString() + queryBuilder.toString();
    }

    /**
     * Returns a StreamId which can be used as an internal Stream ID in a Siddhi Query.
     *
     * @param streamId Stream ID (as in CEP)
     * @return internal Stream ID.
     */
    private static String getInternalStreamId(String streamId) {
        String internalStreamId = streamId.replace(EventStreamConstants.STREAM_DEFINITION_DELIMITER, "__");
        return internalStreamId.replace(".", "_");
    }

    private static String generateDefineStreamStatements(DefineStreamTypes type, String streamId, String internalStreamId)
            throws TemplateManagerException {
        try {
            StreamDefinition streamDefinition = TemplateManagerValueHolder.getEventStreamService().getStreamDefinition(streamId);
            if (streamDefinition == null) {
                throw new TemplateManagerException("No stream has being deployed with Stream ID: " + streamId);
            }

            String statement = "@" + type.toString() + "('" + streamId + "')\n";
            StringBuilder streamDefBuilder = new StringBuilder(DEFINE_STREAM + internalStreamId + " (");
            if (streamDefinition.getMetaData() != null) {
                for (Attribute metaAttribute : streamDefinition.getMetaData()) {
                    streamDefBuilder.append(TemplateManagerConstants.META_PREFIX).append(metaAttribute.getName()).append(" ").append(metaAttribute.getType()).append(", ");
                }
            }
            if (streamDefinition.getCorrelationData() != null) {
                for (Attribute corrAttribute : streamDefinition.getCorrelationData()) {
                    streamDefBuilder.append(TemplateManagerConstants.CORRELATION_PREFIX).append(corrAttribute.getName()).append(" ").append(corrAttribute.getType()).append(", ");
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
            throw new TemplateManagerException("Failed to get stream definition for Stream ID: " + streamId, e);
        }
    }

    public static void saveToRegistry(ScenarioConfiguration configuration)
            throws TemplateManagerException {
        try {
            Registry registry = TemplateManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            StringWriter fileContent = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(ScenarioConfiguration.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_ENCODING, TemplateManagerConstants.DEFAULT_CHARSET);

            jaxbMarshaller.marshal(configuration, fileContent);
            Resource resource = registry.newResource();
            resource.setContent(fileContent.toString());
            String resourceCollectionPath = TemplateManagerConstants.TEMPLATE_CONFIG_PATH
                    + "/" + configuration.getDomain();

            String resourcePath = resourceCollectionPath + "/"
                    + configuration.getName() + TemplateManagerConstants.CONFIG_FILE_EXTENSION;

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
            throw new TemplateManagerException("Could not marshall Scenario: " + configuration.getName() + ", for Domain: "
                    + configuration.getDomain() + ". Could not save to registry.", e);
        } catch (RegistryException e) {
            throw new TemplateManagerException("Could not save Scenario: " + configuration.getName() + ", for Domain: "
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
            throws TemplateManagerException {
        try {
            Registry registry = TemplateManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId());

            registry.delete(TemplateManagerConstants.TEMPLATE_CONFIG_PATH + RegistryConstants.PATH_SEPARATOR
                    + domainName + RegistryConstants.PATH_SEPARATOR + configName + TemplateManagerConstants.CONFIG_FILE_EXTENSION);
        } catch (RegistryException e) {
            throw new TemplateManagerException("Failed to delete scenario from the registry. Scenario name: " + configName
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
            throws TemplateManagerException {

        ScenarioConfiguration scenarioConfiguration = null;
        try {
            Registry registry = TemplateManagerValueHolder.getRegistryService().getConfigSystemRegistry(PrivilegedCarbonContext
                    .getThreadLocalCarbonContext().getTenantId());

            if (registry.resourceExists(path)) {
                Resource configFile = registry.get(path);
                if (configFile != null) {
                    scenarioConfiguration = unmarshalConfiguration(configFile.getContent());
                }
            }
        } catch (RegistryException e) {
            throw new TemplateManagerException("Registry exception occurred when accessing files at "
                    + TemplateManagerConstants.TEMPLATE_CONFIG_PATH, e);
        }

        return scenarioConfiguration;
    }


    /**
     * Returns the ID of a templated artifact.
     *
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
        return domainName + TemplateManagerConstants.CONFIG_NAME_SEPARATOR
                + scenarioName + TemplateManagerConstants.CONFIG_NAME_SEPARATOR + scenarioConfigName
                + TemplateManagerConstants.CONFIG_NAME_SEPARATOR + artifactType + sequenceNumber;
    }

    /**
     * Returns the ID of a Stream Mapping execution plan.
     *
     * @param domainName
     * @param scenarioConfigName
     * @return
     */
    public static String getStreamMappingPlanId(String domainName, String scenarioConfigName) {
        return domainName + TemplateManagerConstants.CONFIG_NAME_SEPARATOR + scenarioConfigName
                + TemplateManagerConstants.CONFIG_NAME_SEPARATOR + TemplateManagerConstants.STREAM_MAPPING_PLAN_SUFFIX;
    }

    /**
     * Returns the ID of a common artifact.
     *
     * @param domainName
     * @param artifactType
     * @param sequenceNumber
     * @return
     */
    public static String getCommonArtifactId(String domainName, String artifactType,
                                             int sequenceNumber) {
        return domainName + TemplateManagerConstants.CONFIG_NAME_SEPARATOR + artifactType + sequenceNumber;
    }
}
