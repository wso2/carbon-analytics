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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class represents a data record in {@link AnalyticsDataSource}. A record's equality is measured
 * by the non-null column name/values that it has, the null column values are ignored.
 */
public class Record {

    private int tenantId;
    
    private String tableName;
    
    private Map<String, Object> values;
    
    private long timestamp;
        
    private String id;
    
    private int hashCode = -1;
    
    public Record(int tenantId, String tableName, Map<String, Object> values, long timestamp) {
        this(null, tenantId, tableName, values, timestamp);
    }
    
    public Record(String id, int tenantId, String tableName, Map<String, Object> values, long timestamp) {
        this.id = id;
        if (this.id == null) {
            this.id = this.generateID();
        }
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.values = values;
        this.timestamp = timestamp;
    }
    
    public String getId() {
        return id;
    }
    
    private String generateID() {
        StringBuilder builder = new StringBuilder();
        builder.append(System.currentTimeMillis());
        builder.append(Math.random());
        return builder.toString();
    }
    
    public int getTenantId() {
        return tenantId;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Map<String, Object> getValues() {
        return values;
    }
    
    public Object getValue(String name) {
        return this.values.get(name);
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Equality is checked not with the field order in mind, if all the columns' key/value pairs match,
     * the records are the same. The persistence media of a record doesn't have to maintain the order of the
     * fields in a record, when writing back, it can use Record's identity to order similar records with the
     * field ordering considered.
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Record)) {
            return false;
        }
        Record rhs = (Record) obj;
        if (this.getTenantId() != rhs.getTenantId()) {
            return false;
        }
        if (!this.getTableName().equals(rhs.getTableName())) {
            return false;
        }
        if (!this.getId().equals(rhs.getId())) {
            return false;
        }
        if (this.getTimestamp() != rhs.getTimestamp()) {
            return false;
        }
        return this.getNotNullValues().equals(rhs.getNotNullValues());
    }
    
    @Override
    public int hashCode() {
        if (this.hashCode == -1) {
            this.hashCode = ((Integer) this.getTenantId()).hashCode();
            this.hashCode += this.getTableName().hashCode() >> 2;
            this.hashCode += this.getId().hashCode() >> 4;
            this.hashCode += String.valueOf(this.getTimestamp()).hashCode() >> 8;
            this.hashCode += this.getNotNullValues().hashCode() >> 16;
        }
        return this.hashCode;
    }

    public Map<String, Object> getNotNullValues() {
        Map<String, Object> result = new HashMap<String, Object>(this.getValues());
        Iterator<Map.Entry<String, Object>> itr = result.entrySet().iterator();
        while (itr.hasNext()) {
            if (itr.next().getValue() == null) {
                itr.remove();
            }
        }
        return result;
    }
    
}
