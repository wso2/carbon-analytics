package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized SimulatorInitializationException to customized HTTP responses
 */
@Component(
        name = "SimulatorInitializationMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class SimulatorInitializationMapper implements ExceptionMapper<SimulatorInitializationException> {

    @Override
    public Response toResponse(SimulatorInitializationException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                .type("application/json")
                .build();
    }
}

