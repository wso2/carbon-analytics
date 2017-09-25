package org.wso2.carbon.business.rules.api.impl;

import org.wso2.carbon.business.rules.api.ApiResponseMessage;
import org.wso2.carbon.business.rules.api.BusinessRuleApiService;
import org.wso2.carbon.business.rules.api.NotFoundException;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-20T06:15:20.154Z")
public class BusinessRuleApiServiceImpl extends BusinessRuleApiService {
    @Override
    public Response deleteBusinessRuleInstance(String instanceUUID
, Boolean forceDelete
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response deployBusinessRuleInstance(String instanceUUID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response getDeploymentStatus(String instanceUUID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response loadBusinessRuleInstance(String instanceUUID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response updateBusinessRuleInstance(String instanceUUID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
