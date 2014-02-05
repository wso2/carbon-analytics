package org.wso2.carbon.bam.cassandra.data.archive.util;

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

import me.prettyprint.hector.api.Cluster;
import org.wso2.carbon.analytics.hive.service.HiveExecutorService;
import org.wso2.carbon.analytics.hive.web.HiveScriptStoreService;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;


public class CassandraArchiveUtil {

    public static final String COLUMN_FAMILY_NAME = "column_family_name";
    public static final String CASSANDRA_PORT = "cassandra_port";
    public static final String CASSANDRA_HOST_IP = "cassandra_host_ip";
    public static final String CASSANDRA_USERNAME = "cassandra_username";
    public static final String CASSANDRA_PASSWORD = "cassandra_password";

    private static DataAccessService dataAccessService;
    private static HiveExecutorService hiveExecutor;
    private static HiveScriptStoreService hiveScriptStoreService;
    private static DataBridgeReceiverService dataBridgeService;
    private static Cluster cassandraCluster;
    public static final String CASSANDRA_ORIGINAL_CF = "cassandra_original_column_family";
    public static final String DEFAULT_CASSANDRA_CLUSTER = "Test Cluster";


    public static void setDataAccessService(DataAccessService dataAccessSrv) {
        dataAccessService = dataAccessSrv;
    }

    public static DataAccessService getDataAccessService() {
        return dataAccessService;
    }

    public static void setHiveExecutorService(HiveExecutorService hiveExecutorService) {
        hiveExecutor = hiveExecutorService;
    }

    public static HiveExecutorService getHiveExecutorService() {
        return hiveExecutor;
    }

    public static void setHiveScriptStoreService(HiveScriptStoreService storeService) {
        hiveScriptStoreService = storeService;
    }

    public static HiveScriptStoreService getHiveScriptStoreService(){
        return hiveScriptStoreService;
    }

    public static void setCluster(Cluster cluster) {
        cassandraCluster = cluster;
    }

    public static Cluster getCassandraCluster() {
        return cassandraCluster;
    }

    public static void setDataBridgeReceiverService(
            DataBridgeReceiverService dataBridgeReceiverService) {
        dataBridgeService = dataBridgeReceiverService;
    }

    public static DataBridgeReceiverService getDataBridgeReceiverService (){
        return dataBridgeService;
    }
}
