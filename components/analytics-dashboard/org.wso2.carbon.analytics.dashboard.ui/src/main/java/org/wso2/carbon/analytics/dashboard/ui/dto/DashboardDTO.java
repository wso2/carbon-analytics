package org.wso2.carbon.analytics.dashboard.ui.dto;

public class DashboardDTO {
    private String id;
    private String title;
    private String group;

    public DashboardDTO(String id, String title, String group) {
        this.id = id;
        this.title = title;
        this.group = group;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }
}
