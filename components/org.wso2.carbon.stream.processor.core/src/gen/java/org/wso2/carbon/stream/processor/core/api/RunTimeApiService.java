package org.wso2.carbon.stream.processor.core.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-20T10:49:25.745Z")
public abstract class RunTimeApiService {
    public abstract Response getRunTime(String host, String port) throws NotFoundException;
}
