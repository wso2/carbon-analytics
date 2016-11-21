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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jaggeryjs.hostobjects.web.WebSocketHostObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.output.adapter.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jaggery Websocket server calls this helper to subscribe/unsubscribe Websocket clients.
 */
public class JaggeryWebSocketHelper {
    private static final Log log = LogFactory.getLog(JaggeryWebSocketHelper.class);

    private static final String FILTER_PROP_NAME = "name";
    private static final String FILTER_PROP_VALUE = "value";
    private static final String CORRELATATION = "correlation_";
    private static final String META = "meta_";
    private static final String NULL = "null";

    public static void subscribeWebSocket(String streamName, String streamVersion, String filterProps, String username,
                                          String sessionId, int tenantId, Object webSocketHostObject) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            StreamDefinition streamDefinition = UIEventAdaptorServiceInternalValueHolder.getEventStreamService().
                    getStreamDefinition(streamName, streamVersion);
            if (!filterProps.equalsIgnoreCase(NULL)) {
                UIEventAdaptorServiceInternalValueHolder.getUIOutputCallbackRegisterServiceImpl().
                        subscribeWebsocket(streamName, streamVersion, new JaggerySessionHolder(sessionId,
                                getFilterProps(streamDefinition, new JSONArray(filterProps)), username, tenantId, (WebSocketHostObject) webSocketHostObject));
            } else {
                UIEventAdaptorServiceInternalValueHolder.getUIOutputCallbackRegisterServiceImpl().
                        subscribeWebsocket(streamName, streamVersion, new JaggerySessionHolder(sessionId,
                                getFilterProps(streamDefinition, null), username, tenantId, (WebSocketHostObject) webSocketHostObject));

            }
        } catch (JSONException | EventStreamConfigurationException e) {
            log.error(e.getMessage(), e);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    private static Map<Integer, String> getFilterProps(StreamDefinition streamDefinition, JSONArray filterPropsJson) throws JSONException {
        Map<Integer, String> filterProps = new HashMap<>();
        if (filterPropsJson != null) {
            int metaDataOffset = 1;

            int correlationDataOffset = metaDataOffset;
            if (streamDefinition.getMetaData() != null) {
                correlationDataOffset = correlationDataOffset + streamDefinition.getMetaData().size();
            }
            int payloadDataOffset = correlationDataOffset;
            if (streamDefinition.getCorrelationData() != null) {
                payloadDataOffset = payloadDataOffset + streamDefinition.getCorrelationData().size();
            }
            for (int i = 0; i < filterPropsJson.length(); i++) {
                JSONObject aFilter = (JSONObject) filterPropsJson.get(i);
                String filterName = (String) aFilter.get(FILTER_PROP_NAME);
                String filterValue = (String) aFilter.get(FILTER_PROP_VALUE);
                if (filterName != null && !filterName.trim().isEmpty() && filterValue != null && !filterValue.trim().isEmpty()) {
                    int index = -1;
                    if (filterName.startsWith(META)) {
                        filterName = filterName.replace(META, "");
                        index = getIndexOf(streamDefinition.getMetaData(), filterName);
                        if (index != -1) {
                            filterProps.put(metaDataOffset + index, filterValue);
                        } else {
                            log.warn("Filter property - " + filterName + " doesn't exists in the stream  - "
                                    + streamDefinition.getStreamId() + ", hence ignoring the filter property");
                        }
                    } else if (filterName.startsWith(CORRELATATION)) {
                        filterName = filterName.replace(CORRELATATION, "");
                        index = getIndexOf(streamDefinition.getCorrelationData(), filterName);
                        if (index != -1) {
                            filterProps.put(correlationDataOffset + index, filterValue);
                        } else {
                            log.warn("Filter property - " + filterName + " doesn't exists in the stream  - "
                                    + streamDefinition.getStreamId() + ", hence ignoring the filter property");
                        }
                    } else {
                        index = getIndexOf(streamDefinition.getPayloadData(), filterName);
                        if (index != -1) {
                            filterProps.put(payloadDataOffset + index, filterValue);
                        } else {
                            log.warn("Filter property - " + filterName + " doesn't exists in the stream  - "
                                    + streamDefinition.getStreamId() + ", hence ignoring the filter property");
                        }
                    }
                }
            }
        }
        return filterProps;
    }

    private static int getIndexOf(List<Attribute> streamAttributes, String attributeName) {
        if (streamAttributes != null) {
            for (int i = 0; i < streamAttributes.size(); i++) {
                if (streamAttributes.get(i).getName().equalsIgnoreCase(attributeName)) {
                    return i;
                }
            }
            return -1;
        } else {
            return -1;
        }
    }

    public static void unsubscribeWebsocket(String streamName, String streamVersion, String sessionId, int tenantId) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            UIEventAdaptorServiceInternalValueHolder.getUIOutputCallbackRegisterServiceImpl().
                    unsubscribeWebsocket(streamName, streamVersion, sessionId);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
}
