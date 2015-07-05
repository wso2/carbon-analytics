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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.MultiMap;
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
import org.apache.spark.util.Utils;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterException;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManager;
import org.wso2.carbon.analytics.dataservice.clustering.GroupEventListener;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.AnalyticsExecutionCall;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsExecutionException;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsUDFException;
import org.wso2.carbon.analytics.spark.core.udf.AnalyticsUDFsRegister;
import org.wso2.carbon.analytics.spark.core.udf.config.UDFConfiguration;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsQueryResult;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsRelationProvider;
import org.wso2.carbon.analytics.spark.core.util.master.AnalyticsRecoveryModeFactoryScala;
import org.wso2.carbon.analytics.spark.core.util.master.StartWorkerExecutionCall;
import org.wso2.carbon.analytics.spark.core.util.master.testRMF;
import org.wso2.carbon.utils.CarbonUtils;
import scala.None$;
import scala.Option;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the analytics query execution context.
 */
public class SparkAnalyticsExecutor implements GroupEventListener {

    private static final int SPARK_TENANT = -5000;

    private static final int BASE_WORKER_UI_PORT = 8090;

    private static final int BASE_WORKER_PORT = 4501;

    private static final int DEFAULT_SPARK_UI_PORT = 4040;

    private static final String MASTER_URL_PROP = "MASTER_HOST";

    private static final int BASE_WEBUI_PORT = 8081;

    private static final int BASE_MASTER_PORT = 7077;

    private static final int BASE_UI_PORT = 4040;

    private static final String CLUSTER_GROUP_NAME = "CARBON_ANALYTICS_EXECUTION";

    private static final String LOCAL_MASTER_URL = "local";

    private static final String CARBON_ANALYTICS_SPARK_APP_NAME = "CarbonAnalytics";

    private static final int WORKER_CORES = 1;

    private static final String WORKER_MEMORY = "1g";

    private static final String WORK_DIR = "work";

    private static final String CUSTOM_RECOVERY_MODE = "CUSTOM";

    private static final String ANALYTICS_RECOVERY_FACTORY = AnalyticsRecoveryModeFactoryScala.class.getCanonicalName();

    private static final Log log = LogFactory.getLog(SparkAnalyticsExecutor.class);

    private SparkConf sparkConf;

    private JavaSparkContext javaSparkCtx;

    private SQLContext sqlCtx;

    private String myHost;

    private int portOffset;

    private int workerCount = 1;

    private MultiMap<Integer, String> sparkTableNames;

    private ListMultimap<Integer, String> inMemSparkTableNames;

    private Map<String, String> propertiesMap;

    private UDFConfiguration udfConfiguration;

    private boolean clientMode = false;

    private boolean isClustered = false;

    private int membershipNumber = 1; // membership number starts from 1

    private int masterCount = 1;

    private Set<LeaderElectable> leaderElectable = new HashSet<>();

    private AnalyticsClusterManager acm;

    private boolean executorStarted = false;

    public SparkAnalyticsExecutor(String myHost, int portOffset) throws AnalyticsException {
        this(myHost, portOffset, CarbonUtils.getCarbonConfigDirPath());
    }

    public SparkAnalyticsExecutor(String myHost, int portOffset, String confPath)
            throws AnalyticsException {
        this.myHost = myHost;
        this.portOffset = portOffset;
        this.udfConfiguration = this.loadUDFConfiguration();

        Map<String, String> propertiesMap = loadCarbonSparkProperties(confPath + File.separator +
                                                                      AnalyticsConstants.SPARK_DEFAULTS_PATH);
        if (propertiesMap.get(AnalyticsConstants.CARBON_SPARK_CLIENT_MODE) != null) {
            this.clientMode = Boolean.parseBoolean(propertiesMap.get(
                    AnalyticsConstants.CARBON_SPARK_CLIENT_MODE));
        }
        if (propertiesMap.get(AnalyticsConstants.CARBON_SPARK_MASTER_COUNT) != null) {
            this.masterCount = Integer.parseInt(propertiesMap.get(
                    AnalyticsConstants.CARBON_SPARK_MASTER_COUNT));
        }

        this.acm = AnalyticsServiceHolder.getAnalyticsClusterManager();

        this.executorStarted = true;
    }

    /**
     * todo: IMPORTANT: this method should make the current tests fail
     *
     * @param confPath
     * @throws AnalyticsClusterException
     */
    public void startSparkServer(String confPath) throws AnalyticsClusterException {
        if (executorStarted) {
            if (!clientMode) {
                log.info("Using Carbon clustering for Spark");
                if (acm.isClusteringEnabled()) {
                    this.isClustered = true;
                    // if clustering enabled, use the ACM
                    this.membershipNumber = acm.joinGroup(CLUSTER_GROUP_NAME, this);
                    log.info("Member joined the cluster with MEMBERSHIP NUMBER : " + this.membershipNumber);

                    String propsFile = confPath + File.separator
                                       + AnalyticsConstants.SPARK_DEFAULTS_PATH;

                    this.sparkConf = initializeSparkConf(this.myHost, BASE_MASTER_PORT,
                                                         this.portOffset, propsFile);

                    testRMF.testThis();

                    if (this.membershipNumber < this.masterCount) {
                        // start master and register the master URL in an ACM property
                        log.info("Members are less than the REDUNDANT MASTER COUNT");
                        startMaster();
                    } else if (this.membershipNumber == this.masterCount) {
                        log.info("Members are equal to the REDUNDANT MASTER COUNT");
                        // start master and register the master URL in an ACM property
                        startMaster();

                        // creating masters are done! send a cluster message to all masters to start workers
                        log.info("Sending a message to masters to start workers");
                        this.acm.executeAll(CLUSTER_GROUP_NAME, new StartWorkerExecutionCall());
                    } else {
                        Utils.loadDefaultSparkProperties(this.sparkConf, propsFile);

                        String[] masterURLs = this.getSparkMastersFromCluster(acm);

                        String thisMasterURL = "spark://" + this.myHost + ":" + (BASE_MASTER_PORT + this.portOffset);
                        // if it was a previous master, start master
                        if (Arrays.asList(masterURLs).contains(thisMasterURL)) {
                            startMaster();
                        }

                        //start Worker;
                        startWorker();

                        // todo: initClient (); in the active leader using a cluster message
                    }
                } else {
                    // else start the node in the local mode
                    this.initializeLocalClient();
                }
            } else {
                //clients points to an external spark master and makes the spark conf accordingly
                log.info("Client mode enabled for Spark");
                log.info("Starting SPARK CLIENT pointing to an external Spark Cluster");
            }
        } else {
            throw new AnalyticsClusterException("Spark executor is not instantiated");
        }
    }


    private String[] getSparkMastersFromCluster(AnalyticsClusterManager acm) {
        List<String> masters = new ArrayList<>(this.masterCount); // todo: change this!!!
        for (int i = 1; i <= this.masterCount; i++) {
            String url = (String) acm.getProperty(CLUSTER_GROUP_NAME, MASTER_URL_PROP + "_" + i);
            if (url != null) {
                masters.add(url);
            }
        }
        return masters.toArray(new String[masters.size()]);
    }

    private UDFConfiguration loadUDFConfiguration() throws AnalyticsException {
        try {
            File confFile = new File(GenericUtils.getAnalyticsConfDirectory() + // todo: check if this conflicts with non- osgi env
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

    private void initClient(String masterUrl, String appName) {
//        initializeSparkConf(masterUrl, this.portOffset, appName);
//        this.javaSparkCtx = new JavaSparkContext(this.sparkConf);
//        this.sqlCtx = new SQLContext(this.javaSparkCtx);
//        initSqlContext(this.javaSparkCtx);
    }

    private void initializeLocalClient() {
        this.sparkConf = new SparkConf();
        this.sparkConf.setMaster(LOCAL_MASTER_URL)
                .setAppName(CARBON_ANALYTICS_SPARK_APP_NAME)
                .set(AnalyticsConstants.SPARK_UI_PORT, Integer.toString(BASE_UI_PORT + portOffset));
        log.info("Started Spark client in the LOCAL mode" +
                 " with the application name : " + CARBON_ANALYTICS_SPARK_APP_NAME +
                 " and UI port : " + (BASE_UI_PORT + portOffset));
        this.javaSparkCtx = new JavaSparkContext(this.sparkConf);
        initSqlContext(this.javaSparkCtx);
    }

    private void initSqlContext(JavaSparkContext jsc) {
        this.sqlCtx = new SQLContext(jsc);
        try {
            registerUDFs(this.sqlCtx);
        } catch (AnalyticsUDFException e) {
            log.error("Error while Initializing Spark SQL Context: ", e);
        }
    }

    private void registerUDFs(SQLContext sqlCtx)
            throws AnalyticsUDFException {
        String[] udfClassesNames = this.udfConfiguration.getCustomUDFClass();
        if (udfClassesNames != null && udfClassesNames.length > 0) {
            AnalyticsUDFsRegister udfAdaptorBuilder = new AnalyticsUDFsRegister();
            try {
                for (String udfClassName : udfClassesNames) {
                    udfClassName = udfClassName.trim();
                    if (!udfClassName.isEmpty()) {
                        Class udf = Class.forName(udfClassName);
                        Method[] methods = udf.getDeclaredMethods();
                        for (Method method : methods) {
                            udfAdaptorBuilder.registerUDF(udf, method, sqlCtx);
                        }
                    }
                }
            } catch (ClassNotFoundException e) {
                throw new AnalyticsUDFException("Error While registering UDFs: " + e.getMessage(), e);
            }
        }
    }

    /**
     * this method starts a spark master with a given parameters.
     *
     * @param host
     * @param port
     * @param webUIport
     * @param sc
     * @param acm
     * @return
     */
    private void startMaster(String host, int port, int webUIport, SparkConf sc,
                             AnalyticsClusterManager acm) {
        log.info("Starting SPARK MASTER");

        if (System.getProperty(AnalyticsConstants.SPARK_MASTER_PORT) != null) {
            port = Integer.parseInt(System.getProperty(AnalyticsConstants.SPARK_MASTER_PORT));
        }

        if (System.getProperty(AnalyticsConstants.SPARK_MASTER_WEBUI_PORT) != null) {
            webUIport = Integer.parseInt(System.getProperty(AnalyticsConstants.SPARK_MASTER_WEBUI_PORT));
        }

        Master.startSystemAndActor(host, port, webUIport, sc);

        String masterURL = "spark://" + host + ":" + port;
        acm.setProperty(CLUSTER_GROUP_NAME, MASTER_URL_PROP + "_" + this.membershipNumber
                , masterURL);

        log.info("Started SPARK MASTER in " + masterURL + " with webUI port : " + webUIport);

        updateMaster(acm, sc);
    }

    public void startMaster() {
        this.startMaster(this.myHost, BASE_MASTER_PORT + portOffset, BASE_WEBUI_PORT + portOffset,
                         this.sparkConf, this.acm);
    }

    private void updateMaster(AnalyticsClusterManager acm, SparkConf sc) {
        String[] masters = getSparkMastersFromCluster(acm);
        String url = "spark://";
        for (int i = 0; i < masters.length; i++) {
            if (i == 0) {
                url = url + masters[i].replace("spark://", "");
            } else {
                url = url + "," + masters[i].replace("spark://", "");
            }
        }
        sc.setMaster(url);
    }

    /**
     * this method checks the existing jvm properties and add the port offset to properties which
     * starts with "spark." and ends with ".port". also, sets the relevant spark conf properties
     *
     * @param sc
     * @param portOffset
     */
    private void addSparkPropertiesPortOffset(SparkConf sc, int portOffset) {
        Properties properties = System.getProperties();

        Enumeration e = properties.propertyNames();
        while (e.hasMoreElements()) {
            String key = (String) e.nextElement();
            if (key.trim().startsWith("spark.") && key.trim().endsWith(".port")) {
                String withPortOffset = Integer.toString(Integer.parseInt(properties.getProperty(key)) + portOffset);
                //set the system property
                System.setProperty(key, withPortOffset);
                //set the spark context property
                sc.set(key, withPortOffset);
            }
        }
    }

    /**
     * this starts a worker with given parameters. it reads the spark defaults from
     * the given properties file and override parameters accordingly. it also adds the port offset
     * to all the port configurations
     *
     * @param workerHost
     * @param workerPort
     * @param workerUiPort
     * @param workerCores
     * @param workerMemory
     * @param workerDir
     * @param masters
     * @param sc
     */
    private void startWorker(String workerHost,
                             int workerPort,
                             int workerUiPort,
                             int workerCores,
                             String workerMemory,
                             String[] masters,
                             String workerDir,
                             SparkConf sc) {

        log.info("Starting SPARK WORKER");

        //check worker port prop
        if (System.getProperty(AnalyticsConstants.SPARK_WORKER_PORT) != null) {
            workerPort = Integer.parseInt(System.getProperty(AnalyticsConstants.SPARK_WORKER_PORT));
        }
        //check worker web ui port prop
        if (System.getProperty(AnalyticsConstants.SPARK_WORKER_WEBUI_PORT) != null) {
            workerUiPort = Integer.parseInt(System.getProperty(AnalyticsConstants.SPARK_WORKER_WEBUI_PORT));
        }
        //check worker cores prop
        if (System.getProperty(AnalyticsConstants.SPARK_WORKER_CORES) != null) {
            workerCores = Integer.parseInt(System.getProperty(AnalyticsConstants.SPARK_WORKER_CORES));
        }
        //check worker mem prop
        if (System.getProperty(AnalyticsConstants.SPARK_WORKER_MEMORY) != null) {
            workerMemory = System.getProperty(AnalyticsConstants.SPARK_WORKER_MEMORY);
        }
        //check worker dir prop
        if (System.getProperty(AnalyticsConstants.SPARK_WORKER_DIR) != null) {
            workerDir = System.getProperty(AnalyticsConstants.SPARK_WORKER_DIR);
        }

        Worker.startSystemAndActor(workerHost, workerPort, workerUiPort, workerCores,
                                   Utils.memoryStringToMb(workerMemory), masters, workerDir,
                                   (Option) None$.MODULE$, sc);

        log.info("Started SPARK WORKER in " + workerHost + ":" + workerPort + " with webUI port "
                 + workerUiPort
                 + " with Masters " + Arrays.toString(masters));

    }

    public void startWorker() {
        this.startWorker(this.myHost, BASE_WORKER_PORT + portOffset, BASE_WORKER_UI_PORT + portOffset,
                         WORKER_CORES, WORKER_MEMORY, this.getSparkMastersFromCluster(this.acm),
                         WORK_DIR, this.sparkConf);
    }

    /**
     * this method initializes spark conf with default properties
     * it also reads the spark defaults from
     * the given properties file and override parameters accordingly. it also adds the port offset
     * to all the port configurations
     *
     * @param masterHost
     * @param masterPort
     * @param portOffset
     */
    private SparkConf initializeSparkConf(String masterHost, int masterPort, int portOffset,
                                          String propsFile) {
        SparkConf conf = new SparkConf();
        conf.set(AnalyticsConstants.SPARK_MASTER, "spark://" + masterHost + ":" + (masterPort + portOffset));

        log.info("Spark defaults loaded from " + propsFile);
        // this will read configs from the file and export as system props
        // NOTE: if properties are mentioned in the file, they take precedence over the defaults, except spark masterURL
        Utils.loadDefaultSparkProperties(conf, propsFile);
        setDefaultsIfMissing(conf, portOffset);
        addSparkPropertiesPortOffset(conf, portOffset);
        return conf;
    }

    private void setDefaultsIfMissing(SparkConf conf, int portOffset) {
        // setting defaults for analytics W/O port offset
        conf.setIfMissing(AnalyticsConstants.SPARK_APP_NAME, CARBON_ANALYTICS_SPARK_APP_NAME);
        conf.setIfMissing(AnalyticsConstants.SPARK_UI_PORT, String.valueOf(DEFAULT_SPARK_UI_PORT
                                                                           + portOffset));

        conf.setIfMissing(AnalyticsConstants.SPARK_SCHEDULER_MODE, "FAIR");
        conf.setIfMissing(AnalyticsConstants.SPARK_RECOVERY_MODE, CUSTOM_RECOVERY_MODE);
        conf.setIfMissing(AnalyticsConstants.SPARK_RECOVERY_MODE_FACTORY, ANALYTICS_RECOVERY_FACTORY);

        //serialization
        conf.setIfMissing(AnalyticsConstants.SPARK_SERIALIZER, KryoSerializer.class.getName());
        conf.setIfMissing(AnalyticsConstants.SPARK_KRYOSERIALIZER_BUFFER, "256k");
        conf.setIfMissing(AnalyticsConstants.SPARK_KRYOSERIALIZER_BUFFER_MAX, "256m");

        //master, worker, drivers and executors todo: replace these with constants strings
        conf.setIfMissing("spark.executor.memory", "1g");
        conf.setIfMissing("spark.master.port", String.valueOf(7077 + portOffset));
        conf.setIfMissing("spark.master.rest.port", String.valueOf(6066+ portOffset));
        conf.setIfMissing("spark.master.webui.port", String.valueOf(8081 + portOffset));
        conf.setIfMissing("spark.worker.cores", "2");
        conf.setIfMissing("spark.worker.memory", "2g");
        conf.setIfMissing("spark.worker.port ", String.valueOf(14501 + portOffset));
        conf.setIfMissing("spark.worker.webui.port", String.valueOf(18090 + portOffset));
        conf.setIfMissing("spark.driver.port", String.valueOf(5000 + portOffset));
        conf.setIfMissing("spark.executor.port", String.valueOf(16000 + portOffset));
        conf.setIfMissing("spark.fileserver.port", String.valueOf(17000 + portOffset));

        //executor constants for spark env
        conf.setIfMissing("spark.executor.extraJavaOptions", "-Dwso2_custom_conf_dir=" + CarbonUtils.getCarbonConfigDirPath());
        conf.setIfMissing("spark.driver.extraJavaOptions", "-Dwso2_custom_conf_dir=" + CarbonUtils.getCarbonConfigDirPath());

        conf.set(AnalyticsConstants.CARBON_TENANT_ID, String.valueOf(SPARK_TENANT));
    }

    private void validateSparkScriptPathPermission() {
        Set<PosixFilePermission> perms = new HashSet<>();
        //add owners permission
        perms.add(PosixFilePermission.OWNER_READ);
        perms.add(PosixFilePermission.OWNER_WRITE);
        perms.add(PosixFilePermission.OWNER_EXECUTE);
        //add group permissions
        perms.add(PosixFilePermission.GROUP_READ);
        perms.add(PosixFilePermission.GROUP_WRITE);
        perms.add(PosixFilePermission.GROUP_EXECUTE);
        try {
            Files.setPosixFilePermissions(Paths.get(CarbonUtils.getCarbonHome() + File.separator +
                                                    AnalyticsConstants.SPARK_COMPUTE_CLASSPATH_SCRIPT_PATH), perms);
        } catch (IOException e) {
            log.warn("Error while checking the permission for " + AnalyticsConstants.SPARK_COMPUTE_CLASSPATH_SCRIPT_PATH
                     + ". " + e.getMessage());
        }
    }

    public void stop() {
        if (this.sqlCtx != null) {
            this.sqlCtx.sparkContext().stop();
        }
    }

    public int getNumPartitionsHint() {
        /* all workers will not have the same CPU count, this is just an approximation */
        return this.getWorkerCount() * Runtime.getRuntime().availableProcessors();
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

    public AnalyticsQueryResult executeQueryLocal(int tenantId, String query)
            throws AnalyticsExecutionException {
        query = query.trim();
        if (query.endsWith(";")) {
            query = query.substring(0, query.length() - 1);
        }
        query = encodeQueryWithTenantId(tenantId, query);
        if (log.isDebugEnabled()) {
            log.debug("Executing : " + query);
        }
        return toResult(this.sqlCtx.sql(query));
    }

    private String encodeQueryWithTenantId(int tenantId, String query)
            throws AnalyticsExecutionException {
        String result = query;
        // parse the query to see if it is a create temporary table
        // add the table names to the hz cluster map with tenantId -> table Name (put if absent)
        // iterate through the dist map and replace the relevant table names
        HazelcastInstance hz = AnalyticsServiceHolder.getHazelcastInstance();
        if (hz != null && this.isClustered) {
            this.sparkTableNames = hz.getMultiMap(AnalyticsConstants.TENANT_ID_AND_TABLES_MAP);
        } else if (this.inMemSparkTableNames == null) {
            this.inMemSparkTableNames = ArrayListMultimap.create();
        }

        Pattern p = Pattern.compile("(?i)(?<=(" + AnalyticsConstants.TERM_CREATE +
                                    "\\s" + AnalyticsConstants.TERM_TEMPORARY +
                                    "\\s" + AnalyticsConstants.TERM_TABLE + "))\\s+\\w+");
        Matcher m = p.matcher(query.trim());
        if (m.find()) {
            //this is a create table query
            // CREATE TEMPORARY TABLE <name> USING CarbonAnalytics OPTIONS(...)
            String tempTableName = m.group().trim();
            if (tempTableName.matches("(?i)if")) {
                throw new AnalyticsExecutionException("Malformed query: CREATE TEMPORARY TABLE IF NOT " +
                                                      "EXISTS is not supported");
            } else {
                if (this.clientMode) {
                    this.sparkTableNames.put(tenantId, tempTableName);
                } else {
                    this.inMemSparkTableNames.put(tenantId, tempTableName);
                }
                //replace the CA shorthand string in the query
                boolean carbonQuery = false;
                query = query.replaceFirst("\\b" + AnalyticsConstants.SPARK_SHORTHAND_STRING + "\\b",
                                           AnalyticsRelationProvider.class.getName());
                if (query.length() > result.length()) {
                    carbonQuery = true;
                }

                int optStrStart = query.toLowerCase().indexOf(AnalyticsConstants.TERM_OPTIONS, m.end());
                int bracketsOpen = query.indexOf("(", optStrStart);
                int bracketsClose = query.indexOf(")", bracketsOpen);

                //if its a carbon query, append the tenantId to the end of options
                String options;
                if (carbonQuery) {
                    options = query.substring(optStrStart, bracketsOpen + 1)
                              + addTenantIdToOptions(tenantId, query.substring(bracketsOpen + 1, bracketsClose))
                              + ")";
                } else {
                    options = query.substring(optStrStart, bracketsClose + 1);
                }

                String beforeOptions = replaceTableNamesInQuery(tenantId, query.substring(0, optStrStart));
                String afterOptions = replaceTableNamesInQuery(tenantId, query.substring(bracketsClose + 1, query.length()));
                result = beforeOptions + options + afterOptions;

            }
        } else {
            result = replaceTableNamesInQuery(tenantId, query);
        }
        return result.trim();
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

        Collection<String> tableNames;
        if (this.isClustered) {
            tableNames = this.sparkTableNames.get(tenantId);
        } else {
            tableNames = this.inMemSparkTableNames.get(tenantId);
        }

        for (String name : tableNames) {
            result = result.replaceAll("\\b" + name + "\\b",
                                       AnalyticsCommonUtils.encodeTableNameWithTenantId(tenantId, name));
        }
        return result;
    }

    private static AnalyticsQueryResult toResult(DataFrame dataFrame)
            throws AnalyticsExecutionException {
        return new AnalyticsQueryResult(dataFrame.schema().fieldNames(),
                                        convertRowsToObjects(dataFrame.collect()));
    }

    private static List<List<Object>> convertRowsToObjects(Row[] rows) {
        List<List<Object>> result = new ArrayList<>();
        List<Object> objects;
        for (Row row : rows) {
            objects = new ArrayList<>();
            for (int i = 0; i < row.length(); i++) {
                objects.add(row.get(i));
            }
            Set<PosixFilePermission> perms = new HashSet<>();
            result.add(objects);
        }
        return result;
    }

    @Override
    public void onBecomingLeader() {
        System.out.println("############### became the leader : ");
        for (LeaderElectable le : leaderElectable) {
            le.electedLeader();
        }
    }

    @Override
    public void onLeaderUpdate() {
        //nothing to do here
        System.out.println("########### leader updated!!!!!!!! : ");
    }

    public int getWorkerCount() {
        return workerCount;
    }

    @Override
    public void onMembersChangeForLeader() {
        try {
            this.workerCount = AnalyticsServiceHolder.getAnalyticsClusterManager().getMembers(CLUSTER_GROUP_NAME).size();
            log.info("Analytics worker updated, total count: " + this.getWorkerCount());
        } catch (AnalyticsClusterException e) {
            log.error("Error in extracting the worker count: " + e.getMessage(), e);
        }
    }

    private Map<String, String> loadCarbonSparkProperties(String filePath) {
        BufferedReader reader = null;
        Map<String, String> propsMap = new HashMap<>();
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#") || !line.startsWith("carbon.")) {
                    // skip if a comment or an empty line or does not start with "carbon."
                    i++;
                    continue;
                }

                if (line.endsWith(";")) {
                    line = line.substring(0, line.length());
                }

                String[] lineSplits = line.split("\\s+");
                if (lineSplits.length > 2) {
                    log.error("Error in spark-defaults.conf file at line " + (i + 1));
                } else {
                    propsMap.put(lineSplits[0], lineSplits[1]);
                }
                i++;
            }
        } catch (IOException e) {
            log.error("File not found ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Could not close buffered reader ", e);
                }
            }
        }
        return propsMap;
    }

    private int addLeaderElectable(LeaderElectable le) {
        this.leaderElectable.add(le);
        return (leaderElectable.size());
    }

    public void processLeaderElectable(LeaderElectable le) {
        if (AnalyticsServiceHolder.getAnalyticsClusterManager().isLeader(CLUSTER_GROUP_NAME)) {
            le.electedLeader();
        } else {
            addLeaderElectable(le);
        }
    }


}