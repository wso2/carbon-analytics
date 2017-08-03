/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.databridge.receiver.thrift.internal.utils;


/**
 * Agent Server Constants
 */
public final class ThriftDataReceiverConstants {

    private ThriftDataReceiverConstants() {
    }
    public static final String SECURE_PORT_ELEMENT = "sslPort";
    public static final String PORT_ELEMENT = "tcpPort";
    public static final String PROTOCOLS_ELEMENT = "sslEnabledProtocols";
    public static final String CIPHERS_ELEMENT = "ciphers";
    public static final String RECEIVER_HOST_NAME = "hostName";
    public static final String DEFAULT_HOSTNAME = "0.0.0.0";
    public static final String DATA_BRIDGE_RECEIVER_NAME = "Thrift";
    public static final String DATA_BRIDGE_KEY_STORE_LOCATION = "keyStoreLocation";
    public static final String DATA_BRIDGE_KEY_STORE_PASSWORD = "keyStorePassword";
}
