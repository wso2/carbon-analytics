package org.wso2.carbon.event.output.adaptor.mysql.internal;

import org.wso2.carbon.databridge.commons.Attribute;

import java.util.ArrayList;

public class TableInfo {
    private String tableName;
    private ArrayList<Attribute> columnOrder;
    private String preparedInsertStatement;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ArrayList<Attribute> getColumnOrder() {
        return columnOrder;
    }

    public void setColumnOrder(ArrayList<Attribute> columnOrder) {
        this.columnOrder = columnOrder;
    }

    public String getPreparedInsertStatement() {
        return preparedInsertStatement;
    }

    public void setPreparedInsertStatement(String insertStatementPrefix) {
        this.preparedInsertStatement = insertStatementPrefix;
    }
}