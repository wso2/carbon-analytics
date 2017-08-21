package org.wso2.carbon.stream.processor.template.manager.core.api;

import org.wso2.carbon.stream.processor.template.manager.core.api.factories.BusinessRuleApiServiceFactory;
import org.wso2.carbon.stream.processor.template.manager.core.model.InlineResponse200;
import org.wso2.msf4j.formparam.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;

@Path("/business-rule")


@io.swagger.annotations.Api(description = "the business-rule API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-21T12:49:26.982Z")
public class BusinessRuleApi  {
   private final BusinessRuleApiService delegate = BusinessRuleApiServiceFactory.getBusinessRuleApi();

    @POST
    @Path("/create")
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Creates a new business rule.", notes = "", response = void.class, tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 405, message = "Business rule validation exception", response = void.class) })
    public Response createRule(@ApiParam(value = "Required parameter values for creating the business rule.", required=true)@FormDataParam("templateID")  String templateID
)
    throws NotFoundException {
        return delegate.createRule(templateID);
    }
    @DELETE
    @Path("/delete/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deletes a selected business rule.", notes = "", response = void.class, tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not found", response = void.class) })
    public Response deleteRule(@ApiParam(value = "ID (name) of the rule which is to be deleted.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.deleteRule(ruleID);
    }
    @GET
    @Path("/deploy/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deploys a selected business rule.", notes = "", response = void.class, tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not found", response = void.class) })
    public Response deployRule(@ApiParam(value = "ID (name) of the rule which is to be deployed.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.deployRule(ruleID);
    }
    @DELETE
    @Path("/force-delete/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Forcibly deletes a selected business rule.", notes = "", response = void.class, tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not found", response = void.class) })
    public Response forceDeleteRule(@ApiParam(value = "ID (name) of the rule which is to be deleted.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.forceDeleteRule(ruleID);
    }
    @GET
    @Path("/list")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "List all the existing business rules.", notes = "", response = String.class, responseContainer = "List", tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "List") })
    public Response listRules()
    throws NotFoundException {
        return delegate.listRules();
    }
    @GET
    @Path("/load/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Loads a selected business rule.", notes = "", response = InlineResponse200.class, tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = InlineResponse200.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not found", response = InlineResponse200.class) })
    public Response loadRule(@ApiParam(value = "ID (name) of the rule which is to be loaded.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.loadRule(ruleID);
    }
    @DELETE
    @Path("/delete/retry/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retries to delete a selected business rule.", notes = "", response = void.class, tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not found", response = void.class) })
    public Response retryToDeleteRule(@ApiParam(value = "ID (name) of the rule which is to be deleted.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.retryToDeleteRule(ruleID);
    }
    @GET
    @Path("/deploy/retry/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deploys a selected business rule.", notes = "", response = void.class, tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not found", response = void.class) })
    public Response retryToDeployRule(@ApiParam(value = "ID (name) of the rule which is to be deployed.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.retryToDeployRule(ruleID);
    }
    @PUT
    @Path("/update/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Updates a selected business rule.", notes = "", response = void.class, tags={ "business-rule", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not foound.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 405, message = "Business rule validation exception", response = void.class) })
    public Response updateRule(@ApiParam(value = "ID (name) of the rule which is to be edited/updated.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.updateRule(ruleID);
    }
}
