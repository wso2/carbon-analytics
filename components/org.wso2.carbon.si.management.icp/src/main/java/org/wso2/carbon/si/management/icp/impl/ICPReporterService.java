/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.si.management.icp.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.swagger.annotations.ApiParam;
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.si.management.icp.utils.Constants;
import org.wso2.carbon.si.management.icp.utils.HttpUtils;
import org.wso2.carbon.si.management.icp.utils.Utils;
import org.wso2.carbon.streaming.integrator.core.api.NotFoundException;
import org.wso2.carbon.streaming.integrator.core.api.SiddhiAppsApiService;
import org.wso2.carbon.streaming.integrator.core.factories.SiddhiAppsApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import java.io.IOException;
import java.nio.file.Files;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Component(
        name = "si-icp-services",
        service = Microservice.class,
        immediate = true
)
@Path("/management")
@RequestInterceptor(AuthenticationInterceptor.class)
public class ICPReporterService implements Microservice {

    private final SiddhiAppsApiService siddhiAppsApiService;

    public ICPReporterService() {
        this.siddhiAppsApiService = SiddhiAppsApiServiceFactory.getSiddhiAppsApi();
    }

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response serverInfo(@Context Request request) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(Constants.CARBON_HOME, System.getProperty(Constants.ENV_CARBON_HOME));
        jsonObject.put(Constants.JAVA_HOME, System.getProperty(Constants.ENV_JAVA_HOME));
        jsonObject.put(Constants.JAVA_VERSION, System.getProperty(Constants.ENV_JAVA_VERSION));
        jsonObject.put(Constants.JAVA_VENDOR, System.getProperty(Constants.ENV_JAVA_VENDOR));
        jsonObject.put(Constants.OS_NAME, System.getProperty(Constants.ENV_OS_NAME));
        jsonObject.put(Constants.OS_VERSION, System.getProperty(Constants.ENV_OS_VERSION));
        jsonObject.put(Constants.PRODUCT_NAME, Constants.WSO2_STREAMING_INTEGRATOR);
        return Response.ok().entity(jsonObject).build();
    }

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@Context Request request) {
        String accessToken = (String) request.getProperty(Constants.REQUEST_ACCESS_TOKEN);
        JsonObject response = new JsonObject();
        response.addProperty(Constants.ACCESS_TOKEN, accessToken);
        return Response.ok().entity(response).build();
    }

    @GET
    @Path("/siddhi-applications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSiddhiApplications(@Context Request request,
                                          @QueryParam("siddhiApp") String appName) {
        try {
            if (appName == null) {
                JsonArray jsonResponse =
                        HttpUtils.convertToJsonArray(siddhiAppsApiService.siddhiAppsStatisticsGet(null, request));
                return Response.ok(Utils.transformArtifacts(jsonResponse, ArtifactType.SIDDHI_APPS)).build();
            }

            JsonObject jsonResponse =
                    HttpUtils.convertToJsonObject(siddhiAppsApiService.siddhiAppsAppNameGet(appName, request));
            JsonObject response = new JsonObject();
            response.addProperty(Constants.CONFIGURATION, jsonResponse.get(Constants.CONTENT).getAsString());
            return Response.ok(response).build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/siddhi-applications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateSiddhiApplicationState(@Context Request request,
                                                 @ApiParam(value = "Siddhi Application", required = true)
                                                 JsonObject body) {
        String appName = body.get(Constants.NAME).getAsString();
        if (!body.has(Constants.ACTION)) {
            return Response.notModified().build();
        }
        String action = body.get(Constants.ACTION).getAsString();
        if (!action.equals(Constants.ACTIVATE) && !action.equals(Constants.DEACTIVATE)) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        JsonObject payload = new JsonObject();
        payload.addProperty(Constants.ACTION, action);
        try {
            Response response = siddhiAppsApiService.siddhiAppsSetState(appName, payload.toString(), request);
            if (response.getStatus() == 200) {
                return Response.ok().build();
            }
            return Response.serverError().build();
        } catch (NotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/sources")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSource(@Context Request request) {
        JsonArray jsonResponse = HttpUtils.convertToJsonArray(siddhiAppsApiService.siddhiAppsSourcesGet(request));
        return Response.ok(Utils.transformArtifacts(jsonResponse, ArtifactType.SOURCES)).build();
    }

    @GET
    @Path("/sinks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSink(@Context Request request) {
        JsonArray jsonResponse = HttpUtils.convertToJsonArray(siddhiAppsApiService.siddhiAppsSinksGet(request));
        return Response.ok(Utils.transformArtifacts(jsonResponse, ArtifactType.SINKS)).build();
    }

    @GET
    @Path("/queries")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getQuery(@Context Request request) {
        JsonArray jsonResponse = HttpUtils.convertToJsonArray(siddhiAppsApiService.siddhiAppsQueriesGet(request));
        return Response.ok(Utils.transformArtifacts(jsonResponse, ArtifactType.QUERIES)).build();
    }

    @GET
    @Path("/stores")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStores(@Context Request request) {
        JsonArray jsonResponse = HttpUtils.convertToJsonArray(siddhiAppsApiService.siddhiAppsTablesGet(request));
        return Response.ok(Utils.transformArtifacts(jsonResponse, ArtifactType.TABLES)).build();
    }

    @GET
    @Path("/windows")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWindow(@Context Request request) {
        JsonArray jsonResponse = HttpUtils.convertToJsonArray(siddhiAppsApiService.siddhiAppsWindowsGet(request));
        return Response.ok(Utils.transformArtifacts(jsonResponse, ArtifactType.WINDOWS)).build();
    }

    @GET
    @Path("/aggregations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAggregation(@Context Request request) {
        JsonArray jsonResponse = HttpUtils.convertToJsonArray(siddhiAppsApiService.siddhiAppsAggregationsGet(request));
        return Response.ok(Utils.transformArtifacts(jsonResponse, ArtifactType.AGGREGATIONS)).build();
    }

    @GET
    @Path("/logs")
    public Response getLogs(@Context Request request, @QueryParam("file") String fileName)
            throws IOException {
        if (fileName == null) {
            return Response.ok(Utils.getLogFileList()).build();
        }

        String carbonHome = System.getProperty(Constants.ENV_CARBON_HOME);
        if (carbonHome == null) {
            return Response.serverError().build();
        }
        return Response.ok(Files.newInputStream(Utils.getLogDirectoryPath(carbonHome).resolve(fileName)))
                .type(MediaType.TEXT_PLAIN).build();
    }

}
