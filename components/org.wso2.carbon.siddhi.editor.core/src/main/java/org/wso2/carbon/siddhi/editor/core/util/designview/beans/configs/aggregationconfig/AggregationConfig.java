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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.aggregationconfig;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.annotationconfig.AnnotationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.SiddhiElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.aggregationconfig.aggregation.AggregateByConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.aggregationconfig.aggregation.SelectConfig;

import java.util.List;

/**
 * Represents a Siddhi Aggregation, for design view
 */
public class AggregationConfig extends SiddhiElementConfig {
    private String name;
    private String from;
    private List<SelectConfig> select;
    private List<String> groupBy;
    private AggregateByConfig aggregateBy;
    private StoreConfig store;
    private List<AnnotationConfig> annotationList;

    public AggregationConfig(String id,
                             String name,
                             String from,
                             List<SelectConfig> select,
                             List<String> groupBy,
                             AggregateByConfig aggregateBy,
                             StoreConfig store,
                             List<AnnotationConfig> annotationList) {
        super(id);
        this.name = name;
        this.from = from;
        this.select = select;
        this.groupBy = groupBy;
        this.aggregateBy = aggregateBy;
        this.store = store;
        this.annotationList = annotationList;
    }

    public String getName() {
        return name;
    }

    public String getFrom() {
        return from;
    }

    public List<SelectConfig> getSelect() {
        return select;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public AggregateByConfig getAggregateBy() {
        return aggregateBy;
    }

    public StoreConfig getStore() {
        return store;
    }

    public List<AnnotationConfig> getAnnotationList() {
        return annotationList;
    }
}
