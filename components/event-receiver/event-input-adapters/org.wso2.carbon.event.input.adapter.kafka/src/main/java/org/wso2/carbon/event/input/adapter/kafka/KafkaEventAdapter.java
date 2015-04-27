/*
 * Copyright (c) 2005 - 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.wso2.carbon.event.input.adapter.kafka;

import kafka.consumer.ConsumerConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.kafka.internal.util.KafkaEventAdapterConstants;

import java.util.*;
import java.util.concurrent.*;

public final class KafkaEventAdapter implements InputEventAdapter {

    private static final Log log = LogFactory.getLog(KafkaEventAdapter.class);
    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdaptorListener;
    private final String id = UUID.randomUUID().toString();
    private boolean readyToPoll = true;
    ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConsumerKafkaAdaptor>> consumerAdaptorMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConsumerKafkaAdaptor>>();


    public static ExecutorService executorService = new ThreadPoolExecutor(KafkaEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE,
            KafkaEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE, KafkaEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS,
            new LinkedBlockingQueue<Runnable>(KafkaEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE));

    public KafkaEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        this.eventAdaptorListener = eventAdaptorListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        String topic = eventAdapterConfiguration.getProperties().get(KafkaEventAdapterConstants.ADAPTER_MESSAGE_TOPIC);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(true);
        String subscriptionId = UUID.randomUUID().toString();
        if (!readyToPoll) {
           readyToPoll = true;
           log.debug("Adapter " + eventAdapterConfiguration.getName() + " readyToPoll " + topic );
        } else {
            createKafkaAdaptorListener(eventAdaptorListener, eventAdapterConfiguration, subscriptionId, tenantId);
        }
    }

    @Override
    public void disconnect() {
        String topic = eventAdapterConfiguration.getProperties().get(KafkaEventAdapterConstants.ADAPTER_MESSAGE_TOPIC);
        if (consumerAdaptorMap != null) {
            consumerAdaptorMap = null;
            readyToPoll = false;
            log.debug("Adapter " + eventAdapterConfiguration.getName() + " disconnected " + topic);
        }
    }

    @Override
    public void destroy() {
    }

    public InputEventAdapterListener getEventAdaptorListener() {
        return eventAdaptorListener;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private static ConsumerConfig createConsumerConfig(String zookeeper, String groupId,
                                                       String optionalConfigs) {
        Properties props = new Properties();
        props.put(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT, zookeeper);
        props.put(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_GROUP_ID, groupId);

        if (optionalConfigs != null) {
            String[] optionalProperties = optionalConfigs.split(",");

            if (optionalProperties != null && optionalProperties.length > 0) {
                for (String header : optionalProperties) {
                    String[] configPropertyWithValue = header.split(":");
                    if (configPropertyWithValue.length == 2) {
                        props.put(configPropertyWithValue[0], configPropertyWithValue[1]);
                    } else {
                        log.warn("Optional configuration property not defined in the correct format.\nRequired - property_name1:property_value1,property_name2:property_value2\nFound - " + optionalConfigs);
                    }
                }
            }
        }
        return new ConsumerConfig(props);
    }


    private void createKafkaAdaptorListener(
            InputEventAdapterListener inputEventAdapterListener,
            InputEventAdapterConfiguration inputEventAdapterConfiguration,
            String subscriptionId, int tenantId) {

        Map<String, String> brokerProperties = new HashMap<String, String>();
        brokerProperties.putAll(inputEventAdapterConfiguration.getProperties());
        String zkConnect = brokerProperties.get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_ZOOKEEPER_CONNECT);
        String groupID = brokerProperties.get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_GROUP_ID);
        String threadsStr = brokerProperties.get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_THREADS);
        String optionalConfiguration = brokerProperties.get(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        int threads = Integer.parseInt(threadsStr);

        String topic = inputEventAdapterConfiguration.getProperties().get(KafkaEventAdapterConstants.ADAPTOR_SUSCRIBER_TOPIC);

        ConsumerKafkaAdaptor consumerAdaptor = new ConsumerKafkaAdaptor(topic, tenantId,
                KafkaEventAdapter.createConsumerConfig(zkConnect, groupID, optionalConfiguration));
        ConcurrentHashMap<String, ConsumerKafkaAdaptor> tenantSpecificConsumerMap = consumerAdaptorMap.get(tenantId);
        if (tenantSpecificConsumerMap == null) {
            tenantSpecificConsumerMap = new ConcurrentHashMap<String, ConsumerKafkaAdaptor>();
            consumerAdaptorMap.put(tenantId, tenantSpecificConsumerMap);
        }
        tenantSpecificConsumerMap.put(subscriptionId, consumerAdaptor);
        consumerAdaptor.run(threads, inputEventAdapterListener);
    }
}