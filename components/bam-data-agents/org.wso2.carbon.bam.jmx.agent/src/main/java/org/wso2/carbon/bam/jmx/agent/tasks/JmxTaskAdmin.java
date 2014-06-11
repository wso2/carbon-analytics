/*
*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

package org.wso2.carbon.bam.jmx.agent.tasks;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.jmx.agent.JmxConstant;
import org.wso2.carbon.bam.jmx.agent.profiles.Profile;
import org.wso2.carbon.bam.jmx.agent.tasks.internal.JmxTaskServiceComponent;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

import java.util.List;

public class JmxTaskAdmin {
    private static final Log log = LogFactory.getLog(JmxTaskAdmin.class);

    public void scheduleProfile(Profile profile) throws AxisFault {
        try {
            TaskManager tm = JmxTaskServiceComponent.getTaskService().getTaskManager(
                    JmxConstant.JMX_SERVICE_TASK_TYPE);

            TaskInfo taskInfo = JmxTaskUtils.convert(profile);
            tm.registerTask(taskInfo);
            tm.scheduleTask(taskInfo.getName());
            log.info(profile.getName() + " enabled.");
        } catch (TaskException e) {
            log.error(e);
        }
    }

    public void removeProfile(String profileName) throws AxisFault {
        try {
            TaskManager tm = JmxTaskServiceComponent.getTaskService().getTaskManager(
                    JmxConstant.JMX_SERVICE_TASK_TYPE);
            tm.deleteTask(profileName);
            log.info(profileName + " disabled.");
        } catch (TaskException e) {
            log.error(e);
        }
    }

    public boolean isTaskScheduled(String profileName) throws AxisFault {
        try {
            TaskManager tm = JmxTaskServiceComponent.getTaskService().getTaskManager(
                    JmxConstant.JMX_SERVICE_TASK_TYPE);
            return tm.isTaskScheduled(profileName);
        } catch (TaskException e) {
            log.error(e);
        }
        return false;
    }

    public boolean profileExists(String profileName) {
        try {
            TaskManager tm = JmxTaskServiceComponent.getTaskService().getTaskManager(
                    JmxConstant.JMX_SERVICE_TASK_TYPE);

            List<TaskInfo> taskInfoList = tm.getAllTasks();
            for (TaskInfo taskInfo : taskInfoList) {
                if (taskInfo.getName().equalsIgnoreCase(profileName)) {
                    return true;
                }
            }

        } catch (TaskException e) {
            log.error(e);
        }

        return false;
    }
}
