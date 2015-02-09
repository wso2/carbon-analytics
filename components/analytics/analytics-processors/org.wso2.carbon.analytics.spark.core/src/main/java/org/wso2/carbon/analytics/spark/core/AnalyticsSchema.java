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
package org.wso2.carbon.analytics.spark.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.spark.sql.catalyst.types.StructField;
import org.apache.spark.sql.catalyst.types.StructType;
import org.apache.spark.sql.types.util.DataTypeConversions;

import scala.collection.JavaConversions;
import scala.collection.Seq;

/**
 * This class represents a Spark SQL table schema.
 */
public class AnalyticsSchema extends StructType {

    private static final long serialVersionUID = 4405181965742932573L;

    public AnalyticsSchema(Seq<org.apache.spark.sql.catalyst.types.StructField> fields) {
        super(fields);
    }
    
    public AnalyticsSchema(String schemaString) {
        super(extractFields(schemaString));
    }
    
    private static Seq<org.apache.spark.sql.catalyst.types.StructField> extractFields(String schemaString) {
        String[] strFields = schemaString.split(",");
        List<StructField> result = new ArrayList<StructField>();
        String name, type;
        String[] strFieldTokens;
        org.apache.spark.sql.api.java.StructField field;
        for (String strField : strFields) {
            strField = strField.trim();
            strFieldTokens = strField.split(" ");
            name = strFieldTokens[0].trim();
            type = strFieldTokens[1].trim().toLowerCase();
            field = org.apache.spark.sql.api.java.DataType.createStructField(name, parseDataType(type), true);
            result.add(DataTypeConversions.asScalaStructField(field));
        }
        return JavaConversions.asScalaBuffer(result);
    }
    
    private static org.apache.spark.sql.api.java.DataType parseDataType(String strType) {
        if (AnalyticsSparkConstants.INTEGER_TYPE.equals(strType)) {
            return org.apache.spark.sql.api.java.DataType.IntegerType;
        } else if (AnalyticsSparkConstants.INT_TYPE.equals(strType)) {
            return org.apache.spark.sql.api.java.DataType.IntegerType;
        } else if (AnalyticsSparkConstants.FLOAT_TYPE.equals(strType)) {
            return org.apache.spark.sql.api.java.DataType.FloatType;
        } else if (AnalyticsSparkConstants.DOUBLE_TYPE.equals(strType)) {
            return org.apache.spark.sql.api.java.DataType.DoubleType;
        } else if (AnalyticsSparkConstants.LONG_TYPE.equals(strType)) {
            return org.apache.spark.sql.api.java.DataType.LongType;
        } else if (AnalyticsSparkConstants.BOOLEAN_TYPE.equals(strType)) {
            return org.apache.spark.sql.api.java.DataType.BooleanType;
        } else if (AnalyticsSparkConstants.STRING_TYPE.equals(strType)) {
            return org.apache.spark.sql.api.java.DataType.StringType;
        } else {
            throw new RuntimeException("Invalid data type: " + strType);
        }
    }

}
