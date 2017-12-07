/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */

package org.wso2.carbon.siddhi.store.api.rest.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.store.api.rest.NotFoundException;
import org.wso2.carbon.siddhi.store.api.rest.ApiResponseMessage;
import org.wso2.carbon.siddhi.store.api.rest.SiddhiStoreDataHolder;
import org.wso2.carbon.siddhi.store.api.rest.StoresApiService;
import org.wso2.carbon.siddhi.store.api.rest.model.ModelApiResponse;
import org.wso2.carbon.siddhi.store.api.rest.model.Query;
import org.wso2.carbon.siddhi.store.api.rest.model.Record;
import org.wso2.carbon.siddhi.store.api.rest.util.LogEncoder;
import org.wso2.carbon.stream.processor.core.SiddhiAppRuntimeService;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.event.Event;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-11-01T11:26:25.925Z")
public class StoresApiServiceImpl extends StoresApiService {

    private static final Logger log = LoggerFactory.getLogger(StoresApiServiceImpl.class);
    @Override
    public Response query(Query body) throws NotFoundException {
        if (body.getQuery() == null || body.getQuery().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Query cannot be empty or null")).build();
        }
        if (body.getAppName() == null || body.getAppName().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Siddhi app name cannot be empty or null")).build();
        }

        SiddhiAppRuntimeService siddhiAppRuntimeService =
                SiddhiStoreDataHolder.getInstance().getSiddhiAppRuntimeService();
        Map<String, SiddhiAppRuntime> siddhiAppRuntimes = siddhiAppRuntimeService.getActiveSiddhiAppRuntimes();
        SiddhiAppRuntime siddhiAppRuntime = siddhiAppRuntimes.get(body.getAppName());
        if (siddhiAppRuntime == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Cannot find an active SiddhiApp with name: " + body.getAppName())).build();
        } else {
            try {
                Event[] events = siddhiAppRuntime.query(body.getQuery());
                List<Record> records = getRecords(events);
                ModelApiResponse response = new ModelApiResponse();
                response.setRecords(records);
                return Response.ok().entity(response).build();
            } catch (Exception e) {
                log.error(LogEncoder.getEncodedString("Error while querying for siddhiApp: " + body.getAppName() +
                        ", with query: " + body.getQuery() + " Error: " + e.getMessage()), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                       "Cannot query: " + e.getMessage())).build();
            }
        }
    }

    private List<Record> getRecords(Event[] events) {
        List<Record> records = new ArrayList<>();
        if (events != null) {
            for (Event event : events) {
                Record record = new Record();
                record.addAll(Arrays.asList(event.getData()));
                records.add(record);
            }
        }
        return records;
    }
}
