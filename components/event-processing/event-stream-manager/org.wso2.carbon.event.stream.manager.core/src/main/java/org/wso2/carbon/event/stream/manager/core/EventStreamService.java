package org.wso2.carbon.event.stream.manager.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.databridge.commons.Credentials;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;
import org.wso2.carbon.event.stream.manager.core.exception.EventStreamConfigurationException;

import java.util.Collection;
import java.util.List;

public interface EventStreamService {

    /**
     *
     * @param streamDefinition
     * @param axisConfiguration
     * @throws EventStreamConfigurationException
     */
    public void addEventStreamDefinitionToStore(StreamDefinition streamDefinition, AxisConfiguration axisConfiguration) throws
            EventStreamConfigurationException;

    /**
     *
     * @param streamDefinition
     * @throws EventStreamConfigurationException
     */
    public void addEventStreamDefinitionToStore(StreamDefinition streamDefinition) throws
            EventStreamConfigurationException;

    /**
     *
     * @param name
     * @param version
     * @param tenantId
     * @return
     * @throws StreamDefinitionStoreException
     */
    public StreamDefinition getStreamDefinitionFromStore(String name, String version, int tenantId)
            throws EventStreamConfigurationException;

    /**
     *
     * @param name
     * @param version
     * @param tenantId
     * @return
     * @throws EventStreamConfigurationException
     */
    public boolean removeStreamDefinitionFromStore(String name, String version, int tenantId)
            throws EventStreamConfigurationException;

    /**
     *
     * @param tenantId
     * @return
     * @throws EventStreamConfigurationException
     */
    public Collection<StreamDefinition> getAllStreamDefinitionsFromStore(int tenantId)
            throws EventStreamConfigurationException;

    /**
     *
     * @param streamName
     * @param streamVersion
     * @param tenantId
     * @throws EventStreamConfigurationException
     */
    public void removeEventStreamDefinition(String streamName, String streamVersion, int tenantId)
            throws EventStreamConfigurationException;

    /**
     *
     * @param eventStreamListener
     */
    public void registerEventStreamListener(EventStreamListener eventStreamListener);

    /**
     *
     * @param streamId
     * @param tenantId
     * @return
     * @throws StreamDefinitionStoreException
     */
    public StreamDefinition getStreamDefinitionFromStore(String streamId, int tenantId)
            throws EventStreamConfigurationException;

    /**
     *
     * @param tenantId
     * @return
     * @throws EventStreamConfigurationException
     */
    public List<String> getStreamIds(int tenantId) throws EventStreamConfigurationException;


    public String generateSampleEvent(String streamId, String eventType, int tenantId) throws  EventStreamConfigurationException;

    /**
     *
     * @param credentials
     * @param streamDefinition
     * @param axisConfiguration
     * @throws StreamDefinitionStoreException
     */
    void addStreamDefinitionToStore(Credentials credentials,
                                     StreamDefinition streamDefinition, AxisConfiguration axisConfiguration)
            throws StreamDefinitionStoreException;
}
