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
package org.wso2.carbon.analytics.spark.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.internal.AnalyticsPersistenceManager;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of AnalyticsProcessorService to deal with actual script store,
 * and spark execution engine.
 */
public class CarbonAnalyticsProcessorService implements AnalyticsProcessorService {

    private static final Log log = LogFactory.getLog(CarbonAnalyticsProcessorService.class);

    /**
     * Save the script information in the tenant registry.
     *
     * @param tenantId       Id of the tenant for which this operation belongs to.
     * @param scriptName     Name of the script which needs to be saved.
     * @param scriptContent  queries content of the script.
     * @param cronExpression The cron expression to specify the
     *                       scheduling time interval of the script.
     * @throws AnalyticsPersistenceException
     */
    public void saveScript(int tenantId, String scriptName, String scriptContent, String cronExpression)
            throws AnalyticsPersistenceException {
        try {
            AnalyticsPersistenceManager.getInstance().saveScript(tenantId, scriptName, scriptContent, cronExpression, null, true);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error occurred when persisting the script. " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void pauseAllScripts(int tenantId) throws AnalyticsExecutionException {
        try {
            List<TaskInfo> taskInfoList = ServiceHolder.getTaskManager().getAllTasks();
            for (TaskInfo task : taskInfoList) {
                if(task.getProperties().get(AnalyticsConstants.TASK_TENANT_ID_PROPERTY).
                        equals(String.valueOf(tenantId))){
                    ServiceHolder.getTaskManager().pauseTask(task.getName());
                    if (log.isDebugEnabled()) {
                        log.debug("Spark script " + task.getName() + " of tenant " + tenantId +
                                " is successfully paused.");
                    }
                }
            }
        } catch (TaskException e) {
            String errorMessage = "Error occurred when pausing the scripts for tenant " + tenantId;
            log.error(errorMessage + e.getMessage(), e);
            throw new AnalyticsExecutionException(e.getMessage() + errorMessage,e);
        }
    }

    @Override
    public List<TaskInfo> getScheduledTasks() throws AnalyticsExecutionException {
        try {
            return ServiceHolder.getTaskManager().getAllTasks();
        } catch (TaskException e) {
            String errorMessage = "Error while retrieving scheduled task scripts.";
            throw new AnalyticsExecutionException(e.getMessage() + errorMessage,e);
        }
    }

    @Override
    public void resumeAllScripts(int tenantId) throws AnalyticsExecutionException {
        try {
            List<TaskInfo> taskInfoList = ServiceHolder.getTaskManager().getAllTasks();
            for (TaskInfo task : taskInfoList) {
                if(task.getProperties().get(AnalyticsConstants.TASK_TENANT_ID_PROPERTY).
                        equals(String.valueOf(tenantId))){
                    ServiceHolder.getTaskManager().resumeTask(task.getName());
                    if (log.isDebugEnabled()) {
                        log.debug("Spark script " + task.getName() + " of tenant " + tenantId +
                                " is successfully resumed.");
                    }
                }
            }
        } catch (TaskException e) {
            String errorMessage = "Error occurred when resuming the scripts for tenant " + tenantId;
            log.error(errorMessage, e);
            throw new AnalyticsExecutionException(errorMessage + e.getMessage(),e);
        }
    }

    /**
     * Delete the script with provided name, from the analytics store
     * from the provided tenant registry space.
     *
     * @param tenantId   Id of the tenant for which this operation belongs to.
     * @param scriptName Name of the script which needs to be deleted.
     * @throws AnalyticsPersistenceException
     */
    public void deleteScript(int tenantId, String scriptName) throws AnalyticsPersistenceException {
        try {
            AnalyticsPersistenceManager.getInstance().deleteScript(tenantId, scriptName);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error while deleting the script : " + scriptName, e);
            throw e;
        }
    }

    /**
     * Update the script information with given details for the specified the tenant registry.
     *
     * @param tenantId       Id of the tenant for which this operation belongs to.
     * @param scriptName     Name of the script for which the information needs to be updated.
     * @param scriptContent  New queries content of the script.
     * @param cronExpression New cron expression of the script.
     * @throws AnalyticsPersistenceException
     */
    public void updateScript(int tenantId, String scriptName, String scriptContent, String cronExpression)
            throws AnalyticsPersistenceException {
        try {
            AnalyticsPersistenceManager.getInstance().putScript(tenantId, scriptName, scriptContent, cronExpression,
                    null, true);
        } catch (AnalyticsPersistenceException e) {
            log.error("Error while updating the script : " + scriptName, e);
            throw e;
        }
    }

    /**
     * Get all the scripts in the provided tenant registry space and returns back.
     *
     * @param tenantId Id of the tenant for which this operation belongs to.
     * @return List of Analytics Scripts which will have associated name, queries, and cron expression.
     * @throws AnalyticsPersistenceException
     */
    public List<AnalyticsScript> getAllScripts(int tenantId) throws AnalyticsPersistenceException {
        return AnalyticsPersistenceManager.getInstance().getAllAnalyticsScripts(tenantId);
    }

    /**
     * Get a specific analytics scripts information with given script name in the provided tenant space.
     *
     * @param tenantId Id of the tenant for which this operation belongs to.
     * @param name     Name of the script.
     * @return Complete information of the script.
     * @throws AnalyticsPersistenceException
     */
    public AnalyticsScript getScript(int tenantId, String name) throws AnalyticsPersistenceException {
        try {
            return AnalyticsPersistenceManager.getInstance().getAnalyticsScript(tenantId, name);
        } catch (AnalyticsPersistenceException ex) {
            log.error("Error while retrieving the script : " + name, ex);
            throw ex;
        }
    }

    /**
     * Execute the script with provided name, in the provided tenant space,
     * and return the results of the execution. This is a asynchronous call,
     * and the thread won't be returned until all it's queries has been executed.
     *
     * @param tenantId   Id of the tenant for which this operation belongs to.
     * @param scriptName Name of the script which needs to be executed.
     * @return The Array of result of each queries of the script.
     * @throws AnalyticsExecutionException
     * @throws AnalyticsPersistenceException
     */
    public AnalyticsQueryResult[] executeScript(int tenantId, String scriptName) throws AnalyticsExecutionException,
            AnalyticsPersistenceException {
        if (ServiceHolder.isAnalyticsExecutionEnabled()) {
            try {
                AnalyticsScript script = AnalyticsPersistenceManager.getInstance().getAnalyticsScript(tenantId,
                        scriptName);
                String[] queries = getQueries(script.getScriptContent());
                if (queries == null) {
                    throw new AnalyticsExecutionException("No complete queries provided in the script. "
                            + script.getScriptContent());
                }
                AnalyticsQueryResult[] results = new AnalyticsQueryResult[queries.length];
                int queryIndex = 0;
                for (String query : queries) {
                    results[queryIndex] = executeQuery(tenantId, query);
                    queryIndex++;
                }
                return results;
            } catch (AnalyticsPersistenceException e) {
                log.error("Error while retrieving the script : " + scriptName, e);
                throw e;
            }
        } else {
            String errorMsg = "Analytics query execution is disabled in this node. Therefore cannot executed the " +
                    "script  - " + scriptName;
            log.error(errorMsg);
            throw new AnalyticsExecutionException(errorMsg);
        }
    }

    /**
     * Get each queries in the provided scripts content.Each queries are
     * being identified with semicolon and new line.
     *
     * @param scriptContent Content of the script.
     * @return Each queries existed in the script conent.
     */
    public String[] getQueries(String scriptContent) {
        if (scriptContent != null && !scriptContent.trim().isEmpty()) {
            scriptContent = scriptContent.replaceAll("\\n|\\r", "");
            String[] queries = scriptContent.split(";(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            List<String> processedQueries = new ArrayList<>();
            for (String query : queries) {
                if (query != null && !query.trim().isEmpty()) {
                    processedQueries.add(query);
                }
            }
            return processedQueries.toArray(new String[processedQueries.size()]);
        }
        return null;
    }

    /**
     * Execute the provided single query in the provided tenant space, and return the results back.
     *
     * @param tenantId Id of the tenant for which this operation belongs to.
     * @param query    An query which was asked to execute.
     * @return The result returned from the execution of the query.
     * @throws AnalyticsExecutionException
     */
    public AnalyticsQueryResult executeQuery(int tenantId, String query) throws AnalyticsExecutionException {
        if (ServiceHolder.isAnalyticsExecutionEnabled()) {
            if (query != null && !query.trim().isEmpty()) {
                try {
                    return ServiceHolder.getAnalyticskExecutor().executeQuery(tenantId, query);
                } catch (AnalyticsExecutionException e) {
                    log.error("Error while executing query : " + query, e);
                    throw e;
                }
            } else {
                log.error("No queries provided to execute at tenant id :" + tenantId);
                throw new AnalyticsExecutionException("No queries provided to execute.");
            }
        } else {
            String errorMsg = "Spark query execution is disabled in this node. Therefore cannot executed the query " +
                    "submitted - " + query;
            log.error(errorMsg);
            throw new AnalyticsExecutionException(errorMsg);
        }
    }

    @Override
    public boolean isAnalyticsExecutionEnabled() {
        return ServiceHolder.isAnalyticsExecutionEnabled();
    }

    /**
     * Checks whether the analytics scheduled task for analytics script is running in background.
     *
     * @param scriptName
     * @return
     * @throws AnalyticsExecutionException
     */
    @Override
    public boolean isAnalyticsTaskExecuting(String scriptName) throws AnalyticsExecutionException {
        if (null != scriptName && !scriptName.trim().isEmpty()) {
            try {
                TaskManager.TaskState state = ServiceHolder.getTaskManager().getTaskState(scriptName);
                return null != state && state == TaskManager.TaskState.BLOCKED;
            } catch (TaskException e) {
                if (e.getCode().equals(TaskException.Code.NO_TASK_EXISTS)){
                    return false;
                } else {
                    log.error("Error while retrieving the status of the task:" + scriptName, e);
                    throw new AnalyticsExecutionException("Error while retrieving the status of the task:" +
                            scriptName, e);
                }
            }
        } else {
            return false;
        }
    }
}
