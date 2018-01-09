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

package org.wso2.carbon.stream.processor.statistics.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.annotations.*;
import org.wso2.carbon.analytics.msf4j.interceptor.common.AuthenticationInterceptor;
import org.wso2.carbon.stream.processor.statistics.factories.SystemDetailsApiServiceFactory;
import org.wso2.carbon.stream.processor.statistics.internal.service.ConfigServiceComponent;
import org.wso2.msf4j.Microservice;
import org.wso2.msf4j.interceptor.annotation.RequestInterceptor;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/system-details")
@RequestInterceptor(AuthenticationInterceptor.class)
@Component(
        name = "org.wso2.carbon.stream.processor.statistic.api.SystemDetailsApi",
        service = Microservice.class,
        immediate = true
)
@io.swagger.annotations.Api(description = "the system-details API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-19T13:30:25.867Z")
public class SystemDetailsApi implements Microservice {
    private static final Log log = LogFactory.getLog(SystemDetailsApi.class);
    private final SystemDetailsApiService delegate = SystemDetailsApiServiceFactory.getSystemDetailsApi();

    /**
     * This is the activation method of ServiceComponent. This will be called when it's references are fulfilled
     *
     * @throws Exception this will be thrown if an issue occurs while executing the activate method
     */
    @Activate
    protected void start() throws Exception {
        log.debug("SystemDetailsApi has been activated.");
    }

    /**
     * This is the deactivation method of ServiceComponent. This will be called when this component
     * is being stopped or references are satisfied during runtime.
     *
     * @throws Exception this will be thrown if an issue occurs while executing the de-activate method
     */
    @Deactivate
    protected void stop() throws Exception {
        log.debug("SystemDetailsApi has been stop.");
    }

    @GET
    @io.swagger.annotations.ApiOperation(value = "The worker's system details.", notes = "Returns worker's system details.", response = void.class, tags = {"Workers",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK.", response = void.class),

            @io.swagger.annotations.ApiResponse(code = 500, message = "An unexpected error occured.", response = void.class)})
    public Response systemDetailsGet()
            throws NotFoundException, org.wso2.carbon.stream.processor.statistics.api.NotFoundException {
        return delegate.systemDetailsGet();
    }

    @Reference(
            name = "org.wso2.carbon.stream.processor.statistic.config.ConfigServiceComponent",
            service = ConfigServiceComponent.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterConfigServiceComponent"
    )
    protected void registerConfigServiceComponent(ConfigServiceComponent configServiceComponent) {
        if (log.isDebugEnabled()) {
            log.debug("@Reference(bind) ConfigServiceComponent");
        }
    }

    protected void unregisterConfigServiceComponent(ConfigServiceComponent configServiceComponent) {
        if (log.isDebugEnabled()) {
            log.debug("@Reference(unbind) ConfigServiceComponent");
        }
    }
}
