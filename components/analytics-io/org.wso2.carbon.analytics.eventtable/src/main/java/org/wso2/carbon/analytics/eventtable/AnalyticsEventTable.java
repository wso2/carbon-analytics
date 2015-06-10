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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.eventtable.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
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
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.Attribute;
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
    
    private boolean postInit;
        
    private String primaryKeys;

    @Override
    public void init(TableDefinition tableDefinition, ExecutionPlanContext executionPlanContext) {
        Annotation fromAnnotation = AnnotationHelper.getAnnotation(SiddhiConstants.ANNOTATION_FROM,
                tableDefinition.getAnnotations());
        this.tableDefinition = tableDefinition;
        this.tableName = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_TABLE_NAME);
        if (this.tableName == null) {
            throw new IllegalArgumentException("The property " + AnalyticsEventTableConstants.ANNOTATION_TABLE_NAME + 
                    " must be provided for analytics event tables.");
        }
        this.primaryKeys = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_PRIMARY_KEYS);
        try {
            this.tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        } catch (Throwable e) {
            this.tenantId = -1;
        }        
    }
    
    private void checkAndProcessPostInit() {
        if (!this.postInit) {
            try {
                this.postInit = true;
                this.processTableSchema();
            } catch (AnalyticsException e) {
                throw new IllegalStateException("Error in processing analytics event table schema: " + 
                        e.getMessage(), e);
            }
        }
    }
    
    private List<String> trimArray(String[] tokens) {
        List<String> result = new ArrayList<String>(tokens.length);
        for (int i = 0; i < tokens.length; i++) {
            result.add(tokens[i].trim());
        }
        return result;
    }
    
    private void processTableSchema() throws AnalyticsException {
        List<ColumnDefinition> cols = new ArrayList<ColumnDefinition>();
        ColumnType colType;
        for (Attribute attr : this.tableDefinition.getAttributeList()) {
            switch (attr.getType()) {
            case BOOL:
                colType = ColumnType.BOOLEAN;
                break;
            case DOUBLE:
                colType = ColumnType.DOUBLE;
                break;
            case FLOAT:
                colType = ColumnType.FLOAT;
                break;
            case INT:
                colType = ColumnType.INTEGER;
                break;
            case LONG:
                colType = ColumnType.LONG;
                break;
            case OBJECT:
                colType = ColumnType.STRING;
                break;
            case STRING:
                colType = ColumnType.STRING;
                break;
            default:
                colType = ColumnType.STRING;
                break;            
            }
            cols.add(new ColumnDefinition(attr.getName(), colType));
        }
        AnalyticsSchema schema = new AnalyticsSchema(cols, this.trimArray(primaryKeys.split(",")));
        ServiceHolder.getAnalyticsDataService().createTable(tenantId, tableName);
        ServiceHolder.getAnalyticsDataService().setTableSchema(tenantId, tableName, schema);
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
        this.checkAndProcessPostInit();
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
            this.attrs = attrs;
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
            checkAndProcessPostInit();
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
            checkAndProcessPostInit();
            AnalyticsEventTableUtils.putEvents(this.tenantId, this.tableName, this.attrs, updatingEventChunk);
        }
    
    }

}
