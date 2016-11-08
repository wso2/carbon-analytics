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
package org.wso2.analytics.engine.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.types.*;
import org.wso2.analytics.data.commons.AnalyticsDataService;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.analytics.data.commons.service.ColumnDefinition;
import org.wso2.analytics.data.commons.sources.AnalyticsCommonConstants;
import org.wso2.analytics.engine.commons.AnalyzerEngineConstants;
import org.wso2.analytics.engine.exceptions.AnalyticsDataServiceLoadException;
import org.wso2.analytics.engine.exceptions.AnalyticsExecutionException;
import org.wso2.analytics.engine.services.AnalyticsServiceHolder;

import java.util.*;

public class AnalyzerEngineUtils {

    private static final Log log = LogFactory.getLog(AnalyzerEngineUtils.class);

    public static boolean isEmptySchema(StructType schema) {
        return schema == null || schema.fieldNames() == null || schema.fieldNames().length == 0;
    }

    public static boolean isEmptyAnalyticsSchema(AnalyticsSchema analyticsSchema) {
        return analyticsSchema == null || analyticsSchema.getColumns() == null || analyticsSchema.getColumns().size() == 0;
    }

    public static long getIncrementalStartTime(long lastAccessTime, AnalyzerEngineConstants.IncrementalWindowUnit windowUnit, int incBuffer) {
        int year, month, day, hour, minute, second;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(lastAccessTime);

        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH);
        day = cal.get(Calendar.DAY_OF_MONTH);
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);
        second = cal.get(Calendar.SECOND);
        switch (windowUnit) {
            case YEAR:
                cal.set(Calendar.YEAR, year - incBuffer);
                cal.set(Calendar.MONTH, 0);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            case MONTH:
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month - incBuffer);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            case DAY:
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day - incBuffer);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            case HOUR:
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR_OF_DAY, hour - incBuffer);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            case MINUTE:
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute - incBuffer);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            case SECOND:
                cal.set(Calendar.YEAR, year);
                cal.set(Calendar.MONTH, month);
                cal.set(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, minute);
                cal.set(Calendar.SECOND, second - incBuffer);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTimeInMillis();
            default:
                return lastAccessTime + 1;
        }
    }

    public static void createTableIfNotExists(String recordStore, String targetTableName)
            throws AnalyticsExecutionException, AnalyticsException, AnalyticsDataServiceLoadException {
        AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
        if (!ads.listRecordStoreNames().contains(recordStore)) {
            throw new AnalyticsExecutionException("Unknown data store name: " + recordStore);
        }
        ads.createTableIfNotExists(recordStore, targetTableName);
    }

    public static AnalyticsSchema createAnalyticsTableSchema(AnalyticsDataService ads, String targetTableName, String schemaString,
                                                             String primaryKeys, boolean mergeFlag, boolean sparkSchema)
            throws AnalyticsException, AnalyticsExecutionException {
        List<String> primaryKeysList;
        if (!primaryKeys.isEmpty()) {
            primaryKeysList = new ArrayList<>(Arrays.asList(primaryKeys.trim().split("\\s*,\\s*")));
        } else {
            primaryKeysList = Collections.emptyList();
        }
        List<ColumnDefinition> schemaColList = createColumnDefinitionsFromString(schemaString);
        AnalyticsSchema schema = new AnalyticsSchema(schemaColList, primaryKeysList);
        if (sparkSchema && !isEmptyAnalyticsSchema(schema)) {
            return schema;
        }
        if (mergeFlag) {
            schema = createMergedSchema(ads, targetTableName, schema);
        }
        return schema;
    }

    /**
     * This method creates a list of column definitions, which will be used to set the schema in the
     * analytics data service. additionally, it creates a structType object for spark schema.
     *
     * @param columns column string
     * @return column def list
     */
    private static List<ColumnDefinition> createColumnDefinitionsFromString(String columns)
            throws AnalyticsExecutionException {
        ArrayList<ColumnDefinition> columnDefinitionList = new ArrayList<>();
        if (columns.trim().isEmpty()) {
            return columnDefinitionList;
        }
        String[] strFields = columns.split("\\s*,\\s*");
        for (String strField : strFields) {
            String[] columnDefinition = strField.trim().split("\\s+");
            if (columnDefinition.length >= 2) {
                //if this is a timestamp column, ignore processing that element in the analytics schema
                if (isTimestampColumn(columnDefinition)) {
                    continue;
                }
                AnalyticsSchema.ColumnType type = stringToColumnType(columnDefinition[1]);
                switch (columnDefinition.length) {
                    case 2:
                        columnDefinitionList.add(new ColumnDefinition(columnDefinition[0], type));
                        break;
                    //fixme : add indexing related schema functionality for the following case statements
                    case 3:
                        if (columnDefinition[2].equalsIgnoreCase(AnalyzerEngineConstants.OPTION_IS_INDEXED)) { // if indexed
                            //This is to be backward compatible with DAS 3.0.1 and DAS 3.0.0, DAS-402
                            if (columnDefinition[1].toLowerCase().equalsIgnoreCase(AnalyzerEngineConstants.FACET_TYPE)) {
                                //todo: add facet type support here
                            } else {
                            }
                        } else if (columnDefinition[2].equalsIgnoreCase(AnalyzerEngineConstants.OPTION_SCORE_PARAM)) { // if score param
                            if (isNumericType(type)) { // if score param && numeric type
                            } else {
                                throw new AnalyticsExecutionException("Score-param assigned to a non-numeric ColumnType");
                            }
                        } else if (columnDefinition[2].equalsIgnoreCase(AnalyzerEngineConstants.OPTION_IS_FACET)) { // if facet,
                        } else {
                            throw new AnalyticsExecutionException("Invalid option for ColumnType");
                        }
                        break;
                    case 4:
                        //support multiple indexing options ex: -i -sp
                        Set<String> indexOptions = new HashSet<>(2);
                        indexOptions.addAll(Arrays.asList(columnDefinition[2], columnDefinition[3]));
                        if (indexOptions.contains(AnalyzerEngineConstants.OPTION_IS_FACET) && // if score param and facet
                                indexOptions.contains(AnalyzerEngineConstants.OPTION_SCORE_PARAM)) {
                        } else if (indexOptions.contains(AnalyzerEngineConstants.OPTION_IS_FACET) &&  //if facet and index
                                indexOptions.contains(AnalyzerEngineConstants.OPTION_IS_INDEXED)) {
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
        return columnDefinitionList;
    }

    private static boolean isTimestampColumn(String[] column) throws AnalyticsExecutionException {
        if (column[0].equalsIgnoreCase(AnalyticsCommonConstants.TIMESTAMP_FIELD)) {
            if (column.length > 3 || column.length < 2) {
                throw new AnalyticsExecutionException("Invalid options for _timestamp");
            } else if (!column[1].equalsIgnoreCase(AnalyzerEngineConstants.LONG_TYPE)) {
                throw new AnalyticsExecutionException("_timestamp field type must be LONG");
            }
            return true;
        }
        return false;
    }

    private static AnalyticsSchema.ColumnType stringToColumnType(String strType) {
        switch (strType.toLowerCase()) {
            case AnalyzerEngineConstants.INTEGER_TYPE:
                return AnalyticsSchema.ColumnType.INTEGER;
            case AnalyzerEngineConstants.INT_TYPE:
                return AnalyticsSchema.ColumnType.INTEGER;
            case AnalyzerEngineConstants.FLOAT_TYPE:
                return AnalyticsSchema.ColumnType.FLOAT;
            case AnalyzerEngineConstants.DOUBLE_TYPE:
                return AnalyticsSchema.ColumnType.DOUBLE;
            case AnalyzerEngineConstants.LONG_TYPE:
                return AnalyticsSchema.ColumnType.LONG;
            case AnalyzerEngineConstants.BOOLEAN_TYPE:
                return AnalyticsSchema.ColumnType.BOOLEAN;
            case AnalyzerEngineConstants.STRING_TYPE:
                return AnalyticsSchema.ColumnType.STRING;
            case AnalyzerEngineConstants.BINARY_TYPE:
                return AnalyticsSchema.ColumnType.BINARY;
            case AnalyzerEngineConstants.FACET_TYPE:
                return AnalyticsSchema.ColumnType.STRING;
            default:
                throw new RuntimeException("Invalid ColumnType: " + strType);
        }
    }

    private static boolean isNumericType(AnalyticsSchema.ColumnType colType) {
        return !(colType.name().equalsIgnoreCase(AnalyzerEngineConstants.STRING_TYPE)
                || colType.name().equalsIgnoreCase(AnalyzerEngineConstants.BINARY_TYPE)
                || colType.name().equalsIgnoreCase(AnalyzerEngineConstants.FACET_TYPE));
    }

    private static AnalyticsSchema createMergedSchema(AnalyticsDataService ads, String targetTableName, AnalyticsSchema schema)
            throws AnalyticsException {
        AnalyticsSchema existingSchema = null;
        try {
            existingSchema = ads.getTableSchema(targetTableName);
        } catch (AnalyticsTableNotAvailableException ignore) {
            log.debug("Table not found when merging schema => " + ":" + targetTableName);
        }
       /* if (!isEmptyAnalyticsSchema(existingSchema)) {
            //fixme: support indexing related schema merging here
            return mergeSchemas(existingSchema, schema.getPrimaryKeys(),
                    new ArrayList<>(schema.getColumns().values()), Collections.emptyList());
        } else {*/
            return schema;
        //}
    }

    public static AnalyticsSchema mergeSchemas(AnalyticsSchema existingSchema,
                                                     List<String> primaryKeys, List<ColumnDefinition> columns, List<String> indices) {
        Set<String> newPrimaryKeys;
        if (existingSchema.getPrimaryKeys() == null) {
            newPrimaryKeys = new HashSet<>();
        } else {
            newPrimaryKeys = new HashSet<>(existingSchema.getPrimaryKeys());
        }
        newPrimaryKeys.addAll(primaryKeys);
        //fixme: handle indexing related merging here
        /*Map<String, ColumnDefinitionExt> newColumns;
        if (existingSchema.getColumns() == null) {
            newColumns = new LinkedHashMap<>();
        } else {
            newColumns = translate(existingSchema.getColumns());
        }
        ColumnDefinitionExt targetColumn;
        for (ColumnDefinition column : columns) {
            targetColumn = newColumns.get(column.getName());
            if (targetColumn == null) {
                targetColumn = ColumnDefinitionExt.copy(column);
                newColumns.put(targetColumn.getName(), targetColumn);
            } else {
                if (column.isIndexed()) {
                    targetColumn.setIndexed(true);
                }
                if (column.isScoreParam()) {
                    targetColumn.setScoreParam(true);
                }
                if (column.isFacet()) {
                    targetColumn.setFacet(true);
                }
            }
        }
        for (String index : indices) {
            processIndex(newColumns, index);
        }
        return new AnalyticsSchema(new ArrayList<ColumnDefinition>(newColumns.values()), new ArrayList<>(newPrimaryKeys));*/
        return null;
    }

    public static StructField[] extractFieldsFromColumns(Map<String, ColumnDefinition> columns) {
        StructField[] resFields = new StructField[columns.size()];
        int i = 0;
        for (Map.Entry<String, ColumnDefinition> entry : columns.entrySet()) {
            String type = entry.getValue().getType().name();
            resFields[i] = new StructField(entry.getKey(), stringToDataType(type), true, Metadata.empty());
            i++;
        }
        return resFields;
    }

    public static DataType stringToDataType(String strType) {
        switch (strType.toLowerCase()) {
            case AnalyzerEngineConstants.INTEGER_TYPE:
                return DataTypes.IntegerType;
            case AnalyzerEngineConstants.INT_TYPE:
                return DataTypes.IntegerType;
            case AnalyzerEngineConstants.FLOAT_TYPE:
                return DataTypes.FloatType;
            case AnalyzerEngineConstants.DOUBLE_TYPE:
                return DataTypes.DoubleType;
            case AnalyzerEngineConstants.LONG_TYPE:
                return DataTypes.LongType;
            case AnalyzerEngineConstants.BOOLEAN_TYPE:
                return DataTypes.BooleanType;
            case AnalyzerEngineConstants.STRING_TYPE:
                return DataTypes.StringType;
            case AnalyzerEngineConstants.BINARY_TYPE:
                return DataTypes.BinaryType;
            case AnalyzerEngineConstants.FACET_TYPE:
                return DataTypes.StringType;
            default:
                throw new RuntimeException("Invalid DataType: " + strType);
        }
    }

    public static Boolean validateSchemaColumns(StructType sparkSchema, AnalyticsSchema analyticsSchema) {
        String[] rddCols = sparkSchema.fieldNames();
        Set<String> temp = analyticsSchema.getColumns().keySet();
        String[] tableCols = temp.toArray(new String[temp.size()]);
        return Arrays.equals(rddCols, tableCols);
    }
}
