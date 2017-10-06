package org.wso2.carbon.business.rules.core.api;

import io.swagger.annotations.ApiParam;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.api.factories.InstancesApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.formparam.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Component(
        name = "instances-api-services",
        service = Microservice.class,
        immediate = true
)

@Path("/instances")
@io.swagger.annotations.Api(description = "the instances API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-06T05:42:16.313Z")
public class InstancesApi implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(InstancesApi.class);
    private final InstancesApiService delegate = InstancesApiServiceFactory.getInstancesApi();

    @POST
    @Consumes({ "multipart/form-data" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Creates a business rule", notes = "Creates a business rule instance from template / from scratch from the given form data", response = Object.class, responseContainer = "List", tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Object.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 405, message = "Business rule validation exception", response = Object.class, responseContainer = "List") })
    public Response createBusinessRule(@ApiParam(value = "Required parameter values for creating the business rule", required=true)@FormDataParam("businessRule")  String businessRule
)
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.createBusinessRule(businessRule);
    }

    @DELETE
    @Path("/{businessRuleInstanceID}")
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Deletes a business rule", notes = "Deletes the business rule that has the given ID", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not found", response = void.class) })
    public Response deleteBusinessRule(@ApiParam(value = "ID of the business rule to be deleted",required=true) @PathParam("businessRuleInstanceID") String businessRuleInstanceID
,@ApiParam(value = "ID of the business rule to be deleted",required=true) @QueryParam("force-delete") Boolean forceDelete
)
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.deleteBusinessRule(businessRuleInstanceID,forceDelete);
    }

    @GET
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns list of business rule instances", notes = "Gets available list of business rule instances", response = Object.class, responseContainer = "List", tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Object.class, responseContainer = "List") })
    public Response getBusinessRules()
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.getBusinessRules();
    }

    @GET
    @Path("/{businessRuleInstanceID}")
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns a business rule instance", notes = "Gets a business rule instance that has the given ID", response = Object.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = Object.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not found", response = Object.class) })
    public Response loadBusinessRule(@ApiParam(value = "ID of the business rule to be loaded",required=true) @PathParam("businessRuleInstanceID") String businessRuleInstanceID
)
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.loadBusinessRule(businessRuleInstanceID);
    }

    @PUT
    @Path("/{businessRuleInstanceID}")
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Updates a business rule instance", notes = "Updates a business rule instance that has the given ID", response = void.class, tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "successful operation", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Business rule not foound", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 405, message = "Business rule validation exception", response = void.class) })
    public Response updateBusinessRule(@ApiParam(value = "ID of the business rule to be edited",required=true) @PathParam("businessRuleInstanceID") String businessRuleInstanceID
)
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.updateBusinessRule(businessRuleInstanceID);
    }

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        if(log.isDebugEnabled()) {
            log.info("Business rules : instances api service component is activated");
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
            log.info("Business rules : instances api service component is deactivated");
        }
    }
}
