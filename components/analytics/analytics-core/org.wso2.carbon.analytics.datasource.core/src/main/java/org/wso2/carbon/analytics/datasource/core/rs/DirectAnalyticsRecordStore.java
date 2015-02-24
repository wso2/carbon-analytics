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
package org.wso2.carbon.analytics.datasource.core.rs;

import java.util.Iterator;
import java.util.List;

import org.wso2.carbon.analytics.datasource.core.AnalyticsException;

/**
 * Analytics record source implementation without data locality semantics. 
 */
public abstract class DirectAnalyticsRecordStore implements AnalyticsRecordStore {
    
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException {
        return new DirectRecordGroup[] { new DirectRecordGroup(tenantId, tableName, columns, timeFrom, timeTo, recordsFrom, recordsCount) };
    }
    
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, List<String> ids) throws AnalyticsException {
        return new DirectRecordGroup[] { new DirectRecordGroup(tenantId, tableName, columns, ids) };        
    }
    
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        if (!(recordGroup instanceof DirectRecordGroup)) {
            throw new AnalyticsException("Invalid RecordGroup implementation in DirectAnalyticsRecordStore: " + 
                    recordGroup.getClass());
        }
        DirectRecordGroup drg = (DirectRecordGroup) recordGroup;
        if (drg.isWithIds()) {
            return this.getRecords(drg.getTenantId(), drg.getTableName(), drg.getColumns(), drg.getIds());
        } else {
            return this.getRecords(drg.getTenantId(), drg.getTableName(), drg.getColumns(), drg.getTimeFrom(), 
                    drg.getTimeTo(), drg.getRecordsFrom(), drg.getRecordsCount());
        }
    }
    
    public abstract Iterator<Record> getRecords(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo, 
            int recordsFrom, int recordsCount) throws AnalyticsException;
    
    public abstract Iterator<Record> getRecords(int tenantId, String tableName, List<String> columns, List<String> ids) throws AnalyticsException;

}
