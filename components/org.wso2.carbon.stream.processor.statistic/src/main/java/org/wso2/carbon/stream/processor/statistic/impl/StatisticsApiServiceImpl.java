package org.wso2.carbon.stream.processor.statistic.impl;


import org.wso2.carbon.stream.processor.statistic.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.statistic.api.NotFoundException;
import org.wso2.carbon.stream.processor.statistic.api.StatisticsApiService;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T07:22:13.522Z")
public class StatisticsApiServiceImpl extends StatisticsApiService {
    @Override
    public Response statisticsGet() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response updateWorker() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
