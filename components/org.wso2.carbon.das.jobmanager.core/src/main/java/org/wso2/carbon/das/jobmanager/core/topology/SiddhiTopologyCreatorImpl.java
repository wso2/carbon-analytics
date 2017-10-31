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

import org.apache.commons.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.SiddhiTopologyCreator;
import org.wso2.carbon.das.jobmanager.core.util.EventHolder;
import org.wso2.carbon.das.jobmanager.core.util.SiddhiTopologyCreatorConstants;
import org.wso2.carbon.das.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
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
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.query.api.util.ExceptionUtil;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Consumes a Siddhi App and produce a {@link SiddhiTopology} based on distributed annotations.
 */

public class SiddhiTopologyCreatorImpl implements SiddhiTopologyCreator {
    private static final Logger log = Logger.getLogger(SiddhiTopologyCreatorImpl.class);
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
        this.siddhiTopologyDataHolder = new SiddhiTopologyDataHolder(getSiddhiAppName(), userDefinedSiddhiApp);
        String defaultExecGroupName = siddhiTopologyDataHolder.getSiddhiAppName() + "-" + UUID.randomUUID();

        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {

            parallel = getExecGroupParallel(executionElement);
            execGroupName = getExecGroupName(executionElement,siddhiTopologyDataHolder.getSiddhiAppName(),defaultExecGroupName);
            siddhiQueryGroup = createSiddhiQueryGroup(execGroupName,parallel);

            if (executionElement instanceof Query) {
                //set query
                queryContextStartIndex = ((Query) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Query) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetaInfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

                assignStreamInfoSiddhiQueryGroup((Query) executionElement, siddhiQueryGroup, true);
                siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(execGroupName, siddhiQueryGroup);

            } else if (executionElement instanceof Partition) {
                //set Partition
                queryContextStartIndex = ((Partition) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Partition) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetaInfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

                //assign partitionKeyMap ,partitionGroupMap
                storePartitionInfo((Partition) executionElement, execGroupName);

                for (Query query : ((Partition) executionElement).getQueryList()) {
                    for (Annotation annotation : query.getAnnotations()) {
                        if (annotation.getElement(SiddhiTopologyCreatorConstants.DISTRIBUTED_IDENTIFIER) != null) {
                            throw new SiddhiAppValidationException(
                                    "Unsupported:@dist annotation inside partition queries");
                        }
                    }
                    assignStreamInfoSiddhiQueryGroup(query, siddhiQueryGroup, false);
                }

                siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(execGroupName, siddhiQueryGroup);
            }
        }
        //prior to assigning publishing strategies checking if a user given source stream is used in multiple execGroups
        checkUserGivenSourceDistribution();
        assignPublishingStrategyOutputStream();
        return new SiddhiTopology(siddhiTopologyDataHolder.getSiddhiAppName(), new ArrayList<>
                (siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values()));
    }

    private String getSiddhiAppName() {
        Element element = AnnotationHelper.getAnnotationElement("name",null,siddhiApp.getAnnotations());
        if (element != null){
            return element.getValue();
        }
        else {
            return SiddhiTopologyCreatorConstants.DEFAULT_SIDDHIAPP_NAME + "-" + UUID.randomUUID();//defaultName
        }
    }

    private String getExecGroupName(ExecutionElement executionElement,String siddhiAppName,String defaultExeGroupName){
        Element element = AnnotationHelper.getAnnotationElement(SiddhiTopologyCreatorConstants.DISTRIBUTED_IDENTIFIER,
                                                                SiddhiTopologyCreatorConstants.EXECGROUP_IDENTIFIER,
                                                                executionElement.getAnnotations());
        if (element != null){
            return  siddhiAppName + "-" + element.getValue();
        }
        else {
            return defaultExeGroupName;
        }
    }

    private int getExecGroupParallel(ExecutionElement executionElement){
        Element element = AnnotationHelper.getAnnotationElement(SiddhiTopologyCreatorConstants
                                                                        .DISTRIBUTED_IDENTIFIER,
                                                                SiddhiTopologyCreatorConstants.PARALLEL_IDENTIFIER,executionElement
                                                                        .getAnnotations());
        if (element != null ){
            return   Integer.parseInt(element.getValue());
        }
        else {
            return SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL;
        }
    }

    private SiddhiQueryGroup createSiddhiQueryGroup(String execGroupName,int parallel){
        SiddhiQueryGroup siddhiQueryGroup;
        if (!siddhiTopologyDataHolder.getSiddhiQueryGroupMap().containsKey(execGroupName)) {
            siddhiQueryGroup =new SiddhiQueryGroup(execGroupName,parallel);
        } else {
            siddhiQueryGroup = siddhiTopologyDataHolder.getSiddhiQueryGroupMap().get(execGroupName);
            //Same execution group given  with different parallel numbers
            if (siddhiQueryGroup.getParallelism() != parallel) {
                throw new SiddhiAppValidationException(
                        "execGroup =" + "\'" + execGroupName + "\' not assigned unique @dist(parallel)");
            }
        }
        return siddhiQueryGroup;
    }

    private String removeMetaInfoQuery(ExecutionElement executionElement, String queryElement) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (Annotation annotation : executionElement.getAnnotations()) {
            if (annotation.getName().toLowerCase()
                    .equals(SiddhiTopologyCreatorConstants.DISTRIBUTED_IDENTIFIER)) {
                queryContextStartIndex = annotation.getQueryContextStartIndex();
                queryContextEndIndex = annotation.getQueryContextEndIndex();
                queryElement = queryElement.replace(
                        ExceptionUtil.getContext(queryContextStartIndex,
                                                 queryContextEndIndex,
                                                 siddhiTopologyDataHolder.getUserDefinedSiddhiApp()), "");
                break;
            }
        }
        return queryElement;
    }

    private String removeMetaInfoStream(String streamId, String streamDefinition, String identifier) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (Annotation annotation : siddhiApp.getStreamDefinitionMap().get(streamId).getAnnotations()) {
            if (annotation.getName().toLowerCase().equals(
                    identifier.replace("@", ""))) {
                queryContextStartIndex = annotation.getQueryContextStartIndex();
                queryContextEndIndex = annotation.getQueryContextEndIndex();
                streamDefinition = streamDefinition.replace(
                        ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex,
                                                 siddhiTopologyDataHolder.getUserDefinedSiddhiApp()), "");
            }
        }
        return streamDefinition;
    }

    //TODO:rename
    //TODO:assign siddhiQueryGroup from returns
    private void assignStreamInfoSiddhiQueryGroup(Query executionElement, SiddhiQueryGroup siddhiQueryGroup,
                                                  boolean isQuery) {
        EventInfoDataHolder eventInfoDataHolder;
        int parallel = siddhiQueryGroup.getParallelism();
        InputStream inputStream = (executionElement).getInputStream();

        //check for validity of the query type eg:join , window, pattern , sequence
        //if joined or .... with partitioned stream for parallel > 1----> pass
        //else if ---> restrict
        if (parallel > SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL) {
            validateQueryType(inputStream, isQuery, siddhiQueryGroup.getName());
        }
        for (String inputStreamId : inputStream.getUniqueStreamIds()) {
            //not an inner Stream
            if (!inputStreamId.startsWith(SiddhiTopologyCreatorConstants.INNERSTREAM_IDENTIFIER)) {
                eventInfoDataHolder = extractEventHolderInfo(inputStreamId, parallel, siddhiQueryGroup.getName());
                TransportStrategy transportStrategy = findStreamSubscriptionStrategy(isQuery, inputStreamId, parallel,
                                                       siddhiQueryGroup.getName());
                //conflicting strategies for an stream in same in same execGroup
                if (siddhiQueryGroup.getInputStreams().get(inputStreamId) != null &&
                        siddhiQueryGroup.getInputStreams().get(inputStreamId).getSubscriptionStrategy().getStrategy()
                                != transportStrategy) {

                    //if query given before partition --> Partitioned Stream--> RR and FG ->accept
                    if (!(siddhiQueryGroup.getInputStreams().get(inputStreamId).getSubscriptionStrategy().getStrategy()
                            .equals(TransportStrategy.ROUND_ROBIN) && transportStrategy
                            .equals(TransportStrategy.FIELD_GROUPING))) {
                        //if query given before partition --> Unpartitioned Stream --> RR and ALL ->don't
                        throw new SiddhiAppValidationException("Unsupported: " + inputStreamId + " in execGroup "
                                                                       + siddhiQueryGroup.getName()
                                                                       + " having conflicting strategies..");
                    }
                }

                String partitionKey = findPartitionKey(inputStreamId,isQuery);
                siddhiQueryGroup.addInputStreamHolder(inputStreamId,
                                                      new InputStreamDataHolder(inputStreamId,
                                                                                eventInfoDataHolder
                                                                                        .getStreamDefinition(),
                                                                                eventInfoDataHolder
                                                                                        .getEventHolderType(),
                                                                                eventInfoDataHolder.isUserGiven(),
                                                                                new SubscriptionStrategyDataHolder(
                                                                                        parallel, transportStrategy,
                                                                                        partitionKey)));
            }
        }

        String outputStreamId = executionElement.getOutputStream().getId();
        //not an inner Stream
        if (!outputStreamId.startsWith(SiddhiTopologyCreatorConstants.INNERSTREAM_IDENTIFIER)) {
            eventInfoDataHolder = extractEventHolderInfo(outputStreamId, parallel, siddhiQueryGroup.getName());
            siddhiQueryGroup.addOutputStreamHolder(outputStreamId,
                                                   new OutputStreamDataHolder(outputStreamId,
                                                                              eventInfoDataHolder.getStreamDefinition(),
                                                                              eventInfoDataHolder.getEventHolderType(),
                                                                              eventInfoDataHolder.isUserGiven()));
        }
    }

    private String findPartitionKey(String streamID, boolean isQuery){
        if (!isQuery){
           return siddhiTopologyDataHolder.getPartitionKeyMap().get(streamID);
        }
        else
        {
            return null;
        }
    }

    private EventInfoDataHolder extractEventHolderInfo(String streamId, int parallel, String groupName) {
        String streamDefinition;
        int[] queryContextEndIndex;
        int[] queryContextStartIndex;
        EventInfoDataHolder eventInfoDataHolder = new EventInfoDataHolder(true);

        if (siddhiApp.getStreamDefinitionMap().containsKey(streamId)) {

            queryContextStartIndex = siddhiApp.getStreamDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getStreamDefinitionMap().get(streamId).getQueryContextEndIndex();
            streamDefinition = ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex,
                                                        siddhiTopologyDataHolder.getUserDefinedSiddhiApp());

            if (!isUserGivenStream(streamDefinition)) {
                streamDefinition = "${" + streamId + "}" + streamDefinition;
            }
            eventInfoDataHolder =
                    new EventInfoDataHolder(streamDefinition, EventHolder.STREAM, isUserGivenStream(streamDefinition));

        } else if (siddhiApp.getTableDefinitionMap().containsKey(streamId)) {
            AbstractDefinition tableDefinition = siddhiApp.getTableDefinitionMap().get(streamId);
            eventInfoDataHolder.setEventHolderType(EventHolder.INMEMORYTABLE);

            for (Annotation annotation : tableDefinition.getAnnotations()) {
                if (annotation.getName().toLowerCase().equals(
                        SiddhiTopologyCreatorConstants.PERSISTENCETABLE_IDENTIFIER)) {
                    eventInfoDataHolder.setEventHolderType(EventHolder.TABLE);
                    break;
                }
            }
            //Validate table parallelism
            if (parallel != SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL &&
                    eventInfoDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE)) {
                throw new SiddhiAppValidationException("Unsupported: "
                                                               + groupName
                                                               + " with In-Memory Table "
                                                               + " having parallel >1 ");
            }

            queryContextStartIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextEndIndex();
            eventInfoDataHolder.setStreamDefinition(ExceptionUtil.getContext(queryContextStartIndex,
                                                                             queryContextEndIndex,
                                                                             siddhiTopologyDataHolder
                                                                                     .getUserDefinedSiddhiApp()));

            if (eventInfoDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE) && siddhiTopologyDataHolder
                    .getInMemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInMemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:Event Table "
                                                                   + streamId
                                                                   + " In-Memory Table referenced from more than one"
                                                                   + " execGroup: execGroup "
                                                                   + groupName
                                                                   + " && "
                                                                   + siddhiTopologyDataHolder.getInMemoryMap()
                                                                                                    .get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInMemoryMap().put(streamId, groupName);
            }

        } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
            if (parallel != SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL) {
                throw new SiddhiAppValidationException("Unsupported: "
                                                               + groupName
                                                               + " with (Defined) Window "
                                                               + " having parallel >1");
            }

            queryContextStartIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextEndIndex();

            eventInfoDataHolder.setStreamDefinition(ExceptionUtil.getContext(queryContextStartIndex,
                                                                             queryContextEndIndex,
                                                                             siddhiTopologyDataHolder
                                                                                     .getUserDefinedSiddhiApp()));
            eventInfoDataHolder.setEventHolderType(EventHolder.WINDOW);

            if (siddhiTopologyDataHolder.getInMemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInMemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:(Defined) Window "
                                                                   + streamId
                                                                   + " In-Memory window referenced from more than one"
                                                                   + " execGroup: execGroup "
                                                                   + groupName
                                                                   + " && "
                                                                   + siddhiTopologyDataHolder.getInMemoryMap()
                            .get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInMemoryMap().put(streamId, groupName);
            }

            //if stream definition is an inferred definition
        } else if (eventInfoDataHolder.getStreamDefinition() == null) {
            if (siddhiAppRuntime.getStreamDefinitionMap().containsKey(streamId)) {
                eventInfoDataHolder = new EventInfoDataHolder(
                        "${" + streamId + "}"
                                + siddhiAppRuntime.getStreamDefinitionMap().get(streamId).toString(),
                        EventHolder.STREAM, false);
            }
        }
        return eventInfoDataHolder;
    }

    private boolean isUserGivenStream(String streamDefinition) {
        return streamDefinition.toLowerCase().contains(
                SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER) || streamDefinition.toLowerCase().contains
                (SiddhiTopologyCreatorConstants.SINK_IDENTIFIER);
    }

    private void validateQueryType(InputStream inputStream, boolean isQuery, String execGroup) {
        boolean partitionStreamExist = false;

        for (String streamId : inputStream.getUniqueStreamIds()) {
            //join,sequence ,pattern,window happens (with/as) partitioned key or inner streams
            if ((siddhiTopologyDataHolder.getPartitionGroupMap().containsKey(streamId) &&
                    siddhiTopologyDataHolder.getPartitionGroupMap().get(streamId).contains(execGroup)) ||
                    streamId.startsWith(SiddhiTopologyCreatorConstants.INNERSTREAM_IDENTIFIER)) {
                partitionStreamExist = true;
                break;
            }
        }

        if (isQuery || !partitionStreamExist) {
            if (inputStream instanceof JoinInputStream) {
                throw new SiddhiAppValidationException(execGroup
                                                               + "Join queries used with parallel greater than 1 "
                                                               + "outside "
                                                               + "partitioned stream");
            } else if (inputStream instanceof StateInputStream) {
                String type = ((StateInputStream) inputStream).getStateType().name();
                throw new SiddhiAppValidationException(execGroup
                                                               + type
                                                               + " queries used with parallel greater than 1 outside "
                                                               + "partitioned stream");

            } else if (inputStream instanceof SingleInputStream) {
                List<StreamHandler> streamHandlers = ((SingleInputStream) inputStream).getStreamHandlers();
                for (StreamHandler streamHandler : streamHandlers) {
                    if (streamHandler instanceof Window) {
                        throw new SiddhiAppValidationException(execGroup
                                                                + " Window queries used with parallel greater "
                                                                + "than "
                                                                + "1 outside "
                                                                + "partitioned stream");
                    }
                }
            }
        }
    }

    private void checkUserGivenSourceDistribution() {
        int i = 0;
        boolean createPassthrough;          //create passthrough query for each user given source stream
        boolean addFirst = false;           //move passthrough query to front of SiddhiQueryGroupList

        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());
        SiddhiQueryGroup passthroughQueryGroup = createPassthroughQueryGroup();

        for (SiddhiQueryGroup siddhiQueryGroup1 : siddhiQueryGroupsList) {
            for (Map.Entry<String, InputStreamDataHolder> entry : siddhiQueryGroup1.getInputStreams().entrySet()) {
                String streamId = entry.getKey();
                InputStreamDataHolder inputStreamDataHolder = entry.getValue();
                if (inputStreamDataHolder.getEventHolderType().equals(EventHolder.STREAM) && inputStreamDataHolder
                        .isUserGiven()) {
                    createPassthrough = true;
                    for (SiddhiQueryGroup siddhiQueryGroup2 : siddhiQueryGroupsList.subList(i + 1,
                                                                                            siddhiQueryGroupsList
                                                                                                    .size())) {
                        if (siddhiQueryGroup2.getInputStreams().containsKey(streamId)) {
                            String runtimeDefinition = removeMetaInfoStream(streamId,inputStreamDataHolder
                                                                              .getStreamDefinition()
                                    ,SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER);
                            addFirst = true;
                            if (createPassthrough) {
                                createPassthroughQuery(passthroughQueryGroup, inputStreamDataHolder, runtimeDefinition);
                                inputStreamDataHolder.setStreamDefinition(runtimeDefinition);
                                inputStreamDataHolder.setUserGiven(false);
                                createPassthrough = false;

                            }
                            siddhiQueryGroup2.getInputStreams().get(streamId).setStreamDefinition(runtimeDefinition);
                            siddhiQueryGroup2.getInputStreams().get(streamId).setUserGiven(false);
                        }
                    }
                }
            }
            i++;
        }
        if (addFirst) {
            addFirst( passthroughQueryGroup);
        }
    }

    private SiddhiQueryGroup createPassthroughQueryGroup(){

        String passthroughExecGroupName = siddhiTopologyDataHolder.getSiddhiAppName() + "-" + UUID.randomUUID();
        SiddhiQueryGroup passthroughQueryGroup = new SiddhiQueryGroup(passthroughExecGroupName,
                                                                      SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL);
        return passthroughQueryGroup;
    }

    private TransportStrategy findStreamSubscriptionStrategy(boolean isQuery, String streamId, int parallel,
                                                             String execGroup) {
        if (parallel > SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL) {
            //partitioned stream residing in partition/ partition + query of the same execGroup
            if (siddhiTopologyDataHolder.getPartitionGroupMap().containsKey(streamId) &&
                    siddhiTopologyDataHolder.getPartitionGroupMap().get(streamId).contains(execGroup)) {
                return TransportStrategy.FIELD_GROUPING;
            } else {
                if (!isQuery) {
                    //inside a partition but not a partitioned stream
                    return TransportStrategy.ALL;
                } else {
                    return TransportStrategy.ROUND_ROBIN;
                }
            }
        } else {
            return TransportStrategy.ALL;
        }
    }

    private void assignPublishingStrategyOutputStream() {
        int i = 0;
        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());

        for (SiddhiQueryGroup siddhiQueryGroup1 : siddhiQueryGroupsList) {
            for (Map.Entry<String, OutputStreamDataHolder> entry : siddhiQueryGroup1.getOutputStreams().entrySet()) {
                OutputStreamDataHolder outputStreamDataHolder = entry.getValue();
                String streamId = entry.getKey();

                if (outputStreamDataHolder.getEventHolderType().equals(EventHolder.STREAM)) {
                    for (SiddhiQueryGroup siddhiQueryGroup2 : siddhiQueryGroupsList.subList(i + 1,
                                                                                            siddhiQueryGroupsList
                                                                                                    .size())) {
                        if (siddhiQueryGroup2.getInputStreams().containsKey(streamId)) {
                            InputStreamDataHolder inputStreamDataHolder = siddhiQueryGroup2.getInputStreams()
                                    .get(streamId);

                            //user given sink stream used by diff execGroup as a source stream
                            //TODO:move inside a method
                            if (outputStreamDataHolder.isUserGiven()) {
                                String runtimeStreamDefinition = removeMetaInfoStream(streamId,
                                                                                      inputStreamDataHolder
                                                                                              .getStreamDefinition(),
                                                                                      SiddhiTopologyCreatorConstants
                                                                                              .SINK_IDENTIFIER);
                                String outputStreamDefinition = outputStreamDataHolder.
                                        getStreamDefinition().replace(runtimeStreamDefinition, "\n"
                                        + "${" + streamId
                                        + "} ")
                                        + runtimeStreamDefinition;
                                outputStreamDataHolder.setStreamDefinition(outputStreamDefinition);
                                inputStreamDataHolder.setStreamDefinition(
                                        "${" + streamId + "} " + runtimeStreamDefinition);
                                inputStreamDataHolder.setUserGiven(false);
                            }
                            SubscriptionStrategyDataHolder subscriptionStrategy = inputStreamDataHolder.
                                    getSubscriptionStrategy();
                            if (subscriptionStrategy.getStrategy().equals(TransportStrategy.FIELD_GROUPING)) {
                                String partitionKey = inputStreamDataHolder.getSubscriptionStrategy().getPartitionKey();
                                outputStreamDataHolder.addPublishingStrategy(
                                        new PublishingStrategyDataHolder(siddhiQueryGroup2.getName(),
                                                                         TransportStrategy.FIELD_GROUPING,
                                                                         partitionKey, inputStreamDataHolder
                                                                                 .getSubscriptionStrategy().getOfferedParallelism()));

                            } else {
                                outputStreamDataHolder.addPublishingStrategy(
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
    }

    //TODO:comments->method level
    //TODO:debug logs
    private void storePartitionInfo(Partition partition, String execGroupName) {
        LinkedList<String> partitionGroupList;//contains all the execGroups containing partitioned streamId
        String partitionKey;

        //assign partitionKeyMap and partitionGroupMap
        for (Map.Entry<String, PartitionType> partitionTypeEntry : partition.getPartitionTypeMap().entrySet()) {
            if (siddhiTopologyDataHolder.getPartitionGroupMap().containsKey(partitionTypeEntry.getKey())) {
                partitionGroupList = siddhiTopologyDataHolder.getPartitionGroupMap().get(partitionTypeEntry.getKey());
            } else {
                partitionGroupList = new LinkedList<>();
            }
            //when more than one partition residing in the same SiddhiApp
            if (partitionGroupList.contains(execGroupName)) {
                //TODO:more than 1 partition can reside if keys are different
                throw new SiddhiAppValidationException("Unsupported in distributed setup :More than 1 partition "
                                                               + "residing on the same execGroup "
                                                               + execGroupName);
            } else {
                partitionGroupList.add(execGroupName);
                siddhiTopologyDataHolder.getPartitionGroupMap().put(partitionTypeEntry.getKey(),
                                                                    partitionGroupList);
            }
            if (partitionTypeEntry.getValue() instanceof ValuePartitionType) {
                partitionKey = ((Variable) ((ValuePartitionType) partitionTypeEntry.getValue()).getExpression())
                        .getAttributeName();
                siddhiTopologyDataHolder.getPartitionKeyMap().put(partitionTypeEntry.getKey(), partitionKey);
            } else {
                //Not yet supported
                throw new SiddhiAppValidationException("Unsupported: "
                                                               + execGroupName
                                                               + " Range PartitionType not Supported in Distributed "
                                                               + "SetUp");
            }
        }
    }
    private void addFirst( SiddhiQueryGroup siddhiQueryGroup) {
        Map<String, SiddhiQueryGroup> output = new LinkedHashMap();
        output.put(siddhiQueryGroup.getName(), siddhiQueryGroup);
        output.putAll(siddhiTopologyDataHolder.getSiddhiQueryGroupMap());
        siddhiTopologyDataHolder.getSiddhiQueryGroupMap().clear();
        siddhiTopologyDataHolder.getSiddhiQueryGroupMap().putAll(output);

    }

    private void createPassthroughQuery(SiddhiQueryGroup siddhiQueryGroup, InputStreamDataHolder inputStreamDataHolder,
                                        String runtimeDefinition) {
        String streamId = inputStreamDataHolder.getStreamName();
        Map<String, String> valuesMap = new HashMap();
        String inputStreamID = SiddhiTopologyCreatorConstants.DEFAULT_INPUTSTREAM_NAME
                              + UUID.randomUUID().toString().replaceAll("-", "");
        valuesMap.put(SiddhiTopologyCreatorConstants.INPUTSTREAMID, inputStreamID);
        valuesMap.put(SiddhiTopologyCreatorConstants.OUTPUTSTREAMID, streamId);
        StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
        String passThroughQuery = substitutor.replace(SiddhiTopologyCreatorConstants.DEFAULT_PASSTROUGH_QUERY_TEMPLATE);
        siddhiQueryGroup.addQuery(passThroughQuery);
        String inputStreamDefinition = inputStreamDataHolder.getStreamDefinition().replace(streamId, inputStreamID);
        String outputStreamDefinition = "${" + streamId + "} " + runtimeDefinition;
        siddhiQueryGroup.getInputStreams()
                .put(inputStreamID, new InputStreamDataHolder(inputStreamID,
                                                              inputStreamDefinition,
                                                              EventHolder.STREAM, true,
                                                              new SubscriptionStrategyDataHolder(SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL,
                                                                                                 TransportStrategy
                                                                                                         .ALL,null)));
        siddhiQueryGroup.getOutputStreams().put(streamId, new OutputStreamDataHolder(streamId,
                                                                                     outputStreamDefinition,
                                                                                     EventHolder
                                                                                             .STREAM,
                                                                                     false));
    }
}