package org.wso2.carbon.status.dashboard.core.config;

/**
 * .
 */
public class DBQueries {
    private String tableCheckQuery;
    private String recordExistsQuery;
    private String recordSelectQuery;
    private String recordInsertQuery;
    private String recordUpdateQuery;
    private String recordDeleteQuery;

    public String getTableCheckQuery() {
        return tableCheckQuery;
    }

    public void setTableCheckQuery(String tableCheckQuery) {
        this.tableCheckQuery = tableCheckQuery;
    }

    public String getRecordExistsQuery() {
        return recordExistsQuery;
    }

    public void setRecordExistsQuery(String recordExistsQuery) {
        this.recordExistsQuery = recordExistsQuery;
    }

    public String getRecordSelectQuery() {
        return recordSelectQuery;
    }

    public void setRecordSelectQuery(String recordSelectQuery) {
        this.recordSelectQuery = recordSelectQuery;
    }

    public String getRecordInsertQuery() {
        return recordInsertQuery;
    }

    public void setRecordInsertQuery(String recordInsertQuery) {
        this.recordInsertQuery = recordInsertQuery;
    }

    public String getRecordUpdateQuery() {
        return recordUpdateQuery;
    }

    public void setRecordUpdateQuery(String recordUpdateQuery) {
        this.recordUpdateQuery = recordUpdateQuery;
    }

    public String getRecordDeleteQuery() {
        return recordDeleteQuery;
    }

    public void setRecordDeleteQuery(String recordDeleteQuery) {
        this.recordDeleteQuery = recordDeleteQuery;
    }
}
