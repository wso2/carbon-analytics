/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.event.formatter.core;

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
import org.wso2.carbon.event.formatter.core.config.EventFormatterConfiguration;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterValidationException;
import org.wso2.carbon.event.formatter.core.internal.CarbonEventFormatterService;
import org.wso2.carbon.event.formatter.core.internal.ds.EventFormatterServiceValueHolder;
import org.wso2.carbon.event.formatter.core.internal.util.EventFormatterConfigurationFile;
import org.wso2.carbon.event.formatter.core.internal.util.FormatterConfigurationBuilder;
import org.wso2.carbon.event.formatter.core.internal.util.helper.EventFormatterConfigurationHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Deploy event formatter as axis2 service
 */
public class EventFormatterDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(EventFormatterDeployer.class);
    private ConfigurationContext configurationContext;
    private Set<String> deployedEventFormatterFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private Set<String> undeployedEventFormatterFilePaths = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
    }

    /**
     * Process the event formatter file, create it and deploy it
     *
     * @param deploymentFileData information about the event formatter
     * @throws org.apache.axis2.deployment.DeploymentException
     *          for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        String path = deploymentFileData.getAbsolutePath();
        if (!deployedEventFormatterFilePaths.contains(path)) {
            try {
                processDeploy(deploymentFileData);
            } catch (EventFormatterConfigurationException e) {
                throw new DeploymentException("Event formatter file " + deploymentFileData.getName() + " is not deployed ", e);
            }
        } else {
            deployedEventFormatterFilePaths.remove(path);
        }
    }

    private OMElement getEventFormatterOMElement(File eventFormatterFile)
            throws DeploymentException {
        String fileName = eventFormatterFile.getName();
        OMElement eventFormatterElement;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(eventFormatterFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            eventFormatterElement = builder.getDocumentElement();
            eventFormatterElement.build();

        } catch (FileNotFoundException e) {
            String errorMessage = " file cannot be found with name : " + fileName;
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for file " + eventFormatterFile.getName();
            log.error(errorMessage, e);
            throw new DeploymentException(errorMessage, e);
        } catch (OMException e) {
            String errorMessage = "XML tags are not properly closed in " + fileName;
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
        return eventFormatterElement;
    }

    public void setExtension(String extension) {

    }

    /**
     * Removing already deployed event formatter configuration
     *
     * @param filePath the path to the event formatter artifact to be removed
     * @throws org.apache.axis2.deployment.DeploymentException
     *
     */
    public void undeploy(String filePath) throws DeploymentException {


        if (!undeployedEventFormatterFilePaths.contains(filePath)) {
            processUndeploy(filePath);
        } else {
            undeployedEventFormatterFilePaths.remove(filePath);
        }

    }

    public void processDeploy(DeploymentFileData deploymentFileData)
            throws DeploymentException, EventFormatterConfigurationException {

        File eventFormatterFile = deploymentFileData.getFile();
        AxisConfiguration axisConfiguration = configurationContext.getAxisConfiguration();
        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String eventFormatterName = "";
        try {
            OMElement eventFormatterOMElement = getEventFormatterOMElement(eventFormatterFile);

            if (!(eventFormatterOMElement.getQName().getLocalPart()).equals(EventFormatterConstants.EF_ELE_ROOT_ELEMENT)) {
                throw new DeploymentException("Invalid root element " + eventFormatterOMElement.getQName() + " in " + eventFormatterFile.getName());
            }

            EventFormatterConfigurationHelper.validateEventFormatterConfiguration(eventFormatterOMElement);
            String mappingType = EventFormatterConfigurationHelper.getOutputMappingType(eventFormatterOMElement);
            if (mappingType != null) {
                mappingType = mappingType.toLowerCase();
                EventFormatterConfiguration eventFormatterConfiguration = FormatterConfigurationBuilder.getEventFormatterConfiguration(eventFormatterOMElement, tenantId, mappingType);
                eventFormatterName = eventFormatterConfiguration.getEventFormatterName();
                if (carbonEventFormatterService.checkEventFormatterValidity(tenantId, eventFormatterName)) {
                    carbonEventFormatterService.addEventFormatterConfiguration(eventFormatterConfiguration);
                    carbonEventFormatterService.addEventFormatterConfigurationFile(tenantId, createEventFormatterConfigurationFile(eventFormatterName,
                            deploymentFileData.getName(), EventFormatterConfigurationFile.Status.DEPLOYED, axisConfiguration, null, null));
                    log.info("Event Formatter configuration successfully deployed and in active state : " + eventFormatterName);
                } else {
                    throw new EventFormatterConfigurationException("Event Formatter not deployed and in inactive state," +
                            " since there is a event formatter registered with the same name in this tenant :" + eventFormatterFile.getName());
                }
            } else {
                throw new EventFormatterConfigurationException("Event Formatter not deployed and in inactive state, " +
                        "since it does not contain a proper mapping type : " + eventFormatterFile.getName());
            }
        } catch (EventFormatterConfigurationException ex) {
            log.error("Event Formatter not deployed and in inactive state, " + ex.getMessage(), ex);
            carbonEventFormatterService.addEventFormatterConfigurationFile(tenantId,
                    createEventFormatterConfigurationFile(eventFormatterName, deploymentFileData.getName(), EventFormatterConfigurationFile.Status.ERROR, null, null, null));
            throw new EventFormatterConfigurationException(ex.getMessage(), ex);
        } catch (EventFormatterValidationException ex) {
            carbonEventFormatterService.addEventFormatterConfigurationFile(tenantId,
                    createEventFormatterConfigurationFile(eventFormatterName, deploymentFileData.getName(),
                            EventFormatterConfigurationFile.Status.WAITING_FOR_DEPENDENCY, axisConfiguration, "Dependency not loaded", ex.getDependency()));
            log.info("Event Formatter deployment held back and in inactive state : " + eventFormatterFile.getName() + ", waiting for dependency : " + ex.getDependency());
        } catch (DeploymentException e) {
            log.error("Event Formatter not deployed and in inactive state : " + eventFormatterFile.getName() + " , " + e.getMessage(), e);
            carbonEventFormatterService.addEventFormatterConfigurationFile(tenantId, createEventFormatterConfigurationFile(eventFormatterName,
                    deploymentFileData.getName(), EventFormatterConfigurationFile.Status.ERROR, null, "Deployment exception occurred", null));
            throw new EventFormatterConfigurationException(e.getMessage(), e);
        }
    }

    public void processUndeploy(String filePath) {

        String fileName = new File(filePath).getName();
        log.info("Event Formatter undeployed successfully : " + fileName);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        CarbonEventFormatterService carbonEventFormatterService = EventFormatterServiceValueHolder.getCarbonEventFormatterService();
        carbonEventFormatterService.removeEventFormatterConfigurationFromMap(fileName, tenantId);
    }

    public void setDirectory(String directory) {

    }

    public void executeManualDeployment(String filePath) throws DeploymentException, EventFormatterConfigurationException {
        processDeploy(new DeploymentFileData(new File(filePath)));
    }

    public void executeManualUndeployment(String filePath) {
        processUndeploy(filePath);
    }

    private EventFormatterConfigurationFile createEventFormatterConfigurationFile(
            String eventFormatterName,
            String fileName,
            EventFormatterConfigurationFile.Status status,
            AxisConfiguration axisConfiguration,
            String deploymentStatusMessage,
            String dependency) {
        EventFormatterConfigurationFile eventFormatterConfigurationFile = new EventFormatterConfigurationFile();
        eventFormatterConfigurationFile.setFileName(fileName);
        eventFormatterConfigurationFile.setEventFormatterName(eventFormatterName);
        eventFormatterConfigurationFile.setStatus(status);
        eventFormatterConfigurationFile.setDependency(dependency);
        eventFormatterConfigurationFile.setDeploymentStatusMessage(deploymentStatusMessage);
        eventFormatterConfigurationFile.setAxisConfiguration(axisConfiguration);

        return eventFormatterConfigurationFile;
    }

    public Set<String> getDeployedEventFormatterFilePaths() {
        return deployedEventFormatterFilePaths;
    }

    public Set<String> getUndeployedEventFormatterFilePaths() {
        return undeployedEventFormatterFilePaths;
    }
}


