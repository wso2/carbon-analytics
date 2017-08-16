package org.wso2.carbon.stream.processor.template.manager.core.api;

import io.swagger.api.*;
import io.swagger.model.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;


import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-16T07:42:06.523Z")
public abstract class TemplateApiService {
    public abstract Response listTemplates() throws NotFoundException;
    public abstract Response listTemplatesByType(String templateType
 ) throws NotFoundException;
}
