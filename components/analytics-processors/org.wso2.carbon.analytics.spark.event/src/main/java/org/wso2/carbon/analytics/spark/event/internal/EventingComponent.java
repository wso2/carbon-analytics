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
package org.wso2.carbon.analytics.spark.event.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.spark.event.EventStreamDataStore;
import org.wso2.carbon.analytics.spark.event.EventingConstants;
import org.wso2.carbon.analytics.spark.event.EventingTask;
import org.wso2.carbon.analytics.spark.event.SparkEventingTaskLocationResolver;
import org.wso2.carbon.event.processor.manager.core.EventManagementService;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * Declarative service component for spark evening.
 *
 * @scr.component name="spark.eventing" immediate="true"
 * @scr.reference name="event.streamService" interface="org.wso2.carbon.event.stream.core.EventStreamService"
 * cardinality="1..1" policy="dynamic" bind="setEventStreamService" unbind="unsetEventStreamService"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 * @scr.reference name="eventManagement.service"
 * interface="org.wso2.carbon.event.processor.manager.core.EventManagementService" cardinality="1..1"
 * policy="dynamic" bind="setEventManagementService" unbind="unsetEventManagementService"
 */
public class EventingComponent {

    private static final Log log = LogFactory.getLog(EventingComponent.class);

    protected void activate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Activating Spark Eventing");
        }
        ServiceHolder.setEventPublisherManagementService(new SparkEventingPublisherManagementService());
        ServiceHolder.getEventManagementService().subscribe(ServiceHolder.getEventPublisherManagementService());
        this.initializeSparkEventingTask();
        if (log.isDebugEnabled()) {
            log.debug("Spark Eventing Activated");
        }
    }
    
    private void initializeSparkEventingTask() {
        try {
            if (this.isReceiverNode() && !this.isSparkEventingTaskDisabled()) {
                EventStreamDataStore.initStore();
                ServiceHolder.getTaskService().registerTaskType(EventingConstants.ANALYTICS_SPARK_EVENTING_TASK_TYPE);
                TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(null, null, 
                        EventingConstants.SPARK_EVENTING_TASK_RUN_INTERVAL_MS, -1);
                triggerInfo.setDisallowConcurrentExecution(true);
                TaskInfo taskInfo = new TaskInfo(EventingConstants.ANALYTICS_SPARK_EVENTING_TASK_NAME, 
                        EventingTask.class.getCanonicalName(), null, triggerInfo);
                taskInfo.setLocationResolverClass(SparkEventingTaskLocationResolver.class.getCanonicalName());
                TaskManager tm = ServiceHolder.getTaskService().getTaskManager(EventingConstants.ANALYTICS_SPARK_EVENTING_TASK_TYPE);
                tm.registerTask(taskInfo);
                tm.rescheduleTask(taskInfo.getName());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while scheduling Spark eventing task: " + e.getMessage(), e);
        }
    }

    protected void deactivate(ComponentContext ctx) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating Spark Eventing");
        }
    }

    protected void setEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        ServiceHolder.setEventStreamService(null);
    }
    
    private boolean isReceiverNode() {
        String propVal = System.getProperty(EventingConstants.DISABLE_EVENT_SINK_SYS_PROP);
        if (propVal == null) {
            return true;
        } else {
            return !Boolean.parseBoolean(propVal);
        }
    }
    
    private boolean isSparkEventingTaskDisabled() {
        String propVal = System.getProperty(EventingConstants.DISABLE_SPARK_EVENTING_TASK_SYS_PROP);
        if (propVal == null) {
            return false;
        } else {
            return Boolean.parseBoolean(propVal);
        }
    }
    
    protected void setTaskService(TaskService taskService) {
        ServiceHolder.setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        ServiceHolder.setTaskService(null);
    }
    
    protected void setEventManagementService(EventManagementService eventManagementService) {
        ServiceHolder.setEventManagementService(eventManagementService);
    }

    protected void unsetEventManagementService(EventManagementService eventManagementService) {
        ServiceHolder.setEventManagementService(null);
    }

}
