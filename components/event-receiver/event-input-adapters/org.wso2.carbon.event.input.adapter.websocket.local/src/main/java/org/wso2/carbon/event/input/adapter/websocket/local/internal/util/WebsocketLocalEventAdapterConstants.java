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

package org.wso2.carbon.event.input.adapter.websocket.local.internal.util;


public final class WebsocketLocalEventAdapterConstants {

    private WebsocketLocalEventAdapterConstants() {
    }

    public static final String ADAPTOR_TYPE_WEBSOCKET_LOCAL = "websocket-local";
    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    public static final int DEFAULT_HTTP_PORT = 9763;
    public static final int DEFAULT_HTTPS_PORT = 9443;

    public static final String ADAPTOR_USAGE_TIPS_PREFIX = "websocket.local.usage.tips.prefix";
    public static final String ADAPTER_USAGE_TIPS_MID = "websocket.local.usage.tips.mid";
    public static final String ADAPTER_USAGE_TIPS_POSTFIX = "websocket.local.usage.tips.postfix";
}
