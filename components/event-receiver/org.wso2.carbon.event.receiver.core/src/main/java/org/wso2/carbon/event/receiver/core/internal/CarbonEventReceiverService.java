/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.receiver.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.input.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterSchema;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterService;
import org.wso2.carbon.event.input.adapter.core.Property;
import org.wso2.carbon.event.receiver.core.EventReceiverService;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConfigurationBuilder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationFileSystemInvoker;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationHelper;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CarbonEventReceiverService implements EventReceiverService {

    private static final Log log = LogFactory.getLog(CarbonEventReceiverService.class);
    private Map<Integer, Map<String, EventReceiver>> tenantSpecificEventReceiverConfigurationMap;
    private Map<Integer, List<EventReceiverConfigurationFile>> tenantSpecificEventReceiverConfigurationFileMap;

    public CarbonEventReceiverService() {
        tenantSpecificEventReceiverConfigurationMap = new ConcurrentHashMap<Integer, Map<String, EventReceiver>>();
        tenantSpecificEventReceiverConfigurationFileMap = new ConcurrentHashMap<Integer, List<EventReceiverConfigurationFile>>();
    }

    @Override
    public void deployEventReceiverConfiguration(EventReceiverConfiguration eventReceiverConfiguration)
            throws EventReceiverConfigurationException {

        String eventReceiverName = eventReceiverConfiguration.getEventReceiverName();

        OMElement omElement = EventReceiverConfigurationBuilder.eventReceiverConfigurationToOM(eventReceiverConfiguration);
        EventReceiverConfigurationHelper.validateEventReceiverConfiguration(omElement);
        String mappingType = EventReceiverConfigurationHelper.getInputMappingType(omElement);
        if (mappingType != null) {
            String repoPath = EventAdapterUtil.getAxisConfiguration().getRepository().getPath();
            EventReceiverUtil.generateFilePath(eventReceiverName, repoPath);
            validateToRemoveInactiveEventReceiverConfiguration(eventReceiverConfiguration.getEventReceiverName());
            EventReceiverConfigurationFileSystemInvoker.encryptAndSave(omElement, eventReceiverName + EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT);
        } else {
            throw new EventReceiverConfigurationException("Mapping type of the Event Receiver " + eventReceiverConfiguration.getEventReceiverName() + " cannot be null");
        }

    }

    @Override
    public void deployEventReceiverConfiguration(String eventReceiverConfigXml)
            throws EventReceiverConfigurationException {
        OMElement omElement;
        try {
            omElement = AXIOMUtil.stringToOM(eventReceiverConfigXml);
        } catch (XMLStreamException e) {
            throw new EventReceiverConfigurationException("Error parsing XML configuration of event receiver.");
        }
        EventReceiverConfigurationHelper.validateEventReceiverConfiguration(omElement);
        String eventReceiverName = EventReceiverConfigurationHelper.getEventReceiverName(omElement);
        String mappingType = EventReceiverConfigurationHelper.getInputMappingType(omElement);
        if (mappingType != null) {
            String repoPath = EventAdapterUtil.getAxisConfiguration().getRepository().getPath();
            EventReceiverUtil.generateFilePath(eventReceiverName, repoPath);
            validateToRemoveInactiveEventReceiverConfiguration(eventReceiverName);
            EventReceiverConfigurationFileSystemInvoker.encryptAndSave(omElement, eventReceiverName + EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT);
        } else {
            throw new EventReceiverConfigurationException("Mapping type of the Event Receiver " + eventReceiverName + " cannot be null");
        }

    }


    @Override
    public void undeployActiveEventReceiverConfiguration(String eventReceiverName)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(eventReceiverName);
        if (fileName != null) {
            EventReceiverConfigurationFileSystemInvoker.delete(fileName);
        } else {
            throw new EventReceiverConfigurationException("Couldn't undeploy the Event Receiver configuration: " + eventReceiverName);
        }

    }

    @Override
    public void undeployInactiveEventReceiverConfiguration(String filename)
            throws EventReceiverConfigurationException {
        validateFilePath(filename);
        EventReceiverConfigurationFileSystemInvoker.delete(filename);

    }

    @Override
    public void editInactiveEventReceiverConfiguration(
            String eventReceiverConfiguration,
            String filename)
            throws EventReceiverConfigurationException {

        validateFilePath(filename);
        editEventReceiverConfiguration(filename, eventReceiverConfiguration, null);
    }

    @Override
    public void editActiveEventReceiverConfiguration(String eventReceiverConfiguration,
                                                     String eventReceiverName)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(eventReceiverName);
        if (fileName == null) {
            fileName = eventReceiverName + EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT;
        }
        editEventReceiverConfiguration(fileName, eventReceiverConfiguration, eventReceiverName);

    }

    @Override
    public EventReceiverConfiguration getActiveEventReceiverConfiguration(String eventReceiverName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventReceiverConfiguration eventReceiverConfiguration = null;
        Map<String, EventReceiver> tenantSpecificEventReceiverMap = this.tenantSpecificEventReceiverConfigurationMap.get(tenantId);
        if (tenantSpecificEventReceiverMap != null && tenantSpecificEventReceiverMap.size() > 0) {
            EventReceiver eventReceiver = tenantSpecificEventReceiverMap.get(eventReceiverName);
            if (eventReceiver != null) {
                eventReceiverConfiguration = eventReceiver.getEventReceiverConfiguration();
            }
        }
        return eventReceiverConfiguration;
    }

    @Override
    public List<EventReceiverConfiguration> getAllActiveEventReceiverConfigurations() {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventReceiverConfiguration> eventReceiverConfigurations = new ArrayList<EventReceiverConfiguration>();
        Map<String, EventReceiver> tenantSpecificEventReceiverMap = this.tenantSpecificEventReceiverConfigurationMap.get(tenantId);
        if (tenantSpecificEventReceiverMap != null) {
            for (EventReceiver eventReceiver : tenantSpecificEventReceiverMap.values()) {
                eventReceiverConfigurations.add(eventReceiver.getEventReceiverConfiguration());
            }
        }
        return eventReceiverConfigurations;
    }

    @Override
    public List<EventReceiverConfiguration> getAllActiveEventReceiverConfigurations(
            String streamId) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventReceiverConfiguration> eventReceiverConfigurations = new ArrayList<EventReceiverConfiguration>();
        Map<String, EventReceiver> tenantSpecificEventReceiverMap = this.tenantSpecificEventReceiverConfigurationMap.get(tenantId);
        if (tenantSpecificEventReceiverMap != null) {
            for (EventReceiver eventReceiver : tenantSpecificEventReceiverMap.values()) {
                String streamWithVersion = eventReceiver.getExportedStreamDefinition().getStreamId();
                if (streamWithVersion.equals(streamId)) {
                    eventReceiverConfigurations.add(eventReceiver.getEventReceiverConfiguration());
                }
            }
        }
        return eventReceiverConfigurations;
    }

    @Override
    public List<EventReceiverConfigurationFile> getAllInactiveEventReceiverConfigurations() {
        List<EventReceiverConfigurationFile> undeployedEventReceiverConfigurationFileList = new ArrayList<EventReceiverConfigurationFile>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = this.tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
        if (eventReceiverConfigurationFiles != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                if (eventReceiverConfigurationFile.getStatus() != EventReceiverConfigurationFile.Status.DEPLOYED) {
                    undeployedEventReceiverConfigurationFileList.add(eventReceiverConfigurationFile);
                }
            }
        }
        return undeployedEventReceiverConfigurationFileList;
    }

    @Override
    public String getInactiveEventReceiverConfigurationContent(String fileName)
            throws EventReceiverConfigurationException {
        validateFilePath(fileName);
        return EventReceiverConfigurationFileSystemInvoker.readEventReceiverConfigurationFile(fileName);
    }

    @Override
    public String getActiveEventReceiverConfigurationContent(String eventReceiverName)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(eventReceiverName);
        return EventReceiverConfigurationFileSystemInvoker.readEventReceiverConfigurationFile(fileName);

    }

    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventReceiverConfigurationException {

        List<String> streamList = new ArrayList<String>();
        EventStreamService eventStreamService = EventReceiverServiceValueHolder.getEventStreamService();
        Collection<StreamDefinition> eventStreamDefinitionList;
        try {
            eventStreamDefinitionList = eventStreamService.getAllStreamDefinitions();
            if (eventStreamDefinitionList != null) {
                for (StreamDefinition streamDefinition : eventStreamDefinitionList) {
                    streamList.add(streamDefinition.getStreamId());
                }
            }

        } catch (EventStreamConfigurationException e) {
            throw new EventReceiverConfigurationException("Error while retrieving stream definition from store", e);
        }

        return streamList;
    }

    @Override
    public void setStatisticsEnabled(String eventReceiverName, boolean statisticsEnabled)
            throws EventReceiverConfigurationException {
        EventReceiverConfiguration eventReceiverConfiguration = getActiveEventReceiverConfiguration(eventReceiverName);
        eventReceiverConfiguration.setStatisticsEnabled(statisticsEnabled);
        editTracingStatistics(eventReceiverConfiguration, eventReceiverName);
    }

    @Override
    public void setTraceEnabled(String eventReceiverName, boolean traceEnabled)
            throws EventReceiverConfigurationException {
        EventReceiverConfiguration eventReceiverConfiguration = getActiveEventReceiverConfiguration(eventReceiverName);
        eventReceiverConfiguration.setTraceEnabled(traceEnabled);
        editTracingStatistics(eventReceiverConfiguration, eventReceiverName);
    }

    @Override
    public String getEventReceiverStatusAsString(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList = tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
        if (eventReceiverConfigurationFileList != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFileList) {
                if (filename != null && filename.equals(eventReceiverConfigurationFile.getFileName())) {
                    String statusMsg = eventReceiverConfigurationFile.getDeploymentStatusMessage();
                    if (eventReceiverConfigurationFile.getDependency() != null) {
                        statusMsg = statusMsg + " [Dependency: " + eventReceiverConfigurationFile.getDependency() + "]";
                    }
                    return statusMsg;
                }
            }
        }
        return EventReceiverConstants.NO_DEPENDENCY_INFO_MSG;
    }

    //Non-Interface public methods

    public void addEventReceiverConfigurationFile(EventReceiverConfigurationFile eventReceiverConfigurationFile, int tenantId) {
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
        if (eventReceiverConfigurationFiles == null) {
            eventReceiverConfigurationFiles = new CopyOnWriteArrayList<>();
        } else {
            for (EventReceiverConfigurationFile anEventReceiverConfigurationFileList : eventReceiverConfigurationFiles) {
                if (anEventReceiverConfigurationFileList.getFileName().equals(eventReceiverConfigurationFile.getFileName())) {
                    return;
                }
            }
        }
        eventReceiverConfigurationFiles.add(eventReceiverConfigurationFile);
        tenantSpecificEventReceiverConfigurationFileMap.put(tenantId, eventReceiverConfigurationFiles);
    }

    public void addEventReceiverConfiguration(EventReceiverConfiguration eventReceiverConfiguration)
            throws EventReceiverConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        // Start: Checking preconditions to add the event receiver
        StreamDefinition exportedStreamDefinition = null;
        try {
            exportedStreamDefinition = EventReceiverServiceValueHolder.getEventStreamService().getStreamDefinition(
                    eventReceiverConfiguration.getToStreamName(), eventReceiverConfiguration.getToStreamVersion());
        } catch (EventStreamConfigurationException e) {
            throw new EventReceiverConfigurationException("Error while retrieving stream definition for stream " + eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion() + " from store", e);
        }

        if (exportedStreamDefinition == null) {
            throw new EventReceiverStreamValidationException("Stream " + eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion() + " does not exist",
                    eventReceiverConfiguration.getToStreamName() + ":" + eventReceiverConfiguration.getToStreamVersion()
            );
        }

        Map<String, EventReceiver> eventReceiverConfigurationMap
                = tenantSpecificEventReceiverConfigurationMap.get(tenantId);
        if (eventReceiverConfigurationMap == null) {
            eventReceiverConfigurationMap = new ConcurrentHashMap<String, EventReceiver>();
        }

        // End; Checking preconditions to add the event receiver
        EventReceiver eventReceiver = new EventReceiver(eventReceiverConfiguration, exportedStreamDefinition,
                EventReceiverServiceValueHolder.getEventManagementService().getManagementModeInfo().getMode());

        try {
            EventReceiverServiceValueHolder.getEventStreamService().subscribe(eventReceiver);
        } catch (EventStreamConfigurationException e) {
            //ignored as this is already checked
        }

        eventReceiverConfigurationMap.put(eventReceiverConfiguration.getEventReceiverName(), eventReceiver);
        tenantSpecificEventReceiverConfigurationMap.put(tenantId, eventReceiverConfigurationMap);
    }

    public void removeEventReceiverConfigurationFile(String fileName)
            throws EventReceiverConfigurationException {
        validateFilePath(fileName);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFileList =
                tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
        if (eventReceiverConfigurationFileList != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFileList) {
                if ((eventReceiverConfigurationFile.getFileName().equals(fileName))) {
                    if (eventReceiverConfigurationFile.getStatus().
                            equals(EventReceiverConfigurationFile.Status.DEPLOYED)) {
                        String eventReceiverName = eventReceiverConfigurationFile.getEventReceiverName();
                        EventReceiver eventReceiver = tenantSpecificEventReceiverConfigurationMap.get(tenantId).remove(eventReceiverName);
                        if (eventReceiver != null) {
                            EventReceiverServiceValueHolder.getEventStreamService().unsubscribe(eventReceiver);
                            eventReceiver.destroy();
                        }
                    }
                    eventReceiverConfigurationFileList.remove(eventReceiverConfigurationFile);
                    return;
                }
            }
        }

    }

    public void activateInactiveEventReceiverConfigurationsForAdapter(String eventAdapterName)
            throws EventReceiverConfigurationException {
        List<EventReceiverConfigurationFile> fileList = new ArrayList<EventReceiverConfigurationFile>();

        if (tenantSpecificEventReceiverConfigurationFileMap != null && tenantSpecificEventReceiverConfigurationFileMap.size() > 0) {
            for (List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles : tenantSpecificEventReceiverConfigurationFileMap.values()) {
                if (eventReceiverConfigurationFiles != null) {
                    for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                        if ((eventReceiverConfigurationFile.getStatus().equals(EventReceiverConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && eventReceiverConfigurationFile.getDependency().equalsIgnoreCase(eventAdapterName)) {
                            fileList.add(eventReceiverConfigurationFile);
                        }
                    }
                }
            }
        }
        for (EventReceiverConfigurationFile receiverConfigurationFile : fileList) {
            try {
                EventReceiverConfigurationFileSystemInvoker.reload(receiverConfigurationFile);
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event Receiver configuration file: " + receiverConfigurationFile.getFileName(), e);
            }
        }
    }

    public void activateInactiveEventReceiverConfigurationsForStream(String streamId,
                                                                     int tenantId) throws EventReceiverConfigurationException {
        List<EventReceiverConfigurationFile> fileList = new ArrayList<EventReceiverConfigurationFile>();

        if (tenantSpecificEventReceiverConfigurationFileMap != null && tenantSpecificEventReceiverConfigurationFileMap.size() > 0) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);

            if (eventReceiverConfigurationFiles != null) {
                for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                    if (EventReceiverConfigurationFile.Status.WAITING_FOR_STREAM_DEPENDENCY.equals(eventReceiverConfigurationFile.getStatus())
                            && streamId.equalsIgnoreCase(eventReceiverConfigurationFile.getDependency())) {
                        fileList.add(eventReceiverConfigurationFile);
                    }
                }
            }
        }
        for (EventReceiverConfigurationFile receiverConfigurationFile : fileList) {
            try {
                EventReceiverConfigurationFileSystemInvoker.reload(receiverConfigurationFile);
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event Receiver configuration file: " + receiverConfigurationFile.getFileName(), e);
            }
        }
    }

    public void deactivateActiveEventReceiverConfigurationsForAdapter(
            String dependency)
            throws EventReceiverConfigurationException {

        InputEventAdapterService eventAdapterService = EventReceiverServiceValueHolder.getInputEventAdapterService();

        List<EventReceiverConfigurationFile> fileList = new ArrayList<EventReceiverConfigurationFile>();
        if (tenantSpecificEventReceiverConfigurationMap != null && tenantSpecificEventReceiverConfigurationMap.size() > 0) {
            for (Map.Entry<Integer, Map<String, EventReceiver>> eventPublisherMapEntry : tenantSpecificEventReceiverConfigurationMap.entrySet()) {
                if (eventPublisherMapEntry.getValue() != null) {
                    int tenantId = eventPublisherMapEntry.getKey();
                    for (EventReceiver eventReceiver : eventPublisherMapEntry.getValue().values()) {
                        String eventAdapterType = eventReceiver.getEventReceiverConfiguration().getFromAdapterConfiguration().getType();
                        if (eventAdapterType.equals(dependency)) {
                            EventReceiverConfigurationFile receiverConfigurationFile = getEventReceiverConfigurationFile(eventReceiver.getEventReceiverConfiguration().getEventReceiverName(), tenantId);
                            if (receiverConfigurationFile != null) {
                                fileList.add(receiverConfigurationFile);
                                eventAdapterService.destroy(eventReceiver.getEventReceiverConfiguration().getFromAdapterConfiguration().getName());
                            }
                        }
                    }
                }
            }
        }

        for (EventReceiverConfigurationFile receiverConfigurationFile : fileList) {
            EventReceiverConfigurationFileSystemInvoker.reload(receiverConfigurationFile);
            log.info("Event receiver : " + receiverConfigurationFile.getEventReceiverName() + " in inactive state because dependency could not be found: " + dependency);
        }
    }

    public void deactivateActiveEventReceiverConfigurationsForStream(String streamId, int tenantId)
            throws EventReceiverConfigurationException {

        InputEventAdapterService eventAdapterService = EventReceiverServiceValueHolder.getInputEventAdapterService();

        List<EventReceiverConfigurationFile> fileList = new ArrayList<EventReceiverConfigurationFile>();
        if (tenantSpecificEventReceiverConfigurationMap != null && tenantSpecificEventReceiverConfigurationMap.size() > 0) {
            Map<String, EventReceiver> eventReceiverMap = tenantSpecificEventReceiverConfigurationMap.get(tenantId);
            if (eventReceiverMap != null) {
                for (EventReceiver eventReceiver : eventReceiverMap.values()) {
                    EventReceiverConfiguration eventReceiverConfiguration = eventReceiver.getEventReceiverConfiguration();
                    String stream = EventReceiverUtil.getExportedStreamIdFrom(eventReceiverConfiguration);
                    if (streamId.equals(stream)) {
                        EventReceiverConfigurationFile receiverConfigurationFile =
                                getEventReceiverConfigurationFile(eventReceiverConfiguration.getEventReceiverName(), tenantId);
                        if (receiverConfigurationFile != null) {
                            fileList.add(receiverConfigurationFile);
                            eventAdapterService.destroy(eventReceiverConfiguration.getFromAdapterConfiguration().getName());
                        }
                    }
                }
            }
        }
        for (EventReceiverConfigurationFile receiverConfigurationFile : fileList) {
            EventReceiverConfigurationFileSystemInvoker.reload(receiverConfigurationFile);
            log.info("Event receiver : " + receiverConfigurationFile.getEventReceiverName() + " in inactive state because event stream dependency  could not be found: " + streamId);
        }
    }

    public boolean isEventReceiverAlreadyExists(int tenantId, String eventReceiverName) {

        if (tenantSpecificEventReceiverConfigurationFileMap.size() > 0) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
            if (eventReceiverConfigurationFiles != null) {
                for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                    if ((eventReceiverConfigurationFile.getEventReceiverName().equals(eventReceiverName))
                            && (eventReceiverConfigurationFile.getStatus().equals(EventReceiverConfigurationFile.Status.DEPLOYED))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isEventReceiverFileAlreadyExist(String eventReceiverFileName, int tenantId) {
        if (tenantSpecificEventReceiverConfigurationFileMap.size() > 0) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
            if (eventReceiverConfigurationFiles != null) {
                for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                    if ((eventReceiverConfigurationFile.getFileName().equals(eventReceiverFileName))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    //Private Methods are below

    private void editTracingStatistics(
            EventReceiverConfiguration eventReceiverConfiguration,
            String eventReceiverName)
            throws EventReceiverConfigurationException {

        String fileName = getFileName(eventReceiverName);
        undeployActiveEventReceiverConfiguration(eventReceiverName);
        OMElement omElement = EventReceiverConfigurationBuilder.eventReceiverConfigurationToOM(eventReceiverConfiguration);
        EventReceiverConfigurationFileSystemInvoker.delete(fileName);
        EventReceiverConfigurationFileSystemInvoker.encryptAndSave(omElement, fileName);
    }

    private String getFileName(String eventReceiverName) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventReceiverConfigurationFileMap.size() > 0) {
            List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
            if (eventReceiverConfigurationFiles != null) {
                for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                    if ((eventReceiverConfigurationFile.getEventReceiverName().equals(eventReceiverName))
                            && eventReceiverConfigurationFile.getStatus().equals(EventReceiverConfigurationFile.Status.DEPLOYED)) {
                        return new File(eventReceiverConfigurationFile.getFileName()).getName();
                    }
                }
            }
        }
        return null;
    }

    private void editEventReceiverConfiguration(String filename,
                                                String eventReceiverConfigurationXml,
                                                String originalEventReceiverName)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventReceiverConfigurationXml);
            omElement.build();
            EventReceiverConfigurationHelper.validateEventReceiverConfiguration(omElement);
            String mappingType = EventReceiverConfigurationHelper.getInputMappingType(omElement);
            if (mappingType != null) {
                EventReceiverConfiguration eventReceiverConfigurationObject = EventReceiverConfigurationBuilder.getEventReceiverConfiguration(omElement, mappingType, true, tenantId);
                if (!(eventReceiverConfigurationObject.getEventReceiverName().equals(originalEventReceiverName))) {
                    if (!isEventReceiverAlreadyExists(tenantId, eventReceiverConfigurationObject.getEventReceiverName())) {
                        EventReceiverConfigurationFileSystemInvoker.delete(filename);
                        EventReceiverConfigurationFileSystemInvoker.encryptAndSave(omElement, filename);
                    } else {
                        throw new EventReceiverConfigurationException("There is already a Event Receiver " + eventReceiverConfigurationObject.getEventReceiverName() + " with the same name");
                    }
                } else {
                    EventReceiverConfigurationFileSystemInvoker.delete(filename);
                    EventReceiverConfigurationFileSystemInvoker.encryptAndSave(omElement, filename);
                }
            } else {
                throw new EventReceiverConfigurationException("Mapping type of the Event Receiver " + originalEventReceiverName + " cannot be null");
            }
        } catch (XMLStreamException e) {
            throw new EventReceiverConfigurationException("Error while building XML configuration: " + e.getMessage(), e);
        }
    }

    private EventReceiverConfigurationFile getEventReceiverConfigurationFile(
            String eventReceiverName, int tenantId) {
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
        if (eventReceiverConfigurationFiles != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                if (eventReceiverConfigurationFile.getEventReceiverName().equals(eventReceiverName)) {
                    return eventReceiverConfigurationFile;
                }
            }
        }
        return null;

    }

    private void validateToRemoveInactiveEventReceiverConfiguration(String eventReceiverName)
            throws EventReceiverConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = eventReceiverName + EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<EventReceiverConfigurationFile> eventReceiverConfigurationFiles = tenantSpecificEventReceiverConfigurationFileMap.get(tenantId);
        if (eventReceiverConfigurationFiles != null) {
            for (EventReceiverConfigurationFile eventReceiverConfigurationFile : eventReceiverConfigurationFiles) {
                if ((eventReceiverConfigurationFile.getFileName().equals(fileName))) {
                    if (!(eventReceiverConfigurationFile.getStatus().equals(EventReceiverConfigurationFile.Status.DEPLOYED))) {
                        EventReceiverConfigurationFileSystemInvoker.delete(fileName);
                        break;
                    }
                }
            }
        }

    }

    public List<String> getEncryptedProperties(String eventAdaptorType) {
        List<String> encryptedProperties = new ArrayList<String>(1);
        //OutputEventAdapterDto dto = OutputEventAdaptorHolder.getInstance().getOutputEventAdaptorService().getEventAdaptorDto(eventAdaptorType);
        InputEventAdapterSchema inputEventAdapterSchema = EventReceiverServiceValueHolder.getInputEventAdapterService().getInputEventAdapterSchema(eventAdaptorType);
        if (inputEventAdapterSchema != null) {
            List<Property> properties = inputEventAdapterSchema.getPropertyList();
            if (properties != null) {
                for (Property prop : properties) {
                    if (prop.isEncrypted()) {
                        encryptedProperties.add(prop.getPropertyName());
                    }
                }
            }
        }
        return encryptedProperties;
    }

    public Map<Integer, Map<String, EventReceiver>> getTenantSpecificEventReceiverMap() {
        return tenantSpecificEventReceiverConfigurationMap;
    }

    private void validateFilePath(String file) throws EventReceiverConfigurationException {
        if (file.contains("../") || file.contains("..\\")) {
            throw new EventReceiverConfigurationException("File name contains restricted path elements. " + file);
        }
    }

    public void start() {
        EventReceiverServiceValueHolder.getInputEventAdapterService().start();
    }

    public void startPolling() {
        EventReceiverServiceValueHolder.getInputEventAdapterService().startPolling();
    }
}
