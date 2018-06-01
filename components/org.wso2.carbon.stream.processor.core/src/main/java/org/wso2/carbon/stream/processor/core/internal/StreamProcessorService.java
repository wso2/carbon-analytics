/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.stream.processor.core.internal;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.core.distribution.DeploymentStatus;
import org.wso2.carbon.stream.processor.core.distribution.DistributionService;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationRecordTableHandler;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSinkHandler;
import org.wso2.carbon.stream.processor.core.ha.HACoordinationSourceHandler;
import org.wso2.carbon.stream.processor.core.ha.HAManager;
import org.wso2.carbon.stream.processor.core.ha.RetryRecordTableConnection;
import org.wso2.carbon.stream.processor.core.ha.exception.HAModeException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppAlreadyExistException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppFilesystemInvoker;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.stream.processor.core.util.DeploymentMode;
import org.wso2.carbon.stream.processor.core.util.RuntimeMode;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.core.table.Table;
import org.wso2.siddhi.core.util.transport.BackoffRetryCounter;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Class which manage Siddhi Apps.
 */
public class StreamProcessorService {

    private static final Logger log = LoggerFactory.getLogger(StreamProcessorService.class);
    private Map<String, SiddhiAppData> siddhiAppMap = new ConcurrentHashMap<>();
    private BackoffRetryCounter backoffRetryCounter = new BackoffRetryCounter();
    private DistributionService distributionService = StreamProcessorDataHolder.getDistributionService();

    public void deploySiddhiApp(String siddhiAppContent, String siddhiAppName) throws SiddhiAppConfigurationException,
            SiddhiAppAlreadyExistException, ConnectionUnavailableException {

        SiddhiAppData siddhiAppData = new SiddhiAppData(siddhiAppContent);

        if (siddhiAppMap.containsKey(siddhiAppName)) {
            throw new SiddhiAppAlreadyExistException("There is a Siddhi App with name " + siddhiAppName +
                    " is already exist");
        }
        if (distributionService.getRuntimeMode() == RuntimeMode.MANAGER && distributionService.getDeploymentMode() ==
                DeploymentMode.DISTRIBUTED) {
            if (distributionService.isLeader()) {
                DeploymentStatus deploymentStatus = distributionService.distribute(siddhiAppContent);
                if (deploymentStatus.isDeployed()) {
                    siddhiAppData.setActive(true);
                    siddhiAppMap.put(siddhiAppName, siddhiAppData);
                    //can't set SiddhiAppRuntime. Hence we will run into issues when retrieving stats for status
                    // dashboard. Need to fix after discussing
                } else {
                    throw new SiddhiAppConfigurationException("Error in deploying Siddhi App " + siddhiAppName + "in "
                            + "distributed mode");
                }
            }
        } else {
            SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
            SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiAppContent);

            if (siddhiAppRuntime != null) {
                Set<String> streamNames = siddhiAppRuntime.getStreamDefinitionMap().keySet();
                Map<String, InputHandler> inputHandlerMap =
                        new ConcurrentHashMap<String, InputHandler>(streamNames.size());
                for (String streamName : streamNames) {
                    inputHandlerMap.put(streamName, siddhiAppRuntime.getInputHandler(streamName));
                }

                HAManager haManager = StreamProcessorDataHolder.getHAManager();

                if (haManager != null) {
                    if (haManager.isActiveNode()) {
                        //Active Node
                        if (StreamProcessorDataHolder.isPersistenceEnabled()) {
                            log.info(
                                    "Periodic Persistence of Active Node Enabled. Restoring From Last Saved Snapshot " +
                                            "for " + siddhiAppName);
                            String revision = null;
                            try {
                                revision = siddhiAppRuntime.restoreLastRevision();
                            } catch (CannotRestoreSiddhiAppStateException e) {
                                log.error("Error in restoring Siddhi app " + siddhiAppRuntime.getName(), e);
                            }
                            if (revision != null) {
                                log.info("Siddhi App " + siddhiAppName + " restored to revision " + revision);
                            }

                        } else {
                            log.info(
                                    "Periodic Persistence is Disabled. It is recommended to enable this feature when " +
                                            "using 2 Node Minimum HA");
                        }

                        log.info("Setting SinksHandlers of " + siddhiAppName + " to Active");
                        Collection<List<Sink>> sinkCollection = siddhiAppRuntime.getSinks();
                        for (List<Sink> sinks : sinkCollection) {
                            for (Sink sink : sinks) {
                                ((HACoordinationSinkHandler) sink.getHandler()).setAsActive();
                            }
                        }

                        log.info("Setting SourceHandlers of " + siddhiAppName + " to Active");
                        Collection<List<Source>> sourceCollection = siddhiAppRuntime.getSources();
                        for (List<Source> sources : sourceCollection) {
                            for (Source source : sources) {
                                ((HACoordinationSourceHandler) source.getMapper().getHandler()).setAsActive();
                            }
                        }
                        log.info("Setting RecordTableHandlers of " + siddhiAppName + " to Active");
                        Collection<Table> tables = siddhiAppRuntime.getTables();
                        for (Table table : tables) {
                            HACoordinationRecordTableHandler recordTableHandler = (HACoordinationRecordTableHandler)
                                    table.getHandler();
                            try {
                                if (recordTableHandler != null) {
                                    recordTableHandler.setAsActive();
                                }
                            } catch (ConnectionUnavailableException e) {
                                backoffRetryCounter.reset();
                                log.error("HA Deployment: Error in connecting to table " + recordTableHandler.getTableId()
                                        + " while changing from passive state to active, will retry in "
                                        + backoffRetryCounter.getTimeInterval(), e);
                                ScheduledExecutorService scheduledExecutorService = Executors.
                                        newSingleThreadScheduledExecutor();
                                backoffRetryCounter.increment();
                                scheduledExecutorService.schedule(new RetryRecordTableConnection(backoffRetryCounter,
                                                table.getHandler(), scheduledExecutorService),
                                        backoffRetryCounter.getTimeIntervalMillis(), TimeUnit.MILLISECONDS);
                            }
                        }

                    } else {
                        //Passive Node
                        if (haManager.isLiveStateSyncEnabled()) {
                            //Live State Sync Enabled of Passive Node
                            log.info("Live State Sync is Enabled for Passive Node. Restoring Active Node current state "
                                    +
                                    "for " + siddhiAppName);
                            boolean isPersisted = StreamProcessorDataHolder
                                    .getHAManager().persistActiveNode(siddhiAppName);

                            if (isPersisted) {
                                if (log.isDebugEnabled()) {
                                    log.debug("Snapshot for " + siddhiAppName + " found from Active Node Live State " +
                                            "Sync before deploying app");
                                }
                                try {
                                    siddhiAppRuntime.restoreLastRevision();
                                } catch (CannotRestoreSiddhiAppStateException e) {
                                    log.error("Error in restoring Siddhi app " + siddhiAppRuntime.getName(), e);
                                }

                            } else {

                                int gracePeriod =
                                        StreamProcessorDataHolder.getDeploymentConfig().getRetryAppSyncPeriod();

                                siddhiAppData.setActive(false);
                                siddhiAppData.setSiddhiAppRuntime(siddhiAppRuntime);
                                siddhiAppData.setInputHandlerMap(inputHandlerMap);
                                siddhiAppData.setDeploymentTime(System.currentTimeMillis());
                                siddhiAppMap.put(siddhiAppName, siddhiAppData);

                                Timer timer = retrySiddhiAppLiveStateSync(gracePeriod, siddhiAppName, siddhiAppData,
                                        siddhiAppRuntime);
                                log.info("Snapshot for " + siddhiAppName + " not found. Make sure Active and Passive" +
                                        " node have deployed the same Siddhi Applications");
                                log.info("Scheduled active node state sync for " + siddhiAppName + " in " +
                                        gracePeriod / 1000 + " seconds.");
                                haManager.addRetrySiddhiAppSyncTimer(timer);

                                return; // Siddhi application set to inactive state
                            }
                        } else {
                            //Live State Sync Disabled of Passive Node
                            if (StreamProcessorDataHolder.isPersistenceEnabled()) {
                                log.info("Live State Sync is Disabled for Passive Node. Restoring Active nodes last " +
                                        "persisted state for " + siddhiAppName);
                                String revision = null;
                                try {
                                    revision = siddhiAppRuntime.restoreLastRevision();
                                } catch (CannotRestoreSiddhiAppStateException e) {
                                    log.error("Error in restoring Siddhi app " + siddhiAppRuntime.getName(), e);
                                }
                                if (revision != null) {
                                    log.info("Siddhi App " + siddhiAppName + " restored to revision " + revision);
                                } else {
                                    int gracePeriod = StreamProcessorDataHolder.getDeploymentConfig().
                                            getRetryAppSyncPeriod();

                                    siddhiAppData.setActive(false);
                                    siddhiAppData.setSiddhiAppRuntime(siddhiAppRuntime);
                                    siddhiAppData.setInputHandlerMap(inputHandlerMap);
                                    siddhiAppData.setDeploymentTime(System.currentTimeMillis());
                                    siddhiAppMap.put(siddhiAppName, siddhiAppData);

                                    Timer timer = retrySiddhiAppPersistenceStateSync(gracePeriod, siddhiAppName,
                                            siddhiAppData, siddhiAppRuntime);
                                    log.info(
                                            "Snapshot for " + siddhiAppName + " not found. Make sure Active and Passive"
                                                    +
                                                    " node have deployed the same Siddhi Applications");
                                    log.info("Scheduled active node persistence sync for " + siddhiAppName + " in " +
                                            gracePeriod / 1000 + " seconds.");
                                    haManager.addRetrySiddhiAppSyncTimer(timer);
                                    return; //Defer the app deployment until state is synced from Active Node
                                }
                            } else {
                                throw new HAModeException(
                                        "Passive Node Periodic Persistence is Disabled and Live State " +
                                                "Sync Disabled. Please enable Periodic Persistence.");
                            }
                        }
                    }
                } else {
                    if (StreamProcessorDataHolder.isPersistenceEnabled()) {
                        log.info("Periodic State persistence enabled. Restoring last persisted state of "
                                + siddhiAppName);
                        String revision = null;
                        try {
                            revision = siddhiAppRuntime.restoreLastRevision();
                        } catch (CannotRestoreSiddhiAppStateException e) {
                            log.error("Error in restoring Siddhi app " + siddhiAppRuntime.getName(), e);
                        }
                        if (revision != null) {
                            log.info("Siddhi App " + siddhiAppName + " restored to revision " + revision);
                        }
                    }
                }

                siddhiAppRuntime.start();
                log.info("Siddhi App " + siddhiAppName + " deployed successfully");
                siddhiAppData.setActive(true);
                siddhiAppData.setSiddhiAppRuntime(siddhiAppRuntime);
                siddhiAppData.setInputHandlerMap(inputHandlerMap);
                siddhiAppData.setDeploymentTime(System.currentTimeMillis());
                siddhiAppMap.put(siddhiAppName, siddhiAppData);
            }
        }
    }

    public void undeploySiddhiApp(String siddhiAppName) {
        if (siddhiAppMap.containsKey(siddhiAppName)) {
            if (distributionService.getRuntimeMode() == RuntimeMode.MANAGER &&
                    distributionService.getDeploymentMode() == DeploymentMode.DISTRIBUTED) {
                distributionService.undeploy(siddhiAppName);
            } else {
                SiddhiAppData siddhiAppData = siddhiAppMap.get(siddhiAppName);
                if (siddhiAppData != null) {
                    if (siddhiAppData.isActive()) {
                        siddhiAppData.getSiddhiAppRuntime().shutdown();
                    }
                }
            }
            siddhiAppMap.remove(siddhiAppName);
            log.info("Siddhi App File " + siddhiAppName + " undeployed successfully.");
        }
    }

    public boolean delete(String siddhiAppName) throws SiddhiAppConfigurationException,
            SiddhiAppDeploymentException {

        if (siddhiAppMap.containsKey(siddhiAppName)) {
            SiddhiAppFilesystemInvoker.delete(siddhiAppName);
            return true;
        }
        return false;
    }

    public String validateAndSave(String siddhiApp, boolean isUpdate) throws SiddhiAppConfigurationException,
            SiddhiAppDeploymentException {

        String siddhiAppName = "";
        try {
            siddhiAppName = getSiddhiAppName(siddhiApp);
            if (isUpdate || !siddhiAppMap.containsKey(siddhiAppName)) {
                SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
                SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
                if (siddhiAppRuntime != null) {
                    SiddhiAppFilesystemInvoker.save(siddhiApp, siddhiAppName);
                    return siddhiAppName;
                }
            }
        } catch (SiddhiAppDeploymentException e) {
            log.error("Exception occurred when saving Siddhi App : " + siddhiAppName, e);
            throw e;
        } catch (Throwable e) {
            log.error("Exception occurred when validating Siddhi App " + siddhiAppName, e);
            throw new SiddhiAppConfigurationException(e);
        }
        return null;
    }

    public String getSiddhiAppName(String siddhiApp) throws SiddhiAppConfigurationException {
        try {
            SiddhiApp parsedSiddhiApp = SiddhiCompiler.parse(siddhiApp);
            Element nameAnnotation = AnnotationHelper.
                    getAnnotationElement(SiddhiAppProcessorConstants.ANNOTATION_NAME_NAME,
                            null, parsedSiddhiApp.getAnnotations());

            if (nameAnnotation == null || nameAnnotation.getValue().isEmpty()) {
                throw new SiddhiAppConfigurationException("Siddhi App name must " +
                        "be provided as @App:name('name').");
            }

            return nameAnnotation.getValue();

        } catch (Throwable e) {
            throw new SiddhiAppConfigurationException("Exception occurred when retrieving Siddhi App Name ", e);
        }
    }

    public boolean isExists(String siddhiApp) throws SiddhiAppConfigurationException {
        return siddhiAppMap.containsKey(getSiddhiAppName(siddhiApp));
    }

    public void addSiddhiAppFile(String siddhiAppName, SiddhiAppData siddhiAppData) {
        siddhiAppMap.put(siddhiAppName, siddhiAppData);
    }

    public Map<String, SiddhiAppData> getSiddhiAppMap() {
        return siddhiAppMap;
    }

    /**
     * When passive node gets live state sync from active node, schedule a retry for state if active node doesn't
     * send a snapshot at startup.
     *
     * @param gracePeriod      time after which passive node retry to get state
     * @param siddhiAppName    name of Siddhi application whose state will be requested
     * @param siddhiAppData    data of relevant siddhi application
     * @param siddhiAppRuntime runtime of relevant siddhi application
     * @return reference to the timer that is started
     */
    private Timer retrySiddhiAppLiveStateSync(int gracePeriod, String siddhiAppName, SiddhiAppData siddhiAppData,
                                              SiddhiAppRuntime siddhiAppRuntime) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                boolean isPersisted =
                        StreamProcessorDataHolder.getHAManager().persistActiveNode(siddhiAppName);
                if (isPersisted) {
                    try {
                        siddhiAppRuntime.restoreLastRevision();
                        siddhiAppRuntime.start();
                        siddhiAppData.setActive(true);
                        siddhiAppData.setSiddhiAppRuntime(siddhiAppRuntime);
                        siddhiAppData.setDeploymentTime(System.currentTimeMillis());
                        siddhiAppMap.put(siddhiAppName, siddhiAppData);
                        log.info("Siddhi App " + siddhiAppName + " deployed successfully after active node sync in "
                                + gracePeriod / 1000 + " seconds");
                    } catch (CannotRestoreSiddhiAppStateException e) {
                        log.error("Error in restoring Siddhi app " + siddhiAppRuntime.getName(), e);
                    }
                } else {
                    log.error("Snapshot for " + siddhiAppName + " not found after " + gracePeriod / 1000 + " seconds." +
                            " Make sure Active and Passive node have deployed the same Siddhi Applications");
                }
            }
        }, gracePeriod);

        return timer;
    }

    /**
     * When passive node gets state sync from active node persistence store, schedule a retry for state if
     * active node hasn't already persisted a state.
     *
     * @param gracePeriod      time after which passive node retry to get state
     * @param siddhiAppName    name of Siddhi application whose state will be requested
     * @param siddhiAppData    data of relevant siddhi application
     * @param siddhiAppRuntime runtime of relevant siddhi application
     * @return reference to the timer that is started
     */
    private Timer retrySiddhiAppPersistenceStateSync(int gracePeriod, String siddhiAppName,
                                                     SiddhiAppData siddhiAppData, SiddhiAppRuntime siddhiAppRuntime) {

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String revision = null;
                try {
                    revision = siddhiAppRuntime.restoreLastRevision();
                } catch (CannotRestoreSiddhiAppStateException e) {
                    log.error("Error in restoring Siddhi app " + siddhiAppRuntime.getName(), e);
                }
                if (revision != null) {
                    log.info("Siddhi App " + siddhiAppName + " restored to revision " + revision);
                    siddhiAppRuntime.start();
                    siddhiAppData.setActive(true);
                    siddhiAppData.setSiddhiAppRuntime(siddhiAppRuntime);
                    siddhiAppData.setDeploymentTime(System.currentTimeMillis());
                    siddhiAppMap.put(siddhiAppName, siddhiAppData);
                    log.info("Siddhi App " + siddhiAppName + " deployed successfully after active node sync in "
                            + gracePeriod / 1000 + " seconds");
                } else {
                    log.error("Snapshot for " + siddhiAppName + " not found after " + gracePeriod / 1000 + " seconds." +
                            " Make sure Active and Passive node have deployed the same Siddhi Applications");
                }
            }
        }, gracePeriod);

        return timer;
    }
}
