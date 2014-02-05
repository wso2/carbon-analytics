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
package org.wso2.carbon.analytics.hive.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.HiveScriptStoreException;
import org.wso2.carbon.analytics.hive.persistence.HiveScriptPersistenceManager;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

import java.util.HashMap;
import java.util.Map;

public class HiveScriptStoreService {
    private static final Log log = LogFactory.getLog(HiveScriptStoreService.class);

    public String retrieveHiveScript(String scriptName) throws HiveScriptStoreException {
        scriptName = validateScriptName(scriptName);
        if (null != scriptName) {
            HiveScriptPersistenceManager manager = HiveScriptPersistenceManager.getInstance();
            return manager.retrieveScript(scriptName);
        } else {
            log.error("Script name is empty. Please provide a valid script name!");
            throw new HiveScriptStoreException("Script name is empty. Please provide a valid" +
                    " script name!");
        }
    }

    public void saveHiveScript(String scriptName, String scriptContent, String cron)
            throws HiveScriptStoreException {
        scriptName = validateScriptName(scriptName);
        if (null != scriptName) {
            HiveScriptPersistenceManager manager = HiveScriptPersistenceManager.getInstance();
            manager.saveScript(scriptName, scriptContent);
        } else {
            log.error("Script name is empty. Please provide a valid script name!");
            throw new HiveScriptStoreException("Script name is empty. Please provide a valid" +
                    " script name!");
        }

        if (cron != null && !cron.equals("")) {
            deleteTask(scriptName);
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo();
            //triggerInfo.setRepeatCount(sequence.getCount());
            //triggerInfo.setIntervalMillis(sequence.getInterval());
            triggerInfo.setDisallowConcurrentExecution(true);
            triggerInfo.setCronExpression(cron);


            Map<String, String> properties = new HashMap<String, String>();
            properties.put(HiveConstants.HIVE_SCRIPT_NAME, scriptName);
            properties.put(HiveConstants.TASK_TENANT_ID_KEY,
                           String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId()));

            TaskInfo info = new TaskInfo(scriptName, HiveConstants.HIVE_DEFAULT_TASK_CLASS,
                                         properties, triggerInfo);
/*            info.setName(scriptName);
            info.setTriggerInfo(triggerInfo);
            info.setTaskClass(HiveConstants.HIVE_DEFAULT_TASK_CLASS);

            info.setParameters(properties);*/

            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            try {
                ServiceHolder.getTaskManager().registerTask(info);
                ServiceHolder.getTaskManager().scheduleTask(info.getName());
            } catch (TaskException e) {
                log.error("Error while scheduling script : " + info.getName() + " for tenant : " +
                        tenantId + "..", e);
                throw new HiveScriptStoreException("Error while scheduling script : " +
                        info.getName() + " for tenant : " + tenantId +
                        "..", e);
            }

            if (log.isDebugEnabled()) {
                log.debug("Registered script execution task : " + info.getName() +
                        " for tenant : " + tenantId);
            }
        } else {
            deleteTask(scriptName);
        }

    }

    public String getCronExpression(String scriptName) throws HiveScriptStoreException {
        scriptName = validateScriptName(scriptName);
        TaskManager.TaskState taskState = null;
        TaskInfo info = null;
        TaskManager manager = ServiceHolder.getTaskManager();
        try {
            info = manager.getTask(scriptName);
            taskState = manager.getTaskState(scriptName);
        } catch (TaskException ignored) {
            //
        }

        if (info != null && taskState != null) {
            return info.getTriggerInfo().getCronExpression();
        }
        return "";
    }

// Not needed since the saving the same script will overwrite.
//    public void editHiveScript(String scriptName, String scriptContent)
//            throws HiveScriptStoreException {
//        deleteScript(scriptName);
//        saveHiveScript(scriptName, scriptContent);
//    }

    public String[] getAllScriptNames() throws HiveScriptStoreException {
        try {
            HiveScriptPersistenceManager manager = HiveScriptPersistenceManager.getInstance();
            return manager.getAllHiveScriptNames();
        } catch (HiveScriptStoreException e) {
            log.error(e.getMessage(), e);
            throw e;
        }
    }

    public void deleteScript(String scriptName) throws HiveScriptStoreException {
        scriptName = validateScriptName(scriptName);
        if (null != scriptName) {
            deleteTask(scriptName);
            HiveScriptPersistenceManager manager = HiveScriptPersistenceManager.getInstance();
            manager.deleteScript(scriptName);
        } else {
            log.error("Script name is empty. Please provide a valid script name!");
            throw new HiveScriptStoreException("Script name is empty. Please provide a valid" +
                    " script name!");
        }

    }


    private void deleteTask(String scriptName) throws HiveScriptStoreException {
        TaskManager.TaskState taskState = null;
        TaskInfo info = null;
        TaskManager manager = ServiceHolder.getTaskManager();
        try {
            info = manager.getTask(scriptName);
            taskState = manager.getTaskState(scriptName);
        } catch (TaskException ignored) {
            //
        }

        if (info != null && taskState != null) {
            try {
                manager.deleteTask(scriptName);
            } catch (TaskException e) {
                log.error("Error while unscheduling task : " + scriptName + "..", e);
                throw new HiveScriptStoreException("Error while unscheduling task : " + scriptName +
                        "..", e);
            }
        }
    }

    private String validateScriptName(String scriptName) {
        if (scriptName.endsWith(HiveConstants.HIVE_SCRIPT_EXT)) {
            scriptName = scriptName.substring(0, scriptName.length() -
                    HiveConstants.HIVE_SCRIPT_EXT.length());
        }
        return scriptName;
    }

    public boolean isTaskExecuting(String scriptName) throws HiveScriptStoreException {
        if (null != scriptName && !scriptName.trim().isEmpty()) {
            TaskManager manager = ServiceHolder.getTaskManager();
            try {
                TaskManager.TaskState state = manager.getTaskState(scriptName);
                return null != state && state == TaskManager.TaskState.BLOCKED;
            } catch (TaskException e) {
                if (e.getCode().equals(TaskException.Code.NO_TASK_EXISTS)){
                    return false;
                } else {
                    log.error("Error while retrieving the status of the task:" + scriptName, e);
                    throw new HiveScriptStoreException("Error while retrieving the status of the task:" + scriptName, e);
                }
            }
        } else {
            return false;
        }
    }
}
