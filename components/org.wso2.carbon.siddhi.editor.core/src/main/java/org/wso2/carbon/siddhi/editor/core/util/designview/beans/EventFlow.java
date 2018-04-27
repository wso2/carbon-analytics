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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.Edge;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.SiddhiAppConfig;

import java.util.List;

/**
 * Represents the visual structure of a Siddhi app, for the design view
 */
public class EventFlow {
    private SiddhiAppConfig siddhiAppConfig;
    private List<Edge> edgeList;

    public EventFlow(SiddhiAppConfig siddhiAppConfig, List<Edge> edgeList) {
        this.siddhiAppConfig = siddhiAppConfig;
        this.edgeList = edgeList;
    }

    public SiddhiAppConfig getSiddhiAppConfig() {
        return siddhiAppConfig;
    }

    public void setSiddhiAppConfig(SiddhiAppConfig siddhiAppConfig) {
        this.siddhiAppConfig = siddhiAppConfig;
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }

    public void setEdgeList(List<Edge> edgeList) {
        this.edgeList = edgeList;
    }
}
