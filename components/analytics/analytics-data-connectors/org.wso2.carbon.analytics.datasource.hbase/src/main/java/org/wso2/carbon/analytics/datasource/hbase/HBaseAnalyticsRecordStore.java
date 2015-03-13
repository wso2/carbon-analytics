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
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.DirectAnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseAnalyticsDSConstants;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

public class HBaseAnalyticsRecordStore extends DirectAnalyticsRecordStore {

    private Admin admin;
    private Connection conn;

    private HBaseAnalyticsConfigurationEntry queryConfig;

    private static final Log log = LogFactory.getLog(HBaseAnalyticsRecordStore.class);

    public HBaseAnalyticsRecordStore(Connection conn, HBaseAnalyticsConfigurationEntry entry) throws IOException {
        this.conn = conn;
        this.admin = conn.getAdmin();
        this.queryConfig = entry;
    }

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        //this.queryConfig = HBaseUtils.lookupConfiguration();
        String dsName = properties.get(HBaseAnalyticsDSConstants.DATASOURCE_NAME);
        if (dsName == null) {
            throw new AnalyticsException("The property '" + HBaseAnalyticsDSConstants.DATASOURCE_NAME +
                    "' is required");
        }
        try {
            this.conn = (Connection) InitialContext.doLookup(dsName);
            this.admin = conn.getAdmin();
        } catch (NamingException e) {
            throw new AnalyticsException("Error in looking up data source: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsException("Error in creating HBase client: " + e.getMessage(), e);
        }
    }

    @Override
    public void createTable(int tenantId, String tableName) throws AnalyticsException {
        /* If the table we're proposing to create already exists, return in silence */
        if (this.tableExists(tenantId, tableName)) {
            log.debug("Creation of table " + tableName + " for tenant " + tenantId +
                    " could not be carried out since said table already exists.");
            return;
        }

        HTableDescriptor dataDescriptor = new HTableDescriptor(TableName.valueOf(
                HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.DATA)));

        /* creating table with standard column families "carbon-analytics-data" and "carbon-analytics-timestamp" */
        dataDescriptor.addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.ANALYTICS_COLUMN_FAMILY_NAME))
                .addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.ANALYTICS_META_COLUMN_FAMILY_NAME)
                        .setMaxVersions(1));

        HTableDescriptor indexDescriptor = new HTableDescriptor(TableName.valueOf(
                HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.INDEX)));
        /* creating table with standard column family "carbon-analytics-index" */
        indexDescriptor.addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.INDEX_COLUMN_FAMILY_NAME)
                .setMaxVersions(1));

        /* Table creation should fail if index cannot be created, so attempting to create index table first. */
        try {
            admin.createTable(indexDescriptor);
            admin.createTable(dataDescriptor);
        } catch (IOException e) {
            throw new AnalyticsException("Error creating table " + tableName + " for tenant " + tenantId, e);
        }
    }

    @Override
    public void setTableSchema(int tenantId, String tableName, AnalyticsSchema schema) throws AnalyticsTableNotAvailableException, AnalyticsException {
        //TODO
    }

    @Override
    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsTableNotAvailableException, AnalyticsException {
        //TODO
        return null;
    }

    private byte[] serializeSchema(AnalyticsSchema schema) throws AnalyticsException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(schema);
            oos.close();
            byte[] output = baos.toByteArray();
            baos.close();
            return output;
        } catch (IOException e) {
            throw new AnalyticsException("Error serializing schema: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        boolean isExist;
        try {
            isExist = this.admin.tableExists(TableName.valueOf(
                    HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.DATA)));
        } catch (IOException e) {
            throw new AnalyticsException("Error checking existence of table " + tableName + " for tenant " + tenantId, e);
        }
        return isExist;
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        /* If the table we're proposing to create does not exist, return in silence */
        if (!(this.tableExists(tenantId, tableName))) {
            log.debug("Deletion of table " + tableName + " for tenant " + tenantId +
                    " could not be carried out since said table did not exist.");
            return;
        }
        TableName dataTable = TableName.valueOf(HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.DATA));
        TableName indexTable = TableName.valueOf(HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.INDEX));
        try {
                /* delete the data table first */
            this.admin.disableTable(dataTable);
            this.admin.deleteTable(dataTable);
                /* then delete the index table */
            this.admin.disableTable(indexTable);
            this.admin.deleteTable(indexTable);
        } catch (IOException e) {
            throw new AnalyticsException("Error deleting table " + tableName, e);
        }

    }

    public void close() {
        try {
            this.admin.close();
            this.conn.close();
        } catch (IOException ignore) {
                /* do nothing, the connection is dead anyway */
        }
        log.debug("Closed HBase connection transients successfully.");
    }

    @Override
    public List<String> listTables(int tenantId) throws AnalyticsException {
        List<String> tables = new ArrayList<>();
        /* Handling the existence of analytics tables only, not index.
         * a caveat: the generated prefix is never null.                 */
        String prefix = HBaseUtils.generateTablePrefix(tenantId, HBaseAnalyticsDSConstants.DATA);
        try {
            HTableDescriptor[] tableDesc = this.admin.listTables();
            String tableName;
            for (HTableDescriptor htd : tableDesc) {
                if (htd != null) {
                    tableName = htd.getNameAsString();
                    /* string checking (clauses 1,2) and pattern matching (clause 3) */
                    if ((tableName != null) && !(tableName.isEmpty()) && (tableName.startsWith(prefix))) {
                        /* trimming out the prefix generated by us, which should be transparent to the user */
                        tables.add(tableName.substring(prefix.length()));
                    }
                }
            }
        } catch (IOException e) {
            throw new AnalyticsException("Error listing tables for tenant " + tenantId + " :" + e.getMessage(), e);
        }
        return tables;
    }

    @Override
    public long getRecordCount(int tenantId, String tableName) throws AnalyticsException {
        throw new HBaseUnsupportedOperationException("Retrieving row count is not supported " +
                "for HBase Analytics Record Stores");
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException {
        Table table, indexTable;
        Put put;
        String recordId, indexTableName;
        long timestamp;
        byte[] columnData;
        Map<String, Object> columns;
        List<Put> puts, indexPuts;
        if (records.isEmpty()) {
            return;
        }
        Map<String, List<Record>> recordBatches = this.generateRecordBatches(records);
        try {
            /* iterating over record batches */
            for (String formattedTableName : recordBatches.keySet()) {
                if ((formattedTableName != null) && !(formattedTableName.isEmpty())) {
                    table = this.conn.getTable(TableName.valueOf(formattedTableName));
                    /* Converting data table name to index table name directly since record-level information
                    * which is required for normal table name construction is not available at this stage. */
                    indexTableName = HBaseUtils.convertUserToIndexTable(formattedTableName);
                    indexTable = this.conn.getTable(TableName.valueOf(indexTableName));
                    List<Record> recordList = recordBatches.get(formattedTableName);
                    puts = new ArrayList<>();
                    indexPuts = new ArrayList<>();
                    /* iterating over single records in a batch */
                    for (Record record : recordList) {
                        if (record != null) {
                            recordId = record.getId();
                            timestamp = record.getTimestamp();
                            columns = record.getValues();
                            put = new Put(recordId.getBytes());
                            /* iterating over columns in a record */
                            for (String key : columns.keySet()) {
                                /* encoding column data to bytes.
                                * Note: the encoded column value also contains the column name. */
                                // TODO: change long encoding to respect HBase lexical ordering
                                columnData = GenericUtils.encodeElement(key, columns.get(key));
                                if (columnData.length != 0) {
                                    put.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_COLUMN_FAMILY_NAME,
                                            HBaseUtils.generateColumnQualifier(key), timestamp, columnData);
                                }
                            }
                            /* Adding timestamp to the meta column family in the data table for indexing purposes */
                            put.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_META_COLUMN_FAMILY_NAME,
                                    HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME, timestamp,
                                    HBaseUtils.encodeLong(timestamp));
                            indexPuts.add(this.putIndexData(record));
                            puts.add(put);
                        }
                    }
                    /* Using Table.put(List<Put>) method to minimise network calls per table */
                    indexTable.put(indexPuts);
                    table.put(puts);
                    table.close();
                    indexTable.close();
                }
            }
        } catch (IOException e) {
            throw new AnalyticsException("Error adding new records: " + e.getMessage(), e);
        }
    }

    private Put putIndexData(Record record) {
        Put indexPut = new Put(HBaseUtils.encodeLong(record.getTimestamp()));
        /* Setting the column qualifier the same as the column value to enable multiple columns per row with
        * unique qualifiers, since we will anyway not use the qualifier during index read */
        indexPut.addColumn(HBaseAnalyticsDSConstants.INDEX_COLUMN_FAMILY_NAME, record.getId().getBytes(),
                record.getId().getBytes());
        return indexPut;
    }

    private Map<String, List<Record>> generateRecordBatches(List<Record> records) {
        Map<String, List<Record>> recordBatches = new HashMap<>();
        List<Record> recordBatch;
        for (Record record : records) {
            recordBatch = recordBatches.get(this.inferRecordIdentity(record));
            if (recordBatch == null) {
                recordBatch = new ArrayList<>();
                recordBatches.put(this.inferRecordIdentity(record), recordBatch);
            }
            recordBatch.add(record);
        }
        return recordBatches;
    }

    private String inferRecordIdentity(Record record) {
        return HBaseUtils.generateTableName(record.getTenantId(), record.getTableName(), HBaseAnalyticsDSConstants.DATA);
    }

    @Override
    public Iterator<Record> getRecords(int tenantId, String tableName, List<String> columns, long timeFrom,
                                       long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException {
        return this.getRecords(tenantId, tableName, columns, this.lookupIndex(tenantId, tableName, timeFrom, timeTo));
    }

    @Override
    public Iterator<Record> getRecords(int tenantId, String tableName, List<String> columns, List<String> ids)
            throws AnalyticsException {
        int batchSize = this.queryConfig.getBatchSize();
        if (batchSize > ids.size()) {
            batchSize = ids.size();
        }
        return new BatchedHBaseResultIterator(tenantId, tableName, columns, ids, this.conn, batchSize);
    }

    private List<String> lookupIndex(int tenantId, String tableName, long startTime, long endTime)
            throws AnalyticsException {
        List<String> recordIds = new ArrayList<>();
        String formattedTableName = HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.INDEX);
        Table indexTable;
        Cell[] cells;

        //TODO: LOGIC FOR NEGATIVE TIMESTAMPS

        /* Setting (end-time)+1L because end-time is exclusive (which is not what we want) */
        Scan indexScan = new Scan(HBaseUtils.encodeLong(startTime), HBaseUtils.encodeLong(endTime + 1L));
        indexScan.addFamily(HBaseAnalyticsDSConstants.INDEX_COLUMN_FAMILY_NAME);
        ResultScanner resultScanner;
        try {
            indexTable = this.conn.getTable(TableName.valueOf(formattedTableName));
            resultScanner = indexTable.getScanner(indexScan);
            for (Result rowResult : resultScanner) {
                cells = rowResult.rawCells();
                for (Cell cell : cells) {
                    recordIds.add(Bytes.toString(CellUtil.cloneValue(cell)));
                }
            }
            resultScanner.close();
            indexTable.close();
        } catch (IOException e) {
            throw new AnalyticsException("Index for table " + tableName + " could not be read", e);
        }
        return recordIds;
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        this.delete(tenantId, tableName, this.lookupIndex(tenantId, tableName, timeFrom, timeTo));
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        Table dataTable;
        List<Delete> dataDeletes = new ArrayList<>();
        String dataTableName = HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.DATA);
        for (String recordId : ids) {
            dataDeletes.add(new Delete(recordId.getBytes()));
        }
        try {
            dataTable = this.conn.getTable(TableName.valueOf(dataTableName));
            dataTable.delete(dataDeletes);
        } catch (IOException e) {
            throw new AnalyticsException("Error deleting records from " + tableName + " for tenant " + tenantId + " : " + e.getMessage(), e);
        }
        this.deleteIndexEntries(tenantId, tableName, this.lookupTimestamp(dataTable, ids, tenantId, tableName));
    }

    private void deleteIndexEntries(int tenantId, String tableName, List<Long> timestamps) throws AnalyticsException {
        Table indexTable;
        List<Delete> indexDeletes = new ArrayList<>();
        String indexTableName = HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.INDEX);
        for (Long timestamp : timestamps) {
            indexDeletes.add(new Delete(HBaseUtils.encodeLong(timestamp)));
        }
        try {
            indexTable = this.conn.getTable(TableName.valueOf(indexTableName));
            indexTable.delete(indexDeletes);
            indexTable.close();
        } catch (IOException e) {
            throw new AnalyticsException("Error deleting record indices from " + tableName + " for tenant " + tenantId + " : " + e.getMessage(), e);
        }
    }

    private List<Long> lookupTimestamp(Table dataTable, List<String> rowIds, int tenantId, String tableName)
            throws AnalyticsException {
        List<Long> timestamps = new ArrayList<>();
        List<Get> gets = new ArrayList<>();
        for (String rowId : rowIds) {
            gets.add(new Get(Bytes.toBytes(rowId)).addColumn(HBaseAnalyticsDSConstants.ANALYTICS_META_COLUMN_FAMILY_NAME,
                    HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME));
        }
        try {
            Result[] results = dataTable.get(gets);
            for (Result res : results) {
                timestamps.add(HBaseUtils.decodeLong(res.value()));
            }
            dataTable.close();
        } catch (IOException e) {
            throw new AnalyticsException("The table " + tableName + " for tenant " + tenantId +
                    " could not be initialized for deletion of rows: " + e.getMessage(), e);
        }
        return timestamps;
    }

    public class HBaseUnsupportedOperationException extends AnalyticsException {
        public HBaseUnsupportedOperationException(String s) {
            super(s);
        }
    }

}