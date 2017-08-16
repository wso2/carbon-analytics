package org.wso2.carbon.stream.processor.template.manager.core.api;

import io.swagger.api.*;
import io.swagger.model.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import io.swagger.model.InlineResponse200;

import java.util.List;
import io.swagger.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-16T07:42:06.523Z")
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
