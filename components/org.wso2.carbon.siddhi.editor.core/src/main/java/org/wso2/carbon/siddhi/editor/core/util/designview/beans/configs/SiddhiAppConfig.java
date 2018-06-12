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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.FunctionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.SiddhiElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.WindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains elements of a Siddhi app
 */
public class SiddhiAppConfig {
    private int finalElementCount = 0;

    private String siddhiAppName = "";
    private List<String> appAnnotationList = new ArrayList<>();
    private List<SourceSinkConfig> sourceList = new ArrayList<>();
    private List<SourceSinkConfig> sinkList = new ArrayList<>();
    private List<StreamConfig> streamList = new ArrayList<>();
    private List<TableConfig> tableList = new ArrayList<>();
    private List<TriggerConfig> triggerList = new ArrayList<>();
    private List<WindowConfig> windowList = new ArrayList<>();
    private List<AggregationConfig> aggregationList = new ArrayList<>();
    private List<FunctionConfig> functionList = new ArrayList<>();
    private Map<QueryListType, List<QueryConfig>> queryLists = new EnumMap<>(QueryListType.class);
    private List<PartitionConfig> partitionList = new ArrayList<>();

    public SiddhiAppConfig() {
        queryLists.put(QueryListType.WINDOW_FILTER_PROJECTION, new ArrayList<>());
        queryLists.put(QueryListType.JOIN, new ArrayList<>());
        queryLists.put(QueryListType.PATTERN, new ArrayList<>());
        queryLists.put(QueryListType.SEQUENCE, new ArrayList<>());
    }

    /**
     * Returns Id for the next element id, after incrementing the final element count
     * @return      Id for the element
     */
    private String generateNextElementId() {
        return String.valueOf(++finalElementCount);
    }

    /**
     * Returns PartitionConnector id, with the given partition id and connector id
     * @param partitionId       Id of the PartitionConfig
     * @param connectorId       Id of the PartitionConnector, within the Partition
     * @return                  PartitionConnector id
     */
    private String generatePartitionConnectorId(String partitionId, String connectorId) {
        return partitionId + "_pc" + connectorId;
    }

    /**
     * Adds a given generic type Siddhi ElementConfig, to the given list of the same generic type
     * @param elementList       List to which, the given element config should be added
     * @param elementConfig     Siddhi ElementConfig object
     * @param <T>               Generic type of the Siddhi ElementConfig object, and each membe of the list
     */
    private <T> void addElement(List<T> elementList, T elementConfig) {
        ((SiddhiElementConfig) elementConfig).setId(generateNextElementId());
        elementList.add(elementConfig);
    }

    /**
     * Adds a given QueryConfig object to its specific query list, denoted by the given QueryInputType
     * @param queryListType     Key with which, the specific query list is denoted
     * @param queryConfig       QueryConfig object
     */
    public void addQuery(QueryListType queryListType, QueryConfig queryConfig) {
        queryConfig.setId(generateNextElementId());
        queryLists.get(queryListType).add(queryConfig);
    }

    /**
     * Adds a given PartitionConfig object to the partitionList
     * @param partitionConfig       PartitionConfig object
     */
    public void addPartition(PartitionConfig partitionConfig) {
        partitionConfig.setId(generateNextElementId());
        // Assign connectorsAndStreams with renamed Ids
        Map<String, String> connectorsAndStreams = new HashMap<>();
        for (Map.Entry<String, String> connectorAndStream : partitionConfig.getConnectorsAndStreams().entrySet()) {
            connectorsAndStreams.put(
                    generatePartitionConnectorId(partitionConfig.getId(), connectorAndStream.getKey()),
                    connectorAndStream.getValue());
        }
        partitionConfig.setConnectorsAndStreams(connectorsAndStreams);

        // Set Partition details for contained for Queries
        for (List<QueryConfig> queryList : partitionConfig.getQueryLists().values()) {
            for (QueryConfig queryConfig : queryList) {
                queryConfig.setId(generateNextElementId());
                queryConfig.setPartitionId(partitionConfig.getId());
                queryConfig.setConnectorsAndStreams(connectorsAndStreams);
            }
        }
        // Set Partition details Streams
        for (StreamConfig streamConfig : partitionConfig.getStreamList()) {
            streamConfig.setId(generateNextElementId());
            streamConfig.setPartitionId(partitionConfig.getId());
            streamConfig.setConnectorsAndStreams(connectorsAndStreams);
        }
        partitionList.add(partitionConfig);
    }

    public void addSource(SourceSinkConfig sourceConfig) {
        addElement(sourceList, sourceConfig);
    }

    public void addSink(SourceSinkConfig sinkConfig) {
        addElement(sinkList, sinkConfig);
    }

    public void add(StreamConfig streamConfig) {
        addElement(streamList, streamConfig);
    }

    public void add(TableConfig tableConfig) {
        addElement(tableList, tableConfig);
    }

    public void add(TriggerConfig triggerConfig) {
        addElement(triggerList, triggerConfig);
    }

    public void add(WindowConfig windowConfig) {
        addElement(windowList, windowConfig);
    }

    public void add(AggregationConfig aggregationConfig) {
        addElement(aggregationList, aggregationConfig);
    }

    public void add(FunctionConfig functionConfig) {
        addElement(functionList, functionConfig);
    }

    public void add(PartitionConfig partitionConfig) {
        addElement(partitionList, partitionConfig);
    }

    public void setAppAnnotationList(List<String> appAnnotationList) {
        this.appAnnotationList = appAnnotationList;
    }

    public void setSiddhiAppName(String siddhiAppName) {
        this.siddhiAppName = siddhiAppName;
    }

    public String getSiddhiAppName() {
        return siddhiAppName;
    }

    public List<String> getAppAnnotationList() {
        return appAnnotationList;
    }

    public List<SourceSinkConfig> getSourceList() {
        return sourceList;
    }

    public List<SourceSinkConfig> getSinkList() {
        return sinkList;
    }

    public List<StreamConfig> getStreamList() {
        return streamList;
    }

    public List<TableConfig> getTableList() {
        return tableList;
    }

    public List<TriggerConfig> getTriggerList() {
        return triggerList;
    }

    public List<WindowConfig> getWindowList() {
        return windowList;
    }

    public List<AggregationConfig> getAggregationList() {
        return aggregationList;
    }

    public Map<QueryListType, List<QueryConfig>> getQueryLists() {
        return queryLists;
    }

    public List<FunctionConfig> getFunctionList() {
        return functionList;
    }

    public List<PartitionConfig> getPartitionList() {
        return partitionList;
    }

    public int getFinalElementCount() {
        return finalElementCount;
    }
}
