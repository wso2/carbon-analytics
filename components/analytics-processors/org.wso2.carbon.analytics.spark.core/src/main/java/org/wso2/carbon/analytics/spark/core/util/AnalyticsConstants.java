/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.spark.core.util;

import org.wso2.carbon.registry.core.RegistryConstants;

/**
 * Holds the constants associated with analytics scripts processing, and execution.
 */
public class AnalyticsConstants {
    private AnalyticsConstants() {
        /**
         * Avoid instantiation
         */
    }

    public static final String SCRIPT_EXTENSION = "xml";
    public static final String SCRIPT_EXTENSION_SEPARATOR = ".";
    public static final String DEFAULT_CRON = "DEFAULT";
    public static final String SCRIPT_TASK_TYPE = "ANALYTICS_SPARK";
    public static final String TASK_TENANT_ID_PROPERTY = "TENANT_ID";
    public static final String TASK_SCRIPT_NAME_PROPERTY = "SCRIPT_NAME";
    public static final String ANALYTICS_SCRIPTS_LOCATION = "repository" + RegistryConstants.PATH_SEPARATOR
                                                            + "components" + RegistryConstants.PATH_SEPARATOR + RegistryConstants.PATH_SEPARATOR
                                                            + "org.wso2.carbon.analytics.spark";
    public static final String CARBON_APPLICATION_DEPLOYMENT_DIR = "carbonapps";
    public static final String CARBON_APPLICATION_EXT = ".car";
    public static final String ANALYTICS_MEDIA_TYPE = "application/xml";
    public static final String SPARK_CONF_DIR = "spark";
    public static final String SPARK_UDF_CONF_FILE = "spark-udf-config.xml";
    public static final String DISABLE_ANALYTICS_EXECUTION_JVM_OPTION = "disableAnalyticsExecution";
    public static final String DISABLE_ANALYTICS_ENGINE_JVM_OPTION = "disableAnalyticsEngine";
    public static final String DISABLE_ANALYTICS_SPARK_CTX_JVM_OPTION = "disableAnalyticsSparkCtx";
    public static final String ENABLE_ANALYTICS_STATS_OPTION = "enableAnalyticsStats";

    // spark DataType strings
    public static final String STRING_TYPE = "string";
    public static final String INT_TYPE = "int";
    public static final String INTEGER_TYPE = "integer";
    public static final String BOOLEAN_TYPE = "boolean";
    public static final String DOUBLE_TYPE = "double";
    public static final String FLOAT_TYPE = "float";
    public static final String LONG_TYPE = "long";
    public static final String BINARY_TYPE = "binary";
    public static final String FACET_TYPE = "facet";

    public static final String TERM_TABLE = "table";
    public static final String TERM_CREATE = "create";
    public static final String TERM_TEMPORARY = "temporary";
    public static final String TERM_OPTIONS = "options";
    public static final String TERM_USING = "using";

    public static final String SPARK_DEFAULTS_FILE = "spark-defaults.conf";
    public static final String FAIR_SCHEDULER_XML = "fairscheduler.xml";
    public static final int SPARK_DEFAULT_PARTITION_COUNT = 6;
    public static final String DEFAULT_CARBON_SCHEDULER_POOL_NAME = "carbon-pool";

    //Analytics relation strings
    public static final String INC_TABLE_COMMIT = "incremental_table_commit";
    public static final String INC_TABLE_RESET = "incremental_table_reset";
    public static final String INC_TABLE_SHOW = "incremental_table_show";
    public static final String INC_TABLE = "incremental_table_";
    public static final String TENANT_ID = "tenantId";
    public static final String TABLE_NAME = "tableName";
    public static final String SCHEMA_STRING = "schema";
    public static final String STREAM_NAME = "streamName";
    public static final String PRIMARY_KEYS = "primaryKeys";
    public static final String RECORD_STORE = "recordStore";
    public static final String MERGE_SCHEMA = "mergeSchema";
    public static final String PRESERVE_ORDER = "preserveOrder";
    public static final String GLOBAL_TENANT_ACCESS = "globalTenantAccess";
    public static final String INC_PARAMS = "incrementalParams";
    public static final String TIMESTAMP_FIELD = "_timestamp";
    public static final String TENANT_ID_FIELD = "_tenantId";
    public static final String DEFAULT_PROCESSED_DATA_STORE_NAME = "PROCESSED_DATA_STORE";
    public static final String SPARK_SHORTHAND_STRING = "CarbonAnalytics";
    public static final String SPARK_JDBC_SHORTHAND_STRING = "CarbonJDBC";
    public static final String SPARK_EVENTS_SHORTHAND_STRING = "CarbonEvents";
    public static final String TENANT_ID_AND_TABLES_MAP = "tenantIdTablesMap";
    public static final String MAX_RECORDS = "1000";


    //Carbon Spark properties strings
    public static final String CARBON_SPARK_MASTER_COUNT = "carbon.spark.master.count";
    public static final String CARBON_SPARK_MASTER = "carbon.spark.master";
    public static final String CARBON_DAS_SYMBOLIC_LINK = "carbon.das.symbolic.link";
    public static final String CARBON_INSERT_BATCH_SIZE = "carbon.insert.batch.size";

    public static final int SPARK_PERSISTENCE_TENANT_ID = -5000;
    public static final String SPARK_MASTER_MAP = "__SPARK_MASTER_MAP__";

    //Spark default properties strings
    public static final String SPARK_MASTER_PORT = "spark.master.port";
    public static final String SPARK_MASTER_WEBUI_PORT = "spark.master.webui.port";
    public static final String SPARK_WORKER_CORES = "spark.worker.cores";
    public static final String SPARK_WORKER_MEMORY = "spark.worker.memory";
    public static final String SPARK_WORKER_PORT = "spark.worker.port";
    public static final String SPARK_WORKER_WEBUI_PORT = "spark.worker.webui.port";
    public static final String SPARK_WORKER_DIR = "spark.worker.dir";
    public static final String SPARK_HISTORY_OPTS = "spark.history.opts";

    public static final String SPARK_APP_NAME = "spark.app.name";
    public static final String SPARK_DRIVER_CORES = "spark.driver.cores";
    public static final String SPARK_DRIVER_MEMORY = "spark.driver.memory";
    public static final String SPARK_EXECUTOR_MEMORY = "spark.executor.memory";
    public static final String SPARK_MASTER = "spark.master";

    public static final String SPARK_SCHEDULER_MODE = "spark.scheduler.mode";
    public static final String SPARK_SCHEDULER_POOL = "spark.scheduler.pool";
    public static final String SPARK_SCHEDULER_ALLOCATION_FILE = "spark.scheduler.allocation.file";
    public static final String SPARK_SERIALIZER = "spark.serializer";
    public static final String SPARK_KRYOSERIALIZER_BUFFER_MAX = "spark.kryoserializer.buffer.max";
    public static final String SPARK_KRYOSERIALIZER_BUFFER = "spark.kryoserializer.buffer";
    public static final String SPARK_UI_PORT = "spark.ui.port";
    public static final String SPARK_RECOVERY_MODE = "spark.deploy.recoveryMode";
    public static final String SPARK_RECOVERY_MODE_FACTORY = "spark.deploy.recoveryMode.factory";

    public static final String SPARK_LOCAL_IP_PROP = "SPARK_LOCAL_IP";
    
    // Compressed Event Analytics related strings
    public static final String COMPRESSED_EVENT_ANALYTICS_SHORTHAND = "CompressedEventAnalytics";
    public static final String DATA_COLUMN = "flowData";
    public static final String META_FIELD_COMPRESSED = "meta_compressed";
    public static final String META_FIELD_TENANT_ID = "meta_tenantId";
    public static final String EVENTS_ATTRIBUTE = "events";
    public static final String PAYLOADS_ATTRIBUTE = "payloads";
    public static final String HOST_ATTRIBUTE = "host";

    public enum IncrementalWindowUnit {
        SECOND,
        MINUTE,
        HOUR,
        DAY,
        MONTH,
        YEAR
    }

}
