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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.Weigher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.Constants;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceUtils;
import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsDataIndexer;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.eventtable.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.siddhi.core.config.ExecutionPlanContext;
import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.ComplexEventChunk;
import org.wso2.siddhi.core.event.state.StateEvent;
import org.wso2.siddhi.core.event.stream.MetaStreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEvent;
import org.wso2.siddhi.core.event.stream.StreamEventCloner;
import org.wso2.siddhi.core.event.stream.StreamEventPool;
import org.wso2.siddhi.core.executor.ExpressionExecutor;
import org.wso2.siddhi.core.executor.VariableExpressionExecutor;
import org.wso2.siddhi.core.table.EventTable;
import org.wso2.siddhi.core.util.SiddhiConstants;
import org.wso2.siddhi.core.util.collection.OverwritingStreamEventExtractor;
import org.wso2.siddhi.core.util.collection.UpdateAttributeMapper;
import org.wso2.siddhi.core.util.collection.operator.Finder;
import org.wso2.siddhi.core.util.collection.operator.MatchingMetaStateHolder;
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
import org.wso2.siddhi.query.api.expression.math.Add;
import org.wso2.siddhi.query.api.expression.math.Divide;
import org.wso2.siddhi.query.api.expression.math.Multiply;
import org.wso2.siddhi.query.api.expression.math.Subtract;
import org.wso2.siddhi.query.api.util.AnnotationHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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

    private boolean caching;

    private int cacheTimeoutSeconds;

    private long cacheSizeBytes;

    private String recordStore;

    private Map<String, List<Record>> cache;

    @Override
    public void init(TableDefinition tableDefinition, MetaStreamEvent tableMetaStreamEvent,
                     StreamEventPool tableStreamEventPool, StreamEventCloner tableStreamEventCloner,
                     ExecutionPlanContext executionPlanContext) {
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
            this.waitForIndexing = true;
        }
        String maxSearchResultCountProp = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_MAX_SEARCH_RESULT_COUNT);
        if (maxSearchResultCountProp != null) {
            maxSearchResultCountProp = maxSearchResultCountProp.trim();
            this.maxSearchResultCount = Integer.parseInt(maxSearchResultCountProp);
        } else {
            this.maxSearchResultCount = -1;
        }

        String cachingProp = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_CACHING);
        if (cachingProp != null) {
            this.caching = Boolean.parseBoolean(cachingProp.trim());
        } else {
            this.caching = false;
        }

        String cacheTimeoutSecsProp = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_CACHE_TIMEOUT_SECONDS);
        if (cacheTimeoutSecsProp != null) {
            this.cacheTimeoutSeconds = Integer.parseInt(cacheTimeoutSecsProp.trim());
            if (this.cacheTimeoutSeconds == -1) {
                this.cacheTimeoutSeconds = Integer.MAX_VALUE;
            }
        } else {
            this.cacheTimeoutSeconds = AnalyticsEventTableConstants.DEFAULT_CACHE_TIMEOUT;
        }

        String cacheSizeBytesProp = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_CACHE_SIZE_BYTES);
        if (cacheSizeBytesProp != null) {
            this.cacheSizeBytes = Integer.parseInt(cacheSizeBytesProp.trim());
        } else {
            this.cacheSizeBytes = AnalyticsEventTableConstants.DEFAULT_CACHE_SIZE;
        }

        this.recordStore = fromAnnotation.getElement(AnalyticsEventTableConstants.ANNOTATION_RECORD_StORE);
        try {
            this.tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        } catch (Error e) {
            /* this would never happens in real server runtime, caught for unit tests */
            this.tenantId = -1;
        }
        
        /* initialize caching if enabled */
        this.initCaching();
    }

    private void initCaching() {
        if (!this.caching) {
            return;
        }
        this.cache = CacheBuilder.newBuilder().maximumWeight(
                this.cacheSizeBytes).weigher(
                new Weigher<String, List<Record>>() {
                    public int weigh(String key, List<Record> value) {
                        return GenericUtils.serializeObject(value).length;
                    }
                }).expireAfterWrite(this.cacheTimeoutSeconds,
                TimeUnit.SECONDS).build().asMap();
    }

    private void checkAndProcessPostInit() {
        if (!this.postInit) {
            try {
                /* to make sure, other event tables with the same schema do
                 * not get a race condition when merging schemas */
                synchronized (this.getClass()) {
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
            if (!attr.getName().equals(AnalyticsEventTableConstants.INTERNAL_TIMESTAMP_ATTRIBUTE)) {
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
        }

        List<String> primaryKeys = AnalyticsDataServiceUtils.tokenizeAndTrimToList(this.primaryKeys, ",");

        try {
            if (!ServiceHolder.getAnalyticsDataService().tableExists(this.tenantId, this.tableName)) {
                log.debug(this.tableName + " table does not exists. Hence creating it");
                if (this.recordStore != null && this.recordStore.length() > 0) {
                    if (!ServiceHolder.getAnalyticsDataService().listRecordStoreNames().contains(this.recordStore)) {
                        throw new AnalyticsException("Unknown record store name " + this.recordStore);
                    }
                    ServiceHolder.getAnalyticsDataService().createTable(this.tenantId, this.recordStore, this.tableName);
                } else {
                    ServiceHolder.getAnalyticsDataService().createTable(this.tenantId, this.tableName);
                }
            }
        } catch (AnalyticsException e) {
            throw new IllegalStateException("Error while accessing table " + this.tableName + " : " + e.getMessage(), e);
        }

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
    public Finder constructFinder(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder,
                                  ExecutionPlanContext executionPlanContext,
                                  List<VariableExpressionExecutor> variableExpressionExecutors,
                                  Map<String, EventTable> eventTableMap) {
        return new AnalyticsTableOperator(this.tenantId, this.tableName, this.tableDefinition.getAttributeList(),
                expression, matchingMetaStateHolder, executionPlanContext, variableExpressionExecutors, eventTableMap);
    }

    @Override
    public StreamEvent find(StateEvent matchingEvent, Finder finder) {
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

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void add(ComplexEventChunk addingEventChunk) {
        this.checkAndProcessPostInit();
        addingEventChunk.reset();
        int count = AnalyticsEventTableUtils.putEvents(this.tenantId, this.tableName,
                this.tableDefinition.getAttributeList(), addingEventChunk);
        if (log.isDebugEnabled()) {
            log.debug("Records added: " + count + " -> " + this.tenantId + ":" + this.tableName);
        }
        this.checkAndWaitForIndexing();
    }

    private void checkAndWaitForIndexing() {
        if (this.waitForIndexing && this.indicesAvailable) {
            long start = 0;
            if (log.isDebugEnabled()) {
                log.debug("Wait for indexing START -> " + this.tenantId + ":" + this.tableName);
                start = System.currentTimeMillis();
            }
            this.waitForIndexing(this.tenantId, this.tableName);
            if (log.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                log.debug("Wait for indexing END: " + (end - start) + " ms -> " + this.tenantId + ":" + this.tableName);
            }
        }
    }

    @Override
    public Operator constructOperator(Expression expression, MatchingMetaStateHolder matchingMetaStateHolder,
                                      ExecutionPlanContext executionPlanContext,
                                      List<VariableExpressionExecutor> variableExpressionExecutors,
                                      Map<String, EventTable> eventTableMap) {
        return new AnalyticsTableOperator(this.tenantId, this.tableName, this.tableDefinition.getAttributeList(),
                expression, matchingMetaStateHolder, executionPlanContext, variableExpressionExecutors, eventTableMap);
    }

    @Override
    public boolean contains(StateEvent matchingEvent, Finder finder) {
        return finder.contains(matchingEvent, null);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void delete(ComplexEventChunk deletingEventChunk, Operator operator) {
        operator.delete(deletingEventChunk, null);
    }

    @Override
    public TableDefinition getTableDefinition() {
        return tableDefinition;
    }

    @Override
    public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Operator operator,
                       UpdateAttributeMapper[] updateAttributeMappers) {
        operator.update(updatingEventChunk, null, updateAttributeMappers);
    }

    @Override
    public void overwriteOrAdd(ComplexEventChunk<StateEvent> overwritingOrAddingEventChunk, Operator operator,
                               UpdateAttributeMapper[] updateAttributeMappers,
                               OverwritingStreamEventExtractor overwritingStreamEventExtractor) {
        this.update(overwritingOrAddingEventChunk, operator, updateAttributeMappers);
    }

    public boolean isCaching() {
        return caching;
    }

    public Map<String, List<Record>> getCache() {
        return cache;
    }

    /**
     * Analytics table {@link Operator} implementation.
     */
    public class AnalyticsTableOperator implements Operator {

        private static final String LUCENE_QUERY_PARAM = "e267ba83-0c77-4e0d-9c5f-cd3a31dbe2d3";

        private static final String LUCENE_CONSTANT_PARAM = "e267ba83-0c77-4e0d-9c5f-cd3a31dbe2d4";

        private int tenantId;

        private String tableName;

        private List<Attribute> myAttrs;

        private Expression expression;
        private MatchingMetaStateHolder matchingMetaStateHolder;

        private ExecutionPlanContext executionPlanContext;

        private List<VariableExpressionExecutor> variableExpressionExecutors;

        private Map<String, EventTable> eventTableMap;

        private String luceneQuery;

        private boolean pkMatchCompatible = true;

        private boolean returnAllRecords = true;

        private Set<String> primaryKeySet;

        private Set<String> candidatePrimaryKeySet;

        private Set<String> indexedKeySet;

        private Set<String> mentionedFields;

        private List<ExpressionExecutor> expressionExecs = new ArrayList<ExpressionExecutor>();

        private int paramIndex = 0;

        private Set<String> eventTableRefs = new HashSet<String>();

        private List<Attribute> outputAttrs;

        private boolean operatorInit;

        private Map<String, Object> primaryKeyRHSValues;

        private List<String> mentionedConstants;

        private int constantIndex = 0;

        public AnalyticsTableOperator(int tenantId, String tableName, List<Attribute> attrs, Expression expression,
                                      MatchingMetaStateHolder matchingMetaStateHolder, ExecutionPlanContext executionPlanContext,
                                      List<VariableExpressionExecutor> variableExpressionExecutors,
                                      Map<String, EventTable> eventTableMap) {
            this.tenantId = tenantId;
            this.tableName = tableName;
            this.myAttrs = attrs;
            this.expression = expression;
            this.matchingMetaStateHolder = matchingMetaStateHolder;
            this.executionPlanContext = executionPlanContext;
            this.variableExpressionExecutors = variableExpressionExecutors;
            this.eventTableMap = eventTableMap;
            this.primaryKeySet = new HashSet<String>();
            this.candidatePrimaryKeySet = new HashSet<>();
            this.indexedKeySet = new HashSet<String>();
            this.mentionedFields = new HashSet<String>();
            this.mentionedConstants = new ArrayList<>();
            this.primaryKeyRHSValues = new LinkedHashMap<>();
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
                for (int i = 0; i < this.constantIndex; i++) {
                    this.luceneQuery = this.luceneQuery.replace(LUCENE_CONSTANT_PARAM + i, this.mentionedConstants.get(i));
                }
                Set<String> nonIndixedFields = new HashSet<String>(this.mentionedFields);
                nonIndixedFields.removeAll(this.indexedKeySet);
                nonIndixedFields.removeAll(this.mentionedConstants);
                if (!this.pkMatchCompatible && nonIndixedFields.size() > 0) {
                    throw new IllegalStateException("The table [" + this.tenantId + ", " + this.tableName +
                            "] requires the field(s): " + nonIndixedFields +
                            " to be indexed for the given analytics event table based query to execute.");
                }
                this.operatorInit = true;
            }
        }

        private void initMetaStateEvent() {

            this.outputAttrs = matchingMetaStateHolder.getMatchingStreamDefinition().getAttributeList();
            for (MetaStreamEvent metaStreamEvent : matchingMetaStateHolder.getMetaStateEvent().getMetaStreamEvents()) {
                String referenceId = metaStreamEvent.getInputReferenceId();
                AbstractDefinition abstractDefinition = metaStreamEvent.getLastInputDefinition();
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
                if (origRHS.toString().startsWith(LUCENE_QUERY_PARAM) || origRHS.toString().startsWith(LUCENE_CONSTANT_PARAM)) {
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
                    case EQUAL:
                        if (!firstPass) {
                            this.candidatePrimaryKeySet.add(field);
                        }
                        this.mentionedFields.add(field);
                    /* for primary keys match */
                        this.primaryKeyRHSValues.put(field, rhs);
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
                Object constantValue = this.returnConstantValue((Constant) expr);
                this.mentionedConstants.add(constantValue.toString());
                return LUCENE_CONSTANT_PARAM + (this.constantIndex++);
            } else if (expr instanceof Variable) {
                Variable var = (Variable) expr;
                if (eventTableRefs.contains(var.getStreamId())) {
                    return var.getAttributeName();
                } else {
                    if (firstPass) {
                        ExpressionExecutor expressionExecutor = ExpressionParser.parseExpression(expr,
                                this.matchingMetaStateHolder.getMetaStateEvent(), this.matchingMetaStateHolder.getDefaultStreamEventIndex(), this.eventTableMap,
                                this.variableExpressionExecutors, this.executionPlanContext, false, 0);
                        this.expressionExecs.add(expressionExecutor);
                        return LUCENE_QUERY_PARAM;
                    } else {
                        return LUCENE_QUERY_PARAM + (this.paramIndex++);
                    }
                }
            } else if (expr instanceof Subtract || expr instanceof Add || expr instanceof Multiply ||
                    expr instanceof Divide || expr instanceof org.wso2.siddhi.query.api.expression.math.Mod) {
                throw new IllegalArgumentException("Analytics Event Table conditions does not support arithmetic operations: " + expr);
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
            }
            if (value instanceof Integer) {
                if ((int) value < 0) {
                    return ("[" + value + " TO " + value + "]").toString();
                } else {
                    return value.toString();
                }
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
        public Finder cloneFinder(String key) {
            return new AnalyticsTableOperator(this.tenantId, this.tableName, this.myAttrs, this.expression, this.matchingMetaStateHolder,
                    this.executionPlanContext, this.variableExpressionExecutors, this.eventTableMap);
        }

        @Override
        public boolean contains(StateEvent matchingEvent, Object candidateEvents) {
            this.initExpressionLogic();
            List<Record> records = this.findRecords(matchingEvent, candidateEvents, true);
            return records.size() > 0 && this.extractCountFromRecord(records.get(0)) > 0;
        }

        @Override
        public StreamEvent find(StateEvent matchingEvent, Object candidateEvents,
                                StreamEventCloner candidateEventCloner) {
            this.initExpressionLogic();
            List<Record> records = this.findRecords(matchingEvent, candidateEvents, false);
            return AnalyticsEventTableUtils.recordsToStreamEvent(this.myAttrs, records);
        }

        private List<Record> findRecords(ComplexEvent matchingEvent, Object candidateEvents, boolean countOnly) {
            long start = 0;
            if (log.isDebugEnabled()) {
                start = System.currentTimeMillis();
            }
            List<Record> records;
            if (this.pkMatchCompatible) {
                /* if no one else complained, check with primary key candidates */
                this.pkMatchCompatible = this.checkPrimaryKeyCompatibleWithCandidates();
            }
            if (this.returnAllRecords) {
                records = this.getAllRecords();
            } else if (this.pkMatchCompatible) {
                Record record = this.getRecordWithEventValues(matchingEvent);
                if (record == null) {
                    records = new ArrayList<>(0);
                } else {
                    records = Arrays.asList(record);
                }
            } else {
                records = this.executeLuceneQuery(matchingEvent, countOnly);
            }
            if (log.isDebugEnabled()) {
                long end = System.currentTimeMillis();
                log.debug("Find Records (CountOnly: " + countOnly + "): " + records.size() +
                        ", Time: " + (end - start) + " ms -> " + this.tenantId + ":" + this.tableName);
            }
            return records;
        }

        private String generateAllRecordsCacheKey() {
            return AnalyticsEventTableConstants.CACHE_KEY_PREFIX_ALL_RECORDS + this.tenantId + ":" + this.tableName;
        }

        private List<Record> getAllRecords() {
            if (isCaching()) {
                String key = this.generateAllRecordsCacheKey();
                List<Record> records = getCache().get(key);
                if (records == null) {
                    records = AnalyticsEventTableUtils.getAllRecords(this.tenantId, this.tableName);
                    getCache().put(key, records);
                    if (log.isDebugEnabled()) {
                        log.debug("Cache updated for all records: " + this.tenantId + ":" + this.tableName);
                    }
                } else if (log.isDebugEnabled()) {
                    log.debug("Cache HIT for all records: " + this.tenantId + ":" + this.tableName);
                }
                return records;
            } else {
                return AnalyticsEventTableUtils.getAllRecords(this.tenantId, this.tableName);
            }
        }

        private String generatePKCacheKey(List<Map<String, Object>> valuesBatch) {
            return AnalyticsEventTableConstants.CACHE_KEY_PREFIX_PK + this.tenantId + ":" + this.tableName + ":" + valuesBatch;
        }

        private Record getRecordWithEventValues(ComplexEvent event) {
            List<Map<String, Object>> valuesBatch = this.extractRecordValuesBatchFromEvent(event);
            if (isCaching()) {
                String key = this.generatePKCacheKey(valuesBatch);
                List<Record> records = getCache().get(key);
                if (records == null) {
                    Record record = this.getRecordWithEventValuesDirect(valuesBatch);
                    if (record != null) {
                        records = Arrays.asList(record);
                        getCache().put(key, records);
                        if (log.isDebugEnabled()) {
                            log.debug("Cache updated for record with values: " + this.tenantId + ":" + this.tableName + " -> " + valuesBatch);
                        }
                    }
                } else if (log.isDebugEnabled()) {
                    log.debug("Cache HIT for record with values: " + this.tenantId + ":" + this.tableName + " -> " + valuesBatch);
                }
                if (records != null && records.size() > 0) {
                    return records.get(0);
                } else {
                    return null;
                }
            } else {
                return this.getRecordWithEventValuesDirect(valuesBatch);
            }
        }

        private List<Map<String, Object>> extractRecordValuesBatchFromEvent(ComplexEvent event) {
            Map<String, Object> values = new HashMap<>();
            int expressionExIndex = 0;
            for (Map.Entry<String, Object> entry : this.primaryKeyRHSValues.entrySet()) {
                if (entry.getValue().toString().startsWith(LUCENE_QUERY_PARAM)) {
                    values.put(entry.getKey(), this.expressionExecs.get(expressionExIndex).execute(event));
                    expressionExIndex++;
                } else {
                    values.put(entry.getKey(), entry.getValue());
                }
            }
            List<Map<String, Object>> valuesBatch = new ArrayList<Map<String, Object>>();
            valuesBatch.add(values);
            return valuesBatch;
        }

        private Record getRecordWithEventValuesDirect(List<Map<String, Object>> valuesBatch) {
            try {
                AnalyticsDataResponse resp = ServiceHolder.getAnalyticsDataService().getWithKeyValues(
                        this.tenantId, this.tableName, 1, null, valuesBatch);
                List<Record> records = AnalyticsDataServiceUtils.listRecords(ServiceHolder.getAnalyticsDataService(), resp);
                if (records.size() > 0) {
                    return records.get(0);
                } else {
                    return null;
                }
            } catch (AnalyticsException e) {
                throw new IllegalStateException("Error in getting event records with values: " + e.getMessage(), e);
            }
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

        private String generateLuceneQueryCacheKey(String query) {
            return AnalyticsEventTableConstants.CACHE_KEY_PREFIX_LUCENE + this.tenantId + ":" + this.tableName + ":" + query;
        }

        private List<Record> executeLuceneQuery(ComplexEvent matchingEvent, boolean countOnly) {
            String query = this.getTranslatedLuceneQuery(matchingEvent);
            if (isCaching()) {
                String key = this.generateLuceneQueryCacheKey(query);
                List<Record> records = getCache().get(key);
                if (records == null) {
                    records = this.executeLuceneQueryDirect(query, countOnly);
                    getCache().put(key, records);
                    if (log.isDebugEnabled()) {
                        log.debug("Cache updated for lucene query (CountOnly: " + countOnly + "): " + this.tenantId +
                                ":" + this.tableName + " -> " + query);
                    }
                } else if (log.isDebugEnabled()) {
                    log.debug("Cache HIT for lucene query (CountOnly: " + countOnly + "): " + this.tenantId +
                            ":" + this.tableName + " -> " + query);
                }
                return records;
            } else {
                return this.executeLuceneQueryDirect(query, countOnly);
            }
        }

        private Record generateCountRecord(int count) {
            return new Record(count, "", null, 0);
        }

        private int extractCountFromRecord(Record record) {
            return record.getTenantId();
        }

        private List<Record> executeLuceneQueryDirect(String query, boolean countOnly) {
            try {
                AnalyticsDataService service = ServiceHolder.getAnalyticsDataService();
                if (log.isDebugEnabled()) {
                    log.debug("Analytics Table Search Query (CountOnly: " + countOnly + "): '" + query + "'");
                }
                int count = maxSearchResultCount;
                if (count == -1 || countOnly) {
                    count = service.searchCount(this.tenantId, this.tableName, query);
                    if (countOnly) {
                        return Arrays.asList(this.generateCountRecord(count));
                    }
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
                List<Record> records = this.findRecords(deletingEventChunk.next(), candidateEvents, false);
                AnalyticsEventTableUtils.deleteRecords(this.tenantId, this.tableName, records);
                if (log.isDebugEnabled()) {
                    log.debug("Records deleted: " + records.size() + " -> " + this.tenantId + ":" + this.tableName);
                }
            }
            checkAndWaitForIndexing();
        }

        @Override
        public void update(ComplexEventChunk<StateEvent> updatingEventChunk, Object candidateEvents,
                           UpdateAttributeMapper[] updateAttributeMappers) {
            this.initExpressionLogic();
            updatingEventChunk.reset();
            ComplexEvent event;
            List<Record> records;
            try {
                while (updatingEventChunk.hasNext()) {
                    event = updatingEventChunk.next();
                    records = this.findRecords(event, candidateEvents, false);
                    this.updateRecordsWithEvent(records, event, updateAttributeMappers);
                    ServiceHolder.getAnalyticsDataService().put(records);
                    if (log.isDebugEnabled()) {
                        log.debug("Records updated: " + records.size() + " -> " + this.tenantId + ":" + this.tableName);
                    }
                }
                checkAndWaitForIndexing();
            } catch (AnalyticsException e) {
                throw new IllegalStateException("Error in executing update query: " + e.getMessage(), e);
            }
        }

        @Override
        public ComplexEventChunk<StreamEvent> overwriteOrAdd(ComplexEventChunk<StateEvent> overwritingOrAddingEventChunk,
                                                             Object candidateEvents,
                                                             UpdateAttributeMapper[] updateAttributeMappers,
                                                             OverwritingStreamEventExtractor overwritingStreamEventExtractor) {
            this.update(overwritingOrAddingEventChunk, candidateEvents, updateAttributeMappers);
            return null;
        }

        private void updateRecordsWithEvent(List<Record> records, ComplexEvent event, UpdateAttributeMapper[] updateAttributeMappers) {
            Map<String, Object> values = AnalyticsEventTableUtils.streamEventToRecordValues(this.outputAttrs, event, updateAttributeMappers);
            Set<String> pks = null;
            try {
                AnalyticsDataService service = ServiceHolder.getAnalyticsDataService();
                List<String> tmpPks = service.getTableSchema(this.tenantId, this.tableName).getPrimaryKeys();
                if (tmpPks != null) {
                    pks = new HashSet<>(tmpPks);
                }
            } catch (AnalyticsException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
            Object lhs, rhs;
            boolean pkMismatch, tsSet;
            for (Record record : records) {
                tsSet = false;
                for (Entry<String, Object> entry : values.entrySet()) {
                    if (pks != null && pks.contains(entry.getKey())) {
                        lhs = entry.getValue();
                        rhs = record.getValue(entry.getKey());
                        pkMismatch = false;
                        if (lhs != null) {
                            if (!lhs.equals(rhs)) {
                                pkMismatch = true;
                            }
                        } else if (rhs != null) {
                            pkMismatch = true;
                        }
                        if (pkMismatch) {
                            throw new IllegalStateException("The primary key cannot be updated, "
                                    + "new values: " + values + ", existing values: " +
                                    record.getValues() + " PKs: " + pks);
                        }
                    }
                    if (entry.getKey().equals(AnalyticsEventTableConstants.INTERNAL_TIMESTAMP_ATTRIBUTE)) {
                        record.setTimestamp((Long) entry.getValue());
                        tsSet = true;
                    } else if (record.getValues().containsKey(entry.getKey())) {
                        record.getValues().put(entry.getKey(), entry.getValue());
                    }
                }
                if (!tsSet) {
                    record.setTimestamp(event.getTimestamp());
                }
            }
        }
    }

}