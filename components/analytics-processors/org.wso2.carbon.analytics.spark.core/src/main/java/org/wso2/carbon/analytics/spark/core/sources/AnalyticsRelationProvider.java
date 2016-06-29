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

import scala.collection.immutable.Map;
import scala.runtime.AbstractFunction0;

import java.io.Serializable;

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
    private boolean mergeFlag;
    private boolean globalTenantAccess;
    private StructType schemaStruct;
    private String incParams;

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
        if (AnalyticsCommonUtils.isSchemaProvided(this.schemaString)) {
            try {
                setSchemaIfProvided();
            } catch (AnalyticsExecutionException e) {
                String msg = "Error while merging the schema for the table : " + this.tableName + " : " + e.getMessage();
                log.error(msg, e);
                throw new RuntimeException(msg, e);
            }
            return getAnalyticsRelation(this.tenantId, this.recordStore, this.tableName, sqlContext,
                                         this.schemaStruct, this.incParams, this.globalTenantAccess,
                                         this.schemaString, this.primaryKeys, this.mergeFlag);
        } else {
            return getAnalyticsRelation(this.tenantId, this.recordStore, this.tableName, sqlContext,
                                         this.incParams, this.globalTenantAccess,
                                         this.schemaString, this.primaryKeys, this.mergeFlag);
        }
    }

    private void setParameters(Map<String, String> parameters) {
        this.tenantId = Integer.parseInt(extractValuesFromMap(AnalyticsConstants.TENANT_ID,
                                                              parameters, "-1234"));
        this.tableName = extractValuesFromMap(AnalyticsConstants.TABLE_NAME, parameters, "");
        this.schemaString = extractValuesFromMap(AnalyticsConstants.SCHEMA_STRING, parameters, "");
        this.streamName = extractValuesFromMap(AnalyticsConstants.STREAM_NAME, parameters, "");
        this.primaryKeys = extractValuesFromMap(AnalyticsConstants.PRIMARY_KEYS, parameters, "");
        this.recordStore = extractValuesFromMap(AnalyticsConstants.RECORD_STORE, parameters,
                                                AnalyticsConstants.DEFAULT_PROCESSED_DATA_STORE_NAME);
        this.mergeFlag = Boolean.parseBoolean(extractValuesFromMap(AnalyticsConstants.MERGE_SCHEMA,
                                                                   parameters, String.valueOf(true)));
        this.globalTenantAccess = Boolean.parseBoolean(extractValuesFromMap(AnalyticsConstants.GLOBAL_TENANT_ACCESS,
                parameters, String.valueOf(false)));
        this.incParams = extractValuesFromMap(AnalyticsConstants.INC_PARAMS, parameters, "");
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
            AnalyticsCommonUtils.createTableIfNotExists(this.dataService, this.recordStore, targetTenantId, targetTableName);
        } catch (AnalyticsException e) {
            throw new AnalyticsExecutionException("Error while accessing table " + targetTableName + " : " + e.getMessage(), e);
        }
    }

    private void setSchemaIfProvided() throws AnalyticsExecutionException {
        int targetTenantId;
        if (this.globalTenantAccess) {
            targetTenantId = Constants.GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID;
        } else {
            targetTenantId = this.tenantId;
        }
        this.schemaStruct = AnalyticsCommonUtils.setSchemaIfProvided(this.dataService, this.schemaString, this.globalTenantAccess,
                this.primaryKeys, this.mergeFlag, targetTenantId, this.tableName);
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

        return getAnalyticsRelation(this.tenantId, this.tableName, this.recordStore, sqlContext,
                                     schema, this.incParams, this.globalTenantAccess,
                                     this.schemaString, this.primaryKeys, this.mergeFlag);
    }
    
    protected AnalyticsRelation getAnalyticsRelation(int tenantId, String recordStore, String tableName,
            SQLContext sqlContext, StructType schema, String incParams, boolean globalTenantAccess, 
            String schemaString, String primaryKeys, boolean mergeFlag) {
        return new AnalyticsRelation(tenantId, recordStore, tableName, sqlContext, schema, incParams, 
            globalTenantAccess, schemaString, primaryKeys, mergeFlag);
    }
    
    protected AnalyticsRelation getAnalyticsRelation(int tenantId, String recordStore, String tableName,
            SQLContext sqlContext, String incParams, boolean globalTenantAccess, String schemaString,
            String primaryKeys, boolean mergeFlag) {
        return new AnalyticsRelation(tenantId, recordStore, tableName, sqlContext, incParams, globalTenantAccess,
            schemaString, primaryKeys, mergeFlag);
    }
}
