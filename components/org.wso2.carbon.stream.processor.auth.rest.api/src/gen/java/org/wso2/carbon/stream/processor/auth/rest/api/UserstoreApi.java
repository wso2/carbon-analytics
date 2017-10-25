package org.wso2.carbon.stream.processor.auth.rest.api;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.stream.processor.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.GroupsListDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.factories.UserstoreApiServiceFactory;

import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.formparam.FormDataParam;
import org.osgi.service.component.annotations.Component;

import java.io.InputStream;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
    name = "org.wso2.carbon.stream.processor.auth.rest.api.UserstoreApi",
    service = Microservice.class,
    immediate = true
)
@Path("/userstore")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/userstore")
@io.swagger.annotations.Api(description = "the userstore API")
public class UserstoreApi implements Microservice  {
   private final UserstoreApiService delegate = UserstoreApiServiceFactory.getUserstoreApi();

    @GET
    @Path("/groups")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get all the groups in the user store.", response = GroupsListDTO.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Get all the groups in the user store Request Successful.", response = GroupsListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid Authorization Header", response = GroupsListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = GroupsListDTO.class) })
    public Response userstoreGroupsGet( @Context Request request)
    throws NotFoundException {
        return delegate.userstoreGroupsGet(request);
    }
    @GET
    @Path("/users/{name}/groups")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Get user's groups from the user store.", response = GroupsListDTO.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Get my groups from the user store Request Successful.", response = GroupsListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid Authorization Header", response = GroupsListDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = GroupsListDTO.class) })
    public Response userstoreUsersNameGroupsGet(@ApiParam(value = "ID of the user to get",required=true) @PathParam("name") String name
 ,@Context Request request)
    throws NotFoundException {
        return delegate.userstoreUsersNameGroupsGet(name,request);
    }
}
