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
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.SiddhiElement;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.definition.FunctionDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;
import org.wso2.siddhi.query.api.definition.WindowDefinition;
import org.wso2.siddhi.query.api.execution.ExecutionElement;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.partition.PartitionType;
import org.wso2.siddhi.query.api.execution.partition.ValuePartitionType;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.selection.OutputAttribute;
import org.wso2.siddhi.query.api.expression.AttributeFunction;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;

import java.util.ArrayList;
import java.util.List;

public class SiddhiAppInfo {

    private String siddhiAppStr;

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

    private int numberOfQueries;
    private int numberOfPartitions;
    private int numberOfValuePartitionTypes;
    private int numberOfRangePartitionTypes;

    public SiddhiAppInfo(String siddhiAppStr) {
        this.siddhiAppStr = siddhiAppStr;
        loadSiddhiAppInfo();
    }

    private void loadSiddhiAppInfo() {
        // Compile 'siddhiAppStr' To A SiddhiApp Object
        SiddhiApp siddhiApp = SiddhiCompiler.parse(siddhiAppStr);

        // This is done just to check for any runtime errors.
        SiddhiAppRuntime siddhiAppRuntime = new SiddhiManager().createSiddhiAppRuntime(siddhiAppStr);

        // Get The App Name And Description
        for (Annotation annotation : siddhiApp.getAnnotations()) {
            if (annotation.getName().equals("name")) {
                appName = annotation.getElements().get(0).getValue();
            } else if (annotation.getName().equals("description")) {
                appDescription = annotation.getElements().get(0).getValue();
            }
        }

        // Get Trigger Info
        for (TriggerDefinition triggerDefinition : siddhiApp.getTriggerDefinitionMap().values()) {
            String triggerDefinitionStr = getContext(triggerDefinition);
            triggers.add(new TriggerInfo(triggerDefinition.getId(), triggerDefinition.getId(), triggerDefinitionStr));
        }

        // Get Stream Info
        // NOTE - This information is taken from the SiddhiAppRuntime class
        for (StreamDefinition streamDefinition : siddhiAppRuntime.getStreamDefinitionMap().values()) {
            StreamInfo streamInfo = generateStreamInfo(streamDefinition);
            if (streamInfo != null) {
                streams.add(streamInfo);
            }
        }

        // Get Table Info
        for (TableDefinition tableDefinition : siddhiApp.getTableDefinitionMap().values()) {
            String tableDefinitionStr = getContext(tableDefinition);
            tables.add(new TableInfo(tableDefinition.getId(), tableDefinition.getId(), tableDefinitionStr));
        }

        // Get Window Info
        for (WindowDefinition windowDefinition : siddhiApp.getWindowDefinitionMap().values()) {
            String windowDefinitionStr = getContext(windowDefinition);
            windows.add(new WindowInfo(windowDefinition.getId(), windowDefinition.getId(), windowDefinitionStr));
        }

        // Get Aggregation Info
        for (AggregationDefinition aggregationDefinition : siddhiApp.getAggregationDefinitionMap().values()) {
            String aggregationDefinitionStr = getContext(aggregationDefinition);
            aggregations.add(new AggregationInfo(aggregationDefinition.getId(), aggregationDefinition.getId(),
                    aggregationDefinitionStr, aggregationDefinition.getBasicSingleInputStream().getStreamId()));
        }

        // Get Function Info
        for (FunctionDefinition functionDefinition : siddhiApp.getFunctionDefinitionMap().values()) {
            String functionDefinitionStr = getContext(functionDefinition);
            functions.add(new FunctionInfo(functionDefinition.getId(), functionDefinition.getId(),
                    functionDefinitionStr));
        }

        // Get Query and Partition Info
        for (ExecutionElement executionElement : siddhiApp.getExecutionElementList()) {
            if (executionElement instanceof Query) {
                Query query = (Query) executionElement;
                QueryInfo queryInfo = generateQueryInfo(query);
                queries.add(queryInfo);
                numberOfQueries++;
            } else {
                Partition partition = (Partition) executionElement;
                PartitionInfo partitionInfo = generatePartitionInfo(partition);
                partitions.add(partitionInfo);
                numberOfPartitions++;
            }
        }
    }

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

    private QueryInfo generateQueryInfo(Query query) {
        QueryInfo queryInfo = new QueryInfo();

        // Set The Name Of The Query
        for (Annotation annotation : query.getAnnotations()) {
            if (annotation.getName().equals("info")) {
                queryInfo.setId(annotation.getElement("name"));
                queryInfo.setName(annotation.getElement("name"));
                break;
            }
        }
        // If Query Does Not Have A Name, Assign A Predefined Name To It
        if (queryInfo.getId() == null || queryInfo.getName() == null) {
            queryInfo.setId("query" + Integer.toString(numberOfQueries));
            queryInfo.setName("Query");
        }

        // Set The Code That Defines This Query
        String queryDefinitionStr = getContext(query);
        queryInfo.setDefinition(queryDefinitionStr);

        // Set The Input & Output Streams
        queryInfo.setInputStreamIds(query.getInputStream().getUniqueStreamIds());
        queryInfo.setOutputStreamId(query.getOutputStream().getId());

        // Set The Javascript/Scala Functions Used In The Query
        queryInfo.setFunctionIds(getFunctionIdsInQuery(query));

        return queryInfo;
    }

    private PartitionInfo generatePartitionInfo(Partition partition) {
        // Create Partition If ExecutionElement Is An Instance Of Partition
        PartitionInfo partitionInfo = new PartitionInfo();

        // Get The Name Of The Partition
        for (Annotation annotation : partition.getAnnotations()) {
            if (annotation.getName().equals("info")) {
                partitionInfo.setId(annotation.getElement("name"));
                partitionInfo.setName(annotation.getElement("name"));
                break;
            }
        }
        // If The Partition Does Not Have A Name, Assign A Predefined Name To It
        if (partitionInfo.getId() == null || partitionInfo.getName() == null) {
            partitionInfo.setId("partition" + Integer.toString(numberOfPartitions));
            partitionInfo.setName("Partition");
        }

        // Set The Code That Defines This Partition
        String partitionDefinitionStr = getContext(partition);
        partitionInfo.setDefinition(partitionDefinitionStr);

        // Set The Queries Defined Inside This Partition
        for (Query query : partition.getQueryList()) {
            QueryInfo queryInfo = generateQueryInfo(query);
            partitionInfo.addQuery(queryInfo);
            numberOfQueries++;
        }

        // Set The Value And Range Partition Information
        for (PartitionType partitionType : partition.getPartitionTypeMap().values()) {
            PartitionTypeInfo partitionTypeInfo = generatePartitionTypeInfo(partitionType);
            // Add This Partition Type Information To The Partition Info
            partitionInfo.addPartitionType(partitionTypeInfo);
        }

        return partitionInfo;
    }

    private PartitionTypeInfo generatePartitionTypeInfo(PartitionType partitionType) {
        PartitionTypeInfo partitionTypeInfo = new PartitionTypeInfo();

        if (partitionType instanceof ValuePartitionType) {
            //Set A Predefined Name & Id For This Value Partition
            partitionTypeInfo.setId("valuePartition" + Integer.toString(numberOfValuePartitionTypes));
            partitionTypeInfo.setName("Value Partition");
            numberOfValuePartitionTypes++;
        } else {
            // Set A Predefined Name & Id For The RangePartition
            partitionTypeInfo.setId("rangePartition" + Integer.toString(numberOfRangePartitionTypes));
            partitionTypeInfo.setName("Range Partition");
            numberOfRangePartitionTypes++;
        }

        String partitionTypeDefinition = getContext(partitionType);
        partitionTypeInfo.setDefinition(partitionTypeDefinition);

        partitionTypeInfo.setStreamId(partitionType.getStreamId());

        return partitionTypeInfo;
    }

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
     * Used To Obtain A Specific Piece Of Code From The Siddhi App String
     * From The Given Start & End Index - Taken From Siddhi Source 'ExceptionUtil.getContext()'
     */
    private String getContext(SiddhiElement siddhiElement) {

        int[] startIndex = siddhiElement.getQueryContextStartIndex();
        int[] endIndex = siddhiElement.getQueryContextEndIndex();

        int startLinePosition = ordinalIndexOf(siddhiAppStr, startIndex[0]);
        int endLinePosition = ordinalIndexOf(siddhiAppStr, endIndex[0]);
        return siddhiAppStr.substring(startLinePosition + startIndex[1], endLinePosition + endIndex[1])
                .replaceAll("'", "\"");
    }

    /**
     * Finds The Ordinal Index To Help The 'getContext()' Method Obtain The
     * Definition Of A Siddhi Element - Taken From Siddhi Source 'ExceptionUtil.ordinalIndexOf()'
     */
    private int ordinalIndexOf(String str, int num) {
        int position = 0;

        while (true) {
            num--;
            if (num <= 0) {
                return position;
            }
            position = str.indexOf('\n', position) + 1;
        }
    }

    // Getters
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
