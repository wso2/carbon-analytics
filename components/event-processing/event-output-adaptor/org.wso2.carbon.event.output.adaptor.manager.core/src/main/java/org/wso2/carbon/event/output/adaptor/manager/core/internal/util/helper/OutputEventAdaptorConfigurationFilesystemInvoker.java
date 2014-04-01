package org.wso2.carbon.event.output.adaptor.manager.core.internal.util.helper;
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
import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorDeployer;
import org.wso2.carbon.event.output.adaptor.manager.core.exception.OutputEventAdaptorManagerConfigurationException;
import org.wso2.carbon.event.output.adaptor.manager.core.internal.util.OutputEventAdaptorManagerConstants;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class used to do the file system related tasks
 */
public final class OutputEventAdaptorConfigurationFilesystemInvoker {

    private static final Log log = LogFactory.getLog(OutputEventAdaptorConfigurationFilesystemInvoker.class);

    private OutputEventAdaptorConfigurationFilesystemInvoker() {
    }

    public static void save(OMElement eventAdaptorElement,
                            String fileName, String pathInFileSystem,
                            AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {

        OutputEventAdaptorConfigurationFilesystemInvoker.save(eventAdaptorElement.toString(), fileName, pathInFileSystem, axisConfiguration);
    }

    public static void save(String eventAdaptorConfig, String eventAdaptorName,
                            String fileName, AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        OutputEventAdaptorDeployer deployer = (OutputEventAdaptorDeployer) getDeployer(axisConfiguration, OutputEventAdaptorManagerConstants.OEA_ELE_DIRECTORY);
        String filePath=getFilePathFromFilename(fileName,axisConfiguration);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent =new XmlFormatter().format(eventAdaptorConfig);
            deployer.getDeployedEventAdaptorFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Output Event Adaptor configuration saved to the filesystem : " + eventAdaptorName);
            deployer.executeManualDeployment(filePath);
        } catch (IOException e) {
            deployer.getDeployedEventAdaptorFilePaths().remove(filePath);
            log.error("Could not save Output Event Adaptor configuration : " + eventAdaptorName, e);
            throw new OutputEventAdaptorManagerConfigurationException("Error while saving ", e);
        }
    }

    public static void delete(String fileName,
                              AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        try {
            String filePath=getFilePathFromFilename(fileName,axisConfiguration);
            File file = new File(filePath);
            if (file.exists()) {
                OutputEventAdaptorDeployer deployer = (OutputEventAdaptorDeployer) getDeployer(axisConfiguration, OutputEventAdaptorManagerConstants.OEA_ELE_DIRECTORY);
                deployer.getUnDeployedEventAdaptorFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete Output Event Adaptor configuration : " + fileName);
                    deployer.getUnDeployedEventAdaptorFilePaths().remove(filePath);
                } else {
                    log.info("Output Event Adaptor deleted from the file system : " + fileName);
                    deployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new OutputEventAdaptorManagerConfigurationException("Error while deleting the Output Event Adaptor : " + e.getMessage());
        }
    }

    private static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        // access the deployment engine through axis config
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }

    public static void reload(String fileName, AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        OutputEventAdaptorDeployer deployer = (OutputEventAdaptorDeployer) getDeployer(axisConfiguration, OutputEventAdaptorManagerConstants.OEA_ELE_DIRECTORY);
        String filePath=getFilePathFromFilename(fileName,axisConfiguration);
        DeploymentFileData deploymentFileData = new DeploymentFileData(new File(filePath));
        try {
            deployer.processUndeploy(filePath);
            deployer.processDeploy(deploymentFileData);
        } catch (DeploymentException e) {
            throw new OutputEventAdaptorManagerConfigurationException(e);
        }

    }

    public static String readEventAdaptorConfigurationFile(String fileName,
                                                           AxisConfiguration axisConfiguration)
            throws OutputEventAdaptorManagerConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        String filePath=getFilePathFromFilename(fileName,axisConfiguration);
        try {
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new OutputEventAdaptorManagerConfigurationException("Output Event Adaptor file not found ", e);
        } catch (IOException e) {
            throw new OutputEventAdaptorManagerConfigurationException("Cannot read the Output Event Adaptor file ", e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                throw new OutputEventAdaptorManagerConfigurationException("Error occurred when reading the file ", e);
            }
        }

        return stringBuilder.toString().trim();
    }


    private static String getFilePathFromFilename(String filename, AxisConfiguration axisConfiguration) {
        return new File(axisConfiguration.getRepository().getPath()).getAbsolutePath() + File.separator + OutputEventAdaptorManagerConstants.OEA_ELE_DIRECTORY + File.separator + filename;
    }

}
