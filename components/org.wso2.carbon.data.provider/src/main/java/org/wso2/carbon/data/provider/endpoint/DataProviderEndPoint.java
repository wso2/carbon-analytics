/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.data.provider.endpoint;


import com.google.gson.Gson;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.data.provider.bean.DataProviderConfigRoot;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.msf4j.websocket.WebSocketEndpoint;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import static org.wso2.carbon.data.provider.utils.DataProviderValueHolder.getDataProviderHelper;

/**
 * Data provider web socket endpoint.
 */
@Component(
        service = WebSocketEndpoint.class,
        immediate = true
)
@ServerEndpoint(value = "/data-provider")
public class DataProviderEndPoint implements WebSocketEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataProviderEndPoint.class);
    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void registerDataSourceService(DataSourceService service) {
        getDataProviderHelper().setDataSourceService(service);
    }

    protected void unregisterDataSourceService(DataSourceService service) {
        getDataProviderHelper().setDataSourceService(null);
    }

    @Reference(service = ConfigProvider.class,
            cardinality = ReferenceCardinality.MANDATORY,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetConfigProvider")
    protected void setConfigProvider(ConfigProvider configProvider) {
        getDataProviderHelper().setConfigProvider(configProvider);
    }

    protected void unsetConfigProvider(ConfigProvider configProvider) {
        getDataProviderHelper().setConfigProvider(null);
    }

    /**
     * Handle initiation of the connection map the session object in the session map.
     *
     * @param session Session object associated with the connection
     */
    @OnOpen
    public static void onOpen(Session session) {
        sessionMap.put(session.getId(), session);
    }

    /**
     * Create DataProvider instance, start it and store it in the providerMap.
     *
     * @param message String message received from the web client
     * @param session Session object associated with the connection
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        DataProviderConfigRoot dataProviderConfigRoot = new Gson().fromJson(message, DataProviderConfigRoot.class);
        try {
            if (dataProviderConfigRoot.getAction().equalsIgnoreCase(DataProviderConfigRoot.Types.SUBSCRIBE.toString()
            )) {
                getDataProviderHelper().removeTopicIfExist(session.getId(), dataProviderConfigRoot.getTopic());
                DataProvider dataProvider = getDataProviderHelper().getDataProvider(dataProviderConfigRoot
                        .getProviderName());
                dataProvider.init(dataProviderConfigRoot.getTopic(), session.getId(),
                        dataProviderConfigRoot.getDataProviderConfiguration()).start();
                getDataProviderHelper().addDataProviderToSessionMap(session.getId(), dataProviderConfigRoot.getTopic(),
                        dataProvider);
            } else if (dataProviderConfigRoot.getAction().equalsIgnoreCase(DataProviderConfigRoot.Types.UNSUBSCRIBE
                    .toString())) {
                getDataProviderHelper().removeTopicIfExist(session.getId(), dataProviderConfigRoot.getTopic());
            } else {
                throw new Exception("Invalid action " + dataProviderConfigRoot.getAction() + " given in the message." +
                        "Valid actions are : " + Arrays.toString(DataProviderConfigRoot.Types.values()));
            }
        } catch (Exception e) {
            try {
                sendText(session.getId(), "Error initializing the data provider endpoint.");
            } catch (IOException e1) {
                //ignore
            }
            LOGGER.error("Error initializing the data provider endpoint for source type " + dataProviderConfigRoot
                    .getProviderName() + ". " + e.getMessage(), e);
            onError(e);
        }
    }

    /**
     * handle disconnection with the client.
     *
     * @param session Session object associated with the connection
     */
    @OnClose
    public void onClose(Session session) {
        Map<String, DataProvider> dataProviderMap = getDataProviderHelper().getTopicDataProviderMap(session.getId());
        for (String topic : dataProviderMap.keySet()) {
            getDataProviderHelper().removeTopicIfExist(session.getId(), topic);
        }
        getDataProviderHelper().removeSessionData(session.getId());
    }

    /**
     * handle on error.
     */
    @OnError
    public void onError(Throwable throwable) {
        LOGGER.error("Error found in method : " + throwable.toString());
    }

    /**
     * Send message to specific client.
     *
     * @param sessionId sessionId of the client.
     * @param text      String message to be sent to the client.
     * @throws IOException If there is a problem delivering the message.
     */
    public static void sendText(String sessionId, String text) throws IOException {
        if (sessionMap.containsKey(sessionId)) {
            if (sessionMap.get(sessionId) != null) {
                sessionMap.get(sessionId).getBasicRemote().sendText(text);
            }
        }
    }

    public static Map<String, Session> getSessionMap() {
        return sessionMap;
    }
}
