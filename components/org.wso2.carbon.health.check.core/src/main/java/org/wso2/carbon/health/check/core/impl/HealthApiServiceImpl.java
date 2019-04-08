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

package org.wso2.carbon.health.check.core.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.health.check.core.api.HealthApiService;
import org.wso2.carbon.health.check.core.api.NotFoundException;
import org.wso2.carbon.health.check.core.model.ServerStatus;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2019-04-03T18:22:48.929Z")
public class HealthApiServiceImpl extends HealthApiService {

    private static final Logger log = LoggerFactory.getLogger(HealthApiServiceImpl.class);

    @Override
    public Response healthGet() throws NotFoundException {

        log.debug("Health check API call received : " + System.currentTimeMillis());
        ServerStatus serverStatus = new ServerStatus();
        serverStatus.setStatus("healthy");
        return Response.ok().entity(serverStatus).build();
    }
}
