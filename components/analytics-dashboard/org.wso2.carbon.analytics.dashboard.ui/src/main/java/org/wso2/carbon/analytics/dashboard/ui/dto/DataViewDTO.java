package org.wso2.carbon.analytics.dashboard.ui.dto;

public class DataViewDTO {
    private String id;
    private String name;
    private String type;
    private String dataSource;
    private ColumnDTO[] columns;
    private String filter;
    private WidgetDTO[] widgets;

    public DataViewDTO(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public ColumnDTO[] getColumns() {
        return columns;
    }

    public void setColumns(ColumnDTO[] columns) {
        this.columns = columns;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public WidgetDTO[] getWidgets() {
        return widgets;
    }

    public void setWidgets(WidgetDTO[] widgets) {
        this.widgets = widgets;
    }
}
