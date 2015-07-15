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
package org.wso2.carbon.event.processor.manager.core.internal.util;

/**
 * Configuration Constants
 */
public final class ConfigurationConstants {

    private ConfigurationConstants() {
    }

    public static final String CEP_MANAGEMENT_XML = "event-processor.xml";

    public static final String CARBON_CONFIG_PORT_OFFSET_NODE = "Ports.Offset";
    public static final String MODE_ELEMENT = "mode";
    public static final String PROCESSING_MODE_NAME_ATTRIBUTE = "name";

    public static final String HOST_NAME_ELEMENT = "hostName";
    public static final String PORT_ELEMENT = "port";
    public static final String MANAGEMENT_ELEMENT = "management";
    public static final String RECONNECTION_INTERVAL_ELEMENT = "reconnectionInterval";
    public static final String TRANSPORT_ELEMENT = "transport";
    public static final String EVENT_SYNC_ELEMENT = "eventSync";


    public static final String PROCESSING_MODE_HA = "HA";

    public static final int HA_DEFAULT_TRANSPORT_PORT = 11224;
    public static final int HA_DEFAULT_RECONNECTION_INTERVAL = 20000;
    public static final int HA_DEFAULT_MANAGEMENT_PORT = 11324;
    public static final String HA_NODE_ACTIVE_STATE = "active";

    public static final String PROCESSING_MODE_SN = "SingleNode";
    public static final String SN_PERSISTENCE_ELEMENT = "persistence";
    public static final String SN_PERSISTENCE_PERSIST_CLASS_ELEMENT = "persister";
    public static final String SN_PERSISTENCE_CLASS_ATTRIBUTE = "class";
    public static final String SN_PERSISTENCE_INTERVAL_ELEMENT = "persistenceIntervalInMinutes";
    public static final String SN_PERSISTENCE_THREAD_POOL_SIZE  = "persisterSchedulerPoolSize" ;
    public static final String SN_PERSISTENCE_PERSIST_CLASS_PROPERTY = "property";
    public static final String SN_PERSISTENCE_PERSIST_CLASS_PROPERTY_KEY = "key";
    public static final String SN_DEFAULT_PERSISTENCE_STORE =
            "org.wso2.carbon.event.processor.core.internal.persistence.FileSystemPersistenceStore";
    public static final long SN_DEFAULT_PERSISTENCE_INTERVAL = 15;
    public static final int SN_DEFAULT_PERSISTENCE_THREAD_POOL_SIZE = 10;

    public static final String PROCESSING_MODE_DISTRIBUTED = "Distributed";
    public static final String DISTRIBUTED_NODE_CONFIG_ELEMENT = "nodeType";
    public static final String DISTRIBUTED_NODE_CONFIG_WORKER_ELEMENT = "worker";
    public static final String DISTRIBUTED_NODE_CONFIG_MANAGER_ELEMENT = "manager";
    public static final String DISTRIBUTED_NODE_CONFIG_MANAGERS_ELEMENT = "managers";
    public static final String DISTRIBUTED_NODE_CONFIG_HEARTBEAT_INTERVAL_ELEMENT = "heartbeatInterval";
    public static final String DISTRIBUTED_NODE_CONFIG_TOPOLOGY_RESUBMIT_INTERVAL_ELEMENT = "topologyResubmitInterval";
    public static final String DISTRIBUTED_NODE_CONFIG_PORT_RANGE_ELEMENT = "portRange";
    public static final String DISTRIBUTED_NODE_CONFIG_DISTRIBUTED_UI_URL_ELEMENT = "distributedUIUrl";
    public static final String DISTRIBUTED_NODE_CONFIG_STORM_JAR_ELEMENT = "stormJar";
    public static final String ENABLE_ATTRIBUTE = "enable";

    public static final String PREFIX = "org.wso2.cep.org.wso2.carbon.event.processor.management";
    public static final String ROLE_MEMBERSHIP_MAP = PREFIX + "role_membership_map";
    public static final String ACTIVEID = PREFIX + "Active";
    public static final String PASSIVEID = PREFIX + "Passive";
    public static final String MEMBERS = PREFIX + "members";
    public static final long AXIS_TIME_INTERVAL_IN_MILLISECONDS = 10000;
    public static enum HAMode {Active, Passive, Backup}

    public static final String STORM_EVENT_PUBLISHER_SYNC_MAP = "stormEventPublisherSyncMap";
}
