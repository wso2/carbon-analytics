package org.wso2.carbon.stream.processor.template.manager.core.api;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.template.manager.core.factories.TemplateApiServiceFactory;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;

@Component(
        name = "template-manager-template-services",
        service = Microservice.class,
        immediate = true
)
@Path("/template")
@io.swagger.annotations.Api(description = "the template API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-17T13:15:44.160Z")
public class TemplateApi implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(TemplateApi.class);
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

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        log.info("Template manager template service is activated");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Template manager template service is deactivated");
    }
}
