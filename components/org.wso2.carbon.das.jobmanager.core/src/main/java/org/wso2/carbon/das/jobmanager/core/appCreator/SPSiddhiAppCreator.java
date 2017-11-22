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

package org.wso2.carbon.das.jobmanager.core.appCreator;

import kafka.admin.AdminUtils;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.wso2.carbon.das.jobmanager.core.internal.ServiceDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.InputStreamDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.OutputStreamDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.PublishingStrategyDataHolder;
import org.wso2.carbon.das.jobmanager.core.topology.SiddhiQueryGroup;
import org.wso2.carbon.das.jobmanager.core.topology.SubscriptionStrategyDataHolder;
import org.wso2.carbon.das.jobmanager.core.util.ResourceManagerConstants;
import org.wso2.carbon.das.jobmanager.core.util.TransportStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SPSiddhiAppCreator extends AbstractSiddhiAppCreator {

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
        String zooKeeperServerURL = ServiceDataHolder.getDeploymentConfig().getZooKeeperURLs();
        ZkClient zkClient = new ZkClient(
                zooKeeperServerURL,
                10000,
                8000,
                ZKStringSerializer$.MODULE$);

        boolean isSecureKafkaCluster = false;
        ZkUtils zkUtils = new ZkUtils(zkClient, new ZkConnection(zooKeeperServerURL), isSecureKafkaCluster);
        Properties topicConfig = new Properties();
        //// TODO: 11/2/17 making replication factor one. research for dynamically setting this
        for (Map.Entry<String, Integer> entry : topicParallelismMap.entrySet()) {
            if (AdminUtils.topicExists(zkUtils, entry.getKey())) {
                AdminUtils.addPartitions(zkUtils, entry.getKey(), entry.getValue(), "", true);
            }
            AdminUtils.createTopic(zkUtils, entry.getKey(), entry.getValue(), 1, topicConfig);
        }
        zkClient.close();


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
                        String sourceString = getUpdatedQuery(ResourceManagerConstants.PARTITIONED_KAFKA_SOURCE_TEMPLATE,
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
            queries.add(new SiddhiQuery(appName, substitutor.replace(queryTemplate)));
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
