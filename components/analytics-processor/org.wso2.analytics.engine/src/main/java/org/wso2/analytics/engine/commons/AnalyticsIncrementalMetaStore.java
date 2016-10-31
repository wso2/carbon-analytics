/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.analytics.engine.commons;

import org.wso2.analytics.dataservice.AnalyticsDataService;
import org.wso2.analytics.dataservice.commons.AnalyticsSchema;
import org.wso2.analytics.dataservice.commons.ColumnDefinition;
import org.wso2.analytics.dataservice.utils.AnalyticsCommonUtils;
import org.wso2.analytics.engine.exceptions.AnalyticsDataServiceLoadException;
import org.wso2.analytics.engine.services.AnalyticsServiceHolder;
import org.wso2.analytics.recordstore.commons.Record;
import org.wso2.analytics.recordstore.exception.AnalyticsException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * This class stores metadata required for incremental processing actions.
 */
public class AnalyticsIncrementalMetaStore {

    private static final String INC_META_TABLE = "__analytics_incremental_meta_table";
    private static final String COLUMN_TABLE_ID = "TABLE_ID";
    private static final String COLUMN_PRIMARY_VALUE = "PRIMARY_VAL";
    private static final String COLUMN_TEMP_VALUE = "TEMP_VAL";

    private AnalyticsDataService ads;

    public AnalyticsIncrementalMetaStore() throws AnalyticsException, AnalyticsDataServiceLoadException {
        this.ads = AnalyticsServiceHolder.getAnalyticsDataService();
        this.ads.createTable(INC_META_TABLE);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition(COLUMN_TABLE_ID, AnalyticsSchema.ColumnType.STRING));
        columns.add(new ColumnDefinition(COLUMN_PRIMARY_VALUE, AnalyticsSchema.ColumnType.LONG));
        columns.add(new ColumnDefinition(COLUMN_TEMP_VALUE, AnalyticsSchema.ColumnType.LONG));
        AnalyticsSchema schema = new AnalyticsSchema(columns, new ArrayList<>(Collections.singletonList(COLUMN_TABLE_ID)));
        this.ads.setTableSchema(INC_META_TABLE, schema);
    }

    public void setLastProcessedTimestamp(String id, long ts, boolean isPrimary) throws AnalyticsException {
        Map<String, Object> values = new HashMap<>();
        values.put(COLUMN_TABLE_ID, id);
        if (isPrimary) {
            values.put(COLUMN_PRIMARY_VALUE, ts);
        } else {
            values.put(COLUMN_PRIMARY_VALUE, this.getLastProcessedTimestamp(id, true));
            values.put(COLUMN_TEMP_VALUE, ts);
        }
        Record record = new Record(INC_META_TABLE, values);
        this.ads.put(new ArrayList<>(Collections.singletonList(record)));
    }

    public long getLastProcessedTimestamp(String id, boolean primaryValue) throws AnalyticsException {
        Map<String, Object> values = new HashMap<>();
        values.put(COLUMN_TABLE_ID, id);
        List<Record> result = AnalyticsCommonUtils.listRecords(
                this.ads, this.ads.getWithKeyValues(INC_META_TABLE, 1, null, new ArrayList<>(Collections.singletonList(values))));
        if (result.size() > 0) {
            Record record = result.get(0);
            Object obj;
            if (primaryValue) {
                obj = record.getValue(COLUMN_PRIMARY_VALUE);
            } else {
                obj = record.getValue(COLUMN_TEMP_VALUE);
            }
            if (obj != null) {
                return (Long) obj;
            } else {
                return Long.MIN_VALUE;
            }
        } else {
            return Long.MIN_VALUE;
        }
    }

    public void resetIncrementalTimestamps(String id) throws AnalyticsException {
        this.setLastProcessedTimestamp(id, Long.MIN_VALUE, true);
        this.setLastProcessedTimestamp(id, Long.MIN_VALUE, false);
    }
}
