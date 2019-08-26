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

package org.wso2.siddhi.parser.core.topology;

import io.siddhi.core.SiddhiAppRuntime;
import io.siddhi.core.partition.PartitionRuntime;
import io.siddhi.core.query.QueryRuntime;
import io.siddhi.core.stream.input.source.Source;
import io.siddhi.core.stream.output.sink.Sink;
import io.siddhi.core.table.Table;
import io.siddhi.core.window.Window;
import org.wso2.siddhi.parser.SiddhiParserDataHolder;
import org.wso2.siddhi.parser.core.SiddhiTopologyCreator;
import org.wso2.siddhi.parser.core.util.EventHolder;
import org.wso2.siddhi.parser.core.util.SiddhiTopologyCreatorConstants;
import org.wso2.siddhi.parser.core.util.TransportStrategy;
import io.siddhi.query.api.SiddhiApp;
import io.siddhi.query.api.annotation.Annotation;
import io.siddhi.query.api.annotation.Element;
import io.siddhi.query.api.definition.AbstractDefinition;
import io.siddhi.query.api.definition.StreamDefinition;
import io.siddhi.query.api.exception.SiddhiAppValidationException;
import io.siddhi.query.api.execution.ExecutionElement;
import io.siddhi.query.api.execution.partition.Partition;
import io.siddhi.query.api.execution.partition.PartitionType;
import io.siddhi.query.api.execution.partition.ValuePartitionType;
import io.siddhi.query.api.execution.query.Query;
import io.siddhi.query.api.execution.query.input.stream.InputStream;
import io.siddhi.query.api.expression.Variable;
import io.siddhi.query.api.util.AnnotationHelper;
import io.siddhi.query.api.util.ExceptionUtil;
import io.siddhi.query.compiler.SiddhiCompiler;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;

/**
 * Consumes a Siddhi App and produce a {@link SiddhiTopology} based on distributed annotations.
 */

public class SiddhiTopologyCreatorImpl implements SiddhiTopologyCreator {

    private static final Logger log = Logger.getLogger(SiddhiTopologyCreatorImpl.class);
    private static final String DEFAULT_MESSAGING_SYSTEM = "nats";
    private SiddhiTopologyDataHolder siddhiTopologyDataHolder;
    private SiddhiApp siddhiApp;
    private String siddhiAppName;
    private SiddhiAppRuntime siddhiAppRuntime;
    private String userDefinedSiddhiApp;
    private boolean transportChannelCreationEnabled = true;
    private boolean isUserGiveSourceStateful = false;

    @Override
    public SiddhiTopology createTopology(String userDefinedSiddhiApp) {
        this.userDefinedSiddhiApp = userDefinedSiddhiApp;
        this.siddhiApp = SiddhiCompiler.parse(userDefinedSiddhiApp);
        this.siddhiAppRuntime = SiddhiParserDataHolder.getSiddhiManager().createSiddhiAppRuntime(userDefinedSiddhiApp);
        this.siddhiAppName = getSiddhiAppName();
        this.siddhiTopologyDataHolder = new SiddhiTopologyDataHolder(siddhiAppName, userDefinedSiddhiApp);

        SiddhiQueryGroup siddhiQueryGroup;
        String execGroupName;
        String defaultExecGroupName = siddhiAppName + "-" + UUID.randomUUID();
        //Parallelism is set to default parallelism, since distributed deployment is not supported at the moment
        int parallelism = SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL;

        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            //groupName is set to default, since all elements go under a single group
            execGroupName = defaultExecGroupName;
            siddhiQueryGroup = createSiddhiQueryGroup(execGroupName, parallelism);
            addExecutionElement(executionElement, siddhiQueryGroup, execGroupName);
        }

        checkUserGivenSourceDistribution();
        assignPublishingStrategyOutputStream();
        cleanInnerGroupStreams(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());
        if (log.isDebugEnabled()) {
            log.debug("Topology was created with " + siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values().size
                    () + " groups. Following are the partial Siddhi apps.");
            for (SiddhiQueryGroup debugSiddhiQueryGroup : siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values()) {
                log.debug(debugSiddhiQueryGroup.getSiddhiApp());
            }
        }
        return new SiddhiTopology(siddhiTopologyDataHolder.getSiddhiAppName(), new ArrayList<>
                (siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values()),
                transportChannelCreationEnabled, isStatefulApp(), isUserGiveSourceStateful);
    }

    private boolean isStatefulApp() {
        for (List<Source> sourceList : siddhiAppRuntime.getSources()) {
            for (Source source : sourceList) {
                if (source.isStateful()) {
                    if (source.getType().equalsIgnoreCase(DEFAULT_MESSAGING_SYSTEM)) {
                        isUserGiveSourceStateful = true;
                    }
                    return true;
                }
            }
        }
        for (List<Sink> sinkList : siddhiAppRuntime.getSinks()) {
            for (Sink sink : sinkList) {
                if (sink.isStateful()) {
                    return true;
                }
            }
        }
        for (QueryRuntime queryRuntime : siddhiAppRuntime.getQueries()) {
            if (queryRuntime.isStateful()) {
                return true;
            }
        }
        for (PartitionRuntime partitionRuntime : siddhiAppRuntime.getPartitions()) {
            for (QueryRuntime queryRuntime : partitionRuntime.getQueries()) {
                if (queryRuntime.isStateful()) {
                    return true;
                }
            }
        }
        for (Table table : siddhiAppRuntime.getTables()) {
            if (table.isStateful()) {
                return true;
            }
        }
        for (Window window : siddhiAppRuntime.getWindows()) {
            if (window.isStateful()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clean input and output streams of the group by removing streams that are only used within that group.
     *
     * @param siddhiQueryGroups Collection of Siddhi Query Groups
     */
    private void cleanInnerGroupStreams(Collection<SiddhiQueryGroup> siddhiQueryGroups) {
        for (SiddhiQueryGroup siddhiQueryGroup : siddhiQueryGroups) {
            siddhiQueryGroup.getInputStreams()
                    .entrySet().removeIf(stringInputStreamDataHolderEntry -> stringInputStreamDataHolderEntry.getValue()
                    .isInnerGroupStream());
            siddhiQueryGroup.getOutputStreams()
                    .entrySet().removeIf(
                    stringOutputStreamDataHolderEntry -> stringOutputStreamDataHolderEntry.getValue()
                            .isInnerGroupStream());
        }
    }

    /**
     * Get SiddhiAppName if given by the user unless a unique SiddhiAppName is returned.
     *
     * @return String SiddhiAppName
     */
    private String getSiddhiAppName() {
        Element element =
                AnnotationHelper.getAnnotationElement(SiddhiTopologyCreatorConstants.SIDDHIAPP_NAME_IDENTIFIER,
                        null, siddhiApp.getAnnotations());
        if (element == null) {
            return SiddhiTopologyCreatorConstants.DEFAULT_SIDDHIAPP_NAME + "-" + UUID.randomUUID();
        } else {
            return element.getValue();
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
     * Get Map of {@link InputStreamDataHolder} corresponding to a {@link Query}.
     *
     * @return Map of  StreamID and {@link InputStreamDataHolder}
     */
    private Map<String, InputStreamDataHolder> getInputStreamHolderInfo(Query executionElement,
                                                                        SiddhiQueryGroup siddhiQueryGroup,
                                                                        boolean isQuery) {
        Map<String, InputStreamDataHolder> inputStreamDataHolderMap = new HashMap<>();
        int parallel = siddhiQueryGroup.getParallelism();
        String execGroupName = siddhiQueryGroup.getName();
        StreamDataHolder streamDataHolder;
        InputStream inputStream = (executionElement).getInputStream();
        for (String inputStreamId : inputStream.getUniqueStreamIds()) {
            //not an inner Stream
            if (!inputStreamId.startsWith(SiddhiTopologyCreatorConstants.INNERSTREAM_IDENTIFIER)) {
                streamDataHolder = extractStreamHolderInfo(inputStreamId, execGroupName);
                // Handle transportStrategy when parallelism > 1
                TransportStrategy transportStrategy = TransportStrategy.ALL;
                InputStreamDataHolder inputStreamDataHolder = siddhiQueryGroup.getInputStreams().get(inputStreamId);
                String partitionKey = siddhiTopologyDataHolder.getPartitionKeyMap().get(inputStreamId);
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
     * Get {@link OutputStreamDataHolder} for an OutputStream of a {@link Query}.
     *
     * @return {@link OutputStreamDataHolder}
     */
    private OutputStreamDataHolder getOutputStreamHolderInfo(String outputStreamId, int parallel,
                                                             String execGroupName) {
        if (!outputStreamId.startsWith(SiddhiTopologyCreatorConstants.INNERSTREAM_IDENTIFIER)) {
            StreamDataHolder streamDataHolder = extractStreamHolderInfo(outputStreamId, execGroupName);
            return new OutputStreamDataHolder(outputStreamId, streamDataHolder.getStreamDefinition(),
                    streamDataHolder.getEventHolderType(), streamDataHolder.isUserGiven());
        } else {
            return null;
        }
    }

    /**
     * Extract primary information corresponding to {@link InputStreamDataHolder} and {@link OutputStreamDataHolder}.
     * Information is retrieved and assigned to {@link StreamDataHolder} .
     *
     * @return StreamDataHolder
     */
    private StreamDataHolder extractStreamHolderInfo(String streamId, String groupName) {
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
            if (!isUserGivenTransport && !siddhiApp.getTriggerDefinitionMap().containsKey(streamId)) {
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
                    siddhiTopologyDataHolder.setStatefulApp(true);
                    break;
                }
            }
            queryContextStartIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getTableDefinitionMap().get(streamId).getQueryContextEndIndex();
            streamDataHolder.setStreamDefinition(ExceptionUtil.getContext(queryContextStartIndex,
                    queryContextEndIndex, siddhiTopologyDataHolder.getUserDefinedSiddhiApp()));
            if (!streamDataHolder.getEventHolderType().equals(EventHolder.INMEMORYTABLE) && !siddhiTopologyDataHolder
                    .getInMemoryMap().containsKey(streamId)) {
                siddhiTopologyDataHolder.getInMemoryMap().put(streamId, groupName);
            }
        } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
            siddhiTopologyDataHolder.setStatefulApp(true);
            queryContextStartIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getWindowDefinitionMap().get(streamId).getQueryContextEndIndex();
            streamDataHolder.setStreamDefinition(ExceptionUtil.getContext(queryContextStartIndex,
                    queryContextEndIndex, siddhiTopologyDataHolder.getUserDefinedSiddhiApp()));
            streamDataHolder.setEventHolderType(EventHolder.WINDOW);
            if (!siddhiTopologyDataHolder.getInMemoryMap().containsKey(streamId)) {
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
     * Checks whether a given Stream definition contains Sink or Source configurations.
     */
    private boolean isUserGivenTransport(String streamDefinition) {
        return streamDefinition.toLowerCase().contains(
                SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER) || streamDefinition.toLowerCase().contains
                (SiddhiTopologyCreatorConstants.SINK_IDENTIFIER);
    }

    private void checkUserGivenSourceDistribution() {
        boolean passthroughQueriesAvailable = false;
        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());
        List<SiddhiQueryGroup> passthroughQueries = new ArrayList<>();

        for (SiddhiQueryGroup siddhiQueryGroup : siddhiQueryGroupsList) {
            for (Entry<String, InputStreamDataHolder> entry : siddhiQueryGroup.getInputStreams().entrySet()) {
                String streamId = entry.getKey();
                InputStreamDataHolder inputStreamDataHolder = entry.getValue();
                if (inputStreamDataHolder.getEventHolderType() != null && inputStreamDataHolder
                        .getEventHolderType().equals(EventHolder.STREAM) &&
                        inputStreamDataHolder.isUserGivenSource()) {
                    String runtimeDefinition = removeMetaInfoStream(streamId,
                            inputStreamDataHolder.getStreamDefinition(), SiddhiTopologyCreatorConstants
                                    .SOURCE_IDENTIFIER);
                    StreamDefinition streamDefinition = siddhiAppRuntime.getStreamDefinitionMap()
                            .get(inputStreamDataHolder.getStreamName());
                    int nonMessagingSources = 0;
                    for (Annotation annotation : streamDefinition.getAnnotations()) {
                        if (annotation.getName().equalsIgnoreCase(SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER
                                .replace("@", ""))) {
                            if (annotation.getElement("type").equalsIgnoreCase(DEFAULT_MESSAGING_SYSTEM)) {
                                siddhiQueryGroup.setMessagingSourceAvailable(true);
                            } else {
                                nonMessagingSources++;
                            }
                        }
                    }
                    if ((!siddhiQueryGroup.isMessagingSourceAvailable() || nonMessagingSources > 0)
                            && isStatefulApp()) {
                        passthroughQueriesAvailable = true;
                        passthroughQueries.addAll(generatePassthroughQueryList(
                                inputStreamDataHolder, runtimeDefinition));
                        inputStreamDataHolder.setStreamDefinition(runtimeDefinition);
                        inputStreamDataHolder.setUserGiven(false);
                        InputStreamDataHolder holder = siddhiQueryGroup.getInputStreams().get(streamId);
                        String consumingStream = "${" + streamId + "} " + removeMetaInfoStream(streamId,
                                holder.getStreamDefinition(), SiddhiTopologyCreatorConstants.SOURCE_IDENTIFIER);
                        holder.setStreamDefinition(consumingStream);
                        holder.setUserGiven(false);
                    }
                }
            }
        }
        //Created SiddhiQueryGroup object will be moved as the first element of the linkedHashMap of already created
        // SiddhiApps only if at least one Passthrough parse is being created.
        if (passthroughQueriesAvailable) {
            addFirst(passthroughQueries);
        }
    }

    private List<SiddhiQueryGroup> generatePassthroughQueryList(InputStreamDataHolder inputStreamDataHolder,
                                                                String runtimeDefinition) {
        List<SiddhiQueryGroup> passthroughQueryGroupList = new ArrayList<>();
        SiddhiQueryGroup passthroughQueryGroup = createPassthroughQueryGroup(inputStreamDataHolder,
                runtimeDefinition, SiddhiTopologyCreatorConstants.DEFAULT_PARALLEL);
        passthroughQueryGroup.setReceiverQueryGroup(true);
        passthroughQueryGroupList.add(passthroughQueryGroup);
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
     *
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
     * Passthrough parse is created using {@link SiddhiTopologyCreatorConstants#DEFAULT_PASSTROUGH_QUERY_TEMPLATE}
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
     * Adds the given execution element to the SiddhiQueryGroup.
     *
     * @param executionElement The execution element to be added to the SiddhiQueryGroup.
     * @param siddhiQueryGroup SiddhiQueryGroup which the execution element to be added.
     * @param queryGroupName   Name of the above SiddhiQueryGroup.
     */
    private void addExecutionElement(ExecutionElement executionElement, SiddhiQueryGroup siddhiQueryGroup,
                                     String queryGroupName) {
        int[] queryContextEndIndex;
        int[] queryContextStartIndex;
        int parallelism = siddhiQueryGroup.getParallelism();
        if (executionElement instanceof Query) {
            //set parse string
            queryContextStartIndex = ((Query) executionElement).getQueryContextStartIndex();
            queryContextEndIndex = ((Query) executionElement).getQueryContextEndIndex();
            siddhiQueryGroup.addQuery(removeMetaInfoQuery(executionElement, ExceptionUtil
                    .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));
            siddhiQueryGroup.addInputStreams(getInputStreamHolderInfo((Query) executionElement,
                    siddhiQueryGroup, true));
            String outputStreamId = ((Query) executionElement).getOutputStream().getId();
            siddhiQueryGroup.addOutputStream(outputStreamId, getOutputStreamHolderInfo(outputStreamId, parallelism,
                    queryGroupName));
        } else if (executionElement instanceof Partition) {
            queryContextStartIndex = ((Partition) executionElement)
                    .getQueryContextStartIndex();
            queryContextEndIndex = ((Partition) executionElement).getQueryContextEndIndex();
            siddhiQueryGroup.addQuery(removeMetaInfoQuery(executionElement, ExceptionUtil
                    .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

            //store partition details
            storePartitionInfo((Partition) executionElement, queryGroupName);
            //for a partition iterate over containing queries to identify required inputStreams and OutputStreams
            for (Query query : ((Partition) executionElement).getQueryList()) {
                siddhiQueryGroup.addInputStreams(getInputStreamHolderInfo(query, siddhiQueryGroup, false));
                String outputStreamId = query.getOutputStream().getId();
                siddhiQueryGroup.addOutputStream(outputStreamId, getOutputStreamHolderInfo(outputStreamId,
                        parallelism, queryGroupName));
            }

        }
        siddhiTopologyDataHolder.getSiddhiQueryGroupMap().put(queryGroupName, siddhiQueryGroup);
    }


    /**
     * Publishing strategies for an OutputStream is assigned if the corresponding outputStream is being used as an
     * InputStream in a separate group.
     */
    private void assignPublishingStrategyOutputStream() {
        int i = 0;
        List<SiddhiQueryGroup> siddhiQueryGroupsList =
                new ArrayList<>(siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values());

        for (SiddhiQueryGroup siddhiQueryGroup1 : siddhiQueryGroupsList) {
            for (Entry<String, OutputStreamDataHolder> entry : siddhiQueryGroup1.getOutputStreams().entrySet()) {
                OutputStreamDataHolder outputStreamDataHolder = entry.getValue();
                String streamId = entry.getKey();

                if (outputStreamDataHolder.getEventHolderType().equals(EventHolder.STREAM)) {
                    Map<String, List<SubscriptionStrategyDataHolder>> fieldGroupingSubscriptions = new HashMap<>();
                    boolean isInnerGroupStream = true;
                    for (SiddhiQueryGroup siddhiQueryGroup2 : siddhiQueryGroupsList.subList(i + 1,
                            siddhiQueryGroupsList.size())) {
                        if (siddhiQueryGroup2.getInputStreams().containsKey(streamId)) {
                            isInnerGroupStream = false;
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

                                if (!outputStreamDataHolder.isSinkBridgeAdded()) {
                                    String outputStreamDefinition = outputStreamDataHolder.
                                            getStreamDefinition().replace(runtimeStreamDefinition, "\n"
                                            + "${" + streamId + "} ") + runtimeStreamDefinition;
                                    outputStreamDataHolder.setStreamDefinition(outputStreamDefinition);
                                    outputStreamDataHolder.setSinkBridgeAdded(true);
                                }

                                inputStreamDataHolder.setStreamDefinition("${" + streamId + "} "
                                        + runtimeStreamDefinition);
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
                    if (isInnerGroupStream && !outputStreamDataHolder.isUserGiven()) {
                        siddhiQueryGroup1.getOutputStreams().get(streamId).setInnerGroupStream(true);
                        if (siddhiQueryGroup1.getInputStreams().get(streamId) != null) {
                            siddhiQueryGroup1.getInputStreams().get(streamId).setInnerGroupStream(true);
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
        for (Entry<String, PartitionType> partitionTypeEntry : partition.getPartitionTypeMap().entrySet()) {

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
                updateInputStreamDataHolders(streamID, partitionKey);
            } else {
                //Not yet supported
                throw new SiddhiAppValidationException("Unsupported: "
                        + execGroupName + " Range PartitionType not Supported in Distributed SetUp");
            }
        }
    }

    private void updateInputStreamDataHolders(String streamID, String partitionKey) {
        for (SiddhiQueryGroup siddhiQueryGroup : siddhiTopologyDataHolder.getSiddhiQueryGroupMap().values()) {
            InputStreamDataHolder holder = siddhiQueryGroup.getInputStreams().get(streamID);
            if (holder != null && holder.getSubscriptionStrategy().getStrategy() != TransportStrategy.FIELD_GROUPING) {
                holder.getSubscriptionStrategy().setPartitionKey(partitionKey);
            }
        }
    }
}
