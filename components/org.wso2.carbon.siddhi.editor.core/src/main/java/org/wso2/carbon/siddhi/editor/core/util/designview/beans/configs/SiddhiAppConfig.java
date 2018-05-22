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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.pattern.PatternQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.SequenceQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection.WindowFilterProjectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.sink.SinkConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.source.SourceConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains elements of a Siddhi app
 */
public class SiddhiAppConfig {
    private int finalElementCount = 0;

    private String appName = "";
    private String appDescription = "";

    private List<QueryConfig> windowFilterProjectionQueryList = new ArrayList<>();
    private List<QueryConfig> joinQueryList = new ArrayList<>();
    private List<QueryConfig> patternQueryList = new ArrayList<>();
    private List<QueryConfig> sequenceQueryList = new ArrayList<>();

    private List<SinkConfig> sinkList = new ArrayList<>();
    private List<SourceConfig> sourceList = new ArrayList<>();
    private List<StreamConfig> streamList = new ArrayList<>();
    private List<TableConfig> tableList = new ArrayList<>();
    private List<TriggerConfig> triggerList = new ArrayList<>();
    private List<WindowConfig> windowList = new ArrayList<>();
    private List<AggregationConfig> aggregationList = new ArrayList<>();
    private List<FunctionConfig> functionList = new ArrayList<>();
    // TODO: 3/27/18 Other {Element}Lists

    /**
     * Returns Id for the next element id, after incrementing the final element count
     * @return      Id for the element
     */
    private String generateNextElementId() {
        return String.valueOf(++finalElementCount);
    }

    /**
     * Adds a given generic type Siddhi ElementConfig, to the given list of the same generic type
     * @param elementList       List reference to which, the given element config should be added
     * @param elementConfig     Siddhi ElementConfig object
     * @param <T>               Type of the Siddhi ElementConfig object and the list
     */
    private <T> void addElement(List<T> elementList, T elementConfig) {
        ((SiddhiElementConfig) elementConfig).setId(generateNextElementId());
        elementList.add(elementConfig);
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public void add(SinkConfig sinkConfig) {
        addElement(sinkList, sinkConfig);
    }

    public void add(SourceConfig sourceConfig) {
        addElement(sourceList, sourceConfig);
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
            addElement(windowFilterProjectionQueryList, queryConfig);
        } else if (queryInputConfig instanceof JoinConfig) {
            addElement(joinQueryList, queryConfig);
        } else if (queryInputConfig instanceof PatternQueryConfig) {
            addElement(patternQueryList, queryConfig);
        } else if (queryInputConfig instanceof SequenceQueryConfig) {
            addElement(sequenceQueryList, queryConfig);
        } else {
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

    public List<SinkConfig> getSinkList() {
        return sinkList;
    }

    public List<SourceConfig> getSourceList() {
        return sourceList;
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

    public List<QueryConfig> getWindowFilterProjectionQueryList() {
        return windowFilterProjectionQueryList;
    }

    public List<QueryConfig> getJoinQueryList() {
        return joinQueryList;
    }

    public List<QueryConfig> getPatternQueryList() {
        return patternQueryList;
    }

    public List<QueryConfig> getSequenceQueryList() {
        return sequenceQueryList;
    }

    public List<FunctionConfig> getFunctionList() {
        return functionList;
    }
}
