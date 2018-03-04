package org.wso2.carbon.status.dashboard.core.bean;

import java.util.ArrayList;
import java.util.List;

public class ParentSummaryDetails {
    private List <String> groups = new ArrayList<>();
    private int childApps = 0;
    private List<String> usedWorkerNode = new ArrayList<>();


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
}
