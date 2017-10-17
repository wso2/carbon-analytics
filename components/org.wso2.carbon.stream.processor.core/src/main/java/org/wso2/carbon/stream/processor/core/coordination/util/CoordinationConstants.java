/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.stream.processor.core.coordination.util;

/**
 * Class that holds Constants used for clustering
 */
public class CoordinationConstants {

    public static final String CLUSTER_CONFIG_NS = "cluster.config";

    public static final String CLUSTER_STRATEGY_CONFIG_NS = "strategyConfig";

    public static final String CLUSTER_MODE_CONFIG_NS = "modeConfig";

    public static final String GROUP_ID = "groupId";

    public static final String CLUSTER_MODE_TYPE = "type";

    public static final String MODE_HA = "ha";

    public static final String LIVE_STATE_SYNC = "liveStateSyncEnabled";

    public static final String ADVERTISED_HOST = "advertisedHost";

    public static final String ADVERTISED_PORT = "advertisedPort";

    public static final String SYNC_GRACE_PERIOD = "syncGracePeriod";

    public static final String PUBLISHER_SYNC_INTERVAL = "publisherSyncInterval";

    public static final String SINK_QUEUE_CAPACITY = "sinkQueueCapacity";

    public static final String SOURCE_QUEUE_CAPACITY = "sourceQueueCapacity";

    public static final String RETRY_APP_SYNC_PERIOD = "retryAppSyncPeriod";
}
