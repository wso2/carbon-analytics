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
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.permissions.PermissionManager;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.stream.processor.core.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.core.api.ApiResponseMessageWithCode;
import org.wso2.carbon.stream.processor.core.api.NotFoundException;
import org.wso2.carbon.stream.processor.core.api.SiddhiAppsApiService;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppData;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.stream.processor.core.model.SiddhiAppContent;
import org.wso2.carbon.stream.processor.core.model.SiddhiAppMetrics;
import org.wso2.carbon.stream.processor.core.model.SiddhiAppRevision;
import org.wso2.carbon.stream.processor.core.model.SiddhiAppStatus;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.util.snapshot.PersistenceReference;


import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


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

    @Override
    public Response siddhiAppsPost(String body, String username) throws NotFoundException {
        String jsonString;
        Response.Status status;
        if (!getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to add Siddhi Apps")
                    .build();
        }
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

    @Override
    public Response siddhiAppsPut(String body, String username) throws NotFoundException {
        String jsonString = new Gson().toString();
        Response.Status status = Response.Status.OK;
        if (!getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to update Siddhi " +
                    "Apps").build();
        }
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

    @Override
    public Response siddhiAppsGet(String isActive, String username) throws NotFoundException {
        String jsonString;
        boolean isActiveValue;

        if (!(getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider().hasPermission(username, new Permission
                (PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to list Siddhi Apps")
                    .build();
        }

        List<String> artifactList = new ArrayList<>();

        Map<String, SiddhiAppData> siddhiAppFileMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppMap();

        if (isActive != null && !isActive.trim().isEmpty()) {
            isActiveValue = Boolean.parseBoolean(isActive);
            for (Map.Entry<String, SiddhiAppData> siddhiAppFileEntry : siddhiAppFileMap.entrySet()) {
                if(isActiveValue == siddhiAppFileEntry.getValue().isActive()) {
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

    @Override
    public Response siddhiAppsAppNameDelete(String appFileName, String username) throws NotFoundException {
        String jsonString;
        Response.Status status = Response.Status.OK;

        if (!getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to delete Siddhi " +
                    "Apps").build();
        }

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

    @Override
    public Response siddhiAppsAppNameGet(String appName, String username) throws NotFoundException {

        String jsonString;

        if (!(getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider().hasPermission(username, new Permission
                (PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to get Siddhi App"
                    + appName).build();
        }

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

    @Override
    public Response siddhiAppsAppNameStatusGet(String appFileName, String username) throws NotFoundException {

        String jsonString;

        if (!(getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider().hasPermission(username, new Permission
                (PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to get status of " +
                    "the Siddhi App " + appFileName).build();
        }

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

    @Override
    public Response siddhiAppsAppNameBackupPost(String appName, String username) throws NotFoundException {
        String jsonString;
        Response.Status status = Response.Status.OK;

        if (!getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to backup current " +
                    "state of the Siddhi App" + appName).build();
        }

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

    @Override
    public Response siddhiAppsAppNameRestorePost(String appName, String revision, String username)
            throws NotFoundException {

        String jsonString;
        Response.Status status = Response.Status.OK;

        if (!getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to restore the " +
                    "Siddhi App" + appName).build();
        }

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

    @Override
    public Response siddhiAppsStatisticsGet(String isActive, String username) throws NotFoundException {
        String jsonString;
        boolean isActiveValue;

        if (!(getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider().hasPermission(username, new Permission
                (PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to get the stats of" +
                    " Siddhi Apps").build();
        }

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
                    if(siddiAppData.isActive()) {
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

    @Override
    public Response siddhiAppStatsEnable(String appFileName, boolean statsEnabled, String username)
            throws NotFoundException {
        String jsonString;

        if (!getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to enable/disable " +
                    "stats for Siddhi App" + appFileName).build();
        }

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().getSiddhiAppMap();
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

    @Override
    public Response siddhiAppsStatsEnable(boolean statsEnabled, String username) throws NotFoundException {

        if (!getPermissionProvider().hasPermission(username, new Permission(PERMISSION_APP_NAME,
                MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to enable/disable " +
                    "stats for all Siddhi App").build();
        }

        Map<String, SiddhiAppData> siddhiAppMap = StreamProcessorDataHolder.getStreamProcessorService().getSiddhiAppMap();
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

    private PermissionProvider getPermissionProvider(){
        return StreamProcessorDataHolder.getPermissionProvider();
    }
}
