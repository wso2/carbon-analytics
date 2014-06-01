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

    public static final String THRIFT_EVENT_RECEIVER_ELEMENT = "thriftDataReceiver";
    public static final String SECURE_PORT_ELEMENT = "securePort";
    public static final String PORT_ELEMENT = "port";

    public static final int CARBON_DEFAULT_PORT_OFFSET = 0;
    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    public static final String RECEIVER_HOST_NAME = "hostName";
}
