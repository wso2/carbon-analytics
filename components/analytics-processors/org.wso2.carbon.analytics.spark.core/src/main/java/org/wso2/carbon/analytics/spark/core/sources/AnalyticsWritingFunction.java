/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import scala.collection.Iterator;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsWritingFunction extends AbstractFunction1<Iterator<Row>, BoxedUnit>
        implements Serializable {

    private static final long serialVersionUID = -1919222653470217466L;
    
    private static final Log log = LogFactory.getLog(AnalyticsWritingFunction.class);

    private int tId;
    
    private int recordBatchSize;
    
    private String tName;
    
    private StructType sch;

    private boolean globalTenantAccess;
    
    private String schemaString;
    
    private String primaryKeys;
    
    private boolean mergeFlag;
    
    private String recordStore;

    public AnalyticsWritingFunction(int tId, String tName, StructType sch, boolean globalTenantAccess, 
            String schemaString, String primaryKeys, boolean mergeFlag, String recordStore, int recordBatchSize) {
        this.tId = tId;
        this.recordBatchSize = recordBatchSize;
        this.tName = tName;
        this.sch = sch;
        this.globalTenantAccess = globalTenantAccess;
        this.schemaString = schemaString;
        this.primaryKeys = primaryKeys;
        this.mergeFlag = mergeFlag;
        this.recordStore = recordStore;
    }

    private void handleAnalyticsTableSchemaInvalidation() {
        AnalyticsDataService ads = ServiceHolder.getAnalyticsDataService();
        if (ads instanceof AnalyticsDataServiceImpl) {
            int targetTenantId;
            if (this.globalTenantAccess) {
                targetTenantId = Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID;
            } else {
                targetTenantId = this.tId;
            }
            ((AnalyticsDataServiceImpl) ads).invalidateAnalyticsTableInfo(targetTenantId, this.tName);
        }
    }
    
    /**
     * Apply the body of this function to the argument.
     *
     * @param iterator The data
     * @return The result of function application
     */
    @Override
    public BoxedUnit apply(Iterator<Row> iterator) {
        List<Row> rows = new ArrayList<>(recordBatchSize);
        /* we have to invalidate the table information, since here, if some other node
        changes the table information, we cannot know about it (no cluster communication) */
        this.handleAnalyticsTableSchemaInvalidation();
        while (iterator.hasNext()) {
            if (rows.size() < recordBatchSize) {
                Row row = iterator.next();
                rows.add(row);
                if (rows.size() == recordBatchSize) {
                    this.recordsPut(rows);
                    rows.clear();
                }
            }
        }
        if (!rows.isEmpty()) {
            this.recordsPut(rows);
        }
        return BoxedUnit.UNIT;
    }
    
    private void recordsPut(List<Row> rows) {
        if (this.globalTenantAccess) {
            this.recordsPutGlobal(rows);
        } else {
            this.recordsPutNormal(rows);
        }
    }
    
    private void recordsPutNormal(List<Row> rows) {
        List<Record> records = new ArrayList<>(rows.size());
        for (Row row : rows) {
            records.add(this.convertRowAndSchemaToRecord(row, this.sch, false));
        }
        AnalyticsDataService ads = ServiceHolder.getAnalyticsDataService();
        try {
            ads.put(records);
        } catch (AnalyticsException e) {
            String msg = "Error while inserting data into table " + this.tName + ": " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
    
    private void recordsPutGlobal(List<Row> rows) {
        List<Record> records = new ArrayList<>(rows.size());
        for (Row row : rows) {
            records.add(this.convertRowAndSchemaToRecord(row, this.sch, true));
        }
        AnalyticsDataService ads = ServiceHolder.getAnalyticsDataService();
        try {
            /* we've to create the separate tenant/table batches here to later create individual
             * tables, if they are not already created */
            for (List<Record> recordsBatch : GenericUtils.generateRecordBatches(records)) {
                try {
                    ads.put(recordsBatch);
                } catch (AnalyticsTableNotAvailableException e) {
                    Record firstRecord = recordsBatch.get(0);
                    this.createTargetTableAndSetSchema(ads, firstRecord.getTenantId(), firstRecord.getTableName());
                    ads.put(recordsBatch);
                }
            }
        } catch (AnalyticsException e) {
            String msg = "Error while inserting data into table " + this.tName + ": " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
    }
    
    protected void createTargetTableAndSetSchema(AnalyticsDataService ads, int targetTenantId, 
            String targetTableName) throws AnalyticsException {
        AnalyticsCommonUtils.createTableIfNotExists(ads, this.recordStore, targetTenantId, targetTableName);
        AnalyticsSchema schema = AnalyticsCommonUtils.createAnalyticsTableSchema(ads, targetTenantId, targetTableName, this.schemaString, 
                this.primaryKeys, this.globalTenantAccess, this.mergeFlag, false);
        ads.setTableSchema(targetTenantId, targetTableName, schema);
    }

    private Record convertRowAndSchemaToRecord(Row row, StructType schema, boolean global) {
        String[] colNames = schema.fieldNames();
        Map<String, Object> result = new HashMap<>();
        long timestamp = -1;
        int targetTenantId = this.tId;
        boolean globalTenantProcessed = false;
        for (int i = 0; i < row.length(); i++) {
            if (colNames[i].equals(AnalyticsConstants.TIMESTAMP_FIELD)) {
                timestamp = row.getLong(i);
            } else if (global && colNames[i].equals(AnalyticsConstants.TENANT_ID_FIELD)) {
                targetTenantId = row.getInt(i);
                globalTenantProcessed = true;
            } else {
                result.put(colNames[i], row.get(i));
            }
        }
        if (global && !globalTenantProcessed) {
            throw new RuntimeException("The field '" + AnalyticsConstants.TENANT_ID_FIELD + "' is not found in row: " + row + 
                    " with schema: " + schema + " when creating a global tenant access record");
        }
        if (timestamp < 0) { // timestamp has not being set
            return new Record(targetTenantId, this.tName, result);
        } else {
            return new Record(targetTenantId, this.tName, result, timestamp);
        }
    }
    
}
