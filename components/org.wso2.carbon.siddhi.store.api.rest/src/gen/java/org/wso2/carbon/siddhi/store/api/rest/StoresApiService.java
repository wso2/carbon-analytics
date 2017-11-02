package org.wso2.carbon.siddhi.store.api.rest;

import org.wso2.carbon.siddhi.store.api.rest.model.Query;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-11-01T11:26:25.925Z")
public abstract class StoresApiService {
    public abstract Response query(Query body
    ) throws NotFoundException;
}
