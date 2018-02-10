package org.wso2.carbon.sp.jobmanager.core.api;


import javax.ws.rs.core.Response;


@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-03T14:53:27.713Z")
public abstract class WorkersApiService {
    public abstract Response getWorkers() throws NotFoundException;
}
