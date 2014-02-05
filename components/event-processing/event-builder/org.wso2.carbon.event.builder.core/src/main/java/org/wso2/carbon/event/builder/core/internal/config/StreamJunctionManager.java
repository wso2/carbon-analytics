/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.event.builder.core.internal.config;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.exception.EventBuilderStreamValidationException;
import org.wso2.carbon.event.builder.core.internal.CarbonEventBuilderService;
import org.wso2.carbon.event.builder.core.internal.EventBuilder;
import org.wso2.carbon.event.builder.core.internal.ds.EventBuilderServiceValueHolder;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderUtil;
import org.wso2.carbon.event.processor.api.receive.BasicEventListener;
import org.wso2.carbon.event.processor.api.receive.EventReceiverStreamNotificationListener;
import org.wso2.carbon.event.processor.api.receive.Wso2EventListener;
import org.wso2.carbon.event.processor.api.receive.exception.EventReceiverException;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StreamJunctionManager {
    private static final String STREAM_DEFINITION_STORE = EventBuilderConstants.DEFAULT_STREAM_DEFINITION_STORE_LOCATION;
    private Map<String, StreamEventJunction> streamEventJunctionMap = new ConcurrentHashMap<String, StreamEventJunction>();
    private Map<String, Set<String>> eventBuilderNameMap = new ConcurrentHashMap<String, Set<String>>();
    private Map<String, StreamDefinition> streamDefinitionMap = new ConcurrentHashMap<String, StreamDefinition>();

    /**
     * @param eventBuilder the event builder to be registered
     */
    public void registerEventSender(EventBuilder eventBuilder) throws EventBuilderConfigurationException {
        StreamDefinition streamDefinition = eventBuilder.getExportedStreamDefinition();
        String streamId;
        if (streamDefinition != null) {
            streamId = streamDefinition.getStreamId();
        } else {
            streamId = EventBuilderUtil.getExportedStreamIdFrom(eventBuilder.getEventBuilderConfiguration());
        }
        if (!streamEventJunctionMap.containsKey(streamId)) {
            streamEventJunctionMap.put(streamId, new StreamEventJunction(streamDefinition, this));
        } else if (streamEventJunctionMap.get(streamId).getExportedStreamDefinition() != null && !streamEventJunctionMap.get(streamId).getExportedStreamDefinition().equals(streamDefinition)) {
            throw new EventBuilderConfigurationException("Stream definition already exists for the same stream ID with different attributes.");
        }
        eventBuilder.setStreamEventJunction(streamEventJunctionMap.get(streamId));
        eventBuilder.subscribeToEventAdaptor();
        if (!eventBuilderNameMap.containsKey(streamId)) {
            eventBuilderNameMap.put(streamId, new HashSet<String>());
        }
        // We add the name to the event builder map at this level and then let it go to inactive by throwing an exception
        // because of a feature associated with passthrough event builder (Passthrough event builder supports EB going inactive
        // and only becoming active at the time of receiving events). If a list is kept to keep names, this flow can put
        // duplicate event builder names. Therefore a HashedSet is used to keep event builder names.
        eventBuilderNameMap.get(streamId).add(eventBuilder.getEventBuilderConfiguration().getEventBuilderName());
        if (streamDefinition != null) {
            registerStreamDefinition(streamDefinition, eventBuilder.getTenantId());
        } else {
            throw new EventBuilderStreamValidationException("Output stream definition is not available ", streamId);
        }
    }

    public void registerStreamDefinition(StreamDefinition streamDefinition, int tenantId) {
        String streamId = streamDefinition.getStreamId();
        if (!streamDefinitionMap.containsKey(streamId)) {
            streamDefinitionMap.put(streamId, streamDefinition);
            CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
            List<EventReceiverStreamNotificationListener> streamNotificationListeners = carbonEventBuilderService.getEventReceiverStreamNotificationListeners();
            for (EventReceiverStreamNotificationListener eventReceiverStreamNotificationListener : streamNotificationListeners) {
                eventReceiverStreamNotificationListener.addedNewEventStream(tenantId, streamId);
            }
        }
    }

    public void unregisterEventSender(EventBuilder eventBuilder) {
        StreamDefinition streamDefinition = eventBuilder.getExportedStreamDefinition();
        String streamId;
        if (streamDefinition != null) {
            streamId = streamDefinition.getStreamId();
        } else {
            streamId = EventBuilderUtil.getExportedStreamIdFrom(eventBuilder.getEventBuilderConfiguration());
        }
        String eventBuilderName = eventBuilder.getEventBuilderConfiguration().getEventBuilderName();
        Set<String> eventBuilderNameList = eventBuilderNameMap.get(streamId);
        eventBuilderNameList.remove(eventBuilderName);
        if (eventBuilderNameList.isEmpty()) {
            eventBuilderNameMap.remove(streamId);
            streamEventJunctionMap.get(streamId).cleanup();
            streamEventJunctionMap.remove(streamId);
            if (streamDefinition != null) {
                unregisterStreamDefinition(streamId, eventBuilder.getTenantId());
            }
        }
    }

    public void unregisterStreamDefinition(String streamId, int tenantId) {
        if (streamDefinitionMap.containsKey(streamId)) {
            streamDefinitionMap.remove(streamId);
            CarbonEventBuilderService carbonEventBuilderService = EventBuilderServiceValueHolder.getCarbonEventBuilderService();
            List<EventReceiverStreamNotificationListener> streamNotificationListeners = carbonEventBuilderService.getEventReceiverStreamNotificationListeners();
            for (EventReceiverStreamNotificationListener eventReceiverStreamNotificationListener : streamNotificationListeners) {
                eventReceiverStreamNotificationListener.removedEventStream(tenantId, streamId);
            }
        }
    }

    public void addEventListener(String streamId, BasicEventListener basicEventListener) throws EventReceiverException {
        if (!streamEventJunctionMap.containsKey(streamId)) {
            throw new EventReceiverException("No stream definition registered for stream ID: " + streamId);
        }

        streamEventJunctionMap.get(streamId).addEventListener(basicEventListener);
    }

    public void addEventListener(String streamId, Wso2EventListener wso2EventListener) throws EventReceiverException {
        if (!streamEventJunctionMap.containsKey(streamId)) {
            throw new EventReceiverException("No stream definition registered for stream ID: " + streamId);
        }

        streamEventJunctionMap.get(streamId).addEventListener(wso2EventListener);
    }

    public void removeEventListener(String streamId, BasicEventListener basicEventListener) throws EventReceiverException {
        if (!streamEventJunctionMap.containsKey(streamId)) {
            throw new EventReceiverException("No stream definition registered for stream ID: " + streamId);
        }

        streamEventJunctionMap.get(streamId).removeEventListener(basicEventListener);
    }

    public void removeEventListener(String streamId, Wso2EventListener wso2EventListener) throws EventReceiverException {
        if (!streamEventJunctionMap.containsKey(streamId)) {
            throw new EventReceiverException("No stream definition registered for stream ID: " + streamId);
        }

        streamEventJunctionMap.get(streamId).removeEventListener(wso2EventListener);
    }

    public List<StreamDefinition> getStreamDefinitions() {
        return new ArrayList<StreamDefinition>(streamDefinitionMap.values());
    }

    public StreamDefinition getStreamDefinition(String streamName, String streamVersion) throws EventBuilderConfigurationException {
        String streamId = streamName + EventBuilderConstants.STREAM_NAME_VER_DELIMITER + streamVersion;
        StreamDefinition resultStreamDef = getStreamDefinitionFromStore(streamName, streamVersion);
        if (resultStreamDef == null) {
            resultStreamDef = streamDefinitionMap.get(streamId);
        }
        return resultStreamDef;
    }

    public StreamDefinition getStreamDefinitionFromStore(String name, String version)
            throws EventBuilderConfigurationException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserRegistry registry = EventBuilderServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(STREAM_DEFINITION_STORE + EventBuilderConstants.REGISTRY_PATH_SEPARATOR + name
                    + EventBuilderConstants.REGISTRY_PATH_SEPARATOR + version)) {
                Resource resource = registry.get(STREAM_DEFINITION_STORE + EventBuilderConstants.REGISTRY_PATH_SEPARATOR + name + EventBuilderConstants.REGISTRY_PATH_SEPARATOR + version);
                Object content = resource.getContent();
                if (content != null) {
                    return EventDefinitionConverterUtils.convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
                }
            }
            return null;
        } catch (RegistryException e) {
            throw new EventBuilderConfigurationException("Error in accessing the governance registry :" + e.getMessage(), e);
        } catch (MalformedStreamDefinitionException e) {
            throw new EventBuilderConfigurationException("Error in getting Stream Definition " + name + ":" + version, e);
        }
    }

    public boolean isStreamDefinitionRegistered(String streamId) {
        return streamDefinitionMap.get(streamId) != null;
    }
}
