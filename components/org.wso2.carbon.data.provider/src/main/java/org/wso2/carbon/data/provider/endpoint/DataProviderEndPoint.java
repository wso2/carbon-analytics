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
import org.json.JSONObject;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.data.provider.AbstractDataProvider;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.data.provider.DataProviderAuthorizer;
import org.wso2.carbon.data.provider.bean.DataProviderConfigRoot;
import org.wso2.carbon.data.provider.rdbms.bean.RDBMSDataProviderConfBean;
import org.wso2.carbon.data.provider.siddhi.SiddhiProvider;
import org.wso2.carbon.datasource.core.api.DataSourceService;
import org.wso2.msf4j.websocket.WebSocketEndpoint;
import org.wso2.transport.http.netty.contract.websocket.WebSocketConnection;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
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
    private static final Map<String, WebSocketConnection> sessionMap = new ConcurrentHashMap<>();
    private static final String WEB_SOCKET_CONFIG_HEADER = "data.provider.configs";
    private static final String WEB_SOCKET_AUTHORIZING_CLASS_CONFIG_HEADER = "authorizingClass";
    private static final String DEFAULT_WEBSOCKET_AUTHORIZING_CLASS
            = "org.wso2.carbon.data.provider.DefaultDataProviderAuthorizer";

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

    @Reference(service = DataProviderAuthorizer.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unsetDataProviderAuthorizer")
    protected void setDataProviderAuthorizer(DataProviderAuthorizer dataProviderAuthorizer) {
        getDataProviderHelper()
                .setDataProviderAuthorizer(dataProviderAuthorizer.getClass().getName(), dataProviderAuthorizer);
        LOGGER.debug("Data Provider Authorizer '{}' registered.", dataProviderAuthorizer.getClass().getName());
    }

    protected void unsetDataProviderAuthorizer(DataProviderAuthorizer dataProviderAuthorizer) {
        getDataProviderHelper().removeDataProviderAuthorizerClass(dataProviderAuthorizer.getClass().getName());
        LOGGER.debug("Data Provider Authorizer '{}' unregistered.", dataProviderAuthorizer.getClass().getName());
    }

    /**
     * Handle initiation of the connection map the session object in the session map.
     *
     * @param webSocketConnection webSocketConnection object associated with the connection
     */
    @OnOpen
    public static void onOpen(WebSocketConnection webSocketConnection) {
        sessionMap.put(webSocketConnection.getChannelId(), webSocketConnection);
    }

    /**
     * Create DataProvider instance, start it and store it in the providerMap.
     *
     * @param message String message received from the web client
     * @param webSocketConnection webSocketConnection object associated with the connection
     */
    @OnMessage
    public void onMessage(String message, WebSocketConnection webSocketConnection) {
        DataProviderConfigRoot dataProviderConfigRoot = new Gson().fromJson(message, DataProviderConfigRoot.class);
        String authoringClassName;
        try {
            Map webSocketConfiguration = null;
            if (!(getDataProviderHelper().getConfigProvider()
                    .getConfigurationObject(WEB_SOCKET_CONFIG_HEADER, Map.class)
                    instanceof RDBMSDataProviderConfBean)) {
                webSocketConfiguration = getDataProviderHelper().getConfigProvider()
                    .getConfigurationObject(WEB_SOCKET_CONFIG_HEADER, Map.class);
            }
            if (webSocketConfiguration != null) {
                JSONObject webSocketConfigJSON = new JSONObject(new Gson().toJson(webSocketConfiguration));
                if (!webSocketConfigJSON.has(WEB_SOCKET_AUTHORIZING_CLASS_CONFIG_HEADER)) {
                    throw new Exception("Web socket authorizing class cannot be found in the deployment.yaml file.");
                }
                authoringClassName = (String) webSocketConfigJSON.get(WEB_SOCKET_AUTHORIZING_CLASS_CONFIG_HEADER);
            } else {
                authoringClassName = DEFAULT_WEBSOCKET_AUTHORIZING_CLASS;
            }
            DataProviderAuthorizer dataProviderAuthorizer;
            try {
                dataProviderAuthorizer = getDataProviderHelper().getDataProviderAuthorizer(authoringClassName);
            } catch (NullPointerException e) {
                throw new Exception("Cannot find the Data Provider Authorizer class for the given class name: "
                        + authoringClassName + ".");
            }
            boolean authorizerResult = dataProviderAuthorizer.authorize(dataProviderConfigRoot);
            if (!authorizerResult) {
                throw new Exception("Access denied to data provider.");
            }
        } catch (Exception e) {
            try {
                sendText(webSocketConnection.getChannelId(), e.getMessage());
            } catch (IOException e1) {
                //ignore
            }
            LOGGER.error("Error occurred while authorizing the access to data provider. " + dataProviderConfigRoot
                    .getProviderName() + ". " + e.getMessage(), e);
            onError(e);
            return;
        }

        try {
            if (dataProviderConfigRoot.getAction().equalsIgnoreCase(DataProviderConfigRoot.Types.SUBSCRIBE.toString()
            )) {
                getDataProviderHelper().removeTopicIfExist(webSocketConnection.getChannelId(),
                        dataProviderConfigRoot.getTopic());
                DataProvider dataProvider = getDataProviderHelper().getDataProvider(dataProviderConfigRoot
                        .getProviderName());
                dataProvider.init(dataProviderConfigRoot.getTopic(), webSocketConnection.getChannelId(),
                        dataProviderConfigRoot.getDataProviderConfiguration()).start();
                getDataProviderHelper().addDataProviderToSessionMap(webSocketConnection.getChannelId(),
                        dataProviderConfigRoot.getTopic(),
                        dataProvider);
                if (dataProvider instanceof SiddhiProvider) {
                    SiddhiProvider siddhiProvider = (SiddhiProvider)dataProvider;
                    if(siddhiProvider.getSiddhiDataProviderConfig().isPaginationEnabled()){
                        siddhiProvider.publishWithPagination(dataProviderConfigRoot.getDataProviderConfiguration(),
                                dataProviderConfigRoot.getTopic(), webSocketConnection.getChannelId());
                    }
                }
            } else if (dataProviderConfigRoot.getAction().equalsIgnoreCase(DataProviderConfigRoot.Types.UNSUBSCRIBE
                    .toString())) {
                getDataProviderHelper().removeTopicIfExist(webSocketConnection.getChannelId(),
                        dataProviderConfigRoot.getTopic());
            } else if (dataProviderConfigRoot.getAction().equalsIgnoreCase(DataProviderConfigRoot.Types.POLLING
                    .toString())){
                Map<String, DataProvider> topicDataProviderMap =  getDataProviderHelper().
                        getTopicDataProviderMap(webSocketConnection.getChannelId());
                if (topicDataProviderMap != null) {
                    DataProvider dataProvider = topicDataProviderMap.get(dataProviderConfigRoot.getTopic());
                    if (dataProvider == null) {
                        throw new Exception("Error while performing action: " + dataProviderConfigRoot.getAction() +
                                ", data provider for session id: " + webSocketConnection.getChannelId() +
                                " not found.");
                    } else if (dataProvider instanceof SiddhiProvider) {
                        SiddhiProvider siddhiProvider = (SiddhiProvider)dataProvider;
                        if (siddhiProvider.getSiddhiDataProviderConfig().isPaginationEnabled()) {
                            siddhiProvider.publishWithPagination(dataProviderConfigRoot.getDataProviderConfiguration(),
                                    dataProviderConfigRoot.getTopic(), webSocketConnection.getChannelId());
                        } else {
                            siddhiProvider.publish(dataProviderConfigRoot.getTopic(),
                                    webSocketConnection.getChannelId());
                        }
                    } else {
                        AbstractDataProvider abstractDataProvider = (AbstractDataProvider) dataProvider;
                        abstractDataProvider.publish(dataProviderConfigRoot.getTopic(),
                                webSocketConnection.getChannelId());
                    }
                } else {
                    throw new Exception("Error while performing action: " + dataProviderConfigRoot.getAction() +
                            ", data provider map for session id: " + webSocketConnection.getChannelId() +
                            " not initialized.");
                }
            } else {
                throw new Exception("Invalid action " + dataProviderConfigRoot.getAction() + " given in the message." +
                        "Valid actions are : " + Arrays.toString(DataProviderConfigRoot.Types.values()));
            }
        } catch (Exception e) {
            try {
                sendText(webSocketConnection.getChannelId(), e.getMessage());
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
     * @param webSocketConnection webSocketConnection object associated with the connection
     */
    @OnClose
    public void onClose(WebSocketConnection webSocketConnection) {
        Map<String, DataProvider> dataProviderMap = getDataProviderHelper().getTopicDataProviderMap(
                webSocketConnection.getChannelId());
        for (String topic : dataProviderMap.keySet()) {
            getDataProviderHelper().removeTopicIfExist(webSocketConnection.getChannelId(), topic);
        }
        getDataProviderHelper().removeSessionData(webSocketConnection.getChannelId());
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
                sessionMap.get(sessionId).pushText(text);
            }
        }
    }
}
