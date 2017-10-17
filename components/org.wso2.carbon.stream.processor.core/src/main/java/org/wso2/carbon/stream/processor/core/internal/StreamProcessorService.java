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
import org.wso2.carbon.cluster.coordinator.service.ClusterCoordinator;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.stream.processor.core.coordination.HACoordinationSinkHandler;
import org.wso2.carbon.stream.processor.core.coordination.HACoordinationSourceHandler;
import org.wso2.carbon.stream.processor.core.coordination.HAManager;
import org.wso2.carbon.stream.processor.core.coordination.exception.HAModeException;
import org.wso2.carbon.stream.processor.core.coordination.util.CompressionUtil;
import org.wso2.carbon.stream.processor.core.coordination.util.CoordinationConstants;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppAlreadyExistException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppFilesystemInvoker;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.sink.Sink;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Class which manage Siddhi Apps.
 */
public class StreamProcessorService {

    private Map<String, SiddhiAppData> siddhiAppMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(StreamProcessorService.class);

    public void deploySiddhiApp(String siddhiAppContent, String siddhiAppName) throws SiddhiAppConfigurationException,
            SiddhiAppAlreadyExistException {

        SiddhiAppData siddhiAppData = new SiddhiAppData(siddhiAppContent);

        if (siddhiAppMap.containsKey(siddhiAppName)) {
            throw new SiddhiAppAlreadyExistException("There is a Siddhi App with name " + siddhiAppName +
                    " is already exist");
        }

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
                        log.info("Periodic Persistence of Active Node Enabled. Restoring From Last Saved Snapshot " +
                                "for " + siddhiAppName);
                        String revision = siddhiAppRuntime.restoreLastRevision();
                        if (revision != null) {
                            log.info("Siddhi App " + siddhiAppName + " restored to revision " + revision);
                        }
                    } else {
                        log.warn("Periodic Persistence is Disabled. It is recommended to enable this feature when " +
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

                } else {
                    //Passive Node
                    if(haManager.isLiveStateSyncEnabled()) {
                        //Live State Sync Enabled of Passive Node
                        log.info("Live State Sync is Enabled for Passive Node. Restoring Active Node current state " +
                                "for " + siddhiAppName);
                        byte[] snapshot = StreamProcessorDataHolder.getHAManager().getActiveNodeSnapshot(siddhiAppName);
                        if (snapshot != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Snapshot for " + siddhiAppName + " found from Active Node Live State " +
                                        "Sync before deploying app");
                            }
                            try {
                                byte[] decompressGZIP = CompressionUtil.decompressGZIP(snapshot);
                                siddhiAppRuntime.restore(decompressGZIP);
                            } catch (IOException e) {
                                log.error("Error Decompressing Bytes " + e.getMessage(), e);
                            }
                        } else {
                            int gracePeriod = 1;
                            try {
                                gracePeriod = (int) ((Map) ((Map) StreamProcessorDataHolder.getInstance().
                                        getConfigProvider().getConfigurationObject(CoordinationConstants.
                                        CLUSTER_CONFIG_NS)).get(CoordinationConstants.CLUSTER_MODE_CONFIG_NS)).
                                        getOrDefault(CoordinationConstants.RETRY_APP_SYNC_PERIOD, 1);
                            } catch (ConfigurationException e) {
                                log.warn("No value given for " + CoordinationConstants.RETRY_APP_SYNC_PERIOD +
                                ". Default value of " + gracePeriod + " minute(s) will be used.");
                            }
                            retrySiddhiAppStateSync(gracePeriod, siddhiAppName, siddhiAppData, siddhiAppRuntime,
                                    inputHandlerMap);
                            log.info("Snapshot for " + siddhiAppName + " not found. Make sure Active and Passive" +
                                    " node have deployed the same Siddhi Applications");
                            log.info("Scheduled active node state sync for " + siddhiAppName + " in " +
                                    gracePeriod + " minute(s).");
                            return; //Defer the app deployment until state is synced from Active Node
                        }
                    } else {
                        //Live State Sync Disabled of Passive Node
                        if (StreamProcessorDataHolder.isPersistenceEnabled()) {
                            log.info("Live State Sync is Disabled for Passive Node. Restoring Active nodes last " +
                                    "persisted state for " + siddhiAppName);
                            String revision = siddhiAppRuntime.restoreLastRevision();
                            if (revision != null) {
                                log.info("Siddhi App " + siddhiAppName + " restored to revision " + revision);
                            }
                        } else {
                            throw new HAModeException("Passive Node Periodic Persistence is Disabled and Live State " +
                                    "Sync Enabled. Please enable Periodic Persistence.");
                        }
                    }
                }
            } else {
                if (StreamProcessorDataHolder.isPersistenceEnabled()) {
                    log.info("Periodic State persistence enabled. Restoring last persisted state of " + siddhiAppName);
                    String revision = siddhiAppRuntime.restoreLastRevision();
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
            siddhiAppMap.put(siddhiAppName, siddhiAppData);
        }
    }

    public void undeploySiddhiApp(String siddhiAppName) {

        if (siddhiAppMap.containsKey(siddhiAppName)) {
            SiddhiAppData siddhiAppData = siddhiAppMap.get(siddhiAppName);
            if (siddhiAppData != null) {
                if (siddhiAppData.isActive()) {
                    siddhiAppData.getSiddhiAppRuntime().shutdown();
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

    private Timer retrySiddhiAppStateSync(int gracePeriod, String siddhiAppName, SiddhiAppData siddhiAppData,
                                          SiddhiAppRuntime siddhiAppRuntime, Map<String,
            InputHandler> inputHandlerMap) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                byte[] snapshot = StreamProcessorDataHolder.getHAManager().getActiveNodeSnapshot(siddhiAppName);
                if (snapshot != null) {
                    try {
                        byte[] decompressGZIP = CompressionUtil.decompressGZIP(snapshot);
                        siddhiAppRuntime.restore(decompressGZIP);
                    } catch (IOException e) {
                        log.error("Error Decompressing Bytes " + e.getMessage(), e);
                    }
                } else {
                    log.error("Snapshot for " + siddhiAppName + " not found after " + gracePeriod +
                            ". Make sure Active and Passive node have deployed the same Siddhi Applications");
                    return; // TODO: 10/16/17 Should throw Exception? Or retry?
                }

                siddhiAppRuntime.start();
                log.info("Siddhi App " + siddhiAppName + " deployed successfully after active node sync in "
                        + gracePeriod + " minutes");
                siddhiAppData.setActive(true);
                siddhiAppData.setSiddhiAppRuntime(siddhiAppRuntime);
                siddhiAppData.setInputHandlerMap(inputHandlerMap);
                siddhiAppMap.put(siddhiAppName, siddhiAppData);
            }
        }, gracePeriod * 60 * 1000L);

    return timer;
    }
}
