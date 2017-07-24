/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.dataservice.core;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataPurgingConfiguration;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsDataPurgingIncludeTable;
import org.wso2.carbon.analytics.dataservice.core.tasks.AnalyticsDataPurgingTask;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.application.deployer.AppDeployerConstants;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the implementation of Carbon application deployer for analytics data purging.
 */
public class AnalyticsDataPurgingDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(AnalyticsDataPurgingDeployer.class);
    private static final String TYPE = "analytics/dataPurging";

    @Override
    public void deployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        if (Boolean.getBoolean(Constants.DISABLE_ANALYTICS_DATA_PURGING_JVM_OPTION)) {
            if (log.isDebugEnabled()) {
                log.debug("Purging task not scheduling through CApp.");
            }
            return;
        }
        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        // loop through all artifacts
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files.size() == 1) {
                    String fileName = files.get(0).getName();
                    fileName = GenericUtils.checkAndReturnPath(fileName);
                    String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                    try {
                        registerPurgingTasks(artifactPath);
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED);
                    } catch (DeploymentException e) {
                        artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_FAILED);
                        throw e;
                    }
                } else if (files.size() != 0) {
                    log.warn("Purging artifact must have a single XML file to be deployed. But " + files.size() + " " +
                             "files found");
                }
            }
        }
    }

    @Override
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfiguration)
            throws DeploymentException {
        if (Boolean.getBoolean(Constants.DISABLE_ANALYTICS_DATA_PURGING_JVM_OPTION)) {
            return;
        }
        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig().getApplicationArtifact()
                .getDependencies();
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (TYPE.equals(artifact.getType())) {
                List<CappFile> files = artifact.getFiles();
                if (files.size() != 1) {
                    log.error("Purging artifact must have a single XML file to be deployed. But " + files.size() + " " +
                              "files found");
                    continue;
                }
                if (AppDeployerConstants.DEPLOYMENT_STATUS_DEPLOYED.equals(artifact.getDeploymentStatus())) {
                    String fileName = artifact.getFiles().get(0).getName();
                    fileName = GenericUtils.checkAndReturnPath(fileName);
                    String artifactPath = artifact.getExtractedPath() + File.separator + fileName;
                    deletePurgingTasks(artifactPath);
                    artifact.setDeploymentStatus(AppDeployerConstants.DEPLOYMENT_STATUS_PENDING);
                }
            }
        }
    }

    private void deletePurgingTasks(String scriptFilePath) {
        File deploymentFileData = new File(scriptFilePath);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Undeploying purging task : " + deploymentFileData.getName() + " for tenant : " + tenantId);
        try {
            AnalyticsDataPurgingConfiguration purgingConfiguration = getAnalyticsDataPurgingConfiguration(deploymentFileData);
            if (purgingConfiguration != null) {
                if (purgingConfiguration.getPurgingIncludeTables() != null) {
                    TaskManager taskManager = AnalyticsServiceHolder.getTaskService().getTaskManager(Constants.ANALYTICS_DATA_PURGING);
                    for (AnalyticsDataPurgingIncludeTable analyticsDataPurgingIncludeTable : purgingConfiguration.getPurgingIncludeTables()) {
                        if (analyticsDataPurgingIncludeTable.getValue() != null && !analyticsDataPurgingIncludeTable
                                .getValue().isEmpty()) {
                            taskManager.deleteTask(getDataPurgingTaskName(analyticsDataPurgingIncludeTable.getValue()));
                        }
                    }
                }
            }
        } catch (TaskException e) {
            log.error("Unable to get task manager instance for ANALYTICS_DATA_PURGING:" + e.getMessage(), e);
        }
    }

    private void registerPurgingTasks(String scriptFilePath) throws DeploymentException {
        File deploymentFileData = new File(scriptFilePath);
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        log.info("Deploying purging task : " + deploymentFileData.getName() + " for tenant : " + tenantId);
        try {
            AnalyticsDataPurgingConfiguration purgingConfiguration = getAnalyticsDataPurgingConfiguration(deploymentFileData);
            if (purgingConfiguration != null) {
                if (purgingConfiguration.getPurgingIncludeTables() != null) {
                    TaskManager taskManager = AnalyticsServiceHolder.getTaskService().getTaskManager(Constants.ANALYTICS_DATA_PURGING);
                    for (AnalyticsDataPurgingIncludeTable analyticsDataPurgingIncludeTable : purgingConfiguration.getPurgingIncludeTables()) {
                        if (analyticsDataPurgingIncludeTable.getValue() != null && !analyticsDataPurgingIncludeTable
                                .getValue().isEmpty()) {
                            TaskInfo dataPurgingTask = createDataPurgingTask(analyticsDataPurgingIncludeTable.getValue(), purgingConfiguration
                                    .getCronExpression(), purgingConfiguration.getRetentionDays());
                            taskManager.registerTask(dataPurgingTask);
                            taskManager.rescheduleTask(dataPurgingTask.getName());
                        }
                    }
                }
            }
        } catch (TaskException e) {
            log.error("Unable to get task manager instance for ANALYTICS_DATA_PURGING:" + e.getMessage(), e);
        }
    }

    private AnalyticsDataPurgingConfiguration getAnalyticsDataPurgingConfiguration(File deploymentFileData) {
        AnalyticsDataPurgingConfiguration purgingConfiguration = null;
        try {
            JAXBContext context = JAXBContext.newInstance(AnalyticsDataPurgingConfiguration.class);
            Unmarshaller un = context.createUnmarshaller();
            purgingConfiguration = (AnalyticsDataPurgingConfiguration) un.unmarshal(deploymentFileData);
        } catch (JAXBException e) {
            String errorMsg = "Error while reading the purging details : " + deploymentFileData.getAbsolutePath();
            log.error(errorMsg, e);
        }
        return purgingConfiguration;
    }

    private TaskInfo createDataPurgingTask(String table, String cronString, int retentionPeriod) {
        String taskName = getDataPurgingTaskName(table);
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(cronString);
        Map<String, String> taskProperties = new HashMap<>(4);
        taskProperties.put(Constants.RETENTION_PERIOD, String.valueOf(retentionPeriod));
        taskProperties.put(Constants.TABLE, table);
        taskProperties.put(Constants.TENANT_ID, String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId()));
        taskProperties.put(Constants.CRON_STRING, cronString);
        return new TaskInfo(taskName, AnalyticsDataPurgingTask.class.getName(), taskProperties, triggerInfo);
    }

    private String getDataPurgingTaskName(String table) {
        return PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain() + "_" + table + "_" + "data_purging_task";
    }
}
