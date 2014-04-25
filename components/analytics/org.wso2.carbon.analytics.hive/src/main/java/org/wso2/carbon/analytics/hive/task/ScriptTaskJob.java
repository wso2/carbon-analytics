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

import org.wso2.carbon.ntask.core.TaskInfo;

import java.util.Map;

/**
 * This class holds the information of TaskInfo object
 */

public class ScriptTaskJob {

    private String name;

    private Map<String, String> properties;

    private int tenantId;

    private TaskInfo.TriggerInfo triggerInfo;

    public ScriptTaskJob(){}

    public ScriptTaskJob(String name, TaskInfo.TriggerInfo triggerInfo, Map<String, String> properties, int tenantId ) {
        this.name = name;
        this.properties = properties;
        this.triggerInfo = triggerInfo;
        this.tenantId = tenantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public TaskInfo.TriggerInfo getTriggerInfo() {
        return triggerInfo;
    }

    public void setTriggerInfo(TaskInfo.TriggerInfo triggerInfo) {
        this.triggerInfo = triggerInfo;
    }

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

}
