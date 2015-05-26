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

package org.wso2.carbon.analytics.spark.core.util;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains the common methods used by the Analytics Spark Core
 */
public class AnalyticsCommonUtils {

    public static DataType stringToDataType(String strType) {
        switch (strType.toLowerCase()) {
            case AnalyticsConstants.INTEGER_TYPE:
                return DataTypes.IntegerType;
            case AnalyticsConstants.INT_TYPE:
                return DataTypes.IntegerType;
            case AnalyticsConstants.FLOAT_TYPE:
                return DataTypes.FloatType;
            case AnalyticsConstants.DOUBLE_TYPE:
                return DataTypes.DoubleType;
            case AnalyticsConstants.LONG_TYPE:
                return DataTypes.LongType;
            case AnalyticsConstants.BOOLEAN_TYPE:
                return DataTypes.BooleanType;
            case AnalyticsConstants.STRING_TYPE:
                return DataTypes.StringType;
            case AnalyticsConstants.BINARY_TYPE:
                return DataTypes.BinaryType;
            default:
                throw new RuntimeException("Invalid DataType: " + strType);
        }
    }

    public static AnalyticsSchema.ColumnType stringToColumnType(String strType) {
        switch (strType.toLowerCase()) {
            case AnalyticsConstants.INTEGER_TYPE:
                return AnalyticsSchema.ColumnType.INTEGER;
            case AnalyticsConstants.INT_TYPE:
                return AnalyticsSchema.ColumnType.INTEGER;
            case AnalyticsConstants.FLOAT_TYPE:
                return AnalyticsSchema.ColumnType.FLOAT;
            case AnalyticsConstants.DOUBLE_TYPE:
                return AnalyticsSchema.ColumnType.DOUBLE;
            case AnalyticsConstants.LONG_TYPE:
                return AnalyticsSchema.ColumnType.LONG;
            case AnalyticsConstants.BOOLEAN_TYPE:
                return AnalyticsSchema.ColumnType.BOOLEAN;
            case AnalyticsConstants.STRING_TYPE:
                return AnalyticsSchema.ColumnType.STRING;
            case AnalyticsConstants.BINARY_TYPE:
                return AnalyticsSchema.ColumnType.BINARY;
            default:
                throw new RuntimeException("Invalid ColumnType: " + strType);
        }
    }

    public static Map<String, Object> convertRowAndSchemaToValuesMap(Row row, StructType schema) {
        String[] colNames = schema.fieldNames();
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < row.length(); i++) {
            result.put(colNames[i], row.get(i));
        }
        return result;
    }

    public static List<Record> dataFrameToRecordsList(int tenantId, String tableName,
                                                      DataFrame dataFrame) {
        Row[] rows = dataFrame.collect();
        List<Record> records = new ArrayList<>();
        StructType schema = dataFrame.schema();
        for (Row row : rows) {
            records.add(new Record(tenantId, tableName, convertRowAndSchemaToValuesMap(row, schema)));
        }
        return records;
    }

    public static Boolean validateSchemaColumns(StructType sparkSchema,
                                                AnalyticsSchema analyticsSchema) {
        String[] rddCols = sparkSchema.fieldNames();
        Set<String> temp = analyticsSchema.getColumns().keySet();
        String[] tableCols = temp.toArray(new String[temp.size()]);

        return Arrays.equals(rddCols, tableCols);
    }

    public static String encodeTableNameWithTenantId(int tenantId, String tableName) {
        String tenantStr;
        String delimiter = "_";
        if (tenantId < 0) {
            tenantStr = "X" + String.valueOf(-tenantId);
        } else {
            tenantStr = "T" + String.valueOf(tenantId);
        }
        return tenantStr + delimiter + tableName;
    }
}
