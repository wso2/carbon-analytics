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
package org.wso2.carbon.analytics.dataservice.indexing;

/**
 * This represents a search result entry from {@link AnalyticsDataIndexer}.
 */
public class SearchResultEntry implements Comparable<SearchResultEntry> {
    
    private String id;
    
    private float score;
    
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
        if (this.score > obj.score) {
            return 1;
        } else if (this.score < obj.score) {
            return -1;
        } else {
            return 0;
        }
    }
    
}
