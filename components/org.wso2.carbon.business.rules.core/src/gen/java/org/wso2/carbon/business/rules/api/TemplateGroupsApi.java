package org.wso2.carbon.business.rules.api;

import org.wso2.carbon.business.rules.api.factories.TemplateGroupsApiServiceFactory;

import io.swagger.annotations.ApiParam;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/template-groups")


@io.swagger.annotations.Api(description = "the template-groups API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class TemplateGroupsApi  {
   private final TemplateGroupsApiService delegate = TemplateGroupsApiServiceFactory.getTemplateGroupsApi();

    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Lists all the existing template groups", notes = "", response = String.class, responseContainer = "List", tags={ "template-groups", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully listed existing template groups", response = String.class, responseContainer = "List") })
    public Response listRuleCollections()
            throws javax.ws.rs.NotFoundException, org.wso2.carbon.business.rules.api.NotFoundException {
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
            throws javax.ws.rs.NotFoundException, org.wso2.carbon.business.rules.api.NotFoundException {
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
            throws NotFoundException, org.wso2.carbon.business.rules.api.NotFoundException {
        return delegate.loadRuleTemplateProperties(templateGroupID,templateID);
    }
}
