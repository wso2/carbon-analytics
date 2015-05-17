/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.output.adapter.mqtt.internal.util;


public final class MQTTEventAdapterConstants {

    private MQTTEventAdapterConstants() {
    }

    public static final String ADAPTER_TYPE_MQTT = "mqtt";
    public static final String ADAPTER_CONF_URL = "url";
    public static final String ADAPTER_CONF_USERNAME = "username";
    public static final String ADAPTER_CONF_USERNAME_HINT = "username.hint";
    public static final String ADAPTER_CONF_PASSWORD = "password";
    public static final String ADAPTER_CONF_PASSWORD_HINT = "password.hint";
    public static final String ADAPTER_CONF_URL_HINT = "url.hint";
    public static final String ADAPTER_MESSAGE_TOPIC = "topic";
    public static final String ADAPTER_MESSAGE_QOS = "qos";
    public static final String ADAPTER_CONF_CLEAN_SESSION = "cleanSession";
    public static final String ADAPTER_CONF_CLEAN_SESSION_HINT = "cleanSession.hint";
    public static final String CONNECTION_KEEP_ALIVE_INTERVAL = "connectionKeepAliveInterval";
    public static final int DEFAULT_CONNECTION_KEEP_ALIVE_INTERVAL = 60;
    public static final String ADAPTER_TEMP_DIRECTORY_NAME = "java.io.tmpdir";

    public static final int DEFAULT_MIN_THREAD_POOL_SIZE = 8;
    public static final int DEFAULT_MAX_THREAD_POOL_SIZE = 100;
    public static final int DEFAULT_EXECUTOR_JOB_QUEUE_SIZE = 2000;
    public static final long DEFAULT_KEEP_ALIVE_TIME_IN_MILLIS = 20000;
    public static final String ADAPTER_MIN_THREAD_POOL_SIZE_NAME = "minThread";
    public static final String ADAPTER_MAX_THREAD_POOL_SIZE_NAME = "maxThread";
    public static final String ADAPTER_KEEP_ALIVE_TIME_NAME = "keepAliveTimeInMillis";
    public static final String ADAPTER_EXECUTOR_JOB_QUEUE_SIZE_NAME = "jobQueueSize";


}
