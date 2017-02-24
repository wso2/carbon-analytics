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
package org.wso2.carbon.analytics.engine.commons;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.TaskContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.data.commons.AnalyticsDataService;
import org.wso2.carbon.analytics.data.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.data.commons.service.AnalyticsDataHolder;
import org.wso2.carbon.analytics.data.commons.sources.AnalyticsCommonConstants;
import org.wso2.carbon.analytics.data.commons.sources.Record;
import org.wso2.carbon.analytics.engine.exceptions.AnalyticsDataServiceLoadException;
import org.wso2.carbon.analytics.engine.services.AnalyticsServiceHolder;

import scala.collection.Iterator;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used by Spark to write to the DAL Layer.
 */
public class AnalyticsDALWriter extends AbstractFunction1<Iterator<Row>, BoxedUnit> implements Serializable {

    private static final long serialVersionUID = -1919222653470217466L;
    private static final Log log = LogFactory.getLog(AnalyticsDALWriter.class);

    private int recordBatchSize;
    private String tableName;
    private StructType sch;

    public AnalyticsDALWriter(String tableName, StructType sch, int recordBatchSize) {
        this.recordBatchSize = recordBatchSize;
        this.tableName = tableName;
        this.sch = sch;
    }

    private void handleAnalyticsTableSchemaInvalidation() {
        AnalyticsDataService ads;
        try {
            ads = AnalyticsServiceHolder.getAnalyticsDataService();
        } catch (AnalyticsDataServiceLoadException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        ads.invalidateTable(this.tableName);
    }

    /**
     * Apply the body of this function to the argument.
     *
     * @param iterator The data
     * @return The result of function application
     */
    @Override
    public BoxedUnit apply(Iterator<Row> iterator) {
        AnalyticsDataHolder.getInstance().setAnalyticsConfigsDir(TaskContext.get()
                .getLocalProperty(AnalyzerEngineConstants.SPARK_ANALYTICS_CONFIGS));
        List<Row> rows = new ArrayList<>(recordBatchSize);
        /* We have to invalidate the table information, since here, if some other node
        changes the table information, we cannot know about it (no cluster communication) */
        this.handleAnalyticsTableSchemaInvalidation();
        while (iterator.hasNext()) {
            if (rows.size() < recordBatchSize) {
                rows.add(iterator.next());
                if (rows.size() == recordBatchSize) {
                    putRecords(rows);
                    rows.clear();
                }
            }
        }
        if (!rows.isEmpty()) {
            putRecords(rows);
        }
        return BoxedUnit.UNIT;
    }

    private void putRecords(List<Row> rows) {
        List<Record> records = new ArrayList<>(rows.size());
        records.addAll(rows.stream().map(row -> this.convertToRecord(row, this.sch)).collect(Collectors.toList()));
        try {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            ads.put(records);
        } catch (AnalyticsException e) {
            String msg = "Error while inserting data into table " + this.tableName + ": " + e.getMessage();
            throw new RuntimeException(msg, e);
        } catch (AnalyticsDataServiceLoadException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private Record convertToRecord(Row row, StructType schema) {
        String[] columnNames = schema.fieldNames();
        Map<String, Object> result = new HashMap<>();
        long timestamp = -1;
        for (int i = 0; i < row.length(); i++) {
            if (columnNames[i].equals(AnalyticsCommonConstants.TIMESTAMP_FIELD)) {
                timestamp = row.getLong(i);
            } else {
                result.put(columnNames[i], row.get(i));
            }
        }
        if (timestamp < 0) { // timestamp has not being set
            return new Record(this.tableName, result);
        } else {
            return new Record(this.tableName, result, timestamp);
        }
    }
}

