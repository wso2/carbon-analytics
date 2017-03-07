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
import org.jaggeryjs.scriptengine.exceptions.ScriptException;

import java.io.IOException;
import java.util.Map;

/**
 * Holds the Session established between the Jaggery Websocket server and the Websocket client.
 */
public class JaggerySessionHolder implements SessionHolder {

    private final String sessionId;
    private Map<Integer, String> filterProps;
    private String username;
    private int tenantId;
    private final WebSocketHostObject webSocketHostObject;

    public JaggerySessionHolder(String sessionId, Map<Integer, String> filterProps,
                                String username, int tenantId,
                                WebSocketHostObject webSocketHostObject) {
        this.sessionId = sessionId;
        this.filterProps = filterProps;
        this.username = username;
        this.tenantId = tenantId;
        this.webSocketHostObject = webSocketHostObject;
    }

    @Override
    public String getId() {
        return sessionId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public int getTenantId() {
        return tenantId;
    }

    @Override
    public Map<Integer, String> getFilterProps() {
        return filterProps;
    }


    @Override
    public void sendText(String message) throws IOException {
        try {
            webSocketHostObject.jsFunction_send(null, webSocketHostObject, new Object[]{message}, null);
        } catch (ScriptException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}
