/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.status.dashboard.core.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the summary details of each parent siddhi application.
 */
public class ParentSummaryDetails {
    private List<String> groups = new ArrayList<>();
    private int childApps = 0;
    private List<String> usedWorkerNode = new ArrayList<>();
    private List<String> deployedChildApps = new ArrayList<>();
    private List<String> unDeployedChildApps = new ArrayList<>();


    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public int getChildApps() {
        return childApps;
    }

    public void setChildApps(int childApps) {
        this.childApps = childApps;
    }

    public List<String> getUsedWorkerNode() {
        return usedWorkerNode;
    }

    public void setUsedWorkerNode(List<String> usedWorkerNode) {
        this.usedWorkerNode = usedWorkerNode;
    }

    public List<String> getDeployedChildApps() {
        return deployedChildApps;
    }

    public void setDeployedChildApps(List<String> deployedChildApps) {
        this.deployedChildApps = deployedChildApps;
    }

    public List<String> getUnDeployedChildApps() {
        return unDeployedChildApps;
    }

    public void setUnDeployedChildApps(List<String> unDeployedChildApps) {
        this.unDeployedChildApps = unDeployedChildApps;
    }
}
