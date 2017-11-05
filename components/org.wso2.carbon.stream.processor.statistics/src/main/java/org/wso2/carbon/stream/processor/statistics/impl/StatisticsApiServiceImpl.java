/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.stream.processor.statistics.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stream.processor.statistics.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.statistics.api.NotFoundException;
import org.wso2.carbon.stream.processor.statistics.api.StatisticsApiService;
import org.wso2.carbon.stream.processor.statistics.internal.OperatingSystemMetricSet;
import org.wso2.carbon.stream.processor.statistics.internal.exception.MetricsConfigException;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-19T09:20:55.612Z")
public class StatisticsApiServiceImpl extends StatisticsApiService {
    private static final Log log = LogFactory.getLog(StatisticsApiServiceImpl.class);
    private OperatingSystemMetricSet operatingSystemMetricSet = new OperatingSystemMetricSet();

    public StatisticsApiServiceImpl() {
        operatingSystemMetricSet.initConnection();
    }

    /**
     * This will provide the realtime metrics such as cpu,load average and memory from the worker.
     *
     * @return Metrics values.
     * @throws NotFoundException Api cannot be found.
     */
    @Override
    public Response statisticsGet() {
        Response.Status status;
        Gson gson = new Gson();
        try {
            String osMetricsJSON = gson.toJson(operatingSystemMetricSet.getMetrics());
            return Response.status(Response.Status.OK).entity(osMetricsJSON).build();
        } catch (Exception e) {
            String message = e.getMessage();
            if(("WSO2 Carbon metrics is not enabled.".equalsIgnoreCase(message)) || ("JMX reporter has been disabled at WSO2 carbon metrics.").equalsIgnoreCase(message)) {
                String osMetricsJSON = gson.toJson(operatingSystemMetricSet.getDefault());
                return Response.status(Response.Status.OK).entity(osMetricsJSON+"#"+message).build();
            } else {// possible only when merics reading
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(operatingSystemMetricSet.getDefault()).build();
            }
        }
    }

    /**
     * Enable and disable the worker metrics remotely.
     *
     * @param statsEnable Boolean value needed to disable or enable metrics.
     * @return Statistics state of the worker.
     * @throws NotFoundException API may not be found.
     */
    public Response enableStats(boolean statsEnable) throws NotFoundException {
        if (!statsEnable) {
            if (operatingSystemMetricSet.isEnableWorkerMetrics()) {
                operatingSystemMetricSet.disableWorkerMetrics();
                return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK,
                        "Sucessfully disabled the metrics.")).build();
            } else {
                return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK,
                        "Metrics are disabled already.")).build();
            }
        } else {
            if (operatingSystemMetricSet.isEnableWorkerMetrics()) {
                return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK,
                        "Metrics are enabled already.")).build();
            } else {
                operatingSystemMetricSet.enableWorkerMetrics();
                return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK,
                        "Successfully enabled the metrics.")).build();
            }
        }
    }
}