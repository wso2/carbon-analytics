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

import io.siddhi.core.SiddhiAppRuntimeImpl;
import io.siddhi.core.aggregation.AggregationRuntime;
import io.siddhi.core.event.ComplexEvent;
import io.siddhi.core.event.ComplexEventChunk;
import io.siddhi.core.event.stream.StreamEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.store.api.rest.NotFoundException;
import org.wso2.carbon.siddhi.store.api.rest.ApiResponseMessage;
import org.wso2.carbon.siddhi.store.api.rest.SiddhiStoreDataHolder;
import org.wso2.carbon.siddhi.store.api.rest.StoresApiService;
import org.wso2.carbon.siddhi.store.api.rest.model.ModelApiResponse;
import org.wso2.carbon.siddhi.store.api.rest.model.Query;
import org.wso2.carbon.siddhi.store.api.rest.model.Record;
import org.wso2.carbon.siddhi.store.api.rest.model.RecordDetail;
import org.wso2.carbon.siddhi.store.api.rest.model.InitAggregationDTO;
import org.wso2.carbon.streaming.integrator.common.SiddhiAppRuntimeService;
import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.event.Event;
import io.siddhi.query.api.definition.Attribute;

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
    public Response initAgg(InitAggregationDTO body) throws NotFoundException {
        SiddhiAppRuntimeService siddhiAppRuntimeService =
                SiddhiStoreDataHolder.getInstance().getSiddhiAppRuntimeService();
        Map<String, SiddhiAppRuntime> siddhiAppRuntimes = siddhiAppRuntimeService.getActiveSiddhiAppRuntimes();
        SiddhiAppRuntimeImpl siddhiAppRuntime = (SiddhiAppRuntimeImpl) siddhiAppRuntimes.get(body.getAppName());
        if (siddhiAppRuntime == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                    "Siddhi Runtime could not be found for app : " + body.getAggName())).build();
        }
        AggregationRuntime aggregationRuntime = siddhiAppRuntime.getAggregationMap().get(body.getAggName());
        if (aggregationRuntime == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Aggregation Runtime could not be found for" + body.getAggName())).build();
        }

        ComplexEventChunk<StreamEvent> timerStreamEventChunk = new ComplexEventChunk<>();
        StreamEvent streamEvent = new StreamEvent(0, 0, 0);
        streamEvent.setType(ComplexEvent.Type.TIMER);
        streamEvent.setTimestamp(System.currentTimeMillis());
        timerStreamEventChunk.add(streamEvent);
        siddhiAppRuntime.getAggregationMap().get(body.getAggName()).initialiseExecutors(true);
        siddhiAppRuntime.getAggregationMap().get(body.getAggName()).processEvents(timerStreamEventChunk);
        return Response.status(Response.Status.OK).entity(new ApiResponseMessage(ApiResponseMessage.INFO,
                "Aggregation initiated for " + body.getAggName())).build();
    }

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
                if (body.isDetails()) {
                    Attribute[] attributes = siddhiAppRuntime.getStoreQueryOutputAttributes(body.getQuery());
                    response.setDetails(getRecordDetails(attributes));
                }
                return Response.ok().entity(response).build();
            } catch (Exception e) {
                log.error("Error while querying for siddhiApp: " + removeCRLFCharacters(body.getAppName()) +
                        ", with query: " + removeCRLFCharacters(body.getQuery()) + " Error: " +
                        removeCRLFCharacters(e.getMessage()), e);
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                                                       "Cannot query: " + e.getMessage())).build();
            }
        }
    }

    /**
     * Get record details.
     *
     * @param attributes Set of attributes
     * @return List of record details
     */
    private List<RecordDetail> getRecordDetails(Attribute[] attributes) {
        List<RecordDetail> details = new ArrayList<>();
        for (Attribute attribute : attributes) {
            details.add(new RecordDetail()
                    .name(attribute.getName())
                    .dataType(attribute.getType().toString()));
        }
        return details;
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

    private static String removeCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }
}
