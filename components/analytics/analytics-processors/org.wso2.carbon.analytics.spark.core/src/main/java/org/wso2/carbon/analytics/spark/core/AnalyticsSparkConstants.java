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

/**
 * Constants used in the Carbon Spark integration.
 */
public class AnalyticsSparkConstants {

    public static final String STRING_TYPE = "string";
    public static final String INT_TYPE = "int";
    public static final String INTEGER_TYPE = "integer";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String DOUBLE_TYPE = "double";
    public static final String FLOAT_TYPE = "float";
    public static final String LONG_TYPE = "long";
    public static final String TERM_TABLE = "table";    
    public static final String TERM_DEFINE = "define";    
    public static final String TERM_INSERT = "insert";    
    public static final String TERM_INTO = "into";
    public static final String TERM_PRIMARY = "primary";
    public static final String TERM_KEY = "key";
    
    public static final String TABLE_INFO_TABLE_NAME = "__TABLE_INFO__";
    public static final int TABLE_INFO_TENANT_ID = -1000;
    public static final String OBJECT = "OBJECT";
    
    public static final String DEFAULT_CHARSET = "UTF8";
    
}
