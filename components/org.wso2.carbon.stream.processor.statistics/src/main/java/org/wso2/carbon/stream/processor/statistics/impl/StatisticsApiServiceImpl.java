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
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.stream.processor.statistics.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.statistics.api.NotFoundException;
import org.wso2.carbon.stream.processor.statistics.api.StatisticsApiService;
import org.wso2.carbon.stream.processor.statistics.bean.WorkerStatistics;
import org.wso2.carbon.stream.processor.statistics.internal.OperatingSystemMetricSet;
import org.wso2.carbon.stream.processor.statistics.internal.exception.MetricsConfigException;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

import static org.wso2.carbon.stream.processor.core.internal.StreamProcessorDataHolder.getPermissionProvider;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-19T09:20:55.612Z")
public class StatisticsApiServiceImpl extends StatisticsApiService {
    private static final Log log = LogFactory.getLog(StatisticsApiServiceImpl.class);
    private OperatingSystemMetricSet operatingSystemMetricSet = new OperatingSystemMetricSet();
    private static final String PERMISSION_APP_NAME = "SAPP";
    private static final String MANAGE_SIDDHI_APP_PERMISSION_STRING = "siddhiApp.manage";
    private static final String VIEW_SIDDHI_APP_PERMISSION_STRING = "siddhiApp.view";
    
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
    public Response statisticsGet(Request request) {
        Response.Status status;
        Gson gson = new Gson();
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, VIEW_SIDDHI_APP_PERMISSION_STRING)) || getPermissionProvider()
                .hasPermission(getUserName(request), new Permission(PERMISSION_APP_NAME,
                        MANAGE_SIDDHI_APP_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to get the stats of" +
                    " system statistics.").build();
        }
        try {
            String osMetricsJSON = gson.toJson(operatingSystemMetricSet.getMetrics());
            return Response.status(Response.Status.OK).entity(osMetricsJSON).build();
        } catch (MetricsConfigException e) {
            String message = e.getMessage();
            WorkerStatistics workerStatistics = operatingSystemMetricSet.getDefault();
            if (("Wso2 Carbon metrics is not enabled.".equalsIgnoreCase(message)) ||
                    ("JMX reporter has been disabled at WSO2 carbon metrics.").equalsIgnoreCase(message)) {
                workerStatistics.setMessage(message);
                String osMetricsJSON = gson.toJson(workerStatistics);
                return Response.status(Response.Status.OK).entity(osMetricsJSON).build();
            } else {// possible only when merics reading
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(workerStatistics).build();
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
    public Response enableStats(boolean statsEnable, Request request) throws NotFoundException {
        if (getUserName(request) != null && !getPermissionProvider().hasPermission(getUserName(request), new
                Permission(PERMISSION_APP_NAME, MANAGE_SIDDHI_APP_PERMISSION_STRING))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permissions to enable/disable " +
                    "stats for all node").build();
        }
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
    
    private static String getUserName(org.wso2.msf4j.Request request) {
        
        Object username = request.getProperty("username");
        return username != null ? username.toString() : null;
    }
}