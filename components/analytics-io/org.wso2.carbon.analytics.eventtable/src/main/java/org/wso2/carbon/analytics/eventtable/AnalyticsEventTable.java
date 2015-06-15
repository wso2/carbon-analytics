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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
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
    
    private String tableName;
        
    private TableDefinition tableDefinition;
    
    private int tenantId;
    
    private boolean postInit;
        
    private String primaryKeys;
    
    private String indices;
    
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
        if (this.primaryKeys == null) {
            this.primaryKeys = "";
        }
        this.indices = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_INDICES);
        if (this.indices == null) {
            this.indices = "";
        }
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
        AnalyticsSchema schema = new AnalyticsSchema(cols, this.trimArray(this.primaryKeys.split(",")));
        Map<String, ColumnDefinition> indexedColumns = new HashMap<String, ColumnDefinition>();
        for (String index : this.indices.split(",")) {
            this.processIndex(indexedColumns, index.trim());
        }
        schema.setIndexedColumns(indexedColumns);
        ServiceHolder.getAnalyticsDataService().createTable(this.tenantId, this.tableName);
        ServiceHolder.getAnalyticsDataService().setTableSchema(this.tenantId, this.tableName, schema);
    }
    
    private void processIndex(Map<String, ColumnDefinition> indexedColumns, String index) {
        String[] tokens = index.split(" ");
        String name = tokens[0].trim();
        ColumnDefinition column = indexedColumns.get(name);
        if (column != null) {
            column.setIndexed(true);
            Set<String> options = new HashSet<String>();
            for (int i = 1; i < tokens.length; i++) {
                options.add(tokens[i]);
            }
            if (options.contains(AnalyticsEventTableConstants.OPTION_SCORE_PARAM)) {
                column.setScoreParam(true);
            }
        }
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
        
        private String luceneQuery;
        
        private boolean pkMatchCompatible = true;
        
        private boolean returnAllRecords = true;
        
        private Set<String> primaryKeySet;
        
        private Set<String> indexedKeySet;
        
        private Set<String> mentionedFields;
        
        private boolean initExprLogic;
        
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
            this.indexedKeySet = new HashSet<String>();
            this.mentionedFields = new HashSet<String>();
        }
        
        private void initExpressionLogic() {
            if (this.initExprLogic) {
                return;
            }
            this.initExprLogic = true;
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
            this.luceneQuery = this.luceneQueryFromExpression(this.expression).toString();
            Set<String> nonIndixedFields = new HashSet<String>(this.mentionedFields);
            nonIndixedFields.removeAll(this.indexedKeySet);
            if (nonIndixedFields.size() > 0) {
                throw new IllegalStateException("The table [" + this.tenantId + ", " + this.tableName + 
                        "] requires the field(s): " + nonIndixedFields + 
                        " to be indexed for the given analytics event table based query to execute.");
            }
        }
        
        private void checkPrimaryKeyUsage(String field) {
            if (!this.primaryKeySet.contains(field)) {
                this.pkMatchCompatible = false;
            }
        }
        
        private Object luceneQueryFromExpression(Expression expr) {
            if (expr instanceof And) {
                this.returnAllRecords = false;
                And andExpr = (And) expr;
                return "(" + luceneQueryFromExpression(andExpr.getLeftExpression()) + 
                        " AND " + luceneQueryFromExpression(andExpr.getRightExpression()) + ")";
            } else if (expr instanceof Or) {
                this.returnAllRecords = false;
                Or orExpr = (Or) expr;
                this.pkMatchCompatible = false;
                return "(" + luceneQueryFromExpression(orExpr.getLeftExpression()) + 
                        " OR " + luceneQueryFromExpression(orExpr.getRightExpression()) + ")";
            } else if (expr instanceof Compare) {
                this.returnAllRecords = false;
                Compare compare = (Compare) expr;
                Object rhv;
                String field = luceneQueryFromExpression(compare.getLeftExpression()).toString();
                switch (compare.getOperator()) {
                case CONTAINS:
                    this.pkMatchCompatible = false;
                    this.mentionedFields.add(field);
                    return "(" + field + ": " + luceneQueryFromExpression(compare.getRightExpression()) + ")";
                case EQUAL:
                    this.checkPrimaryKeyUsage(field);
                    this.mentionedFields.add(field);
                    return "(" + field + ": " + luceneQueryFromExpression(compare.getRightExpression()) + ")";
                case GREATER_THAN:
                    this.pkMatchCompatible = false;
                    rhv = luceneQueryFromExpression(compare.getRightExpression());
                    this.mentionedFields.add(field);
                    return "(" + field + ": {" + this.toLuceneQueryRHSValue(rhv) + " TO " + 
                            this.rangeExtentValueForValueType(rhv, true) + "]" + ")";
                case GREATER_THAN_EQUAL:
                    this.pkMatchCompatible = false;
                    rhv = luceneQueryFromExpression(compare.getRightExpression());
                    this.mentionedFields.add(field);
                    return "(" + field + ": [" + this.toLuceneQueryRHSValue(rhv) + " TO " + 
                            this.rangeExtentValueForValueType(rhv, true) + "]" + ")";
                case INSTANCE_OF:
                    this.pkMatchCompatible = false;
                    throw new IllegalStateException("INSTANCE_OF is not supported in analytics event tables.");
                case LESS_THAN:
                    this.pkMatchCompatible = false;
                    rhv = luceneQueryFromExpression(compare.getRightExpression());
                    this.mentionedFields.add(field);
                    return "(" + field + ": [" + this.rangeExtentValueForValueType(rhv, false) + " TO " + 
                            this.toLuceneQueryRHSValue(rhv) + "}" + ")";
                case LESS_THAN_EQUAL:
                    this.pkMatchCompatible = false;
                    rhv = luceneQueryFromExpression(compare.getRightExpression());
                    this.mentionedFields.add(field);
                    return "(" + field + ": [" + this.rangeExtentValueForValueType(rhv, false) + " TO " + 
                            this.toLuceneQueryRHSValue(rhv) + "]" + ")";
                case NOT_EQUAL:
                    this.pkMatchCompatible = false;
                    this.mentionedFields.add(field);
                    return "(-" + field + ": " + luceneQueryFromExpression(compare.getRightExpression()) + ")";
                default:
                    return true;                
                }
            } else if (expr instanceof Constant) {
                return this.returnConstantValue((Constant) expr);
            } else {
                return true;
            }
        }
        
        private String toLuceneQueryRHSValue(Object value) {
            if (value instanceof String || value instanceof Boolean) {
                return "\"" + value + "\"";
            } else {
                return value.toString();
            }
        }
        
        private String rangeExtentValueForValueType(Object value, boolean max) {
            if (value instanceof Integer) {
                if (max) {
                    return Integer.toString(Integer.MAX_VALUE);
                } else {
                    return Integer.toString(Integer.MIN_VALUE);
                }
            } else if (value instanceof Long) {
                if (max) {
                    return Long.toString(Long.MAX_VALUE);
                } else {
                    return Long.toString(Long.MIN_VALUE);
                }
            } else if (value instanceof Float) {
                if (max) {
                    return Float.toString(Float.MAX_VALUE);
                } else {
                    return Float.toString(Float.MIN_VALUE);
                }
            } else if (value instanceof Double) {
                if (max) {
                    return Double.toString(Double.MAX_VALUE);
                } else {
                    return Double.toString(Double.MIN_VALUE);
                }
            } else if (value instanceof Boolean) {
                return "*";
            } else if (value instanceof String) {
                return "*";
            } else {
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
        public StreamEvent find(ComplexEvent matchingEvent, Object candidateEvents, StreamEventCloner streamEventCloner) {
            this.initExpressionLogic();
            if (this.returnAllRecords) {
                List<Record> records = AnalyticsEventTableUtils.getAllRecords(this.tenantId, this.tableName);
                return AnalyticsEventTableUtils.recordsToStreamEvent(this.attrs, records);
            } else if (this.pkMatchCompatible) {
                Record record = AnalyticsEventTableUtils.getRecordWithEventValues(this.tenantId, this.tableName, 
                        this.attrs, matchingEvent);
                return AnalyticsEventTableUtils.recordToStreamEvent(this.attrs, record);
            } else {
                List<Record> records = this.executeLuceneQuery();
                return AnalyticsEventTableUtils.recordsToStreamEvent(this.attrs, records);
            }
        }
        
        private List<Record> executeLuceneQuery() {
            try {
                AnalyticsDataService service = ServiceHolder.getAnalyticsDataService();
                List<SearchResultEntry> searchResults = service.search(this.tenantId, this.tableName, this.luceneQuery, 0, -1);
                List<String> ids = new ArrayList<String>();
                for (SearchResultEntry entry : searchResults) {
                    ids.add(entry.getId());
                }
                RecordGroup[] rgs = service.get(this.tenantId, this.tableName, 1, null, ids);
                return GenericUtils.listRecords(service, rgs);
            } catch (AnalyticsException e) {
                throw new IllegalStateException("Error in executing lucene query: " + e.getMessage(), e);
            }
        }
    
        @Override
        public void delete(ComplexEventChunk<StreamEvent> deletingEventChunk, Object candidateEvents) {
            this.initExpressionLogic();
            throw new IllegalStateException("cannot delete records in analytics event tables.");
        }
    
        @Override
        public void update(ComplexEventChunk<StreamEvent> updatingEventChunk, Object candidateEvents, int[] mappingPosition) {
            this.initExpressionLogic();
            AnalyticsEventTableUtils.putEvents(this.tenantId, this.tableName, this.attrs, updatingEventChunk);
        }
    
    }

}
