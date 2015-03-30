/*
 * *
 *  * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.wso2.carbon.analytics.dashboard.batch.admin;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dashboard.batch.admin.data.Cell;
import org.wso2.carbon.analytics.dashboard.batch.admin.data.Column;
import org.wso2.carbon.analytics.dashboard.batch.admin.data.Row;
import org.wso2.carbon.analytics.dashboard.batch.admin.data.Table;
import org.wso2.carbon.analytics.dashboard.batch.admin.internal.ds.BatchAnalyticsDashboardAdminValueHolder;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Admin service that responds to data requests originated from dashboard frontend.
 * Basically this service returns records for a given table, table schema and list of table names.
 */
public class BatchAnalyticsDashboardAdminService extends AbstractAdmin {
    private static final String DATAVIEWS_DIR = "/repository/components/org.wso2.carbon.analytics.dataviews/";
    private static final String DASHBOARDS_DIR = "/repository/components/org.wso2.carbon.analytics.dashboards/";

    private SecureAnalyticsDataService analyticsDataService;
    private Log logger = LogFactory.getLog(BatchAnalyticsDashboardAdminService.class);

    public BatchAnalyticsDashboardAdminService() {
        this.analyticsDataService = BatchAnalyticsDashboardAdminValueHolder.getAnalyticsDataService();
    }

    /**
     * Returns a list of analytic tables names.
     *
     * @return An array of strings.
     * @throws AxisFault If something happened during operation.
     */
    public String[] getTableNames() throws AxisFault {
        List<String> tables = null;
        try {
            tables = analyticsDataService.listTables(getUsername());
        } catch (AnalyticsException e) {
            logger.error("Unable to get a list of tables from Analytics data layer for tenant: " + getUsername(), e);
            throw new AxisFault("Unable to get a list of tables from Analytics data layer for tenant: "
                    + getUsername(), e);
        }
        return tables.toArray(new String[tables.size()]);
    }

    /**
     * Returns the schema for a given analytic table.
     *
     * @param tableName Name of the analytics table.
     * @return An array of Column objects. Column object consists of a name and type.
     * @throws AxisFault
     */
    public Column[] getTableSchema(String tableName) throws AxisFault {
        try {
            AnalyticsSchema schema = analyticsDataService.getTableSchema(getUsername(), tableName);
            Map<String, AnalyticsSchema.ColumnType> colMap = schema.getColumns();
            if (colMap != null) {
                Column[] columns = new Column[colMap.size()];
                int i = 0;
                for (Map.Entry<String, AnalyticsSchema.ColumnType> entry : colMap.entrySet()) {
                    columns[i++] = new Column(entry.getKey(), String.valueOf(entry.getValue()));
                }
                return columns;
            } else {
                return new Column[0];
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to get the schema for table: " + tableName +
                    "from Analytics data layer for tenant: " + getUsername(), e);
            throw new AxisFault("Unable to get the schema for table: " + tableName +
                    "from Analytics data layer for tenant: " + getUsername(), e);
        }
    }

    /**
     * Returns a collection of records from an analytic table.
     *
     * @param tableName   name of the analytics table data to be read from.
     * @param timeFrom    starting timestamp.
     * @param timeTo      end timestamp.
     * @param startIndex  starting index. Specially when paginating.
     * @param recordCount how many records required? defaults to all.
     * @param searchQuery optional lucene query.
     * @return A Table object filled with records.
     * @throws AxisFault
     */
    //TODO implement pagination based reading, facets
    public Table getRecords(String tableName, long timeFrom, long timeTo, int startIndex, int recordCount,
                            String searchQuery)
            throws AxisFault {
        //todo use String.format() to prevent scttering fo log lines
        if (logger.isDebugEnabled()) {
            logger.debug("Search Query: " + searchQuery);
            logger.debug("timeFrom: " + timeFrom);
            logger.debug("timeTo: " + timeTo);
            logger.debug("Start Index: " + startIndex);
            logger.debug("Page Size: " + recordCount);
        }
        String username = getUsername();
        Table table = new Table();
        table.setName(tableName);
        RecordGroup[] results = new RecordGroup[0];
        long searchCount = 0;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            //TODO implement me :(  facets FTW!
        } else {
            try {
                results = analyticsDataService
                        .get(username, tableName, 1, null, timeFrom, timeTo, startIndex, recordCount);
            } catch (Exception e) {
                logger.error("Unable to get records from Analytics data layer for tenant: " + username +
                        " and for table:" + tableName, e);
                throw new AxisFault("Unable to get records from Analytics data layer for tenant: " + username +
                        " and for table:" + tableName, e);
            }
        }
        if (results != null) {
            List<Record> records;
            List<Row> rowList = new ArrayList<Row>();
            try {
                records = GenericUtils.listRecords(analyticsDataService, results);
            } catch (Exception e) {
                logger.error("Unable to convert result to record for tenant: " + username +
                        " and for table:" + tableName, e);
                throw new AxisFault("Unable to convert result to record for tenant: " + username +
                        " and for table:" + tableName, e);
            }
            if (records != null && !records.isEmpty()) {
                for (Record record : records) {
                    rowList.add(createRow(record));
                }
            }
            Row[] rows = new Row[rowList.size()];
            rowList.toArray(rows);
            table.setRows(rows);
        }
        return table;
    }

    @Override
    protected String getUsername() {
        return super.getUsername() + "@" + getTenantDomain();
    }

    /**
     * Returns a Row object from a Record.
     *
     * @param record Record object returned from Analytics data service.
     * @return
     */
    private Row createRow(Record record) {
        Row row = new Row();
        Cell[] cells = new Cell[record.getValues().size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : record.getValues().entrySet()) {
            cells[i++] = createCell(entry.getKey(), entry.getValue());
        }
        row.setCells(cells);
        return row;
    }

    private Cell createCell(String key, Object value) {
        Cell cell = new Cell(key, String.valueOf(value));
        return cell;
    }
}
