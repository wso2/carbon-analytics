package org.wso2.carbon.stream.processor.template.manager.core.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-17T13:15:44.160Z")
public abstract class TemplateApiService {
    public abstract Response listTemplates() throws NotFoundException;
    public abstract Response listTemplatesByType(String templateType
 ) throws NotFoundException;
}
