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
package org.wso2.carbon.event.receiver.core;

import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;

import java.util.List;

public interface EventReceiverService {

    /**
     * Updates the event receiver with the given syntax
     *
     * @param eventReceiverConfigXml the XML configuration of the event receiver as a string
     */
    public void editInactiveEventReceiverConfiguration(String eventReceiverConfigXml,
                                                       String filename)
            throws EventReceiverConfigurationException;

    /**
     * Updates the event receiver according to the passed in {@link EventReceiverConfiguration}
     *
     * @param originalEventReceiverName the original name of the event receiver
     * @param originalEventReceiverName the original name of the event receiver
     */
    public void editActiveEventReceiverConfiguration(String eventReceiverConfigXml,
                                                     String originalEventReceiverName)
            throws EventReceiverConfigurationException;

    /**
     * Getting all the event receiver configuration instance details.
     *
     * @return - list of available event configuration
     */
    public List<EventReceiverConfiguration> getAllActiveEventReceiverConfigurations();

    /**
     * Getting all event receiver configurations specific for a stream
     *
     * @param streamId stream id for which event receiver configurations are needed
     * @return  the event receiver configuration
     */
    public List<EventReceiverConfiguration> getAllActiveEventReceiverConfigurations(
            String streamId);

    /**
     * Returns the {@link EventReceiverConfiguration} for the event receiver with given name
     *
     * @param eventReceiverName the event receiver name
     * @return {@link EventReceiverConfiguration} that is associated with the event receiver of the name passed in
     */
    public EventReceiverConfiguration getActiveEventReceiverConfiguration(String eventReceiverName);

    /**
     * @return List of EventReceiverConfigurationFile
     */
    public List<EventReceiverConfigurationFile> getAllInactiveEventReceiverConfigurations();

    /**
     * Returns the event receiver XML configuration for the given event receiver name and tenant id
     *
     * @param eventReceiverName  the name of the event receiver
     * @return the XML configuration syntax as a string
     */
    public String getActiveEventReceiverConfigurationContent(String eventReceiverName)
            throws EventReceiverConfigurationException;

    /**
     * Returns the event receiver XML configuration for the given filePath and tenant id
     *
     * @return the XML configuration syntax as a string
     */
    public String getInactiveEventReceiverConfigurationContent(String filename)
            throws EventReceiverConfigurationException;

    /**
     * Undeploys an active event receiver configuration of the given name for the axis configuration
     * and deletes it from the file system.
     *
     * @param eventReceiverName  the event receiver name
     */
    public void undeployActiveEventReceiverConfiguration(String eventReceiverName)
            throws EventReceiverConfigurationException;

    /**
     * Removes the event receiver configuration file from the file system and memory
     *
     * @param filename          the name of the event receiver configuration file
     */
    public void undeployInactiveEventReceiverConfiguration(String filename)
            throws EventReceiverConfigurationException;

    /**
     * Deploys an event receiver configuration and saves the associated configuration file to the filesystem.
     *
     * @param eventReceiverConfiguration the {@link org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration} object
     */
    public void deployEventReceiverConfiguration(
            EventReceiverConfiguration eventReceiverConfiguration) throws EventReceiverConfigurationException;

    /**
     * Enable or disable tracing for the event receiver of given name
     *  @param eventReceiverName  event receiver name
     * @param traceEnabled      {@code true} or {@code false} specifying whether trace is enabled or not
     */
    public void setTraceEnabled(String eventReceiverName, boolean traceEnabled)
            throws EventReceiverConfigurationException;

    /**
     * Enable or disable statistics for the event receiver of given name
     *
     * @param eventReceiverName  event receiver name
     * @param statisticsEnabled {@code true} or {@code false} specifying whether statistics is enabled or not
     */
    public void setStatisticsEnabled(String eventReceiverName, boolean statisticsEnabled)
            throws EventReceiverConfigurationException;

    /**
     * Returns the deployment status and dependency information as a formatted string for event receiver associated
     * with the filename specified
     *
     * @param filename the filename of the event receiver
     * @return a string description for the status of the event receiver specified
     */
    @Deprecated
    public String getEventReceiverStatusAsString(String filename);

    public void deployEventReceiverConfiguration(String eventReceiverConfigXml) throws EventReceiverConfigurationException;
}
