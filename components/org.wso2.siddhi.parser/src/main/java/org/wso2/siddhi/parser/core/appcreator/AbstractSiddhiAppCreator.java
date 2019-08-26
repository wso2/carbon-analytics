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

import org.wso2.siddhi.parser.core.SiddhiAppCreator;
import org.wso2.siddhi.parser.core.topology.SiddhiQueryGroup;
import org.wso2.siddhi.parser.core.topology.SiddhiTopology;
import org.wso2.siddhi.parser.service.model.MessagingSystem;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract implementation of {@link SiddhiAppCreator}. Developers can use this extension point
 * to implement custom Siddhi App Creator based on the distribute implementation.
 */
public abstract class AbstractSiddhiAppCreator implements SiddhiAppCreator {

    public List<DeployableSiddhiQueryGroup> createApps(SiddhiTopology topology, MessagingSystem messagingSystem) {
        List<DeployableSiddhiQueryGroup> deployableSiddhiQueryGroupList =
                new ArrayList(topology.getQueryGroupList().size());
        for (SiddhiQueryGroup queryGroup : topology.getQueryGroupList()) {
            DeployableSiddhiQueryGroup deployableQueryGroup =
                    new DeployableSiddhiQueryGroup(queryGroup.getName(),
                            queryGroup.isReceiverQueryGroup(), /*queryGroup.isUserGivenSource(),*/
                            queryGroup.getParallelism());
            deployableQueryGroup.setSiddhiQueries(createApps(topology.getName(), queryGroup, messagingSystem));
            deployableSiddhiQueryGroupList.add(deployableQueryGroup);
        }
        return deployableSiddhiQueryGroupList;
    }

    /**
     * This method should return valid concrete Siddhi App/s as Strings. No. of returned Siddhi
     * Apps should equal the parallelism count for parse group.
     *
     * @param queryGroup Input parse group to produce Siddhi Apps.
     * @return List of valid concrete Siddhi Apps as String.
     */
    protected abstract List<SiddhiQuery> createApps(String siddhiAppName,
                                                    SiddhiQueryGroup queryGroup, MessagingSystem messagingSystem);

    protected List<Integer> getPartitionNumbers(int appParallelism, int availablePartitionCount,
                                                int currentAppNum) {
        List<Integer> partitionNumbers = new ArrayList();
        if (availablePartitionCount == appParallelism) {
            partitionNumbers.add(currentAppNum);
            return partitionNumbers;
        } else {
            //availablePartitionCount < appParallelism scenario cannot occur according to design.
            // Hence if availablePartitionCount > appParallelism
            //// TODO: 10/19/17 improve logic
            int partitionsPerNode = availablePartitionCount / appParallelism;
            if (currentAppNum + 1 == appParallelism) { //if last app
                int remainingPartitions = availablePartitionCount - ((appParallelism - 1) *
                        partitionsPerNode);
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

    protected List<SiddhiQuery> generateQueryList(String queryTemplate, String queryGroupName,
                                                  int parallelism) {
        List<SiddhiQuery> queries = new ArrayList(parallelism);
        for (int i = 0; i < parallelism; i++) {
            Map<String, String> valuesMap = new HashMap(1);
            String appName = queryGroupName + "-" + (i + 1);
            valuesMap.put("appName", appName);
            StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
            queries.add(new SiddhiQuery(appName, substitutor.replace(queryTemplate),
                    false));
        }
        return queries;
    }

    protected void updateQueryList(List<SiddhiQuery> queryList, Map<String, String> valuesMap) {
        StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
        for (SiddhiQuery query : queryList) {
            String updatedQuery = substitutor.replace(query.getApp());
            query.setApp(updatedQuery);
        }
    }

    protected String getUpdatedQuery(String query, Map<String, String> valuesMap) {
        StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
        return substitutor.replace(query);
    }

    /**
     *@param siddhiAppName   Name of the userdefined siddhi app
     * @param streamName      Currently processing stream name
     * @param groupingField   Partition key field, if available otherwise null
     * @return  created topic name
     *
     * Creates the topic name from above parameters
     */
    protected String getTopicName(String siddhiAppName, String streamName, String groupingField) {
        return siddhiAppName + "_" + streamName + (groupingField == null ? ""
                : ("_" + groupingField));
    }
}
