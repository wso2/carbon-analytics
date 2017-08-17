package org.wso2.carbon.stream.processor.template.manager.core.api;

import org.wso2.carbon.stream.processor.template.manager.core.factories.TemplateApiServiceFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;

@Path("/template")


@io.swagger.annotations.Api(description = "the template API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-17T13:15:44.160Z")
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
