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

    public static final String DEFAULT_KAFKA_SOURCE_TEMPLATE = "@source(type='kafka', topic.list='${" + TOPIC_LIST +
            "}', group.id='${" + CONSUMER_GROUP_ID + "}', threading.option='partition.wise', bootstrap.servers='${"
            + BOOTSTRAP_SERVER_URL + "}', @map(type='binary'))";
    public static final String PARTITIONED_KAFKA_SOURCE_TEMPLATE =
            "@source(type='kafka', topic.list='${" + TOPIC_LIST + "}', group.id='${" + CONSUMER_GROUP_ID + "}', "
                    + "threading.option='partition.wise', bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', "
                    + "partition.no.list='${" + PARTITION_LIST + "}',@map(type='binary'))";

    public static final String DEFAULT_KAFKA_SINK_TEMPLATE = "@sink(type='kafka', topic='${" + TOPIC_LIST +
            "}' , bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', @map(type='binary'))";

    public static final String PARTITIONED_KAFKA_SINK_TEMPLATE = "@sink(type='kafka', topic='${" + TOPIC_LIST +
            "}' , bootstrap.servers='${" + BOOTSTRAP_SERVER_URL + "}', @map(type='binary'), @distribution"
            + "(strategy='partitioned', partitionKey='${" + PARTITION_KEY + "}', ${" + DESTINATIONS + "} )";

    public static final String DESTINATION = "@destination(partition.no = ${" + PARTITION_NO + "})";

}
