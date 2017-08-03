package org.wso2.carbon.stream.processor.common.exception;

import org.osgi.service.component.annotations.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Exception mapper that maps customized ResourceNotFoundExceptions to customized HTTP responses
 */
@Component(
        name = "ResourceNotFoundMapper",
        service = ExceptionMapper.class,
        immediate = true
)
public class ResourceNotFoundMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Override
    public Response toResponse(ResourceNotFoundException e) {
        return Response.status(Response.Status.NOT_FOUND)
                .header("Access-Control-Allow-Origin", "*")
                .entity(new ResponseMapper(Response.Status.NOT_FOUND, e.getMessage()))
                .type("application/json")
                .build();
    }
}
