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
package org.wso2.carbon.databridge.agent.util;

import java.io.File;

/**
 * Class to define the constants that are used.
 */
public class DataEndpointConstants {

    private DataEndpointConstants() {
    }

    public static final String DATA_AGENT_CONF_FILE_NAME = "data.agent.config.yaml";
    public static final String DATA_AGENT_CONF_FILE_PATH = File.separator
            + "data-bridge" + File.separator + DATA_AGENT_CONF_FILE_NAME;
    public static final int DEFAULT_DATA_AGENT_BATCH_SIZE = 100;
    public static final String LB_URL_GROUP_SEPARATOR = ",";
    public static final String FAILOVER_URL_GROUP_SEPARATOR = "|";
    public static final String FAILOVER_URL_GROUP_SEPARATOR_REGEX = "\\|";
    public static final int DEFAULT_AUTH_PORT_OFFSET = 100;
    public static final String SEPARATOR = "##";

    public static final String THRIFT_DATA_AGENT_TYPE = "THRIFT";
    public static final String BINARY_DATA_AGENT_TYPE = "BINARY";

    public static final String SYNC_STRATEGY = "sync";
    public static final String ASYNC_STRATEGY = "async";

    public static final String DATA_AGENT_CONFIG_NAMESPACE = "data.agent.config";

}
