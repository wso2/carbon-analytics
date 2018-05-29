/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core.impl;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.stream.processor.core.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.core.api.ApiResponseMessageWithCode;
import org.wso2.carbon.stream.processor.core.api.NotFoundException;
import org.wso2.carbon.stream.processor.core.api.SiddhiAppsApiService;
import org.wso2.carbon.stream.processor.core.impl.utils.Constants;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.beans.SiddhiAppElements;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.stream.processor.core.model.SiddhiAppContent;
import org.wso2.carbon.stream.processor.core.model.SiddhiAppMetrics;
import org.wso2.carbon.stream.processor.core.model.SiddhiAppRevision;
import org.wso2.carbon.stream.processor.core.model.SiddhiAppStatus;
import org.wso2.msf4j.Request;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.SiddhiElement;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.*;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.partition.PartitionType;
import org.wso2.siddhi.query.api.execution.partition.RangePartitionType;
import org.wso2.siddhi.query.api.execution.partition.ValuePartitionType;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;


/**
 * Siddhi Service Implementataion Class
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-05-31T15:43:24.557Z")
public class SiddhiAppsApiServiceImpl extends SiddhiAppsApiService {

    private static final Logger log = LoggerFactory.getLogger(SiddhiAppsApiServiceImpl.class);
    private static final String PERMISSION_APP_NAME = "SAPP";
    private static final String MANAGE_SIDDHI_APP_PERMISSION_STRING = "siddhiApp.manage";
    private static final String VIEW_SIDDHI_APP_PERMISSION_STRING = "siddhiApp.view";

    public Response siddhiAppsPost(String body) throws NotFoundException {
        String jsonString;
        Response.Status status;
        try {
            String siddhiAppName = StreamProcessorDataHolder.
                    getStreamProcessorService().validateAndSave(body, false);
            if (siddhiAppName != null) {
                URI location = new URI(SiddhiAppProcessorConstants.SIDDHI_APP_REST_PREFIX + File.separator +
                        File.separator + siddhiAppName);
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                        "Siddhi App saved succesfully and will be deployed in next deployment cycle"));
                return Response.created(location).entity(jsonString).build();
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.CONFLICT,
                        "There is a Siddhi App already " +
                                "exists with same name"));
                status = Response.Status.CONFLICT;
            }

        } catch (SiddhiAppDeploymentException | URISyntaxException e) {
            jsonString = new Gson().
                    toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.FILE_PROCESSING_ERROR,
                            e.getMessage()));
            status = Response.Status.INTERNAL_SERVER_ERROR;
        } catch (SiddhiAppConfigurationException e) {
            jsonString = new Gson().toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.VALIDATION_ERROR,
                    e.getMessage()));
            status = Response.Status.BAD_REQUEST;
        }

        return Response.status(status).entity(jsonString).build();
    }

    public Response siddhiAppsPut(String body) throws NotFoundException {
        String jsonString = new Gson().toString();
        Response.Status status = Response.Status.OK;
        try {
            boolean isAlreadyExists = StreamProcessorDataHolder.
                    getStreamProcessorService().isExists(body);
            String siddhiAppName = StreamProcessorDataHolder.
                    getStreamProcessorService().validateAndSave(body, true);
            if (siddhiAppName != null) {
                if (isAlreadyExists) {
                    jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                            "Siddhi App updated succesfully and will be deployed in next deployment cycle"));
                } else {
                    URI location = new URI(SiddhiAppProcessorConstants.SIDDHI_APP_REST_PREFIX + File.separator +
                            File.separator + siddhiAppName);
                    jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                            "Siddhi App saved succesfully and will be deployed in next deployment cycle"));
                    return Response.created(location).entity(jsonString).build();
                }
            }
        } catch (SiddhiAppDeploymentException | URISyntaxException e) {
            jsonString = new Gson().
                    toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.FILE_PROCESSING_ERROR,
                            e.getMessage()));
            status = Response.Status.INTERNAL_SERVER_ERROR;
        } catch (SiddhiAppConfigurationException e) {
            jsonString = new Gson().toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.VALIDATION_ERROR,
                    e.getMessage()));
            status = Response.Status.BAD_REQUEST;
        }

        return Response.status(status).entity(jsonString).build();
    }

    public Response siddhiAppsGet(String isActive) throws NotFoundException {
        String jsonString;
        boolean isActiveValue;

        List<String> artifactList = new ArrayList<>();

        Map<String, SiddhiAppData> siddhiAppFileMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppMap();

        if (isActive != null && !isActive.trim().isEmpty()) {
            isActiveValue = Boolean.parseBoolean(isActive);
            for (Map.Entry<String, SiddhiAppData> siddhiAppFileEntry : siddhiAppFileMap.entrySet()) {
                if (isActiveValue == siddhiAppFileEntry.getValue().isActive()) {
                    artifactList.add(siddhiAppFileEntry.getKey());
                }
            }

        } else {
            for (Map.Entry<String, SiddhiAppData> siddhiAppFileEntry : siddhiAppFileMap.entrySet()) {
                artifactList.add(siddhiAppFileEntry.getKey());
            }
        }

        return Response.ok().entity(artifactList).build();
    }

    public Response siddhiAppsAppNameDelete(String appFileName) throws NotFoundException {
        String jsonString;
        Response.Status status = Response.Status.OK;

        try {
            if (StreamProcessorDataHolder.getStreamProcessorService().delete(appFileName)) {
                return Response.status(status).build();
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                        "There is no Siddhi App exist " +
                                "with provided name : " + appFileName));
                status = Response.Status.NOT_FOUND;
            }
        } catch (SiddhiAppConfigurationException e) {
            jsonString = new Gson().toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.VALIDATION_ERROR,
                    e.getMessage()));
            status = Response.Status.BAD_REQUEST;
        } catch (SiddhiAppDeploymentException e) {
            jsonString = new Gson().
                    toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.FILE_PROCESSING_ERROR,
                            e.getMessage()));
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status).entity(jsonString).build();
    }

    public Response siddhiAppsAppNameGet(String appName) throws NotFoundException {

        String jsonString;
        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppMap();

        if (siddhiAppMap.containsKey(appName)) {
            SiddhiAppData siddhiAppData = siddhiAppMap.get(appName);
            SiddhiAppContent siddhiAppContent = new SiddhiAppContent();
            siddhiAppContent.setcontent(siddhiAppData.getSiddhiApp());
            return Response.ok().entity(siddhiAppContent).build();
        }

        jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                "There is no Siddhi App exist " +
                        "with provided name : " + appName));
        return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();

    }

    public Response siddhiAppsAppNameStatusGet(String appFileName) throws NotFoundException {

        String jsonString;

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppMap();

        if (siddhiAppMap.containsKey(appFileName)) {
            SiddhiAppData siddhiAppData = siddhiAppMap.get(appFileName);
            SiddhiAppStatus siddhiAppStatus = new SiddhiAppStatus();
            siddhiAppStatus.setStatus(siddhiAppData.isActive() ?
                    SiddhiAppProcessorConstants.SIDDHI_APP_STATUS_ACTIVE :
                    SiddhiAppProcessorConstants.SIDDHI_APP_STATUS_INACTIVE);
            return Response.ok().entity(siddhiAppStatus).build();
        }

        jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                "There is no Siddhi App exist " +
                        "with provided name : " + appFileName));
        return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();
    }

    public Response siddhiAppsAppNameBackupPost(String appName) throws NotFoundException {
        String jsonString;
        Response.Status status = Response.Status.OK;

        try {
            SiddhiAppRuntime siddhiAppRuntime = StreamProcessorDataHolder.getSiddhiManager().
                    getSiddhiAppRuntime(appName);
            if (siddhiAppRuntime != null) {
                PersistenceReference persistenceReference = siddhiAppRuntime.persist();
                SiddhiAppRevision siddhiAppRevision = new SiddhiAppRevision();
                siddhiAppRevision.setrevision(persistenceReference.getRevision());
                return Response.status(Response.Status.CREATED).entity(siddhiAppRevision).build();
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                        "There is no Siddhi App exist " +
                                "with provided name : " + appName));
                status = Response.Status.NOT_FOUND;
            }
        } catch (Exception e) {
            log.error("Exception occurred when backup the state for Siddhi App : " + appName, e);
            jsonString = new Gson().
                    toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.FILE_PROCESSING_ERROR,
                            e.getMessage()));
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status).entity(jsonString).build();
    }

    public Response siddhiAppsAppNameRestorePost(String appName, String revision)
            throws NotFoundException {

        String jsonString;
        Response.Status status = Response.Status.OK;

        try {
            SiddhiAppRuntime siddhiAppRuntime = StreamProcessorDataHolder.getSiddhiManager().
                    getSiddhiAppRuntime(appName);
            if (siddhiAppRuntime != null) {
                if (revision == null) {
                    siddhiAppRuntime.restoreLastRevision();
                    jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                            "State restored to last revision for Siddhi App :" +
                                    appName));
                } else {
                    siddhiAppRuntime.restoreRevision(revision);
                    jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                            "State restored to revision " + revision + " for Siddhi App :" +
                                    appName));
                }
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                        "There is no Siddhi App exist " +
                                "with provided name : " + appName));
                status = Response.Status.NOT_FOUND;
            }
        } catch (Exception e) {
            log.error("Exception occurred when restoring the state for Siddhi App : " + appName, e);
            jsonString = new Gson().
                    toJson(new ApiResponseMessageWithCode(ApiResponseMessageWithCode.FILE_PROCESSING_ERROR,
                            e.getMessage()));
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status).entity(jsonString).build();
    }

    public Response siddhiAppsStatisticsGet(String isActive) throws NotFoundException {
        String jsonString;
        boolean isActiveValue;

        Map<String, SiddhiAppData> siddhiAppFileMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppMap();
        List<SiddhiAppMetrics> siddhiAppMetricsList = new ArrayList();
        if (!siddhiAppFileMap.isEmpty()) {
            if (isActive != null && !isActive.trim().isEmpty()) {
                isActiveValue = Boolean.parseBoolean(isActive);
                for (Map.Entry<String, SiddhiAppData> siddhiAppFileEntry : siddhiAppFileMap.entrySet()) {
                    SiddhiAppData siddiAppData = siddhiAppFileEntry.getValue();
                    if (isActiveValue = siddiAppData.isActive()) {
                        long age = (System.currentTimeMillis() - siddiAppData.getDeploymentTime());
                        SiddhiAppMetrics appMetrics = new SiddhiAppMetrics();
                        appMetrics.setAge(age);
                        appMetrics.appName(siddhiAppFileEntry.getKey());
                        appMetrics.isStatEnabled(siddiAppData.getSiddhiAppRuntime().isStatsEnabled());
                        appMetrics.status(siddiAppData.isActive() ?
                                SiddhiAppProcessorConstants.SIDDHI_APP_STATUS_ACTIVE :
                                SiddhiAppProcessorConstants.SIDDHI_APP_STATUS_INACTIVE);
                        siddhiAppMetricsList.add(appMetrics);
                    }
                }
            } else {
                for (Map.Entry<String, SiddhiAppData> siddhiAppFileEntry : siddhiAppFileMap.entrySet()) {
                    SiddhiAppData siddiAppData = siddhiAppFileEntry.getValue();
                    SiddhiAppMetrics appMetrics = new SiddhiAppMetrics();
                    if (siddiAppData.isActive()) {
                        long age = (System.currentTimeMillis() - siddiAppData.getDeploymentTime());
                        appMetrics.setAge(age);
                    } else {
                        appMetrics.setAge(0);
                    }
                    appMetrics.appName(siddhiAppFileEntry.getKey());
                    if (siddiAppData.isActive()) {
                        appMetrics.isStatEnabled(siddiAppData.getSiddhiAppRuntime().isStatsEnabled());
                    } else {
                        appMetrics.isStatEnabled(false);
                    }
                    appMetrics.status(siddiAppData.isActive() ?
                            SiddhiAppProcessorConstants.SIDDHI_APP_STATUS_ACTIVE :
                            SiddhiAppProcessorConstants.SIDDHI_APP_STATUS_INACTIVE);
                    siddhiAppMetricsList.add(appMetrics);
                }
            }
            return Response.ok().entity(siddhiAppMetricsList).build();
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                    "There are no any Siddhi App exist."));
            return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();
        }
    }

    public Response siddhiAppStatsEnable(String appFileName, boolean statsEnabled)
            throws NotFoundException {
        String jsonString;

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService()
                .getSiddhiAppMap();
        SiddhiAppData siddiAppData = siddhiAppMap.get(appFileName);
        if (siddiAppData != null) {
            if (statsEnabled && siddiAppData.getSiddhiAppRuntime().isStatsEnabled()) {
                log.info("Stats has already annabled for siddhi app :" + appFileName);
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                        "Stats has already annabled for siddhi app :" + appFileName));
            } else if (!(statsEnabled) && !(siddiAppData.getSiddhiAppRuntime().isStatsEnabled())) {
                log.info("Stats has already disabled for siddhi app :" + appFileName);
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                        "Stats has already disabled for siddhi app :" + appFileName));
            } else {
                siddiAppData.getSiddhiAppRuntime().enableStats(statsEnabled);
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                        "Sucessfully updated Aiddhi App : " + appFileName));
            }
            return Response.status(Response.Status.OK).entity(jsonString).build();
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                    "There is no Siddhi App exist " +
                            "with provided name : " + appFileName));
            return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();
        }
    }

    public Response siddhiAppsStatsEnable(boolean statsEnabled) throws NotFoundException {

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService()
                .getSiddhiAppMap();
        for (Map.Entry siddhiAppEntry : siddhiAppMap.entrySet()) {
            SiddhiAppData siddiAppData = (SiddhiAppData) siddhiAppEntry.getValue();
            if ((statsEnabled && !siddiAppData.getSiddhiAppRuntime().isStatsEnabled()) || (!statsEnabled &&
                    siddiAppData.getSiddhiAppRuntime().isStatsEnabled())) {
                siddiAppData.getSiddhiAppRuntime().enableStats(statsEnabled);
                if (log.isDebugEnabled()) {
                    log.info("Stats has been sucessfull updated for siddhi app :" + siddhiAppEntry.getKey());
                }
            }
        }
        String jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                "All siddhi apps Sucessfully updated."));
        return Response.status(Response.Status.OK).entity(jsonString).build();
    }

    public Response siddhiAppElementsGet(String appName) throws NotFoundException {
        String jsonString;
        Map<String, SiddhiAppData> siddhiAppDataMap = StreamProcessorDataHolder.getStreamProcessorService()
                .getSiddhiAppMap();
        if (siddhiAppDataMap.containsKey(appName)) {
            String siddhiAppString = siddhiAppDataMap.get(appName).getSiddhiApp();
            SiddhiApp siddhiApp = SiddhiCompiler.parse(String.valueOf(siddhiAppString));
            SiddhiAppRuntime siddhiAppRuntime = new SiddhiManager().createSiddhiAppRuntime(siddhiApp);
            List<SiddhiAppElements> listOfSiddhiAppElements = new ArrayList<>();
            for (int i = 0; i < siddhiApp.getExecutionElementList().size(); i++) {
                if (siddhiApp.getExecutionElementList().get(i) instanceof Query) {
                    for (String inputStreamId : (((Query) siddhiApp.getExecutionElementList().get(i)).getInputStream()
                            .getUniqueStreamIds())) {
                        SiddhiAppElements siddhiAppElements = new SiddhiAppElements();
                        siddhiAppElements.setInputStreamId(inputStreamId);
                        loadInputData(siddhiApp, siddhiAppRuntime, inputStreamId, siddhiAppString, siddhiAppElements);
                        String outPutStreamId = ((Query) siddhiApp.getExecutionElementList().get(i))
                                .getOutputStream().getId();
                        siddhiAppElements.setOutputStreamId(outPutStreamId);
                        loadOutputData(siddhiApp, siddhiAppRuntime, outPutStreamId, siddhiAppString, siddhiAppElements);
                        loadFunctionData(siddhiApp, ((Query) siddhiApp.getExecutionElementList().get(i)).getSelector
                                ().getSelectionList(), siddhiAppElements, siddhiAppString);
                        loadQueryName(siddhiApp.getExecutionElementList().get(i).getAnnotations(), siddhiAppElements);
                        Query query = (Query) siddhiApp.getExecutionElementList().get(i);
                        siddhiAppElements.setQuery(getDefinition(query, siddhiAppString));
                        listOfSiddhiAppElements.add(siddhiAppElements);
                    }
                } else if (siddhiApp.getExecutionElementList().get(i) instanceof Partition) {
                    List<Query> partitionStream = ((Partition) siddhiApp.getExecutionElementList().get(i))
                            .getQueryList();
                    for (Query query : partitionStream) {
                        for (String inputStreamId : query.getInputStream().getUniqueStreamIds()) {
                            SiddhiAppElements siddhiAppElements = new SiddhiAppElements();

                            siddhiAppElements.setInputStreamId(inputStreamId);
                            siddhiAppElements.setOutputStreamId(query.getOutputStream().getId());
                            siddhiAppElements.setPartitionQuery(getDefinition(query, siddhiAppString));
                            loadQueryName(siddhiApp.getExecutionElementList().get(i).getAnnotations(),
                                    siddhiAppElements);

                            loadInputData(siddhiApp, siddhiAppRuntime, inputStreamId, siddhiAppString,
                                    siddhiAppElements);
                            String outputStreamId = query.getOutputStream().getId();

                            loadOutputData(siddhiApp, siddhiAppRuntime, outputStreamId, siddhiAppString,
                                    siddhiAppElements);
                            loadFunctionData(siddhiApp, query.getSelector().getSelectionList(), siddhiAppElements,
                                    siddhiAppString);

                            for (PartitionType partitionType : ((Partition) siddhiApp.getExecutionElementList().get(i))
                                    .getPartitionTypeMap().values()) {
                                if (partitionType instanceof ValuePartitionType) {
                                    siddhiAppElements.setPartitionType(Constants.VALUE_PARTITION_TYPE);
                                    String partitionTypeDefinition = getDefinition(partitionType, siddhiAppString);
                                    siddhiAppElements.setPartitionTypeQuery(partitionTypeDefinition);
                                } else if (partitionType instanceof RangePartitionType) {
                                    siddhiAppElements.setPartitionType(Constants.RANGE_PARTITION_TYPE);
                                    String partitionTypeDefinition = getDefinition(partitionType, siddhiAppString);
                                    siddhiAppElements.setPartitionTypeQuery(partitionTypeDefinition);
                                } else {
                                    throw new IllegalArgumentException("An unidentified instance of the PartitionType" +
                                            " " + "Class was found");
                                }
                            }

                            Partition partitionQuery = (Partition) siddhiApp.getExecutionElementList().get(i);
                            siddhiAppElements.setQuery(getDefinition(partitionQuery, siddhiAppString));
                            listOfSiddhiAppElements.add(siddhiAppElements);
                        }
                    }
                }
            }

            loadSAggregarionData(siddhiApp, siddhiAppRuntime, listOfSiddhiAppElements, siddhiAppString);
            return Response.ok().entity(listOfSiddhiAppElements).build();
        }

        jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                "There is no Siddhi App exist " + "with provided name : " + appName));
        return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();
    }


    /**
     * Obtain the siddhi app and the type of input stream.
     */

    private void loadInputData(SiddhiApp siddhiApp, SiddhiAppRuntime siddhiAppRuntime, String inputStream, String
            appData, SiddhiAppElements siddhiAppElements) {
        Map<String, StreamDefinition> streamDefinitionMap = (siddhiAppRuntime.getStreamDefinitionMap());
        Map<String, TableDefinition> tableDefinitionMap = siddhiAppRuntime.getTableDefinitionMap();
        Map<String, TriggerDefinition> triggerDefinitionMap = siddhiApp.getTriggerDefinitionMap();
        Map<String, WindowDefinition> windowDefinitionMap = siddhiAppRuntime.getWindowDefinitionMap();
        for (Map.Entry<String, StreamDefinition> entry : streamDefinitionMap.entrySet()) {
            if (entry.getKey().equals(inputStream)) {
                siddhiAppElements.setInputStreamSiddhiApp(String.valueOf(entry.getValue()));
                siddhiAppElements.setInputStreamType(Constants.STREAM_TYPE);
                break;
            }
        }

        for (Map.Entry<String, TableDefinition> entry : tableDefinitionMap.entrySet()) {
            if (entry.getKey().equals(inputStream)) {
                siddhiAppElements.setInputStreamSiddhiApp(String.valueOf(entry.getValue()));
                siddhiAppElements.setInputStreamType(Constants.TABLE_TYPE);
                break;
            }
        }

        for (Map.Entry<String, TriggerDefinition> entry : triggerDefinitionMap.entrySet()) {
            if (entry.getKey().equals(inputStream)) {
                siddhiAppElements.setInputStreamSiddhiApp(getDefinition(entry.getValue(), appData));
                siddhiAppElements.setInputStreamType(Constants.TRIGGER_TYPE);
                break;
            }
        }

        for (Map.Entry<String, WindowDefinition> entry : windowDefinitionMap.entrySet()) {
            if (entry.getKey().equals(inputStream)) {
                siddhiAppElements.setInputStreamSiddhiApp(getDefinition(entry.getValue(), appData));
                siddhiAppElements.setInputStreamType(Constants.WINDOW_TYPE);
                break;
            }
        }
    }

    /**
     * Obtain the siddhi app and the type of output stream.
     */

    private void loadOutputData(SiddhiApp siddhiApp, SiddhiAppRuntime siddhiAppRuntime, String outputStream, String
            appData, SiddhiAppElements siddhiAppElements) {
        Map<String, StreamDefinition> streamDefinitionMap = (siddhiAppRuntime.getStreamDefinitionMap());
        Map<String, TableDefinition> tableDefinitionMap = siddhiAppRuntime.getTableDefinitionMap();
        Map<String, TriggerDefinition> triggerDefinitionMap = siddhiApp.getTriggerDefinitionMap();
        Map<String, WindowDefinition> windowDefinitionMap = siddhiAppRuntime.getWindowDefinitionMap();
        for (Map.Entry<String, StreamDefinition> entry : streamDefinitionMap.entrySet()) {
            if (entry.getKey().equals(outputStream)) {
                siddhiAppElements.setOutputStreamSiddhiApp(String.valueOf(entry.getValue()));
                siddhiAppElements.setOutputStreamType(Constants.STREAM_TYPE);
                break;
            }
        }

        for (Map.Entry<String, TableDefinition> entry : tableDefinitionMap.entrySet()) {
            if (entry.getKey().equals(outputStream)) {
                siddhiAppElements.setOutputStreamSiddhiApp(String.valueOf(entry.getValue()));
                siddhiAppElements.setOutputStreamType(Constants.TABLE_TYPE);
                break;
            }
        }

        for (Map.Entry<String, TriggerDefinition> entry : triggerDefinitionMap.entrySet()) {
            if (entry.getKey().equals(outputStream)) {
                siddhiAppElements.setOutputStreamSiddhiApp(getDefinition(entry.getValue(), appData));
                siddhiAppElements.setOutputStreamType(Constants.TRIGGER_TYPE);
                break;
            }
        }

        for (Map.Entry<String, WindowDefinition> entry : windowDefinitionMap.entrySet()) {
            if (entry.getKey().equals(outputStream)) {
                siddhiAppElements.setOutputStreamSiddhiApp(getDefinition(entry.getValue(), appData));
                siddhiAppElements.setOutputStreamType(Constants.WINDOW_TYPE);
                break;
            }
        }
    }

    /**
     * obtains information of all the user defined Functions.
     */

    private void loadFunctionData(SiddhiApp siddhiApp, List<OutputAttribute> functionList, SiddhiAppElements
            siddhiAppElements, String appData) {
        for (FunctionDefinition functionDefinition : siddhiApp.getFunctionDefinitionMap().values()) {
            for (OutputAttribute app : functionList) {

                if (app.getExpression() instanceof AttributeFunction) {
                    if (functionDefinition.getId().equals(((AttributeFunction) app.getExpression()).getName())) {
                        String functionDefinitionStr = getDefinition(functionDefinition, appData);
                        siddhiAppElements.setFunction(functionDefinition.getId());
                        siddhiAppElements.setFunctionQuery(functionDefinitionStr);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Obtains information of all the Aggregations.
     */

    private void loadSAggregarionData(SiddhiApp siddhiApp, SiddhiAppRuntime siddhiAppRuntime, List<SiddhiAppElements>
            streams, String appData) {
        for (AggregationDefinition aggregationDefinition : siddhiApp.getAggregationDefinitionMap().values()) {
            SiddhiAppElements siddhiAppElements = new SiddhiAppElements();
            siddhiAppElements.setInputStreamId(aggregationDefinition.getBasicSingleInputStream().getStreamId());
            siddhiAppElements.setOutputStreamId(aggregationDefinition.getId());
            loadInputData(siddhiApp, siddhiAppRuntime, aggregationDefinition.getBasicSingleInputStream()
                    .getStreamId(), appData, siddhiAppElements);
            String aggregationDefinitionStr = getDefinition(aggregationDefinition, appData);
            siddhiAppElements.setOutputStreamSiddhiApp(aggregationDefinitionStr);
            siddhiAppElements.setOutputStreamType(Constants.AGGREGATION);
            streams.add(siddhiAppElements);
        }
    }

    /**
     * Obtain query name of each siddhi app elements
     */
    private void loadQueryName(List<Annotation> queryAnnotations, SiddhiAppElements siddhiAppElements) {
        for (Annotation annotation : queryAnnotations) {
            for (Element element : annotation.getElements()) {
                siddhiAppElements.setQueryName(element.getValue());
            }
        }
        if (siddhiAppElements.getQueryName() == null) {
            siddhiAppElements.setQueryName(Constants.DEFAULT_QUERY_NAME);
        }
    }


    /**
     * Obtains the piece of the code from the siddhiAppString variable where the given SiddhiElement object is defined.
     *
     * @param siddhiElement The SiddhiElement object where the definition needs to be obtained from
     * @return The definition of the given SiddhiElement object as a String
     */
    private String getDefinition(SiddhiElement siddhiElement, String siddhiAppString) {
        int[] startIndex = siddhiElement.getQueryContextStartIndex();
        int[] endIndex = siddhiElement.getQueryContextEndIndex();

        int startLinePosition = ordinalIndexOf(startIndex[0], siddhiAppString);
        int endLinePosition = ordinalIndexOf(endIndex[0], siddhiAppString);

        return siddhiAppString.substring(startLinePosition + startIndex[1], endLinePosition + endIndex[1])
                .replaceAll("'", "\"");
    }


    /**
     * Gets the relative position in the siddhiAppString of the start of the given line number.
     *
     * @param lineNumber The line number in which the relative start position should be obtained
     * @return The relative position of where the given line starts in the siddhiAppString
     */
    private int ordinalIndexOf(int lineNumber, String siddhiAppString) {
        int position = 0;
        while (lineNumber >= 0) {
            lineNumber--;
            if (lineNumber <= 0) {
                break;
            }
            position = siddhiAppString.indexOf('\n', position) + 1;
        }
        return position;
    }


    @Override
    public Response siddhiAppsPost(String body, Request request) throws NotFoundException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to add Siddhi Apps")
                    .build();
        }
        return siddhiAppsPost(body);
    }

    @Override
    public Response siddhiAppsPut(String body, Request request) throws NotFoundException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to update Siddhi " +
                    "Apps").build();
        }
        return siddhiAppsPut(body);
    }

    @Override
    public Response siddhiAppsGet(String isActive, Request request) throws NotFoundException {

        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to list Siddhi Apps")
                    .build();
        }
        return siddhiAppsGet(isActive);
    }

    @Override
    public Response siddhiAppsAppNameDelete(String appFileName, Request request) throws NotFoundException {

        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to delete Siddhi " +
                    "Apps").build();
        }
        return siddhiAppsAppNameDelete(appFileName);

    }

    @Override
    public Response siddhiAppsAppNameGet(String appName, Request request) throws NotFoundException {

        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to get Siddhi App"
                    + appName).build();
        }
        return siddhiAppsAppNameGet(appName);
    }

    @Override
    public Response siddhiAppsAppNameStatusGet(String appFileName, Request request) throws NotFoundException {

        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to get status of " +
                    "the Siddhi App " + appFileName).build();
        }
        return siddhiAppsAppNameStatusGet(appFileName);
    }

    @Override
    public Response siddhiAppsAppNameBackupPost(String appName, Request request) throws NotFoundException {

        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to backup current " +
                    "state of the Siddhi App" + appName).build();
        }
        return siddhiAppsAppNameBackupPost(appName);

    }

    @Override
    public Response siddhiAppsAppNameRestorePost(String appName, String revision, Request request)
            throws NotFoundException {

        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to restore the " +
                    "Siddhi App" + appName).build();
        }
        return siddhiAppsAppNameRestorePost(appName, revision);
    }

    @Override
    public Response siddhiAppsStatisticsGet(String isActive, Request request) throws NotFoundException {

        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to get the stats of" +
                    " Siddhi Apps").build();
        }
        return siddhiAppsStatisticsGet(isActive);
    }

    @Override
    public Response siddhiAppStatsEnable(String appFileName, boolean statsEnabled, Request request)
            throws NotFoundException {

        if (getUserName(request) != null && !getPermissionProvider().hasPermission
                (getUserName(request), new Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to enable/disable " +
                    "stats for Siddhi App" + appFileName).build();
        }

        return siddhiAppStatsEnable(appFileName, statsEnabled);
    }

    @Override
    public Response siddhiAppsStatsEnable(boolean statsEnabled, Request request) throws NotFoundException {

        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to enable/disable " +
                    "stats for all Siddhi App").build();
        }
        return siddhiAppsStatsEnable(statsEnabled);
    }

    @Override
    public Response siddhiAppsElementsGet(String appName, Request request) throws NotFoundException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to enable/disable " +
                    "stats for all Siddhi App").build();
        }
        return siddhiAppElementsGet(appName);
    }

    private static String getUserName(Request request) {
        Object username = request.getProperty("username");
        return username != null ? username.toString() : null;
    }

    private PermissionProvider getPermissionProvider() {
        return StreamProcessorDataHolder.getPermissionProvider();
    }
}
