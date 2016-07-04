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

package org.wso2.carbon.event.publisher.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.publisher.template.deployer.internal.EventPublisherTemplateDeployerValueHolder;
import org.wso2.carbon.event.publisher.template.deployer.internal.EventPublisherTemplateSaveFailedException;
import org.wso2.carbon.event.publisher.template.deployer.internal.util.EventPublisherTemplateDeployerConstants;
import org.wso2.carbon.event.publisher.template.deployer.internal.util.EventPublisherTemplateDeployerHelper;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EventPublisherTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(EventPublisherTemplateDeployer.class);

    @Override
    public String getType() {
        return EventPublisherTemplateDeployerConstants.EVENT_RECEIVER_DEPLOYER_TYPE;
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        String artifactId;
        if (template == null) {
            throw new TemplateDeploymentException("No artifact received to be deployed.");
        }
        artifactId = template.getArtifactId();
        undeployArtifact(artifactId, true);
        deployNewArtifact(template);
    }

    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template)
            throws TemplateDeploymentException {
        String receiverName = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String receiverConfig = template.getArtifact();
            receiverName = EventPublisherTemplateDeployerValueHolder.getEventReceiverService().getEventReceiverName(receiverConfig);
            String existingReceiverConfigXml = getExistingEventReceiverConfigXml(tenantId, receiverName);

            if (existingReceiverConfigXml == null) { //todo: test whether this will be null when the file is not present.
                deployArtifact(template);
            }  else {
                log.info("Common-Artifact Event Receiver with name: " + receiverName + " of Domain " + template.getConfiguration().getDomain()
                         + " was not deployed as it is already being deployed.");
            }
        } catch (EventReceiverConfigurationException e) {
            throw new TemplateDeploymentException("Could not deploy Common-Artifact Event Receiver with name: "
                                                  + receiverName + ", for Artifact ID: " + template.getArtifactId(), e);
        } catch (IOException e) {
            throw new TemplateDeploymentException("Could not deploy Common-Artifact Event Receiver with name: "
                                                  + receiverName + ", for Artifact ID: " + template.getArtifactId(), e);
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        undeployArtifact(artifactId, true);
    }


    private void deployNewArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        String receiverName = null;
        String artifactId = null;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = EventPublisherTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            String receiverConfig = template.getArtifact();
            receiverName = EventPublisherTemplateDeployerValueHolder.getEventReceiverService().getEventReceiverName(receiverConfig);

            String mappingResourcePath = EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + receiverName;
            if (registry.resourceExists(mappingResourcePath)) {
                String existingReceiverConfigXml = getExistingEventReceiverConfigXml(tenantId, receiverName);  //todo: ATM, not handling existingConfig==null case
                if (EventPublisherTemplateDeployerHelper.areReceiverConfigXmlsSimilar(receiverConfig, existingReceiverConfigXml)) {
                    EventPublisherTemplateDeployerHelper.updateRegistryMaps(registry, artifactId, receiverName);
                    log.info("Event receiver: " + receiverName + " has already being deployed for Artifact ID: " + artifactId);
                } else {
                    throw new TemplateDeploymentException("Failed to deploy Event Receiver with name: " + receiverName +
                                                          ", as there exists another Event Receiver with the same name " +
                                                          "but different configuration. Artifact ID: " + artifactId);
                }
            } else {
                EventPublisherTemplateDeployerHelper.updateRegistryMaps(registry, artifactId, receiverName);
                saveEventReceiver(receiverName, receiverConfig, tenantId);
            }
        }  catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when deploying Event Receiver for artifact ID: " + template.getArtifactId(), e);
        } catch (EventReceiverConfigurationException e) {
            throw new TemplateDeploymentException("Could not deploy Event Receiver with name: " + receiverName + ", for Artifact ID: " + artifactId, e);//todo
        } catch (IOException e) {
            throw new TemplateDeploymentException("Could not deploy Event Receiver with name: " + receiverName + ", for Artifact ID: " + artifactId, e);
        } catch (SAXException e) {
            throw new TemplateDeploymentException("Could not deploy Event Receiver with name: " + receiverName + ", for Artifact ID: " + artifactId, e);
        } catch (EventPublisherTemplateSaveFailedException e) {
            undeployArtifact(artifactId, false);
            throw new TemplateDeploymentException("Could not deploy Event Receiver with name: " + receiverName + ", for Artifact ID: " + artifactId, e);
        }
    }

    private void undeployArtifact(String artifactId, boolean doDeleteReceiver)
            throws TemplateDeploymentException {
        String receiverName;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = EventPublisherTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                registry.put(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH, registry.newCollection());
            }

            Collection infoCollection = registry.get(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH, 0, -1);

            receiverName = infoCollection.getProperty(artifactId);

            if (receiverName != null) {
                infoCollection.removeProperty(artifactId);    //cleaning up the map
                registry.put(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

                String mappingResourcePath = EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + receiverName;
                if (registry.resourceExists(mappingResourcePath)) {
                    try {
                        Resource mappingResource = registry.get(mappingResourcePath);
                        String mappingResourceContent = new String((byte[]) mappingResource.getContent());

                        //Removing artifact ID, along with separator comma.
                        int beforeCommaIndex = mappingResourceContent.indexOf(artifactId) - 1;
                        int afterCommaIndex = mappingResourceContent.indexOf(artifactId) + artifactId.length();
                        if (beforeCommaIndex > 0) {
                            mappingResourceContent = mappingResourceContent.replace(
                                    EventPublisherTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER + artifactId, "");
                        } else if (afterCommaIndex < mappingResourceContent.length()) {
                            mappingResourceContent = mappingResourceContent.replace(
                                    artifactId + EventPublisherTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER, "");
                        } else {
                            mappingResourceContent = mappingResourceContent.replace(artifactId, "");
                        }

                        if (mappingResourceContent.equals("")) {
                            //undeploying existing event receiver
                            if (doDeleteReceiver) {
                                EventPublisherTemplateDeployerHelper.deleteEventReceiver(tenantId, artifactId);
                            }
                            //deleting mappingResource
                            registry.delete(mappingResourcePath);
                        } else {
                            mappingResource.setContent(mappingResourceContent);
                            registry.put(mappingResourcePath, mappingResource);
                        }
                    } catch (RegistryException e) {
                        throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                              + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                              + ", when trying to undeploy Event Stream with artifact ID: " + artifactId, e);
                    }
                } else {
                    log.warn("Registry data in inconsistent. Resource '" + mappingResourcePath + "' which needs to be deleted is not found.");
                }
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when trying to undeploy Event Receiver for artifact ID: " + artifactId, e);
        }
    }

    private void saveEventReceiver(String receiverName, String eventReceiverConfigXml, int tenantId)
            throws EventPublisherTemplateSaveFailedException {
        OutputStreamWriter writer = null;
        String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId) +
                          EventReceiverConstants.ER_CONFIG_DIRECTORY + File.separator + receiverName +
                          EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT;
        try {
            /* save contents to .xml file */
            File file = new File(filePath);

            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

            // get the content in bytes
            writer.write(eventReceiverConfigXml);
            log.info("Event Receiver : " + receiverName + " saved in the filesystem");
        } catch (IOException e) {
            throw new EventPublisherTemplateSaveFailedException("Failed to save Event Receiver: " + receiverName, e);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e) {
                    throw new EventPublisherTemplateSaveFailedException("Failed to save Event Receiver: " + receiverName, e);
                }
                try {
                    writer.close();
                } catch (IOException e) {     //todo: review
                    log.warn("Failed to close Output Stream Writer for File: " + receiverName +
                             EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT);
                }
            }
        }
    }

    public static String getExistingEventReceiverConfigXml(int tenantId, String receiverName)
            throws IOException {
        String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId) +
                          EventReceiverConstants.ER_CONFIG_DIRECTORY + File.separator + receiverName +
                          EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT;
        File file = new File(filePath);
        if (file.exists()) {
            byte[] encoded = Files.readAllBytes(Paths.get(filePath));
            return new String(encoded);
        } else {
            return null;
        }
    }
}
