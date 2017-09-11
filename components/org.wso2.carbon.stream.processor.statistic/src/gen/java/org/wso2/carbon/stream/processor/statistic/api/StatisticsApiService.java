package org.wso2.carbon.stream.processor.statistic.api;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T07:22:13.522Z")
public abstract class StatisticsApiService {
    public abstract Response statisticsGet() throws NotFoundException;
    public abstract Response updateWorker() throws NotFoundException;
}
