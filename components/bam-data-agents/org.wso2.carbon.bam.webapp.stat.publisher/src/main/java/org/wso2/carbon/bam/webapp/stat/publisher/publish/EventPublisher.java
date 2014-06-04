/*
* Copyright 2004,2013 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.bam.webapp.stat.publisher.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.webapp.stat.publisher.conf.InternalEventingConfigData;
import org.wso2.carbon.bam.webapp.stat.publisher.data.WebappStatEvent;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.StreamDefinition;

import java.util.ArrayList;
import java.util.List;

/*
* Purpose of this class is to publish the event to BAM server.
*/
public class EventPublisher {

    private static Log log = LogFactory.getLog(EventPublisher.class);

    public void publish(WebappStatEvent webappStatEvent, InternalEventingConfigData configData) {
        List<Object> correlationData = webappStatEvent.getCorrelationData();
        List<Object> metaData = webappStatEvent.getMetaData();
        List<Object> payLoadData = webappStatEvent.getEventData();

        String key = null;
        EventPublisherConfig eventPublisherConfig = null;

        StreamDefinition streamDef = null;
        key = configData.getUrl() + "_" + configData.getUserName() + "_" +
                configData.getPassword();
        eventPublisherConfig = WebappAgentUtil.getEventPublisherConfig(key);
        streamDef = configData.getStreamDefinition();

        String streamId = null;

        if (!configData.isLoadBalancingConfig()) {
            try {
                if (eventPublisherConfig == null) {
                    synchronized (EventPublisher.class) {
                        eventPublisherConfig = WebappAgentUtil.getEventPublisherConfig(key);
                        if (null == eventPublisherConfig) {
                            eventPublisherConfig = new EventPublisherConfig();
                            AsyncDataPublisher asyncDataPublisher = new AsyncDataPublisher(configData.getUrl(),
                                    configData.getUserName(),
                                    configData.getPassword());
                            asyncDataPublisher.addStreamDefinition(streamDef);
                            eventPublisherConfig.setDataPublisher(asyncDataPublisher);
                            WebappAgentUtil.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                        }
                    }
                }

                AsyncDataPublisher asyncDataPublisher = eventPublisherConfig.getDataPublisher();

                asyncDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData),
                        getObjectArray(correlationData),
                        getObjectArray(payLoadData));

            } catch (AgentException e) {
                log.error("Error occurred while sending the event", e);
            }
        } else {
            try {
                if (eventPublisherConfig == null) {
                    synchronized (EventPublisher.class) {

                        eventPublisherConfig = WebappAgentUtil.getEventPublisherConfig(key);
                        if (null == eventPublisherConfig) {
                            eventPublisherConfig = new EventPublisherConfig();
                            ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
                            ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(configData.getUrl());

                            for (String aReceiverGroupURL : receiverGroupUrls) {
                                ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
                                String[] urls = aReceiverGroupURL.split(",");
                                for (String aUrl : urls) {
                                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), configData.getUserName(),
                                            configData.getPassword());
                                    dataPublisherHolders.add(aNode);
                                }
                                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                                allReceiverGroups.add(group);
                            }

                            LoadBalancingDataPublisher loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);

                            loadBalancingDataPublisher.addStreamDefinition(streamDef);
                            eventPublisherConfig.setLoadBalancingPublisher(loadBalancingDataPublisher);
                            WebappAgentUtil.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                        }
                    }
                }

                LoadBalancingDataPublisher loadBalancingDataPublisher = eventPublisherConfig.getLoadBalancingDataPublisher();

                loadBalancingDataPublisher.publish(streamDef.getName(), streamDef.getVersion(), getObjectArray(metaData), getObjectArray(correlationData),
                        getObjectArray(payLoadData));

            } catch (AgentException e) {
                log.error("Error occurred while sending the event", e);
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
