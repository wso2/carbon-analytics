/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.message.tracer.handler.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.message.tracer.handler.data.TracingInfo;
import org.wso2.carbon.analytics.message.tracer.handler.stream.StreamDefCreator;
import org.wso2.carbon.analytics.message.tracer.handler.util.ServiceHolder;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Publisher {

    private static final Log LOG = LogFactory.getLog(Publisher.class);

    public Publisher() {
    }

    public void publish(TracingInfo tracingInfo) {

        List<Object> correlationData = getCorrelationData(tracingInfo);
        List<Object> metaData = getMetaData(tracingInfo);
        List<Object> payLoadData = getEventData(tracingInfo);
        Map<String, String> arbitraryData = tracingInfo.getAdditionalValues();
        StreamDefinition streamDef;
        try {
            streamDef = StreamDefCreator.getStreamDef();
        } catch (MalformedStreamDefinitionException e) {
            LOG.error("Unable to create stream: " + e.getMessage(), e);
            return;
        }
        if (streamDef != null) {
            EventStreamService eventStreamService = ServiceHolder.getEventStreamService();
            if (eventStreamService != null) {
                try {
                    eventStreamService.addEventStreamDefinition(streamDef);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Added stream definition to event publisher service.");
                    }
                } catch (EventStreamConfigurationException e) {
                    LOG.error("Error in adding stream definition to service:" + e.getMessage(), e);
                }
                Event tracingEvent = new Event();
                tracingEvent.setStreamId(streamDef.getStreamId());
                tracingEvent.setCorrelationData(correlationData.toArray());
                tracingEvent.setMetaData(metaData.toArray());
                tracingEvent.setPayloadData(payLoadData.toArray());
                tracingEvent.setArbitraryDataMap(arbitraryData);
                eventStreamService.publish(tracingEvent);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Successfully published event");
                }
            }
        }
    }

    public static List<Object> getCorrelationData(TracingInfo tracingInfo) {
        List<Object> correlationData = new ArrayList<Object>(1);
        correlationData.add(tracingInfo.getActivityId());
        return correlationData;
    }

    public static List<Object> getMetaData(TracingInfo tracingInfo) {
        List<Object> metaData = new ArrayList<Object>(7);
        metaData.add(tracingInfo.getRequestUrl());
        metaData.add(tracingInfo.getHost());
        metaData.add(tracingInfo.getServer());
        return metaData;
    }

    public static List<Object> getEventData(TracingInfo tracingInfo) {
        List<Object> payloadData = new ArrayList<Object>(8);
        payloadData.add(tracingInfo.getServiceName());
        payloadData.add(tracingInfo.getOperationName());
        payloadData.add(tracingInfo.getMessageDirection());
        payloadData.add(tracingInfo.getPayload());
        payloadData.add(tracingInfo.getHeader());
        payloadData.add(tracingInfo.getTimestamp());
        payloadData.add(tracingInfo.getStatus());
        payloadData.add(tracingInfo.getUserName());
        return payloadData;
    }
}
