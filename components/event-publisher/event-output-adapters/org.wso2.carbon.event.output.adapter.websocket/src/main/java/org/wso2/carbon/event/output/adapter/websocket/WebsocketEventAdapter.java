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
package org.wso2.carbon.event.output.adapter.websocket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.tyrus.client.ClientManager;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterConfiguration;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterRuntimeException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.output.adapter.websocket.internal.WebsocketClient;
import org.wso2.carbon.event.output.adapter.websocket.internal.util.WebsocketEventAdapterConstants;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public final class WebsocketEventAdapter implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(WebsocketEventAdapter.class);
    private OutputEventAdapterConfiguration eventAdapterConfiguration;
    private Map<String, String> globalProperties;

    private Session session;
    private String socketServerUrl;

    public WebsocketEventAdapter(OutputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
    }


    @Override
    public void init() throws OutputEventAdapterException {
        WebsocketEventAdapterFactory.validateOutputEventAdapterConfigurations(eventAdapterConfiguration);
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
          connect();
    }

    @Override
    public void connect() {
        ClientEndpointConfig clientEndpointConfig = ClientEndpointConfig.Builder.create().build();
        ClientManager client = ClientManager.createClient();
        socketServerUrl = eventAdapterConfiguration.getStaticProperties().get(WebsocketEventAdapterConstants.ADAPTER_SERVER_URL);
        try {
            session = client.connectToServer(new WebsocketClient(), clientEndpointConfig, new URI(socketServerUrl));
        } catch (DeploymentException e) {
            throw new OutputEventAdapterRuntimeException("The adaptor "+eventAdapterConfiguration.getName()+" failed to connect to the websocket server "+
                    socketServerUrl ,e);
        } catch (IOException e) {
            throw new OutputEventAdapterRuntimeException("The adaptor "+eventAdapterConfiguration.getName()+" failed to connect to the websocket server "+
                    socketServerUrl ,e);
        } catch (URISyntaxException e) {
            throw new OutputEventAdapterRuntimeException("The adaptor "+eventAdapterConfiguration.getName()+" failed to connect to the websocket server "+
                    socketServerUrl ,e);
        }
    }

    @Override
    public void publish(Object message, Map<String, String> dynamicProperties) {
        if (message instanceof Object[]) {
            for (Object object : (Object[])message){
                synchronized (session){
                    session.getAsyncRemote().sendText(object.toString());
                }
            }
        } else {
            synchronized (session){
                session.getAsyncRemote().sendText(message.toString());      //this method call was synchronized to fix CEP-996
            }
        }
    }

    @Override
    public void disconnect() {
        try {
            if(session != null){
                session.close();
            }
        } catch (IOException e) {
            throw new OutputEventAdapterRuntimeException("The adaptor "+eventAdapterConfiguration.getName()+" failed to disconnect from the websocket server "+
                    socketServerUrl ,e);
        }
    }

    @Override
    public void destroy() {
        //Nothing to be destroyed.
    }
}
