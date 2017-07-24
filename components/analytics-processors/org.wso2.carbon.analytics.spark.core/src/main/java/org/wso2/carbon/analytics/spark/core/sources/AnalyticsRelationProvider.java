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
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.base.MultitenantConstants;
import scala.collection.JavaConversions;
import scala.collection.immutable.Map;

import java.util.HashMap;

/**
 * This class allows spark to communicate with the the Analytics Dataservice when used in Spark SQL
 * with the 'USING' keyword
 */
public class AnalyticsRelationProvider implements RelationProvider,
                                                  SchemaRelationProvider {

    private static final Log log = LogFactory.getLog(AnalyticsRelationProvider.class);

    private int tenantId;
    private String tableName;
    private String schemaString;
    private String streamName;
    private String primaryKeys;
    private String recordStore;
    private boolean mergeFlag;
    private boolean globalTenantAccess;
    private StructType schemaStruct;
    private String incParams;
    private boolean preserveOrder;

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
        setParameters(parameters);
        doTableActions();
        return getAnalyticsRelation(this.tenantId, this.recordStore, this.tableName, sqlContext,
                                    this.schemaStruct, this.incParams, this.globalTenantAccess,
                                    this.schemaString, this.primaryKeys, this.mergeFlag, this.preserveOrder);
    }

    private void doTableActions() {
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
    }

    private void setParameters(Map<String, String> parameters) {
        java.util.Map<String, String> jMap = new HashMap<>(JavaConversions.asJavaMap(parameters));
        this.tenantId = Integer.parseInt(extractAndRemoveValuesFromMap(AnalyticsConstants.TENANT_ID, jMap, "-1234"));
        this.tableName = extractAndRemoveValuesFromMap(AnalyticsConstants.TABLE_NAME, jMap, "");
        this.schemaString = extractAndRemoveValuesFromMap(AnalyticsConstants.SCHEMA_STRING, jMap, "");
        this.streamName = extractAndRemoveValuesFromMap(AnalyticsConstants.STREAM_NAME, jMap, "");
        this.primaryKeys = extractAndRemoveValuesFromMap(AnalyticsConstants.PRIMARY_KEYS, jMap, "");
        this.recordStore = extractAndRemoveValuesFromMap(AnalyticsConstants.RECORD_STORE, jMap,
                                                         AnalyticsConstants.DEFAULT_PROCESSED_DATA_STORE_NAME);
        this.mergeFlag = Boolean.parseBoolean(extractAndRemoveValuesFromMap(AnalyticsConstants.MERGE_SCHEMA,
                                                                            jMap, String.valueOf(true)));
        this.globalTenantAccess = Boolean.parseBoolean(extractAndRemoveValuesFromMap(AnalyticsConstants.GLOBAL_TENANT_ACCESS,
                                                                                     jMap, String.valueOf(false)));
        this.preserveOrder = Boolean.parseBoolean(extractAndRemoveValuesFromMap(AnalyticsConstants.PRESERVE_ORDER,
                                                                                                 jMap, String.valueOf(false)));
        this.incParams = extractAndRemoveValuesFromMap(AnalyticsConstants.INC_PARAMS, jMap, "");
        checkParameters(jMap);
    }

    private void checkParameters(java.util.Map<String, String> jMap) {
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
            throw new AnalyticsExecutionException("Empty " + AnalyticsConstants.TABLE_NAME + " OR "
                                                  + AnalyticsConstants.STREAM_NAME);
        }
        int targetTenantId;
        if (this.globalTenantAccess) {
            if (this.tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                targetTenantId = Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID;
            } else {
                throw new RuntimeException("Global tenant write can only be done by the super tenant");
            }
        } else {
            targetTenantId = this.tenantId;
        }
        this.createTableIfNotExist(targetTenantId, this.tableName);
    }

    private void createTableIfNotExist(int targetTenantId, String targetTableName) throws AnalyticsExecutionException {
        try {
            AnalyticsCommonUtils.createTableIfNotExists(ServiceHolder.getAnalyticsDataService(), this.recordStore, targetTenantId, targetTableName);
        } catch (AnalyticsException e) {
            throw new AnalyticsExecutionException("Error while accessing table " + targetTableName + " : " + e.getMessage(), e);
        }
    }

    protected AnalyticsSchema createAnalyticsTableSchema(AnalyticsDataService ads, int targetTenantId,
                                                         String targetTableName,
                                                         String schemaString, String primaryKeys,
                                                         boolean globalTenantAccess, boolean mergeFlag)
            throws AnalyticsException {
        return AnalyticsCommonUtils.createAnalyticsTableSchema(ads, targetTenantId, targetTableName, schemaString,
                                                               primaryKeys, globalTenantAccess, mergeFlag, false);
    }

    protected StructType createSparkSchemaStruct(AnalyticsDataService ads, int targetTenantId, String targetTableName,
                                                 String schemaString, String primaryKeys, boolean globalTenantAccess,
                                                 boolean mergeFlag) throws AnalyticsException {
        return AnalyticsCommonUtils.createSparkSchemaStruct(ads, targetTenantId, targetTableName, schemaString,
                                                            primaryKeys, globalTenantAccess, mergeFlag);
    }

    private void setSchemaIfProvided() throws AnalyticsExecutionException {
        int targetTenantId;
        if (this.globalTenantAccess) {
            targetTenantId = Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID;
        } else {
            targetTenantId = this.tenantId;
        }
        try {
            AnalyticsSchema schema = this.createAnalyticsTableSchema(ServiceHolder.getAnalyticsDataService(), targetTenantId, this.tableName,
                                                                     this.schemaString, this.primaryKeys, this.globalTenantAccess, this.mergeFlag);
            if (schema != null) {
                ServiceHolder.getAnalyticsDataService().setTableSchema(targetTenantId, this.tableName, schema);
            }
            this.schemaStruct = this.createSparkSchemaStruct(ServiceHolder.getAnalyticsDataService(), targetTenantId,
                                                             this.tableName, this.schemaString, this.primaryKeys, this.globalTenantAccess, this.mergeFlag);
        } catch (AnalyticsException e) {
            throw new AnalyticsExecutionException("Error in setting provided schema: " + e.getMessage(), e);
        }
    }

    private String extractAndRemoveValuesFromMap(String key, java.util.Map<String, String> map,
                                                 final String defaultVal) {
        String value = map.get(key.toLowerCase());
        if (value == null) {
            return defaultVal;
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
    public BaseRelation createRelation(SQLContext sqlContext, Map<String, String> parameters,
                                       StructType schema) {
        //Here the schema is provided as struct type & NOT in the parameters
        //This exctracts the schema information, set schema in the ds and create a new analytics relationNOTE: this schema contains comments, which are included in the metadata fields

        setParameters(parameters);
        doTableActions();
        try {
            AnalyticsSchema schemaFromDS;
            schemaFromDS = ServiceHolder.getAnalyticsDataService().getTableSchema(this.tenantId, this.tableName);
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

        return getAnalyticsRelation(this.tenantId, this.tableName, this.recordStore, sqlContext,
                                    schema, this.incParams, this.globalTenantAccess,
                                    this.schemaString, this.primaryKeys, this.mergeFlag, this.preserveOrder);
    }


    protected AnalyticsRelation getAnalyticsRelation(int tenantId, String recordStore, String tableName,
                                                   SQLContext sqlContext, StructType schema, String incParams,
                                                   boolean globalTenantAccess,
                                                   String schemaString, String primaryKeys, boolean mergeFlag,
                                                   boolean preserveOrder) {
        return new AnalyticsRelation(tenantId, recordStore, tableName, sqlContext, schema, incParams,
                                     globalTenantAccess, schemaString, primaryKeys, mergeFlag, preserveOrder);
    }
}
