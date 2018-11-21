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

package org.wso2.carbon.sp.jobmanager.core.util;

/**
 * This class contains the constants needed for the distributed deployment.
 */
public class ResourceManagerConstants {
    public static final String RUNTIME_NAME_WORKER = "worker";

    public static final String RUNTIME_NAME_MANAGER = "manager";

    public static final String CLUSTER_CONFIG_NS = "cluster.config";

    public static final String DEPLOYMENT_CONFIG_NS = "deployment.config";

    public static final String MODE_DISTRIBUTED = "distributed";

    public static final String STATE_NEW = "NEW";

    public static final String STATE_EXISTS = "EXISTS";

    public static final String QUEUE_GROUP_NAME = "queueGroupName";

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

    public static final String DEFAULT_KAFKA_SOURCE_TEMPLATE = "@source(type='kafka', topic.list="
            + "'${" + TOPIC_LIST + "}', group.id='${" + CONSUMER_GROUP_ID + "}',"
            + " threading.option='single.thread', bootstrap.servers='${"
            + BOOTSTRAP_SERVER_URL + "}', @map(type='" + MAPPING + "'))";
    public static final String PARTITIONED_KAFKA_SOURCE_TEMPLATE =
            "@source(type='kafka', topic.list='${" + TOPIC_LIST + "}', group.id='${"
                    + CONSUMER_GROUP_ID + "}', threading.option='partition.wise',"
                    + " bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', "
                    + "partition.no.list='${" + PARTITION_LIST + "}',@map(type='" + MAPPING + "'))";

    public static final String DEFAULT_KAFKA_SINK_TEMPLATE = "@sink(type='kafka', topic='${"
            + TOPIC_LIST + "}' , bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', @map(type='"
            + MAPPING + "'))";

    public static final String PARTITIONED_KAFKA_SINK_TEMPLATE = "@sink(type='kafka', topic='${"
            + TOPIC_LIST + "}' , bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', @map(type='"
            + MAPPING + "'), @distribution(strategy='partitioned', partitionKey='${"
            + PARTITION_KEY + "}', ${" + DESTINATIONS + "} ))";

    public static final String PARTITION_TOPIC = "partitionTopic";

    public static final String MB_DESTINATION = "mbDestination";

    public static final String FACTORY_INITIAL = "factoryinitial";

    public static final String PROVIDER_URL = "providerurl";

    public static final String DESTINATION_TOPIC = "@destination(destination = '${"
            + PARTITION_TOPIC + "}')";
    public static final String CLIENT_ID = "clientid";

    public static final String CLUSTER_ID = "clusterid";

    public static final String NATS_SERVER_URL = "natsserverurl";

    public static final String DEFAULT_MB_TOPIC_SOURCE_TEMPLATE = "@source(type='jms',"
            + "factory.initial='${" + FACTORY_INITIAL + "}',"
            + "provider.url='${" + PROVIDER_URL + "}',connection.factory.type='topic',"
            + "destination ='${" + MB_DESTINATION  +  "}' , connection.factory.jndi.name="
            + "'TopicConnectionFactory',@map(type='" + MAPPING + "'))";

    public static final String DEFAULT_MB_QUEUE_SOURCE_TEMPLATE = "@source(type='jms',"
            + "factory.initial='${" + FACTORY_INITIAL + "}',"
            + "provider.url='${"  + PROVIDER_URL + "}',connection.factory.type='queue',"
            + "destination ='${" + MB_DESTINATION  +  "}',connection.factory.jndi.name="
            + "'QueueConnectionFactory',@map(type ='" + MAPPING + "'))";

    public static final String DEFAULT_MB_TOPIC_SINK_TEMPLATE = "@sink(type='jms',"
            + "factory.initial='${" + FACTORY_INITIAL + "}',"
            + "provider.url='${"  + PROVIDER_URL + "}',connection.factory.type='topic',"
            + "destination = '${" + MB_DESTINATION  +  "}', connection.factory.jndi.name="
            + "'TopicConnectionFactory',@map(type='" + MAPPING + "'))";

    public static final String DEFAULT_MB_QUEUE_SINK_TEMPLATE = "@sink(type='jms',"
            + "factory.initial='${" + FACTORY_INITIAL + "}',"
            + "provider.url='${" + PROVIDER_URL + "}',connection.factory.type='queue',"
            + "destination = '${" + MB_DESTINATION  +  "}', connection.factory.jndi.name="
            + "'QueueConnectionFactory',@map(type='" + MAPPING + "'))";

    public static final String PARTITIONED_MB_SINK_TEMPLATE = "@sink(type='jms',"
            + "factory.initial='${" + FACTORY_INITIAL + "}',"
            + "provider.url='${" + PROVIDER_URL
            + "}',@distribution(strategy='partitioned', partitionKey='${" + PARTITION_KEY + "}',"
            + "${" + DESTINATIONS + "}),connection.factory.type='topic',"
            + "connection.factory.jndi.name='TopicConnectionFactory',"
            + "@map(type='" + MAPPING + "'))";

    public static final String PARTITIONED_NATS_SINK_TEMPLATE = "@sink(type='nats',"
            + "cluster.id='${" + CLUSTER_ID + "}',"
            + "@distribution(strategy='partitioned', partitionKey='${" + PARTITION_KEY + "}',"
            + "${" + DESTINATIONS + "}), bootstrap.servers="
            + "'${" + NATS_SERVER_URL + "}',@map(type='" + MAPPING + "'))";

    public static final String DEFAULT_NATS_SINK_TEMPLATE = "@sink(type='nats',"
            + "cluster.id='${" + CLUSTER_ID + "}',"
            + "destination = '${" + TOPIC_LIST +  "}', bootstrap.servers="
            + "'${" + NATS_SERVER_URL + "}',@map(type='" + MAPPING + "'))";

    public static final String DEFAULT_NATS_SOURCE_TEMPLATE = "@source(type='nats',"
            + "cluster.id='${" + CLUSTER_ID + "}',"
            + "destination = '${" + TOPIC_LIST +  "}', bootstrap.servers="
            + "'${" + NATS_SERVER_URL + "}',@map(type='" + MAPPING + "'))";

    public static final String RR_NATS_SOURCE_TEMPLATE = "@source(type='nats',"
            + "cluster.id='${" + CLUSTER_ID + "}',"
            + "queue.group.name='${"  + QUEUE_GROUP_NAME + "}',"
            + "destination = '${" + TOPIC_LIST +  "}', bootstrap.servers="
            + "'${" + NATS_SERVER_URL + "}',@map(type='" + MAPPING + "'))";

    public static final String DESTINATION = "@destination(partition.no = '${" + PARTITION_NO
            + "}')";

    public static final String KEY_NODE_ID = "managerNodeId";

    public static final String KEY_NODE_MAX_RETRY = "heartbeatMaxRetry";

    public static final String KEY_NODE_INTERVAL = "heartbeatInterval";

    public static final String KEY_NODE_PROTOCOL = "httpsInterface";

    public static final String KEY_NODE_HOST = "httpsInterfaceHost";

    public static final String KEY_NODE_PORT = "httpsInterfacePort";

    public static final String KEY_NODE_USERNAME = "httpsInterfaceUsername";

    public static final String KEY_NODE_PASSWORD = "httpsInterfacePassword";

    public static final String TASK_UPSERT_RESOURCE_MAPPING = "Inserting/Updating resource mapping "
            + "group";

    public static final String TASK_GET_RESOURCE_MAPPING = "Getting resource mapping group";

    public static final String CREATE_RESOURCE_MAPPING_TABLE = "create_resource_mapping_table";

    public static final String CHECK_FOR_RESOURCE_MAPPING_TABLE = "check_for_resource_mapping_table";

    public static final String PS_DELETE_RESOURCE_MAPPING_ROW = "ps_delete_resource_mapping_row";

    public static final String PS_INSERT_RESOURCE_MAPPING_ROW = "ps_insert_resource_mapping_row";

    public static final String PS_SELECT_RESOURCE_MAPPING_ROW = "ps_select_resource_mapping_row";

    public static final String QUERY_YAML_FILE_NAME = "queries.yaml";

}
