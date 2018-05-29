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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.SiddhiElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;

import java.util.List;

/**
 * Represents a Siddhi Aggregation
 */
public class AggregationConfig extends SiddhiElementConfig {
    private String name;
    private String from;
    private AttributesSelectionConfig select;
    private List<String> groupBy;
    private String aggregateByAttribute;
    private AggregateByTimePeriod aggregateByTimePeriod;
    private StoreConfig store;
    private List<String> annotationList;

    public AggregationConfig(String id,
                             String name,
                             String from,
                             AttributesSelectionConfig select,
                             List<String> groupBy,
                             String aggregateByAttribute,
                             AggregateByTimePeriod aggregateByTimePeriod,
                             StoreConfig store,
                             List<String> annotationList) {
        super(id);
        this.name = name;
        this.from = from;
        this.select = select;
        this.groupBy = groupBy;
        this.aggregateByAttribute = aggregateByAttribute;
        this.aggregateByTimePeriod = aggregateByTimePeriod;
        this.store = store;
        this.annotationList = annotationList;
    }

    public String getName() {
        return name;
    }

    public String getFrom() {
        return from;
    }

    public AttributesSelectionConfig getSelect() {
        return select;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public String getAggregateByAttribute() {
        return aggregateByAttribute;
    }

    public AggregateByTimePeriod getAggregateByTimePeriod() {
        return aggregateByTimePeriod;
    }

    public StoreConfig getStore() {
        return store;
    }

    public List<String> getAnnotationList() {
        return annotationList;
    }
}
