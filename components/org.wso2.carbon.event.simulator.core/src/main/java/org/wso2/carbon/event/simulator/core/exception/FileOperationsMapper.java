package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized FileOperationsException to customized HTTP responses
 */
@Component(
        name = "FileOperationsMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class FileOperationsMapper implements ExceptionMapper<FileOperationsException> {
    @Override
    public Response toResponse(FileOperationsException e) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                .type("application/json")
                .build();
    }
}
