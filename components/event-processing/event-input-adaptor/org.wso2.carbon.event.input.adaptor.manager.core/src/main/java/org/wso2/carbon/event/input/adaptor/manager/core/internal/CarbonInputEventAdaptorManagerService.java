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

package org.wso2.carbon.event.input.adaptor.manager.core.internal;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.core.config.InternalInputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorFile;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorInfo;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorNotificationListener;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.util.InputEventAdaptorManagerConstants;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.util.helper.InputEventAdaptorConfigurationFilesystemInvoker;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.util.helper.InputEventAdaptorConfigurationHelper;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * carbon implementation of the event adaptor manager.
 */
public class CarbonInputEventAdaptorManagerService
        implements InputEventAdaptorManagerService {
    private static final Log log = LogFactory.getLog(CarbonInputEventAdaptorManagerService.class);
    //To hold the list of input event adaptor listeners
    public List<InputEventAdaptorNotificationListener> inputEventAdaptorNotificationListener;
    //To hold the active input event adaptor configuration objects
    private Map<Integer, Map<String, InputEventAdaptorConfiguration>> tenantSpecificEventAdaptorConfigurationMap;
    //To hold the information regarding the input events adaptors
    private Map<Integer, Map<String, InputEventAdaptorInfo>> tenantSpecificInputEventAdaptorInfoMap;
    //To hold input event adaptor file information
    private Map<Integer, List<InputEventAdaptorFile>> eventAdaptorFileMap;

    public CarbonInputEventAdaptorManagerService() {
        tenantSpecificEventAdaptorConfigurationMap = new ConcurrentHashMap<Integer, Map<String, InputEventAdaptorConfiguration>>();
        eventAdaptorFileMap = new ConcurrentHashMap<Integer, List<InputEventAdaptorFile>>();
        tenantSpecificInputEventAdaptorInfoMap = new HashMap<Integer, Map<String, InputEventAdaptorInfo>>();
        inputEventAdaptorNotificationListener = new ArrayList<InputEventAdaptorNotificationListener>();
    }

    public void deployInputEventAdaptorConfiguration(
            InputEventAdaptorConfiguration eventAdaptorConfiguration,
            AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        String eventAdaptorName = eventAdaptorConfiguration.getName();
        OMElement omElement = InputEventAdaptorConfigurationHelper.eventAdaptorConfigurationToOM(eventAdaptorConfiguration);

        if (InputEventAdaptorConfigurationHelper.validateEventAdaptorConfiguration(InputEventAdaptorConfigurationHelper.fromOM(omElement))) {
            File directory = new File(axisConfiguration.getRepository().getPath());
            if (!directory.exists()) {
                if (directory.mkdir()) {
                    throw new InputEventAdaptorManagerConfigurationException("Cannot create directory to add tenant specific Input Event Adaptor :" + eventAdaptorName);
                }
            }
            directory = new File(directory.getAbsolutePath() + File.separator + InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    throw new InputEventAdaptorManagerConfigurationException("Cannot create directory " + InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY + " to add tenant specific Input Event Adaptor :" + eventAdaptorName);
                }
            }

            validateToRemoveInactiveEventAdaptorConfiguration(eventAdaptorName, axisConfiguration);
            InputEventAdaptorConfigurationFilesystemInvoker.save(omElement, eventAdaptorName, eventAdaptorName + ".xml", axisConfiguration);

        } else {
            log.error("There is no Input Event Adaptor type called " + eventAdaptorConfiguration.getType() + " is available");
            throw new InputEventAdaptorManagerConfigurationException("There is no Input Event Adaptor type called " + eventAdaptorConfiguration.getType() + " is available ");
        }
    }

    public void undeployActiveInputEventAdaptorConfiguration(String eventAdaptorName,
                                                             AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<InputEventAdaptorFile> inputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
        if (inputEventAdaptorFileList != null) {
            for (InputEventAdaptorFile inputEventAdaptorFile : inputEventAdaptorFileList) {
                if ((inputEventAdaptorFile.getEventAdaptorName().equals(eventAdaptorName))) {
                    InputEventAdaptorConfigurationFilesystemInvoker.delete(inputEventAdaptorFile.getFileName(), axisConfiguration);
                    break;
                }
            }
        }
    }

    public void undeployInactiveInputEventAdaptorConfiguration(String fileName,
                                                               AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        InputEventAdaptorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
    }

    public void editActiveInputEventAdaptorConfiguration(String eventAdaptorConfiguration,
                                                         String eventAdaptorName,
                                                         AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventAdaptorConfiguration);
            omElement.toString();
            InputEventAdaptorConfiguration eventAdaptorConfigurationObject = InputEventAdaptorConfigurationHelper.fromOM(omElement);
            if (!eventAdaptorConfigurationObject.getName().equals(eventAdaptorName)) {
                if (checkAdaptorValidity(tenantId, eventAdaptorConfigurationObject.getName())) {
                    validateToEditEventAdaptorConfiguration(tenantId, eventAdaptorName, axisConfiguration, omElement);
                } else {
                    throw new InputEventAdaptorManagerConfigurationException("There is a event adaptor already registered with the same name");
                }
            } else {
                validateToEditEventAdaptorConfiguration(tenantId, eventAdaptorName, axisConfiguration, omElement);
            }

        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new InputEventAdaptorManagerConfigurationException("Not a valid xml object, " + e.getMessage(), e);
        }
    }

    public void editInactiveInputEventAdaptorConfiguration(
            String eventAdaptorConfiguration,
            String filename,
            AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        try {
            OMElement omElement = AXIOMUtil.stringToOM(eventAdaptorConfiguration);
            omElement.toString();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            InputEventAdaptorConfiguration eventAdaptorConfigurationObject = InputEventAdaptorConfigurationHelper.fromOM(omElement);
            if (checkAdaptorValidity(tenantId, eventAdaptorConfigurationObject.getName())) {
                if (InputEventAdaptorConfigurationHelper.validateEventAdaptorConfiguration(eventAdaptorConfigurationObject)) {
                    undeployInactiveInputEventAdaptorConfiguration(filename, axisConfiguration);
                    InputEventAdaptorConfigurationFilesystemInvoker.save(omElement, eventAdaptorConfigurationObject.getName(), filename, axisConfiguration);
                } else {
                    log.error("There is no event adaptor type called " + eventAdaptorConfigurationObject.getType() + " is available");
                    throw new InputEventAdaptorManagerConfigurationException("There is no Input Event Adaptor type called "
                            + eventAdaptorConfigurationObject.getType() + " is available ");
                }
            } else {
                throw new InputEventAdaptorManagerConfigurationException("There is a Input Event Adaptor with the same name");
            }
        } catch (XMLStreamException e) {
            log.error("Error while creating the xml object");
            throw new InputEventAdaptorManagerConfigurationException("Not a valid xml object " + e.getMessage(), e);
        }
    }

    public List<InputEventAdaptorConfiguration> getAllActiveInputEventAdaptorConfiguration(
            AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        List<InputEventAdaptorConfiguration> eventAdaptorConfigurations = new ArrayList<InputEventAdaptorConfiguration>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (tenantSpecificEventAdaptorConfigurationMap.get(tenantId) != null) {
            for (InputEventAdaptorConfiguration eventAdaptorConfiguration : tenantSpecificEventAdaptorConfigurationMap.get(
                    tenantId).values()) {
                eventAdaptorConfigurations.add(eventAdaptorConfiguration);
            }
        }
        return eventAdaptorConfigurations;
    }

    @Override
    public InputEventAdaptorConfiguration getActiveInputEventAdaptorConfiguration(
            String name,
            int tenantId)
            throws InputEventAdaptorManagerConfigurationException {

        if (tenantSpecificEventAdaptorConfigurationMap.get(tenantId) == null) {
            return null;
        }
        return tenantSpecificEventAdaptorConfigurationMap.get(tenantId).get(name);
    }

    @Override
    public List<InputEventAdaptorFile> getAllInactiveInputEventAdaptorConfiguration(
            AxisConfiguration axisConfiguration) {

        List<InputEventAdaptorFile> unDeployedInputEventAdaptorFileList = new ArrayList<InputEventAdaptorFile>();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (eventAdaptorFileMap.get(tenantId) != null) {
            for (InputEventAdaptorFile inputEventAdaptorFile : eventAdaptorFileMap.get(tenantId)) {
                if (!inputEventAdaptorFile.getStatus().equals(InputEventAdaptorFile.Status.DEPLOYED)) {
                    unDeployedInputEventAdaptorFileList.add(inputEventAdaptorFile);
                }
            }
        }
        return unDeployedInputEventAdaptorFileList;
    }

    @Override
    public String getActiveInputEventAdaptorConfigurationContent(String eventAdaptorName,
                                                                 AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String fileName = getFileName(tenantId, eventAdaptorName);
        if (fileName != null) {
            return InputEventAdaptorConfigurationFilesystemInvoker.readEventAdaptorConfigurationFile(fileName, axisConfiguration);
        } else {
            throw new InputEventAdaptorManagerConfigurationException("Error while retrieving the Input Event Adaptor configuration : " + eventAdaptorName);
        }
    }

    public String getInactiveInputEventAdaptorConfigurationContent(String fileName,
                                                                   AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        return InputEventAdaptorConfigurationFilesystemInvoker.readEventAdaptorConfigurationFile(fileName, axisConfiguration);
    }

    @Override
    public void setStatisticsEnabled(String eventAdaptorName,
                                     AxisConfiguration axisConfiguration, boolean flag)
            throws InputEventAdaptorManagerConfigurationException {

        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = getActiveInputEventAdaptorConfiguration(eventAdaptorName, tenantId);
        inputEventAdaptorConfiguration.setEnableStatistics(flag);
        editTracingStatistics(inputEventAdaptorConfiguration, eventAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public void setTracingEnabled(String eventAdaptorName, AxisConfiguration axisConfiguration,
                                  boolean flag)
            throws InputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = getActiveInputEventAdaptorConfiguration(eventAdaptorName, tenantId);
        inputEventAdaptorConfiguration.setEnableTracing(flag);
        editTracingStatistics(inputEventAdaptorConfiguration, eventAdaptorName, tenantId, axisConfiguration);
    }

    @Override
    public List<InputEventAdaptorInfo> getInputEventAdaptorInfo(int tenantId) {

        Map<String, InputEventAdaptorInfo> inputEventAdaptorInfoMap = tenantSpecificInputEventAdaptorInfoMap.get(tenantId);

        if (inputEventAdaptorInfoMap != null) {
            List<InputEventAdaptorInfo> inputEventAdaptorInfoList = new ArrayList<InputEventAdaptorInfo>();
            for (InputEventAdaptorInfo inputEventAdaptorInfo : inputEventAdaptorInfoMap.values()) {
                inputEventAdaptorInfoList.add(inputEventAdaptorInfo);
            }
            return inputEventAdaptorInfoList;
        }
        return null;
    }

    public void registerDeploymentNotifier(
            InputEventAdaptorNotificationListener inputEventAdaptorNotificationListener)
            throws InputEventAdaptorManagerConfigurationException {
        this.inputEventAdaptorNotificationListener.add(inputEventAdaptorNotificationListener);

        notifyActiveEventAdaptorConfigurationFiles();
    }

    @Override
    public String getDefaultWso2EventAdaptor(AxisConfiguration axisConfiguration) throws InputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, InputEventAdaptorInfo> inputEventAdaptorInfoMap = tenantSpecificInputEventAdaptorInfoMap.get(tenantId);
        if (inputEventAdaptorInfoMap != null) {
            InputEventAdaptorInfo defaultInputEventAdaptorInfo = inputEventAdaptorInfoMap.get(InputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_INPUT_ADAPTOR);
            if (defaultInputEventAdaptorInfo != null && defaultInputEventAdaptorInfo.getEventAdaptorType().equals(InputEventAdaptorManagerConstants.ADAPTOR_TYPE_WSO2EVENT)) {
                return inputEventAdaptorInfoMap.get(InputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_INPUT_ADAPTOR).getEventAdaptorName();
            }
        }

        // If execution reaches here, it means that the default adaptor did not exist.
        // So create a default adaptor and return the name.
        InputEventAdaptorConfiguration inputEventAdaptorConfiguration = new InputEventAdaptorConfiguration();
        inputEventAdaptorConfiguration.setName(InputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_INPUT_ADAPTOR);
        inputEventAdaptorConfiguration.setType(InputEventAdaptorManagerConstants.ADAPTOR_TYPE_WSO2EVENT);
        InternalInputEventAdaptorConfiguration internalInputEventAdaptorConfiguration = new InternalInputEventAdaptorConfiguration();
        inputEventAdaptorConfiguration.setInputConfiguration(internalInputEventAdaptorConfiguration);
        String filename = inputEventAdaptorConfiguration.getName() + InputEventAdaptorManagerConstants.IEA_CONFIG_FILE_EXTENSION_WITH_DOT;
        if(!InputEventAdaptorConfigurationFilesystemInvoker.isFileExists(filename, axisConfiguration)) {
            deployInputEventAdaptorConfiguration(inputEventAdaptorConfiguration, axisConfiguration);
        }

        return inputEventAdaptorConfiguration.getName();
    }

    @Override
    public String getDefaultWso2EventAdaptor() throws InputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        Map<String, InputEventAdaptorInfo> inputEventAdaptorInfoMap = tenantSpecificInputEventAdaptorInfoMap.get(tenantId);
        if (inputEventAdaptorInfoMap != null) {
            InputEventAdaptorInfo defaultInputEventAdaptorInfo = inputEventAdaptorInfoMap.get(InputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_INPUT_ADAPTOR);
            if (defaultInputEventAdaptorInfo != null && defaultInputEventAdaptorInfo.getEventAdaptorType().equals(InputEventAdaptorManagerConstants.ADAPTOR_TYPE_WSO2EVENT)) {
                return inputEventAdaptorInfoMap.get(InputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_INPUT_ADAPTOR).getEventAdaptorName();
            }
        }

        return InputEventAdaptorManagerConstants.DEFAULT_WSO2EVENT_INPUT_ADAPTOR;
    }

    //Non-Interface public methods

    /**
     * This method is used to store all the deployed and non-deployed event adaptors in a map
     * (A flag used to uniquely identity whether the event adaptor deployed or not
     */
    public void addInputEventAdaptorConfigurationFile(int tenantId,
                                                      InputEventAdaptorFile inputEventAdaptorFile) {

        List<InputEventAdaptorFile> inputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);

        if (inputEventAdaptorFileList == null) {
            inputEventAdaptorFileList = new ArrayList<InputEventAdaptorFile>();
        } else {
            for (InputEventAdaptorFile anInputEventAdaptorFileList : inputEventAdaptorFileList) {
                if (anInputEventAdaptorFileList.getFileName().equals(inputEventAdaptorFile.getFileName())) {
                    return;
                }
            }
        }
        inputEventAdaptorFileList.add(inputEventAdaptorFile);
        eventAdaptorFileMap.put(tenantId, inputEventAdaptorFileList);

    }

    /**
     * to add to the tenant specific event adaptor configuration map (only the correctly deployed event adaptors)
     *
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     *
     */
    public void addInputEventAdaptorConfiguration(
            int tenantId, InputEventAdaptorConfiguration eventAdaptorConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        Map<String, InputEventAdaptorConfiguration> eventAdaptorConfigurationMap
                = tenantSpecificEventAdaptorConfigurationMap.get(tenantId);

        if (eventAdaptorConfigurationMap == null) {
            eventAdaptorConfigurationMap = new ConcurrentHashMap<String, InputEventAdaptorConfiguration>();
            eventAdaptorConfigurationMap.put(eventAdaptorConfiguration.getName(), eventAdaptorConfiguration);
            tenantSpecificEventAdaptorConfigurationMap.put(tenantId, eventAdaptorConfigurationMap);
        } else {
            eventAdaptorConfigurationMap.put(eventAdaptorConfiguration.getName(), eventAdaptorConfiguration);
        }
        addToTenantSpecificEventAdaptorInfoMap(tenantId, eventAdaptorConfiguration);
    }

    /**
     * To check whether there is a event adaptor with the same name
     */
    public boolean checkAdaptorValidity(int tenantId, String eventAdaptorName) {

        if (eventAdaptorFileMap.size() > 0) {
            List<InputEventAdaptorFile> inputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
            if (inputEventAdaptorFileList != null) {
                for (InputEventAdaptorFile inputEventAdaptorFile : inputEventAdaptorFileList) {
                    if ((inputEventAdaptorFile.getEventAdaptorName().equals(eventAdaptorName)) && (inputEventAdaptorFile.getStatus().equals(InputEventAdaptorFile.Status.DEPLOYED))) {
                        log.error("Input Event adaptor " + eventAdaptorName + " is already registered with this tenant");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * to remove the deployed and not-deployed the event adaptor configuration from the map.
     */
    public void removeInputEventAdaptorConfiguration(String fileName, int tenantId) {
        List<InputEventAdaptorFile> inputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
        if (inputEventAdaptorFileList != null) {
            for (InputEventAdaptorFile inputEventAdaptorFile : inputEventAdaptorFileList) {
                if ((inputEventAdaptorFile.getFileName().equals(fileName))) {
                    if (inputEventAdaptorFile.getStatus().equals(InputEventAdaptorFile.Status.DEPLOYED)) {
                        String eventAdaptorName = inputEventAdaptorFile.getEventAdaptorName();
                        removeFromTenantSpecificEventAdaptorInfoMap(tenantId, eventAdaptorName);
                        Iterator<InputEventAdaptorNotificationListener> deploymentListenerIterator = inputEventAdaptorNotificationListener.iterator();
                        // Remove from configuration map and then notify listeners of removal
                        InputEventAdaptorConfiguration removedITAConfiguration = null;
                        if (tenantSpecificEventAdaptorConfigurationMap.get(tenantId) != null) {
                            removedITAConfiguration = tenantSpecificEventAdaptorConfigurationMap.get(tenantId).remove(eventAdaptorName);
                        }
                        while (deploymentListenerIterator.hasNext()) {
                            InputEventAdaptorNotificationListener inputEventAdaptorNotificationListener = deploymentListenerIterator.next();
                            inputEventAdaptorNotificationListener.configurationRemoved(tenantId, removedITAConfiguration);
                        }
                    }
                    inputEventAdaptorFileList.remove(inputEventAdaptorFile);
                    break;
                }
            }
        }
    }

    public void activateInactiveInputEventAdaptorConfiguration()
            throws InputEventAdaptorManagerConfigurationException {

        List<InputEventAdaptorFile> inputEventAdaptorFiles = new ArrayList<InputEventAdaptorFile>();

        if (eventAdaptorFileMap != null && eventAdaptorFileMap.size() > 0) {

            for (Map.Entry<Integer, List<InputEventAdaptorFile>> integerListEntry : eventAdaptorFileMap.entrySet()) {
                Map.Entry eventAdaptorEntry = (Map.Entry) integerListEntry;
                Iterator<InputEventAdaptorFile> eventAdaptorFileIterator = ((List<InputEventAdaptorFile>) eventAdaptorEntry.getValue()).iterator();
                while (eventAdaptorFileIterator.hasNext()) {
                    InputEventAdaptorFile inputEventAdaptorFile = eventAdaptorFileIterator.next();
                    if (inputEventAdaptorFile.getStatus().equals(InputEventAdaptorFile.Status.WAITING_FOR_DEPENDENCY)) {
                        inputEventAdaptorFiles.add(inputEventAdaptorFile);
                    }
                }
            }
        }

        for (InputEventAdaptorFile inputEventAdaptorFile : inputEventAdaptorFiles) {
            try {
                InputEventAdaptorConfigurationFilesystemInvoker.reload(inputEventAdaptorFile.getFileName(), inputEventAdaptorFile.getAxisConfiguration());
            } catch (Exception e) {
                log.error("Exception occurred while trying to deploy the Input Event Adaptor configuration file : " + new File(inputEventAdaptorFile.getFileName()).getName());
            }

        }
    }

    public void notifyActiveEventAdaptorConfigurationFiles()
            throws InputEventAdaptorManagerConfigurationException {

        if (eventAdaptorFileMap != null && eventAdaptorFileMap.size() > 0) {
            for (Map.Entry<Integer, List<InputEventAdaptorFile>> integerListEntry : eventAdaptorFileMap.entrySet()) {
                Map.Entry eventAdaptorEntry = (Map.Entry) integerListEntry;
                for (InputEventAdaptorFile inputEventAdaptorFile : ((List<InputEventAdaptorFile>) eventAdaptorEntry.getValue())) {
                    if (inputEventAdaptorFile.getStatus().equals(InputEventAdaptorFile.Status.DEPLOYED)) {
                        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
                        Iterator<InputEventAdaptorNotificationListener> deploymentListenerIterator = inputEventAdaptorNotificationListener.iterator();
                        while (deploymentListenerIterator.hasNext()) {
                            InputEventAdaptorNotificationListener inputEventAdaptorNotificationListener = deploymentListenerIterator.next();
                            inputEventAdaptorNotificationListener.configurationAdded(tenantId, inputEventAdaptorFile.getEventAdaptorName());
                        }
                    }
                }
            }
        }
    }

    //Private methods are below

    private void editTracingStatistics(
            InputEventAdaptorConfiguration inputEventAdaptorConfiguration,
            String eventAdaptorName, int tenantId, AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        String fileName = getFileName(tenantId, eventAdaptorName);
        undeployActiveInputEventAdaptorConfiguration(eventAdaptorName, axisConfiguration);
        OMElement omElement = InputEventAdaptorConfigurationHelper.eventAdaptorConfigurationToOM(inputEventAdaptorConfiguration);
        InputEventAdaptorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
        InputEventAdaptorConfigurationFilesystemInvoker.save(omElement, eventAdaptorName, fileName, axisConfiguration);
    }

    /**
     * to add to the input adaptor maps that gives information which event adaptors supports
     * for event adaptor configuration
     *
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     *
     */
    private void addToTenantSpecificEventAdaptorInfoMap(int tenantId,
                                                        InputEventAdaptorConfiguration eventAdaptorConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        if (eventAdaptorConfiguration.getInputConfiguration() != null) {
            InputEventAdaptorInfo inputEventAdaptorInfo = new InputEventAdaptorInfo();
            inputEventAdaptorInfo.setEventAdaptorName(eventAdaptorConfiguration.getName());
            inputEventAdaptorInfo.setEventAdaptorType(eventAdaptorConfiguration.getType());
            Map<String, InputEventAdaptorInfo> eventAdaptorInfoMap = tenantSpecificInputEventAdaptorInfoMap.get(tenantId);

            if (eventAdaptorInfoMap != null) {

                eventAdaptorInfoMap.put(eventAdaptorConfiguration.getName(), inputEventAdaptorInfo);
            } else {
                eventAdaptorInfoMap = new HashMap<String, InputEventAdaptorInfo>();
                eventAdaptorInfoMap.put(eventAdaptorConfiguration.getName(), inputEventAdaptorInfo);
            }
            tenantSpecificInputEventAdaptorInfoMap.put(tenantId, eventAdaptorInfoMap);

        }

    }

    /**
     * to remove the event adaptor configuration when deployed from the map after un-deploy
     */
    private void removeFromTenantSpecificEventAdaptorInfoMap(int tenantId,
                                                             String eventAdaptorName) {

        Map<String, InputEventAdaptorInfo> inputEventAdaptorInfoMap = tenantSpecificInputEventAdaptorInfoMap.get(tenantId);

        if (inputEventAdaptorInfoMap != null && inputEventAdaptorInfoMap.containsKey(eventAdaptorName)) {
            inputEventAdaptorInfoMap.remove(eventAdaptorName);
        }

    }

    /**
     * to get the file path of a event adaptor
     */
    private String getFileName(int tenantId, String eventAdaptorName) {

        if (eventAdaptorFileMap.size() > 0) {
            List<InputEventAdaptorFile> inputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
            if (inputEventAdaptorFileList != null) {
                for (InputEventAdaptorFile inputEventAdaptorFile : inputEventAdaptorFileList) {
                    if ((inputEventAdaptorFile.getEventAdaptorName().equals(eventAdaptorName)) && (inputEventAdaptorFile.getStatus().equals(InputEventAdaptorFile.Status.DEPLOYED))) {
                        return inputEventAdaptorFile.getFileName();
                    }
                }
            }
        }
        return null;
    }

    /**
     * this stores the event adaptor configuration to the file system after validating the event adaptor when doing editing
     *
     * @throws org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException
     *
     */
    private void validateToEditEventAdaptorConfiguration(int tenantId,
                                                         String eventAdaptorName,
                                                         AxisConfiguration axisConfiguration,
                                                         OMElement omElement)
            throws InputEventAdaptorManagerConfigurationException {
        InputEventAdaptorConfiguration eventAdaptorConfiguration = InputEventAdaptorConfigurationHelper.fromOM(omElement);
        if (InputEventAdaptorConfigurationHelper.validateEventAdaptorConfiguration(eventAdaptorConfiguration)) {
            String fileName = getFileName(tenantId, eventAdaptorName);
            if (fileName == null) {
                fileName = eventAdaptorName + InputEventAdaptorManagerConstants.IEA_CONFIG_FILE_EXTENSION_WITH_DOT;
            }
            InputEventAdaptorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
            InputEventAdaptorConfigurationFilesystemInvoker.save(omElement, eventAdaptorName, fileName, axisConfiguration);
        } else {
            log.error("There is no Input Event Adaptor type called " + eventAdaptorConfiguration.getType() + " is available ");
            throw new InputEventAdaptorManagerConfigurationException("There is no Input Event Adaptor type called " + eventAdaptorConfiguration.getType() + " is available ");
        }
    }

    private void validateToRemoveInactiveEventAdaptorConfiguration(String adaptorName,
                                                                   AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String fileName = adaptorName + InputEventAdaptorManagerConstants.IEA_CONFIG_FILE_EXTENSION_WITH_DOT;
        List<InputEventAdaptorFile> inputEventAdaptorFileList = eventAdaptorFileMap.get(tenantId);
        if (inputEventAdaptorFileList != null) {
            for (InputEventAdaptorFile inputEventAdaptorFile : inputEventAdaptorFileList) {
                if ((inputEventAdaptorFile.getFileName().equals(fileName))) {
                    if (!(inputEventAdaptorFile.getStatus().equals(InputEventAdaptorFile.Status.DEPLOYED))) {
                        InputEventAdaptorConfigurationFilesystemInvoker.delete(fileName, axisConfiguration);
                        break;
                    }
                }
            }
        }

    }

}