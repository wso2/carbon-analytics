package org.wso2.carbon.stream.processor.template.manager.core.api;

import org.wso2.carbon.stream.processor.template.manager.core.factories.BusinessRuleApiServiceFactory;
import org.wso2.carbon.stream.processor.template.manager.core.model.InlineResponse200;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;


@Path("/business-rule")


@io.swagger.annotations.Api(description = "the business-rule API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-17T13:15:44.160Z")
public class BusinessRuleApi  {
   private final BusinessRuleApiService delegate = BusinessRuleApiServiceFactory.getBusinessRuleApi();

    @POST
    @Path("/create")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Creates a new business rule.", notes = "", response = InlineResponse200.class, responseContainer = "List", tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = InlineResponse200.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = InlineResponse200.class, responseContainer = "List") })
    public Response createRule()
    throws NotFoundException {
        return delegate.createRule();
    }
    @DELETE
    @Path("/delete/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deletes a selected business rule.", notes = "", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = void.class) })
    public Response deleteRule(@ApiParam(value = "ID (name) of the rule which is to be deleted.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.deleteRule(ruleID);
    }
    @GET
    @Path("/deploy/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deploy the business rule.", notes = "", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = void.class) })
    public Response deployRule(@ApiParam(value = "ID (name) of the rule which is to be deployed.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.deployRule(ruleID);
    }
    @GET
    @Path("/edit/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Edit a selected business rule.", notes = "", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = void.class) })
    public Response editRule(@ApiParam(value = "ID (name) of the rule which is to be edited.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.editRule(ruleID);
    }
    @DELETE
    @Path("/force-delete/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Forcibly deletes a selected business rule.", notes = "", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = void.class) })
    public Response forceDeleteRule(@ApiParam(value = "ID (name) of the rule which is to be deleted.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.forceDeleteRule(ruleID);
    }
    @GET
    @Path("/list")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Loads a selected template.", notes = "", response = String.class, responseContainer = "List", tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = String.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = String.class, responseContainer = "List") })
    public Response listRules()
    throws NotFoundException {
        return delegate.listRules();
    }
    @DELETE
    @Path("/delete/retry/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retry to delete a selected business rule.", notes = "", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = void.class) })
    public Response retryToDeleteRule(@ApiParam(value = "ID (name) of the rule which is to be deleted.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.retryToDeleteRule(ruleID);
    }
    @GET
    @Path("/deploy/retry/{ruleID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Retry to deploy the business rule.", notes = "", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Invalid tag value", response = void.class) })
    public Response retryToDeployRule(@ApiParam(value = "ID (name) of the rule which is to be deployed.",required=true) @PathParam("ruleID") String ruleID
)
    throws NotFoundException {
        return delegate.retryToDeployRule(ruleID);
    }
}
