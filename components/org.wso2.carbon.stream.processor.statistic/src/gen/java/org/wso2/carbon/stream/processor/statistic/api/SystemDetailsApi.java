package org.wso2.carbon.stream.processor.statistic.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.msf4j.Microservice;
import org.wso2.carbon.stream.processor.statistic.factories.SystemDetailsApiServiceFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Component(
        name = "SP-Status-Dashboard-System-Details-Service",
        service = Microservice.class,
        immediate = true
)
@Path("/system-details")
@io.swagger.annotations.Api(description = "the system-details API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T05:58:14.415Z")
public class SystemDetailsApi implements Microservice {
    private static final Log log = LogFactory.getLog(SystemDetailsApi.class);
    private final SystemDetailsApiService delegate = SystemDetailsApiServiceFactory.getSystemDetailsApi();
    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        log.info("Start System Details ***************************************************");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info("Stop System Details ######################################");
    }
    @GET
    @io.swagger.annotations.ApiOperation(value = "The worker's system details.", notes = "Lists all worker's system details.", response = void.class, tags={ "Workers", })
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 401, message = "Unauthorized.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 408, message = "Server not reachable hence request timeout.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class) })
    public Response systemDetailsGet()
            throws NotFoundException {
        return delegate.systemDetailsGet();
    }
}
