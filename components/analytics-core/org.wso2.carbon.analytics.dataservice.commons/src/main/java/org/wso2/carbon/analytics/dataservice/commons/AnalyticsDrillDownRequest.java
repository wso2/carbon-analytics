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

package org.wso2.carbon.analytics.dataservice.commons;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the details about Drill down search. Use this class when you need to query
 * faceted/categorized information
 */
public class AnalyticsDrillDownRequest implements Serializable {

    private static final long serialVersionUID = 5472794378224537697L;
    //table name on which the drill down is performed
    private String tableName;
    //List of facets / List of category path to drill down
    private Map<String, List<String>> categoryPaths;
    //List of Range facets/buckets to drilldown
    private List<AnalyticsDrillDownRange> ranges;
    //Field name which is bucketed.
    private String rangeField;
    // language query
    private  String query;
    // represents the score function and the values
    private String scoreFunction;
    // maximum records for each category in each facet
    private int recordCount;
    //Records start index
    private  int recordStart;
    //Fields by which the sorting is performed
    private List<SortByField> sortByFields;
    
    public AnalyticsDrillDownRequest() {
    }

    public AnalyticsDrillDownRequest(String tableName,
                                     Map<String, List<String>> categoryPaths, String rangeField,
                                     List<AnalyticsDrillDownRange> ranges, String query,
                                     String scoreFunction, int recordCount, List<SortByField> sortByFields,
                                     int recordStart) {
        this.tableName = tableName;
        this.categoryPaths = categoryPaths;
        this.rangeField = rangeField;
        this.ranges = ranges;
        this.query = query;
        this.scoreFunction = scoreFunction;
        this.recordCount = recordCount;
        this.recordStart = recordStart;
        this.sortByFields = sortByFields;
    }

    /**
     * @returns the table name.
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Sets the table Name.
     * @param tableName name of the table
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * returns the list of Facets being queried.
     * @return list of facets
     */
    public Map<String, List<String>> getCategoryPaths() {
        return categoryPaths;
    }

    /**
     * Sets the facets.
     * @param categoryPaths list of facets
     */
    public void setCategoryPaths(Map<String, List<String>> categoryPaths) {
        this.categoryPaths = categoryPaths;
    }

    /**
     * Adds a facets to existing list of facets.
     * @param categoryPath the facet object being inserted
     */
    public void addCategoryPath(String field, List<String> categoryPath) {
        if (categoryPaths == null) {
            categoryPaths = new LinkedHashMap<>();
        }
        categoryPaths.put(field, categoryPath);
    }

    /**
     * Returns the Scoring function.
     * @return The score function
     */
    public String getScoreFunction() {
        return scoreFunction;
    }

    /**
     * Sets the score function.
     * @param score the score function
     */
    public void setScoreFunction(String score) {
        this.scoreFunction = score;
    }

    /**
     * Returns the query expression.
     * @return the expression in regex or lucene
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query expression in lucene or regex.
     * @param query
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Returns the number of maximum records per a child facet in each facet field in drilldown result.
     * @return the number of records in a child facet in max. Default value is 10, if not set.
     */
    public int getRecordCount() {

        return recordCount;
    }

    /**
     * Sets the maximum number of records that can be there in a child facet in a facet field of result.
     * @param recordCount The maximum number of records in achild facet.Default value is 0, if not set
     */
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    public int getRecordStartIndex() {

        return recordStart;
    }

    /**
     * Set the starting index of the records under each cateogry
     * @param recordStart 0 based index
     */
    public void setRecordStartIndex(int recordStart) {
        this.recordStart = recordStart;
    }

    public List<AnalyticsDrillDownRange> getRanges() {
        return ranges;
    }

    public void setRanges(List<AnalyticsDrillDownRange> ranges) {
        this.ranges = ranges;
    }

    public String getRangeField() {
        return rangeField;
    }

    public void setRangeField(String rangeField) {
        this.rangeField = rangeField;
    }

    public List<SortByField> getSortByFields() {
        return sortByFields;
    }

    public void setSortByFields(List<SortByField> sortByFields) {
        this.sortByFields = sortByFields;
    }
}