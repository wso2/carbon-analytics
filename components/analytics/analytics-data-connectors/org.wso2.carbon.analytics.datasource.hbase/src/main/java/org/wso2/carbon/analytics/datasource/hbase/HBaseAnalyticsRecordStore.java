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
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.DirectAnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseAnalyticsDSConstants;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseUtils;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.*;

public class HBaseAnalyticsRecordStore extends DirectAnalyticsRecordStore {

    private Admin admin;
    private Connection conn;

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
            this.conn = ConnectionFactory.createConnection(config);
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
                .addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.ANALYTICS_TS_COLUMN_FAMILY_NAME)
                        .setMaxVersions(1));

        HTableDescriptor htd_idx = new HTableDescriptor(TableName.valueOf(
                HBaseUtils.generateIndexTableName(tenantId, tableName)));
        /* creating table with standard column family "carbon-analytics-index" */
        htd_idx.addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.INDEX_COLUMN_FAMILY_NAME)
                .setMaxVersions(1));

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
        if (this.tableExists(tenantId, tableName)) {
            try {
                this.admin.deleteTable(TableName.valueOf(HBaseUtils.generateAnalyticsTableName(tenantId, tableName)));
                this.admin.deleteTable(TableName.valueOf(HBaseUtils.generateIndexTableName(tenantId, tableName)));
            } catch (IOException e) {
                throw new AnalyticsException("Error deleting table " + tableName, e);
            }
        } else {
            log.debug("Deletion of table " + tableName + " can not be carried out since said table does not exist.");
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
                if (htd != null) {
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
    public long getRecordCount(int tenantId, String tableName) throws AnalyticsException {
        throw new AnalyticsException("Retrieving row count is not supported for " +
                "HBase Analytics Record Store implementation");
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException {
        Table table;
        Table indexTable;
        Put put, timestampPut;
        String recordId, indexTableName;
        long timestamp;
        byte[] columnData;
        Map<String, Object> columns;
        List<Put> puts;
        List<Put> indexPuts;
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
                                            key.getBytes(), timestamp, columnData);
                                }
                            }
                            indexPuts.add(this.putIndexData(record));
                            /* Adding timestamp under a separate column family to be used for index deletion*/
                            put.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_TS_COLUMN_FAMILY_NAME,
                                    HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME, timestamp,
                                    GenericUtils.encodeLong(timestamp));
                            puts.add(put);
                        }
                    }
                    /* Using Table.put(List<Put>) method to minimise network calls per table */
                    indexTable.put(indexPuts);
                    table.put(puts);
                }
            }
        } catch (IOException e) {
            throw new AnalyticsException("Error adding new records: " + e.getMessage(), e);
        }
    }

    private Put putIndexData(Record record) {
        Put indexPut = new Put(GenericUtils.encodeLong(record.getTimestamp()));
        /* Setting the column qualifier the same as the column value to enable multiple columns per row with
        * unique qualifiers, since we will anyway not use the qualifier during index read */
        indexPut.addColumn(HBaseAnalyticsDSConstants.INDEX_COLUMN_FAMILY_NAME, record.getId().getBytes(),
                record.getId().getBytes());
        return indexPut;
    }

    private Map<String, List<Record>> generateRecordBatches(List<Record> records) {
        Map<String, List<Record>> recordBatches = new HashMap<String, List<Record>>();
        List<Record> recordBatch;
        for (Record record : records) {
            recordBatch = recordBatches.get(this.calculateRecordIdentity(record));
            if (recordBatch == null) {
                recordBatch = new ArrayList<Record>();
                recordBatches.put(this.calculateRecordIdentity(record), recordBatch);
            }
            recordBatch.add(record);
        }
        return recordBatches;
    }

    private String calculateRecordIdentity(Record record) {
        return HBaseUtils.generateAnalyticsTableName(record.getTenantId(), record.getTableName());
    }

    @Override
    public Iterator<Record> getRecords(int tenantId, String tableName, List<String> columns, long timeFrom,
                                       long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException {
        return this.getRecords(tenantId, tableName, columns, this.lookupIndex(tenantId, tableName, timeFrom, timeTo));
    }

    @Override
    public Iterator<Record> getRecords(int tenantId, String tableName, List<String> columns, List<String> ids)
            throws AnalyticsException {
        return new HBaseResultIterator(tenantId, tableName, ids, columns, this.conn);
    }

    private List<String> lookupIndex(int tenantId, String tableName, long startTime, long endTime)
            throws AnalyticsException {
        List<String> recordIds = new ArrayList<>();
        String formattedTableName = HBaseUtils.generateIndexTableName(tenantId, tableName);
        Table indexTable;
        Cell[] cells;
        try {
            indexTable = this.conn.getTable(TableName.valueOf(formattedTableName));
        } catch (IOException e) {
            throw new AnalyticsException("Index for table " + tableName + " could not be initialized", e);
        }
        /* Setting (end-time)+1L because end-time is exclusive (which is not what we want) */
        Scan indexScan = new Scan(GenericUtils.encodeLong(startTime), GenericUtils.encodeLong(endTime + 1L));
        indexScan.addFamily(HBaseAnalyticsDSConstants.INDEX_COLUMN_FAMILY_NAME);
        ResultScanner resultScanner;
        try {
            resultScanner = indexTable.getScanner(indexScan);
            for (Result rowResult : resultScanner) {
                cells = rowResult.rawCells();
                for (Cell cell : cells) {
                    recordIds.add(Bytes.toString(CellUtil.cloneValue(cell)));
                }
            }
            resultScanner.close();
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
        //TODO
    }

    private long lookupTimestamp(Table dataTable, String rowId, int tenantId, String tableName)
            throws AnalyticsException {
        long timestamp = 0L;
       Get timestampGet = new Get(Bytes.toBytes(rowId));
        timestampGet.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_TS_COLUMN_FAMILY_NAME,
                HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME);
        try {
            Result res = dataTable.get(timestampGet);
            timestamp = GenericUtils.decodeLong(res.value());
        } catch (IOException e) {
            throw new AnalyticsException("The table " + tableName + " for tenant " + tenantId +
                    " could not be initialized for deletion: " + e.getMessage(), e);
        }
        return timestamp;
    }

    private class HBaseResultIterator implements Iterator<Record> {

        private static final int BATCH_SIZE = 1000;

        private boolean preFetched;
        private Record nextRecord;

        private Connection conn;

        private String tableName, currentId;
        private int tenantId, ids, currentIndex;
        private long timestamp;

        private List<String> recordIds, columns;

        private Table table;

        HBaseResultIterator(int tenantId, String tableName, List<String> recordIds, List<String> columns, Connection conn) throws AnalyticsException {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.recordIds = recordIds;
            this.columns = columns;
            this.conn = conn;
            this.ids = recordIds.size();
            this.currentId = null;

            try {
                this.table = this.conn.getTable(TableName.valueOf(HBaseUtils.generateAnalyticsTableName(tenantId, tableName)));
            } catch (IOException e) {
                throw new AnalyticsException("The table " + tableName + " for tenant " + tenantId +
                        " could not be initialized for reading: " + e.getMessage(), e);
            }
        }

        @Override
        public boolean hasNext() {
            if (!this.preFetched) {
                this.nextRecord = this.next();
                this.preFetched = true;
            }
            return nextRecord != null;
        }

        @Override
        public Record next() {
            if (this.preFetched) {
                this.preFetched = false;
                Record record = this.nextRecord;
                this.nextRecord = null;
                return record;
            }

            if (this.currentIndex < this.ids) {
                this.currentId = recordIds.get(this.currentIndex);
            } else {
                this.cleanup();
                return null;
            }

            Get get = new Get(Bytes.toBytes(this.currentId));
            for (String column : this.columns) {
                if (column != null && !(column.isEmpty())) {
                    get.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_COLUMN_FAMILY_NAME, column.getBytes());
                }
            }
            try {
                Result currentResult = this.table.get(get);
                Cell[] cells = currentResult.rawCells();
                Map<String, Object> values = null;
                if (cells.length > 0) {
                    this.timestamp = cells[0].getTimestamp();
                    values = HBaseUtils.decodeElementValue(cells);
                }
                this.currentIndex++;
                return new Record(this.tenantId, this.tableName, values, this.timestamp);
            } catch (Exception e) {
                this.cleanup();
                throw new HBaseRuntimeException("Error reading data from table " + this.tableName + " for tenant " + this.tenantId, e);
            }

        }

        private void cleanup() {
            try {
                this.table.close();
                this.conn.close();
            } catch (IOException ignore) {
                /* do nothing, the connection is dead anyway */
            }
        }
    }

    public class HBaseRuntimeException extends RuntimeException {
        HBaseRuntimeException(String s, Throwable t) {
            super(s, t);
        }
    }

}