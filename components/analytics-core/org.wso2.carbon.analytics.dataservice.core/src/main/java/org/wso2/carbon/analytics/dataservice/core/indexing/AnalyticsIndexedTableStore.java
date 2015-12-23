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
package org.wso2.carbon.analytics.dataservice.core.indexing;

import java.util.HashSet;
import java.util.Set;

import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * This class represents the analytics tables information which has indices defined for them.
 */
public class AnalyticsIndexedTableStore {

    private Set<IndexedTableId> indexedTableIds = new HashSet<AnalyticsIndexedTableStore.IndexedTableId>();
    
    private IndexedTableId[] indexedTableIdsArray = new IndexedTableId[0];
        
    public synchronized void addIndexedTable(int tenantId, String tableName) {
        this.indexedTableIds.add(new IndexedTableId(tenantId, tableName));
        this.refreshIndexedTableArray();
    }
    
    public synchronized void removeIndexedTable(int tenantId, String tableName) {
        this.indexedTableIds.remove(new IndexedTableId(tenantId, tableName));
        this.refreshIndexedTableArray();
    }
    
    private void refreshIndexedTableArray() {
        this.indexedTableIdsArray = this.indexedTableIds.toArray(new IndexedTableId[0]);
    }
    
    public IndexedTableId[] getAllIndexedTables() {
        return this.indexedTableIdsArray;
    }
    
    /**
     * Represents an identifier for an indexed table.
     */
    public static class IndexedTableId {
        
        private int tenantId;
        
        private String tableName;
        
        public IndexedTableId(int tenantId, String tableName) {
            this.tenantId = tenantId;
            this.tableName = tableName;
        }
        
        public int getTenantId() {
            return tenantId;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        @Override
        public int hashCode() {
            return GenericUtils.calculateTableIdentity(this.tenantId, this.tableName).hashCode();
        }
        
        @Override
        public boolean equals(Object rhs) {
            if (!(rhs instanceof IndexedTableId)) {
                return false;
            }
            return GenericUtils.calculateTableIdentity(this.tenantId, this.tableName).equals(
                    GenericUtils.calculateTableIdentity(((IndexedTableId) rhs).getTenantId(), 
                            ((IndexedTableId) rhs).getTableName()));
                    
        }
        
        @Override
        public String toString() {
            return "[" + this.getTenantId() + "," + this.getTableName() + "]";
        }
        
    }
    
}
