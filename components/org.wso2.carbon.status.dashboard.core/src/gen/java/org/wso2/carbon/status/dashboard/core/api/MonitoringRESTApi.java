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

package org.wso2.carbon.status.dashboard.core.api;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.analytics.msf4j.interceptor.common.util.InterceptorConstants;
import org.wso2.carbon.status.dashboard.core.model.Node;
import org.wso2.carbon.status.dashboard.core.model.StatsEnable;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import java.io.IOException;
import java.sql.SQLException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "the workers API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-11T07:55:11.886Z")
public class MonitoringRESTApi implements Microservice {
    private static final Log logger = LogFactory.getLog(MonitoringRESTApi.class);
    public static final String API_CONTEXT_PATH = "/apis/workers";

    private final MonitoringApiService workersApi;

    public MonitoringRESTApi(MonitoringApiService dashboardDataProvider) {
        this.workersApi = dashboardDataProvider;
    }

    /**
     * This API is responsible for adding the new worker to the status dashboard.
     *
     * @param worker user given worker details {HOST,PORT}
     * @return responce indicating that a given worker is added or not.
     * @throws NotFoundException
     */
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Add a new worker.", notes = "Adds a new worker.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Worker is creted successfully.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 409,
                    message = "Reqest accepted but a worker with the given host and port "
                            + "already exists.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response addWorker(@Context Request request,
                              @ApiParam(value = "Worker object that needs to be added.", required = true) Node worker
    )
            throws NotFoundException {
        return workersApi.addWorker(worker, getUserName(request));
    }

    @POST
    @Path("/manager")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Add a new manager.", notes = "Adds a new manager",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Manager is created successfully.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 409,
                    message = "Request accepted but a manager with the given host and "
                            + "port already exists.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response addManager(@Context Request request,
                               @ApiParam(value = "Manager object need to be added.", required = true) Node manager
    ) throws NotFoundException {
        return workersApi.addManager(manager, getUserName(request));
    }


    /**
     * This API is responsible of handling the test connection function at the adding the new worker.
     *
     * @param id worker ID
     * @return return the authentication for remote worker is sucess.
     * @throws NotFoundException
     */
    @POST
    @Path("/{id}/status")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Tests connection.", notes = "Tests the connection of a worker.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 407, message = "Proxy Authentication Required",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 408, message = "Request Timeout", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response testConnection(@Context Request request,
                                   @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
    ) throws NotFoundException {
        return workersApi.testConnection(id, getUserName(request));
    }

    /**
     * Get all real-time all worker details by reaching each worker nodes.
     *
     * @return Responce with all worker details.
     * @throws NotFoundException API not found exception.
     * @throws SQLException      throws when inserting worker general details of reachable nodes to WORKER GENERAL
     *                           DETAILS
     *                           DB.
     */
    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "List all workers.", notes = "Lists all registered workers.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getAllWorkers(@Context Request request)
            throws NotFoundException, SQLException {
        return workersApi.getAllWorkers(getUserName(request));
    }

    /**
     * Get all the single deployment siddhi apps
     *
     * @param request
     * @return
     * @throws NotFoundException
     * @throws SQLException
     */
    @GET
    @Path("/siddhi-apps/single-deployment-apps")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "List all the single Deployment siddhi applications that are " +
            "deployed in worker nodes.", notes
            = "Lists all siddhi applications.",
            response = void.class, tags = {"Nodes",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getSingleDeploymentSiddhiApps(@Context Request request)
            throws NotFoundException, SQLException {
        return workersApi.getSingleDeploymentSiddhiApps(getUserName(request));
    }

    @GET
    @Path("/siddhi-apps/ha-apps")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "List all the ha deployment siddhi applications that deployed in " +
            "worker nodes.", notes = "Lists all siddhi applications.",
            response = void.class, tags = {"Nodes",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getAllHASiddhiApps(@Context Request request)
            throws NotFoundException, SQLException {
        return workersApi.getHASiddhiApps(getUserName(request));
    }


    @GET
    @Path("/{id}/runTime")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Read the system runtime.", notes = "Lists all configuration.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getRuntime(@Context Request request,
                               @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id)
            throws NotFoundException {
        return workersApi.getRuntimeEnv(id, getUserName(request));
    }

    /**
     * List all the siddhi apps that are depoloyed in the active manager nodes
     * @param request
     * @return
     * @throws NotFoundException
     * @throws SQLException
     */
    @GET
    @Path("/manager/siddhi-apps")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "List all siddhi applications that deployed in manager nodes.",
            notes = "Lists all siddhi applications.",
            response = void.class, tags = {"Nodes",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getAllManagersSiddhiApps(@Context Request request)
            throws NotFoundException, SQLException {
        return workersApi.getAllManagersSiddhiApps(getUserName(request));
    }

    /**
     * Delete worker from the status dashboard. PS. Do not delete metrics details.
     *
     * @param id workerId
     * @return Response with delete state.
     * @throws NotFoundException API not found exception.
     * @throws SQLException      throws when Deleting worker configuration and general details from the database.
     */
    @DELETE
    @Path("/{id}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Deletes a worker.", notes = "Removes the worker with the worker " +
            "id specified. Path param of **id** determines id of the worker. ", response = ApiResponseMessage.class,
            tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The worker is successfully deleted.",
                    response = ApiResponseMessage.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker is not found.",
                    response = ApiResponseMessage.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = ApiResponseMessage.class)})
    public Response deleteWorker(@Context Request request,
                                 @ApiParam(value = "Id of the worker.", required = true) @PathParam("id") String id
    )
            throws NotFoundException, SQLException {
        return workersApi.deleteWorker(id, getUserName(request));
    }

    @DELETE
    @Path("/manager/{id}")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Deletes a manager.",
            notes = "Removes the manager with the manager Id specified. Path param "
                    + "**id** determines id of the manager. ",
            response = ApiResponseMessage.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The manager is successfully deleted.",
                    response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The manager is not found",
                    response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = ApiResponseMessage.class)})
    public Response deleteManager(@Context Request request,
                                  @ApiParam(value = "Id of the manager.", required = true) @PathParam("id") String id)
            throws NotFoundException, SQLException {
        return workersApi.deleteManager(id, getUserName(request));
    }

    /**
     * Reading the dashboard configuration details from the deploment YML of dashboard running server.
     *
     * @return return configuration details
     * @throws NotFoundException
     * @throws SQLException
     */
    @GET
    @Path("/config")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Read configuration details.", notes = "Lists all configuration.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getDashboardConfig(@Context Request request)
            throws NotFoundException, SQLException {
        return workersApi.getDashboardConfig(getUserName(request));
    }

    /**
     * Get user sysAdminRoles by username.
     *
     * @param username
     * @return User sysAdminRoles.
     * @throws NotFoundException
     */
    @GET
    @Path("/roles")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get user roles of a specified user", notes = "Lists sysAdminRoles " +
            "of a given user.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getRolesByUsername(@Context Request request,
                                       @ApiParam(value = "Id of the worker.", required = true)
                                       @QueryParam("permissionSuffix") String permissionSuffix
    ) throws NotFoundException {
        return workersApi.getRolesByUsername(getUserName(request), permissionSuffix);
    }

    /**
     * Read worker general details.
     *
     * @param id
     * @return
     * @throws NotFoundException
     */
    @POST
    @Path("/{id}/system-details")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get general details of a worker.",
            notes = "Retrieves the general details of worker with the specified id.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The worker is successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker specified is not found.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getWorkerGeneral(@Context Request request,
                                     @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
    )
            throws NotFoundException {
        return workersApi.populateWorkerGeneralDetails(id, getUserName(request));
    }

    /**
     * Fletch worker metrics values from the DB.
     *
     * @param id
     * @param period hr,min,sec
     * @param type   cpu,load,memory
     * @param more   display expanded list of metrics.
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("/{id}/history")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get history statistics details of a worker.",
            notes = "Retrieves the history statistics details of worker with the "
                    + "specified id.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getWorkerHistory(
            @Context Request request,
            @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "Time period to get history.") @QueryParam("period") String period
            , @ApiParam(value = "Required types to get statistics .") @QueryParam("type") String type
            , @ApiParam(value = "Is required more statistics.") @QueryParam("more") Boolean more
    )
            throws NotFoundException {
        return workersApi.getWorkerHistory(id, period, type, more, getUserName(request));
    }

    /**
     * Get all siddhi apps of withing the worker.
     *
     * @param id
     * @param period
     * @param type
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("/{id}/siddhi-apps")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get details of all Siddhi Apps of a given worker.",
            notes = "Retrieves the Siddhi App details of worker with the specified id.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getAllSiddhiApps(
            @Context Request request,
            @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "Time period to get history.") @QueryParam("period") String period
            , @ApiParam(value = "Page Number") @QueryParam("page") Integer pageNum
            , @ApiParam(value = "Required types to get statistics .") @QueryParam("type") String type
    )
            throws NotFoundException {
        return workersApi.getAllSiddhiApps(id, period, type, pageNum, getUserName(request));
    }

    /**
     * Get all HA Status.
     *
     * @param id
     * @param period
     * @param type
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("/{id}/ha-status")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get HA details of a given worker.", notes = "Retrieves ha details " +
            "of worker with the specified id.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getHAStatus(
            @Context Request request,
            @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
    )
            throws NotFoundException {
        return workersApi.getHADetails(id, getUserName(request));
    }

    /**
     * Get text view of the siddhi app.
     *
     * @param id
     * @param appName
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("/{id}/siddhi-apps/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get text view and flow of a siddhi-app.",
            notes = "Retrieves the general text view and flow of a siddhi-app",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getSiddhiAppDetails(
            @Context Request request,
            @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam("appName") String appName
    )
            throws NotFoundException {
        return workersApi.getSiddhiAppDetails(id, appName, getUserName(request));
    }

    /**
     * This will provide the enable and disable the remotely without redeploying the siddhi app.
     *
     * @param id
     * @param appName
     * @param statsEnable
     * @return
     * @throws NotFoundException
     */
    @PUT
    @Path("/{id}/siddhi-apps/{appName}/statistics")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Enable/disable Siddhi App statistics.",
            notes = "Enable or disable statistics of specified Siddhi App. ",
            response = ApiResponseMessage.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Statistics enabled successfully.",
                    response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Worker not found.",
                    response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = ApiResponseMessage.class)})
    public Response enableSiddhiAppStats(@Context Request request,
                                         @ApiParam(value = "ID of the worker.", required = true) @PathParam("id")
                                                 String id
            , @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam("appName") String appName
            , @ApiParam(value = "statsEnable", required = true) StatsEnable statsEnable
    )
            throws NotFoundException {
        return workersApi.enableSiddhiAppStats(id, appName, statsEnable, getUserName(request));
    }

    /**
     * Get the siddhi app metrics histro on a specify time interval or by defalt 5 min
     *
     * @param id
     * @param appName
     * @param period
     * @param type
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("/{id}/siddhi-apps/{appName}/history")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get history statistics details of a siddhi app.",
            notes = "Retrieves the history statistics details of siddhi app with the "
                    + "specified id.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getAppHistory(
            @Context Request request,
            @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam("appName") String appName
            , @ApiParam(value = "Time period to get history.") @QueryParam("period") String period
            , @ApiParam(value = "Required types to get statistics .") @QueryParam("type") String type
    )
            throws NotFoundException {
        return workersApi.getAppHistory(id, appName, period, type, getUserName(request));
    }

    /**
     * Get the component list and the component current merics.
     *
     * @param id
     * @param appName
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("/{id}/siddhi-apps/{appName}/components")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get listed components of a siddhi-app.",
            notes = "all comoneted and ytheir data of a siddhi-app", response = void.class,
            tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Componet successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The app or worker not found is not found.",
                    response = void.class)})
    public Response getSiddhiAppComponents(
            @Context Request request,
            @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam("appName") String appName
    )
            throws NotFoundException {
        return workersApi.getSiddhiAppComponents(id, appName, getUserName(request));
    }

    // TODO: 11/1/17 Should be implemented with flaw chart in next version

    /**
     * component history
     *
     * @param id
     * @param appName
     * @param componentId
     * @param period
     * @param type
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("/{id}/siddhi-apps/{appName}/components/{componentType}/{componentId}/history")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get history statistics details of a siddhi app component.",
            notes = "Retrieves the history statistics details of siddhi app component "
                    + "with the specified id.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getComponentHistory(
            @Context Request request,
            @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam("appName") String appName
            , @ApiParam(value = "ID of the siddhi app compnent type.", required = true) @PathParam("componentType")
                    String componentType
            , @ApiParam(value = "ID of the siddhi app compnent id.", required = true) @PathParam("componentId")
                    String componentId
            , @ApiParam(value = "Time period to get history.") @QueryParam("period") String period
            , @ApiParam(value = "Required types to get statistics .") @QueryParam("type") String type
    )
            throws NotFoundException {
        return workersApi
                .getComponentHistory(id, appName, componentType, componentId, period, type, getUserName(request));
    }

    @GET
    @Path("/manager/{id}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "List all the registered manager nodes.",
            notes = "List all the managers that are connected with the give manager node"
                    + " (HA mode).",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getAllManagers(@Context Request request,
                                   @ApiParam(value = "ID of the worker.", required = true) @PathParam("id")
                                           String id) throws NotFoundException {
        return workersApi.getManagerHADetails(id, getUserName(request));
    }

    @GET
    @Path("/manager")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "List all managers.", notes = "Lists all registered managers.",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),
            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getManagers(@Context Request request)
            throws NotFoundException, SQLException {
        return workersApi.getManagers(getUserName(request));
    }

    @GET
    @Path("/manager/{id}/siddhi-apps")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the summary details of all siddhi apps of a given manager.",
            notes = "Retrieves the siddhi app summary details of manager with the "
                    + "specified id.",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Summary successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Summary not found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getSiddhiApps(@Context Request request,
                                  @ApiParam(value = "ID of the worker.", required = true) @PathParam("id")
                                          String id) throws NotFoundException, IOException {
        return workersApi.getSiddhiApps(id, getUserName(request));
    }

    /**
     * Get text view of the siddhi app.
     *
     * @param id
     * @param appName
     * @return
     * @throws NotFoundException
     */
    @GET
    @Path("/manager/{id}/siddhi-apps/{appName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get text view and flow of a siddhi-app.",
            notes = "Retrieves the general text view and flow of a siddhi-app",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getManagerSiddhiAppTextView(
            @Context Request request,
            @ApiParam(value = "ID of the manager.", required = true) @PathParam("id") String id
            , @ApiParam(value = "Name of the siddhi app.", required = true) @PathParam("appName") String appName
    )
            throws NotFoundException {
        return workersApi.getManagerSiddhiAppTextView(id, appName, getUserName(request));
    }


    @GET
    @Path("/manager/{id}/siddhi-apps/{appName}/child-apps")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the child apps details.",
            notes = "Retrieves child app details of a parent siddhi-app",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getChildAppsDetails(
            @Context Request request,
            @ApiParam(value = "ID of the manager.", required = true) @PathParam("id") String id
            , @ApiParam(value = "Name of the siddhi app.", required = true) @PathParam("appName") String appName
    )
            throws NotFoundException, IOException {
        return workersApi.getChildAppsDetails(id, appName, getUserName(request));
    }

    @GET
    @Path("/manager/{id}/siddhi-apps/{appName}/child-apps/transport")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the transport details of each child apps",
            notes = "Retrieves the transport details of each child siddhi-apps",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.",
                    response = void.class)})
    public Response getChildAppsTransportDetails(
            @Context Request request,
            @ApiParam(value = "ID of the manager.", required = true) @PathParam("id") String id
            , @ApiParam(value = "Name of the siddhi app.", required = true) @PathParam("appName") String appName
    )
            throws NotFoundException, IOException {
        return workersApi.getChildAppsTransportDetails(id, appName, getUserName(request));
    }

    @GET
    @Path("/manager/{id}/clusteredResourceNodeDetails")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the resource cluster information of a given manager.",
            notes = "Retrieves the resoource cluster summary details of manager with the "
                    + "specified id.",
            response = void.class, tags = {"Managers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Summary successfully retrieved.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Summary not found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getClusterResourceNodeDetails(@Context Request request,
                                                  @ApiParam(value = "ID of the Node.", required = true) @PathParam("id")
                                                          String id) throws NotFoundException, IOException {
        return workersApi.getClusterResourceNodeDetails(id, getUserName(request));
    }

    @GET
    @Path("/{id}/siddhi-apps/{appName}/elements")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieve all the elements of particular siddhi apps",
            response = ApiResponseMessage.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully retrieved elements",
                    response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Worker not found.",
                    response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = ApiResponseMessage.class)})
    public Response getSiddhiAppElements(@Context Request request, @ApiParam(value = "ID of the worker.", required =
            true) @PathParam("id") String id, @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam
            ("appName") String appName) throws NotFoundException,IOException {
        return workersApi.getSiddhiAppElements(id, appName, getUserName(request));
    }


    private static String getUserName(Request request) {
        return request.getProperty(InterceptorConstants.PROPERTY_USERNAME).toString();
    }

}
