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

package org.wso2.carbon.analytics.eventsink.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.EventSinkTemplateDeployerValueHolder;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.util.EventSinkTemplateDeployerConstants;
import org.wso2.carbon.analytics.eventsink.template.deployer.internal.util.EventSinkTemplateDeployerHelper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import java.util.List;

/**
 * Event Sink Template Deployer for Execution Manager.
 */
public class EventSinkTemplateDeployer implements TemplateDeployer {
    private static final Log log = LogFactory.getLog(EventSinkTemplateDeployer.class);

    @Override
    public String getType() {
        return EventSinkTemplateDeployerConstants.EVENT_SINK_DEPLOYER_TYPE;
    }

    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        String artifactId = null;
        Registry registry;
        String domain = null;
        String scenario = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }
            if(template.getConfiguration() != null) {
                domain = template.getConfiguration().getDomain();
                scenario = template.getConfiguration().getScenario();
            }
            artifactId = template.getArtifactId();
            //clean up resources associated to previously deployed artifact
            undeployArtifact(artifactId);
            //updating resources associated to incoming artifact
            AnalyticsEventStore incomingEventStore = EventSinkTemplateDeployerHelper
                    .unmarshallEventSinkConfig(template);
            List<String> incomingStreamIds = incomingEventStore.getEventSource().getStreamIds();
            //all the stream IDs have same name hence we can read stream name from any element; here we read from zeroth element.
            String incomingStreamName = incomingStreamIds.get(0).split(EventStreamConstants.STREAM_DEFINITION_DELIMITER)[0];
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            registry = EventSinkTemplateDeployerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);
            EventSinkTemplateDeployerHelper.updateArtifactAndStreamIdMappings(registry, artifactId, incomingStreamIds);
            EventSinkTemplateDeployerHelper.updateArtifactAndColDefMappings(registry, artifactId,
                                                                            incomingEventStore.getAnalyticsTableSchema().getColumns(), incomingStreamName);
            //deploy with merge
            EventSinkTemplateDeployerValueHolder.getAnalyticsEventSinkService().putEventStoreWithSchemaMerge(tenantId, incomingEventStore);
        }  catch (AnalyticsEventStoreException e) {
            EventSinkTemplateDeployerHelper.cleanRegistryWithUndeploy(artifactId, false);
            throw new TemplateDeploymentException("Failed to deploy eventSink configuration in Domain: "
                                                  + domain + ", Scenario: "
                                                  + scenario + ". Error occurred in the deployment process.", e);
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
        EventSinkTemplateDeployerHelper.cleanRegistryWithUndeploy(artifactId, true);
    }
}
