package org.wso2.carbon.event.input.adaptor.manager.core.internal.util.helper;
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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorDeployer;
import org.wso2.carbon.event.input.adaptor.manager.core.exception.InputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.input.adaptor.manager.core.internal.util.InputEventAdaptorManagerConstants;

import java.io.*;

/**
 * This class used to do the file system related tasks
 */
public final class InputEventAdaptorConfigurationFilesystemInvoker {

    private static final Log log = LogFactory.getLog(InputEventAdaptorConfigurationFilesystemInvoker.class);

    private InputEventAdaptorConfigurationFilesystemInvoker() {
    }

    public static void save(OMElement eventAdaptorElement,
                            String eventAdaptorName, String fileName,
                            AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {

        InputEventAdaptorConfigurationFilesystemInvoker.save(eventAdaptorElement.toString(), eventAdaptorName, fileName, axisConfiguration);
    }

    public static void save(String eventAdaptorConfiguration, String eventAdaptorName,
                            String fileName, AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        InputEventAdaptorDeployer deployer = (InputEventAdaptorDeployer) getDeployer(axisConfiguration, InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY);
        String filePath = getfilePathFromFilename(fileName, axisConfiguration);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = new XmlFormatter().format(eventAdaptorConfiguration);
            deployer.getDeployedEventAdaptorFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Input Event Adaptor configuration saved in th filesystem : " + eventAdaptorName);
            deployer.executeManualDeployment(filePath);
        } catch (IOException e) {
            deployer.getDeployedEventAdaptorFilePaths().remove(filePath);
            log.error("Could not save Input Event Adaptor configuration " + eventAdaptorName, e);
            throw new InputEventAdaptorManagerConfigurationException("Error while saving : " + e.getMessage(), e);
        }
    }

    public static boolean isFileExists(String filename, AxisConfiguration axisConfiguration) {
        String filePath = getfilePathFromFilename(filename, axisConfiguration);
        File file = new File(filePath);
        return file.exists();
    }

    public static void delete(String fileName, AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        try {
            String filePath = getfilePathFromFilename(fileName, axisConfiguration);
            File file = new File(filePath);
            if (file.exists()) {
                InputEventAdaptorDeployer deployer = (InputEventAdaptorDeployer) getDeployer(axisConfiguration, InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY);
                deployer.getUndeployedEventAdaptorFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete Input Event Adaptor configuration : " + fileName);
                    deployer.getUndeployedEventAdaptorFilePaths().remove(filePath);
                } else {
                    log.info("Input Event Adaptor configuration deleted from file system : " + fileName);
                    deployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new InputEventAdaptorManagerConfigurationException("Error while deleting the Input Event Adaptor " + e.getMessage(), e);
        }
    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }

    public static void reload(String fileName, AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        InputEventAdaptorDeployer deployer = (InputEventAdaptorDeployer) getDeployer(axisConfiguration, InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY);
        String filePath = getfilePathFromFilename(fileName, axisConfiguration);

        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(filePath));
        try {
            deployer.processUndeploy(filePath);
            deployer.processDeploy(deploymentFileData);
        } catch (DeploymentException e) {
            throw new InputEventAdaptorManagerConfigurationException(e);
        }

    }

    public static String readEventAdaptorConfigurationFile(String fileName,
                                                           AxisConfiguration axisConfiguration)
            throws InputEventAdaptorManagerConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        String filePath = getfilePathFromFilename(fileName, axisConfiguration);
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new InputEventAdaptorManagerConfigurationException("Input Event Adaptor file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new InputEventAdaptorManagerConfigurationException("Cannot read the Input Event Adaptor file : " + e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                throw new InputEventAdaptorManagerConfigurationException("Error occurred when reading the file : " + e.getMessage(), e);
            }
        }

        return stringBuilder.toString().trim();
    }

    private static String getfilePathFromFilename(String filename, AxisConfiguration axisConfiguration) {
        return new File(axisConfiguration.getRepository().getPath()).getAbsolutePath() + File.separator + InputEventAdaptorManagerConstants.IEA_ELE_DIRECTORY + File.separator + filename;
    }
}
