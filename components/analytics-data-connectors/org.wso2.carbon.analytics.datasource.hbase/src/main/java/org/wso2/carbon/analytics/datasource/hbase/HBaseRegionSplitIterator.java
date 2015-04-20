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

import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseAnalyticsDSConstants;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseRuntimeException;
import org.wso2.carbon.analytics.datasource.hbase.util.HBaseUtils;

import java.io.IOException;
import java.util.*;

/**
 * Subclass of java.util.Iterator used for streaming records contained within an HBase region boundary given the start
 * and end row keys of the region
 */
public class HBaseRegionSplitIterator implements Iterator<Record> {

    byte[] startRow, endRow;

    private int tenantId;

    private String tableName;
    private Table table;
    private Iterator<Result> resultIterator = Collections.emptyIterator();

    Set<String> colSet = null;

    public HBaseRegionSplitIterator(int tenantId, String tableName, List<String> columns, Connection conn,
                                    byte[] startRow, byte[] endRow) throws AnalyticsException, AnalyticsTableNotAvailableException {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.startRow = startRow;
        this.endRow = endRow;
        Admin admin = null;
        TableName finalName = TableName.valueOf(
                HBaseUtils.generateTableName(tenantId, tableName, HBaseAnalyticsDSConstants.TableType.DATA));
        try {
            admin = conn.getAdmin();
            if (!admin.tableExists(finalName)) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            }
            this.table = conn.getTable(finalName);
        } catch (IOException e) {
            throw new AnalyticsException("The table " + tableName + " for tenant " + tenantId +
                    " could not be initialized for reading: " + e.getMessage(), e);
        } finally {
            GenericUtils.closeQuietly(admin);
        }
        Scan splitScan = new Scan();
        splitScan.setStartRow(this.startRow);
        splitScan.setStopRow(this.endRow);

        if (columns != null && columns.size() > 0) {
            this.colSet = new HashSet<>(columns);
            splitScan.addColumn(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME,
                    HBaseAnalyticsDSConstants.ANALYTICS_ROWDATA_QUALIFIER_NAME);
        } else {
            splitScan.addFamily(HBaseAnalyticsDSConstants.ANALYTICS_DATA_COLUMN_FAMILY_NAME);
        }
        try {
            ResultScanner scanner = table.getScanner(splitScan);
            this.resultIterator = scanner.iterator();
        } catch (IOException e) {
            if (e instanceof RetriesExhaustedException) {
                throw new AnalyticsTableNotAvailableException(tenantId, tableName);
            }
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
        if (!this.hasNext()) {
            this.cleanup();
        }
        try {
            Result currentResult = this.resultIterator.next();
            byte[] rowId = currentResult.getRow();
            Record record = HBaseUtils.constructRecord(currentResult, tenantId, tableName, colSet);
            if (record != null) {
                return record;
            } else {
                throw new HBaseRuntimeException("Invalid data found on row " + new String(rowId));
            }
        } catch (AnalyticsException e) {
            this.cleanup();
            throw new HBaseRuntimeException("Error reading data from table " + this.tableName + " for tenant " +
                    this.tenantId, e);
        }
    }

    @Override
    public void remove() {
            /* nothing to do here, since this is a read-only iterator */
    }

    private void cleanup() {
        GenericUtils.closeQuietly(this.table);
    }

}
