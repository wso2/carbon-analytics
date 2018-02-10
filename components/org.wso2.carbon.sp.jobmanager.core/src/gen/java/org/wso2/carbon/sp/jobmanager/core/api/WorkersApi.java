package org.wso2.carbon.sp.jobmanager.core.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.sp.jobmanager.core.factories.WorkersApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

//
@Component(
        name = "worker",
        service = Microservice.class,
        immediate = true
)

@Path("/workers")

@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "the workers API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-03T14:53:27.713Z")
public class WorkersApi implements Microservice {
    private static final Log logger = LogFactory.getLog(WorkersApi.class);
    private final WorkersApiService workersApi = WorkersApiServiceFactory.getWorkersApi();


    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the number worker nodes in the resource cluster.",
                                         notes = "Retrieve number of worker nodes in the resource cluster",
                                         response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Worker node details retrieved successfully.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Worker node is not found.",
                                                response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                                                response = void.class)})
    public Response getWorkers()
            throws NotFoundException {
        return workersApi.getWorkers();
    }
}
