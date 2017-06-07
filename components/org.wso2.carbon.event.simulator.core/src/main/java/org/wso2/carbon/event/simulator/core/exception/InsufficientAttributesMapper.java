package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized InsufficientAttributesException to customized HTTP responses
 */

@Component(
        name = "InsufficientAttributesMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class InsufficientAttributesMapper implements ExceptionMapper<InsufficientAttributesException> {
    @Override
    public Response toResponse(InsufficientAttributesException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.BAD_REQUEST, e.getMessage()))
                .type("application/json")
                .build();
    }
}
