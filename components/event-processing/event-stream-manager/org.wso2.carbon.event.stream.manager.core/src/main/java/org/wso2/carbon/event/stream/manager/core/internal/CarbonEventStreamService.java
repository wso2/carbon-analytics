package org.wso2.carbon.event.stream.manager.core.internal;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.processor.api.passthrough.PassthroughReceiverConfigurator;
import org.wso2.carbon.event.processor.api.passthrough.exception.PassthroughConfigurationException;
import org.wso2.carbon.event.processor.api.receive.exception.EventReceiverException;
import org.wso2.carbon.event.stream.manager.core.EventStreamListener;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.manager.core.internal.ds.EventStreamServiceValueHolder;
import org.wso2.carbon.event.stream.manager.core.internal.util.EventStreamConstants;
import org.wso2.carbon.event.stream.manager.core.internal.util.SampleEventGenerator;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class CarbonEventStreamService implements EventStreamService {

    private static final Log log = LogFactory.getLog(CarbonEventStreamService.class);
    private static final String STREAM_DEFINITION_STORE = "/StreamDefinitions";
    private List<EventStreamListener> eventStreamListenerList = new ArrayList<EventStreamListener>();

    @Override
    public void addStreamDefinitionToStore(Credentials credentials,
                                           StreamDefinition streamDefinition,
                                           AxisConfiguration axisConfiguration)
            throws StreamDefinitionStoreException {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext privilegedCarbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();

            privilegedCarbonContext.setTenantId(EventStreamServiceValueHolder.getRealmService().getTenantManager().getTenantId(credentials.getDomainName()));
            privilegedCarbonContext.setTenantDomain(credentials.getDomainName());

            PassthroughReceiverConfigurator passthroughReceiverConfigurator = EventStreamServiceValueHolder.getPassthroughReceiverConfigurator();

            try {
                UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceUserRegistry(credentials.getUsername(), credentials.getPassword());
                Resource resource = registry.newResource();
                resource.setContent(EventDefinitionConverterUtils.convertToJson(streamDefinition));
                resource.setMediaType("application/json");
                registry.put(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + streamDefinition.getName() + RegistryConstants.PATH_SEPARATOR + streamDefinition.getVersion(), resource);
                passthroughReceiverConfigurator.deployDefaultEventBuilder(streamDefinition.getStreamId(), axisConfiguration);

                log.info("Stream definition added to registry successfully : " + streamDefinition.getStreamId());

                for (EventStreamListener eventStreamListener : eventStreamListenerList) {
                    eventStreamListener.addedEventStream(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), streamDefinition.getName(), streamDefinition.getVersion());
                }

            } catch (RegistryException e) {
                log.error("Error in saving Stream Definition " + streamDefinition);
            }

        } catch (UserStoreException e) {
            throw new StreamDefinitionStoreException("Error in saving definition " + streamDefinition + " to registry, " + e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }


    }

    @Override
    public void addEventStreamDefinitionToStore(StreamDefinition streamDefinition,
                                                AxisConfiguration axisConfiguration)
            throws EventStreamConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        StreamDefinition existingDefinition;
        existingDefinition = getStreamDefinitionFromStore(streamDefinition.getName(), streamDefinition.getVersion(), tenantId);

        if (existingDefinition == null) {
            saveStreamDefinitionToStore(streamDefinition, tenantId);

            PassthroughReceiverConfigurator passthroughReceiverConfigurator = EventStreamServiceValueHolder.getPassthroughReceiverConfigurator();
            if (passthroughReceiverConfigurator != null) {
                try {
                    passthroughReceiverConfigurator.deployDefaultEventBuilder(streamDefinition.getStreamId(), axisConfiguration);
                } catch (EventReceiverException e) {
                    throw new EventStreamConfigurationException(e);
                }
            }

            for (EventStreamListener eventStreamListener : eventStreamListenerList) {
                eventStreamListener.addedEventStream(tenantId, streamDefinition.getName(), streamDefinition.getVersion());
            }
            return;
        }
        if (!existingDefinition.equals(streamDefinition)) {

            throw new EventStreamConfigurationException("Another Stream with same name and version exist :"
                                                        + EventDefinitionConverterUtils.convertToJson(existingDefinition));
        }
    }

    @Override
    public void addEventStreamDefinitionToStore(StreamDefinition streamDefinition)
            throws EventStreamConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        StreamDefinition existingDefinition;
        existingDefinition = getStreamDefinitionFromStore(streamDefinition.getName(), streamDefinition.getVersion(), tenantId);

        if (existingDefinition == null) {
            saveStreamDefinitionToStore(streamDefinition, tenantId);
            PassthroughReceiverConfigurator passthroughReceiverConfigurator = EventStreamServiceValueHolder.getPassthroughReceiverConfigurator();
            if (passthroughReceiverConfigurator != null) {
                try {
                    passthroughReceiverConfigurator.saveDefaultEventBuilder(streamDefinition.getStreamId());
                } catch (EventReceiverException e) {
                    throw new EventStreamConfigurationException(e);
                }
            }
            for (EventStreamListener eventStreamListener : eventStreamListenerList) {
                eventStreamListener.addedEventStream(tenantId, streamDefinition.getName(), streamDefinition.getVersion());
            }
            return;
        }
        if (!existingDefinition.equals(streamDefinition)) {
            throw new EventStreamConfigurationException("Another Stream with same name and version exist : "
                                                        + EventDefinitionConverterUtils.convertToJson(existingDefinition));
        }
    }

    @Override
    public void removeEventStreamDefinition(String streamName, String streamVersion, int tenantId)
            throws EventStreamConfigurationException {

        if (removeStreamDefinitionFromStore(streamName, streamVersion, tenantId)) {
            log.info("Stream definition - " + streamName + ":" + streamVersion + " removed from registry successfully");
        }

        for (EventStreamListener eventStreamListener : eventStreamListenerList) {
            eventStreamListener.removedEventStream(tenantId, streamName, streamVersion);
        }

    }

    @Override
    public void registerEventStreamListener(EventStreamListener eventStreamListener) {
        if (eventStreamListener != null) {
            eventStreamListenerList.add(eventStreamListener);
        }
    }

    private void saveStreamDefinitionToStore(StreamDefinition streamDefinition, int tenantId)
            throws EventStreamConfigurationException {
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            Resource resource = registry.newResource();

            resource.setContent(EventDefinitionConverterUtils.convertToJson(streamDefinition));
            resource.setMediaType("application/json");
            registry.put(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + streamDefinition.getName() + RegistryConstants.PATH_SEPARATOR + streamDefinition.getVersion(), resource);
            log.info("Stream definition added to registry successfully : " + streamDefinition.getStreamId());
        } catch (RegistryException e) {
            log.error("Error in saving Stream Definition " + streamDefinition);
            throw new EventStreamConfigurationException("Error in saving Stream Definition " + streamDefinition, e);
        }
    }

    @Override
    public StreamDefinition getStreamDefinitionFromStore(String name, String version, int tenantId)
            throws EventStreamConfigurationException {

        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            if (registry.resourceExists(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version)) {
                Resource resource = registry.get(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version);
                Object content = resource.getContent();
                if (content != null) {
                    return EventDefinitionConverterUtils.convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error in getting Stream Definition " + name + ":" + version, e);
            throw new EventStreamConfigurationException("Error in getting Stream Definition " + name + ":" + version, e);
        }
    }

    @Override
    public boolean removeStreamDefinitionFromStore(String name, String version, int tenantId)
            throws EventStreamConfigurationException {
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);
            registry.delete(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version);
            return !registry.resourceExists(STREAM_DEFINITION_STORE + RegistryConstants.PATH_SEPARATOR + name + RegistryConstants.PATH_SEPARATOR + version);
        } catch (RegistryException e) {
            log.error("Error in deleting Stream Definition " + name + ":" + version);
            throw new EventStreamConfigurationException("Error in deleting Stream Definition " + name + ":" + version, e);
        }
    }

    @Override
    public Collection<StreamDefinition> getAllStreamDefinitionsFromStore(int tenantId)
            throws EventStreamConfigurationException {
        ConcurrentHashMap<String, StreamDefinition> map = new ConcurrentHashMap<String, StreamDefinition>();
        try {
            UserRegistry registry = EventStreamServiceValueHolder.getRegistryService().getGovernanceSystemRegistry(tenantId);

            if (!registry.resourceExists(STREAM_DEFINITION_STORE)) {
                registry.put(STREAM_DEFINITION_STORE, registry.newCollection());
            } else {
                org.wso2.carbon.registry.core.Collection collection = (org.wso2.carbon.registry.core.Collection) registry.get(STREAM_DEFINITION_STORE);
                for (String streamNameCollection : collection.getChildren()) {

                    org.wso2.carbon.registry.core.Collection innerCollection = (org.wso2.carbon.registry.core.Collection) registry.get(streamNameCollection);
                    for (String streamVersionCollection : innerCollection.getChildren()) {

                        Resource resource = registry.get(streamVersionCollection);
                        try {
                            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(RegistryUtils.decodeBytes((byte[]) resource.getContent()));
                            map.put(streamDefinition.getStreamId(), streamDefinition);
                        } catch (Throwable e) {
                            log.error("Error in retrieving streamDefinition from the resource at " + resource.getPath(), e);
                            throw new EventStreamConfigurationException("Error in retrieving streamDefinition from the resource at " + resource.getPath(), e);
                        }
                    }
                }
            }

        } catch (RegistryException e) {
            log.error("Error in retrieving streamDefinitions from the registry", e);
            throw new EventStreamConfigurationException("Error in retrieving streamDefinitions from the registry", e);
        }

        return map.values();
    }

    @Override
    public StreamDefinition getStreamDefinitionFromStore(String streamId, int tenantId)
            throws EventStreamConfigurationException {

        return getStreamDefinitionFromStore(DataBridgeCommonsUtils.getStreamNameFromStreamId(streamId),
                                            DataBridgeCommonsUtils.getStreamVersionFromStreamId(streamId), tenantId);
    }

    @Override
    public List<String> getStreamIds(int tenantId) throws EventStreamConfigurationException {
        Collection<StreamDefinition> streamDefinitions = getAllStreamDefinitionsFromStore(tenantId);
        List<String> streamDefinitionsIds = new ArrayList<String>(streamDefinitions.size());
        for (StreamDefinition streamDefinition : streamDefinitions) {
            streamDefinitionsIds.add(streamDefinition.getStreamId());
        }

        return streamDefinitionsIds;
    }

    @Override
    public String generateSampleEvent(String streamId, String eventType, int tenantId)
            throws EventStreamConfigurationException {

        StreamDefinition streamDefinition = getStreamDefinitionFromStore(streamId, tenantId);

        if (eventType.equals(EventStreamConstants.XML_EVENT)) {
            return SampleEventGenerator.generateXMLEvent(streamDefinition);
        }else if (eventType.equals(EventStreamConstants.JSON_EVENT)) {
            return SampleEventGenerator.generateJSONEvent(streamDefinition);
        }else if (eventType.equals(EventStreamConstants.TEXT_EVENT)) {
            return SampleEventGenerator.generateTextEvent(streamDefinition);
        }
        return null;
    }

    public void processPendingStreamList() {
        if (EventStreamServiceValueHolder.getPassthroughReceiverConfigurator() != null) {
            if (EventStreamServiceValueHolder.getPendingStreamIdList() != null && (!EventStreamServiceValueHolder.getPendingStreamIdList().isEmpty())) {
                for (StreamDefinition streamDefinition : EventStreamServiceValueHolder.getPendingStreamIdList()) {
                    try {
                        addEventStreamDefinitionToStore(streamDefinition);
                    } catch (EventStreamConfigurationException e) {
                        throw new PassthroughConfigurationException("Error while loading streams from config ", e);
                    }
                }
                EventStreamServiceValueHolder.setPendingStreamIdList(null);
            }
        }
    }
}