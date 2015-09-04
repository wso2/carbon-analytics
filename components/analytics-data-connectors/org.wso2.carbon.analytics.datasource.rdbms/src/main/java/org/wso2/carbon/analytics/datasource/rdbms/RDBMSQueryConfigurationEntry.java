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
package org.wso2.carbon.analytics.datasource.rdbms;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the query configuration required to initialize a {@link RDBMSAnalyticsRecordStore}.
 */
@XmlRootElement (name = "database")
public class RDBMSQueryConfigurationEntry {

    private String databaseName;
    
    private String category;
    
    private double minVersion;
    
    private double maxVersion;
    
    private String recordTableCheckQuery;
    
    private String[] recordTableInitQueries;
    
    private String[] recordTableDeleteQueries;
    
    private String recordCountQuery;
    
    private String[] fsTableInitQueries;
    
    private String fsTablesCheckQuery;
    
    private String recordMergeQuery;
    
    private String recordInsertQuery;
    
    private String recordUpdateQuery;
    
    private String recordRetrievalQuery;
    
    private String recordDeletionQuery;
    
    private String recordRetrievalWithIdsQuery;
    
    private String recordDeletionWithIdsQuery;
        
    private int recordBatchSize = RDBMSAnalyticsDSConstants.RECORD_BATCH_SIZE;

    private boolean paginationSupported;

    private boolean recordCountSupported;
        
    private PaginationMode paginationMode;
    
    private String fsPathRetrievalQuery;
    
    private String fsListFilesQuery;
    
    private String fsUpdateDataPathQuery;
    
    private String fsInsertPathQuery;
    
    private String fsFileLengthRetrievalQuery;
    
    private String fsSetFileLengthQuery;
    
    private String fsReadDataChunkQuery;
    
    private String fsWriteDataChunkQuery;
    
    private String fsUpdateDataChunkQuery;
    
    private String fsMergeDataChunkQuery;
    
    private String fsDeletePathQuery;
    
    private String fsDeleteDataQuery;
    
    private int fsDataChunkSize;

    private boolean blobLengthRequired;

    private boolean forwardOnlyReadEnabled;

    private int fetchSize = Integer.MIN_VALUE;

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    @XmlAttribute (name = "name", required = true)
    public String getDatabaseName() {
        return databaseName;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    @XmlAttribute (name = "category", required = false)
    public String getCategory() {
        return category;
    }
    
    public void setMinVersion(double minVersion) {
        this.minVersion = minVersion;
    }
    
    @XmlAttribute (name = "minVersion", required = false)
    public double getMinVersion() {
        return minVersion;
    }
    
    public void setMaxVersion(double maxVersion) {
        this.maxVersion = maxVersion;
    }
    
    @XmlAttribute (name = "maxVersion", required = false)
    public double getMaxVersion() {
        return maxVersion;
    }
    
    public void setRecordTableCheckQuery(String recordTableCheckQuery) {
        this.recordTableCheckQuery = recordTableCheckQuery;
    }
    
    public String getRecordTableCheckQuery() {
        return recordTableCheckQuery;
    }
    
    @XmlElementWrapper (name = "recordTableInitQueries")
    @XmlElement (name = "query")
    public String[] getRecordTableInitQueries() {
        return recordTableInitQueries;
    }
  
    public void setRecordTableInitQueries(String[] recordTableInitQueries) {
        this.recordTableInitQueries = recordTableInitQueries;
    }
    
    @XmlElementWrapper (name = "recordTableDeleteQueries")
    @XmlElement (name = "query")
    public String[] getRecordTableDeleteQueries() {
        return recordTableDeleteQueries;
    }
  
    public void setRecordTableDeleteQueries(String[] recordTableDeleteQueries) {
        this.recordTableDeleteQueries = recordTableDeleteQueries;
    }
    
    @XmlElementWrapper (name = "fsTableInitQueries")
    @XmlElement (name = "query")
    public String[] getFsTableInitQueries() {
        return fsTableInitQueries;
    }
  
    public void setFsTableInitQueries(String[] fsTableInitQueries) {
        this.fsTableInitQueries = fsTableInitQueries;
    }
    
    public String getFsTablesCheckQuery() {
        return fsTablesCheckQuery;
    }
    
    public void setFsTablesCheckQuery(String fsTablesCheckQuery) {
        this.fsTablesCheckQuery = fsTablesCheckQuery;
    }
    
    public String getRecordMergeQuery() {
        return recordMergeQuery;
    }
    
    public void setRecordMergeQuery(String recordMergeQuery) {
        this.recordMergeQuery = recordMergeQuery;
    }
    
    public String getRecordInsertQuery() {
        return recordInsertQuery;
    }
    
    public void setRecordInsertQuery(String recordInsertQuery) {
        this.recordInsertQuery = recordInsertQuery;
    }
    
    public String getRecordUpdateQuery() {
        return recordUpdateQuery;
    }
    
    public void setRecordUpdateQuery(String recordUpdateQuery) {
        this.recordUpdateQuery = recordUpdateQuery;
    }
    
    public String getRecordRetrievalQuery() {
        return recordRetrievalQuery;
    }
    
    public void setRecordRetrievalQuery(String recordRetrievalQuery) {
        this.recordRetrievalQuery = recordRetrievalQuery;
    }
    
    public String getRecordDeletionQuery() {
        return recordDeletionQuery;
    }
    
    public void setRecordDeletionQuery(String recordDeletionQuery) {
        this.recordDeletionQuery = recordDeletionQuery;
    }
    
    public String getRecordRetrievalWithIdsQuery() {
        return recordRetrievalWithIdsQuery;
    }
    
    public void setRecordRetrievalWithIdsQuery(String recordRetrievalWithIdsQuery) {
        this.recordRetrievalWithIdsQuery = recordRetrievalWithIdsQuery;
    }

    public String getRecordDeletionWithIdsQuery() {
        return recordDeletionWithIdsQuery;
    }

    public void setRecordDeletionWithIdsQuery(String recordDeletionWithIdsQuery) {
        this.recordDeletionWithIdsQuery = recordDeletionWithIdsQuery;
    }
    
    public int getRecordBatchSize() {
        return recordBatchSize;
    }

    public void setRecordBatchSize(int recordBatchSize) {
        this.recordBatchSize = recordBatchSize;
    }

    public String getFsPathRetrievalQuery() {
        return fsPathRetrievalQuery;
    }
    
    public void setFsPathRetrievalQuery(String fsPathRetrievalQuery) {
        this.fsPathRetrievalQuery = fsPathRetrievalQuery;
    }
    
    public String getFsListFilesQuery() {
        return fsListFilesQuery;
    }
    
    public void setFsListFilesQuery(String fsListFilesQuery) {
        this.fsListFilesQuery = fsListFilesQuery;
    }
    
    public String getFsInsertPathQuery() {
        return fsInsertPathQuery;
    }
    
    public void setFsInsertPathQuery(String fsInsertPathQuery) {
        this.fsInsertPathQuery = fsInsertPathQuery;
    }
    
    public String getFsFileLengthRetrievalQuery() {
        return fsFileLengthRetrievalQuery;
    }
    
    public void setFsFileLengthRetrievalQuery(String fsFileLengthRetrievalQuery) {
        this.fsFileLengthRetrievalQuery = fsFileLengthRetrievalQuery;
    }
    
    public String getFsSetFileLengthQuery() {
        return fsSetFileLengthQuery;
    }
    
    public void setFsSetFileLengthQuery(String fsSetFileLengthQuery) {
        this.fsSetFileLengthQuery = fsSetFileLengthQuery;
    }
   
    public String getFsReadDataChunkQuery() {
        return fsReadDataChunkQuery;
    }
    
    public void setFsReadDataChunkQuery(String fsReadDataChunkQuery) {
        this.fsReadDataChunkQuery = fsReadDataChunkQuery;
    }
    
    public String getFsWriteDataChunkQuery() {
        return fsWriteDataChunkQuery;
    }

    public void setFsWriteDataChunkQuery(String fsWriteDataChunkQuery) {
        this.fsWriteDataChunkQuery = fsWriteDataChunkQuery;
    }
    
    public String getFsDeletePathQuery() {
        return fsDeletePathQuery;
    }
    
    public String getFsDeleteDataQuery() {
        return fsDeleteDataQuery;
    }
    
    public void setFsDeletePathQuery(String fsDeletePathQuery) {
        this.fsDeletePathQuery = fsDeletePathQuery;
    }
    
    public void setFsDeleteDataQuery(String fsDeleteDataQuery) {
        this.fsDeleteDataQuery = fsDeleteDataQuery;
    }
    
    public String getFsUpdateDataPathQuery() {
        return fsUpdateDataPathQuery;
    }
    
    public void setFsUpdateDataPathQuery(String fsUpdateDataPathQuery) {
        this.fsUpdateDataPathQuery = fsUpdateDataPathQuery;
    }

    public int getFsDataChunkSize() {
        return fsDataChunkSize;
    }

    public void setFsDataChunkSize(int fsDataChunkSize) {
        this.fsDataChunkSize = fsDataChunkSize;
    }
    
    public String getFsUpdateDataChunkQuery() {
        return fsUpdateDataChunkQuery;
    }
    
    public String getFsMergeDataChunkQuery() {
        return fsMergeDataChunkQuery;
    }
    
    public void setFsMergeDataChunkQuery(String fsMergeDataChunkQuery) {
        this.fsMergeDataChunkQuery = fsMergeDataChunkQuery;
    }

    public void setFsUpdateDataChunkQuery(String fsUpdateDataChunkQuery) {
        this.fsUpdateDataChunkQuery = fsUpdateDataChunkQuery;
    }
    
    public String getRecordCountQuery() {
        return recordCountQuery;
    }
    
    public void setRecordCountQuery(String recordCountQuery) {
        this.recordCountQuery = recordCountQuery;
    }
    
    @XmlElement (required = true)
    public PaginationMode getPaginationMode() {
        return paginationMode;
    }
    
    public void setPaginationMode(PaginationMode paginationMode) {
        this.paginationMode = paginationMode;
    }

    @XmlElement(name = "blobLengthRequired", nillable = true)
    public boolean isBlobLengthRequired() {
        return blobLengthRequired;
    }

    public void setBlobLengthRequired(boolean blobLengthRequired) {
        this.blobLengthRequired = blobLengthRequired;
    }

    @XmlElement(name = "forwardOnlyReadEnabled", nillable = true)
    public boolean isForwardOnlyReadEnabled() {
        return forwardOnlyReadEnabled;
    }

    public void setForwardOnlyReadEnabled(boolean forwardOnlyReadEnabled) {
        this.forwardOnlyReadEnabled = forwardOnlyReadEnabled;
    }

    @XmlElement(name = "paginationSupported")
    public boolean isPaginationSupported() {
        return paginationSupported;
    }

    public void setPaginationSupported(boolean paginationSupported) {
        this.paginationSupported = paginationSupported;
    }

    @XmlElement(name = "recordCountSupported")
    public boolean isRecordCountSupported() {
        return recordCountSupported;
    }

    public void setRecordCountSupported(boolean recordCountSupported) {
        this.recordCountSupported = recordCountSupported;
    }

    @XmlElement(name = "fetchSize", nillable = true)
    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * Pagination mode to be used for a specific RDBMS.
     */
    public static enum PaginationMode {
        /* MySQL, H2, MSSQL 2012 like, (recordsFrom, recordsCount) */
        MODE1,
        /* Oracle, MSSQL ROWNUM like, (recordsFrom + recordsCount, recordsFrom) */
        MODE2,
        /* inverse MODE2, (recordsFrom, recordsFrom + recordsCount) */
        MODE3
    }
    
}
