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

import org.apache.lucene.analysis.Analyzer;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.DrillDownResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataServiceConfigProperty;
import org.wso2.carbon.analytics.dataservice.config.AnalyticsDataServiceConfiguration;
import org.wso2.carbon.analytics.dataservice.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTimeoutException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * The implementation of {@link AnalyticsDataService}.
 */
public class AnalyticsDataServiceImpl implements AnalyticsDataService {

    private static final String ANALYTICS_DATASERVICE_GROUP = "__ANALYTICS_DATASERVICE_GROUP__";
    
    private AnalyticsRecordStore analyticsRecordStore;
        
    private AnalyticsDataIndexer indexer;
    
    private Map<String, AnalyticsSchema> schemaMap = new HashMap<String, AnalyticsSchema>();
    
    public AnalyticsDataServiceImpl(AnalyticsRecordStore analyticsRecordStore,
            AnalyticsFileSystem analyticsFileSystem, int shardCount) throws AnalyticsException {
        this.analyticsRecordStore = analyticsRecordStore;
        this.indexer = new AnalyticsDataIndexer(analyticsRecordStore, analyticsFileSystem, shardCount);
        AnalyticsServiceHolder.setAnalyticsDataService(this);
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            acm.joinGroup(ANALYTICS_DATASERVICE_GROUP, null);
        } 
        this.indexer.init();
    }
    
    public AnalyticsDataServiceImpl(AnalyticsDataServiceConfiguration config) throws AnalyticsException {
        AnalyticsRecordStore ars;
        AnalyticsFileSystem afs;
        Analyzer luceneAnalyzer;
        try {
            String arsClass = config.getAnalyticsRecordStoreConfiguration().getImplementation();
            String afsClass = config.getAnalyticsFileSystemConfiguration().getImplementation();
            String analyzerClass = config.getLuceneAnalyzerConfiguration().getImplementation();
            ars = (AnalyticsRecordStore) Class.forName(arsClass).newInstance();
            afs = (AnalyticsFileSystem) Class.forName(afsClass).newInstance();
            ars.init(this.convertToMap(config.getAnalyticsRecordStoreConfiguration().getProperties()));
            afs.init(this.convertToMap(config.getAnalyticsFileSystemConfiguration().getProperties()));
            luceneAnalyzer = (Analyzer) Class.forName(analyzerClass).newInstance();
        } catch (Exception e) {
            throw new AnalyticsException("Error in creating analytics data service from configuration: " + 
                    e.getMessage(), e);
        }
        this.analyticsRecordStore = ars;
        this.indexer = new AnalyticsDataIndexer(ars, afs, config.getShardCount(), luceneAnalyzer);
        AnalyticsServiceHolder.setAnalyticsDataService(this);
        this.indexer.init();
    }
    
    private Map<String, String> convertToMap(AnalyticsDataServiceConfigProperty[] props) {
        Map<String, String> result = new HashMap<String, String>();
        for (AnalyticsDataServiceConfigProperty prop : props) {
            result.put(prop.getName(), prop.getValue());
        }
        return result;
    }
    
    public AnalyticsDataIndexer getIndexer() {
        return indexer;
    }
    
    public AnalyticsRecordStore getAnalyticsRecordStore() {
        return analyticsRecordStore;
    }
    
    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        this.getAnalyticsRecordStore().createTable(tenantId, tableName);
    }
    
    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema)
            throws AnalyticsTableNotAvailableException, AnalyticsException {
        this.getAnalyticsRecordStore().setTableSchema(tenantId, tableName, schema);
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            /* send cluster message to invalidate */
            acm.executeAll(ANALYTICS_DATASERVICE_GROUP, new AnalyticsSchemaChangeMessage(tenantId, tableName));
        } else {
            this.invalidateAnalyticsSchema(tenantId, tableName);
        }
    }
    
    private void invalidateAnalyticsSchema(int tenantId, String tableName) {
        this.schemaMap.remove(GenericUtils.calculateTableIdentity(tenantId, tableName));
    }
    
    @Override
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsTableNotAvailableException,
            AnalyticsException {
        return this.getAnalyticsRecordStore().getTableSchema(tenantId, tableName);
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        return this.getAnalyticsRecordStore().tableExists(tenantId, tableName);
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        this.getAnalyticsRecordStore().deleteTable(tenantId, tableName);
        this.clearIndices(tenantId, tableName);
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        return this.getAnalyticsRecordStore().listTables(tenantId);
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) 
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsRecordStore().getRecordCount(tenantId, tableName, timeFrom, timeTo);
    }
    
    /**
     * This method preprocesses the records before adding to the record store,
     * e.g. update the record ids if its not already set by using the table
     * schema's primary keys.
     * @param records
     */
    private void preprocessRecords(List<Record> records) throws AnalyticsException {
        Collection<List<Record>> recordBatches = GenericUtils.generateRecordBatches(records);
        for (List<Record> recordBatch : recordBatches) {
            this.preprocessRecordBatch(recordBatch);
        }
    }
    
    private AnalyticsSchema lookupSchema(int tenantId, String tableName) throws AnalyticsException {
        AnalyticsSchema schema = this.schemaMap.get(GenericUtils.calculateTableIdentity(tenantId, tableName));
        if (schema == null) {
            schema = this.getAnalyticsRecordStore().getTableSchema(tenantId, tableName);
        }
        return schema;
    }
    
    private void populateWithGenerateIds(List<Record> records) {
        for (Record record : records) {
            if (record.getId() == null) {
                record.setId(GenericUtils.generateRecordID());
            }
        }
    }
    
    private void populateRecordWithPrimaryKeyAwareId(Record record, List<String> primaryKeys) {
        StringBuilder builder = new StringBuilder();
        Object obj;
        for (String key : primaryKeys) {
            obj = record.getValue(key);
            if (obj != null) {
                builder.append(obj.toString());
            }
        }
        /* to make sure, we don't have an empty string */
        builder.append(".");
        try {
            byte[] data = builder.toString().getBytes(Constants.DEFAULT_CHARSET);
            record.setId(UUID.nameUUIDFromBytes(data).toString());
        } catch (UnsupportedEncodingException e) {
            /* this wouldn't happen */
            throw new RuntimeException(e);
        }
    }
    
    private void populateRecordsWithPrimaryKeyAwareIds(List<Record> records, List<String> primaryKeys) {
        for (Record record : records) {
            /* users have the ability to explicitly provide a record id,
             * in-spite of having primary keys defined to auto generate the id */
            if (record.getId() == null) {
                this.populateRecordWithPrimaryKeyAwareId(record, primaryKeys);
            }
        }
    }
    
    private void preprocessRecordBatch(List<Record> recordBatch) throws AnalyticsException {
        Record firstRecord = recordBatch.get(0);
        AnalyticsSchema schema = this.lookupSchema(firstRecord.getTenantId(), firstRecord.getTableName());
        List<String> primaryKeys = schema.getPrimaryKeys();
        if (primaryKeys != null && primaryKeys.size() > 0) {
            this.populateRecordsWithPrimaryKeyAwareIds(recordBatch, primaryKeys);
        } else {
            this.populateWithGenerateIds(recordBatch);
        }
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        this.preprocessRecords(records);
        this.getAnalyticsRecordStore().put(records);
        this.getIndexer().put(records);
    }
    
    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
            long timeFrom, long timeTo, int recordsFrom, 
            int recordsCount) throws AnalyticsException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsRecordStore().get(tenantId, tableName, numPartitionsHint, columns, timeFrom, 
                timeTo, recordsFrom, recordsCount);
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint,
            List<String> columns, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return this.getAnalyticsRecordStore().get(tenantId, tableName, numPartitionsHint, columns, ids);
    }
    
    @Override
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        return this.getAnalyticsRecordStore().readRecords(recordGroup);
    }
    
    @Override
    public boolean isPaginationSupported() {
        return this.getAnalyticsRecordStore().isPaginationSupported();
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        this.getIndexer().delete(tenantId, tableName, 
                this.getRecordIdsFromTimeRange(tenantId, tableName, timeFrom, timeTo));
        this.getAnalyticsRecordStore().delete(tenantId, tableName, timeFrom, timeTo);
    }
    
    private List<String> getRecordIdsFromTimeRange(int tenantId, String tableName, long timeFrom, 
            long timeTo) throws AnalyticsException {
        List<Record> records = GenericUtils.listRecords(this,
                                                        this.get(tenantId, tableName, 1, null, timeFrom, timeTo, 0, -1));
        List<String> result = new ArrayList<>(records.size());
        for (Record record : records) {
            result.add(record.getId());
        }
        return result;
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        this.getIndexer().delete(tenantId, tableName, ids);
        this.getAnalyticsRecordStore().delete(tenantId, tableName, ids);
    }

    @Override
    public void setIndices(int tenantId, String tableName, 
            Map<String, IndexType> columns) throws AnalyticsIndexException {
        this.getIndexer().setIndices(tenantId, tableName, columns);
    }

    @Override
    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns,
                           List<String> scoreParams) throws AnalyticsIndexException {
        this.getIndexer().setIndices(tenantId, tableName, columns, scoreParams);
    }

    @Override
    public List<SearchResultEntry> search(int tenantId, String tableName, String language, String query,
            int start, int count) throws AnalyticsIndexException, AnalyticsException {
        return this.getIndexer().search(tenantId, tableName, language, query, start, count);
    }
    
    @Override
    public int searchCount(int tenantId, String tableName, String language, 
            String query) throws AnalyticsIndexException {
        return this.getIndexer().searchCount(tenantId, tableName, language, query);
    }

    @Override
    public Map<String, List<DrillDownResultEntry>> drillDown(int tenantId, AnalyticsDrillDownRequest drillDownRequest)
            throws AnalyticsIndexException {
        return this.getIndexer().drillDown(tenantId, drillDownRequest);
    }

    @Override
    public Map<String, IndexType> getIndices(int tenantId, 
            String tableName) throws AnalyticsIndexException, AnalyticsException {
        return this.getIndexer().lookupIndices(tenantId, tableName);
    }

    @Override
    public List<String> getScoreParams(int tenantId, String tableName)
            throws AnalyticsIndexException, AnalyticsException {
        return this.getIndexer().lookupScoreParams(tenantId, tableName);
    }

    @Override
    public void clearIndices(int tenantId, String tableName) throws AnalyticsIndexException, AnalyticsException {
        this.getIndexer().clearIndices(tenantId, tableName);
    }

    @Override
    public void waitForIndexing(long maxWait) throws AnalyticsException,
            AnalyticsTimeoutException {
        this.getIndexer().waitForIndexing(maxWait);
    }
    
    @Override
    public void destroy() throws AnalyticsException {
        if (this.indexer != null) {
            this.indexer.close();
        }
    }
    
    /**
     * This is executed to invalidate the specific analytics schema information at the current node.
     */
    public static class AnalyticsSchemaChangeMessage implements Callable<String>, Serializable {

        private static final long serialVersionUID = 299364639589319379L;

        private int tenantId;
        
        private String tableName;
        
        public AnalyticsSchemaChangeMessage(int tenantId, String tableName) {
            this.tenantId = tenantId;
            this.tableName = tableName;
        }
        
        @Override
        public String call() throws Exception {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            if (ads == null) {
                throw new AnalyticsException("The analytics data service implementation is not registered");
            }
            /* these cluster messages are specific to AnalyticsDataServiceImpl */
            if (ads instanceof AnalyticsDataServiceImpl) {
                AnalyticsDataServiceImpl adsImpl = (AnalyticsDataServiceImpl) ads;
                adsImpl.invalidateAnalyticsSchema(this.tenantId, this.tableName);
            }
            return "OK";
        }
        
    }
    
}
