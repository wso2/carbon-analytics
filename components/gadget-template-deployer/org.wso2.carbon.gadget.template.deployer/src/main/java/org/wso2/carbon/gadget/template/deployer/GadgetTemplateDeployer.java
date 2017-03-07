/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.gadget.template.deployer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.gadget.template.deployer.internal.GadgetTemplateDeployerConstants;
import org.wso2.carbon.gadget.template.deployer.internal.GadgetTemplateDeployerException;
import org.wso2.carbon.gadget.template.deployer.internal.util.GadgetTemplateDeployerUtility;
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
import java.util.Properties;
import java.util.Set;

public class GadgetTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(GadgetTemplateDeployer.class);

    @Override
    public String getType() {
        return GadgetTemplateDeployerConstants.ARTIFACT_TYPE;
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {

        String artifactId = template.getArtifactId();
        String content = template.getArtifact();

        Map<String, String> artifacts = new HashMap<>();
        Map<String, String> properties = new HashMap<>();

        DocumentBuilderFactory factory = getSecuredDocumentBuilder();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(content)));
            NodeList configNodes = document.getElementsByTagName(GadgetTemplateDeployerConstants.CONFIG_TAG);
            if (configNodes.getLength() > 0) {
                Node configNode = configNodes.item(0);  // Only one node is expected
                if (configNode.hasChildNodes()) {

                    // Extract the details
                    NodeList nodeList = configNode.getChildNodes();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (GadgetTemplateDeployerConstants.PROPERTIES_TAG.equalsIgnoreCase(node.getNodeName()) && node.hasChildNodes()) {
                            // Properties
                            NodeList propertiesNodeList = node.getChildNodes();
                            for (int j = 0; j < propertiesNodeList.getLength(); j++) {
                                Node propertyNode = propertiesNodeList.item(j);
                                if (GadgetTemplateDeployerConstants.PROPERTY_TAG.equalsIgnoreCase(propertyNode.getNodeName())) {
                                    Attr attr = (Attr) propertyNode.getAttributes().getNamedItem(GadgetTemplateDeployerConstants.NAME_ATTRIBUTE);
                                    properties.put(attr.getValue(), propertyNode.getFirstChild().getNodeValue().trim());
                                }
                            }
                        } else if (GadgetTemplateDeployerConstants.ARTIFACTS_TAG.equalsIgnoreCase(node.getNodeName()) && node.hasChildNodes()) {
                            NodeList artifactNodeList = node.getChildNodes();
                            for (int j = 0; j < artifactNodeList.getLength(); j++) {
                                Node artifactNode = artifactNodeList.item(j);
                                if (GadgetTemplateDeployerConstants.ARTIFACT_TAG.equalsIgnoreCase(artifactNode.getNodeName())) {
                                    Attr attr = (Attr) artifactNode.getAttributes().getNamedItem(GadgetTemplateDeployerConstants.FILE_ATTRIBUTE);
                                    artifacts.put(attr.getValue(), artifactNode.getFirstChild().getNodeValue());
                                }
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw new GadgetTemplateDeployerException("Error in creating XML document builder.", e);
        } catch (SAXException e) {
            throw new GadgetTemplateDeployerException("Error in parsing XML content of: " + artifactId, e);
        } catch (IOException e) {
            throw new GadgetTemplateDeployerException("Error in loading XML content of: " + artifactId, e);
        }

        if (!properties.containsKey(GadgetTemplateDeployerConstants.DIRECTORY_NAME)) {
            throw new GadgetTemplateDeployerException("Artifact does not contain " + GadgetTemplateDeployerConstants.DIRECTORY_NAME + " property.");
        }

        String gadgetArtifactPath = GadgetTemplateDeployerUtility.getGadgetArtifactPath();
        File destination = new File(gadgetArtifactPath + properties.get(GadgetTemplateDeployerConstants.DIRECTORY_NAME));
        GadgetTemplateDeployerUtility.validatePath(properties.get(GadgetTemplateDeployerConstants.DIRECTORY_NAME));

        // Store the directory name for the artifact id
        Registry registry = GadgetTemplateDeployerUtility.getRegistry();
        try {
            Resource resource;
            if (registry.resourceExists(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH)) {
                // If same gadgets for same artifact exist, remove them first
                resource = registry.get(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH);

                // Delete this artifact if exists
                if (resource.getProperty(artifactId) != null) {
                    undeployArtifact(artifactId);
                }
            } else {
                resource = registry.newResource();
            }
            resource.setProperty(artifactId, properties.get(GadgetTemplateDeployerConstants.DIRECTORY_NAME));
            // Save the resource
            registry.put(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH, resource);
        } catch (RegistryException e) {
            throw new GadgetTemplateDeployerException("Failed to access resource at: " + GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH + " from registry", e);
        }

        // Copy the static files
        String templateParentDir = new StringBuilder(CarbonUtils.getCarbonConfigDirPath())
                .append(File.separator).append(GadgetTemplateDeployerConstants.TEMPLATE_MANAGER).append(File.separator)
                .append(GadgetTemplateDeployerConstants.GADGET_TEMPLATES).toString();
        File templateDirectory = new File(templateParentDir, properties.get(GadgetTemplateDeployerConstants.TEMPLATE_DIRECTORY));
        GadgetTemplateDeployerUtility.validatePath(properties.get(GadgetTemplateDeployerConstants.TEMPLATE_DIRECTORY));

        // Copy all the default templates
        try {
            FileUtils.copyDirectory(templateDirectory, destination);
        } catch (IOException e) {
            throw new GadgetTemplateDeployerException("Failed to copy " + templateDirectory.getAbsolutePath() + " to " + destination.getAbsolutePath(), e);
        }

        // Save the artifacts
        for (Map.Entry<String, String> entry : artifacts.entrySet()) {
            String fileName = entry.getKey();
            GadgetTemplateDeployerUtility.validatePath(fileName);
            File targetFile = new File(destination, fileName);
            FileWriter writer = null;
            try {
                writer = new FileWriter(targetFile);
                writer.write(entry.getValue());
            } catch (IOException e) {
                throw new GadgetTemplateDeployerException("Failed to write artifact to: " + targetFile.getAbsolutePath(), e);
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
        log.info("Deployed successfully gadget: " + artifactId);
    }

    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template) throws TemplateDeploymentException {

        Registry registry = GadgetTemplateDeployerUtility.getRegistry();

        try {
            if (registry.resourceExists(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH)) {
                // If same gadgets for same artifact exist, remove them first
                Resource resource = registry.get(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH);
                if (resource.getProperty(template.getArtifactId()) == null) {
                    deployArtifact(template);
                }
            } else {
                deployArtifact(template);
            }
        } catch (RegistryException e) {
            throw new GadgetTemplateDeployerException("Failed to access resource at: " + GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH + " from registry", e);
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {

        Registry registry = GadgetTemplateDeployerUtility.getRegistry();
        try {
            if (registry.resourceExists(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH)) {
                Resource resource = registry.get(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH);
                String directory = resource.getProperty(artifactId);

                if (directory != null) {
                    // Remove the artifact entry from registry
                    resource.removeProperty(artifactId);

                    boolean isSharedGadgetDirectory = false;

                    // Check whether other artifacts use the same gadget. If so, don't delete the folder.
                    Properties properties = resource.getProperties();
                    Set<Object> keys = properties.keySet();
                    for (Object key : keys) {
                        String dir = resource.getProperty(key.toString());
                        if (directory.equals(dir)) {
                            // Same gadget is used by other artifacts too
                            isSharedGadgetDirectory = true;
                            break;
                        }
                    }

                    if (!isSharedGadgetDirectory) {
                        String gadgetArtifactPath = GadgetTemplateDeployerUtility.getGadgetArtifactPath();
                        File destination = new File(gadgetArtifactPath + directory);
                        GadgetTemplateDeployerUtility.validatePath(directory);
                        try {
                            FileUtils.deleteDirectory(destination);
                        } catch (IOException e) {
                            throw new GadgetTemplateDeployerException("Failed to delete directory: " + destination.getAbsolutePath(), e);
                        }
                    }

                    registry.put(GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH, resource);

                    log.info("Undeployed successfully gadget: " + artifactId);
                } else {
                    // Does not exist
                    log.warn("Artifact: " + artifactId + " does not exist to undeploy");
                }
            } else {
                // Does not exist
                log.warn("Artifact: " + artifactId + " does not exist to undeploy");
            }
        } catch (RegistryException e) {
            throw new GadgetTemplateDeployerException("Failed to access resource at: " + GadgetTemplateDeployerConstants.ARTIFACT_DIRECTORY_MAPPING_PATH + " from registry", e);
        }
    }

    private static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setXIncludeAware(false);
        dbf.setExpandEntityReferences(false);
        try {
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            dbf.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        org.apache.xerces.util.SecurityManager securityManager = new org.apache.xerces.util.SecurityManager();
        securityManager.setEntityExpansionLimit(GadgetTemplateDeployerConstants.ENTITY_EXPANSION_LIMIT);
        dbf.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);

        return dbf;
    }
}
