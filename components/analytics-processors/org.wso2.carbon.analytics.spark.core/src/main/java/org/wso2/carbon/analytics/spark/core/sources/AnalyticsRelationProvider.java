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
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.RelationProvider;
import org.apache.spark.sql.sources.SchemaRelationProvider;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import scala.collection.immutable.Map;
import scala.runtime.AbstractFunction0;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils.isEmptyAnalyticsSchema;
import static org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils.structTypeFromAnalyticsSchema;

/**
 * This class allows spark to communicate with the the Analytics Dataservice when used in Spark SQL
 * with the 'USING' keyword
 */
public class AnalyticsRelationProvider implements RelationProvider,
                                                  SchemaRelationProvider, Serializable {

    private static final Log log = LogFactory.getLog(AnalyticsRelationProvider.class);
    private static final long serialVersionUID = 8688336885845108375L;

    private int tenantId;
    private String tableName;
    private String schemaString;
    private String streamName;
    private String primaryKeys;
    private AnalyticsDataService dataService;
    private String recordStore;
    private StructType schemaStruct;

    public AnalyticsRelationProvider() {
        this.dataService = ServiceHolder.getAnalyticsDataService();
    }

    /**
     * Returns a new base relation with the given parameters.
     * Note: the parameters' keywords are case insensitive and this insensitivity is enforced
     * by the Map that is passed to the function.
     *
     * @param sqlContext sqlContext
     * @param parameters tenantId, tableName, schema, streamName, streamVersion, primaryKeys, recordStore
     */
    @Override
    public AnalyticsRelation createRelation(SQLContext sqlContext, Map<String, String> parameters) {
        setParameters(parameters);
        try {
            createTableIfNotExist();
        } catch (AnalyticsExecutionException e) {
            String msg = "Error while creating the table : " + this.tableName + " : " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        if (isSchemaProvided()) {
            try {
                setSchemaIfProvided();
            } catch (AnalyticsExecutionException e) {
                String msg = "Error while merging the schema for the table : " + this.tableName + " : " + e.getMessage();
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
            return new AnalyticsRelation(this.tenantId, this.recordStore, this.tableName, sqlContext
                    , this.schemaStruct);
        } else {
            return new AnalyticsRelation(this.tenantId, this.recordStore, this.tableName, sqlContext);
        }
    }

    private void setParameters(Map<String, String> parameters) {
        this.tenantId = Integer.parseInt(extractValuesFromMap(AnalyticsConstants.TENANT_ID, parameters, "-1234"));
        this.tableName = extractValuesFromMap(AnalyticsConstants.TABLE_NAME, parameters, "");
        this.schemaString = extractValuesFromMap(AnalyticsConstants.SCHEMA_STRING, parameters, "");
        this.streamName = extractValuesFromMap(AnalyticsConstants.STREAM_NAME, parameters, "");
        this.primaryKeys = extractValuesFromMap(AnalyticsConstants.PRIMARY_KEYS, parameters, "");
        this.recordStore = extractValuesFromMap(AnalyticsConstants.RECORD_STORE, parameters,
                                                AnalyticsConstants.DEFAULT_PROCESSED_DATA_STORE_NAME);
    }

    private void createTableIfNotExist() throws AnalyticsExecutionException {
        if (!this.tableName.isEmpty()) {
            try {
                if (!this.dataService.tableExists(this.tenantId, this.tableName)) {
                    logDebug(this.tableName + " table does not exists. Hence creating it");
                    if (!this.dataService.listRecordStoreNames().contains(this.recordStore)) {
                        throw new AnalyticsExecutionException("Unknown data store name " + this.recordStore);
                    }
                    this.dataService.createTable(this.tenantId, this.recordStore, this.tableName);
                }
            } catch (AnalyticsException e) {
                throw new AnalyticsExecutionException("Error while accessing table " + this.tableName + " : " + e.getMessage(), e);
            }
        } else if (!this.streamName.isEmpty()) {
            try {
                this.tableName = AnalyticsCommonUtils.convertStreamNameToTableName(this.streamName);
                if (!this.dataService.tableExists(this.tenantId, this.tableName)) {
                    if (!this.dataService.listRecordStoreNames().contains(this.recordStore)) {
                        throw new AnalyticsExecutionException("Unknown data store name " + this.recordStore);
                    }
                    this.dataService.createTable(this.tenantId, this.recordStore, this.tableName);
                }
            } catch (AnalyticsException e) {
                throw new AnalyticsExecutionException("Error while accessing table " + this.tableName
                                                      + " : " + e.getMessage(), e);
            }
        } else {
            throw new AnalyticsExecutionException("Empty " + AnalyticsConstants.TABLE_NAME + " OR "
                                                  + AnalyticsConstants.STREAM_NAME);
        }

    }

    private void setSchemaIfProvided() throws AnalyticsExecutionException {
        if (isSchemaProvided()) {
            logDebug("Schema is provided, hence setting the schema in the analytics data service");
            List<ColumnDefinition> colList = this.createColumnDefinitionsFromString(this.schemaString);
            List<String> pKeyList;
            if (!this.primaryKeys.isEmpty()) {
                pKeyList = this.createPrimaryKeyList(this.primaryKeys);
            } else {
                logDebug("No primary keys present, hence setting an empty list");
                pKeyList = Collections.emptyList();
            }

            AnalyticsSchema finalSchema = new AnalyticsSchema(colList, pKeyList);
            try {
                AnalyticsSchema existingSchema = this.dataService.getTableSchema(this.tenantId, this.tableName);
                if (!isEmptyAnalyticsSchema(existingSchema)) {
                    logDebug("There is an existing schema already present. Hence, merging the schemas");
                    finalSchema = AnalyticsDataServiceUtils.createMergedSchema
                            (existingSchema, pKeyList, colList, Collections.<String>emptyList());
                }
            } catch (AnalyticsException e) {
                throw new AnalyticsExecutionException("Error while reading " + this.tableName + " table schema: " + e.getMessage(), e);
            }

            try {
                this.dataService.setTableSchema(this.tenantId, this.tableName, finalSchema);
            } catch (AnalyticsException e) {
                throw new AnalyticsExecutionException("Error while setting " + this.tableName + " table schema: " + e.getMessage(), e);
            }

            StructType tempStruct = structTypeFromAnalyticsSchema(finalSchema);
            if (this.schemaString.contains(AnalyticsConstants.TIMESTAMP_FIELD)) {
                this.schemaStruct = tempStruct.merge(new StructType(new StructField[]{new StructField(
                        AnalyticsConstants.TIMESTAMP_FIELD, DataTypes.LongType, true, Metadata.empty())}));
            } else {
                this.schemaStruct = tempStruct;
            }

        } else {
            if (!this.primaryKeys.isEmpty()) {
                throw new AnalyticsExecutionException("Primary keys set to an empty Schema");
            }
        }
    }

    private boolean isSchemaProvided() {
        return !this.schemaString.isEmpty();
    }

    private String extractValuesFromMap(String key, Map<String, String> map,
                                        final String defaultVal) {
        return map.getOrElse(key, new AbstractFunction0<String>() {
            public String apply() {
                return defaultVal;
            }
        });
    }

    /**
     * this method creates a list of column definitions, which will be used to set the schema in the
     * analytics data service. additionally, it creates a structType object for spark schema
     *
     * @param colsStr column string
     * @return column def list
     */
    private List<ColumnDefinition> createColumnDefinitionsFromString(String colsStr)
            throws AnalyticsExecutionException {
        String[] strFields = colsStr.split("\\s*,\\s*");
        ArrayList<ColumnDefinition> resList = new ArrayList<>();
        for (String strField : strFields) {
            String[] tokens = strField.trim().split("\\s+");

            if (tokens.length >= 2) {
                if (isTimestampColumn(tokens)) {
                    logDebug("if this is a timestamp column, ignore processing that element in " +
                             "the analytics schema");
                    continue;
                }

                AnalyticsSchema.ColumnType type = AnalyticsCommonUtils.stringToColumnType(tokens[1]);
                switch (tokens.length) {
                    case 2:
                        resList.add(new ColumnDefinition(tokens[0], type));
                        break;
                    case 3:
                        if (tokens[2].equalsIgnoreCase("-i")) { // if indexed
                            resList.add(new ColumnDefinition(tokens[0], type, true, false));
                        } else if (tokens[2].equalsIgnoreCase("-sp")) { // if score param
                            if (AnalyticsCommonUtils.isNumericType(type)) { // if score param && numeric type
                                resList.add(new ColumnDefinition(tokens[0], type, true, true));
                            } else {
                                throw new AnalyticsExecutionException("Score-param assigned to a non-numeric ColumnType");
                            }
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

    private boolean isTimestampColumn(String[] tokens) throws AnalyticsExecutionException {
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

    private List<String> createPrimaryKeyList(String primaryKeyStr) {
        return new ArrayList<>(Arrays.asList(primaryKeyStr.trim().split("\\s*,\\s*")));
    }

    /**
     * Returns a new base relation with the given parameters and user defined schema.
     * Note: the parameters' keywords are case insensitive and this insensitivity is enforced
     * by the Map that is passed to the function.
     *
     * @param sqlContext sqlContext
     * @param parameters tenantId, tableName, schema, streamName, streamVersion, primaryKeys
     * @param schema     schema specified in line with the query
     */
    @Override
    public BaseRelation createRelation(SQLContext sqlContext, Map<String, String> parameters,
                                       StructType schema) {
        //Here the schema is provided as struct type & NOT in the parameters
        //This exctracts the schema information, set schema in the ds and create a new analytics relationNOTE: this schema contains comments, which are included in the metadata fields

        setParameters(parameters);
        try {
            createTableIfNotExist();
        } catch (AnalyticsExecutionException e) {
            String msg = "Error while creating the table : " + this.tableName + " : " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }
        try {
            setSchemaIfProvided();
        } catch (AnalyticsExecutionException e) {
            String msg = "Error while merging the schema for the table : " + this.tableName + " : " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        try {
            AnalyticsSchema schemaFromDS;
            schemaFromDS = dataService.getTableSchema(this.tenantId, this.tableName);
            if (!AnalyticsCommonUtils.validateSchemaColumns(schema, schemaFromDS)) {
                String msg = "Incompatible schemas for the table " + this.tableName;
                log.error(msg);
                throw new RuntimeException(msg);
            }
        } catch (AnalyticsException e) {
            String msg = "Failed to load the schema for table " + tableName + ": " + e.getMessage();
            log.error(msg, e);
            throw new RuntimeException(msg, e);
        }

        return new AnalyticsRelation(this.tenantId, this.tableName, this.recordStore, sqlContext,
                                     schema);
    }

//    todo: Implement the creatable relation

    /**
     * Creates a relation with the given parameters based on the contents of the given
     * DataFrame. The mode specifies the expected behavior of createRelation when
     * data already exists.
     * Right now, there are three modes, Append, Overwrite, and ErrorIfExists.
     * Append mode means that when saving a DataFrame to a data source, if data already exists,
     * contents of the DataFrame are expected to be appended to existing data.
     * Overwrite mode means that when saving a DataFrame to a data source, if data already exists,
     * existing data is expected to be overwritten by the contents of the DataFrame.
     * ErrorIfExists mode means that when saving a DataFrame to a data source,
     * if data already exists, an exception is expected to be thrown.
     */
//    @Override
//    public BaseRelation createRelation(SQLContext sqlContext, SaveMode mode,
//                                       Map<String, String> parameters, DataFrame data) {
//        //implement this
//        //extract data from the dataframe, save it using the savemode and create the relation using the first initializer
//        throw new RuntimeException("Creatable relation is not implemented as yet");
//    }
    private void logDebug(String msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }
}
