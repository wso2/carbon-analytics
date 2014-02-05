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

package org.wso2.carbon.event.formatter.core.internal.util.helper;


import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.formatter.core.EventFormatterDeployer;
import org.wso2.carbon.event.formatter.core.config.EventFormatterConstants;
import org.wso2.carbon.event.formatter.core.exception.EventFormatterConfigurationException;

import java.io.*;

/**
 * This class used to do the file system related tasks
 */
public class EventFormatterConfigurationFilesystemInvoker {

    private static final Log log = LogFactory.getLog(EventFormatterConfigurationFilesystemInvoker.class);

    public static void save(OMElement eventFormatterOMElement,
                            String fileName,
                            AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {

        EventFormatterConfigurationFilesystemInvoker.save(eventFormatterOMElement.toString(), fileName, axisConfiguration);
    }

    public static void save(String eventFormatter,
                            String fileName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        EventFormatterDeployer eventFormatterDeployer = (EventFormatterDeployer) getDeployer(axisConfiguration, EventFormatterConstants.TM_ELE_DIRECTORY);
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = new XmlFormatter().format(eventFormatter);
            eventFormatterDeployer.getDeployedEventFormatterFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Event Formatter configuration saved in the filesystem : " + new File(filePath).getName());
            eventFormatterDeployer.executeManualDeployment(filePath);
        } catch (IOException e) {
            eventFormatterDeployer.getDeployedEventFormatterFilePaths().remove(filePath);
            log.error("Could not save Event Formatter configuration : " + fileName, e);
            throw new EventFormatterConfigurationException("Error while saving ", e);
        }
    }

    public static void delete(String fileName,
                              AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            File file = new File(filePath);
            if (file.exists()) {
                EventFormatterDeployer deployer = (EventFormatterDeployer) getDeployer(axisConfiguration, EventFormatterConstants.TM_ELE_DIRECTORY);
                deployer.getUndeployedEventFormatterFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete Event Formatter configuration : " + fileName);
                    deployer.getUndeployedEventFormatterFilePaths().remove(filePath);
                } else {
                    log.info("Event Formatter configuration deleted from the file system : " + fileName);
                    deployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new EventFormatterConfigurationException("Error while deleting the Event Formatter : " + e.getMessage(), e);
        }
    }

    public static boolean isEventFormatterConfigurationFileExists(String fileName, AxisConfiguration axisConfiguration) {
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        File file = new File(filePath);
        return file.exists();
    }

    public static void reload(String fileName, AxisConfiguration axisConfiguration) throws EventFormatterConfigurationException {
        EventFormatterDeployer deployer = (EventFormatterDeployer) getDeployer(axisConfiguration, EventFormatterConstants.TM_ELE_DIRECTORY);
        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            deployer.processUndeploy(filePath);
            deployer.processDeploy(new DeploymentFileData(new File(filePath)));
        } catch (DeploymentException e) {
            throw new EventFormatterConfigurationException(e);
        }

    }

    public static String readEventFormatterConfigurationFile(String fileName, AxisConfiguration axisConfiguration)
            throws EventFormatterConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventFormatterConfigurationException("Event formatter file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new EventFormatterConfigurationException("Cannot read the Event Formatter file : " + e.getMessage(), e);
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

    private static String getFilePathFromFilename(String filename, AxisConfiguration axisConfiguration) {
        return new File(axisConfiguration.getRepository().getPath()).getAbsolutePath() + File.separator + EventFormatterConstants.TM_ELE_DIRECTORY + File.separator + filename;
    }

}
