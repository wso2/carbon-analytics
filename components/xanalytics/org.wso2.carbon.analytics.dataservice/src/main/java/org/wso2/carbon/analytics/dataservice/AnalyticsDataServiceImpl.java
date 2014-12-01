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

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;

/**
 * The implementation of {@link AnalyticsDataService}.
 */
public class AnalyticsDataServiceImpl implements AnalyticsDataService {

    private AnalyticsDataSource analyticsDataSource;
    
    private AnalyticsIndexDefinitionRepository indexRepo;
    
    public AnalyticsDataServiceImpl(AnalyticsDataSource analyticsDataSource) throws AnalyticsDataServiceException {
        this.analyticsDataSource = analyticsDataSource;
        try {
            this.indexRepo = new AnalyticsIndexDefinitionRepository(this.analyticsDataSource.getFileSystem());
        } catch (AnalyticsDataSourceException e) {
            throw new AnalyticsDataServiceException("Error in creating AnalyticsIndexDefinitionRepository: " + 
                    e.getMessage(), e);
        }
    }
    
    public AnalyticsDataServiceImpl(AnalyticsDataServiceConfiguration config) {
    }
    
    public AnalyticsDataSource getAnalyticsDataSource() {
        return analyticsDataSource;
    }
    
    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsDataSourceException {
        this.getAnalyticsDataSource().createTable(tenantId, tableName);
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsDataSourceException {
        return this.getAnalyticsDataSource().tableExists(tenantId, tableName);
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsDataSourceException {
        this.getAnalyticsDataSource().deleteTable(tenantId, tableName);
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsDataSourceException {
        return this.getAnalyticsDataSource().listTables(tenantId);
    }

    @Override
    public long getRecordCount(int tenantId, String tableName) throws AnalyticsDataSourceException,
            AnalyticsTableNotAvailableException {
        return this.getAnalyticsDataSource().getRecordCount(tenantId, tableName);
    }

    @Override
    public void put(List<Record> records) throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException {
        this.getAnalyticsDataSource().put(records);
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo,
            int recordsFrom, int recordsCount) throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsDataSource().get(tenantId, tableName, columns, timeFrom, timeTo, recordsFrom, recordsCount);
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, List<String> ids)
            throws AnalyticsDataSourceException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsDataSource().get(tenantId, tableName, columns, ids);
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsDataSourceException,
            AnalyticsTableNotAvailableException {
        this.getAnalyticsDataSource().delete(tenantId, tableName, timeFrom, timeTo);
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsDataSourceException,
            AnalyticsTableNotAvailableException {
        this.getAnalyticsDataSource().delete(tenantId, tableName, ids);
    }

    @Override
    public void addIndex(int tenantId, String tableName, String column) {
        
    }

    @Override
    public List<String> search(int tenantId, String tableName, String language, String query)
            throws AnalyticsDataServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getIndices(int tenantId, String tableName) {
        // TODO Auto-generated method stub
        return null;
    }


}
