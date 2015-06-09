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

import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.MetaComplexEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.core.util.SiddhiConstants;

/**
 * This class implements the Siddhi event table interface {@link EventTable} for analytics tables.
 */
public class AnalyticsEventTable implements EventTable {
    
    private String tableName;
        
    private TableDefinition tableDefinition;
    
    private int tenantId;

    @Override
    public void init(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) {
        Annotation fromAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_FROM,
                tableDefinition.getAnnotations());
        this.tableDefinition = tableDefinition;
        this.tableName = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_TABLE_NAME);
        if (this.tableName == null) {
            String streamName = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_STREAM_NAME);
            if (streamName == null) {
                throw new IllegalArgumentException("The properties either " + AnalyticsEventTableConstants.ANNOTATION_STREAM_NAME + " or " + 
                        AnalyticsEventTableConstants.ANNOTATION_TABLE_NAME + " must be provided for analytics event tables.");
            }
            this.tableName = GenericUtils.streamToTableName(streamName);
        }
        this.tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }
    
    @Override
    public Finder constructFinder(Expression expression, MetaComplexEvent metaComplexEvent, 
            ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, 
            Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return new AnalyticsTableOperator(this.tenantId, this.tableName, this.tableDefinition.getAttributeList(), 
                expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, 
                eventTableMap, matchingStreamIndex, withinTime);
    }

    @Override
    public StreamEvent find(ComplexEvent matchingEvent, Finder finder) {
        return finder.find(matchingEvent, null, null);
    }

    @Override
    public void add(ComplexEventChunk<StreamEvent> addingEventChunk) {
        addingEventChunk.reset();
        AnalyticsEventTableUtils.putEvents(this.tenantId, this.tableName, 
                this.tableDefinition.getAttributeList(), addingEventChunk);
    }

    @Override
    public Operator constructOperator(Expression expression, MetaComplexEvent metaComplexEvent, 
            ExecutionPlanContext executionPlanContext, List<VariableExpressionExecutor> variableExpressionExecutors, 
            Map<String, EventTable> eventTableMap, int matchingStreamIndex, long withinTime) {
        return new AnalyticsTableOperator(this.tenantId, this.tableName, this.tableDefinition.getAttributeList(), 
                expression, metaComplexEvent, executionPlanContext, variableExpressionExecutors, 
                eventTableMap, matchingStreamIndex, withinTime);
    }

    @Override
    public boolean contains(ComplexEvent matchingEvent, Finder finder) {
        return finder.contains(matchingEvent, null);
    }

    @Override
    public void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Operator operator) {
        operator.delete(deletingEventChunk, null);
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Operator operator, int[] mappingPosition) {
        operator.update(updatingEventChunk, null, null);
    }

}
