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

import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStore;

/**
 * This interface represents the analytics data service operations.
 */
public interface AnalyticsDataService extends AnalyticsRecordStore {

    /**
     * Adds an index to the given table's column under the given tenant. The indices must be
     * saved in a persistent storage under analytics data service, to be able to lookup the 
     * indices later, i.e. these indexes should be in-effect after a server restart.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param column The column to add the index to
     */
    void addIndex(int tenantId, String tableName, String column);
    
    /**
     * Returns the declared indices for a given table under the given tenant.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @return
     */
    List<String> getIndices(int tenantId, String tableName);
    
    /**
     * Searches the data with a given search query.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param language The language used to give the search query
     * @param query The search query
     * @return A list of record ids of matched records
     * @throws AnalyticsDataServiceException
     */
    List<String> search(int tenantId, String tableName, String language, String query) throws AnalyticsDataServiceException;
    
}
