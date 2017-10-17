package org.wso2.carbon.das.jobmanager.core;

import org.wso2.carbon.das.jobmanager.core.factories.ResourceManagerApiServiceFactory;
import org.wso2.carbon.das.jobmanager.core.model.Deployment;
import org.wso2.carbon.das.jobmanager.core.model.ManagerNode;
import org.wso2.carbon.das.jobmanager.core.model.Node;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;

@Path("/resourceManager")
@io.swagger.annotations.Api(
        description = "the resourceManager API"
)
@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-17T16:36:16.751Z"
)
public class ResourceManagerApi implements Microservice {
    private final ResourceManagerApiService delegate = ResourceManagerApiServiceFactory.getResourceManagerApi();

    @GET
    @Path("/deployment")
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Get the satus.",
            notes = "Returns status of manager nodes cluster and the resourcepool.",
            response = Deployment.class, tags = {"ResourceManager",}
    )
    @io.swagger.annotations.ApiResponses(
            value = {
                    @io.swagger.annotations.ApiResponse(
                            code = 200,
                            message = "Successful operation",
                            response = Deployment.class
                    )
            }
    )
    public Response getStatus()
            throws NotFoundException {
        return delegate.getStatus();
    }

    @POST
    @Path("/heartbeat")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(
            value = "Register resource node and Updates Heartbeat.",
            notes = "Resource nodes will call this endpoint to get them self registerd in the resource pool. " +
                    "Once registered, consecetive calls for this endpoints will be used as heartbeat from tose " +
                    "resource nodes.",
            response = ManagerNode.class,
            tags = {"ResourceManager",}
    )
    @io.swagger.annotations.ApiResponses(
            value = {
                    @io.swagger.annotations.ApiResponse(
                            code = 200,
                            message = "Resource successfully added / Heartbeat updated.",
                            response = ManagerNode.class
                    ),

                    @io.swagger.annotations.ApiResponse(
                            code = 301,
                            message = "Not the current leader (redir to correct leader).",
                            response = ManagerNode.class
                    ),

                    @io.swagger.annotations.ApiResponse(
                            code = 405,
                            message = "Invalid input",
                            response = ManagerNode.class
                    )
            }
    )
    public Response updateHeartbeat(@ApiParam(value = "Node object that needs to be registered or update heartbeats" +
            " as the resource.", required = true) Node body) throws NotFoundException {
        return delegate.updateHeartbeat(body);
    }
}
