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
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseAnalyticsDSConstants;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseRuntimeException;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseUtils;

import java.io.IOException;
import java.util.*;

public class HBaseRegionSplitIterator implements Iterator<Record> {

    byte[] startRow, endRow;

    private int tenantId;

    private String tableName;
    private Table table;
    private Iterator<Result> resultIterator;


    public HBaseRegionSplitIterator(int tenantId, String tableName, List<String> columns, Connection conn,
                                    byte[] startRow, byte[] endRow) throws AnalyticsException {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.startRow = startRow;
        this.endRow = endRow;
        try {
            this.table = conn.getTable(TableName.valueOf(
                    HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.DATA)));
        } catch (IOException e) {
            throw new AnalyticsException("The table " + tableName + " for tenant " + tenantId +
                    " could not be initialized for reading: " + e.getMessage(), e);
        }
        Scan splitScan = new Scan();
        splitScan.setStartRow(this.startRow);
        splitScan.setStopRow(this.endRow);
        if (columns != null) {
            for (String column : columns) {
                if (column != null && !(column.isEmpty())) {
                    splitScan.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                            HBaseUtils.generateColumnQualifier(column));
                }
            }
        } else {
            splitScan.addFamily(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME);
        }
        try {
            ResultScanner scanner = table.getScanner(splitScan);
            this.resultIterator = scanner.iterator();
        } catch (IOException e) {
            throw new AnalyticsException("The table " + tableName + " for tenant " + tenantId +
                    " could not be read: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean hasNext() {
        return this.resultIterator.hasNext();
    }

    @Override
    public Record next() {
        if (this.hasNext()) {
            try {
                Result currentResult = this.resultIterator.next();
                Cell[] cells = currentResult.rawCells();
                byte[] rowId = currentResult.getRow();
                Map<String, Object> values;
                long timestamp;
                if (cells.length > 0) {
                    values = HBaseUtils.decodeElementValue(cells);
                    timestamp = cells[0].getTimestamp();
                    values.remove(HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME);
                } else {
                    Get get = new Get(rowId);
                    get.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_META_COLUMN_FAMILY_NAME,
                            HBaseAnalyticsDSConstants.ANALYTICS_TS_QUALIFIER_NAME);
                    Result timestampResult = this.table.get(get);
                    timestamp = HBaseUtils.decodeLong(timestampResult.value());
                    values = new HashMap<>();
                }
                return new Record(new String(rowId), this.tenantId, this.tableName, values, timestamp);
            } catch (Exception e) {
                this.cleanup();
                throw new HBaseRuntimeException("Error reading data from table " + this.tableName + " for tenant " +
                        this.tenantId, e);
            }
        } else {
            this.cleanup();
            throw new NoSuchElementException("No further elements exist in iterator");
        }
    }

    @Override
    public void remove() {
            /* nothing to do here, since this is a read-only iterator */
    }

    private void cleanup() {
        try {
            this.table.close();
        } catch (IOException ignore) {
            /* do nothing, the connection is dead anyway */
        }
    }

}
