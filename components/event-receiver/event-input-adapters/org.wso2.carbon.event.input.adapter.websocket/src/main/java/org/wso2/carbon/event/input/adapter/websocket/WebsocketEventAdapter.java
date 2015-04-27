/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.websocket;

import org.glassfish.tyrus.client.ClientManager;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.websocket.internal.WebsocketClient;
import org.wso2.carbon.event.input.adapter.websocket.internal.util.WebsocketEventAdapterConstants;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class WebsocketEventAdapter implements InputEventAdapter {

    private InputEventAdapterListener eventAdapterListener;
    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private final String socketServerUrl;
    private ClientManager client;

    public WebsocketEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        socketServerUrl = eventAdapterConfiguration.getProperties().get(WebsocketEventAdapterConstants.ADAPTER_SERVER_URL);
    }

    @Override
    public void init(InputEventAdapterListener eventAdaptorListener) throws InputEventAdapterException {
        this.eventAdapterListener = eventAdaptorListener;
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        connect();
    }

    @Override
    public void connect() {
        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().build();
        client = ClientManager.createClient();
        try {
            client.connectToServer(new WebsocketClient(eventAdapterListener), clientEndpointConfig, new URI(socketServerUrl));
        } catch (DeploymentException e) {
            throw new ConnectionUnavailableException("The adapter "+eventAdapterConfiguration.getName()+" failed to connect to the websocket server "+
                    socketServerUrl ,e);
        } catch (IOException e) {
            throw new ConnectionUnavailableException("The adapter "+eventAdapterConfiguration.getName()+" failed to connect to the websocket server "+
                    socketServerUrl ,e);
        } catch (URISyntaxException e) {
            throw new ConnectionUnavailableException("The adapter "+eventAdapterConfiguration.getName()+" failed to connect to the websocket server "+
                    socketServerUrl ,e);
        }
    }

    @Override
    public void disconnect() {
        client.shutdown();
    }

    @Override
    public void destroy() {
        if(client != null){
            client.shutdown();
        }
    }
}
