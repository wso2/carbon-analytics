/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.input.adaptor.manager.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;

import java.util.List;

public interface InputEventAdaptorManagerService {

    /**
     * use to add a new Event adaptor Configuration instance to the system. A Event Adaptor Configuration instance represents the
     * details of a particular event adaptor connection details.
     *
     * @param eventAdaptorConfiguration - event adaptor configuration to be added
     */
    public void deployInputEventAdaptorConfiguration(
            InputEventAdaptorConfiguration eventAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * removes the event adaptor configuration instance from the system.
     *
     * @param name              - event adaptor configuration to be removed
     */
    public void undeployActiveInputEventAdaptorConfiguration(String name,
                                                             AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * This method used to delete the un-deployed Event Adaptor File
     *
     * @param fileName fileName of the Adaptor File that going to be deleted
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     *
     */
    public void undeployInactiveInputEventAdaptorConfiguration(String fileName,
                                                               AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * To edit an event adaptor configuration
     *
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     *
     */
    public void editActiveInputEventAdaptorConfiguration(String eventAdaptorConfiguration,
                                                         String eventAdaptorName,
                                                         AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException;


    /**
     * Used to save not deployed event adaptor configuration details
     *
     * @param eventAdaptorConfiguration configuration details
     * @param fileName                      file path of the adaptor
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     *
     */
    public void editInactiveInputEventAdaptorConfiguration(
            String eventAdaptorConfiguration,
            String fileName,
            AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * getting all the event adaptor details. this is used to dispaly all the
     * event adaptor configuration instances.
     * @return - list of available event adaptor configuration
     */
    public List<InputEventAdaptorConfiguration> getAllActiveInputEventAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * retuns the event adaptor configuration for the given name
     *
     * @param name     - event adaptor configuration name
     * @return - event adaptor configuration
     */
    public InputEventAdaptorConfiguration getActiveInputEventAdaptorConfiguration(
            String name,
            int tenantId)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of InputEventAdaptorFile
     */
    public List<InputEventAdaptorFile> getAllInactiveInputEventAdaptorConfiguration(
            AxisConfiguration axisConfiguration);

    /**
     * Use to get the event adaptor configuration file
     *
     * @param eventAdaptorName event adaptor name
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     *
     */
    public String getActiveInputEventAdaptorConfigurationContent(String eventAdaptorName,
                                                                 AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException;


    /**
     * get the event adaptor file of the not deployed adaptor
     *
     *
     * @param fileName file path of the not deployed event adaptor
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     *
     */
    public String getInactiveInputEventAdaptorConfigurationContent(String fileName,
                                                                   AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * Method used enable or disable the statistics for a input event adaptor configuration
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     */
    public void setStatisticsEnabled(String eventAdaptorName,
                                     AxisConfiguration axisConfiguration, boolean flag)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * Method used to enable or disable tracing for input event adaptor configuration
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     */
    public void setTracingEnabled(String eventAdaptorName, AxisConfiguration axisConfiguration,
                                  boolean flag)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     * To get the list of information about the IN event adaptors
     *
     * @param tenantId tenant Id
     */
    public List<InputEventAdaptorInfo> getInputEventAdaptorInfo(int tenantId);

    /**
     * Method used to register a input event adaptor deployment notifier
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     */
    public void registerDeploymentNotifier(
            InputEventAdaptorNotificationListener inputEventAdaptorNotificationListener)
            throws InputEventAdaptorManagerConfigurationException;

    /**
     *
     * @param axisConfiguration
     * @return
     * @throws InputEventAdaptorManagerConfigurationException
     */
    public String getDefaultWso2EventAdaptor(AxisConfiguration axisConfiguration) throws InputEventAdaptorManagerConfigurationException;

    /**
     *
     * @return
     * @throws InputEventAdaptorManagerConfigurationException
     */
    public String getDefaultWso2EventAdaptor() throws InputEventAdaptorManagerConfigurationException;
}
