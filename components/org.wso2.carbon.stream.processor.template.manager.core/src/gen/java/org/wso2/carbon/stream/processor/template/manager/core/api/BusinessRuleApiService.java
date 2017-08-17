package org.wso2.carbon.stream.processor.template.manager.core.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-17T13:15:44.160Z")
public abstract class BusinessRuleApiService {
    public abstract Response createRule() throws NotFoundException;
    public abstract Response deleteRule(String ruleID
 ) throws NotFoundException;
    public abstract Response deployRule(String ruleID
 ) throws NotFoundException;
    public abstract Response editRule(String ruleID
 ) throws NotFoundException;
    public abstract Response forceDeleteRule(String ruleID
 ) throws NotFoundException;
    public abstract Response listRules() throws NotFoundException;
    public abstract Response retryToDeleteRule(String ruleID
 ) throws NotFoundException;
    public abstract Response retryToDeployRule(String ruleID
 ) throws NotFoundException;
}
