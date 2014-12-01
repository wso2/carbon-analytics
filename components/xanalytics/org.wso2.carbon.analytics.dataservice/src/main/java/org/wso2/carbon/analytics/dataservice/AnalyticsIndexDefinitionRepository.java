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

import java.util.List;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;

/**
 * This class represents a repository for storing index definitions.
 */
public class AnalyticsIndexDefinitionRepository {

    private FileSystem fileSystem;
    
    public AnalyticsIndexDefinitionRepository(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }
    
    public void loadTenant(int tenantId) throws AnalyticsDataServiceException {
    }
    
    public List<String> getIndices(int tenantId, String tableName) throws AnalyticsDataServiceException {
        try {
            return this.fileSystem.list("/");
        } catch (AnalyticsDataSourceException e) {
            throw new AnalyticsDataServiceException("Error in retrieving index definitions: " + e.getMessage(), e);
        }
    }
    
    public void addIndices(int tenantId, String tableName, List<String> columns) throws AnalyticsDataServiceException {
        
    }
    
    public List<String> deleteIndices(int tenantId, String tableName, List<String> columns) throws AnalyticsDataServiceException {
        return null;
    }
    
    public void clearAllIndices(int tenantId, String tableName) throws AnalyticsDataServiceException {
        
    }
    
    public boolean containIndicesForTable(int tenantId, String tableName) throws AnalyticsDataServiceException {
        return false;
    }
    
}
