/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.stream.core.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.event.stream.core.*;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.ds.EventStreamServiceValueHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventStreamRuntime {

    private static final Log log = LogFactory.getLog(EventStreamRuntime.class);

    private Map<Integer, Map<String, EventJunction>> tenantSpecificEventJunctions =
            new HashMap<Integer, Map<String, EventJunction>>();

    public void loadEventStream(String streamId)
            throws EventStreamConfigurationException {

        StreamDefinition streamDefinition = EventStreamServiceValueHolder.getCarbonEventStreamService().getStreamDefinition(
                DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId),
                DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId));
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (streamDefinition != null) {
            Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
            if (eventJunctionMap == null) {
                eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
                tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
            }
            EventJunction junction = new EventJunction(streamDefinition);
            eventJunctionMap.put(streamDefinition.getStreamId(), junction);

            for (EventStreamListener eventStreamListener : EventStreamServiceValueHolder.getEventStreamListenerList()) {
                eventStreamListener.addedEventStream(tenantId, streamDefinition.getName(),
                        streamDefinition.getVersion());
            }
        }
    }

    public void unloadEventStream(String streamId)
            throws EventStreamConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        StreamDefinition streamDefinition = EventStreamServiceValueHolder.getCarbonEventStreamService().getStreamDefinition(
                DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId),
                DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId));

        if (streamDefinition == null) {
            Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
            if (eventJunctionMap != null) {
                eventJunctionMap.remove(streamId);
            }

            for (EventStreamListener eventStreamListener : EventStreamServiceValueHolder.getEventStreamListenerList()) {
                eventStreamListener.removedEventStream(tenantId, DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId), DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId));
            }
        }

    }

    private EventJunction getOrConstructEventJunction(String streamId) throws EventStreamConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap == null) {
            eventJunctionMap = new ConcurrentHashMap<String, EventJunction>();
            tenantSpecificEventJunctions.put(tenantId, eventJunctionMap);
        }
        EventJunction eventJunction = eventJunctionMap.get(streamId);
        if (eventJunction == null) {
            StreamDefinition streamDefinition = null;
            try {
                streamDefinition = EventStreamServiceValueHolder.getCarbonEventStreamService().getStreamDefinition(streamId);
            } catch (Exception e) {
                throw new EventStreamConfigurationException("Cannot retrieve Stream " + streamId + " for tenant " + tenantId);
            }
            if (streamDefinition == null) {
                throw new EventStreamConfigurationException("Stream " + streamId + " is not configured to tenant " + tenantId);
            }
            eventJunction = new EventJunction(streamDefinition);
            eventJunctionMap.put(streamDefinition.getStreamId(), eventJunction);
        }
        return eventJunction;
    }

    public void publish(String streamId, Event event) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if(eventJunctionMap != null && eventJunctionMap.containsKey(streamId)) {
            EventJunction eventJunction = eventJunctionMap.get(streamId);
            eventJunction.sendEvent(event);
        } else {
            log.debug("Event " + event.toString() + " dropped since no junction found for the streamId " + streamId);
        }
    }

    public void subscribe(SiddhiEventConsumer siddhiEventConsumer) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(siddhiEventConsumer.getStreamId());
        eventJunction.addConsumer(siddhiEventConsumer);

    }

    public void subscribe(RawEventConsumer rawEventConsumer) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(rawEventConsumer.getStreamId());
        eventJunction.addConsumer(rawEventConsumer);
    }

    public void subscribe(EventProducer eventProducer) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(eventProducer.getStreamId());
        eventJunction.addProducer(eventProducer);
    }

    public void subscribe(WSO2EventConsumer wso2EventConsumer) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(wso2EventConsumer.getStreamId());
        eventJunction.addConsumer(wso2EventConsumer);
    }

    public void subscribe(WSO2EventListConsumer wso2EventListConsumer) throws EventStreamConfigurationException {
        EventJunction eventJunction = getOrConstructEventJunction(wso2EventListConsumer.getStreamId());
        eventJunction.addConsumer(wso2EventListConsumer);
    }

    public void unsubscribe(SiddhiEventConsumer siddhiEventConsumer) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(siddhiEventConsumer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeConsumer(siddhiEventConsumer);
            }
        }
    }

    public void unsubscribe(RawEventConsumer rawEventConsumer) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(rawEventConsumer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeConsumer(rawEventConsumer);
            }
        }
    }

    public void unsubscribe(EventProducer eventProducer) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(eventProducer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeProducer(eventProducer);
            }
        }
    }

    public void unsubscribe(WSO2EventConsumer wso2EventConsumer) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(wso2EventConsumer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeConsumer(wso2EventConsumer);
            }
        }
    }

    public void unsubscribe(WSO2EventListConsumer wso2EventListConsumer) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventJunction> eventJunctionMap = tenantSpecificEventJunctions.get(tenantId);
        if (eventJunctionMap != null) {
            EventJunction eventJunction = eventJunctionMap.get(wso2EventListConsumer.getStreamId());
            if (eventJunction != null) {
                eventJunction.removeConsumer(wso2EventListConsumer);
            }
        }
    }
}
