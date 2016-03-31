/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class contains the details about Drill down search. Use this class when you need to query
 * faceted/categorized information
 */
public class AnalyticsDrillDownRequestBean implements Serializable {
    private static final long serialVersionUID = -1727639629855431223L;

    private String tableName;
    private CategoryPathBean[] categoryPaths;
    private AnalyticsDrillDownRangeBean[] ranges;
    private String rangeField;
    private String query;
    private String scoreFunction;
    private int recordCount;
    private int recordStart;
    private SortByFieldBean[] sortByFields;
    private String[] columns;

    public AnalyticsDrillDownRequestBean() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public CategoryPathBean[] getCategoryPaths() {
        return categoryPaths;
    }

    public void setCategoryPaths(CategoryPathBean[] categoryPaths) {
        this.categoryPaths = categoryPaths;
    }

    public AnalyticsDrillDownRangeBean[] getRanges() {
        return ranges;
    }

    public void setRanges(AnalyticsDrillDownRangeBean[] ranges) {
        this.ranges = ranges;
    }

    public String getRangeField() {
        return rangeField;
    }

    public void setRangeField(String rangeField) {
        this.rangeField = rangeField;
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

    public int getRecordStart() {
        return recordStart;
    }

    public void setRecordStart(int recordStart) {
        this.recordStart = recordStart;
    }

    public SortByFieldBean[] getSortByFields() {
        return sortByFields;
    }

    public void setSortByFields(SortByFieldBean[] sortByFields) {
        this.sortByFields = sortByFields;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }
}
