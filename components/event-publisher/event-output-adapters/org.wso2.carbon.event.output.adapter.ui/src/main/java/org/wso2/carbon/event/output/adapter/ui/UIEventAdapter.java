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
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.ui.internal.UIOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.output.adapter.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.output.adapter.ui.internal.util.UIEventAdapterConstants;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Contains the life cycle of executions regarding the UI Adapter
 *
 */

public class UIEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(UIEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;
    private String streamId;
    private LinkedBlockingDeque<Object> streamSpecificEvents;

    public UIEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String,
            String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }

    @Override
    public void init() throws OutputEventAdapterException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if(eventAdapterConfiguration.getStaticProperties().get(UIEventAdapterConstants
                .ADAPTER_UI_OUTPUT_STREAM_VERSION) == null || " ".equals(eventAdapterConfiguration
                .getStaticProperties().get(UIEventAdapterConstants
                        .ADAPTER_UI_OUTPUT_STREAM_VERSION))){
            eventAdapterConfiguration.getStaticProperties().put(UIEventAdapterConstants.ADAPTER_UI_OUTPUT_STREAM_VERSION,
                    UIEventAdapterConstants.ADAPTER_UI_DEFAULT_OUTPUT_STREAM_VERSION);

        }

        streamId = eventAdapterConfiguration.getStaticProperties().get(
                UIEventAdapterConstants.ADAPTER_UI_OUTPUT_STREAM_NAME) + UIEventAdapterConstants.ADAPTER_UI_COLON +
                eventAdapterConfiguration.getStaticProperties().get(UIEventAdapterConstants
                        .ADAPTER_UI_OUTPUT_STREAM_VERSION);

        ConcurrentHashMap<Integer,ConcurrentHashMap<String, String>> tenantSpecifcEventOutputAdapterMap =
                UIEventAdaptorServiceInternalValueHolder.getTenantSpecificOutputEventStreamAdapterMap();

        ConcurrentHashMap<String, String> streamSpecifAdapterMap = tenantSpecifcEventOutputAdapterMap.get(tenantId);

        if(streamSpecifAdapterMap == null){
            streamSpecifAdapterMap = new ConcurrentHashMap<String, String>();
            if (null != tenantSpecifcEventOutputAdapterMap.putIfAbsent(tenantId, streamSpecifAdapterMap)){
                streamSpecifAdapterMap = tenantSpecifcEventOutputAdapterMap.get(tenantId);
            }
        }

        String adapterName = streamSpecifAdapterMap.get(streamId);

        if(adapterName != null){
            throw new OutputEventAdapterException(("An Output ui event adapter \""+ adapterName +"\" is already" +
                    " exist for stream id \""+ streamId + "\""));
        } else{
            streamSpecifAdapterMap.put(streamId, eventAdapterConfiguration.getName());

            ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>> tenantSpecificStreamMap =
                    UIEventAdaptorServiceInternalValueHolder.getTenantSpecificStreamEventMap();
            ConcurrentHashMap<String, LinkedBlockingDeque<Object>> streamSpecificEventsMap = tenantSpecificStreamMap.get(tenantId);

            if(streamSpecificEventsMap == null){
                streamSpecificEventsMap = new ConcurrentHashMap<String, LinkedBlockingDeque<Object>>();
                if (null != tenantSpecificStreamMap.putIfAbsent(tenantId, streamSpecificEventsMap)){
                    streamSpecificEventsMap = tenantSpecificStreamMap.get(tenantId);
                }
            }
            streamSpecificEvents = streamSpecificEventsMap.get(streamId);

            if (streamSpecificEvents == null){
                streamSpecificEvents = new LinkedBlockingDeque<Object>();
                if (null != streamSpecificEventsMap.putIfAbsent(streamId,streamSpecificEvents)){
                    streamSpecificEvents = streamSpecificEventsMap.get(streamId);
                }
            }
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

        UIOutputCallbackControllerServiceImpl uiOutputCallbackControllerServiceImpl =
                UIEventAdaptorServiceInternalValueHolder
                        .getUIOutputCallbackRegisterServiceImpl();
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        CopyOnWriteArrayList<Session> sessions = uiOutputCallbackControllerServiceImpl.getSessions(tenantId, streamId);

        StringBuilder allEventsAsString = null;
        int queueSize;

        if(globalProperties.get("eventQueueSize") != null){
            queueSize = Integer.parseInt(globalProperties.get("eventQueueSize"));
        } else {
            queueSize = UIEventAdapterConstants.EVENTS_QUEUE_SIZE;
        }

        if (message instanceof Object[]) {

            Boolean firstFilteredValue = true;
            for (Object object : (Object[])message){
                allEventsAsString = new StringBuilder("[");
                Event event = (Event) object;
                Boolean eventsExists = false;
                if(!firstFilteredValue){
                    allEventsAsString.append(",");
                }
                firstFilteredValue = false;

                if(streamSpecificEvents.size() == queueSize){
                    streamSpecificEvents.removeFirst();
                }

                if(event.getMetaData() != null){
                    Object[] metaData = event.getMetaData();
                    eventsExists = true;
                    for(int i=0;i < metaData.length;i++){
                        allEventsAsString.append("\"");
                        allEventsAsString.append(metaData[i]);
                        allEventsAsString.append("\"");
                        if(i != (metaData.length-1)){
                            allEventsAsString.append(",");
                        }
                    }
                }
                if(event.getCorrelationData() != null){
                    Object[] correlationData = event.getCorrelationData();

                    if(eventsExists){
                        allEventsAsString.append(",");
                    } else{
                        eventsExists = true;
                    }
                    for(int i=0;i < correlationData.length;i++){
                        allEventsAsString.append("\"");
                        allEventsAsString.append(correlationData[i]);
                        allEventsAsString.append("\"");
                        if(i != (correlationData.length-1)){
                            allEventsAsString.append(",");
                        }
                    }
                }
                if(event.getPayloadData() != null){

                    Object[] payloadData = event.getPayloadData();
                    if(eventsExists){
                        allEventsAsString.append(",");
                    }

                    for(int i=0;i < payloadData.length;i++){
                        allEventsAsString.append("\"");
                        allEventsAsString.append(payloadData[i]);
                        allEventsAsString.append("\"");
                        if(i != (payloadData.length-1)){
                            allEventsAsString.append(",");
                        }
                    }
                }
                allEventsAsString.append("]");
                Object[] eventValues = new Object[UIEventAdapterConstants.INDEX_TWO];
                eventValues[UIEventAdapterConstants.INDEX_ZERO] = allEventsAsString;
                eventValues[UIEventAdapterConstants.INDEX_ONE] = System.currentTimeMillis();
                streamSpecificEvents.add(eventValues);
            }
        } else {

            Event event = (Event)(message);
            allEventsAsString = new StringBuilder("[");
            Boolean eventsExists = false;

            if(streamSpecificEvents.size() == queueSize){
                streamSpecificEvents.removeFirst();
            }

            if(event.getMetaData() != null){

                Object[] metaData = event.getMetaData();
                eventsExists = true;
                for(int i=0;i < metaData.length;i++){
                    allEventsAsString.append("\"");
                    allEventsAsString.append(metaData[i]);
                    allEventsAsString.append("\"");
                    if(i != (metaData.length-1)){
                        allEventsAsString.append(",");
                    }
                }
            }

            if(event.getCorrelationData() != null){
                Object[] correlationData = event.getCorrelationData();

                if(eventsExists){
                    allEventsAsString.append(",");
                } else{
                    eventsExists = true;
                }
                for(int i=0;i < correlationData.length;i++){
                    allEventsAsString.append("\"");
                    allEventsAsString.append(correlationData[i]);
                    allEventsAsString.append("\"");
                    if(i != (correlationData.length-1)){
                        allEventsAsString.append(",");
                    }
                }
            }

            if(event.getPayloadData() != null){

                Object[] payloadData = event.getPayloadData();
                if(eventsExists){
                    allEventsAsString.append(",");
                }
                for(int i=0;i < payloadData.length;i++){
                    allEventsAsString.append("\"");
                    allEventsAsString.append(payloadData[i]);
                    allEventsAsString.append("\"");
                    if(i != (payloadData.length-1)){
                        allEventsAsString.append(",");
                    }
                }
            }

            allEventsAsString.append("]");
            Object[] eventValues = new Object[UIEventAdapterConstants.INDEX_TWO];
            eventValues[UIEventAdapterConstants.INDEX_ZERO] = allEventsAsString;
            eventValues[UIEventAdapterConstants.INDEX_ONE] = System.currentTimeMillis();
            streamSpecificEvents.add(eventValues);
        }

        if (sessions != null){
            for (Session session : sessions) {
                synchronized (session) {
                    session.getAsyncRemote().sendText(allEventsAsString.toString());
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug(
                        "Dropping the message: '" + message + "', since no clients have being registered to receive " +
                                "events from ui adapter: '" + eventAdapterConfiguration.getName() + "', " +
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
}

