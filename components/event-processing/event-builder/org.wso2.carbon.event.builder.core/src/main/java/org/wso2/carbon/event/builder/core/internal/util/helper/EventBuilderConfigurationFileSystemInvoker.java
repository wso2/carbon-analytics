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

package org.wso2.carbon.event.builder.core.internal.util.helper;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.builder.core.EventBuilderDeployer;
import org.wso2.carbon.event.builder.core.exception.EventBuilderConfigurationException;
import org.wso2.carbon.event.builder.core.internal.util.EventBuilderConstants;
import org.wso2.carbon.event.builder.core.internal.util.XmlFormatter;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.*;

public class EventBuilderConfigurationFileSystemInvoker {
    private static final Log log = LogFactory.getLog(EventBuilderConfigurationFileSystemInvoker.class);

    public static void save(
            OMElement eventBuilderConfigOMElement,
            String fileName, AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        saveAndDeploy(eventBuilderConfigOMElement.toString(), fileName, axisConfiguration);
    }

    public static void saveAndDeploy(String eventBuilderConfigXml, String fileName, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        EventBuilderDeployer eventBuilderDeployer = EventBuilderConfigHelper.getEventBuilderDeployer(axisConfiguration);
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = XmlFormatter.format(eventBuilderConfigXml);
            eventBuilderDeployer.getDeployedEventBuilderFilePaths().add(filePath);
            out.write(xmlContent);
            out.close();
            log.info("Event builder configuration saved to the filesystem :" + fileName);
            DeploymentFileData deploymentFileData = new DeploymentFileData(new File(filePath));
            eventBuilderDeployer.executeManualDeployment(deploymentFileData);
        } catch (IOException e) {
            eventBuilderDeployer.getDeployedEventBuilderFilePaths().remove(filePath);
            log.error("Error while saving event builder configuration: " + fileName, e);
        }
    }

    public static void save(String eventBuilderConfigXml, String fileName) throws EventBuilderConfigurationException {
        String filePath = getFilePathFromFilename(fileName);
        try {
            /* save contents to .xml file */
            BufferedWriter out = new BufferedWriter(new FileWriter(filePath));
            String xmlContent = XmlFormatter.format(eventBuilderConfigXml);
            out.write(xmlContent);
            out.close();
            log.info("Event builder configuration saved to the filesystem :" + fileName);
        } catch (IOException e) {
            log.error("Error while saving event builder configuration: " + fileName, e);
        }

    }

    public static void delete(String fileName, AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {

        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            File file = new File(filePath);
            String filename = file.getName();
            if (file.exists()) {
                EventBuilderDeployer eventBuilderDeployer = EventBuilderConfigHelper.getEventBuilderDeployer(axisConfiguration);
                eventBuilderDeployer.getUndeployedEventBuilderFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + filename);
                    eventBuilderDeployer.getUndeployedEventBuilderFilePaths().remove(filePath);
                } else {
                    log.info("Event builder configuration deleted from the file system :" + filename);
                    eventBuilderDeployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new EventBuilderConfigurationException("Error while deleting the event builder :" + e.getMessage(), e);
        }
    }

    public static boolean isFileExists(String fileName, AxisConfiguration axisConfiguration) {
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean isFileExists(String fileName) {
        String filePath = getFilePathFromFilename(fileName);
        File file = new File(filePath);
        return file.exists();
    }

    public static String readEventBuilderConfigurationFile(String fileName, AxisConfiguration axisConfiguration)
            throws EventBuilderConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader(getFilePathFromFilename(fileName, axisConfiguration)));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new EventBuilderConfigurationException("Event builder file not found : " + e.getMessage(), e);
        } catch (IOException e) {
            throw new EventBuilderConfigurationException("Cannot read the event builder file : " + e.getMessage(), e);
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

    public static void reload(String fileName, AxisConfiguration axisConfiguration) throws EventBuilderConfigurationException {
        EventBuilderDeployer deployer = EventBuilderConfigHelper.getEventBuilderDeployer(axisConfiguration);
        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            deployer.processUndeployment(filePath);
            deployer.processDeployment(new DeploymentFileData(new File(filePath)));
        } catch (DeploymentException e) {
            throw new EventBuilderConfigurationException(e);
        }
    }

    private static String getFilePathFromFilename(String filename, AxisConfiguration axisConfiguration) {
        return new File(axisConfiguration.getRepository().getPath()).getAbsolutePath() + File.separator + EventBuilderConstants.EB_CONFIG_DIRECTORY + File.separator + filename;
    }

    private static String getFilePathFromFilename(String filename) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        String repositoryPath = MultitenantUtils.getAxis2RepositoryPath(tenantId);
        return new File(repositoryPath).getAbsolutePath() + File.separator + EventBuilderConstants.EB_CONFIG_DIRECTORY + File.separator + filename;
    }
}
