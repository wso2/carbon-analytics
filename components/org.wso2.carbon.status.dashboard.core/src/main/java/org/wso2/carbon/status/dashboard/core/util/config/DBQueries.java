package org.wso2.carbon.status.dashboard.core.util.config;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;

@Configuration(description = "Database queries configuration")
public class DBQueries {

    @Element(description = "table create query")
    private String  tableCreateQuery;

    @Element(description = "table check query")
    private String tableCheckQuery;

    @Element(description = "index create query")
    private String indexCreateQuery;

    @Element(description = "record exist query")
    private String recordExistsQuery;

    @Element(description = "record select query")
    private String recordSelectQuery;

    @Element(description = "record insert query")
    private String recordInsertQuery;

    @Element(description = "record update query")
    private String recordUpdateQuery;

    @Element(description = "record delete query")
    private String recordDeleteQuery;

    @Element(description = "string size")
    private String stringSize;

    @Element(description = "binary type")
    private String binaryType;

    @Element(description = "boolean type")
    private String booleanType;

    @Element(description = "double type")
    private String doubleType;

    @Element(description = "float type")
    private String floatType;

    @Element(description = "integer type")
    private String integerType;

    @Element(description = "long type")
    private String longType;

    @Element(description = "string type")
    private String stringType;

    public String getTableCreateQuery() {
        return tableCreateQuery;
    }

    public String getTableCheckQuery() {
        return tableCheckQuery;
    }

    public String getIndexCreateQuery() {
        return indexCreateQuery;
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

    public String getStringSize() {
        return stringSize;
    }

    public String getBinaryType() {
        return binaryType;
    }

    public String getBooleanType() {
        return booleanType;
    }

    public String getDoubleType() {
        return doubleType;
    }
}
