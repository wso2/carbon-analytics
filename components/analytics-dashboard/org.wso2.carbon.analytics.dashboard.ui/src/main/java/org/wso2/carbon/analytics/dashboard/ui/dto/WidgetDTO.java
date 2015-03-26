package org.wso2.carbon.analytics.dashboard.ui.dto;

public class WidgetDTO {

    private String id;
    private String title;
    private String config;

    public WidgetDTO(String id, String title, String config) {
        this.id = id;
        this.title = title;
        this.config = config;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
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

}