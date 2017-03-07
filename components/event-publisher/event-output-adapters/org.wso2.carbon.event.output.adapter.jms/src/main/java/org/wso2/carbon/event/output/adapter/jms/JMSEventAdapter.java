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
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.jms.internal.util.JMSConnectionFactory;
import org.wso2.carbon.event.output.adapter.jms.internal.util.JMSConstants;
import org.wso2.carbon.event.output.adapter.jms.internal.util.JMSEventAdapterConstants;
import org.wso2.carbon.event.output.adapter.jms.internal.util.JMSMessageSender;

import javax.jms.Connection;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

public class JMSEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(JMSEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private PublisherDetails publisherDetails = null;
    private static ExecutorService executorService;
    private int tenantId;

    public JMSEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration,
                           Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        //ExecutorService will be assigned  if it is null
        if (executorService == null) {
            int minThread;
            int maxThread;
            long defaultKeepAliveTime;
            int jobQueSize;

            //If global properties are available those will be assigned else constant values will be assigned
            if (JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS_NOT_ALLOWED.equals(
                    eventAdapterConfiguration.getStaticProperties().get(JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS)) ) {
                minThread = 1;
            } else if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(
                        JMSEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = JMSEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS_NOT_ALLOWED.equals(
                    eventAdapterConfiguration.getStaticProperties().get(JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS))) {
                maxThread = 1;
            } else if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
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
            JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adaptorProperties, eventAdapterConfiguration.getName(), adaptorProperties.get(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION), 1);
            Connection connection = jmsConnectionFactory.createConnection();
            connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            connection.close();
            jmsConnectionFactory.close();
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

        try {
            executorService.submit(new JMSSender(message, dynamicProperties));
        } catch (RejectedExecutionException e) {
            EventAdapterUtil.logAndDrop(eventAdapterConfiguration.getName(), message, "Job queue is full", e, log, tenantId);
        }
    }


    @Override
    public void disconnect() {
        if (publisherDetails != null) {
            publisherDetails.getJmsMessageSender().close();
            publisherDetails.getJmsConnectionFactory().close();
        }
    }

    @Override
    public void destroy() {
        //not required
    }

    @Override
    public boolean isPolled() {
        return false;
    }

    private PublisherDetails initPublisher(
            OutputEventAdapterConfiguration outputEventAdaptorConfiguration,
            Map<String, String> messageConfig) {

        PublisherDetails publisherDetails;
        Hashtable<String, String> adapterProperties =
                convertMapToHashTable(outputEventAdaptorConfiguration.getStaticProperties());

        Map<String, String> jmsProperties = this.extractProperties(eventAdapterConfiguration.getStaticProperties().get(
                JMSEventAdapterConstants.ADAPTER_PROPERTIES));

        Map<String, String> jmsSecuredProperties = this.extractProperties(eventAdapterConfiguration.getStaticProperties().get(
                JMSEventAdapterConstants.ADAPTER_SECURED_PROPERTIES));

        if (jmsProperties != null) {
            adapterProperties.remove(JMSEventAdapterConstants.ADAPTER_PROPERTIES);
            adapterProperties.putAll(jmsProperties);
        }

        if(jmsSecuredProperties != null){
            adapterProperties.remove(JMSEventAdapterConstants.ADAPTER_SECURED_PROPERTIES);
            adapterProperties.putAll(jmsSecuredProperties);
        }

        int maxConnections;
        if (JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS_NOT_ALLOWED.equals(
            eventAdapterConfiguration.getStaticProperties().get(JMSEventAdapterConstants.ADAPTER_JMS_ALLOW_CONCURRENT_CONNECTIONS))) {
            maxConnections = 1;
        } else if (globalProperties.get(JMSEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
            maxConnections = Integer.parseInt(globalProperties.get(
                    JMSEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
        } else {
            maxConnections = JMSEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
        }

        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(adapterProperties, outputEventAdaptorConfiguration.getName(), messageConfig.get(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION), maxConnections);
        JMSMessageSender jmsMessageSender = new JMSMessageSender(jmsConnectionFactory);
        publisherDetails = new PublisherDetails(jmsConnectionFactory, jmsMessageSender, messageConfig);

        return publisherDetails;
    }

    public static class PublisherDetails {
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
        Hashtable<String, String> table = new Hashtable<String, String>();
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

    private Map<String, String> extractProperties(String properties) {
        if (properties == null || properties.trim().length() == 0) {
            return null;
        }

        String[] entries = properties.split(JMSEventAdapterConstants.PROPERTY_SEPARATOR);
        String[] keyValue;
        Map<String, String> result = new HashMap<String, String>();
        for (String property : entries) {
            try {
                keyValue = property.split(JMSEventAdapterConstants.ENTRY_SEPARATOR, 2);
                result.put(keyValue[0].trim(), keyValue[1].trim());
            } catch (Exception e) {
                log.warn("JMS property '" + property + "' is not defined in the correct format.", e);
            }
        }
        return result;

    }

    public class JMSSender implements Runnable {

        private Object jmsMessage;
        private Map<String, String> dynamicProperties;

        public JMSSender(Object jmsMessage, Map<String, String> dynamicProperties) {
            this.jmsMessage = jmsMessage;
            this.dynamicProperties = dynamicProperties;

        }

        @Override
        public void run() {
            publisherDetails.getJmsMessageSender().send(jmsMessage, publisherDetails, dynamicProperties.get(JMSEventAdapterConstants.ADAPTER_JMS_HEADER));
        }
    }

}
