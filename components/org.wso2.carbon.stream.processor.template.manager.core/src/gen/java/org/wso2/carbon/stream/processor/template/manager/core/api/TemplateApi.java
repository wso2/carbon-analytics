package org.wso2.carbon.stream.processor.template.manager.core.api;

import org.wso2.carbon.stream.processor.template.manager.core.api.factories.TemplateApiServiceFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;

@Path("/template")


@io.swagger.annotations.Api(description = "the template API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-21T12:49:26.982Z")
public class TemplateApi  {
   private final TemplateApiService delegate = TemplateApiServiceFactory.getTemplateApi();

    @GET
    @Path("/list")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List all the existing templates.", notes = "", response = String.class, responseContainer = "List", tags={ "template", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "List") })
    public Response listAllTemplates()
    throws NotFoundException {
        return delegate.listAllTemplates();
    }
    @GET
    @Path("/list/{templateType}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List templates of selected type.", notes = "", response = void.class, tags={ "template", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid template type has been provided", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Templates of given type not found", response = void.class) })
    public Response listTemplatesByType(@ApiParam(value = "Type of the templates which are needed to be listed.",required=true) @PathParam("templateType") String templateType
)
    throws NotFoundException {
        return delegate.listTemplatesByType(templateType);
    }
    @GET
    @Path("/load/{templateID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Loads a selected template.", notes = "", response = void.class, tags={ "template", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid template id has been provided", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Template not found.", response = void.class) })
    public Response loadTemplate(@ApiParam(value = "ID (name) of the template which is to be loaded.",required=true) @PathParam("templateID") String templateID
)
    throws NotFoundException {
        return delegate.loadTemplate(templateID);
    }
}
