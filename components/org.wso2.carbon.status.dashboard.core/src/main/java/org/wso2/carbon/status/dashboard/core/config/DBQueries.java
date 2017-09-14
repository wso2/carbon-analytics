package org.wso2.carbon.status.dashboard.core.config;

import java.util.Map;

/**
 * Model class containing database queries.
 */
public class DBQueries {

    private Map<String,String> propertyMap;
    private String tableCheckQuery;
    private String recordExistsQuery;
    private String recordSelectQuery;
    private String recordInsertQuery;
    private String recordUpdateQuery;
    private String recordDeleteQuery;

    public DBQueries(Map<String,String > propertyMap){
        this.propertyMap=propertyMap;
        this.setProperties(propertyMap);
    }

    private void setProperties(Map<String,String> propertyMap){
        this.tableCheckQuery=propertyMap.get("tableCheckQuery");
        this.recordExistsQuery=propertyMap.get("recordExistsQuery");
        this.recordSelectQuery=propertyMap.get("recordSelectQuery");
        this.recordInsertQuery=propertyMap.get("recordInsertQuery");
        this.recordUpdateQuery=propertyMap.get("recordUpdateQuery");
        this.recordDeleteQuery=propertyMap.get("recordDeleteQuery");
    }

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

}
