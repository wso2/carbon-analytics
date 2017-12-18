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
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.status.dashboard.core.api.ApiResponseMessage;
import org.wso2.carbon.status.dashboard.core.api.NotFoundException;
import org.wso2.carbon.status.dashboard.core.api.WorkerServiceFactory;
import org.wso2.carbon.status.dashboard.core.api.WorkersApi;
import org.wso2.carbon.status.dashboard.core.api.WorkersApiService;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppMetricsHistory;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppStatus;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppsData;
import org.wso2.carbon.status.dashboard.core.bean.WorkerConfigurationDetails;
import org.wso2.carbon.status.dashboard.core.bean.WorkerGeneralDetails;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMetricsHistory;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMetricsSnapshot;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMoreMetricsHistory;
import org.wso2.carbon.status.dashboard.core.bean.WorkerResponce;
import org.wso2.carbon.status.dashboard.core.dbhandler.DeploymentConfigs;
import org.wso2.carbon.status.dashboard.core.dbhandler.StatusDashboardMetricsDBHandler;
import org.wso2.carbon.status.dashboard.core.dbhandler.StatusDashboardWorkerDBHandler;
import org.wso2.carbon.status.dashboard.core.exception.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.impl.utils.Constants;
import org.wso2.carbon.status.dashboard.core.internal.ApiResponseMessageWithCode;
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

import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.PROTOCOL;
import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.WORKER_JVM_MEMORY_HEAP_COMMITTED;
import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.WORKER_JVM_MEMORY_HEAP_INIT;

/**
 * This API implement for handling the stream processor worker hadling such asadding , deleating, editing, fletching
 * data from DB and API connection handling.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-11T07:55:11.886Z")
@Component(service = WorkersApiServiceImpl.class,
        immediate = true)
public class WorkersApiServiceImpl extends WorkersApiService {
    private static final int MAX_SIDDHI_APPS_PER_PAGE = 100;
    private static final Log logger = LogFactory.getLog(WorkersApiService.class);
    private Gson gson = new Gson();
    private static final Map<String, String> workerIDCarbonIDMap = new HashMap<>();
    private DeploymentConfigs dashboardConfigurations;
    private PermissionProvider permissionProvider;
    private static final String STATS_MANAGER_PERMISSION_STRING = Constants.PERMISSION_APP_NAME +
            Constants.PERMISSION_SUFFIX_METRICS_MANAGER;
    private static final String MANAGER_PERMISSION_STRING = Constants.PERMISSION_APP_NAME +
            Constants.PERMISSION_SUFFIX_MANAGER;
    private static final String VIWER_PERMISSION_STRING = Constants.PERMISSION_APP_NAME +
            Constants.PERMISSION_SUFFIX_VIEWER;

    public WorkersApiServiceImpl() {
        permissionProvider = DashboardDataHolder.getInstance().getPermissionProvider();
        dashboardConfigurations = DashboardDataHolder.getInstance().getStatusDashboardDeploymentConfigs();
    }

    /**
     * This is the activation method of ConfigServiceComponent. This will be called when it's references are fulfilled
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start()  {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) Status Dashboard WorkersApiServiceImpl API");
        }
    }

    /**
     * This is the deactivation method of ConfigServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) Status Dashboard WorkersApiServiceImpl API");
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
    public Response addWorker(Worker worker, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                MANAGER_PERMISSION_STRING));
        if (isAuthorized) {
            if (worker.getHost() != null) {
                String workerID = generateWorkerKey(worker.getHost(), String.valueOf(worker.getPort()));
                WorkerConfigurationDetails workerConfigData = new WorkerConfigurationDetails(workerID, worker.getHost(),
                        Integer.valueOf(worker.getPort()));
                StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
                try {
                    workerDBHandler.insertWorkerConfiguration(workerConfigData);
                } catch (RDBMSTableException e) {
                    return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                            "Error while adding the worker " + workerID + " caused by " + e.getMessage()))
                            .build();
                }
                //This part to be sucess is optiona at this level
                String response = getWorkerGeneralDetails(generateURLHostPort(worker.getHost(),
                        String.valueOf(worker.getPort())), workerID);
                if (!response.contains("Unnable to reach worker.")) {
                    WorkerGeneralDetails workerGeneralDetails = gson.fromJson(response,
                            WorkerGeneralDetails.class);
                    workerGeneralDetails.setWorkerId(workerID);
                    try {
                        workerDBHandler.insertWorkerGeneralDetails(workerGeneralDetails);
                    } catch (RDBMSTableException e) {
                        logger.warn("Worker " + removeCRLFCharacters(workerID) +
                                " currently not active. Retry to reach " + "later");
                    }
                    workerIDCarbonIDMap.put(workerID, workerGeneralDetails.getCarbonId());
                    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Worker id: "
                            + workerID + "sucessfully added.")).build();
                } else if (response.contains("Unnable to reach worker.")) {
                    //shold able to add a worker so the responce is ok
                    return Response.status(Response.Status.OK).entity(new ApiResponseMessage
                            (ApiResponseMessage.OK, "Worker id: "
                                    + workerID + "sucessfully added. But worker not reachable.")).build();
                } else {
                    //if the respnce is null but should able to add a worker
                    return Response.status(Response.Status.OK).entity(new ApiResponseMessage
                            (ApiResponseMessage.OK, "Worker id: "
                                    + workerID + ("sucessfully added. But unknown error has occured while trying to reach " +
                                    "worker"))).build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid data :" + worker.toString()).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
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
    public Response getAllWorkers(String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            Map<String, List<WorkerOverview>> groupedWorkers = new HashMap<>();
            StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
            List<WorkerConfigurationDetails> workerList = workerDBHandler.selectAllWorkers();
            if (!workerList.isEmpty()) {
                workerList.parallelStream().forEach(worker ->
                        {
                            try {
                                WorkerOverview workerOverview = new WorkerOverview();
                                feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                                                generateURLHostPort(worker.getHost(), String.valueOf(worker.getPort())),
                                        getUsername(),
                                        getPassword()).getWorker();
                                if (workerResponse != null) {
                                    Long timeInMillis = System.currentTimeMillis();
                                    String responseBody = workerResponse.body().toString();
                                    ServerDetails serverDetails = gson.fromJson(responseBody, ServerDetails.class);
                                    String message = serverDetails.getMessage();
                                    if (message == null || message.isEmpty()) {
                                        workerOverview.setStatusMessage("Success");
                                    } else {
                                        workerOverview.setStatusMessage(message);
                                    }
                                    feign.Response activeSiddiAppsResponse = WorkerServiceFactory
                                            .getWorkerHttpsClient(PROTOCOL + generateURLHostPort(worker
                                                            .getHost(), String.valueOf(worker.getPort())),
                                                    getUsername(), getPassword()).getSiddhiApps(true);
                                    String activeSiddiAppsResponseBody = activeSiddiAppsResponse.body().toString();
                                    List<String> activeApps = gson.fromJson(activeSiddiAppsResponseBody,
                                            new TypeToken<List<String>>() {
                                            }.getType());
                                    feign.Response inactiveSiddiAppsResponse = WorkerServiceFactory
                                            .getWorkerHttpsClient(PROTOCOL + generateURLHostPort(worker
                                                            .getHost(), String.valueOf(worker.getPort())),
                                                    getUsername(), getPassword()).getSiddhiApps(false);
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
                                    List nonClusterList = groupedWorkers.get(Constants.NON_CLUSTERS_ID);
                                    String clusterID = serverDetails.getClusterId();
                                    List existing = groupedWorkers.get(clusterID);
                                    if (serverDetails.getClusterId() == null && (nonClusterList == null)) {
                                        List<WorkerOverview> workers = new ArrayList<>();
                                        workers.add(workerOverview);
                                        groupedWorkers.put(Constants.NON_CLUSTERS_ID, workers);
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
                                    lastSnapshot.updateRunningStatus(Constants.NOT_REACHABLE_ID);
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
                                    serverDetails.setRunningStatus(Constants.NOT_REACHABLE_ID);
                                    workerOverview.setServerDetails(serverDetails);
                                    workerOverview.setLastUpdate((long) 0);
                                    //grouping the never reached
                                    if (groupedWorkers.get(Constants.NEVER_REACHED) == null) {
                                        List<WorkerOverview> workers = new ArrayList<>();
                                        workers.add(workerOverview);
                                        groupedWorkers.put(Constants.NEVER_REACHED, workers);
                                    } else {
                                        List existing = groupedWorkers.get(Constants.NEVER_REACHED);
                                        existing.add(workerOverview);
                                    }
                                }
                            }
                        }
                );
            }
            String jsonString = new Gson().toJson(groupedWorkers);
            return Response.ok().entity(jsonString).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Unauthorized for user : " + username).build();
        }
    }


    /**
     * Get worker general details.
     *
     * @param id worker Id
     * @return General details of the worker.
     * @throws NotFoundException
     */
    public Response populateWorkerGeneralDetails(String id, String userName) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(userName, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
            WorkerGeneralDetails workerGeneralDetails = workerDBHandler.selectWorkerGeneralDetails(id);
            if (workerGeneralDetails == null) {
                String[] hostPort = id.split(Constants.WORKER_KEY_GENERATOR);
                if (hostPort.length == 2) {
                    String workerUri = generateURLHostPort(hostPort[0], hostPort[1]);
                    String response = getWorkerGeneralDetails(workerUri, id);
                    if (!response.contains("Unnable to reach worker.")) {
                        WorkerGeneralDetails newWorkerGeneralDetails = gson.fromJson(response, WorkerGeneralDetails
                                .class);
                        newWorkerGeneralDetails.setWorkerId(id);
                        //isnser to the DB
                        workerDBHandler.insertWorkerGeneralDetails(newWorkerGeneralDetails);
                        workerIDCarbonIDMap.put(id, newWorkerGeneralDetails.getCarbonId());
                        return Response.ok().entity(response).build();
                    } else {
                        String jsonString = new Gson().
                                toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.DATA_NOT_FOUND,
                                        response));
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                    }
                } else {
                    logger.error("Invalid format of worker id " + removeCRLFCharacters(id));
                    return Response.status(Response.Status.BAD_REQUEST).build();
                }
            } else {
                String responseBody = gson.toJson(workerGeneralDetails, WorkerGeneralDetails.class);
                return Response.status(Response.Status.OK).entity(responseBody).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + userName).build();
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
    public Response getWorkerHistory(String workerId, String period, String type, Boolean more, String username) throws
            NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String carbonId = workerIDCarbonIDMap.get(workerId);
            if (carbonId == null) {
                carbonId = getCarbonID(workerId);
            }
            long timeInterval = period != null ? parsePeriod(period) : Constants.DEFAULT_TIME_INTERVAL_MILLIS;
            StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
            if (type == null) {
                if ((more != null) && more) {
                    WorkerMoreMetricsHistory history = new WorkerMoreMetricsHistory();
                    if (timeInterval <= 3600000) {
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
                        history.setJvmOsVirtualMemoryCommittedSize(metricsDBHandler.selectWorkerMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_VIRTUAL_MEMORY_COMMITTED_SIZE,
                                System.currentTimeMillis()));
                        //if only enabled
                        history.setJvmMemoryPoolsSize(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_POOL, System.currentTimeMillis()));
                        history.setJvmThreadsBlockedCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_BLOCKED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsDeadlockCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_DEADLOCKED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsNewCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_NEW_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsRunnableCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_RUNNABLE_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsTerminatedCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_TERMINATED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsTimedWaitingCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_TIMD_WATING_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsWaitingCount(metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_WAITING_THREADS_COUNT, System.currentTimeMillis()));
                    } else {
                        history.setJvmClassLoadingLoadedCurrent(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_CLASS_LOADING_LOADED_CURRENT,
                                System.currentTimeMillis()));
                        history.setJvmClassLoadingLoadedTotal(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.WORKER_JVM_CLASS_LOADING_LOADED_TOTAL, System.currentTimeMillis()));
                        history.setJvmClassLoadingUnloadedTotal(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.WORKER_JVM_CLASS_LOADING_UNLOADED_TOTAL, System.currentTimeMillis()));
                        history.setJvmGcPsMarksweepCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.WORKER_JVM_GC_PS_MARKSWEEP_COUNT, System.currentTimeMillis()));
                        history.setJvmGcPsMarksweepTime(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.WORKER_JVM_GC_PS_MARKSWEEP_TIME, System.currentTimeMillis()));
                        history.setJvmGcPsScavengeCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.WORKER_JVM_GC_PS_SCAVENGE_COUNT, System.currentTimeMillis()));
                        history.setJvmGcPsScavengeTime(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.WORKER_JVM_GC_PS_SCAVENGE_TIME, System.currentTimeMillis()));
                        history.setJvmMemoryHeapCommitted(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis()));
                        history.setJvmMemoryHeapInit(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryHeapMax(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryHeapUsage(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_USAGE, System.currentTimeMillis()));
                        history.setJvmMemoryHeapUsed(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_USED, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapInit(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapMax(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapCommitted(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_COMMITTED, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapUsage(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_USAGE, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapUsed(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_USED, System.currentTimeMillis()));
                        history.setJvmMemoryTotalCommitted(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_TOTAL_COMMITTED, System.currentTimeMillis()));
                        history.setJvmMemoryTotalInit(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_TOTAL_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryTotalMax(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_TOTAL_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryTotalUsed(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_TOTAL_USED, System.currentTimeMillis()));
                        history.setJvmOsPhysicalMemoryTotalSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_TOTAL_SIZE,
                                System.currentTimeMillis()));
                        history.setJvmOsPhysicalMemoryFreeSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_FREE_SIZE,
                                System.currentTimeMillis()));
                        history.setJvmThreadsDaemonCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_THREADS_DAEMON_COUNT, System.currentTimeMillis()));
                        history.setJvmOsFileDescriptorMaxCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_MAX_COUNT,
                                System.currentTimeMillis()));
                        history.setJvmOsFileDescriptorOpenCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_OPEN_COUNT,
                                System.currentTimeMillis()));
                        history.setJvmThreadsCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmOsSwapSpaceTotalSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_SWAP_SPACE_TOTAL_SIZE, System.currentTimeMillis()));
                        history.setJvmOsSwapSpaceFreeSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_SWAP_SPACE_FREE_SIZE, System.currentTimeMillis()));
                        history.setJvmOsCpuLoadProcess(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_CPU_LOAD_PROCESS, System.currentTimeMillis()));
                        history.setJvmOsCpuLoadSystem(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_CPU_LOAD_SYSTEM, System.currentTimeMillis()));
                        history.setJvmOsSystemLoadAverage(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_SYSTEM_LOAD_AVERAGE, System.currentTimeMillis()));
                        history.setJvmOsVirtualMemoryCommittedSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_VIRTUAL_MEMORY_COMMITTED_SIZE,
                                System.currentTimeMillis()));
                        //if only enabled
                        history.setJvmMemoryPoolsSize(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_POOL, System.currentTimeMillis()));
                        history.setJvmThreadsBlockedCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_BLOCKED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsDeadlockCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_DEADLOCKED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsNewCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_NEW_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsRunnableCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_RUNNABLE_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsTerminatedCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_TERMINATED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsTimedWaitingCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_TIMD_WATING_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsWaitingCount(metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_WAITING_THREADS_COUNT, System.currentTimeMillis()));
                    }
                    String jsonString = new Gson().toJson(history);
                    return Response.ok().entity(jsonString).build();
                } else {
                    WorkerMetricsHistory workerMetricsHistory = new WorkerMetricsHistory();
                    if (timeInterval <= 3600000) {
                        List<List<Object>> workerThroughput = metricsDBHandler.selectWorkerThroughput(carbonId,
                                timeInterval, System.currentTimeMillis());
                        List<List<Object>> workerMemoryUsed = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.HEAP_MEMORY_USED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryTotal = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.HEAP_MEMORY_MAX, System.currentTimeMillis());
                        List<List<Object>> workerMemoryCommitted = metricsDBHandler.selectWorkerMetrics(carbonId,
                                timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryInit = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis());
                        List<List<Object>> workerSystemCUP = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.SYSTEM_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerProcessCUP = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.PROCESS_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerLoadAverage = metricsDBHandler.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.LOAD_AVG_USAGE, System.currentTimeMillis());
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
                        List<List<Object>> workerMemoryUsed = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.HEAP_MEMORY_USED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryTotal = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.HEAP_MEMORY_MAX, System.currentTimeMillis());
                        List<List<Object>> workerSystemCUP = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.SYSTEM_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerProcessCUP = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.PROCESS_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerLoadAverage = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.LOAD_AVG_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerMemoryCommitted = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryInit = metricsDBHandler.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
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
                                    timeInterval, Constants.HEAP_MEMORY_USED, System.currentTimeMillis());
                            List<List<Object>> workerMemoryTotal = metricsDBHandler.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.HEAP_MEMORY_MAX, System.currentTimeMillis());
                            List<List<Object>> workerMemoryCommitted = metricsDBHandler.selectWorkerMetrics(carbonId,
                                    timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis());
                            List<List<Object>> workerMemoryInit = metricsDBHandler.selectWorkerMetrics(carbonId,
                                    timeInterval, WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis());
                            workerMetricsHistory.setTotalMemory(workerMemoryTotal);
                            workerMetricsHistory.setUsedMemory(workerMemoryUsed);
                            workerMetricsHistory.setInitMemory(workerMemoryInit);
                            workerMetricsHistory.setCommittedMemory(workerMemoryCommitted);
                            break;
                        }
                        case "cpu": {
                            List<List<Object>> workerSystemCUP = metricsDBHandler.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.SYSTEM_CPU_USAGE, System.currentTimeMillis());
                            List<List<Object>> workerProcessCUP = metricsDBHandler.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.PROCESS_CPU_USAGE, System.currentTimeMillis());

                            workerMetricsHistory.setProcessCPUData(workerProcessCUP);
                            workerMetricsHistory.setSystemCPU(workerSystemCUP);
                            break;
                        }
                        case "load": {
                            List<List<Object>> workerLoadAverage = metricsDBHandler.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.LOAD_AVG_USAGE, System.currentTimeMillis());
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
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
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
    public Response getAllSiddhiApps(String workerId, String period, String type, Integer pangeNum, String username) throws
            NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String carbonId = workerIDCarbonIDMap.get(workerId);
            if (carbonId == null) {
                carbonId = getCarbonID(workerId);
            }
            String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                SiddhiAppMetricsHistory siddhiAppMetricsHistory;
                int curentPageNum = pangeNum == null ? 1 : pangeNum;
                SiddhiAppsData siddhiAppsData = new SiddhiAppsData(curentPageNum);
                siddhiAppsData.setMaxPageCount(MAX_SIDDHI_APPS_PER_PAGE);
                List<SiddhiAppStatus> siddhiAppMetricsHistoryList = new ArrayList<>();
                int timeInterval = period != null ? Integer.parseInt(period) : Constants.DEFAULT_TIME_INTERVAL_MILLIS;
                String workerid = generateURLHostPort(hostPort[0], hostPort[1]);
                StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
                try {
                    feign.Response workerSiddiAllApps = WorkerServiceFactory.getWorkerHttpsClient
                            (PROTOCOL + workerid, getUsername(), getPassword()).getAllAppDetails();
                    if (workerSiddiAllApps.status() == 200) {
                        String responseAppBody = workerSiddiAllApps.body().toString();
                        List<SiddhiAppStatus> totalApps = gson.fromJson(responseAppBody,
                                new TypeToken<List<SiddhiAppStatus>>() {
                                }.getType());
                        siddhiAppsData.setTotalAppsCount(totalApps.size());
                        int limit = curentPageNum * MAX_SIDDHI_APPS_PER_PAGE < totalApps.size() ?
                                curentPageNum * MAX_SIDDHI_APPS_PER_PAGE : totalApps.size();
                        if (!totalApps.isEmpty()) {
                            for (int i = (curentPageNum - 1) * MAX_SIDDHI_APPS_PER_PAGE; i < limit; i++) {
                                SiddhiAppStatus app = totalApps.get(i);
                                app.populateAgetime();
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
                                        String[] typesRequested = type.split(Constants.URL_PARAM_SPLITTER);
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
                    } else {
                        String jsonString = new Gson().toJson(siddhiAppsData);
                        return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();
                    }
                } catch (feign.RetryableException e) {
                    String jsonString = new Gson().toJson(siddhiAppsData);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                }
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
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
    public Response getAppHistory(String workerId, String appName, String period, String type, String username)
            throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            List<SiddhiAppMetricsHistory> siddhiAppList = new ArrayList<>();
            String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String carbonId = workerIDCarbonIDMap.get(workerId);
                if (carbonId == null) {
                    carbonId = getCarbonID(workerId);
                }
                long timeInterval = period != null ? parsePeriod(period) : Constants.DEFAULT_TIME_INTERVAL_MILLIS;
                StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
                if (timeInterval <= 3600000) {
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
                } else {
                    SiddhiAppMetricsHistory siddhiAppMetricsHistory = new SiddhiAppMetricsHistory(appName);
                    List<List<Object>> memory = metricsDBHandler.selectAppAggOverallMetrics("memory", carbonId,
                            timeInterval, appName, System.currentTimeMillis());
                    siddhiAppMetricsHistory.setMemory(memory);
                    List<List<Object>> throughput = metricsDBHandler.selectAppAggOverallMetrics("throughput",
                            carbonId, timeInterval, appName, System.currentTimeMillis());
                    siddhiAppMetricsHistory.setThroughput(throughput);
                    List<List<Object>> latency = metricsDBHandler.selectAppAggOverallMetrics("latency",
                            carbonId, timeInterval, appName, System.currentTimeMillis());
                    siddhiAppMetricsHistory.setLatency(latency);
                    siddhiAppList.add(siddhiAppMetricsHistory);
                }
                String jsonString = new Gson().toJson(siddhiAppList);
                return Response.ok().entity(jsonString).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
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
    public Response getSiddhiAppDetails(String id, String appName, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = id.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String workerURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response siddhiAppResponce = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                            workerURIBody, this.getUsername(), this.getPassword()).getSiddhiApp(appName);
                    String responseAppBody = siddhiAppResponce.body().toString();
                    if (siddhiAppResponce.status() == 200) {
                        return Response.ok().entity(responseAppBody).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity(responseAppBody).build();
                    }
                } catch (feign.RetryableException e) {
                    String jsonString = new Gson().
                            toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.SERVER_CONNECTION_ERROR,
                                    e.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                }
            }
            return Response.status(Response.Status.BAD_REQUEST).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Try to reach the worker node;
     *
     * @param workerURI host:port
     * @return response from the worker.
     */
    private String getWorkerGeneralDetails(String workerURI, String workerId) {
        try {
            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL + workerURI,
                    this.getUsername(), this.getPassword()).getSystemDetails();
            return workerResponse.body().toString();
        } catch (feign.RetryableException e) {
            if (logger.isDebugEnabled()) {
                logger.warn(removeCRLFCharacters(workerId) + " Unnable to reach worker.", e);
            } else {
                logger.warn(removeCRLFCharacters(workerId) + " Unnable to reach worker.");
            }
            return workerId + " Unnable to reach worker. Caused by: " + e.getMessage();
        }
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
                String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
                String responce = getWorkerGeneralDetails(generateURLHostPort(hostPort[0], hostPort[1]), workerId);
                if (!responce.contains("Unnable to reach worker.")) {
                    WorkerGeneralDetails workerGeneralDetails = gson.fromJson(responce, WorkerGeneralDetails.class);
                    workerGeneralDetails.setWorkerId(workerId);
                    workerDBHandler.insertWorkerGeneralDetails(workerGeneralDetails);
                    workerIDCarbonIDMap.put(workerId, workerGeneralDetails.getCarbonId());
                    return workerGeneralDetails.getCarbonId();
                }
                logger.warn("could not find carbon id hend use worker ID " + removeCRLFCharacters(workerId) +
                        "as carbon id");
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
    public Response getSiddhiAppComponents(String workerId, String appName, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
            String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String carbonId = workerIDCarbonIDMap.get(workerId);
                if (carbonId == null) {
                    carbonId = getCarbonID(workerId);
                }
                Map<String, List<String>> components = metricsDBHandler.selectAppComponentsList(carbonId, appName,
                        Constants.DEFAULT_TIME_INTERVAL_MILLIS, System.currentTimeMillis());
                List componentsRecentMetrics = metricsDBHandler.selectComponentsLastMetric
                        (carbonId, appName, components, Constants.DEFAULT_TIME_INTERVAL_MILLIS,
                                System.currentTimeMillis());
                String json = gson.toJson(componentsRecentMetrics);
                return Response.ok().entity(json).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    @Override
    public Response getRolesByUsername(String username, String permissionSuffix) {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                Constants.PERMISSION_APP_NAME + "." + permissionSuffix));
        if (isAuthorized) {
            return Response.ok()
                    .entity(isAuthorized)
                    .build();
        } else {
            return Response.ok()
                    .entity(isAuthorized)
                    .build();
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
        return host + Constants.WORKER_KEY_GENERATOR + port;
    }

    /**
     * Generate the worker ker wich is use for rest call.
     *
     * @param host the Host of the worker node
     * @param port the Port of the worker node
     * @return returnconcadinating the host:port
     */
    private String generateURLHostPort(String host, String port) {
        return host + Constants.URL_HOST_PORT_SEPERATOR + port;
    }

    /**
     * Delete an existing worker.
     *
     * @param id worker Id
     * @return Response whether the worker is sucessfully deleted or not.
     * @throws NotFoundException
     */
    @Override
    public Response deleteWorker(String id, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                MANAGER_PERMISSION_STRING));
        if (isAuthorized) {
            StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
            try {
                workerDBHandler.deleteWorkerGeneralDetails(id);
                boolean result = workerDBHandler.deleteWorkerConfiguration(id);
                if (result) {
                    workerIDCarbonIDMap.remove(id);
                }
                return Response.status(Response.Status.OK).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                        "Worker is deleted successfully")).build();
            } catch (RDBMSTableException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Error while deleting the " +
                                "worker " + e.getMessage())).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
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
    public Response enableSiddhiAppStats(String workerId, String appName, StatsEnable statEnable, String username)
            throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                STATS_MANAGER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String uri = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL + uri,
                            getUsername(), getPassword()).enableAppStatistics(appName, statEnable);
                    if (workerResponse.status() == 200) {
                        return Response.ok().entity(workerResponse.body().toString()).build();
                    } else {
                        logger.error(workerResponse.body());
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(workerResponse.body())
                                .build();
                    }
                } catch (feign.RetryableException e) {
                    String jsonString = new Gson().
                            toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.SERVER_CONNECTION_ERROR,
                                    e.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                }
            }
            return Response.status(Response.Status.BAD_REQUEST).entity("Invalid url format").build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    @Override
    public Response getHADetails(String workerId, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
            ServerHADetails serverHADetails = new ServerHADetails();
            int status = 404;
            if (hostPort.length == 2) {
                String uri = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL + uri,
                            getUsername(),
                            getPassword()).getWorker();
                    String responseBody = workerResponse.body().toString();
                    status = workerResponse.status();
                    try {
                        //sucess senario
                        serverHADetails = gson.fromJson(responseBody, ServerHADetails.class);
                    } catch (JsonSyntaxException e) {
                        logger.error("Error parsing the responce", e);
                    }
                } catch (feign.RetryableException e) {
                    String jsonString = new Gson().
                            toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.SERVER_CONNECTION_ERROR,
                                    e.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                }
            }
            String jsonString = new Gson().toJson(serverHADetails);
            if (status == 200) {
                return Response.ok().entity(jsonString).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    @Override
    public Response getComponentHistory(String workerId, String appName, String componentType, String componentId
            , String period, String type, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String carbonId = getCarbonID(workerId);
            StatusDashboardMetricsDBHandler metricsDBHandler = WorkersApi.getMetricStore();
            long timeInterval = period != null ? parsePeriod(period) : Constants.DEFAULT_TIME_INTERVAL_MILLIS;
            Map<String, List<List<Object>>> componentHistory = new HashMap<>();
            // || ("Microsoft SQL Server").equalsIgnoreCase(dbType)
            if ((timeInterval <= 3600000)) {
                switch (componentType.toLowerCase()) {
                    case "streams": {
                        String metricsType = "throughput";
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
                        String metricsType = "throughput";
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
            } else {
                switch (componentType.toLowerCase()) {
                    case "streams": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "trigger": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "storequeries": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "queries": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "memory";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "tables": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "memory";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "throughput";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sources": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sinks": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sourcemappers": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sinkmappers": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricsDBHandler.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                }
            }
            String json = gson.toJson(componentHistory);
            return Response.ok().entity(json).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Return the worker configuration fromthe worker services table for using when editing the worker.
     *
     * @param id the worker ID
     * @return Responce with the worker configuration.
     * @throws NotFoundException
     */
    @Override
    public Response getWorkerConfig(String id, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            StatusDashboardWorkerDBHandler workerDBHandler = WorkersApi.getDashboardStore();
            WorkerConfigurationDetails workerConfig = workerDBHandler.selectWorkerConfigurationDetails(id);
            Worker worker = new Worker();
            if (workerConfig != null) {
                worker.setHost(workerConfig.getHost());
                worker.setPort(workerConfig.getPort());
            }
            String jsonString = new Gson().toJson(worker);
            return Response.ok().entity(jsonString).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
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
    public Response testConnection(String workerId, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
            int status = 404;
            if (hostPort.length == 2) {
                WorkerResponce workerResponce = new WorkerResponce();
                String uri = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL + uri,
                            getUsername(),
                            getPassword()).getWorker();
                    status = workerResponse.status();
                    if (status == 200) {
                        workerResponce.setCode(200);
                        workerResponce.setMessage("Sucessfully reached the worker : " + workerId);
                    } else if(status == 404){
                        workerResponce.setCode(404);
                        workerResponce.setMessage("Cannot reach the worker. Worker : " + workerId + " is not " +
                                "reachable");
                    } else if(status == 401){
                        workerResponce.setCode(401);
                        workerResponce.setMessage("Cannot reach the worker. Worker : " + workerId +
                                " has wrong credentials.");
                    } else {
                        workerResponce.setCode(500);
                        workerResponce.setMessage("Worker : " + workerId +
                                " not reachable by unexpected internal server error.");
                    }
                } catch (feign.RetryableException e) {
                    workerResponce.setCode(404);
                    workerResponce.setMessage("Worker : " + workerId + " is not " +
                    "reachable");
                }
                String jsonString = new Gson().toJson(workerResponce);
                return Response.ok().entity(jsonString).build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("Worker : " + workerId +
                        " is not in a format of expected").build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Read the SP Status Dashboard YML file and returen polling enterval.
     *
     * @return the polling interval of realtime data.
     * @throws NotFoundException
     */
    @Override
    public Response getDashboardConfig(String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            DashboardConfig config = new DashboardConfig();
            config.setPollingInterval(dashboardConfigurations.getPollingInterval());
            String jsonString = new Gson().toJson(config);
            return Response.ok().entity(jsonString).build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Get worker asscess username.
     *
     * @return
     */

    private String getUsername() {
        return dashboardConfigurations.getUsername();
    }

    /**
     * GetGet worker asscess password.
     *
     * @return
     */
    private String getPassword() {
        return dashboardConfigurations.getPassword();
    }

    /**
     * Parser for time
     *
     * @param interval query time interval.
     * @return
     */
    private long parsePeriod(String interval) {
        long millisVal = Constants.DEFAULT_TIME_INTERVAL_MILLIS;
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
        } else if (interval.contains("ms")) {
            millisVal = Long.parseLong(numberOnly);
        } else {
            try {
                millisVal = Long.parseLong(interval);
            } catch (ClassCastException | NumberFormatException e) {
                logger.error(String.format("Invalid parsing the value time period %s to milliseconds. Hence proceed " +
                        "with default time", removeCRLFCharacters(interval)), e);
            }
        }
        return millisVal;
    }

    private String removeCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }
}
