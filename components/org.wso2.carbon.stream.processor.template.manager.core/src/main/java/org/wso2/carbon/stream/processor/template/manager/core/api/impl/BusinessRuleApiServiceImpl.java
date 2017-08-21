package org.wso2.carbon.stream.processor.template.manager.core.api.impl;

import org.wso2.carbon.stream.processor.template.manager.core.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.template.manager.core.api.BusinessRuleApiService;
import org.wso2.carbon.stream.processor.template.manager.core.api.NotFoundException;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-08-21T12:49:26.982Z")
public class BusinessRuleApiServiceImpl extends BusinessRuleApiService {
    @Override
    public Response createRule(String templateID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response deleteRule(String ruleID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response deployRule(String ruleID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response forceDeleteRule(String ruleID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response listRules() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response loadRule(String ruleID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response retryToDeleteRule(String ruleID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response retryToDeployRule(String ruleID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response updateRule(String ruleID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
