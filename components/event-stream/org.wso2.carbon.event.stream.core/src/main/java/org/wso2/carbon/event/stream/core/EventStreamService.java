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
package org.wso2.carbon.event.stream.core;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import java.util.List;

public interface EventStreamService {


    /**
     * @param name
     * @param version
     * @return
     */
    public StreamDefinition getStreamDefinition(String name, String version)
            throws EventStreamConfigurationException;

    /**
     * @param streamId
     * @return
     */
    public StreamDefinition getStreamDefinition(String streamId)
            throws EventStreamConfigurationException;

    public EventStreamConfiguration getEventStreamConfiguration(String streamId);

    /**
     * @return
     * @throws EventStreamConfigurationException
     */
    public List<StreamDefinition> getAllStreamDefinitions()
            throws EventStreamConfigurationException;


    public List<EventStreamConfiguration> getAllEventStreamConfigurations()
            throws EventStreamConfigurationException;
    /**
     * @param streamDefinition
     * @throws EventStreamConfigurationException
     */
    public void addEventStreamDefinition(StreamDefinition streamDefinition) throws
            EventStreamConfigurationException;

    /**
     * @param streamName
     * @param streamVersion
     * @throws EventStreamConfigurationException
     */
    public void removeEventStreamDefinition(String streamName, String streamVersion)
            throws EventStreamConfigurationException;

    /**
     * @return
     * @throws EventStreamConfigurationException
     */
    public List<String> getStreamIds() throws EventStreamConfigurationException;


    public String generateSampleEvent(String streamId, String eventType)
            throws EventStreamConfigurationException;

    public void subscribe(SiddhiEventConsumer siddhiEventConsumer) throws EventStreamConfigurationException;

    public void subscribe(RawEventConsumer rawEventConsumer) throws EventStreamConfigurationException;

    public void subscribe(EventProducer eventProducer) throws EventStreamConfigurationException;

    public void subscribe(WSO2EventConsumer wso2EventConsumer) throws EventStreamConfigurationException;

    public void subscribe(WSO2EventListConsumer wso2EventListConsumer) throws EventStreamConfigurationException;

    public void unsubscribe(SiddhiEventConsumer siddhiEventConsumer);

    public void unsubscribe(RawEventConsumer rawEventConsumer);

    public void unsubscribe(EventProducer eventProducer);

    public void unsubscribe(WSO2EventConsumer wso2EventConsumer);

    public void unsubscribe(WSO2EventListConsumer wso2EventConsumer);

    public void publish(Event event);

}
