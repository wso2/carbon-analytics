/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.dataservice.core;

import org.wso2.carbon.analytics.datasource.core.AnalyticsDataSourceConstants;

import java.io.File;

/**
 * This class hold constants that required for data service
 */
public class Constants {

    public static final String PERMISSION_LIST_TABLE = "/permission/admin/manage/analytics/table/list";
    public static final String PERMISSION_CREATE_TABLE = "/permission/admin/manage/analytics/table/create";
    public static final String PERMISSION_DROP_TABLE = "/permission/admin/manage/analytics/table/drop";
    public static final String PERMISSION_LIST_RECORD = "/permission/admin/manage/analytics/records/get";
    public static final String PERMISSION_PUT_RECORD = "/permission/admin/manage/analytics/records/put";
    public static final String PERMISSION_GET_RECORD = "/permission/admin/manage/analytics/records/get";
    public static final String PERMISSION_DELETE_RECORD = "/permission/admin/manage/analytics/records/delete";
    public static final String PERMISSION_SEARCH_RECORD = "/permission/admin/manage/analytics/records/search";
    public static final String DEFAULT_CHARSET = "UTF8";
    public static final String ANALYTICS_SCHEMA_FILE_EXTENSION = "xml";
    public static final String RETENTION_PERIOD = "retentionPeriod";
    public static final String INCLUDE_TABLES = "includeTables";
    public static final String INCLUDE_CLASS_SPLITTER = "###";
    public static final String DISABLE_ANALYTICS_DATA_PURGING_JVM_OPTION = "disableDataPurging";
    public static final int RECORDS_BATCH_SIZE = 1000;
    public static final int DEFAULT_INDEX_REPLICATION_FACTOR = 1;
    public static final String ANALYTICS_DATA_PURGING = "ANALYTICS_DATA_PURGING";
    public static final String TABLE = "table";
    public static final String TENANT_ID = "tenantId";
    public static final String CRON_STRING = "cronString";

    public static final int INDEX_WORKER_STOP_WAIT_TIME = 60000;
    public static final int REINDEX_WORKER_STOP_WAIT_TIME = 60000;
    public static final int TAXONOMY_WORKER_STOP_WAIT_TIME = 60000;
    public static final int META_INFO_TENANT_ID = -1000;
    public static final int GLOBAL_TENANT_TABLE_ACCESS_TENANT_ID = -2000;
    public static final String GLOBAL_SHARD_ALLOCATION_CONFIG_TABLE = "__GLOBAL_SHARD_ALLOCATION_CONFIG__";
    public static final String INDEX_STAGING_DATA_TABLE = "__INDEX_STAGING_DATA__";
    public static final String INDEX_STAGING_DATA_COLUMN = "VALUE";

    public static final long DEFAULT_SHARD_INDEX_RECORD_BATCH_SIZE = 20971520;
    public static final long SHARD_INDEX_RECORD_BATCH_SIZE_MIN = 1000;
    public static final int DEFAULT_SHARD_INDEX_WORKER_INTERVAL = 1500;
    public static final int SHARD_INDEX_WORKER_INTERVAL_MIN = 10;
    public static final int SHARD_INDEX_WORKER_INTERVAL_MAX = 60000;
    public static final int DEFAULT_INDEX_WORKER_COUNT = 1;
    public static final int DEFAULT_MAX_INDEXER_COMMUNICATOR_BUFFER_SIZE = 1024;
    public static final int DEFAULT_INDEXING_QUEUE_CLEANUP_THRESHOLD = 209715200;
    public static final String DEFAULT_TAXONOMY_WRITER_CACHE = "DEFAULT";
    public static final String LRU_TAXONOMY_WRITER_CACHE = "LRU";
    public static final String DEFAULT_LRU_CACHE_TYPE = "STRING";
    public static final String HASHED_LRU_CACHE_TYPE = "HASHED";
    public static final int DEFAULT_LRU_CACHE_SIZE = 4096;
    public static final String DISABLE_INDEXING_ENV_PROP = "disableIndexing";
    public static final String ANALYTICS_INDEXING_GROUP = "__ANALYTICS_INDEXING_GROUP__";

    public static final String DISABLE_LOCAL_INDEX_QUEUE_OPTION = "disableLocalIndexQueue";

    public static final String DEFAULT_INDEX_STORE_LOCATION = AnalyticsDataSourceConstants.CARBON_HOME_VAR
            + File.separator + "repository" + File.separator + "data" + File.separator + "index_data" + File.separator;

    public static final String INDEX_STORE_DIR_PREFIX = "shard";

    public static final String LOCAL_SHARD_ALLOCATION_CONFIG_LOCATION = AnalyticsDataSourceConstants.CARBON_HOME_VAR
            + File.separator + "repository" + File.separator + "data" + File.separator
            + "local-shard-allocation-config.conf";
    public static final String LOCAL_SHARD_REPLICA_CONFIG_LOCATION = AnalyticsDataSourceConstants.CARBON_HOME_VAR
            + File.separator + "repository" + File.separator + "data" + File.separator
            + "local-shard-replica-config.conf";
    public static final String MY_NODEID_LOCATION = AnalyticsDataSourceConstants.CARBON_HOME_VAR + File.separator +
            "repository" + File.separator + "data" + File.separator + "my-node-id.dat";
    public static final String DEPRECATED_LOCAL_SHARD_ALLOCATION_CONFIG_LOCATION = AnalyticsDataSourceConstants
            .CARBON_HOME_VAR + File.separator + "repository" + File.separator + "conf" + File.separator + "analytics"
            + File.separator + "local-shard-allocation-config.conf";
    public static final String DEPRECATED_MY_NODEID_LOCATION = AnalyticsDataSourceConstants
            .CARBON_HOME_VAR + File.separator + "repository" + File.separator + "conf" + File.separator + "analytics"
            + File.separator + "my-node-id.dat";
    public static final String DEFAULT_LOCAL_INDEX_STAGING_LOCATION = AnalyticsDataSourceConstants.CARBON_HOME_VAR + File.separator + "repository"
            + File.separator + "data" + File.separator + "index_staging_queues" + File.separator;
}
