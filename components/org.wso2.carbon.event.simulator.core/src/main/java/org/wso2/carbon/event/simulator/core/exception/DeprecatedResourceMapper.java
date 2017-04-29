package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized DeprecatedResourceException to customized HTTP responses
 */
@Component(
        name = "DeprecatedResourceMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class DeprecatedResourceMapper implements ExceptionMapper<EventGenerationException> {
    @Override
    public Response toResponse(EventGenerationException e) {
        return Response.status(Response.Status.NOT_FOUND).
                entity(new ResponseMapper(Response.Status.NOT_FOUND, e.getMessage())).
                type("application/json").
                build();
    }
}
