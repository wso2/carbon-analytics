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
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfigurationFile;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.template.deployer.internal.EventPublisherTemplateDeployerValueHolder;
import org.wso2.carbon.event.publisher.template.deployer.internal.util.EventPublisherTemplateDeployerConstants;
import org.wso2.carbon.event.publisher.template.deployer.internal.util.EventPublisherTemplateDeployerHelper;
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

public class EventPublisherTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(EventPublisherTemplateDeployer.class);

    @Override
    public String getType() {
        return EventPublisherTemplateDeployerConstants.EVENT_PUBLISHER_DEPLOYER_TYPE;
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
        String publisherName = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }

            String publisherConfig = template.getArtifact();
            publisherName = EventPublisherTemplateDeployerValueHolder.getEventPublisherService().getEventPublisherName(publisherConfig);
            boolean isPublisherExist = false;
            for (EventPublisherConfiguration publisherConfiguration :
                    EventPublisherTemplateDeployerValueHolder.getEventPublisherService().getAllActiveEventPublisherConfigurations()) {
                if (publisherName.equals(publisherConfiguration.getEventPublisherName())) {
                    isPublisherExist = true;
                }
            }
            if (!isPublisherExist) {
                for (EventPublisherConfigurationFile publisherConfigurationFile :
                        EventPublisherTemplateDeployerValueHolder.getEventPublisherService().getAllInactiveEventPublisherConfigurations()) {
                    if (publisherName.equals(publisherConfigurationFile.getEventPublisherName())) {
                        isPublisherExist = true;
                    }
                }
            }

            if (!isPublisherExist) {
                deployArtifact(template);
            } else {
                log.info("Common-Artifact Event Publisher with name: " + publisherName + " of Domain " + template.getConfiguration().getDomain()
                         + " was not deployed as it is already being deployed.");
            }
        } catch (EventPublisherConfigurationException e) {
            throw new TemplateDeploymentException("Could not deploy Common-Artifact Event Publisher with name: "
                                                  + publisherName + ", for Artifact ID: " + template.getArtifactId(), e);
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        undeployArtifact(artifactId, true);
    }


    private void deployNewArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        String publisherName = null;
        String artifactId = template.getArtifactId();
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = EventPublisherTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            String publisherConfig = template.getArtifact();
            publisherName = EventPublisherTemplateDeployerValueHolder.getEventPublisherService().getEventPublisherName(publisherConfig);

            String mappingResourcePath = EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + publisherName;
            if (registry.resourceExists(mappingResourcePath)) {
                String existingPublisherConfigXml = getExistingEventPublisherConfigXml(tenantId, publisherName);  //not handling existingConfig==null case because handling such user errors will make this component too complicated.
                if (EventPublisherTemplateDeployerHelper.arePublisherConfigXmlsSimilar(publisherConfig, existingPublisherConfigXml)) {
                    EventPublisherTemplateDeployerHelper.updateRegistryMaps(registry, artifactId, publisherName);
                    log.info("[Artifact ID: " + artifactId + "] Event Publisher: " + publisherName + " has already being deployed.");
                } else {
                    throw new TemplateDeploymentException("[Artifact ID: " + artifactId + "] Failed to deploy Event Publisher with name: " + publisherName +
                                                          ", as there exists another Event Publisher with the same name " +
                                                          "but different configuration.");
                }
            } else {
                EventPublisherTemplateDeployerHelper.updateRegistryMaps(registry, artifactId, publisherName);
                EventPublisherTemplateDeployerValueHolder.getEventPublisherService().deployEventPublisherConfiguration(publisherConfig);
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when deploying Event Publisher for artifact ID: " + template.getArtifactId(), e);
        } catch (EventPublisherConfigurationException e) {
            throw new TemplateDeploymentException("Could not deploy Event Publisher with name: " + publisherName + ", for Artifact ID: " + artifactId, e);
        } catch (IOException e) {
            throw new TemplateDeploymentException("Could not deploy Event Publisher with name: " + publisherName + ", for Artifact ID: " + artifactId, e);
        } catch (SAXException e) {
            throw new TemplateDeploymentException("Could not deploy Event Publisher with name: " + publisherName + ", for Artifact ID: " + artifactId, e);
        }
    }

    private void undeployArtifact(String artifactId, boolean doDeletePublisher)
            throws TemplateDeploymentException {
        String publisherName;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = EventPublisherTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                registry.put(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH, registry.newCollection());
            }

            Collection infoCollection = registry.get(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH, 0, -1);

            publisherName = infoCollection.getProperty(artifactId);

            if (publisherName != null) {
                infoCollection.removeProperty(artifactId);    //cleaning up the map
                registry.put(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

                String mappingResourcePath = EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + publisherName;
                if (registry.resourceExists(mappingResourcePath)) {
                    try {
                        Resource mappingResource = registry.get(mappingResourcePath);
                        String mappingResourceContent = new String((byte[]) mappingResource.getContent());

                        //Removing artifact ID, along with separator comma.
                        int beforeCommaIndex = mappingResourceContent.indexOf(artifactId) - 1;
                        int afterCommaIndex = mappingResourceContent.indexOf(artifactId) + artifactId.length();
                        if (beforeCommaIndex > 0) {
                            mappingResourceContent = mappingResourceContent.replace(
                                    EventPublisherTemplateDeployerConstants.META_INFO_PUBLISHER_NAME_SEPARATER + artifactId, "");
                        } else if (afterCommaIndex < mappingResourceContent.length()) {
                            mappingResourceContent = mappingResourceContent.replace(
                                    artifactId + EventPublisherTemplateDeployerConstants.META_INFO_PUBLISHER_NAME_SEPARATER, "");
                        } else {
                            mappingResourceContent = mappingResourceContent.replace(artifactId, "");
                        }

                        if (mappingResourceContent.equals("")) {
                            //undeploying existing event publisher
                            if (doDeletePublisher) {
                                boolean isPublisherActive = false;
                                for (EventPublisherConfiguration publisherConfiguration :
                                        EventPublisherTemplateDeployerValueHolder.getEventPublisherService().getAllActiveEventPublisherConfigurations()) {
                                    if (publisherName.equals(publisherConfiguration.getEventPublisherName())) {
                                        isPublisherActive = true;
                                    }
                                }
                                if (isPublisherActive) {
                                    EventPublisherTemplateDeployerValueHolder.getEventPublisherService().undeployActiveEventPublisherConfiguration(publisherName);
                                } else {
                                    EventPublisherTemplateDeployerValueHolder.getEventPublisherService().undeployInactiveEventPublisherConfiguration(publisherName);
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
                                                              + ", when trying to undeploy Event Publisher with artifact ID: " + artifactId, e);
                    } catch (EventPublisherConfigurationException e) {
                        throw new TemplateDeploymentException("Failed to undeploy Event Publisher: " + publisherName + ", for Artifact ID: " + artifactId, e);
                    }
                }
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when trying to undeploy Event Publisher for artifact ID: " + artifactId, e);
        }
    }

    public static String getExistingEventPublisherConfigXml(int tenantId, String receiverName)
            throws IOException {
        String filePath = MultitenantUtils.getAxis2RepositoryPath(tenantId) +
                          EventPublisherConstants.EF_CONFIG_DIRECTORY + File.separator + receiverName +
                          EventPublisherConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
        File file = new File(filePath);
        if (file.exists()) {
            byte[] encoded = Files.readAllBytes(Paths.get(filePath));
            return new String(encoded);
        } else {
            return null;
        }
    }
}
