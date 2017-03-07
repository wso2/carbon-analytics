/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.output.adapter.wso2event.internal.util;


public final class WSO2EventAdapterConstants {

    private WSO2EventAdapterConstants() {
    }

    public static final String ADAPTER_CONF_WSO2EVENT_PROP_RECEIVER_URL = "receiverURL";
    public static final String ADAPTER_CONF_WSO2EVENT_PROP_AUTHENTICATOR_URL = "authenticatorURL";
    public static final String ADAPTER_CONF_WSO2EVENT_PROP_USER_NAME = "username";
    public static final String ADAPTER_CONF_WSO2EVENT_PROP_PASSWORD = "password";
    public static final String ADAPTER_CONF_WSO2EVENT_PROP_PROTOCOL = "protocol";
    public static final String ADAPTER_CONF_WSO2EVENT_PROP_PUBLISHING_MODE = "publishingMode";
    public static final String ADAPTER_CONF_WSO2EVENT_PROP_PUBLISH_TIMEOUT_MS = "publishTimeout";
    public static final String ADAPTER_TYPE_WSO2EVENT = "wso2event";
    public static final String ADAPTER_USAGE_TIPS_WSO2EVENT = "wso2event.usage.tips";

    public static final String ADAPTER_CONF_WSO2EVENT_HINT_RECEIVER_URL = "enterReceiverUrl";
    public static final String ADAPTER_CONF_WSO2EVENT_HINT_AUTHENTICATOR_URL = "enterAuthenticatorUrl";
    public static final String ADAPTER_CONF_WSO2EVENT_HINT_USER_NAME = "enterUsername";
    public static final String ADAPTER_CONF_WSO2EVENT_HINT_PASSWORD = "enterPassword";
    public static final String ADAPTER_CONF_WSO2EVENT_HINT_PROTOCOL = "selectProtocol";
    public static final String ADAPTER_CONF_WSO2EVENT_HINT_PUBLISHING_MODE = "selectPublishingMode";
    public static final String ADAPTER_CONF_WSO2EVENT_HINT_PUBLISH_TIMEOUT_MS = "enterPublishTimeout";
    public static final String ADAPTER_STATIC_CONFIG_STREAM_NAME = "stream";
    public static final String ADAPTER_STATIC_CONFIG_STREAM_VERSION = "version";

    public static final String ADAPTER_PROTOCOL_THRIFT = "thrift";
    public static final String ADAPTER_PROTOCOL_BINARY = "binary";

    public static final String ADAPTER_PUBLISHING_MODE_BLOCKING = "blocking";
    public static final String ADAPTER_PUBLISHING_MODE_NON_BLOCKING = "non-blocking";

    public static final String ADAPTER_CONF_PATH = "config.path";

    public static final String DEFAULT_THRIFT_TCP_URL = "default.thrift.tcp.url";
    public static final String DEFAULT_THRIFT_SSL_URL = "default.thrift.ssl.url";
    public static final String DEFAULT_BINARY_TCP_URL = "default.binary.tcp.url";
    public static final String DEFAULT_BINARY_SSL_URL = "default.binary.ssl.url";



}
