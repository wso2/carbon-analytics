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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.execution.manager.core.internal.ds.ExecutionManagerValueHolder;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
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

            String eventSinkConfigXml = template.getArtifact();
            if (eventSinkConfigXml == null || eventSinkConfigXml.isEmpty()) {
                throw new TemplateDeploymentException("EventSink configuration in Domain: " + template.getConfiguration().getDomain()
                                                      + ", Scenario: " +template.getConfiguration().getScenario() + "is empty or not available.");
            }

            JAXBContext context = JAXBContext.newInstance(AnalyticsEventStore.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(eventSinkConfigXml);
            AnalyticsEventStore analyticsEventStore = (AnalyticsEventStore) unmarshaller.unmarshal(reader);

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            EventSinkTemplateDeployerValueHolder.getAnalyticsEventSinkService().putEventStore(tenantId, analyticsEventStore);

            //template-validation to avoid NPE
            if (analyticsEventStore.getEventSource() == null) {
                throw new TemplateDeploymentException("Invalid EventSink configuration. No EventSource information given in EventSink Configuration. "
                                                      + "For Domain: " + template.getConfiguration().getDomain() + ", for Scenario: "
                                                      + template.getConfiguration().getScenario());
            }
            streamIds = analyticsEventStore.getEventSource().getStreamIds();
            if (streamIds == null || streamIds.isEmpty()) {
                throw new TemplateDeploymentException("Invalid EventSink configuration. No EventSource information given in EventSink Configuration. "
                                                      + "For Domain: " + template.getConfiguration().getDomain() + ", for Scenario: "
                                                      + template.getConfiguration().getScenario());
            }
            //In a valid configuration, all the stream Id's have the same stream name. Here we get the stream name from zero'th element.
            String[] streamIdComponents = streamIds.get(0).split(EventStreamConstants.STREAM_DEFINITION_DELIMITER);
            if (streamIdComponents.length != 2) {
                throw new TemplateDeploymentException("Invalid Stream Id: " + streamIds.get(0) + " found in Event Sink Configuration.");
            }
            String streamName = streamIdComponents[0];

            Registry registry = ExecutionManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH)) {
                registry.put(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, registry.newCollection());
            }

            Resource mappingResource = null;
            String mappingResourceContent = null;
            String mappingResourcePath = EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + streamName;

            if (registry.resourceExists(mappingResourcePath)) {
                mappingResource = registry.get(mappingResourcePath);
                mappingResourceContent = mappingResource.getContent().toString();//todo: test
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


        } catch (JAXBException e) {
            throw new TemplateDeploymentException("Failed to deploy eventSink configuration in Domain: "
                                                  + template.getConfiguration().getDomain() + ", Scenario: "
                                                  + template.getConfiguration().getScenario() + ". Could not unmarshall Event Sink configuration.", e);
        } catch (AnalyticsEventStoreException e) {
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

            String eventSinkConfigXml = template.getArtifact();
            if (eventSinkConfigXml == null || eventSinkConfigXml.isEmpty()) {
                throw new TemplateDeploymentException("EventSink configuration in Domain: " + template.getConfiguration().getDomain()
                                                      + ", Scenario: " +template.getConfiguration().getScenario() + "is empty or not available.");
            }

            JAXBContext context = JAXBContext.newInstance(AnalyticsEventStore.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(eventSinkConfigXml);
            AnalyticsEventStore analyticsEventStore = (AnalyticsEventStore) unmarshaller.unmarshal(reader);

            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            EventSinkTemplateDeployerValueHolder.getAnalyticsEventSinkService().putEventStore(tenantId, analyticsEventStore);

            //template-validation to avoid NPE
            if (analyticsEventStore.getEventSource() == null) {
                throw new TemplateDeploymentException("Invalid EventSink configuration. No EventSource information given in EventSink Configuration. "
                                                      + "For Domain: " + template.getConfiguration().getDomain() + ", for Scenario: "
                                                      + template.getConfiguration().getScenario());
            }
            streamIds = analyticsEventStore.getEventSource().getStreamIds();
            if (streamIds == null || streamIds.isEmpty()) {
                throw new TemplateDeploymentException("Invalid EventSink configuration. No EventSource information given in EventSink Configuration. "
                                                      + "For Domain: " + template.getConfiguration().getDomain() + ", for Scenario: "
                                                      + template.getConfiguration().getScenario());
            }
            //In a valid configuration, all the stream Id's have the same stream name. Here we get the stream name from zero'th element.
            String[] streamIdComponents = streamIds.get(0).split(EventStreamConstants.STREAM_DEFINITION_DELIMITER);
            if (streamIdComponents.length != 2) {
                throw new TemplateDeploymentException("Invalid Stream Id: " + streamIds.get(0) + " found in Event Sink Configuration.");
            }
            String streamName = streamIdComponents[0];

            Registry registry = ExecutionManagerValueHolder.getRegistryService()
                    .getConfigSystemRegistry(tenantId);

            if (!registry.resourceExists(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + streamName)) {
                deployArtifact(template);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("Event Sink Common Artifact with Stream name: " + streamName + " of Domain " + template.getConfiguration().getDomain()
                              + " was not deployed as it is already being deployed.");
                }
            }

        } catch (JAXBException e) {
            throw new TemplateDeploymentException("Failed to deploy eventSink configuration in Domain: "
                                                  + template.getConfiguration().getDomain() + ", Scenario: "
                                                  + template.getConfiguration().getScenario() + ". Could not unmarshall Event Sink configuration.", e);
        } catch (AnalyticsEventStoreException e) {
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
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        //todo: does nothing for now

    }

}
