package org.wso2.carbon.stream.processor.statistic.api;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.msf4j.Microservice;
import org.wso2.carbon.stream.processor.statistic.factories.StatisticsApiServiceFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component(
        name = "SP-Status-Dashboard-Statistics-Service",
        service = Microservice.class,
        immediate = true
)
@Path("/statistics")
@io.swagger.annotations.Api(description = "the statistics API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T07:22:13.522Z")
public class StatisticsApi implements Microservice   {
    private static final Log log = LogFactory.getLog(StatisticsApi.class);
    private final StatisticsApiService delegate = StatisticsApiServiceFactory.getStatisticsApi();
    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        log.info("Start Statistics ***************************************************");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Stop Statistics ######################################");
    }
    @GET
    @io.swagger.annotations.ApiOperation(value = "List all workers.", notes = "Lists all registered workers.", response = void.class, tags={ "Workers", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class) })
    public Response statisticsGet()
            throws NotFoundException, org.wso2.carbon.stream.processor.statistic.api.NotFoundException {
        return delegate.statisticsGet();
    }
    @PUT
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Update an existing worker.", notes = "Updates the worker. ", response = ApiResponseMessage.class, tags={ "Workers", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully updated the worker.", response = ApiResponseMessage.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Worker not found.", response = ApiResponseMessage.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = ApiResponseMessage.class) })
    public Response updateWorker()
            throws NotFoundException, org.wso2.carbon.stream.processor.statistic.api.NotFoundException {
        return delegate.updateWorker();
    }
}
