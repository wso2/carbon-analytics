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
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.List;

public class EventSinkTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(EventSinkTemplateDeployer.class);

    @Override
    public String getType() {
        return EventSinkTemplateDeployerConstants.EVENT_SINK_DEPLOYER_TYPE;
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        List<String> streamIds = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = EventSinkTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                registry.put(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, registry.newCollection());
            }

            Collection infoCollection = registry.get(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, 0, -1);

            String artifactId = template.getArtifactId();
            String streamName = infoCollection.getProperty(artifactId);

            //~~~~~~~~~~~~~Cleaning up previously deployed event store, if any.

            if (streamName != null) {    //meaning, this particular template element has previously deployed an event sink. We need to undeploy it if it has no other users.
                infoCollection.removeProperty(artifactId);    //cleaning up the map before undeploying
                registry.put(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

                //Checking whether any other scenario configs/domains are using this event sink....
                //this info is being kept in a map
                String mappingResourcePath = EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + streamName;
                if (registry.resourceExists(mappingResourcePath)) {
                    EventSinkTemplateDeployerHelper.cleanMappingResourceAndUndeploy(tenantId, registry, mappingResourcePath, artifactId, streamName);
                }
            }

            //~~~~~~~~~~~~~Deploying new event sink

            AnalyticsEventStore analyticsEventStore = EventSinkTemplateDeployerHelper
                    .unmarshallEventSinkConfig(template);
            analyticsEventStore.setMergeSchema(true);

            streamName = EventSinkTemplateDeployerHelper.getEventStreamName(analyticsEventStore, template);

            AnalyticsEventStore existingEventStore = EventSinkTemplateDeployerHelper.getExistingEventStore(tenantId, streamName);

            analyticsEventStore = EventSinkTemplateDeployerHelper.mergeWithExistingEventStore(existingEventStore, analyticsEventStore);

            EventSinkTemplateDeployerValueHolder.getAnalyticsEventSinkService().putEventStore(tenantId, analyticsEventStore);

            EventSinkTemplateDeployerHelper.updateRegistryMaps(registry, infoCollection, artifactId, streamName);

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
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            Registry registry = EventSinkTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                registry.put(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, registry.newCollection());
            }

            Collection infoCollection = registry.get(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, 0, -1);

            String streamName = infoCollection.getProperty(artifactId);

            if (streamName != null) {
                infoCollection.removeProperty(artifactId);    //cleaning up the map before undeploying
                registry.put(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

                //Checking whether any other scenario configs/domains are using this event sink....
                //this info is being kept in a map
                String mappingResourcePath = EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + streamName;
                if (registry.resourceExists(mappingResourcePath)) {
                    EventSinkTemplateDeployerHelper.cleanMappingResourceAndUndeploy(tenantId, registry, mappingResourcePath, artifactId, streamName);
                } else {
                    log.warn("Registry data in inconsistent. Resource '" + mappingResourcePath + "' which needs to be deleted is not found.");
                }
            } else {
                log.warn("Registry data in inconsistent. No Stream name associated to artifact ID: " + artifactId + ". Hence nothing to be undeployed.");
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when deploying Event Sink for Artifact ID: " + artifactId , e);
        }

    }

}
