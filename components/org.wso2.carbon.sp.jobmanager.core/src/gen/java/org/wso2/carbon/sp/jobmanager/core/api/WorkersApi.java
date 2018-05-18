/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.sp.jobmanager.core.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.sp.jobmanager.core.factories.WorkersApiServiceFactory;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Auto generated class from Swagger to MSF4J.
 */

@Component(
        service = Microservice.class,
        immediate = true
)

@Path("/resourceClusterWorkers")

@RequestInterceptor(AuthenticationInterceptor.class)
@io.swagger.annotations.Api(description = "the workers API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2018-02-03T14:53:27.713Z")
public class WorkersApi implements Microservice {
    private static final Log logger = LogFactory.getLog(WorkersApi.class);
    private final WorkersApiService workersApi = WorkersApiServiceFactory.getWorkersApi();

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the number worker nodes in the resource cluster.",
            notes = "Retrieve number of worker nodes in the resource cluster",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Worker node details retrieved successfully.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Worker node is not found.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getWorkers(@Context Request request)
            throws NotFoundException {
        return workersApi.getWorkers(request);
    }

    @Path("/clusteredWorkerNodeDetails")

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get the details of worker nodes in the resource cluster.",
            notes = "Retrieve details of worker nodes in the resource cluster",
            response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Worker node details retrieved successfully.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Worker node is not found.",
                    response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.",
                    response = void.class)})
    public Response getClusteredWorkerNodeDetails(@Context Request request)
            throws NotFoundException, IOException {
        return workersApi.getClusteredWorkerNodeDetails(request);
    }
}
