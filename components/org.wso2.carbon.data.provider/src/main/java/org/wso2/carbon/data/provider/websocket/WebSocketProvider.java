/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.data.provider.websocket;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.data.provider.DataProvider;
import org.wso2.carbon.data.provider.InputFieldTypes;
import org.wso2.carbon.data.provider.ProviderConfig;
import org.wso2.carbon.data.provider.bean.DataSetMetadata;
import org.wso2.carbon.data.provider.exception.DataProviderException;
import org.wso2.carbon.data.provider.websocket.bean.WebSocketChannel;
import org.wso2.carbon.data.provider.websocket.endpoint.WebSocketProviderEndPoint;

import java.util.HashMap;
import java.util.Map;

@Component(
        service = DataProvider.class,
        immediate = true
)
public class WebSocketProvider implements DataProvider {
    private static final Logger log = LoggerFactory.getLogger(WebSocketProvider.class);
    private WebSocketChannel webSocketChannel;

    @Override
    public DataProvider init(String topic, String sessionId, JsonElement jsonElement) throws DataProviderException {
        webSocketChannel = new Gson().fromJson(jsonElement, WebSocketChannel.class);
        webSocketChannel.setSessionId(sessionId);
        webSocketChannel.setSubscriberTopic(topic);
        return this;
    }

    @Override
    public void start() {
        WebSocketProviderEndPoint.subscribeToTopic(webSocketChannel.getTopic(), webSocketChannel);
    }

    @Override
    public void stop() {
        WebSocketProviderEndPoint.unsubscribeFromTopic(webSocketChannel.getTopic(), webSocketChannel.getSessionId());
    }

    @Override
    public boolean configValidator(ProviderConfig providerConfig) throws DataProviderException {
        // no validations required
        return false;
    }

    @Override
    public String providerName() {
        return "WebSocketProvider";
    }

    @Override
    public DataSetMetadata dataSetMetadata() {
        // not applicable because web-socket will only provide a stream of data.
        return null;
    }

    @Override
    public String providerConfig() {
        Map<String, String> renderingTypes = new HashMap<>();
        renderingTypes.put("topic", InputFieldTypes.TEXT_FIELD);
        renderingTypes.put("mapType", InputFieldTypes.TEXT_FIELD);
        return new Gson().toJson(new Object[]{renderingTypes, new WebSocketChannel()});
    }
}
