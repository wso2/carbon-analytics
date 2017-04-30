package org.wso2.carbon.event.simulator.core.exception;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.event.simulator.core.service.ResponseMapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized FileNotFoundException to customized HTTP responses
 */
@Component(
        name = "FileNotFoundMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class FileNotFoundMapper implements ExceptionMapper<FileNotFoundException> {

    @Override
    public Response toResponse(FileNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND).
                entity(new ResponseMapper(Response.Status.NOT_FOUND, e.getMessage())).
                type("application/json").
                build();
    }
}
