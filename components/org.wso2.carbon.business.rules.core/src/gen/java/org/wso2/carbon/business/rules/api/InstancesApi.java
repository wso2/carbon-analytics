package org.wso2.carbon.business.rules.api;

import io.swagger.annotations.ApiParam;

import org.wso2.carbon.business.rules.api.factories.InstancesApiServiceFactory;
import org.wso2.carbon.business.rules.model.BusinessRule;
import org.wso2.msf4j.formparam.FormDataParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.*;

@Path("/instances")


@io.swagger.annotations.Api(description = "the instances API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class InstancesApi  {
   private final InstancesApiService delegate = InstancesApiServiceFactory.getInstancesApi();

    @POST
    
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Creates a new business rule instance from a template.", notes = "", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "New business rule has been successfully created.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "Validation error occured.", response = void.class) })
    public Response createBusinessRuleInstance(@ApiParam(value = "Required parameter values for creating the business rule.", required=true)@FormDataParam("propertyValues")  String propertyValues
)
    throws NotFoundException {
        return delegate.createBusinessRuleInstance(propertyValues);
    }
    @GET
    
    
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Lists all existing business rule instances.", notes = "", response = BusinessRule.class, responseContainer = "List", tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully listed all available business rules.", response = BusinessRule.class, responseContainer = "List") })
    public Response listBusinessRules()
    throws NotFoundException {
        return delegate.listBusinessRules();
    }
}
