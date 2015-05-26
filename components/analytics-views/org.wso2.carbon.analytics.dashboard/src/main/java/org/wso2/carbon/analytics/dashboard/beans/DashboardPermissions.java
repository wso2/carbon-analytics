package org.wso2.carbon.analytics.dashboard.beans;

import java.util.ArrayList;
import java.util.List;

public class DashboardPermissions {
    private List<String> viewers;
    private List<String> editors;

    public DashboardPermissions() {
        viewers = new ArrayList<String>();
        editors = new ArrayList<String>();
    }

    public List<String> getViewers() {
        return viewers;
    }

    public void setViewers(List<String> viewers) {
        this.viewers = viewers;
    }

    public List<String> getEditors() {
        return editors;
    }

    public void setEditors(List<String> editors) {
        this.editors = editors;
    }
}
