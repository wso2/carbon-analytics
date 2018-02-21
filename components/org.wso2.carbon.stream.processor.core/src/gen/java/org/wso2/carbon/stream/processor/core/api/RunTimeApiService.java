package org.wso2.carbon.stream.processor.core.api;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

/**
 * Auto generated class from Swagger to MSF4J.
 */

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-20T10:49:25.745Z")
public abstract class RunTimeApiService {
    public abstract Response getRunTime(Request request) throws NotFoundException;
}
