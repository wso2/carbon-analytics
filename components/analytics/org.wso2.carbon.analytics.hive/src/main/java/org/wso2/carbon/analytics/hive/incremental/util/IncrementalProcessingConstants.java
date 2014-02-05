package org.wso2.carbon.analytics.hive.incremental.util;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class IncrementalProcessingConstants {

    public static final String ENABLED = "true";

    public static final String HIVE_INCREMENTAL_METASTORE_DATASOURCE = "WSO2BAM_HIVE_INCREMENTAL_DATASOURCE";
    public static final String MARKER_CF_NAME = "HIVE_MARKER_META_DATA";

    public static final String LAST_ACCESSED_TIME_COLUMN_NAME = "lastAccessedTime";

    public static final String DATASOURCE_PROPS_KEYSPACE = "keyspaceName";
    public static final String DATASOURCE_PROPS_USERNAME = "username";
    public static final String DATASOURCE_PROPS_PASSWORD = "password";
    public static final String DATASOURCE_PROPS_REPLICATION_FACTOR = "replicationFactor";
    public static final String DATASOURCE_PROPS_READ_CONSISTENCY = "readConsistencyLevel";
    public static final String DATASOURCE_PROPS_WRITE_CONSISTENCY = "WriteConsistencyLevel";
    public static final String DATASOURCE_PROPS_STRATEGY_CLASS = "strategyClass";

    public static final String INCREMENTAL_MARKER_NAME_PROPERY = "name";
    public static final String INCREMENTAL_MARKER_TABLES = "tables";
    public static final String INCREMENTAL_BUFFER_TIME = "bufferTime";
    public static final String INCREMENTAL_FROM_TIME = "fromTime";
    public static final String INCREMENTAL_TO_TIME = "toTime";
    public static final String SCRIPT_NAME = "scriptName";
    public static final String HAS_NON_INDEXED_DATA = "hasNonIndexedData";
    public static final String TRUE = "true";
    public static final String FALSE = "false";
    public static final String LAST_ACCESSED_TIME = "lastAccessedTime";
    public static final String SKIP_INCREMENTAL_PROCESS = "skipIncrementalProcess";

    public static final String CURRENT_TIME = "$now";
    public static final String DAYS = "d";
}


