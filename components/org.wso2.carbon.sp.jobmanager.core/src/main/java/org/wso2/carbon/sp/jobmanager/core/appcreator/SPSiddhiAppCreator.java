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
import org.wso2.carbon.sp.jobmanager.core.exception.ResourceManagerException;
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
public class SPSiddhiAppCreator extends AbstractSiddhiAppCreator {
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

    private void processOutputStreams(String siddhiAppName, String groupName, List<SiddhiQuery> queryList,
                                      Collection<OutputStreamDataHolder> outputStreams) {
        Map<String, String> sinkValuesMap = new HashMap<>();
        String bootstrapServerURL = ServiceDataHolder.getDeploymentConfig().getBootstrapURLs();
        sinkValuesMap.put(ResourceManagerConstants.BOOTSTRAP_SERVER_URL, bootstrapServerURL);
        for (OutputStreamDataHolder outputStream : outputStreams) {
            Map<String, String> sinkList = new HashMap<>();
            Map<String, Integer> partitionKeys = new HashMap<>();
            Map<String, Integer> topicParallelismMap = new HashMap<>();
            for (PublishingStrategyDataHolder holder : outputStream.getPublishingStrategyList()) {
                sinkValuesMap.put(ResourceManagerConstants.TOPIC_LIST, siddhiAppName + "." +
                        outputStream.getStreamName() + (holder.getGroupingField() == null ? "" : ("." + holder
                        .getGroupingField())));
                if (holder.getStrategy() == TransportStrategy.FIELD_GROUPING) {
                    if (partitionKeys.get(holder.getGroupingField()) != null &&
                            partitionKeys.get(holder.getGroupingField()) > holder.getParallelism()) {
                        continue;
                    }
                    //Remove if there is any previous R/R or ALL publishing
                    sinkValuesMap.remove(siddhiAppName + "." + outputStream.getStreamName());
                    partitionKeys.put(holder.getGroupingField(), holder.getParallelism());
                    sinkValuesMap.put(ResourceManagerConstants.PARTITION_KEY, holder.getGroupingField());
                    List<String> destinations = new ArrayList<>(holder.getParallelism());
                    for (int i = 0; i < holder.getParallelism(); i++) {
                        Map<String, String> destinationMap = new HashMap<>(holder.getParallelism());
                        destinationMap.put(ResourceManagerConstants.PARTITION_NO, String.valueOf(i));
                        destinations.add(getUpdatedQuery(ResourceManagerConstants.DESTINATION, destinationMap));
                    }
                    sinkValuesMap.put(ResourceManagerConstants.DESTINATIONS, StringUtils.join(destinations, ","));
                    String sinkString = getUpdatedQuery(ResourceManagerConstants.PARTITIONED_KAFKA_SINK_TEMPLATE,
                            sinkValuesMap);
                    sinkList.put(sinkValuesMap.get(ResourceManagerConstants.TOPIC_LIST), sinkString);
                    topicParallelismMap.put(sinkValuesMap.get(ResourceManagerConstants.TOPIC_LIST),
                            holder.getParallelism());
                } else {
                    //ATM we are handling both strategies in same manner. Later will improve to have multiple
                    // partitions for RR
                    if (partitionKeys.isEmpty()) {
                        //Define a sink only if there are no partitioned sinks present
                        String sinkString = getUpdatedQuery(ResourceManagerConstants.DEFAULT_KAFKA_SINK_TEMPLATE,
                                sinkValuesMap);
                        sinkList.put(sinkValuesMap.get(ResourceManagerConstants.TOPIC_LIST), sinkString);
                    }
                }
            }
            Map<String, String> queryValuesMap = new HashMap<>(1);
            queryValuesMap.put(outputStream.getStreamName(), StringUtils.join(sinkList.values(), "\n"));
            updateQueryList(queryList, queryValuesMap);
            createTopicPartitions(topicParallelismMap);
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
                        log.info("Topic " + topic + " has higher number of partitions than expected partition count. Hence"
                                + " have to delete the topic and recreate with " + partitions + "partitions.");
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

    private void processInputStreams(String siddhiAppName, String groupName, List<SiddhiQuery> queryList,
                                     Collection<InputStreamDataHolder> inputStreams) {
        Map<String, String> sourceValuesMap = new HashMap<>();
        String bootstrapServerURL = ServiceDataHolder.getDeploymentConfig().getBootstrapURLs();
        sourceValuesMap.put(ResourceManagerConstants.BOOTSTRAP_SERVER_URL, bootstrapServerURL);
        for (InputStreamDataHolder inputStream : inputStreams) {
            SubscriptionStrategyDataHolder subscriptionStrategy = inputStream.getSubscriptionStrategy();
            sourceValuesMap.put(ResourceManagerConstants.TOPIC_LIST, siddhiAppName + "." +
                    inputStream.getStreamName() + (inputStream.getSubscriptionStrategy().getPartitionKey() ==
                    null ? "" : ("." + inputStream.getSubscriptionStrategy().getPartitionKey())));
            if (!inputStream.isUserGiven()) {
                if (subscriptionStrategy.getStrategy() == TransportStrategy.FIELD_GROUPING) {
                    sourceValuesMap.put(ResourceManagerConstants.CONSUMER_GROUP_ID, groupName);
                    for (int i = 0; i < queryList.size(); i++) {
                        List<Integer> partitionNumbers = getPartitionNumbers(queryList.size(), subscriptionStrategy
                                .getOfferedParallelism(), i);
                        sourceValuesMap.put(ResourceManagerConstants.PARTITION_LIST, StringUtils.join(partitionNumbers,
                                ","));
                        String sourceString =
                                getUpdatedQuery(ResourceManagerConstants.PARTITIONED_KAFKA_SOURCE_TEMPLATE,
                                        sourceValuesMap);
                        Map<String, String> queryValuesMap = new HashMap<>(1);
                        queryValuesMap.put(inputStream.getStreamName(), sourceString);
                        String updatedQuery = getUpdatedQuery(queryList.get(i).getApp(), queryValuesMap);
                        queryList.get(i).setApp(updatedQuery);
                    }
                } else if (subscriptionStrategy.getStrategy() == TransportStrategy.ROUND_ROBIN) {
                    sourceValuesMap.put(ResourceManagerConstants.CONSUMER_GROUP_ID, groupName);
                    String sourceString = getUpdatedQuery(ResourceManagerConstants.DEFAULT_KAFKA_SOURCE_TEMPLATE,
                            sourceValuesMap);
                    Map<String, String> queryValuesMap = new HashMap<>(1);
                    queryValuesMap.put(inputStream.getStreamName(), sourceString);
                    updateQueryList(queryList, queryValuesMap);
                } else {
                    for (int i = 0; i < queryList.size(); i++) {
                        sourceValuesMap.put(ResourceManagerConstants.CONSUMER_GROUP_ID, groupName + "-" +
                                i);
                        String sourceString = getUpdatedQuery(ResourceManagerConstants.DEFAULT_KAFKA_SOURCE_TEMPLATE,
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
            queries.add(new SiddhiQuery(appName, substitutor.replace(queryTemplate),false));
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
