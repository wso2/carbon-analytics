package org.wso2.carbon.status.dashboard.core.eventFlow;

import org.apache.log4j.Logger;
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
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamFunction;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.util.ExceptionUtil;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * .
 */
public class SiddhiAppComponents {
    private static final Logger log = Logger.getLogger(SiddhiAppComponents.class);
    private static SiddhiApp siddhiApp;
    private static StringBuilder eventFlowNodes = new StringBuilder(" '{ \"nodes\": [ ");
    private static StringBuilder eventFlowEdges = new StringBuilder("\"edges\": [ ");
    private static String userDefinedSiddhiAp;

    private static void createComponentList(String userDefinedSiddhiApp) {
        userDefinedSiddhiAp = userDefinedSiddhiApp;
        siddhiApp = SiddhiCompiler.parse(userDefinedSiddhiApp);
        SiddhiQueryDataHolder siddhiQueryGroup;
        int[] queryContextEndIndex;
        int[] queryContextStartIndex;
        String execGroupName;
        String defaultExecGroupName = getAppName() + "-" + UUID.randomUUID();
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            execGroupName = null;
            for (Annotation annotation : executionElement.getAnnotations()) {
                if (annotation.getElement(EventFlowConstants.EXECGROUP_IDENTIFIER) != null) {
                    execGroupName = getAppName()
                            + "-"
                            + annotation.getElement(EventFlowConstants.EXECGROUP_IDENTIFIER);
                }
            }
            if (execGroupName == null) {
                execGroupName = defaultExecGroupName;
            }
            siddhiQueryGroup = new SiddhiQueryDataHolder();
            siddhiQueryGroup.setName(execGroupName);

            if (executionElement instanceof Query) {
                //set query
                queryContextStartIndex = ((Query) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Query) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetaInfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));
                assignStreamInfoSiddhiQueryGroup((Query) executionElement, siddhiQueryGroup, true);
            } else if (executionElement instanceof Partition) {
                //set Partition
                queryContextStartIndex = ((Partition) executionElement).getQueryContextStartIndex();
                queryContextEndIndex = ((Partition) executionElement).getQueryContextEndIndex();
                siddhiQueryGroup.addQuery(removeMetaInfoQuery(executionElement, ExceptionUtil
                        .getContext(queryContextStartIndex, queryContextEndIndex, userDefinedSiddhiApp)));

                //assign partitionKeyMap and partitionGroupMap
                storePartitionInfo((Partition) executionElement, execGroupName);

                for (Query query : ((Partition) executionElement).getQueryList()) {
                    for (Annotation annotation : query.getAnnotations()) {
                        if (annotation.getElement(EventFlowConstants.DISTRIBUTED_IDENTIFIER) != null) {
                            throw new SiddhiAppValidationException(
                                    "Unsupported:@dist annotation inside partition queries");
                        }
                    }
                    assignStreamInfoSiddhiQueryGroup(query, siddhiQueryGroup, false);
                }
            }
        }
        if (!siddhiApp.getTriggerDefinitionMap().isEmpty()){
            for (String triggerDefinition: siddhiApp.getTriggerDefinitionMap().keySet()) {
                addNode(triggerDefinition, ComponentHolderType.TRIGGER.name());
            }
        }
    }

    private static String getAppName() {
        for (int i = 0; i < siddhiApp.getAnnotations().size(); i++) {
            if (siddhiApp.getAnnotations().get(i).getName().equals("name")) {
                return siddhiApp.getAnnotations().get(i).getElements().get(0).getValue();
            }
        }
        return EventFlowConstants.DEFAULT_SIDDHIAPP_NAME + "-" + UUID.randomUUID();//defaultName
    }

    private static String removeMetaInfoQuery(ExecutionElement executionElement, String queryElement) {
        int[] queryContextStartIndex;
        int[] queryContextEndIndex;

        for (Annotation annotation : executionElement.getAnnotations()) {
            if (annotation.getName().toLowerCase()
                    .equals(EventFlowConstants.DISTRIBUTED_IDENTIFIER)) {
                queryContextStartIndex = annotation.getQueryContextStartIndex();
                queryContextEndIndex = annotation.getQueryContextEndIndex();
                queryElement = queryElement.replace(
                        ExceptionUtil.getContext(queryContextStartIndex,
                                queryContextEndIndex,
                                userDefinedSiddhiAp), "");
                break;
            }
        }
        return queryElement;
    }

    private static void assignStreamInfoSiddhiQueryGroup(Query executionElement, SiddhiQueryDataHolder siddhiQueryGroup,
                                                         boolean isQuery) {
        InputStream inputStream;
        List<String> inputStreamList = new ArrayList<>();
        SingleInputStream singleInputStream;
        if ((executionElement).getInputStream() instanceof SingleInputStream) {
            singleInputStream = (SingleInputStream) (executionElement).getInputStream();
            for (StreamHandler streamHandler : singleInputStream.getStreamHandlers()) {
                if (streamHandler instanceof Window) {
                    addNode(singleInputStream.getStreamId()+((Window) streamHandler).getName(),ComponentHolderType.WINDOW
                            .name());
                    addEdge(singleInputStream.getStreamId(),singleInputStream
                            .getStreamId()+":"+((Window) streamHandler).getName(),ComponentHolderType.STREAM.name(),
                            ComponentHolderType.WINDOW.name());
                } else if (streamHandler instanceof Filter) {
                    addNode(singleInputStream.getStreamId()+":"+(streamHandler).toString(),ComponentHolderType.WINDOW.name());
                } else if (streamHandler instanceof StreamFunction) {
                    addNode(singleInputStream.getStreamId()+":"+((StreamFunction) streamHandler).getName(),ComponentHolderType.WINDOW.name());
                }
            }
            inputStream = singleInputStream;
        } else {
            inputStream = (executionElement).getInputStream();
        }
        for (String inputStreamId : inputStream.getAllStreamIds()) {
            //not an inner Stream
            if (!inputStreamId.startsWith(EventFlowConstants.INNERSTREAM_IDENTIFIER)) {
                extractAppComponents(inputStreamId);
                inputStreamList.add(inputStreamId);
            }
        }

        String outputStreamId = executionElement.getOutputStream().getId();
        //not an inner Stream
        if (!outputStreamId.startsWith(EventFlowConstants.INNERSTREAM_IDENTIFIER)) {
            extractAppComponents(outputStreamId);
        }
        for (String inStream : inputStreamList) {
            addEdge(inStream, outputStreamId, ComponentHolderType.STREAM.name(), ComponentHolderType.STREAM.toString());
        }
    }

    private static void extractAppComponents(String streamId) {
        String streamDefinition = null;
        int[] queryContextEndIndex;
        int[] queryContextStartIndex;
        if (siddhiApp.getStreamDefinitionMap().containsKey(streamId)) {
            queryContextStartIndex = siddhiApp.getStreamDefinitionMap().get(streamId).getQueryContextStartIndex();
            queryContextEndIndex = siddhiApp.getStreamDefinitionMap().get(streamId).getQueryContextEndIndex();
            streamDefinition = ExceptionUtil.getContext(queryContextStartIndex, queryContextEndIndex,
                    userDefinedSiddhiAp);
            if (!isUserGivenStream(streamDefinition)) {
                ComponentInfoDataHolder streamData =
                        new ComponentInfoDataHolder(streamId, ComponentHolderType.STREAM);
                addNode(streamData.getElementId(), streamData.getComponentHolderType().name());
            }
            for (Annotation annotation : siddhiApp.getStreamDefinitionMap().get(streamId).getAnnotations()) {
                if (annotation.getName().toLowerCase().equals("window")) {
                    ComponentInfoDataHolder windowData = new ComponentInfoDataHolder();
                    windowData.setElements(annotation.getElements());
                    windowData.setComponentHolderTypeType(ComponentHolderType.WINDOW);
                    addNode(windowData.getElementId(), windowData.getComponentHolderType().name());
                    ComponentInfoDataHolder streamData =
                            new ComponentInfoDataHolder(streamId, ComponentHolderType.STREAM);
                    addNode(streamData.getElementId(), streamData.getComponentHolderType().name());
                    addEdge(windowData.getElementId(), streamData.getElementId(), windowData.getComponentHolderType().name(), streamData.getComponentHolderType().name());
                    break;
                }
            }
            if (streamDefinition.toLowerCase().contains(
                    EventFlowConstants.SOURCE_IDENTIFIER)) {
                ComponentInfoDataHolder sourceData =
                        new ComponentInfoDataHolder(streamId, ComponentHolderType.SOURCE);
                ComponentInfoDataHolder mapperData = null;
                for (Annotation annotation : siddhiApp.getStreamDefinitionMap().get(streamId).getAnnotations()) {
                    if (annotation.getName().equalsIgnoreCase("source")) {
                        List<Element> elements = new ArrayList<>();
                        elements.add(new Element("type", annotation.getElement("type")));
                        sourceData.setElements(elements);
                        for (Annotation innerAnnotation : annotation.getAnnotations()) {
                            if (innerAnnotation.getName().equalsIgnoreCase("map")) {
                                mapperData = new ComponentInfoDataHolder(streamId, ComponentHolderType.MAPPER);
                                List<Element> mapperElements = new ArrayList<>();
                                mapperElements.add(new Element("type", innerAnnotation.getElement("type")));
                                mapperData.setElements(mapperElements);
                            }
                        }
                    }
                }
                addNode(sourceData.getElementId(), sourceData.getComponentHolderType().name());
                ComponentInfoDataHolder streamData =
                        new ComponentInfoDataHolder(streamId, ComponentHolderType.STREAM);
                addNode(streamData.getElementId(), streamData.getComponentHolderType().name());
                if (mapperData != null) {
                    addEdge(sourceData.getElementId(), mapperData.getElementId(), sourceData.getComponentHolderType().name
                            (), mapperData.getComponentHolderType().name());
                    addEdge(mapperData.getElementId(), streamData.getElementId(), mapperData.getComponentHolderType()
                            .name(), streamData.getComponentHolderType().name());
                } else {
                    addEdge(sourceData.getElementId(), streamData.getElementId(), sourceData.getComponentHolderType().name
                            (), streamData.getComponentHolderType().name());
                }
            }
            //if source or Sink
            if (streamDefinition.toLowerCase().contains
                    (EventFlowConstants.SINK_IDENTIFIER)) {
                ComponentInfoDataHolder sinkData =
                        new ComponentInfoDataHolder(streamId, ComponentHolderType.SINK);
                ComponentInfoDataHolder mapperData = null;
                for (Annotation annotation : siddhiApp.getStreamDefinitionMap().get(streamId).getAnnotations()) {
                    if (annotation.getName().equalsIgnoreCase("sink")) {
                        List<Element> elements = new ArrayList<>();
                        elements.add(new Element("type", annotation.getElement("type")));
                        sinkData.setElements(elements);
                        for (Annotation innerAnnotation : annotation.getAnnotations()) {
                            if (innerAnnotation.getName().equalsIgnoreCase("map")) {
                                mapperData = new ComponentInfoDataHolder(streamId, ComponentHolderType.MAPPER);
                                List<Element> mapperElements = new ArrayList<>();
                                mapperElements.add(new Element("type", innerAnnotation.getElement("type")));
                                mapperData.setElements(mapperElements);
                            }
                        }
                    }
                }
                addNode(sinkData.getElementId(), sinkData.getComponentHolderType().name());
                ComponentInfoDataHolder streamData =
                        new ComponentInfoDataHolder(streamId, ComponentHolderType.STREAM);
                addNode(streamData.getElementId(), streamData.getComponentHolderType().name());
                if (mapperData != null) {
                    addEdge(sinkData.getElementId(), mapperData.getElementId(), sinkData.getComponentHolderType().name
                            (), mapperData.getComponentHolderType().name());
                    addEdge(mapperData.getElementId(), streamData.getElementId(), mapperData.getComponentHolderType()
                            .name(), streamData.getComponentHolderType().name());
                } else {
                    addEdge(sinkData.getElementId(), streamData.getElementId(), sinkData.getComponentHolderType().name
                            (), streamData.getComponentHolderType().name());
                }
            }

        } else if (siddhiApp.getTableDefinitionMap().containsKey(streamId)) {
            ComponentInfoDataHolder tableData = new ComponentInfoDataHolder();
            AbstractDefinition tableDefinition = siddhiApp.getTableDefinitionMap().get(streamId);
            tableData.setComponentHolderTypeType(ComponentHolderType.INMEMORYTABLE);
            for (Annotation annotation : tableDefinition.getAnnotations()) {
                if (annotation.getName().toLowerCase().equals(
                        EventFlowConstants.PERSISTENCETABLE_IDENTIFIER)) {
                    tableData.setComponentHolderTypeType(ComponentHolderType.TABLE);
                    List<Element> elements = new ArrayList<>();
                    elements.add(new Element("type", annotation.getElement("type")));
                    elements.add(new Element("jdbc.url", annotation.getElement("jdbc.url")));
                    tableData.setElements(elements);
                    break;
                }
            }
            addNode(tableData.getElementId(), tableData.getComponentHolderType().name());
            ComponentInfoDataHolder streamData =
                    new ComponentInfoDataHolder(streamId, ComponentHolderType.STREAM);
            addNode(streamData.getElementId(), streamData.getComponentHolderType().name());
            addEdge(tableData.getElementId(), streamData.getElementId(), tableData.getComponentHolderType().name(),
                    streamData.getComponentHolderType().name());
        } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
            ComponentInfoDataHolder componentInfoDataHolder = new ComponentInfoDataHolder();
            for (Annotation annotation : siddhiApp.getWindowDefinitionMap().get(streamId).getAnnotations()) {
                if (annotation.getName().toLowerCase().equals("window")) {
                    componentInfoDataHolder.setElements(annotation.getElements());
                    componentInfoDataHolder.setComponentHolderTypeType(ComponentHolderType.WINDOW);
                    break;
                }
            }
            //if stream definition is an inferred definition
        } else if (siddhiApp.getAggregationDefinitionMap().containsKey(streamId)) {
            ComponentInfoDataHolder aggregation =
                    new ComponentInfoDataHolder(streamId, ComponentHolderType.AGGREGATION);
            addNode(aggregation.getElementId(), aggregation.getComponentHolderType().name());
        } else if (siddhiApp.getFunctionDefinitionMap().containsKey(streamId)) {
            ComponentInfoDataHolder functionData =
                    new ComponentInfoDataHolder(streamId, ComponentHolderType.FUNCTION);
            addNode(functionData.getElementId(), functionData.getComponentHolderType().name());
        } else {
            ComponentInfoDataHolder streamData =
                    new ComponentInfoDataHolder(streamId, ComponentHolderType.STREAM);
            addNode(streamData.getElementId(), streamData.getComponentHolderType().name());
        }
    }

    private static void addNode(String nodeID, String type) {
        eventFlowNodes.append("  { \"id\": \"").append(nodeID).append(":").append(type).append
                ("\", \"label\":\"").append(nodeID).append("\", \"nodeclass\": \"" + type + "\" },");
    }

    private static void addEdge(String fromNode, String toNode, String fromType, String toType) {
        eventFlowEdges.append("  { \"from\": \"").append(fromNode).append(":").append(fromType).
                append("\", \"to\":\"").append(toNode).append(":").append(toType).append("\" },");
    }


    private static boolean isUserGivenStream(String streamDefinition) {
        return streamDefinition.toLowerCase().contains(
                EventFlowConstants.SOURCE_IDENTIFIER) || streamDefinition.toLowerCase().contains
                (EventFlowConstants.SINK_IDENTIFIER);
    }

    private static void storePartitionInfo(Partition partition, String execGroupName) {
        LinkedList<String> partitionKeyList; //contains all the partition-keys corresponding to a partitioned streamId.
        LinkedList<String> partitionGroupList;//contains all the execGroups containing partitioned streamId
        String partitionKey;

        //assign partitionKeyMap and partitionGroupMap
        for (Map.Entry<String, PartitionType> partitionTypeEntry : partition.getPartitionTypeMap().entrySet()) {

            partitionKeyList = new LinkedList<>();
            partitionGroupList = new LinkedList<>();
            //when more than one partition residing in the same SiddhiApp
            if (partitionGroupList.contains(execGroupName)) {
                throw new SiddhiAppValidationException("Unsupported in distributed setup :More than 1 partition "
                        + "residing on "
                        + "the same "
                        + "execGroup " + execGroupName);
            } else {
                partitionGroupList.add(execGroupName);
            }
            if (partitionTypeEntry.getValue() instanceof ValuePartitionType) {
                partitionKey = ((Variable) ((ValuePartitionType) partitionTypeEntry.getValue()).getExpression())
                        .getAttributeName();
                partitionKeyList.add(partitionKey);
            } else {
                //Not yet supported
                throw new SiddhiAppValidationException("Unsupported: "
                        + execGroupName + " Range PartitionType not Supported in "
                        + "Distributed SetUp");
            }
        }
    }

    public static void main(String[] args) {
        String siddhiApp2 = "@App:name(\"store-test-plan\")\n" +
                "@app:statistics(reporter = 'jdbc', interval = '15' )\n" +
                "define trigger FiveMinTriggerStream at every 5 min;\n";
        createComponentList(siddhiApp2);
        StringBuilder eventFlow = new StringBuilder(eventFlowNodes.substring(0, eventFlowNodes.length() - 1));
        eventFlow.append("]}'");
        System.out.println(eventFlow.toString());
        StringBuilder eventFlow2 = new StringBuilder(eventFlowEdges.substring(0, eventFlowEdges.length() - 1));
        eventFlow2.append("]}'");
        System.out.println(eventFlow2.toString());
    }
}
