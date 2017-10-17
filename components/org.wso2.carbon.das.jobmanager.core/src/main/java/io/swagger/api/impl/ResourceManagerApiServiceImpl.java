package io.swagger.api.impl;

import javax.ws.rs.core.Response;

import io.swagger.api.ApiResponseMessage;
import io.swagger.api.NotFoundException;
import io.swagger.api.ResourceManagerApiService;
import io.swagger.model.Node;

@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-17T16:36:16.751Z"
)
public class ResourceManagerApiServiceImpl extends ResourceManagerApiService {
    @Override
    public Response getStatus() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response updateHeartbeat(Node body
    ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
