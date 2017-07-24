/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.lucene.analysis.Analyzer;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;

/**
 * Properties related to {@link AnalyticsDataIndexer}.
 */
public class AnalyticsIndexerInfo {

    private Analyzer luceneAnalyzer;
        
    private AnalyticsRecordStore analyticsRecordStore;

    private AnalyticsDataService analyticsDataService;
    
    private AnalyticsIndexedTableStore indexedTableStore;

    private int shardCount;
    
    private int indexReplicationFactor;

    private long shardIndexRecordBatchSize;
    
    private int shardIndexWorkerInterval;
    
    private String indexStoreLocation;
    
    private int indexWorkerCount;

    private int maxRAMPerIndexingThreadInMB;

    private double maxRAMBufferForLuceneDocsInMB;

    private boolean useCompoundIndexFile;

    private int indexCommunicatorBufferSize;

    private int indexQueueCleanupThreshold;

    private String taxonomyWriterCacheType;

    private String taxonomyWriterLRUCacheType;

    private int taxonomyWriterLRUCacheSize;


    public Analyzer getLuceneAnalyzer() {
        return luceneAnalyzer;
    }

    public void setLuceneAnalyzer(Analyzer luceneAnalyzer) {
        this.luceneAnalyzer = luceneAnalyzer;
    }

    public AnalyticsRecordStore getAnalyticsRecordStore() {
        return analyticsRecordStore;
    }

    public void setAnalyticsRecordStore(AnalyticsRecordStore analyticsRecordStore) {
        this.analyticsRecordStore = analyticsRecordStore;
    }

    public AnalyticsDataService getAnalyticsDataService() {
        return analyticsDataService;
    }

    public void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        this.analyticsDataService = analyticsDataService;
    }

    public AnalyticsIndexedTableStore getIndexedTableStore() {
        return indexedTableStore;
    }

    public void setIndexedTableStore(AnalyticsIndexedTableStore indexedTableStore) {
        this.indexedTableStore = indexedTableStore;
    }

    public int getShardCount() {
        return shardCount;
    }

    public void setShardCount(int shardCount) {
        this.shardCount = shardCount;
    }
    
    public int getIndexReplicationFactor() {
        return indexReplicationFactor;
    }

    public void setIndexReplicationFactor(int indexReplicationFactor) {
        this.indexReplicationFactor = indexReplicationFactor;
    }

    public long getShardIndexRecordBatchSize() {
        return shardIndexRecordBatchSize;
    }

    public void setShardIndexRecordBatchSize(long shardIndexRecordBatchSize) {
        this.shardIndexRecordBatchSize = shardIndexRecordBatchSize;
    }
    
    public int getShardIndexWorkerInterval() {
        return shardIndexWorkerInterval;
    }

    public void setShardIndexWorkerInterval(int shardIndexWorkerInterval) {
        this.shardIndexWorkerInterval = shardIndexWorkerInterval;
    }

    public String getIndexStoreLocation() {
        return indexStoreLocation;
    }

    public void setIndexStoreLocation(String indexStoreLocation) {
        this.indexStoreLocation = indexStoreLocation;
    }

    public int getIndexWorkerCount() {
        return indexWorkerCount;
    }

    public void setIndexWorkerCount(int indexWorkerCount) {
        this.indexWorkerCount = indexWorkerCount;
    }

    public boolean isUseCompoundIndexFile() {
        return useCompoundIndexFile;
    }

    public void setUseCompoundIndexFile(boolean useCompoundIndexFile) {
        this.useCompoundIndexFile = useCompoundIndexFile;
    }

    public int getMaxRAMPerIndexingThreadInMB() {
        return maxRAMPerIndexingThreadInMB;
    }

    public void setMaxRAMPerIndexingThreadInMB(int maxRAMPerIndexingThreadInMB) {
        this.maxRAMPerIndexingThreadInMB = maxRAMPerIndexingThreadInMB;
    }

    public double getMaxRAMBufferForLuceneDocsInMB() {
        return maxRAMBufferForLuceneDocsInMB;
    }

    public void setMaxRAMBufferForLuceneDocsInMB(double maxRAMBufferForLuceneDocsInBytes) {
        this.maxRAMBufferForLuceneDocsInMB = maxRAMBufferForLuceneDocsInBytes / (1024 * 1024);
    }

    public int getIndexCommunicatorBufferSize() {
        return indexCommunicatorBufferSize;
    }

    public void setIndexCommunicatorBufferSize(int indexCommunicatorBufferSize) {
        this.indexCommunicatorBufferSize = indexCommunicatorBufferSize;
    }

    public int getIndexQueueCleanupThreshold() {
        return indexQueueCleanupThreshold;
    }

    public void setIndexQueueCleanupThreshold(int indexQueueCleanupThreshold) {
        this.indexQueueCleanupThreshold = indexQueueCleanupThreshold;
    }

    public String getTaxonomyWriterCacheType() {
        return taxonomyWriterCacheType;
    }

    public void setTaxonomyWriterCacheType(String taxonomyWriterCacheType) {
        this.taxonomyWriterCacheType = taxonomyWriterCacheType;
    }

    public String getTaxonomyWriterLRUCacheType() {
        return taxonomyWriterLRUCacheType;
    }

    public void setTaxonomyWriterLRUCacheType(String taxonomyWriterLRUCacheType) {
        this.taxonomyWriterLRUCacheType = taxonomyWriterLRUCacheType;
    }

    public int getTaxonomyWriterLRUCacheSize() {
        return taxonomyWriterLRUCacheSize;
    }

    public void setTaxonomyWriterLRUCacheSize(int taxonomyWriterLRUCacheSize) {
        this.taxonomyWriterLRUCacheSize = taxonomyWriterLRUCacheSize;
    }
}
