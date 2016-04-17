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

import org.wso2.carbon.analytics.datasource.commons.RecordGroup;

import java.io.Serializable;
import java.util.List;

/**
 * This class represents an analytics data service response.
 */
public class AnalyticsDataResponse implements Serializable {

    private static final long serialVersionUID = 7912542783175151940L;

    private List<Entry> entries;

    public AnalyticsDataResponse() {
    }

    public AnalyticsDataResponse(List<Entry> entries) {
        this.entries = entries;
    }

    public List<Entry> getEntries() {
        return entries;
    }
    
    /**
     * This class represents a single record store name / record group entry.
     */
    public static class Entry implements Serializable {
        
        private static final long serialVersionUID = 3278533423223275273L;

        private String recordStoreName;
        
        private RecordGroup recordGroup;
        
        public Entry() { }
        
        public Entry(String recordStoreName, RecordGroup recordGroup) {
            this.recordStoreName = recordStoreName;
            this.recordGroup = recordGroup;
        }
        
        public String getRecordStoreName() {
            return recordStoreName;
        }
        
        public RecordGroup getRecordGroup() {
            return recordGroup;
        }
        
    }
    
}
