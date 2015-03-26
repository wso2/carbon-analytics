package org.wso2.carbon.analytics.dashboard.ui.dto;

public class TableDTO {
    private String name;
    private Object[] data;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }
}