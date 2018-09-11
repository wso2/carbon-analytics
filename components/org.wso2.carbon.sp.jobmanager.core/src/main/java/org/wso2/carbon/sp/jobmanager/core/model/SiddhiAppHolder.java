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

package org.wso2.carbon.sp.jobmanager.core.model;

import java.io.Serializable;

/**
 * Represents a deployed child Siddhi app.
 */
public class SiddhiAppHolder implements Serializable {
    private static final long serialVersionUID = 3345845929151967554L;
    private String parentAppName;
    private String groupName;
    private String appName;
    private String siddhiApp;
    private ResourceNode deployedNode;
    private boolean isReceiverQueryGroup;
    private int parallelism;

    public SiddhiAppHolder(String parentAppName, String groupName, String appName, String siddhiApp,
                           ResourceNode deployedNode, Boolean isReceiverQueryGroup, int parallelism) {
        this.parentAppName = parentAppName;
        this.groupName = groupName;
        this.appName = appName;
        this.siddhiApp = siddhiApp;
        this.deployedNode = deployedNode;
        this.isReceiverQueryGroup = isReceiverQueryGroup;
        this.parallelism = parallelism;
    }

    public String getParentAppName() {
        return parentAppName;
    }

    public void setParentAppName(String parentAppName) {
        this.parentAppName = parentAppName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getSiddhiApp() {
        return siddhiApp;
    }

    public void setSiddhiApp(String siddhiApp) {
        this.siddhiApp = siddhiApp;
    }

    public ResourceNode getDeployedNode() {
        return deployedNode;
    }

    public void setDeployedNode(ResourceNode deployedNode) {
        this.deployedNode = deployedNode;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public boolean isReceiverQueryGroup() {
        return isReceiverQueryGroup;
    }

    public void setReceiverQueryGroup(boolean receiverQueryGroup) {
        isReceiverQueryGroup = receiverQueryGroup;
    }

    public int getParallelism() {
        return parallelism;
    }

    @Override
    public String toString() {
        return String.format("SiddhiApp { parentName: %s, groupName: %s, appName: %s }",
                getParentAppName(), getGroupName(), getAppName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SiddhiAppHolder that = (SiddhiAppHolder) o;
        if (getParentAppName() != null
                ? !getParentAppName().equals(that.getParentAppName())
                : that.getParentAppName() != null) {
            return false;
        }
        if (getGroupName() != null
                ? !getGroupName().equals(that.getGroupName())
                : that.getGroupName() != null) {
            return false;
        }
        if (getAppName() != null
                ? !getAppName().equals(that.getAppName())
                : that.getAppName() != null) {
            return false;
        }
        if (isReceiverQueryGroup != that.isReceiverQueryGroup()) {
            return false;
        }
        return getSiddhiApp() != null
                ? getSiddhiApp().equals(that.getSiddhiApp())
                : that.getSiddhiApp() == null;
    }

    @Override
    public int hashCode() {
        int result = getParentAppName() != null ? getParentAppName().hashCode() : 0;
        result = 31 * result + (getGroupName() != null ? getGroupName().hashCode() : 0);
        result = 31 * result + (getAppName() != null ? getAppName().hashCode() : 0);
        result = 31 * result + (getSiddhiApp() != null ? getSiddhiApp().hashCode() : 0);
        return result;
    }
}
