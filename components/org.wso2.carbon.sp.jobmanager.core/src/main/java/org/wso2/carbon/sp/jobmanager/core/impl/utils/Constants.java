/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.sp.jobmanager.core.impl.utils;

/**
 * Class to define SQL queries and constants.
 */
public class Constants {
    public static final String PERMISSION_APP_NAME = "MON";
    public static final String PERMISSION_SUFFIX_VIEWER = ".viewer";
    public static final String PERMISSION_SUFFIX_MANAGER = ".manager";
    public static final String MANAGER_KEY_GENERATOR = "_";
    public static final String MANAGERID = "MANAGERID";
    public static final String HOST = "HOST";
    public static final String PORT = "PORT";
    public static final String DATA_TYPE_INTEGER = "integerType";
    public static final String DATA_TYPE_STRING = "stringType";
    public static final String DATA_TYPE_DOUBLE = "doubleType";
    public static final String DATA_TYPE_LONG = "longType";
    public static final String DATA_TYPE_FLOAT = "floatType";
    public static final String DATA_TYPE_BOOL = "booleanType";
    public static final String HA_ACTIVE_STATUS = "Active";
    public static final String SIDDHI_APP_ACTIVE_STATUS = "Active";
    public static final String HA_PASIVE_STATUS = "pasive";
    public static final String WAITING_STATUS = "waiting";

    public static final String KAFKA_SOURCE = "source";
    public static final String KAFKA_SOURCE_TOPIC_LIST = "topic.list";
    public static final String KAFKA_SINK = "sink";
    public static final String KAFKA_SINK_TOPIC = "topic";

    public static final String NODE_ID = "nodeId";
    public static final String HTTPS_HOST = "https_host";
    public static final String HTTPS_PORT = "https_port";


    public static final String PROTOCOL = "https://";
    public static final String URL_HOST_PORT_SEPARATOR = ":";
    public static final int CLIENT_CONNECTION_TIMEOUT = 5000;
    public static final int CLIENT_READ_TIMEOUT = 5000;

    private Constants() {
        //preventing initialization
    }

}
