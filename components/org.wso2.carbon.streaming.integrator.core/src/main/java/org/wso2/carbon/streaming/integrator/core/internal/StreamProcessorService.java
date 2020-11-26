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

package org.wso2.carbon.streaming.integrator.core.internal;


import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.SiddhiManager;
import io.siddhi.core.exception.CannotRestoreSiddhiAppStateException;
import io.siddhi.core.exception.ConnectionUnavailableException;
import io.siddhi.core.stream.input.InputHandler;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.table.Table;
import io.siddhi.core.util.transport.BackoffRetryCounter;
import io.siddhi.query.api.SiddhiApp;
import io.siddhi.query.api.annotation.Element;
import io.siddhi.query.api.util.AnnotationHelper;
import io.siddhi.query.compiler.SiddhiCompiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.streaming.integrator.core.ha.HACoordinationRecordTableHandler;
import org.wso2.carbon.streaming.integrator.core.ha.HACoordinationSinkHandler;
import org.wso2.carbon.streaming.integrator.core.ha.HACoordinationSourceHandler;
import org.wso2.carbon.streaming.integrator.core.ha.HAManager;
import org.wso2.carbon.streaming.integrator.core.ha.RetryRecordTableConnection;
import org.wso2.carbon.streaming.integrator.core.internal.exception.SiddhiAppAlreadyExistException;
import org.wso2.carbon.streaming.integrator.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.streaming.integrator.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.streaming.integrator.core.internal.util.SiddhiAppFilesystemInvoker;
import org.wso2.carbon.streaming.integrator.core.internal.util.SiddhiAppProcessorConstants;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public void deploySiddhiApp(String siddhiAppContent, String siddhiAppName) throws SiddhiAppConfigurationException,
            SiddhiAppAlreadyExistException, ConnectionUnavailableException {

        SiddhiAppData siddhiAppData = new SiddhiAppData(siddhiAppContent);
        boolean persistenceStoreClearEnabled = Boolean.valueOf
                (System.getProperty(SiddhiAppProcessorConstants.PERSISTENCE_STORE_CLEAR_ENABLED));
        String siddhiApp = System.getProperty(SiddhiAppProcessorConstants.SIDDHI_APP);

        if (siddhiAppMap.containsKey(siddhiAppName)) {
            throw new SiddhiAppAlreadyExistException("There is a Siddhi App with name " + siddhiAppName +
                    " is already exist");
        }
        SiddhiManager siddhiManager = StreamProcessorDataHolder.getSiddhiManager();
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiAppContent);

        Collection<Table> tables = siddhiAppRuntime.getTables();
        Set<String> streamNames = siddhiAppRuntime.getStreamDefinitionMap().keySet();
        Map<String, InputHandler> inputHandlerMap =
                new ConcurrentHashMap<String, InputHandler>(streamNames.size());
        for (String streamName : streamNames) {
            inputHandlerMap.put(streamName, siddhiAppRuntime.getInputHandler(streamName));
        }

        HAManager haManager = StreamProcessorDataHolder.getHAManager();

        if (haManager != null) {
            Collection<List<Sink>> sinkCollection = siddhiAppRuntime.getSinks();
            Collection<List<Source>> sourceCollection = siddhiAppRuntime.getSources();
            if (haManager.isActiveNode()) {
                //Active Node
                if (StreamProcessorDataHolder.isPersistenceEnabled()) {

                    if (persistenceStoreClearEnabled) {
                        if (siddhiApp != null) {
                            if (siddhiApp.equals(siddhiAppName)) {
                                siddhiAppRuntime.clearAllRevisions();
                                log.info("Deleting all the revisions of the Periodic Persistence of " +
                                        "Active Node for " + siddhiAppName);
                            }
                        } else {
                            log.info("Deleting all the revisions of the Periodic Persistence of " +
                                    "Active Node for " + siddhiAppName);
                            siddhiAppRuntime.clearAllRevisions();
                        }
                    } else {
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
                    }
                } else {
                    log.info(
                            "Periodic Persistence is Disabled. It is recommended to enable this feature when " +
                                    "using 2 Node Minimum HA");
                }

                log.info("Setting SourceHandlers of " + siddhiAppName + " to Active");
                for (List<Source> sources : sourceCollection) {
                    for (Source source : sources) {
                        ((HACoordinationSourceHandler) source.getMapper().getHandler()).setAsActive();
                    }
                }

                log.info("Setting SinksHandlers of " + siddhiAppName + " to Active");
                for (List<Sink> sinks : sinkCollection) {
                    for (Sink sink : sinks) {
                        HACoordinationSinkHandler handler = (HACoordinationSinkHandler) sink.getHandler();
                        if (handler != null) {
                            handler.setAsActive();
                        }
                    }
                }

                log.info("Setting RecordTableHandlers of " + siddhiAppName + " to Active");
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
                siddhiAppData.setActive(true);
                siddhiAppData.setSiddhiAppRuntime(siddhiAppRuntime);
                siddhiAppData.setInputHandlerMap(inputHandlerMap);
                siddhiAppData.setDeploymentTime(System.currentTimeMillis());
                siddhiAppMap.put(siddhiAppName, siddhiAppData);
                siddhiAppRuntime.start();
                log.info("Siddhi App " + siddhiAppName + " deployed successfully");

            } else {
                //Passive Node
                siddhiAppData.setActive(false);
                siddhiAppRuntime.shutdown();
                siddhiAppData.setInputHandlerMap(inputHandlerMap);
                siddhiAppData.setDeploymentTime(System.currentTimeMillis());
                siddhiAppMap.put(siddhiAppName, siddhiAppData);
            }
        } else {
            if (StreamProcessorDataHolder.isPersistenceEnabled()) {
                log.info("Periodic State persistence enabled. Restoring last persisted state of "
                        + siddhiAppName);
                String revision = null;

                if (persistenceStoreClearEnabled) {
                    if (siddhiApp != null) {
                        if (siddhiApp.equals(siddhiAppName)) {
                            siddhiAppRuntime.clearAllRevisions();
                            log.info("Deleting all the revisions of the Periodic Persistence of " +
                                    "Active Node for " + siddhiAppName);
                        }
                    } else {
                        log.info("Deleting all the revisions of the Periodic Persistence of " +
                                "Active Node for " + siddhiAppName);
                        siddhiAppRuntime.clearAllRevisions();
                    }

                } else {
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

    public boolean isExists(String siddhiAppName) throws SiddhiAppConfigurationException {
        return siddhiAppMap.containsKey(siddhiAppName);
    }

    public void addSiddhiAppFile(String siddhiAppName, SiddhiAppData siddhiAppData) {
        siddhiAppMap.put(siddhiAppName, siddhiAppData);
    }

    public Map<String, SiddhiAppData> getSiddhiAppMap() {
        return siddhiAppMap;
    }

    public boolean isActiveNode() {
        return StreamProcessorDataHolder.getHAManager().isActiveNode();
    }
}
