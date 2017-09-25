package org.wso2.carbon.business.rules.api;

import org.wso2.carbon.business.rules.api.factories.BusinessRuleApiServiceFactory;
import org.wso2.carbon.business.rules.model.BusinessRule;
import org.wso2.carbon.business.rules.model.BusinessRuleDeploymentStatus;

import io.swagger.annotations.ApiParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/business-rule")


@io.swagger.annotations.Api(description = "the business-rule API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class BusinessRuleApi  {
   private final BusinessRuleApiService delegate = BusinessRuleApiServiceFactory.getBusinessRuleApi();

    @DELETE
    @Path("/instances/{instanceUUID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Un-deploys and deletes, or forcibly deletes (delete without undeploying) the given business rule instance.", notes = "", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Required business rule instance deleted successfully.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Failed to delete required business rule instance.", response = void.class) })
    public Response deleteBusinessRuleInstance(@ApiParam(value = "Name of the business rule which is to be deleted.",required=true) @PathParam("instanceUUID") String instanceUUID
,@ApiParam(value = "Informs whether given business rule instance should be forcibly deleted or not.", defaultValue="false") @DefaultValue("false") @QueryParam("force-delete") Boolean forceDelete
)
    throws NotFoundException {
        return delegate.deleteBusinessRuleInstance(instanceUUID,forceDelete);
    }
    @POST
    @Path("/instances/{instanceUUID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deploys the given business rule instance.", notes = "Deploys templates(derived siddhi apps) that belong to the given business rule instance.", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Business rule instance has been successfully deployed.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Failed to deploy business rule instance.", response = void.class) })
    public Response deployBusinessRuleInstance(@ApiParam(value = "UUID of the business rule instance which is to be deployed.",required=true) @PathParam("instanceUUID") String instanceUUID
)
    throws NotFoundException {
        return delegate.deployBusinessRuleInstance(instanceUUID);
    }
    @GET
    @Path("/instances/{instanceUUID}/deployment-status")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Gets the deployment status of the given business rule instance.", notes = "", response = BusinessRuleDeploymentStatus.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Deployment status of required businesss rule instance is received successfully.", response = BusinessRuleDeploymentStatus.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Required business rule instance is not found.", response = BusinessRuleDeploymentStatus.class) })
    public Response getDeploymentStatus(@ApiParam(value = "UUID of business rule instance of which deployment status is required.",required=true) @PathParam("instanceUUID") String instanceUUID
)
    throws NotFoundException {
        return delegate.getDeploymentStatus(instanceUUID);
    }
    @GET
    @Path("/instances/{instanceUUID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Loads the given business rule instance", notes = "Loads the business rule instance, with the given business rule instance’s ID", response = BusinessRule.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully loaded the requested business rule instance.", response = BusinessRule.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Requested business rule instance is not found", response = BusinessRule.class) })
    public Response loadBusinessRuleInstance(@ApiParam(value = "UUID of the business rule instance which is to be loaded.",required=true) @PathParam("instanceUUID") String instanceUUID
)
    throws NotFoundException {
        return delegate.loadBusinessRuleInstance(instanceUUID);
    }
    @PUT
    @Path("/instances/{instanceUUID}")
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Updates a given business rule.", notes = "Updates the given business rule instance, with newly filled form data", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully updated the business rule instance.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Failed to update the given business rule instance.", response = void.class) })
    public Response updateBusinessRuleInstance(@ApiParam(value = "UUID of the business rule instance which is to be updated.",required=true) @PathParam("instanceUUID") String instanceUUID
)
    throws NotFoundException {
        return delegate.updateBusinessRuleInstance(instanceUUID);
    }
}
