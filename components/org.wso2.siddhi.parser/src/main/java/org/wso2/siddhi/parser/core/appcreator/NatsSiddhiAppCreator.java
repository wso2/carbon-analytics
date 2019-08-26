/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.parser.core.appcreator;

import org.wso2.siddhi.parser.core.topology.InputStreamDataHolder;
import org.wso2.siddhi.parser.core.topology.OutputStreamDataHolder;
import org.wso2.siddhi.parser.core.topology.PublishingStrategyDataHolder;
import org.wso2.siddhi.parser.core.topology.SiddhiQueryGroup;
import org.wso2.siddhi.parser.core.topology.SubscriptionStrategyDataHolder;
import org.wso2.siddhi.parser.core.util.TransportStrategy;
import org.wso2.siddhi.parser.service.model.MessagingSystem;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates distributed siddhi application which can be distributed using Nats-streaming.
 */
public class NatsSiddhiAppCreator extends AbstractSiddhiAppCreator {

    private static final Logger log = Logger.getLogger(NatsSiddhiAppCreator.class);
    //App creator constants
    public static final String APP_NAME = "appName";
    public static final String TOPIC_LIST = "topicList";
    public static final String CONSUMER_GROUP_ID = "groupID";
    public static final String BOOTSTRAP_SERVER_URL = "bootstrapServerURL";
    public static final String PARTITION_LIST = "partitionList";
    public static final String PARTITION_KEY = "partitionKey";
    public static final String DESTINATIONS = "destinations";
    public static final String PARTITION_NO = "partitionNo";
    public static final String MAPPING = "text";
    public static final String PARTITION_TOPIC = "partitionTopic";
    public static final String DESTINATION_TOPIC = "@destination(destination = '${"
            + PARTITION_TOPIC + "}')";
    public static final String CLUSTER_ID = "clusterid";
    public static final String NATS_SERVER_URL = "natsserverurl";
    public static final String PARTITIONED_NATS_SINK_TEMPLATE = "@sink(type='nats',"
            + "cluster.id='${" + CLUSTER_ID + "}',"
            + "@distribution(strategy='partitioned', partitionKey='${" + PARTITION_KEY + "}',"
            + "${" + DESTINATIONS + "}), bootstrap.servers="
            + "'${" + NATS_SERVER_URL + "}',@map(type='" + MAPPING + "'))";
    public static final String DEFAULT_NATS_SINK_TEMPLATE = "@sink(type='nats',"
            + "cluster.id='${" + CLUSTER_ID + "}',"
            + "destination = '${" + TOPIC_LIST + "}', bootstrap.servers="
            + "'${" + NATS_SERVER_URL + "}',@map(type='" + MAPPING + "'))";
    public static final String DEFAULT_NATS_SOURCE_TEMPLATE = "@source(type='nats',"
            + "cluster.id='${" + CLUSTER_ID + "}',"
            + "destination = '${" + TOPIC_LIST + "}', bootstrap.servers="
            + "'${" + NATS_SERVER_URL + "}',@map(type='" + MAPPING + "'))";
    public static final String QUEUE_GROUP_NAME = "queueGroupName";
    public static final String RR_NATS_SOURCE_TEMPLATE = "@source(type='nats',"
            + "cluster.id='${" + CLUSTER_ID + "}',"
            + "queue.group.name='${" + QUEUE_GROUP_NAME + "}',"
            + "destination = '${" + TOPIC_LIST + "}', bootstrap.servers="
            + "'${" + NATS_SERVER_URL + "}',@map(type='" + MAPPING + "'))";

    private String clusterId = "";
    private String natsServerUrl = "";

    @Override
    protected List<SiddhiQuery> createApps(String siddhiAppName, SiddhiQueryGroup queryGroup,
                                           MessagingSystem messagingSystem) {
        String groupName = queryGroup.getName();
        String queryTemplate = queryGroup.getSiddhiApp();
        List<SiddhiQuery> queryList = generateQueryList(queryTemplate, groupName, queryGroup
                .getParallelism());
        if (messagingSystem != null && messagingSystem.getConfig() != null) {
            natsServerUrl = messagingSystem.getConfig().getBootstrapServerURLs();
            clusterId = messagingSystem.getConfig().getClusterId();
        }
        processInputStreams(siddhiAppName, groupName, queryList, queryGroup.getInputStreams().values());
        processOutputStreams(siddhiAppName, queryList, queryGroup.getOutputStreams().values());
        if (log.isDebugEnabled()) {
            log.debug("Following parse list is created for the Siddhi Query Group " + queryGroup.getName() + " "
                    + "representing Siddhi App " + siddhiAppName + ".");
            for (SiddhiQuery siddhiQuery : queryList) {
                log.debug(siddhiQuery.getApp());
            }
        }
        return queryList;
    }

    /**
     * @param siddhiAppName Name of the initial user defined siddhi application.
     * @param queryList     Contains the parse of the current execution group replicated
     *                      to the parallelism of the group.
     * @param outputStreams Collection of current execution group's output streams
     *                      Assigns the nats sink configurations for output streams.
     */
    private void processOutputStreams(String siddhiAppName, List<SiddhiQuery> queryList,
                                      Collection<OutputStreamDataHolder> outputStreams) {

        Map<String, String> sinkValuesMap = new HashMap();
        sinkValuesMap.put(CLUSTER_ID, clusterId);
        sinkValuesMap.put(NATS_SERVER_URL, natsServerUrl);

        for (OutputStreamDataHolder outputStream : outputStreams) {
            Map<String, String> sinkList = new HashMap();
            Map<String, Integer> partitionKeys = new HashMap();

            for (PublishingStrategyDataHolder holder : outputStream.getPublishingStrategyList()) {
                sinkValuesMap.put("topicList", siddhiAppName + "_" +
                        outputStream.getStreamName() + (holder.getGroupingField() == null ? "" : ("_" + holder
                        .getGroupingField())));
                if (holder.getStrategy() == TransportStrategy.FIELD_GROUPING) {
                    if (partitionKeys.get(holder.getGroupingField()) != null &&
                            partitionKeys.get(holder.getGroupingField()) > holder.getParallelism()) {
                        continue;
                    }

                    partitionKeys.put(holder.getGroupingField(), holder.getParallelism());
                    sinkValuesMap.put(PARTITION_KEY, holder.getGroupingField());
                    List<String> destinations = new ArrayList(holder.getParallelism());

                    for (int i = 0; i < holder.getParallelism(); i++) {
                        Map<String, String> destinationMap = new HashMap(holder.getParallelism());
                        destinationMap.put(PARTITION_TOPIC,
                                sinkValuesMap.get(TOPIC_LIST)
                                        + "_" + String.valueOf(i));
                        destinations.add(getUpdatedQuery(DESTINATION_TOPIC,
                                destinationMap));
                    }

                    sinkValuesMap.put(DESTINATIONS,
                            StringUtils.join(destinations, ","));
                    String sinkString =
                            getUpdatedQuery(PARTITIONED_NATS_SINK_TEMPLATE,
                                    sinkValuesMap);
                    sinkList.put(sinkValuesMap.get(TOPIC_LIST),
                            sinkString);
                } else {
                    //ATM we are handling both strategies in same manner. Later will improve to have multiple
                    // partitions for RR
                    String sinkString = getUpdatedQuery(DEFAULT_NATS_SINK_TEMPLATE,
                            sinkValuesMap);
                    sinkList.put(sinkValuesMap.get(TOPIC_LIST), sinkString);
                }
            }
            Map<String, String> queryValuesMap = new HashMap(1);
            queryValuesMap.put(outputStream.getStreamName(), StringUtils.join(sinkList.values(), "\n"));
            updateQueryList(queryList, queryValuesMap);
        }
    }

    /**
     * @param siddhiAppName Name of the initial user defined siddhi application.
     * @param queryList     Contains the parse of the current execution group replicated
     *                      to the parallelism of the group.
     * @param inputStreams  Collection of current execution group's input streams
     *                      Assigns the nats source configurations for input streams.
     */
    private void processInputStreams(String siddhiAppName, String groupName, List<SiddhiQuery> queryList,
                                     Collection<InputStreamDataHolder> inputStreams) {

        Map<String, String> sourceValuesMap = new HashMap();
        for (InputStreamDataHolder inputStream : inputStreams) {
            SubscriptionStrategyDataHolder subscriptionStrategy = inputStream.getSubscriptionStrategy();
            sourceValuesMap.put(CLUSTER_ID, clusterId);
            sourceValuesMap.put(NATS_SERVER_URL, natsServerUrl);

            if (!inputStream.isUserGiven()) {
                if (subscriptionStrategy.getStrategy() == TransportStrategy.FIELD_GROUPING) {
                    sourceValuesMap.put(TOPIC_LIST, getTopicName(siddhiAppName,
                            inputStream.getStreamName(), inputStream.getSubscriptionStrategy().getPartitionKey()));
                    for (int i = 0; i < queryList.size(); i++) {
                        List<String> sourceQueries = new ArrayList();
                        List<Integer> partitionNumbers = getPartitionNumbers(queryList.size(), subscriptionStrategy
                                .getOfferedParallelism(), i);
                        for (int topicCount : partitionNumbers) {
                            String topicName = getTopicName(siddhiAppName, inputStream.getStreamName(),
                                    inputStream.getSubscriptionStrategy().getPartitionKey()) + "_"
                                    + Integer.toString(topicCount);

                            sourceValuesMap.put(TOPIC_LIST, topicName);
                            String sourceQuery = getUpdatedQuery(DEFAULT_NATS_SOURCE_TEMPLATE, sourceValuesMap);
                            sourceQueries.add(sourceQuery);
                        }

                        String combinedQueryHeader = StringUtils.join(sourceQueries,
                                System.lineSeparator());
                        Map<String, String> queryValuesMap = new HashMap(1);
                        queryValuesMap.put(inputStream.getStreamName(), combinedQueryHeader);
                        String updatedQuery = getUpdatedQuery(queryList.get(i).getApp()
                                , queryValuesMap);
                        queryList.get(i).setApp(updatedQuery);
                    }

                } else if (subscriptionStrategy.getStrategy() == TransportStrategy.ROUND_ROBIN) {
                    sourceValuesMap.put(TOPIC_LIST, getTopicName(siddhiAppName,
                            inputStream.getStreamName(), null));
                    sourceValuesMap.put(QUEUE_GROUP_NAME, groupName);
                    String sourceString = getUpdatedQuery(RR_NATS_SOURCE_TEMPLATE, sourceValuesMap);
                    Map<String, String> queryValuesMap = new HashMap(1);
                    queryValuesMap.put(inputStream.getStreamName(), sourceString);
                    updateQueryList(queryList, queryValuesMap);

                } else if (subscriptionStrategy.getStrategy() == TransportStrategy.ALL) {

                    sourceValuesMap.put(TOPIC_LIST, getTopicName(siddhiAppName,
                            inputStream.getStreamName(), null));
                    for (SiddhiQuery aQueryList : queryList) {
                        String sourceString = getUpdatedQuery(DEFAULT_NATS_SOURCE_TEMPLATE, sourceValuesMap);
                        Map<String, String> queryValuesMap = new HashMap(1);
                        queryValuesMap.put(inputStream.getStreamName(), sourceString);
                        String updatedQuery = getUpdatedQuery(aQueryList.getApp(), queryValuesMap);
                        aQueryList.setApp(updatedQuery);
                    }
                }
            }
        }
    }
}
