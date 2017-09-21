package org.wso2.carbon.business.rules.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public abstract class InstancesApiService {
    public abstract Response createBusinessRuleInstance(String propertyValues
 ) throws NotFoundException;
    public abstract Response listBusinessRules() throws NotFoundException;
}
