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
import org.wso2.carbon.analytics.eventsink.AnalyticsTableSchema;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.EventSinkTemplateDeployerValueHolder;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.util.EventSinkTemplateDeployerConstants;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.util.EventSinkTemplateDeployerHelper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.ArrayList;
import java.util.List;

public class EventSinkTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(EventSinkTemplateDeployer.class);

    @Override
    public String getType() {
        return EventSinkTemplateDeployerConstants.EVENT_SINK_DEPLOYER_TYPE;
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        String artifactId = null;
        Registry registry = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }

            //~~~~~~~~~~~~~Clean up resources associated to previously deployed artifact

            artifactId = template.getArtifactId();

            undeployArtifact(artifactId);

            //~~~~~~~~~~~~~deploying new event sink

            AnalyticsEventStore incomingEventStore = EventSinkTemplateDeployerHelper
                    .unmarshallEventSinkConfig(template);
            List<String> incomingStreamIds = incomingEventStore.getEventSource().getStreamIds();
            String incomingStreamName = incomingStreamIds.get(0).split(EventStreamConstants.STREAM_DEFINITION_DELIMITER)[0];

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            registry = EventSinkTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            AnalyticsEventStore existingEventStore = EventSinkTemplateDeployerHelper
                    .getExistingEventStore(tenantId, incomingStreamName);

            //check whether the incoming Sink Config will try to unsubscibe any Streams
            if (existingEventStore != null) {
                for (String streamId: existingEventStore.getEventSource().getStreamIds()) {
                    if (!incomingStreamIds.contains(streamId)) {
                        throw new TemplateDeploymentException("Deploying new Event Sink configuration with artifact ID: " + artifactId
                                                              + " will remove persistence configuration of Stream: " + streamId
                                                              + ", hence deployment held back.");
                    }
                }
            }

            EventSinkTemplateDeployerHelper.updateArtifactAndStreamIdMappings(registry, artifactId, incomingStreamIds);

            //Check whether any existing Table Schema which is used by another artifact will get overwritten by this deployment.
            // If so, do not deploy.
            for (AnalyticsTableSchema.Column column: incomingEventStore.getAnalyticsTableSchema().getColumns()) {
                String incomingColKey = EventSinkTemplateDeployerHelper.getKeyForColumn(column, incomingStreamName);
                String incomingColHash = column.getHash();
                if (registry.resourceExists(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_COLUMN_DEF_HASH_RESOURCE_PATH)) {
                    Resource propertyContainer = registry.get(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_COLUMN_DEF_HASH_RESOURCE_PATH);
                    String existingColHash = propertyContainer.getProperty(incomingColKey);
                    if (existingColHash == null) {
                        propertyContainer.addProperty(incomingColKey, incomingColHash);
                        registry.put(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_COLUMN_DEF_HASH_RESOURCE_PATH, propertyContainer);

                        Resource artifactIds = registry.newResource();
                        artifactIds.setMediaType("text/plain");
                        artifactIds.setContent(artifactId);
                        String resourcePath = EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_ARTIFACT_IDS_COLLECTION_PATH
                                              + RegistryConstants.PATH_SEPARATOR + incomingColKey;
                        registry.put(resourcePath, artifactIds);

                        EventSinkTemplateDeployerHelper.addToArtifactIdToColumnDefMap(registry, artifactId, incomingColKey);

                    } else if (incomingColHash.equals(existingColHash)) {
                        String resourcePath = EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_ARTIFACT_IDS_COLLECTION_PATH
                                              + RegistryConstants.PATH_SEPARATOR + incomingColKey;
                        Resource artifactIdResource = registry.get(resourcePath);
                        String artifactIds = new String((byte[]) artifactIdResource.getContent());
                        artifactIds += EventSinkTemplateDeployerConstants.SEPARATOR + artifactId;
                        artifactIdResource.setContent(artifactIds);
                        registry.put(resourcePath, artifactIdResource);

                        EventSinkTemplateDeployerHelper.addToArtifactIdToColumnDefMap(registry, artifactId, incomingColKey);

                    } else {
                        throw new TemplateDeploymentException("Deploying new Event Sink configuration with artifact ID: " + artifactId
                                                              + " will over-write the existing Column with ID: " + incomingColKey
                                                              + ", hence deployment held back.");
                    }
                } else {
                    Resource propertyContainer = registry.newResource();
                    propertyContainer.addProperty(incomingColKey, incomingColHash);
                    registry.put(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_COLUMN_DEF_HASH_RESOURCE_PATH, propertyContainer);

                    Resource artifactIds = registry.newResource();
                    artifactIds.setMediaType("text/plain");
                    artifactIds.setContent(artifactId);
                    String resourcePath = EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_ARTIFACT_IDS_COLLECTION_PATH
                                          + RegistryConstants.PATH_SEPARATOR + incomingColKey;
                    registry.put(resourcePath, artifactIds);

                    EventSinkTemplateDeployerHelper.addToArtifactIdToColumnDefMap(registry, artifactId, incomingColKey);
                }
            }

            //deploy with merge
            EventSinkTemplateDeployerValueHolder.getAnalyticsEventSinkService().putEventStoreWithSchemaMerge(tenantId, incomingEventStore);

        }  catch (AnalyticsEventStoreException e) {
            EventSinkTemplateDeployerHelper.cleanTableSchemaRegistryRecords(registry, artifactId);
            throw new TemplateDeploymentException("Failed to deploy eventSink configuration in Domain: "
                                                  + template.getConfiguration().getDomain() + ", Scenario: "
                                                  + template.getConfiguration().getScenario() + ". Error occurred in the deployment process.", e);
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when deploying Event Sink with Artifact ID: " + artifactId , e);
        }
    }


    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template)
            throws TemplateDeploymentException {
        String artifactId = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            Registry registry = EventSinkTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            artifactId = template.getArtifactId();

            String columnDefKeyResourcePath = EventSinkTemplateDeployerConstants.ARTIFACT_ID_TO_COLUMN_DEF_KEYS_COLLECTION_PATH
                                              + RegistryConstants.PATH_SEPARATOR + artifactId;
            if (!registry.resourceExists(columnDefKeyResourcePath)) {
                deployArtifact(template);
            } else {
                log.info("Event Sink Common Artifact with ID: " + artifactId
                         + " was not deployed as it is already being deployed.");
            }

        }  catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when deploying Event Sink, with Artifact ID: " + artifactId , e);
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        Registry registry = null;
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            registry = EventSinkTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            //removing subscriptions to events, if no other subscriptions available.
            String artifactIdToStreamIdsResourcePath = EventSinkTemplateDeployerConstants.ARTIFACT_ID_TO_STREAM_IDS_COLLECTION_PATH
                                  + RegistryConstants.PATH_SEPARATOR + artifactId;
            if (registry.resourceExists(artifactIdToStreamIdsResourcePath)) {
                Resource streamIdResource = registry.get(artifactIdToStreamIdsResourcePath);
                String streamIds;
                if (streamIdResource.getContent() != null) {
                    streamIds = new String((byte[]) streamIdResource.getContent());
                    String[] streamIdArr = streamIds.split(EventSinkTemplateDeployerConstants.SEPARATOR);
                    List<String> unsubscriptions = new ArrayList<>();
                    for (int i = 0; i < streamIdArr.length; i++) {
                        if (registry.resourceExists(EventSinkTemplateDeployerConstants.STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH
                                                    + RegistryConstants.PATH_SEPARATOR + streamIdArr[i])) {
                            Resource artifactListResource = registry.get(EventSinkTemplateDeployerConstants.STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH
                                                                         + RegistryConstants.PATH_SEPARATOR + streamIdArr[i]);
                            String artifacts = new String((byte[]) artifactListResource.getContent());
                            artifacts = EventSinkTemplateDeployerHelper.removeArtifactIdFromList(artifacts, artifactId);
                            artifactListResource.setContent(artifacts);
                            registry.put(EventSinkTemplateDeployerConstants.STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH
                                         + RegistryConstants.PATH_SEPARATOR + streamIdArr[i], artifactListResource);
                            if (artifacts.isEmpty()) {
                                EventSinkTemplateDeployerValueHolder.getAnalyticsEventSinkService().getAnalyticsEventStreamListener()
                                        .unsubscribeFromStream(tenantId, streamIdArr[i]);
                                unsubscriptions.add(streamIdArr[i]);
                            }
                        }
                    }
                    if (unsubscriptions.size() > 0) {
                        String streamName = streamIdArr[0].split(EventStreamConstants.STREAM_DEFINITION_DELIMITER)[0];
                        AnalyticsEventStore analyticsEventStore = EventSinkTemplateDeployerHelper
                                .getExistingEventStore(tenantId, streamName);
                        for (String streamId: unsubscriptions) {
                            analyticsEventStore.getEventSource().getStreamIds().remove(streamId);
                        }
                        if (analyticsEventStore.getEventSource().getStreamIds().size() > 0) {
                            EventSinkTemplateDeployerValueHolder.getAnalyticsEventSinkService().putEventStore(tenantId, analyticsEventStore);
                        } else {
                            EventSinkTemplateDeployerHelper.deleteEventSinkConfigurationFile(tenantId, streamName);
                        }
                    }
                }
            }

            EventSinkTemplateDeployerHelper.cleanTableSchemaRegistryRecords(registry, artifactId);

        }  catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when undeploying Event Sink with Artifact ID: " + artifactId , e);
        } catch (AnalyticsEventStoreException e) {
            throw new TemplateDeploymentException("Could not save the merged Analytic Event Store " +
                                                  "when undeploying Event Sink with Artifact ID: " + artifactId);
        }
    }
}
