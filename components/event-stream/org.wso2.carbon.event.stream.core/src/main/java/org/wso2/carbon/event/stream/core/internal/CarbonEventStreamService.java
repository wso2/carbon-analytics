/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.stream.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.event.stream.core.EventProducer;
import org.wso2.carbon.event.stream.core.EventStreamConfiguration;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.SiddhiEventConsumer;
import org.wso2.carbon.event.stream.core.WSO2EventConsumer;
import org.wso2.carbon.event.stream.core.WSO2EventListConsumer;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.event.stream.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.event.stream.core.internal.util.CarbonEventStreamUtil;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.event.stream.core.internal.util.SampleEventGenerator;
import org.wso2.carbon.event.stream.core.internal.util.helper.EventStreamConfigurationFileSystemInvoker;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventStreamService implements EventStreamService {

    private static final Log log = LogFactory.getLog(CarbonEventStreamService.class);
    private final List<StreamDefinition> pendingStreams = new ArrayList<StreamDefinition>();
    private Map<Integer, ConcurrentHashMap<String, EventStreamConfiguration>> tenantSpecificEventStreamConfigs = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, EventStreamConfiguration>>();

    public void removeEventStreamConfigurationFromMap(String fileName) throws EventStreamConfigurationException{
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventStreamConfiguration> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        String streamId = null;
        if (eventStreamConfigs != null) {
            for (EventStreamConfiguration eventStreamConfiguration : eventStreamConfigs.values()) {
                if (eventStreamConfiguration.getFileName().equals(fileName)) {
                    streamId = eventStreamConfiguration.getStreamDefinition().getStreamId();
                    break;
                }
            }
        }
        if (streamId != null) {
            eventStreamConfigs.remove(streamId);
            for (EventStreamListener eventStreamListener : EventStreamServiceValueHolder.getEventStreamListenerList()) {
                eventStreamListener.removedEventStream(tenantId,
                        DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId),
                        DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId));
            }
            EventStreamServiceValueHolder.getEventStreamRuntime().deleteStreamJunction(streamId);
        }
    }

    /**
     * Pending streams will be added
     */
    public void addPendingStreams() {
        synchronized (pendingStreams) {
            for (StreamDefinition stream : pendingStreams) {
                try {
                    addEventStreamDefinition(stream);
                } catch (EventStreamConfigurationException e) {
                    log.error("Error occurred when adding stream " + stream.getName(), e);
                }
            }
            pendingStreams.clear();
        }
    }

    /**
     * @param name
     * @param version
     * @return
     */
    @Override
    public StreamDefinition getStreamDefinition(String name, String version) throws EventStreamConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventStreamConfiguration> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if (eventStreamConfigs != null && eventStreamConfigs.containsKey(name + ":" + version)) {
            return eventStreamConfigs.get(name + ":" + version).getStreamDefinition();
        }
        return null;
    }

    /**
     * @param streamId
     * @return StreamDefinition and returns null if ont exist
     */
    @Override
    public StreamDefinition getStreamDefinition(String streamId) throws EventStreamConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventStreamConfiguration> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if (eventStreamConfigs != null && eventStreamConfigs.containsKey(streamId)) {
            return eventStreamConfigs.get(streamId).getStreamDefinition();
        }
        return null;
    }

    public EventStreamConfiguration getEventStreamConfiguration(String streamId) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventStreamConfiguration> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if (eventStreamConfigs != null && eventStreamConfigs.containsKey(streamId)) {
            return eventStreamConfigs.get(streamId);
        }
        return null;
    }

    @Override
    public List<StreamDefinition> getAllStreamDefinitions() throws EventStreamConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventStreamConfiguration> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        List<StreamDefinition> list = new ArrayList<StreamDefinition>();
        if (eventStreamConfigs == null) {
            return list;
        }
        for (EventStreamConfiguration eventStreamConfiguration : eventStreamConfigs.values()) {
            list.add(eventStreamConfiguration.getStreamDefinition());
        }
        return list;
    }

    @Override
    public List<EventStreamConfiguration> getAllEventStreamConfigurations() throws EventStreamConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventStreamConfiguration> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if (eventStreamConfigs == null) {
            return new ArrayList<EventStreamConfiguration>();
        }
        return new ArrayList<EventStreamConfiguration>(eventStreamConfigs.values());
    }

    public void addEventStreamConfig(EventStreamConfiguration eventStreamConfiguration)
            throws EventStreamConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, EventStreamConfiguration> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if (eventStreamConfigs == null) {
            eventStreamConfigs = new ConcurrentHashMap<String, EventStreamConfiguration>();
            tenantSpecificEventStreamConfigs.put(tenantId, eventStreamConfigs);
        }
        eventStreamConfigs.put(eventStreamConfiguration.getStreamDefinition().getStreamId(), eventStreamConfiguration);
        for (EventStreamListener eventStreamListener : EventStreamServiceValueHolder.getEventStreamListenerList()) {
            eventStreamListener.addedEventStream(tenantId,
                    eventStreamConfiguration.getStreamDefinition().getName(),
                    eventStreamConfiguration.getStreamDefinition().getVersion());
        }
    }

    public void validateEventStreamDefinition(StreamDefinition streamDefinition) throws EventStreamConfigurationException {
        // validate meta attribute
        if (streamDefinition.getMetaData() != null && streamDefinition.getMetaData().size() != 0) {
            for (int i = 0; i < streamDefinition.getMetaData().size(); i++) {
                String checkName = streamDefinition.getMetaData().get(i).getName();
                for (int j = i + 1; j < streamDefinition.getMetaData().size(); j++) {
                    if (checkName.equals(streamDefinition.getMetaData().get(j).getName())) {
                        throw new EventStreamConfigurationException("Cannot have same name \'" + checkName +"\' for multiple meta data attributes, give different names");
                    }
                }
            }
        }
        // validate correlation attribute
        if (streamDefinition.getCorrelationData() != null && streamDefinition.getCorrelationData().size() != 0) {
            for (int i = 0; i < streamDefinition.getCorrelationData().size(); i++) {
                String checkName = streamDefinition.getCorrelationData().get(i).getName();
                for (int j = i + 1; j < streamDefinition.getCorrelationData().size(); j++) {
                    if (checkName.equals(streamDefinition.getCorrelationData().get(j).getName())) {
                        throw new EventStreamConfigurationException("Cannot have same name \'" + checkName + "\' for multiple correlation data attributes, give different names");
                    }
                }
            }
        }
        // validate payload attribute
        if (streamDefinition.getPayloadData() != null && streamDefinition.getPayloadData().size() != 0) {
            for (int i = 0; i < streamDefinition.getPayloadData().size(); i++) {
                String checkName = streamDefinition.getPayloadData().get(i).getName();
                for (int j = i + 1; j < streamDefinition.getPayloadData().size(); j++) {
                    if (checkName.equals(streamDefinition.getPayloadData().get(j).getName())) {
                        throw new EventStreamConfigurationException("Cannot have same name \'" + checkName +"\' for multiple payload data attributes, give different names");
                    }
                }
            }
        }
    }

    @Override
    public void addEventStreamDefinition(StreamDefinition streamDefinition) throws
            EventStreamConfigurationException {

        //If ConfigurationContextService is available stream will be saved,
        //Else stream will be added pendingStreams list to save once ConfigurationContextService is available
        if (EventStreamServiceValueHolder.getConfigurationContextService() != null) {
            AxisConfiguration axisConfig = getAxisConfiguration();
            String directoryPath = new File(axisConfig.getRepository().getPath())
                    .getAbsolutePath() + File.separator + EventStreamConstants.EVENT_STREAMS;
            File directory = new File(directoryPath);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new EventStreamConfigurationException("Cannot create directory to add tenant specific Event Stream : "
                            + streamDefinition.getStreamId());
                }
            }
            String filePath = directoryPath + File.separator +
                    streamDefinition.getName() + EventStreamConstants.STREAM_DEFINITION_FILE_DELIMITER
                    + streamDefinition.getVersion() + EventStreamConstants.STREAM_DEFINITION_FILE_EXTENSION;
            StreamDefinition streamDefinitionOld = getStreamDefinition(streamDefinition.getStreamId());
            if (streamDefinitionOld != null) {
                if (!(streamDefinitionOld.equals(streamDefinition))) {
                    throw new StreamDefinitionAlreadyDefinedException("Different stream definition with same stream id "
                            + streamDefinition.getStreamId() + " already exist " + streamDefinitionOld.toString()
                            + ", cannot add stream definition " + streamDefinition.toString());
                } else {
                    return;
                }
            }
            validateEventStreamDefinition(streamDefinition);
            EventStreamConfigurationFileSystemInvoker.save(streamDefinition, filePath, axisConfig);
        } else {
            synchronized (pendingStreams) {
                if (EventStreamServiceValueHolder.getConfigurationContextService() != null) {
                    addEventStreamDefinition(streamDefinition);
                } else {
                    pendingStreams.add(streamDefinition);
                }
            }
        }
    }


    private AxisConfiguration getAxisConfiguration() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        AxisConfiguration axisConfig;
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfig = EventStreamServiceValueHolder.getConfigurationContextService().getServerConfigContext().getAxisConfiguration();
        } else {
            axisConfig = TenantAxisUtils.getTenantAxisConfiguration(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(), EventStreamServiceValueHolder.getConfigurationContextService().getServerConfigContext());
        }
        return axisConfig;
    }

    public void removeEventStreamDefinition(String streamId) throws EventStreamConfigurationException {
        String name = null, version = null;
        if (streamId != null && streamId.contains(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)) {
            name = streamId.split(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)[0];
            version = streamId.split(StreamdefinitionStoreConstants.STREAM_ID_SPLITTER)[1];
        }
        removeEventStreamDefinition(name, version);
    }

    @Override
    public void removeEventStreamDefinition(String streamName, String streamVersion)
            throws EventStreamConfigurationException {
        AxisConfiguration axisConfig = getAxisConfiguration();
        EventStreamConfigurationFileSystemInvoker.delete(streamName + EventStreamConstants.STREAM_DEFINITION_FILE_DELIMITER
                + streamVersion + EventStreamConstants.STREAM_DEFINITION_FILE_EXTENSION, axisConfig);

        log.info("Stream definition - " + streamName + EventStreamConstants.STREAM_DEFINITION_DELIMITER + streamVersion
                + " removed successfully");
    }

    @Override
    public List<String> getStreamIds() throws EventStreamConfigurationException {
        Collection<StreamDefinition> eventStreamConfigs = getAllStreamDefinitions();
        List<String> streamDefinitionsIds = new ArrayList<String>(eventStreamConfigs.size());
        for (StreamDefinition streamDefinition : eventStreamConfigs) {
            streamDefinitionsIds.add(streamDefinition.getStreamId());
        }

        return streamDefinitionsIds;
    }


    @Override
    public String generateSampleEvent(String streamId, String eventType)
            throws EventStreamConfigurationException {

        StreamDefinition streamDefinition = getStreamDefinition(streamId);

        if (eventType.equals(EventStreamConstants.XML_EVENT)) {
            return SampleEventGenerator.generateXMLEvent(streamDefinition);
        } else if (eventType.equals(EventStreamConstants.JSON_EVENT)) {
            return SampleEventGenerator.generateJSONEvent(streamDefinition);
        } else if (eventType.equals(EventStreamConstants.TEXT_EVENT)) {
            return SampleEventGenerator.generateTextEvent(streamDefinition);
        }
        return null;
    }

    @Override
    public void subscribe(SiddhiEventConsumer siddhiEventConsumer) throws EventStreamConfigurationException {
        EventStreamServiceValueHolder.getEventStreamRuntime().subscribe(siddhiEventConsumer);
    }

    @Override
    public void subscribe(EventProducer eventProducer) throws EventStreamConfigurationException {
        EventStreamServiceValueHolder.getEventStreamRuntime().subscribe(eventProducer);
    }

    @Override
    public void subscribe(WSO2EventConsumer wso2EventConsumer) throws EventStreamConfigurationException {
        EventStreamServiceValueHolder.getEventStreamRuntime().subscribe(wso2EventConsumer);
    }

    @Override
    public void subscribe(WSO2EventListConsumer wso2EventListConsumer) throws EventStreamConfigurationException {
        EventStreamServiceValueHolder.getEventStreamRuntime().subscribe(wso2EventListConsumer);
    }

    @Override
    public void unsubscribe(SiddhiEventConsumer siddhiEventConsumer) {
        EventStreamServiceValueHolder.getEventStreamRuntime().unsubscribe(siddhiEventConsumer);
    }

    @Override
    public void unsubscribe(EventProducer eventProducer) {
        EventStreamServiceValueHolder.getEventStreamRuntime().unsubscribe(eventProducer);
    }

    @Override
    public void unsubscribe(WSO2EventConsumer wso2EventConsumer) {
        EventStreamServiceValueHolder.getEventStreamRuntime().unsubscribe(wso2EventConsumer);
    }

    @Override
    public void unsubscribe(WSO2EventListConsumer wso2EventConsumer) {
        EventStreamServiceValueHolder.getEventStreamRuntime().unsubscribe(wso2EventConsumer);
    }

    @Override
    public void publish(Event event) {
        EventStreamServiceValueHolder.getEventStreamRuntime().publish(event.getStreamId(), event);
    }


    public boolean isEventStreamFileExists(String eventStreamFileName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, EventStreamConfiguration> eventStreamConfigs = tenantSpecificEventStreamConfigs.get(tenantId);
        if (eventStreamConfigs != null) {
            for (EventStreamConfiguration eventStreamConfiguration : eventStreamConfigs.values()) {
                if (eventStreamConfiguration.getFileName().equals(eventStreamFileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isMatchForStreamDefinition(StreamDefinition streamDefinition, StreamDefinition existingStreamDefinition) {
        if (streamDefinition == null || existingStreamDefinition == null) {
            throw new IllegalArgumentException("Stream definitions passed in cannot be null!");
        }
        List<Attribute> existingStreamMetaData = existingStreamDefinition.getMetaData();
        List<Attribute> incomingStreamMetaData = streamDefinition.getMetaData();
        if (existingStreamMetaData != null && incomingStreamMetaData != null) {
            if (incomingStreamMetaData.size() != existingStreamMetaData.size()) {
                return false;
            }
            for (int i = 0; i < existingStreamMetaData.size(); i++) {
                Attribute attribute = existingStreamMetaData.get(i);
                if (incomingStreamMetaData.get(i) == null || !incomingStreamMetaData.get(i).equals(attribute)) {
                    return false;
                }
            }
        }
        List<Attribute> existingStreamCorrelationData = existingStreamDefinition.getCorrelationData();
        List<Attribute> incomingStreamCorrelationData = streamDefinition.getCorrelationData();
        if (existingStreamCorrelationData != null && incomingStreamCorrelationData != null) {

            if (incomingStreamCorrelationData.size() != existingStreamCorrelationData.size()) {
                return false;
            }
            for (int i = 0; i < existingStreamCorrelationData.size(); i++) {
                Attribute attribute = existingStreamCorrelationData.get(i);
                if (incomingStreamCorrelationData.get(i) == null || !incomingStreamCorrelationData.get(i).equals(attribute)) {
                    return false;
                }
            }
        }
        List<Attribute> existingStreamPayloadData = existingStreamDefinition.getPayloadData();
        List<Attribute> incomingStreamPayloadData = streamDefinition.getPayloadData();
        if (existingStreamPayloadData != null && incomingStreamPayloadData != null) {
            if (incomingStreamPayloadData.size() != existingStreamPayloadData.size()) {
                return false;
            }
            for (int i = 0; i < existingStreamPayloadData.size(); i++) {
                Attribute attribute = existingStreamPayloadData.get(i);
                if (incomingStreamPayloadData.get(i) == null || !incomingStreamPayloadData.get(i).equals(attribute)) {
                    return false;
                }
            }
        }
        return true;
    }
}
