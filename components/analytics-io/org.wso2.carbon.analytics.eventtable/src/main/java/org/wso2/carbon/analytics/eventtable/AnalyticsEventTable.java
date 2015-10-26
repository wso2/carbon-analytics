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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.dataservice.commons.Constants;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
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
import org.wso2.siddhi.core.event.state.MetaStateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.Operator;
import org.wso2.siddhi.core.util.parser.ExpressionParser;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.Attribute;
import org.wso2.siddhi.query.api.definition.TableDefinition;
import org.wso2.siddhi.query.api.expression.Expression;
import org.wso2.siddhi.query.api.expression.Variable;
import org.wso2.siddhi.query.api.expression.condition.And;
import org.wso2.siddhi.query.api.expression.condition.Compare;
import org.wso2.siddhi.query.api.expression.condition.Or;
import org.wso2.siddhi.query.api.expression.constant.BoolConstant;
import org.wso2.siddhi.query.api.expression.constant.Constant;
import org.wso2.siddhi.query.api.expression.constant.DoubleConstant;
import org.wso2.siddhi.query.api.expression.constant.FloatConstant;
import org.wso2.siddhi.query.api.expression.constant.IntConstant;
import org.wso2.siddhi.query.api.expression.constant.LongConstant;
import org.wso2.siddhi.query.api.expression.constant.StringConstant;
import org.wso2.siddhi.query.api.util.AnnotationHelper;
import org.wso2.siddhi.core.util.SiddhiConstants;

/**
 * This class implements the Siddhi event table interface {@link EventTable} for analytics tables.
 */
public class AnalyticsEventTable implements EventTable {
    
    private static final Log log = LogFactory.getLog(AnalyticsEventTable.class);
    
    private String tableName;
        
    private TableDefinition tableDefinition;
    
    private int tenantId;
    
    private boolean postInit;
        
    private String primaryKeys;
    
    private String indices;
    
    private boolean mergeSchema;
    
    private boolean waitForIndexing;
    
    private int maxSearchResultCount;
    
    private boolean indicesAvailable;

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
        this.indices = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_INDICES);
        String mergeSchemaProp = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_MERGE_SCHEMA);
        if (mergeSchemaProp != null) {
            mergeSchemaProp = mergeSchemaProp.trim();
            this.mergeSchema = Boolean.parseBoolean(mergeSchemaProp);
        } else {
            this.mergeSchema = true;
        }
        String waitForIndexingProp = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_WAIT_FOR_INDEXING);
        if (waitForIndexingProp != null) {
            waitForIndexingProp = waitForIndexingProp.trim();
            this.waitForIndexing = Boolean.parseBoolean(waitForIndexingProp);
        } else {
            this.waitForIndexing = false;
        }
        String maxSearchResultCountProp = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_MAX_SEARCH_RESULT_COUNT);
        if (maxSearchResultCountProp != null) {
            maxSearchResultCountProp = maxSearchResultCountProp.trim();
            this.maxSearchResultCount = Integer.parseInt(maxSearchResultCountProp);
        } else {
            this.maxSearchResultCount = -1;
        }
        try {
            this.tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        } catch (Throwable e) {
            /* this would never happens in real server runtime, caught for unit tests */
            this.tenantId = -1;
        }        
    }
    
    private void checkAndProcessPostInit() {
        if (!this.postInit) {
            try {
                synchronized (this) {
                    if (!this.postInit) {
                        this.processTableSchema();
                        this.postInit = true;
                    }
                }
            } catch (AnalyticsException e) {
                throw new IllegalStateException("Error in processing analytics event table schema: " + 
                        e.getMessage(), e);
            }
        }
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
        List<String> primaryKeys = AnalyticsDataServiceUtils.tokenizeAndTrimToList(this.primaryKeys, ",");
        ServiceHolder.getAnalyticsDataService().createTable(this.tenantId, this.tableName);
        AnalyticsSchema schema;
        if (this.mergeSchema) {
            schema = ServiceHolder.getAnalyticsDataService().getTableSchema(this.tenantId, this.tableName);
        } else {
            schema = new AnalyticsSchema();
        }
        schema = AnalyticsDataServiceUtils.createMergedSchema(schema, primaryKeys, cols, 
                AnalyticsDataServiceUtils.tokenizeAndTrimToList(this.indices, ","));
        if (schema.getIndexedColumns().size() > 0) {
            this.indicesAvailable = true;
        }
        ServiceHolder.getAnalyticsDataService().setTableSchema(this.tenantId, this.tableName, schema);
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
    
    private void waitForIndexing(int tenantId, String tableName) {
        try {
            ServiceHolder.getAnalyticsDataService().waitForIndexing(this.tenantId, this.tableName, -1);
        } catch (Exception e) {
            throw new IllegalStateException("Error in waiting for indexing in analytics event table: " + 
                    e.getMessage(), e);
        }        
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void add(ComplexEventChunk addingEventChunk) {
        this.checkAndProcessPostInit();
        addingEventChunk.reset();
        AnalyticsEventTableUtils.putEvents(this.tenantId, this.tableName, 
                this.tableDefinition.getAttributeList(), addingEventChunk);
        this.checkAndWaitForIndexing();
    }
    
    private void checkAndWaitForIndexing() {
        if (this.waitForIndexing && this.indicesAvailable) {
            this.waitForIndexing(this.tenantId, this.tableName);
        }
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

    @SuppressWarnings("rawtypes")
    @Override
    public void delete(ComplexEventChunk deletingEventChunk, Operator operator) {
        operator.delete(deletingEventChunk, null);
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void update(ComplexEventChunk updatingEventChunk, Operator operator, int[] mappingPosition) {
        operator.update(updatingEventChunk, null, null);
    }
    
    /**
     * Analytics table {@link Operator} implementation.
     */
    public class AnalyticsTableOperator implements Operator {
    
        private static final String LUCENE_QUERY_PARAM = "e267ba83-0c77-4e0d-9c5f-cd3a31dbe2d3";

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
        
        private String luceneQuery;
        
        private boolean pkMatchCompatible = true;
        
        private boolean returnAllRecords = true;
        
        private Set<String> primaryKeySet;
        
        private Set<String> candidatePrimaryKeySet;
        
        private Set<String> indexedKeySet;
        
        private Set<String> mentionedFields;
                
        private List<ExpressionExecutor> expressionExecs = new ArrayList<ExpressionExecutor>();
        
        private MetaStateEvent metaStateEvent;
        
        private int paramIndex = 0;
        
        private Set<String> eventTableRefs = new HashSet<String>();
        
        private List<Attribute> outputAttrs;
        
        private boolean operatorInit;
        
        private Map<String, Object> constantRHSValues;
        
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
            this.primaryKeySet = new HashSet<String>();
            this.candidatePrimaryKeySet = new HashSet<>();
            this.indexedKeySet = new HashSet<String>();
            this.mentionedFields = new HashSet<String>();
            this.constantRHSValues = new HashMap<>();
            this.initMetaStateEvent();
            /* first parse for evaluating the query, since expression evaluation cannot be
             * done in a lazy manner */
            this.luceneQueryFromExpression(this.expression, true);
        }
        
        private void initExpressionLogic() {
            if (this.operatorInit) {
                return;
            }
            synchronized (this) {
                if (this.operatorInit) {
                    return;
                }
                checkAndProcessPostInit();
                try {
                    AnalyticsSchema schema = ServiceHolder.getAnalyticsDataService().getTableSchema(this.tenantId, this.tableName);
                    List<String> primaryKeys = schema.getPrimaryKeys();
                    if (primaryKeys != null) {
                        this.primaryKeySet.addAll(primaryKeys);
                    }
                    Set<String> indices = schema.getIndexedColumns().keySet();
                    if (indices != null) {
                        this.indexedKeySet.addAll(indices);
                    }
                } catch (AnalyticsException e) {
                    throw new IllegalStateException("Unable to lookup table schema: " + e.getMessage(), e);
                }
                this.luceneQuery = this.luceneQueryFromExpression(this.expression, false).toString();
                Set<String> nonIndixedFields = new HashSet<String>(this.mentionedFields);
                nonIndixedFields.removeAll(this.indexedKeySet);
                if (!this.pkMatchCompatible && nonIndixedFields.size() > 0) {
                    throw new IllegalStateException("The table [" + this.tenantId + ", " + this.tableName + 
                            "] requires the field(s): " + nonIndixedFields + 
                            " to be indexed for the given analytics event table based query to execute.");
                }
                this.operatorInit = true;
            }
        }
        
        private void initMetaStateEvent() {
            if (this.metaComplexEvent instanceof MetaStreamEvent) {
                this.metaStateEvent = new MetaStateEvent(1);
                this.metaStateEvent.addEvent(((MetaStreamEvent) this.metaComplexEvent));
            } else {
                MetaStreamEvent[] metaStreamEvents = ((MetaStateEvent) this.metaComplexEvent).getMetaStreamEvents();
                for (int candidateEventPosition = 0; candidateEventPosition < metaStreamEvents.length; candidateEventPosition++) {
                    MetaStreamEvent metaStreamEvent = metaStreamEvents[candidateEventPosition];
                    if (candidateEventPosition != this.matchingStreamIndex
                            && metaStreamEvent.getLastInputDefinition().equalsIgnoreAnnotations(tableDefinition)) {
                        this.metaStateEvent = ((MetaStateEvent) this.metaComplexEvent);
                        break;
                    }
                }
                if (this.metaStateEvent == null) {
                    this.metaStateEvent = new MetaStateEvent(metaStreamEvents.length + 1);
                    for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
                        this.metaStateEvent.addEvent(metaStreamEvent);
                    }
                }
            }
            MetaStreamEvent[] metaStreamEvents = metaStateEvent.getMetaStreamEvents();
            for (MetaStreamEvent metaStreamEvent : metaStreamEvents) {
                String referenceId = metaStreamEvent.getInputReferenceId();
                AbstractDefinition abstractDefinition = metaStreamEvent.getInputDefinitions().get(0);
                if (this.outputAttrs == null) {
                    this.outputAttrs = metaStreamEvent.getOutputData();
                }
                if (!abstractDefinition.getId().trim().equals("")) {
                    if (abstractDefinition instanceof TableDefinition) {
                        this.eventTableRefs.add(abstractDefinition.getId());
                        if (referenceId != null) {
                            this.eventTableRefs.add(referenceId);
                        }
                    }
                }
            }            
            if (tableDefinition instanceof TableDefinition) {
                this.eventTableRefs.add(tableDefinition.getId());
            }            
        }
        
        private boolean checkPrimaryKeyCompatibleWithCandidates() {
            return this.primaryKeySet.equals(this.candidatePrimaryKeySet);
        }
        
        private ColumnType getFieldType(String field, boolean firstPass) {
            if (firstPass) {
                return ColumnType.STRING;
            }
            try {
                AnalyticsSchema schema = ServiceHolder.getAnalyticsDataService().getTableSchema(this.tenantId, this.tableName);
                ColumnDefinition column = schema.getColumns().get(field);
                if (column != null) {
                    return column.getType();
                }
                return ColumnType.STRING;
            } catch (AnalyticsException e) {
                throw new IllegalStateException("Error in checking if a field is string: " + e.getMessage(), e);
            }
        }
        
        private Object luceneQueryFromExpression(Expression expr, boolean firstPass) {
            if (expr instanceof And) {
                this.returnAllRecords = false;
                And andExpr = (And) expr;
                return "(" + luceneQueryFromExpression(andExpr.getLeftExpression(), firstPass) + 
                        " AND " + luceneQueryFromExpression(andExpr.getRightExpression(), firstPass) + ")";
            } else if (expr instanceof Or) {
                this.returnAllRecords = false;
                Or orExpr = (Or) expr;
                this.pkMatchCompatible = false;
                return "(" + luceneQueryFromExpression(orExpr.getLeftExpression(), firstPass) + 
                        " OR " + luceneQueryFromExpression(orExpr.getRightExpression(), firstPass) + ")";
            } else if (expr instanceof Compare) {
                this.returnAllRecords = false;
                Compare compare = (Compare) expr;
                Object origLHS = luceneQueryFromExpression(compare.getLeftExpression(), firstPass);
                Object origRHS = luceneQueryFromExpression(compare.getRightExpression(), firstPass);
                String field;
                Object rhs;
                org.wso2.siddhi.query.api.expression.condition.Compare.Operator operator = compare.getOperator();
                if (origRHS.toString().startsWith(LUCENE_QUERY_PARAM)) {
                    field = origLHS.toString();
                    rhs = origRHS;
                } else {
                    field = origRHS.toString();
                    rhs = origLHS;
                    switch (operator) {
                    case GREATER_THAN:
                        operator = org.wso2.siddhi.query.api.expression.condition.Compare.Operator.LESS_THAN;
                        break;
                    case GREATER_THAN_EQUAL:
                        operator = org.wso2.siddhi.query.api.expression.condition.Compare.Operator.LESS_THAN_EQUAL;
                        break;
                    case LESS_THAN:
                        operator = org.wso2.siddhi.query.api.expression.condition.Compare.Operator.GREATER_THAN;
                        break;
                    case LESS_THAN_EQUAL:
                        operator = org.wso2.siddhi.query.api.expression.condition.Compare.Operator.GREATER_THAN_EQUAL;
                        break;
                    default:
                        break;
                    }
                }
                switch (operator) {
                case CONTAINS:
                    this.pkMatchCompatible = false;
                    this.mentionedFields.add(field);
                    return "(" + field + ": " + this.toLuceneQueryRHSValue(rhs) + ")";
                case EQUAL:
                    if (!firstPass) {
                        this.candidatePrimaryKeySet.add(field);
                    }
                    this.mentionedFields.add(field);
                    this.constantRHSValues.put(field, rhs);
                    return "(" + (this.getFieldType(field, firstPass).equals(ColumnType.STRING) ? 
                            Constants.NON_TOKENIZED_FIELD_PREFIX : "") + 
                            field + ": " + this.toLuceneQueryRHSValue(rhs) + ")";                
                case GREATER_THAN:
                    this.pkMatchCompatible = false;
                    this.mentionedFields.add(field);
                    return "(" + field + ": {" + this.toLuceneQueryRHSValue(rhs) + " TO " + 
                            this.rangeExtentValueForValueType(this.getFieldType(field, firstPass), true) + "]" + ")";
                case GREATER_THAN_EQUAL:
                    this.pkMatchCompatible = false;
                    this.mentionedFields.add(field);
                    return "(" + field + ": [" + this.toLuceneQueryRHSValue(rhs) + " TO " + 
                            this.rangeExtentValueForValueType(this.getFieldType(field, firstPass), true) + "]" + ")";
                case INSTANCE_OF:
                    this.pkMatchCompatible = false;
                    throw new IllegalStateException("INSTANCE_OF is not supported in analytics event tables.");
                case LESS_THAN:
                    this.pkMatchCompatible = false;
                    this.mentionedFields.add(field);
                    return "(" + field + ": [" + this.rangeExtentValueForValueType(this.getFieldType(field, firstPass), false) + " TO " + 
                            this.toLuceneQueryRHSValue(rhs) + "}" + ")";
                case LESS_THAN_EQUAL:
                    this.pkMatchCompatible = false;
                    this.mentionedFields.add(field);
                    return "(" + field + ": [" + this.rangeExtentValueForValueType(this.getFieldType(field, firstPass), false) + " TO " + 
                            this.toLuceneQueryRHSValue(rhs) + "]" + ")";
                case NOT_EQUAL:
                    this.pkMatchCompatible = false;
                    this.mentionedFields.add(field);
                    return "(-" + Constants.NON_TOKENIZED_FIELD_PREFIX + field + ": " + 
                    luceneQueryFromExpression(compare.getRightExpression(), firstPass) + ")";
                default:
                    return true;
                }
            } else if (expr instanceof Constant) {
                return this.returnConstantValue((Constant) expr);
            } else if (expr instanceof Variable) {
                Variable var = (Variable) expr;
                if (eventTableRefs.contains(var.getStreamId())) {
                    return var.getAttributeName();
                } else {
                    if (firstPass) {
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expr,
                                this.metaStateEvent, this.matchingStreamIndex, this.eventTableMap, 
                                this.variableExpressionExecutors, this.executionPlanContext, false, 0);
                        this.expressionExecs.add(expressionExecutor);
                        return LUCENE_QUERY_PARAM;
                    } else {
                        return LUCENE_QUERY_PARAM + (this.paramIndex++);
                    }
                }
            } else {
                return true;
            }
        }
        
        private String toLuceneQueryRHSValue(Object value) {
            if (value == null) {
                value = AnalyticsDataIndexer.NULL_INDEX_VALUE;
            }
            if (value instanceof String && !value.toString().startsWith(LUCENE_QUERY_PARAM)) {
                return "\"" + value + "\"";
            }
            if (value instanceof Boolean) {
                return "\"" + value + "\"";
            } else {
                return value.toString();
            }
        }
        
        private String rangeExtentValueForValueType(ColumnType type, boolean max) {
            switch (type) {
            case BOOLEAN:
                return "*";
            case DOUBLE:
                if (max) {
                    return Double.toString(Double.MAX_VALUE);
                } else {
                    return Double.toString(-Double.MAX_VALUE);
                }
            case FLOAT:
                if (max) {
                    return Float.toString(Float.MAX_VALUE);
                } else {
                    return Float.toString(-Float.MAX_VALUE);
                }
            case INTEGER:
                if (max) {
                    return Integer.toString(Integer.MAX_VALUE);
                } else {
                    return Integer.toString(Integer.MIN_VALUE);
                }
            case LONG:
                if (max) {
                    return Long.toString(Long.MAX_VALUE);
                } else {
                    return Long.toString(Long.MIN_VALUE);
                }
            case STRING:
                return "*";
            default:
                return "*";            
            }
        }
        
        private Object returnConstantValue(Constant constant) {
            if (constant instanceof IntConstant) {
                return ((IntConstant) constant).getValue();
            } else if (constant instanceof LongConstant) {
                return ((LongConstant) constant).getValue();
            } else if (constant instanceof FloatConstant) {
                return ((FloatConstant) constant).getValue();
            } else if (constant instanceof DoubleConstant) {
                return ((DoubleConstant) constant).getValue();
            } else if (constant instanceof BoolConstant) {
                return ((BoolConstant) constant).getValue();
            } else if (constant instanceof StringConstant) {
                return ((StringConstant) constant).getValue();
            } else {
                return constant.toString();
            }
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
        public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, 
                StreamEventCloner streamEventCloner) {
            this.initExpressionLogic();
            List<Record> records = this.findRecords(matchingEvent, candidateEvents, streamEventCloner);
            return AnalyticsEventTableUtils.recordsToStreamEvent(this.attrs, records);
        }
        
        private List<Record> findRecords(ComplexEvent matchingEvent, Object candidateEvents, 
                StreamEventCloner streamEventCloner) {
            List<Record> records;
            if (this.pkMatchCompatible) {
                /* if no one else complained, check with primary key candidates */
                this.pkMatchCompatible = this.checkPrimaryKeyCompatibleWithCandidates();
            }
            if (this.returnAllRecords) {
                records = AnalyticsEventTableUtils.getAllRecords(this.tenantId, this.tableName);
            } else if (this.pkMatchCompatible) {
                Record record = AnalyticsEventTableUtils.getRecordWithEventValues(this.tenantId, this.tableName, 
                        this.attrs, matchingEvent, this.constantRHSValues);
                if (record == null) {
                    records = new ArrayList<>(0);
                } else {
                    records = Arrays.asList(record);
                }
            } else {
                records = this.executeLuceneQuery(matchingEvent);
            }
            return records;
        }
        
        private String getTranslatedLuceneQuery(ComplexEvent matchingEvent) {
            String query = this.luceneQuery;
            String value;
            for (int i = 0; i < this.expressionExecs.size(); i++) {
                value = this.toLuceneQueryRHSValue(this.expressionExecs.get(i).execute(matchingEvent));
                query = query.replace(LUCENE_QUERY_PARAM + i, value);
            }
            return query;
        }
        
        private List<Record> executeLuceneQuery(ComplexEvent matchingEvent) {
            try {
                AnalyticsDataService service = ServiceHolder.getAnalyticsDataService();
                String query = this.getTranslatedLuceneQuery(matchingEvent);
                if (log.isDebugEnabled()) {
                    log.debug("Analytics Table Search Query: '" + query + "'");
                }
                int count = maxSearchResultCount;
                if (count == -1) {
                    count = service.searchCount(this.tenantId, this.tableName, query);
                }
                if (count == 0) {
                    return new ArrayList<Record>(0);
                }
                List<SearchResultEntry> searchResults = service.search(this.tenantId, this.tableName, query, 0, count);
                List<String> ids = new ArrayList<String>();
                for (SearchResultEntry entry : searchResults) {
                    ids.add(entry.getId());
                }
                AnalyticsDataResponse resp = service.get(this.tenantId, this.tableName, 1, null, ids);
                return AnalyticsDataServiceUtils.listRecords(service, resp);
            } catch (AnalyticsException e) {
                throw new IllegalStateException("Error in executing lucene query: " + e.getMessage(), e);
            }
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void delete(ComplexEventChunk deletingEventChunk, Object candidateEvents) {
            this.initExpressionLogic();
            deletingEventChunk.reset();
            while (deletingEventChunk.hasNext()) {
                List<Record> records = this.findRecords(deletingEventChunk.next(), candidateEvents, null);
                AnalyticsEventTableUtils.deleteRecords(this.tenantId, this.tableName, records);
            }
            checkAndWaitForIndexing();
        }

        @SuppressWarnings("rawtypes")
        @Override
        public void update(ComplexEventChunk updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
            this.initExpressionLogic();
            updatingEventChunk.reset();
            ComplexEvent event;
            List<Record> records;
            try {
                while (updatingEventChunk.hasNext()) {
                    event = updatingEventChunk.next();
                    records = this.findRecords(event, candidateEvents, null);
                    this.updateRecordsWithEvent(records, event);
                    ServiceHolder.getAnalyticsDataService().put(records);
                }
                checkAndWaitForIndexing();
            } catch (AnalyticsException e) {
                throw new IllegalStateException("Error in executing update query: " + e.getMessage(), e);
            }
        }
        
        private void updateRecordsWithEvent(List<Record> records, ComplexEvent event) {
            Map<String, Object> values = AnalyticsEventTableUtils.streamEventToRecordValues(this.tenantId, 
                    this.tableName, this.outputAttrs, event);
            for (Record record : records) {
                for (Entry<String, Object> entry : values.entrySet()) {
                    if (record.getValues().containsKey(entry.getKey())) {
                        record.getValues().put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }
    
    }

}
	