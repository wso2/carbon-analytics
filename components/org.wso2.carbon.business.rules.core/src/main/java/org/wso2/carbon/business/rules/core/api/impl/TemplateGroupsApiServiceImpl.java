package org.wso2.carbon.business.rules.core.api.impl;

import org.wso2.carbon.business.rules.core.api.ApiResponseMessage;
import org.wso2.carbon.business.rules.core.api.NotFoundException;
import org.wso2.carbon.business.rules.core.api.TemplateGroupsApiService;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-06T05:42:16.313Z")
public class TemplateGroupsApiServiceImpl extends TemplateGroupsApiService {
    @Override
    public Response getRuleTemplate(String templateGroupID
, String ruleTemplateID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response getRuleTemplates(String templateGroupID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response getTemplateGroup(String templateGroupID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response getTemplateGroups() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
