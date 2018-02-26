/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.eventflow;

import org.wso2.carbon.siddhi.editor.core.util.eventflow.constants.SiddhiAnnotationType;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.AggregationInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.FunctionInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.PartitionInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.PartitionTypeInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.QueryInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.StreamInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.TableInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.TriggerInfo;
import org.wso2.carbon.siddhi.editor.core.util.eventflow.info.WindowInfo;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.SiddhiElement;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.definition.WindowDefinition;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.partition.PartitionType;
import org.wso2.siddhi.query.api.execution.partition.RangePartitionType;
import org.wso2.siddhi.query.api.execution.partition.ValuePartitionType;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Obtains a Siddhi App as a string and parses it to a SiddhiApp object to identify map the data in it.
 */
public class SiddhiAppMap {

    private String siddhiAppString;
    private SiddhiApp siddhiApp;
    private SiddhiAppRuntime siddhiAppRuntime;

    private String appName;
    private String appDescription;

    private List<StreamInfo> streams = new ArrayList<>();
    private List<TableInfo> tables = new ArrayList<>();
    private List<WindowInfo> windows = new ArrayList<>();
    private List<TriggerInfo> triggers = new ArrayList<>();
    private List<AggregationInfo> aggregations = new ArrayList<>();
    private List<FunctionInfo> functions = new ArrayList<>();
    private List<QueryInfo> queries = new ArrayList<>();
    private List<PartitionInfo> partitions = new ArrayList<>();

    public SiddhiAppMap(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
        loadSiddhiAppInfo();
    }

    /**
     * The main method that is called from the constructor to obtain the
     * information of a Siddhi App from the SiddhiAppStr.
     */
    private void loadSiddhiAppInfo() {
        try {
            // Compile 'siddhiAppString' To A SiddhiApp Object
            siddhiApp = SiddhiCompiler.parse(siddhiAppString);
            // This is done to check for any runtime errors and to obtain all the streams.
            siddhiAppRuntime = new SiddhiManager().createSiddhiAppRuntime(siddhiAppString);

            loadAppNameAndDescription();
            loadTriggers();
            loadStreams();
            loadTables();
            loadWindows();
            loadAggregations();
            loadFunctions();
            loadQueriesAndPartitions();

        } catch (Throwable e) {
            throw new SiddhiAppCreationException(e.getMessage());
        }
    }

    private void loadQueriesAndPartitions() throws ClassNotFoundException {
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            if (executionElement instanceof Query) {
                Query query = (Query) executionElement;
                QueryInfo queryInfo = generateQueryInfo(query);
                queries.add(queryInfo);
            } else if (executionElement instanceof Partition) {
                Partition partition = (Partition) executionElement;
                PartitionInfo partitionInfo = generatePartitionInfo(partition);
                partitions.add(partitionInfo);
            } else {
                throw new ClassNotFoundException("An unidentified instance of the ExecutionElement Class was found");
            }
        }
    }

    private void loadFunctions() {
        for (FunctionDefinition functionDefinition : siddhiApp.getFunctionDefinitionMap().values()) {
            String functionDefinitionStr = getDefinition(functionDefinition);
            functions.add(new FunctionInfo(functionDefinition.getId(), functionDefinition.getId(),
                    functionDefinitionStr));
        }
    }

    private void loadAggregations() {
        for (AggregationDefinition aggregationDefinition : siddhiApp.getAggregationDefinitionMap().values()) {
            String aggregationDefinitionStr = getDefinition(aggregationDefinition);
            aggregations.add(new AggregationInfo(aggregationDefinition.getId(), aggregationDefinition.getId(),
                    aggregationDefinitionStr, aggregationDefinition.getBasicSingleInputStream().getStreamId()));
        }
    }

    private void loadWindows() {
        for (WindowDefinition windowDefinition : siddhiApp.getWindowDefinitionMap().values()) {
            String windowDefinitionStr = getDefinition(windowDefinition);
            windows.add(new WindowInfo(windowDefinition.getId(), windowDefinition.getId(), windowDefinitionStr));
        }
    }

    private void loadTables() {
        for (TableDefinition tableDefinition : siddhiApp.getTableDefinitionMap().values()) {
            String tableDefinitionStr = getDefinition(tableDefinition);
            tables.add(new TableInfo(tableDefinition.getId(), tableDefinition.getId(), tableDefinitionStr));
        }
    }

    private void loadStreams() throws ClassNotFoundException {
        for (StreamDefinition streamDefinition : siddhiAppRuntime.getStreamDefinitionMap().values()) {
            StreamInfo streamInfo = generateStreamInfo(streamDefinition);
            if (streamInfo != null) {
                streams.add(streamInfo);
            }
        }

        for (Map<String, AbstractDefinition> map : siddhiAppRuntime.getPartitionedInnerStreamDefinitionMap().values()) {
            for (AbstractDefinition abstractDefinition : map.values()) {
                if (abstractDefinition instanceof StreamDefinition) {
                    StreamDefinition streamDefinition = (StreamDefinition) abstractDefinition;
                    StreamInfo streamInfo = generateStreamInfo(streamDefinition);
                    if (streamInfo != null) {
                        streams.add(streamInfo);
                    }
                } else {
                    throw new ClassNotFoundException("The partitioned inner stream definition map" +
                            " can only have instances of class type 'StreamDefinition'");
                }
            }
        }
    }

    private void loadTriggers() {
        for (TriggerDefinition triggerDefinition : siddhiApp.getTriggerDefinitionMap().values()) {
            String triggerDefinitionStr = getDefinition(triggerDefinition);
            triggers.add(new TriggerInfo(triggerDefinition.getId(), triggerDefinition.getId(), triggerDefinitionStr));
        }
    }

    private void loadAppNameAndDescription() {
        for (Annotation annotation : siddhiApp.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationType.NAME.getTypeAsString())) {
                appName = annotation.getElements().get(0).getValue();
            } else if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationType.DESCRIPTION.getTypeAsString())) {
                appDescription = annotation.getElements().get(0).getValue();
            }
        }
    }

    /**
     * Creates and returns an instance of the StreamInfo class based on the given Siddhi StreamDefinition object.
     *
     * @param streamDefinition The given Siddhi StreamDefinition object
     * @return The created StreamInfo object
     */
    private StreamInfo generateStreamInfo(StreamDefinition streamDefinition) {
        StreamInfo streamInfo = null;
        if (!triggers.isEmpty()) {
            // Check Whether The Stream Has Been Already Defined As A Trigger
            boolean isTriggerDefined = false;
            for (TriggerInfo triggerInfo : triggers) {
                if (streamDefinition.getId().equals(triggerInfo.getId())) {
                    isTriggerDefined = true;
                }
            }
            if (!isTriggerDefined) {
                // Create Stream If The Trigger With The Same Name Is Not Defined
                streamInfo = new StreamInfo(streamDefinition.getId(), streamDefinition.getId(),
                        streamDefinition.toString().replaceAll("'", "\""));
            }
        } else {
            // Create Stream If The App Does Not Have Any Triggers
            streamInfo = new StreamInfo(streamDefinition.getId(), streamDefinition.getId(),
                    streamDefinition.toString().replaceAll("'", "\""));
        }

        return streamInfo;
    }

    /**
     * Creates and returns an instance of the QueryInfo class based on the given Siddhi Query object.
     *
     * @param query The given Siddhi Query object
     * @return The created QueryInfo object
     */
    private QueryInfo generateQueryInfo(Query query) {
        QueryInfo queryInfo = new QueryInfo();

        // Set The Name Of The Query
        for (Annotation annotation : query.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationType.INFO.getTypeAsString())) {
                queryInfo.setId(annotation.getElement(SiddhiAnnotationType.NAME.getTypeAsString()));
                queryInfo.setName(annotation.getElement(SiddhiAnnotationType.NAME.getTypeAsString()));
                break;
            }
        }
        // If Query Does Not Have A Name, Assign A Predefined Name To It
        if (queryInfo.getId() == null || queryInfo.getName() == null) {
            queryInfo.setId(UUID.randomUUID().toString());
            queryInfo.setName("Query");
        }

        // Set The Code That Defines This Query
        String queryDefinitionStr = getDefinition(query);
        queryInfo.setDefinition(queryDefinitionStr);

        // Set The Input & Output Streams
        queryInfo.setInputStreamIds(query.getInputStream().getUniqueStreamIds());
        queryInfo.setOutputStreamId(query.getOutputStream().getId());

        // Set The Javascript/Scala Functions Used In The Query
        queryInfo.setFunctionIds(getFunctionIdsInQuery(query));

        return queryInfo;
    }

    /**
     * Creates and returns an instance of the PartitionInfo class based on the given Siddhi Partition object.
     *
     * @param partition The given Siddhi Partition object
     * @return The created PartitionInfo object
     */
    private PartitionInfo generatePartitionInfo(Partition partition) throws ClassNotFoundException {
        // Create Partition If ExecutionElement Is An Instance Of Partition
        PartitionInfo partitionInfo = new PartitionInfo();

        // Get The Name Of The Partition
        for (Annotation annotation : partition.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase(SiddhiAnnotationType.INFO.getTypeAsString())) {
                partitionInfo.setId(annotation.getElement(SiddhiAnnotationType.NAME.getTypeAsString()));
                partitionInfo.setName(annotation.getElement(SiddhiAnnotationType.NAME.getTypeAsString()));
                break;
            }
        }
        // If The Partition Does Not Have A Name, Assign A Predefined Name To It
        if (partitionInfo.getId() == null || partitionInfo.getName() == null) {
            partitionInfo.setId(UUID.randomUUID().toString());
            partitionInfo.setName("Partition");
        }

        // Set The Code That Defines This Partition
        String partitionDefinitionStr = getDefinition(partition);
        partitionInfo.setDefinition(partitionDefinitionStr);

        // Set The Queries Defined Inside This Partition
        for (Query query : partition.getQueryList()) {
            QueryInfo queryInfo = generateQueryInfo(query);
            partitionInfo.addQuery(queryInfo);
        }

        // Set The Value And Range Partition Information
        for (PartitionType partitionType : partition.getPartitionTypeMap().values()) {
            PartitionTypeInfo partitionTypeInfo = generatePartitionTypeInfo(partitionType);
            // Add This Partition Type Information To The Partition SiddhiElementInfo
            partitionInfo.addPartitionType(partitionTypeInfo);
        }

        return partitionInfo;
    }

    /**
     * Creates and returns an instance of the PartitionTypeInfo class based on the given Siddhi PartitionType object.
     *
     * @param partitionType The given Siddhi PartitionType object
     * @return The created PartitionTypeInfo object
     */
    private PartitionTypeInfo generatePartitionTypeInfo(PartitionType partitionType) throws ClassNotFoundException {
        PartitionTypeInfo partitionTypeInfo = new PartitionTypeInfo();

        if (partitionType instanceof ValuePartitionType) {
            //Set A Predefined Name & Id For This Value Partition
            partitionTypeInfo.setId(UUID.randomUUID().toString());
            partitionTypeInfo.setName("Value Partition");
        } else if (partitionType instanceof RangePartitionType) {
            // Set A Predefined Name & Id For The RangePartition
            partitionTypeInfo.setId(UUID.randomUUID().toString());
            partitionTypeInfo.setName("Range Partition");
        } else {
            throw new ClassNotFoundException("An unidentified instance of the PartitionType Class was found");
        }

        String partitionTypeDefinition = getDefinition(partitionType);
        partitionTypeInfo.setDefinition(partitionTypeDefinition);

        partitionTypeInfo.setStreamId(partitionType.getStreamId());

        return partitionTypeInfo;
    }

    /**
     * Returns the list of user defined functions that are used in a given Siddhi Query object.
     *
     * @param query The Siddhi Query object to obtain the user define function references from
     * @return The list of the names of the user defined functions used in the given query
     */
    private List<String> getFunctionIdsInQuery(Query query) {
        List<String> queryFunctions = new ArrayList<>();
        for (OutputAttribute outputAttribute : query.getSelector().getSelectionList()) {
            if (outputAttribute.getExpression() instanceof AttributeFunction) {
                AttributeFunction attributeFunction = (AttributeFunction) outputAttribute.getExpression();
                // Check whether the function defined is of JS/Scala type
                boolean isFunctionInList = false;
                for (FunctionInfo function : functions) {
                    if (function.getId().equals(attributeFunction.getName())) {
                        isFunctionInList = true;
                        break;
                    }
                }
                if (isFunctionInList) {
                    // If it is a user defined function, then add it's ID.
                    queryFunctions.add(attributeFunction.getName());
                }
            }
        }
        return queryFunctions;
    }

    /**
     * Obtains the piece of the code from the siddhiAppString variable where the given SiddhiElement object is defined.
     *
     * @param siddhiElement The SiddhiElement object where the definition needs to be obtained from
     * @return The definition of the given SiddhiElement object as a String
     */
    private String getDefinition(SiddhiElement siddhiElement) {
        int[] startIndex = siddhiElement.getQueryContextStartIndex();
        int[] endIndex = siddhiElement.getQueryContextEndIndex();

        int startLinePosition = ordinalIndexOf(startIndex[0]);
        int endLinePosition = ordinalIndexOf(endIndex[0]);

        return siddhiAppString.substring(startLinePosition + startIndex[1], endLinePosition + endIndex[1])
                .replaceAll("'", "\"");
    }

    /**
     * Gets the relative position in the siddhiAppString of the start of the given line number.
     *
     * @param lineNumber The line number in which the relative start position should be obtained
     * @return The relative position of where the given line starts in the siddhiAppString
     */
    private int ordinalIndexOf(int lineNumber) {
        int position = 0;
        // TODO: 2/22/18 replace while true with a definite value in the while loop
        while (lineNumber >= 0) {
            lineNumber--;
            if (lineNumber <= 0) {
                break;
            }
            position = siddhiAppString.indexOf('\n', position) + 1;
        }
        return position;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public List<TriggerInfo> getTriggers() {
        return triggers;
    }

    public List<StreamInfo> getStreams() {
        return streams;
    }

    public List<TableInfo> getTables() {
        return tables;
    }

    public List<WindowInfo> getWindows() {
        return windows;
    }

    public List<AggregationInfo> getAggregations() {
        return aggregations;
    }

    public List<FunctionInfo> getFunctions() {
        return functions;
    }

    public List<QueryInfo> getQueries() {
        return queries;
    }

    public List<PartitionInfo> getPartitions() {
        return partitions;
    }

}
