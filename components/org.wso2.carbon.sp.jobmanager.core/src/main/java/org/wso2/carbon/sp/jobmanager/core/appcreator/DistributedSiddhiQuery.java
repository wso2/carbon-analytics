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

import java.util.List;

/**
 * POJO Class to hold created query groups and other information of a single Siddhi Apps.
 */
public class DistributedSiddhiQuery {
    private String appName;
    private List<DeployableSiddhiQueryGroup> queryGroups;

    public DistributedSiddhiQuery(String appName, List<DeployableSiddhiQueryGroup> queryGroups) {
        this.appName = appName;
        this.queryGroups = queryGroups;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public List<DeployableSiddhiQueryGroup> getQueryGroups() {
        return queryGroups;
    }

    public void setQueryGroups(List<DeployableSiddhiQueryGroup> queryGroups) {
        this.queryGroups = queryGroups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DistributedSiddhiQuery that = (DistributedSiddhiQuery) o;
        if (getAppName() != null ? !getAppName().equals(that.getAppName()) : that.getAppName() != null) {
            return false;
        }
        return getQueryGroups() != null
                ? getQueryGroups().equals(that.getQueryGroups()) : that.getQueryGroups() == null;
    }

    @Override
    public int hashCode() {
        int result = getAppName() != null ? getAppName().hashCode() : 0;
        result = 31 * result + (getQueryGroups() != null ? getQueryGroups().hashCode() : 0);
        return result;
    }
}
