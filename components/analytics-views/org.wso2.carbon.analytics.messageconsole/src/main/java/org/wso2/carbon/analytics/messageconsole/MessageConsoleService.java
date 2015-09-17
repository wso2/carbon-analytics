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
package org.wso2.carbon.analytics.messageconsole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.dataservice.core.tasks.AnalyticsDataPurgingTask;
import org.wso2.carbon.analytics.messageconsole.beans.PermissionBean;
import org.wso2.carbon.analytics.messageconsole.beans.ScheduleTaskInfo;
import org.wso2.carbon.analytics.messageconsole.exception.MessageConsoleException;
import org.wso2.carbon.analytics.messageconsole.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is the service class for message console. This represent all the message console backend operation.
 */

public class MessageConsoleService extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(MessageConsoleService.class);
    private static final String AT_SIGN = "@";

    public MessageConsoleService() {
    }

    /**
     * This method will return PermissionBean that contains permissions regarding message console operations for
     * logged in user.
     *
     * @return PermissionBean instance
     * @throws MessageConsoleException
     */
    public PermissionBean getAvailablePermissions() throws MessageConsoleException {
        PermissionBean permission = new PermissionBean();
        String username = super.getUsername();
        try {
            AuthorizationManager authorizationManager = getUserRealm().getAuthorizationManager();
            permission.setListTable(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_LIST_TABLE,
                                                                          CarbonConstants.UI_PERMISSION_ACTION));
            permission.setSearchRecord(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_SEARCH_RECORD,
                                                                             CarbonConstants.UI_PERMISSION_ACTION));
            permission.setListRecord(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_LIST_RECORD,
                                                                           CarbonConstants.UI_PERMISSION_ACTION));
            permission.setDeleteRecord(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_DELETE_RECORD,
                                                                             CarbonConstants.UI_PERMISSION_ACTION));
        } catch (UserStoreException e) {
            throw new MessageConsoleException("Unable to get user permission details due to " + e.getMessage(), e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Granted analytics permission for user[" + username + "] :" + permission.toString());
        }
        return permission;
    }

    /**
     * This method is use to get logged in username with tenant domain
     *
     * @return Username with tenant domain
     */
    @Override
    protected String getUsername() {
        return super.getUsername() + AT_SIGN + super.getTenantDomain();
    }

    /**
     * Scheduling data purging task for given table
     *
     * @param table           Table name that need to purge
     * @param cronString      Task cron schedule  information
     * @param retentionPeriod Data retention period
     * @throws MessageConsoleException
     */
    public void scheduleDataPurging(String table, String cronString, int retentionPeriod)
            throws MessageConsoleException {
        try {
            TaskInfo taskInfo = createDataPurgingTask(table, cronString, retentionPeriod);
            TaskManager taskManager = ServiceHolder.getTaskService().getTaskManager(Constants.ANALYTICS_DATA_PURGING);
            if (taskManager != null) {
                taskManager.deleteTask(taskInfo.getName());
                if (cronString != null) {
                    taskManager.registerTask(taskInfo);
                    taskManager.rescheduleTask(taskInfo.getName());
                }
            } else {
                log.warn("TaskManager instance is null");
            }
        } catch (TaskException e) {
            throw new MessageConsoleException("Unable to schedule a purging task for " + table + " with corn " +
                                              "schedule[" + cronString + "] due to " + e.getMessage(), e);
        }
    }

    /**
     * Get already schedule information for given table
     *
     * @param table Table name that need to get task information
     * @return ScheduleTaskInfo instance
     * @throws MessageConsoleException
     */
    public ScheduleTaskInfo getDataPurgingDetails(String table) throws MessageConsoleException {
        ScheduleTaskInfo taskInfo = new ScheduleTaskInfo();
        try {
            TaskManager taskManager = ServiceHolder.getTaskService().getTaskManager(Constants.ANALYTICS_DATA_PURGING);
            if (taskManager != null && taskManager.isTaskScheduled(getDataPurgingTaskName(table))) {
                TaskInfo task = taskManager.getTask(getDataPurgingTaskName(table));
                if (task != null) {
                    taskInfo.setCronString(task.getProperties().get(Constants.CRON_STRING));
                    taskInfo.setRetentionPeriod(Integer.parseInt(task.getProperties().get(Constants.RETENTION_PERIOD)));
                }
            }
        } catch (TaskException e) {
            throw new MessageConsoleException("Unable to get schedule details for " + table + " due to " + e
                    .getMessage(), e);
        }
        return taskInfo;
    }

    private TaskInfo createDataPurgingTask(String table, String cronString, int retentionPeriod) {
        String taskName = getDataPurgingTaskName(table);
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(cronString);
        Map<String, String> taskProperties = new HashMap<>(4);
        taskProperties.put(Constants.RETENTION_PERIOD, String
                .valueOf(retentionPeriod));
        taskProperties.put(Constants.TABLE, table);
        taskProperties.put(Constants.TENANT_ID, String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId()));
        taskProperties.put(Constants.CRON_STRING, cronString);
        return new TaskInfo(taskName, AnalyticsDataPurgingTask.class.getName(), taskProperties, triggerInfo);
    }

    private String getDataPurgingTaskName(String table) {
        return getTenantDomain() + "_" + table + "_" + "data_purging_task";
    }
}
