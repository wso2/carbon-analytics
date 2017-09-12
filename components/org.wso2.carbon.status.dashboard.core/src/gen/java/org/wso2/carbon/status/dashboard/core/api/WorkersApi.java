package org.wso2.carbon.status.dashboard.core.api;

import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.status.dashboard.core.factories.WorkersApiServiceFactory;
import org.wso2.carbon.status.dashboard.core.model.Worker;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Component(
        name = "stream-processor-status-dashboard-services",
        service = Microservice.class,
        immediate = true
)

@Path("/workers")


@io.swagger.annotations.Api(description = "the workers API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T07:55:11.886Z")
public class WorkersApi implements Microservice{
    private static final Log logger = LogFactory.getLog(WorkersApi.class);
    private final WorkersApiService delegate = WorkersApiServiceFactory.getWorkersApi();

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Add a new worker.", notes = "Adds a new worker.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201, message = "Worker is creted successfully.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Reqest accepted but a worker with the given host and port already exists.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class)})
    public Response addWorker(@ApiParam(value = "Worker object that needs to be added.", required = true) Worker worker
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.addWorker(worker);
    }

    @DELETE
    @Path("/{id}")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Deletes a worker.", notes = "Removes the worker with the worker id specified. Path param of **id** determines id of the worker. ", response = ApiResponseMessage.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The worker is successfully deleted.", response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker is not found.", response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = ApiResponseMessage.class)})
    public Response deleteWorker(@ApiParam(value = "Id of the worker.", required = true) @PathParam("id") String id
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.deleteWorker(id);
    }

    @GET
    @Path("/{id}/siddhi-apps/{appName}/history")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get history statistics details of a siddhi app.", notes = "Retrieves the history statistics details of siddhi app with the specified id.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.", response = void.class)})
    public Response getAppHistory(@ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam("appName") String appName
            , @ApiParam(value = "Time period to get history.") @QueryParam("period") String period
            , @ApiParam(value = "Required types to get statistics .") @QueryParam("type") String type
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.getAppHistory(id, appName, period, type);
    }

    @GET
    @Path("/{id}/siddhi-apps/{appName}/components/{componentId}/history")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get history statistics details of a siddhi app component.", notes = "Retrieves the history statistics details of siddhi app component with the specified id.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.", response = void.class)})
    public Response getComponetHistory(@ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam("appName") String appName
            , @ApiParam(value = "ID of the siddhi app compnent.", required = true) @PathParam("componentId") String componentId
            , @ApiParam(value = "Time period to get history.") @QueryParam("period") String period
            , @ApiParam(value = "Required types to get statistics .") @QueryParam("type") String type
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.getComponetHistory(id, appName, componentId, period, type);
    }

    @GET
    @Path("/{id}/siddhi-apps/{appName}")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get text view and flow of a siddhi-app.", notes = "Retrieves the general text view and flow of a siddhi-app", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.", response = void.class)})
    public Response getSiddhiAppDetails(@ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "ID of the siddhi app.", required = true) @PathParam("appName") String appName
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.getSiddhiAppDetails(id, appName);
    }

    @GET
    @Path("/{id}/siddhi-apps")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get general details of a worker.", notes = "Retrieves the general details of worker with the specified id.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.", response = void.class)})
    public Response getSiddhiApps(@ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.getSiddhiApps(id);
    }

    @GET
    @Path("/{id}")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get configuration details of a worker.", notes = "Retrieves the configuration details of worker with the specified id.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The worker is successfully retrieved.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker specified is not found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class)})
    public Response getWorkerConfig(@ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.getWorkerConfig(id);
    }

    @GET
    @Path("/{id}/system-details")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get general details of a worker.", notes = "Retrieves the general details of worker with the specified id.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "The worker is successfully retrieved.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker specified is not found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class)})
    public Response getWorkerGeneral(@ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.getWorkerGeneral(id);
    }

    @GET
    @Path("/{id}/history")

    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get history statistics details of a worker.", notes = "Retrieves the history statistics details of worker with the specified id.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "History successfully retrieved.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "The worker history is not found.", response = void.class)})
    public Response getWorkerHistory(@ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
            , @ApiParam(value = "Time period to get history.") @QueryParam("period") String period
            , @ApiParam(value = "Required types to get statistics .") @QueryParam("type") String type
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.getWorkerHistory(id, period, type);
    }

    @POST
    @Path("/{id}/status")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Tests connection.", notes = "Tests the connection of a worker.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Bad Request", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 407, message = "Proxy Authentication Required", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 408, message = "Request Timeout", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class)})
    public Response testConnection(@ApiParam(value = "Username and password to test connection.", required = true) Worker auth
            , @ApiParam(value = "ID of the worker.", required = true) @PathParam("id") String id
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.testConnection(auth, id);
    }

    @PUT
    @Path("/{id}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update an existing worker.", notes = "Updates the worker. ", response = ApiResponseMessage.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully updated the worker.", response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Worker not found.", response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = ApiResponseMessage.class)})
    public Response updateWorker(@ApiParam(value = "ID of worker that needs to be updated.", required = true) @PathParam("id") String id
    )
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.updateWorker(id);
    }

    @GET


    @io.swagger.annotations.ApiOperation(value = "List all workers.", notes = "Lists all registered workers.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class)})
    public Response workersGet()
            throws NotFoundException, org.wso2.carbon.status.dashboard.core.api.NotFoundException {
        return delegate.workersGet();
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        logger.info("***************************************************");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        logger.info("######################################");
    }
}
