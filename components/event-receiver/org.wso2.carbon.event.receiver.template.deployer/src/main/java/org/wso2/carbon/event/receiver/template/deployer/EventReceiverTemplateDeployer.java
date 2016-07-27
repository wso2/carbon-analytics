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
package org.wso2.carbon.event.receiver.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.template.deployer.internal.EventReceiverTemplateDeployerValueHolder;
import org.wso2.carbon.event.receiver.template.deployer.internal.util.EventReceiverTemplateDeployerConstants;
import org.wso2.carbon.event.receiver.template.deployer.internal.util.EventReceiverTemplateDeployerHelper;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class EventReceiverTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(EventReceiverTemplateDeployer.class);

    @Override
    public String getType() {
        return EventReceiverTemplateDeployerConstants.EVENT_RECEIVER_DEPLOYER_TYPE;
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

            String receiverConfig = template.getArtifact();

            boolean isReceiverExist = false;
            receiverName = EventReceiverTemplateDeployerValueHolder.getEventReceiverService().getEventReceiverName(receiverConfig);
            for (EventReceiverConfiguration receiverConfiguration :
                    EventReceiverTemplateDeployerValueHolder.getEventReceiverService().getAllActiveEventReceiverConfigurations()) {
                if (receiverName.equals(receiverConfiguration.getEventReceiverName())) {
                    isReceiverExist = true;
                }
            }
            if (!isReceiverExist) {
                for (EventReceiverConfigurationFile receiverConfigurationFile :
                        EventReceiverTemplateDeployerValueHolder.getEventReceiverService().getAllInactiveEventReceiverConfigurations()) {
                    if (receiverName.equals(receiverConfigurationFile.getEventReceiverName())) {
                        isReceiverExist = true;
                    }
                }
            }

            if (!isReceiverExist) {
                deployArtifact(template);
            } else {
                log.info("Common-Artifact Event Receiver with name: " + receiverName + " of Domain " + template.getConfiguration().getDomain()
                         + " was not deployed as it is already being deployed.");
            }
        } catch (EventReceiverConfigurationException e) {
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
        String artifactId = template.getArtifactId();
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = EventReceiverTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            String receiverConfig = template.getArtifact();
            receiverName = EventReceiverTemplateDeployerValueHolder.getEventReceiverService().getEventReceiverName(receiverConfig);

            String mappingResourcePath = EventReceiverTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + receiverName;
            if (registry.resourceExists(mappingResourcePath)) {
                String existingReceiverConfigXml = getExistingEventReceiverConfigXml(tenantId, receiverName);  //not handling existingConfig==null case because that will make this component too complicated.
                if (EventReceiverTemplateDeployerHelper.areReceiverConfigXmlsSimilar(receiverConfig, existingReceiverConfigXml)) {
                    EventReceiverTemplateDeployerHelper.updateRegistryMaps(registry, artifactId, receiverName);
                    log.info("[Artifact ID: " + artifactId + "] Event receiver: " + receiverName + " has already being deployed.");
                } else {
                    throw new TemplateDeploymentException("[Artifact ID: " + artifactId + "] Failed to deploy Event Receiver with name: " + receiverName +
                                                          ", as there exists another Event Receiver with the same name " +
                                                          "but different configuration.");
                }
            } else {
                EventReceiverTemplateDeployerHelper.updateRegistryMaps(registry, artifactId, receiverName);
                EventReceiverTemplateDeployerValueHolder.getEventReceiverService().deployEventReceiverConfiguration(receiverConfig);
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when deploying Event Receiver for artifact ID: " + template.getArtifactId(), e);
        } catch (EventReceiverConfigurationException e) {
            throw new TemplateDeploymentException("Could not deploy Event Receiver with name: " + receiverName + ", for Artifact ID: " + artifactId, e);
        } catch (IOException e) {
            throw new TemplateDeploymentException("Could not deploy Event Receiver with name: " + receiverName + ", for Artifact ID: " + artifactId, e);
        } catch (SAXException e) {
            throw new TemplateDeploymentException("Could not deploy Event Receiver with name: " + receiverName + ", for Artifact ID: " + artifactId, e);
        }
    }

    private void undeployArtifact(String artifactId, boolean doDeleteReceiver)
            throws TemplateDeploymentException {
        String receiverName;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = EventReceiverTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(EventReceiverTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                registry.put(EventReceiverTemplateDeployerConstants.META_INFO_COLLECTION_PATH, registry.newCollection());
            }

            Collection infoCollection = registry.get(EventReceiverTemplateDeployerConstants.META_INFO_COLLECTION_PATH, 0, -1);

            receiverName = infoCollection.getProperty(artifactId);

            if (receiverName != null) {
                infoCollection.removeProperty(artifactId);    //cleaning up the map
                registry.put(EventReceiverTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

                String mappingResourcePath = EventReceiverTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + receiverName;
                if (registry.resourceExists(mappingResourcePath)) {
                    try {
                        Resource mappingResource = registry.get(mappingResourcePath);
                        String mappingResourceContent = new String((byte[]) mappingResource.getContent());

                        //Removing artifact ID, along with separator comma.
                        int beforeCommaIndex = mappingResourceContent.indexOf(artifactId) - 1;
                        int afterCommaIndex = mappingResourceContent.indexOf(artifactId) + artifactId.length();
                        if (beforeCommaIndex > 0) {
                            mappingResourceContent = mappingResourceContent.replace(
                                    EventReceiverTemplateDeployerConstants.META_INFO_RECEIVER_NAME_SEPARATER + artifactId, "");
                        } else if (afterCommaIndex < mappingResourceContent.length()) {
                            mappingResourceContent = mappingResourceContent.replace(
                                    artifactId + EventReceiverTemplateDeployerConstants.META_INFO_RECEIVER_NAME_SEPARATER, "");
                        } else {
                            mappingResourceContent = mappingResourceContent.replace(artifactId, "");
                        }

                        if (mappingResourceContent.equals("")) {
                            //undeploying existing event receiver
                            if (doDeleteReceiver) {
                                boolean isReceiverActive = false;
                                for (EventReceiverConfiguration receiverConfiguration :
                                        EventReceiverTemplateDeployerValueHolder.getEventReceiverService().getAllActiveEventReceiverConfigurations()) {
                                    if (receiverName.equals(receiverConfiguration.getEventReceiverName())) {
                                        isReceiverActive = true;
                                    }
                                }
                                if (isReceiverActive) {
                                    EventReceiverTemplateDeployerValueHolder.getEventReceiverService().undeployActiveEventReceiverConfiguration(receiverName);
                                } else {
                                    EventReceiverTemplateDeployerValueHolder.getEventReceiverService().undeployInactiveEventReceiverConfiguration(receiverName);
                                }
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
                                                              + ", when trying to undeploy Event Receiver with artifact ID: " + artifactId, e);
                    } catch (EventReceiverConfigurationException e) {
                        throw new TemplateDeploymentException("Failed to undeploy Event Receiver: " + receiverName + ", for Artifact ID: " + artifactId, e);
                    }
                }
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when trying to undeploy Event Receiver for artifact ID: " + artifactId, e);
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
