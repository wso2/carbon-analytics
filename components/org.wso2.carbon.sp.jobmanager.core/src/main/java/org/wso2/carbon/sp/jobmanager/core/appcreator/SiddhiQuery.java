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

package org.wso2.carbon.sp.jobmanager.core.appcreator;

/**
 * Gives details about siddhi application.
 */
public class SiddhiQuery {
    private String appName;
    private String app;
    private boolean isReceiverQuery;

    private SiddhiQuery() {
        // Avoiding empty initialization
    }

    public SiddhiQuery(String appName, String app, boolean isReceiverQuery) {
        this.appName = appName;
        this.app = app;
        this.isReceiverQuery = isReceiverQuery;
    }

    public String getAppName() {
        return appName;
    }

    public SiddhiQuery setAppName(String appName) {
        this.appName = appName;
        return this;
    }

    public String getApp() {
        return app;
    }

    public SiddhiQuery setApp(String app) {
        this.app = app;
        return this;
    }

    public boolean isReceiverQuery() {
        return isReceiverQuery;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SiddhiQuery that = (SiddhiQuery) o;
        if (getAppName() != null ? !getAppName().equals(that.getAppName()) : that.getAppName() != null) {
            return false;
        }
        return getApp() != null ? getApp().equals(that.getApp()) : that.getApp() == null;
    }

    @Override
    public int hashCode() {
        int result = getAppName() != null ? getAppName().hashCode() : 0;
        result = 31 * result + (getApp() != null ? getApp().hashCode() : 0);
        return result;
    }
}
