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

package org.wso2.carbon.event.output.adaptor.manager.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adaptor.core.config.InternalOutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.core.config.OutputEventAdaptorConfiguration;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorFile;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorInfo;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorNotificationListener;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.output.adaptor.manager.core.internal.ds.OutputEventAdaptorManagerValueHolder;
import org.wso2.carbon.event.output.adaptor.manager.core.internal.util.OutputEventAdaptorManagerConstants;
import org.wso2.carbon.event.output.adaptor.manager.core.internal.util.helper.OutputEventAdaptorConfigurationFilesystemInvoker;
import org.wso2.carbon.event.output.adaptor.manager.core.internal.util.helper.OutputEventAdaptorConfigurationHelper;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * carbon implementation of the event adaptor manager.
 */
public class CarbonOutputEventAdaptorManagerService
        implements OutputEventAdaptorManagerService {
    private static final Log log = LogFactory.getLog(CarbonOutputEventAdaptorManagerService.class);
    private static final String OUTPUT_EVENT_ADAPTOR = "Output Event Adaptor";
    /**
     * event adaptor configuration map to keep the event configuration details
     */

    //List that holds all the notification listeners
    public List<OutputEventAdaptorNotificationListener> outputEventAdaptorNotificationListener;
    //Holds all the output event adaptor configuration objects
    private Map<Integer, Map<String, OutputEventAdaptorConfiguration>> tenantSpecificEventAdaptorConfigurationMap;
    //Holds the minimum information about the output event adaptors
    private Map<Integer, Map<String, OutputEventAdaptorInfo>> tenantSpecificOutputEventAdaptorInfoMap;
    //Holds all the output event adaptor file info
    private Map<Integer, List<OutputEventAdaptorFile>> eventAdaptorFileMap;

    public CarbonOutputEventAdaptorManagerService() {
        tenantSpecificEventAdaptorConfigurationMap = new ConcurrentHashMap<Integer, Map<String, OutputEventAdaptorConfiguration>>();
        eventAdaptorFileMap = new ConcurrentHashMap<Integer, List<OutputEventAdaptorFile>>();
        tenantSpecificOutputEventAdaptorInfoMap = new HashMap<Integer, Map<String, OutputEventAdaptorInfo>>();
        outputEventAdaptorNotificationListener = new ArrayList<OutputEventAdaptorNotificationListener>();
    }

    @Override
    public void deployOutputEventAdaptorConfiguration(
            OutputEventAdaptorConfiguration eventAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {

        String eventAdaptorName = eventAdaptorConfiguration.getName();
        OMElement omElement = OutputEventAdaptorConfigurationHelper.eventAdaptorConfigurationToOM(eventAdaptorConfiguration);

        if (OutputEventAdaptorConfigurationHelper.validateEventAdaptorConfiguration(OutputEventAdaptorConfigurationHelper.fromOM(omElement))) {
            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new OutputEventAdaptorManagerConfigurationException("Cannot create directory to add tenant specific Output Event Adaptor :" + eventAdaptorName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + OutputEventAdaptorManagerConstants.OEA_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new OutputEventAdaptorManagerConfigurationException("Cannot create directory " + OutputEventAdaptorManagerConstants.OEA_ELE_DIRECTORY + " to add tenant specific Output Event Adaptor :" + eventAdaptorName);
                }
            }
            validateToRemoveInactiveEventAdaptorConfiguration(eventAdaptorName, axisConfiguration);
            OutputEventAdaptorConfigurationFilesystemInvoker.save(omElement, eventAdaptorName, eventAdaptorName + ".xml", axisConfiguration);
        } else {
            log.error("There is no Output Event Adaptor type called " + eventAdaptorConfiguration.getType() + " is available");
            throw new OutputEventAdaptorManagerConfigurationException("There is no Output Event Adaptor type called " + eventAdaptorConfiguration.getType() + " is available ");
        }
    }

    @Override
    public void undeployActiveOutputEventAdaptorConfiguration(String eventAdaptorName,
                                                              AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<OutputEventAdaptorFile> outputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
        if (outputEventAdaptorFileList != null) {
            for (OutputEventAdaptorFile outputEventAdaptorFile : outputEventAdaptorFileList) {

                if ((outputEventAdaptorFile.getEventAdaptorName().equals(eventAdaptorName))) {

                    OutputEventAdaptorConfigurationFilesystemInvoker.delete(outputEventAdaptorFile.getFileName(), axisConfiguration);
                    break;
                }
            }
        }
    }

    @Override
    public List<OutputEventAdaptorConfiguration> getAllActiveOutputEventAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {

        List<OutputEventAdaptorConfiguration> eventAdaptorConfigurations = new ArrayList<OutputEventAdaptorConfiguration>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventAdaptorConfigurationMap.get(tenantId) != null) {
            for (OutputEventAdaptorConfiguration eventAdaptorConfiguration : tenantSpecificEventAdaptorConfigurationMap.get(
                    tenantId).values()) {
                eventAdaptorConfigurations.add(eventAdaptorConfiguration);
            }
        }
        return eventAdaptorConfigurations;
    }

    @Override
    public OutputEventAdaptorConfiguration getActiveOutputEventAdaptorConfiguration(
            String name,
            int tenantId)
            throws OutputEventAdaptorManagerConfigurationException {

        if (tenantSpecificEventAdaptorConfigurationMap.get(tenantId) == null) {
            throw new OutputEventAdaptorManagerConfigurationException("There is no any configuration exists for " + tenantId);
        }
        return tenantSpecificEventAdaptorConfigurationMap.get(tenantId).get(name);
    }

    @Override
    public List<OutputEventAdaptorFile> getAllInactiveOutputEventAdaptorConfiguration(
            AxisConfiguration axisConfiguration) {
        List<OutputEventAdaptorFile> unDeployedOutputEventAdaptorFileList = new ArrayList<OutputEventAdaptorFile>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (eventAdaptorFileMap.get(tenantId) != null) {
            for (OutputEventAdaptorFile outputEventAdaptorFile : eventAdaptorFileMap.get(tenantId)) {
                if (!outputEventAdaptorFile.getStatus().equals(OutputEventAdaptorFile.Status.DEPLOYED)) {
                    unDeployedOutputEventAdaptorFileList.add(outputEventAdaptorFile);
                }
            }
        }
        return unDeployedOutputEventAdaptorFileList;
    }

    @Override
    public void undeployInactiveOutputEventAdaptorConfiguration(String fileName,
                                                                AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {

        OutputEventAdaptorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
    }

    @Override
    public void editActiveOutputEventAdaptorConfiguration(String eventAdaptorConfiguration,
                                                          String eventAdaptorName,
                                                          AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventAdaptorConfiguration);
            omElement.toString();
            OutputEventAdaptorConfiguration eventAdaptorConfigurationObject = OutputEventAdaptorConfigurationHelper.fromOM(omElement);
            if (!eventAdaptorConfigurationObject.getName().equals(eventAdaptorName)) {
                if (checkAdaptorValidity(tenantId, eventAdaptorConfigurationObject.getName())) {
                    validateEventAdaptorConfiguration(tenantId, eventAdaptorName, axisConfiguration, omElement);
                } else {
                    throw new OutputEventAdaptorManagerConfigurationException("There is a Output Event Adaptor already registered with the same name");
                }
            } else {
                validateEventAdaptorConfiguration(tenantId, eventAdaptorName, axisConfiguration, omElement);
            }

        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new OutputEventAdaptorManagerConfigurationException("Not a valid xml object, " + e.getMessage(), e);
        }
    }

    @Override
    public void editInactiveOutputEventAdaptorConfiguration(
            String eventAdaptorConfiguration,
            String filename,
            AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventAdaptorConfiguration);
            omElement.toString();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            OutputEventAdaptorConfiguration eventAdaptorConfigurationObject = OutputEventAdaptorConfigurationHelper.fromOM(omElement);
            if (checkAdaptorValidity(tenantId, eventAdaptorConfigurationObject.getName())) {
                if (OutputEventAdaptorConfigurationHelper.validateEventAdaptorConfiguration(eventAdaptorConfigurationObject)) {
                    undeployInactiveOutputEventAdaptorConfiguration(filename, axisConfiguration);
                    OutputEventAdaptorConfigurationFilesystemInvoker.save(omElement, eventAdaptorConfigurationObject.getName(), filename, axisConfiguration);
                } else {
                    log.error("There is no Output Event Adaptor type called " + eventAdaptorConfigurationObject.getType() + " is available");
                    throw new OutputEventAdaptorManagerConfigurationException("There is no Output Event Adaptor type called " + eventAdaptorConfigurationObject.getType() + " is available ");
                }
            } else {
                throw new OutputEventAdaptorManagerConfigurationException("There is a Output Event Adaptor with the same name");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new OutputEventAdaptorManagerConfigurationException("Not a valid xml object " + e.getMessage(), e);
        }
    }

    @Override
    public String getActiveOutputEventAdaptorConfigurationContent(String eventAdaptorName,
                                                                  AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {

        String fileName = "";
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<OutputEventAdaptorFile> outputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
        if (outputEventAdaptorFileList != null) {
            for (OutputEventAdaptorFile outputEventAdaptorFile : outputEventAdaptorFileList) {
                if ((outputEventAdaptorFile.getEventAdaptorName().equals(eventAdaptorName))) {
                    fileName = outputEventAdaptorFile.getFileName();
                }
            }
        }
        return OutputEventAdaptorConfigurationFilesystemInvoker.readEventAdaptorConfigurationFile(fileName, axisConfiguration);
    }

    @Override
    public String getInactiveOutputEventAdaptorConfigurationContent(String fileName,
                                                                    AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        return OutputEventAdaptorConfigurationFilesystemInvoker.readEventAdaptorConfigurationFile(fileName, axisConfiguration);
    }

    @Override
    public void setStatisticsEnabled(String eventAdaptorName,
                                     AxisConfiguration axisConfiguration, boolean flag)
            throws OutputEventAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        OutputEventAdaptorConfiguration outputEventAdaptorConfiguration = getActiveOutputEventAdaptorConfiguration(eventAdaptorName, tenantId);
        outputEventAdaptorConfiguration.setEnableStatistics(flag);
        editTracingStatistics(outputEventAdaptorConfiguration, eventAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void setTracingEnabled(String eventAdaptorName, AxisConfiguration axisConfiguration,
                                  boolean flag)
            throws OutputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        OutputEventAdaptorConfiguration outputEventAdaptorConfiguration = getActiveOutputEventAdaptorConfiguration(eventAdaptorName, tenantId);
        outputEventAdaptorConfiguration.setEnableTracing(flag);
        editTracingStatistics(outputEventAdaptorConfiguration, eventAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void registerDeploymentNotifier(
            OutputEventAdaptorNotificationListener outputEventAdaptorNotificationListener)
            throws OutputEventAdaptorManagerConfigurationException {
        this.outputEventAdaptorNotificationListener.add(outputEventAdaptorNotificationListener);

        notifyActiveEventAdaptorConfigurationFiles();
    }

    @Override
    public List<OutputEventAdaptorInfo> getOutputEventAdaptorInfo(int tenantId) {

        Map<String, OutputEventAdaptorInfo> outputEventAdaptorInfoMap = tenantSpecificOutputEventAdaptorInfoMap.get(tenantId);
        if (outputEventAdaptorInfoMap != null) {
            List<OutputEventAdaptorInfo> outputEventAdaptorInfoList = new ArrayList<OutputEventAdaptorInfo>();
            for (OutputEventAdaptorInfo outputEventAdaptorInfo : outputEventAdaptorInfoMap.values()) {
                outputEventAdaptorInfoList.add(outputEventAdaptorInfo);
            }

            return outputEventAdaptorInfoList;
        }
        return null;
    }

    @Override
    public String getDefaultWso2EventAdaptor(AxisConfiguration axisConfiguration) throws OutputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, OutputEventAdaptorInfo> outputEventAdaptorInfoMap = tenantSpecificOutputEventAdaptorInfoMap.get(tenantId);
        if (outputEventAdaptorInfoMap != null) {
            OutputEventAdaptorInfo defaultOutputEventAdaptorInfo = outputEventAdaptorInfoMap.get(OutputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_OUTPUT_ADAPTOR);
            if (defaultOutputEventAdaptorInfo != null && defaultOutputEventAdaptorInfo.getEventAdaptorType().equals(OutputEventAdaptorManagerConstants.ADAPTOR_TYPE_WSO2EVENT)) {
                return outputEventAdaptorInfoMap.get(OutputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_OUTPUT_ADAPTOR).getEventAdaptorName();
            }
        }

        // If execution reaches here, it means that the default adaptor did not exist.
        // So create a default adaptor and return the name.
        OutputEventAdaptorConfiguration outputEventAdaptorConfiguration = new OutputEventAdaptorConfiguration();
        outputEventAdaptorConfiguration.setName(OutputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_OUTPUT_ADAPTOR);
        outputEventAdaptorConfiguration.setType(OutputEventAdaptorManagerConstants.ADAPTOR_TYPE_WSO2EVENT);
        InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = new InternalOutputEventAdaptorConfiguration();
        internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(OutputEventAdaptorManagerConstants.ADAPTOR_MESSAGE_RECEIVER_URL, OutputEventAdaptorManagerConstants.DEFAULT_THRIFT_RECEIVER_URL);
        internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(OutputEventAdaptorManagerConstants.ADAPTOR_MESSAGE_AUTHENTICATOR_URL, OutputEventAdaptorManagerConstants.DEFAULT_THRIFT_AUTHENTICATOR_URL);
        internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(OutputEventAdaptorManagerConstants.ADAPTOR_MESSAGE_USERNAME, OutputEventAdaptorManagerConstants.DEFAULT_THRIFT_USERNAME);
        internalOutputEventAdaptorConfiguration.addEventAdaptorProperty(OutputEventAdaptorManagerConstants.ADAPTOR_MESSAGE_PASSWORD, OutputEventAdaptorManagerConstants.DEFAULT_THRIFT_PASSWORD);
        outputEventAdaptorConfiguration.setOutputConfiguration(internalOutputEventAdaptorConfiguration);
        deployOutputEventAdaptorConfiguration(outputEventAdaptorConfiguration, axisConfiguration);

        return outputEventAdaptorConfiguration.getName();
    }

    //Non-Interface public methods below

    /**
     * This method is used to store all the deployed and non-deployed event adaptors in a map
     * (A flag used to uniquely identity whether the event adaptor deployed or not
     */
    public void addOutputEventAdaptorConfigurationFile(int tenantId,
                                                       OutputEventAdaptorFile outputEventAdaptorFile) {

        List<OutputEventAdaptorFile> outputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);

        if (outputEventAdaptorFileList == null) {
            outputEventAdaptorFileList = new ArrayList<OutputEventAdaptorFile>();
        } else {
            for (OutputEventAdaptorFile anOutputEventAdaptorFileList : outputEventAdaptorFileList) {
                if (anOutputEventAdaptorFileList.getFileName().equals(outputEventAdaptorFile.getFileName())) {
                    return;
                }
            }
        }
        outputEventAdaptorFileList.add(outputEventAdaptorFile);
        eventAdaptorFileMap.put(tenantId, outputEventAdaptorFileList);


    }

    /**
     * To check whether there is a Event adaptor with the same name
     */
    public boolean checkAdaptorValidity(int tenantId, String eventAdaptorName) {

        if (eventAdaptorFileMap.size() > 0) {
            List<OutputEventAdaptorFile> outputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
            if (outputEventAdaptorFileList != null) {
                for (OutputEventAdaptorFile outputEventAdaptorFile : outputEventAdaptorFileList) {
                    if ((outputEventAdaptorFile.getEventAdaptorName().equals(eventAdaptorName)) && (outputEventAdaptorFile.getStatus().equals(OutputEventAdaptorFile.Status.DEPLOYED))) {
                        log.error("Event adaptor " + eventAdaptorName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * to remove the deployed and not-deployed the Event adaptor configuration from the map.
     */
    public void removeOutputEventAdaptorConfiguration(String fileName, int tenantId) {
        List<OutputEventAdaptorFile> outputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
        if (outputEventAdaptorFileList != null) {
            for (OutputEventAdaptorFile outputEventAdaptorFile : outputEventAdaptorFileList) {
                if ((outputEventAdaptorFile.getFileName().equals(fileName))) {
                    if (outputEventAdaptorFile.getStatus().equals(OutputEventAdaptorFile.Status.DEPLOYED)) {
                        String eventAdaptorName = outputEventAdaptorFile.getEventAdaptorName();
                        if (tenantSpecificEventAdaptorConfigurationMap.get(tenantId) != null) {
                            tenantSpecificEventAdaptorConfigurationMap.get(tenantId).remove(eventAdaptorName);
                        }

                        removeFromTenantSpecificEventAdaptorInfoMap(tenantId, eventAdaptorName);
                        Iterator<OutputEventAdaptorNotificationListener> deploymentListenerIterator = outputEventAdaptorNotificationListener.iterator();
                        while (deploymentListenerIterator.hasNext()) {
                            OutputEventAdaptorNotificationListener outputEventAdaptorNotificationListener = deploymentListenerIterator.next();
                            outputEventAdaptorNotificationListener.configurationRemoved(tenantId, eventAdaptorName);
                        }
                    }
                    outputEventAdaptorFileList.remove(outputEventAdaptorFile);
                    return;
                }
            }
        }
    }

    /**
     * to add to the tenant specific event adaptor configuration map (only the correctly deployed event adaptors)
     *
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    public void addOutputEventAdaptorConfiguration(
            int tenantId, OutputEventAdaptorConfiguration eventAdaptorConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        Map<String, OutputEventAdaptorConfiguration> eventAdaptorConfigurationMap =
                tenantSpecificEventAdaptorConfigurationMap.get(tenantId);

        if (eventAdaptorConfiguration.isEnableStatistics()) {
            OutputEventAdaptorManagerValueHolder.getEventStatisticsService().getEventStatisticMonitor(tenantId, OUTPUT_EVENT_ADAPTOR, eventAdaptorConfiguration.getName(), null);
        }
        if (eventAdaptorConfigurationMap == null) {
            eventAdaptorConfigurationMap = new ConcurrentHashMap<String, OutputEventAdaptorConfiguration>();
            eventAdaptorConfigurationMap.put(eventAdaptorConfiguration.getName(), eventAdaptorConfiguration);
            tenantSpecificEventAdaptorConfigurationMap.put(tenantId, eventAdaptorConfigurationMap);
        } else {
            eventAdaptorConfigurationMap.put(eventAdaptorConfiguration.getName(), eventAdaptorConfiguration);
        }
        addToTenantSpecificEventAdaptorInfoMap(tenantId, eventAdaptorConfiguration);
    }

    public void activateInactiveOutputEventAdaptorConfiguration()
            throws OutputEventAdaptorManagerConfigurationException {

        List<OutputEventAdaptorFile> outputEventAdaptorFiles = new ArrayList<OutputEventAdaptorFile>();

        if (eventAdaptorFileMap != null && eventAdaptorFileMap.size() > 0) {

            Iterator eventAdaptorEntryIterator = eventAdaptorFileMap.entrySet().iterator();
            while (eventAdaptorEntryIterator.hasNext()) {
                Map.Entry eventAdaptorEntry = (Map.Entry) eventAdaptorEntryIterator.next();
                Iterator<OutputEventAdaptorFile> eventAdaptorFileIterator = ((List<OutputEventAdaptorFile>) eventAdaptorEntry.getValue()).iterator();
                while (eventAdaptorFileIterator.hasNext()) {
                    OutputEventAdaptorFile outputEventAdaptorFile = eventAdaptorFileIterator.next();
                    if (outputEventAdaptorFile.getStatus().equals(OutputEventAdaptorFile.Status.WAITING_FOR_DEPENDENCY)) {
                        outputEventAdaptorFiles.add(outputEventAdaptorFile);
                    }
                }
            }
        }

        for (OutputEventAdaptorFile outputEventAdaptorFile : outputEventAdaptorFiles) {
            try {
                OutputEventAdaptorConfigurationFilesystemInvoker.reload(outputEventAdaptorFile.getFileName(), outputEventAdaptorFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Output Event Adaptor configuration file : " + new File(outputEventAdaptorFile.getFileName()).getName());
            }
        }
    }

    public void notifyActiveEventAdaptorConfigurationFiles()
            throws OutputEventAdaptorManagerConfigurationException {

        if (eventAdaptorFileMap != null && eventAdaptorFileMap.size() > 0) {
            Iterator eventAdaptorEntryIterator = eventAdaptorFileMap.entrySet().iterator();
            while (eventAdaptorEntryIterator.hasNext()) {
                Map.Entry eventAdaptorEntry = (Map.Entry) eventAdaptorEntryIterator.next();
                Iterator<OutputEventAdaptorFile> eventAdaptorFileIterator = ((List<OutputEventAdaptorFile>) eventAdaptorEntry.getValue()).iterator();
                while (eventAdaptorFileIterator.hasNext()) {
                    OutputEventAdaptorFile outputEventAdaptorFile = eventAdaptorFileIterator.next();
                    if (outputEventAdaptorFile.getStatus().equals(OutputEventAdaptorFile.Status.DEPLOYED)) {
                        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                        Iterator<OutputEventAdaptorNotificationListener> deploymentListenerIterator = outputEventAdaptorNotificationListener.iterator();
                        while (deploymentListenerIterator.hasNext()) {
                            OutputEventAdaptorNotificationListener outputEventAdaptorNotificationListener = deploymentListenerIterator.next();
                            outputEventAdaptorNotificationListener.configurationAdded(tenantId, outputEventAdaptorFile.getEventAdaptorName());
                        }
                    }
                }
            }
        }
    }

    //Private methods are below
    private void editTracingStatistics(
            OutputEventAdaptorConfiguration outputEventAdaptorConfiguration,
            String eventAdaptorName, int tenantId, AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {

        String fileName = getFileName(tenantId, eventAdaptorName);
        undeployActiveOutputEventAdaptorConfiguration(eventAdaptorName, axisConfiguration);
        OMElement omElement = OutputEventAdaptorConfigurationHelper.eventAdaptorConfigurationToOM(outputEventAdaptorConfiguration);
        OutputEventAdaptorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
        OutputEventAdaptorConfigurationFilesystemInvoker.save(omElement, eventAdaptorName, fileName, axisConfiguration);
    }

    /**
     * to add to the output and output adaptor maps that gives information which event adaptors supports
     * for event adaptor configuration
     *
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    private void addToTenantSpecificEventAdaptorInfoMap(int tenantId,
                                                        OutputEventAdaptorConfiguration eventAdaptorConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {


        if (eventAdaptorConfiguration.getOutputConfiguration() != null) {
            OutputEventAdaptorInfo outputEventAdaptorInfo = new OutputEventAdaptorInfo();
            outputEventAdaptorInfo.setEventAdaptorName(eventAdaptorConfiguration.getName());
            outputEventAdaptorInfo.setEventAdaptorType(eventAdaptorConfiguration.getType());
            Map<String, OutputEventAdaptorInfo> eventAdaptorInfoMap = tenantSpecificOutputEventAdaptorInfoMap.get(tenantId);

            if (eventAdaptorInfoMap != null) {

                eventAdaptorInfoMap.put(eventAdaptorConfiguration.getName(), outputEventAdaptorInfo);
            } else {
                eventAdaptorInfoMap = new HashMap<String, OutputEventAdaptorInfo>();
                eventAdaptorInfoMap.put(eventAdaptorConfiguration.getName(), outputEventAdaptorInfo);
            }
            tenantSpecificOutputEventAdaptorInfoMap.put(tenantId, eventAdaptorInfoMap);
        }

    }

    /**
     * to remove the event adaptor configuration when deployed from the map after un-deploy
     */
    private void removeFromTenantSpecificEventAdaptorInfoMap(int tenantId,
                                                             String eventAdaptorName) {

        Map<String, OutputEventAdaptorInfo> outputEventAdaptorInfoMap = tenantSpecificOutputEventAdaptorInfoMap.get(tenantId);

        if (outputEventAdaptorInfoMap != null && outputEventAdaptorInfoMap.containsKey(eventAdaptorName)) {
            outputEventAdaptorInfoMap.remove(eventAdaptorName);
        }
    }

    /**
     * to get the file path of a event adaptor
     */
    private String getFileName(int tenantId, String eventAdaptorName) {

        if (eventAdaptorFileMap.size() > 0) {
            List<OutputEventAdaptorFile> outputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
            if (outputEventAdaptorFileList != null) {
                for (OutputEventAdaptorFile outputEventAdaptorFile : outputEventAdaptorFileList) {
                    if ((outputEventAdaptorFile.getEventAdaptorName().equals(eventAdaptorName)) && outputEventAdaptorFile.getStatus().equals(OutputEventAdaptorFile.Status.DEPLOYED)) {
                        return outputEventAdaptorFile.getFileName();
                    }
                }
            }
        }
        return null;
    }

    /**
     * this stores the event adaptor configuration to the file system after validating the event adaptor when doing editing
     *
     * @throws org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException
     *
     */
    private void validateEventAdaptorConfiguration(int tenantId, String eventAdaptorName,
                                                   AxisConfiguration axisConfiguration,
                                                   OMElement omElement)
            throws OutputEventAdaptorManagerConfigurationException {
        OutputEventAdaptorConfiguration eventAdaptorConfiguration = OutputEventAdaptorConfigurationHelper.fromOM(omElement);
        if (OutputEventAdaptorConfigurationHelper.validateEventAdaptorConfiguration(eventAdaptorConfiguration)) {
            String fileName = getFileName(tenantId, eventAdaptorName);
            if (fileName == null) {
                fileName = eventAdaptorName + OutputEventAdaptorManagerConstants.OEA_CONFIG_FILE_EXTENSION_WITH_DOT;
            }
            OutputEventAdaptorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
            OutputEventAdaptorConfigurationFilesystemInvoker.save(omElement, eventAdaptorName, fileName, axisConfiguration);
        } else {
            log.error("There is no Output Event Adaptor type called " + eventAdaptorConfiguration.getType() + " is available ");
            throw new OutputEventAdaptorManagerConfigurationException("There is no Output Event Adaptor type called " + eventAdaptorConfiguration.getType() + " is available ");
        }
    }

    private void validateToRemoveInactiveEventAdaptorConfiguration(String adaptorName, AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = adaptorName + OutputEventAdaptorManagerConstants.OEA_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<OutputEventAdaptorFile> inputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
        if (inputEventAdaptorFileList != null) {
            for (OutputEventAdaptorFile outputEventAdaptorFile : inputEventAdaptorFileList) {
                if ((outputEventAdaptorFile.getFileName().equals(fileName))) {
                    if (!(outputEventAdaptorFile.getStatus().equals(OutputEventAdaptorFile.Status.DEPLOYED))) {
                        OutputEventAdaptorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
                        break;
                    }
                }
            }
        }

    }

}