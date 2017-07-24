
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *Contains the resulting facet count and the matched ids of a category.
 */
public class DrillDownResultEntry implements Serializable {

    private static final long serialVersionUID = -6227491464673947835L;
    
    private String category;
    private String[] categoryPath;
    private List<String> recordIds;
    private Double recordCount; //TODO : score
    //variables to, from, hierarchicalFacets will only be used if the drilldown is numeric range based.
    private double to;
    private double from;
    private Map<String, List<DrillDownResultEntry>> hierarchicalFacets;

    public DrillDownResultEntry() {
        this.recordIds = new ArrayList<>();
    }

    public DrillDownResultEntry(
            List<String> recordIds) {
        this.recordIds = recordIds;
    }

    public void addNewRecordId(String id) {
        if (recordIds == null) {
            recordIds = new ArrayList<>();
        }
        recordIds.add(id);
    }

    public void addNewRecordIds(List<String> facetedIds) {
        if (recordIds == null) {
            recordIds = new ArrayList<>();
        }
        recordIds.addAll(facetedIds);
    }

    public void setRecordIds(List<String> recordIds) {
        this.recordIds = recordIds;
    }

    public List<String> getRecordIds() {
        return recordIds;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String[] getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(String[] categoryPath) {
        this.categoryPath = categoryPath;
    }

    /**
     * adds a child facet to this facet field. Use this one only if this object use as a range
     * facet result.Otherwise this method makes no sense.
     * @param fieldName indexed Field
     * @param facet child facet result. this should be a categorical facets, not a range facet result
     */
    public void addHierarchicalFacets(String fieldName, DrillDownResultEntry facet) {
        if (this.hierarchicalFacets == null) {
            this.hierarchicalFacets = new LinkedHashMap<>();
        }
        List<DrillDownResultEntry> facets = this.hierarchicalFacets.get(fieldName);
        if (facets == null) {
            facets = new ArrayList<>();
            this.hierarchicalFacets.put(fieldName, facets);
        }
        facets.add(facet);
    }

    public Map<String, List<DrillDownResultEntry>> getHierarchicalFacets() {
        return hierarchicalFacets;
    }

    public void setHierarchicalFacets(Map<String, List<DrillDownResultEntry>> hierarchicalFacets) {
        this.hierarchicalFacets = hierarchicalFacets;
    }

    public Double getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Double recordCount) {
        this.recordCount = recordCount;
    }

    public void incrementRecordCount(Double recordCount) {
        this.recordCount += recordCount;
    }

    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }

    public double getFrom() {
        return from;
    }

    public void setFrom(double from) {
        this.from = from;
    }
}
