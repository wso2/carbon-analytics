/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.status.dashboard.core.impl;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.models.Role;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.status.dashboard.core.api.ApiResponseMessage;
import org.wso2.carbon.status.dashboard.core.api.NotFoundException;
import org.wso2.carbon.status.dashboard.core.api.WorkerServiceFactory;
import org.wso2.carbon.status.dashboard.core.api.WorkersApi;
import org.wso2.carbon.status.dashboard.core.api.WorkersApiService;
import org.wso2.carbon.status.dashboard.core.bean.InmemoryAuthenticationConfig;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppMetricsHistory;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppStatus;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppsData;
import org.wso2.carbon.status.dashboard.core.bean.SpDashboardConfiguration;
import org.wso2.carbon.status.dashboard.core.bean.WorkerConfigurationDetails;
import org.wso2.carbon.status.dashboard.core.bean.WorkerGeneralDetails;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMetricsHistory;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMetricsSnapshot;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMoreMetricsHistory;
import org.wso2.carbon.status.dashboard.core.dbhandler.StatusDashboardMetricsDBHandler;
import org.wso2.carbon.status.dashboard.core.dbhandler.StatusDashboardWorkerDBHandler;
import org.wso2.carbon.status.dashboard.core.dbhandler.exceptions.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.impl.utils.Constants;
import org.wso2.carbon.status.dashboard.core.internal.DashboardDataHolder;
import org.wso2.carbon.status.dashboard.core.internal.WorkerStateHolder;
import org.wso2.carbon.status.dashboard.core.model.DashboardConfig;
import org.wso2.carbon.status.dashboard.core.model.ServerDetails;
import org.wso2.carbon.status.dashboard.core.model.ServerHADetails;
import org.wso2.carbon.status.dashboard.core.model.StatsEnable;
import org.wso2.carbon.status.dashboard.core.model.Worker;
import org.wso2.carbon.status.dashboard.core.model.WorkerOverview;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.WORKER_JVM_MEMORY_HEAP_COMMITTED;
import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.WORKER_JVM_MEMORY_HEAP_INIT;

/**
 * This API implement for handling the stream processor worker hadling such asadding , deleating, editing, fletching
 * data from DB and API connection handling.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-11T07:55:11.886Z")
public class WorkersApiServiceImpl extends WorkersApiService {
    private static final String SYSTEM_CPU_USAGE = "jvm.os.cpu.load.system";
    private static final String PROCESS_CPU_USAGE = "jvm.os.cpu.load.process";
    private static final String HEAP_MEMORY_USED = "jvm.memory.heap.used";
    private static final String HEAP_MEMORY_MAX = "jvm.memory.heap.max";
    private static final String LOAD_AVG_USAGE = "jvm.os.system.load.average";
    private static final String WORKER_KEY_GENERATOR = "_";
    private static final String URL_HOST_PORT_SEPERATOR = ":";
    private static final String PROTOCOL = "http://";
    private static final String SIDDHI_APP_METRIC_TYPE = "SIDDHI_APP";
    private static final String URL_PARAM_SPLITTER = "&";
    private static final String WORKER_METRIC_TYPE = "WORKER";
    private static final String SELECT_ALL_EXPRESSION = "*";
    private static final String NON_CLUSTERS_ID = "Non Clusters";
    private static final String NOT_REACHABLE_ID = "Not-Reachable";
    private static final String NEVER_REACHED = "Never Reached";
    private static final int MAX_SIDDHI_APPS_PER_PAGE = 100;
    private static final Log logger = LogFactory.getLog(WorkersApiService.class);
    private static final int DEFAULT_TIME_INTERVAL_MILLIS = 300000;
    private Gson gson = new Gson();
    public static Map<String, String> workerIDCarbonIDMap = new HashMap<>();
    public static Map<String, InmemoryAuthenticationConfig> workerInmemoryConfigs = new HashMap<>();
    private SpDashboardConfiguration dashboardConfigurations;
    private IdPClient idPClient;

    public WorkersApiServiceImpl() {
        ConfigProvider configProvider = DashboardDataHolder.getInstance().getConfigProvider();
        DashboardConfig config = new DashboardConfig();
        try {
            dashboardConfigurations = configProvider
                    .getConfigurationObject(SpDashboardConfiguration.class);
        } catch (ConfigurationException e) {
            logger.error("Error getting the dashboard configuration.", e);
        }
    }

    /**
     * Add a new worker.
     *
     * @param worker Worker object that's needed to be added.
     * @return Response whether the worker is sucessfully added or not.
     * @throws NotFoundException
     */
    @Override
    public Response addWorker(Worker worker) throws NotFoundException {
        if (worker.getHost() != null) {
            String workerID = generateWorkerKey(worker.getHost(), String.valueOf(worker.getPort()));
            WorkerConfigurationDetails workerConfigData = new WorkerConfigurationDetails(workerID, worker.getHost(),
                    Integer.valueOf(worker.getPort()));
            StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
            try {
                workerDBHandler.insertWorkerConfiguration(workerConfigData);
            } catch (RDBMSTableException e) {
                return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                        "Error while adding the worker " + workerID + " caused by " + e.getMessage())).build();
            }
            String response = getWorkerGeneralDetails(generateURLHostPort(worker.getHost(),
                    String.valueOf(worker.getPort())), workerID);
            if (response != null) {
                WorkerGeneralDetails workerGeneralDetails = gson.fromJson(response,
                        WorkerGeneralDetails.class);
                workerGeneralDetails.setWorkerId(workerID);
                try {
                    workerDBHandler.insertWorkerGeneralDetails(workerGeneralDetails);
                } catch (RDBMSTableException e) {
                    logger.warn("Worker " + workerID + " currently not active. Retry to reach later");
                }
                workerIDCarbonIDMap.put(workerID, workerGeneralDetails.getCarbonId());
                workerInmemoryConfigs.put(workerID, new InmemoryAuthenticationConfig(this.getAdminUsername(),
                        this.getAdminPassword()));
            }
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Worker id: " + workerID +
                    "sucessfully added.")).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity("Invali data :" + worker.toString()).build();
        }
    }

    /**
     * Return all realtime statistics of the workers.If worker is not currently reachable then send the last
     * persistant state of that worker.
     *
     * @return Realtime data and status of workers.
     * @throws NotFoundException
     */
    @Override
    public Response getAllWorkers() throws NotFoundException {
        Map<String, List<WorkerOverview>> groupedWorkers = new HashMap<>();
        StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
        List<WorkerConfigurationDetails> workerList = workerDBHandler.selectAllWorkers();
        if (!workerList.isEmpty()) {
            // TODO: 11/12/17 need to maintain pool for supporting async
            workerList.stream().forEach(worker ->
                    {
                        try {
                            WorkerOverview workerOverview = new WorkerOverview();
                            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpClient("http://" +
                                            generateURLHostPort(worker.getHost(), String.valueOf(worker.getPort())),
                                    getAdminUsername(),
                                    getAdminPassword()).getWorker();
                            if (workerResponse != null) {
                                Long timeInMillis = System.currentTimeMillis();
                                String responseBody = workerResponse.body().toString();
                                ServerDetails serverDetails = null;
                                try {
                                    //sucess senario
                                    serverDetails = gson.fromJson(responseBody, ServerDetails.class);
                                    workerOverview.setStatusMessage("Success");
                                } catch (JsonSyntaxException e) {
                                    String[] decodeResponce = responseBody.split("#");
                                    if (decodeResponce.length == 2) {
                                        // if matrics not avalable
                                        serverDetails = gson.fromJson(decodeResponce[0], ServerDetails.class);
                                        workerOverview.setStatusMessage(decodeResponce[1]);
                                    } else {
                                        serverDetails = new ServerDetails();
                                    }
                                }
                                feign.Response activeSiddiAppsResponse = WorkerServiceFactory
                                        .getWorkerHttpClient(PROTOCOL +
                                                        generateURLHostPort(worker
                                                                .getHost(), String.valueOf(worker.getPort())),
                                                getAdminUsername(),
                                                getAdminPassword()).getSiddhiApps(true);
                                String activeSiddiAppsResponseBody = activeSiddiAppsResponse.body().toString();
                                List<String> activeApps = gson.fromJson(activeSiddiAppsResponseBody,
                                        new TypeToken<List<String>>() {
                                        }.getType());
                                feign.Response inactiveSiddiAppsResponse = WorkerServiceFactory
                                        .getWorkerHttpClient(PROTOCOL + generateURLHostPort(worker
                                                        .getHost(), String.valueOf(worker.getPort())), getAdminUsername(),
                                                getAdminPassword()).getSiddhiApps(false);
                                String inactiveSiddiAppsResponseBody = inactiveSiddiAppsResponse.body().toString();
                                List<String> inactiveApps = gson.fromJson(inactiveSiddiAppsResponseBody, new
                                        TypeToken<List<String>>() {
                                        }.getType());
                                serverDetails.setSiddhiApps(activeApps.size(), inactiveApps.size());
                                WorkerMetricsSnapshot snapshot = new WorkerMetricsSnapshot(serverDetails,
                                        timeInMillis);
                                WorkerStateHolder.addMetrics(worker.getWorkerId(), snapshot);
                                workerOverview.setLastUpdate(timeInMillis);
                                workerOverview.setWorkerId(worker.getWorkerId());
                                workerOverview.setServerDetails(serverDetails);
                                //grouping the clusters of the workers
                                List nonClusterList = groupedWorkers.get(NON_CLUSTERS_ID);
                                String clusterID = serverDetails.getClusterId();
                                List existing = groupedWorkers.get(clusterID);
                                if (serverDetails.getClusterId() == null && (nonClusterList == null)) {
                                    List<WorkerOverview> workers = new ArrayList<>();
                                    workers.add(workerOverview);
                                    groupedWorkers.put(NON_CLUSTERS_ID, workers);
                                } else if (clusterID == null && (nonClusterList != null)) {
                                    nonClusterList.add(workerOverview);
                                } else if (clusterID != null && (existing == null)) {
                                    List<WorkerOverview> workers = new ArrayList<>();
                                    workers.add(workerOverview);
                                    groupedWorkers.put(clusterID, workers);
                                } else if (clusterID != null && (existing != null)) {
                                    existing.add(workerOverview);
                                }
                            }
                        } catch (feign.RetryableException e) {
                            WorkerMetricsSnapshot lastSnapshot = WorkerStateHolder.getMetrics(worker.getWorkerId());
                            if (lastSnapshot != null) {
                                lastSnapshot.updateRunningStatus(NOT_REACHABLE_ID);
                                WorkerOverview workerOverview = new WorkerOverview();
                                workerOverview.setLastUpdate(lastSnapshot.getTimeStamp());
                                workerOverview.setWorkerId(worker.getWorkerId());
                                workerOverview.setServerDetails(lastSnapshot.getServerDetails());
                                if (groupedWorkers.get(lastSnapshot.getServerDetails().getClusterId()) != null) {
                                    groupedWorkers.get(lastSnapshot.getServerDetails().getClusterId())
                                            .add(workerOverview);
                                } else {
                                    List<WorkerOverview> workers = new ArrayList<>();
                                    workers.add(workerOverview);
                                    groupedWorkers.put(lastSnapshot.getServerDetails().getClusterId(), workers);
                                }
                            } else {
                                WorkerOverview workerOverview = new WorkerOverview();
                                workerOverview.setWorkerId(worker.getWorkerId());
                                ServerDetails serverDetails = new ServerDetails();
                                serverDetails.setRunningStatus(NOT_REACHABLE_ID);
                                workerOverview.setServerDetails(serverDetails);
                                workerOverview.setLastUpdate((long) 0);
                                //grouping the never reached
                                if (groupedWorkers.get(NEVER_REACHED) == null) {
                                    List<WorkerOverview> workers = new ArrayList<>();
                                    workers.add(workerOverview);
                                    groupedWorkers.put(NEVER_REACHED, workers);
                                } else {
                                    List existing = groupedWorkers.get(NEVER_REACHED);
                                    existing.add(workerOverview);
                                }
                            }
                        }
                    }
            );
        }
        String jsonString = new Gson().toJson(groupedWorkers);
        return Response.ok().entity(jsonString).build();
    }

    /**
     * Delete an existing worker.
     *
     * @param id worker Id
     * @return Response whether the worker is sucessfully deleted or not.
     * @throws NotFoundException
     */
    @Override
    public Response deleteWorker(String id) throws NotFoundException {
        StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
        try {
            workerDBHandler.deleteWorkerGeneralDetails(id);
            boolean result = workerDBHandler.deleteWorkerConfiguration(id);
            if (result) {
                workerIDCarbonIDMap.remove(id);
                workerInmemoryConfigs.remove(id);
            }
            return Response.status(Response.Status.OK).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                    "Worker is deleted successfully")).build();
        } catch (RDBMSTableException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Error while deleting the " +
                            "worker " + e.getMessage())).build();
        }
    }

    /**
     * Get worker general details.
     *
     * @param id worker Id
     * @return General details of the worker.
     * @throws NotFoundException
     */
    @Override
    public Response getWorkerGeneralDetails(String id) throws NotFoundException {
        StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
        WorkerGeneralDetails workerGeneralDetails = workerDBHandler.selectWorkerGeneralDetails(id);
        if (workerGeneralDetails == null) {
            String[] hostPort = id.split(WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String workerUri = generateURLHostPort(hostPort[0], hostPort[1]);
                String responseBody = getWorkerGeneralDetails(workerUri, id);
                if (responseBody != null) {
                    WorkerGeneralDetails newWorkerGeneralDetails = gson.fromJson(responseBody, WorkerGeneralDetails
                            .class);
                    newWorkerGeneralDetails.setWorkerId(id);
                    workerDBHandler.insertWorkerGeneralDetails(newWorkerGeneralDetails);
                    workerIDCarbonIDMap.put(id, newWorkerGeneralDetails.getCarbonId());
                }
                return Response.ok().entity(responseBody).build();
            } else {
                logger.error("Invalid format of worker id " + id);
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            String responseBody = gson.toJson(workerGeneralDetails, WorkerGeneralDetails.class);
            return Response.ok().entity(responseBody).build();
        }
    }

    /**
     * Get worker metrics history such as latency,memory,load average
     *
     * @param workerId workerID
     * @param period   time interval that metrics needed.
     * @param type     type of metrics.
     * @return returnmetrics for a given time.
     * @throws NotFoundException
     */
    @Override
    public Response getWorkerHistory(String workerId, String period, String type, Boolean more) throws
            NotFoundException {
        String carbonId = workerIDCarbonIDMap.get(workerId);
        if (carbonId == null) {
            carbonId = getCarbonID(workerId);
        }
        long timeInterval = period != null ? parsPeriod(period) : DEFAULT_TIME_INTERVAL_MILLIS;
        StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
        if (type == null) {
            if ((more != null) && more) {
                WorkerMoreMetricsHistory history = new WorkerMoreMetricsHistory();
                if(timeInterval <= 3600000) {
                    history.setJvmClassLoadingLoadedCurrent(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_CLASS_LOADING_LOADED_CURRENT, System.currentTimeMillis()));
                    history.setJvmClassLoadingLoadedTotal(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_CLASS_LOADING_LOADED_TOTAL, System.currentTimeMillis()));
                    history.setJvmClassLoadingUnloadedTotal(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_CLASS_LOADING_UNLOADED_TOTAL, System.currentTimeMillis()));
                    history.setJvmGcPsMarksweepCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_GC_PS_MARKSWEEP_COUNT, System.currentTimeMillis()));
                    history.setJvmGcPsMarksweepTime(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_GC_PS_MARKSWEEP_TIME, System.currentTimeMillis()));
                    history.setJvmGcPsScavengeCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_GC_PS_SCAVENGE_COUNT, System.currentTimeMillis()));
                    history.setJvmGcPsScavengeTime(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_GC_PS_SCAVENGE_TIME, System.currentTimeMillis()));
                    history.setJvmMemoryHeapCommitted(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis()));
                    history.setJvmMemoryHeapInit(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis()));
                    history.setJvmMemoryHeapMax(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_HEAP_MAX, System.currentTimeMillis()));
                    history.setJvmMemoryHeapUsage(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_HEAP_USAGE, System.currentTimeMillis()));
                    history.setJvmMemoryHeapUsed(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_HEAP_USED, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapInit(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_INIT, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapMax(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_MAX, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapCommitted(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_COMMITTED, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapUsage(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_USAGE, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapUsed(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_USED, System.currentTimeMillis()));
                    history.setJvmMemoryTotalCommitted(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_TOTAL_COMMITTED, System.currentTimeMillis()));
                    history.setJvmMemoryTotalInit(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_TOTAL_INIT, System.currentTimeMillis()));
                    history.setJvmMemoryTotalMax(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_TOTAL_MAX, System.currentTimeMillis()));
                    history.setJvmMemoryTotalUsed(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_TOTAL_USED, System.currentTimeMillis()));
                    history.setJvmOsPhysicalMemoryTotalSize(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_TOTAL_SIZE, System.currentTimeMillis()));
                    history.setJvmOsPhysicalMemoryFreeSize(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_FREE_SIZE, System.currentTimeMillis()));
                    history.setJvmThreadsDaemonCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_THREADS_DAEMON_COUNT, System.currentTimeMillis()));
                    history.setJvmOsFileDescriptorMaxCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_MAX_COUNT, System.currentTimeMillis()));
                    history.setJvmOsFileDescriptorOpenCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_OPEN_COUNT, System.currentTimeMillis()));
                    history.setJvmThreadsCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_THREADS_COUNT, System.currentTimeMillis()));
                    history.setJvmOsSwapSpaceTotalSize(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_SWAP_SPACE_TOTAL_SIZE, System.currentTimeMillis()));
                    history.setJvmOsSwapSpaceFreeSize(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_SWAP_SPACE_FREE_SIZE, System.currentTimeMillis()));
                    history.setJvmOsCpuLoadProcess(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_CPU_LOAD_PROCESS, System.currentTimeMillis()));
                    history.setJvmOsCpuLoadSystem(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_CPU_LOAD_SYSTEM, System.currentTimeMillis()));
                    history.setJvmOsSystemLoadAverage(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_SYSTEM_LOAD_AVERAGE, System.currentTimeMillis()));
                    history.setJvmOsVirtualMemoryCommittedSize(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_VIRTUAL_MEMORY_COMMITTED_SIZE, System.currentTimeMillis()));
                } else {
                    history.setJvmClassLoadingLoadedCurrent(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_CLASS_LOADING_LOADED_CURRENT, System.currentTimeMillis()));
                    history.setJvmClassLoadingLoadedTotal(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_CLASS_LOADING_LOADED_TOTAL, System.currentTimeMillis()));
                    history.setJvmClassLoadingUnloadedTotal(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_CLASS_LOADING_UNLOADED_TOTAL, System.currentTimeMillis()));
                    history.setJvmGcPsMarksweepCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_GC_PS_MARKSWEEP_COUNT, System.currentTimeMillis()));
                    history.setJvmGcPsMarksweepTime(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_GC_PS_MARKSWEEP_TIME, System.currentTimeMillis()));
                    history.setJvmGcPsScavengeCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_GC_PS_SCAVENGE_COUNT, System.currentTimeMillis()));
                    history.setJvmGcPsScavengeTime(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_GC_PS_SCAVENGE_TIME, System.currentTimeMillis()));
                    history.setJvmMemoryHeapCommitted(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis()));
                    history.setJvmMemoryHeapInit(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis()));
                    history.setJvmMemoryHeapMax(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_HEAP_MAX, System.currentTimeMillis()));
                    history.setJvmMemoryHeapUsage(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_HEAP_USAGE, System.currentTimeMillis()));
                    history.setJvmMemoryHeapUsed(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_HEAP_USED, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapInit(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_INIT, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapMax(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_MAX, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapCommitted(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_COMMITTED, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapUsage(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_USAGE, System.currentTimeMillis()));
                    history.setJvmMemoryNonHeapUsed(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_NON_HEAP_USED, System.currentTimeMillis()));
                    history.setJvmMemoryTotalCommitted(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_TOTAL_COMMITTED, System.currentTimeMillis()));
                    history.setJvmMemoryTotalInit(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_TOTAL_INIT, System.currentTimeMillis()));
                    history.setJvmMemoryTotalMax(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_TOTAL_MAX, System.currentTimeMillis()));
                    history.setJvmMemoryTotalUsed(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_MEMORY_TOTAL_USED, System.currentTimeMillis()));
                    history.setJvmOsPhysicalMemoryTotalSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_TOTAL_SIZE, System.currentTimeMillis()));
                    history.setJvmOsPhysicalMemoryFreeSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_FREE_SIZE, System.currentTimeMillis()));
                    history.setJvmThreadsDaemonCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_THREADS_DAEMON_COUNT, System.currentTimeMillis()));
                    history.setJvmOsFileDescriptorMaxCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_MAX_COUNT, System.currentTimeMillis()));
                    history.setJvmOsFileDescriptorOpenCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_OPEN_COUNT, System.currentTimeMillis()));
                    history.setJvmThreadsCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_THREADS_COUNT, System.currentTimeMillis()));
                    history.setJvmOsSwapSpaceTotalSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_SWAP_SPACE_TOTAL_SIZE, System.currentTimeMillis()));
                    history.setJvmOsSwapSpaceFreeSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_SWAP_SPACE_FREE_SIZE, System.currentTimeMillis()));
                    history.setJvmOsCpuLoadProcess(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_CPU_LOAD_PROCESS, System.currentTimeMillis()));
                    history.setJvmOsCpuLoadSystem(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_CPU_LOAD_SYSTEM, System.currentTimeMillis()));
                    history.setJvmOsSystemLoadAverage(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_SYSTEM_LOAD_AVERAGE, System.currentTimeMillis()));
                    history.setJvmOsVirtualMemoryCommittedSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            Constants.WORKER_JVM_OS_VIRTUAL_MEMORY_COMMITTED_SIZE, System.currentTimeMillis()));
                }
                String jsonString = new Gson().toJson(history);
                return Response.ok().entity(jsonString).build();
            } else {
                WorkerMetricsHistory workerMetricsHistory = new WorkerMetricsHistory();
                if(timeInterval <= 3600000) {
                    List<List<Object>> workerThroughput = metricsDBHandler.selectWorkerThroughput(carbonId,
                            timeInterval, System.currentTimeMillis());
                    List<List<Object>> workerMemoryUsed = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            HEAP_MEMORY_USED, System.currentTimeMillis());
                    List<List<Object>> workerMemoryTotal = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            HEAP_MEMORY_MAX, System.currentTimeMillis());
                    List<List<Object>> workerMemoryCommitted = metricsDBHandler.selectWorkerMetrics(carbonId,
                            timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED , System.currentTimeMillis());
                    List<List<Object>> workerMemoryInit = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis());
                    List<List<Object>> workerSystemCUP = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            SYSTEM_CPU_USAGE, System.currentTimeMillis());
                    List<List<Object>> workerProcessCUP = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            PROCESS_CPU_USAGE, System.currentTimeMillis());
                    List<List<Object>> workerLoadAverage = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                            LOAD_AVG_USAGE, System.currentTimeMillis());
                    workerMetricsHistory.setLoadAverage(workerLoadAverage);
                    workerMetricsHistory.setProcessCPUData(workerProcessCUP);
                    workerMetricsHistory.setSystemCPU(workerSystemCUP);
                    workerMetricsHistory.setThroughput(workerThroughput);
                    workerMetricsHistory.setTotalMemory(workerMemoryTotal);
                    workerMetricsHistory.setUsedMemory(workerMemoryUsed);
                    workerMetricsHistory.setInitMemory(workerMemoryInit);
                    workerMetricsHistory.setCommittedMemory(workerMemoryCommitted);
                } else {
                    List<List<Object>> workerThroughput = metricsDBHandler.selectWorkerAggregatedThroughput(carbonId,
                            timeInterval, System.currentTimeMillis());
                    List<List<Object>> workerMemoryUsed = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            HEAP_MEMORY_USED, System.currentTimeMillis());
                    List<List<Object>> workerMemoryTotal = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            HEAP_MEMORY_MAX, System.currentTimeMillis());
                    List<List<Object>> workerSystemCUP = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            SYSTEM_CPU_USAGE, System.currentTimeMillis());
                    List<List<Object>> workerProcessCUP = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            PROCESS_CPU_USAGE, System.currentTimeMillis());
                    List<List<Object>> workerLoadAverage = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            LOAD_AVG_USAGE, System.currentTimeMillis());
                    List<List<Object>> workerMemoryCommitted = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                            timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED , System.currentTimeMillis());
                    List<List<Object>> workerMemoryInit = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                            WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis());
                    workerMetricsHistory.setLoadAverage(workerLoadAverage);
                    workerMetricsHistory.setProcessCPUData(workerProcessCUP);
                    workerMetricsHistory.setSystemCPU(workerSystemCUP);
                    workerMetricsHistory.setThroughput(workerThroughput);
                    workerMetricsHistory.setTotalMemory(workerMemoryTotal);
                    workerMetricsHistory.setUsedMemory(workerMemoryUsed);
                    workerMetricsHistory.setInitMemory(workerMemoryInit);
                    workerMetricsHistory.setCommittedMemory(workerMemoryCommitted);
                }
                String jsonString = new Gson().toJson(workerMetricsHistory);
                return Response.ok().entity(jsonString).build();
            }
        } else {
            WorkerMetricsHistory workerMetricsHistory = new WorkerMetricsHistory();
            String[] typesRequested = type.split(",");
            for (String eachType : typesRequested) {
                switch (eachType) {
                    case "memory": {
                        List<List<Object>> workerMemoryUsed = metricsDBHandler.selectWorkerMetrics(carbonId,
                                timeInterval, HEAP_MEMORY_USED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryTotal = metricsDBHandler.selectWorkerMetrics(carbonId,
                                timeInterval, HEAP_MEMORY_MAX, System.currentTimeMillis());
                        List<List<Object>> workerMemoryCommitted = metricsDBHandler.selectWorkerMetrics(carbonId,
                                timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED , System.currentTimeMillis());
                        List<List<Object>> workerMemoryInit = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis());
                        workerMetricsHistory.setTotalMemory(workerMemoryTotal);
                        workerMetricsHistory.setUsedMemory(workerMemoryUsed);
                        workerMetricsHistory.setInitMemory(workerMemoryInit);
                        workerMetricsHistory.setCommittedMemory(workerMemoryCommitted);
                        break;
                    }
                    case "cpu": {
                        List<List<Object>> workerSystemCUP = metricsDBHandler.selectWorkerMetrics(carbonId,
                                timeInterval, SYSTEM_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerProcessCUP = metricsDBHandler.selectWorkerMetrics(carbonId,
                                timeInterval, PROCESS_CPU_USAGE, System.currentTimeMillis());

                        workerMetricsHistory.setProcessCPUData(workerProcessCUP);
                        workerMetricsHistory.setSystemCPU(workerSystemCUP);
                        break;
                    }
                    case "load": {
                        List<List<Object>> workerLoadAverage = metricsDBHandler.selectWorkerMetrics(carbonId,
                                timeInterval, LOAD_AVG_USAGE, System.currentTimeMillis());
                        workerMetricsHistory.setLoadAverage(workerLoadAverage);
                        break;
                    }
                    case "throughput": {
                        List<List<Object>> workerThroughput = metricsDBHandler.selectWorkerThroughput(carbonId,
                                timeInterval, System.currentTimeMillis());
                        workerMetricsHistory.setThroughput(workerThroughput);
                        break;
                    }
                    default: {
                        throw new RuntimeException("Please Enter valid MetricElement type.");
                    }
                }
            }
            String jsonString = new Gson().toJson(workerMetricsHistory);
            return Response.ok().entity(jsonString).build();
        }
    }

    /**
     * Get all siddhi apps and siddhi app summary.
     *
     * @param carbonId
     * @param period
     * @param type
     * @return
     * @throws NotFoundException
     */
    @Override
    public Response getAllSiddhiApps(String workerId, String period, String type, Integer pangeNum) throws
            NotFoundException {
        String carbonId = workerIDCarbonIDMap.get(workerId);
        if (carbonId == null) {
            carbonId = getCarbonID(workerId);
        }
        String[] hostPort = workerId.split(WORKER_KEY_GENERATOR);
        if (hostPort.length == 2) {
            SiddhiAppMetricsHistory siddhiAppMetricsHistory = new SiddhiAppMetricsHistory();
            int curentPageNum = pangeNum == null ? 1 : pangeNum;
            SiddhiAppsData siddhiAppsData = new SiddhiAppsData(curentPageNum);
            siddhiAppsData.setMaxPageCount(MAX_SIDDHI_APPS_PER_PAGE);
            List<SiddhiAppStatus> siddhiAppMetricsHistoryList = new ArrayList<>();
            int timeInterval = period != null ? Integer.parseInt(period) : DEFAULT_TIME_INTERVAL_MILLIS;
            String workerid = generateURLHostPort(hostPort[0], hostPort[1]);
            StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
            InmemoryAuthenticationConfig usernamePasswordConfig = workerInmemoryConfigs.get(workerId);
            if (usernamePasswordConfig == null) {
                usernamePasswordConfig = getAuthConfig(workerId);
            }
            try {
                feign.Response workerSiddiAllApps = WorkerServiceFactory.getWorkerHttpClient(PROTOCOL + workerid,
                        usernamePasswordConfig.getUserName(),
                        usernamePasswordConfig.getPassWord()).getAllAppDetails();
                String responseAppBody = workerSiddiAllApps.body().toString();
                List<SiddhiAppStatus> totalApps = gson.fromJson(responseAppBody,
                        new TypeToken<List<SiddhiAppStatus>>() {}.getType());
                siddhiAppsData.setTotalAppsCount(totalApps.size());
                int limit =curentPageNum* MAX_SIDDHI_APPS_PER_PAGE < totalApps.size()?
                        curentPageNum*MAX_SIDDHI_APPS_PER_PAGE:totalApps.size();
                if (!totalApps.isEmpty()) {
                    for (int i = (curentPageNum - 1) * MAX_SIDDHI_APPS_PER_PAGE; i < limit; i++) {
                        SiddhiAppStatus app = totalApps.get(i);
                        String appName = app.getAppName();
                        siddhiAppMetricsHistory = new SiddhiAppMetricsHistory(appName);
                        if ((app.getStatus().equalsIgnoreCase("active")) && (app.isStatEnabled())) {
                            if (type == null) {
                                List<List<Object>> memory = metricsDBHandler.selectAppOverallMetrics
                                        ("memory", carbonId, timeInterval, appName,
                                                System.currentTimeMillis());
                                siddhiAppMetricsHistory.setMemory(memory);
                                List<List<Object>> throughput = metricsDBHandler.selectAppOverallMetrics
                                        ("throughput", carbonId, timeInterval, appName,
                                                System.currentTimeMillis());
                                siddhiAppMetricsHistory.setThroughput(throughput);
                                List<List<Object>> latency = metricsDBHandler.selectAppOverallMetrics
                                        ("latency", carbonId, timeInterval, appName,
                                                System.currentTimeMillis());
                                siddhiAppMetricsHistory.setLatency(latency);
                            } else {
                                String[] typesRequested = type.split(URL_PARAM_SPLITTER);
                                for (String eachType : typesRequested) {
                                    switch (eachType) {
                                        case "memory": {
                                            List<List<Object>> memory = metricsDBHandler.selectAppOverallMetrics
                                                    ("memory", carbonId, timeInterval, appName,
                                                            System.currentTimeMillis());
                                            siddhiAppMetricsHistory.setMemory(memory);
                                            break;
                                        }
                                        case "throughput": {
                                            List<List<Object>> throughput = metricsDBHandler.selectAppOverallMetrics
                                                    ("throughput", carbonId, timeInterval, appName,
                                                            System.currentTimeMillis());
                                            siddhiAppMetricsHistory.setThroughput(throughput);
                                            break;
                                        }
                                        case "latency": {
                                            List<List<Object>> latency = metricsDBHandler.selectAppOverallMetrics
                                                    ("latency", carbonId, timeInterval, appName,
                                                            System.currentTimeMillis());
                                            siddhiAppMetricsHistory.setLatency(latency);
                                            break;
                                        }
                                        default: {
                                            throw new RuntimeException("Please Enter valid MetricElement type.");
                                        }
                                    }
                                }
                            }
                        }
                        app.setAppMetricsHistory(siddhiAppMetricsHistory);
                        siddhiAppMetricsHistoryList.add(app);
                    }
                    siddhiAppsData.setSiddhiAppMetricsHistoryList(siddhiAppMetricsHistoryList);
                }
                String jsonString = new Gson().toJson(siddhiAppsData);
                return Response.ok().entity(jsonString).build();
            } catch (feign.RetryableException e) {
                String jsonString = new Gson().toJson(siddhiAppsData);
                return Response.ok().entity(jsonString).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /**
     * Get siddhi app metrics histrory such as memory,throughputand latency.
     *
     * @param id      the worker id of the siddhiapp.
     * @param appName siddhi application name.
     * @param period  time interval that metrics dataneeded to be get.
     * @param type    type of metrics which is needed to be taken.
     * @return response with metrics data.
     * @throws NotFoundException
     */
    @Override
    public Response getAppHistory(String workerId, String appName, String period, String type)
            throws NotFoundException {
        List<SiddhiAppMetricsHistory> siddhiAppList = new ArrayList<>();
        String[] hostPort = workerId.split(WORKER_KEY_GENERATOR);
        if (hostPort.length == 2) {
            String carbonId = workerIDCarbonIDMap.get(workerId);
            if (carbonId == null) {
                carbonId = getCarbonID(workerId);
            }
            long timeInterval = period != null ? parsPeriod(period) : DEFAULT_TIME_INTERVAL_MILLIS;
            StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
            SiddhiAppMetricsHistory siddhiAppMetricsHistory = new SiddhiAppMetricsHistory(appName);
            List<List<Object>> memory = metricsDBHandler.selectAppOverallMetrics("memory", carbonId,
                    timeInterval, appName, System.currentTimeMillis());
            siddhiAppMetricsHistory.setMemory(memory);
            List<List<Object>> throughput = metricsDBHandler.selectAppOverallMetrics("throughput",
                    carbonId, timeInterval, appName, System.currentTimeMillis());
            siddhiAppMetricsHistory.setThroughput(throughput);
            List<List<Object>> latency = metricsDBHandler.selectAppOverallMetrics("latency",
                    carbonId, timeInterval, appName, System.currentTimeMillis());
            siddhiAppMetricsHistory.setLatency(latency);
            siddhiAppList.add(siddhiAppMetricsHistory);
            String jsonString = new Gson().toJson(siddhiAppList);
            return Response.ok().entity(jsonString).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }


    /**
     * This method return the both siddi apptext view and flow chart.PS: Currently implemetented till text view.
     *
     * @param id      workerid of the siddhi app
     * @param appName siddhiapp name
     * @return the responce with the text view of the siddhi app.
     * @throws NotFoundException
     */
    @Override
    public Response getSiddhiAppDetails(String id, String appName) throws NotFoundException {
        String[] hostPort = id.split(WORKER_KEY_GENERATOR);
        if (hostPort.length == 2) {
            InmemoryAuthenticationConfig usernamePasswordConfig = workerInmemoryConfigs.get(id);
            if (usernamePasswordConfig == null) {
                usernamePasswordConfig = getAuthConfig(id);
            }
            String workerURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
            feign.Response workerSiddiActiveApps = WorkerServiceFactory.getWorkerHttpClient(PROTOCOL + workerURIBody,
                    usernamePasswordConfig.getUserName(), usernamePasswordConfig.getPassWord()).getSiddhiApp(appName);
            String responseAppBody = workerSiddiActiveApps.body().toString();
            return Response.ok().entity(responseAppBody).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).build();
    }

    /**
     * Try to reach the worker node;
     *
     * @param workerURI host:port
     * @return response from the worker.
     */
    private String getWorkerGeneralDetails(String workerURI, String workerId) {
        InmemoryAuthenticationConfig usernamePasswordConfig = workerInmemoryConfigs.get(workerId);
        if (usernamePasswordConfig == null) {
            usernamePasswordConfig = getAuthConfig(workerId);
        }
        try {
            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpClient(PROTOCOL + workerURI,
                    usernamePasswordConfig.getUserName(), usernamePasswordConfig.getPassWord()).getSystemDetails();
            return workerResponse.body().toString();
        } catch (feign.RetryableException e) {
            if (logger.isDebugEnabled()) {
                logger.warn(workerId + " Worker not reachable.", e);
            } else {
                logger.warn(workerId + " Worker not reachable.");
            }
        }
        return null;
    }

    /**
     * This is use when dashboard server restart
     *
     * @param id worker id
     * @return InmemoryAuthenticationConfig which is miss in inmemory map
     */
    public InmemoryAuthenticationConfig getAuthConfig(String id) {
        InmemoryAuthenticationConfig usernamePasswordConfig = new InmemoryAuthenticationConfig();
        usernamePasswordConfig.setUserName(getAdminUsername());
        usernamePasswordConfig.setPassWord(getAdminPassword());
        workerInmemoryConfigs.put(id, usernamePasswordConfig);
        return usernamePasswordConfig;
    }

    /**
     * Get the carbon id of thw worker if carbon id not presented in inmemry state.
     *
     * @param workerId the worker ID
     * @return
     */
    private String getCarbonID(String workerId) {
        if (workerId != null) {
            StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
            String workerGeneralCArbonId = null;
            workerGeneralCArbonId = workerDBHandler.selectWorkerCarbonID(workerId);
            if (workerGeneralCArbonId != null) {
                workerIDCarbonIDMap.put(workerId, workerGeneralCArbonId);
                return workerGeneralCArbonId;
            } else {
                String[] hostPort = workerId.split(WORKER_KEY_GENERATOR);
                String responce = getWorkerGeneralDetails(generateURLHostPort(hostPort[0], hostPort[1]), workerId);
                if (responce != null) {
                    WorkerGeneralDetails workerGeneralDetails = gson.fromJson(responce, WorkerGeneralDetails.class);
                    workerGeneralDetails.setWorkerId(workerId);
                    workerDBHandler.insertWorkerGeneralDetails(workerGeneralDetails);
                    workerIDCarbonIDMap.put(workerId, workerGeneralDetails.getCarbonId());
                    workerInmemoryConfigs.put(workerId, new InmemoryAuthenticationConfig(hostPort[0], hostPort[1]));
                    return workerGeneralDetails.getCarbonId();
                }
                logger.warn("could not find carbon id hend use worker ID " + workerId + "as carbon id");
                return workerId;
            }
        } else {
            return null;
        }
    }

    /**
     * Get all siddhi app components.
     *
     * @param id      carbon id of the worker.
     * @param appName the siddhi app name.
     * @return
     * @throws NotFoundException
     */
    @Override
    public Response getSiddhiAppComponents(String workerId, String appName) throws NotFoundException {
        StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
        String[] hostPort = workerId.split(WORKER_KEY_GENERATOR);
        if (hostPort.length == 2) {
            String carbonId = workerIDCarbonIDMap.get(workerId);
            if (carbonId == null) {
                carbonId = getCarbonID(workerId);
            }
            Map<String, List<String>> components = metricsDBHandler.selectAppComponentsList(carbonId, appName,
                    DEFAULT_TIME_INTERVAL_MILLIS, System.currentTimeMillis());
            List componentsRecentMetrics = metricsDBHandler.selectComponentsLastMetric
                    (carbonId, appName, components, DEFAULT_TIME_INTERVAL_MILLIS, System.currentTimeMillis());
            String json = gson.toJson(componentsRecentMetrics);
            return Response.ok().entity(json).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @Override
    public Response getRolesByUsername(String username) throws NotFoundException {
        try {
            List<Role> roles = idPClient.getUserRoles(username);
            return Response.ok().entity(roles).build();
        } catch (IdPClientException e) {
            logger.error("Cannot retrieve user roles for '" + username + "'.", e);
            return Response.serverError().entity("Cannot retrieve user roles for '" + username + "'.").build();
        }
    }

    /**
     * Generate the worker ker wich is uniquelyidenfy in the status dashboard as wellas routing.
     *
     * @param host the Host of the worker node
     * @param port the Port of the worker node
     * @return returnconcadinating the host_port
     */
    private String generateWorkerKey(String host, String port) {
        return host + WORKER_KEY_GENERATOR + port;
    }

    /**
     * Generate the worker ker wich is use for rest call.
     *
     * @param host the Host of the worker node
     * @param port the Port of the worker node
     * @return returnconcadinating the host:port
     */
    private String generateURLHostPort(String host, String port) {
        return host + URL_HOST_PORT_SEPERATOR + port;
    }

    /**
     * Enable or dissable the siddhi app metrics
     *
     * @param id      worker id
     * @param appName the appname
     * @return Responce
     * @throws NotFoundException
     */
    @Override
    public Response enableSiddhiAppStats(String workerId, String appName, StatsEnable statEnable)
            throws NotFoundException {
        String[] hostPort = workerId.split(WORKER_KEY_GENERATOR);
        if (hostPort.length == 2) {
            String uri = generateURLHostPort(hostPort[0], hostPort[1]);
            InmemoryAuthenticationConfig usernamePasswordConfig = workerInmemoryConfigs.get(workerId);
            if (usernamePasswordConfig == null) {
                usernamePasswordConfig = getAuthConfig(workerId);
            }
            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpClient(PROTOCOL + uri,
                    usernamePasswordConfig.getUserName(), usernamePasswordConfig.getPassWord()).enableAppStatistics
                    (appName, statEnable);

            if (workerResponse.status() == 200) {
                return Response.ok().entity(workerResponse.body().toString()).build();
            } else {
                logger.error(workerResponse.body());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(workerResponse.body()).build();
            }
        }
        return Response.status(Response.Status.BAD_REQUEST).entity("Invalid url format").build();
    }

    @Override
    public Response getHADetails(String workerId) throws NotFoundException {
        String[] hostPort = workerId.split(WORKER_KEY_GENERATOR);
        ServerHADetails serverHADetails = null;
        if (hostPort.length == 2) {
            String uri = generateURLHostPort(hostPort[0], hostPort[1]);
            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpClient(PROTOCOL + uri, getAdminUsername(),
                    getAdminPassword()).getWorker();
            String responseBody = workerResponse.body().toString();
            try {
                //sucess senario
                serverHADetails = gson.fromJson(responseBody, ServerHADetails.class);
            } catch (JsonSyntaxException e) {
                String[] decodeResponce = responseBody.split("#");
                if (decodeResponce.length == 2) {
                    // if matrics not avalable
                    serverHADetails = gson.fromJson(decodeResponce[0], ServerHADetails.class);
                } else {
                    serverHADetails = new ServerHADetails();
                }
            }
        }
        String jsonString = new Gson().toJson(serverHADetails);
        return Response.ok().entity(jsonString).build();
    }

    @Override
    public Response getComponentHistory(String workerId, String appName, String componentType, String componentId
            , String period, String type) throws NotFoundException {
        String carbonId = getCarbonID(workerId);
        StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
        long timeInterval = period != null ? parsPeriod(period) : DEFAULT_TIME_INTERVAL_MILLIS;
        Map<String, List<List<Object>>> componentHistory = new HashMap<>();
        switch (componentType.toLowerCase()) {
            case "streams": {
                String metricsType = "throughput";
                //org.wso2.siddhi.SiddhiApps.UniqueLengthBatchWindowSiddhiAppTest.Siddhi.Streams.
                // IgnoreOutputStream.throughput
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
            case "trigger": {
                String metricsType = "throughput";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
            case "storequeries": {
                String metricsType = "latency";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
            case "queries": {
                String metricsType = "latency";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                metricsType = "memory";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
            case "tables": {
                String metricsType = "latency";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                metricsType = "memory";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                metricsType = "throughput";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
            case "sources": {
                String metricsType = "throughput";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
            case "sinks": {
                String metricsType  = "throughput";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
            case "sourcemappers": {
                String metricsType = "latency";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
            case "sinkmappers": {
                String metricsType = "latency";
                componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsHistory(carbonId, appName,
                        timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                break;
            }
        }
        String json = gson.toJson(componentHistory);
        return Response.ok().entity(json).build();
    }

    /**
     * Return the worker configuration fromthe worker services table for using when editing the worker.
     *
     * @param id the worker ID
     * @return Responce with the worker configuration.
     * @throws NotFoundException
     */
    @Override
    public Response getWorkerConfig(String id) throws NotFoundException {
        StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
        WorkerConfigurationDetails workerConfig = workerDBHandler.selectWorkerConfigurationDetails(id);
        Worker worker = new Worker();
        if (workerConfig != null) {
            worker.setHost(workerConfig.getHost());
            worker.setPort(workerConfig.getPort());
        }
        String jsonString = new Gson().toJson(worker);
        return Response.ok().entity(jsonString).build();
    }

    /**
     * Test teh worker credentilas are ok or not.
     *
     * @param auth authentication details.
     * @param id   workerID
     * @return
     * @throws NotFoundException
     */
    @Override
    public Response testConnection(String auth) throws NotFoundException {
        // TODO: 10/16/17   This is not supported yet support with globle interceptor.
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "This is not supported yet"))
                .build();
    }

    /**
     * Read the SP Status Dashboard YML file and returen polling enterval.
     *
     * @return the polling interval of realtime data.
     * @throws NotFoundException
     */
    @Override
    public Response getDashboardConfig() throws NotFoundException {
        ConfigProvider configProvider = DashboardDataHolder.getInstance().getConfigProvider();
        DashboardConfig config = new DashboardConfig();
        if (dashboardConfigurations != null) {
            config.setPollingInterval(dashboardConfigurations.getPollingInterval());
        } else {
            loadConfig();
            config.setPollingInterval(dashboardConfigurations.getPollingInterval());
        }
        String jsonString = new Gson().toJson(config);
        return Response.ok().entity(jsonString).build();
    }

    private String getAdminUsername() {
        if (dashboardConfigurations != null) {
            return dashboardConfigurations.getAdminUsername();
        } else {
            loadConfig();
            return dashboardConfigurations.getAdminUsername();
        }
    }

    private String getAdminPassword() {
        if (dashboardConfigurations != null) {
            return dashboardConfigurations.getAdminPassword();
        } else {
            loadConfig();
            return dashboardConfigurations.getAdminPassword();
        }
    }

    private void loadConfig() {
        ConfigProvider configProvider = DashboardDataHolder.getInstance().getConfigProvider();
        DashboardConfig config = new DashboardConfig();
        try {
            dashboardConfigurations = configProvider
                    .getConfigurationObject(SpDashboardConfiguration.class);
        } catch (ConfigurationException e) {
            logger.error("Error getting the dashboard configuration.", e);
        }
    }

    private long parsPeriod(String interval) {
        long millisVal = DEFAULT_TIME_INTERVAL_MILLIS;
        String numberOnly = interval.replaceAll("[^0-9]", "");
        if (interval.contains("sec")) {
            millisVal = Long.parseLong(numberOnly) * 1000;
        } else if (interval.contains("min")) {
            millisVal = Long.parseLong(numberOnly) * 60000;
        } else if (interval.contains("hr")) {
            millisVal = Long.parseLong(numberOnly) * 3600000;
        } else if (interval.contains("wk")) {
            millisVal = Long.parseLong(numberOnly) * 604800000;
        } else if (interval.contains("day")) {
            millisVal = Long.parseLong(numberOnly) * 86400000;
        }else if (interval.contains("ms")) {
            millisVal = Long.parseLong(numberOnly);
        } else {
            try {
                millisVal = Long.parseLong(interval);
            } catch (ClassCastException | NumberFormatException e) {
                logger.error(String.format("Invalid parsing the value time period %d to milliseconds. Hence proceed " +
                        "with default time", interval));
            }
        }
        return millisVal;
    }

    @Reference(
            name = "IdPClient",
            service = IdPClient.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetIdP"
    )
    protected void setIdP(IdPClient client) {
        this.idPClient = client;
    }

    protected void unsetIdP(IdPClient client) {
        this.idPClient = null;
    }
}
