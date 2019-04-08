/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.health.check.core.api;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.health.check.core.factories.HealthApiServiceFactory;
import org.wso2.carbon.health.check.core.model.ServerStatus;
import org.wso2.msf4j.Microservice;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Component(
        name = "stream-processor-health-check-api",
        service = Microservice.class,
        immediate = true
)

@Path("/health")
@io.swagger.annotations.Api(description = "the health API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2019-04-03T18:22:48.929Z")
public class HealthApi implements Microservice {
    private final HealthApiService delegate = HealthApiServiceFactory.getHealthApi();

    @GET
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Fetches the status of the Stream Processor.",
            notes = "Fetches the status of the Stream Processor. ", response = ServerStatus.class, tags = {"Artifact",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message = "The state of the Stream Processor is successfully retrieved.",
                    response = ServerStatus.class)})
    public Response healthGet()
            throws NotFoundException {
        return delegate.healthGet();
    }
}
