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
package org.wso2.carbon.analytics.datasource.cassandra;

import java.util.List;

import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * Cassandra {@link RecordGroup} implementation, which is token range aware,
 * used for partition based record groups.
 */
public class TokenRangeRecordGroup implements RecordGroup {
    
    private static final long serialVersionUID = -8748485743904308191L;

    private int tenantId;
    
    private String tableName;
    
    private List<String> columns;
    
    private List<CassandraTokenRange> tokenRanges;
    
    private String host;
    
    private int count;
    
    public TokenRangeRecordGroup(int tenantId, String tableName, List<String> columns,
            List<CassandraTokenRange> tokenRanges, String host, int count) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.columns = columns;
        this.tokenRanges = tokenRanges;
        this.host = host;
        this.count = count;
    }

    @Override
    public String[] getLocations() throws AnalyticsException {
        return new String[] { this.host };
    }
    
    public int getTenantId() {
        return tenantId;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public List<String> getColumns() {
        return columns;
    }
    
    public List<CassandraTokenRange> getTokenRanges() {
        return tokenRanges;
    }
    
    public int getCount() {
        return count;
    }
    
}