/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.io.Serializable;

import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.RelationProvider;
import org.apache.spark.sql.sources.SchemaRelationProvider;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;

/**
 * This class allows spark to communicate with the the Analytics Dataservice when used in Spark SQL
 * with the 'USING' keyword.
 */
public class CompressedEventAnalyticsRelationProvider extends AnalyticsRelationProvider implements RelationProvider,
        SchemaRelationProvider, Serializable {
    private static final long serialVersionUID = 8688336885845108375L;
    
    public CompressedEventAnalyticsRelationProvider() {
        super();
    }

    @Override
    protected AnalyticsRelation getAnalyticsRelation(int tenantId, String recordStore, String tableName,
            SQLContext sqlContext, StructType schema, String incParams, boolean globalTenantAccess, String schemaString,
            String primaryKeys, boolean mergeFlag, boolean preserveOrder) {
        return new CompressedEventAnalyticsRelation(tenantId, recordStore, tableName, sqlContext, schema, incParams, 
            globalTenantAccess, schemaString, primaryKeys, mergeFlag, preserveOrder);
    }
    
    @Override
    protected AnalyticsSchema createAnalyticsTableSchema(AnalyticsDataService ads, int targetTenantId, String targetTableName, 
            String schemaString, String primaryKeys, boolean globalTenantAccess, boolean mergeFlag) throws AnalyticsException {
        /* we don't want this provider to update the target analytics table schema */
        return null;
    }
    
    @Override
    protected StructType createSparkSchemaStruct(AnalyticsDataService ads, int targetTenantId, String targetTableName, 
            String schemaString, String primaryKeys, boolean globalTenantAccess, boolean mergeFlag) throws AnalyticsException {
        /* this should not merge with the original analytics table schema, which is the compressed one */
        return AnalyticsCommonUtils.createSparkSchemaStruct(ads, targetTenantId, targetTableName, schemaString, 
                primaryKeys, globalTenantAccess, false);
    }
    
}