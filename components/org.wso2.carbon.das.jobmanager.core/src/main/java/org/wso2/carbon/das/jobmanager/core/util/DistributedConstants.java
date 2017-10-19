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

package org.wso2.carbon.das.jobmanager.core.util;

/**
 * This class contains the constants needed for the distributed deployment.
 */
public class DistributedConstants {
    public static final String RUNTIME_NAME_WORKER = "worker";

    public static final String RUNTIME_NAME_MANAGER = "default"; // TODO: 10/15/17 For testing, change to manager

    public static final String CLUSTER_CONFIG_NS = "cluster.config";

    public static final String MODE_DISTRIBUTED = "distributed";

    public static final String MODE_HA = "ha";

    //App creator constants
    public static final String APP_NAME = "appName";
    public static final String TOPIC_LIST = "topicList";
    public static final String CONSUMER_GROUP_ID = "groupID";
    public static final String BOOTSTRAP_SERVER_URL = "bootstrapServerURL";
    public static final String PARTITION_LIST = "partitionList";
    public static final String PARTITION_KEY = "partitionKey";
    public static final String DESTINATIONS = "destinations";
    public static final String PARTITION_NO = "partitionNo";
    public static final String MAPPING = "xml";

    public static final String DEFAULT_KAFKA_SOURCE_TEMPLATE = "@source(type='kafka', topic.list='${" + TOPIC_LIST +
            "}', group.id='${" + CONSUMER_GROUP_ID + "}', threading.option='single.thread', bootstrap.servers='${"
            + BOOTSTRAP_SERVER_URL + "}', @map(type='" + MAPPING + "'))";
    public static final String PARTITIONED_KAFKA_SOURCE_TEMPLATE =
            "@source(type='kafka', topic.list='${" + TOPIC_LIST + "}', group.id='${" + CONSUMER_GROUP_ID + "}', "
                    + "threading.option='partition.wise', bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', "
                    + "partition.no.list='${" + PARTITION_LIST + "}',@map(type='" + MAPPING + "'))";

    public static final String DEFAULT_KAFKA_SINK_TEMPLATE = "@sink(type='kafka', topic='${" + TOPIC_LIST +
            "}' , bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', @map(type='" + MAPPING + "'))";

    public static final String PARTITIONED_KAFKA_SINK_TEMPLATE = "@sink(type='kafka', topic='${" + TOPIC_LIST +
            "}' , bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', @map(type='" + MAPPING + "'), @distribution"
            + "(strategy='partitioned', partitionKey='${" + PARTITION_KEY + "}', ${" + DESTINATIONS + "} ))";

    public static final String DESTINATION = "@destination(partition.no = '${" + PARTITION_NO + "}')";

}
