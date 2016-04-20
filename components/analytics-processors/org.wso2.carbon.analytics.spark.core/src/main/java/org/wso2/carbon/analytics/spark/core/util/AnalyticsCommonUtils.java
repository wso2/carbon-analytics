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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsUDFException;
import scala.collection.Iterator;

import javax.lang.model.type.NullType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains the common methods used by the Analytics Spark Core
 */
public class AnalyticsCommonUtils {
    private static final Log log = LogFactory.getLog(AnalyticsCommonUtils.class);

    public static DataType getDataType(Type returnType) throws AnalyticsUDFException {
        DataType udfReturnType = null;
        if (returnType == Integer.TYPE || returnType == Integer.class) {
            udfReturnType = DataTypes.IntegerType;
        } else if (returnType == Double.TYPE || returnType == Double.class) {
            udfReturnType = DataTypes.DoubleType;
        } else if (returnType == Float.TYPE || returnType == Float.class) {
            udfReturnType = DataTypes.FloatType;
        } else if (returnType == Long.TYPE || returnType == Long.class) {
            udfReturnType = DataTypes.LongType;
        } else if (returnType == Boolean.TYPE || returnType == Boolean.class) {
            udfReturnType = DataTypes.BooleanType;
        } else if (returnType == String.class) {
            udfReturnType = DataTypes.StringType;
        } else if (returnType == Short.TYPE || returnType == Short.class) {
            udfReturnType = DataTypes.ShortType;
        } else if (returnType == NullType.class) {
            udfReturnType = DataTypes.NullType;
        } else if (returnType == Byte.TYPE || returnType == Byte.class) {
            udfReturnType = DataTypes.ByteType;
        } else if (returnType == byte[].class || returnType == Byte[].class) {
            udfReturnType = DataTypes.BinaryType;
        } else if (returnType == Date.class) {
            udfReturnType = DataTypes.DateType;
        } else if (returnType == Timestamp.class) {
            udfReturnType = DataTypes.TimestampType;
        } else if (returnType == BigDecimal.class) {
            udfReturnType = DataTypes.createDecimalType();
        } else if (returnType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) returnType;
            /*if return type is a List types will contain only 1 element, if return type is Map it will have
            2 elements types representing key and the value.*/
            Type[] types = type.getActualTypeArguments();
            if (types != null && types.length > 0) {
                switch (types.length) {
                    case 1: {
                        udfReturnType = DataTypes.createArrayType(getDataType(types[0]));
                        break;
                    }
                    case 2: {
                        udfReturnType = DataTypes.createMapType(getDataType(types[0]), getDataType(types[1]));
                        break;
                    }
                    default:
                        throw new AnalyticsUDFException("Cannot Map the return type either to ArrayType or MapType");
                }
            }
        } else {
            throw new AnalyticsUDFException("Cannot determine the return DataType: " + returnType.toString());
        }
        return udfReturnType;
    }

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
            case AnalyticsConstants.FACET_TYPE:
                return DataTypes.StringType;
            default:
                log.error("Invalid DataType: " + strType);
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
            case AnalyticsConstants.FACET_TYPE:
                return AnalyticsSchema.ColumnType.STRING;
            default:
                log.error("Invalid ColumnType: " + strType);
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

    public static String convertStreamNameToTableName(String stream) {
        return stream.replaceAll("\\.", "_");
    }

    public static boolean isNumericType(AnalyticsSchema.ColumnType colType) {
        return !(colType.name().equalsIgnoreCase(AnalyticsConstants.STRING_TYPE)
                 || colType.name().equalsIgnoreCase(AnalyticsConstants.BINARY_TYPE)
                 || colType.name().equalsIgnoreCase(AnalyticsConstants.FACET_TYPE));
    }

    public static boolean isEmptyAnalyticsSchema(AnalyticsSchema analyticsSchema) {
        return analyticsSchema == null || analyticsSchema.getColumns() == null;
    }

    public static boolean isEmptySchema(StructType schema) {
        return schema == null || schema.fieldNames() == null;
    }

    public static StructField[] extractFieldsFromColumns(Map<String, ColumnDefinition> columns) {
        StructField[] resFields = new StructField[columns.size()];

        int i = 0;
        for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
            String type = entry.getValue().getType().name();
            resFields[i] = new StructField(entry.getKey(), AnalyticsCommonUtils.stringToDataType(type),
                                           true, Metadata.empty());
            i++;
        }
        return resFields;
    }

    public static StructField[] extractFieldsFromString(String schemaString) {
        String[] strFields = schemaString.split(",");
        StructField[] resFields = new StructField[(strFields.length)];
        String name, type;
        String[] strFieldTokens;
        for (int i = 0; i < strFields.length; i++) {
            strFieldTokens = strFields[i].trim().split(" ");
            name = strFieldTokens[0].trim();
            type = strFieldTokens[1].trim().toLowerCase();
            StructField field = new StructField(name, AnalyticsCommonUtils.stringToDataType(type),
                                                true, Metadata.empty());
            resFields[i] = field;
        }
        return resFields;
    }

    public static AnalyticsSchema analyticsSchemaFromStructType(StructType schema) {
        List<ColumnDefinition> colDefs = new ArrayList<>();
        Iterator<StructField> fieldIter = schema.iterator();
        while (fieldIter.hasNext()) {
            StructField field = fieldIter.next();
            String name = field.name();
            AnalyticsSchema.ColumnType type = AnalyticsCommonUtils.stringToColumnType(field.dataType().typeName());
            colDefs.add(new ColumnDefinition(name, type));
        }

        return new AnalyticsSchema(colDefs, Collections.<String>emptyList());
    }

    public static StructType structTypeFromAnalyticsSchema(AnalyticsSchema analyticsSchema){
        return new StructType(extractFieldsFromColumns(analyticsSchema.getColumns()));
    }
}
