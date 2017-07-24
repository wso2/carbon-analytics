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
public class SearchResultEntry implements Comparable<SearchResultEntry>, Serializable {
    
    private static final long serialVersionUID = 843580266763606577L;

    private String id;
    
    private float score;
    
    public SearchResultEntry() { }
    
    public SearchResultEntry(String id, float score) {
        this.id = id;
        this.score = score;
    }
    
    public String getId() {
        return id;
    }
    
    public float getScore() {
        return score;
    }

    @Override
    public int compareTo(SearchResultEntry obj) {
        return Float.compare(this.score, obj.getScore());
    }
    
    @Override
    public boolean equals(Object rhs) {
        if (rhs instanceof SearchResultEntry) {
            SearchResultEntry obj = (SearchResultEntry) rhs;
            return obj.getId().equals(this.getId()) && obj.getScore() == this.getScore();
        } else {
            return false;
        }
    }
    
    @Override
    public int hashCode() {
        return this.getId().hashCode() + Float.floatToIntBits(this.getScore()) << 10;
    }
    
}
