
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *Contains the resulting facet count and the matched ids of a category.
 */
public class DrillDownResultEntry {
    private String category;
    private String[] categoryPath;
    private List<String> facetedIds;
    private Double facetCount;
    private Map<String, List<DrillDownResultEntry>> hierarchicalFacets;

    public DrillDownResultEntry() {
        this.facetedIds = new ArrayList<>();
        this.facetCount = 0.0;
    }

    public DrillDownResultEntry(
            List<String> facetedIds,
            Double facetCount) {
        this.facetedIds = facetedIds;
        this.facetCount = facetCount;
    }

    public void addNewFacetId(String id) {
        if (facetedIds == null) {
            facetedIds = new ArrayList<>();
        }
        facetedIds.add(id);
    }

    public void setFacetedIds( List<String> facetedIds) {
        this.facetedIds = facetedIds;
    }

    public List<String> getFacetIds() {
        return facetedIds;
    }

    public void setFacetCount(Double facetedCount) {
        this.facetCount = facetedCount;
    }

    public  double getFacetCount() {
        if (facetCount == null) {
            facetCount = 0.0;
        }
        return facetCount;
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
}
