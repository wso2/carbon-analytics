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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.SiddhiElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.QueryOutputConfig;

import java.util.List;

/**
 * Represents a Siddhi Query
 */
public class QueryConfig extends SiddhiElementConfig {
    private QueryInputConfig queryInput;
    private AttributesSelectionConfig select;
    private List<String> groupBy;
    private List<QueryOrderByConfig> orderBy;
    private long limit;
    private String having;
    private String outputRateLimit;
    private QueryOutputConfig queryOutput;
    private List<String> annotationList;

    public QueryInputConfig getQueryInput() {
        return queryInput;
    }

    public void setQueryInput(QueryInputConfig queryInput) {
        this.queryInput = queryInput;
    }

    public AttributesSelectionConfig getSelect() {
        return select;
    }

    public void setSelect(AttributesSelectionConfig select) {
        this.select = select;
    }

    public List<String> getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(List<String> groupBy) {
        this.groupBy = groupBy;
    }

    public List<QueryOrderByConfig> getOrderBy() {
        return orderBy;
    }

    public void setOrderBy(List<QueryOrderByConfig> orderBy) {
        this.orderBy = orderBy;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public String getHaving() {
        return having;
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public String getOutputRateLimit() {
        return outputRateLimit;
    }

    public void setOutputRateLimit(String outputRateLimit) {
        this.outputRateLimit = outputRateLimit;
    }

    public QueryOutputConfig getQueryOutput() {
        return queryOutput;
    }

    public void setQueryOutput(QueryOutputConfig queryOutput) {
        this.queryOutput = queryOutput;
    }

    public List<String> getAnnotationList() {
        return annotationList;
    }

    public void setAnnotationList(List<String> annotationList) {
        this.annotationList = annotationList;
    }
}
