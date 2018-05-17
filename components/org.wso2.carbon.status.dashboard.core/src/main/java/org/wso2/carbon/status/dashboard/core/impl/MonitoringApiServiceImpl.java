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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.status.dashboard.core.api.ApiResponseMessage;
import org.wso2.carbon.status.dashboard.core.api.MonitoringApiService;
import org.wso2.carbon.status.dashboard.core.api.NotFoundException;
import org.wso2.carbon.status.dashboard.core.api.WorkerServiceFactory;
import org.wso2.carbon.status.dashboard.core.bean.ManagerClusterInfo;
import org.wso2.carbon.status.dashboard.core.bean.ManagerMetricsSnapshot;
import org.wso2.carbon.status.dashboard.core.bean.ManagerSiddhiApps;
import org.wso2.carbon.status.dashboard.core.bean.NodeConfigurationDetails;
import org.wso2.carbon.status.dashboard.core.bean.ParentSiddhiApp;
import org.wso2.carbon.status.dashboard.core.bean.ParentSummaryDetails;
import org.wso2.carbon.status.dashboard.core.bean.ResourceClusterInfo;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppMetricsHistory;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppStatus;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppSummaryInfo;
import org.wso2.carbon.status.dashboard.core.bean.SiddhiAppsData;
import org.wso2.carbon.status.dashboard.core.bean.WorkerGeneralDetails;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMetricsHistory;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMetricsSnapshot;
import org.wso2.carbon.status.dashboard.core.bean.WorkerMoreMetricsHistory;
import org.wso2.carbon.status.dashboard.core.bean.WorkerResponse;
import org.wso2.carbon.status.dashboard.core.dbhandler.DeploymentConfigs;
import org.wso2.carbon.status.dashboard.core.dbhandler.StatusDashboardDBHandler;
import org.wso2.carbon.status.dashboard.core.dbhandler.StatusDashboardMetricsDBHandler;
import org.wso2.carbon.status.dashboard.core.exception.RDBMSTableException;
import org.wso2.carbon.status.dashboard.core.impl.utils.Constants;
import org.wso2.carbon.status.dashboard.core.internal.ApiResponseMessageWithCode;
import org.wso2.carbon.status.dashboard.core.internal.MonitoringDataHolder;
import org.wso2.carbon.status.dashboard.core.internal.WorkerStateHolder;
import org.wso2.carbon.status.dashboard.core.internal.services.DatasourceServiceComponent;
import org.wso2.carbon.status.dashboard.core.internal.services.PermissionGrantServiceComponent;
import org.wso2.carbon.status.dashboard.core.model.DashboardConfig;
import org.wso2.carbon.status.dashboard.core.model.ManagerDetails;
import org.wso2.carbon.status.dashboard.core.model.ManagerOverView;
import org.wso2.carbon.status.dashboard.core.model.Node;
import org.wso2.carbon.status.dashboard.core.model.ServerDetails;
import org.wso2.carbon.status.dashboard.core.model.ServerHADetails;
import org.wso2.carbon.status.dashboard.core.model.StatsEnable;
import org.wso2.carbon.status.dashboard.core.model.WorkerOverview;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.HOUR;
import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.PROTOCOL;
import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.WORKER_JVM_MEMORY_HEAP_COMMITTED;
import static org.wso2.carbon.status.dashboard.core.impl.utils.Constants.WORKER_JVM_MEMORY_HEAP_INIT;

/**
 * This API implement for handling the stream processor worker hadling such asadding , deleating, editing, fletching
 * data from DB and API connection handling.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-11T07:55:11.886Z")
@Component(service = MonitoringApiService.class, immediate = true)
public class MonitoringApiServiceImpl extends MonitoringApiService {

    private static StatusDashboardDBHandler dashboardStore;
    private static StatusDashboardMetricsDBHandler metricStore;
    private static final int MAX_SIDDHI_APPS_PER_PAGE = 100;
    private static final Log logger = LogFactory.getLog(MonitoringApiService.class);
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

    public MonitoringApiServiceImpl() {
        permissionProvider = MonitoringDataHolder.getInstance().getPermissionProvider();
        dashboardConfigurations = MonitoringDataHolder.getInstance().getStatusDashboardDeploymentConfigs();
    }

    public static StatusDashboardDBHandler getDashboardStore() { //todo: remove static
        return dashboardStore;
    }

    /**
     * This is the deactivation method of ConfigServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) Status Dashboard MonitoringApiServiceImpl API");
        }
    }

    /**
     * This is the activation method of ConfigServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() {
        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) Status Dashboard MonitoringApiServiceImpl API");
        }
        dashboardStore = new StatusDashboardDBHandler();
        metricStore = new StatusDashboardMetricsDBHandler();
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
            List<NodeConfigurationDetails> workerList = dashboardStore.selectAllWorkers();
            List<NodeConfigurationDetails> managerList = dashboardStore.getAllManagerConfigDetails();
            List<String> ResourceClusteredWorkerNode = new ArrayList<>();
            if (!managerList.isEmpty()) {
                managerList.parallelStream().forEach(manager -> {
                    try {
                        feign.Response resourceResponse = WorkerServiceFactory.getWorkerHttpsClient(
                                PROTOCOL + generateURLHostPort(manager.getHost(), String.valueOf(manager
                                        .getPort())), this.getUsername(), this.getPassword()).getClusterNodeDetails();
                        if (resourceResponse.status() == 200) {
                            Reader inputStream = resourceResponse.body().asReader();
                            List<ResourceClusterInfo> clusterInfos = gson.fromJson(
                                    inputStream, new TypeToken<List<ResourceClusterInfo>>() {
                                    }.getType());
                            for (ResourceClusterInfo clusterInfo : clusterInfos) {
                                String nodeId = clusterInfo.getNodeId();
                                ResourceClusteredWorkerNode.add(nodeId);
                            }
                        }
                    } catch (feign.RetryableException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(removeCRLFCharacters(manager.getWorkerId()) + " Unnable to reach manager.", e);
                        }
                        logger.warn(removeCRLFCharacters(manager.getWorkerId()) + " Unnable to reach manager.");

                    } catch (IOException e) {
                        logger.warn("Error occured while getting the response " + e.getMessage());
                    }
                });
            }

            if (!workerList.isEmpty()) {
                workerList.parallelStream().forEach(worker -> {
                    if (!ResourceClusteredWorkerNode.contains(getCarbonID(worker.getWorkerId()))) {
                        try {
                            WorkerOverview workerOverview = new WorkerOverview();
                            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                                            generateURLHostPort(worker.getHost(), String.valueOf(worker.getPort())),
                                    getUsername(), getPassword()).getWorker();
                            if ((workerResponse != null) && (workerResponse.status() == 200)) {
                                Long timeInMillis = System.currentTimeMillis();
                                String responseBody = workerResponse.body().toString();
                                ServerDetails serverDetails = gson.fromJson(responseBody, ServerDetails.class);
                                String message = serverDetails.getMessage();
                                if (message == null || message.isEmpty()) {
                                    workerOverview.setStatusMessage("Success");
                                } else {
                                    workerOverview.setStatusMessage(message);
                                }
                                feign.Response activeSiddiAppsResponse = WorkerServiceFactory.getWorkerHttpsClient(
                                        PROTOCOL + generateURLHostPort(worker.getHost(), String.valueOf(
                                                worker.getPort())), getUsername(), getPassword()).getSiddhiApps(true);
                                String activeSiddiAppsResponseBody = activeSiddiAppsResponse.body().toString();
                                List<String> activeApps = gson.fromJson(activeSiddiAppsResponseBody,
                                        new TypeToken<List<String>>() {
                                        }.getType());
                                feign.Response inactiveSiddiAppsResponse = WorkerServiceFactory
                                        .getWorkerHttpsClient(PROTOCOL + generateURLHostPort(worker.getHost(),
                                                String.valueOf(worker.getPort())), getUsername(),
                                                getPassword()).getSiddhiApps(false);
                                String inactiveSiddiAppsResponseBody = inactiveSiddiAppsResponse.body().toString();
                                List<String> inactiveApps = gson.fromJson(inactiveSiddiAppsResponseBody,
                                        new TypeToken<List<String>>() {
                                        }.getType());
                                serverDetails.setSiddhiApps(activeApps.size(), inactiveApps.size());
                                WorkerMetricsSnapshot snapshot = new WorkerMetricsSnapshot(serverDetails, timeInMillis);
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
                            } else {
                                workerOverview.setWorkerId(worker.getWorkerId());
                                ServerDetails serverDetails = new ServerDetails();
                                serverDetails.setRunningStatus(Constants.NOT_REACHABLE_ID);
                                workerOverview.setStatusMessage(getErrorMessage(workerResponse.status()));
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
                });
            }
            String jsonString = new Gson().toJson(groupedWorkers);
            return Response.ok().entity(jsonString).build();
        } else {
            logger.error("Unauthorized for user : " + username);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Unauthorized for user : " + username).build();
        }
    }

    private String getErrorMessage(int errorCode) {

        if (errorCode == 401) {
            return "Unauthorize to reach worker";
        } else if (errorCode == 404) {
            return "Worker not found.";
        } else {
            return "Internal server error.";
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
            WorkerGeneralDetails workerGeneralDetails = dashboardStore.selectWorkerGeneralDetails(id);
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
                        dashboardStore.insertWorkerGeneralDetails(newWorkerGeneralDetails);
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
            logger.error("Unauthorized to perform get all workers for user : " + userName);
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
            if (type == null) {
                if ((more != null) && more) {
                    WorkerMoreMetricsHistory history = new WorkerMoreMetricsHistory();
                    if (timeInterval <= HOUR) {
                        history.setJvmClassLoadingLoadedCurrent(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_CLASS_LOADING_LOADED_CURRENT, System.currentTimeMillis()));
                        history.setJvmClassLoadingLoadedTotal(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_CLASS_LOADING_LOADED_TOTAL, System.currentTimeMillis()));
                        history.setJvmClassLoadingUnloadedTotal(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_CLASS_LOADING_UNLOADED_TOTAL, System.currentTimeMillis()));
                        history.setJvmGcPsMarksweepCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_GC_PS_MARKSWEEP_COUNT, System.currentTimeMillis()));
                        history.setJvmGcPsMarksweepTime(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_GC_PS_MARKSWEEP_TIME, System.currentTimeMillis()));
                        history.setJvmGcPsScavengeCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_GC_PS_SCAVENGE_COUNT, System.currentTimeMillis()));
                        history.setJvmGcPsScavengeTime(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_GC_PS_SCAVENGE_TIME, System.currentTimeMillis()));
                        history.setJvmMemoryHeapCommitted(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis()));
                        history.setJvmMemoryHeapInit(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryHeapMax(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryHeapUsage(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_USAGE, System.currentTimeMillis()));
                        history.setJvmMemoryHeapUsed(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_USED, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapInit(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_NON_HEAP_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapMax(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_NON_HEAP_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapCommitted(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_NON_HEAP_COMMITTED, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapUsage(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_NON_HEAP_USAGE, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapUsed(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_NON_HEAP_USED, System.currentTimeMillis()));
                        history.setJvmMemoryTotalCommitted(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_TOTAL_COMMITTED, System.currentTimeMillis()));
                        history.setJvmMemoryTotalInit(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_TOTAL_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryTotalMax(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_TOTAL_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryTotalUsed(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_TOTAL_USED, System.currentTimeMillis()));
                        history.setJvmOsPhysicalMemoryTotalSize(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_TOTAL_SIZE, System.currentTimeMillis()));
                        history.setJvmOsPhysicalMemoryFreeSize(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_FREE_SIZE, System.currentTimeMillis()));
                        history.setJvmThreadsDaemonCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_THREADS_DAEMON_COUNT, System.currentTimeMillis()));
                        history.setJvmOsFileDescriptorMaxCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_MAX_COUNT, System.currentTimeMillis()));
                        history.setJvmOsFileDescriptorOpenCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_OPEN_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmOsSwapSpaceTotalSize(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_SWAP_SPACE_TOTAL_SIZE, System.currentTimeMillis()));
                        history.setJvmOsSwapSpaceFreeSize(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_SWAP_SPACE_FREE_SIZE, System.currentTimeMillis()));
                        history.setJvmOsCpuLoadProcess(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_CPU_LOAD_PROCESS, System.currentTimeMillis()));
                        history.setJvmOsCpuLoadSystem(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_CPU_LOAD_SYSTEM, System.currentTimeMillis()));
                        history.setJvmOsSystemLoadAverage(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_SYSTEM_LOAD_AVERAGE, System.currentTimeMillis()));
                        history.setJvmOsVirtualMemoryCommittedSize(metricStore.selectWorkerMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_VIRTUAL_MEMORY_COMMITTED_SIZE,
                                System.currentTimeMillis()));
                        //if only enabled
                        history.setJvmMemoryPoolsSize(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_POOL, System.currentTimeMillis()));
                        history.setJvmThreadsBlockedCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_BLOCKED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsDeadlockCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_DEADLOCKED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsNewCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_NEW_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsRunnableCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_RUNNABLE_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsTerminatedCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_TERMINATED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsTimedWaitingCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_TIMD_WATING_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsWaitingCount(metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_WAITING_THREADS_COUNT, System.currentTimeMillis()));
                    } else {
                        history.setJvmClassLoadingLoadedCurrent(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_CLASS_LOADING_LOADED_CURRENT,
                                System.currentTimeMillis()));
                        history.setJvmClassLoadingLoadedTotal(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_CLASS_LOADING_LOADED_TOTAL,
                                System.currentTimeMillis()));
                        history.setJvmClassLoadingUnloadedTotal(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_CLASS_LOADING_UNLOADED_TOTAL,
                                System.currentTimeMillis()));
                        history.setJvmGcPsMarksweepCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_GC_PS_MARKSWEEP_COUNT, System.currentTimeMillis()));
                        history.setJvmGcPsMarksweepTime(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_GC_PS_MARKSWEEP_TIME, System.currentTimeMillis()));
                        history.setJvmGcPsScavengeCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_GC_PS_SCAVENGE_COUNT, System.currentTimeMillis()));
                        history.setJvmGcPsScavengeTime(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_GC_PS_SCAVENGE_TIME, System.currentTimeMillis()));
                        history.setJvmMemoryHeapCommitted(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis()));
                        history.setJvmMemoryHeapInit(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryHeapMax(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryHeapUsage(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_USAGE, System.currentTimeMillis()));
                        history.setJvmMemoryHeapUsed(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_HEAP_USED, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapInit(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapMax(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapCommitted(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_COMMITTED,
                                System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapUsage(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_USAGE, System.currentTimeMillis()));
                        history.setJvmMemoryNonHeapUsed(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_NON_HEAP_USED, System.currentTimeMillis()));
                        history.setJvmMemoryTotalCommitted(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_TOTAL_COMMITTED, System.currentTimeMillis()));
                        history.setJvmMemoryTotalInit(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_MEMORY_TOTAL_INIT, System.currentTimeMillis()));
                        history.setJvmMemoryTotalMax(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_TOTAL_MAX, System.currentTimeMillis()));
                        history.setJvmMemoryTotalUsed(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_TOTAL_USED, System.currentTimeMillis()));
                        history.setJvmOsPhysicalMemoryTotalSize(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_TOTAL_SIZE,
                                System.currentTimeMillis()));
                        history.setJvmOsPhysicalMemoryFreeSize(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_PHYSICAL_MEMORY_FREE_SIZE,
                                System.currentTimeMillis()));
                        history.setJvmThreadsDaemonCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_THREADS_DAEMON_COUNT, System.currentTimeMillis()));
                        history.setJvmOsFileDescriptorMaxCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_MAX_COUNT,
                                System.currentTimeMillis()));
                        history.setJvmOsFileDescriptorOpenCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_FILE_DESCRIPTOR_OPEN_COUNT,
                                System.currentTimeMillis()));
                        history.setJvmThreadsCount(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmOsSwapSpaceTotalSize(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_SWAP_SPACE_TOTAL_SIZE,
                                System.currentTimeMillis()));
                        history.setJvmOsSwapSpaceFreeSize(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_SWAP_SPACE_FREE_SIZE,
                                System.currentTimeMillis()));
                        history.setJvmOsCpuLoadProcess(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_CPU_LOAD_PROCESS, System.currentTimeMillis()));
                        history.setJvmOsCpuLoadSystem(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_OS_CPU_LOAD_SYSTEM, System.currentTimeMillis()));
                        history.setJvmOsSystemLoadAverage(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_OS_SYSTEM_LOAD_AVERAGE, System.currentTimeMillis()));
                        history.setJvmOsVirtualMemoryCommittedSize(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval,
                                Constants.WORKER_JVM_OS_VIRTUAL_MEMORY_COMMITTED_SIZE, System.currentTimeMillis()));
                        //if only enabled
                        history.setJvmMemoryPoolsSize(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_MEMORY_POOL, System.currentTimeMillis()));
                        history.setJvmThreadsBlockedCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_BLOCKED_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsDeadlockCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_DEADLOCKED_THREADS_COUNT,
                                System.currentTimeMillis()));
                        history.setJvmThreadsNewCount(metricStore.selectWorkerAggregatedMetrics(carbonId, timeInterval,
                                Constants.WORKER_JVM_NEW_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsRunnableCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_RUNNABLE_THREADS_COUNT, System.currentTimeMillis()));
                        history.setJvmThreadsTerminatedCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_TERMINATED_THREADS_COUNT,
                                System.currentTimeMillis()));
                        history.setJvmThreadsTimedWaitingCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_TIMD_WATING_THREADS_COUNT,
                                System.currentTimeMillis()));
                        history.setJvmThreadsWaitingCount(metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.WORKER_JVM_WAITING_THREADS_COUNT, System.currentTimeMillis()));
                    }
                    String jsonString = new Gson().toJson(history);
                    return Response.ok().entity(jsonString).build();
                } else {
                    WorkerMetricsHistory workerMetricsHistory = new WorkerMetricsHistory();
                    if (timeInterval <= HOUR) {
                        List<List<Object>> workerThroughput = metricStore.selectWorkerThroughput(carbonId,
                                timeInterval, System.currentTimeMillis());
                        List<List<Object>> workerMemoryUsed = metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.HEAP_MEMORY_USED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryTotal = metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.HEAP_MEMORY_MAX, System.currentTimeMillis());
                        List<List<Object>> workerMemoryCommitted = metricStore.selectWorkerMetrics(carbonId,
                                timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryInit = metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis());
                        List<List<Object>> workerSystemCUP = metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.SYSTEM_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerProcessCUP = metricStore.selectWorkerMetrics(carbonId, timeInterval,
                                Constants.PROCESS_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerLoadAverage = metricStore.selectWorkerMetrics(carbonId, timeInterval,
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
                        List<List<Object>> workerThroughput = metricStore.selectWorkerAggregatedThroughput(carbonId,
                                timeInterval, System.currentTimeMillis());
                        List<List<Object>> workerMemoryUsed = metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.HEAP_MEMORY_USED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryTotal = metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.HEAP_MEMORY_MAX, System.currentTimeMillis());
                        List<List<Object>> workerSystemCUP = metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.SYSTEM_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerProcessCUP = metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.PROCESS_CPU_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerLoadAverage = metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, Constants.LOAD_AVG_USAGE, System.currentTimeMillis());
                        List<List<Object>> workerMemoryCommitted = metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis());
                        List<List<Object>> workerMemoryInit = metricStore.selectWorkerAggregatedMetrics(carbonId,
                                timeInterval, WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis());
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
                            List<List<Object>> workerMemoryUsed = metricStore.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.HEAP_MEMORY_USED, System.currentTimeMillis());
                            List<List<Object>> workerMemoryTotal = metricStore.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.HEAP_MEMORY_MAX, System.currentTimeMillis());
                            List<List<Object>> workerMemoryCommitted = metricStore.selectWorkerMetrics(carbonId,
                                    timeInterval, WORKER_JVM_MEMORY_HEAP_COMMITTED, System.currentTimeMillis());
                            List<List<Object>> workerMemoryInit = metricStore.selectWorkerMetrics(carbonId,
                                    timeInterval, WORKER_JVM_MEMORY_HEAP_INIT, System.currentTimeMillis());
                            workerMetricsHistory.setTotalMemory(workerMemoryTotal);
                            workerMetricsHistory.setUsedMemory(workerMemoryUsed);
                            workerMetricsHistory.setInitMemory(workerMemoryInit);
                            workerMetricsHistory.setCommittedMemory(workerMemoryCommitted);
                            break;
                        }
                        case "cpu": {
                            List<List<Object>> workerSystemCUP = metricStore.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.SYSTEM_CPU_USAGE, System.currentTimeMillis());
                            List<List<Object>> workerProcessCUP = metricStore.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.PROCESS_CPU_USAGE, System.currentTimeMillis());

                            workerMetricsHistory.setProcessCPUData(workerProcessCUP);
                            workerMetricsHistory.setSystemCPU(workerSystemCUP);
                            break;
                        }
                        case "load": {
                            List<List<Object>> workerLoadAverage = metricStore.selectWorkerMetrics(carbonId,
                                    timeInterval, Constants.LOAD_AVG_USAGE, System.currentTimeMillis());
                            workerMetricsHistory.setLoadAverage(workerLoadAverage);
                            break;
                        }
                        case "throughput": {
                            List<List<Object>> workerThroughput = metricStore.selectWorkerThroughput(carbonId,
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
            logger.error("Unauthorized to perform get worker history for user : " + username);
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
    public Response getAllSiddhiApps(String workerId, String period, String type, Integer pangeNum,
                                     String username) throws
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
                StatusDashboardMetricsDBHandler metricsDBHandler = metricStore;
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
                                                    List<List<Object>> throughput =
                                                            metricsDBHandler.selectAppOverallMetrics
                                                                    ("throughput", carbonId, timeInterval, appName,
                                                                            System.currentTimeMillis());
                                                    siddhiAppMetricsHistory.setThroughput(throughput);
                                                    break;
                                                }
                                                case "latency": {
                                                    List<List<Object>> latency =
                                                            metricsDBHandler.selectAppOverallMetrics
                                                                    ("latency", carbonId, timeInterval, appName,
                                                                            System.currentTimeMillis());
                                                    siddhiAppMetricsHistory.setLatency(latency);
                                                    break;
                                                }
                                                default: {
                                                    throw new RuntimeException(
                                                            "Please Enter valid MetricElement type.");
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
                    } else if (workerSiddiAllApps.status() == 401) {
                        String jsonString = new Gson().toJson(siddhiAppsData);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
                    } else {
                        String jsonString = new Gson().toJson(siddhiAppsData);
                        return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();
                    }
                } catch (feign.RetryableException e) {
                    String jsonString = new Gson().toJson(siddhiAppsData);
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                }
            }
            logger.error("Inproper format of worker ID:" + workerId);
            return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of worker ID:" + workerId)
                    .build();
        } else {
            logger.error("Unauthorized to perform get all siddhi apps for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }


    /**
     * Get siddhi app metrics histrory such as memory,throughput and latency.
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
                if (timeInterval <= HOUR) {
                    SiddhiAppMetricsHistory siddhiAppMetricsHistory = new SiddhiAppMetricsHistory(appName);
                    List<List<Object>> memory = metricStore.selectAppOverallMetrics("memory", carbonId,
                            timeInterval, appName,
                            System.currentTimeMillis());
                    siddhiAppMetricsHistory.setMemory(memory);
                    List<List<Object>> throughput = metricStore.selectAppOverallMetrics("throughput",
                            carbonId, timeInterval, appName,
                            System.currentTimeMillis());
                    siddhiAppMetricsHistory.setThroughput(throughput);
                    List<List<Object>> latency = metricStore.selectAppOverallMetrics("latency",
                            carbonId, timeInterval, appName,
                            System.currentTimeMillis());
                    siddhiAppMetricsHistory.setLatency(latency);
                    siddhiAppList.add(siddhiAppMetricsHistory);
                } else {
                    SiddhiAppMetricsHistory siddhiAppMetricsHistory = new SiddhiAppMetricsHistory(appName);
                    List<List<Object>> memory = metricStore.selectAppAggOverallMetrics("memory", carbonId,
                            timeInterval, appName,
                            System.currentTimeMillis());
                    siddhiAppMetricsHistory.setMemory(memory);
                    List<List<Object>> throughput = metricStore.selectAppAggOverallMetrics("throughput",
                            carbonId, timeInterval,
                            appName,
                            System.currentTimeMillis());
                    siddhiAppMetricsHistory.setThroughput(throughput);
                    List<List<Object>> latency = metricStore.selectAppAggOverallMetrics("latency",
                            carbonId, timeInterval, appName,
                            System.currentTimeMillis());
                    siddhiAppMetricsHistory.setLatency(latency);
                    siddhiAppList.add(siddhiAppMetricsHistory);
                }
                String jsonString = new Gson().toJson(siddhiAppList);
                return Response.ok().entity(jsonString).build();
            } else {
                logger.error("Inproper format of worker ID:" + workerId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of worker ID:" + workerId)
                        .build();
            }
        } else {
            logger.error("Unauthorized to perform get siddhi app history for user : " + username);
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
                                    workerURIBody,
                            this.getUsername(),
                            this.getPassword())
                            .getSiddhiApp(appName);
                    String responseAppBody = siddhiAppResponce.body().toString();
                    if (siddhiAppResponce.status() == 200) {
                        return Response.ok().entity(responseAppBody).build();
                    } else if (siddhiAppResponce.status() == 401) {
                        String jsonString = new Gson().toJson(responseAppBody);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
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
            logger.error("Inproper format of worker ID:" + id);
            return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of worker ID:" + id).build();
        } else {
            logger.error("Unauthorized to perform get siddhi app details for user : " + username);
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
                    this.getUsername(),
                    this.getPassword())
                    .getSystemDetails();
            if (workerResponse.status() == 200) {
                return workerResponse.body().toString();
            } else {
                return workerId + " Unnable to reach worker. Caused by: " + getErrorMessage(workerResponse.status());
            }
        } catch (feign.RetryableException e) {
            if (logger.isDebugEnabled()) {
                logger.warn(removeCRLFCharacters(workerId) + " Unnable to reach worker.", e);
            } else {
                // if e include large log is pringting continously.
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
            String workerGeneralCArbonId = null;
            workerGeneralCArbonId = dashboardStore.selectWorkerCarbonID(workerId);
            if (workerGeneralCArbonId != null) {
                workerIDCarbonIDMap.put(workerId, workerGeneralCArbonId);
                return workerGeneralCArbonId;
            } else {
                String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
                String responce = getWorkerGeneralDetails(generateURLHostPort(hostPort[0], hostPort[1]), workerId);
                if (!responce.contains("Unnable to reach worker.")) {
                    WorkerGeneralDetails workerGeneralDetails = gson.fromJson(responce, WorkerGeneralDetails.class);
                    workerGeneralDetails.setWorkerId(workerId);
                    dashboardStore.insertWorkerGeneralDetails(workerGeneralDetails);
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

            String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String carbonId = workerIDCarbonIDMap.get(workerId);
                if (carbonId == null) {
                    carbonId = getCarbonID(workerId);
                }
                Map<String, List<String>> components = metricStore.selectAppComponentsList(carbonId, appName,
                        Constants
                                .DEFAULT_TIME_INTERVAL_MILLIS,
                        System.currentTimeMillis());
                List componentsRecentMetrics = metricStore.selectComponentsLastMetric
                        (carbonId, appName, components, Constants.DEFAULT_TIME_INTERVAL_MILLIS,
                                System.currentTimeMillis());
                String json = gson.toJson(componentsRecentMetrics);
                return Response.ok().entity(json).build();
            } else {
                logger.error("Inproper format of worker ID:" + workerId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of worker ID:" + workerId)
                        .build();
            }
        } else {
            logger.error("Unauthorized to perform get siddhi app component for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    @Override
    public Response getRolesByUsername(String username, String permissionSuffix) {

        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                Constants.PERMISSION_APP_NAME + "." + permissionSuffix));
        if (isAuthorized) {
            return Response.ok().entity(isAuthorized).build();
        } else {
            return Response.ok().entity(isAuthorized).build();
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
            try {
                dashboardStore.deleteWorkerGeneralDetails(id);
                boolean result = dashboardStore.deleteWorkerConfiguration(id);
                if (result) {
                    workerIDCarbonIDMap.remove(id);
                }
                return Response.status(Response.Status.OK).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                        "Worker is deleted "
                                + "successfully"))
                        .build();
            } catch (RDBMSTableException e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Error while deleting the " +
                                "worker " + e.getMessage())).build();
            }
        } else {
            logger.error("Unauthorized to perform delete worker for user : " + username);
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
                            getUsername(),
                            getPassword())
                            .enableAppStatistics(appName, statEnable);
                    if (workerResponse.status() == 200) {
                        return Response.ok().entity(workerResponse.body().toString()).build();
                    } else if (workerResponse.status() == 401) {
                        String jsonString = new Gson().toJson(workerResponse.body());
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
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
            } else {
                logger.error("Inproper format of worker ID:" + workerId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of worker ID:" + workerId)
                        .build();
            }
        } else {
            logger.error("Unauthorized to perform enable siddhi app statistics for user : " + username);
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
                            getUsername(), getPassword()).getWorker();
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
            } else {
                logger.error("Inproper format of worker ID:" + workerId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of worker ID:" + workerId)
                        .build();
            }
            String jsonString = new Gson().toJson(serverHADetails);
            if (status == 200) {
                return Response.ok().entity(jsonString).build();
            } else if (status == 401) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
            }
        } else {
            logger.error("Unauthorized to perform get HA detatils for user : " + username);
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
            long timeInterval = period != null ? parsePeriod(period) : Constants.DEFAULT_TIME_INTERVAL_MILLIS;
            Map<String, List<List<Object>>> componentHistory = new HashMap<>();
            // || ("Microsoft SQL Server").equalsIgnoreCase(dbType)
            if ((timeInterval <= HOUR)) {
                switch (componentType.toLowerCase()) {
                    case "streams": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "trigger": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "storequeries": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "queries": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "memory";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "tables": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "memory";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sources": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sinks": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sourcemappers": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sinkmappers": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                }
            } else {
                switch (componentType.toLowerCase()) {
                    case "streams": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "trigger": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "storequeries": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "queries": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "memory";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "tables": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval,
                                System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "memory";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sources": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sinks": {
                        String metricsType = "throughput";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sourcemappers": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                    case "sinkmappers": {
                        String metricsType = "latency";
                        componentHistory.put(metricsType, metricStore.selectAppComponentsAggHistory(carbonId, appName,
                                timeInterval, System.currentTimeMillis(), metricsType, componentType, componentId));
                        break;
                    }
                }
            }
            String json = gson.toJson(componentHistory);
            return Response.ok().entity(json).build();
        } else {
            logger.error("Unauthorized to perform get component history for user : " + username);
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
            NodeConfigurationDetails workerConfig = dashboardStore.selectWorkerConfigurationDetails(id);
            Node worker = new Node();
            if (workerConfig != null) {
                worker.setHost(workerConfig.getHost());
                worker.setPort(workerConfig.getPort());
            }
            String jsonString = new Gson().toJson(worker);
            return Response.ok().entity(jsonString).build();
        } else {
            logger.error("Unauthorized to perform get worker configurations for user : " + username);
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
                MANAGER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = workerId.split(Constants.WORKER_KEY_GENERATOR);
            int status = 404;
            if (hostPort.length == 2) {
                WorkerResponse workerResponce = new WorkerResponse();
                String uri = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL + uri,
                            getUsername(), getPassword()).getWorker();
                    status = workerResponse.status();
                    if (status == 200) {
                        workerResponce.setCode(200);
                        workerResponce.setMessage("Sucessfully reached the worker : " + workerId);
                    } else if (status == 404) {
                        workerResponce.setCode(404);
                        workerResponce.setMessage("Cannot reach the worker. Worker : " + workerId + " is not " +
                                "reachable");
                    } else if (status == 401) {
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
                logger.error("Inproper format of worker ID:" + workerId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of worker ID:" + workerId)
                        .build();
            }
        } else {
            logger.error("Unauthorized to perform test connection for user : " + username);
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
            logger.error("Unauthorized  to perform get dashboard configurations for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Rest API's Related to Distributed View.
     */

    /**
     * Add a new worker.
     *
     * @param worker Worker object that's needed to be added.
     * @return Response whether the worker is sucessfully added or not.
     * @throws NotFoundException
     */
    @Override
    public Response addWorker(Node worker, String username) throws NotFoundException {

        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                MANAGER_PERMISSION_STRING));
        if (isAuthorized) {
            if (worker.getHost() != null) {
                String workerID = generateWorkerKey(worker.getHost(), String.valueOf(worker.getPort()));
                NodeConfigurationDetails workerConfigData = new NodeConfigurationDetails(workerID, worker.getHost(),
                        Integer.valueOf(worker.getPort()));
                StatusDashboardDBHandler workerDBHandler = dashboardStore;
                try {
                    workerDBHandler.insertWorkerConfiguration(workerConfigData);
                } catch (RDBMSTableException e) {
                    logger.error("Error occured while inserting the Worker due to " + e.getMessage(), e);
                    return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                            "Error occured while inserting the Worker due to " + e.getMessage())).build();
                }
                //This part to be sucess is optional at this level
                String response = getWorkerGeneralDetails(generateURLHostPort(worker.getHost(),
                        String.valueOf(worker.getPort())), workerID);
                if (!response.contains("Unnable to reach worker.")) {
                    WorkerGeneralDetails workerGeneralDetails = gson.fromJson(response, WorkerGeneralDetails.class);
                    workerGeneralDetails.setWorkerId(workerID);
                    try {
                        workerDBHandler.insertWorkerGeneralDetails(workerGeneralDetails);
                    } catch (RDBMSTableException e) {
                        logger.warn("Worker " + removeCRLFCharacters(workerID) +
                                " currently not active. Retry to reach " + "later", e);
                    }
                    workerIDCarbonIDMap.put(workerID, workerGeneralDetails.getCarbonId());
                    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Worker id: "
                            + workerID + "sucessfully added.")).build();
                } else if (response.contains("Unnable to reach worker.")) {
                    //shold able to add a worker so the responce is ok
                    return Response.status(Response.Status.OK).entity(new ApiResponseMessage
                            (ApiResponseMessage.OK, "Worker id: " + workerID + "sucessfully added. But "
                                    + "worker not reachable.")).build();
                } else {
                    return Response.status(Response.Status.OK).entity(new ApiResponseMessage
                            (ApiResponseMessage.OK, "Worker id: "
                                    + workerID + ("sucessfully added. But unknown error has occured while "
                                    + "trying to reach worker"))).build();
                }
            } else {
                logger.error("Invalid data :" + worker.toString());
                return Response.status(Response.Status.BAD_REQUEST).entity("Invalid data :" + worker.toString())
                        .build();
            }
        } else {
            logger.error("Unauthorized to perform add worker for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Add a new manager.
     *
     * @param manager Manager object that's needed to be added.
     * @return Response whether the worker is sucessfully added or not.
     * @throws NotFoundException
     */
    @Override
    public Response addManager(Node manager, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                MANAGER_PERMISSION_STRING));
        if (isAuthorized) {
            if (manager.getHost() != null && manager.getPort() != 0) {
                String managerId = manager.getHost() + Constants.WORKER_KEY_GENERATOR
                        + String.valueOf(manager.getPort());
                NodeConfigurationDetails managerConfigurationDetails =
                        new NodeConfigurationDetails(managerId, manager.getHost(), Integer.valueOf(manager.getPort()));
                try {
                    dashboardStore.insertManagerConfiguration(managerConfigurationDetails);

                    return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK,
                            "managerId " + "\n" + managerId + "\n" + "successfully " + " added")).build();
                } catch (RDBMSTableException e) {
                    logger.error("Error occurred while inserting the Manager due to " + e.getMessage(), e);
                    return Response.serverError().entity(new ApiResponseMessage(ApiResponseMessage.ERROR, "Error "
                            + "occured while inserting the Manager due to " + e.getMessage())).build();
                }
            } else {
                logger.error("There is no manager node specified:" + manager.toString());
                return Response.status(Response.Status.BAD_REQUEST).entity("There is no manager node specified. " +
                        "Please add vaild host name and port" + manager.toString()).build();
            }
        } else {
            logger.error("Unauthorized to perform add manager for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Delete an existing manager Node.
     *
     * @param id worker Id
     * @return Response whether the worker is sucessfully deleted or not.
     * @throws NotFoundException
     */
    @Override
    public Response deleteManager(String id, String username) throws NotFoundException {

        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                MANAGER_PERMISSION_STRING));
        if (isAuthorized) {
            try {
                dashboardStore.deleteManagerConfiguration(id);
                return Response.ok().entity(
                        new ApiResponseMessage(ApiResponseMessage.OK, id + " Successfully deleted")).build();
            } catch (RDBMSTableException ex) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                        new ApiResponseMessage(ApiResponseMessage.ERROR, "Error occured while deleting the "
                                + "manager" + "\n" + id + "\n" + ex.getMessage())).build();
            }
        } else {
            logger.error("Unauthorized to perform delete worker for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Return all realtime statistics of the managers.If manager is not currently reachable then send the last
     * persistant state of that manager.
     *
     * @return Realtime data and status of workers.
     * @throws NotFoundException
     */

    @Override
    public Response getManagers(String username) throws NotFoundException, SQLException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            Map<String, List<ManagerOverView>> groupedManagers = new HashMap<>();
            List<NodeConfigurationDetails> managerList = dashboardStore.getAllManagerConfigDetails();
            if (!managerList.isEmpty()) {
                logger.info(managerList.toString());
                managerList.parallelStream().forEach(manager -> {
                    try {
                        ManagerOverView managerOverView = new ManagerOverView();
                        feign.Response managerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                                generateURLHostPort(manager.getHost(), String.valueOf(
                                        manager.getPort())), getUsername(), getPassword()).getWorker();
                        if ((managerResponse != null) && (managerResponse.status() == 200)) {
                            Long timeInMillis = System.currentTimeMillis();
                            String responseBody = managerResponse.body().toString();
                            ManagerDetails serverDetails = gson.fromJson(responseBody, ManagerDetails.class);
                            String message = serverDetails.getMessage();
                            if (message == null || message.isEmpty()) {
                                managerOverView.setStatusMessage("Success");
                            } else {
                                managerOverView.setStatusMessage(message);
                            }
                            feign.Response activeSiddiAppsResponse = WorkerServiceFactory.getWorkerHttpsClient(
                                    PROTOCOL + generateURLHostPort(manager.getHost(), String.valueOf(manager
                                            .getPort())), getUsername(), getPassword()).getSiddhiApps(true);
                            String activeSiddiAppsResponseBody = activeSiddiAppsResponse.body().toString();
                            List<String> activeApps = gson.fromJson(activeSiddiAppsResponseBody,
                                    new TypeToken<List<String>>() {

                                    }.getType());
                            feign.Response inactiveSiddiAppsResponse = WorkerServiceFactory.getWorkerHttpsClient(
                                    PROTOCOL + generateURLHostPort(manager.getHost(), String.valueOf(manager
                                            .getPort())), getUsername(), getPassword()).getSiddhiApps(false);
                            String inactiveSiddiAppsResponseBody = inactiveSiddiAppsResponse.body().toString();
                            List<String> inactiveApps = gson.fromJson(inactiveSiddiAppsResponseBody,
                                    new TypeToken<List<String>>() {

                                    }.getType());
                            serverDetails.setSiddhiApps(activeApps.size(), inactiveApps.size());
                            ManagerMetricsSnapshot snapshot = new ManagerMetricsSnapshot(serverDetails, timeInMillis);
                            WorkerStateHolder.addManagerMetrics(manager.getWorkerId(), snapshot);
                            managerOverView.setLastUpdate(timeInMillis);
                            managerOverView.setWorkerId(manager.getWorkerId());

                            feign.Response haDetails = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                                            generateURLHostPort(manager.getHost(), String.valueOf(manager.getPort())),
                                    getUsername(), getPassword()).getManagerDetails();
                            String haResponseBody = haDetails.body().toString();
                            ManagerClusterInfo clusterInfo = gson.fromJson(haResponseBody, ManagerClusterInfo.class);
                            managerOverView.setServerDetails(serverDetails);
                            managerOverView.setClusterInfo(clusterInfo);

                            //grouping the clusters of the managers
                            List nonClusterList = groupedManagers.get(Constants.NON_CLUSTERS_ID);
                            String clusterID = clusterInfo.getGroupId();
                            List existingGroupList = groupedManagers.get(clusterID);
                            if (nonClusterList == null) {
                                nonClusterList = new ArrayList<>();
                            }
                            if (existingGroupList == null) {
                                existingGroupList = new ArrayList<>();
                            }
                            if (clusterInfo.getGroupId().equals(" ") || clusterID.equals(" ")) {
                                nonClusterList.add(managerOverView);
                                groupedManagers.put(Constants.NON_CLUSTERS_ID, nonClusterList);
                            } else if ((!clusterID.equals(" "))) {
                                existingGroupList.add(managerOverView);
                                groupedManagers.put(clusterID, existingGroupList);
                            }
                        } else {
                            managerOverView.setWorkerId(manager.getWorkerId());
                            ManagerDetails serverDetails = new ManagerDetails();
                            ManagerClusterInfo clusterInfo = new ManagerClusterInfo();
                            serverDetails.setRunningStatus(Constants.NOT_REACHABLE_ID);
                            managerOverView.setStatusMessage(getErrorMessage(managerResponse.status()));
                            managerOverView.setServerDetails(serverDetails);
                            managerOverView.setClusterInfo(clusterInfo);
                            managerOverView.setLastUpdate((long) 0);
                            //grouping the never reached
                            if (groupedManagers.get(Constants.NEVER_REACHED) == null) {
                                List<ManagerOverView> managers = new ArrayList<>();
                                managers.add(managerOverView);
                                groupedManagers.put(Constants.NEVER_REACHED, managers);
                            } else {
                                List existing = groupedManagers.get(Constants.NEVER_REACHED);
                                existing.add(managerOverView);
                            }
                        }
                    } catch (feign.RetryableException e) {
                        ManagerMetricsSnapshot lastSnapshot = WorkerStateHolder.getManagerMetrics(
                                manager.getWorkerId());
                        if (lastSnapshot != null) {
                            lastSnapshot.updateRunningStatus(Constants.NOT_REACHABLE_ID);
                            ManagerOverView managerOverView = new ManagerOverView();
                            managerOverView.setLastUpdate(lastSnapshot.getTimeStamp());
                            managerOverView.setWorkerId(manager.getWorkerId());
                            managerOverView.setServerDetails(lastSnapshot.getServerDetails());
                            managerOverView.setClusterInfo(lastSnapshot.getClusterInfo());
                            if (groupedManagers.get(lastSnapshot.getClusterInfo().getGroupId()) != null) {
                                groupedManagers.get(lastSnapshot.getClusterInfo().getGroupId()).add(managerOverView);
                            } else {
                                List<ManagerOverView> managers = new ArrayList<>();
                                managers.add(managerOverView);
                                groupedManagers.put(Constants.NOT_REACHABLE_ID, managers);
                            }
                        } else {
                            ManagerOverView managerOverView = new ManagerOverView();
                            managerOverView.setWorkerId(manager.getWorkerId());
                            ManagerDetails serverDetails = new ManagerDetails();
                            ManagerClusterInfo clusterInfo = new ManagerClusterInfo();
                            serverDetails.setRunningStatus(Constants.NEVER_REACHED);
                            managerOverView.setServerDetails(serverDetails);
                            managerOverView.setClusterInfo(clusterInfo);
                            managerOverView.setLastUpdate((long) 0);
                            //grouping the never reached
                            if (groupedManagers.get(Constants.NEVER_REACHED) == null) {
                                List<ManagerOverView> managers = new ArrayList<>();
                                managers.add(managerOverView);
                                groupedManagers.put(Constants.NEVER_REACHED, managers);
                            } else {
                                List existing = groupedManagers.get(Constants.NEVER_REACHED);
                                existing.add(managerOverView);
                            }
                        }
                    }
                });
            }
            String jsonString = new Gson().toJson(groupedManagers);
            return Response.ok().entity(jsonString).build();
        } else {
            logger.error("Unauthorized for user : " + username);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Returns HA Details of manager nodes. whether active or pasive
     *
     * @param managerId : Id of the manager node
     * @param username
     * @return
     * @throws NotFoundException
     */
    @Override
    public Response getManagerHADetails(String managerId, String username) throws NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = managerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String managerURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response managerResponse = WorkerServiceFactory.getWorkerHttpsClient
                            (PROTOCOL + managerURIBody, this.getUsername(), this.getPassword()).getManagerDetails();
                    String responseAppBody = managerResponse.toString();
                    if (managerResponse.status() == 200) {
                        logger.info(managerResponse.body().toString());
                        return Response.ok().entity(managerResponse.body().toString()).build();
                    } else if (managerResponse.status() == 401) {
                        String jsonString = new Gson().toJson(responseAppBody);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity(responseAppBody).build();
                    }
                } catch (feign.RetryableException e) {
                    String jsonString = new Gson()
                            .toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.SERVER_CONNECTION_ERROR,
                                    e.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                }
            } else {
                logger.error("Inproper format of manager ID" + managerId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of managerId " + managerId)
                        .build();
            }
        } else {
            logger.error("Unauthorized to perform get siddhi app details for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Reurns the text view of the parent siddhi application
     *
     * @param managerId :  Id of the manager node
     * @param appName   : parent siddhi app name
     * @param username
     * @return
     * @throws NotFoundException
     */
    @Override
    public Response getManagerSiddhiAppTextView(String managerId, String appName, String username) throws
            NotFoundException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = managerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String managerURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response managerResponse = WorkerServiceFactory.getWorkerHttpsClient
                            (PROTOCOL + managerURIBody, this.getUsername(), this.getPassword())
                            .getManagerSiddhiAppTextView(appName);
                    String responseBody = managerResponse.body().toString();
                    String appJson = new Gson().toJson(responseBody);
                    if (managerResponse.status() == 200) {
                        return Response.ok().entity(appJson).build();
                    } else if (managerResponse.status() == 401) {
                        String jsonString = new Gson().toJson(responseBody);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity(responseBody).build();
                    }
                } catch (feign.RetryableException ex) {
                    String jsonString = new Gson().toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode
                            .SERVER_CONNECTION_ERROR,
                            ex.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                }
            } else {
                logger.error("Inproper format of manager ID" + managerId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Inproper format of managerId " + managerId)
                        .build();
            }
        } else {
            logger.error("Unauthorized to perform get siddhi app details for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /***
     * Return the run time environment of the node
     * @param username
     * @return the runtime environmet
     * @throws NotFoundException
     */

    @Override
    public Response getRuntimeEnv(String managerId, String username) throws NotFoundException {

        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = managerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String workerURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response siddhiAppResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                            workerURIBody, this.getUsername(), this.getPassword()).getRunTime();
                    String responseAppBody = siddhiAppResponse.toString();
                    if (siddhiAppResponse.status() == 200) {
                        //logger.info(siddhiAppResponce.body().toString());
                        return Response.ok().entity(siddhiAppResponse.body().toString()).build();
                    } else if (siddhiAppResponse.status() == 401) {
                        String jsonString = new Gson().toJson(responseAppBody);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity(responseAppBody).build();
                    }
                } catch (feign.RetryableException e) {
                    String jsonString = new Gson().toJson(Constants.NOT_REACHABLE_ID);
                    return Response.ok().entity(jsonString).build();
                }
            }
            logger.error("Inproper format of worker ID:" + managerId);
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Inproper format of worker ID:" + managerId).build();
        } else {
            logger.error("Unauthorized to perform get siddhi app details for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Returns the summary details of deployed parent siddhi application.
     *
     * @param managerId : Id of the manager node
     * @param username
     * @return
     * @throws NotFoundException
     * @throws IOException
     */
    @Override
    public Response getSiddhiApps(String managerId, String username) throws NotFoundException, IOException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = managerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String managerURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response managerResponse = WorkerServiceFactory.getWorkerHttpsClient
                            (PROTOCOL + managerURIBody, this.getUsername(), this.getPassword()).getSiddhiApps();
                    if (managerResponse.status() == 200) {
                        Reader reader = managerResponse.body().asReader();
                        List<ParentSiddhiApp> totalApps = gson.fromJson(reader,
                                new TypeToken<List<ParentSiddhiApp>>() {
                                }.getType());
                        if (!totalApps.isEmpty()) {
                            Map<String, ParentSummaryDetails> appSummary = new HashMap<>();

                            for (ParentSiddhiApp siddhiapp : totalApps) {
                                String parentAppName = siddhiapp.getParentAppName();
                                if (!(appSummary.containsKey(parentAppName))) {
                                    appSummary.put(siddhiapp.getParentAppName(), new ParentSummaryDetails());
                                }

                                ParentSummaryDetails existingParentAppName = appSummary.get(parentAppName);
                                if (existingParentAppName.getGroups() != null) {
                                    if (!(existingParentAppName.getGroups().contains(siddhiapp.getGroupName()))) {
                                        existingParentAppName.getGroups().add(siddhiapp.getGroupName());
                                    }
                                }

                                int numberOfChildApp = existingParentAppName.getChildApps() + 1;
                                existingParentAppName.setChildApps(numberOfChildApp);
                                if (siddhiapp.getId() != null) {
                                    if (!existingParentAppName.getUsedWorkerNode().contains(siddhiapp.getId())) {
                                        existingParentAppName.getUsedWorkerNode().add(siddhiapp.getId());
                                    }
                                }
                                if (!existingParentAppName.getUsedWorkerNode().contains(siddhiapp.getId())) {
                                    existingParentAppName.getUnDeployedChildApps().add(siddhiapp.getId());
                                } else {
                                    existingParentAppName.getDeployedChildApps().add(siddhiapp.getId());
                                }
                            }
                            feign.Response resourceClusterWorkerDetails = WorkerServiceFactory.getWorkerHttpsClient
                                    (PROTOCOL + managerURIBody, this.getUsername(), this.getPassword())
                                    .getResourceClusterWorkers();
                            String resourceClusterResponseBody = resourceClusterWorkerDetails.body().toString();
                            List<Map<String, String>> parentAppSummary = new ArrayList<>();
                            for (Map.Entry<String, ParentSummaryDetails> entry : appSummary.entrySet()) {
                                Map<String, String> parentAppDetail = new HashMap<>();
                                parentAppDetail.put("parentAppName", entry.getKey());
                                parentAppDetail.put("managerId", managerId);
                                parentAppDetail
                                        .put("numberOfGroups", Integer.toString(entry.getValue().getGroups().size()));
                                parentAppDetail
                                        .put("numberOfChildApp", Integer.toString(entry.getValue().getChildApps()));
                                parentAppDetail.put("usedWorkerNodes",
                                        Integer.toString(entry.getValue().getUsedWorkerNode().size()));
                                parentAppDetail.put("deployedChildApps", Integer.toString(entry.getValue()
                                        .getDeployedChildApps().size()));
                                parentAppDetail.put("notDeployedChildApps", Integer.toString(entry.getValue()
                                        .getUnDeployedChildApps().size()));
                                parentAppDetail.put("totalWorkerNodes", resourceClusterResponseBody);
                                parentAppSummary.add(parentAppDetail);
                            }
                            return Response.ok().entity(parentAppSummary).build();
                        } else {
                            String jsonErrorMessage = new Gson().toJson("There is no siddhi app deployed in the "
                                    + "manager node");
                            return Response.ok().entity(jsonErrorMessage).build();
                        }
                    } else if (managerResponse.status() == 401) {
                        String jsonString = new Gson().toJson(managerResponse.body().toString());
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
                    } else if (managerResponse.status() == 204) {
                        return Response.status(Response.Status.NO_CONTENT).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                } catch (feign.RetryableException e) {
                    String jsonString = new Gson().toJson(new ApiResponseMessageWithCode(
                            ApiResponseMessageWithCode.SERVER_CONNECTION_ERROR, e.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
                }
            } else {
                logger.error("Improper format of manager ID" + managerId);
                return Response.status(Response.Status.BAD_REQUEST).entity("Improper format of managerId " + managerId)
                        .build();
            }
        } else {
            logger.error("Unauthorized to perform get siddhi app details for user : " + username);
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized for user : " + username).build();
        }
    }


    /**
     * Returns kafka topic details of each child apps
     *
     * @param managerId : id of the manager Node
     * @param appName   : parent siddhi application
     * @param username
     * @throws IOException
     */

    @Override
    public Response getChildAppsTransportDetails(String managerId, String appName, String username) throws IOException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants
                .PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = managerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String managerURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response managerResponse = WorkerServiceFactory.getWorkerHttpsClient
                            (PROTOCOL + managerURIBody, this.getUsername(), this.getPassword()).getKafkaDetails
                            (appName);
                    String responseBody = managerResponse.body().toString();
                    if (managerResponse.status() == 200) {
                        InputStream reader = managerResponse.body().asInputStream();
                        return Response.ok().entity(reader).build();
                    } else if (managerResponse.status() == 401) {
                        String jsonString = new Gson().toJson(responseBody);
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity(responseBody).build();
                    }
                } catch (feign.RetryableException ex) {
                    String errString = new Gson().toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode
                            .SERVER_CONNECTION_ERROR, ex.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errString).build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("In proper format of managerId "
                        + "" + managerId).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized user : " + username).build();
        }
    }

    /**
     * Returns all the single deployment siddhi apps
     *
     * @param username
     * @return
     * @throws NotFoundException
     * @throws SQLException
     */
    @Override
    public Response getSingleDeploymentSiddhiApps(String username) throws NotFoundException, SQLException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(
                Constants.PERMISSION_APP_NAME, VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            List<NodeConfigurationDetails> registeredWorkers = dashboardStore.selectAllWorkers();
            List<NodeConfigurationDetails> registeredManagers = dashboardStore.getAllManagerConfigDetails();
            List<SiddhiAppSummaryInfo> siddhiAppSummaryInfos = new ArrayList<>();

            List<String> ResourceClusteredWorkerNode = new ArrayList<>();
            if (!registeredManagers.isEmpty()) {
                registeredManagers.parallelStream().forEach(manager -> {
                    try {
                        feign.Response resourceResponse = WorkerServiceFactory.getWorkerHttpsClient(
                                PROTOCOL + generateURLHostPort(manager.getHost(), String.valueOf(manager
                                        .getPort())), this.getUsername(), this.getPassword()).getClusterNodeDetails();
                        if (resourceResponse.status() == 200) {
                            Reader inputStream = resourceResponse.body().asReader();
                            List<ResourceClusterInfo> clusterInfos = gson.fromJson(
                                    inputStream, new TypeToken<List<ResourceClusterInfo>>() {
                                    }.getType());
                            for (ResourceClusterInfo clusterInfo : clusterInfos) {
                                String nodeId = clusterInfo.getNodeId();
                                ResourceClusteredWorkerNode.add(nodeId);
                            }
                        }
                    } catch (feign.RetryableException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(removeCRLFCharacters(manager.getWorkerId()) + " Unnable to reach manager.", e);
                        }
                        logger.warn(removeCRLFCharacters(manager.getWorkerId()) + " Unnable to reach manager.");

                    } catch (IOException e) {
                        logger.warn("Error occured while getting the response " + e.getMessage());
                    }
                });
            }
            if (!registeredWorkers.isEmpty()) {
                registeredWorkers.parallelStream().forEach(worker -> {
                    ServerHADetails serverHADetails = new ServerHADetails();
                    if (!ResourceClusteredWorkerNode.contains(getCarbonID(worker.getWorkerId()))) {
                        try {
                            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                                    generateURLHostPort(worker.getHost(), String.valueOf(
                                            worker.getPort())), getUsername(), getPassword()).getWorker();
                            String responseBody = workerResponse.body().toString();
                            serverHADetails = gson.fromJson(responseBody, ServerHADetails.class);
                            if (serverHADetails.getClusterId().equals(Constants.NON_CLUSTERS_ID)) {
                                feign.Response registeredWorkerSiddhiAppsResponse = WorkerServiceFactory
                                        .getWorkerHttpsClient

                                                (PROTOCOL + generateURLHostPort(worker.getHost(), String.valueOf(
                                                        worker.getPort())), this.getUsername(), this.getPassword())
                                        .getAllAppDetails();
                                if (registeredWorkerSiddhiAppsResponse.status() == 200) {
                                    Reader inputReader = registeredWorkerSiddhiAppsResponse.body().asReader();
                                    List<SiddhiAppStatus> totalApps = gson.fromJson(inputReader, new
                                            TypeToken<List<SiddhiAppStatus>>() {
                                            }.getType());

                                    for (SiddhiAppStatus siddhiapp : totalApps) {
                                        SiddhiAppSummaryInfo siddhiAppSummaryInfo = new SiddhiAppSummaryInfo();
                                        siddhiAppSummaryInfo.setAppName(siddhiapp.getAppName());
                                        siddhiAppSummaryInfo.setStatus(siddhiapp.getStatus());
                                        siddhiAppSummaryInfo.setLastUpdate(siddhiapp.getTimeAgo());
                                        siddhiAppSummaryInfo.setStatEnabled(siddhiapp.isStatEnabled());
                                        siddhiAppSummaryInfo.setDeployedNodeType("Worker");
                                        siddhiAppSummaryInfo.setDeployedNodeHost(worker.getHost());
                                        siddhiAppSummaryInfo.setDeployedNodePort(String.valueOf(worker.getPort()));
                                        siddhiAppSummaryInfos.add(siddhiAppSummaryInfo);
                                    }
                                }
                            }
                        } catch (feign.RetryableException ex) {
                            logger.error("Error ocurred while connecting the node " + worker.getWorkerId());
                        } catch (IOException e) {
                            logger.error("error occurred while retrieving response ", e);
                        }
                    }
                });
            }
            return Response.ok().entity(siddhiAppSummaryInfos).build();
        } else {
            logger.error("Unauthorized for user : " + username);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Returns all the HA Siddhi app details
     *
     * @param username
     * @return
     * @throws NotFoundException
     * @throws SQLException
     */
    @Override
    public Response getHASiddhiApps(String username) throws NotFoundException, SQLException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(
                Constants.PERMISSION_APP_NAME, VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            List<NodeConfigurationDetails> registeredWorkers = dashboardStore.selectAllWorkers();
            List<NodeConfigurationDetails> registeredManagers = dashboardStore.getAllManagerConfigDetails();
            List<SiddhiAppSummaryInfo> siddhiAppSummaryInfos = new ArrayList<>();
            List<String> ResourceClusteredWorkerNode = new ArrayList<>();
            if (!registeredManagers.isEmpty()) {
                registeredManagers.parallelStream().forEach(manager -> {
                    try {
                        feign.Response resourceResponse = WorkerServiceFactory.getWorkerHttpsClient(
                                PROTOCOL + generateURLHostPort(manager.getHost(), String.valueOf(manager
                                        .getPort())), this.getUsername(), this.getPassword()).getClusterNodeDetails();
                        if (resourceResponse.status() == 200) {
                            Reader inputStream = resourceResponse.body().asReader();
                            List<ResourceClusterInfo> clusterInfos = gson.fromJson(
                                    inputStream, new TypeToken<List<ResourceClusterInfo>>() {
                                    }.getType());
                            for (ResourceClusterInfo clusterInfo : clusterInfos) {
                                String nodeId = clusterInfo.getNodeId();
                                ResourceClusteredWorkerNode.add(nodeId);
                            }
                        }
                    } catch (feign.RetryableException e) {
                        if (logger.isDebugEnabled()) {
                            logger.debug(removeCRLFCharacters(manager.getWorkerId()) + " Unnable to reach manager.", e);
                        }
                        logger.warn(removeCRLFCharacters(manager.getWorkerId()) + " Unnable to reach manager.");

                    } catch (IOException e) {
                        logger.warn("Error occured while getting the response " + e.getMessage());
                    }
                });
            }

            if (!registeredWorkers.isEmpty()) {
                registeredWorkers.parallelStream().forEach(worker -> {
                    ServerHADetails serverHADetails = new ServerHADetails();
                    if (!ResourceClusteredWorkerNode.contains(getCarbonID(worker.getWorkerId()))) {
                        try {
                            feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(PROTOCOL +
                                    generateURLHostPort(worker.getHost(), String.valueOf(
                                            worker.getPort())), getUsername(), getPassword()).getWorker();
                            String responseBody = workerResponse.body().toString();
                            serverHADetails = gson.fromJson(responseBody, ServerHADetails.class);
                            if (!serverHADetails.getClusterId().equals(Constants.NON_CLUSTERS_ID) && serverHADetails
                                    .getHAStatus().equalsIgnoreCase(Constants.ACTIVE_APP_STATUS)) {
                                feign.Response registeredWorkerSiddhiAppsResponse = WorkerServiceFactory
                                        .getWorkerHttpsClient

                                                (PROTOCOL + generateURLHostPort(worker.getHost(), String.valueOf(
                                                        worker.getPort())), this.getUsername(), this.getPassword())
                                        .getAllAppDetails();
                                if (registeredWorkerSiddhiAppsResponse.status() == 200) {
                                    Reader inputReader = registeredWorkerSiddhiAppsResponse.body().asReader();
                                    List<SiddhiAppStatus> totalApps = gson.fromJson(inputReader, new
                                            TypeToken<List<SiddhiAppStatus>>() {
                                            }.getType());

                                    for (SiddhiAppStatus siddhiapp : totalApps) {
                                        SiddhiAppSummaryInfo siddhiAppSummaryInfo = new SiddhiAppSummaryInfo();
                                        siddhiAppSummaryInfo.setAppName(siddhiapp.getAppName());
                                        siddhiAppSummaryInfo.setStatus(siddhiapp.getStatus());
                                        siddhiAppSummaryInfo.setLastUpdate(siddhiapp.getTimeAgo());
                                        siddhiAppSummaryInfo.setStatEnabled(siddhiapp.isStatEnabled());
                                        siddhiAppSummaryInfo.setDeployedNodeType("Worker");
                                        siddhiAppSummaryInfo.setDeployedNodeHost(worker.getHost());
                                        siddhiAppSummaryInfo.setDeployedNodePort(String.valueOf(worker.getPort()));
                                        siddhiAppSummaryInfos.add(siddhiAppSummaryInfo);
                                    }
                                }
                            }
                        } catch (feign.RetryableException ex) {
                            logger.error("Error ocurred while connecting the node " + worker.getWorkerId());
                        } catch (IOException e) {
                            logger.error("error occurred while retrieving response ", e);
                        }
                    }
                });
            }
            return Response.ok().entity(siddhiAppSummaryInfos).build();
        } else {
            logger.error("Unauthorized for user : " + username);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Unauthorized for user : " + username).build();
        }
    }

    /**
     * Return all the siddhi apps that are deployed in the active manager nodes
     *
     * @param username
     * @return
     * @throws NotFoundException
     * @throws SQLException
     */

    @Override
    public Response getAllManagersSiddhiApps(String username) throws NotFoundException, SQLException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(
                Constants.PERMISSION_APP_NAME, VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            List<NodeConfigurationDetails> registeredManagers = dashboardStore.getAllManagerConfigDetails();
            List<SiddhiAppSummaryInfo> managerInfos = new ArrayList<>();
            if (!registeredManagers.isEmpty()) {
                registeredManagers.parallelStream().forEach(manager -> {
                    try {
                        Response registeredManagerSiddhiAppResponse = getSiddhiApps(manager.getWorkerId(), username);
                        if (registeredManagerSiddhiAppResponse.getStatus() == 200) {
                            List<ManagerSiddhiApps> totalApps = gson.fromJson(String.valueOf
                                    (registeredManagerSiddhiAppResponse
                                            .getEntity()), new TypeToken<List<ManagerSiddhiApps>>() {

                            }.getType());
                            if (!totalApps.isEmpty()) {
                                for (ManagerSiddhiApps managerSiddhiApps : totalApps) {
                                    SiddhiAppSummaryInfo siddhiAppSummaryInfo = new SiddhiAppSummaryInfo();
                                    siddhiAppSummaryInfo.setAppName(managerSiddhiApps.getParentAppName());
                                    siddhiAppSummaryInfo.setDeployedNodeHost(manager.getHost());
                                    siddhiAppSummaryInfo.setDeployedNodePort(String.valueOf(manager.getPort()));
                                    siddhiAppSummaryInfo.setDeployedNodeType("Manager");
                                    siddhiAppSummaryInfo.setLastUpdate("N/A");
                                    if (!managerSiddhiApps.getUsedWorkerNodes().equals("0")) {
                                        siddhiAppSummaryInfo.setStatus(Constants.ACTIVE_APP_STATUS);
                                    } else {
                                        siddhiAppSummaryInfo.setStatus(Constants.PASSIVE_APP_STATUS);
                                    }
                                    managerInfos.add(siddhiAppSummaryInfo);
                                }
                            }
                        }
                    } catch (IOException e) {
                        logger.error("error occured while retrieving response", e);
                    } catch (NotFoundException e) {
                        logger.error("Requested response is not found ", e);
                    }
                });
            }
            return Response.ok().entity(managerInfos).build();
        } else {
            logger.error("Unauthorized for user : " + username);
            return Response.status(Response.Status.FORBIDDEN)
                    .entity("Unauthorized for user : " + username).build();
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

    /**
     * Returns the child siddhi application details of the specific parent siddhi application
     *
     * @param managerId : Id of the manager node
     * @param appName   : Parent siddhi application name
     * @param username
     * @return child app details
     * @throws NotFoundException
     * @throws IOException
     */
    @Override
    public Response getChildAppsDetails(String managerId, String appName, String username)
            throws NotFoundException, IOException {
        boolean isAuthorized = permissionProvider.hasPermission(username, new Permission(Constants.PERMISSION_APP_NAME,
                VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = managerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String managerURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response managerResponse = WorkerServiceFactory.getWorkerHttpsClient
                            (PROTOCOL + managerURIBody, this.getUsername(), this.getPassword()).getChildAppDetails
                            (appName);
                    if (managerResponse.status() == 200) {
                        InputStream reader = managerResponse.body().asInputStream();
                        return Response.ok().entity(reader).build();
                    } else if (managerResponse.status() == 401) {
                        String jsonString = new Gson().toJson(managerResponse.body().toString());
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity(managerResponse.body()
                                .toString()).build();
                    }
                } catch (feign.RetryableException e) {
                    String errString = new Gson().toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode
                            .SERVER_CONNECTION_ERROR, e.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errString).build();

                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("In proper format of managerId "
                        + " " + managerId).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("Unauthorized user : " + username).build();
        }
    }

    @Override
    public Response getClusterResourceNodeDetails(String managerId, String username)
            throws NotFoundException, IOException {
        boolean isAuthorized = permissionProvider.hasPermission(username,
                new Permission(Constants.PERMISSION_APP_NAME, VIWER_PERMISSION_STRING));
        if (isAuthorized) {
            String[] hostPort = managerId.split(Constants.WORKER_KEY_GENERATOR);
            if (hostPort.length == 2) {
                String nodeURIBody = generateURLHostPort(hostPort[0], hostPort[1]);
                try {
                    feign.Response resourceResponse = WorkerServiceFactory.getWorkerHttpsClient(
                            PROTOCOL + nodeURIBody, this.getUsername(), this.getPassword()).getClusterNodeDetails();
                    if (resourceResponse.status() == 200) {
                        Reader inputStream = resourceResponse.body().asReader();
                        List<ResourceClusterInfo> clusterInfos = gson.fromJson(inputStream,
                                new TypeToken<List<ResourceClusterInfo>>() {
                                }.getType());
                        Map<String, List<WorkerOverview>> totalResourceClusterDetails = new HashMap<>();
                        List<WorkerOverview> resourceClusterList = new ArrayList<>();
                        List<NodeConfigurationDetails> storedWorkerList = dashboardStore.selectAllWorkers();

                        for (ResourceClusterInfo clusterInfo : clusterInfos) {
                            if (!storedWorkerList.isEmpty()) {
                                for (NodeConfigurationDetails worker : storedWorkerList) {
                                    if (clusterInfo.getNodeId().equals(getCarbonID(worker.getWorkerId()))) {
                                        WorkerOverview workerOverview = new WorkerOverview();
                                        workerOverview.setNodeId(getCarbonID(worker.getWorkerId()));
                                        feign.Response workerResponse = WorkerServiceFactory.getWorkerHttpsClient(
                                                PROTOCOL + generateURLHostPort(worker.getHost(), String.valueOf(
                                                        worker.getPort())), getUsername(), getPassword()).getWorker();
                                        if ((workerResponse != null) && (workerResponse.status() == 200)) {
                                            Long timeInMillis = System.currentTimeMillis();
                                            String responseBody = workerResponse.body().toString();
                                            ServerDetails serverDetails = gson.fromJson(
                                                    responseBody, ServerDetails.class);
                                            String message = serverDetails.getMessage();
                                            if (message == null || message.isEmpty()) {
                                                workerOverview.setStatusMessage("Success");
                                            } else {
                                                workerOverview.setStatusMessage(message);
                                            }
                                            feign.Response activeSiddiAppsResponse = WorkerServiceFactory
                                                    .getWorkerHttpsClient(PROTOCOL + generateURLHostPort(
                                                            worker.getHost(), String.valueOf(worker.getPort())),
                                                            getUsername(), getPassword()).getSiddhiApps(true);
                                            String activeSiddiAppsResponseBody = activeSiddiAppsResponse.body()
                                                    .toString();
                                            List<String> activeApps = gson.fromJson(activeSiddiAppsResponseBody,
                                                    new TypeToken<List<String>>() {
                                                    }.getType());
                                            feign.Response inactiveSiddiAppsResponse = WorkerServiceFactory
                                                    .getWorkerHttpsClient(PROTOCOL + generateURLHostPort(
                                                            worker.getHost(), String.valueOf(worker.getPort())),
                                                            getUsername(), getPassword()).getSiddhiApps(false);
                                            String inactiveSiddiAppsResponseBody =
                                                    inactiveSiddiAppsResponse.body().toString();
                                            List<String> inactiveApps = gson.fromJson(inactiveSiddiAppsResponseBody,
                                                    new TypeToken<List<String>>() {
                                                    }.getType());
                                            serverDetails.setSiddhiApps(activeApps.size(), inactiveApps.size());
                                            WorkerMetricsSnapshot snapshot = new WorkerMetricsSnapshot(
                                                    serverDetails, timeInMillis);
                                            WorkerStateHolder.addMetrics(worker.getWorkerId(), snapshot);
                                            workerOverview.setLastUpdate(timeInMillis);
                                            workerOverview.setWorkerId(worker.getWorkerId());
                                            workerOverview.setServerDetails(serverDetails);
                                            resourceClusterList.add(workerOverview);
                                        }
                                        break;
                                    }
                                }
                            } else {
                                WorkerOverview workerOverview = new WorkerOverview();
                                workerOverview.setNodeId(clusterInfo.getNodeId());
                                workerOverview.setStatusMessage("Please add the node manually.");
                                resourceClusterList.add(workerOverview);
                            }
                        }
                        List<String> alreadyExistingResourceNodeNodeId = new ArrayList<>();
                        for (WorkerOverview overview : resourceClusterList) {
                            alreadyExistingResourceNodeNodeId.add(overview.getNodeId());
                        }
                        for (ResourceClusterInfo clusterInfo : clusterInfos) {
                            if (!alreadyExistingResourceNodeNodeId.contains(clusterInfo.getNodeId())) {
                                WorkerOverview workerOverview = new WorkerOverview();
                                workerOverview.setNodeId(clusterInfo.getNodeId());
                                workerOverview.setStatusMessage("Please add the node manually.");
                                resourceClusterList.add(workerOverview);
                            }
                        }

                        if (resourceClusterList.size() != 0) {
                            totalResourceClusterDetails.put("ResourceCluster", resourceClusterList);
                        }
                        String jsonString = new Gson().toJson(totalResourceClusterDetails);
                        return Response.ok().entity(jsonString).build();

                    } else if (resourceResponse.status() == 401) {
                        String jsonString = new Gson().toJson(resourceResponse.body().toString());
                        return Response.status(Response.Status.UNAUTHORIZED).entity(jsonString).build();
                    } else {
                        return Response.status(Response.Status.NOT_FOUND).entity(resourceResponse.body()
                                .toString()).build();
                    }
                } catch (feign.RetryableException e) {
                    String errString = new Gson().toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode
                            .SERVER_CONNECTION_ERROR, e.getMessage()));
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(errString).build();
                }
            } else {
                return Response.status(Response.Status.BAD_REQUEST).entity("In proper format of managerId "
                        + " " + managerId).build();
            }
        } else {
            return Response.status(Response.Status.FORBIDDEN).entity("unauthorized user : " + username).build();
        }
    }

    public static StatusDashboardMetricsDBHandler getMetricStore() {

        return metricStore;
    }

    private String removeCRLFCharacters(String str) {

        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }

    @Reference(
            name = "org.wso2.carbon.status.dashboard.core.internal.services.DatasourceServiceComponent",
            service = DatasourceServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterServiceDatasource"
    )
    public void regiterServiceDatasource(DatasourceServiceComponent datasourceServiceComponent) {

        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) DatasourceServiceComponent");
        }

    }

    public void unregisterServiceDatasource(DatasourceServiceComponent datasourceServiceComponent) {

        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) DatasourceServiceComponent");
        }
    }

    @Reference(
            name = "org.wso2.carbon.status.dashboard.core.internal.services.PermissionGrantServiceComponent",
            service = PermissionGrantServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterServicePermissionGrantService"
    )
    public void registerServicePermissionGrantService(PermissionGrantServiceComponent permissionGrantServiceComponent) {

        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(bind) ServicePermissionGrantService");
        }
    }

    public void unregisterServicePermissionGrantService(
            PermissionGrantServiceComponent permissionGrantServiceComponent) {

        if (logger.isDebugEnabled()) {
            logger.debug("@Reference(unbind) ServicePermissionGrantService");
        }
    }
}
