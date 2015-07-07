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
import org.apache.hadoop.hbase.util.Pair;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.hbase.rg.HBaseIDRecordGroup;
import org.wso2.carbon.analytics.datasource.hbase.rg.HBaseRegionSplitRecordGroup;
import org.wso2.carbon.analytics.datasource.hbase.rg.HBaseTimestampRecordGroup;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseAnalyticsDSConstants;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseUtils;
import org.wso2.carbon.ndatasource.common.DataSourceException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Apache HBase implementation of {@link org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore}
 */
public class HBaseAnalyticsRecordStore implements AnalyticsRecordStore {

    private Connection conn;

    private HBaseAnalyticsConfigurationEntry queryConfig;

    private static final Log log = LogFactory.getLog(HBaseAnalyticsRecordStore.class);

    public HBaseAnalyticsRecordStore(Connection conn, HBaseAnalyticsConfigurationEntry entry) throws IOException, AnalyticsException {
        this.conn = conn;
        this.queryConfig = entry;
    }

    public HBaseAnalyticsRecordStore() {
        this.conn = null;
        this.queryConfig = null;
    }

    @Override
    public void init(Map<String, String> properties) throws AnalyticsException {
        this.queryConfig = HBaseUtils.lookupConfiguration();
        String dsName = properties.get(HBaseAnalyticsDSConstants.DATASOURCE_NAME);
        if (dsName == null) {
            throw new AnalyticsException("The property '" + HBaseAnalyticsDSConstants.DATASOURCE_NAME +
                    "' is required");
        }
        try {
            this.conn = (Connection) GenericUtils.loadGlobalDataSource(dsName);
        } catch (DataSourceException e) {
            throw new AnalyticsException("Error establishing connection to HBase instance based on data source" +
                    " definition: " + e.getMessage(), e);
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
                HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.TableType.DATA)));
        /* creating table with standard column family "carbon-analytics-data" for storing the actual row data
         * in one column and the record timestamp in another */
        dataDescriptor.addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME)
                .setMaxVersions(1));

        HTableDescriptor indexDescriptor = new HTableDescriptor(TableName.valueOf(
                HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.TableType.INDEX)));
        /* creating table with standard column family "carbon-analytics-index" for storing timestamp -> ID index*/
        indexDescriptor.addFamily(new HColumnDescriptor(HBaseAnalyticsDSConstants.ANALYTICS_INDEX_COLUMN_FAMILY_NAME)
                .setMaxVersions(1));

        /* Table creation should fail if index cannot be created, so attempting to create index table first. */
        Admin admin = null;
        try {
            admin = this.conn.getAdmin();
            admin.createTable(indexDescriptor);
            admin.createTable(dataDescriptor);
        } catch (IOException e) {
            throw new AnalyticsException("Error creating table " + tableName + " for tenant " + tenantId, e);
        } finally {
            GenericUtils.closeQuietly(admin);
        }
    }

    public boolean tableExists(int tenantId, String tableName) throws AnalyticsException {
        boolean isExist;
        Admin admin = null;
        try {
            admin = this.conn.getAdmin();
            isExist = admin.tableExists(TableName.valueOf(
                    HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.TableType.DATA)));
        } catch (IOException e) {
            throw new AnalyticsException("Error checking existence of table " + tableName + " for tenant " + tenantId, e);
        } finally {
            GenericUtils.closeQuietly(admin);
        }
        return isExist;
    }

    @Override
    public void deleteTable(int tenantId, String tableName) throws AnalyticsException {
        /* If the table we're proposing to create does not exist, return in silence */
        if (!(this.tableExists(tenantId, tableName))) {
            if (log.isDebugEnabled()) {
                log.debug("Deletion of table " + tableName + " for tenant " + tenantId +
                        " could not be carried out since said table did not exist.");
            }
            return;
        }
        Admin admin = null;
        TableName dataTable = TableName.valueOf(HBaseUtils.generateTableName(tenantId, tableName,
                HBaseAnalyticsDSConstants.TableType.DATA));
        TableName indexTable = TableName.valueOf(HBaseUtils.generateTableName(tenantId, tableName,
                HBaseAnalyticsDSConstants.TableType.INDEX));
        try {
            admin = this.conn.getAdmin();
            /* delete the data table first */
            admin.disableTable(dataTable);
            admin.deleteTable(dataTable);
            /* finally, delete the index table */
            admin.disableTable(indexTable);
            admin.deleteTable(indexTable);
        } catch (IOException e) {
            throw new AnalyticsException("Error deleting table " + tableName, e);
        } finally {
            GenericUtils.closeQuietly(admin);
        }
    }

    @Override
    public boolean isPaginationSupported() {
        /* Pagination & determination of record counts are not supported for this implementation. */
        return false;
    }

    @Override
    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        throw new HBaseUnsupportedOperationException("Retrieving row count is not supported " +
                "for HBase Analytics Record Stores");
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException, AnalyticsTableNotAvailableException {
        int tenantId = 0;
        String tableName = null;
        Table table, indexTable;
        if (records.isEmpty()) {
            return;
        }
        Map<String, List<Record>> recordBatches = this.generateRecordBatches(records);
        try {
            /* iterating over record batches */
            for (Map.Entry<String, List<Record>> entry : recordBatches.entrySet()) {
                tenantId = HBaseUtils.inferTenantId(entry.getKey());
                tableName = HBaseUtils.inferTableName(entry.getKey());
                table = this.conn.getTable(TableName.valueOf(HBaseUtils.generateTableName(tenantId, tableName,
                        HBaseAnalyticsDSConstants.TableType.DATA)));
                indexTable = this.conn.getTable(TableName.valueOf(HBaseUtils.generateTableName(tenantId, tableName,
                        HBaseAnalyticsDSConstants.TableType.INDEX)));
                /* Populating batched Put instances from records in a single batch */
                List<List<Put>> allPuts = this.populatePuts(recordBatches.get(entry.getKey()));
                /* Using Table.put(List<Put>) method to minimise network calls per table */
                try {
                    indexTable.put(allPuts.get(0));
                    table.put(allPuts.get(1));
                } finally {
                    table.close();
                    indexTable.close();
                }
            }
        } catch (IOException e) {
            if ((e instanceof RetriesExhaustedException) && e.getMessage().contains("was not found")) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            }
            throw new AnalyticsException("Error adding new records: " + e.getMessage(), e);
        }
    }

    private List<List<Put>> populatePuts(List<Record> records) throws AnalyticsException {
        byte[] data;
        List<Put> puts = new ArrayList<>();
        List<Put> indexPuts = new ArrayList<>();
        for (Record record : records) {
            String recordId = record.getId();
            long timestamp = record.getTimestamp();
            Map<String, Object> columns = record.getValues();
            if ((columns == null) || columns.isEmpty()) {
                data = new byte[]{};
            } else {
                data = GenericUtils.encodeRecordValues(columns);
            }
            Put put = new Put(recordId.getBytes(StandardCharsets.UTF_8));
            put.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                    HBaseAnalyticsDSConstants.ANALYTICS_ROWDATA_QUALIFIER_NAME, timestamp, data);
            put.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                    HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME, timestamp, HBaseUtils.encodeLong(timestamp));
            indexPuts.add(this.putIndexData(record));
            puts.add(put);
        }
        List<List<Put>> output = new ArrayList<>();
        output.add(indexPuts);
        output.add(puts);
        return output;
    }

    private Put putIndexData(Record record) {
        Put indexPut = new Put(HBaseUtils.encodeLong(record.getTimestamp()));
        /* Setting the column qualifier the same as the column value to enable multiple columns per row with
        * unique qualifiers, since we will anyway not use the qualifier during index read */
        indexPut.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_INDEX_COLUMN_FAMILY_NAME, record.getId().getBytes(StandardCharsets.UTF_8),
                record.getId().getBytes(StandardCharsets.UTF_8));
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
        return HBaseUtils.generateGenericTableName(record.getTenantId(), record.getTableName());
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                             long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException,
            AnalyticsTableNotAvailableException {
        if (recordsCount > 0 || recordsFrom > 0) {
            throw new HBaseUnsupportedOperationException("Pagination is not supported for HBase Analytics Record Stores");
        }
        if (!this.tableExists(tenantId, tableName)) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        if ((timeFrom == Long.MIN_VALUE) && (timeTo == Long.MAX_VALUE)) {
            return this.computeRegionSplits(tenantId, tableName, columns);
        } else {
            return new HBaseTimestampRecordGroup[]{
                    new HBaseTimestampRecordGroup(tenantId, tableName, columns, timeFrom, timeTo)
            };
        }
    }

    @Override
    public RecordGroup[] get(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
                             List<String> ids) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (!this.tableExists(tenantId, tableName)) {
            throw new AnalyticsTableNotAvailableException(tenantId, tableName);
        }
        return new HBaseIDRecordGroup[]{
                new HBaseIDRecordGroup(tenantId, tableName, columns, ids)
        };
    }

    @Override
    public AnalyticsIterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsException, AnalyticsTableNotAvailableException {
        if (recordGroup instanceof HBaseIDRecordGroup) {
            HBaseIDRecordGroup idRecordGroup = (HBaseIDRecordGroup) recordGroup;
            return this.getRecords(idRecordGroup.getTenantId(), idRecordGroup.getTableName(),
                    idRecordGroup.getColumns(), idRecordGroup.getIds());
        } else if (recordGroup instanceof HBaseTimestampRecordGroup) {
            HBaseTimestampRecordGroup tsRecordGroup = (HBaseTimestampRecordGroup) recordGroup;
            return this.getRecords(tsRecordGroup.getTenantId(), tsRecordGroup.getTableName(),
                    tsRecordGroup.getColumns(), tsRecordGroup.getStartTime(), tsRecordGroup.getEndTime());
        } else if (recordGroup instanceof HBaseRegionSplitRecordGroup) {
            HBaseRegionSplitRecordGroup rsRecordGroup = (HBaseRegionSplitRecordGroup) recordGroup;
            return this.getRecords(rsRecordGroup.getTenantId(), rsRecordGroup.getTableName(),
                    rsRecordGroup.getColumns(), rsRecordGroup.getStartRow(), rsRecordGroup.getEndRow());
        } else {
            throw new AnalyticsException("Invalid HBase RecordGroup implementation: " + recordGroup.getClass());
        }
    }

    public AnalyticsIterator<Record> getRecords(int tenantId, String tableName, List<String> columns, List<String> ids)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        int batchSize = this.queryConfig.getBatchSize();
        return new HBaseRecordIterator(tenantId, tableName, columns, ids, this.conn, batchSize);
    }

    public AnalyticsIterator<Record> getRecords(int tenantId, String tableName, List<String> columns, long startTime, long endTime)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        int batchSize = this.queryConfig.getBatchSize();
        return new HBaseTimestampIterator(tenantId, tableName, columns, startTime, endTime, this.conn, batchSize);
    }

    public AnalyticsIterator<Record> getRecords(int tenantId, String tableName, List<String> columns, byte[] startRow, byte[] endRow)
            throws AnalyticsException, AnalyticsTableNotAvailableException {
        return new HBaseRegionSplitIterator(tenantId, tableName, columns, this.conn, startRow, endRow);
    }

    private RecordGroup[] computeRegionSplits(int tenantId, String tableName, List<String> columns) throws AnalyticsException {
        List<RecordGroup> regionalGroups = new ArrayList<>();
        String formattedTableName = HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.TableType.DATA);
        try {
            RegionLocator locator = this.conn.getRegionLocator(TableName.valueOf(formattedTableName));
            final Pair<byte[][], byte[][]> startEndKeys = locator.getStartEndKeys();
            byte[][] startKeys = startEndKeys.getFirst();
            byte[][] endKeys = startEndKeys.getSecond();
            for (int i = 0; i < startKeys.length && i < endKeys.length; i++) {
                RecordGroup regionalGroup = new HBaseRegionSplitRecordGroup(tenantId, tableName, columns, startKeys[i],
                        endKeys[i], locator.getRegionLocation(startKeys[i]).getHostname());
                regionalGroups.add(regionalGroup);
            }
        } catch (IOException e) {
            throw new AnalyticsException("Error computing region splits for table " + tableName +
                    " for tenant " + tenantId);
        }
        return regionalGroups.toArray(new RecordGroup[regionalGroups.size()]);
    }

    private List<String> lookupIndex(int tenantId, String tableName, long startTime, long endTime)
            throws AnalyticsException {
        List<String> recordIds = new ArrayList<>();
        String formattedTableName = HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.TableType.INDEX);
        Table indexTable = null;
        Cell[] cells;
        Scan indexScan = new Scan();
        if (startTime != Long.MAX_VALUE) {
            indexScan.setStartRow(HBaseUtils.encodeLong(startTime));
        }
        if ((endTime != Long.MAX_VALUE)) {
            indexScan.setStopRow(HBaseUtils.encodeLong(endTime));
        }
        indexScan.addFamily(HBaseAnalyticsDSConstants.ANALYTICS_INDEX_COLUMN_FAMILY_NAME);
        ResultScanner resultScanner = null;
        try {
            indexTable = this.conn.getTable(TableName.valueOf(formattedTableName));
            resultScanner = indexTable.getScanner(indexScan);
            for (Result rowResult : resultScanner) {
                cells = rowResult.rawCells();
                for (Cell cell : cells) {
                    recordIds.add(new String(CellUtil.cloneValue(cell), StandardCharsets.UTF_8));
                }
            }
        } catch (IOException e) {
            throw new AnalyticsException("Index for table " + tableName + " could not be read", e);
        } finally {
            GenericUtils.closeQuietly(resultScanner);
            GenericUtils.closeQuietly(indexTable);
        }
        return recordIds;
    }

    @Override
    public void delete(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsException {
        this.delete(tenantId, tableName, this.lookupIndex(tenantId, tableName, timeFrom, timeTo));
    }

    @Override
    public void delete(int tenantId, String tableName, List<String> ids) throws AnalyticsException {
        Table dataTable = null;
        List<Delete> dataDeletes = new ArrayList<>();
        String dataTableName = HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.TableType.DATA);
        List<Long> timestamps = this.lookupTimestamps(dataTableName, ids, tenantId, tableName);
        for (String recordId : ids) {
            dataDeletes.add(new Delete(recordId.getBytes(StandardCharsets.UTF_8)));
        }
        try {
            dataTable = this.conn.getTable(TableName.valueOf(dataTableName));
            dataTable.delete(dataDeletes);
            //TODO: HBase bug in delete propagation. WORKAROUND BELOW
/*            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        } catch (IOException e) {
            throw new AnalyticsException("Error deleting records from " + tableName + " for tenant " + tenantId + " : "
                    + e.getMessage(), e);
        } finally {
            GenericUtils.closeQuietly(dataTable);
        }
        this.deleteIndexEntries(tenantId, tableName, timestamps);
    }

    @Override
    public void destroy() throws AnalyticsException {
        try {
            this.conn.close();
            log.debug("Closed HBase connection transients successfully.");
        } catch (IOException ignore) {
                /* do nothing, the connection is dead anyway */
        }
    }

    private void deleteIndexEntries(int tenantId, String tableName, List<Long> timestamps) throws AnalyticsException {
        Table indexTable;
        List<Delete> indexDeletes = new ArrayList<>();
        String indexTableName = HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.TableType.INDEX);
        for (Long timestamp : timestamps) {
            indexDeletes.add(new Delete(HBaseUtils.encodeLong(timestamp)));
        }
        try {
            indexTable = this.conn.getTable(TableName.valueOf(indexTableName));
            indexTable.delete(indexDeletes);
            indexTable.close();
        } catch (IOException e) {
            throw new AnalyticsException("Error deleting record indices from " + tableName + " for tenant " + tenantId +
                    " : " + e.getMessage(), e);
        }
    }

    private List<Long> lookupTimestamps(String dataTableName, List<String> rowIds, int tenantId, String tableName)
            throws AnalyticsException {
        List<Long> timestamps = new ArrayList<>();
        List<Get> gets = new ArrayList<>();
        Table dataTable = null;
        for (String rowId : rowIds) {
            if (!rowId.isEmpty()) {
                gets.add(new Get(rowId.getBytes(StandardCharsets.UTF_8)).addColumn(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                        HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME));
            }
        }
        try {
            dataTable = this.conn.getTable(TableName.valueOf(dataTableName));
            Result[] results = dataTable.get(gets);
            for (Result res : results) {
                if ((res != null) && (res.value() != null) && (res.value().length != 0)) {
                    timestamps.add(HBaseUtils.decodeLong(res.value()));
                }
            }

        } catch (IOException e) {
            throw new AnalyticsException("The table " + tableName + " for tenant " + tenantId +
                    " could not be initialized for deletion of rows: " + e.getMessage(), e);
        } finally {
            GenericUtils.closeQuietly(dataTable);
        }
        return timestamps;
    }

    public static class HBaseUnsupportedOperationException extends AnalyticsException {

        private static final long serialVersionUID = -380641886204128313L;

        public HBaseUnsupportedOperationException(String s) {
            super(s);
        }
    }

}