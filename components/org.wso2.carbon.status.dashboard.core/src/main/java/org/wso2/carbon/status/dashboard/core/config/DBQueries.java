package org.wso2.carbon.status.dashboard.core.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

import java.util.Map;

/**
 * Model class containing database queries.
 */
@Configuration(description = "Database queries configuration")
public class DBQueries {

    //private Map<String, String> propertyMap;
    @Element(description = "tableCheckQuery")
    private String tableCheckQuery;

    @Element(description = "recordExistsQuery")
    private String recordExistsQuery;

    @Element(description = "recordSelectQuery")
    private String recordSelectQuery;

    @Element(description = "recordInsertQuery")
    private String recordInsertQuery;

    @Element(description = "recordUpdateQuery")
    private String recordUpdateQuery;

    @Element(description = "recordDeleteQuery")
    private String recordDeleteQuery;

    @Element(description = "recordSelectSomeQuery")
    private String recordSelectSomeQuery;

    public DBQueries() {}

//    private void setProperties(Map<String, String> propertyMap) {
//        this.tableCheckQuery = propertyMap.get("tableCheckQuery");
//        this.recordExistsQuery = propertyMap.get("recordExistsQuery");
//        this.recordSelectQuery = propertyMap.get("recordSelectQuery");
//        this.recordInsertQuery = propertyMap.get("recordInsertQuery");
//        this.recordUpdateQuery = propertyMap.get("recordUpdateQuery");
//        this.recordDeleteQuery = propertyMap.get("recordDeleteQuery");
//        this.recordSelectSomeQuery = propertyMap.get("recordSelectSomeQuery");
//    }

    public String getTableCheckQuery() {
        return tableCheckQuery;
    }


    public String getRecordExistsQuery() {
        return recordExistsQuery;
    }


    public String getRecordSelectQuery() {
        return recordSelectQuery;
    }


    public String getRecordInsertQuery() {
        return recordInsertQuery;
    }


    public String getRecordUpdateQuery() {
        return recordUpdateQuery;
    }


    public String getRecordDeleteQuery() {
        return recordDeleteQuery;
    }

    public String getRecordSelectSomeQuery() {
        return recordSelectSomeQuery;
    }

    public void setRecordSelectSomeQuery(String recordSelectSomeQuery) {
        this.recordSelectSomeQuery = recordSelectSomeQuery;
    }

    public void setTableCheckQuery(String tableCheckQuery) {
        this.tableCheckQuery = tableCheckQuery;
    }

    public void setRecordExistsQuery(String recordExistsQuery) {
        this.recordExistsQuery = recordExistsQuery;
    }

    public void setRecordSelectQuery(String recordSelectQuery) {
        this.recordSelectQuery = recordSelectQuery;
    }

    public void setRecordInsertQuery(String recordInsertQuery) {
        this.recordInsertQuery = recordInsertQuery;
    }

    public void setRecordUpdateQuery(String recordUpdateQuery) {
        this.recordUpdateQuery = recordUpdateQuery;
    }

    public void setRecordDeleteQuery(String recordDeleteQuery) {
        this.recordDeleteQuery = recordDeleteQuery;
    }
}
