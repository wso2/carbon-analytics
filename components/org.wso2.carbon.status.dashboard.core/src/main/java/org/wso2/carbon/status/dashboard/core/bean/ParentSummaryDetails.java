package org.wso2.carbon.status.dashboard.core.bean;

import java.util.ArrayList;
import java.util.List;

public class ParentSummaryDetails {
    private List <String> groups = new ArrayList<>();
    private int childApps = 0;
    // TODO: 4/17/18 initialize as null 
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
