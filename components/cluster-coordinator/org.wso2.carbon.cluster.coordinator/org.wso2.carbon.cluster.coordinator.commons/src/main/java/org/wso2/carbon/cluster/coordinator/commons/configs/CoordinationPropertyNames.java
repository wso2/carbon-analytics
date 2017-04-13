/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.cluster.coordinator.commons.configs;

/**
 * This class contains the configuration constants needed for the RDBMS coordination algorithm.
 */
public class CoordinationPropertyNames {
    /**
     * Name of the coordination strategy class.
     */
    public static final String RDBMS_COORDINATION_STRATERGY =
            "org.wso2.carbon.cluster.coordinator.rdbms.RDBMSCoordinationStrategy";
    /**
     * Name of the coordination strategy class.
     */
    public static final String ZOOKEEPER_COORDINATION_STRATERGY =
            "org.wso2.carbon.cluster.coordinator.zookeeper.ZookeeperCoordinationStrategy";
    /**
     * Coordination heartbeat interval property name which the nodes will be updating the database heartbeats.
     */
    public static final String RDBMS_BASED_COORDINATION_HEARTBEAT_INTERVAL = "heartbeatInterval";
    /**
     * A node will poll the databases with this time interval property name.
     */
    public static final String RDBMS_BASED_EVENT_POLLING_INTERVAL = "eventPollingInterval";
    /**
     * A node will poll the databases with this time interval property name.
     */
    public static final String RDBMS_BASED_PERFORM_TASK_THREAD_COUNT = "taskThreadCount";

    /**
     * Zookeeper connection string property name
     */
    public static final String ZOOKEEPER_CONNECTION_STRING = "connectionString";

    /**
     * This class does not need to be instantiated as all the variables are static and public.
     */
    private CoordinationPropertyNames() {
    }
}
