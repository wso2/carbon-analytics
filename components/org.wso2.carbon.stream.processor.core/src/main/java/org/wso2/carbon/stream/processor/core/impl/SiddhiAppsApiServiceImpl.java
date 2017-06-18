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
import org.wso2.carbon.stream.processor.core.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.core.api.NotFoundException;
import org.wso2.carbon.stream.processor.core.api.SiddhiAppsApiService;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppConfiguration;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppFile;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants;
import org.wso2.carbon.stream.processor.core.model.Artifact;
import org.wso2.carbon.stream.processor.core.model.ArtifactContent;
import org.wso2.siddhi.core.ExecutionPlanRuntime;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants.SIDDHIQL_DEPLOYMENT_DIRECTORY;
import static org.wso2.carbon.stream.processor.core.internal.util.SiddhiAppProcessorConstants.SIDDHIQL_FILES_DIRECTORY;

/**
 * Siddhi Service Implementataion Class
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-05-31T15:43:24.557Z")
public class SiddhiAppsApiServiceImpl extends SiddhiAppsApiService {

    private static final Logger log = LoggerFactory.getLogger(SiddhiAppsApiServiceImpl.class);

    @Override
    public Response siddhiAppsPost(String body) throws NotFoundException {
        String jsonString = new Gson().toString();
        Response.Status status = Response.Status.CREATED;
        try {
            String siddhiAppName = StreamProcessorDataHolder.
                    getStreamProcessorService().validateAndSave(body, false);
            if (siddhiAppName != null) {
                URI location = new URI(SIDDHIQL_DEPLOYMENT_DIRECTORY + File.separator + SIDDHIQL_FILES_DIRECTORY +
                        File.separator + siddhiAppName + SiddhiAppProcessorConstants.SIDDHIQL_FILE_EXTENSION);
                return Response.status(status).header(HttpHeaders.LOCATION, location).build();
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.CONFLICT,
                        "There is a Siddhi App already " +
                                "exists with same name"));
                status = Response.Status.CONFLICT;
            }

        } catch (SiddhiAppDeploymentException | URISyntaxException e) {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.INTERNAL_SERVER_ERROR,
                    e.getMessage()));
            status = Response.Status.INTERNAL_SERVER_ERROR;
        } catch (SiddhiAppConfigurationException e) {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.VALIDATION_ERROR,
                    e.getMessage()));
            status = Response.Status.BAD_REQUEST;
        }

        return Response.status(status).entity(jsonString).build();
    }

    @Override
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
                    return Response.status(status).build();
                } else {
                    URI location = new URI(SIDDHIQL_DEPLOYMENT_DIRECTORY + File.separator +
                            SIDDHIQL_FILES_DIRECTORY + File.separator + siddhiAppName +
                            SiddhiAppProcessorConstants.SIDDHIQL_FILE_EXTENSION);
                    return Response.created(location).header(HttpHeaders.LOCATION, location).build();
                }
            }
        } catch (SiddhiAppDeploymentException | URISyntaxException e) {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.INTERNAL_SERVER_ERROR,
                    e.getMessage()));
            status = Response.Status.INTERNAL_SERVER_ERROR;
        } catch (SiddhiAppConfigurationException e) {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.VALIDATION_ERROR,
                    e.getMessage()));
            status = Response.Status.BAD_REQUEST;
        }

        return Response.status(status).entity(jsonString).build();
    }

    @Override
    public Response siddhiAppsGet() throws NotFoundException {
        String jsonString = new Gson().toString();
        List<Artifact> artifactList = new ArrayList<>();

        Map<String, SiddhiAppFile> siddhiAppFileMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppFileMap();

        for (Map.Entry<String, SiddhiAppFile> siddhiAppFileEntry : siddhiAppFileMap.entrySet()) {
            Artifact artifact = new Artifact();
            artifact.setName(siddhiAppFileEntry.getKey());
            artifact.setIsActive(siddhiAppFileEntry.getValue().isActive());
            artifactList.add(artifact);
        }

        return Response.ok().entity(artifactList).build();
    }

    @Override
    public Response siddhiAppsAppFileNameDelete(String appFileName) throws NotFoundException {
        String jsonString = new Gson().toString();
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
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.VALIDATION_ERROR,
                    e.getMessage()));
            status = Response.Status.BAD_REQUEST;
        } catch (SiddhiAppDeploymentException e) {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.INTERNAL_SERVER_ERROR,
                    e.getMessage()));
            status = Response.Status.INTERNAL_SERVER_ERROR;
        }

        return Response.status(status).entity(jsonString).build();
    }

    @Override
    public Response siddhiAppsAppFileNameGet(String appFileName) throws NotFoundException {

        if (!appFileName.endsWith(SiddhiAppProcessorConstants.SIDDHIQL_FILE_EXTENSION)) {
            appFileName += SiddhiAppProcessorConstants.SIDDHIQL_FILE_EXTENSION;
        }

        String jsonString = new Gson().toString();
        Map<String, SiddhiAppFile> siddhiAppFileMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppFileMap();

        if (siddhiAppFileMap.containsKey(appFileName)) {
            SiddhiAppFile siddhiAppFile = siddhiAppFileMap.get(appFileName);
            ArtifactContent artifactContent = new ArtifactContent();
            artifactContent.setcontent(siddhiAppFile.getSiddhiApp());
            return Response.ok().entity(artifactContent).build();
        }

        jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                "There is no Siddhi App exist " +
                        "with provided name : " + appFileName));
        return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();

    }

    @Override
    public Response siddhiAppsAppFileNameStatusGet(String appFileName) throws NotFoundException {

        if (!appFileName.endsWith(SiddhiAppProcessorConstants.SIDDHIQL_FILE_EXTENSION)) {
            appFileName += SiddhiAppProcessorConstants.SIDDHIQL_FILE_EXTENSION;
        }

        String jsonString = new Gson().toString();
        Map<String, SiddhiAppFile> siddhiAppFileMap = StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppFileMap();

        if (siddhiAppFileMap.containsKey(appFileName)) {
            SiddhiAppFile siddhiAppFile = siddhiAppFileMap.get(appFileName);
            return siddhiAppFile.isActive() ? Response.ok().entity(SiddhiAppProcessorConstants.
                    SIDDHI_APP_STATUS_ACTIVE).build() : Response.ok().entity(SiddhiAppProcessorConstants.
                    SIDDHI_APP_STATUS_INACTIVE).build();
        }

        jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                "There is no Siddhi App exist " +
                        "with provided name : " + appFileName));
        return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();
    }


    @Override
    public Response siddhiAppsAppNameBackupPost(String appName) throws NotFoundException {
        String jsonString;
        Response.Status status = Response.Status.OK;
        try {
            ExecutionPlanRuntime executionPlanRuntime = StreamProcessorDataHolder.getSiddhiManager().
                    getExecutionPlanRuntime(appName);
            if (executionPlanRuntime != null) {
                executionPlanRuntime.persist();
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                        "State persisted for Siddhi App :" +
                                appName));
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.NOT_FOUND,
                        "There is no Siddhi App exist " +
                                "with provided name : " + appName));
                status = Response.Status.NOT_FOUND;
            }
        } catch (Exception e) {
            log.error("Exception occurred when backup the state for Siddhi App : " + appName, e);
            jsonString = new Gson().
                    toJson(new ApiResponseMessage(ApiResponseMessage.INTERNAL_SERVER_ERROR, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
        }

        return Response.status(status).entity(jsonString).build();
    }

    @Override
    public Response siddhiAppsAppNameRestorePost(String appName, String revision) throws NotFoundException {

        String jsonString;
        Response.Status status = Response.Status.OK;
        try {
            ExecutionPlanRuntime executionPlanRuntime = StreamProcessorDataHolder.getSiddhiManager().
                    getExecutionPlanRuntime(appName);
            if (executionPlanRuntime != null) {
                if (revision == null) {
                    executionPlanRuntime.restoreLastRevision();
                    jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.SUCCESS,
                            "State restored to last revision for Siddhi App :" +
                                    appName));
                } else {
                    executionPlanRuntime.restoreRevision(revision);
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
                    toJson(new ApiResponseMessage(ApiResponseMessage.INTERNAL_SERVER_ERROR, e.getMessage()));
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(jsonString).build();
        }

        return Response.status(status).entity(jsonString).build();
    }

}
