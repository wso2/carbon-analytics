package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized FileAlreadyExistsException to customized HTTP responses
 */

@Component(
        name = "FileAlreadyExistsMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class FileAlreadyExistsMapper implements ExceptionMapper<FileAlreadyExistsException> {

    @Override
    public Response toResponse(FileAlreadyExistsException e) {
        return Response.status(Response.Status.CONFLICT)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.CONFLICT, e.getMessage()))
                .type("application/json")
                .build();
    }
}
