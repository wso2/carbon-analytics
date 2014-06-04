/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.bam.message.tracer.handler.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.message.tracer.handler.conf.EventPublishConfigHolder;
import org.wso2.carbon.bam.message.tracer.handler.conf.EventPublisherConfig;
import org.wso2.carbon.bam.message.tracer.handler.conf.EventingConfigData;
import org.wso2.carbon.bam.message.tracer.handler.data.TracingInfo;
import org.wso2.carbon.bam.message.tracer.handler.stream.StreamDefCreator;
import org.wso2.carbon.bam.message.tracer.handler.util.TenantEventConfigData;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

public class Publisher {

    private static Log log = LogFactory.getLog(Publisher.class);

    public Publisher() {
    }

    public static final String UNDERSCORE = "_";
    public static final String URL_SEPARATOR = ",";


    public void publish(int tenantID, TracingInfo tracingInfo) {

        List<Object> correlationData = getCorrelationData(tracingInfo);
        List<Object> metaData = getMetaData(tracingInfo);
        List<Object> payLoadData = getEventData(tracingInfo);

        StreamDefinition streamDef;
        try {
            streamDef = StreamDefCreator.getStreamDef();
        } catch (MalformedStreamDefinitionException e) {
            log.error("Unable to create stream: " + e.getMessage(), e);
            return;
        }

        if (streamDef != null) {

            EventingConfigData serverConfig = TenantEventConfigData.getTenantSpecificEventingConfigData().get(tenantID);

            String key = serverConfig.getUrl() + UNDERSCORE + serverConfig.getUserName() + UNDERSCORE + serverConfig.getPassword();
            EventPublisherConfig eventPublisherConfig = EventPublishConfigHolder.getEventPublisherConfig(key);
            if (isLoadBalancingConfig(serverConfig.getUrl())) {
                if (log.isDebugEnabled()) {
                    log.debug("Load balancing receiver mode working.");
                }
                try {
                    if (eventPublisherConfig == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Newly creating publisher configuration.");
                        }
                        synchronized (Publisher.class) {
                            eventPublisherConfig = new EventPublisherConfig();
                            ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
                            ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(serverConfig.getUrl());

                            for (String aReceiverGroupURL : receiverGroupUrls) {
                                ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                                String[] urls = aReceiverGroupURL.split(URL_SEPARATOR);
                                for (String aUrl : urls) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Adding node: " + aUrl);
                                    }
                                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), serverConfig.getUserName(),
                                                                                        serverConfig.getPassword());
                                    dataPublisherHolders.add(aNode);
                                }
                                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                                allReceiverGroups.add(group);
                            }

                            LoadBalancingDataPublisher loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);

                            if (log.isDebugEnabled()) {
                                log.debug("Created stream definition.");
                            }
                            loadBalancingDataPublisher.addStreamDefinition(streamDef);
                            eventPublisherConfig.setLoadBalancingDataPublisher(loadBalancingDataPublisher);
                            if (log.isDebugEnabled()) {
                                log.debug("Adding config info to map.");
                            }
                            EventPublishConfigHolder.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                        }
                    }
                    LoadBalancingDataPublisher loadBalancingDataPublisher = eventPublisherConfig.getLoadBalancingDataPublisher();

                    loadBalancingDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData), getObjectArray(correlationData),
                                                       getObjectArray(payLoadData));
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully published data.");
                    }

                } catch (AgentException e) {
                    log.error("Error occurred while sending the event", e);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("single node receiver mode working.");
                }
                try {
                    if (eventPublisherConfig == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Newly creating publisher configuration.");
                        }
                        synchronized (Publisher.class) {
                            eventPublisherConfig = new EventPublisherConfig();
                            AsyncDataPublisher asyncDataPublisher = new AsyncDataPublisher(serverConfig.getUrl(),
                                                                                           serverConfig.getUserName(),
                                                                                           serverConfig.getPassword());
                            if (log.isDebugEnabled()) {
                                log.debug("Created stream definition.");
                            }
                            asyncDataPublisher.addStreamDefinition(streamDef);
                            eventPublisherConfig.setAsyncDataPublisher(asyncDataPublisher);
                            if (log.isDebugEnabled()) {
                                log.debug("Adding config info to map.");
                            }
                            EventPublishConfigHolder.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                        }
                    }

                    AsyncDataPublisher asyncDataPublisher = eventPublisherConfig.getAsyncDataPublisher();

                    asyncDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData),
                                               getObjectArray(correlationData),
                                               getObjectArray(payLoadData));
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully published data.");
                    }

                } catch (AgentException e) {
                    log.error("Error occurred while sending the event", e);
                }
            }
        }
    }

    private Object[] getObjectArray(List<Object> list) {
        if (list.size() > 0) {
            return list.toArray();
        }
        return null;
    }

    public static List<Object> getCorrelationData(TracingInfo tracingInfo) {

        List<Object> correlationData = new ArrayList<Object>(1);
        correlationData.add(tracingInfo.getActivityId());
        return correlationData;
    }

    public static List<Object> getMetaData(TracingInfo tracingInfo) {

        List<Object> metaData = new ArrayList<Object>(7);
        metaData.add(tracingInfo.getRequestUrl());
        metaData.add(tracingInfo.getRemoteAddress());
        metaData.add(tracingInfo.getContentType());
        metaData.add(tracingInfo.getUserAgent());
        metaData.add(tracingInfo.getHost());
        metaData.add(tracingInfo.getReferer());
        metaData.add(tracingInfo.getServer());

        return metaData;
    }

    public static List<Object> getEventData(TracingInfo tracingInfo) {

        List<Object> payloadData = new ArrayList<Object>(7);
        payloadData.add(tracingInfo.getServiceName());
        payloadData.add(tracingInfo.getOperationName());
        payloadData.add(tracingInfo.getMessageDirection());
        payloadData.add(tracingInfo.getPayload());
        payloadData.add(tracingInfo.getHeader());
        payloadData.add(tracingInfo.getTimestamp());
        payloadData.add(tracingInfo.getStatus());

        return payloadData;
    }

    public boolean isLoadBalancingConfig(String url) {
        return (url != null && url.contains(URL_SEPARATOR));
    }
}
