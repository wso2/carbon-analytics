/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.hive.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds a list of ScriptTaskJobs which are pending and the
 * looping functionality to add the task ofter task service is available
 *
 */

public class ScriptTaskProcessor {

    private static final Log log = LogFactory.getLog(ScriptTaskProcessor.class);

    private static final ScriptTaskProcessor scriptTaskProcessor = new ScriptTaskProcessor();

    private List<ScriptTaskJob> pendingTasksData = new ArrayList<ScriptTaskJob>();

    private ScriptTaskProcessor() {
    }

    public List<ScriptTaskJob> getPendingTasksData() {
        return pendingTasksData;
    }

    public void setPendingTasksData(List<ScriptTaskJob> pendingTasksData) {
        this.pendingTasksData = pendingTasksData;
    }

    public static ScriptTaskProcessor getInstance() {
        return scriptTaskProcessor;
    }

    public synchronized void addTask(ScriptTaskJob pendingTask) {
        if (ServiceHolder.getTaskService() != null) {
            this.processTask(pendingTask);
        } else {
            pendingTasksData.add(pendingTask);
        }
    }

    private void processTask(ScriptTaskJob taskData) {
        int tenantId = taskData.getTenantId();
        TaskInfo info = new TaskInfo(taskData.getName(), HiveConstants.HIVE_DEFAULT_TASK_CLASS , taskData.getProperties(), taskData.getTriggerInfo());
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getCurrentContext().setTenantId(tenantId, true);
            ServiceHolder.getTaskManager().registerTask(info);
            ServiceHolder.getTaskManager().rescheduleTask(info.getName());
            if (log.isDebugEnabled()) {
                log.debug("Registered script execution task : " + info.getName() +
                        " for tenant : " + tenantId);
            }
        } catch (TaskException e) {
            log.error("Error while scheduling script : " + info.getName() + " for tenant : " +
                    tenantId + "..", e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    public synchronized void processPendingTasks() {
        for (ScriptTaskJob taskData : pendingTasksData) {
            this.processTask(taskData);
        }
        pendingTasksData.clear();
    }

}
