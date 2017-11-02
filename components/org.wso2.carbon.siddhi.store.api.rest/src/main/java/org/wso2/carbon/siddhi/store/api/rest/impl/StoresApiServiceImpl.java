package org.wso2.carbon.siddhi.store.api.rest.impl;

import org.wso2.carbon.siddhi.store.api.rest.NotFoundException;
import org.wso2.carbon.siddhi.store.api.rest.ApiResponseMessage;
import org.wso2.carbon.siddhi.store.api.rest.SiddhiStoreDataHolder;
import org.wso2.carbon.siddhi.store.api.rest.StoresApiService;
import org.wso2.carbon.siddhi.store.api.rest.model.ModelApiResponse;
import org.wso2.carbon.siddhi.store.api.rest.model.Query;
import org.wso2.carbon.siddhi.store.api.rest.model.Record;
import org.wso2.carbon.stream.processor.common.SiddhiAppRuntimeService;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.event.Event;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-11-01T11:26:25.925Z")
public class StoresApiServiceImpl extends StoresApiService {

    @Override
    public Response query(Query body) throws NotFoundException {
        if (body.getQuery() == null || body.getQuery().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "query cannot be empty or null")).build();
        }
        if (body.getTableDef() == null || body.getTableDef().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Table definition cannot be empty or null")).build();
        }

        SiddhiAppRuntimeService siddhiAppRuntimeService =
                SiddhiStoreDataHolder.getInstance().getSiddhiAppRuntimeService();
        Map<String, SiddhiAppRuntime> siddhiAppRuntimes = siddhiAppRuntimeService.getActiveSiddhiAppRuntimes();
        SiddhiAppRuntime siddhiAppRuntime = siddhiAppRuntimes.get(body.getTableDef());
        if (siddhiAppRuntime == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new ApiResponseMessage(ApiResponseMessage
                    .ERROR, "Cannot find an active SiddhiApp with name: " + body.getTableDef())).build();
        } else {
            Event[] events = siddhiAppRuntime.query(body.getQuery());
            List<Record> records = getRecords(events);
            ModelApiResponse response = new ModelApiResponse();
            response.setRecords(records);
            return Response.ok().entity(response).build();
        }
    }

    private List<Record> getRecords(Event[] events) {
        List<Record> records = new ArrayList<>();
        if (events != null) {
            for (Event event : events) {
                Record record = new Record();
                record.add(event.getData());
                records.add(record);
            }
        }
        return records;
    }
}
