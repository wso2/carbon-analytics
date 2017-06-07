package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * customized exception class for when size of file uploaded exceeds the size limit
 */
@Component(
        name = "FileLimitExceededMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class FileLimitExceededMapper implements ExceptionMapper<FileLimitExceededException> {
    @Override
    public Response toResponse(FileLimitExceededException e) {
        return Response.status(Response.Status.FORBIDDEN)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.FORBIDDEN, e.getMessage()))
                .type("application/json")
                .build();
    }
}
