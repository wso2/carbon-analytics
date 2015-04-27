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
package org.wso2.carbon.event.input.adapter.mqtt;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.mqtt.internal.util.MQTTAdapterListener;
import org.wso2.carbon.event.input.adapter.mqtt.internal.util.MQTTBrokerConnectionConfiguration;
import org.wso2.carbon.event.input.adapter.mqtt.internal.util.MQTTEventAdapterConstants;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MQTTEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private InputEventAdapterListener eventAdapterListener;
    private final Map<String, String> globalProperties;
    private final String id = UUID.randomUUID().toString();

    public static ConcurrentHashMap<Integer, Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String,
            MQTTAdapterListener>>>> inputEventAdapterListenerMap =
            new ConcurrentHashMap<Integer, Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String,
                    MQTTAdapterListener>>>>();

    public MQTTEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
                            Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdapterListener) throws InputEventAdapterException {
        this.eventAdapterListener = eventAdapterListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        createMQTTAdapterListener(tenantId);
    }

    @Override
    public void disconnect() {

        String topic = eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterListener>>>
                adapterDestinationSubscriptionsMap = inputEventAdapterListenerMap.get(tenantId);
        if (adapterDestinationSubscriptionsMap == null) {
            throw new InputEventAdapterRuntimeException("There is no subscription for " + topic + " for tenant "
                    + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true));
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterListener>> destinationSubscriptionsMap =
                adapterDestinationSubscriptionsMap.get(eventAdapterConfiguration.getName());
        if (destinationSubscriptionsMap == null) {
            throw new InputEventAdapterRuntimeException("There is no subscription for " + topic
                    + " for event adapter " + eventAdapterConfiguration.getName());
        }

        ConcurrentHashMap<String, MQTTAdapterListener> subscriptionsMap = destinationSubscriptionsMap.get(topic);
        if (subscriptionsMap == null) {
            throw new InputEventAdapterRuntimeException("There is no subscription for " + topic);
        }

        MQTTAdapterListener mqttAdapterListener = subscriptionsMap.get(id);
        if (mqttAdapterListener != null) {
            mqttAdapterListener.stopListener(eventAdapterConfiguration.getName());
            subscriptionsMap.remove(id);
        }

    }

    @Override
    public void destroy() {
    }

    public InputEventAdapterListener getEventAdapterListener() {
        return eventAdapterListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MQTTEventAdapter)) return false;

        MQTTEventAdapter that = (MQTTEventAdapter) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private void createMQTTAdapterListener(int tenantId) {

        String topic = eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC);
        Map<String, ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterListener>>> tenantSpecificListenerMap
                = inputEventAdapterListenerMap.get(tenantId);
        if (tenantSpecificListenerMap == null) {
            tenantSpecificListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String,
                    ConcurrentHashMap<String, MQTTAdapterListener>>>();
            inputEventAdapterListenerMap.put(tenantId, tenantSpecificListenerMap);
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterListener>> adapterSpecificListenerMap =
                tenantSpecificListenerMap.get(eventAdapterConfiguration.getName());

        if (adapterSpecificListenerMap == null) {
            adapterSpecificListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, MQTTAdapterListener>>();
            if (null != tenantSpecificListenerMap.put(eventAdapterConfiguration.getName(),
                    adapterSpecificListenerMap)) {
                adapterSpecificListenerMap = tenantSpecificListenerMap.get(eventAdapterConfiguration.getName());
            }
        }

        ConcurrentHashMap<String, MQTTAdapterListener> topicSpecificListenMap = adapterSpecificListenerMap.get(topic);
        if (topicSpecificListenMap == null) {
            topicSpecificListenMap = new ConcurrentHashMap<String, MQTTAdapterListener>();
            if (null != adapterSpecificListenerMap.putIfAbsent(topic, topicSpecificListenMap)) {
                topicSpecificListenMap = adapterSpecificListenerMap.get(topic);
            }
        }

        MQTTBrokerConnectionConfiguration mqttBrokerConnectionConfiguration = new MQTTBrokerConnectionConfiguration(
                eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_URL),
                eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_USERNAME),
                eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_PASSWORD),
                eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_CLEAN_SESSION),
                eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_CONF_KEEP_ALIVE));

        MQTTAdapterListener mqttAdapterListener = new MQTTAdapterListener(mqttBrokerConnectionConfiguration,
                eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_TOPIC),
                eventAdapterConfiguration.getProperties().get(MQTTEventAdapterConstants.ADAPTER_MESSAGE_CLIENTID),
                eventAdapterListener, tenantId);
        topicSpecificListenMap.put(id, mqttAdapterListener);

        mqttAdapterListener.createConnection();

    }

}
