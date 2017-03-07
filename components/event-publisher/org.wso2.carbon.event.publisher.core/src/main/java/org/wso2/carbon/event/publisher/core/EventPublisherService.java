/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.publisher.core;

import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfigurationFile;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;

import java.util.List;

public interface EventPublisherService {


    /**
     * Method used to add a new event publisher configuration
     *
     * @param eventPublisherConfiguration
     * @throws EventPublisherConfigurationException
     */
    public void deployEventPublisherConfiguration(
            EventPublisherConfiguration eventPublisherConfiguration)
            throws EventPublisherConfigurationException;

    /**
     * Method used to add a new event publisher configuration by passing in the xml configuration
     *
     * @param eventPublisherConfigXml
     * @throws EventPublisherConfigurationException
     */
    public void deployEventPublisherConfiguration(
            String eventPublisherConfigXml)
            throws EventPublisherConfigurationException;

    /**
     * This method used to un-deploy the active event publisher configuration from filesystem
     *
     * @param eventPublisherName
     * @throws EventPublisherConfigurationException
     */
    public void undeployActiveEventPublisherConfiguration(String eventPublisherName)
            throws EventPublisherConfigurationException;

    /**
     * Method used to undeploy inactive event publisher configuration file from filesystem
     *
     * @param fileName
     * @throws EventPublisherConfigurationException
     */
    public void undeployInactiveEventPublisherConfiguration(String fileName)
            throws EventPublisherConfigurationException;

    /**
     * Method used to get edit the inactive event publisher configuration info
     *
     * @param eventPublisherConfiguration
     * @param fileName
     * @throws EventPublisherConfigurationException
     */
    public void editInactiveEventPublisherConfiguration(
            String eventPublisherConfiguration,
            String fileName)
            throws EventPublisherConfigurationException;

    /**
     * Method used to edit the active event publisher configuration info
     *
     * @param eventPublisherConfiguration
     * @param eventPublisherName
     * @throws EventPublisherConfigurationException
     */
    public void editActiveEventPublisherConfiguration(String eventPublisherConfiguration,
                                                      String eventPublisherName)
            throws EventPublisherConfigurationException;


    /**
     * Method used to get the active  event publisher configuration as an object
     *
     * @param eventPublisherName
     * @return
     * @throws EventPublisherConfigurationException
     */
    public EventPublisherConfiguration getActiveEventPublisherConfiguration(
            String eventPublisherName)
            throws EventPublisherConfigurationException;

    /**
     * This method used to get all the active event publisher configuration objects
     *
     * @return
     * @throws EventPublisherConfigurationException
     */
    public List<EventPublisherConfiguration> getAllActiveEventPublisherConfigurations()
            throws EventPublisherConfigurationException;

    public List<EventPublisherConfiguration> getAllActiveEventPublisherConfigurations(
            String streamId)
            throws EventPublisherConfigurationException;


    /**
     * This method used to get all inactive event publisher configuration file objects
     *
     * @return
     */
    public List<EventPublisherConfigurationFile> getAllInactiveEventPublisherConfigurations();


    /**
     * Method used to get the inactive event publisher configuration xml as a string
     *
     * @param filename
     * @return
     * @throws EventPublisherConfigurationException
     */
    public String getInactiveEventPublisherConfigurationContent(String filename)
            throws EventPublisherConfigurationException;


    /**
     * Method used to get the active event publisher configuration xml as a string
     *
     * @param eventPublisherName
     * @return
     * @throws EventPublisherConfigurationException
     */
    public String getActiveEventPublisherConfigurationContent(String eventPublisherName)
            throws EventPublisherConfigurationException;

    /**
     * Method used to get the specific stream definition for a given streamId
     *
     * @param streamNameWithVersion
     * @return
     * @throws EventPublisherConfigurationException
     */
    public StreamDefinition getStreamDefinition(String streamNameWithVersion)
            throws EventPublisherConfigurationException;


    /**
     * Method used to get the resource from the registry
     *
     * @param resourcePath
     * @return
     * @throws EventPublisherConfigurationException
     */
    public String getRegistryResourceContent(String resourcePath)
            throws EventPublisherConfigurationException;

    /**
     * Method used to enable/disable the statistics for an event publisher
     *
     * @param eventPublisherName the event publisher name to which statistics collecting state should be changed
     * @throws EventPublisherConfigurationException
     */
    public void setStatisticsEnabled(String eventPublisherName, boolean statisticsEnabled)
            throws EventPublisherConfigurationException;

    /**
     * Method used to enable/disable the tracing for an event publisher
     *
     * @param eventPublisherName the event publisher name to which tracing state should be changed
     * @param traceEnabled
     */
    public void setTraceEnabled(String eventPublisherName, boolean traceEnabled)
            throws EventPublisherConfigurationException;

    /**
     * Method used to enable/disable the processing for an event publisher
     *
     * @param eventPublisherName the event publisher name to which tracing state should be changed
     * @param processingEnabled
     */
    public void setProcessEnabled(String eventPublisherName, boolean processingEnabled)
            throws EventPublisherConfigurationException;

    /**
     * Returns the Event Publisher Name as in the given {@code eventPublisherConfigXml}.
     *
     * @param eventPublisherConfigXml Event Publisher Configuration, in XML format.
     * @return The name of the Event Publisher, as given in the {@code eventPublisherConfigXml}
     * @throws EventPublisherConfigurationException
     */
    public String getEventPublisherName(String eventPublisherConfigXml)
            throws EventPublisherConfigurationException;

}
