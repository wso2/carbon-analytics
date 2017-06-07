package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized InvalidFileExceptions to customized HTTP responses
 */
@Component(
        name = "InvalidFileMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class InvalidFileMapper implements ExceptionMapper<InvalidFileException> {

    @Override
    public Response toResponse(InvalidFileException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.BAD_REQUEST, e.getMessage()))
                .type("application/json")
                .build();
    }
}
