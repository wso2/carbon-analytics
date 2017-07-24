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
package org.wso2.carbon.analytics.spark.event;

import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.InsertableRelation;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;
import org.wso2.carbon.analytics.spark.core.util.CarbonScalaUtils;
import scala.reflect.ClassTag$;

import java.io.Serializable;

/**
 * Extends <code>org.apache.spark.sql.sources.BaseRelation</code>
 */
public class StreamRelation extends BaseRelation implements InsertableRelation, Serializable {

    private static final long serialVersionUID = 1628290158392312871L;

    private SQLContext sqlContext;
    
    private StructType schema;
    
    private String streamId;
    
    private int tenantId;

    private boolean globalTenantAccess;

    public StreamRelation(int tenantId,SQLContext sqlContext, String streamId, String payloadString, boolean globalTenantAccess) {
        this.tenantId = tenantId;
        this.sqlContext = sqlContext;
        this.streamId = streamId;
        this.globalTenantAccess = globalTenantAccess;
        this.schema = new StructType(this.extractFieldsFromString(payloadString));
    }

    @Override
    public void insert(DataFrame data, boolean b) {
        for (int i = 0; i < data.rdd().partitions().length; i++) {
            data.sqlContext().sparkContext().runJob(data.rdd(),
                    new EventIteratorFunction(this.tenantId, this.streamId, data.schema(), this.globalTenantAccess),
                    CarbonScalaUtils.getNumberSeq(i, i + 1), false,
                    ClassTag$.MODULE$.Unit());
        }
    }

    @Override
    public SQLContext sqlContext() {
        return this.sqlContext;
    }

    @Override
    public StructType schema() {
        return this.schema;
    }

    private StructField[] extractFieldsFromString(String payloadString) {
        String[] strFields = payloadString.split(",");
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
    
}
