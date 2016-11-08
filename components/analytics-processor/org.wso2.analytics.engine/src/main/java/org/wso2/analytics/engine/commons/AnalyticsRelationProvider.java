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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.RelationProvider;
import org.apache.spark.sql.sources.SchemaRelationProvider;
import org.apache.spark.sql.types.StructType;
import org.wso2.analytics.data.commons.AnalyticsDataService;
import org.wso2.analytics.data.commons.exception.AnalyticsException;
import org.wso2.analytics.data.commons.service.AnalyticsSchema;
import org.wso2.analytics.data.commons.utils.AnalyticsCommonUtils;
import org.wso2.analytics.engine.exceptions.AnalyticsDataServiceLoadException;
import org.wso2.analytics.engine.exceptions.AnalyticsExecutionException;
import org.wso2.analytics.engine.services.AnalyticsServiceHolder;
import org.wso2.analytics.engine.utils.AnalyzerEngineUtils;
import scala.collection.JavaConversions;
import scala.collection.immutable.Map;

import java.util.HashMap;

import static org.wso2.analytics.engine.utils.AnalyzerEngineUtils.createAnalyticsTableSchema;

/**
 * This class allows spark to communicate with the the Analytics Dataservice when used in Spark SQL
 * with the 'USING' keyword
 */
public class AnalyticsRelationProvider implements RelationProvider, SchemaRelationProvider {

    private static final Log log = LogFactory.getLog(AnalyticsRelationProvider.class);

    private String tableName;
    private String schemaString;
    private String streamName;
    private String primaryKeys;
    private String recordStore;
    private boolean mergeFlag;
    private StructType schemaStruct;
    private String incParams;

    public AnalyticsRelationProvider() {

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
        init(parameters);
        return getAnalyticsRelation(this.recordStore, this.tableName, sqlContext,
                this.schemaStruct, this.incParams, this.schemaString, this.primaryKeys, this.mergeFlag);
    }

    private void prepareTable() {
        try {
            createTableIfNotExist();
            setSchemaIfProvided();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void init(Map<String, String> parameters) {
        java.util.Map<String, String> jMap = new HashMap<>(JavaConversions.asJavaMap(parameters));
        this.tableName = extractAndRemoveValuesFromMap(AnalyzerEngineConstants.TABLE_NAME, jMap, "");
        this.schemaString = extractAndRemoveValuesFromMap(AnalyzerEngineConstants.SCHEMA_STRING, jMap, "");
        this.streamName = extractAndRemoveValuesFromMap(AnalyzerEngineConstants.STREAM_NAME, jMap, "");
        this.primaryKeys = extractAndRemoveValuesFromMap(AnalyzerEngineConstants.PRIMARY_KEYS, jMap, "");
        this.recordStore = extractAndRemoveValuesFromMap(AnalyzerEngineConstants.RECORD_STORE, jMap,
                AnalyzerEngineConstants.DEFAULT_PROCESSED_DATA_STORE_NAME);
        this.mergeFlag = Boolean.parseBoolean(extractAndRemoveValuesFromMap(AnalyzerEngineConstants.MERGE_SCHEMA,
                jMap, String.valueOf(true)));
        this.incParams = extractAndRemoveValuesFromMap(AnalyzerEngineConstants.INC_PARAMS, jMap, "");
        checkUnusedParameters(jMap);
        prepareTable();
    }

    private void checkUnusedParameters(java.util.Map<String, String> jMap) {
        if (jMap.size() > 0) {
            StringBuilder buf = new StringBuilder();
            buf.append("Unknown options : ");
            for (String key : jMap.keySet()) {
                buf.append(key).append(" ");
            }
            throw new RuntimeException(buf.toString());
        }
    }

    private void createTableIfNotExist() throws AnalyticsExecutionException {
        if (this.tableName.isEmpty()) {
            this.tableName = AnalyticsCommonUtils.convertStreamNameToTableName(this.streamName);
        }
        if (this.tableName.isEmpty()) {
            throw new AnalyticsExecutionException("Empty " + AnalyzerEngineConstants.TABLE_NAME + " OR "
                    + AnalyzerEngineConstants.STREAM_NAME);
        }
        try {
            AnalyzerEngineUtils.createTableIfNotExists(this.recordStore, this.tableName);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected StructType createSparkSchemaStruct(AnalyticsDataService ads, String targetTableName,
                                                 String schemaString, String primaryKeys,
                                                 boolean mergeFlag) throws AnalyticsException, AnalyticsExecutionException {
        AnalyticsSchema schema = createAnalyticsTableSchema(ads, targetTableName, schemaString, primaryKeys, mergeFlag, true);
        return new StructType(AnalyzerEngineUtils.extractFieldsFromColumns(schema.getColumns()));
    }

    private void setSchemaIfProvided() throws AnalyticsExecutionException, AnalyticsDataServiceLoadException {
        try {
            AnalyticsDataService ads = AnalyticsServiceHolder.getAnalyticsDataService();
            AnalyticsSchema schema = createAnalyticsTableSchema(ads, tableName, schemaString,
                    primaryKeys, mergeFlag, true);
            if (schema != null) {
                ads.setTableSchema(this.tableName, schema);
            }
            this.schemaStruct = this.createSparkSchemaStruct(ads, this.tableName, this.schemaString, this.primaryKeys, this.mergeFlag);
        } catch (AnalyticsException e) {
            throw new AnalyticsExecutionException("Error in setting provided schema: " + e.getMessage(), e);
        }
    }

    private String extractAndRemoveValuesFromMap(String key, java.util.Map<String, String> map,
                                                 final String defaultValue) {
        String value = map.get(key.toLowerCase());
        if (value == null) {
            return defaultValue;
        } else {
            map.remove(key.toLowerCase());
            return value;
        }
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
    public BaseRelation createRelation(SQLContext sqlContext, Map<String, String> parameters, StructType schema) {
        /*
        * Here the schema is provided as struct type & NOT in the parameters
        * This extracts the schema information, set schema in the ds and create a new analytics relation
        * NOTE: this schema contains comments, which are included in the metadata fields
        */
        init(parameters);
        prepareTable();
        try {
            AnalyticsSchema schemaFromDS;
            schemaFromDS = AnalyticsServiceHolder.getAnalyticsDataService().getTableSchema(this.tableName);
            if (!AnalyzerEngineUtils.validateSchemaColumns(schema, schemaFromDS)) {
                String msg = "Incompatible schemas for the table " + this.tableName;
                throw new RuntimeException(msg);
            }
        } catch (AnalyticsException e) {
            String msg = "Failed to load the schema for table " + tableName + ": " + e.getMessage();
            throw new RuntimeException(msg, e);
        } catch (AnalyticsDataServiceLoadException e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        return getAnalyticsRelation(this.tableName, this.recordStore, sqlContext, schema, this.incParams,
                this.schemaString, this.primaryKeys, this.mergeFlag);
    }


    protected AnalyticsRelation getAnalyticsRelation(String recordStore, String tableName,
                                                     SQLContext sqlContext, StructType schema, String incParams,
                                                     String schemaString, String primaryKeys, boolean mergeFlag) {
        return new AnalyticsRelation(recordStore, tableName, sqlContext, schema, incParams, schemaString, primaryKeys, mergeFlag);
    }
}

