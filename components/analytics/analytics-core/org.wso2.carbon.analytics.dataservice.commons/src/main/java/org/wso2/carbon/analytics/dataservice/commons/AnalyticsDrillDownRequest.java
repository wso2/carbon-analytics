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

import org.wso2.carbon.analytics.datasource.commons.AnalyticsCategoryPath;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the details about Drill down search. Use this class when you need to query
 * faceted/categorized information
 */
public class AnalyticsDrillDownRequest {

    //table name on which the drill down is performed
    private String tableName;
    //List of facets / List of category path to drill down
    private Map<String, AnalyticsCategoryPath> categoryPaths;
    //List of Range facets to drilldown
    private Map<String, List<AnalyticsDrillDownRange>> ranges;
    //query language - either lucene or regex
    private String language;
    // language query
    private  String languageQuery;
    // represents the score function and the values
    private String scoreFunction;
    //maximun number of categories for each facet field
    private int categoryCount;

    private int categoryStart;
    // maximum records for each category in each facet
    private int recordCount;
    //Records start index
    private  int recordStart;
    //make this true if the ids of the records are needed
    private boolean withIds;

    public  AnalyticsDrillDownRequest() {
        this(false);
    }

    public  AnalyticsDrillDownRequest(boolean withIds) {
        this.withIds = withIds;
    }

    public AnalyticsDrillDownRequest(String tableName,
                                     Map<String, AnalyticsCategoryPath> categoryPaths,
                                     Map<String, List<AnalyticsDrillDownRange>> ranges,
                                     String language, String languageQuery,
                                     String scoreFunction, int categoryCount, int recordCount,
                                     int recordStart, boolean withIds) {
        this.tableName = tableName;
        this.categoryPaths = categoryPaths;
        this.ranges = ranges;
        this.language = language;
        this.languageQuery = languageQuery;
        this.scoreFunction = scoreFunction;
        this.categoryCount = categoryCount;
        this.recordCount = recordCount;
        this.recordStart = recordStart;
        this.withIds = withIds;
    }

    /**
     * returns the table name.
     * @return
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
    public Map<String, AnalyticsCategoryPath> getCategoryPaths() {
        return categoryPaths;
    }

    /**
     * Sets the facets.
     * @param categoryPaths list of facets
     */
    public void setCategoryPaths(Map<String, AnalyticsCategoryPath> categoryPaths) {
        this.categoryPaths = categoryPaths;
    }

    /**
     * Adds a facets to existing list of facets.
     * @param categoryPath the facet object being inserted
     */
    public void addCategoryPath(String field, AnalyticsCategoryPath categoryPath) {
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
        this.scoreFunction = scoreFunction;
    }

    /**
     * Returns the query expression.
     * @return the expression in regex or lucene
     */
    public String getLanguageQuery() {
        return languageQuery;
    }

    /**
     * Sets the query expression in lucene or regex.
     * @param languageQuery
     */
    public void setLanguageQuery(String languageQuery) {
        this.languageQuery = languageQuery;
    }

    /**
     * Returns the query language lucene or regex.
     * @return the language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Sets the query language. It should be lucene or regex.
     * @param language Th language type
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * Get the List of Drill down ranges. This can be null if the
     * drilldown mechanism is not ranged based.
     * @return The map of ranges with the fields
     */
    public Map<String, List<AnalyticsDrillDownRange>> getRangeFacets() {
        return ranges;
    }

    /**
     * Sets the ranges for drilling down ranges.
     * @param ranges The map of ranges, the key of the map represents the field bieng range-queried
     *                and value represents a list which can have several ranges for one field.
     */
    public void setRangeFacets(Map<String, List<AnalyticsDrillDownRange>> ranges) {
        this.ranges = ranges;
    }

    /**
     * Inserts a single range to existing List of ranges.
     * @param range A drill down range being inserted
     */
    public  void addRangeFacet(String field, AnalyticsDrillDownRange range) {
        if (this.ranges == null) {
            this.ranges = new LinkedHashMap<>();
        }
        List<AnalyticsDrillDownRange> ranges = this.getRangeFacets().get(field);

        if (ranges == null) {
            ranges = new ArrayList<>();
            this.ranges.put(field, ranges);
        }
        ranges.add(range);
    }

    /**
     * Get the maximum number of  child categories/facets for each field to be present
     * in the drilldown result.
     * @return The number of maximum child facets
     */
    public int getCategoryCount() {
        if (categoryCount < 0) {
            return 0;
        }
        return categoryCount;
    }

    /**
     * Sets the maximum number of child facets to be returned from the drilldown.
     * @param categoryCount the number of child facets in max.
     */
    public void setCategoryCount(int categoryCount) {
        this.categoryCount = categoryCount;
    }

    /**
     * Returns the number of maximum records per a child facet in each facet field in drilldown result.
     * @return the number of records in a child facet in max.
     */
    public int getRecordCount() {
        if (recordCount < 0) {
            return 0;
        }
        return recordCount;
    }

    /**
     * Sets the maximum number of records that can be there in a child facet in a facet field of result.
     * @param recordCount The maximum number of records in achild facet.
     */
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    /**
     * Check if the drill down request to include the resulting IDs. Otherwise the result will not have
     * Record IDS and will have only the facet details.
     * @return Boolean if
     */
    public boolean isWithIds() {
        return withIds;
    }

    /**
     * Make sure result will contain the record ids of the matching records.
     * @param withIds this should be true if the user wants the record ids in the resulting drilldown object.
     */
    public void setWithIds(boolean withIds) {
        this.withIds = withIds;
    }

    public int getRecordStartIndex() {
        if (recordStart < 0) {
            return 0;
        }
        return recordStart;
    }

    /**
     * Set the starting index of the records under each cateogry
     * @param recordStart 0 based index
     */
    public void setRecordStartIndex(int recordStart) {
        this.recordStart = recordStart;
    }

    public int getCategoryStartIndex() {
        if (categoryStart < 0) {
            return 0;
        }
        return categoryStart;
    }

    /**
     * set the starting index of the categories
     * @param categoryStart 0 based index
     */
    public void setCategoryStartIndex(int categoryStart) {
        this.categoryStart = categoryStart;
    }
}