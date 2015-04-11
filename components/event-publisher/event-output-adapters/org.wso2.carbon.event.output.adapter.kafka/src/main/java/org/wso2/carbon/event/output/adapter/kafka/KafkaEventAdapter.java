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
package org.wso2.carbon.event.output.adapter.kafka;

import kafka.admin.AdminUtils;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.I0Itec.zkclient.ZkClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.kafka.internal.util.KafkaEventAdapterConstants;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import kafka.utils.ZKStringSerializer$;

public class KafkaEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(KafkaEventAdapter.class);
    private static ThreadPoolExecutor threadPoolExecutor;
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private ProducerConfig config;
    private Producer<String, Object> producer;

    public KafkaEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                             Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        //ThreadPoolExecutor will be assigned  if it is null
        if (threadPoolExecutor == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(KafkaEventAdapterConstants.MIN_THREAD_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(KafkaEventAdapterConstants.MIN_THREAD_NAME));
            } else {
                minThread = KafkaEventAdapterConstants.MIN_THREAD;
            }

            if (globalProperties.get(KafkaEventAdapterConstants.MAX_THREAD_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(KafkaEventAdapterConstants.MAX_THREAD_NAME));
            } else {
                maxThread = KafkaEventAdapterConstants.MAX_THREAD;
            }

            if (globalProperties.get(KafkaEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        KafkaEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = KafkaEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME;
            }

            threadPoolExecutor = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime,
                    TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));
        }

    }

    @Override
    public void testConnect() {
        connect();
    }

    @Override
    public void connect() {
        Map<String, String> staticProperties = eventAdapterConfiguration.getStaticProperties();
        String kafkaConnect = staticProperties.get(KafkaEventAdapterConstants.ADAPTOR_META_BROKER_LIST);
        log.info(kafkaConnect);

        String optionalConfigs = staticProperties.get(KafkaEventAdapterConstants.ADAPTOR_OPTIONAL_CONFIGURATION_PROPERTIES);
        Properties props = new Properties();
        props.put("metadata.broker.list", kafkaConnect);
        props.put("serializer.class", "kafka.serializer.StringEncoder");

        if (optionalConfigs != null) {
            String[] optionalProperties = optionalConfigs.split(",");

            if (optionalProperties != null && optionalProperties.length > 0) {
                for (String header : optionalProperties) {
                    String[] configPropertyWithValue = header.split(":");
                    if(configPropertyWithValue.length == 2){
                        props.put(configPropertyWithValue[0], configPropertyWithValue[1]);
                    }else {
                        log.warn("Optional configuration property not defined in the correct format");
                    }
                }
            }
        }

        config = new ProducerConfig(props);
        producer = new Producer<String, Object>(config);
       try {
           String testTopic = "org.wso2.carbon.event.output.adapter.kafka.test";

           ZkClient zkClient = new ZkClient("localhost:2181", 10000, 10000, ZKStringSerializer$.MODULE$);
           AdminUtils.createTopic(zkClient, testTopic, 10, 1, new Properties());

           KeyedMessage<String, Object> data = new KeyedMessage<String, Object>(testTopic,"Successfully connected to kafka server");
           producer.send(data);

        }catch(kafka.common.TopicExistsException e){
           log.info("test topic already created.");
        }catch (Exception e){
           throw new OutputEventAdapterRuntimeException("The adaptor "+eventAdapterConfiguration.getName()+" failed to connect to the kafka server "
                   ,e);
       }


    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        String topic = dynamicProperties.get(KafkaEventAdapterConstants.ADAPTOR_PUBLISH_TOPIC);

        KeyedMessage<String, Object> data = new KeyedMessage<String, Object>(topic,message.toString());
        producer.send(data);
    }

    @Override
    public void disconnect() {
        //close producer
        if(producer != null){
            producer.close();
        }
    }

    @Override
    public void destroy() {
        //not required
    }

}
