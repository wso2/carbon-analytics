package org.wso2.carbon.event.formatter.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.formatter.core.EventFormatterService;
import org.wso2.carbon.event.formatter.core.config.EventFormatter;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterConfigurationFile;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterUtil;
import org.wso2.carbon.event.formatter.core.internal.util.FormatterConfigurationBuilder;
import org.wso2.carbon.event.formatter.core.internal.util.helper.EventFormatterConfigurationFilesystemInvoker;
import org.wso2.carbon.event.formatter.core.internal.util.helper.EventFormatterConfigurationHelper;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughSenderConfigurator;
import org.wso2.carbon.event.processor.api.send.EventProducer;
import org.wso2.carbon.event.processor.api.send.exception.EventProducerException;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CarbonEventFormatterService
        implements EventFormatterService, PassthroughSenderConfigurator {

    private static final Log log = LogFactory.getLog(CarbonEventFormatterService.class);
    private Map<Integer, Map<String, EventFormatter>> tenantSpecificEventFormatterConfigurationMap;
    private Map<Integer, List<EventFormatterConfigurationFile>> eventFormatterConfigurationFileMap;


    public CarbonEventFormatterService() {
        tenantSpecificEventFormatterConfigurationMap = new ConcurrentHashMap<Integer, Map<String, EventFormatter>>();
        eventFormatterConfigurationFileMap = new ConcurrentHashMap<Integer, List<EventFormatterConfigurationFile>>();
    }

    @Override
    public void deployEventFormatterConfiguration(
            EventFormatterConfiguration eventFormatterConfiguration,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        String eventFormatterName = eventFormatterConfiguration.getEventFormatterName();

        OMElement omElement = FormatterConfigurationBuilder.eventFormatterConfigurationToOM(eventFormatterConfiguration);
        EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
        if (EventFormatterConfigurationHelper.getOutputMappingType(omElement) != null) {

            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new EventFormatterConfigurationException("Cannot create directory to add tenant specific Event Formatter : " + eventFormatterName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + EventFormatterConstants.TM_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new EventFormatterConfigurationException("Cannot create directory " + EventFormatterConstants.TM_ELE_DIRECTORY + " to add tenant specific event adaptor :" + eventFormatterName);
                }
            }
            validateToRemoveInactiveEventFormatterConfiguration(eventFormatterName, axisConfiguration);
            EventFormatterConfigurationFilesystemInvoker.save(omElement, eventFormatterName + ".xml", axisConfiguration);

        } else {
            throw new EventFormatterConfigurationException("Mapping type of the Event Formatter " + eventFormatterName + " cannot be null");
        }

    }

    @Override
    public void undeployActiveEventFormatterConfiguration(String eventFormatterName,
                                                          AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventFormatterName);
        if (fileName != null) {
            EventFormatterConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
        } else {
            throw new EventFormatterConfigurationException("Couldn't undeploy the Event Formatter configuration : " + eventFormatterName);
        }

    }

    @Override
    public void undeployInactiveEventFormatterConfiguration(String filename,
                                                            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        EventFormatterConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
    }

    @Override
    public void editInactiveEventFormatterConfiguration(
            String eventFormatterConfiguration,
            String filename,
            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        editEventFormatterConfiguration(filename, axisConfiguration, eventFormatterConfiguration, null);
    }

    @Override
    public void editActiveEventFormatterConfiguration(String eventFormatterConfiguration,
                                                      String eventFormatterName,
                                                      AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventFormatterName);
        if (fileName == null) {
            fileName = eventFormatterName + EventFormatterConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
        }
        editEventFormatterConfiguration(fileName, axisConfiguration, eventFormatterConfiguration, eventFormatterName);

    }

    @Override
    public EventFormatterConfiguration getActiveEventFormatterConfiguration(
            String eventFormatterName,
            int tenantId)
            throws EventFormatterConfigurationException {

        EventFormatterConfiguration eventFormatterConfiguration = null;

        Map<String, EventFormatter> tenantSpecificEventFormatterMap = tenantSpecificEventFormatterConfigurationMap.get(tenantId);
        if (tenantSpecificEventFormatterMap != null && tenantSpecificEventFormatterMap.size() > 0) {
            eventFormatterConfiguration = tenantSpecificEventFormatterMap.get(eventFormatterName).getEventFormatterConfiguration();
        }
        return eventFormatterConfiguration;
    }

    @Override
    public List<EventFormatterConfiguration> getAllActiveEventFormatterConfiguration(
            AxisConfiguration axisConfiguration) throws EventFormatterConfigurationException {
        List<EventFormatterConfiguration> eventFormatterConfigurations = new ArrayList<EventFormatterConfiguration>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventFormatterConfigurationMap.get(tenantId) != null) {
            for (EventFormatter eventFormatter : tenantSpecificEventFormatterConfigurationMap.get(tenantId).values()) {
                eventFormatterConfigurations.add(eventFormatter.getEventFormatterConfiguration());
            }
        }
        return eventFormatterConfigurations;
    }

    @Override
    public List<EventFormatterConfiguration> getAllActiveEventFormatterConfiguration(
            AxisConfiguration axisConfiguration, String streamId)
            throws EventFormatterConfigurationException {
        List<EventFormatterConfiguration> eventFormatterConfigurations = new ArrayList<EventFormatterConfiguration>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventFormatterConfigurationMap.get(tenantId) != null) {
            for (EventFormatter eventFormatter : tenantSpecificEventFormatterConfigurationMap.get(tenantId).values()) {
                if (eventFormatter.getStreamId().equals(streamId)) {
                    eventFormatterConfigurations.add(eventFormatter.getEventFormatterConfiguration());
                }
            }
        }
        return eventFormatterConfigurations;
    }

    @Override
    public List<EventFormatterConfigurationFile> getAllInactiveEventFormatterConfiguration(
            AxisConfiguration axisConfiguration) {

        List<EventFormatterConfigurationFile> undeployedEventFormatterFileList = new ArrayList<EventFormatterConfigurationFile>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (eventFormatterConfigurationFileMap.get(tenantId) != null) {
            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileMap.get(tenantId)) {
                if (!eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED)) {
                    undeployedEventFormatterFileList.add(eventFormatterConfigurationFile);
                }
            }
        }
        return undeployedEventFormatterFileList;
    }

    @Override
    public String getInactiveEventFormatterConfigurationContent(String filename,
                                                                AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        return EventFormatterConfigurationFilesystemInvoker.readEventFormatterConfigurationFile(filename, axisConfiguration);
    }

    @Override
    public String getActiveEventFormatterConfigurationContent(String eventFormatterName,
                                                              AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventFormatterName);
        return EventFormatterConfigurationFilesystemInvoker.readEventFormatterConfigurationFile(fileName, axisConfiguration);
    }

    public List<String> getAllEventStreams(AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        List<String> streamList = new ArrayList<String>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventStreamService eventStreamService = EventFormatterServiceValueHolder.getEventStreamService();
        Collection<StreamDefinition> eventStreamDefinitionList;
        try {
            eventStreamDefinitionList = eventStreamService.getAllStreamDefinitionsFromStore(tenantId);
            if (eventStreamDefinitionList != null) {
                for (StreamDefinition streamDefinition : eventStreamDefinitionList) {
                    streamList.add(streamDefinition.getStreamId());
                }
            }

        } catch (EventStreamConfigurationException e) {
            throw new EventFormatterConfigurationException("Error while retrieving stream definition from store", e);
        }

        return streamList;
    }

    public StreamDefinition getStreamDefinition(String streamNameWithVersion,
                                                AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventStreamService eventStreamService = EventFormatterServiceValueHolder.getEventStreamService();
        try {
            return eventStreamService.getStreamDefinitionFromStore(streamNameWithVersion, tenantId);
        } catch (EventStreamConfigurationException e) {
            throw new EventFormatterConfigurationException("Error while getting stream definition from store : " + e.getMessage(), e);
        }
    }

    public String getRegistryResourceContent(String resourcePath, int tenantId)
            throws EventFormatterConfigurationException {
        RegistryService registryService = EventFormatterServiceValueHolder.getRegistryService();

        String registryData;
        Resource registryResource = null;
        try {
            String pathPrefix = resourcePath.substring(0, resourcePath.indexOf(':') + 2);
            if (pathPrefix.equalsIgnoreCase(EventFormatterConstants.REGISTRY_CONF_PREFIX)) {
                resourcePath = resourcePath.replace(pathPrefix, "");
                registryResource = registryService.getConfigSystemRegistry().get(resourcePath);
            } else if (pathPrefix.equalsIgnoreCase(EventFormatterConstants.REGISTRY_GOVERNANCE_PREFIX)) {
                resourcePath = resourcePath.replace(pathPrefix, "");
                registryResource = registryService.getGovernanceSystemRegistry().get(resourcePath);
            }

            if (registryResource != null) {
                Object registryContent = registryResource.getContent();
                if (registryContent != null) {
                    registryData = (RegistryUtils.decodeBytes((byte[]) registryContent));
                } else {
                    throw new EventFormatterConfigurationException("There is no registry resource content available at " + resourcePath);
                }

            } else {
                throw new EventFormatterConfigurationException("Resource couldn't found from registry at " + resourcePath);
            }

        } catch (RegistryException e) {
            throw new EventFormatterConfigurationException("Error while retrieving the resource from registry at " + resourcePath, e);
        } catch (ClassCastException e) {
            throw new EventFormatterConfigurationException("Invalid mapping content found in " + resourcePath, e);
        }
        return registryData;
    }

    @Override
    public void setStatisticsEnabled(String eventFormatterName, AxisConfiguration axisConfiguration,
                                     boolean flag)
            throws EventFormatterConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventFormatterConfiguration eventFormatterConfiguration = getActiveEventFormatterConfiguration(eventFormatterName, tenantId);
        eventFormatterConfiguration.setEnableStatistics(flag);
        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
    }

    @Override
    public void setTraceEnabled(String eventFormatterName, AxisConfiguration axisConfiguration,
                                boolean flag)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        EventFormatterConfiguration eventFormatterConfiguration = getActiveEventFormatterConfiguration(eventFormatterName, tenantId);
        eventFormatterConfiguration.setEnableTracing(flag);
        editTracingStatistics(eventFormatterConfiguration, eventFormatterName, tenantId, axisConfiguration);
    }

    @Override
    public String getEventFormatterStatusAsString(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
        if (eventFormatterConfigurationFileList != null) {
            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                if (filename != null && filename.equals(eventFormatterConfigurationFile.getFileName())) {
                    String statusMsg = eventFormatterConfigurationFile.getDeploymentStatusMessage();
                    if (eventFormatterConfigurationFile.getDependency() != null) {
                        statusMsg = statusMsg + " [Dependency: " + eventFormatterConfigurationFile.getDependency() + "]";
                    }
                    return statusMsg;
                }
            }
        }

        return EventFormatterConstants.NO_DEPENDENCY_INFO_MSG;
    }

    @Override
    public void deployDefaultEventSender(String streamId, AxisConfiguration axisConfiguration)
            throws EventProducerException {
        OutputEventAdaptorManagerService outputEventAdaptorManagerService = EventFormatterServiceValueHolder.getOutputEventAdaptorManagerService();
        try {
            String defaultFormatterFilename = streamId.replaceAll(EventFormatterConstants.STREAM_ID_SEPERATOR, EventFormatterConstants.NORMALIZATION_STRING)
                                              + EventFormatterConstants.DEFAULT_EVENT_FORMATTER_POSTFIX + EventFormatterConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
            if (!EventFormatterConfigurationFilesystemInvoker.isEventFormatterConfigurationFileExists(defaultFormatterFilename, axisConfiguration)) {
                String eventAdaptorName = outputEventAdaptorManagerService.getDefaultWso2EventAdaptor(axisConfiguration);
                EventFormatterConfiguration defaultEventFormatterConfiguration =
                        EventFormatterUtil.createDefaultEventFormatter(streamId, eventAdaptorName);
                deployEventFormatterConfiguration(defaultEventFormatterConfiguration, axisConfiguration);
            }
        } catch (OutputEventAdaptorManagerConfigurationException e) {
            throw new EventProducerException("Error retrieving default wso2 event output adaptor : " + e.getMessage(), e);
        } catch (EventFormatterConfigurationException e) {
            throw new EventProducerException("Error creating a default event formatter : " + e.getMessage(), e);
        }
    }

    //Non-Interface public methods

    public boolean checkEventFormatterValidity(int tenantId, String eventFormatterName) {

        if (eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
            if (eventFormatterConfigurationFileList != null) {
                for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                    if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) && (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED))) {
                        log.error("Event Formatter " + eventFormatterName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void addEventFormatterConfigurationFile(int tenantId,
                                                   EventFormatterConfigurationFile eventFormatterConfigurationFile) {

        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);

        if (eventFormatterConfigurationFileList == null) {
            eventFormatterConfigurationFileList = new ArrayList<EventFormatterConfigurationFile>();
        } else {
            for (EventFormatterConfigurationFile anEventFormatterConfigurationFileList : eventFormatterConfigurationFileList) {
                if (anEventFormatterConfigurationFileList.getFileName().equals(eventFormatterConfigurationFile.getFileName())) {
                    return;
                }
            }
        }
        eventFormatterConfigurationFileList.add(eventFormatterConfigurationFile);
        eventFormatterConfigurationFileMap.put(tenantId, eventFormatterConfigurationFileList);
    }

    public void addEventFormatterConfiguration(
            EventFormatterConfiguration eventFormatterConfiguration)
            throws EventFormatterConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        Map<String, EventFormatter> eventFormatterConfigurationMap
                = tenantSpecificEventFormatterConfigurationMap.get(tenantId);

        if (eventFormatterConfigurationMap == null) {
            eventFormatterConfigurationMap = new ConcurrentHashMap<String, EventFormatter>();
        }

        EventFormatter eventFormatter = new EventFormatter(eventFormatterConfiguration);
        eventFormatterConfigurationMap.put(eventFormatterConfiguration.getEventFormatterName(), eventFormatter);

        tenantSpecificEventFormatterConfigurationMap.put(tenantId, eventFormatterConfigurationMap);
    }

    public void removeEventFormatterConfigurationFromMap(String fileName, int tenantId) {
        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
        if (eventFormatterConfigurationFileList != null) {
            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                if ((eventFormatterConfigurationFile.getFileName().equals(fileName))) {
                    if (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED)) {
                        String eventFormatterName = eventFormatterConfigurationFile.getEventFormatterName();
                        if (tenantSpecificEventFormatterConfigurationMap.get(tenantId) != null) {
                            EventFormatter eventFormatter = tenantSpecificEventFormatterConfigurationMap.get(tenantId).get(eventFormatterName);
                            List<EventProducer> eventProducerList = eventFormatter.getEventProducerList();
                            for (EventProducer eventProducer : eventProducerList) {
                                eventProducer.unsubscribe(tenantId, eventFormatter.getStreamId(), eventFormatter.getEventFormatterSender());
                            }
                            tenantSpecificEventFormatterConfigurationMap.get(tenantId).remove(eventFormatterName);
                        }
                    }
                    eventFormatterConfigurationFileList.remove(eventFormatterConfigurationFile);
                    return;
                }
            }
        }
    }

    public void activateInactiveEventFormatterConfiguration(int tenantId, String dependency)
            throws EventFormatterConfigurationException {

        List<EventFormatterConfigurationFile> fileList = new ArrayList<EventFormatterConfigurationFile>();

        if (eventFormatterConfigurationFileMap != null && eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);

            if (eventFormatterConfigurationFileList != null) {
                for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                    if ((eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY)) && eventFormatterConfigurationFile.getDependency().equalsIgnoreCase(dependency)) {
                        fileList.add(eventFormatterConfigurationFile);
                    }
                }
            }
        }
        for (EventFormatterConfigurationFile eventFormatterConfigurationFile : fileList) {
            try {
                EventFormatterConfigurationFilesystemInvoker.reload(eventFormatterConfigurationFile.getFileName(), eventFormatterConfigurationFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Event formatter configuration file : " + new File(eventFormatterConfigurationFile.getFileName()).getName(), e);
            }
        }
    }

    public void deactivateActiveEventFormatterConfiguration(int tenantId, String dependency)
            throws EventFormatterConfigurationException {

        List<EventFormatterConfigurationFile> fileList = new ArrayList<EventFormatterConfigurationFile>();
        if (tenantSpecificEventFormatterConfigurationMap != null && tenantSpecificEventFormatterConfigurationMap.size() > 0) {
            Map<String, EventFormatter> eventFormatterMap = tenantSpecificEventFormatterConfigurationMap.get(tenantId);
            if (eventFormatterMap != null) {
                for (EventFormatter eventFormatter : eventFormatterMap.values()) {
                    String streamNameWithVersion = eventFormatter.getEventFormatterConfiguration().getFromStreamName() + ":" + eventFormatter.getEventFormatterConfiguration().getFromStreamVersion();
                    String eventAdaptorName = eventFormatter.getEventFormatterConfiguration().getToPropertyConfiguration().getEventAdaptorName();
                    if (streamNameWithVersion.equals(dependency) || eventAdaptorName.equals(dependency)) {
                        EventFormatterConfigurationFile eventFormatterConfigurationFile = getEventFormatterConfigurationFile(eventFormatter.getEventFormatterConfiguration().getEventFormatterName(), tenantId);
                        if (eventFormatterConfigurationFile != null) {
                            fileList.add(eventFormatterConfigurationFile);
                        }
                    }
                }
            }
        }

        for (EventFormatterConfigurationFile eventFormatterConfigurationFile : fileList) {
            EventFormatterConfigurationFilesystemInvoker.reload(eventFormatterConfigurationFile.getFileName(), eventFormatterConfigurationFile.getAxisConfiguration());
            log.info("Event formatter : " + eventFormatterConfigurationFile.getEventFormatterName() + "  is in inactive state because dependency could not be found : " + dependency);
        }
    }

    //Private Methods are below

    private void editTracingStatistics(
            EventFormatterConfiguration eventFormatterConfiguration,
            String eventFormatterName, int tenantId, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        String fileName = getFileName(tenantId, eventFormatterName);
        undeployActiveEventFormatterConfiguration(eventFormatterName, axisConfiguration);
        OMElement omElement = FormatterConfigurationBuilder.eventFormatterConfigurationToOM(eventFormatterConfiguration);
        EventFormatterConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
        EventFormatterConfigurationFilesystemInvoker.save(omElement, fileName, axisConfiguration);
    }

    private String getFileName(int tenantId, String eventFormatterName) {

        if (eventFormatterConfigurationFileMap.size() > 0) {
            List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);
            if (eventFormatterConfigurationFileList != null) {
                for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                    if ((eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) && (eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED))) {
                        return eventFormatterConfigurationFile.getFileName();
                    }
                }
            }
        }
        return null;
    }

    private void editEventFormatterConfiguration(String filename,
                                                 AxisConfiguration axisConfiguration,
                                                 String eventFormatterConfiguration,
                                                 String originalEventFormatterName)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventFormatterConfiguration);
            omElement.toString();
            EventFormatterConfigurationHelper.validateEventFormatterConfiguration(omElement);
            String mappingType = EventFormatterConfigurationHelper.getOutputMappingType(omElement);
            if (mappingType != null) {
                EventFormatterConfiguration eventFormatterConfigurationObject = FormatterConfigurationBuilder.getEventFormatterConfiguration(omElement, tenantId, mappingType);
                if (!(eventFormatterConfigurationObject.getEventFormatterName().equals(originalEventFormatterName))) {
                    if (checkEventFormatterValidity(tenantId, eventFormatterConfigurationObject.getEventFormatterName())) {
                        EventFormatterConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
                        EventFormatterConfigurationFilesystemInvoker.save(omElement, filename, axisConfiguration);
                    } else {
                        throw new EventFormatterConfigurationException("There is a Event Formatter " + eventFormatterConfigurationObject.getEventFormatterName() + " with the same name");
                    }
                } else {
                    EventFormatterConfigurationFilesystemInvoker.delete(filename, axisConfiguration);
                    EventFormatterConfigurationFilesystemInvoker.save(omElement, filename, axisConfiguration);
                }
            } else {
                throw new EventFormatterConfigurationException("Mapping type of the Event Formatter " + originalEventFormatterName + " cannot be null");

            }

        } catch (XMLStreamException e) {
            throw new EventFormatterConfigurationException("Not a valid xml object : " + e.getMessage(), e);
        }

    }

    private EventFormatterConfigurationFile getEventFormatterConfigurationFile(
            String eventFormatterName, int tenantId) {
        List<EventFormatterConfigurationFile> eventFormatterConfigurationFileList = eventFormatterConfigurationFileMap.get(tenantId);

        if (eventFormatterConfigurationFileList != null) {
            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFileList) {
                if (eventFormatterConfigurationFile.getEventFormatterName().equals(eventFormatterName)) {
                    return eventFormatterConfigurationFile;
                }
            }
        }
        return null;

    }

    private void validateToRemoveInactiveEventFormatterConfiguration(String eventFormatterName,
                                                                     AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = eventFormatterName + EventFormatterConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<EventFormatterConfigurationFile> eventFormatterConfigurationFiles = eventFormatterConfigurationFileMap.get(tenantId);
        if (eventFormatterConfigurationFiles != null) {
            for (EventFormatterConfigurationFile eventFormatterConfigurationFile : eventFormatterConfigurationFiles) {
                if ((eventFormatterConfigurationFile.getFileName().equals(fileName))) {
                    if (!(eventFormatterConfigurationFile.getStatus().equals(EventFormatterConfigurationFile.Status.DEPLOYED))) {
                        EventFormatterConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
                        break;
                    }
                }
            }
        }

    }

}