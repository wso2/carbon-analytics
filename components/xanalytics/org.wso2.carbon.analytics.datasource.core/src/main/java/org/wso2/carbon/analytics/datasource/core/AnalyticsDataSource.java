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

import java.io.IOException;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.core.fs.FileSystem;

/**
 * This interface represents the common data store implementations used in analytics. The tenant concept
 * is put here as a design vs performance compromise, where if we not to introduce the tenant specifics here,
 * then, we have to re-implement similar functionality in the upper layer to support tenancy, e.g. in 
 * Analytcs Data Service, new set of classes have to be created which are mostly similar to the ones here to
 * bring in the concept of tenants.
 */
public interface AnalyticsDataSource extends AnalyticsRecordStore {
    
    /**
     * This method initializes the AnalyticsDataSource implementation, and is called once before any other method.
     * @param properties The properties associated with this analytics data source 
     * @throws AnalyticsException
     */
    void init(Map<String, String> properties) throws AnalyticsException;
    
    /**
     * Creates and returns a {@link FileSystem} object to do file related operations.
     * @return The {@link FileSystem} object
     * @throws IOException
     */
    FileSystem getFileSystem() throws IOException;

}
