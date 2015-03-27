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
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.analytics.dashboard.batch.admin.data.Cell;
import org.wso2.carbon.analytics.dashboard.batch.admin.data.Row;
import org.wso2.carbon.analytics.dashboard.batch.admin.data.Table;
import org.wso2.carbon.analytics.dashboard.batch.admin.internal.ds.BatchAnalyticsDashboardAdminValueHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BatchAnalyticsDashboardAdminService extends AbstractAdmin {


    /**
     * Relative Registry locations for dataViews and dashboards.
     */
    private static final String DATAVIEWS_DIR =
            "/repository/components/org.wso2.carbon.analytics.dataviews/";
    private static final String DASHBOARDS_DIR =
            "/repository/components/org.wso2.carbon.analytics.dashboards/";
    private SecureAnalyticsDataService analyticsDataService;
    /**
     * Logger
     */
    private Log logger = LogFactory.getLog(BatchAnalyticsDashboardAdminService.class);

    public BatchAnalyticsDashboardAdminService() {
        this.analyticsDataService = BatchAnalyticsDashboardAdminValueHolder.getAnalyticsDataService();
    }


    public Table getRecords(String tableName, long timeFrom, long timeTo, int startIndex, int recordCount,
            String searchQuery)
            throws AxisFault {
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
            //TODO implement me :(
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
