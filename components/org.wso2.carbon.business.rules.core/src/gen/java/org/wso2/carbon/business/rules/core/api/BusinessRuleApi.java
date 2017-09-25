package org.wso2.carbon.business.rules.core.api;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.api.factories.BusinessRuleApiServiceFactory;
import org.wso2.carbon.business.rules.core.model.BusinessRule;
import org.wso2.carbon.business.rules.core.model.BusinessRuleDeploymentStatus;
import org.wso2.msf4j.Microservice;

import io.swagger.annotations.ApiParam;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Component(
        name = "business-rule-api-services",
        service = Microservice.class,
        immediate = true
)

@Path("/business-rule")
@io.swagger.annotations.Api(description = "the business-rule API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class BusinessRuleApi implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(BusinessRuleApi.class);
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
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
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
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
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
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
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
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
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
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.updateBusinessRuleInstance(instanceUUID);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        if(log.isDebugEnabled()) {
            log.info("Business rules :" + " business rule api service component is activated");
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
            log.info("Business rules : business rule api service component is deactivated");
        }
    }
}
