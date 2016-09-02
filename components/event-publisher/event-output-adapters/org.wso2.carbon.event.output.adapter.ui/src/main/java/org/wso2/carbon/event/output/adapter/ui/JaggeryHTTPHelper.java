/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.output.adapter.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.output.adapter.ui.internal.util.UIEventAdapterConstants;

/**
 * Jaggery HTTP server calls this helper to retrieve events when an HTTP client polls the HTTP server.
 */
public class JaggeryHTTPHelper {
    public static String retrieveEvents(String streamName, String streamVersion, String lastUpdatedTime, int tenantId) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            JsonObject eventDetails = UIEventAdaptorServiceInternalValueHolder.getUIOutputCallbackRegisterServiceImpl().
                    retrieveEvents(streamName, streamVersion, lastUpdatedTime);
            if(eventDetails == null){
                JsonObject errorData = new JsonObject();
                errorData.addProperty("error","StreamId: " + streamName + UIEventAdapterConstants.ADAPTER_UI_COLON
                                              + streamVersion + " is not registered to receive events.");
                return new Gson().toJson(errorData);
            } else {
                return new Gson().toJson(eventDetails);
            }
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
