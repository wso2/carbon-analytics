/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.streaming.integrator.core.ha.util;

public class HAConstants {

    public static final int EVENT_BUFFER_EXTRACTOR_THREAD_POOL_SIZE = 5;
    public static final String CHANNEL_ID_MESSAGE = "eventMessage";
    public static final String CHANNEL_ID_CONTROL_MESSAGE = "controlMessage";
    public static final String PERSISTED_APP_SPLIT_DELIMITER = "__";
    public static final int PROTOCOL_AND_MESSAGE_BYTE_LENGTH = 5;
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String ACTIVE_NODE_CONNECTION_POOL_ID = "activeNode_connection_pool";
    public static final String HOST = "host";
    public static final String PORT = "port";
    public static final String ADVERTISED_HOST = "advertisedHost";
    public static final String ADVERTISED_PORT = "advertisedPort";


}
