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

/**
 * This bean class represents the drill down information to perform drilldown operations
 * (search and count)
 */
public class DrillDownRequestBean {

    private String tableName;
    private DrillDownPathBean[] categories;
    private String language;
    private String query;
    private String scoreFunction;
    private int recordCount;
    private int categoryCount;
    private  int recordStart;
    private  int categoryStart;
    private boolean includeIds;
    private DrillDownFieldRangeBean[] ranges;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public DrillDownPathBean[] getCategories() {
        return categories;
    }

    public void setCategories(DrillDownPathBean[] categories) {
        this.categories = categories;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getScoreFunction() {
        return scoreFunction;
    }

    public void setScoreFunction(String scoreFunction) {
        this.scoreFunction = scoreFunction;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(int categoryCount) {
        this.categoryCount = categoryCount;
    }

    public int getRecordStart() {
        return recordStart;
    }

    public void setRecordStart(int recordStart) {
        this.recordStart = recordStart;
    }

    public boolean isIncludeIds() {
        return includeIds;
    }

    public void setIncludeIds(boolean includeIds) {
        this.includeIds = includeIds;
    }

    public DrillDownFieldRangeBean[] getRanges() {
        return ranges;
    }

    public void setRanges(DrillDownFieldRangeBean[] ranges) {
        this.ranges = ranges;
    }

    public int getCategoryStart() {
        return categoryStart;
    }

    public void setCategoryStart(int categoryStart) {
        this.categoryStart = categoryStart;
    }
}
