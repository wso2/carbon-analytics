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
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-06T05:42:16.313Z")
public class TemplateGroupsApi implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(TemplateGroupsApi.class);
    private final TemplateGroupsApiService delegate = TemplateGroupsApiServiceFactory.getTemplateGroupsApi();

    @GET
    @Path("/{templateGroupID}/templates/{ruleTemplateID}")
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns a rule template", notes = "Gets the rule template that has the given ID, which is available under the template group with the given ID", response = Object.class, tags={ "rule-templates", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Object.class) })
    public Response getRuleTemplate(@ApiParam(value = "ID of the template group",required=true) @PathParam("templateGroupID") String templateGroupID
,@ApiParam(value = "ID of the rule template",required=true) @PathParam("ruleTemplateID") String ruleTemplateID
)
            throws javax.ws.rs.NotFoundException, org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.getRuleTemplate(templateGroupID,ruleTemplateID);
    }

    @GET
    @Path("/{templateGroupID}/templates")
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns rule templates", notes = "Gets rule templates available under the template group with the given ID", response = Object.class, responseContainer = "List", tags={ "rule-templates", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Object.class, responseContainer = "List") })
    public Response getRuleTemplates(@ApiParam(value = "ID of the template group",required=true) @PathParam("templateGroupID") String templateGroupID)
            throws javax.ws.rs.NotFoundException, org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.getRuleTemplates(templateGroupID);
    }

    @GET
    @Path("/{templateGroupID}")
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns a template group", notes = "Gets template group that has the given ID", response = Object.class, tags={ "template-groups", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Object.class),
        @io.swagger.annotations.ApiResponse(code = 404, message = "Template group not found", response = Object.class) })
    public Response getTemplateGroup(@ApiParam(value = "ID of the template group",required=true) @PathParam("templateGroupID") String templateGroupID
) throws javax.ws.rs.NotFoundException, org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.getTemplateGroup(templateGroupID);
    }

    @GET
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns template groups", notes = "Gets available template groups", response = Object.class, responseContainer = "List", tags={ "template-groups", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Object.class, responseContainer = "List") })
    public Response getTemplateGroups()
            throws NotFoundException, org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.getTemplateGroups();
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        if(log.isDebugEnabled()) {
            log.info("Business rules : template groups api service component is activated");
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
        if(log.isDebugEnabled()) {
            log.info("Business rules : template groups api service component is deactivated");
        }
    }
}

