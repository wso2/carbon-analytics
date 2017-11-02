package org.wso2.carbon.siddhi.store.api.rest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.siddhi.store.api.rest.factories.StoresApiServiceFactory;
import org.wso2.carbon.siddhi.store.api.rest.model.ModelApiResponse;
import org.wso2.carbon.siddhi.store.api.rest.model.Query;
import org.wso2.carbon.stream.processor.common.SiddhiAppRuntimeService;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import io.swagger.annotations.ApiParam;

@Component(
        name = "siddhi-store-query-service",
        service = Microservice.class,
        immediate = true
)

@Path("/stores")
@io.swagger.annotations.Api(description = "The stores API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-11-01T11:26:25.925Z")
public class StoresApi implements Microservice {
    private final StoresApiService delegate = StoresApiServiceFactory.getStoresApi();

    @POST
    @Path("/query")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Submit a Siddhi query and get the result records from a store",
            notes = "", response = ModelApiResponse.class, tags = {"store",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK, query was successfully submitted",
                    response = ModelApiResponse.class),

            @io.swagger.annotations.ApiResponse(code = 405, message = "Invalid input",
                    response = ModelApiResponse
                            .class)})
    public Response query(@ApiParam(value = "Query object which contains the query which returns the store records",
            required = true) Query body)
            throws NotFoundException {
        return delegate.query(body);
    }

    @Reference(
            name = "siddhi.app.runtime.service",
            service = SiddhiAppRuntimeService.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetSiddhiAppRuntimeService"
    )
    protected void setSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
        SiddhiStoreDataHolder.getInstance().setSiddhiAppRuntimeService(siddhiAppRuntimeService);
    }

    protected void unsetSiddhiAppRuntimeService(SiddhiAppRuntimeService siddhiAppRuntimeService) {
        SiddhiStoreDataHolder.getInstance().setSiddhiAppRuntimeService(null);
    }
}
