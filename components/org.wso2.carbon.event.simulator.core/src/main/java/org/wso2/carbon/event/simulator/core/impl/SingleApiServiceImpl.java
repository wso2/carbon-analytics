package org.wso2.carbon.event.simulator.core.impl;

import org.wso2.carbon.event.simulator.core.api.SingleApiService;
import org.wso2.carbon.event.simulator.core.exception.SimulationValidationException;
import org.wso2.carbon.event.simulator.core.internal.generator.SingleEventGenerator;
import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public class SingleApiServiceImpl extends SingleApiService {
    @Override
    public Response runSingleSimulation(String singleEventConfiguration) {
        try {
            SingleEventGenerator.sendEvent(singleEventConfiguration);
            return Response.ok().header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.OK, "Single Event simulation started successfully"))
                    .build();
        } catch (SimulationValidationException | ResourceNotFoundException e) {
            return Response.serverError().header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage())).build();
        }
    }
}
