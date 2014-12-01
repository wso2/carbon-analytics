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
import java.util.Set;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSource;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;

/**
 * The implementation of {@link AnalyticsDataService}.
 */
public class AnalyticsDataServiceImpl implements AnalyticsDataService {

    private AnalyticsDataSource analyticsDataSource;
    
    private AnalyticsDataIndexer indexer;
    
    public AnalyticsDataServiceImpl(AnalyticsDataSource analyticsDataSource) throws AnalyticsException {
        this.analyticsDataSource = analyticsDataSource;
        try {
            this.indexer = new AnalyticsDataIndexer(this.analyticsDataSource.getFileSystem());
        } catch (AnalyticsException e) {
            throw new AnalyticsException("Error in creating AnalyticsDataIndexer: " + 
                    e.getMessage(), e);
        }
    }
    
    public AnalyticsDataServiceImpl(AnalyticsDataServiceConfiguration config) {
    }
    
    public AnalyticsDataIndexer getIndexer() {
        return indexer;
    }
    
    public AnalyticsDataSource getAnalyticsDataSource() {
        return analyticsDataSource;
    }
    
    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        this.getAnalyticsDataSource().createTable(tenantId, tableName);
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        return this.getAnalyticsDataSource().tableExists(tenantId, tableName);
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        this.getAnalyticsDataSource().deleteTable(tenantId, tableName);
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        return this.getAnalyticsDataSource().listTables(tenantId);
    }

    @Override
    public long getRecordCount(int tenantId, String tableName) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        return this.getAnalyticsDataSource().getRecordCount(tenantId, tableName);
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        this.getAnalyticsDataSource().put(records);
        this.getIndexer().process(records);
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo,
            int recordsFrom, int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsDataSource().get(tenantId, tableName, columns, timeFrom, 
                timeTo, recordsFrom, recordsCount);
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsDataSource().get(tenantId, tableName, columns, ids);
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        this.getAnalyticsDataSource().delete(tenantId, tableName, timeFrom, timeTo);
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        this.getAnalyticsDataSource().delete(tenantId, tableName, ids);
    }

    @Override
    public void setIndices(int tenantId, String tableName, Set<String> columns) throws AnalyticsIndexException {
        this.getIndexer().setIndices(tenantId, tableName, columns);
    }

    @Override
    public List<String> search(int tenantId, String tableName, String language, String query)
            throws AnalyticsIndexException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        return this.getIndexer().getIndices(tenantId, tableName);
    }

    @Override
    public void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException {
        this.getIndexer().clearIndices(tenantId, tableName);
    }

}
