package org.wso2.carbon.stream.processor.auth.rest.api;


import io.swagger.annotations.ApiParam;

import org.wso2.carbon.stream.processor.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.RedirectionDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.UserDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.factories.LoginApiServiceFactory;

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
    name = "org.wso2.carbon.stream.processor.auth.rest.api.LoginApi",
    service = Microservice.class,
    immediate = true
)
@Path("/login")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@ApplicationPath("/login")
@io.swagger.annotations.Api(description = "the login API")
public class LoginApi implements Microservice  {
   private final LoginApiService delegate = LoginApiServiceFactory.getLoginApi();

    @POST
    @Path("/{appName}")
    @Consumes({ "application/x-www-form-urlencoded", "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Login Request to Stream Processor which returns user info, access and refresh token as cookies along with user name.", response = UserDTO.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Authorization Request Successful.", response = UserDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 302, message = "Redirection for OAuth2 authorization grant type.", response = UserDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid Authorization Header", response = UserDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = UserDTO.class) })
    public Response loginAppNamePost(@ApiParam(value = "AppName",required=true) @PathParam("appName") String appName
,@ApiParam(value = "")@FormDataParam("username")  String username
,@ApiParam(value = "")@FormDataParam("password")  String password
,@ApiParam(value = "")@FormDataParam("grantType")  String grantType
,@ApiParam(value = "", defaultValue="false")@FormDataParam("rememberMe")  Boolean rememberMe
 ,@Context Request request)
    throws NotFoundException {
        return delegate.loginAppNamePost(appName,username,password,grantType,rememberMe,request);
    }
    @GET
    @Path("/callback/{appName}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "", notes = "Login Request callback for Authorization Code grant type which returns user info, access and refresh token as cookies along with user name. .", response = UserDTO.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Authorization Request Successful.", response = UserDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 401, message = "Invalid Authorization Header", response = UserDTO.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = UserDTO.class) })
    public Response loginCallbackAppNameGet(@ApiParam(value = "AppName",required=true) @PathParam("appName") String appName
 ,@Context Request request)
    throws NotFoundException {
        return delegate.loginCallbackAppNameGet(appName,request);
    }
}
