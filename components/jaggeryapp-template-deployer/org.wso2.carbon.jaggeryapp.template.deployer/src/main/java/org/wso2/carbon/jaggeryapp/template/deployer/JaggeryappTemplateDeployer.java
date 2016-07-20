/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.jaggeryapp.template.deployer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.JaggeryappTemplateDeployerConstants;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.JaggeryappTemplateDeployerException;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.JaggeryappTemplateDeployerValueHolder;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.util.JaggeryappTemplateDeployerHelper;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.util.JaggeryappTemplateDeployerUtility;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class JaggeryappTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(JaggeryappTemplateDeployer.class);

    @Override
    public String getType() {
        return JaggeryappTemplateDeployerConstants.ARTIFACT_TYPE;
    }

    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        String artifactId = template.getArtifactId();
        String content = template.getArtifact();

        Map<String, String> artifacts = new HashMap<>();
        Map<String, String> properties = new HashMap<>();

        DocumentBuilderFactory factory = JaggeryappTemplateDeployerHelper.getSecuredDocumentBuilder();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(content)));
            NodeList configNodes = document.getElementsByTagName(JaggeryappTemplateDeployerConstants.CONFIG_TAG);
            if (configNodes.getLength() > 0) {
                Node configNode = configNodes.item(0);  // Only one node is expected
                if (configNode.hasChildNodes()) {

                    // Extract the details
                    NodeList nodeList = configNode.getChildNodes();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (JaggeryappTemplateDeployerConstants.PROPERTIES_TAG.equalsIgnoreCase(node.getNodeName()) && node.hasChildNodes()) {
                            // Properties
                            NodeList propertiesNodeList = node.getChildNodes();
                            for (int j = 0; j < propertiesNodeList.getLength(); j++) {
                                Node propertyNode = propertiesNodeList.item(j);
                                if (JaggeryappTemplateDeployerConstants.PROPERTY_TAG.equalsIgnoreCase(propertyNode.getNodeName())) {
                                    Attr attr = (Attr) propertyNode.getAttributes().getNamedItem(JaggeryappTemplateDeployerConstants.NAME_ATTRIBUTE);
                                    properties.put(attr.getValue(), propertyNode.getFirstChild().getNodeValue().trim());
                                }
                            }
                        } else if (JaggeryappTemplateDeployerConstants.ARTIFACTS_TAG.equalsIgnoreCase(node.getNodeName()) && node.hasChildNodes()) {
                            NodeList artifactNodeList = node.getChildNodes();
                            for (int j = 0; j < artifactNodeList.getLength(); j++) {
                                Node artifactNode = artifactNodeList.item(j);
                                if (JaggeryappTemplateDeployerConstants.ARTIFACT_TAG.equalsIgnoreCase(artifactNode.getNodeName())) {
                                    Attr attr = (Attr) artifactNode.getAttributes().getNamedItem(JaggeryappTemplateDeployerConstants.FILE_ATTRIBUTE);
                                    artifacts.put(attr.getValue(), artifactNode.getFirstChild().getNodeValue());
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw new JaggeryappTemplateDeployerException("Error in creating XML document builder. ", e);
        } catch (SAXException e) {
            throw new JaggeryappTemplateDeployerException("Error in parsing XML content of: " + artifactId, e);
        } catch (IOException e) {
            throw new JaggeryappTemplateDeployerException("Error in loading XML content of: " + artifactId, e);
        }

        if (!properties.containsKey(JaggeryappTemplateDeployerConstants.DIRECTORY_NAME)) {
            throw new JaggeryappTemplateDeployerException("Artifact does not contain " + JaggeryappTemplateDeployerConstants.DIRECTORY_NAME + " property.");
        }
        String jaggeryArtifactPath = JaggeryappTemplateDeployerUtility.getJaggeryappArtifactPath();
        File destination = new File(jaggeryArtifactPath + properties.get(JaggeryappTemplateDeployerConstants.DIRECTORY_NAME));
        JaggeryappTemplateDeployerHelper.validateFilePath(properties.get(JaggeryappTemplateDeployerConstants.DIRECTORY_NAME));

        String templateParentDirectory = new StringBuilder(CarbonUtils.getCarbonConfigDirPath())
                .append(File.separator).append(JaggeryappTemplateDeployerConstants.TEMPLATE_MANAGER).append(File.separator)
                .append(JaggeryappTemplateDeployerConstants.JAGGERYAPP_TEMPLATES).append(File.separator).toString();
        File templateDirectory = new File(templateParentDirectory + properties.get(JaggeryappTemplateDeployerConstants.TEMPLATE_DIRECTORY));
        JaggeryappTemplateDeployerHelper.validateFilePath(properties.get(JaggeryappTemplateDeployerConstants.TEMPLATE_DIRECTORY));

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Registry registry = JaggeryappTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);
            Resource resource;
            if (registry.resourceExists(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                resource = registry.get(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH);
                // If same artifact id exists it is an edit
                if (resource.getProperty(artifactId) != null) {
                    undeployArtifact(artifactId);
                }
                // Throw exception if same jaggeryapp exists
                if (destination.exists()) {
                    throw new JaggeryappTemplateDeployerException("Jaggeryapp with same name " + properties.get(JaggeryappTemplateDeployerConstants.DIRECTORY_NAME) + " already exists");
                }
            } else {
                resource = registry.newResource();
            }
            resource.setProperty(artifactId, properties.get(JaggeryappTemplateDeployerConstants.DIRECTORY_NAME));
            // Save the resource
            registry.put(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH, resource);
        } catch (RegistryException e) {
            throw new JaggeryappTemplateDeployerException("Failed to access resource at: " + JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH + " from registry", e);
        }

        boolean failedToDeploy = false;
        try {
            // Copy all the default templates
            FileUtils.copyDirectory(templateDirectory, destination);
            // Save the artifacts
            for (Map.Entry<String, String> entry : artifacts.entrySet()) {
                String fileName = entry.getKey();
                File targetFile = new File(destination, fileName);
                FileWriter writer = null;
                try {
                    writer = new FileWriter(targetFile);
                    writer.write(entry.getValue());
                } catch (IOException e) {
                    failedToDeploy = true;
                    log.error("Failed to write artifact to: " + targetFile.getAbsolutePath());
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                        } catch (IOException e) {
                            log.warn("Failed to close FileWriter of " + targetFile.getAbsolutePath());
                        }
                    }
                }
            }
        } catch (IOException e) {
            failedToDeploy = true;
            log.error("Failed to copy " + templateDirectory.getAbsolutePath() + " to " + destination.getAbsolutePath());
        }
        if (failedToDeploy) {
            undeployArtifact(artifactId);
            throw new JaggeryappTemplateDeployerException("Failed to deploy jaggerapp " + artifactId);
        } else {
            log.info("Deployed successfully jaggeryapp: " + artifactId);
        }
    }

    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Registry registry = JaggeryappTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);
            if (registry.resourceExists(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                Resource resource = registry.get(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH);
                String directory = resource.getProperty(artifactId);

                if (directory != null) {
                    // Remove the artifact entry from registry
                    resource.removeProperty(artifactId);
                    File destination = new File(JaggeryappTemplateDeployerUtility.getJaggeryappArtifactPath() + directory);
                    try {
                        FileUtils.deleteDirectory(destination);
                    } catch (IOException e) {
                        throw new JaggeryappTemplateDeployerException("Failed to delete directory: " + destination.getAbsolutePath(), e);
                    }
                    registry.put(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH, resource);
                    log.info("jaggeryapp successfully undeployed : " + artifactId);
                } else {
                    log.warn("Artifact: " + artifactId + " does not exist to undeploy");
                }
            } else {
                log.warn("Artifact: " + artifactId + " does not exist to undeploy");
            }
        } catch (RegistryException e) {
            throw new JaggeryappTemplateDeployerException("Failed to access resource at: " + JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH + " from registry", e);
        }
    }

    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template) throws TemplateDeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Registry registry = JaggeryappTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);
            if (registry.resourceExists(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                Resource resource = registry.get(JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH);
                if (resource.getProperty(template.getArtifactId()) == null) {
                    deployArtifact(template);
                }
            } else {
                deployArtifact(template);
            }
        } catch (RegistryException e) {
            throw new JaggeryappTemplateDeployerException("Failed to access resource at: " + JaggeryappTemplateDeployerConstants.META_INFO_COLLECTION_PATH + " from registry", e);
        }
    }
}