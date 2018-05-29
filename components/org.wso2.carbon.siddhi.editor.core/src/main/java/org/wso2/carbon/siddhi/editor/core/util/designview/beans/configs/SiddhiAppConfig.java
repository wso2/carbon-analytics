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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sourcesink.SourceSinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryInputType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains elements of a Siddhi app
 */
public class SiddhiAppConfig {
    private int finalElementCount = 0;

    private String appName = "";
    private String appDescription = "";

    private List<SourceSinkConfig> sourceList = new ArrayList<>();
    private List<SourceSinkConfig> sinkList = new ArrayList<>();
    private List<StreamConfig> streamList = new ArrayList<>();
    private List<TableConfig> tableList = new ArrayList<>();
    private List<TriggerConfig> triggerList = new ArrayList<>();
    private List<WindowConfig> windowList = new ArrayList<>();
    private List<AggregationConfig> aggregationList = new ArrayList<>();
    private List<FunctionConfig> functionList = new ArrayList<>();
    private Map<String, List<QueryConfig>> queryLists = new HashMap<>();

    public SiddhiAppConfig() {
        queryLists.put(QueryInputType.WINDOW_FILTER_PROJECTION.toString(), new ArrayList<>());
        queryLists.put(QueryInputType.JOIN.toString(), new ArrayList<>());
        queryLists.put(QueryInputType.PATTERN.toString(), new ArrayList<>());
        queryLists.put(QueryInputType.SEQUENCE.toString(), new ArrayList<>());
    }

    /**
     * Returns Id for the next element id, after incrementing the final element count
     * @return      Id for the element
     */
    private String generateNextElementId() {
        return String.valueOf(++finalElementCount);
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
     * @param queryType         Key with which, the specific query list is denoted
     * @param queryLists        Map of query lists, where key is the type of query, and value is the specific query list
     * @param queryConfig       QueryConfig object
     */
    private void addQuery(QueryInputType queryType,
                          Map<String, List<QueryConfig>> queryLists,
                          QueryConfig queryConfig) {
        queryConfig.setId(generateNextElementId());
        queryLists.get(queryType.toString()).add(queryConfig);
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
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

    public void add(QueryConfig queryConfig) {
        // Categorize QueryConfig from its Input, and add QueryConfig to the relevant list
        QueryInputConfig queryInputConfig = queryConfig.getQueryInput();
        if (queryInputConfig instanceof WindowFilterProjectionConfig) {
//            addElement(windowFilterProjectionQueryList, queryConfig); TODO review & remove
            addQuery(QueryInputType.WINDOW_FILTER_PROJECTION, queryLists, queryConfig);
        } else if (queryInputConfig instanceof JoinConfig) {
//            addElement(joinQueryList, queryConfig); TODO review & remove
            addQuery(QueryInputType.JOIN, queryLists, queryConfig);
        } else {
            // TODO add pattern & sequences
            throw new IllegalArgumentException("Type of Query Input is unknown, for adding the Query");
        }
    }

    public void add(FunctionConfig functionConfig) {
        addElement(functionList, functionConfig);
    }

    public String getAppName() {
        return appName;
    }

    public String getAppDescription() {
        return appDescription;
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

    public Map<String, List<QueryConfig>> getQueryLists() {
        return queryLists;
    }

    public List<FunctionConfig> getFunctionList() {
        return functionList;
    }
}
