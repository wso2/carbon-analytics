/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.spark.core.internal;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.deploy.master.LeaderElectable;
import org.apache.spark.deploy.master.Master;
import org.apache.spark.deploy.worker.Worker;
import org.apache.spark.serializer.KryoSerializer;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.apache.spark.sql.jdbc.carbon.AnalyticsJDBCRelationProvider;
import org.apache.spark.util.Utils;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.core.Constants;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.dataservice.core.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.core.clustering.GroupEventListener;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.AnalyticsExecutionCall;
import org.wso2.carbon.analytics.spark.core.deploy.AnalyticsPersistenceEngine;
import org.wso2.carbon.analytics.spark.core.deploy.AnalyticsRecoveryModeFactory;
import org.wso2.carbon.analytics.spark.core.deploy.CheckElectedLeaderExecutionCall;
import org.wso2.carbon.analytics.spark.core.deploy.ElectLeaderExecutionCall;
import org.wso2.carbon.analytics.spark.core.deploy.InitClientExecutionCall;
import org.wso2.carbon.analytics.spark.core.deploy.StartWorkerExecutionCall;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsUDFException;
import org.wso2.carbon.analytics.spark.core.sources.AnalyticsRelationProvider;
import org.wso2.carbon.analytics.spark.core.sources.CompressedEventAnalyticsRelationProvider;
import org.wso2.carbon.analytics.spark.core.udf.AnalyticsUDFsRegister;
import org.wso2.carbon.analytics.spark.core.udf.CarbonUDAF;
import org.wso2.carbon.analytics.spark.core.udf.CarbonUDF;
import org.wso2.carbon.analytics.spark.core.udf.config.CustomUDAF;
import org.wso2.carbon.analytics.spark.core.udf.config.UDFConfiguration;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;
import org.wso2.carbon.analytics.spark.core.util.SparkTableNamesHolder;
import org.wso2.carbon.analytics.spark.utils.ComputeClasspath;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.CarbonUtils;
import scala.Option;
import scala.Tuple2;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the analytics query execution context.
 */
public class SparkAnalyticsExecutor implements GroupEventListener {

    private static final String CLUSTER_GROUP_NAME = "CARBON_ANALYTICS_EXECUTION";

    private static final String DEFAULT_SPARK_APP_NAME = "DefaultCarbonAnalyticsApp";

    private static final Log log = LogFactory.getLog(SparkAnalyticsExecutor.class);

    private String sparkMaster;

    private SparkConf sparkConf;

    private SQLContext sqlCtx;

    private String myHost;

    private int portOffset = 0;

    private int workerCount = 1;

    private SparkTableNamesHolder sparkTableNamesHolder;

    private UDFConfiguration udfConfiguration;

    private int redundantMasterCount = 1;

    private Set<LeaderElectable> leaderElectable = new HashSet<>();

    private AnalyticsClusterManager acm;

    private boolean masterActive = false;

    private boolean workerActive = false;

    private boolean clientActive = false;

    private boolean electedLeader = false;

    private Map<String, String> shorthandStringsMap = new HashMap<>();

    private static final int MAX_RETRIES = 30;

    private static final long MAX_RETRY_WAIT_INTERVAL = 60000L;

    private ClusterMode clusterMode;

    public SparkAnalyticsExecutor(String myHost, int portOffset) throws AnalyticsException {
        this.myHost = myHost;
        this.portOffset = portOffset;
        this.udfConfiguration = this.loadUDFConfiguration();

        this.acm = AnalyticsServiceHolder.getAnalyticsClusterManager();

        // sends this host name and base master port to initialize the spark conf
        String propsFile = GenericUtils.getAnalyticsConfDirectory() + File.separator +
                           "analytics" + File.separator +
                           AnalyticsConstants.SPARK_CONF_DIR + File.separator +
                           AnalyticsConstants.SPARK_DEFAULTS_FILE;
        if (!new File(propsFile).exists()) {
            throw new AnalyticsExecutionException("spark-defaults.conf file does not exists in path "
                                                  + propsFile);
        }
        this.sparkConf = initializeSparkConf(this.portOffset, propsFile);

        this.sparkMaster = getStringFromSparkConf(AnalyticsConstants.CARBON_SPARK_MASTER, "local");
        this.clusterMode = getClusterMode(this.sparkMaster);
        this.redundantMasterCount = this.sparkConf.getInt(AnalyticsConstants.CARBON_SPARK_MASTER_COUNT, 1);

        this.sparkTableNamesHolder = new SparkTableNamesHolder(this.acm.isClusteringEnabled());
        this.registerShorthandStrings();
    }

    /**
     * @throws AnalyticsClusterException
     */
    public void initializeSparkServer() throws AnalyticsException {
        this.sparkConf.setMaster(this.sparkMaster);

        switch (clusterMode) {
            case local:
            case standaloneSpark:
            case yarn:
            case mesos:
                log.info("Starting SPARK in the Client " + clusterMode.toString() + ". Master : " + this.sparkMaster);
                if (acm.isClusteringEnabled()) {
                    acm.joinGroup(CLUSTER_GROUP_NAME, this);
                }
                initializeAnalyticsClient();
                break;
            case carbonSpark:
                log.info("Starting SPARK in the Carbon Clustering mode");
                this.redundantMasterCount = this.sparkConf.getInt(AnalyticsConstants.CARBON_SPARK_MASTER_COUNT, 2);

                if (this.sparkTableNamesHolder == null) {
                    this.sparkTableNamesHolder = new SparkTableNamesHolder(true);
                }
                if (acm.isClusteringEnabled()) {
                    this.runClusteredSetupLogic();
                } else {
                    throw new AnalyticsClusterException("Spark started in the cluster mode without " +
                                                        "enabling Carbon Clustering");
                }
                break;
        }
    }

    private void runClusteredSetupLogic() throws AnalyticsException {
        //port offsetted master name
        String thisMasterUrl = "spark://" + this.myHost + ":" + this.sparkConf.
                get(AnalyticsConstants.SPARK_MASTER_PORT);
        logDebug("Spark master URL for this node : " + thisMasterUrl);

        HazelcastInstance hz = AnalyticsServiceHolder.getHazelcastInstance();
        Map<String, Object> masterMap = hz.getMap(AnalyticsConstants.SPARK_MASTER_MAP);

        Object localMember = acm.getLocalMember();
        log.info("Local member : " + localMember);
        Set<String> masterUrls = masterMap.keySet();
        logDebug("Master URLs : " + Arrays.toString(masterUrls.toArray()));

        //start master logic
        log.info("Current Spark Master map size : " + masterMap.size());
        if (masterUrls.contains(thisMasterUrl) || masterMap.size() < this.redundantMasterCount) {
            log.info("Masters available are less than the redundant master count or " +
                     "This is/ has been a member of the MasterMap");

            if (!masterUrls.contains(thisMasterUrl)) {
                log.info("Adding member to the Spark Master map : " + localMember);
                masterMap.put(thisMasterUrl, localMember);
            }

            log.info("Starting SPARK MASTER...");
            this.startMaster();
        }

        if (acm.getMembers(CLUSTER_GROUP_NAME).size() == 0) {
            log.info("Analytics Execution cluster is empty. Hence cleaning up Spark meta data...");
            cleanupSparkMetaTable();
        }

        acm.joinGroup(CLUSTER_GROUP_NAME, this);
        log.info("Member joined the Carbon Analytics Execution cluster : " + localMember);

        this.processLeaderElectable();

        // start worker and client logic
        log.info("Spark Master map size after starting masters : " + masterMap.size());
        if (masterMap.size() >= this.redundantMasterCount) {
            log.info("Redundant master count reached. Starting workers in all members...");
            this.acm.executeAll(CLUSTER_GROUP_NAME, new StartWorkerExecutionCall());

            log.info("Redundant master count reached. Starting Spark client app in " +
                     "the carbon cluster master...");
            this.initializeAnalyticsClient();
        }
    }

    private void cleanupSparkMetaTable() throws AnalyticsClusterException {
        try {
            AnalyticsPersistenceEngine.cleanupSparkMetaTable();
        } catch (AnalyticsException e) {
            throw new AnalyticsClusterException("Unable to cleanup the Spark Meta table", e);
        }
    }

    private String[] getSparkMastersFromCluster() {
        HazelcastInstance hz = AnalyticsServiceHolder.getHazelcastInstance();
        IMap<String, Object> masterMap = hz.getMap(AnalyticsConstants.SPARK_MASTER_MAP);
        Set<String> masterUrls = masterMap.keySet();
        return masterUrls.toArray(new String[masterUrls.size()]);
    }

    private UDFConfiguration loadUDFConfiguration() throws AnalyticsException {
        try {
            File confFile = new File(GenericUtils.getAnalyticsConfDirectory() +
                                     File.separator + "analytics" +
                                     File.separator + AnalyticsConstants.SPARK_CONF_DIR +
                                     File.separator + AnalyticsConstants.SPARK_UDF_CONF_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsUDFException("Cannot load UDFs, " +
                                                "the UDF configuration file cannot be found at: " +
                                                confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(UDFConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (UDFConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsUDFException(
                    "Error in processing UDF configuration: " + e.getMessage(), e);
        }
    }

    private void initializeAnalyticsClient() throws AnalyticsException {
        if (acm.isClusteringEnabled()) {
            log.info("Sending a cluster message to the leader to initialize the Spark application");
            this.acm.executeOne(CLUSTER_GROUP_NAME, acm.getLeader(CLUSTER_GROUP_NAME),
                                new InitClientExecutionCall());
        } else {
            log.info("Initializing the Spark application locally");
            this.initializeAnalyticsClientLocal();
        }
    }

    public synchronized void initializeAnalyticsClientLocal() throws AnalyticsException {
        if (ServiceHolder.isAnalyticsSparkContextEnabled()) {
            if (!this.clientActive) {
                //master URL needs to be updated according to the spark masters in the cluster if carbon clustering is used
                if (this.clusterMode == ClusterMode.carbonSpark) {
                    updateMaster(this.sparkConf);
                }
                initializeSqlContext(this.initializeSparkContext(this.sparkConf));
                this.clientActive = true;
                log.info("Started Spark CLIENT in the cluster pointing to MASTER " + this.sparkConf.get(AnalyticsConstants.SPARK_MASTER) +
                         " with the application name : " + this.sparkConf.get(AnalyticsConstants.SPARK_APP_NAME) +
                         " and UI port : " + this.sparkConf.get(AnalyticsConstants.SPARK_UI_PORT));
            } else {
                log.info("Client is already active in this node, therefore ignoring client init");
            }
        } else {
            this.logDebug("Analytics Spark Context is disabled in this node, therefore ignoring the client initiation.");
        }
    }

    private JavaSparkContext initializeSparkContext(SparkConf conf) throws AnalyticsException {
        JavaSparkContext jsc;
        try {
            jsc = new JavaSparkContext(conf);
        } catch (Throwable e) {
            throw new AnalyticsException("Unable to create analytics client. " + e.getMessage(), e);
        }
        ServiceHolder.setJavaSparkContext(jsc);
        return jsc;
    }

    private void initializeSqlContext(JavaSparkContext jsc) throws AnalyticsUDFException {
        this.sqlCtx = new SQLContext(jsc);
        registerUDFs(this.sqlCtx);
        registerUDAFs(this.sqlCtx);
    }

    public void registerUDFFromOSGIComponent(CarbonUDF carbonUDF) throws AnalyticsUDFException {
        if (this.sqlCtx != null) {
            AnalyticsUDFsRegister analyticsUDFsRegister = AnalyticsUDFsRegister.getInstance();
            Class udf = carbonUDF.getClass();
            Method[] methods = udf.getDeclaredMethods();
            for (Method method : methods) {
                if (Modifier.isPublic(method.getModifiers())) {
                    analyticsUDFsRegister.registerUDF(udf, method, sqlCtx);
                }
            }
        } else {
            ServiceHolder.addCarbonUDFs(carbonUDF);
        }
    }

    public void registerUDAFFromOSGIComponent(CarbonUDAF carbonUDAF) throws AnalyticsUDFException {
        if (this.sqlCtx != null) {
            AnalyticsUDFsRegister analyticsUDFsRegister = AnalyticsUDFsRegister.getInstance();
            String name = carbonUDAF.getAlias();
            Class<? extends UserDefinedAggregateFunction> clazz = carbonUDAF.getClass();
            analyticsUDFsRegister.registerUDAF(name, clazz, sqlCtx);
        } else {
            ServiceHolder.addCarbonUDAFs(carbonUDAF);
        }
    }

    private void registerUDAFs(SQLContext sqlCtx) throws AnalyticsUDFException {
        Map<String, Class<? extends UserDefinedAggregateFunction>> udafMap = new HashMap<>();
        if (this.udfConfiguration.getCustomUDAFs() != null && !this.udfConfiguration.getCustomUDAFs().isEmpty()) {
            for (CustomUDAF udaf : this.udfConfiguration.getCustomUDAFs()) {
                try {
                    if (!udaf.getAlias().isEmpty() && !udaf.getImplClass().isEmpty()) {
                        Class<? extends UserDefinedAggregateFunction> clazz = Class.forName(udaf.getImplClass()).asSubclass(UserDefinedAggregateFunction.class);
                        udafMap.put(udaf.getAlias(), clazz);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        if (!ServiceHolder.getCarbonUDAFs().isEmpty()) {
            udafMap.putAll(ServiceHolder.getCarbonUDAFs());
        }
        AnalyticsUDFsRegister udafRegister = AnalyticsUDFsRegister.getInstance();
        for (String udaf : udafMap.keySet()) {
            udafRegister.registerUDAF(udaf, udafMap.get(udaf), sqlCtx);
        }
    }

    private void registerUDFs(SQLContext sqlCtx)
            throws AnalyticsUDFException {
        List<String> udfClassNames = new ArrayList<>();
        if (!this.udfConfiguration.getCustomUDFClass().isEmpty()) {
            udfClassNames.addAll(this.udfConfiguration.getCustomUDFClass());
        }
        if (!ServiceHolder.getCarbonUDFs().isEmpty()) {
            //get the class names as String from the map's keySet.
            udfClassNames.addAll(ServiceHolder.getCarbonUDFs().keySet());
        }
        AnalyticsUDFsRegister udfAdaptorBuilder = AnalyticsUDFsRegister.getInstance();
        try {
            for (String udfClassName : udfClassNames) {
                udfClassName = udfClassName.trim();
                if (!udfClassName.isEmpty()) {
                    Class udf = Class.forName(udfClassName);
                    Method[] methods = udf.getDeclaredMethods();
                    for (Method method : methods) {
                        try {
                            if (Modifier.isPublic(method.getModifiers())) {
                                udfAdaptorBuilder.registerUDF(udf, method, sqlCtx);
                            }
                        } catch (AnalyticsUDFException e) {
                            log.error("Error while registering the UDF method: " + method.getName() + ", " + e.getMessage(), e);
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new AnalyticsUDFException("Error While registering UDFs: " + e.getMessage(), e);
        }
    }

    /**
     * this method starts a spark master with a given parameters.
     */
    private synchronized void startMaster() throws AnalyticsClusterException {
        if (!this.masterActive) {
            String host = this.myHost;
            int port = this.sparkConf.getInt(AnalyticsConstants.SPARK_MASTER_PORT, 7077 + this.portOffset);
            int webUiPort = this.sparkConf.getInt(AnalyticsConstants.SPARK_MASTER_WEBUI_PORT, 8081 + this.portOffset);

            Master.startRpcEnvAndEndpoint(host, port, webUiPort, this.sparkConf);

            log.info("Started SPARK MASTER in spark://" + host + ":" + port + " with webUI port : " + webUiPort);

            updateMaster(this.sparkConf);
            this.masterActive = true;
        } else {
            logDebug("Master is already active in this node, therefore ignoring Master startup");
        }
//        processLeaderElectable();
    }

    /**
     * sends a cluster message and checks if there is an elected leader
     * if there is no elected leader, it will make this master, the elected leader
     */
    private void processLeaderElectable() throws AnalyticsClusterException {
        if (!isElectedLeaderAvailable()) {
            log.info("No elected leader is available. Hence electing this member as the leader");
            this.electAsLeader();
        }
    }

    private boolean isElectedLeaderAvailable() throws AnalyticsClusterException {
        if (acm.getMembers(CLUSTER_GROUP_NAME).isEmpty()) {
            log.info("Cluster is empty. Hence no elected leader available");
            return false;
        }

        List<Boolean> clusterElectedLeaders = acm.executeAll(CLUSTER_GROUP_NAME,
                                                             new CheckElectedLeaderExecutionCall());
        return clusterElectedLeaders.contains(true);
    }

    public boolean isElectedLeader() {
        return electedLeader;
    }

    /**
     * updates the spark master string of a given spark context by looking at the spark masters
     * map in the cluster
     *
     * @param conf spark conf
     */
    private void updateMaster(SparkConf conf) {
        String[] masters = getSparkMastersFromCluster();
        StringBuilder buf = new StringBuilder();
        buf.append("spark://");
        for (int i = 0; i < masters.length; i++) {
            buf.append(masters[i].replace("spark://", ""));
            if (i < masters.length - 1) {
                buf.append(",");
            }
        }
        conf.setMaster(buf.toString());
    }

    /**
     * this method checks the existing jvm properties and add the port offset to properties which
     * starts with "spark." and ends with ".port". also, sets the relevant spark conf properties
     *
     * @param conf       spark conf
     * @param portOffset port offset
     */
    private void addSparkPropertiesPortOffset(SparkConf conf, int portOffset) {
        Tuple2<String, String>[] properties = conf.getAll();

        for (Tuple2<String, String> prop : properties) {
            String key = prop._1().trim();
            if (key.startsWith("spark.") && key.endsWith(".port")) {
                String withPortOffset = Integer.toString(Integer.parseInt(prop._2()) + portOffset);
                conf.set(key, withPortOffset);
            }
        }
    }

    /**
     * this starts a worker with given parameters. it reads the spark defaults from
     * the given properties file and override parameters accordingly. it also adds the port offset
     * to all the port configurations
     */
    public synchronized void startWorker() {
        if (!this.workerActive) {
            String workerHost = this.myHost;
            int workerPort = this.sparkConf.getInt(AnalyticsConstants.SPARK_WORKER_PORT, 10000 + this.portOffset);
            int workerUiPort = this.sparkConf.getInt(AnalyticsConstants.SPARK_WORKER_WEBUI_PORT, 10500 + this.portOffset);
            int workerCores = this.sparkConf.getInt(AnalyticsConstants.SPARK_WORKER_CORES, 1);
            String workerMemory = getStringFromSparkConf(AnalyticsConstants.SPARK_WORKER_MEMORY, "1g");
            String[] masters = this.getSparkMastersFromCluster();
            String workerDir = getStringFromSparkConf(AnalyticsConstants.SPARK_WORKER_DIR, "work");

            Worker.startRpcEnvAndEndpoint(workerHost, workerPort, workerUiPort, workerCores,
                                          Utils.memoryStringToMb(workerMemory), masters, workerDir,
                                          Option.empty(), this.sparkConf);

            log.info("Started SPARK WORKER in " + workerHost + ":" + workerPort + " with webUI port "
                     + workerUiPort + " with Masters " + Arrays.toString(masters));

            this.workerActive = true;
        } else {
            logDebug("Worker is already active in this node, therefore ignoring worker startup");
        }
    }

    /**
     * this method initializes spark conf with default properties
     * it also reads the spark defaults from
     * the given properties file and override parameters accordingly. it also adds the port offset
     * to all the port configurations
     *
     * @param portOffset port offset
     * @param propsFile  location of the properties file
     */
    private SparkConf initializeSparkConf(int portOffset, String propsFile)
            throws AnalyticsException {
        // create a spark conf object without loading defaults
        SparkConf conf = new SparkConf(false);

        // read the properties from the file. this file would be the primary locations where the
        // defaults are loaded from
        log.info("Loading Spark defaults from " + propsFile);
        scala.collection.Map<String, String> properties = Utils.getPropertiesFromFile(propsFile);
        conf.setAll(properties);
        setAdditionalConfigs(conf);
        addSparkPropertiesPortOffset(conf, portOffset);
        return conf;
    }

    private void setAdditionalConfigs(SparkConf conf) throws AnalyticsException {
        //executor constants for spark env
        String carbonHome = null, carbonConfDir, analyticsSparkConfDir;
        try {
            carbonHome = conf.get(AnalyticsConstants.CARBON_DAS_SYMBOLIC_LINK);
            logDebug("CARBON HOME set with the symbolic link " + carbonHome);
        } catch (NoSuchElementException e) {
            try {
                carbonHome = CarbonUtils.getCarbonHome();
            } catch (Throwable ex) {
                logDebug("CARBON HOME can not be found. Spark conf in non-carbon environment");
            }
        }
        logDebug("CARBON HOME used for Spark Conf : " + carbonHome);

        if (carbonHome != null) {
            carbonConfDir = carbonHome + File.separator + "repository" + File.separator + "conf";
        } else {
            logDebug("CARBON HOME is NULL. Spark conf in non-carbon environment. Using the custom conf path");
            carbonConfDir = GenericUtils.getAnalyticsConfDirectory();
        }
        analyticsSparkConfDir = carbonConfDir + File.separator + "analytics" + File.separator + "spark";

        conf.setIfMissing(AnalyticsConstants.SPARK_APP_NAME, DEFAULT_SPARK_APP_NAME);
        conf.setIfMissing(AnalyticsConstants.SPARK_DRIVER_CORES, "1");
        conf.setIfMissing(AnalyticsConstants.SPARK_DRIVER_MEMORY, "512m");
        conf.setIfMissing(AnalyticsConstants.SPARK_EXECUTOR_MEMORY, "512m");

        conf.setIfMissing(AnalyticsConstants.SPARK_UI_PORT, "4040");
        conf.setIfMissing(AnalyticsConstants.SPARK_HISTORY_OPTS, "18080");

        conf.setIfMissing(AnalyticsConstants.SPARK_SERIALIZER, KryoSerializer.class.getName());
        conf.setIfMissing(AnalyticsConstants.SPARK_KRYOSERIALIZER_BUFFER, "256k");
        conf.setIfMissing(AnalyticsConstants.SPARK_KRYOSERIALIZER_BUFFER_MAX, "256m");

        conf.setIfMissing("spark.blockManager.port", "12000");
        conf.setIfMissing("spark.broadcast.port", "12500");
        conf.setIfMissing("spark.driver.port", "13000");
        conf.setIfMissing("spark.executor.port", "13500");
        conf.setIfMissing("spark.fileserver.port", "14000");
        conf.setIfMissing("spark.replClassServer.port", "14500");

        conf.setIfMissing(AnalyticsConstants.SPARK_MASTER_PORT, "7077");
        conf.setIfMissing("spark.master.rest.port", "6066");
        conf.setIfMissing(AnalyticsConstants.SPARK_MASTER_WEBUI_PORT, "8081");

        conf.setIfMissing(AnalyticsConstants.SPARK_WORKER_CORES, "1");
        conf.setIfMissing(AnalyticsConstants.SPARK_WORKER_MEMORY, "1g");
        conf.setIfMissing(AnalyticsConstants.SPARK_WORKER_DIR, "work");
        conf.setIfMissing(AnalyticsConstants.SPARK_WORKER_PORT, "11000");
        conf.setIfMissing(AnalyticsConstants.SPARK_WORKER_WEBUI_PORT, "11500");

        conf.setIfMissing(AnalyticsConstants.SPARK_SCHEDULER_MODE, "FAIR");
        conf.setIfMissing(AnalyticsConstants.SPARK_SCHEDULER_POOL, AnalyticsConstants.
                DEFAULT_CARBON_SCHEDULER_POOL_NAME);
        conf.setIfMissing(AnalyticsConstants.SPARK_SCHEDULER_ALLOCATION_FILE,
                          analyticsSparkConfDir + File.separator + AnalyticsConstants.FAIR_SCHEDULER_XML);
        conf.setIfMissing(AnalyticsConstants.SPARK_RECOVERY_MODE, "CUSTOM");
        conf.setIfMissing(AnalyticsConstants.SPARK_RECOVERY_MODE_FACTORY,
                          AnalyticsRecoveryModeFactory.class.getName());

        String agentConfPath = carbonHome + File.separator + "repository" + File.separator +
                               "conf" + File.separator + "data-bridge" + File.separator + "data-agent-config.xml";

        String jvmOpts = " -Dwso2_custom_conf_dir=" + carbonConfDir
                         + " -Dcarbon.home=" + carbonHome
                         + " -D" + Constants.DISABLE_LOCAL_INDEX_QUEUE_OPTION + "=true"
                         + " -DdisableIndexing=true"
                         + " -DdisableDataPurging=true"
                         + " -DdisableEventSink=true"
                         + " -Djavax.net.ssl.trustStore=" + System.getProperty("javax.net.ssl.trustStore")
                         + " -Djavax.net.ssl.trustStorePassword=" + System.getProperty("javax.net.ssl.trustStorePassword")
                         + " -DAgent.Config.Path=" + agentConfPath
                         + getLog4jPropertiesJvmOpt(analyticsSparkConfDir);

        conf.set("spark.executor.extraJavaOptions", conf.get("spark.executor.extraJavaOptions", "") + jvmOpts);
        conf.set("spark.driver.extraJavaOptions", conf.get("spark.driver.extraJavaOptions", "") + jvmOpts);

        //setting the default limit for the spark query results
        conf.setIfMissing("carbon.spark.results.limit", "1000");

        String sparkClasspath = (System.getProperty("SPARK_CLASSPATH") == null) ?
                                "" : System.getProperty("SPARK_CLASSPATH");

        // if the master url starts with "spark", this means that the cluster would be pointed
        // an external cluster. in an external cluster, having more than one implementations of
        // sl4j is not possible. hence, it would be removed from the executor cp. DAS-199
        if (carbonHome != null) {
            try {
                ClusterMode clusterMode = getClusterMode(conf.get(AnalyticsConstants.CARBON_SPARK_MASTER));
                if (clusterMode != ClusterMode.local && clusterMode != ClusterMode.carbonSpark) {
                    sparkClasspath = ComputeClasspath.getSparkClasspath(sparkClasspath, carbonHome, new String[]{"slf4j"});
                } else {
                    sparkClasspath = ComputeClasspath.getSparkClasspath(sparkClasspath, carbonHome);
                }
            } catch (IOException e) {
                throw new AnalyticsExecutionException("Unable to create the extra spark classpath" + e.getMessage(), e);
            }
        } else {
            logDebug("CARBON HOME is NULL. Spark conf in non-carbon environment");
        }

        try {
            conf.set("spark.executor.extraClassPath", conf.get("spark.executor.extraClassPath") + ";" + sparkClasspath);
        } catch (NoSuchElementException e) {
            conf.set("spark.executor.extraClassPath", sparkClasspath);
        }

        try {
            conf.set("spark.driver.extraClassPath", conf.get("spark.driver.extraClassPath") + ";" + sparkClasspath);
        } catch (NoSuchElementException e) {
            conf.set("spark.driver.extraClassPath", sparkClasspath);
        }

        conf.setIfMissing(AnalyticsConstants.CARBON_INSERT_BATCH_SIZE, AnalyticsConstants.MAX_RECORDS);
    }

    private String getLog4jPropertiesJvmOpt(String analyticsSparkConfDir) {
        File tempFile = new File(analyticsSparkConfDir + File.separator + "log4j.properties");

        if (tempFile.exists()) {
            return " -Dlog4j.configuration=file:" + File.separator + File.separator + tempFile.getAbsolutePath();
        } else {
            return "";
        }
    }

    private String getStringFromSparkConf(String config, String defaultVal) {
        try {
            return this.sparkConf.get(config);
        } catch (NoSuchElementException e) {
            return defaultVal;
        }
    }

    public void stop() {
        if (this.sqlCtx != null) {
            this.sqlCtx.sparkContext().stop();
        }
    }

    public int getNumPartitionsHint() throws AnalyticsException {
        /* all workers will not have the same CPU count, this is just an approximation */
        int workerCount = this.getWorkerCount();
        int workerCores = this.sparkConf.getInt(AnalyticsConstants.SPARK_WORKER_CORES, 1);
        int partitionCount = workerCount * workerCores;

        if (workerCount == 0) {
            throw new AnalyticsException("Error while calculating NumPartitionsHint. Worker count is zero.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Partition count: " + partitionCount);
        }

        return partitionCount;
    }

    public AnalyticsQueryResult executeQuery(int tenantId, String query)
            throws AnalyticsExecutionException {
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled() && !acm.isLeader(CLUSTER_GROUP_NAME)) {
            try {
                return acm.executeOne(CLUSTER_GROUP_NAME, acm.getLeader(CLUSTER_GROUP_NAME),
                                      new AnalyticsExecutionCall(tenantId, query));
            } catch (AnalyticsClusterException e) {
                throw new AnalyticsExecutionException("Error executing analytics query: " + e.getMessage(), e);
            }
        } else {
            return this.executeQueryLocal(tenantId, query);
        }
    }

    private AnalyticsQueryResult executeQueryLocal(int tenantId, String query)
            throws AnalyticsExecutionException {

        if (AnalyticsDataServiceUtils.isCarbonServer()) {
            PrivilegedCarbonContext.startTenantFlow();
            // Mandating initialisation of tenant domain for CarbonJDBC multi-tenant scenarios
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
        }

        String origQuery = query.trim();
        query = query.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1).trim();
        }

        try {
            // process incremental queries
            if (checkIncrementalQuery(query)) {
                return processIncQuery(tenantId, query);
            }

            query = encodeQueryWithTenantId(tenantId, query);
            if (log.isDebugEnabled()) {
                log.debug("Executing : " + origQuery);
            }

            long start = System.currentTimeMillis();
            boolean success = true;
            try {
                if (this.sqlCtx == null) {
                    throw new AnalyticsExecutionException("Spark SQL Context is not available. " +
                                                          "Check if the cluster has instantiated properly.");
                }
                this.sqlCtx.sparkContext().setLocalProperty(AnalyticsConstants.SPARK_SCHEDULER_POOL,
                                                            this.sparkConf.get(AnalyticsConstants.SPARK_SCHEDULER_POOL));
                DataFrame result = this.sqlCtx.sql(query);
                return toResult(result);
            } catch (Throwable e) {
                success = false;
                throw new AnalyticsExecutionException("Exception in executing query " + origQuery, e);
            } finally {
                long end = System.currentTimeMillis();
                if (ServiceHolder.isAnalyticsStatsEnabled()) {
                    if (success) {
                        log.info("Executed query: " + origQuery + " \nTime Elapsed: " + (end - start) / 1000.0 + " seconds.");
                    } else {
                        log.error("Unable to execute query: " + origQuery + " \nTime Elapsed: " + (end - start) / 1000.0 + " seconds.");
                    }
                }
            }
        } finally {
            if (AnalyticsDataServiceUtils.isCarbonServer()) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        }
    }

    private AnalyticsQueryResult processIncQuery(int tenantId, String query) throws AnalyticsExecutionException {
        AnalyticsQueryResult result;
        String[] splits = query.split("(\\s*,\\s*|\\s+)");

        switch (splits[0].toLowerCase()) {
            case AnalyticsConstants.INC_TABLE_COMMIT:
                result = processIncTableCommit(tenantId, Arrays.copyOfRange(splits, 1, splits.length));
                break;
            case AnalyticsConstants.INC_TABLE_RESET:
                result = processIncTableReset(tenantId, Arrays.copyOfRange(splits, 1, splits.length));
                break;
            case AnalyticsConstants.INC_TABLE_SHOW:
                result = processIncTableShow(tenantId, Arrays.copyOfRange(splits, 1, splits.length));
                break;
            default:
                throw new AnalyticsExecutionException("Invalid incremental query: " + query);
        }
        return result;
    }

    private AnalyticsQueryResult processIncTableShow(int tenantId, String[] tableIds)
            throws AnalyticsExecutionException {
        List<List<Object>> tableResults = new ArrayList<>();
        for (String tableId : tableIds) {
            ArrayList<Object> tableResult = new ArrayList<>(3);
            tableResult.add(tableId);
            try {
                tableResult.add(ServiceHolder.getIncrementalMetaStore().getLastProcessedTimestamp(tenantId, tableId, false));
                tableResult.add(ServiceHolder.getIncrementalMetaStore().getLastProcessedTimestamp(tenantId, tableId, true));
            } catch (AnalyticsException e) {
                throw new AnalyticsExecutionException(e.getMessage(), e);
            }
            tableResults.add(tableResult);
        }
        return new AnalyticsQueryResult(new String[]{"TABLE_ID", "TEMP_VAL", "PRIMARY_VAL"}, tableResults);
    }

    private AnalyticsQueryResult processIncTableReset(int tenantId, String[] tableIds)
            throws AnalyticsExecutionException {
        for (String tableId : tableIds) {
            try {
                ServiceHolder.getIncrementalMetaStore().resetIncrementalTimestamps(tenantId, tableId);
            } catch (AnalyticsException e) {
                throw new AnalyticsExecutionException(e.getMessage(), e);
            }
        }
        return AnalyticsQueryResult.emptyAnalyticsQueryResult();
    }

    private AnalyticsQueryResult processIncTableCommit(int tenantId, String[] tableIds)
            throws AnalyticsExecutionException {
        List<List<Object>> tableResults = new ArrayList<>();
        for (String tableId : tableIds) {
            ArrayList<Object> tableResult = new ArrayList<>(2);
            tableResult.add(tableId);
            try {
                tableResult.add(ServiceHolder.getIncrementalMetaStore().getLastProcessedTimestamp(tenantId, tableId, true));
                long tempTS = ServiceHolder.getIncrementalMetaStore().getLastProcessedTimestamp(tenantId, tableId, false);
                ServiceHolder.getIncrementalMetaStore().setLastProcessedTimestamp(tenantId, tableId, tempTS, true);
                tableResult.add(tempTS);
            } catch (AnalyticsException e) {
                throw new AnalyticsExecutionException(e.getMessage(), e);
            }
            tableResults.add(tableResult);
        }
        return new AnalyticsQueryResult(new String[]{"TABLE_ID", "PREV_PRIMARY_VAL", "NEW_PRIMARY_VAL"}, tableResults);
    }

    private boolean checkIncrementalQuery(String str) {
        return str.trim().toLowerCase().startsWith(AnalyticsConstants.INC_TABLE);
    }

    private String encodeQueryWithTenantId(int tenantId, String query)
            throws AnalyticsExecutionException {
        String result;
        // parse the query to see if it is a create temporary table
        // add the table names to the hz cluster map with tenantId -> table Name (put if absent)
        // iterate through the dist map and replace the relevant table names

        Pattern p = Pattern.compile("(?i)(?<=(" + AnalyticsConstants.TERM_CREATE +
                                    "\\s" + AnalyticsConstants.TERM_TEMPORARY +
                                    "\\s" + AnalyticsConstants.TERM_TABLE + "))\\s+\\w+" +
                                    "(?=\\s+" + AnalyticsConstants.TERM_USING + "\\s\\D+\\s+" + AnalyticsConstants.TERM_OPTIONS + "\\s*\\()");
        Matcher m = p.matcher(query.trim());
        if (m.find()) {
            //this is a create table query
            // CREATE TEMPORARY TABLE <name> USING CarbonAnalytics OPTIONS(...)
            String tempTableName = m.group().trim();
            if (tempTableName.matches("(?i)if")) {
                throw new AnalyticsExecutionException("Malformed query: CREATE TEMPORARY TABLE IF NOT " +
                                                      "EXISTS is not supported");
            } else {
                synchronized (this.sparkTableNamesHolder) {
                    this.sparkTableNamesHolder.addTableName(tenantId, tempTableName);
                }
                result = this.replaceShorthandStrings(query);

                int optStrStart = result.toLowerCase().indexOf(AnalyticsConstants.TERM_OPTIONS, m.end());
                int bracketsOpen = result.indexOf("(", optStrStart);
                int bracketsClose = result.indexOf(")", bracketsOpen);

                //if its a carbon query, append the tenantId to the end of options
                String options;
                if (this.isCarbonQuery(query)) {
                    options = result.substring(optStrStart, bracketsOpen + 1)
                              + addTenantIdToOptions(tenantId, result.substring(bracketsOpen + 1, bracketsClose))
                              + ")";
                } else {
                    options = result.substring(optStrStart, bracketsClose + 1);
                }

                String beforeOptions = replaceTableNamesInQuery(tenantId, result.substring(0, optStrStart));
                String afterOptions = replaceTableNamesInQuery(tenantId, result.substring(bracketsClose + 1, result.length()));
                result = beforeOptions + options + afterOptions;

            }
        } else {
            result = this.replaceTableNamesInQuery(tenantId, query);
        }
        return result.trim();
    }

    private boolean isCarbonQuery(String query) {
        return (query.contains(AnalyticsConstants.SPARK_SHORTHAND_STRING) || query
                .contains(AnalyticsConstants.COMPRESSED_EVENT_ANALYTICS_SHORTHAND));
    }

    private String replaceShorthandStrings(String query) {
        for (Map.Entry<String, String> entry : this.shorthandStringsMap.entrySet()) {
            query = query.replaceFirst("\\b" + entry.getKey() + "\\b", entry.getValue());
        }
        return query;
    }

    private void registerShorthandStrings() {
        this.addShorthandString(AnalyticsConstants.SPARK_SHORTHAND_STRING,
                                AnalyticsRelationProvider.class.getName());
        this.addShorthandString(AnalyticsConstants.SPARK_JDBC_SHORTHAND_STRING,
                                AnalyticsJDBCRelationProvider.class.getName());
        this.addShorthandString(AnalyticsConstants.COMPRESSED_EVENT_ANALYTICS_SHORTHAND,
                                CompressedEventAnalyticsRelationProvider.class.getName());
        this.addShorthandString(AnalyticsConstants.SPARK_EVENTS_SHORTHAND_STRING,
                                "org.wso2.carbon.analytics.spark.event.EventStreamProvider");
        // NOTE: Had to put the name string here, otherwise it creates a circular dependency
    }

    private void addShorthandString(String shorthand, String className) {
        try {
            Class.forName(className);
            this.shorthandStringsMap.put(shorthand, className);
        } catch (ClassNotFoundException e) {
            log.error(e);
        }
    }

    private String addTenantIdToOptions(int tenantId, String optStr)
            throws AnalyticsExecutionException {
        String[] opts = optStr.split("\\s*,\\s*");
        boolean hasTenantId = false;
        for (String option : opts) {
            String[] splits = option.trim().split("\\s+", 2);
            hasTenantId = splits[0].equals(AnalyticsConstants.TENANT_ID);
            if (hasTenantId && tenantId != Integer.parseInt(splits[1].replaceAll("^\"|\"$", ""))) {
                throw new AnalyticsExecutionException("Mismatching tenants : " + tenantId +
                                                      " and " + splits[1].replaceAll("^\"|\"$", ""));
            }
        }
        // if tenatId is not present, add it as another field
        if (!hasTenantId) {
            optStr = optStr + " , " + AnalyticsConstants.TENANT_ID + " \"" + tenantId + "\"";
        }
        return optStr;
    }

    private String replaceTableNamesInQuery(int tenantId, String query) {
        String result = query;

        synchronized (this.sparkTableNamesHolder) {
            Collection<String> tableNames = this.sparkTableNamesHolder.getTableNames(tenantId);

            for (String name : tableNames) {
                result = result.replaceAll("\\b" + name + "\\b",
                                           AnalyticsCommonUtils.encodeTableNameWithTenantId(tenantId, name));
            }
            return result;
        }
    }

    private AnalyticsQueryResult toResult(DataFrame dataFrame)
            throws AnalyticsExecutionException {
        int resultsLimit = this.sparkConf.getInt("carbon.spark.results.limit", -1);
        if (resultsLimit != -1) {
            return new AnalyticsQueryResult(dataFrame.schema().fieldNames(),
                                            convertRowsToObjects(dataFrame.limit(resultsLimit).collect()));
        } else {
            return new AnalyticsQueryResult(dataFrame.schema().fieldNames(),
                                            convertRowsToObjects(dataFrame.collect()));
        }
    }

    private List<List<Object>> convertRowsToObjects(Row[] rows) {
        List<List<Object>> result = new ArrayList<>();
        List<Object> objects;
        for (Row row : rows) {
            objects = new ArrayList<>();
            for (int i = 0; i < row.length(); i++) {
                objects.add(row.get(i));
            }
//            Set<PosixFilePermission> perms = new HashSet<>();
            result.add(objects);
        }
        return result;
    }

    /**
     * when this cluster message arrives, there are two implications
     * - current cluster leader is down. this may or may not be the spark leader. so depending on
     * the situation a suitable spark leader will be elected by this cluster leader
     * - spark app resides in the cluster leader node, so it has also gone down when this
     * message arrives. so, a new client needs to be created.
     */
    @Override
    public void onBecomingLeader() {
        log.info("This node is now the CARBON CLUSTERING LEADER");
        int retries = 0;
        boolean isFailed = !this.executeOnBecomingLeaderFlow();

        while (isFailed && (retries < MAX_RETRIES)) {
            log.info("Retrying executing On Becoming Leader flow. Retry count = " + retries);
            long waitTime = Math.min(getWaitTimeExp(retries), MAX_RETRY_WAIT_INTERVAL);
            retryWait(waitTime);
            isFailed = !this.executeOnBecomingLeaderFlow();
            retries++;
        }
    }

    private boolean executeOnBecomingLeaderFlow() {
        if (log.isDebugEnabled()) {
            log.debug("Executing On Becoming Leader Flow : ");
        }
        try {
            if (clusterMode == ClusterMode.carbonSpark) {
                HazelcastInstance hz = AnalyticsServiceHolder.getHazelcastInstance();
                IMap<String, Object> masterMap = hz.getMap(AnalyticsConstants.SPARK_MASTER_MAP);

                if (masterMap.isEmpty()) {
                    // masterMap empty means that there haven't been any masters in the cluster
                    // so, no electable leader is available.
                    // therefore this node is put to the map as a possible leader
                    log.info("Spark master map is empty...");
                    String masterUrl = "spark://" + this.myHost + ":" + this.sparkConf.getInt(
                            AnalyticsConstants.SPARK_MASTER_PORT, 7077 + this.portOffset);
                    masterMap.put(masterUrl, acm.getLocalMember());
                    log.info("Added " + masterUrl + " to the MasterMap");
                } else if (masterMap.size() >= this.redundantMasterCount) {
                    log.info("Redundant master count fulfilled : " + masterMap.size());
                    // when becoming leader, this checks if there is an elected spark leader available in the cluster.
                    // if there is, then the cluster is already in a workable state.
                    // else, a suitable leader needs to be elected.
                    if (!isElectedLeaderAvailable()) {
                        log.info("No Elected SPARK LEADER in the cluster. Electing a suitable leader...");
                        try {
                            electSuitableLeader();
                        } catch (AnalyticsClusterException e) {
                            String msg = "Unable to elect a suitable leader : " + e.getMessage();
                            log.error(msg, e);
                            throw new RuntimeException(msg, e);
                        }
                    }

                    // new spark client app will be created, pointing to the spark masters
                    log.info("Initializing new spark client app...");
                    this.initializeAnalyticsClient();
                } else {
                    log.info("Master map size is less than the redundant master count");
                }
            } else {
                log.info("Analytics cluster leadership has changed. Hence, re-creating the Spark Client application");
                this.initializeAnalyticsClient();
            }
            return true;
        } catch (Exception e) {
            String msg = "Error in processing on becoming leader cluster message: " + e.getMessage();
            log.warn(msg, e);
            return false;
        }
    }

    /**
     * this method, elected a suitable spark leader from the spark leader map
     * approach:
     * takes the spark masters map from the hz cluster. for each of these masters, check if it is
     * currently active in the cluster. if it is, immediately it will be elected as the spark leader.
     * this will be done through a cluster message
     *
     * @throws AnalyticsClusterException
     */
    private void electSuitableLeader() throws AnalyticsClusterException {
        HazelcastInstance hz = AnalyticsServiceHolder.getHazelcastInstance();
        IMap<String, Object> masterMap = hz.getMap(AnalyticsConstants.SPARK_MASTER_MAP);
        List<Object> masterMembers = new ArrayList<>(masterMap.values());

        List<Object> groupMembers = acm.getMembers(CLUSTER_GROUP_NAME);

        boolean foundSuitableMaster = false;
        for (Object masterMember : masterMembers) {
            if (groupMembers.contains(masterMember)) {
                //this means that this master is active in the cluster
                acm.executeOne(CLUSTER_GROUP_NAME, masterMember, new ElectLeaderExecutionCall());
                foundSuitableMaster = true;
                log.info("Suitable leader elected : " + masterMember);
                break;
            }
        }
        if (!foundSuitableMaster) {
            log.error("No Spark master is available in the cluster to be elected as the leader");
        }
    }

    /**
     * this method makes the LeaderElectable object of this node, as the elected leader
     */
    public synchronized void electAsLeader() {
        log.info("Elected as the Spark Leader");
        for (LeaderElectable le : this.leaderElectable) {
            le.electedLeader();
        }
        this.electedLeader = true;
    }

    @Override
    public void onLeaderUpdate() {
        // nothing to do here because when the carbon cluster leader is changed, the newly elected
        // master, performs the relevant operations needed to get the spark cluster up again.
    }

    private int getWorkerCount() {
        return workerCount;
    }

    /**
     * this message arrives, when this node is the leader and some other member's state in the changed.
     *
     * @param removedMember true if some member have been removed
     *                      <p/>
     *                      if a member has been removed and he was the elected leader of the spark
     *                      cluster, this means that a new spark leader has to be elected.
     */
    @Override
    public void onMembersChangeForLeader(boolean removedMember) {
        log.info("Member change, remove: " + removedMember);
        int retries = 0;
        boolean isFailed = !this.executeOnMembersChangeForLeaderFlow(removedMember);
        while (isFailed && (retries < MAX_RETRIES)) {
            log.info("Retrying executing On Member Change for Leader Flow. Retry count = " + retries);
            long waitTime = Math.min(getWaitTimeExp(retries), MAX_RETRY_WAIT_INTERVAL);
            retryWait(waitTime);
            isFailed = !this.executeOnMembersChangeForLeaderFlow(removedMember);
            retries++;
        }
    }

    private boolean executeOnMembersChangeForLeaderFlow(boolean removedMember) {
        this.logDebug("Execute On Members Change For Leader Flow");
        try {
            if (clusterMode == ClusterMode.carbonSpark) {
                this.workerCount = AnalyticsServiceHolder.getAnalyticsClusterManager().getMembers(CLUSTER_GROUP_NAME).size();
                log.info("Analytics worker updated, total count: " + this.getWorkerCount());

                if (removedMember) {
                    if (!isElectedLeaderAvailable()) {
                        //this means that the elected spark master has been removed
                        log.info("Removed member was the Spark elected leader. Electing a suitable leader...");
                        electSuitableLeader();
                    } else {
                        log.info("Elected leader already available.");
                    }
                }
            }
            return true;
        } catch (Exception e) {
            String msg = "Error while executing On Members Change For Leader Flow: " + e.getMessage();
            log.warn(msg, e);
            return false;
        }
    }

    /**
     * this registers a LeaderElectable object here. this method is invoked from the
     * AnalyticsLeaderElectionAgent
     *
     * @param le leader electable object
     */
    public void registerLeaderElectable(LeaderElectable le) {
        this.leaderElectable.add(le);
        log.info("Spark leader electable registered");
    }

    private void logDebug(String msg) {
        if (log.isDebugEnabled()) {
            log.debug(msg);
        }
    }

    @Override
    public void onMemberRemoved() {
        /* nothing to do */
    }

    private long getWaitTimeExp(int retryCount) {
        return ((long) Math.pow(2, retryCount) * 100L);
    }

    private void retryWait(long waitTime) {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ignored) {
        }
    }

    private enum ClusterMode {
        local("Local"),
        carbonSpark("Carbon Spark"),
        standaloneSpark("Standalone Spark"),
        yarn("Spark on YARN"),
        mesos("Spark on Mesos");

        private String name;

        ClusterMode(String name) {
            this.name = name;
        }

        private String getValue() {
            return name;
        }

        @Override
        public String toString() {
            return this.getValue();
        }

    }

    private ClusterMode getClusterMode(String sparkMaster) throws AnalyticsExecutionException {
        if (sparkMaster.toLowerCase().startsWith("local")) {
            if (acm.isClusteringEnabled()) {
                log.warn("Using 'local' with Carbon clustering is deprecated. " +
                         "Please use 'carbon.spark.master carbon' instead!");
                return ClusterMode.carbonSpark;
            } else {
                return ClusterMode.local;
            }
        } else if (sparkMaster.toLowerCase().startsWith("carbon")) {
            if (!acm.isClusteringEnabled()) {
                throw new AnalyticsExecutionException("Using Carbon Clustering without enabling clustering in axis2. " +
                                                      "Please refer axis2 settings.");
            }
            return ClusterMode.carbonSpark;
        } else if (sparkMaster.toLowerCase().startsWith("spark")) {
            return ClusterMode.standaloneSpark;
        } else if (sparkMaster.toLowerCase().startsWith("yarn")) {
            if (sparkMaster.equalsIgnoreCase("yarn-cluster")) {
                throw new AnalyticsExecutionException("\"yarn-cluster\" mode is not supported in DAS. " +
                                                      "Please use \"yarn-cluster\"!");
            }
            return ClusterMode.yarn;
        } else if (sparkMaster.toLowerCase().startsWith("mesos")) {
            return ClusterMode.mesos;
        } else {
            throw new AnalyticsExecutionException("Unknown cluster mode for Spark : " + sparkMaster);
        }
    }

}

