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

import java.util.ArrayList;
import java.util.List;

/**
 * Contains elements of a Siddhi app, for the design view
 */
public class SiddhiAppConfig {
    private int finalElementCount;

    private String appName;
    private String appDescription;

    // private List<TriggerConfig> triggerList;
    // private List<TriggerConfig> streamList;

//    private List<> aggregations;
//    private List<> functions;
//    private List<> partitions;
//    private List<> queries;
    private List<SinkConfig> sinkList;
    private List<SourceConfig> sourceList;
    private List<StreamConfig> streamList;
    private List<TableConfig> tableList;
    private List<TriggerConfig> triggerList;
    private List<WindowConfig> windowList;
    // TODO: 3/27/18 Other {Element}Lists

    // TODO: 3/28/18 For restricting unnecessary instantiation


    public SiddhiAppConfig() {
        finalElementCount = 0;
        appName = "";
        appDescription = "";
//        aggregations = new ArrayList<>();
//        functions = new ArrayList<>();
//        partitions = new ArrayList<>();
//        queries = new ArrayList<>();
        sinkList = new ArrayList<>();
        sourceList = new ArrayList<>();
        streamList = new ArrayList<>();
        tableList = new ArrayList<>();
        triggerList = new ArrayList<>();
        windowList = new ArrayList<>();
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAppDescription(String appDescription) {
        this.appDescription = appDescription;
    }

    public void add(SinkConfig sinkConfig) {
        sinkList.add(sinkConfig);
        finalElementCount++;
    }

    public void add(SourceConfig sourceConfig) {
        sourceList.add(sourceConfig);
        finalElementCount++;
    }

    public void add(StreamConfig streamConfig) {
        streamList.add(streamConfig);
        finalElementCount++;
    }

    public void add(TableConfig tableConfig) {
        tableList.add(tableConfig);
        finalElementCount++;
    }

    public void add(TriggerConfig triggerConfig) {
        triggerList.add(triggerConfig);
        finalElementCount++;
    }

    public void add(WindowConfig windowConfig) {
        windowList.add(windowConfig);
        finalElementCount++;
    }

    public int getFinalElementCount() {
        return finalElementCount;
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
}
