/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.input.adaptor.manager.core;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.config.InputEventAdaptorConfiguration;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.CarbonInputEventAdaptorManagerService;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.ds.InputEventAdaptorManagerValueHolder;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.util.InputEventAdaptorManagerConstants;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.util.helper.InputEventAdaptorConfigurationHelper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy event adaptors as axis2 service
 */
@SuppressWarnings("unused")
public class InputEventAdaptorDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(InputEventAdaptorDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedEventAdaptorFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> undeployedEventAdaptorFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());



    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;

    }

    /**
     * Process the event adaptor file, create it and deploy it
     *
     * @param deploymentFileData information about the event adaptor
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();

        if (!deployedEventAdaptorFilePaths.contains(path)) {
            try {
                processDeploy(deploymentFileData);
            } catch (InputEventAdaptorManagerConfigurationException e) {
                throw new DeploymentException("Input Event Adaptor not deployed and in inactive state :  " + new File(path).getName(), e);
            }
        } else {
            deployedEventAdaptorFilePaths.remove(path);
        }
    }

    private OMElement getEventAdaptorOMElement(File eventAdaptorFile)
            throws DeploymentException {
        String fileName = eventAdaptorFile.getName();
        OMElement eventAdaptorElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(eventAdaptorFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            eventAdaptorElement = builder.getDocumentElement();
            eventAdaptorElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " .xml file cannot be found in the path : " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + eventAdaptorFile.getName() + " located in the path : " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (OMException e) {
            String errorMessage = "XML tags are not properly closed " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
        return eventAdaptorElement;
    }

    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed bucket
     *
     * @param filePath the path to the bucket to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void undeploy(String filePath) throws DeploymentException {

        if (!undeployedEventAdaptorFilePaths.contains(filePath)) {
            processUndeploy(filePath);
        } else {
            undeployedEventAdaptorFilePaths.remove(filePath);
        }

    }

    public void processDeploy(DeploymentFileData deploymentFileData)
            throws DeploymentException, InputEventAdaptorManagerConfigurationException {

        File eventAdaptorFile = deploymentFileData.getFile();
        CarbonInputEventAdaptorManagerService carbonEventAdaptorManagerService = InputEventAdaptorManagerValueHolder.getCarbonEventAdaptorManagerService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String eventAdaptorName = "";
        try {
            OMElement eventAdaptorOMElement = getEventAdaptorOMElement(eventAdaptorFile);
            InputEventAdaptorConfiguration eventAdaptorConfiguration = InputEventAdaptorConfigurationHelper.fromOM(eventAdaptorOMElement);

            if (!(eventAdaptorOMElement.getQName().getLocalPart()).equals(InputEventAdaptorManagerConstants.IEA_ELE_ROOT_ELEMENT)) {
                throw new DeploymentException("Invalid root element " + eventAdaptorOMElement.getQName().getLocalPart() + " in " + eventAdaptorFile.getName());
            }

            if (eventAdaptorConfiguration.getName() == null || eventAdaptorConfiguration.getType() == null || eventAdaptorConfiguration.getName().trim().isEmpty()) {
                throw new DeploymentException(eventAdaptorFile.getName() + " is not a valid input event adaptor configuration file");
            }

            eventAdaptorName = eventAdaptorOMElement.getAttributeValue(new QName(InputEventAdaptorManagerConstants.IEA_ATTR_NAME));

            if (InputEventAdaptorConfigurationHelper.validateEventAdaptorConfiguration(InputEventAdaptorConfigurationHelper.fromOM(eventAdaptorOMElement))) {
                if (carbonEventAdaptorManagerService.checkAdaptorValidity(tenantId, eventAdaptorName)) {
                    carbonEventAdaptorManagerService.addInputEventAdaptorConfiguration(tenantId, eventAdaptorConfiguration);
                    carbonEventAdaptorManagerService.addInputEventAdaptorConfigurationFile(tenantId, createInputEventAdaptorFile(eventAdaptorName, eventAdaptorFile.getName(), InputEventAdaptorFile.Status.DEPLOYED, null, null, null));

                    log.info("Input Event Adaptor deployed successfully and in active state : " + eventAdaptorName);
                    if (carbonEventAdaptorManagerService.inputEventAdaptorNotificationListener != null) {
                        for (InputEventAdaptorNotificationListener inputEventAdaptorNotificationListener : carbonEventAdaptorManagerService.inputEventAdaptorNotificationListener) {
                            inputEventAdaptorNotificationListener.configurationAdded(tenantId, eventAdaptorName);
                        }
                    }
                } else {
                    throw new InputEventAdaptorManagerConfigurationException(eventAdaptorName + " is already registered for this tenant");
                }
            } else {
                carbonEventAdaptorManagerService.addInputEventAdaptorConfigurationFile(tenantId, createInputEventAdaptorFile(eventAdaptorName, eventAdaptorFile.getName(), InputEventAdaptorFile.Status.WAITING_FOR_DEPENDENCY, configurationContext.getAxisConfiguration(), "Event Adaptor type is not found", eventAdaptorConfiguration.getType()));
                log.info("Input Event Adaptor deployment held back and in inactive state : " + eventAdaptorName + ", waiting for input event adaptor type dependency " + eventAdaptorConfiguration.getType());
            }
        } catch (InputEventAdaptorManagerConfigurationException ex) {
            carbonEventAdaptorManagerService.addInputEventAdaptorConfigurationFile(tenantId, createInputEventAdaptorFile(eventAdaptorName, eventAdaptorFile.getName(), InputEventAdaptorFile.Status.ERROR, null, null, null));
            log.error("Input Event Adaptor not deployed and in inactive state : " + eventAdaptorFile.getName() + " , " + ex.getMessage(), ex);
            throw new InputEventAdaptorManagerConfigurationException(ex.getMessage(), ex);
        } catch (DeploymentException e) {
            carbonEventAdaptorManagerService.addInputEventAdaptorConfigurationFile(tenantId, createInputEventAdaptorFile(eventAdaptorName, eventAdaptorFile.getName(), InputEventAdaptorFile.Status.ERROR, configurationContext.getAxisConfiguration(), "Deployment exception occurred", null));
            log.error("Input Event Adaptor not deployed and in inactive state : " + eventAdaptorFile.getName() + " , " + e.getMessage(), e);
            throw new DeploymentException(e.getMessage(), e);
        }

    }

    public void processUndeploy(String filePath) {

        String fileName = new File(filePath).getName();
        log.info("Input Event Adaptor undeployed successfully : " + fileName);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CarbonInputEventAdaptorManagerService carbonEventAdaptorManagerService = InputEventAdaptorManagerValueHolder.getCarbonEventAdaptorManagerService();
        carbonEventAdaptorManagerService.removeInputEventAdaptorConfiguration(fileName, tenantId);
    }

    public void setDirectory(String directory) {

    }

    public void executeManualDeployment(String filePath) throws
                                                         InputEventAdaptorManagerConfigurationException, DeploymentException {
        processDeploy(new DeploymentFileData(new File(filePath)));
    }

    public void executeManualUndeployment(String filePath) {
        processUndeploy(filePath);
    }

    private InputEventAdaptorFile createInputEventAdaptorFile(String eventAdaptorName,
                                                              String fileName,
                                                              InputEventAdaptorFile.Status status,
                                                              AxisConfiguration axisConfiguration,
                                                              String deploymentStatusMessage,
                                                              String dependency) {

        InputEventAdaptorFile inputEventAdaptorFile = new InputEventAdaptorFile();
        inputEventAdaptorFile.setFileName(fileName);
        inputEventAdaptorFile.setEventAdaptorName(eventAdaptorName);
        inputEventAdaptorFile.setAxisConfiguration(axisConfiguration);
        inputEventAdaptorFile.setDependency(dependency);
        inputEventAdaptorFile.setDeploymentStatusMessage(deploymentStatusMessage);
        inputEventAdaptorFile.setStatus(status);

        return inputEventAdaptorFile;

    }

    public Set<String> getDeployedEventAdaptorFilePaths() {
        return deployedEventAdaptorFilePaths;
    }

    public Set<String> getUndeployedEventAdaptorFilePaths() {
        return undeployedEventAdaptorFilePaths;
    }
}


