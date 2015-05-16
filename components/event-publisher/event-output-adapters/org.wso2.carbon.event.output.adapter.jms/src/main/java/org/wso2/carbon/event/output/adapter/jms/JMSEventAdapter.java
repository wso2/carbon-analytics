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
package org.wso2.carbon.event.output.adapter.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.jms.internal.util.*;

import javax.jms.Connection;
import javax.jms.Message;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.*;
import java.util.concurrent.*;

public class JMSEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(JMSEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private PublisherDetails publisherDetails = null;
    private static ExecutorService executorService;

    public JMSEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                           Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        //ExecutorService will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(
                        JMSEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = JMSEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(
                        JMSEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = JMSEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        JMSEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = JMSEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
            }

            if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer.parseInt(globalProperties.get(
                        JMSEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = JMSEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(jobQueSize));
        }

    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {

        try {
            Hashtable<String, String> adaptorProperties = new Hashtable<String, String>();
            adaptorProperties.putAll(eventAdapterConfiguration.getStaticProperties());
            JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, eventAdapterConfiguration.getName());
            Connection connection = jmsConnectionFactory.getConnection();
            connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.close();
            jmsConnectionFactory.stop();
        } catch (Exception e) {
            throw new OutputEventAdapterRuntimeException(e);
        }
    }

    @Override
    public void connect() {

        String topicName = eventAdapterConfiguration.getStaticProperties().get(
                JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION);

        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, topicName);
        publisherDetails = initPublisher(eventAdapterConfiguration, messageConfig);
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        Message jmsMessage = publisherDetails.getJmsMessageSender().convertToJMSMessage(message,
                publisherDetails.getMessageConfig());
        setJMSTransportHeaders(jmsMessage, dynamicProperties.get(JMSEventAdapterConstants.ADAPTER_JMS_HEADER));
        try {
            executorService.submit(new JMSSender(jmsMessage));
        } catch (RejectedExecutionException e) {
            log.error("There is no thread connection left to publish event : " + message, e);
        }
    }

    @Override
    public void disconnect() {
        if (publisherDetails != null) {
            publisherDetails.getJmsMessageSender().close();
            publisherDetails.getJmsConnectionFactory().stop();
        }
    }

    @Override
    public void destroy() {
        //not required
    }

    private PublisherDetails initPublisher(
            OutputEventAdapterConfiguration outputEventAdaptorConfiguration,
            Map<String, String> messageConfig) {

        PublisherDetails publisherDetails;
        Hashtable<String, String> adaptorProperties =
                convertMapToHashTable(outputEventAdaptorConfiguration.getStaticProperties());
        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, outputEventAdaptorConfiguration.getName());
        JMSMessageSender jmsMessageSender = new JMSMessageSender(jmsConnectionFactory, messageConfig);
        publisherDetails = new PublisherDetails(jmsConnectionFactory, jmsMessageSender, messageConfig);

        return publisherDetails;
    }

    private Message setJMSTransportHeaders(Message message, String headerProperty) {

        Map<String, String> messageConfiguration = new HashMap<String, String>();

        if (headerProperty != null && message != null) {
            String[] headers = headerProperty.split(JMSEventAdapterConstants.HEADER_SEPARATOR);

            if (headers != null && headers.length > 0) {
                for (String header : headers) {
                    try {
                        String[] headerPropertyWithValue = header.split(JMSEventAdapterConstants.ENTRY_SEPARATOR, 2);
                        messageConfiguration.put(headerPropertyWithValue[0], headerPropertyWithValue[1]);
                    } catch (Exception e) {
                        log.warn("Header property \" " + header + " \" is not defined in the correct format", e);
                    }
                }
            }

            try {
                return JMSUtils.setTransportHeaders(messageConfiguration, message);
            } catch (JMSException e) {
                throw new OutputEventAdapterRuntimeException(e);
            }
        }

        return message;
    }

    class PublisherDetails {
        private final JMSConnectionFactory jmsConnectionFactory;
        private final JMSMessageSender jmsMessageSender;
        private final Map<String, String> messageConfig;

        public PublisherDetails(JMSConnectionFactory jmsConnectionFactory,
                                JMSMessageSender jmsMessageSender, Map<String, String> messageConfig) {
            this.jmsConnectionFactory = jmsConnectionFactory;
            this.jmsMessageSender = jmsMessageSender;
            this.messageConfig = messageConfig;
        }

        public JMSConnectionFactory getJmsConnectionFactory() {
            return jmsConnectionFactory;
        }

        public JMSMessageSender getJmsMessageSender() {
            return jmsMessageSender;
        }

        public Map<String, String> getMessageConfig() {
            return messageConfig;
        }

    }

    private Hashtable<String, String> convertMapToHashTable(Map<String, String> map) {
        Hashtable<String, String> table = new Hashtable();
        Iterator it = map.entrySet().iterator();

        //Iterate through the hash map
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            //null values will be removed
            if (pair.getValue() != null) {
                table.put(pair.getKey().toString(), pair.getValue().toString());
            }
        }

        return table;
    }

    public class JMSSender implements Runnable {

        private Message jmsMessage;

        public JMSSender(Message jmsMessage) {
            this.jmsMessage = jmsMessage;

        }

        @Override
        public void run() {
            publisherDetails.getJmsMessageSender().send(jmsMessage, publisherDetails.getMessageConfig());
        }
    }

}
