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
package org.wso2.carbon.analytics.spark.admin.dto;

/**
 * DTO of analytics script information which transferred between
 * the admin service and client.
 */
public class AnalyticsScriptDto {

    private String name;

    private String scriptContent;

    private String cronExpression;

    private boolean editable;

    public AnalyticsScriptDto(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public String getName() {
        return name;
    }

    public String getScriptContent() {
        return scriptContent;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public boolean isTaskScheduled() {
        return cronExpression != null && !cronExpression.isEmpty();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
