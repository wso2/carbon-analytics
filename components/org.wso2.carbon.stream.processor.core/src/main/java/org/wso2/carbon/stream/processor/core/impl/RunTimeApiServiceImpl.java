/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.stream.processor.core.impl;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.stream.processor.core.api.ApiResponseMessageWithCode;
import org.wso2.carbon.stream.processor.core.api.NotFoundException;
import org.wso2.carbon.stream.processor.core.api.RunTimeApiService;

import javax.ws.rs.core.Response;

/**
 * RunTime Service Implementataion Class that exposes the run time of the node.
 */

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2018-02-20T10:49:25.745Z")
@Component(service = RunTimeApiService.class, immediate = true)

public class RunTimeApiServiceImpl extends RunTimeApiService {

    @Override
    public Response getRunTime() throws NotFoundException {
        if (System.getProperty("wso2.runtime") != null) {
            return Response.ok().entity(System.getProperty("wso2.runtime")).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity(
                    new ApiResponseMessageWithCode(ApiResponseMessageWithCode.FILE_PROCESSING_ERROR,
                                                   "something went wrong please check")).build();
        }
    }
}
