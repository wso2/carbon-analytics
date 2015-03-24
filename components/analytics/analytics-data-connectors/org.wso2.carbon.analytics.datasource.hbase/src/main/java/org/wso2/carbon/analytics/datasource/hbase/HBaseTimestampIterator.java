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

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseAnalyticsDSConstants;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseRuntimeException;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseUtils;

import java.io.IOException;
import java.util.*;

public class HBaseTimestampIterator implements Iterator<Record> {

    private List<String> columns;

    private int tenantId, batchSize;

    private byte[] latestRow, endRow;
    private static final long POSTFIX = 1L;

    private boolean fullyFetched;
    private String tableName;
    private Table table, indexTable;
    private Iterator<Record> subIterator = Collections.emptyIterator();

    HBaseTimestampIterator(int tenantId, String tableName, List<String> columns, long timeFrom, long timeTo,
                           Connection conn, int batchSize) throws AnalyticsException {
        if ((timeFrom > timeTo) || (batchSize < 0)) {
            throw new AnalyticsException("Invalid parameters specified for reading data from table " + tableName +
                    " for tenant " + tenantId);
        } else {
            this.init(conn, tenantId, tableName, columns, batchSize);
            /* setting the initial row to start time -1 because it will soon be incremented by 1L. */
            this.latestRow = HBaseUtils.encodeLong(timeFrom - POSTFIX);
            this.endRow = HBaseUtils.encodeLong(timeTo);
            /* pre-fetching from HBase and populating records for the first time */
            this.preFetch();
        }
    }

    @Override
    public boolean hasNext() {
        boolean hasMore = this.subIterator.hasNext();
        if (!hasMore) {
            this.preFetch();
        }
        return this.subIterator.hasNext();
    }

    @Override
    public Record next() {
        if (this.hasNext()) {
            return this.subIterator.next();
        } else {
            this.cleanup();
            throw new NoSuchElementException("No further elements exist in iterator");
        }
    }

    @Override
    public void remove() {
        /* nothing to do here, since this is a read-only iterator */
    }

    private void preFetch() {
        if (this.fullyFetched) {
            return;
        }
        List<String> currentBatch = this.populateRecordBatches();
        if (currentBatch.size() == 0) {
            return;
        }
        Set<String> colSet = null;
        List<Record> fetchedRecords = new ArrayList<>();
        List<Get> gets = new ArrayList<>();

        for (String currentId : currentBatch) {
            Get get = new Get(Bytes.toBytes(currentId));

            /* if the list of columns to be retrieved is null, retrieve ALL columns. */
            if (this.columns != null && this.columns.size() > 0) {
                colSet = new HashSet<>(this.columns);
                get.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                        HBaseAnalyticsDSConstants.ANALYTICS_ROWDATA_QUALIFIER_NAME);
            } else {
                get.addFamily(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME);
            }
            gets.add(get);
        }

        try {
            Result[] results = this.table.get(gets);
            for (Result currentResult : results) {
                Cell[] cells = currentResult.rawCells();
                byte[] rowId = currentResult.getRow();
                Map<String, Object> values = GenericUtils.decodeRecordValues(CellUtil.cloneValue(cells[0]), colSet);
                long timestamp = cells[0].getTimestamp();
                fetchedRecords.add(new Record(new String(rowId), this.tenantId, this.tableName, values, timestamp));
            }
            this.subIterator = fetchedRecords.iterator();
        } catch (Exception e) {
            //this.cleanup();
            throw new HBaseRuntimeException("Error reading data from table " + this.tableName + " for tenant " +
                    this.tenantId, e);
        }
    }

    private List<String> populateRecordBatches() {
        List<String> currentBatch = new ArrayList<>();
        int counter = 0;
        Scan indexScan = new Scan();
        long latestTime = HBaseUtils.decodeLong(this.latestRow);
        indexScan.setStartRow(HBaseUtils.encodeLong(latestTime + POSTFIX));
        indexScan.setStopRow(this.endRow);
        indexScan.addFamily(HBaseAnalyticsDSConstants.ANALYTICS_INDEX_COLUMN_FAMILY_NAME);
        ResultScanner resultScanner;
        try {
            resultScanner = indexTable.getScanner(indexScan);
            for (Result rowResult : resultScanner) {
                if (counter == this.batchSize) {
                    break;
                }
                Cell[] cells = rowResult.rawCells();
                for (Cell cell : cells) {
                    currentBatch.add(Bytes.toString(CellUtil.cloneValue(cell)));
                }
                this.latestRow = rowResult.getRow();
                counter++;
            }
            resultScanner.close();
            indexTable.close();
        } catch (IOException e) {
            throw new HBaseRuntimeException("Error reading index data for table " + this.tableName + ", tenant " +
                    this.tenantId, e);
        }
        if (counter == 0) {
            this.fullyFetched = true;
        }
        return currentBatch;
    }

    private void init(Connection conn, int tenantId, String tableName, List<String> columns, int batchSize) throws AnalyticsException {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.columns = columns;
        this.batchSize = batchSize;
        try {
            this.indexTable = conn.getTable(TableName.valueOf(
                    HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.INDEX)));
            this.table = conn.getTable(TableName.valueOf(
                    HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.DATA)));
        } catch (IOException e) {
            throw new AnalyticsException("The table " + tableName + " for tenant " + tenantId +
                    " could not be initialized for reading: " + e.getMessage(), e);
        }
    }

    private void cleanup() {
        try {
            this.table.close();
        } catch (IOException ignore) {
            /* do nothing, the connection is dead anyway */
        }
    }
}
