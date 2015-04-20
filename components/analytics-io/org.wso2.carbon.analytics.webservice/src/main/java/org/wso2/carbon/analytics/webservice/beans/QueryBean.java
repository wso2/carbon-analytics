/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * The Class QueryBean.
 */
public class QueryBean implements Serializable {

    private static final long serialVersionUID = -3940101281983944720L;
    /**
     * The tenant id.
     */
    private int tenantId;

    /**
     * The table name.
     */
    private String tableName;

    /**
     * The columns.
     */
    private IndexEntryBean[] columns;

    /**
     * The language.
     */
    private String language;

    /**
     * The query.
     */
    private String query;

    /**
     * The start.
     */
    private int start;

    /**
     * The count.
     */
    private int count;

    public QueryBean() {
    }

    /**
     * Gets the tenant id.
     *
     * @return the tenant id
     */
    public int getTenantId() {
        return tenantId;
    }

    /**
     * Sets the tenant id.
     *
     * @param tenantId the new tenant id
     */
    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Gets the table name.
     *
     * @return the table name
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the table name.
     *
     * @param tableName the new table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * Gets the columns.
     *
     * @return the columns
     */
    public IndexEntryBean[] getColumns() {
        return columns;
    }

    /**
     * Sets the columns.
     *
     * @param columns the columns
     */
    public void setColumns(IndexEntryBean[] columns) {
        this.columns = columns;
    }

    /**
     * Gets the language.
     *
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the language.
     *
     * @param language the new language
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query.
     *
     * @param query the new query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Gets the start.
     *
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * Sets the start.
     *
     * @param start the new start
     */
    public void setStart(int start) {
        this.start = start;
    }

    /**
     * Gets the count.
     *
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * Sets the count.
     *
     * @param count the new count
     */
    public void setCount(int count) {
        this.count = count;
    }
}
