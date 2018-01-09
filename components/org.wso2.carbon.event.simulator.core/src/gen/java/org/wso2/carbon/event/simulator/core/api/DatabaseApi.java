package org.wso2.carbon.event.simulator.core.api;

import io.swagger.annotations.ApiParam;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.analytics.msf4j.interceptor.common.util.InterceptorConstants;
import org.wso2.carbon.event.simulator.core.factories.DatabaseApiServiceFactory;
import org.wso2.carbon.event.simulator.core.model.*;


import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Component(
        name = "simulator-core-database-services",
        service = Microservice.class,
        immediate = true
)
@Path("/simulation/connectToDatabase")
@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "the connectToDatabase API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-07-20T09:30:14.336Z")
public class DatabaseApi implements Microservice {
    private final DatabaseApiService delegate = DatabaseApiServiceFactory.getConnectToDatabaseApi();
    private static final Logger log = LoggerFactory.getLogger(DatabaseApi.class);

    @POST
    @Path("/{tableName}/retrieveColumnNames")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retreive database table columns", notes = "", response = void.class,
            tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully retrieved the database tables",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Database Connection has been failed",
                    response = void.class)})
    public Response getDatabaseTableColumns(
            @Context Request request,
            @ApiParam(value = "Database connection parameters to get the database tables", required = true)
                    DBConnectionModel body
            ,
            @ApiParam(value = "Table name to get the columns", required = true) @PathParam("tableName") String tableName
    )
            throws javax.ws.rs.NotFoundException, NotFoundException {
        return delegate.getDatabaseTableColumns(body, tableName, request);
    }

    @POST
    @Path("/retrieveTableNames")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Retreive database tables", notes = "", response = void.class,
            tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully retrieved the database tables",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Database Connection has been failed",
                    response = void.class)})
    public Response getDatabaseTables(
            @Context Request request,
            @ApiParam(value = "Database connection parameters to get the database tables", required = true)
                    DBConnectionModel body
    )
            throws javax.ws.rs.NotFoundException, NotFoundException {
        return delegate.getDatabaseTables(body, request);
    }

    @POST

    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Test a database connection.", notes = "", response = void.class,
            tags = {"simulator",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully connected to the database",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 400, message = "Database Connection has been failed",
                    response = void.class)})
    public Response testDBConnection(
            @Context Request request,
            @ApiParam(value = "Database connection parameters to test the database connection", required = true)
                    DBConnectionModel body) throws NotFoundException {
        return delegate.testDBConnection(body, request);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        if (log.isDebugEnabled()) {
            log.debug("Event Simulator database service component is activated");
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
        log.info("Event Simulator database service component is deactivated");
    }
}
