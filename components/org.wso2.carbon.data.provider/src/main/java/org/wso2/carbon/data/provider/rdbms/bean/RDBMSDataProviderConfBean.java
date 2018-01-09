/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.data.provider.rdbms.bean;

import org.wso2.carbon.config.annotation.Configuration;
import org.wso2.carbon.config.annotation.Element;
import org.wso2.carbon.database.query.manager.config.Queries;

import java.util.ArrayList;

/**
 * RDBMS data provider configuration bean.
 */
@Configuration(namespace = "wso2.rdbms.data.provider", description = "WSO2 RDBMS Data Provider Configuration object.")
public class RDBMSDataProviderConfBean {
    @Element(description = "Regex for sanitizing user provided sql SELECT in query.")
    String sqlSelectQuerySanitizingRegex = null;

    @Element(description = "Regex for sanitizing user provided sql WHERE in query.")
    String sqlWhereQuerySanitizingRegex = null;

    @Element(description = "Regex for sanitizing user provided table name in query.")
    String sqlTableNameSanitizingRegex = null;

    @Element(description = "Database queries template array list.")
    ArrayList<Queries> queries = new ArrayList<>();

    @Element(description = "Array of linear column types in the database.")
    String[] linearTypes = new String[]{"INTEGER", "INT", "SMALLINT", "TINYINT", "MEDIUMINT", "BIGINT",
            "DECIMAL", "NUMERIC", "FLOAT", "DOUBLE", "INT4", "SIGNED", "INT2", "YEAR", "BIGINT", "INT8",
            "IDENTITY", "NUMBER", "DEC", "PRECISION", "FLOAT8", "REAL", "FLOAT4", "NUMBER", "BINARY_FLOAT",
            "BINARY_DOUBLE"};

    @Element(description = "Array of ordinal column types in the database.")
    String[] ordinalTypes = new String[]{"CHAR", "VARCHAR", "BINARY", "VARBINARY", "BLOB", "TEXT", "ENUM", "SET",
            "LONGVARCHAR", "VARCHAR2", "NVARCHAR", "NVARCHAR2",
            "VARCHAR_CASESENSITIVE", "VARCHAR_IGNORECASE", "NCHAR", "CLOB", "TINYTEXT", "MEDIUMTEXT", "LONGTEXT",
            "NTEXT", "NCLOB"};

    @Element(description = "Array of time column types in the database.")
    String[] timeTypes = new String[]{"DATE", "TIME", "DATETIME", "TIMESTAMP"};

    public String getSqlSelectQuerySanitizingRegex() {
        return sqlSelectQuerySanitizingRegex;
    }

    public String getSqlTableNameSanitizingRegex() {
        return sqlTableNameSanitizingRegex;
    }

    public String getSqlWhereQuerySanitizingRegex() {
        return sqlWhereQuerySanitizingRegex;
    }

    public ArrayList<Queries> getQueries() {
        return queries;
    }

    public String[] getLinearTypes() {
        return linearTypes;
    }

    public String[] getOrdinalTypes() {
        return ordinalTypes;
    }

    public String[] getTimeTypes() {
        return timeTypes;
    }

    public void setSqlSelectQuerySanitizingRegex(String sqlSelectQuerySanitizingRegex) {
        this.sqlSelectQuerySanitizingRegex = sqlSelectQuerySanitizingRegex;
    }

    public void setSqlWhereQuerySanitizingRegex(String sqlWhereQuerySanitizingRegex) {
        this.sqlWhereQuerySanitizingRegex = sqlWhereQuerySanitizingRegex;
    }

    public void setSqlTableNameSanitizingRegex(String sqlTableNameSanitizingRegex) {
        this.sqlTableNameSanitizingRegex = sqlTableNameSanitizingRegex;
    }

    public void setQueries(ArrayList<Queries> queries) {
        this.queries = queries;
    }

    public void setLinearTypes(String[] linearTypes) {
        this.linearTypes = linearTypes;
    }

    public void setOrdinalTypes(String[] ordinalTypes) {
        this.ordinalTypes = ordinalTypes;
    }

    public void setTimeTypes(String[] timeTypes) {
        this.timeTypes = timeTypes;
    }
}
