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
package org.wso2.carbon.event.input.adapter.jms;

import org.apache.axis2.transport.base.threads.NativeWorkerPool;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.jms.internal.util.*;

import javax.jms.JMSException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class JMSEventAdapter implements InputEventAdapter {

    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private InputEventAdapterListener eventAdapterListener;
    private final String id = UUID.randomUUID().toString();

    private ConcurrentHashMap<Integer, ConcurrentHashMap<String,
            ConcurrentHashMap<String, ConcurrentHashMap<String,
                    SubscriptionDetails>>>> tenantAdaptorDestinationSubscriptionsMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>>();
//    public static ExecutorService executorService = new ThreadPoolExecutor(
//                                                      SOAPEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE,
//            SOAPEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE, SOAPEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME,
//      TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(SOAPEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE));

    public JMSEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration,
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
        createJMSAdaptorListener(eventAdapterListener, "1", tenantId);

    }

    @Override
    public void disconnect() {

        String destination = eventAdapterConfiguration.getProperties()
                .get(JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION);

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>
                adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            throw new InputEventAdapterRuntimeException("There is no subscription for " + destination + " for tenant "
                    + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true));
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>> destinationSubscriptionsMap =
                adaptorDestinationSubscriptionsMap.get(eventAdapterConfiguration.getName());
        if (destinationSubscriptionsMap == null) {
            throw new InputEventAdapterRuntimeException("There is no subscription for " + destination
                    + " for event adaptor " + eventAdapterConfiguration.getName());
        }

        ConcurrentHashMap<String, SubscriptionDetails> subscriptionsMap = destinationSubscriptionsMap.get(destination);
        if (subscriptionsMap == null) {
            throw new InputEventAdapterRuntimeException("There is no subscription for " + destination);
        }

        SubscriptionDetails subscriptionDetails = subscriptionsMap.get(id);
        if (subscriptionDetails == null) {
            throw new InputEventAdapterRuntimeException("There is no subscription for " + destination
                    + " for the subscriptionId:" + id);
        } else {

            try {
                subscriptionDetails.close();
                subscriptionsMap.remove(id);
            } catch (JMSException e) {
                throw new InputEventAdapterRuntimeException("Can not unsubscribe from the destination " + destination
                        + " with the event adaptor " + eventAdapterConfiguration.getName(), e);
            }

        }
    }

    @Override
    public void destroy() {
    }

    public InputEventAdapterListener getEventAdaptorListener() {
        return eventAdapterListener;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JMSEventAdapter)) return false;

        JMSEventAdapter that = (JMSEventAdapter) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private void createJMSAdaptorListener(
            InputEventAdapterListener inputEventAdaptorListener, String subscriptionId, int tenantId) {


        ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>
                adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
        if (adaptorDestinationSubscriptionsMap == null) {
            adaptorDestinationSubscriptionsMap = new ConcurrentHashMap<String,
                    ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>>>();

            if (null != tenantAdaptorDestinationSubscriptionsMap.putIfAbsent(tenantId,
                    adaptorDestinationSubscriptionsMap)) {
                adaptorDestinationSubscriptionsMap = tenantAdaptorDestinationSubscriptionsMap.get(tenantId);
            }
        }

        ConcurrentHashMap<String, ConcurrentHashMap<String, SubscriptionDetails>> destinationSubscriptionsMap =
                adaptorDestinationSubscriptionsMap.get(eventAdapterConfiguration.getName());
        if (destinationSubscriptionsMap == null) {
            destinationSubscriptionsMap = new ConcurrentHashMap<String,
                    ConcurrentHashMap<String, SubscriptionDetails>>();

            if (null != adaptorDestinationSubscriptionsMap.putIfAbsent(eventAdapterConfiguration.getName(),
                    destinationSubscriptionsMap)) {
                destinationSubscriptionsMap = adaptorDestinationSubscriptionsMap.get(
                        eventAdapterConfiguration.getName());
            }
        }

        String destination = eventAdapterConfiguration.getProperties().get(
                JMSEventAdapterConstants.ADAPTER_JMS_DESTINATION);

        ConcurrentHashMap<String, SubscriptionDetails> subscriptionsMap = destinationSubscriptionsMap.get(destination);
        if (subscriptionsMap == null) {
            subscriptionsMap = new ConcurrentHashMap<String, SubscriptionDetails>();
            if (null != destinationSubscriptionsMap.putIfAbsent(destination, subscriptionsMap)) {
                subscriptionsMap = destinationSubscriptionsMap.get(destination);
            }
        }


        Map<String, String> adaptorProperties = new HashMap<String, String>();


        adaptorProperties.putAll(eventAdapterConfiguration.getProperties());

        JMSConnectionFactory jmsConnectionFactory = new JMSConnectionFactory(convertMapToHashTable(adaptorProperties),
                eventAdapterConfiguration.getName());


        Map<String, String> messageConfig = new HashMap<String, String>();
        messageConfig.put(JMSConstants.PARAM_DESTINATION, destination);
        JMSTaskManager jmsTaskManager = JMSTaskManagerFactory.createTaskManagerForService(jmsConnectionFactory,
                eventAdapterConfiguration.getName(), new NativeWorkerPool(4, 100, 1000, 1000, "JMS Threads",
                        "JMSThreads" + UUID.randomUUID().toString()), messageConfig);
        jmsTaskManager.setJmsMessageListener(new JMSMessageListener(inputEventAdaptorListener));

        JMSListener jmsListener = new JMSListener(eventAdapterConfiguration.getName() + "#" + destination,
                jmsTaskManager);
        jmsListener.startListener();
        SubscriptionDetails subscriptionDetails = new SubscriptionDetails(jmsConnectionFactory, jmsListener);
        subscriptionsMap.put(subscriptionId, subscriptionDetails);

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


    class SubscriptionDetails {

        private final JMSConnectionFactory jmsConnectionFactory;
        private final JMSListener jmsListener;

        public SubscriptionDetails(JMSConnectionFactory jmsConnectionFactory,
                                   JMSListener jmsListener) {
            this.jmsConnectionFactory = jmsConnectionFactory;
            this.jmsListener = jmsListener;
        }

        public void close() throws JMSException {
            this.jmsListener.stopListener();
            this.jmsConnectionFactory.stop();
        }

        public JMSConnectionFactory getJmsConnectionFactory() {
            return jmsConnectionFactory;
        }

        public JMSListener getJmsListener() {
            return jmsListener;
        }
    }


}
