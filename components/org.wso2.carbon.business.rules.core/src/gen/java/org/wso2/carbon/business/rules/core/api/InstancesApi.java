package org.wso2.carbon.business.rules.core.api;

import io.swagger.annotations.ApiParam;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.api.factories.InstancesApiServiceFactory;
import org.wso2.carbon.business.rules.core.model.BusinessRule;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.formparam.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component(
        name = "instances-api-services",
        service = Microservice.class,
        immediate = true
)

@Path("/instances")
@io.swagger.annotations.Api(description = "the instances API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class InstancesApi implements Microservice {
    private static final Logger log = LoggerFactory.getLogger(InstancesApi.class);
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
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.createBusinessRuleInstance(propertyValues);
    }
    @GET
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Lists all existing business rule instances.", notes = "", response = BusinessRule.class, responseContainer = "List", tags={ "business-rules", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully listed all available business rules.", response = BusinessRule.class, responseContainer = "List") })
    public Response listBusinessRules()
    throws org.wso2.carbon.business.rules.core.api.NotFoundException {
        return delegate.listBusinessRules();
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
