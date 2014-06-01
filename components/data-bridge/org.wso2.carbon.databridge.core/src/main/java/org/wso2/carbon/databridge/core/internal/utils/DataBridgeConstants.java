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

package org.wso2.carbon.databridge.core.internal.utils;


/**
 * Agent Server Constants
 */
public final class DataBridgeConstants {

    public static final String DATA_BRIDGE_DIR = "data-bridge";

    private DataBridgeConstants() {
    }

    public static final int NO_OF_WORKER_THREADS = 10;
    public static final int EVENT_BUFFER_CAPACITY = 10000;
    public static final int CLIENT_TIMEOUT_MS = 30000;

    public static final String STREAM_DEFINITIONS_XML = "stream-definitions.xml";
    public static final String DATA_BRIDGE_CONFIG_XML = "data-bridge-config.xml";
    public static final String DATA_BRIDGE_NAMESPACE = "http://wso2.org/carbon/databridge";

    public static final String DATA_BRIDGE_ROOT_ELEMENT = "dataBridgeConfiguration";
    public static final String STREAM_DEFINITION_STORE_ELEMENT = "StreamDefinitionStore";
    public static final String STREAM_DEFINITIONS_ELEMENT = "streamDefinitions";
    public static final String DOMAIN_NAME_ATTRIBUTE = "domainName";

    public static final String WORKER_THREADS_ELEMENT = "workerThreads";
    public static final String EVENT_BUFFER_CAPACITY_ELEMENT = "eventBufferCapacity";
    public static final String CLIENT_TIMEOUT_ELEMENT = "clientTimeoutMS";

    public static final String DEFAULT_DEFINITION_STORE = "org.wso2.carbon.databridge.core.definitionstore.InMemoryStreamDefinitionStore";
}
