/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.streaming.integrator.statistics.api;


import io.swagger.annotations.ApiParam;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.streaming.integrator.core.util.StatsEnable;
import org.wso2.carbon.streaming.integrator.statistics.factories.StatisticsApiServiceFactory;
import org.wso2.carbon.streaming.integrator.statistics.internal.OperatingSystemMetricSet;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Component(
        name = "org.wso2.carbon.streaming.integrator.statistics.api.StatisticsApi",
        service = Microservice.class,
        immediate = true
)
@Path("/statistics")
@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "The statistics API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T07:22:13.522Z")
public class StatisticsApi implements Microservice   {
    private static final Log log = LogFactory.getLog(StatisticsApi.class);
    private final StatisticsApiService delegate = StatisticsApiServiceFactory.getStatisticsApi();
    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        log.info(StatisticsApi.class.getName() + " service component has  started.");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.info(StatisticsApi.class.getName() + " service component has stop.");
    }

    /**
     * Get worker Realtime metrics wsing JMX reporting.
     * @return Responce including worker jvm realtime values.
     * @throws NotFoundException
     * @throws org.wso2.carbon.streaming.integrator.statistics.api.NotFoundException
     */
    @GET
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns real time worker details.", notes = "Returns real time worker details.", response = void.class, tags={ "Workers", })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found.", response = void.class),
        
        @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class) })
    public Response statisticsGet(
            @Context Request request
    )
            throws NotFoundException, org.wso2.carbon.streaming.integrator.statistics.api.NotFoundException {
        return delegate.statisticsGet(request);
    }

    /**
     * Dissable the stream processor worker metrics.
     * @param statsEnable
     * @return Resonce including state of update.
     * @throws NotFoundException
     * @throws org.wso2.carbon.streaming.integrator.statistics.api.NotFoundException
     */
    @PUT
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Enable/disable worker statistics",
            notes = "Enable/disable worker statistics", response = ApiResponseMessage.class, tags={ "Workers", })
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Successfully enabled/disabled worker statistics.", response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Worker not found.", response = ApiResponseMessage.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = ApiResponseMessage.class) })
    public Response enableStats(@Context Request request, @ApiParam(value = "statsEnable", required = true) StatsEnable statsEnable)
            throws NotFoundException, org.wso2.carbon.streaming.integrator.statistics.api.NotFoundException {
        return delegate.enableStats(statsEnable.getEnabledSiddhiStatLevel(),request);
    }

    @Reference(
            name = "org.wso2.carbon.streaming.integrator.statistics.internal.OperatingSystemMetricSet",
            service = OperatingSystemMetricSet.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterOperatingSystemMetricSet"
    )
    protected void registerOperatingSystemMetricSet(OperatingSystemMetricSet configServiceComponent){
        if (log.isDebugEnabled()) {
            log.debug("@Reference(bind) OperatingSystemMetricSet.");
        }
    }

    protected void unregisterOperatingSystemMetricSet(OperatingSystemMetricSet configServiceComponent){
        if (log.isDebugEnabled()) {
            log.debug("@Reference(unbind) OperatingSystemMetricSet.");
        }
    }
}
