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

import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;

import java.util.ArrayList;
import java.util.Collections;

public class AnalyticsCommonUtilsTest {

    @Test
    public void testValidateSchemaColumns() throws Exception {
        System.out.println("\n************** START: VALIDATE SCHEMA UTIL METHOD TEST **************");
        StructType sparkSchema = new StructType(new StructField[]{
                new StructField("name", DataTypes.StringType, true, Metadata.empty()),
                new StructField("age", DataTypes.IntegerType, true, Metadata.empty()),
                new StructField("field", DataTypes.StringType, true, Metadata.empty())}
        );

        ArrayList<ColumnDefinition> colDefList = new ArrayList<>();
        colDefList.add(new ColumnDefinition("name", AnalyticsSchema.ColumnType.STRING));
        colDefList.add(new ColumnDefinition("age", AnalyticsSchema.ColumnType.INTEGER));
        colDefList.add(new ColumnDefinition("field", AnalyticsSchema.ColumnType.STRING));
        AnalyticsSchema analyticsSchema = new AnalyticsSchema(colDefList, Collections.<String>emptyList());

        System.out.println("Testing equal schemas.. ");
        Assert.assertTrue(AnalyticsCommonUtils.validateSchemaColumns(sparkSchema, analyticsSchema),
                          "two schemas are expected to be equal, but returned different!");
        System.out.println("\n************** END: VALIDATE SCHEMA UTIL METHOD TEST **************");
    }
}