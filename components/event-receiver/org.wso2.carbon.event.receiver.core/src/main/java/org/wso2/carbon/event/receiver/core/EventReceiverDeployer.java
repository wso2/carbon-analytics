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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.application.deployer.EventProcessingDeployer;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfiguration;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverStreamValidationException;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverValidationException;
import org.wso2.carbon.event.receiver.core.internal.CarbonEventReceiverService;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverConfigurationBuilder;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationFileSystemInvoker;
import org.wso2.carbon.event.receiver.core.internal.util.helper.EventReceiverConfigurationHelper;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy event receivers as axis2 service
 */
public class EventReceiverDeployer extends AbstractDeployer implements EventProcessingDeployer {

    private static Log log = LogFactory.getLog(EventReceiverDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedEventReceiverFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> undeployedEventReceiverFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    @Override
    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Process the event receiver configuration file, create it and deploy it
     *
     * @param deploymentFileData information about the event receiver
     * @throws org.apache.axis2.deployment.DeploymentException for any errors
     */
    @Override
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();
        if (!deployedEventReceiverFilePaths.contains(path)) {
            try {
                processDeployment(deploymentFileData);
            } catch (Throwable e) {
                throw new DeploymentException("Event publisher file " + deploymentFileData.getName() + " is not deployed ", e);
            }
        } else {
            deployedEventReceiverFilePaths.remove(path);
        }
    }

    private OMElement getEventReceiverConfigOMElement(File ebConfigFile) throws DeploymentException {
        OMElement ebConfigElement = null;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(ebConfigFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder receiver = new StAXOMBuilder(parser);
            ebConfigElement = receiver.getDocumentElement();
            ebConfigElement.build();
        } catch (FileNotFoundException e) {
            String errorMessage = ebConfigFile.getName() + " cannot be found";
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + ebConfigFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (Exception e) {
            String errorMessage = "Error parsing configuration syntax : " + ebConfigFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Cannot close the input stream";
                log.error(errorMessage, e);
            }
        }

        return ebConfigElement;
    }

    @Override
    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed event receiver configuration file
     *
     * @param filePath the filePath to the EventReceiverConfiguration file to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     */
    @Override
    public void undeploy(String filePath) throws DeploymentException {
        if (!undeployedEventReceiverFilePaths.contains(filePath)) {
            try {
                processUndeployment(filePath);
            } catch (Throwable e) {
                throw new DeploymentException("Event receiver file " + new File(filePath).getName() + " is not undeployed properly", e);
            }
        } else {
            undeployedEventReceiverFilePaths.remove(filePath);
        }
    }

    public void setDirectory(String directory) {

    }

    public void processDeployment(DeploymentFileData deploymentFileData)
            throws DeploymentException, EventReceiverConfigurationException {

        File eventReceiverFile = deploymentFileData.getFile();
        boolean isEditable = !eventReceiverFile.getAbsolutePath().contains(File.separator + "carbonapps" + File.separator);
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        CarbonEventReceiverService carbonEventReceiverService = EventReceiverServiceValueHolder.getCarbonEventReceiverService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String eventReceiverName = "";

        String streamNameWithVersion = null;
        OMElement eventReceiverOMElement = null;
        if (!carbonEventReceiverService.isEventReceiverFileAlreadyExist(eventReceiverFile.getName(), tenantId)) {
            try {
                eventReceiverOMElement = getEventReceiverConfigOMElement(eventReceiverFile);
                if (!eventReceiverOMElement.getLocalName().equals(EventReceiverConstants.ER_ELEMENT_ROOT_ELEMENT)) {
                    throw new EventReceiverConfigurationException("Wrong event receiver configuration file, Invalid root element " + eventReceiverOMElement.getQName() + " in " + eventReceiverFile.getName());
                }

                boolean isEncrypted = EventReceiverConfigurationHelper.validateEncryptedProperties(eventReceiverOMElement);

                if (isEditable && !isEncrypted) {
                    String fileName = eventReceiverFile.getName();
                    EventReceiverConfigurationFileSystemInvoker.delete(fileName);
                    EventReceiverConfigurationFileSystemInvoker.encryptAndSave(eventReceiverOMElement, fileName);
                    return;
                }

                EventReceiverConfigurationHelper.validateEventReceiverConfiguration(eventReceiverOMElement);

                eventReceiverName = eventReceiverOMElement.getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_NAME));
                String mappingType = EventReceiverConfigurationHelper.getInputMappingType(eventReceiverOMElement);
                if (mappingType != null) {
                    mappingType = mappingType.toLowerCase();
                    EventReceiverConfiguration eventReceiverConfiguration = EventReceiverConfigurationBuilder.getEventReceiverConfiguration(eventReceiverOMElement, mappingType, isEditable, tenantId);
                    eventReceiverName = eventReceiverConfiguration.getEventReceiverName();
                    if (!carbonEventReceiverService.isEventReceiverAlreadyExists(tenantId, eventReceiverName)) {
                        carbonEventReceiverService.addEventReceiverConfiguration(eventReceiverConfiguration);
                        carbonEventReceiverService.addEventReceiverConfigurationFile(createEventReceiverConfigurationFile(eventReceiverName,
                                deploymentFileData.getFile(), EventReceiverConfigurationFile.Status.DEPLOYED, tenantId, null, null), tenantId);
                        log.info("Event Receiver configuration successfully deployed and in active state : " + eventReceiverName);
                    } else {
                        throw new EventReceiverConfigurationException("Event Receiver not deployed and in inactive state," +
                                " since there is a event receiver registered with the same name in this tenant :" + eventReceiverFile.getName());
                    }
                } else {
                    throw new EventReceiverConfigurationException("Event Receiver not deployed and in inactive state, " +
                            "since it does not contain a proper mapping type : " + eventReceiverFile.getName());
                }
            } catch (EventReceiverConfigurationException e) {
                log.error("Error, Event Receiver not deployed and in inactive state, " + e.getMessage(), e);
                carbonEventReceiverService.addEventReceiverConfigurationFile(createEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(),
                        EventReceiverConfigurationFile.Status.ERROR, tenantId, "Exception when deploying event receiver configuration file:\n" + e.getMessage(), null), tenantId);
                throw new EventReceiverConfigurationException(e.getMessage(), e);
            } catch (EventReceiverValidationException e) {
                carbonEventReceiverService.addEventReceiverConfigurationFile(createEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(),
                        EventReceiverConfigurationFile.Status.WAITING_FOR_DEPENDENCY, tenantId, e.getMessage(), e.getDependency()), tenantId);
                log.info("Event receiver deployment held back and in inactive state :" + eventReceiverFile.getName() + ", waiting for Input Event Adapter dependency :" + e.getDependency());
            } catch (EventReceiverStreamValidationException e) {
                carbonEventReceiverService.addEventReceiverConfigurationFile(createEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(),
                        EventReceiverConfigurationFile.Status.WAITING_FOR_STREAM_DEPENDENCY, tenantId, e.getMessage(), e.getDependency()), tenantId);
                log.info("Event receiver deployment held back and in inactive state :" + eventReceiverFile.getName() + ", Stream validation exception :" + e.getMessage());

            } catch (Throwable e) {
                log.error("Event Receiver not deployed, invalid configuration found at " + eventReceiverFile.getName() + ", and in inactive state, " + e.getMessage(), e);
                carbonEventReceiverService.addEventReceiverConfigurationFile(createEventReceiverConfigurationFile(eventReceiverName, deploymentFileData.getFile(),
                        EventReceiverConfigurationFile.Status.ERROR, tenantId, "Deployment exception: " + e.getMessage(), null), tenantId);
                throw new EventReceiverConfigurationException(e);
            }
        } else {
            log.info("Event Receiver " + eventReceiverName + " is already registered with this tenant (" + tenantId + "), hence ignoring redeployment");
        }
    }

    private EventReceiverConfigurationFile createEventReceiverConfigurationFile(
            String eventReceiverName,
            File file,
            EventReceiverConfigurationFile.Status status,
            int  tenantId,
            String deploymentStatusMessage,
            String dependency) {
        EventReceiverConfigurationFile eventReceiverConfigurationFile = new EventReceiverConfigurationFile();
        eventReceiverConfigurationFile.setFileName(file.getName());
        eventReceiverConfigurationFile.setFilePath(file.getAbsolutePath());
        eventReceiverConfigurationFile.setEventReceiverName(eventReceiverName);
        eventReceiverConfigurationFile.setStatus(status);
        eventReceiverConfigurationFile.setDependency(dependency);
        eventReceiverConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventReceiverConfigurationFile.setTenantId(tenantId);

        return eventReceiverConfigurationFile;
    }

    public void processUndeployment(String filePath) throws EventReceiverConfigurationException {
        String fileName = new File(filePath).getName();
        log.info("Event Receiver undeployed successfully : " + fileName);
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CarbonEventReceiverService carbonEventReceiverService = EventReceiverServiceValueHolder.getCarbonEventReceiverService();
        carbonEventReceiverService.removeEventReceiverConfigurationFile(fileName);
    }

    public void executeManualDeployment(DeploymentFileData deploymentFileData)
            throws EventReceiverConfigurationException {
        try {
            processDeployment(deploymentFileData);
        } catch (DeploymentException e) {
            throw new EventReceiverConfigurationException("Error while attempting manual deployment :" + e.getMessage(), e);
        }
    }

    public void executeManualUndeployment(String filePath)
            throws EventReceiverConfigurationException {
        processUndeployment(filePath);
    }

    public Set<String> getDeployedEventReceiverFilePaths() {
        return deployedEventReceiverFilePaths;
    }

    public Set<String> getUndeployedEventReceiverFilePaths() {
        return undeployedEventReceiverFilePaths;
    }
}


