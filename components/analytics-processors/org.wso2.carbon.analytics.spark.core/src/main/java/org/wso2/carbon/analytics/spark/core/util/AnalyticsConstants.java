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
    public static final String ANALYTICS_MEDIA_TYPE = "application/xml";
    public static final String SPARK_COMPUTE_CLASSPATH_SCRIPT_PATH = "bin/compute-classpath.sh";
    public static final String SPARK_CONF_DIR = "spark";
    public static final String SPARK_UDF_CONF_FILE = "spark-udf-config.xml";
    public static final String DISABLE_ANALYTICS_EXECUTION_JVM_OPTION = "disableAnalyticsExecution";
    public static final String DISABLE_ANALYTICS_ENGINE_JVM_OPTION = "disableAnalyticsEngine";

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
    public static final String TERM_DEFINE = "define";
    public static final String TERM_INSERT = "insert";
    public static final String TERM_INTO = "into";
    public static final String TERM_PRIMARY = "primary";
    public static final String TERM_KEY = "key";
    public static final String TERM_AS = "as";
    public static final String TERM_FROM = "from";
    public static final String TERM_JOIN = "join";
    public static final String TERM_CREATE = "create";
    public static final String TERM_TEMPORARY = "temporary";
    public static final String TERM_USING = "using";
    public static final String TERM_OPTIONS = "options";

    public static final String TABLE_INFO_TABLE_NAME = "__TABLE_INFO__";
    public static final int TABLE_INFO_TENANT_ID = -1000;
    public static final String OBJECT = "OBJECT";
    public static final String DEFAULT_CHARSET = "UTF8";
    public static final String SCRIPT_DEPLOYMENT_DIR = "spark-scripts";
    public static final String SPARK_DEFAULTS_PATH = "spark/spark-defaults.conf";
    public static final int SPARK_DEFAULT_PARTITION_COUNT = 6;

    //Analytics relation strings
    public static final String TENANT_ID = "tenantId";
    public static final String TABLE_NAME = "tableName";
    public static final String SCHEMA_STRING = "schema";
    public static final String STREAM_NAME = "streamName";
    public static final String PRIMARY_KEYS = "primaryKeys";
    public static final String SPARK_SHORTHAND_STRING = "CarbonAnalytics";
    public static final String CARBON_STRING = "carbon";
    public static final String TENANT_ID_AND_TABLES_MAP = "tenantIdTablesMap";
    public static final int MAX_RECORDS = 1000;

    //EventStream relation constants
    public static final String VERSION = "version";
    public static final String DESCRIPTION = "description";
    public static final String NICKNAME = "nickname";
    public static final String PAYLOAD = "payload";

    //Carbon Spark properties strings
    public static final String CARBON_SPARK_MASTER_COUNT = "carbon.spark.master.count";
    public static final String CARBON_SPARK_CLIENT_MODE = "carbon.spark.client.mode";

    //Spark default properties strings
    public static final String SPARK_MASTER_IP = "spark.master.ip";
    public static final String SPARK_MASTER_PORT = "spark.master.port";
    public static final String SPARK_MASTER_WEBUI_PORT = "spark.master.webui.port";
    public static final String SPARK_MASTER_OPTS = "spark.master.opts";
    public static final String SPARK_WORKER_CORES = "spark.worker.cores";
    public static final String SPARK_WORKER_MEMORY = "spark.worker.memory";
    public static final String SPARK_WORKER_PORT = "spark.worker.port";
    public static final String SPARK_WORKER_WEBUI_PORT = "spark.worker.webui.port";
    public static final String SPARK_WORKER_INSTANCES = "spark.worker.instances";
    public static final String SPARK_WORKER_DIR = "spark.worker.dir";
    public static final String SPARK_WORKER_OPTS = "spark.worker.opts";
    public static final String SPARK_HISTORY_OPTS = "spark.history.opts";
    public static final String SPARK_DAEMON_JAVA_OPTS = "spark.daemon.java.opts";
    public static final String SPARK_PUBLIC_DNS = "spark.public.dns";

    public static final String SPARK_APP_NAME = "spark.app.name";
    public static final String SPARK_DRIVER_CORES = "spark.driver.cores";
    public static final String SPARK_DRIVER_MAXRESULTSIZE = "spark.driver.maxResultSize";
    public static final String SPARK_DRIVER_MEMORY = "spark.driver.memory";
    public static final String SPARK_EXECUTOR_MEMORY = "spark.executor.memory";
    public static final String SPARK_EXTRALISTENERS = "spark.extraListeners";
    public static final String SPARK_LOCAL_DIR = "spark.local.dir";
    public static final String SPARK_LOGCONF = "spark.logConf";
    public static final String SPARK_MASTER = "spark.master";

    public static final String SPARK_UI_PORT = "spark.ui.port";

    public static final String SPARK_RECOVERY_MODE = "spark.deploy.recoveryMode";

    public static final String SPARK_RECOVERY_MODE_FACTORY = "spark.deploy.recoveryMode.factory";

    //Extra constants available for Spark
/*  public static final String SPARK_DRIVER_EXTRACLASSPATH = "spark.driver.extraClassPath";
    public static final String SPARK_DRIVER_EXTRAJAVAOPTIONS = "spark.driver.extraJavaOptions";
    public static final String SPARK_DRIVER_EXTRALIBRARYPATH = "spark.driver.extraLibraryPath";
    public static final String SPARK_DRIVER_USERCLASSPATHFIRST = "spark.driver.userClassPathFirst";
    public static final String SPARK_EXECUTOR_EXTRACLASSPATH = "spark.executor.extraClassPath";
    public static final String SPARK_EXECUTOR_EXTRAJAVAOPTIONS = "spark.executor.extraJavaOptions";
    public static final String SPARK_EXECUTOR_EXTRALIBRARYPATH = "spark.executor.extraLibraryPath";
    public static final String SPARK_EXECUTOR_LOGS_ROLLING_MAXRETAINEDFILES = "spark.executor.logs.rolling.maxRetainedFiles";
    public static final String SPARK_EXECUTOR_LOGS_ROLLING_SIZE_MAXBYTES = "spark.executor.logs.rolling.size.maxBytes";
    public static final String SPARK_EXECUTOR_LOGS_ROLLING_STRATEGY = "spark.executor.logs.rolling.strategy";
    public static final String SPARK_EXECUTOR_LOGS_ROLLING_TIME_INTERVAL = "spark.executor.logs.rolling.time.interval";
    public static final String SPARK_EXECUTOR_USERCLASSPATHFIRST = "spark.executor.userClassPathFirst";
    public static final String SPARK_REDUCER_MAXMBINFLIGHT = "spark.reducer.maxMbInFlight";
    public static final String SPARK_SHUFFLE_BLOCKTRANSFERSERVICE = "spark.shuffle.blockTransferService";
    public static final String SPARK_SHUFFLE_COMPRESS = "spark.shuffle.compress";
    public static final String SPARK_SHUFFLE_CONSOLIDATEFILES = "spark.shuffle.consolidateFiles";
    public static final String SPARK_SHUFFLE_FILE_BUFFER_KB = "spark.shuffle.file.buffer.kb";
    public static final String SPARK_SHUFFLE_IO_MAXRETRIES = "spark.shuffle.io.maxRetries";
    public static final String SPARK_SHUFFLE_IO_NUMCONNECTIONSPERPEER = "spark.shuffle.io.numConnectionsPerPeer";
    public static final String SPARK_SHUFFLE_IO_PREFERDIRECTBUFS = "spark.shuffle.io.preferDirectBufs";
    public static final String SPARK_SHUFFLE_IO_RETRYWAIT = "spark.shuffle.io.retryWait";
    public static final String SPARK_SHUFFLE_MANAGER = "spark.shuffle.manager";
    public static final String SPARK_SHUFFLE_MEMORYFRACTION = "spark.shuffle.memoryFraction";
    public static final String SPARK_SHUFFLE_SORT_BYPASSMERGETHRESHOLD = "spark.shuffle.sort.bypassMergeThreshold";
    public static final String SPARK_SHUFFLE_SPILL = "spark.shuffle.spill";
    public static final String SPARK_SHUFFLE_SPILL_COMPRESS = "spark.shuffle.spill.compress";
    public static final String SPARK_EVENTLOG_COMPRESS = "spark.eventLog.compress";
    public static final String SPARK_EVENTLOG_DIR = "spark.eventLog.dir";
    public static final String SPARK_EVENTLOG_ENABLED = "spark.eventLog.enabled";
    public static final String SPARK_UI_KILLENABLED = "spark.ui.killEnabled";
    public static final String SPARK_UI_RETAINEDJOBS = "spark.ui.retainedJobs";
    public static final String SPARK_UI_RETAINEDSTAGES = "spark.ui.retainedStages";
    public static final String SPARK_BROADCAST_COMPRESS = "spark.broadcast.compress";
    public static final String SPARK_CLOSURE_SERIALIZER = "spark.closure.serializer";
    public static final String SPARK_IO_COMPRESSION_CODEC = "spark.io.compression.codec";
    public static final String SPARK_IO_COMPRESSION_LZ4_BLOCK_SIZE = "spark.io.compression.lz4.block.size";
    public static final String SPARK_IO_COMPRESSION_SNAPPY_BLOCK_SIZE = "spark.io.compression.snappy.block.size";
    public static final String SPARK_KRYO_CLASSESTOREGISTER = "spark.kryo.classesToRegister";
    public static final String SPARK_KRYO_REFERENCETRACKING = "spark.kryo.referenceTracking";
    public static final String SPARK_KRYO_REGISTRATIONREQUIRED = "spark.kryo.registrationRequired";
    public static final String SPARK_KRYO_REGISTRATOR = "spark.kryo.registrator";
    public static final String SPARK_KRYOSERIALIZER_BUFFER_MAX_MB = "spark.kryoserializer.buffer.max.mb";
    public static final String SPARK_KRYOSERIALIZER_BUFFER_MB = "spark.kryoserializer.buffer.mb";
    public static final String SPARK_RDD_COMPRESS = "spark.rdd.compress";
    public static final String SPARK_SERIALIZER = "spark.serializer";
    public static final String SPARK_SERIALIZER_OBJECTSTREAMRESET = "spark.serializer.objectStreamReset";
    public static final String SPARK_BROADCAST_BLOCKSIZE = "spark.broadcast.blockSize";
    public static final String SPARK_BROADCAST_FACTORY = "spark.broadcast.factory";
    public static final String SPARK_CLEANER_TTL = "spark.cleaner.ttl";
    public static final String SPARK_DEFAULT_PARALLELISM = "spark.default.parallelism";
    public static final String SPARK_EXECUTOR_HEARTBEATINTERVAL = "spark.executor.heartbeatInterval";
    public static final String SPARK_FILES_FETCHTIMEOUT = "spark.files.fetchTimeout";
    public static final String SPARK_FILES_USEFETCHCACHE = "spark.files.useFetchCache";
    public static final String SPARK_FILES_OVERWRITE = "spark.files.overwrite";
    public static final String SPARK_HADOOP_CLONECONF = "spark.hadoop.cloneConf";
    public static final String SPARK_HADOOP_VALIDATEOUTPUTSPECS = "spark.hadoop.validateOutputSpecs";
    public static final String SPARK_STORAGE_MEMORYFRACTION = "spark.storage.memoryFraction";
    public static final String SPARK_STORAGE_MEMORYMAPTHRESHOLD = "spark.storage.memoryMapThreshold";
    public static final String SPARK_STORAGE_UNROLLFRACTION = "spark.storage.unrollFraction";
    public static final String SPARK_TACHYONSTORE_BASEDIR = "spark.tachyonStore.baseDir";
    public static final String SPARK_TACHYONSTORE_URL = "spark.tachyonStore.url";
    public static final String SPARK_AKKA_FAILURE_DETECTOR_THRESHOLD = "spark.akka.failure-detector.threshold";
    public static final String SPARK_AKKA_FRAMESIZE = "spark.akka.frameSize";
    public static final String SPARK_AKKA_HEARTBEAT_INTERVAL = "spark.akka.heartbeat.interval";
    public static final String SPARK_AKKA_HEARTBEAT_PAUSES = "spark.akka.heartbeat.pauses";
    public static final String SPARK_AKKA_THREADS = "spark.akka.threads";
    public static final String SPARK_AKKA_TIMEOUT = "spark.akka.timeout";
    public static final String SPARK_BLOCKMANAGER_PORT = "spark.blockManager.port";
    public static final String SPARK_BROADCAST_PORT = "spark.broadcast.port";
    public static final String SPARK_DRIVER_HOST = "spark.driver.host";
    public static final String SPARK_DRIVER_PORT = "spark.driver.port";
    public static final String SPARK_EXECUTOR_PORT = "spark.executor.port";
    public static final String SPARK_FILESERVER_PORT = "spark.fileserver.port";
    public static final String SPARK_NETWORK_TIMEOUT = "spark.network.timeout";
    public static final String SPARK_PORT_MAXRETRIES = "spark.port.maxRetries";
    public static final String SPARK_REPLCLASSSERVER_PORT = "spark.replClassServer.port";
    public static final String SPARK_CORES_MAX = "spark.cores.max";
    public static final String SPARK_LOCALEXECUTION_ENABLED = "spark.localExecution.enabled";
    public static final String SPARK_LOCALITY_WAIT = "spark.locality.wait";
    public static final String SPARK_LOCALITY_WAIT_NODE = "spark.locality.wait.node";
    public static final String SPARK_LOCALITY_WAIT_PROCESS = "spark.locality.wait.process";
    public static final String SPARK_LOCALITY_WAIT_RACK = "spark.locality.wait.rack";
    public static final String SPARK_SCHEDULER_MAXREGISTEREDRESOURCESWAITINGTIME = "spark.scheduler.maxRegisteredResourcesWaitingTime";
    public static final String SPARK_SCHEDULER_MINREGISTEREDRESOURCESRATIO = "spark.scheduler.minRegisteredResourcesRatio";
    public static final String SPARK_SCHEDULER_MODE = "spark.scheduler.mode";
    public static final String SPARK_SCHEDULER_REVIVE_INTERVAL = "spark.scheduler.revive.interval";
    public static final String SPARK_SPECULATION = "spark.speculation";
    public static final String SPARK_SPECULATION_INTERVAL = "spark.speculation.interval";
    public static final String SPARK_SPECULATION_MULTIPLIER = "spark.speculation.multiplier";
    public static final String SPARK_SPECULATION_QUANTILE = "spark.speculation.quantile";
    public static final String SPARK_TASK_CPUS = "spark.task.cpus";
    public static final String SPARK_TASK_MAXFAILURES = "spark.task.maxFailures";
    public static final String SPARK_DYNAMICALLOCATION_ENABLED = "spark.dynamicAllocation.enabled";
    public static final String SPARK_DYNAMICALLOCATION_EXECUTORIDLETIMEOUT = "spark.dynamicAllocation.executorIdleTimeout";
    public static final String SPARK_DYNAMICALLOCATION_INITIALEXECUTORS = "spark.dynamicAllocation.initialExecutors";
    public static final String SPARK_DYNAMICALLOCATION_MAXEXECUTORS = "spark.dynamicAllocation.maxExecutors";
    public static final String SPARK_DYNAMICALLOCATION_MINEXECUTORS = "spark.dynamicAllocation.minExecutors";
    public static final String SPARK_DYNAMICALLOCATION_SCHEDULERBACKLOGTIMEOUT = "spark.dynamicAllocation.schedulerBacklogTimeout";
    public static final String SPARK_DYNAMICALLOCATION_SUSTAINEDSCHEDULERBACKLOGTIMEOUT = "spark.dynamicAllocation.sustainedSchedulerBacklogTimeout";
    public static final String SPARK_ACLS_ENABLE = "spark.acls.enable";
    public static final String SPARK_ADMIN_ACLS = "spark.admin.acls";
    public static final String SPARK_AUTHENTICATE = "spark.authenticate";
    public static final String SPARK_AUTHENTICATE_SECRET = "spark.authenticate.secret";
    public static final String SPARK_CORE_CONNECTION_ACK_WAIT_TIMEOUT = "spark.core.connection.ack.wait.timeout";
    public static final String SPARK_CORE_CONNECTION_AUTH_WAIT_TIMEOUT = "spark.core.connection.auth.wait.timeout";
    public static final String SPARK_MODIFY_ACLS = "spark.modify.acls";
    public static final String SPARK_UI_FILTERS = "spark.ui.filters";
    public static final String SPARK_UI_VIEW_ACLS = "spark.ui.view.acls";
    public static final String SPARK_SSL_ENABLED = "spark.ssl.enabled";
    public static final String SPARK_SSL_ENABLEDALGORITHMS = "spark.ssl.enabledAlgorithms";
    public static final String SPARK_SSL_KEYPASSWORD = "spark.ssl.keyPassword";
    public static final String SPARK_SSL_KEYSTORE = "spark.ssl.keyStore";
    public static final String SPARK_SSL_KEYSTOREPASSWORD = "spark.ssl.keyStorePassword";
    public static final String SPARK_SSL_PROTOCOL = "spark.ssl.protocol";
    public static final String SPARK_SSL_TRUSTSTORE = "spark.ssl.trustStore";
    public static final String SPARK_SSL_TRUSTSTOREPASSWORD = "spark.ssl.trustStorePassword";*/


}
