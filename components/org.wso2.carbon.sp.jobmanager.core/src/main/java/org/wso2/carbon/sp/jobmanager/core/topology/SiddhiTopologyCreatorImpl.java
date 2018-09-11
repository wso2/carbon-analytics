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

package org.wso2.carbon.sp.jobmanager.core.topology;

import org.apache.commons.text.StrSubstitutor;
import org.apache.log4j.Logger;
import org.wso2.carbon.sp.jobmanager.core.SiddhiTopologyCreator;
import org.wso2.carbon.sp.jobmanager.core.util.EventHolder;
import org.wso2.carbon.sp.jobmanager.core.util.SiddhiTopologyCreatorConstants;
import org.wso2.carbon.sp.jobmanager.core.util.TransportStrategy;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.annotation.Element;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
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
        String siddhiAppName = getSiddhiAppName();
        this.siddhiTopologyDataHolder = new SiddhiTopologyDataHolder(siddhiAppName, userDefinedSiddhiApp);
        String defaultExecGroupName = siddhiAppName + "-" + UUID.randomUUID();
        boolean transportChannelCreationEnabled = isTransportChannelCreationEnabled(siddhiApp.getAnnotations());
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {

            parallel = getExecGroupParallel(executionElement);

            execGroupName = getExecGroupName(executionElement, siddhiAppName, defaultExecGroupName);
            siddhiQueryGroup = createSiddhiQueryGroup(execGroupName, parallel);

            if (executionElement instanceof Query) {
                //set query string
                queryContextStartIndex = ((Query) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Query) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetaInfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));


                siddhiQueryGroup.addInputStreams(getInputStreamHolderInfo((Query) executionElement,
                        siddhiQueryGroup, true));

                String outputStreamId = ((Query) executionElement).getOutputStream().getId();
                siddhiQueryGroup.addOutputStream(outputStreamId, getOutputStreamHolderInfo(outputStreamId,
                        parallel, execGroupName));

                siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(execGroupName, siddhiQueryGroup);

            } else if (executionElement instanceof Partition) {
                //set Partition string
                queryContextStartIndex = ((Partition) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Partition) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetaInfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

                //store partition details
                storePartitionInfo((Partition) executionElement, execGroupName);

                //for a partition iterate over containing queries to identify required inputStreams and OutputStreams
                for (Query query : ((Partition) executionElement).getQueryList()) {
                    if (AnnotationHelper.getAnnotation(SiddhiTopologyCreatorConstants.DISTRIBUTED_IDENTIFIER,
                            query.getAnnotations()) != null) {
                        throw new SiddhiAppValidationException("Unsupported:@dist annotation inside partition queries");
                    }

                    siddhiQueryGroup.addInputStreams(getInputStreamHolderInfo(query, siddhiQueryGroup, false));

                    String outputStreamId = query.getOutputStream().getId();
                    siddhiQueryGroup.addOutputStream(outputStreamId, getOutputStreamHolderInfo(outputStreamId,
                            parallel, execGroupName));
                }

                siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(execGroupName, siddhiQueryGroup);
            }
        }

        //prior to assigning publishing strategies checking if a user given source stream is used in multiple execGroups
        checkUserGivenSourceDistribution();
        assignPublishingStrategyOutputStream();
        return new SiddhiTopology(siddhiTopologyDataHolder.getSiddhiAppName(), new ArrayList<>
                (siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values()), transportChannelCreationEnabled);
    }


    /**
     * Get SiddhiAppName if given by the user unless a unique SiddhiAppName is returned
     *
     * @return String SiddhiAppName
     */
    private String getSiddhiAppName() {
        Element element =
                AnnotationHelper.getAnnotationElement(SiddhiTopologyCreatorConstants.SIDDHIAPP_NAME_IDENTIFIER,
                        null, siddhiApp.getAnnotations());
        if (element == null) {
            return SiddhiTopologyCreatorConstants.DEFAULT_SIDDHIAPP_NAME + "-" + UUID.randomUUID(); //defaultName
        } else {
            return element.getValue();
        }
    }

    /**
     * Get execGroup name if given by the user unless a unique execGroup is returned.
     */
    private String getExecGroupName(ExecutionElement executionElement, String siddhiAppName,
                                    String defaultExeGroupName) {
        Element element = AnnotationHelper.getAnnotationElement(SiddhiTopologyCreatorConstants.DISTRIBUTED_IDENTIFIER,
                SiddhiTopologyCreatorConstants.EXECGROUP_IDENTIFIER, executionElement.getAnnotations());
        if (element == null) {
            return defaultExeGroupName;
        } else {
            return siddhiAppName + "-" + element.getValue();
        }
    }

    /**
     * Get parallel value of the execGroup if given by the user unless default parallel.
     * {@link SiddhiTopologyCreatorConstants#DEFAULT_PARALLEL} value is returned
     */
    private int getExecGroupParallel(ExecutionElement executionElement) {
        Element element = AnnotationHelper.getAnnotationElement(SiddhiTopologyCreatorConstants.DISTRIBUTED_IDENTIFIER,
                SiddhiTopologyCreatorConstants.PARALLEL_IDENTIFIER, executionElement.getAnnotations());
        if (element == null) {
            return SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL;
        } else {
            return Integer.parseInt(element.getValue());
        }
    }

    /**
     * Get user preference on creating kafka topics from {@link org.wso2.carbon.sp.jobmanager.core.SiddhiAppCreator}.
     * If no value is defined 'true' is returned
     */
    private boolean isTransportChannelCreationEnabled(List<Annotation> annotationList) {
        Element element = AnnotationHelper.getAnnotationElement(
                SiddhiTopologyCreatorConstants.TRANSPORT_CHANNEL_CREATION_IDENTIFIER, null, annotationList);
        if (element == null) {
            return true;
        } else {
            return Boolean.valueOf(element.getValue());
        }
    }


    /**
     * If the corresponding {@link SiddhiQueryGroup} exists that object will be returned unless new object is created.
     */
    private SiddhiQueryGroup createSiddhiQueryGroup(String execGroupName, int parallel) {
        SiddhiQueryGroup siddhiQueryGroup;
        if (!siddhiTopologyDataHolder.getSiddhiQueryGroupMap().containsKey(execGroupName)) {
            siddhiQueryGroup = new SiddhiQueryGroup(execGroupName, parallel);
        } else {
            siddhiQueryGroup = siddhiTopologyDataHolder.getSiddhiQueryGroupMap().get(execGroupName);
            //An executionGroup should have a constant parallel number
            if (siddhiQueryGroup.getParallelism() != parallel) {
                throw new SiddhiAppValidationException(
                        "execGroup = " + execGroupName + " not assigned constant @dist(parallel)");
            }
        }
        return siddhiQueryGroup;
    }


    /**
     * If {@link Query,Partition} string contains {@link SiddhiTopologyCreatorConstants#DISTRIBUTED_IDENTIFIER},
     * meta info related to distributed deployment is removed.
     */
    private String removeMetaInfoQuery(ExecutionElement executionElement, String queryElement) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (Annotation annotation : executionElement.getAnnotations()) {
            if (annotation.getName().toLowerCase()
                    .equals(SiddhiTopologyCreatorConstants.DISTRIBUTED_IDENTIFIER)) {
                queryContextStartIndex = annotation.getQueryContextStartIndex();
                queryContextEndIndex = annotation.getQueryContextEndIndex();
                queryElement = queryElement.replace(
                        ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex,
                                siddhiTopologyDataHolder.getUserDefinedSiddhiApp()), "");
                break;
            }
        }
        return queryElement;
    }

    /**
     * Get Map of {@link InputStreamDataHolder} corresponding to a {@link Query}
     *
     * @return Map of  StreamID and {@link InputStreamDataHolder}
     */
    private Map<String, InputStreamDataHolder> getInputStreamHolderInfo(Query executionElement,
                                                                        SiddhiQueryGroup siddhiQueryGroup,
                                                                        boolean isQuery) {
        int parallel = siddhiQueryGroup.getParallelism();
        String execGroupName = siddhiQueryGroup.getName();
        StreamDataHolder streamDataHolder;
        InputStream inputStream = (executionElement).getInputStream();
        Map<String, InputStreamDataHolder> inputStreamDataHolderMap = new HashMap<>();

        if (parallel > SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL) {
            validateQueryType(inputStream, isQuery, execGroupName);
        }

        for (String inputStreamId : inputStream.getUniqueStreamIds()) {
            //not an inner Stream
            if (!inputStreamId.startsWith(SiddhiTopologyCreatorConstants.INNERSTREAM_IDENTIFIER)) {
                streamDataHolder = extractStreamHolderInfo(inputStreamId, parallel, execGroupName);
                TransportStrategy transportStrategy = findStreamSubscriptionStrategy(isQuery, inputStreamId, parallel,
                        execGroupName);

                InputStreamDataHolder inputStreamDataHolder = siddhiQueryGroup.getInputStreams().get(inputStreamId);
                //conflicting strategies for an stream in same in same execGroup
                if (inputStreamDataHolder != null && inputStreamDataHolder
                        .getSubscriptionStrategy().getStrategy() != transportStrategy) {
                    //if query given before partition --> Partitioned Stream--> RR and FG ->accept
                    if (!(inputStreamDataHolder.getSubscriptionStrategy().getStrategy()
                            .equals(TransportStrategy.ROUND_ROBIN) && transportStrategy
                            .equals(TransportStrategy.FIELD_GROUPING))) {
                        //if query given before partition --> Unpartitioned Stream --> RR and ALL ->don't
                        throw new SiddhiAppValidationException("Unsupported: " + inputStreamId + " in execGroup "
                                + execGroupName + " having conflicting strategies.");
                    }
                }

                String partitionKey = findPartitionKey(inputStreamId, isQuery);
                inputStreamDataHolder = new InputStreamDataHolder(inputStreamId,
                        streamDataHolder.getStreamDefinition(),
                        streamDataHolder.getEventHolderType(),
                        streamDataHolder.isUserGiven(),
                        new SubscriptionStrategyDataHolder(parallel, transportStrategy, partitionKey));
                inputStreamDataHolderMap.put(inputStreamId, inputStreamDataHolder);


            }
        }

        return inputStreamDataHolderMap;
    }

    /**
     * Get {@link OutputStreamDataHolder} for an OutputStream of a {@link Query}
     *
     * @return {@link OutputStreamDataHolder}
     */
    private OutputStreamDataHolder getOutputStreamHolderInfo(String outputStreamId, int parallel,
                                                             String execGroupName) {
        if (!outputStreamId.startsWith(SiddhiTopologyCreatorConstants.INNERSTREAM_IDENTIFIER)) {
            StreamDataHolder streamDataHolder = extractStreamHolderInfo(outputStreamId, parallel, execGroupName);
            return new OutputStreamDataHolder(outputStreamId, streamDataHolder.getStreamDefinition(),
                    streamDataHolder.getEventHolderType(), streamDataHolder.isUserGiven());
        } else {
            return null;
        }
    }

    /**
     * If streamID corresponds to a partitioned Stream, partition-key is returned unless null value is returned.
     */
    private String findPartitionKey(String streamID, boolean isQuery) {
        return siddhiTopologyDataHolder.getPartitionKeyMap().get(streamID);
    }

    /**
     * Extract primary information corresponding to {@link InputStreamDataHolder} and {@link OutputStreamDataHolder}.
     * Information is retrieved and assigned to {@link StreamDataHolder} .
     *
     * @return StreamDataHolder
     */
    private StreamDataHolder extractStreamHolderInfo(String streamId, int parallel, String groupName) {
        String streamDefinition;
        int[] queryContextEndIndex;
        int[] queryContextStartIndex;
        StreamDataHolder streamDataHolder = new StreamDataHolder(true);
        Map<String, StreamDefinition> streamDefinitionMap = siddhiApp.getStreamDefinitionMap();
        boolean isUserGivenTransport;
        if (streamDefinitionMap.containsKey(streamId)) {
            StreamDefinition definition = streamDefinitionMap.get(streamId);
            queryContextStartIndex = definition.getQueryContextStartIndex();
            queryContextEndIndex = definition.getQueryContextEndIndex();
            streamDefinition = ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex,
                    siddhiTopologyDataHolder.getUserDefinedSiddhiApp());
            isUserGivenTransport = isUserGivenTransport(streamDefinition);
            if (!isUserGivenTransport) {
                streamDefinition = "${" + streamId + "}" + streamDefinition;
            }
            streamDataHolder =
                    new StreamDataHolder(streamDefinition, EventHolder.STREAM, isUserGivenTransport);

        } else if (siddhiApp.getTableDefinitionMap().containsKey(streamId)) {
            AbstractDefinition tableDefinition = siddhiApp.getTableDefinitionMap().get(streamId);
            streamDataHolder.setEventHolderType(EventHolder.INMEMORYTABLE);

            for (Annotation annotation : tableDefinition.getAnnotations()) {
                if (annotation.getName().toLowerCase().equals(
                        SiddhiTopologyCreatorConstants.PERSISTENCETABLE_IDENTIFIER)) {
                    streamDataHolder.setEventHolderType(EventHolder.TABLE);
                    break;
                }
            }
            //In-Memory tables will not be supported when parallel >1
            if (parallel != SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL &&
                    streamDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE)) {
                throw new SiddhiAppValidationException("Unsupported: "
                        + groupName + " with In-Memory Table  having parallel >1 ");
            }

            queryContextStartIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextEndIndex();
            streamDataHolder.setStreamDefinition(ExceptionUtil.getContext(queryContextStartIndex,
                    queryContextEndIndex, siddhiTopologyDataHolder.getUserDefinedSiddhiApp()));

            if (streamDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE) && siddhiTopologyDataHolder
                    .getInMemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInMemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:Event Table "
                            + streamId + " In-Memory Table referenced from more than one execGroup: execGroup "
                            + groupName + " && " + siddhiTopologyDataHolder.getInMemoryMap().get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInMemoryMap().put(streamId, groupName);
            }

        } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
            if (parallel != SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL) {
                throw new SiddhiAppValidationException("Unsupported: "
                        + groupName + " with (Defined) Window " + " having parallel >1");
            }

            queryContextStartIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextEndIndex();

            streamDataHolder.setStreamDefinition(ExceptionUtil.getContext(queryContextStartIndex,
                    queryContextEndIndex, siddhiTopologyDataHolder.getUserDefinedSiddhiApp()));
            streamDataHolder.setEventHolderType(EventHolder.WINDOW);

            if (siddhiTopologyDataHolder.getInMemoryMap().containsKey(streamId)) {
                if (!siddhiTopologyDataHolder.getInMemoryMap().get(streamId).equals(groupName)) {
                    throw new SiddhiAppValidationException("Unsupported:(Defined) Window "
                            + streamId + " In-Memory window referenced from more than one execGroup: execGroup "
                            + groupName + " && " + siddhiTopologyDataHolder.getInMemoryMap().get(streamId));
                }
            } else {
                siddhiTopologyDataHolder.getInMemoryMap().put(streamId, groupName);
            }

            //if stream definition is an inferred definition
        } else if (streamDataHolder.getStreamDefinition() == null) {
            if (siddhiAppRuntime.getStreamDefinitionMap().containsKey(streamId)) {
                streamDataHolder = new StreamDataHolder(
                        "${" + streamId + "}"
                                + siddhiAppRuntime.getStreamDefinitionMap().get(streamId).toString(),
                        EventHolder.STREAM, false);
            }
        }
        return streamDataHolder;
    }


    /**
     * Transport strategy corresponding to an InputStream is calculated.
     *
     * @return {@link TransportStrategy}
     */
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

    /**
     * Checks whether a given Stream definition contains Sink or Source configurations.
     */
    private boolean isUserGivenTransport(String streamDefinition) {
        return streamDefinition.toLowerCase().contains(
                SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER) || streamDefinition.toLowerCase().contains
                (SiddhiTopologyCreatorConstants.SINK_IDENTIFIER);
    }

    /**
     * Validates whether a given query is acceptable for distributed deployment.
     * eg:Join ,Sequence , Pattern , Window queries will not be allowed for parallel distributed deployment unless
     * the query consists of Partitioned or a Inner Stream.
     */
    private void validateQueryType(InputStream inputStream, boolean isQuery, String execGroup) {
        boolean partitionStreamExist = false;

        for (String streamId : inputStream.getUniqueStreamIds()) {
            //join,sequence ,pattern,window happens (with/as) partitioned or inner streams
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
                        + "Join queries used with parallel greater than 1 outside partitioned stream");
            } else if (inputStream instanceof StateInputStream) {
                String type = ((StateInputStream) inputStream).getStateType().name();
                throw new SiddhiAppValidationException(execGroup + type + " queries used with parallel greater than" +
                        " 1 outside partitioned stream");

            } else if (inputStream instanceof SingleInputStream) {
                List<StreamHandler> streamHandlers = ((SingleInputStream) inputStream).getStreamHandlers();
                for (StreamHandler streamHandler : streamHandlers) {
                    if (streamHandler instanceof Window) {
                        throw new SiddhiAppValidationException(execGroup
                                + " Window queries used with parallel greater than 1 outside partitioned stream");
                    }
                }
            }
        }
    }

    /**
     * Checks if the Distributed SiddhiApp contains InputStreams with @Source Configurations which are used by multiple
     * execGroups.Such inputStreams will be added to a separate execGroup as a separate SiddhiApp including a completed
     * {@link SiddhiTopologyCreatorConstants#DEFAULT_PASSTROUGH_QUERY_TEMPLATE} passthrough Queries.
     * <p>
     * Note: The passthrough query will create an internal Stream which will enrich awaiting excGroups.
     */

    private void checkUserGivenSourceDistribution() {
        int i = 0;
        boolean createPassthrough;          //create passthrough query for each user given source stream
        boolean passthroughQueriesAvailable = false;           //move passthrough query to front of SiddhiQueryGroupList

        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());
        List<SiddhiQueryGroup> passthroughQueries = new ArrayList<>();

        for (SiddhiQueryGroup siddhiQueryGroup1 : siddhiQueryGroupsList) {
            for (Map.Entry<String, InputStreamDataHolder> entry : siddhiQueryGroup1.getInputStreams().entrySet()) {
                String streamId = entry.getKey();
                InputStreamDataHolder inputStreamDataHolder = entry.getValue();
                if (inputStreamDataHolder.getEventHolderType().equals(EventHolder.STREAM) && inputStreamDataHolder
                        .isUserGiven()) {
                    createPassthrough = true;
                    for (SiddhiQueryGroup siddhiQueryGroup2 : siddhiQueryGroupsList.subList(i + 1,
                            siddhiQueryGroupsList.size())) {
                        if (siddhiQueryGroup2.getInputStreams().containsKey(streamId)) {
                            String runtimeDefinition = removeMetaInfoStream(streamId,
                                    inputStreamDataHolder.getStreamDefinition(), SiddhiTopologyCreatorConstants
                                            .SOURCE_IDENTIFIER);
                            passthroughQueriesAvailable = true;
                            //A passthrough query will be created only once for an inputStream satisfying above
                            // requirements.
                            if (createPassthrough) {
                                passthroughQueries.addAll(generatePassthroughQueryList(
                                        streamId, inputStreamDataHolder, runtimeDefinition));
                                inputStreamDataHolder.setStreamDefinition(runtimeDefinition);
                                inputStreamDataHolder.setUserGiven(false);
                                createPassthrough = false;

                            }
                            InputStreamDataHolder holder1 = siddhiQueryGroup1.getInputStreams().get(streamId);
                            String consumingStream = "${" + streamId + "} " + removeMetaInfoStream(streamId,
                                    holder1.getStreamDefinition(), SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER);
                            holder1.setStreamDefinition(consumingStream);
                            holder1.setUserGiven(false);

                            InputStreamDataHolder holder2 = siddhiQueryGroup2.getInputStreams().get(streamId);
                            consumingStream = "${" + streamId + "} " + removeMetaInfoStream(streamId,
                                    holder2.getStreamDefinition(), SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER);
                            holder2.setStreamDefinition(consumingStream);
                            holder2.setUserGiven(false);
                        }
                    }
                }
            }
            i++;
        }

        //Created SiddhiQueryGroup object will be moved as the first element of the linkedHashMap of already created
        // SiddhiApps only if at least one Passthrough query is being created.
        if (passthroughQueriesAvailable) {
            addFirst(passthroughQueries);
        }
    }

    private List<SiddhiQueryGroup> generatePassthroughQueryList(String streamId,
                                                                InputStreamDataHolder inputStreamDataHolder,
                                                                String runtimeDefinition) {
        List<SiddhiQueryGroup> passthroughQueryGroupList = new ArrayList<>();
        StreamDefinition definition = siddhiApp.getStreamDefinitionMap().get(streamId);
        for (Annotation annotation : definition.getAnnotations()) {
            int parallelism = getSourceParallelism(annotation);
            SiddhiQueryGroup passthroughQueryGroup = createPassthroughQueryGroup(inputStreamDataHolder,
                                                                                 runtimeDefinition, parallelism);
            passthroughQueryGroup.setReceiverQueryGroup(true);
            passthroughQueryGroupList.add(passthroughQueryGroup);
        }
        return passthroughQueryGroupList;
    }


    /**
     * If the Stream definition string contains {@link SiddhiTopologyCreatorConstants#SINK_IDENTIFIER} or
     * {@link SiddhiTopologyCreatorConstants#SOURCE_IDENTIFIER} ,meta info related to Sink/Source configuration is
     * removed.
     *
     * @return Stream definition String after removing Sink/Source configuration
     */
    private String removeMetaInfoStream(String streamId, String streamDefinition, String identifier) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (Annotation annotation : siddhiApp.getStreamDefinitionMap().get(streamId).getAnnotations()) {
            if (annotation.getName().toLowerCase().equals(identifier.replace("@", ""))) {
                queryContextStartIndex = annotation.getQueryContextStartIndex();
                queryContextEndIndex = annotation.getQueryContextEndIndex();
                streamDefinition = streamDefinition.replace(
                        ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex,
                                siddhiTopologyDataHolder.getUserDefinedSiddhiApp()), "");
            }
        }
        return streamDefinition;
    }

    /**
     * PassthroughSiddhiQueryGroups will be moved as the first elements of the existing linkedHashMap of
     * SiddhiQueryGroups.
     * @param passthroughSiddhiQueryGroupList List of Passthrough queries
     */
    private void addFirst(List<SiddhiQueryGroup> passthroughSiddhiQueryGroupList) {
        Map<String, SiddhiQueryGroup> output = new LinkedHashMap();
        for (SiddhiQueryGroup passthroughSiddhiQueryGroup : passthroughSiddhiQueryGroupList) {
            output.put(passthroughSiddhiQueryGroup.getName(), passthroughSiddhiQueryGroup);
        }
        output.putAll(siddhiTopologyDataHolder.getSiddhiQueryGroupMap());
        siddhiTopologyDataHolder.getSiddhiQueryGroupMap().clear();
        siddhiTopologyDataHolder.getSiddhiQueryGroupMap().putAll(output);
    }

    /**
     * Passthrough query is created using {@link SiddhiTopologyCreatorConstants#DEFAULT_PASSTROUGH_QUERY_TEMPLATE}
     * and required {@link InputStreamDataHolder} and {@link OutputStreamDataHolder} are assigned to the
     * SiddhiQueryGroup.
     */
    private SiddhiQueryGroup createPassthroughQueryGroup(InputStreamDataHolder inputStreamDataHolder,
                                                         String runtimeDefinition, int parallelism) {
        String passthroughExecGroupName = siddhiTopologyDataHolder.getSiddhiAppName() + "-" +
                SiddhiTopologyCreatorConstants.PASSTHROUGH + "-" + new Random().nextInt(99999);
        SiddhiQueryGroup siddhiQueryGroup = new SiddhiQueryGroup(passthroughExecGroupName,
                                                                 parallelism);
        String streamId = inputStreamDataHolder.getStreamName();
        Map<String, String> valuesMap = new HashMap();
        String inputStreamID = SiddhiTopologyCreatorConstants.PASSTHROUGH + inputStreamDataHolder.getStreamName();
        valuesMap.put(SiddhiTopologyCreatorConstants.INPUTSTREAMID, inputStreamID);
        valuesMap.put(SiddhiTopologyCreatorConstants.OUTPUTSTREAMID, streamId);
        StrSubstitutor substitutor = new StrSubstitutor(valuesMap);
        String passThroughQuery = substitutor.replace(SiddhiTopologyCreatorConstants.DEFAULT_PASSTROUGH_QUERY_TEMPLATE);
        siddhiQueryGroup.addQuery(passThroughQuery);
        String inputStreamDefinition = inputStreamDataHolder.getStreamDefinition().replace(streamId, inputStreamID);
        String outputStreamDefinition = "${" + streamId + "} " + runtimeDefinition;
        siddhiQueryGroup.getInputStreams()
                .put(inputStreamID, new InputStreamDataHolder(inputStreamID,
                                                              inputStreamDefinition, EventHolder.STREAM, true,
                                                              new SubscriptionStrategyDataHolder(
                                                                      SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL,
                                                                      TransportStrategy.ALL, null)));
        siddhiQueryGroup.getOutputStreams().put(streamId, new OutputStreamDataHolder(streamId, outputStreamDefinition,
                                                                                     EventHolder.STREAM, false));
        return siddhiQueryGroup;
    }

    /**
     * Publishing strategies for an OutputStream is assigned if the corresponding outputStream is being used as an
     * InputStream in a separate execGroup.
     */
    private void assignPublishingStrategyOutputStream() {
        int i = 0;
        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());

        for (SiddhiQueryGroup siddhiQueryGroup1 : siddhiQueryGroupsList) {
            for (Map.Entry<String, OutputStreamDataHolder> entry : siddhiQueryGroup1.getOutputStreams().entrySet()) {
                OutputStreamDataHolder outputStreamDataHolder = entry.getValue();
                String streamId = entry.getKey();

                if (outputStreamDataHolder.getEventHolderType().equals(EventHolder.STREAM)) {
                    Map<String, List<SubscriptionStrategyDataHolder>> fieldGroupingSubscriptions = new HashMap<>();
                    for (SiddhiQueryGroup siddhiQueryGroup2 : siddhiQueryGroupsList.subList(i + 1,
                            siddhiQueryGroupsList.size())) {
                        if (siddhiQueryGroup2.getInputStreams().containsKey(streamId)) {
                            InputStreamDataHolder inputStreamDataHolder = siddhiQueryGroup2.getInputStreams()
                                    .get(streamId);

                            //Checks if the Distributed SiddhiApp contains OutputStreams with Sink Configurations
                            //which are used as an inputStream in a different execGroup
                            //If above then a  outputStream Sink configuration is concatenated with placeholder
                            //corresponding to the outputStreamId
                            //InputStream definition will be replaced.
                            if (outputStreamDataHolder.isUserGiven()) {
                                String runtimeStreamDefinition = removeMetaInfoStream(streamId,
                                        inputStreamDataHolder.getStreamDefinition(),
                                        SiddhiTopologyCreatorConstants.SINK_IDENTIFIER);
                                String outputStreamDefinition = outputStreamDataHolder.
                                        getStreamDefinition().replace(runtimeStreamDefinition, "\n"
                                        + "${" + streamId + "} ") + runtimeStreamDefinition;
                                outputStreamDataHolder.setStreamDefinition(outputStreamDefinition);
                                inputStreamDataHolder
                                        .setStreamDefinition("${" + streamId + "} " + runtimeStreamDefinition);
                                inputStreamDataHolder.setUserGiven(false);
                            }

                            SubscriptionStrategyDataHolder subscriptionStrategy = inputStreamDataHolder.
                                    getSubscriptionStrategy();
                            if (subscriptionStrategy.getStrategy().equals(TransportStrategy.FIELD_GROUPING)) {
                                String partitionKey = subscriptionStrategy.getPartitionKey();
                                if (fieldGroupingSubscriptions.containsKey(partitionKey)) {
                                    fieldGroupingSubscriptions.get(partitionKey).add(
                                            subscriptionStrategy);
                                } else {
                                    List<SubscriptionStrategyDataHolder> strategyList = new ArrayList<>();
                                    strategyList.add(subscriptionStrategy);
                                    fieldGroupingSubscriptions.put(partitionKey, strategyList);
                                }

                            } else {
                                outputStreamDataHolder.addPublishingStrategy(
                                        new PublishingStrategyDataHolder(subscriptionStrategy.getStrategy(),
                                                siddhiQueryGroup2.getParallelism()));
                            }

                        }
                    }
                    for (Entry<String, List<SubscriptionStrategyDataHolder>> subscriptionParentEntry :
                            fieldGroupingSubscriptions.entrySet()) {
                        String partitionKey = subscriptionParentEntry.getKey();
                        List<SubscriptionStrategyDataHolder> strategyList = subscriptionParentEntry.getValue();
                        strategyList.sort(new StrategyParallelismComparator().reversed());
                        int parallelism = strategyList.get(0).getOfferedParallelism();
                        for (SubscriptionStrategyDataHolder holder : strategyList) {
                            holder.setOfferedParallelism(parallelism);
                        }
                        outputStreamDataHolder.addPublishingStrategy(
                                new PublishingStrategyDataHolder(
                                        TransportStrategy.FIELD_GROUPING, partitionKey, parallelism));
                    }
                }

            }
            i++;
        }
    }

    /**
     * Details required while processing Partitions are stored.
     */
    private void storePartitionInfo(Partition partition, String execGroupName) {
        List<String> partitionGroupList; //contains all the execGroups containing partitioned streamId
        String partitionKey;

        //assign partitionGroupMap
        for (Map.Entry<String, PartitionType> partitionTypeEntry : partition.getPartitionTypeMap().entrySet()) {

            String streamID = partitionTypeEntry.getKey();
            if (siddhiTopologyDataHolder.getPartitionGroupMap().containsKey(streamID)) {
                partitionGroupList = siddhiTopologyDataHolder.getPartitionGroupMap().get(streamID);
            } else {
                partitionGroupList = new ArrayList<>();
            }

            partitionGroupList.add(execGroupName);
            siddhiTopologyDataHolder.getPartitionGroupMap().put(streamID, partitionGroupList);

            if (partitionTypeEntry.getValue() instanceof ValuePartitionType) {
                partitionKey = ((Variable) ((ValuePartitionType) partitionTypeEntry.getValue()).getExpression())
                        .getAttributeName();

                //More than one partition corresponding to same partition-key of a stream can not reside in the same
                // execGroup.
                if (siddhiTopologyDataHolder.getPartitionKeyGroupMap().get(streamID + partitionKey) != null &&
                        siddhiTopologyDataHolder.getPartitionKeyGroupMap().get(streamID + partitionKey)
                                .equals(execGroupName)) {
                    throw new SiddhiAppValidationException("Unsupported in distributed setup :More than 1 partition "
                            + "residing on the same execGroup " + execGroupName + " for " + streamID + " "
                            + partitionKey);
                }
                siddhiTopologyDataHolder.getPartitionKeyGroupMap().put(streamID + partitionKey, execGroupName);
                siddhiTopologyDataHolder.getPartitionKeyMap().put(streamID, partitionKey);

            } else {
                //Not yet supported
                throw new SiddhiAppValidationException("Unsupported: "
                        + execGroupName + " Range PartitionType not Supported in Distributed SetUp");
            }
        }
    }

    /**
     * Provide the parallelism count for pass-through query. In case of multiple user given values,
     * lowest one will be selected. Default parallelism value would be 1.
     *
     * @param annotation Source annotation
     * @return Parallelism count for pass-through query
     */
    private int getSourceParallelism(Annotation annotation) {
        Set<Integer> parallelismSet = new HashSet<>();
        List<Annotation> distAnnotations = annotation.getAnnotations(
                SiddhiTopologyCreatorConstants.DISTRIBUTED_IDENTIFIER);
        if (distAnnotations.size() > 0) {
            for (Annotation distAnnotation : distAnnotations) {
                if (distAnnotation.getElement(SiddhiTopologyCreatorConstants.PARALLEL_IDENTIFIER) != null) {
                    parallelismSet.add(Integer.valueOf(distAnnotation.getElement(
                            SiddhiTopologyCreatorConstants.PARALLEL_IDENTIFIER)));
                } else {
                    return SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL;
                }
            }
        } else {
            return SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL;
        }
        return parallelismSet.stream().min(Comparator.comparing(Integer::intValue)).get();

    }
}
