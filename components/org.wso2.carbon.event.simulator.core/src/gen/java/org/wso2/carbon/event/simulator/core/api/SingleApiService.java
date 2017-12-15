package org.wso2.carbon.event.simulator.core.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public abstract class SingleApiService {
    public abstract Response runSingleSimulation(String body) throws NotFoundException;
}
