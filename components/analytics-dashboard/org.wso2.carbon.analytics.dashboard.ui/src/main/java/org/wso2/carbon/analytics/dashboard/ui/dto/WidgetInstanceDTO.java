package org.wso2.carbon.analytics.dashboard.ui.dto;

public class WidgetInstanceDTO {
    private String id;
    private DimensionDTO  dimensions;

    public WidgetInstanceDTO(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DimensionDTO getDimensions() {
        return dimensions;
    }

    public void setDimensions(DimensionDTO dimensions) {
        this.dimensions = dimensions;
    }
}
