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

import org.jaggeryjs.hostobjects.web.WebSocketHostObject;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.ui.internal.ds.UIEventAdaptorServiceInternalValueHolder;

/**
 * Jaggery Websocket server calls this helper to subscribe/unsubscribe Websocket clients.
 */
public class JaggeryWebSocketHelper {

    public static void subscribeWebSocket(String streamName, String streamVersion, String sessionId,
                                          Object webSocketHostObject, int tenantId) {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
            UIEventAdaptorServiceInternalValueHolder.getUIOutputCallbackRegisterServiceImpl().
                    subscribeWebsocket(streamName, streamVersion, new JaggerySessionHolder(sessionId,
                            (WebSocketHostObject) webSocketHostObject));
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
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
