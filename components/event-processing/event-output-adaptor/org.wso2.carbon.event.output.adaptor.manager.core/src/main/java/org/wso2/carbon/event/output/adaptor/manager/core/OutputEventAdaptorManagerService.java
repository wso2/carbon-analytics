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

package org.wso2.carbon.event.output.adaptor.manager.core;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;

import java.util.List;

public interface OutputEventAdaptorManagerService {

    /**
     * use to add a new Event adaptor Configuration instance to the system. A event adaptor Configuration instance represents the
     * details of a particular event adaptor connection details.
     */

    public void deployOutputEventAdaptorConfiguration(
            OutputEventAdaptorConfiguration eventAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * removes the event adaptor configuration instance from the system.
     */
    public void undeployActiveOutputEventAdaptorConfiguration(String name,
                                                              AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * getting all the event adaptor proxy instance deatils. this is used to dispaly all the
     * event adaptor configuration instances.
     *
     * @return - list of available event adaptor configuration
     */
    public List<OutputEventAdaptorConfiguration> getAllActiveOutputEventAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException;


    /**
     * retuns the event adaptor configuration for the given name
     *
     * @return - event adaptor configuration
     */
    public OutputEventAdaptorConfiguration getActiveOutputEventAdaptorConfiguration(
            String name,
            int tenantId)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * @param axisConfiguration - Axis2 Configuration Object
     * @return List of OutputEventAdaptorFile
     */
    public List<OutputEventAdaptorFile> getAllInactiveOutputEventAdaptorConfiguration(
            AxisConfiguration axisConfiguration);

    /**
     * This method used to delete the un-deployed Event adaptor Adaptor File
     *
     * @param fileName fileName of the Adaptor File that going to be deleted
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public void undeployInactiveOutputEventAdaptorConfiguration(String fileName,
                                                                AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * To edit a event adaptor configuration
     *
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public void editActiveOutputEventAdaptorConfiguration(String eventAdaptorConfiguration,
                                                          String eventAdaptorName,
                                                          AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException;


    /**
     * Used to save not deployed event adaptor configuration details
     *
     * @param eventAdaptorConfiguration configuration details
     * @param fileName                      file path of the adaptor
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public void editInactiveOutputEventAdaptorConfiguration(
            String eventAdaptorConfiguration,
            String fileName,
            AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * Use to get the event adaptor configuration file
     *
     * @param eventAdaptorName event adaptor name
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public String getActiveOutputEventAdaptorConfigurationContent(String eventAdaptorName,
                                                                  AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * get the event adaptor file of the not deployed adaptor
     *
     *
     * @param fileName file path of the not deployed event adaptor
     * @param axisConfiguration
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public String getInactiveOutputEventAdaptorConfigurationContent(String fileName,
                                                                    AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * Method used to enable or disable the statistics for a output event adaptor configuration
     *
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public void setStatisticsEnabled(String eventAdaptorName,
                                     AxisConfiguration axisConfiguration, boolean flag)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * Method used to enable or disable the tracing for a output event adaptor configuration
     *
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public void setTracingEnabled(String eventAdaptorName, AxisConfiguration axisConfiguration,
                                  boolean flag)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * Method used to register output event adaptor notification listener object
     *
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public void registerDeploymentNotifier(
            OutputEventAdaptorNotificationListener outputEventAdaptorNotificationListener)
            throws OutputEventAdaptorManagerConfigurationException;

    /**
     * To get the list of information about the OUT event adaptors
     */
    public List<OutputEventAdaptorInfo> getOutputEventAdaptorInfo(int tenantId);

    public String getDefaultWso2EventAdaptor(AxisConfiguration axisConfiguration) throws OutputEventAdaptorManagerConfigurationException;
}
