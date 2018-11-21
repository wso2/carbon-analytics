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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.AggregateByTimePeriod;
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

    public void setName(String name) {
        this.name = name;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setSelect(AttributesSelectionConfig select) {
        this.select = select;
    }

    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }

    public void setAggregateByAttribute(String aggregateByAttribute) {
        this.aggregateByAttribute = aggregateByAttribute;
    }

    public void setAggregateByTime(AggregateByTimePeriod aggregateByTimePeriod) {
        this.aggregateByTimePeriod = aggregateByTimePeriod;
    }

    public void setStore(StoreConfig store) {
        this.store = store;
    }

    public void setAnnotationList(List<String> annotationList) {
        this.annotationList = annotationList;
    }
}
