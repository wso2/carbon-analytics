/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.processor.manager.core.config;

/**
 * Configuration Constants
 */
public final class ConfigurationConstants {

    public static final String CEP_MANAGEMENT_XML = "event-processing.xml";

    public static final int CARBON_DEFAULT_PORT_OFFSET = 0;
    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    public static final String CONFIG_ELEMENT = "eventProcessingConfig";
    public static final String PROCESSING_ELEMENT = "processing";
    public static final String PROCESSING_MODE_ATTRIBUTE = "mode";

    public static final String RECEIVER_HOST_NAME = "hostName";
    public static final String PORT_ELEMENT = "port";

    public static final String PROCESSING_MODE_HA = "HA";
    public static final String HA_TRANSPORT_ELEMENT = "transport";
    public static final String HA_MANAGEMENT_ELEMENT = "management";
    public static final String HA_RECONNECTION_INTERVAL_ELEMENT = "reconnectionInterval";
    public static final int HA_DEFAULT_TRANSPORT_PORT = 11224;
    public static final int HA_DEFAULT_RECONNECTION_INTERVAL = 20000;
    public static final int HA_DEFAULT_MANAGEMENT_PORT = 11324;

    public static final String PROCESSING_MODE_SN = "SingleNode";
    public static final String SN_PERSISTENCE_ELEMENT = "persistence";
    public static final String SN_PERSISTENCE_PERSIST_CLASS_ELEMENT = "persister";
    public static final String SN_PERSISTENCE_CLASS_ATTRIBUTE = "class";
    public static final String SN_PERSISTENCE_INTERVAL_ELEMENT = "persistenceIntervalInMillis";
    public static final String SN_PERSISTENCE_THREAD_POOL_SIZE  = "persisterSchedulerPoolSize" ;
    public static final String SN_DEFAULT_PERSISTENCE_STORE =
            "org.wso2.carbon.event.processor.core.internal.persistence.FileSystemPersistenceStore";
    public static final long SN_DEFAULT_PERSISTENCE_INTERVAL = 1000000;
    public static final int SN_DEFAULT_PERSISTENCE_THREAD_POOL_SIZE = 10;

    public static final String PROCESSING_MODE_DISTRIBUTED = "Distributed";
    public static final String DISTRIBUTED_NODE_CONFIG_ELEMENT = "nodeType";
    public static final String DISTRIBUTED_NODE_CONFIG_RECEIVER_ELEMENT = "receiver";
    public static final String DISTRIBUTED_NODE_CONFIG_PUBLISHER_ELEMENT = "publisher";
    public static final String DISTRIBUTED_NODE_CONFIG_MANAGER_ELEMENT = "manager";
    public static final String ENABLE_ATTRIBUTE = "enable";
}
