/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.websocket.local;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapter;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterConfiguration;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterException;
import org.wso2.carbon.event.input.adapter.core.exception.TestConnectionNotSupportedException;
import org.wso2.carbon.event.input.adapter.websocket.local.internal.WebsocketLocalInputCallbackRegisterServiceImpl;
import org.wso2.carbon.event.input.adapter.websocket.local.internal.ds.WebsocketLocalEventAdapterServiceInternalValueHolder;

import java.util.Map;

public class WebsocketLocalEventAdapter implements InputEventAdapter {

    private InputEventAdapterListener eventAdapterListener;
    private final InputEventAdapterConfiguration eventAdapterConfiguration;
    private final Map<String, String> globalProperties;
    private WebsocketLocalInputCallbackRegisterServiceImpl websocketLocalInputCallbackRegisterService;

    public WebsocketLocalEventAdapter(InputEventAdapterConfiguration eventAdapterConfiguration, Map<String, String> globalProperties) {
        this.eventAdapterConfiguration = eventAdapterConfiguration;
        this.globalProperties = globalProperties;
        websocketLocalInputCallbackRegisterService = WebsocketLocalEventAdapterServiceInternalValueHolder.getWebsocketLocalInputCallbackRegisterServiceImpl();
    }

    @Override
    public void init(InputEventAdapterListener eventAdapterListener) throws InputEventAdapterException {
        this.eventAdapterListener = eventAdapterListener;
        websocketLocalInputCallbackRegisterService.subscribeAdapterListener(eventAdapterConfiguration.getName(), this.eventAdapterListener);
    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException {
        throw new TestConnectionNotSupportedException("not-supported");
    }

    @Override
    public void connect() {
        //Nothing to do.
    }

    @Override
    public void disconnect() {
        //Nothing to do.
    }

    @Override
    public void destroy() {
        websocketLocalInputCallbackRegisterService.unsubscribeAdapterListener(eventAdapterConfiguration.getName());
        this.eventAdapterListener = null;
    }

    @Override
    public boolean isEventDuplicatedInCluster() {
        return false;
    }

    @Override
    public boolean isPolling() {
        return false;
    }
}
