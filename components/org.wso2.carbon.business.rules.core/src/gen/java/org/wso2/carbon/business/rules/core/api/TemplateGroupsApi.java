package org.wso2.carbon.business.rules.core.api;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.api.factories.TemplateGroupsApiServiceFactory;
import org.wso2.msf4j.Microservice;

import io.swagger.annotations.ApiParam;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component(
        name = "template-groups-api-services",
        service = Microservice.class,
        immediate = true
)

@Path("/template-groups")
@io.swagger.annotations.Api(description = "the template-groups API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class TemplateGroupsApi implements Microservice{
    private static final Logger log = LoggerFactory.getLogger(TemplateGroupsApi.class);
    private final TemplateGroupsApiService delegate = TemplateGroupsApiServiceFactory.getTemplateGroupsApi();

    @GET
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Lists all the existing template groups", notes = "", response = String.class, responseContainer = "List", tags={ "template-groups", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully listed existing template groups", response = String.class, responseContainer = "List") })
    public Response listRuleCollections()
            throws javax.ws.rs.NotFoundException, org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.listRuleCollections();
    }
    @GET
    @Path("/{templateGroupID}/templates")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Lists all the rule templates, existing under the given rule collection id.", notes = "", response = String.class, responseContainer = "List", tags={ "rule-templates", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successfully listed rule templates under given template group.", response = String.class, responseContainer = "List") })
    public Response listRuleTemplates(@ApiParam(value = "Name of the rule collection which is needed to be extracted.",required=true) @PathParam("templateGroupID") String templateGroupID
)
            throws javax.ws.rs.NotFoundException, org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.listRuleTemplates(templateGroupID);
    }
    @GET
    @Path("/{templateGroupID}/templates/{templateID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Lists templated fields of the given rule template", notes = "", response = void.class, tags={ "rule-templates", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successfully retrieved the templated fields of the given template.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "No such template exists.", response = void.class) })
    public Response loadRuleTemplateProperties(@ApiParam(value = "Name of the rule collection which is needed to be extracted.",required=true) @PathParam("templateGroupID") String templateGroupID
,@ApiParam(value = "Name of the rule template of which templated fields are required.",required=true) @PathParam("templateID") String templateID
)
            throws NotFoundException, org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.loadRuleTemplateProperties(templateGroupID,templateID);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        log.info("Business rules : template groups api service component is activated");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Business rules : template groups api service component is deactivated");
    }
}
