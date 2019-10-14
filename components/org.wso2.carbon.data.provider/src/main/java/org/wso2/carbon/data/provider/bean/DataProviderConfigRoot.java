/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.data.provider.bean;

import com.google.gson.JsonElement;

/**
 * Root bean class for data provider configuration.
 */
public class DataProviderConfigRoot {
    private String topic;
    private String widgetName;
    private String username;
    private String dashboardId;
    private String providerName;
    private String action;
    private JsonElement dataProviderConfiguration;

    /**
     * Enum for define the supportive data provider action types.
     */
    public enum Types {
        SUBSCRIBE, UNSUBSCRIBE, POLLING
    }

    public DataProviderConfigRoot() {
        this.topic = "";
        this.widgetName = "";
        this.username = "";
        this.dashboardId = "";
        this.providerName = "";
        this.action = "";
        this.dataProviderConfiguration = null;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getWidgetName() {
        return widgetName;
    }

    public void setWidgetName(String widgetName) {
        this.widgetName = widgetName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDashboardId() {
        return dashboardId;
    }

    public void setDashboardId(String dashboardId) {
        this.dashboardId = dashboardId;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public JsonElement getDataProviderConfiguration() {
        return dataProviderConfiguration;
    }

    public void setDataProviderConfiguration(JsonElement dataProviderConfiguration) {
        this.dataProviderConfiguration = dataProviderConfiguration;
    }
}
