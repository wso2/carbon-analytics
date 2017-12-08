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
package org.wso2.carbon.stream.processor.statistics.impl;

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stream.processor.statistics.api.ApiResponseMessage;
import org.wso2.carbon.stream.processor.statistics.api.NotFoundException;
import org.wso2.carbon.stream.processor.statistics.api.SystemDetailsApiService;
import org.wso2.carbon.stream.processor.statistics.internal.WorkerGeneralDetails;

import javax.ws.rs.core.Response;

/**
 * API Implementation for getting the worker general details.
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-19T13:30:25.867Z")
public class SystemDetailsApiServiceImpl extends SystemDetailsApiService {
    private static final Log log = LogFactory.getLog(SystemDetailsApiServiceImpl.class);

    /**
     * Provide the worker general details.
     * @return Worker general details.
     * @throws NotFoundException thrown when API is not found.
     */
    @Override
    public Response systemDetailsGet() throws NotFoundException {
        WorkerGeneralDetails workerGeneralDetails = WorkerGeneralDetails.getInstance();
        String jsonString = new Gson().toJson(workerGeneralDetails);
        Response.Status status= Response.Status.OK;
        return Response.status(status).entity(jsonString).build();
    }
}
