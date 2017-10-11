package org.wso2.carbon.business.rules.core.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-11T05:39:16.839Z")
public abstract class BusinessRulesApiService {
    public abstract Response createBusinessRule(String businessRule
 ) throws NotFoundException;
    public abstract Response deleteBusinessRule(String businessRuleInstanceID
 ,Boolean forceDelete
 ) throws NotFoundException;
    public abstract Response getBusinessRules() throws NotFoundException;
    public abstract Response getRuleTemplate(String templateGroupID
 ,String ruleTemplateID
 ) throws NotFoundException;
    public abstract Response getRuleTemplates(String templateGroupID
 ) throws NotFoundException;
    public abstract Response getTemplateGroup(String templateGroupID
 ) throws NotFoundException;
    public abstract Response getTemplateGroups() throws NotFoundException;
    public abstract Response loadBusinessRule(String businessRuleInstanceID
 ) throws NotFoundException;
    public abstract Response updateBusinessRule(String businessRule
 ,String businessRuleInstanceID
 ) throws NotFoundException;
}
