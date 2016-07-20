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
package org.wso2.carbon.event.publisher.core.internal.util.helper;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.event.output.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.publisher.core.EventPublisherDeployer;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.event.publisher.core.internal.util.EventPublisherUtil;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.Iterator;
import java.util.List;

/**
 * This class used to do the file system related tasks
 */
public class EventPublisherConfigurationFilesystemInvoker {

    private static final Log log = LogFactory.getLog(EventPublisherConfigurationFilesystemInvoker.class);

    public static void encryptAndSave(OMElement eventAdaptorElement, String fileName)
            throws EventPublisherConfigurationException {

        String adaptorType = eventAdaptorElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO)).getAttributeValue(new QName(EventPublisherConstants.EF_ATTR_TA_TYPE));

        //get Static and Dynamic PropertyLists
        List<String> encryptedProperties = EventPublisherServiceValueHolder.getCarbonEventPublisherService().getEncryptedProperties(adaptorType);
        Iterator propertyIter = eventAdaptorElement.getFirstChildWithName(new QName(EventPublisherConstants.EF_CONF_NS, EventPublisherConstants.EF_ELEMENT_TO)).getChildrenWithName(new QName(EventPublisherConstants.EF_ELE_PROPERTY));
        if (propertyIter.hasNext()) {
            while (propertyIter.hasNext()) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(EventPublisherConstants.EF_ATTR_NAME));

                if (encryptedProperties.contains(name.trim())) {
                    OMAttribute encryptedAttribute = propertyOMElement.getAttribute(new QName(EventPublisherConstants.EF_ATTR_ENCRYPTED));

                    if (encryptedAttribute == null || (!"true".equals(encryptedAttribute.getAttributeValue()))) {
                        String value = propertyOMElement.getText();

                        try {
                            value = new String(CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(value.getBytes()));
                            propertyOMElement.setText(value);
                            propertyOMElement.addAttribute(EventPublisherConstants.EF_ATTR_ENCRYPTED, "true", null);
                        } catch (Exception e) {
                            log.error("Unable to decrypt the encrypted field: " + name + " for adaptor type: " + adaptorType);
                            propertyOMElement.setText("");
                        }
                    }
                }
            }
        }
        EventPublisherConfigurationFilesystemInvoker.save(eventAdaptorElement, fileName);
    }

    public static void save(OMElement eventPublisherOMElement,
                            String fileName)
            throws EventPublisherConfigurationException {

        EventPublisherConfigurationFilesystemInvoker.save(eventPublisherOMElement.toString(), fileName);
    }

    public static void save(String eventPublisher,
                            String fileName)
            throws EventPublisherConfigurationException {
        AxisConfiguration axisConfiguration = EventPublisherUtil.getAxisConfiguration();
        EventPublisherDeployer eventPublisherDeployer = (EventPublisherDeployer) getDeployer(axisConfiguration, EventPublisherConstants.EF_CONFIG_DIRECTORY);
        EventPublisherUtil.validateFilePath(fileName);
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = new XmlFormatter().format(eventPublisher);
            eventPublisherDeployer.getDeployedEventPublisherFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Event Publisher configuration saved in the filesystem : " + new File(filePath).getName());
            eventPublisherDeployer.executeManualDeployment(filePath);
        } catch (IOException e) {
            eventPublisherDeployer.getDeployedEventPublisherFilePaths().remove(filePath);
            log.error("Could not save Event Publisher configuration : " + fileName, e);
            throw new EventPublisherConfigurationException("Error while saving ", e);
        }
    }

    public static void delete(String fileName)
            throws EventPublisherConfigurationException {
        try {
            AxisConfiguration axisConfiguration = EventPublisherUtil.getAxisConfiguration();
            EventPublisherUtil.validateFilePath(fileName);
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            File file = new File(filePath);
            if (file.exists()) {
                EventPublisherDeployer deployer = (EventPublisherDeployer) getDeployer(axisConfiguration, EventPublisherConstants.EF_CONFIG_DIRECTORY);
                deployer.getUndeployedEventPublisherFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete Event Publisher configuration : " + fileName);
                    deployer.getUndeployedEventPublisherFilePaths().remove(filePath);
                } else {
                    log.info("Event Publisher configuration deleted from the file system : " + fileName);
                    deployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new EventPublisherConfigurationException("Error while deleting the Event Publisher : " + e.getMessage(), e);
        }
    }

    public static boolean isEventPublisherConfigurationFileExists(String fileName,
                                                                  AxisConfiguration axisConfiguration) throws EventPublisherConfigurationException{
        EventPublisherUtil.validateFilePath(fileName);
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        File file = new File(filePath);
        return file.exists();
    }

    public static void reload(String filePath)
            throws EventPublisherConfigurationException {
       AxisConfiguration axisConfiguration= EventPublisherUtil.getAxisConfiguration();
        EventPublisherDeployer deployer = (EventPublisherDeployer) getDeployer(axisConfiguration, EventPublisherConstants.EF_CONFIG_DIRECTORY);
        try {
            deployer.processUndeployment(filePath);
            deployer.processDeployment(new DeploymentFileData(new File(filePath)));
        } catch (DeploymentException e) {
            throw new EventPublisherConfigurationException(e);
        }

    }

    public static String readEventPublisherConfigurationFile(String fileName)
            throws EventPublisherConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            AxisConfiguration axisConfiguration = EventPublisherUtil.getAxisConfiguration();
            EventPublisherUtil.validateFilePath(fileName);
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventPublisherConfigurationException("Event publisher file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new EventPublisherConfigurationException("Cannot read the Event Publisher file : " + e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when reading the file : " + e.getMessage(), e);
            }
        }
        return stringBuilder.toString().trim();
    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }

    private static String getFilePathFromFilename(String filename,
                                                  AxisConfiguration axisConfiguration) {
        return new File(EventAdapterUtil.getAxisConfiguration().getRepository().getPath()).getAbsolutePath()
                + File.separator + EventPublisherConstants.EF_CONFIG_DIRECTORY + File.separator + filename;
    }

}
