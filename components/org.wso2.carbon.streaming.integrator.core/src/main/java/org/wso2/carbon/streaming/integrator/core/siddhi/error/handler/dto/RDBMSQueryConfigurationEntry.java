/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.streaming.integrator.core.siddhi.error.handler.dto;

/**
 * Class that maps individual database entries from resources/error-store-queries.yaml.
 */
public class RDBMSQueryConfigurationEntry {
    private String databaseName;
    private String isTableExistQuery;
    private String createTableQuery;
    private String insertTableQuery;
    private String selectTableQuery;
    private String selectWithLimitOffsetQuery;
    private String selectCountQuery;
    private String deleteQuery;

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getCreateTableQuery() {
        return createTableQuery;
    }

    public void setCreateTableQuery(String createTableQuery) {
        this.createTableQuery = createTableQuery;
    }

    public String getInsertTableQuery() {
        return insertTableQuery;
    }

    public void setInsertTableQuery(String insertTableQuery) {
        this.insertTableQuery = insertTableQuery;
    }

    public String getIsTableExistQuery() {
        return isTableExistQuery;
    }

    public void setIsTableExistQuery(String isTableExistQuery) {
        this.isTableExistQuery = isTableExistQuery;
    }

    public String getSelectTableQuery() {
        return selectTableQuery;
    }

    public void setSelectTableQuery(String selectTableQuery) {
        this.selectTableQuery = selectTableQuery;
    }

    public String getSelectCountQuery() {
        return selectCountQuery;
    }

    public void setSelectCountQuery(String selectCountQuery) {
        this.selectCountQuery = selectCountQuery;
    }

    public String getSelectWithLimitOffsetQuery() {
        return selectWithLimitOffsetQuery;
    }

    public void setSelectWithLimitOffsetQuery(String selectWithLimitOffsetQuery) {
        this.selectWithLimitOffsetQuery = selectWithLimitOffsetQuery;
    }

    public String getDeleteQuery() {
        return deleteQuery;
    }

    public void setDeleteQuery(String deleteQuery) {
        this.deleteQuery = deleteQuery;
    }

}
