/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.eventtable;

import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.expression.Expression;

/**
 * Analytics table {@link Operator} implementation.
 */
public class AnalyticsTableOperator implements Operator {

    private int tenantId;
    
    private String tableName;
    
    private List<Attribute> attrs;
    
    private Expression expression;
    
    private MetaComplexEvent metaComplexEvent;
    
    private ExecutionPlanContext executionPlanContext;
    
    private List<VariableExpressionExecutor> variableExpressionExecutors;
    
    private Map<String, EventTable> eventTableMap;
    
    private int matchingStreamIndex;
    
    private long withinTime;
    
    public AnalyticsTableOperator(int tenantId, String tableName, List<Attribute> attrs, Expression expression, 
            MetaComplexEvent metaComplexEvent, ExecutionPlanContext executionPlanContext, 
            List<VariableExpressionExecutor> variableExpressionExecutors, 
            Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        this.tenantId = tenantId;
        this.tableName = tableName;
        this.expression = expression;
        this.metaComplexEvent = metaComplexEvent;
        this.executionPlanContext = executionPlanContext;
        this.variableExpressionExecutors = variableExpressionExecutors;
        this.eventTableMap = eventTableMap;
        this.matchingStreamIndex = matchingStreamIndex;
        this.withinTime = withinTime;
    }
    
    @Override
    public Finder cloneFinder() {
        return new AnalyticsTableOperator(this.tenantId, this.tableName, this.attrs, this.expression, this.metaComplexEvent, 
                this.executionPlanContext, this.variableExpressionExecutors, this.eventTableMap, 
                this.matchingStreamIndex, this.withinTime);
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Object candidateEvents) {
        return this.find(matchingEvent, candidateEvents, null) != null;
    }

    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, StreamEventCloner streamEventCloner) {
        Record record = AnalyticsEventTableUtils.getRecordWithEventValues(this.tenantId, this.tableName, 
                this.attrs, matchingEvent);
        return AnalyticsEventTableUtils.recordToStreamEvent(this.attrs, record);
    }

    @Override
    public void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Object candidateEvents) {
        throw new IllegalStateException("cannot delete records in analytics event tables.");
    }

    @Override
    public void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
        AnalyticsEventTableUtils.putEvents(this.tenantId, this.tableName, this.attrs, updatingEventChunk);
    }

}
