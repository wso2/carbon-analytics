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
import java.util.Map;

import org.wso2.carbon.analytics.dataservice.indexing.IndexType;
import org.wso2.carbon.analytics.dataservice.indexing.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStore;

/**
 * This interface represents the analytics data service operations.
 */
public interface AnalyticsDataService extends AnalyticsRecordStore {

    /**
     * Sets the indices for a given table under the given tenant. The indices must be
     * saved in a persistent storage under analytics data service, to be able to lookup the 
     * indices later, i.e. these indices should be in-effect after a server restart.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param columns The set of columns to create indices for, and their data types
     * @throws AnalyticsIndexException
     */
    void setIndices(int tenantId, String tableName, Map<String, IndexType> columns) throws AnalyticsIndexException;
    
    /**
     * Returns the declared indices for a given table under the given tenant.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @return List of indices of the table
     * @throws AnalyticsIndexException
     */
    Map<String, IndexType> getIndices(int tenantId, String tableName) throws AnalyticsIndexException;
    
    /**
     * Clears all the indices for the given table.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @throws AnalyticsIndexException
     */
    void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException;
    
    /**
     * Searches the data with a given search query.
     * @param tenantId The tenant id
     * @param tableName The table name
     * @param language The language used to give the search query
     * @param query The search query
     * @param start The start location of the result, 0 based
     * @param count The maximum number of result entries to be returned
     * @return A list of {@link SearchResultEntry}s
     * @throws AnalyticsIndexException
     */
    List<SearchResultEntry> search(int tenantId, String tableName, String language, 
            String query, int start, int count) throws AnalyticsIndexException;
    
    /**
     * Destroys and frees any resources taken up by the analytics data service implementation.
     */
    void destroy() throws AnalyticsException;
    
}
