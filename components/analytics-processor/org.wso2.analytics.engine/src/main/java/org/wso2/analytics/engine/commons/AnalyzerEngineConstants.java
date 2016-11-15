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

public class AnalyzerEngineConstants {

    private AnalyzerEngineConstants() {
        /**
         * Avoid initialization.
         */
    }

    public static final int SPARK_DEFAULT_PARTITION_COUNT = 6;

    public static final String TABLE_NAME = "tableName";
    public static final String SCHEMA_STRING = "schema";
    public static final String STREAM_NAME = "streamName";
    public static final String PRIMARY_KEYS = "primaryKeys";
    public static final String RECORD_STORE = "recordStore";
    public static final String DEFAULT_PROCESSED_DATA_STORE_NAME = "PROCESSED_DATA_STORE";
    public static final String MERGE_SCHEMA = "mergeSchema";

    // Incremental table parameters
    public static final String INC_PARAMS = "incrementalParams";
    public static final String INC_TABLE = "incremental_table_";
    public static final String INC_TABLE_COMMIT = "incremental_table_commit";
    public static final String INC_TABLE_RESET = "incremental_table_reset";
    public static final String INC_TABLE_SHOW = "incremental_table_show";

    public static final String CARBON_INSERT_BATCH_SIZE = "carbon.insert.batch.size";

    public enum IncrementalWindowUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        MONTH,
        YEAR

    }

    //Spark data types
    public static final String STRING_TYPE = "string";
    public static final String INT_TYPE = "int";
    public static final String INTEGER_TYPE = "integer";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String DOUBLE_TYPE = "double";
    public static final String FLOAT_TYPE = "float";
    public static final String LONG_TYPE = "long";
    public static final String BINARY_TYPE = "binary";
    public static final String FACET_TYPE = "facet";

    // Spark schema details
    public static final String OPTION_IS_FACET = "-f";
    public static final String OPTION_IS_INDEXED = "-i";
    public static final String OPTION_SCORE_PARAM = "-sp";

    // Spark conf location related options
    public static final String SPARK_CONF_FOLDER = "spark";
    public static final String SPARK_MASTER = "spark.master";
    public static final String SPARK_APP_NAME = "spark.app.name";


}


