/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.bam.notification.task.internal;

import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.Keyspace;
import me.prettyprint.hector.api.ddl.ComparatorType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.bam.datasource.utils.DataSourceUtils;
import org.wso2.carbon.bam.notification.task.NotificationDispatchTask;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.TaskInfo.TriggerInfo;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * @scr.component name="bam.notification.task.component" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="event.stream.service" interface="org.wso2.carbon.event.stream.manager.core.EventStreamService"
 * cardinality="1..1" policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService" 
 */
public class NotificationDispatchComponent {

    private static final String DISABLE_NOTIFICATION_TASK = "disable.notification.task";

    private static TaskService taskService;
    
    private static EventStreamService eventStreamService;
    
    private static final Log log = LogFactory.getLog(NotificationDispatchComponent.class);

    protected void activate(ComponentContext ctxt) {
        try {
            if (System.getProperty(DISABLE_NOTIFICATION_TASK) != null) {
                return;
            }
            getTaskService().registerTaskType(NotificationDispatchTask.TASK_TYPE);
            if (log.isDebugEnabled()) {
                log.debug("Notification dispatch bundle is activated ");
            }
            this.initRecordStore();
            this.setupNotificationTask();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }
    
    private void initRecordStore() throws Exception {
        Object[] objs = DataSourceUtils.getClusterKeyspaceFromRDBMSDataSource(
                MultitenantConstants.SUPER_TENANT_ID, 
                NotificationDispatchTask.BAM_CASSANDRA_UTIL_DATASOURCE);
        Cluster cluster = (Cluster) objs[0];
        Keyspace ks = (Keyspace) objs[1];
        DataSourceUtils.createColumnFamilyIfNotExist(cluster, 
                ks.getKeyspaceName(), 
                NotificationDispatchTask.BAM_NOTIFICATION_CF, 
                ComparatorType.UTF8TYPE);
    }
    
    private void setupNotificationTask() throws Exception {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(
                    MultitenantConstants.SUPER_TENANT_ID, true);
            TaskManager tm = getTaskService().getTaskManager(NotificationDispatchTask.TASK_TYPE);
            TriggerInfo triggerInfo = new TriggerInfo(null, null, 
                    NotificationDispatchTask.TASK_INTERVAL, -1);
            TaskInfo taskInfo = new TaskInfo(NotificationDispatchTask.TASK_NAME, 
                    NotificationDispatchTask.class.getName(), null, triggerInfo);
            tm.registerTask(taskInfo);
            tm.rescheduleTask(NotificationDispatchTask.TASK_NAME);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
    
    protected void setTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Task Service");
        }
        NotificationDispatchComponent.taskService = taskService;
    }

    protected void unsetTaskService(TaskService taskService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Task Service");
        }
        NotificationDispatchComponent.taskService = null;
    }

    public static TaskService getTaskService() {
        return NotificationDispatchComponent.taskService;
    }
    
    protected void setEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting the Event Stream Service");
        }
        NotificationDispatchComponent.eventStreamService = eventStreamService;
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        if (log.isDebugEnabled()) {
            log.debug("Unsetting the Event Stream Service");
        }
        NotificationDispatchComponent.eventStreamService = null;
    }

    public static EventStreamService getEventStreamService() {
        return NotificationDispatchComponent.eventStreamService;
    }

}
