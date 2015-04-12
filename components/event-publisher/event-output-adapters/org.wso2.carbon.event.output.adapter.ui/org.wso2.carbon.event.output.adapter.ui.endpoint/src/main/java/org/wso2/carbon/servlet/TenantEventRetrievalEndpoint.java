package org.wso2.carbon.servlet;
/*
 *
 * Copyright (c) 2014-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.ui.UIOutputCallbackControllerService;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import util.UIConstants;

import javax.ws.rs.GET;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Getting events for HTTP request from a tenant
 */

@Path("/t/{tdomain}/")
public class TenantEventRetrievalEndpoint{

    private static final Log log = LogFactory.getLog(SuperTenantEventRetrievalEndpoint.class);
    protected UIOutputCallbackControllerService uiOutputCallbackControllerService;

    public TenantEventRetrievalEndpoint() {
        uiOutputCallbackControllerService = (UIOutputCallbackControllerService) PrivilegedCarbonContext
                .getThreadLocalCarbonContext()
                .getOSGiService(UIOutputCallbackControllerService.class,null);

    }

    /**
     * Retrieve events when polling
     *
     * @param streamName - StreamName extracted from the http url.
     * @param version - Version extracted from the http url.
     * @param lastUpdatedTime - Last event's dispatched name.
     * @param tdomain - Tenant domain extracted from http url
     * @return response
     */
    @GET
    @Path("/{streamname}/{version}")
    public Response retrieveEvents(@PathParam("streamname") String streamName, @PathParam("version") String version,
            @QueryParam("lastUpdatedTime") String lastUpdatedTime, @PathParam("tdomain") String tdomain) {

        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tdomain,true);
        PrivilegedCarbonContext.startTenantFlow();
        String streamId = streamName + UIConstants.ADAPTER_UI_COLON + version;
        JsonObject eventDetails = uiOutputCallbackControllerService.retrieveEvents(streamName, version,lastUpdatedTime);
        PrivilegedCarbonContext.endTenantFlow();
        String jsonString;
        if(eventDetails == null){
            JsonObject errorData = new JsonObject();
            errorData.addProperty("error","StreamId: " + streamId + " is not registered to receive events.");
            jsonString = new Gson().toJson(errorData);
            return Response.status(Response.Status.NOT_FOUND).entity(jsonString).header
                    ("Access-Control-Allow-Origin","*").build();
        } else{
            jsonString = new Gson().toJson(eventDetails);
            return Response.ok(jsonString, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin",
                    "*").build();
        }
    }
}
