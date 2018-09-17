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

package org.wso2.carbon.sp.jobmanager.core.appcreator;

import kafka.admin.AdminUtils;
import kafka.utils.ZkUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.bean.ZooKeeperConfig;
import org.wso2.carbon.sp.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.sp.jobmanager.core.topology.InputStreamDataHolder;
import org.wso2.carbon.sp.jobmanager.core.topology.OutputStreamDataHolder;
import org.wso2.carbon.sp.jobmanager.core.topology.PublishingStrategyDataHolder;
import org.wso2.carbon.sp.jobmanager.core.topology.SiddhiQueryGroup;
import org.wso2.carbon.sp.jobmanager.core.topology.SubscriptionStrategyDataHolder;
import org.wso2.carbon.sp.jobmanager.core.util.ResourceManagerConstants;
import org.wso2.carbon.sp.jobmanager.core.util.SiddhiTopologyCreatorConstants;
import org.wso2.carbon.sp.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * Creates distributed siddhi application.
 */
public class SPMBSiddhiAppCreator extends AbstractSiddhiAppCreator {
    private static final Logger log = Logger.getLogger(SPSiddhiAppCreator.class);
    private static final int TIMEOUT = 120;

    @Override
    protected List<SiddhiQuery> createApps(String siddhiAppName, SiddhiQueryGroup queryGroup) {
        String groupName = queryGroup.getName();
        String queryTemplate = queryGroup.getSiddhiApp();
        List<SiddhiQuery> queryList = generateQueryList(queryTemplate, siddhiAppName, groupName, queryGroup
                .getParallelism());
        processInputStreams(siddhiAppName, groupName, queryList, queryGroup.getInputStreams().values());
        processOutputStreams(siddhiAppName, groupName, queryList, queryGroup.getOutputStreams().values());
        return queryList;
    }

    /**
     *
     * @param siddhiAppName Name of the initial user defined siddhi application
     * @param groupName     Name of the currently processing execution group
     * @param queryList     Contains the query of the current execution group replicated
     *                      to the parallelism of the group.
     * @param outputStreams Collection of current execution group's output streams
     *
     * Assign the jms transport headers for each instance siddhi applications output streams for given execution group
     */
    private void processOutputStreams(String siddhiAppName, String groupName, List<SiddhiQuery> queryList,
                                      Collection<OutputStreamDataHolder> outputStreams) {
        //Store the data for sink stream header
        Map<String, String> sinkValuesMap = new HashMap<>();
        for (OutputStreamDataHolder outputStream : outputStreams) {

            //Contains the header string for each stream
            Map<String, String> sinkList = new HashMap<>();

            //contains the parallelism count for each partition
            Map<String, Integer> partitionKeys = new HashMap<>();

            //to keep track of multiple RR holders in a single output stream
            int rrHolderCount = 0;

            for (PublishingStrategyDataHolder holder : outputStream.getPublishingStrategyList()) {

                sinkValuesMap.put(ResourceManagerConstants.MB_DESTINATION, siddhiAppName + "_" +
                        outputStream.getStreamName() + (holder.getGroupingField() == null ? "" : ("_" + holder
                        .getGroupingField())));
                if (holder.getStrategy() == TransportStrategy.FIELD_GROUPING) {
                    if (partitionKeys.get(holder.getGroupingField()) != null &&
                            partitionKeys.get(holder.getGroupingField()) > holder.getParallelism()) {
                        continue;
                    }
                    //Remove if there is any previous R/R or ALL publishing
                    sinkValuesMap.remove(siddhiAppName + "_" + outputStream.getStreamName());

                    //add or replace new partition key and parallelism
                    partitionKeys.put(holder.getGroupingField(), holder.getParallelism());
                    //add partition key  for header creation
                    sinkValuesMap.put(ResourceManagerConstants.PARTITION_KEY, holder.getGroupingField());
                    //use to add destination annotation
                    List<String> destinations = new ArrayList<>(holder.getParallelism());
                    for (int i = 0; i < holder.getParallelism(); i++) {
                        Map<String, String> destinationMap = new HashMap<>(holder.getParallelism());
                        //create destination annotation
                        destinationMap.put(ResourceManagerConstants.PARTITION_TOPIC,
                                sinkValuesMap.get(ResourceManagerConstants.MB_DESTINATION)
                                        + "_" + String.valueOf(i));
                        //create the distribution full annotation for each strategy
                        destinations.add(getUpdatedQuery(ResourceManagerConstants.DESTINATION_TOPIC, destinationMap));
                    }

                    //create the final distribution annotation
                    sinkValuesMap.put(ResourceManagerConstants.DESTINATIONS,
                            StringUtils.join(destinations, ","));
                    String sinkString = getUpdatedQuery(ResourceManagerConstants.PARTITIONED_MB_SINK_TEMPLATE,
                            sinkValuesMap);
                    sinkList.put(sinkValuesMap.get(ResourceManagerConstants.MB_DESTINATION), sinkString);

                } else if (holder.getStrategy() == TransportStrategy.ROUND_ROBIN) {



                    if (partitionKeys.isEmpty()) {
                        //if holder uses RR as strategy then unique topic name will be defined
                        sinkValuesMap.put(ResourceManagerConstants.MB_DESTINATION, siddhiAppName + "_" +
                                outputStream.getStreamName() + "_" + String.valueOf(rrHolderCount));
                        rrHolderCount++;

                        String sinkString = getUpdatedQuery(ResourceManagerConstants.DEFAULT_MB_QUEUE_SINK_TEMPLATE,
                                sinkValuesMap);
                        sinkList.put(sinkValuesMap.get(ResourceManagerConstants.MB_DESTINATION), sinkString);
                    }

                } else if (holder.getStrategy() == TransportStrategy.ALL) {
                    String sinkString = getUpdatedQuery(ResourceManagerConstants.DEFAULT_MB_TOPIC_SINK_TEMPLATE,
                            sinkValuesMap);
                    sinkList.put(sinkValuesMap.get(ResourceManagerConstants.MB_DESTINATION), sinkString);
                }
            }
            Map<String, String> queryValuesMap = new HashMap<>(1);
            queryValuesMap.put(outputStream.getStreamName(), StringUtils.join(sinkList.values(),  "\n"));
            updateQueryList(queryList, queryValuesMap);

        }
    }

    private void createTopicPartitions(Map<String, Integer> topicParallelismMap) {
        ZkUtils zkUtils;
        String[] bootstrapServerURLs = null;
        SafeZkClient safeZkClient;
        String bootstrapServerURL = ServiceDataHolder.getDeploymentConfig().getBootstrapURLs();
        ZooKeeperConfig zooKeeperConfig = ServiceDataHolder.getDeploymentConfig().getZooKeeperConfig();
        String zooKeeperServerURL;
        if (zooKeeperConfig != null) {
            zooKeeperServerURL = zooKeeperConfig.getZooKeeperURLs();
        } else {
            zooKeeperServerURL = ServiceDataHolder.getDeploymentConfig().getZooKeeperURLs();
            zooKeeperConfig = new ZooKeeperConfig();
            log.warn("Deprecated configuration used to specify zooKeeperURLs." +
                    " Please refer official documentation for latest configurations.");
        }
        if (zooKeeperServerURL == null) {
            throw new SiddhiAppCreationException("ZooKeeper URLs are not provided " +
                    "in deployment.yaml under deployment.config. Hence cannot check existence of topics.");
        } else {
            String[] zooKeeperServerURLs = zooKeeperServerURL.replaceAll("\\s+", "").split(",");
            boolean isSecureKafkaCluster = false;
            safeZkClient = new SafeZkClient();
            zkUtils = safeZkClient.createZkClient(zooKeeperServerURLs, isSecureKafkaCluster,
                    zooKeeperConfig.getSessionTimeout(), zooKeeperConfig.getConnectionTimeout());
        }

        if (bootstrapServerURL != null) {
            bootstrapServerURLs = bootstrapServerURL.replaceAll("\\s+", "").split(",");
        } else if (transportChannelCreationEnabled) {
            throw new SiddhiAppCreationException("Bootstrap server URLs  are not provided " +
                    "in deployment.yaml under deployment.config. Hence cannot create required topics.");
        }

        Properties topicConfig = new Properties();
        for (Map.Entry<String, Integer> entry : topicParallelismMap.entrySet()) {
            String topic = entry.getKey();
            Integer partitions = entry.getValue();
            if (AdminUtils.topicExists(zkUtils, topic)) {
                int existingPartitions = AdminUtils.fetchTopicMetadataFromZk(topic, zkUtils).partitionMetadata()
                        .size();
                if (existingPartitions < partitions) {
                    (new SafeKafkaInvoker()).addKafkaPartition(zkUtils, topic, partitions);
                    log.info("Added " + partitions + " partitions to topic " + topic);
                } else if (existingPartitions > partitions) {
                    if (transportChannelCreationEnabled) {
                        log.info("Topic " + topic + " has higher number of partitions than expected partition count."
                                + " Hence have to delete the topic and recreate with " + partitions + "partitions.");
                        AdminUtils.deleteTopic(zkUtils, topic);
                        long startTime = System.currentTimeMillis();
                        while (AdminUtils.topicExists(zkUtils, topic)) {
                            try {
                                TimeUnit.SECONDS.sleep(1);
                                if (System.currentTimeMillis() - startTime > TIMEOUT * 1000L) {
                                    break;
                                }
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        if (!AdminUtils.topicExists(zkUtils, topic)) {
                            (new SafeKafkaInvoker()).createKafkaTopic(bootstrapServerURLs, zkUtils, topicConfig, topic,
                                    partitions);
                            log.info("Created topic " + topic + " with " + partitions + " partitions.");
                        } else {
                            throw new SiddhiAppCreationException("Topic " + topic + " deletion failed. Hence Could not "
                                    + "create new topic to facilitate new partitions."
                            );
                        }
                    } else {
                        throw new SiddhiAppCreationException("Number of partitions in the existing topic has higher " +
                                "number of partitions than the expected count. Hence need to delete and recreate " +
                                "topic with " + partitions + "partitions.  User has disabled topic creation by " +
                                "setting "  + SiddhiTopologyCreatorConstants.TRANSPORT_CHANNEL_CREATION_IDENTIFIER +
                                " property to false. Hence new topics can't be created and Siddhi App deployemnt " +
                                "will be aborted.");
                    }
                }
            } else {
                if (transportChannelCreationEnabled) {
                    new SafeKafkaInvoker().createKafkaTopic(bootstrapServerURLs, zkUtils, topicConfig, topic,
                            partitions);
                    log.info("Created topic " + topic + " with " + partitions + " partitions.");
                } else {
                    throw new SiddhiAppCreationException("Topic " + topic + " creation failed. User has disabled " +
                            "topic creation by setting " +
                            SiddhiTopologyCreatorConstants.TRANSPORT_CHANNEL_CREATION_IDENTIFIER +
                            " property to false. Hence Siddhi App deployment will be aborted.");
                }
            }
        }
        safeZkClient.closeClient();
    }

    private Map<String, Integer> rrTrackerMap = new HashMap<>();
    /**
     *
     * @param siddhiAppName Name of the initial user defined siddhi application
     * @param groupName     Name of the currently processing execution group
     * @param queryList     Contains the query of the current execution group replicated
     *                      to the parallelism of the group.
     * @param inputStreams  Collection of current execution group's input streams
     *
     * Assign the jms transport headers for each instance siddhi applications input
     *                      streams for a given execution group
     */
    private void processInputStreams(String siddhiAppName, String groupName, List<SiddhiQuery> queryList,
                                     Collection<InputStreamDataHolder> inputStreams) {
        Map<String, String> sourceValuesMap = new HashMap<>();


        for (InputStreamDataHolder inputStream : inputStreams) {

            SubscriptionStrategyDataHolder subscriptionStrategy = inputStream.getSubscriptionStrategy();
            sourceValuesMap.put(ResourceManagerConstants.MB_DESTINATION, siddhiAppName + "_" +
                    inputStream.getStreamName() + (inputStream.getSubscriptionStrategy().getPartitionKey() ==
                    null ? "" : ("_" + inputStream.getSubscriptionStrategy().getPartitionKey())));

            if (!inputStream.isUserGiven()) {
                if (subscriptionStrategy.getStrategy() == TransportStrategy.FIELD_GROUPING) {

                    for (int i = 0; i < queryList.size(); i++) {
                        List<String> sourceQueries = new ArrayList<>();
                        //consider the each instance source code of single siddhi execution group assigns
                        //then the particular stream headers produced
                        List<Integer> partitionNumbers = getPartitionNumbers(queryList.size(), subscriptionStrategy
                                .getOfferedParallelism(), i);
                        for (int topicCount : partitionNumbers) {
                            String topicName = siddhiAppName + "_" + inputStream.getStreamName()
                                    + "_" + inputStream.getSubscriptionStrategy().getPartitionKey()
                                    + "_" + Integer.toString(topicCount);
                            sourceValuesMap.put(ResourceManagerConstants.MB_DESTINATION, topicName);


                            String sourceQuery =
                                    getUpdatedQuery(ResourceManagerConstants.DEFAULT_MB_TOPIC_SOURCE_TEMPLATE,
                                            sourceValuesMap);
                            sourceQueries.add(sourceQuery);
                        }

                        String combinedQueryHeader = StringUtils.join(sourceQueries, "\n");
                        //
                        Map<String, String> queryValuesMap = new HashMap<>(1);
                        queryValuesMap.put(inputStream.getStreamName(), combinedQueryHeader);
                        String updatedQuery = getUpdatedQuery(queryList.get(i).getApp(), queryValuesMap);
                        queryList.get(i).setApp(updatedQuery);

                    }
                } else if (subscriptionStrategy.getStrategy() == TransportStrategy.ROUND_ROBIN) {

                    String queueName;
                    int queueCount;
                    if (rrTrackerMap.get(inputStream.getStreamName()) != null) {
                        queueCount = rrTrackerMap.get(inputStream.getStreamName());
                        queueName = siddhiAppName + "_" + inputStream.getStreamName() + "_"
                                + Integer.toString(queueCount);
                        queueCount += 1;
                        rrTrackerMap.put(inputStream.getStreamName(), queueCount);

                    } else {
                        queueCount = 0;
                        rrTrackerMap.put(inputStream.getStreamName(), queueCount);
                        queueName = siddhiAppName + "_" + inputStream.getStreamName() + "_"
                                + Integer.toString(queueCount);
                        queueCount += 1;
                        rrTrackerMap.put(inputStream.getStreamName(), queueCount);
                    }

                    sourceValuesMap.put(ResourceManagerConstants.MB_DESTINATION, queueName);
                    String sourceString = getUpdatedQuery(ResourceManagerConstants.DEFAULT_MB_QUEUE_SOURCE_TEMPLATE,
                            sourceValuesMap);
                    Map<String, String> queryValuesMap = new HashMap<>(1);
                    queryValuesMap.put(inputStream.getStreamName(), sourceString);
                    updateQueryList(queryList, queryValuesMap);
                } else {

                    sourceValuesMap.put(ResourceManagerConstants.MB_DESTINATION, siddhiAppName + "_" +
                            inputStream.getStreamName());

                    for (int i = 0; i < queryList.size(); i++) {

                        String sourceString = getUpdatedQuery(ResourceManagerConstants.DEFAULT_MB_TOPIC_SOURCE_TEMPLATE,
                                sourceValuesMap);

                        Map<String, String> queryValuesMap = new HashMap<>(1);
                        queryValuesMap.put(inputStream.getStreamName(), sourceString);
                        String updatedQuery = getUpdatedQuery(queryList.get(i).getApp(), queryValuesMap);
                        queryList.get(i).setApp(updatedQuery);
                    }
                }
            }
        }
    }

    private List<Integer> getPartitionNumbers(int appParallelism, int availablePartitionCount, int currentAppNum) {
        List<Integer> partitionNumbers = new ArrayList<>();
        if (availablePartitionCount == appParallelism) {
            partitionNumbers.add(currentAppNum);
            return partitionNumbers;
        } else {
            //availablePartitionCount < appParallelism scenario cannot occur according to design. Hence if
            // availablePartitionCount > appParallelism
            //// TODO: 10/19/17 improve logic
            int partitionsPerNode = availablePartitionCount / appParallelism;
            if (currentAppNum + 1 == appParallelism) { //if last app
                int remainingPartitions = availablePartitionCount - ((appParallelism - 1) * partitionsPerNode);
                for (int j = 0; j < remainingPartitions; j++) {
                    partitionNumbers.add((currentAppNum * partitionsPerNode) + j);
                }
                return partitionNumbers;
            } else {
                for (int j = 0; j < partitionsPerNode; j++) {
                    partitionNumbers.add((currentAppNum * partitionsPerNode) + j);
                }
                return partitionNumbers;
            }
        }
    }

    private List<SiddhiQuery> generateQueryList(String queryTemplate, String parentAppName, String queryGroupName, int
            parallelism) {
        List<SiddhiQuery> queries = new ArrayList<>(parallelism);
        for (int i = 0; i < parallelism; i++) {
            Map<String, String> valuesMap = new HashMap<>(1);
            String appName = queryGroupName + "-" + (i + 1);
            valuesMap.put(ResourceManagerConstants.APP_NAME, appName);
            StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
            queries.add(new SiddhiQuery(appName, substitutor.replace(queryTemplate), false));
        }
        return queries;
    }

    private void updateQueryList(List<SiddhiQuery> queryList, Map<String, String> valuesMap) {
        StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
        for (SiddhiQuery query : queryList) {
            String updatedQuery = substitutor.replace(query.getApp());
            query.setApp(updatedQuery);
        }
    }

    private String getUpdatedQuery(String query, Map<String, String> valuesMap) {
        StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
        return substitutor.replace(query);
    }
}
