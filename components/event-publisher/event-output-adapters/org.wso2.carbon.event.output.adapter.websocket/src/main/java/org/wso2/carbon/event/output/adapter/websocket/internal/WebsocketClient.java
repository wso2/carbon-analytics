/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.event.output.adapter.websocket.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

/**
 * This is the client that is used to publish events to a user-configured websocket-server end point.
 */
public class WebsocketClient extends Endpoint {

    private static final Log log = LogFactory.getLog(WebsocketClient.class);

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        if (log.isDebugEnabled()){
            log.debug("Websocket Output Adaptor: WebsocketClient connected, with session ID: " + session.getId()+", to the remote end point URI - "+session.getRequestURI());
        }
    }
}
