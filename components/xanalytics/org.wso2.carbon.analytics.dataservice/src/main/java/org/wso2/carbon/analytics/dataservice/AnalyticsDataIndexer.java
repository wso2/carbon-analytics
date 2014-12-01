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
package org.wso2.carbon.analytics.dataservice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;

/**
 * This class represents the indexing functionality.
 */
public class AnalyticsDataIndexer {

    private AnalyticsIndexDefinitionRepository repository;
    
    private Map<String, Set<String>> indexDefs = new HashMap<String, Set<String>>();
    
    public AnalyticsDataIndexer(FileSystem fileSystem) {
        this.repository = new AnalyticsIndexDefinitionRepository(fileSystem);
    }
    
    public AnalyticsIndexDefinitionRepository getRepository() {
        return repository;
    }
    
    public void process(List<Record> records) throws AnalyticsException {
        Set<String> indices;
        for (Record record : records) {
            indices = this.getIndices(record.getTenantId(), record.getTableName());
            if (indices.size() > 0) {
                this.addToIndex(record, indices);
            }
        }
    }
    
    private void addToIndex(Record record, Set<String> indices) {
        
    }
    
    public void setIndices(int tenantId, String tableName, Set<String> columns) throws AnalyticsIndexException {
        String tableId = this.generateTableId(tenantId, tableName);
        this.indexDefs.put(tableId, columns);
        this.getRepository().setIndices(tenantId, tableName, columns);
        this.notifyClusterIndexChange(tenantId, tableName);
    }
    
    public Set<String> getIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        String tableId = this.generateTableId(tenantId, tableName);
        Set<String> cols = this.indexDefs.get(tableId);
        if (cols == null) {
            cols = this.getRepository().getIndices(tenantId, tableName);
            this.indexDefs.put(tableId, cols);
        }
        return cols; 
    }
    
    public void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        String tableId = this.generateTableId(tenantId, tableName);
        this.indexDefs.remove(tableId);
        this.getRepository().clearAllIndices(tenantId, tableName);
        this.notifyClusterIndexChange(tenantId, tableName);
    }
    
    private String generateTableId(int tenantId, String tableName) {
        return tenantId + "_" + tableName;
    }
    
    public void clusterNoficationReceived(int tenantId, String tableName) throws AnalyticsIndexException {
        /* remove the entry from the cache, this will force the next index operations to load
         * the index definition from the back-end store, this makes sure, we have optimum cache cleanup
         * and improves memory usage for tenant partitioning */
        this.indexDefs.remove(this.generateTableId(tenantId, tableName));
    }
    
    private void notifyClusterIndexChange(int tenantId, String tableName) throws AnalyticsIndexException {
        
    }
    
}
