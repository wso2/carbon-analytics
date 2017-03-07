/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.receiver.core.internal.util.helper;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.event.input.adapter.core.EventAdapterUtil;
import org.wso2.carbon.event.receiver.core.EventReceiverDeployer;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConfigurationFile;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.exception.EventReceiverConfigurationException;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.event.receiver.core.internal.util.EventReceiverUtil;
import org.wso2.carbon.event.receiver.core.internal.util.XmlFormatter;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.namespace.QName;
import java.io.*;
import java.util.Iterator;
import java.util.List;

public class EventReceiverConfigurationFileSystemInvoker {
    private static final Log log = LogFactory.getLog(EventReceiverConfigurationFileSystemInvoker.class);

    public static void encryptAndSave(OMElement eventAdaptorElement, String fileName)
            throws EventReceiverConfigurationException {

        String adaptorType = eventAdaptorElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM)).getAttributeValue(new QName(EventReceiverConstants.ER_ATTR_TA_TYPE));

        //get Static and Dynamic PropertyLists
        List<String> encryptedProperties = EventReceiverServiceValueHolder.getCarbonEventReceiverService().getEncryptedProperties(adaptorType);
        Iterator propertyIter = eventAdaptorElement.getFirstChildWithName(new QName(EventReceiverConstants.ER_CONF_NS, EventReceiverConstants.ER_ELEMENT_FROM)).getChildrenWithName(new QName(EventReceiverConstants.ER_ELEMENT_PROPERTY));
        if (propertyIter.hasNext()) {
            while (propertyIter.hasNext()) {
                OMElement propertyOMElement = (OMElement) propertyIter.next();
                String name = propertyOMElement.getAttributeValue(
                        new QName(EventReceiverConstants.ER_ATTR_NAME));

                if (encryptedProperties.contains(name.trim())) {
                    OMAttribute encryptedAttribute = propertyOMElement.getAttribute(new QName(EventReceiverConstants.ER_ATTR_ENCRYPTED));

                    if (encryptedAttribute == null || (!"true".equals(encryptedAttribute.getAttributeValue()))) {
                        String value = propertyOMElement.getText();

                        try {
                            value = new String(CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(value.getBytes()));
                            propertyOMElement.setText(value);
                            propertyOMElement.addAttribute(EventReceiverConstants.ER_ATTR_ENCRYPTED, "true", null);
                        } catch (Exception e) {
                            log.error("Unable to decrypt the encrypted field: " + name + " for adaptor type: " + adaptorType);
                            propertyOMElement.setText("");
                        }
                    }
                }
            }
        }
        EventReceiverConfigurationFileSystemInvoker.save(eventAdaptorElement, fileName);
    }

    public static void save(OMElement eventReceiverConfigOMElement, String fileName)
            throws EventReceiverConfigurationException {
        saveAndDeploy(eventReceiverConfigOMElement.toString(), fileName);
    }

    public static void saveAndDeploy(String eventReceiverConfigXml, String fileName)
            throws EventReceiverConfigurationException {
        EventReceiverDeployer eventReceiverDeployer = EventReceiverConfigurationHelper.getEventReceiverDeployer(EventAdapterUtil.getAxisConfiguration());
        EventReceiverUtil.validateFilePath(fileName);
        String filePath = getFilePathFromFilename(fileName);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = XmlFormatter.format(eventReceiverConfigXml);
            eventReceiverDeployer.getDeployedEventReceiverFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Event receiver configuration saved to the filesystem :" + fileName);
            DeploymentFileData deploymentFileData = new DeploymentFileData(new File(filePath));
            eventReceiverDeployer.executeManualDeployment(deploymentFileData);
        } catch (IOException e) {
            eventReceiverDeployer.getDeployedEventReceiverFilePaths().remove(filePath);
            log.error("Error while saving event receiver configuration: " + fileName, e);
        }
    }

    public static void delete(String fileName)
            throws EventReceiverConfigurationException {

        try {
            EventReceiverUtil.validateFilePath(fileName);
            String filePath = getFilePathFromFilename(fileName);
            File file = new File(filePath);
            String filename = file.getName();
            if (file.exists()) {
                EventReceiverDeployer eventReceiverDeployer = EventReceiverConfigurationHelper.getEventReceiverDeployer(EventAdapterUtil.getAxisConfiguration());
                eventReceiverDeployer.getUndeployedEventReceiverFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + filename);
                    eventReceiverDeployer.getUndeployedEventReceiverFilePaths().remove(filePath);
                } else {
                    log.info("Event receiver configuration deleted from the file system :" + filename);
                    eventReceiverDeployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new EventReceiverConfigurationException("Error while deleting the event receiver :" + e.getMessage(), e);
        }
    }

    public static boolean isFileExists(String fileName) throws EventReceiverConfigurationException{
        EventReceiverUtil.validateFilePath(fileName);
        String filePath = getFilePathFromFilename(fileName);
        File file = new File(filePath);
        return file.exists();
    }

    public static String readEventReceiverConfigurationFile(String fileName)
            throws EventReceiverConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            EventReceiverUtil.validateFilePath(fileName);
            String filePath = getFilePathFromFilename(fileName);
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventReceiverConfigurationException("Event receiver file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new EventReceiverConfigurationException("Cannot read the event receiver file : " + e.getMessage(), e);
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

    public static void reload(EventReceiverConfigurationFile eventReceiverConfigurationFile)
            throws EventReceiverConfigurationException {
        EventReceiverUtil.validateFilePath(eventReceiverConfigurationFile.getFileName());
        String filePath = eventReceiverConfigurationFile.getFilePath();
        AxisConfiguration axisConfiguration = EventAdapterUtil.getAxisConfiguration();
        EventReceiverDeployer deployer = EventReceiverConfigurationHelper.getEventReceiverDeployer(axisConfiguration);
        try {
            deployer.processUndeployment(filePath);
            deployer.processDeployment(new DeploymentFileData(new File(filePath)));
        } catch (DeploymentException e) {
            throw new EventReceiverConfigurationException(e);
        }
    }

    /**
     * Returns the full path. IMPORTANT: This method uses {@link MultitenantUtils} to get the Axis2RepositoryPath and might
     * give incorrect values if the axis2 repository path has been modified at startup (i.e. for samples)
     *
     * @param filename filename of the config file
     * @return the full path
     */
    private static String getFilePathFromFilename(String filename) {
        return new File(EventAdapterUtil.getAxisConfiguration().getRepository().getPath()).getAbsolutePath()
                + File.separator + EventReceiverConstants.ER_CONFIG_DIRECTORY + File.separator + filename;
    }
}
