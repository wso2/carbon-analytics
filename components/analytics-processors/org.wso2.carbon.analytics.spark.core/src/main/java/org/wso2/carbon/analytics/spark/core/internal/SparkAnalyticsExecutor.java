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
import org.apache.spark.deploy.master.Master;
import org.apache.spark.deploy.master.MasterArguments;
import org.apache.spark.deploy.worker.Worker;
import org.apache.spark.deploy.worker.WorkerArguments;
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
import org.wso2.carbon.utils.CarbonUtils;
import scala.None$;
import scala.Option;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the analytics query execution context.
 */
public class SparkAnalyticsExecutor implements GroupEventListener {

    private static final int BASE_WORKER_UI_PORT = 8090;

    private static final int BASE_WORKER_PORT = 4501;

    private static final int DEFAULT_SPARK_UI_PORT = 4040;

    private static final String MASTER_PORT_GROUP_PROP = "MASTER_PORT";

    private static final String MASTER_HOST_GROUP_PROP = "MASTER_HOST";

    private static final int BASE_WEBUI_PORT = 8081;

    private static final int BASE_MASTER_PORT = 7077;

    private static final String CLUSTER_GROUP_NAME = "CARBON_ANALYTICS_EXECUTION";

    private static final String LOCAL_MASTER_URL = "local";

    private static final String CARBON_ANALYTICS_SPARK_APP_NAME = "CarbonAnalytics";

    private static final String WORKER_CORES = "1";

    private static final String WORKER_MEMORY = "1g";

    private static final String WORK_DIR = "work";

    private static final Log log = LogFactory.getLog(SparkAnalyticsExecutor.class);

    private SparkConf sparkConf;

    private JavaSparkContext javaSparkCtx;

    private SQLContext sqlCtx;

    private String myHost;

    private int portOffset;

    private int workerCount = 1;

    private MultiMap<Integer, String> sparkTableNames;

    private ListMultimap<Integer, String> inMemSparkTableNames;

    private boolean isClustered = false;

    private UDFConfiguration udfConfiguration;

    private Object workerActorSystem;
    private Object masterActorSystem;

    public SparkAnalyticsExecutor(String myHost, int portOffset) throws AnalyticsException {
        this.myHost = myHost;
        this.portOffset = portOffset;
        this.udfConfiguration = this.loadUDFConfiguration();
        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        if (acm.isClusteringEnabled()) {
            this.initSparkDataListener();
            acm.joinGroup(CLUSTER_GROUP_NAME, this);
        } else {
            this.initLocalClient();
        }
    }

    private UDFConfiguration loadUDFConfiguration() throws AnalyticsException {
        try {
            File confFile = new File(GenericUtils.getAnalyticsConfDirectory() +
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
        if (this.sparkConf==null) {
            this.sparkConf = initSparkConf(masterUrl, appName);
        }
//        this.javaSparkCtx = new JavaSparkContext(this.sparkConf);
//        this.sqlCtx = new SQLContext(this.javaSparkCtx);
        initSqlContext(new JavaSparkContext(this.sparkConf));
    }

    private void initLocalClient() {
        this.sparkConf = new SparkConf();
        this.sparkConf.setMaster(LOCAL_MASTER_URL).setAppName(CARBON_ANALYTICS_SPARK_APP_NAME);
        this.sparkConf.set("spark.ui.port", String.valueOf(DEFAULT_SPARK_UI_PORT + portOffset));
        this.javaSparkCtx = new JavaSparkContext(this.sparkConf);
//        this.sqlCtx = new SQLContext(this.javaSparkCtx);
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

    private void startMaster(String host, String port, String webUIport, String propsFile,
                             SparkConf sc) {
        String[] argsArray = new String[]{"-h", host,
                "-p", port,
                "--webui-port", webUIport,
                "--properties-file", propsFile //CarbonUtils.getCarbonHome() + File.separator + AnalyticsConstants.SPARK_DEFAULTS_PATH
        };
        MasterArguments args = new MasterArguments(argsArray, sc);
        this.masterActorSystem = Master.startSystemAndActor(args.host(), args.port(), args.webUiPort(), sc)._1();
    }

    private void startWorker(String workerHost, String masterHost, String masterPort,
                             String workerPort,
                             String workerUiPort, String workerCores, String workerMemory,
                             String workerDir,
                             String propFile, SparkConf sc) {
        String master = "spark://" + masterHost + ":" + masterPort;
        String[] argsArray = new String[]{master,
                "-h", workerHost,
                "-p", workerPort,
                "--webui-port", workerUiPort,
                "-c", workerCores,
                "-m", workerMemory,
                "-d", workerDir,
                "--properties-file", propFile //CarbonUtils.getCarbonHome() + File.separator + AnalyticsConstants.SPARK_DEFAULTS_PATH
        };
        WorkerArguments args = new WorkerArguments(argsArray, this.sparkConf);
        this.workerActorSystem = Worker.startSystemAndActor(args.host(), args.port(), args.webUiPort(),
                args.cores(), args.memory(), args.masters(),
                args.workDir(), (Option) None$.MODULE$, sc)._1();
    }

    private SparkConf initSparkConf(String masterUrl, String appName) {
        SparkConf conf = new SparkConf();
        conf.setIfMissing("spark.master", masterUrl);
        conf.setIfMissing("spark.app.name", appName);
        conf.set("spark.ui.port", String.valueOf(DEFAULT_SPARK_UI_PORT + portOffset));
        return conf;
    }

    private void initSparkDataListener() {
        this.validateSparkScriptPathPermission();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        SparkDataListener listener = new SparkDataListener();
        executor.execute(listener);
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
//            this.javaSparkCtx.close();
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
        if (hz != null) {
            this.isClustered = true;
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
                if (this.isClustered) {
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
            result.add(objects);
        }
        return result;
    }

    @Override
    public void onBecomingLeader() {
        String propsFile = CarbonUtils.getCarbonHome() + File.separator
                + AnalyticsConstants.SPARK_DEFAULTS_PATH;
        Utils.loadDefaultSparkProperties(new SparkConf(), propsFile);
        log.info("Spark defaults loaded from " + propsFile);

        String masterPort = System.getProperty(AnalyticsConstants.SPARK_MASTER_PORT);
        if (masterPort == null) {
            masterPort = Integer.toString(BASE_MASTER_PORT + this.portOffset);
        }
        String webuiPort = System.getProperty(AnalyticsConstants.SPARK_MASTER_WEBUI_PORT);
        if (webuiPort == null) {
            webuiPort = Integer.toString(BASE_WEBUI_PORT + this.portOffset);
        }

        String master = System.getProperty(AnalyticsConstants.SPARK_MASTER);
        if (master == null) {
            master = "spark://" + this.myHost + ":" + masterPort;
        }
        String appName = System.getProperty(AnalyticsConstants.SPARK_APP_NAME);
        if (appName == null) {
            appName = CARBON_ANALYTICS_SPARK_APP_NAME;
        }
        this.sparkConf = initSparkConf(master, appName);

        this.startMaster(this.myHost, masterPort, webuiPort, propsFile, this.sparkConf);

        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        acm.setProperty(CLUSTER_GROUP_NAME, MASTER_HOST_GROUP_PROP, this.myHost);
        acm.setProperty(CLUSTER_GROUP_NAME, MASTER_PORT_GROUP_PROP, masterPort);
        log.info("Analytics master started: [" + master + "]");
    }

    @Override
    public void onLeaderUpdate() {
        String propsFile = CarbonUtils.getCarbonHome() + File.separator
                + AnalyticsConstants.SPARK_DEFAULTS_PATH;
        Utils.loadDefaultSparkProperties(new SparkConf(), propsFile);
        log.info("Spark defaults loaded from " + propsFile);

        AnalyticsClusterManager acm = AnalyticsServiceHolder.getAnalyticsClusterManager();
        //take master information from the cluster
        String masterHost = (String) acm.getProperty(CLUSTER_GROUP_NAME, MASTER_HOST_GROUP_PROP);
        String masterPort = (String) acm.getProperty(CLUSTER_GROUP_NAME, MASTER_PORT_GROUP_PROP);

        String workerPort = System.getProperty(AnalyticsConstants.SPARK_WORKER_PORT);
        if (workerPort == null) {
            workerPort = Integer.toString(BASE_WORKER_PORT + this.portOffset);
        }

        String workerUiPort = System.getProperty(AnalyticsConstants.SPARK_WORKER_WEBUI_PORT);
        if (workerUiPort == null) {
            workerUiPort = Integer.toString(BASE_WORKER_UI_PORT + this.portOffset);
        }

        String workerCores = System.getProperty(AnalyticsConstants.SPARK_WORKER_CORES);
        if (workerCores == null) {
            workerCores = WORKER_CORES;
        }

        String workerMem = System.getProperty(AnalyticsConstants.SPARK_WORKER_MEMORY);
        if (workerMem == null) {
            workerMem = WORKER_MEMORY;
        }

        String workerDir = System.getProperty(AnalyticsConstants.SPARK_WORKER_DIR);
        if (workerDir == null) {
            workerDir = CarbonUtils.getCarbonHome() + File.separator + WORK_DIR;
        }

        String appName = System.getProperty(AnalyticsConstants.SPARK_APP_NAME);
        if (appName == null) {
            appName = CARBON_ANALYTICS_SPARK_APP_NAME;
        }

        this.startWorker(this.myHost, masterHost, masterPort, workerPort, workerUiPort, workerCores,
                workerMem, workerDir, propsFile, this.sparkConf);

        log.info("Analytics worker started: [" + this.myHost + ":" + workerPort + ":" + workerUiPort + "] "
                + "Master [" + masterHost + ":" + masterPort + "]");

        if (acm.isLeader(CLUSTER_GROUP_NAME)) {
            this.initClient("spark://" + masterHost + ":" + masterPort, appName);
        }

        log.info("Analytics client started: App Name [" + appName + "] Master [" + masterHost + ":" + masterPort + "]");
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

}