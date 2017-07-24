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
package org.wso2.carbon.analytics.dataservice.commons;

import java.io.Serializable;

/**
 * This represents a search result entry from Indexer.
 */
public class CategorySearchResultEntry implements Comparable<CategorySearchResultEntry>,
                                                  Serializable {

    private static final long serialVersionUID = -146528425726651682L;
    
    private String categoryValue;

    private double score;

    public CategorySearchResultEntry() { }
    
    public CategorySearchResultEntry(String category, double score) {
        this.categoryValue = category;
        this.score = score;
    }

    public String getCategoryValue() {
        return categoryValue;
    }

    public double getScore() {
        return score;
    }

    @Override
    public int compareTo(CategorySearchResultEntry obj) {
        return Double.compare(this.score, obj.getScore());
    }
    
    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof CategorySearchResultEntry) {
            CategorySearchResultEntry obj = (CategorySearchResultEntry) rhs;
            return obj.getCategoryValue().equals(this.getCategoryValue()) && obj.getScore() == this.getScore();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(this.getScore());
        return this.getCategoryValue().hashCode() + (int)(bits ^ (bits >>> 32)) << 10;
    }
    
}
