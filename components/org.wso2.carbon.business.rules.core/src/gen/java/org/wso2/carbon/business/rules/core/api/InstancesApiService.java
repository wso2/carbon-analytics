package org.wso2.carbon.business.rules.core.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-06T05:42:16.313Z")
public abstract class InstancesApiService {
    public abstract Response createBusinessRule(String businessRule
 ) throws NotFoundException;
    public abstract Response deleteBusinessRule(String businessRuleInstanceID
 ,Boolean forceDelete
 ) throws NotFoundException;
    public abstract Response getBusinessRules() throws NotFoundException;
    public abstract Response loadBusinessRule(String businessRuleInstanceID
 ) throws NotFoundException;
    public abstract Response updateBusinessRule(String businessRuleInstanceID
 ) throws NotFoundException;
}
