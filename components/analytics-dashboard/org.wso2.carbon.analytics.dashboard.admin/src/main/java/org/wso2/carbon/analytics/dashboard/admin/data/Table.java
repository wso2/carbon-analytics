package org.wso2.carbon.analytics.dashboard.admin.data;

public class Table {
    private String name;
    private Row[] rows;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Row[] getRows() {
        return rows;
    }

    public void setRows(Row[] rows) {
        this.rows = rows;
    }
}
