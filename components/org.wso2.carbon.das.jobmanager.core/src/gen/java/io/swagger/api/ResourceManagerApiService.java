package io.swagger.api;

import javax.ws.rs.core.Response;

import io.swagger.model.Node;

@javax.annotation.Generated(
        value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-17T16:36:16.751Z"
)
public abstract class ResourceManagerApiService {
    public abstract Response getStatus() throws NotFoundException;

    public abstract Response updateHeartbeat(Node body
    ) throws NotFoundException;
}
