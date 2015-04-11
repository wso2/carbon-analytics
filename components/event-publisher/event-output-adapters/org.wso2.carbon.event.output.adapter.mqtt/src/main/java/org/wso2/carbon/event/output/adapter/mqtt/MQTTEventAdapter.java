/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.output.adapter.mqtt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.mqtt.internal.util.MQTTAdapterPublisher;
import org.wso2.carbon.event.output.adapter.mqtt.internal.util.MQTTBrokerConnectionConfiguration;
import org.wso2.carbon.event.output.adapter.mqtt.internal.util.MQTTEventAdapterConstants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MQTTEventAdapter implements OutputEventAdapter {

    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterPublisher>>>
            publisherMap = new ConcurrentHashMap<String,
            ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterPublisher>>>();

    private ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterPublisher>> clientIdSpecificEventSenderMap;

    public MQTTEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }


    @Override
    public void init() throws OutputEventAdapterException {
        //not required
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-available");
    }

    @Override
    public void connect() {
        clientIdSpecificEventSenderMap = publisherMap.get(eventAdapterConfiguration.getName());
        if (null == clientIdSpecificEventSenderMap) {
            clientIdSpecificEventSenderMap =
                    new ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterPublisher>>();
            if (null != publisherMap.putIfAbsent(eventAdapterConfiguration.getName(), clientIdSpecificEventSenderMap)) {
                clientIdSpecificEventSenderMap = publisherMap.get(eventAdapterConfiguration.getName());
            }
        }
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        String clientId = dynamicProperties.get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_CLIENTID);
        ConcurrentHashMap<String, MQTTAdapterPublisher> topicSpecificEventPublisherMap =
                clientIdSpecificEventSenderMap.get(clientId);
        if (null == topicSpecificEventPublisherMap) {
            topicSpecificEventPublisherMap = new ConcurrentHashMap<String, MQTTAdapterPublisher>();
            if (null != clientIdSpecificEventSenderMap.putIfAbsent(clientId, topicSpecificEventPublisherMap)) {
                topicSpecificEventPublisherMap = clientIdSpecificEventSenderMap.get(clientId);
            }
        }

        String topic = dynamicProperties.get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC);
        MQTTAdapterPublisher mqttAdapterPublisher = topicSpecificEventPublisherMap.get(topic);
        if (mqttAdapterPublisher == null) {
            MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration =
                    new MQTTBrokerConnectionConfiguration(eventAdapterConfiguration.getStaticProperties()
                            .get(MQTTEventAdapterConstants.ADAPTER_CONF_URL),
                            eventAdapterConfiguration.getStaticProperties()
                                    .get(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME),
                            eventAdapterConfiguration.getStaticProperties()
                                    .get(MQTTEventAdapterConstants.ADAPTER_CONF_PASSWORD),
                            eventAdapterConfiguration.getStaticProperties()
                                    .get(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION),
                            eventAdapterConfiguration.getStaticProperties()
                                    .get(MQTTEventAdapterConstants.ADAPTER_CONF_KEEP_ALIVE));

            mqttAdapterPublisher = new MQTTAdapterPublisher(mqttBrokerConnectionConfiguration,
                    dynamicProperties.get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC),
                    dynamicProperties.get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_CLIENTID));
            topicSpecificEventPublisherMap.put(topic, mqttAdapterPublisher);
        }
        String qos = eventAdapterConfiguration.getStaticProperties().get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_QOS);

        if (qos == null) {
            mqttAdapterPublisher.publish(message.toString());
        } else {
            mqttAdapterPublisher.publish(Integer.parseInt(qos), message.toString());
        }

    }

    @Override
    public void disconnect() {
        publisherMap.clear();
    }

    @Override
    public void destroy() {
        //not required
    }
}
