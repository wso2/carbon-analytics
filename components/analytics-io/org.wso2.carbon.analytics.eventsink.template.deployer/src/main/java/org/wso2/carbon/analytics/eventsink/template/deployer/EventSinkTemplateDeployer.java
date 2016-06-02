/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.eventsink.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.EventSinkTemplateDeployerValueHolder;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.util.EventSinkTemplateDeployerConstants;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.util.EventSinkTemplateDeployerHelper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.List;

public class EventSinkTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(EventSinkTemplateDeployer.class);

    @Override
    public String getType() {
        return "eventsink";
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        List<String> streamIds = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }

            AnalyticsEventStore analyticsEventStore = EventSinkTemplateDeployerHelper
                    .unmarshallEventSinkConfig(template);

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            EventSinkTemplateDeployerValueHolder.getAnalyticsEventSinkService().putEventStore(tenantId, analyticsEventStore);

            String streamName = EventSinkTemplateDeployerHelper.getEventStreamName(analyticsEventStore, template);

            Registry registry = EventSinkTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                registry.put(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, registry.newCollection());
            }

            Resource mappingResource;
            String mappingResourceContent = null;
            String mappingResourcePath = EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + streamName;

            if (registry.resourceExists(mappingResourcePath)) {
                mappingResource = registry.get(mappingResourcePath);
                mappingResourceContent = new String((byte[])mappingResource.getContent());
            } else {
                mappingResource = registry.newResource();
            }

            if (mappingResourceContent == null) {
                mappingResourceContent = template.getArtifactId();
            } else {
                mappingResourceContent += EventSinkTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER
                                          + template.getArtifactId();
            }

            mappingResource.setMediaType("text/plain");   //todo: no constant exist for this in RegistryConstants
            mappingResource.setContent(mappingResourceContent);
            registry.put(mappingResourcePath, mappingResource);


        }  catch (AnalyticsEventStoreException e) {
            throw new TemplateDeploymentException("Failed to deploy eventSink configuration in Domain: "
                                                  + template.getConfiguration().getDomain() + ", Scenario: "
                                                  + template.getConfiguration().getScenario() + ". Error occurred in the deployment process.", e);
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when deploying Event Sink for Stream: " + streamIds.get(0) , e);
        }

    }


    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template)
            throws TemplateDeploymentException {
        List<String> streamIds = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }

            AnalyticsEventStore analyticsEventStore = EventSinkTemplateDeployerHelper
                    .unmarshallEventSinkConfig(template);

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            Registry registry = ExecutionManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            String streamName = EventSinkTemplateDeployerHelper.getEventStreamName(analyticsEventStore, template);
            String mappingResourcePath = EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR
                                         + streamName;
            if (!registry.resourceExists(mappingResourcePath)) {
                deployArtifact(template);
            } else {
                log.info("Event Sink Common Artifact with Stream name: " + streamName + " of Domain " + template.getConfiguration().getDomain()
                          + " was not deployed as it is already being deployed.");
            }

        }  catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when deploying Event Sink for Stream: " + streamIds.get(0) , e);
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            Registry registry = EventSinkTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (registry.resourceExists(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                String[] mappingResourcePaths = registry.get(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH,0,-1).getChildren();
                boolean hasMapping = false;

                for (int i = 0; i<mappingResourcePaths.length; i++) {
                    Resource mappingResource = registry.get(mappingResourcePaths[i]);
                    String mappingResourceContent = new String((byte[])mappingResource.getContent());

                    if (mappingResourceContent.contains(artifactId)) {

                        //Removing artifact ID, along with separator comma.
                        int beforeCommaIndex = mappingResourceContent.indexOf(artifactId) - 1;
                        int afterCommaIndex = mappingResourceContent.indexOf(artifactId) + artifactId.length();
                        if (beforeCommaIndex != 0) {
                            mappingResourceContent = mappingResourceContent.replace(
                                    EventSinkTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER + artifactId, "");
                        } else if (afterCommaIndex != mappingResourceContent.length() - 1) {
                            mappingResourceContent = mappingResourceContent.replace(
                                    artifactId + EventSinkTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER, "");
                        } else {
                            mappingResourceContent = mappingResourceContent.replace(artifactId, "");
                        }

                        //if no other artifact IDs are mapped to this stream name, we can delete the Event Sink Config file.
                        if (mappingResourceContent.equals("")) {
                            String[] pathChunks = mappingResourcePaths[i].split(RegistryConstants.PATH_SEPARATOR);
                            String streamName = pathChunks[pathChunks.length -1];
                            EventSinkTemplateDeployerHelper.deleteEventSinkConfigurationFile(tenantId, streamName);
                            registry.delete(mappingResourcePaths[i]);
                        } else {
                            mappingResource.setContent(mappingResourceContent);
                            registry.put(mappingResourcePaths[i], mappingResource);
                        }
                        hasMapping = true;
                        break;
                    }
                }
                if (!hasMapping) {
                    log.warn("No collection exist at registry path: " + EventSinkTemplateDeployerConstants
                            .META_INFO_COLLECTION_PATH + ". Nothing to undeploy.");
                }
            } else {
                log.warn("No mapping exist for Event Sink Artifact ID: " + artifactId + ". Nothing to undeploy.");
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when undeploying Event Sink Template with ID: " + artifactId , e);
        }

    }

}
