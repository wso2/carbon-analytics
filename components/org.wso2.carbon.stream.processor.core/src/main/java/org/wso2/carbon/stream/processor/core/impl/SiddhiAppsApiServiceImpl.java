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
import org.wso2.carbon.stream.processor.core.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.core.api.NotFoundException;
import org.wso2.carbon.stream.processor.core.api.SiddhiAppsApiService;
import org.wso2.carbon.stream.processor.core.internal.SiddhiAppConfiguration;
import org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder;
import org.wso2.carbon.stream.processor.core.internal.exception.SiddhiAppConfigurationException;
import org.wso2.carbon.stream.processor.core.model.Artifact;
import org.wso2.siddhi.core.ExecutionPlanRuntime;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Siddhi Service Implementataion Class
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-05-31T15:43:24.557Z")
public class SiddhiAppsApiServiceImpl extends SiddhiAppsApiService {

    @Override
    public Response siddhiAppsPost(String body) throws NotFoundException {
        String jsonString = new Gson().toString();
        Response.Status status = Response.Status.OK;
        try {
            if (StreamProcessorDataHolder.getStreamProcessorService().save(body, false)) {
                return Response.status(status).entity(jsonString).build();
            } else {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                        "There is a Siddhi App already " +
                                "exists with same name"));
                status = Response.Status.CONFLICT;
            }

        } catch (Exception e) {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()));
            status = Response.Status.BAD_REQUEST;
        }

        return Response.status(status).entity(jsonString).build();
    }

    @Override
    public Response siddhiAppsPut(String body) throws NotFoundException {
        String jsonString = new Gson().toString();
        Response.Status status = Response.Status.OK;
        try {
            if (StreamProcessorDataHolder.getStreamProcessorService().save(body, true)) {
                return Response.status(status).entity(jsonString).build();
            }
        } catch (Exception e) {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()));
            status = Response.Status.BAD_REQUEST;
        }

        return Response.status(status).entity(jsonString).build();
    }

    @Override
    public Response siddhiAppsGet() throws NotFoundException {
        List<String> artifactList = new ArrayList<>();
        for (SiddhiAppConfiguration siddhiAppConfiguration : StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppConfigurationMap().values()) {
            artifactList.add(siddhiAppConfiguration.getName());
        }

        return Response.ok().entity(artifactList).build();
    }

    @Override
    public Response siddhiAppsAppNameDelete(String appName) throws NotFoundException {
        String jsonString = new Gson().toString();
        Response.Status status = Response.Status.OK;
        if (appName != null) {
            try {
                if (StreamProcessorDataHolder.getStreamProcessorService().delete(appName)) {
                    return Response.status(status).entity(jsonString).build();
                } else {
                    jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                            "There is no Siddhi App exist " +
                                    "with provided name : " + appName));
                    status = Response.Status.NOT_FOUND;
                }
            } catch (SiddhiAppConfigurationException e) {
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                        e.getMessage()));
                status = Response.Status.INTERNAL_SERVER_ERROR;
            }
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                    "Invalid Request"));
            status = Response.Status.BAD_REQUEST;

        }
        return Response.status(status).entity(jsonString).build();
    }

    @Override
    public Response siddhiAppsAppNameGet(String appName) throws NotFoundException {

        for (SiddhiAppConfiguration siddhiAppConfiguration : StreamProcessorDataHolder.
                getStreamProcessorService().getSiddhiAppConfigurationMap().values()) {
            if (siddhiAppConfiguration.getName().equalsIgnoreCase(appName)) {
                Artifact artifact = new Artifact();
                artifact.setcontent(siddhiAppConfiguration.getSiddhiApp());
                return Response.ok().entity(artifact).build();
            }
        }
        String jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                "There is no Siddhi App exist " +
                        "with provided name : " + appName));
        return Response.status(Response.Status.NOT_FOUND).entity(jsonString).build();
    }

    @Override
    public Response siddhiAppsAppNameBackupPost(String appName) throws NotFoundException {
        String jsonString;
        Response.Status status = Response.Status.OK;
        ExecutionPlanRuntime executionPlanRuntime = StreamProcessorDataHolder.getSiddhiManager().
                getExecutionPlanRuntime(appName);
        if (executionPlanRuntime != null) {
            executionPlanRuntime.persist();
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.OK,
                    "State persisted for Siddhi App :" +
                            appName));
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                    "There is no Siddhi App exist " +
                            "with provided name : " + appName));
            status = Response.Status.NOT_FOUND;
        }

        return Response.status(status).entity(jsonString).build();
    }

    @Override
    public Response siddhiAppsAppNameRestorePost(String appName, String revision) throws NotFoundException {

        String jsonString;
        Response.Status status = Response.Status.OK;
        ExecutionPlanRuntime executionPlanRuntime = StreamProcessorDataHolder.getSiddhiManager().
                getExecutionPlanRuntime(appName);
        if (executionPlanRuntime != null) {
            if (revision == null) {
                executionPlanRuntime.restoreLastRevision();
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.OK,
                        "State restored to last revision for Siddhi App :" +
                                appName));
            } else {
                executionPlanRuntime.restoreRevision(revision);
                jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.OK,
                        "State restored to revision " + revision + " for Siddhi App :" +
                                appName));
            }
        } else {
            jsonString = new Gson().toJson(new ApiResponseMessage(ApiResponseMessage.ERROR,
                    "There is no Siddhi App exist " +
                            "with provided name : " + appName));
            status = Response.Status.NOT_FOUND;
        }

        return Response.status(status).entity(jsonString).build();
    }

}
