package org.wso2.carbon.business.rules.core.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-06T05:42:16.313Z")
public abstract class TemplateGroupsApiService {
    public abstract Response getRuleTemplate(String templateGroupID
 ,String ruleTemplateID
 ) throws NotFoundException;
    public abstract Response getRuleTemplates(String templateGroupID
 ) throws NotFoundException;
    public abstract Response getTemplateGroup(String templateGroupID
 ) throws NotFoundException;
    public abstract Response getTemplateGroups() throws NotFoundException;
}
