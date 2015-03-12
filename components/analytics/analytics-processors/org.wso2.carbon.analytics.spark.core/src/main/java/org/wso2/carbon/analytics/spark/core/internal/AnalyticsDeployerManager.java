/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.spark.core.internal;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.AnalyticsDeployer;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.analytics.spark.core.AnalyticsTask;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsDeploymentException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AnalyticsDeployerManager {
    private static final Log log = LogFactory.getLog(AnalyticsDeployerManager.class);
    private static AnalyticsDeployerManager instance = new AnalyticsDeployerManager();
    private Map<Integer, List<AnalyticsScript>> tenantScriptsCache;
    private List<DeploymentFileData> pausedDeployments;

    private AnalyticsDeployerManager() {
        this.tenantScriptsCache = new ConcurrentHashMap<>();
        this.pausedDeployments = new ArrayList<>();
    }

    public static AnalyticsDeployerManager getInstance() {
        return instance;
    }

    public void deploy(int tenantId, AnalyticsScript analyticsScript) throws AnalyticsDeploymentException {
        addAnalyticsScriptIfNotExists(tenantId, analyticsScript);
        if (analyticsScript.getCronExpression() != null) {
            Map<String, String> properties = new HashMap<>();
            properties.put(AnalyticsConstants.TASK_TENANT_ID_PROPERTY, String.valueOf(tenantId));
            properties.put(AnalyticsConstants.TASK_SCRIPT_NAME_PROPERTY, analyticsScript.getName());
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(analyticsScript.getCronExpression());
            TaskInfo taskInfo = new TaskInfo(analyticsScript.getName(), AnalyticsTask.class.getCanonicalName(),
                    properties, triggerInfo);
            try {
                ServiceHolder.getTaskManager().registerTask(taskInfo);
                ServiceHolder.getTaskManager().rescheduleTask(taskInfo.getName());
            } catch (TaskException e) {
                removeAnalyticsScriptIfExists(tenantId, analyticsScript.getName());
                throw new AnalyticsDeploymentException("Error while trying to schedule task for script : "
                        + analyticsScript.getName() + " for tenant: " + tenantId + " with cron expression :"
                        + analyticsScript.getCronExpression() + " ." + e.getMessage(), e);
            }
        }
    }

    public void unDeploy(int tenantId, String analyticsScriptName) throws AnalyticsDeploymentException {
        AnalyticsScript script = getAnalyticsScript(tenantId, analyticsScriptName);
        if (script.getCronExpression() != null) {
            try {
                ServiceHolder.getTaskManager().deleteTask(analyticsScriptName);
            } catch (TaskException e) {
                throw new AnalyticsDeploymentException("Error while trying to un schedule task for script : "
                        + analyticsScriptName + " for tenant: " + tenantId + " with cron expression :"
                        + script.getCronExpression() + " ." + e.getMessage(), e);
            }
        }
        removeAnalyticsScriptIfExists(tenantId, analyticsScriptName);
    }

    public List<AnalyticsScript> getAnalyticScripts(int tenantId) {
        List<AnalyticsScript> scripts = tenantScriptsCache.get(tenantId);
        if (scripts == null) {
            synchronized (this) {
                scripts = tenantScriptsCache.get(tenantId);
                if (scripts == null) {
                    scripts = new ArrayList<>();
                    tenantScriptsCache.put(tenantId, scripts);
                }
            }
        }
        return scripts;
    }

    private void removeAnalyticsScriptIfExists(int tenantId, String analyticsScriptName) {
        List<AnalyticsScript> analyticsScripts = getAnalyticScripts(tenantId);
        for (AnalyticsScript analyticsScript : analyticsScripts) {
            if (analyticsScript.getName().equals(analyticsScriptName)) {
                analyticsScripts.remove(analyticsScript);
                break;
            }
        }
    }

    private void addAnalyticsScriptIfNotExists(int tenantId, AnalyticsScript analyticsScript) throws AnalyticsDeploymentException {
        List<AnalyticsScript> scripts = getAnalyticScripts(tenantId);
        for (AnalyticsScript script : scripts) {
            if (script.getName().equals(analyticsScript.getName())) {
                throw new AnalyticsDeploymentException("Already a script exists with name : " + script.getName()
                        + " therefore cannot deploy the new script name: " + analyticsScript.getName());
            }
        }
        scripts.add(analyticsScript);
    }

    public AnalyticsScript getAnalyticsScript(int tenantId, String scriptName) throws AnalyticsDeploymentException {
        List<AnalyticsScript> scripts = getAnalyticScripts(tenantId);
        for (AnalyticsScript script : scripts) {
            if (script.getName().equals(scriptName)) {
                return script;
            }
        }
        throw new AnalyticsDeploymentException("No script deployed with script name : " + scriptName);
    }

    public void addPausedDeployment(DeploymentFileData deploymentFileData){
        this.pausedDeployments.add(deploymentFileData);
    }

    public void cleanupPausedDeployments(){
        AnalyticsDeployer deployer = new AnalyticsDeployer();
        for (DeploymentFileData deploymentFileData: pausedDeployments){
            try {
                deployer.deploy(deploymentFileData);
            } catch (DeploymentException e) {
                log.error("Error while cleaning up paused deployments. "+ e.getMessage(), e);
            }
        }
    }
}
