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
package org.wso2.carbon.analytics.datasource.core;

import java.util.List;

/**
 * Analytics data source implementation without data locality semantics. 
 */
public abstract class DirectAnalyticsDataSource implements AnalyticsDataSource {
    
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException {
        List<Record> records = this.getRecords(tenantId, tableName, columns, timeFrom, timeTo, recordsFrom, recordsCount);
        return new DirectRecordGroup[] { new DirectRecordGroup(records) };
    }
    
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, List<String> ids) throws AnalyticsException {
        List<Record> records = this.getRecords(tenantId, tableName, columns, ids);
        if (records.size() > 0) {
            return new DirectRecordGroup[] { new DirectRecordGroup(records) };
        } else {
            return new DirectRecordGroup[0];
        }
    }
    
    public abstract List<Record> getRecords(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo, 
            int recordsFrom, int recordsCount) throws AnalyticsException;
    
    public abstract List<Record> getRecords(int tenantId, String tableName, List<String> columns, List<String> ids) throws AnalyticsException;
    
    /**
     * {@link RecordGroup} implementation for direct analytics data source.
     */
    public class DirectRecordGroup implements RecordGroup {

        private static final String LOCALHOST = "127.0.0.1";
        
        private List<Record> records;
        
        public DirectRecordGroup(List<Record> records) {
            this.records = records;
        }
        
        @Override
        public String[] getLocations() throws AnalyticsException {
            return new String[] { LOCALHOST };
        }

        @Override
        public List<Record> getRecords() throws AnalyticsException {
            return records;
        }
        
    }

}
