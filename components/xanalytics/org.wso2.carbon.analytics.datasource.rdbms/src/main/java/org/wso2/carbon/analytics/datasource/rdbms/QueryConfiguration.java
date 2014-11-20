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

/**
 * This class represents the query configuration required to initialize a {@link RDBMSAnalyticsDataSource}.
 */
public class QueryConfiguration {

    private String[] initQueries;
    
    private String systemTablesCheckQuery;
    
    private String recordInsertQuery;
    
    private String recordRetrievalQuery;
    
    private String recordDeletionQuery;
    
    private String recordRetrievalWithIdsQuery;
    
    private String recordDeletionWithIdsQuery;

    public String[] getInitQueries() {
        return initQueries;
    }
  
    public void setInitQueries(String[] initQueries) {
        this.initQueries = initQueries;
    }
    
    public String getSystemTablesCheckQuery() {
        return systemTablesCheckQuery;
    }
    
    public void setSystemTablesCheckQuery(String systemTablesCheckQuery) {
        this.systemTablesCheckQuery = systemTablesCheckQuery;
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
    
}
