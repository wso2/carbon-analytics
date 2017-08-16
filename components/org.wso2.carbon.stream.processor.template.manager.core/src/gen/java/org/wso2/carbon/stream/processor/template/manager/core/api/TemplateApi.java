package org.wso2.carbon.stream.processor.template.manager.core.api;

import io.swagger.model.*;
import io.swagger.api.TemplateApiService;
import io.swagger.api.factories.TemplateApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;


import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;

@Path("/template")


@io.swagger.annotations.Api(description = "the template API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-16T07:42:06.523Z")
public class TemplateApi  {
   private final TemplateApiService delegate = TemplateApiServiceFactory.getTemplateApi();

    @GET
    @Path("/list")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List existing templates.", notes = "", response = String.class, responseContainer = "List", tags={ "templates", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid status value", response = String.class, responseContainer = "List") })
    public Response listTemplates()
    throws NotFoundException {
        return delegate.listTemplates();
    }
    @GET
    @Path("/{templateType}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List existing templates.", notes = "List existing templates filtered by their type.", response = void.class, tags={ "templates", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = void.class) })
    public Response listTemplatesByType(@ApiParam(value = "Type of templates which are needed to be listed.",required=true) @PathParam("templateType") String templateType
)
    throws NotFoundException {
        return delegate.listTemplatesByType(templateType);
    }
}
