package org.wso2.carbon.business.rules.core.api.impl;

import org.wso2.carbon.business.rules.core.api.ApiResponseMessage;
import org.wso2.carbon.business.rules.core.api.BusinessRuleApiService;
import org.wso2.carbon.business.rules.core.api.NotFoundException;
import org.wso2.carbon.business.rules.core.datasource.QueryExecutor;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.database.query.manager.QueryManager;

import java.sql.ResultSet;
import java.sql.SQLException;

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
        QueryManager queryManager = new QueryManager("business.rules");
        String q = queryManager.getQuery("RETRIEVE_BUSINESS_RULE");
        QueryExecutor queryExecutor = new QueryExecutor();
        try {
            ResultSet resultSet = queryExecutor.executeRetrieveBusinessRule("SmartHomePlan");
        } catch (BusinessRulesDatasourceException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response updateBusinessRuleInstance(String instanceUUID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
