package org.wso2.carbon.event.simulator.core.api;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.event.simulator.core.exception.FileOperationsException;
import org.wso2.carbon.event.simulator.core.factories.FeedApiServiceFactory;
import org.wso2.carbon.event.simulator.core.model.InlineResponse200;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

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


@Component(
        name = "simulator-core-event-feed-services",
        service = Microservice.class,
        immediate = true
)
@Path("/simulation/feed")
@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "the feed API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-07-20T09:30:14.336Z")
public class FeedApi implements Microservice {
    private final FeedApiService delegate = FeedApiServiceFactory.getFeedApi();
    private static final Logger log = LoggerFactory.getLogger(FeedApi.class);

    @POST
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Upload feed simulation configuration to the system", notes = "",
            response = void.class, tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully uploaded simulation",
                    response = void.class)})
    public Response addFeedSimulation(
            @Context Request request,
            @ApiParam(value = "Simulation object which is need to be saved", required = true)
                    String body) throws NotFoundException {
        return delegate.addFeedSimulation(body, request);
    }

    @DELETE
    @Path("/{simulationName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Delete a simulation configuration by name",
            notes = "For valid response try integer IDs with positive integer value. "
                    + "Negative or non-integer values will generate API errors",
            response = void.class, tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully deleted simulation configuration",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "No event simulation configuration available "
                    + "under simulation name", response = void.class)})
    public Response deleteFeedSimulation(
            @Context Request request,
            @ApiParam(value = "Simulation name to delete the configuration.", required = true)
            @PathParam("simulationName") String simulationName) throws NotFoundException {
        return delegate.deleteFeedSimulation(simulationName, request);
    }

    @GET
    @Path("/{simulationName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieve a simulation configuration by name.", notes = "Some desc",
            response = String.class, tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully sent simulation configuratio    n.",
                    response = String.class),
            @io.swagger.annotations.ApiResponse(code = 404,
                    message = "No simulation configuration available under simulation name",
                    response = String.class)})
    public Response getFeedSimulation(
            @Context Request request,
            @ApiParam(value = "Simulation name to get the configuration.", required = true)
            @PathParam("simulationName") String simulationName) throws NotFoundException {
        return delegate.getFeedSimulation(simulationName, request);
    }

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieve all feed simulation configurations", notes = "Some desc.",
            response = String.class, tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully updated simulation configuration.",
                    response = String.class),
            @io.swagger.annotations.ApiResponse(code = 404, message = "No simulation configurations available.",
                    response = String.class)})
    public Response getFeedSimulations(@Context Request request) throws NotFoundException {
        return delegate.getFeedSimulations(request);
    }

    @POST
    @Path("/{simulationName}")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Operate a simulation configuration by name", notes = "some desc",
            response = void.class, tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message = "Successfully performed action on the feed simulation "
                            + "configuration",
                    response = void.class),
            @io.swagger.annotations.ApiResponse(code = 400,
                    message = "Invalid action specified for simulation. Actions supported"
                            + " are run, pause, resume, stop.",
                    response = void.class)})
    public Response operateFeedSimulation(
            @Context Request request,
            @ApiParam(value = "Action to be perform on the feed simulation eg: run, pause, resume, stop",
                    required = true)
            @QueryParam("action") String action,
            @ApiParam(value = "Simulation name to execute the action on the configuration.", required = true)
            @PathParam("simulationName") String simulationName)
            throws NotFoundException {
        return delegate.operateFeedSimulation(action, simulationName, request);
    }

    @PUT
    @Path("/{simulationName}")
    @Consumes({"text/plain"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Update an uploaded simulation configuration",
            notes = "Some description", response = InlineResponse200.class,
            tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully updated simulation configuration.",
                    response = InlineResponse200.class),
            @io.swagger.annotations.ApiResponse(code = 404,
                    message = "No event simulation configuration available under "
                            + "simulation name",
                    response = InlineResponse200.class)})
    public Response updateFeedSimulation(
            @Context Request request,
            @ApiParam(value = "Feed Simulation configuration name", required = true)
            @PathParam("simulationName") String simulationName,
            @ApiParam(value = "Simulation object which is need to be updated",
                    required = true) String body) throws NotFoundException, FileOperationsException {
        return delegate.updateFeedSimulation(simulationName, body, request);
    }

    @GET
    @Path("/{simulationName}/status")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retrieve a simulation configuration statusby name.",
            notes = "Some desc", response = String.class, tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully sent simulation status.",
                    response = String.class),

            @io.swagger.annotations.ApiResponse(code = 404,
                    message = "No simulation configuration available under simulation name",
                    response = String.class)})
    public Response getFeedSimulationStatus(
            @Context Request request,
            @ApiParam(value = "Simulation name to get the configuration.", required = true)
            @PathParam("simulationName") String simulationName)
            throws NotFoundException {
        return delegate.getFeedSimulationStatus(simulationName, request);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Feed Event Simulator service component is activated");
        }
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Feed Event Simulator service component is deactivated");
    }
}
