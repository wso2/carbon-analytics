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

package org.wso2.carbon.das.jobmanager.core.topology;

import org.wso2.carbon.das.jobmanager.core.SiddhiTopologyCreator;
import org.wso2.carbon.das.jobmanager.core.util.EventHolder;
import org.wso2.carbon.das.jobmanager.core.util.SiddhiTopologyCreatorConstants;
import org.wso2.carbon.das.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.partition.PartitionType;
import org.wso2.siddhi.query.api.execution.partition.ValuePartitionType;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.util.ExceptionUtil;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SiddhiTopologyCreatorImpl implements SiddhiTopologyCreator {

    private SiddhiTopologyDataHolder siddhiTopologyDataHolder;
    private SiddhiApp siddhiApp;
    private SiddhiAppRuntime siddhiAppRuntime;

    @Override
    public SiddhiTopology createTopology(String userDefinedSiddhiApp) {
        this.siddhiApp = SiddhiCompiler.parse(userDefinedSiddhiApp);
        this.siddhiAppRuntime = (new SiddhiManager()).createSiddhiAppRuntime(userDefinedSiddhiApp);
        SiddhiQueryGroup siddhiQueryGroup;
        int[] queryContextEndIndex;
        int[] queryContextStartIndex;
        String execGroupName;
        int parallel;
        this.siddhiTopologyDataHolder = new SiddhiTopologyDataHolder(getAppName(), userDefinedSiddhiApp);

        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            parallel = 1;
            execGroupName = null;

            for (int i = 0; i < executionElement.getAnnotations().size(); i++) {
                if (executionElement.getAnnotations().get(i)
                        .getElement(SiddhiTopologyCreatorConstants.execGroupIdentifier) != null) {
                    execGroupName =
                            siddhiTopologyDataHolder.getSiddhiAppName() + "-" + executionElement.getAnnotations().get(i)
                                    .getElement(SiddhiTopologyCreatorConstants.execGroupIdentifier);
                }

                if (executionElement.getAnnotations().get(i)
                        .getElement(SiddhiTopologyCreatorConstants.parallelIdentifier) != null) {
                    parallel = Integer.parseInt(executionElement.getAnnotations().get(i)
                                                        .getElement(SiddhiTopologyCreatorConstants.parallelIdentifier));
                }
            }
            if (execGroupName != null && !siddhiTopologyDataHolder.getSiddhiQueryGroupMap()
                    .containsKey(execGroupName)) {

                siddhiQueryGroup = new SiddhiQueryGroup();
                siddhiQueryGroup.setName(execGroupName);
                siddhiQueryGroup.setParallelism(parallel);

            } else if (siddhiTopologyDataHolder.getSiddhiQueryGroupMap().containsKey(execGroupName)) {
                siddhiQueryGroup = siddhiTopologyDataHolder.getSiddhiQueryGroupMap().get(execGroupName);

                //Same execution group given  with different parallel numbers
                if (siddhiQueryGroup.getParallelism() != parallel) {
                    throw new SiddhiAppValidationException(
                            "execGroup =" + "\'" + execGroupName + "\' not assigned unique @dist(parallel)");
                }

            } else {
                //will work if execGroup is not mentioned-those will go to a single app
                siddhiQueryGroup = new SiddhiQueryGroup();
                siddhiQueryGroup.setName(siddhiTopologyDataHolder.getSiddhiAppName() + "-" + UUID.randomUUID());
                siddhiQueryGroup.setParallelism(parallel);
            }
            //if execution element is a query
            if (executionElement instanceof Query) {

                //set query
                queryContextStartIndex = ((Query) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Query) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetainfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));


                siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(execGroupName, assignStreamInfoSiddhiQueryGroup(
                        (Query) executionElement, execGroupName, siddhiQueryGroup, true));

            } else if (executionElement instanceof Partition) {
                //set Partition
                queryContextStartIndex = ((Partition) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Partition) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetainfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

                addPartitionKey((Partition) executionElement, execGroupName);

                List<Query> partitionQueryList = ((Partition) executionElement).getQueryList();
                for (Query query : partitionQueryList) {
                    for (int k = 0; k < query.getAnnotations().size(); k++) {
                        if (query.getAnnotations().get(k)
                                .getElement(SiddhiTopologyCreatorConstants.distributedIdentifier) != null) {
                            throw new SiddhiAppValidationException(
                                    "Unsupported:@dist annotation inside partition queries");
                        }
                    }
                    siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(
                            execGroupName, assignStreamInfoSiddhiQueryGroup(query, execGroupName,
                                                                            siddhiQueryGroup, false));
                }
            }
        }
        return new SiddhiTopology(siddhiTopologyDataHolder.getSiddhiAppName(), assignPublishingStrategyOutputStream());
    }

    private String getAppName() {
        for (int i = 0; i < siddhiApp.getAnnotations().size(); i++) {
            if (siddhiApp.getAnnotations().get(i).getName().equals("name")) {
                return siddhiApp.getAnnotations().get(i).getElements().get(0).getValue();
            }
        }
        return SiddhiTopologyCreatorConstants.defaultSiddhiAppName + "-" + UUID.randomUUID();//defaultName
    }

    private String removeMetainfoQuery(ExecutionElement executionElement, String queryElement) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (int i = 0; i < executionElement.getAnnotations().size(); i++) {
            if (executionElement.getAnnotations().get(i).getName().toLowerCase()
                    .equals(SiddhiTopologyCreatorConstants.distributedIdentifier)) {
                queryContextStartIndex = executionElement.getAnnotations().get(i).getQueryContextStartIndex();
                queryContextEndIndex = executionElement.getAnnotations().get(i).getQueryContextEndIndex();
                queryElement = queryElement.replace(
                        ExceptionUtil.getContext(queryContextStartIndex,
                                                 queryContextEndIndex,
                                                 siddhiTopologyDataHolder.getUserDefinedSiddhiApp()), "");
            }
        }
        return queryElement;
    }

    private String removeMetainfoStream(String streamId, String streamDefinition) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (Annotation annotation : siddhiApp.getStreamDefinitionMap().get(streamId).getAnnotations()) {
            if (annotation.getName().toLowerCase().equals(
                    SiddhiTopologyCreatorConstants.sinkIdentifier.replace("@", ""))) {
                queryContextStartIndex = annotation.getQueryContextStartIndex();
                queryContextEndIndex = annotation.getQueryContextEndIndex();
                streamDefinition = streamDefinition.replace(
                        ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex,
                                                 siddhiTopologyDataHolder.getUserDefinedSiddhiApp()), "");
            }
        }
        return streamDefinition;
    }

    private SiddhiQueryGroup assignStreamInfoSiddhiQueryGroup(Query executionElement, String groupName,
                                                              SiddhiQueryGroup siddhiQueryGroup, boolean queryElement) {
        StreamInfoDataHolder streamInfoDataHolder;
        int parallel = siddhiQueryGroup.getParallelism();
        InputStream inputStream = (executionElement).getInputStream();
        List<String> listInputStream = inputStream.getAllStreamIds();

        //send to check for validity of the query type eg:join , window, pattern , sequence
        //if joined or .... with paritioned stream for parallel>1---->fine
        //else if ---> restrict
        if (parallel > 1) {
            //send to check for validity of the query type eg:join , window, pattern , sequence
            checkQueryType(inputStream, queryElement);
        }

        for (String inputStreamId : listInputStream) {


            //not an inner Stream
            if (!inputStreamId.contains(SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
                streamInfoDataHolder = returnStreamInfo(inputStreamId, parallel, groupName);
                TransportStrategy transportStrategy =
                        findStreamSubscriptionStrategy(queryElement, inputStreamId, parallel,
                                                       siddhiQueryGroup.getName());
                siddhiQueryGroup.addInputStreamHolder(inputStreamId,
                                                      new InputStreamDataHolder(inputStreamId,
                                                                                streamInfoDataHolder
                                                                                        .getStreamDefinition(),
                                                                                streamInfoDataHolder
                                                                                        .getEventHolderType(),
                                                                                streamInfoDataHolder.isUserGiven(),
                                                                                new SubscriptionStrategyDataHolder(
                                                                                        parallel, transportStrategy)));
            }
        }

        String outputStreamId = executionElement.getOutputStream().getId();
        //not an inner Stream
        if (!outputStreamId.contains(SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
            streamInfoDataHolder = returnStreamInfo(outputStreamId, parallel, groupName);
            siddhiQueryGroup.addOutputStreamHolder(outputStreamId,
                                                   new OutputStreamDataHolder(outputStreamId,
                                                                              streamInfoDataHolder
                                                                                      .getStreamDefinition(),
                                                                              streamInfoDataHolder.getEventHolderType(),
                                                                              streamInfoDataHolder.isUserGiven()));
        }
        return siddhiQueryGroup;
    }

    private StreamInfoDataHolder returnStreamInfo(String streamId, int parallel,
                                                  String groupName) {
        String streamDefinition;
        int[] queryContextEndIndex;
        int[] queryContextStartIndex;
        StreamInfoDataHolder streamInfoDataHolder = new StreamInfoDataHolder(true);

        if (siddhiApp.getStreamDefinitionMap().containsKey(streamId)) {

            queryContextStartIndex = siddhiApp.getStreamDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getStreamDefinitionMap().get(streamId).getQueryContextEndIndex();
            streamDefinition = ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex,
                                                        siddhiTopologyDataHolder.getUserDefinedSiddhiApp());
            if (!isUserGivenStream(streamDefinition)) {
                streamDefinition = "${" + streamId + "}" + streamDefinition;
            }
            streamInfoDataHolder =
                    new StreamInfoDataHolder(streamDefinition, EventHolder.STREAM, isUserGivenStream(streamDefinition));

        } else if (siddhiApp.getTableDefinitionMap().containsKey(streamId)) {
            AbstractDefinition tableDefinition = siddhiApp.getTableDefinitionMap().get(streamId);
            streamInfoDataHolder.setEventHolderType(EventHolder.INMEMORYTABLE);

            for (int k = 0; k < tableDefinition.getAnnotations().size(); k++) {
                if (tableDefinition.getAnnotations().get(k).getName().toLowerCase().equals
                        (SiddhiTopologyCreatorConstants.persistenceTableIdentifier)) {
                    streamInfoDataHolder.setEventHolderType(EventHolder.TABLE);
                    break;
                }
            }
            //need to check In-Memory or other
            if (parallel != 1 && streamInfoDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE)) {
                throw new SiddhiAppValidationException("In-Memory Tables can not have parallel >1");
            }

            queryContextStartIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextEndIndex();
            streamInfoDataHolder.setStreamDefinition(ExceptionUtil
                                                             .getContext(queryContextStartIndex, queryContextEndIndex,
                                                                         siddhiTopologyDataHolder
                                                                                 .getUserDefinedSiddhiApp()));

            if (streamInfoDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE) && siddhiTopologyDataHolder
                    .getInMemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInMemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:Event Table " + streamId +
                                                                   " In-Memory Table used in two execGroups: execGroup "
                                                                   + groupName + " && " + siddhiTopologyDataHolder
                            .getInMemoryMap().get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInMemoryMap().put(streamId, groupName);
            }

        } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
            if (parallel != 1) {
                throw new SiddhiAppValidationException("(Defined) Window can not have parallel >1");
            }

            queryContextStartIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextEndIndex();

            streamInfoDataHolder.setStreamDefinition(ExceptionUtil
                                                             .getContext(queryContextStartIndex, queryContextEndIndex,
                                                                         siddhiTopologyDataHolder
                                                                                 .getUserDefinedSiddhiApp()));
            streamInfoDataHolder.setEventHolderType(EventHolder.WINDOW);

            if (siddhiTopologyDataHolder.getInMemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInMemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:(Defined) Window " + streamId +
                                                                   " In-Memory window used in two execGroups: "
                                                                   + "execGroup "
                                                                   + groupName + " && " + siddhiTopologyDataHolder
                            .getInMemoryMap().get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInMemoryMap().put(streamId, groupName);
            }

            //if stream definition is an inferred definition
        } else if (streamInfoDataHolder.getStreamDefinition() == null) {
            if (siddhiAppRuntime.getStreamDefinitionMap().containsKey(streamId)) {
                streamInfoDataHolder = new StreamInfoDataHolder(
                        "${" + streamId + "}"
                                + siddhiAppRuntime.getStreamDefinitionMap().get(streamId).toString(),
                        EventHolder.STREAM, false);
            } else if (siddhiAppRuntime.getTableDefinitionMap().containsKey(streamId)) {
                if (parallel != 1) {
                    throw new SiddhiAppValidationException("(In-Memory Tables can not have parallel >1");
                }
                streamInfoDataHolder =
                        new StreamInfoDataHolder(siddhiAppRuntime.getTableDefinitionMap().get(streamId).toString(),
                                                 EventHolder.INMEMORYTABLE, false);
            }
        }
        return streamInfoDataHolder;
    }

    private boolean isUserGivenStream(String streamDefinition) {
        if (streamDefinition.toLowerCase().contains(
                SiddhiTopologyCreatorConstants.sourceIdentifier) || streamDefinition.toLowerCase().contains
                (SiddhiTopologyCreatorConstants.sinkIdentifier)) {
            return true;
        } else {
            return false;
        }
    }

    private void checkQueryType(InputStream inputStream, boolean queryElement) {
        boolean partitionStreamExist = false;

        for (String streamId : inputStream.getUniqueStreamIds()) {
            //fine join ...... happens with partiioned key and inner streams
            if (siddhiTopologyDataHolder.getPartitionKeyMap().containsKey(streamId) || streamId.contains
                    (SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
                partitionStreamExist = true;
                break;
            }
        }

        if (queryElement || !partitionStreamExist) {
            if (inputStream instanceof JoinInputStream) {
                throw new SiddhiAppValidationException("Join queries can not have parallel greater than 1");

            } else if (inputStream instanceof StateInputStream) {
                String type = ((StateInputStream) inputStream).getStateType().name();
                throw new SiddhiAppValidationException(type + " queries can not have parallel greater than 1");

            } else if (inputStream instanceof SingleInputStream) {
                List<StreamHandler> streamHandlers = ((SingleInputStream) inputStream).getStreamHandlers();

                for (StreamHandler streamHandler : streamHandlers) {
                    if (streamHandler instanceof Window) {
                        throw new SiddhiAppValidationException("Window queries can not have parallel greater than 1");
                    }
                }
            }
        }
    }

    private TransportStrategy findStreamSubscriptionStrategy(boolean queryElement, String streamId, int parallel,
                                                             String execGroup) {
        if (parallel > 1) {
            if (!queryElement) {
                if (siddhiTopologyDataHolder.getPartitionKeyMap().containsKey(streamId)) {
                    return TransportStrategy.FIELD_GROUPING;
                } else {
                    //inside a partition but not a partitioned stream
                    return TransportStrategy.ALL;
                }
            } else {
                //if same execGroup --> if stream exists in PartitionedGroup ---> then
                if (siddhiTopologyDataHolder.getPartitionGroupMap().get(streamId) != null &&
                        siddhiTopologyDataHolder.getPartitionGroupMap().get(streamId).contains(execGroup)) {
                    return TransportStrategy.FIELD_GROUPING;
                } else {
                    return TransportStrategy.ROUND_ROBIN;
                }
            }
        } else {
            return TransportStrategy.ALL;
        }

    }

    private List<SiddhiQueryGroup> assignPublishingStrategyOutputStream() {
        boolean fieldGrouping;
        String runctimeStreamDefinition;
        String outputStreamDefinition;
        int i = 0;
        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());

        for (SiddhiQueryGroup siddhiQueryGroup1 : siddhiQueryGroupsList) {
            for (String key : siddhiQueryGroup1.getOutputStream().keySet()) {

                if (siddhiQueryGroup1.getOutputStream().get(key).getEventHolderType()
                        .equals(EventHolder.STREAM)) {
                    for (SiddhiQueryGroup siddhiQueryGroup2 : siddhiQueryGroupsList.subList(i + 1,
                                                                                       siddhiQueryGroupsList.size())) {
                        if (siddhiQueryGroup2.getInputStreams().containsKey(key)) {

                            //when user given sink stream used by diff execGroup as source stream
                            //additional sink will be added
                            if (siddhiQueryGroup1.getOutputStream().get(key).isUserGiven()) {
                                runctimeStreamDefinition = removeMetainfoStream(key,
                                                                                siddhiQueryGroup2.getInputStreams()
                                                                                        .get(key)
                                                                                        .getStreamDefinition());
                                outputStreamDefinition = siddhiQueryGroup1.getOutputStream().get
                                        (key) + "\n" + "${" + key + "} ";
                                siddhiQueryGroup1.getOutputStream().get(key)
                                        .setStreamDefinition(outputStreamDefinition);
                                siddhiQueryGroup2.getInputStreams().get(key).setStreamDefinition(
                                        "${" + key + "} " + runctimeStreamDefinition);
                            }

                            SubscriptionStrategyDataHolder subscriptionStrategy =
                                    siddhiQueryGroup2.getInputStreams().get(key).getSubscriptionStrategy();
                            if (subscriptionStrategy.getStrategy().equals(TransportStrategy.FIELD_GROUPING)) {
                                fieldGrouping = true;
                                for (PublishingStrategyDataHolder publishingStrategyDataHolder :
                                        siddhiQueryGroup1.getOutputStream().get(key).getPublishingStrategyList()) {

                                    if (publishingStrategyDataHolder.getGroupingField() != null
                                            && publishingStrategyDataHolder.getGroupingField()
                                            .equals(siddhiTopologyDataHolder.getPartitionKeyMap().get(key)
                                                            .getFirst())) {

                                        int index = siddhiQueryGroup1.getOutputStream().get(key)
                                                .getPublishingStrategyList().indexOf(publishingStrategyDataHolder);

                                        //check partition-key if same ---> need to check for max-No of groups
                                        siddhiQueryGroup1.getOutputStream().get(key)
                                                .getPublishingStrategyList().get(index).setParallelism(
                                                Math.max(siddhiQueryGroup1.getParallelism(),
                                                         siddhiQueryGroup2.getParallelism()));

                                        siddhiQueryGroup2.getInputStreams().get(key)
                                                .getSubscriptionStrategy().setOfferedParallelism(
                                                Math.max(siddhiQueryGroup1.getParallelism(),
                                                         siddhiQueryGroup2.getParallelism()));


                                        siddhiTopologyDataHolder.getPartitionKeyMap().get(key).removeFirst();
                                        //else new strategy;
                                        fieldGrouping = false;
                                        break;
                                    }
                                }
                                if (fieldGrouping) {
                                    siddhiQueryGroup1.getOutputStream().get(key).addPublishingStrategy(
                                            new PublishingStrategyDataHolder(siddhiQueryGroup2.getName(),
                                                                             TransportStrategy.FIELD_GROUPING,
                                                                             siddhiTopologyDataHolder
                                                                                     .getPartitionKeyMap().get(key)
                                                                                     .getFirst(),
                                                                             siddhiQueryGroup2.getParallelism()));
                                    siddhiQueryGroup2.getInputStreams().get(key).getSubscriptionStrategy()
                                            .setPartitionKey(
                                                    siddhiTopologyDataHolder.getPartitionKeyMap().get(key).getFirst());
                                    siddhiTopologyDataHolder.getPartitionKeyMap().get(key).removeFirst();

                                }

                            } else {
                                siddhiQueryGroup1.getOutputStream().get(key).addPublishingStrategy(
                                        new PublishingStrategyDataHolder(siddhiQueryGroup2.getName(),
                                                                         subscriptionStrategy.getStrategy(),
                                                                         siddhiQueryGroup2.getParallelism()));
                            }

                        }
                    }
                }

            }
            i++;
        }
        return siddhiQueryGroupsList;
    }

    private void addPartitionKey(Partition partition, String execGroupName) {
        LinkedList<String> partitionKeyList;
        LinkedList<String> partitionGroupList;

        //set partitionKeyMap and partitionGroupMap
        for (Map.Entry<String, PartitionType> partitionTypeEntry : partition.getPartitionTypeMap().entrySet()) {
            if (siddhiTopologyDataHolder.getPartitionKeyMap().containsKey(partitionTypeEntry.getKey())) {
                partitionKeyList = siddhiTopologyDataHolder.getPartitionKeyMap().get(partitionTypeEntry.getKey());
                partitionGroupList = siddhiTopologyDataHolder.getPartitionGroupMap().get(partitionTypeEntry.getKey());
            } else {
                partitionKeyList = new LinkedList<>();
                partitionGroupList = new LinkedList<>();
            }

            //when more than one partition residing in the same SiddhiApp
            if (partitionGroupList.contains(execGroupName)) {
                throw new SiddhiAppValidationException("Unsupported in distributed setup    :More than 1 partition "
                                                               + "residing on "
                                                               + "the same "
                                                               + "execGroup " + execGroupName);
            } else {
                partitionGroupList.add(execGroupName);
                siddhiTopologyDataHolder.getPartitionGroupMap().put(partitionTypeEntry.getKey(),
                                                                    partitionGroupList);
            }
            if (partitionTypeEntry.getValue() instanceof ValuePartitionType) {
                partitionKeyList
                        .add(((Variable) ((ValuePartitionType) partitionTypeEntry.getValue()).getExpression())
                                     .getAttributeName());
                siddhiTopologyDataHolder.getPartitionKeyMap().put(partitionTypeEntry.getKey(),
                                                                  partitionKeyList);
            } else {
                //Not yet supported
                throw new SiddhiAppValidationException("Range PartitionType not Supported in Distributed SetUp");
            }
        }
    }
    //TODO:User given source used in more than 1 execGroup or used with parallel >1
}