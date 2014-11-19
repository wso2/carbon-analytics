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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This class represents a data record in {@link AnalyticsDataSource}. A record's equality is measured
 * by the non-null column name/values that it has, the null column values are ignored.
 */
public class Record {

    private String tableName;
    
    private List<Column> values;
    
    private long timestamp;
        
    private String id;
    
    private int hashCode = -1;
    
    public Record(String tableName, List<Column> values, long timestamp) {
        this(null, tableName, values, timestamp);
    }
    
    public Record(String id, String tableName, List<Column> values, long timestamp) {
        this.id = id;
        if (this.id == null) {
            this.id = this.generateID();
        }
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
    
    public String getTableName() {
        return tableName;
    }
    
    public List<Column> getValues() {
        return values;
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
        if (!this.getTableName().equals(rhs.getTableName())) {
            return false;
        }
        if (!this.getId().equals(rhs.getId())) {
            return false;
        }
        if (this.getTimestamp() != rhs.getTimestamp()) {
            return false;
        }
        return new HashSet<Column>(this.getNotNullValues()).equals(new HashSet<Column>(rhs.getNotNullValues()));
    }
    
    @Override
    public int hashCode() {
        if (this.hashCode == -1) {
            this.hashCode = this.getTableName().toUpperCase().hashCode();
            this.hashCode += this.getId().hashCode() >> 2;
            this.hashCode += String.valueOf(this.getTimestamp()).hashCode() >> 4;
            this.hashCode += new HashSet<Column>(this.getNotNullValues()).hashCode() >> 8;
        }
        return this.hashCode;
    }

    public List<Column> getNotNullValues() {
        List<Column> result = new ArrayList<Record.Column>();
        for (Column col : this.getValues()) {
            if (col.getValue() != null) {
                result.add(col);
            }
        }
        return result;
    }
    
    /**
     * Represents a record column.
     */
    public static class Column {
        
        private String name;
                
        private Object value;
        
        public Column(String name, Object value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public Object getValue() {
            return value;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Column)) {
                return false;
            }
            Column rhs = (Column) obj;
            /* The column name match is case insensitive */
            if (this.getName().equals(rhs.getName())) {
                Object val1 = this.getValue();
                Object val2 = rhs.getValue();
                if (val1 == null && val2 == null) {
                    return true;
                }
                if (val1 == null || val2 == null) {
                    return false;
                }
                return val1.equals(val2);
            } else {
                return false;
            }
        }
        
        @Override
        public int hashCode() {
            return this.getName().hashCode() + 
                    (this.getValue() != null ? this.getValue().hashCode() * 2 : 0);
        }
                
    }
    
}
