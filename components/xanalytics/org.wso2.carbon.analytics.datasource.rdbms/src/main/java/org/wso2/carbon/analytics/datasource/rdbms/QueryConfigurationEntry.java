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
 * This class represents the query configuration required to initialize a {@link RDBMSAnalyticsDataSource}.
 */
@XmlRootElement (name = "database")
public class QueryConfigurationEntry {

    private String databaseName;
    
    private String[] recordTableInitQueries;
    
    private String[] recordTableDeleteQueries;
    
    private String recordCountQuery;
    
    private String[] fsTableInitQueries;
    
    private String fsTablesCheckQuery;
    
    private String recordInsertQuery;
    
    private String recordRetrievalQuery;
    
    private String recordDeletionQuery;
    
    private String recordRetrievalWithIdsQuery;
    
    private String recordDeletionWithIdsQuery;
        
    private boolean paginationFirstZeroIndexed;
    
    private boolean paginationFirstInclusive;
    
    private boolean paginationSecondLength;
    
    private boolean paginationSecondZeroIndexed;
    
    private boolean paginationSecondInclusive;
    
    private String fsPathRetrievalQuery;
    
    private String fsListFilesQuery;
    
    private String fsInsertPathQuery;
    
    private String fsFileLengthRetrievalQuery;
    
    private String fsSetFileLengthQuery;
    
    private String fsReadDataChunkQuery;
    
    private String fsWriteDataChunkQuery;
    
    private String fsUpdateDataChunkQuery;
    
    private String fsDeletePathQuery;
    
    private int fsDataChunkSize;

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    @XmlAttribute (name = "name")
    public String getDatabaseName() {
        return databaseName;
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
    
    public String getRecordInsertQuery() {
        return recordInsertQuery;
    }
    
    public void setRecordInsertQuery(String recordInsertQuery) {
        this.recordInsertQuery = recordInsertQuery;
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

    public boolean isPaginationFirstZeroIndexed() {
        return paginationFirstZeroIndexed;
    }
    
    public void setPaginationFirstZeroIndexed(boolean paginationFirstZeroIndexed) {
        this.paginationFirstZeroIndexed = paginationFirstZeroIndexed;
    }
    
    public boolean isPaginationFirstInclusive() {
        return paginationFirstInclusive;
    }
    
    public void setPaginationFirstInclusive(boolean paginationFirstInclusive) {
        this.paginationFirstInclusive = paginationFirstInclusive;
    }
    
    public boolean isPaginationSecondLength() {
        return paginationSecondLength;
    }
    
    public void setPaginationSecondLength(boolean paginationSecondLength) {
        this.paginationSecondLength = paginationSecondLength;
    }
    
    public boolean isPaginationSecondZeroIndexed() {
        return paginationSecondZeroIndexed;
    }
    
    public void setPaginationSecondZeroIndexed(boolean paginationSecondZeroIndexed) {
        this.paginationSecondZeroIndexed = paginationSecondZeroIndexed;
    }

    public boolean isPaginationSecondInclusive() {
        return paginationSecondInclusive;
    }
    
    public void setPaginationSecondInclusive(boolean paginationSecondInclusive) {
        this.paginationSecondInclusive = paginationSecondInclusive;
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
    
    public void setFsDeletePathQuery(String fsDeletePathQuery) {
        this.fsDeletePathQuery = fsDeletePathQuery;
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
    
    public void setFsUpdateDataChunkQuery(String fsUpdateDataChunkQuery) {
        this.fsUpdateDataChunkQuery = fsUpdateDataChunkQuery;
    }
    
    public String getRecordCountQuery() {
        return recordCountQuery;
    }
    
    public void setRecordCountQuery(String recordCountQuery) {
        this.recordCountQuery = recordCountQuery;
    }
    
}
