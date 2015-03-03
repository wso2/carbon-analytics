/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.datasource.hbase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.core.rs.RecordGroup;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseAnalyticsDSConstants;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.*;

public class HBaseAnalyticsRecordStore implements AnalyticsRecordStore {

    private Admin admin;

    private static final Log log = LogFactory.getLog(HBaseAnalyticsRecordStore.class);

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        String dsName = properties.get(HBaseAnalyticsDSConstants.DATASOURCE_NAME);
        if (dsName == null) {
            throw new AnalyticsException("The property '" + HBaseAnalyticsDSConstants.DATASOURCE_NAME +
                    "' is required");
        }
        try {
            Configuration config = (Configuration) InitialContext.doLookup(dsName);
            Connection conn = ConnectionFactory.createConnection(config);
            this.admin = conn.getAdmin();
        } catch (NamingException e) {
            throw new AnalyticsException("Error in looking up data source: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsException("Error in creating HBase client: " + e.getMessage(), e);
        }
    }

    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        HTableDescriptor htd = new HTableDescriptor(TableName.valueOf(
                HBaseUtils.generateAnalyticsTableName(tenantId, tableName)));

        /* creating table with standard column families "carbon-analytics-data" and "carbon-analytics-timestamp" */
        htd.addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.ANALYTICS_COLUMN_FAMILY_NAME))
                .addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.ANALYTICS_TS_COLUMN_FAMILY_NAME));

        HTableDescriptor htd_idx = new HTableDescriptor(TableName.valueOf(
                HBaseUtils.generateIndexTableName(tenantId, tableName)));
        /* creating table with standard column family "carbon-analytics-index" */
        htd_idx.addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.ANALYTICS_COLUMN_FAMILY_NAME));

        /* Table creation should fail if index cannot be created, so attempting to create index table first. */
        try {
            admin.createTable(htd_idx);
            admin.createTable(htd);
        } catch (IOException e) {
            throw new AnalyticsException("Error creating table: " + tableName, e);
        }
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        boolean isExist;
        try {
            isExist = this.admin.tableExists(TableName.valueOf(HBaseUtils.generateAnalyticsTableName(tenantId, tableName)));
        } catch (IOException e) {
            throw new AnalyticsException("Error checking table existence: " + tableName, e);
        }
        return isExist;
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        try {
            this.admin.deleteTable(TableName.valueOf(HBaseUtils.generateAnalyticsTableName(tenantId, tableName)));
            this.admin.deleteTable(TableName.valueOf(HBaseUtils.generateIndexTableName(tenantId, tableName)));
        } catch (IOException e) {
            throw new AnalyticsException("Error deleting table " + tableName, e);
        }
    }

    public void close() throws IOException {
        log.debug("Closing HBase Admin instance.");
        this.admin.close();
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        List<String> tables = new ArrayList<>();
        /* Handling the existence of analytics tables only, not index */
        String prefix = HBaseUtils.generateAnalyticsTablePrefix(tenantId);
        try {
            HTableDescriptor[] tableDesc = this.admin.listTables();
            String tableName;
            for (HTableDescriptor htd : tableDesc) {
                if (htd != null){
                    tableName = htd.getNameAsString();
                    /* string checking (clauses 1,2) and pattern matching (clause 3) */
                    if ((tableName != null) && !(tableName.isEmpty()) && (tableName.startsWith(prefix))) {
                        tables.add(tableName);
                    }
                }
            }
        } catch (IOException e) {
            throw new AnalyticsException("Error listing tables: " + e.getMessage(), e);
        }
        return tables;
    }

    @Override
    public long getRecordCount(int tenantId, String tableName) throws AnalyticsException, AnalyticsTableNotAvailableException {
        throw new AnalyticsException("Retrieving row count is not supported for " +
                "HBase Analytics Record Store implementation");
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (records.size() == 0) {
            return;
        }
        // TODO
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, long timeFrom,
                             long timeTo, int recordsFrom, int recordsCount)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        // TODO
        return new RecordGroup[0];
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, List<String> columns, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        // TODO
        return new RecordGroup[0];
    }

    @Override
    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException {
        // TODO
        return null;
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        // TODO
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        // TODO
    }

}
