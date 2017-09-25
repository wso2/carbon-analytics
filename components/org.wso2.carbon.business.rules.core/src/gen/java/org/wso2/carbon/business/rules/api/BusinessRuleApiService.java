package org.wso2.carbon.business.rules.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public abstract class BusinessRuleApiService {
    public abstract Response deleteBusinessRuleInstance(String instanceUUID
 ,Boolean forceDelete
 ) throws NotFoundException;
    public abstract Response deployBusinessRuleInstance(String instanceUUID
 ) throws NotFoundException;
    public abstract Response getDeploymentStatus(String instanceUUID
 ) throws NotFoundException;
    public abstract Response loadBusinessRuleInstance(String instanceUUID
 ) throws NotFoundException;
    public abstract Response updateBusinessRuleInstance(String instanceUUID
 ) throws NotFoundException;
}
