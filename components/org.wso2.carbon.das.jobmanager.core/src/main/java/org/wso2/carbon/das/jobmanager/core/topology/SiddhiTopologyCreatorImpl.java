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
                if (executionElement.getAnnotations().get(i).getElement("execGroup") != null) {
                    execGroupName =
                            siddhiTopologyDataHolder.getSiddhiAppName() + "-" + executionElement.getAnnotations().get(i)
                                    .getElement("execGroup");
                }

                if (executionElement.getAnnotations().get(i).getElement("parallel") != null) {
                    parallel = Integer.parseInt(executionElement.getAnnotations().get(i).getElement("parallel"));
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

                LinkedList<String> stringArrayList;
                //set PartitionMap
                for (Map.Entry<String, PartitionType> partitionTypeEntry : ((Partition) executionElement)
                        .getPartitionTypeMap().entrySet()) {

                    if (siddhiTopologyDataHolder.getPartitionTypeMap().containsKey(partitionTypeEntry.getKey())) {
                        stringArrayList =
                                siddhiTopologyDataHolder.getPartitionTypeMap().get(partitionTypeEntry.getKey());
                    } else {

                        stringArrayList = new LinkedList<>();
                    }

                    if (partitionTypeEntry.getValue() instanceof ValuePartitionType) {
                        stringArrayList
                                .add(((Variable) ((ValuePartitionType) partitionTypeEntry.getValue()).getExpression())
                                        .getAttributeName());

                        siddhiTopologyDataHolder.getPartitionTypeMap().put(partitionTypeEntry.getKey(),
                                stringArrayList);
                    } else {
                        //for now
                        throw new SiddhiAppValidationException(
                                "Range PartitionType not Supported in Distributed SetUp");
                    }
                }

                List<Query> partitionQueryList = ((Partition) executionElement).getQueryList();
                for (Query query : partitionQueryList) {
                    for (int k = 0; k < query.getAnnotations().size(); k++) {
                        if (query.getAnnotations().get(k).getElement("dist") != null) {
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
        return "SiddhiApp-" + UUID.randomUUID();//defaultName
    }

    private String removeMetainfoQuery(ExecutionElement executionElement, String queryElement) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (int i = 0; i < executionElement.getAnnotations().size(); i++) {
            if (executionElement.getAnnotations().get(i).getName().toLowerCase().equals("dist")) {
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

    private SiddhiQueryGroup assignStreamInfoSiddhiQueryGroup(Query executionElement, String groupName,
                                                              SiddhiQueryGroup siddhiQueryGroup, boolean queryElement) {

        String inputStreamId;
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

        for (int j = 0; j < listInputStream.size(); j++) {

            inputStreamId = listInputStream.get(j);
            //not an inner Stream
            if (!inputStreamId.contains(SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
                streamInfoDataHolder = returnStreamInfo(inputStreamId, parallel, groupName);
                TransportStrategy transportStrategy =
                        checkQueryStrategy(inputStream, queryElement, inputStreamId, parallel);
                siddhiQueryGroup.addInputStreamHolder(inputStreamId,
                        new InputStreamDataHolder(inputStreamId,
                                streamInfoDataHolder.getStreamDefinition(),
                                streamInfoDataHolder.getEventHolderType(),
                                streamInfoDataHolder.isUserGiven(),
                                new SubscriptionStrategyDataHolder(parallel, transportStrategy)));
            }
        }

        String outputStreamId = executionElement.getOutputStream().getId();
        //not an inner Stream
        if (!outputStreamId.contains(SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
            streamInfoDataHolder = returnStreamInfo(outputStreamId, parallel, groupName);
            siddhiQueryGroup.addOutputStreamHolder(outputStreamId,
                    new OutputStreamDataHolder(outputStreamId,
                            streamInfoDataHolder.getStreamDefinition(),
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
                    .getInmemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInmemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:Event Table " + streamId +
                            " In-Memory Table used in two execGroups: execGroup "
                            + groupName + " && " + siddhiTopologyDataHolder
                            .getInmemoryMap().get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInmemoryMap().put(streamId, groupName);
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

            if (siddhiTopologyDataHolder.getInmemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInmemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:(Defined) Window " + streamId +
                            " In-Memory window used in two execGroups: execGroup "
                            + groupName + " && " + siddhiTopologyDataHolder
                            .getInmemoryMap().get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInmemoryMap().put(streamId, groupName);
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
            if (siddhiTopologyDataHolder.getPartitionTypeMap().containsKey(streamId) || streamId.contains
                    (SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
                partitionStreamExist = true;
                break;
            }
        }

        if (queryElement || !partitionStreamExist) {
            if (inputStream instanceof JoinInputStream) {
                throw new SiddhiAppValidationException("Join queries can not have parallel greater than 1  ");

            } else if (inputStream instanceof StateInputStream) {

                String type = ((StateInputStream) inputStream).getStateType().name();
                throw new SiddhiAppValidationException(type + " queries can not have parallel greater than 1  ");

            } else if (inputStream instanceof SingleInputStream) {
                List<StreamHandler> streamHandlers = ((SingleInputStream) inputStream).getStreamHandlers();

                for (int i = 0; i < streamHandlers.size(); i++) {
                    if (streamHandlers.get(i) instanceof Window) {
                        throw new SiddhiAppValidationException("Window queries can not have parallel greater than 1  ");
                    }
                }
            }
        }

    }

    private TransportStrategy checkQueryStrategy(InputStream inputStream, boolean queryElement, String streamId,
                                                 int parallel) {
        if (parallel > 1) {
            if (!queryElement) {
                if (siddhiTopologyDataHolder.getPartitionTypeMap().containsKey(streamId)) {
                    return TransportStrategy.FIELD_GROUPING;
                } else {
                    //inside a partition but not a partitioned stream
                    return TransportStrategy.ALL;
                }

            } else {
                return TransportStrategy.ROUND_ROBIN;
            }
        } else {
            return TransportStrategy.ALL;
        }

    }

    private List<SiddhiQueryGroup> assignPublishingStrategyOutputStream() {

        SiddhiQueryGroup siddhiQueryGroup;
        boolean fieldGrouping;
        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());

        for (int i = 0; i < siddhiQueryGroupsList.size(); i++) {
            for (String key : siddhiQueryGroupsList.get(i).getOutputStream().keySet()) {

                if (siddhiQueryGroupsList.get(i).getOutputStream().get(key).getEventHolderType()
                        .equals(EventHolder.STREAM)) {
                    for (int j = i + 1; j < siddhiQueryGroupsList.size(); j++) {
                        if (siddhiQueryGroupsList.get(j).getInputStreams().containsKey(key)) {

                            siddhiQueryGroup = siddhiQueryGroupsList.get(j);
                            SubscriptionStrategyDataHolder subscriptionStrategy =
                                    siddhiQueryGroup.getInputStreams().get(key).getSubscriptionStrategy();
                            if (subscriptionStrategy.getStrategy().equals(TransportStrategy.FIELD_GROUPING)) {

                                fieldGrouping = true;
                                for (PublishingStrategyDataHolder publishingStrategyDataHolder : siddhiQueryGroupsList
                                        .get(i).getOutputStream().get(key).getPublishingStrategyList()) {

                                    if (publishingStrategyDataHolder.getGroupingField() != null
                                            && publishingStrategyDataHolder.getGroupingField()
                                            .equals(siddhiTopologyDataHolder.getPartitionTypeMap().get(key)
                                                    .getFirst())) {

                                        int index = siddhiQueryGroupsList.get(i).getOutputStream().get(key)
                                                .getPublishingStrategyList().indexOf(publishingStrategyDataHolder);

                                        //check partition-key if same ---> need to check for max-No of groups
                                        siddhiQueryGroupsList.get(i).getOutputStream().get(key)
                                                .getPublishingStrategyList().get(index).setParallelism(
                                                Math.max(siddhiQueryGroupsList.get(i).getParallelism(),
                                                        siddhiQueryGroupsList.get(j).getParallelism()));

                                        siddhiQueryGroupsList.get(j).getInputStreams().get(key)
                                                .getSubscriptionStrategy().setOfferedParallelism(
                                                Math.max(siddhiQueryGroupsList.get(i).getParallelism(),
                                                        siddhiQueryGroupsList.get(j).getParallelism()));


                                        siddhiTopologyDataHolder.getPartitionTypeMap().get(key).removeFirst();
                                        //else new strategy;
                                        fieldGrouping = false;
                                        break;
                                    }
                                }
                                if (fieldGrouping) {
                                    siddhiQueryGroupsList.get(i).getOutputStream().get(key).addPublishingStrategy(
                                            new PublishingStrategyDataHolder(siddhiQueryGroup.getName(),
                                                    TransportStrategy.FIELD_GROUPING,
                                                    siddhiTopologyDataHolder
                                                            .getPartitionTypeMap().get(key)
                                                            .getFirst(),
                                                    siddhiQueryGroup.getParallelism()));
                                    siddhiQueryGroupsList.get(j).getInputStreams().get(key).getSubscriptionStrategy()
                                            .setPartitionKey(
                                                    siddhiTopologyDataHolder.getPartitionTypeMap().get(key).getFirst());
                                    siddhiTopologyDataHolder.getPartitionTypeMap().get(key).removeFirst();

                                }

                            } else {
                                siddhiQueryGroupsList.get(i).getOutputStream().get(key).addPublishingStrategy(
                                        new PublishingStrategyDataHolder(siddhiQueryGroup.getName(),
                                                subscriptionStrategy.getStrategy(),
                                                siddhiQueryGroup.getParallelism()));
                            }

                        }
                    }
                }
            }
        }
        return siddhiQueryGroupsList;
    }

}