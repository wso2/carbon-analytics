/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.analytics.spark.core.jdbc;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class contains all the Spark JDBC query configuration mappings.
 */
@XmlRootElement(name = "database")
public class SparkJDBCQueryConfigEntry {

    private String databaseName;

    private String version = "";

    private String tableCheckQuery;

    private String tableCreateQuery;

    private String tableTruncateQuery;

    private String indexCreateQuery;

    private String recordInsertQuery;

    private String recordMergeQuery;

    private String quoteMark = "";

    private boolean keyExplicitNotNull = false;

    private SparkJDBCTypeMapping sparkJDBCTypeMapping;

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @XmlAttribute(name = "name", required = true)
    public String getDatabaseName() {
        return databaseName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlAttribute(name = "version")
    public String getVersion() {
        return version;
    }

    public void setTableCheckQuery(String tableCheckQuery) {
        this.tableCheckQuery = tableCheckQuery;
    }

    public String getTableCheckQuery() {
        return tableCheckQuery;
    }

    public String getTableCreateQuery() {
        return tableCreateQuery;
    }

    public void setTableCreateQuery(String tableCreateQuery) {
        this.tableCreateQuery = tableCreateQuery;
    }

    public String getTableTruncateQuery() {
        return tableTruncateQuery;
    }

    public void setTableTruncateQuery(String tableTruncateQuery) {
        this.tableTruncateQuery = tableTruncateQuery;
    }

    public String getIndexCreateQuery() {
        return indexCreateQuery;
    }

    public void setIndexCreateQuery(String indexCreateQuery) {
        this.indexCreateQuery = indexCreateQuery;
    }

    public String getRecordInsertQuery() {
        return recordInsertQuery;
    }

    public void setRecordInsertQuery(String recordInsertQuery) {
        this.recordInsertQuery = recordInsertQuery;
    }

    public String getRecordMergeQuery() {
        return recordMergeQuery;
    }

    public void setRecordMergeQuery(String recordMergeQuery) {
        this.recordMergeQuery = recordMergeQuery;
    }

    public String getQuoteMark() {
        return quoteMark;
    }

    public void setQuoteMark(String quoteMark) {
        this.quoteMark = quoteMark;
    }

    public boolean isKeyExplicitNotNull() {
        return keyExplicitNotNull;
    }

    public void setKeyExplicitNotNull(boolean keyExplicitNotNull) {
        this.keyExplicitNotNull = keyExplicitNotNull;
    }

    @XmlElement(name = "typeMapping")
    public SparkJDBCTypeMapping getSparkJDBCTypeMapping() {
        return sparkJDBCTypeMapping;
    }

    public void setSparkJDBCTypeMapping(SparkJDBCTypeMapping sparkJDBCTypeMapping) {
        this.sparkJDBCTypeMapping = sparkJDBCTypeMapping;
    }

}
