/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.output.adapter.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.ui.internal.UIOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.output.adapter.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.output.adapter.ui.internal.util.UIEventAdapterConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Contains the life cycle of executions regarding the UI Adapter
 */

public class UIEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(UIEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private String streamId;
    private int queueSize;
    private LinkedBlockingDeque<Object> streamSpecificEvents;
    private static ThreadPoolExecutor executorService;

    public UIEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
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
            if (globalProperties.get(UIEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME) != null) {
                minThread = Integer.parseInt(globalProperties.get(
                        UIEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE_NAME));
            } else {
                minThread = UIEventAdapterConstants.ADAPTER_MIN_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(UIEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME) != null) {
                maxThread = Integer.parseInt(globalProperties.get(
                        UIEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE_NAME));
            } else {
                maxThread = UIEventAdapterConstants.ADAPTER_MAX_THREAD_POOL_SIZE;
            }

            if (globalProperties.get(UIEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME) != null) {
                defaultKeepAliveTime = Integer.parseInt(globalProperties.get(
                        UIEventAdapterConstants.ADAPTER_KEEP_ALIVE_TIME_NAME));
            } else {
                defaultKeepAliveTime = UIEventAdapterConstants.DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS;
            }

            if (globalProperties.get(UIEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME) != null) {
                jobQueSize = Integer.parseInt(globalProperties.get(
                        UIEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME));
            } else {
                jobQueSize = UIEventAdapterConstants.ADAPTER_EXECUTOR_JOB_QUEUE_SIZE;
            }

            executorService = new ThreadPoolExecutor(minThread, maxThread, defaultKeepAliveTime, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(jobQueSize));
        }


        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        streamId = eventAdapterConfiguration.getOutputStreamIdOfWso2eventMessageFormat();
        if (streamId == null || streamId.isEmpty()) {
            throw new OutputEventAdapterRuntimeException("UI event adapter needs a output stream id");
        }

        ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> tenantSpecifcEventOutputAdapterMap =
                UIEventAdaptorServiceInternalValueHolder.getTenantSpecificOutputEventStreamAdapterMap();

        ConcurrentHashMap<String, String> streamSpecifAdapterMap = tenantSpecifcEventOutputAdapterMap.get(tenantId);

        if (streamSpecifAdapterMap == null) {
            streamSpecifAdapterMap = new ConcurrentHashMap<String, String>();
            if (null != tenantSpecifcEventOutputAdapterMap.putIfAbsent(tenantId, streamSpecifAdapterMap)) {
                streamSpecifAdapterMap = tenantSpecifcEventOutputAdapterMap.get(tenantId);
            }
        }

        String adapterName = streamSpecifAdapterMap.get(streamId);

        if (adapterName != null) {
            throw new OutputEventAdapterException(("An Output ui event adapter \"" + adapterName + "\" is already" +
                    " exist for stream id \"" + streamId + "\""));
        } else {
            streamSpecifAdapterMap.put(streamId, eventAdapterConfiguration.getName());

            ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>> tenantSpecificStreamMap =
                    UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamEventMap();
            ConcurrentHashMap<String, LinkedBlockingDeque<Object>> streamSpecificEventsMap = tenantSpecificStreamMap.get(tenantId);

            if (streamSpecificEventsMap == null) {
                streamSpecificEventsMap = new ConcurrentHashMap<String, LinkedBlockingDeque<Object>>();
                if (null != tenantSpecificStreamMap.putIfAbsent(tenantId, streamSpecificEventsMap)) {
                    streamSpecificEventsMap = tenantSpecificStreamMap.get(tenantId);
                }
            }
            streamSpecificEvents = streamSpecificEventsMap.get(streamId);

            if (streamSpecificEvents == null) {
                streamSpecificEvents = new LinkedBlockingDeque<Object>();
                if (null != streamSpecificEventsMap.putIfAbsent(streamId, streamSpecificEvents)) {
                    streamSpecificEvents = streamSpecificEventsMap.get(streamId);
                }
            }
        }

        if (globalProperties.get(UIEventAdapterConstants.ADAPTER_EVENT_QUEUE_SIZE_NAME) != null) {
            queueSize = Integer.parseInt(globalProperties.get(UIEventAdapterConstants.ADAPTER_EVENT_QUEUE_SIZE_NAME));
        } else {
            queueSize = UIEventAdapterConstants.EVENTS_QUEUE_SIZE;
        }
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        //Not needed
    }

    @Override
    public void connect() {
        //Not needed
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {

        Event event = (Event) message;
        StringBuilder eventBuilder = new StringBuilder("[");
        Boolean dataExist = false;

        if (streamSpecificEvents.size() == queueSize) {
            streamSpecificEvents.removeFirst();
        }

        if (event.getMetaData() != null) {

            Object[] metaData = event.getMetaData();
            dataExist = true;
            for (int i = 0; i < metaData.length; i++) {
                eventBuilder.append("\"");
                eventBuilder.append(metaData[i]);
                eventBuilder.append("\"");
                if (i != (metaData.length - 1)) {
                    eventBuilder.append(",");
                }
            }
        }

        if (event.getCorrelationData() != null) {
            Object[] correlationData = event.getCorrelationData();

            if (dataExist) {
                eventBuilder.append(",");
            } else {
                dataExist = true;
            }
            for (int i = 0; i < correlationData.length; i++) {
                eventBuilder.append("\"");
                eventBuilder.append(correlationData[i]);
                eventBuilder.append("\"");
                if (i != (correlationData.length - 1)) {
                    eventBuilder.append(",");
                }
            }
        }

        if (event.getPayloadData() != null) {

            Object[] payloadData = event.getPayloadData();
            if (dataExist) {
                eventBuilder.append(",");
            }
            for (int i = 0; i < payloadData.length; i++) {
                eventBuilder.append("\"");
                eventBuilder.append(payloadData[i]);
                eventBuilder.append("\"");
                if (i != (payloadData.length - 1)) {
                    eventBuilder.append(",");
                }
            }
        }

        eventBuilder.append("]");
        String eventString = eventBuilder.toString();
        Object[] eventValues = new Object[UIEventAdapterConstants.INDEX_TWO];
        eventValues[UIEventAdapterConstants.INDEX_ZERO] = eventString;
        eventValues[UIEventAdapterConstants.INDEX_ONE] = System.currentTimeMillis();
        streamSpecificEvents.add(eventValues);

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            executorService.execute(new WebSocketSender(eventString, tenantId));
        } catch (RejectedExecutionException e) {
            log.error("Event Dropped by Output UI Adapter '" + eventAdapterConfiguration.getName() + "'");
            if (log.isDebugEnabled()) {
                log.debug("Dropping the message: '" + message + "', since since buffer queue is full for" +
                        "ui adapter: '" + eventAdapterConfiguration.getName() + "', " +
                        "for tenant ID: " + tenantId);
            }
        }

    }

    @Override
    public void disconnect() {
        //Not needed
    }

    @Override
    public void destroy() {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        //Removing outputadapter and streamId
        UIEventAdaptorServiceInternalValueHolder
                .getTenantSpecificOutputEventStreamAdapterMap().get(tenantId).remove(streamId);

        //Removing the streamId and events registered for the output adapter
        UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamEventMap().get(tenantId).remove(streamId);

    }

    private class WebSocketSender implements Runnable {

        private String message;
        private int tenantId = MultitenantConstants.INVALID_TENANT_ID;

        public WebSocketSender(String message, int tenantId) {
            this.message = message;
            this.tenantId = tenantId;
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {

            UIOutputCallbackControllerServiceImpl uiOutputCallbackControllerServiceImpl = UIEventAdaptorServiceInternalValueHolder.getUIOutputCallbackRegisterServiceImpl();
            CopyOnWriteArrayList<Session> sessions = uiOutputCallbackControllerServiceImpl.getSessions(tenantId, streamId);
            if (sessions != null) {
                for (Session session : sessions) {
                    synchronized (session) {
                        try {
                            session.getBasicRemote().sendText(message);
                        } catch (IOException e) {
                            log.error("Event Dropped by Output UI Adapter '" + eventAdapterConfiguration.getName() + "'");
                            if (log.isDebugEnabled()) {
                                log.debug("Dropping the message: '" + message + "' from ui adapter: '" + eventAdapterConfiguration.getName() + "' " +
                                        "for tenant ID: " + tenantId + ", as " + e.getMessage(), e);
                            }
                        }
                    }
                }
            } else {
                log.error("Event Dropped by Output UI Adapter '" + eventAdapterConfiguration.getName() + "'");
                if (log.isDebugEnabled()) {
                    log.debug("Dropping the message: '" + message + "', since no clients have being registered to receive " +
                            "events from ui adapter: '" + eventAdapterConfiguration.getName() + "', " +
                            "for tenant ID: " + tenantId);
                }
            }
        }
    }
}

