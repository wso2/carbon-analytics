package org.wso2.carbon.event.processor.core.internal.util;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.processor.core.EventProcessorDeployer;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;

import java.io.*;

public class EventProcessorConfigurationFilesystemInvoker {
    private static final Log log = LogFactory.getLog(EventProcessorConfigurationFilesystemInvoker.class);


    public static void save(OMElement executionPlanOM,
                            String executionPlanName,
                            String fileName,
                            AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {

        EventProcessorConfigurationFilesystemInvoker.save(executionPlanOM.toString(), executionPlanName, fileName, axisConfiguration);
    }

    public static void save(String executionPlan, String executionPlanName,
                            String fileName, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        EventProcessorDeployer eventProcessorDeployer = (EventProcessorDeployer) getDeployer(axisConfiguration, EventProcessorConstants.EP_ELE_DIRECTORY);
        String filePath = getFilePathFromFilename(fileName, axisConfiguration);
        try {
            OutputStreamWriter writer = null;
            try {
                /* save contents to .xml file */
                File file = new File(filePath);
                writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");

                // get the content in bytes
                String xmlContent = EventProcessorUtil.formatXml(executionPlan);
                eventProcessorDeployer.getDeployedExecutionPlanFilePaths().add(filePath);
                writer.write(xmlContent);
                log.info("Execution plan configuration for " + executionPlanName + " saved in the filesystem");
            } finally {
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
            eventProcessorDeployer.executeManualDeployment(filePath);
        } catch (IOException e) {
            eventProcessorDeployer.getDeployedExecutionPlanFilePaths().remove(filePath);
            log.error("Error while saving " + executionPlanName, e);
            throw new ExecutionPlanConfigurationException("Error while saving ", e);
        }
    }

    public static void delete(String fileName, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        try {
            String filePath=getFilePathFromFilename(fileName,axisConfiguration);
            File file = new File(filePath);
            if (file.exists()) {
                EventProcessorDeployer deployer = (EventProcessorDeployer) getDeployer(axisConfiguration, EventProcessorConstants.EP_ELE_DIRECTORY);
                deployer.getUnDeployedExecutionPlanFilePaths().add(filePath);
                boolean fileDeleted = file.delete();
                if (!fileDeleted) {
                    log.error("Could not delete " + fileName);
                    deployer.getUnDeployedExecutionPlanFilePaths().remove(filePath);
                } else {
                    log.info(fileName + " is deleted from the file system");
                    deployer.executeManualUndeployment(filePath);
                }
            }
        } catch (Exception e) {
            throw new ExecutionPlanConfigurationException("Error while deleting the execution plan file ", e);
        }
    }

    public static void reload(String fileName, AxisConfiguration axisConfiguration) throws ExecutionPlanConfigurationException {
        EventProcessorDeployer eventProcessorDeployer = (EventProcessorDeployer) getDeployer(axisConfiguration, EventProcessorConstants.EP_ELE_DIRECTORY);
        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            eventProcessorDeployer.processUndeploy(filePath);
            eventProcessorDeployer.processDeploy(new DeploymentFileData(new File(filePath)));
        } catch (ExecutionPlanConfigurationException e) {
            throw new ExecutionPlanConfigurationException(e);
        }

    }

    public static Deployer getDeployer(AxisConfiguration axisConfig, String endpointDirPath) {
        DeploymentEngine deploymentEngine = (DeploymentEngine) axisConfig.getConfigurator();
        return deploymentEngine.getDeployer(endpointDirPath, "xml");
    }


    private static String getFilePathFromFilename(String fileName, AxisConfiguration axisConfiguration) {
        return  new File(axisConfiguration.getRepository().getPath()).getAbsolutePath() + File.separator + EventProcessorConstants.EP_ELE_DIRECTORY + File.separator + fileName;
    }

    public static String readExecutionPlanConfigFile(String fileName, AxisConfiguration axisConfiguration)
            throws ExecutionPlanConfigurationException {
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String filePath = getFilePathFromFilename(fileName, axisConfiguration);
            bufferedReader = new BufferedReader(new FileReader(filePath));
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append("\n");
            }
        } catch (FileNotFoundException e) {
            throw new ExecutionPlanConfigurationException("Execution plan file not found, " + fileName + "," + e.getMessage(), e);
        } catch (IOException e) {
            throw new ExecutionPlanConfigurationException("Cannot read the execution plan file, " + fileName + "," + e.getMessage(), e);
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                log.error("Error occurred when reading the file, " + fileName + "," + e.getMessage(), e);
            }
        }
        return stringBuilder.toString().trim();
    }


}
