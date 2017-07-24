/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.message.tracer.internal.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.message.tracer.data.EventStreamDef;
import org.wso2.carbon.analytics.message.tracer.data.Message;
import org.wso2.carbon.analytics.message.tracer.data.ServerConfig;
import org.wso2.carbon.analytics.message.tracer.internal.conf.EventPublisherConfig;
import org.wso2.carbon.analytics.message.tracer.internal.utils.EventConfigUtil;
import org.wso2.carbon.analytics.message.tracer.internal.utils.EventPublishConfigHolder;
import org.wso2.carbon.analytics.message.tracer.internal.utils.StreamDefUtil;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventPublisher {

    public static final String UNDERSCORE = "_";

    private static Log log = LogFactory.getLog(EventPublisher.class);

    private ServerConfig serverConfig;

    public EventPublisher(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public void publish(Message message) {

        List<Object> correlationData = EventConfigUtil.getCorrelationData(message);
        List<Object> metaData = EventConfigUtil.getMetaData(message);
        List<Object> payLoadData = EventConfigUtil.getEventData(message);
        Map<String, String> arbitraryDataMap = EventConfigUtil.getArbitraryDataMap(message);

        StreamDefinition streamDef = StreamDefUtil.getStreamDefinition(new EventStreamDef());

        if (streamDef != null) {

            String key = serverConfig.getUrl() + UNDERSCORE + serverConfig.getUsername() + UNDERSCORE + serverConfig.getPassword();
            EventPublisherConfig eventPublisherConfig = EventPublishConfigHolder.getEventPublisherConfig(key);
            if (serverConfig.isLoadBalancingConfig()) {
                if (log.isDebugEnabled()) {
                    log.debug("Load balancing receiver mode working.");
                }
                try {
                    if (eventPublisherConfig == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Newly creating publisher configuration.");
                        }
                        synchronized (EventPublisher.class) {
                            eventPublisherConfig = new EventPublisherConfig();
                            ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
                            ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(serverConfig.getUrl());

                            for (String aReceiverGroupURL : receiverGroupUrls) {
                                ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                                String[] urls = aReceiverGroupURL.split(ServerConfig.URL_SEPARATOR);
                                for (String aUrl : urls) {
                                    if (log.isDebugEnabled()) {
                                        log.debug("Adding node: " + aUrl);
                                    }
                                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), serverConfig.getUsername(),
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
                                                       getObjectArray(payLoadData), arbitraryDataMap);
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
                        synchronized (EventPublisher.class) {
                            eventPublisherConfig = new EventPublisherConfig();
                            AsyncDataPublisher asyncDataPublisher = new AsyncDataPublisher(serverConfig.getUrl(),
                                                                                           serverConfig.getUsername(),
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
                                               getObjectArray(payLoadData), arbitraryDataMap);
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
}
