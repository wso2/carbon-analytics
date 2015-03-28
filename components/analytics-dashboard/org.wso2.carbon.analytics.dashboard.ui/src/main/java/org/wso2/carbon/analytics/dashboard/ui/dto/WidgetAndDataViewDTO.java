package org.wso2.carbon.analytics.dashboard.ui.dto;

import java.util.ArrayList;
import java.util.List;

public class WidgetAndDataViewDTO {
    private String id;
    private String name;
    private String type;
    private String datasource;
    private String filter;
    private List<ColumnDTO> columns;
    private WidgetDTO widget;

    public WidgetAndDataViewDTO() {
        this.columns = new ArrayList<ColumnDTO>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public WidgetDTO getWidget() {
        return widget;
    }

    public void setWidget(WidgetDTO widget) {
        this.widget = widget;
    }

    public List<ColumnDTO> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnDTO> columns) {
        this.columns = columns;
    }
}
