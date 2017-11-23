/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.stream.processor.core.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.stream.processor.core.factories.HaApiServiceFactory;
import org.wso2.carbon.stream.processor.core.model.Error;
import org.wso2.carbon.stream.processor.core.model.HAStateSyncObject;
import org.wso2.carbon.stream.processor.core.model.OutputSyncTimestampCollection;
import org.osgi.service.component.annotations.Component;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component(
        name = "siddhi-ha-services",
        service = Microservice.class,
        immediate = true
)
@Path("/ha")
//@RequestInterceptor(AuthenticationInterceptor.class)
@Api(description = "APIs for HA Deployment")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-09-21T09:31:22.101Z")
public class HaApi implements Microservice {
    private final HaApiService delegate = HaApiServiceFactory.getHaApi();

    @GET
    @Path("/outputSyncTimestamps")
    @Produces({"application/json"})
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "The timestamps of last published events is successfully retrieved for each sink.",
                    response = OutputSyncTimestampCollection.class),

            @ApiResponse(
                    code = 404,
                    message = "The timestamps of last published events of each sink not retrieved.",
                    response = Error.class)})
    public Response haPublishedtsGet() throws NotFoundException {
        return delegate.haOutputSyncTimestampGet();
    }

    @GET
    @Path("/state")
    @Produces({"application/json"})
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "The state of all Siddhi applications is successfully retrieved.",
                    response = HAStateSyncObject.class),

            @ApiResponse(
                    code = 404,
                    message = "The state of all Siddhi applications is not successfully retrieved.",
                    response = Error.class)})
    public Response haStateGet() throws NotFoundException, IOException {
        return delegate.haStateGet();
    }

    @GET
    @Path("/state/{siddhiAppName}")
    @Produces({"application/json"})
    @ApiResponses(value = {
            @ApiResponse(
                    code = 200,
                    message = "The state of the requested Siddhi application is successfully retrieved.",
                    response = HAStateSyncObject.class),

            @ApiResponse(
                    code = 404,
                    message = "The state of the requested Siddhi application is not successfully retrieved.",
                    response = Error.class)})
    public Response haStateGet(@PathParam("siddhiAppName") String siddhiAppName) throws NotFoundException,
            IOException {
        return delegate.haStateGet(siddhiAppName);
    }
}
