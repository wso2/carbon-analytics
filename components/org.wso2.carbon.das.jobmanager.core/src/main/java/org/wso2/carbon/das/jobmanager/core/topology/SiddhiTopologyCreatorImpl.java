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

import org.apache.log4j.Logger;
import org.wso2.carbon.das.jobmanager.core.SiddhiTopologyCreator;
import org.wso2.carbon.das.jobmanager.core.util.EventHolder;
import org.wso2.carbon.das.jobmanager.core.util.SiddhiTopologyCreatorConstants;
import org.wso2.carbon.das.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppRuntimeException;
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

/**
 *Consumes a Siddhi App and produce a {@link SiddhiTopology} based on distributed annotations.
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
        String defaultExecGroupName =null;
        this.siddhiTopologyDataHolder = new SiddhiTopologyDataHolder(getAppName(), userDefinedSiddhiApp);

        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            parallel = 1;
            execGroupName = null;

            for (Annotation annotation: executionElement.getAnnotations()) {
                if (annotation.getElement(SiddhiTopologyCreatorConstants.execGroupIdentifier) != null) {
                    execGroupName = siddhiTopologyDataHolder.getSiddhiAppName()
                                    + "-"
                                    + annotation.getElement(SiddhiTopologyCreatorConstants.execGroupIdentifier);
                }
                if (annotation.getElement(SiddhiTopologyCreatorConstants.parallelIdentifier) != null) {
                    parallel = Integer.parseInt(
                            annotation.getElement(SiddhiTopologyCreatorConstants.parallelIdentifier));
                }
            }

            if (execGroupName == null && defaultExecGroupName != null){
                execGroupName =defaultExecGroupName;
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
                execGroupName = siddhiTopologyDataHolder.getSiddhiAppName() + "-" + UUID.randomUUID();
                defaultExecGroupName = execGroupName;
                siddhiQueryGroup.setName(execGroupName);
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
                        (Query) executionElement, siddhiQueryGroup, true));

            } else if (executionElement instanceof Partition) {
                //set Partition
                queryContextStartIndex = ((Partition) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Partition) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetainfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

                //assign partitionKeyMap and partitionGroupMap
                storePartitionInfo((Partition) executionElement, execGroupName);

                for (Query query : ((Partition) executionElement).getQueryList()) {
                    for (Annotation annotation:  query.getAnnotations()) {
                        if (annotation.getElement(SiddhiTopologyCreatorConstants.distributedIdentifier) != null) {
                            throw new SiddhiAppValidationException(
                                    "Unsupported:@dist annotation inside partition queries");
                        }
                    }
                    siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(
                            execGroupName, assignStreamInfoSiddhiQueryGroup(query,siddhiQueryGroup, false));
                }
            }
        }

    //prior to assigning publishing strategies checking if a user given source stream is used in multiple execGroups
        checkUserGivenSourceDistribution();
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

        for (Annotation annotation: executionElement.getAnnotations()) {
            if (annotation.getName().toLowerCase()
                    .equals(SiddhiTopologyCreatorConstants.distributedIdentifier)) {
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

    private String removeMetaInfoStream(String streamId, String streamDefinition) {
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

    private SiddhiQueryGroup assignStreamInfoSiddhiQueryGroup(Query executionElement,SiddhiQueryGroup siddhiQueryGroup,
                                                              boolean queryElement) {
        StreamInfoDataHolder streamInfoDataHolder;
        int parallel = siddhiQueryGroup.getParallelism();
        InputStream inputStream = (executionElement).getInputStream();

        //check for validity of the query type eg:join , window, pattern , sequence
        //if joined or .... with partitioned stream for parallel > 1----> pass
        //else if ---> restrict
        if (parallel > 1) {
            checkQueryType(inputStream, queryElement,siddhiQueryGroup.getName());
        }

        for (String inputStreamId : inputStream.getAllStreamIds()) {
            //not an inner Stream
            if (!inputStreamId.contains(SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
                streamInfoDataHolder = extractStreamInfo(inputStreamId, parallel, siddhiQueryGroup.getName());
                TransportStrategy transportStrategy =
                        findStreamSubscriptionStrategy(queryElement, inputStreamId, parallel,
                                                       siddhiQueryGroup.getName());
                //conflicting strategies for an stream in same in same execGroup
                if (siddhiQueryGroup.getInputStreams().get(inputStreamId) != null &&
                        siddhiQueryGroup.getInputStreams().get(inputStreamId).getSubscriptionStrategy().getStrategy()
                                !=transportStrategy){

                    if (siddhiQueryGroup.getInputStreams().get(inputStreamId).getSubscriptionStrategy().getStrategy()
                            .equals(TransportStrategy.ROUND_ROBIN) && transportStrategy.equals(TransportStrategy.FIELD_GROUPING))
                    {
                        //if query given before partition --> Partitioned Stream--> RR and FG ->accept
                    }
                    else {
                        //if query given before partition --> Unpartitioned Stream --> RR and ALL ->don't
                        //if query given after partition --> Partitioned Stream --> ALL and RR ->don't
                        //TODO:change exception message
                        throw new SiddhiAppValidationException("Unsupported: " +inputStreamId+ " in execGroup "
                                                                       + siddhiQueryGroup.getName()
                                                                       + " having conflicting strategies.." );
                    }

                }
                siddhiQueryGroup.addInputStreamHolder(inputStreamId,
                                                   new InputStreamDataHolder(inputStreamId,
                                                                             streamInfoDataHolder.getStreamDefinition(),
                                                                             streamInfoDataHolder.getEventHolderType(),
                                                                             streamInfoDataHolder.isUserGiven(),
                                                                             new SubscriptionStrategyDataHolder(
                                                                                     parallel, transportStrategy)));
            }
        }

        String outputStreamId = executionElement.getOutputStream().getId();
        //not an inner Stream
        if (!outputStreamId.contains(SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
            streamInfoDataHolder = extractStreamInfo(outputStreamId, parallel, siddhiQueryGroup.getName());
            siddhiQueryGroup.addOutputStreamHolder(outputStreamId,
                                                   new OutputStreamDataHolder(outputStreamId,
                                                                             streamInfoDataHolder.getStreamDefinition(),
                                                                             streamInfoDataHolder.getEventHolderType(),
                                                                             streamInfoDataHolder.isUserGiven()));
        }
        return siddhiQueryGroup;
    }

    private StreamInfoDataHolder extractStreamInfo(String streamId, int parallel, String groupName) {
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

            for (Annotation annotation: tableDefinition.getAnnotations()) {
                if (annotation.getName().toLowerCase().equals(
                        SiddhiTopologyCreatorConstants.persistenceTableIdentifier)) {
                        streamInfoDataHolder.setEventHolderType(EventHolder.TABLE);
                        break;
                }
            }
            //Validate table parallelism
            if (parallel != 1 && streamInfoDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE)) {
                throw new SiddhiAppValidationException("Unsupported: "
                                                               +groupName
                                                               + " with In-Memory Table "
                                                               + " having parallel >1 ");
            }

            queryContextStartIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextEndIndex();
            streamInfoDataHolder.setStreamDefinition(ExceptionUtil.getContext(queryContextStartIndex,
                                                     queryContextEndIndex,
                                                     siddhiTopologyDataHolder.getUserDefinedSiddhiApp()));

            if (streamInfoDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE) && siddhiTopologyDataHolder
                    .getInMemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInMemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:Event Table "
                                                            + streamId
                                                            + " In-Memory Table referenced from more than one"
                                                            + " execGroup: execGroup "
                                                            + groupName
                                                            + " && "
                                                            + siddhiTopologyDataHolder.getInMemoryMap().get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInMemoryMap().put(streamId, groupName);
            }

        } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
            if (parallel != 1) {
                throw new SiddhiAppValidationException("Unsupported: "
                                                              + groupName
                                                              + " with (Defined) Window "
                                                              + " having parallel >1");
            }

            queryContextStartIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextEndIndex();

            streamInfoDataHolder.setStreamDefinition(ExceptionUtil.getContext(queryContextStartIndex,
                                                     queryContextEndIndex,
                                                     siddhiTopologyDataHolder.getUserDefinedSiddhiApp()));
            streamInfoDataHolder.setEventHolderType(EventHolder.WINDOW);

            if (siddhiTopologyDataHolder.getInMemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInMemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:(Defined) Window "
                                                             + streamId
                                                             + " In-Memory window referenced from more than one"
                                                             + " execGroup: execGroup "
                                                             + groupName
                                                             + " && "
                                                             + siddhiTopologyDataHolder.getInMemoryMap().get(streamId));
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
                    throw new SiddhiAppValidationException("Unsupported: "
                                                                 + groupName
                                                                 + " with In-Memory Table "
                                                                 + " having parallel >1 ");
                }
                streamInfoDataHolder =
                        new StreamInfoDataHolder(siddhiAppRuntime.getTableDefinitionMap().get(streamId).toString(),
                                                 EventHolder.INMEMORYTABLE, false);
            }
        }
        return streamInfoDataHolder;
    }

    private boolean isUserGivenStream(String streamDefinition) {
        return streamDefinition.toLowerCase().contains(
                SiddhiTopologyCreatorConstants.sourceIdentifier) || streamDefinition.toLowerCase().contains
                (SiddhiTopologyCreatorConstants.sinkIdentifier);
    }

    private void checkQueryType(InputStream inputStream, boolean queryElement,String execGroup) {
        boolean partitionStreamExist = false;

        for (String streamId : inputStream.getUniqueStreamIds()) {
            //join,sequence ,pattern,window happens (with/as) partitioned key or inner streams
            if ((siddhiTopologyDataHolder.getPartitionGroupMap().get(streamId)!=null &&
                    siddhiTopologyDataHolder.getPartitionGroupMap().get(streamId).contains(execGroup)) ||
                    streamId.contains(SiddhiTopologyCreatorConstants.innerStreamIdentifier)) {
                partitionStreamExist = true;
                break;
            }
        }

        if (queryElement || !partitionStreamExist) {
            if (inputStream instanceof JoinInputStream) {
                throw new SiddhiAppValidationException(execGroup
                                                      + "Join queries used with parallel greater than 1 outside "
                                                      + "partitioned stream");
            } else if (inputStream instanceof StateInputStream) {
                String type = ((StateInputStream) inputStream).getStateType().name();
                throw new SiddhiAppValidationException(execGroup
                                                       +type
                                                       + " queries used with parallel greater than 1 outside "
                                                       + "partitioned stream");

            } else if (inputStream instanceof SingleInputStream) {
                List<StreamHandler> streamHandlers = ((SingleInputStream) inputStream).getStreamHandlers();
                for (StreamHandler streamHandler : streamHandlers) {
                    if (streamHandler instanceof Window) {
                        throw new SiddhiAppValidationException(execGroup
                                                               +" Window queries used with parallel greater than "
                                                               + "1 outside "
                                                               + "partitioned stream");
                    }
                }
            }
        }
    }

    private void checkUserGivenSourceDistribution(){

        int i=0;
        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());

        for (SiddhiQueryGroup siddhiQueryGroup1 : siddhiQueryGroupsList) {
            for (String streamId : siddhiQueryGroup1.getInputStreams().keySet()) {
                for (SiddhiQueryGroup siddhiQueryGroup2 : siddhiQueryGroupsList.subList(i + 1,
                                                                                        siddhiQueryGroupsList.size())){
                    //usergiven source used in more than 1 execGroup
                    //adding isolating the source stream with a passthrough will fix the issue
                    if (siddhiQueryGroup1.getInputStreams().get(streamId).isUserGiven() &&
                            siddhiQueryGroup2.getInputStreams().containsKey(streamId) ){
                        throw new SiddhiAppRuntimeException("Unsupported: External source "+ streamId +" in multiple "
                                                                    + "execGroups:");
                    }
                }
            }
            i++;
        }
    }

    private TransportStrategy findStreamSubscriptionStrategy(boolean queryElement, String streamId, int parallel,
                                                             String execGroup) {
        if (parallel > 1) {
            if (!queryElement) { //partition
                if (siddhiTopologyDataHolder.getPartitionGroupMap().get(streamId) != null &&
                        siddhiTopologyDataHolder.getPartitionGroupMap().get(streamId).contains(execGroup)) {
                    return TransportStrategy.FIELD_GROUPING;
                } else {
                    //inside a partition but not a partitioned stream
                    return TransportStrategy.ALL;
                }
            } else {
                //in same execGroup --> if stream exists in Partitioned and query ---> then
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
        String runtimeStreamDefinition;
        String outputStreamDefinition;
        SubscriptionStrategyDataHolder subscriptionStrategy;
        int i = 0;
        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());

        for (SiddhiQueryGroup siddhiQueryGroup1 : siddhiQueryGroupsList) {
            for (String streamId : siddhiQueryGroup1.getOutputStream().keySet()) {

                if (siddhiQueryGroup1.getOutputStream().get(streamId).getEventHolderType()
                        .equals(EventHolder.STREAM)) {
                    for (SiddhiQueryGroup siddhiQueryGroup2 : siddhiQueryGroupsList.subList(i + 1,
                                                                                       siddhiQueryGroupsList.size())) {
                        if (siddhiQueryGroup2.getInputStreams().containsKey(streamId)) {
                            //when user given sink stream used by diff execGroup as a source stream
                            //additional sink will be added
                            if (siddhiQueryGroup1.getOutputStream().get(streamId).isUserGiven()) {
                                runtimeStreamDefinition = removeMetaInfoStream(streamId,
                                                                               siddhiQueryGroup2.getInputStreams()
                                                                                        .get(streamId)
                                                                                        .getStreamDefinition());
                                outputStreamDefinition = siddhiQueryGroup1.getOutputStream().get(streamId).
                                        getStreamDefinition().replace(runtimeStreamDefinition,"\n"
                                        + "${" + streamId
                                        + "} ")
                                        + runtimeStreamDefinition;
                                siddhiQueryGroup1.getOutputStream().get(streamId)
                                        .setStreamDefinition(outputStreamDefinition);
                                siddhiQueryGroup2.getInputStreams().get(streamId).setStreamDefinition(
                                        "${" + streamId + "} " + runtimeStreamDefinition);
                                siddhiQueryGroup2.getInputStreams().get(streamId).setUserGiven(false);
                            }

                             subscriptionStrategy =
                                    siddhiQueryGroup2.getInputStreams().get(streamId).getSubscriptionStrategy();
                            if (subscriptionStrategy.getStrategy().equals(TransportStrategy.FIELD_GROUPING)) {
                                fieldGrouping = true;
                                for (PublishingStrategyDataHolder publishingStrategyDataHolder :
                                        siddhiQueryGroup1.getOutputStream().get(streamId).getPublishingStrategyList()){

                                    if (publishingStrategyDataHolder.getGroupingField() != null
                                            && publishingStrategyDataHolder.getGroupingField()
                                            .equals(siddhiTopologyDataHolder.getPartitionKeyMap().get(streamId)
                                                            .getFirst())) {

                                        int index = siddhiQueryGroup1.getOutputStream().get(streamId)
                                                .getPublishingStrategyList().indexOf(publishingStrategyDataHolder);

                                        //if more than 1 execGroup contains same partition key with diff parallelism
                                        siddhiQueryGroup1.getOutputStream().get(streamId)
                                                .getPublishingStrategyList().get(index).setParallelism(
                                                Math.max(siddhiQueryGroup1.getParallelism(),
                                                         siddhiQueryGroup2.getParallelism()));

                                        siddhiQueryGroup2.getInputStreams().get(streamId)
                                                .getSubscriptionStrategy().setOfferedParallelism(
                                                Math.max(siddhiQueryGroup1.getParallelism(),
                                                         siddhiQueryGroup2.getParallelism()));

                                        siddhiTopologyDataHolder.getPartitionKeyMap().get(streamId).removeFirst();
                                        //else new strategy;
                                        fieldGrouping = false;
                                        break;
                                    }
                                }
                                if (fieldGrouping) {
                                    siddhiQueryGroup1.getOutputStream().get(streamId).addPublishingStrategy(
                                            new PublishingStrategyDataHolder(siddhiQueryGroup2.getName(),
                                                                             TransportStrategy.FIELD_GROUPING,
                                                                             siddhiTopologyDataHolder
                                                                                     .getPartitionKeyMap().get(streamId)
                                                                                     .getFirst(),
                                                                             siddhiQueryGroup2.getParallelism()));
                                    siddhiQueryGroup2.getInputStreams().get(streamId).getSubscriptionStrategy()
                                            .setPartitionKey(
                                                    siddhiTopologyDataHolder.getPartitionKeyMap().get(streamId).getFirst());
                                    siddhiTopologyDataHolder.getPartitionKeyMap().get(streamId).removeFirst();

                                }
                            } else {
                                siddhiQueryGroup1.getOutputStream().get(streamId).addPublishingStrategy(
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

    private void storePartitionInfo(Partition partition, String execGroupName) {
        LinkedList<String> partitionKeyList; //contains all the partition-keys corresponding to a partitioned streamId.
        LinkedList<String> partitionGroupList;//contains all the execGroups containing partitioned streamId

        //assign partitionKeyMap and partitionGroupMap
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
                throw new SiddhiAppValidationException("Unsupported in distributed setup :More than 1 partition "
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
                throw new SiddhiAppValidationException("Unsupported: "
                                                               +execGroupName+" Range PartitionType not Supported in "
                                                               + "Distributed SetUp");
            }
        }
    }
}