package org.wso2.carbon.stream.processor.template.manager.core.api.impl;

import org.wso2.carbon.stream.processor.template.manager.core.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.template.manager.core.api.NotFoundException;
import org.wso2.carbon.stream.processor.template.manager.core.api.TemplateApiService;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-21T12:49:26.982Z")
public class TemplateApiServiceImpl extends TemplateApiService {
    @Override
    public Response listAllTemplates() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response listTemplatesByType(String templateType
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response loadTemplate(String templateID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
