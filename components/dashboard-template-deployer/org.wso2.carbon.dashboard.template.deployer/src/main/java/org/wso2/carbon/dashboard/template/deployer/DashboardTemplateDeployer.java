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

package org.wso2.carbon.dashboard.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.dashboard.template.deployer.internal.DashboardTemplateDeployerConstants;
import org.wso2.carbon.dashboard.template.deployer.internal.DashboardTemplateDeployerException;
import org.wso2.carbon.dashboard.template.deployer.internal.util.DashboardTemplateDeployerUtility;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.api.Resource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DashboardTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(DashboardTemplateDeployer.class);

    @Override
    public String getType() {
        return DashboardTemplateDeployerConstants.ARTIFACT_TYPE;
    }

    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {

        String artifactId = template.getArtifactId();
        String content = null;

        Map<String, String> properties = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(template.getArtifact())));
            NodeList configNodes = document.getElementsByTagName(DashboardTemplateDeployerConstants.CONFIG_TAG);
            if (configNodes.getLength() > 0) {
                Node configNode = configNodes.item(0);  // Only one node is expected
                if (configNode.hasChildNodes()) {

                    // Extract the details
                    NodeList nodeList = configNode.getChildNodes();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (DashboardTemplateDeployerConstants.PROPERTIES_TAG.equalsIgnoreCase(node.getNodeName()) && node.hasChildNodes()) {
                            // Properties
                            NodeList propertiesNodeList = node.getChildNodes();
                            for (int j = 0; j < propertiesNodeList.getLength(); j++) {
                                Node propertyNode = propertiesNodeList.item(j);
                                if (DashboardTemplateDeployerConstants.PROPERTY_TAG.equalsIgnoreCase(propertyNode.getNodeName())) {
                                    Attr attr = (Attr) propertyNode.getAttributes().getNamedItem(DashboardTemplateDeployerConstants.NAME_ATTRIBUTE);
                                    properties.put(attr.getValue(), propertyNode.getFirstChild().getNodeValue().trim());
                                }
                            }
                        } else if (DashboardTemplateDeployerConstants.CONTENT_TAG.equalsIgnoreCase(node.getNodeName())) {
                            content = node.getFirstChild().getNodeValue();
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            throw new DashboardTemplateDeployerException("Error in creating XML document builder.", e);
        } catch (SAXException e) {
            throw new DashboardTemplateDeployerException("Error in parsing XML content of: " + artifactId, e);
        } catch (IOException e) {
            throw new DashboardTemplateDeployerException("Error in loading XML content of: " + artifactId, e);
        }

        if (content == null || content.trim().isEmpty()) {
            throw new DashboardTemplateDeployerException("Empty dashboard content for artifact: " + artifactId);
        }

        // Store the directory name for the artifact id
        Registry registry = DashboardTemplateDeployerUtility.getRegistry();
        try {
            Resource resource;
            if (registry.resourceExists(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH)) {
                // If same gadgets for same artifact exist, remove them first
                resource = registry.get(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH);

                // Delete this artifact if exists
                if (resource.getProperty(artifactId) != null) {
                    undeployArtifact(artifactId);
                }
            } else {
                resource = registry.newResource();
            }
            resource.setProperty(artifactId, properties.get(DashboardTemplateDeployerConstants.DASHBOARD_ID));
            // Save the resource
            registry.put(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH, resource);
        } catch (RegistryException e) {
            throw new DashboardTemplateDeployerException("Failed to access resource at: " + DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH + " in registry", e);
        }

        try {
            Resource resource = registry.newResource();
            resource.setContent(content);
            resource.setMediaType("application/json");
            registry.put(DashboardTemplateDeployerConstants.DASHBOARDS_RESOURCE_PATH + properties.get(DashboardTemplateDeployerConstants.DASHBOARD_ID), resource);

            log.info("Dashboard definition of [" + artifactId + "] has been created.");
        } catch (RegistryException e) {
            throw new DashboardTemplateDeployerException("Failed to access resource at: " + DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH + " in registry", e);
        }

    }

    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {

        Registry registry = DashboardTemplateDeployerUtility.getRegistry();
        try {
            if (registry.resourceExists(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH)) {
                Resource resource = registry.get(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH);
                String dashboardId = resource.getProperty(artifactId);

                if (dashboardId != null) {
                    // Remove the artifact entry from registry
                    resource.removeProperty(artifactId);

                    boolean isSharedDashboardId = false;

                    // Check whether other artifacts use the same gadget. If so, don't delete the folder.
                    Properties properties = resource.getProperties();
                    Set<Object> keys = properties.keySet();
                    for (Object key : keys) {
                        String id = resource.getProperty(key.toString());
                        if (dashboardId.equals(id)) {
                            // Same gadget is used by other artifacts too
                            isSharedDashboardId = true;
                            break;
                        }
                    }

                    if (!isSharedDashboardId) {
                        String path = DashboardTemplateDeployerConstants.DASHBOARDS_RESOURCE_PATH + dashboardId;
                        try {
                            if (registry.resourceExists(path)) {
                                registry.delete(path);
                                log.info("Dashboard definition of [" + artifactId + "] has been undeployed.");
                            } else {
                                log.warn("Dashboard definition of [" + artifactId + "] does not exist at " + path);
                            }
                        } catch (RegistryException e) {
                            throw new DashboardTemplateDeployerException(e.getMessage(), e);
                        }
                    }

                    registry.put(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH, resource);

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
            throw new DashboardTemplateDeployerException("Failed to access resource at: " + DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH + " from registry", e);
        }


    }

    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template) throws TemplateDeploymentException {

        Registry registry = DashboardTemplateDeployerUtility.getRegistry();

        try {
            if (registry.resourceExists(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH)) {
                // If same gadgets for same artifact exist, remove them first
                Resource resource = registry.get(DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH);
                if (resource.getProperty(template.getArtifactId()) == null) {
                    deployArtifact(template);
                }
            } else {
                deployArtifact(template);
            }
        } catch (RegistryException e) {
            throw new DashboardTemplateDeployerException("Failed to access resource at: " + DashboardTemplateDeployerConstants.ARTIFACT_DASHBOARD_ID_MAPPING_PATH + " from registry", e);
        }
    }
}
