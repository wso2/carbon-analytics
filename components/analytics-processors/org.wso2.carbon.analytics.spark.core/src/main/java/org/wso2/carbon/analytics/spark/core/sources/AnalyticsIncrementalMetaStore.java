/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.sources;

import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class stores metadata required for incremental processing actions.
 */
public class AnalyticsIncrementalMetaStore {

    private static final String INC_META_TABLE = "__analytics_incremental_meta_table";

    private static final String COLUMN_TENANT_ID = "TENANT_ID";

    private static final String COLUMN_TABLE_ID = "TABLE_ID";

    private static final String COLUMN_PRIMARY_VALUE = "PRIMARY_VAL";

    private static final String COLUMN_TEMP_VALUE = "TEMP_VAL";

    private AnalyticsDataService ads;

    public AnalyticsIncrementalMetaStore() throws AnalyticsException {
        this.ads = ServiceHolder.getAnalyticsDataService();
        this.ads.createTable(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, INC_META_TABLE);
        List<ColumnDefinition> columns = new ArrayList<>();
        columns.add(new ColumnDefinition(COLUMN_TENANT_ID, AnalyticsSchema.ColumnType.INTEGER));
        columns.add(new ColumnDefinition(COLUMN_TABLE_ID, AnalyticsSchema.ColumnType.STRING));
        columns.add(new ColumnDefinition(COLUMN_PRIMARY_VALUE, AnalyticsSchema.ColumnType.LONG));
        columns.add(new ColumnDefinition(COLUMN_TEMP_VALUE, AnalyticsSchema.ColumnType.LONG));
        AnalyticsSchema schema = new AnalyticsSchema(columns, new ArrayList<>(Arrays.asList(COLUMN_TENANT_ID, COLUMN_TABLE_ID)));
        this.ads.setTableSchema(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, INC_META_TABLE, schema);
    }

    public void setLastProcessedTimestamp(int tenantId, String id, long ts, boolean primary) throws AnalyticsException {
        Map<String, Object> values = new HashMap<>();
        values.put(COLUMN_TENANT_ID, tenantId);
        values.put(COLUMN_TABLE_ID, id);
        if (primary) {
            values.put(COLUMN_PRIMARY_VALUE, ts);
        } else {
            values.put(COLUMN_PRIMARY_VALUE, this.getLastProcessedTimestamp(tenantId, id, true));
            values.put(COLUMN_TEMP_VALUE, ts);
        }
        Record record = new Record(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID, INC_META_TABLE, values);
        this.ads.put(new ArrayList<>(Arrays.asList(record)));
    }

    public long getLastProcessedTimestamp(int tenantId, String id, boolean primary) throws AnalyticsException {
        Map<String, Object> values = new HashMap<>();
        values.put(COLUMN_TENANT_ID, tenantId);
        values.put(COLUMN_TABLE_ID, id);
        List<Record> result = AnalyticsDataServiceUtils.listRecords(
                this.ads, this.ads.getWithKeyValues(AnalyticsConstants.SPARK_PERSISTENCE_TENANT_ID,
                                                    INC_META_TABLE, 1, null, new ArrayList<>(Arrays.asList(values))));
        if (result.size() > 0) {
            Record record = result.get(0);
            Object obj;
            if (primary) {
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

    public void resetIncrementalTimestamps(int tenantId, String id) throws AnalyticsException {
        this.setLastProcessedTimestamp(tenantId, id, Long.MIN_VALUE, true);
        this.setLastProcessedTimestamp(tenantId, id, Long.MIN_VALUE, false);
    }
}


