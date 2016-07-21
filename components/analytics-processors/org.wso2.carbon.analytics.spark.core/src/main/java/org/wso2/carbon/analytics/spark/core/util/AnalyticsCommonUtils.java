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
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
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
import java.util.HashSet;
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
        return analyticsSchema == null || analyticsSchema.getColumns() == null || analyticsSchema.getColumns().size() == 0;
    }

    public static boolean isEmptySchema(StructType schema) {
        return schema == null || schema.fieldNames() == null || schema.fieldNames().length == 0;
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

    public static StructType structTypeFromAnalyticsSchema(AnalyticsSchema analyticsSchema) {
        return new StructType(extractFieldsFromColumns(analyticsSchema.getColumns()));
    }

    public static void createTableIfNotExists(AnalyticsDataService ads, String recordStore,
                                              int targetTenantId, String targetTableName) throws AnalyticsException {
        if (!ads.listRecordStoreNames().contains(recordStore)) {
            throw new AnalyticsExecutionException("Unknown data store name: " + recordStore);
        }
        ads.createTableIfNotExists(targetTenantId, recordStore, targetTableName);
    }

    public static boolean isSchemaProvided(String schemaString) {
        return !schemaString.isEmpty();
    }

    private static void logDebug(String msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    private static List<String> createPrimaryKeyList(String primaryKeyStr) {
        return new ArrayList<>(Arrays.asList(primaryKeyStr.trim().split("\\s*,\\s*")));
    }

    private static boolean isTimestampColumn(String[] tokens) throws AnalyticsExecutionException {
        if (tokens[0].equalsIgnoreCase(AnalyticsConstants.TIMESTAMP_FIELD)) {
            if (tokens.length > 3 || tokens.length < 2) {
                throw new AnalyticsExecutionException("Invalid options for _timestamp");
            } else if (!tokens[1].equalsIgnoreCase(AnalyticsConstants.LONG_TYPE)) {
                throw new AnalyticsExecutionException("_timestamp field type must be LONG");
            }
            return true;
        }
        return false;
    }

    private static boolean isTenantFieldColumn(String[] tokens) throws AnalyticsExecutionException {
        return tokens[0].equalsIgnoreCase(AnalyticsConstants.TENANT_ID_FIELD);
    }

    /**
     * this method creates a list of column definitions, which will be used to set the schema in the
     * analytics data service. additionally, it creates a structType object for spark schema
     *
     * @param colsStr column string
     * @return column def list
     */
    private static List<ColumnDefinition> createColumnDefinitionsFromString(String colsStr, boolean globalTenantAccess,
                                                                            boolean sparkSchema)
            throws AnalyticsExecutionException {
        ArrayList<ColumnDefinition> resList = new ArrayList<>();

        if (colsStr.trim().isEmpty()) {
            return resList;
        }

        String[] strFields = colsStr.split("\\s*,\\s*");
        for (String strField : strFields) {
            String[] tokens = strField.trim().split("\\s+");
            if (tokens.length >= 2) {
                if (!sparkSchema && isTimestampColumn(tokens)) {
                    logDebug("if this is a timestamp column, ignore processing that element in " +
                             "the analytics schema");
                    continue;
                }
                if (!sparkSchema && globalTenantAccess && isTenantFieldColumn(tokens)) {
                    /* skip adding special _tenantId field used in global tenant access to the schema */
                    continue;
                }
                AnalyticsSchema.ColumnType type = AnalyticsCommonUtils.stringToColumnType(tokens[1]);
                switch (tokens.length) {
                    case 2:
                        resList.add(new ColumnDefinition(tokens[0], type));
                        break;
                    case 3:
                        if (tokens[2].equalsIgnoreCase(AnalyticsDataServiceUtils.OPTION_IS_INDEXED)) { // if indexed
                            //This is to be backward compatible with DAS 3.0.1 and DAS 3.0.0, DAS-402
                            if (tokens[1].toLowerCase().equalsIgnoreCase(AnalyticsConstants.FACET_TYPE)) {
                                resList.add(new ColumnDefinitionExt(tokens[0], type, true, false, true));
                            } else {
                                resList.add(new ColumnDefinition(tokens[0], type, true, false));
                            }
                        } else if (tokens[2].equalsIgnoreCase(AnalyticsDataServiceUtils.OPTION_SCORE_PARAM)) { // if score param
                            if (AnalyticsCommonUtils.isNumericType(type)) { // if score param && numeric type
                                resList.add(new ColumnDefinition(tokens[0], type, true, true));
                            } else {
                                throw new AnalyticsExecutionException("Score-param assigned to a non-numeric ColumnType");
                            }
                        } else if (tokens[2].equalsIgnoreCase(AnalyticsDataServiceUtils.OPTION_IS_FACET)) { // if facet,
                            resList.add(new ColumnDefinitionExt(tokens[0], type, true, false, true));

                        } else {
                            throw new AnalyticsExecutionException("Invalid option for ColumnType");
                        }
                        break;
                    case 4:
                        Set<String> indexOptions = new HashSet<>(2);
                        indexOptions.addAll(Arrays.asList(tokens[2], tokens[3]));
                        if (indexOptions.contains(AnalyticsDataServiceUtils.OPTION_IS_FACET) && // if score param and facet
                            indexOptions.contains(AnalyticsDataServiceUtils.OPTION_SCORE_PARAM)) {
                            resList.add(new ColumnDefinitionExt(tokens[0], type, true, true, true));
                        } else if (indexOptions.contains(AnalyticsDataServiceUtils.OPTION_IS_FACET) &&  //if facet and index
                                   indexOptions.contains(AnalyticsDataServiceUtils.OPTION_IS_INDEXED)) {
                            resList.add(new ColumnDefinitionExt(tokens[0], type, true, false, true));
                        } else {
                            throw new AnalyticsExecutionException("Invalid option for ColumnType");
                        }
                        break;
                    default:
                        throw new AnalyticsExecutionException("Invalid ColumnType");
                }
            } else {
                throw new AnalyticsExecutionException("Invalid ColumnType");
            }
        }

        return resList;
    }

    public static StructType createSparkSchemaStruct(AnalyticsDataService ads, int targetTenantId,
                                                     String targetTableName,
                                                     String schemaString, String primaryKeys,
                                                     boolean globalTenantAccess, boolean mergeFlag)
            throws AnalyticsException {
        AnalyticsSchema schema = createAnalyticsTableSchema(ads, targetTenantId, targetTableName, schemaString,
                                                            primaryKeys, globalTenantAccess, mergeFlag, true);
        return structTypeFromAnalyticsSchema(schema);
    }

    public static AnalyticsSchema createAnalyticsTableSchema(AnalyticsDataService ads, int targetTenantId,
                                                             String targetTableName,
                                                             String schemaString, String primaryKeys,
                                                             boolean globalTenantAccess, boolean mergeFlag,
                                                             boolean sparkSchema) throws AnalyticsException {
        List<String> pKeyList;
        if (!primaryKeys.isEmpty()) {
            pKeyList = createPrimaryKeyList(primaryKeys);
        } else {
            pKeyList = Collections.emptyList();
        }
        List<ColumnDefinition> schemaColList = createColumnDefinitionsFromString(schemaString, globalTenantAccess, sparkSchema);
        AnalyticsSchema schema = new AnalyticsSchema(schemaColList, pKeyList);

        if (sparkSchema && !isEmptyAnalyticsSchema(schema)) {
            return schema;
        }

        if (mergeFlag) {
            schema = createMergedSchema(ads, targetTenantId, targetTableName, schema);
        }
        return schema;
    }

    private static AnalyticsSchema createMergedSchema(AnalyticsDataService ads, int targetTenantId,
                                                      String targetTableName, AnalyticsSchema schema)
            throws AnalyticsException {
        AnalyticsSchema existingSchema = null;
        try {
            existingSchema = ads.getTableSchema(targetTenantId, targetTableName);
        } catch (AnalyticsTableNotAvailableException ignore) {
            /* ignore */
            if (log.isDebugEnabled()) {
                log.debug("Table not found when merging schema => " + targetTenantId + ":" + targetTableName);
            }
        }
        if (!isEmptyAnalyticsSchema(existingSchema)) {
            return AnalyticsDataServiceUtils.createMergedSchema(existingSchema, schema.getPrimaryKeys(),
                                                                new ArrayList<>(schema.getColumns().values()), Collections.<String>emptyList());
        } else {
            return schema;
        }
    }

}
