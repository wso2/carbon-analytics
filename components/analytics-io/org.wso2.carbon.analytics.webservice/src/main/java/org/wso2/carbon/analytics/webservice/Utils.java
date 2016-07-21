/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.webservice;

import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.dataservice.commons.AggregateField;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.webservice.beans.AggregateResponse;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsAggregateField;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsAggregateRequest;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsCategoryPathBean;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.beans.EventBean;
import org.wso2.carbon.analytics.webservice.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.beans.SchemaColumnBean;
import org.wso2.carbon.analytics.webservice.beans.SortByFieldBean;
import org.wso2.carbon.analytics.webservice.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.webservice.beans.ValuesBatchBean;
import org.wso2.carbon.analytics.webservice.exception.AnalyticsWebServiceException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents a set of utility functionalities for the analytics REST API.
 */
public class Utils {

    public static final String EMPTY_VALUE = "";

    private Utils() {
    }

    /**
     * Creates the record beans from records.
     *
     * @param records the records
     * @return the list of recordBeans
     */
    public static List<RecordBean> createRecordBeans(List<Record> records) {
        List<RecordBean> recordBeans = new ArrayList<>();
        if (records != null) {
            for (Record record : records) {
                RecordBean recordBean = createRecordBean(record);
                recordBeans.add(recordBean);
            }
        }
        return recordBeans;
    }

    /**
     * Creates the record beans from records.
     *
     * @param records the records
     * @return the Map of recordBeans with id as key and recordbean as value
     */
    public static Map<String, RecordBean> createRecordBeansKeyedWithIds(RecordBean[] records) {
        Map<String, RecordBean> recordBeans = new HashMap<>();
        if (records != null) {
            for (RecordBean record : records) {
                recordBeans.put(record.getId(), record);
            }
        }
        return recordBeans;
    }

    /**
     * Create a RecordBean object out of a Record object
     *
     * @param record the record object
     * @return RecordBean object
     */
    public static RecordBean createRecordBean(Record record) {
        RecordBean recordBean = new RecordBean();
        recordBean.setId(record.getId());
        recordBean.setTableName(record.getTableName());
        recordBean.setTimestamp(record.getTimestamp());
        if (record.getValues() != null) {
            recordBean.setValues(createRecordEntryBeans(record.getValues()));
        }
        return recordBean;
    }

    @SuppressWarnings("unchecked")
    private static RecordValueEntryBean[] createRecordEntryBeans(Map<String, Object> values) {
        List<RecordValueEntryBean> beans = new ArrayList<>(values.size());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            RecordValueEntryBean bean = new RecordValueEntryBean();
            if (entry.getValue() instanceof List) {
                List<Object> analyticsCategoryPath = (List<Object>) entry.getValue();
                Iterator<Object> iterator = analyticsCategoryPath.iterator();
                List<String> pathAsString = new ArrayList<>();
                while (iterator.hasNext()) {
                    pathAsString.add(String.valueOf(iterator.next()));
                }
                AnalyticsCategoryPathBean categoryPathBean = new AnalyticsCategoryPathBean();
                categoryPathBean.setPath(pathAsString.toArray(new String[pathAsString.size()]));
                bean.setFieldName(entry.getKey());
                bean.setAnalyticsCategoryPathBeanValue(categoryPathBean);
                bean.setType(RecordValueEntryBean.FACET);
                beans.add(bean);
            } else {
                beans.add(getRecordValueEntryBean(entry.getKey(), entry.getValue()));
            }
        }
        return beans.toArray(new RecordValueEntryBean[beans.size()]);
    }

    /**
     * Gets the record ids from search results.
     *
     * @param searchResults the search results
     * @return the record ids from search results
     */
    public static String[] getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        if (searchResults != null) {
            for (SearchResultEntry searchResult : searchResults) {
                ids.add(searchResult.getId());
            }
        }
        return ids.toArray(new String[ids.size()]);
    }

    /**
     * Create table schema bean from a analytics schema
     *
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(AnalyticsSchema analyticsSchema) {
        if (analyticsSchema == null) {
            return null;
        }
        List<SchemaColumnBean> columnBeans = new ArrayList<>();
        if (analyticsSchema.getColumns() != null) {
            for (Map.Entry<String, ColumnDefinition> columnTypeEntry : analyticsSchema.getColumns().entrySet()) {
                SchemaColumnBean bean = new SchemaColumnBean();
                bean.setColumnName(columnTypeEntry.getKey());
                bean.setColumnType(getColumnTypeBean(columnTypeEntry.getValue()));
                bean.setScoreParam(columnTypeEntry.getValue().isScoreParam());
                bean.setIndex(columnTypeEntry.getValue().isIndexed());
                bean.setFacet(columnTypeEntry.getValue().isFacet());
                columnBeans.add(bean);
            }
        }
        List<String> primaryKeys = new ArrayList<>();
        if (analyticsSchema.getPrimaryKeys() != null) {
            primaryKeys = analyticsSchema.getPrimaryKeys();
        }
        return new AnalyticsSchemaBean(columnBeans.toArray(new SchemaColumnBean[columnBeans.size()]),
                                       primaryKeys.toArray(new String[primaryKeys.size()]));
    }

    /**
     * convert a column type to bean type
     *
     * @param columnDefinition the ColumnType to be converted to bean type
     * @return ColumnTypeBean instance
     */
    private static String getColumnTypeBean(ColumnDefinition columnDefinition) {
        switch (columnDefinition.getType()) {
            case STRING:
                return RecordValueEntryBean.STRING;
            case INTEGER:
                return RecordValueEntryBean.INTEGER;
            case LONG:
                return RecordValueEntryBean.LONG;
            case FLOAT:
                return RecordValueEntryBean.FLOAT;
            case DOUBLE:
                return RecordValueEntryBean.DOUBLE;
            case BOOLEAN:
                return RecordValueEntryBean.BOOLEAN;
            case BINARY:
                return RecordValueEntryBean.BINARY;
            default:
                return RecordValueEntryBean.STRING;
        }
    }

    private static RecordValueEntryBean getRecordValueEntryBean(String fieldName, Object value) {
        RecordValueEntryBean recordValueEntryBean = new RecordValueEntryBean();
        recordValueEntryBean.setFieldName(fieldName);
        if (value != null) {
            switch (value.getClass().getSimpleName().toUpperCase()) {
                case RecordValueEntryBean.STRING: {
                    recordValueEntryBean.setStringValue(String.valueOf(value));
                    recordValueEntryBean.setType(RecordValueEntryBean.STRING);
                    break;
                }
                case RecordValueEntryBean.INTEGER: {
                    recordValueEntryBean.setIntValue(Integer.parseInt(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.INTEGER);
                    break;
                }
                case RecordValueEntryBean.LONG: {
                    recordValueEntryBean.setLongValue(Long.parseLong(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.LONG);
                    break;
                }
                case RecordValueEntryBean.BOOLEAN: {
                    recordValueEntryBean.setBooleanValue(Boolean.parseBoolean(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.BOOLEAN);
                    break;
                }
                case RecordValueEntryBean.FLOAT: {
                    recordValueEntryBean.setFloatValue(Float.parseFloat(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.FLOAT);
                    break;
                }
                case RecordValueEntryBean.DOUBLE: {
                    recordValueEntryBean.setDoubleValue(Double.parseDouble(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.DOUBLE);
                    break;
                }
                default: {
                    recordValueEntryBean.setStringValue(String.valueOf(value));
                    recordValueEntryBean.setType(RecordValueEntryBean.STRING);
                }
            }
        } else {
            recordValueEntryBean.setStringValue(EMPTY_VALUE);
            recordValueEntryBean.setType(RecordValueEntryBean.STRING);
        }
        return recordValueEntryBean;
    }

    /**
     * Gets the streamDefinition object from stream definition bean class
     * @param streamDefinitionBean The streamDefinition bean class
     * @return The converted StreamDefinition object.
     * @throws MalformedStreamDefinitionException
     * @throws AnalyticsWebServiceException
     */
    public static StreamDefinition getStreamDefinition(StreamDefinitionBean streamDefinitionBean)
            throws MalformedStreamDefinitionException, AnalyticsWebServiceException {
        String name = streamDefinitionBean.getName();
        String version = streamDefinitionBean.getVersion();
        StreamDefinition streamDefinition;
        if (name != null && version != null) {
            streamDefinition = new StreamDefinition(name, version);
        } else if (name != null) {
            streamDefinition = new StreamDefinition(name);
        } else {
            throw new AnalyticsWebServiceException("Stream name cannot be empty!");
        }
        streamDefinition.setNickName(streamDefinitionBean.getNickName());
        streamDefinition.setDescription(streamDefinitionBean.getDescription());
        streamDefinition.setCorrelationData(getAttributeData(streamDefinitionBean.getCorrelationData()));
        streamDefinition.setMetaData(getAttributeData(streamDefinitionBean.getMetaData()));
        streamDefinition.setPayloadData(getAttributeData(streamDefinitionBean.getPayloadData()));
        if (streamDefinitionBean.getTags() != null) {
            streamDefinition.setTags(Arrays.asList(streamDefinitionBean.getTags()));
        }
        return  streamDefinition;
    }

    private static List<Attribute> getAttributeData(StreamDefAttributeBean[] attributeBeans) throws AnalyticsWebServiceException{
        List<Attribute> attributes = new ArrayList<>();
        if (attributeBeans != null) {
            for (StreamDefAttributeBean bean : attributeBeans) {
                Attribute attribute = new Attribute(bean.getName(), getAttributeType(bean.getType()));
                attributes.add(attribute);
            }
        }
        return attributes;
    }

    private static AttributeType getAttributeType(String type) throws AnalyticsWebServiceException {
        if (type != null) {
            switch (type) {
                case RecordValueEntryBean.STRING : {
                    return AttributeType.STRING;
                }
                case RecordValueEntryBean.BOOLEAN : {
                    return AttributeType.BOOL;
                }
                case RecordValueEntryBean.FLOAT : {
                    return AttributeType.FLOAT;
                }
                case RecordValueEntryBean.DOUBLE : {
                    return AttributeType.DOUBLE;
                }
                case RecordValueEntryBean.INTEGER : {
                    return AttributeType.INT;
                }
                case RecordValueEntryBean.LONG : {
                    return AttributeType.LONG;
                }
                default: {
                    throw new AnalyticsWebServiceException("Unkown type found while reading stream definition bean.");
                }
            }
        } else {
            throw new AnalyticsWebServiceException("Type is not defined.");
        }
    }

    public static Event getEvent(EventBean eventBean, StreamDefinition streamDefinition)
            throws AnalyticsWebServiceException {
        Event event = new Event();
        event.setStreamId(streamDefinition.getStreamId());
        event.setTimeStamp(eventBean.getTimeStamp());
        event.setMetaData(getEventData(eventBean.getMetaData(), streamDefinition.getMetaData()));
        event.setCorrelationData(getEventData(eventBean.getCorrelationData(), streamDefinition.getCorrelationData()));
        event.setPayloadData(getEventData(eventBean.getPayloadData(), streamDefinition.getPayloadData()));
        event.setArbitraryDataMap(getArbitraryValues(eventBean.getArbitraryData()));
        return  event;
    }

    private static Map<String, String> getArbitraryValues(RecordValueEntryBean[] arbitraryData) {
        Map<String, String> arbitraryDataMap = new HashMap<>();
        if (arbitraryData != null) {
            for (RecordValueEntryBean bean : arbitraryData) {
                arbitraryDataMap.put(bean.getFieldName(), bean.getStringValue());
            }
        }
        if (arbitraryDataMap.isEmpty()) {
            return null;
        }
        return arbitraryDataMap;
    }

    private static Object[] getEventData(RecordValueEntryBean[] beans, List<Attribute> columns)
            throws AnalyticsWebServiceException {
        List<Object> values = new ArrayList<>();
        Map<String, RecordValueEntryBean> valueEntryBeans = new HashMap<>();
        if (beans != null) {
            for (RecordValueEntryBean bean : beans) {
                valueEntryBeans.put(bean.getFieldName(), bean);
            }
        }
        if (columns != null) {
            for (Attribute column : columns) {
                if (column != null) {
                    AttributeType type = column.getType();
                    String columnName = column.getName();
                    RecordValueEntryBean bean = valueEntryBeans.get(columnName);
                    if (bean != null) {
                        switch (type) {
                            case DOUBLE:
                                values.add(new BigDecimal(bean.toString()).doubleValue());
                                break;
                            case INT:
                                values.add(new BigDecimal(bean.toString()).intValue());
                                break;
                            case BOOL:
                                values.add(Boolean.parseBoolean(bean.toString()));
                                break;
                            case LONG:
                                values.add(new BigDecimal(bean.toString()).longValue());
                                break;
                            case FLOAT:
                                values.add(new BigDecimal(bean.toString()).floatValue());
                                break;
                            case STRING:
                                values.add(bean.toString());
                                break;
                            default:
                                throw new AnalyticsWebServiceException("DataType is not valid for [" +
                                                                       bean.getFieldName());
                        }
                    } else {
                        throw new AnalyticsWebServiceException("value is not given for field: " +
                                                               columnName);
                    }
                } else {
                    throw new AnalyticsWebServiceException("Record Values are null");
                }
            }
        }
        return values.toArray(new Object[values.size()]);
    }

    public static StreamDefinitionBean getStreamDefinitionBean(StreamDefinition streamDefinition)
            throws AnalyticsWebServiceException {
        StreamDefinitionBean bean = new StreamDefinitionBean();
        bean.setName(streamDefinition.getName());
        bean.setDescription(streamDefinition.getDescription());
        bean.setNickName(streamDefinition.getNickName());
        bean.setVersion(streamDefinition.getVersion());
        List<String> tags = streamDefinition.getTags();
        List<Attribute> metaData = streamDefinition.getMetaData();
        List<Attribute> correlationData = streamDefinition.getCorrelationData();
        List<Attribute> payloadData = streamDefinition.getPayloadData();
        if (tags != null) {
            bean.setTags(tags.toArray(new String[tags.size()]));
        }
        if (metaData != null) {
            bean.setMetaData(createStreamDefAttributes(metaData));
        }
        if (correlationData != null) {
            bean.setCorrelationData(createStreamDefAttributes(correlationData));
        }
        if (payloadData != null) {
            bean.setPayloadData(createStreamDefAttributes(payloadData));
        }
        return bean;
    }

    private static StreamDefAttributeBean[] createStreamDefAttributes(List<Attribute> attributes)
            throws AnalyticsWebServiceException {
        List<StreamDefAttributeBean> beans = new ArrayList<>();
        for (Attribute attribute : attributes) {
            StreamDefAttributeBean bean = new StreamDefAttributeBean();
            bean.setName(attribute.getName());
            switch (attribute.getType()) {
                case LONG:
                    bean.setType(RecordValueEntryBean.LONG);
                    break;
                case STRING:
                    bean.setType(RecordValueEntryBean.STRING);
                    break;
                case BOOL:
                    bean.setType(RecordValueEntryBean.BOOLEAN);
                    break;
                case FLOAT:
                    bean.setType(RecordValueEntryBean.FLOAT);
                    break;
                case DOUBLE:
                    bean.setType(RecordValueEntryBean.DOUBLE);
                    break;
                case INT:
                    bean.setType(RecordValueEntryBean.INTEGER);
                    break;
                default:
                    throw new AnalyticsWebServiceException("Unknown Datatype found in Stream Definition");
            }
            beans.add(bean);
        }
        return beans.toArray(new StreamDefAttributeBean[beans.size()]);
    }

    public static List<Map<String, Object>> getValuesBatch(ValuesBatchBean[] valuesBatchBeans, AnalyticsSchema schema)
            throws AnalyticsWebServiceException {
        List<Map<String, Object>> valuesBatch = new ArrayList<>();
        Map<String, ColumnDefinition> columns = schema.getColumns();
        if (valuesBatchBeans != null) {
            for (ValuesBatchBean valuesBatchBean : valuesBatchBeans) {
                RecordValueEntryBean[] beans = valuesBatchBean.getKeyValues();
                if (beans != null) {
                    Map<String, Object> keyValues = new HashMap<>();
                    for (RecordValueEntryBean recordValueEntryBean : beans) {
                        String fieldName = recordValueEntryBean.getFieldName();
                        keyValues.put(fieldName, getObjectValue(columns.get(fieldName).getType(), recordValueEntryBean));
                    }
                    if (!keyValues.isEmpty()) {
                        valuesBatch.add(keyValues);
                    }
                } else {
                    throw new AnalyticsWebServiceException("Key values are null in the valuesBatch");
                }
            }
            return valuesBatch;
        } else {
            throw new AnalyticsWebServiceException("ValuesBatch is null");
        }
    }

    private static Object getObjectValue(AnalyticsSchema.ColumnType type,
                                         RecordValueEntryBean recordValueEntryBean)
            throws AnalyticsWebServiceException {
        Object value;
        switch (type) {
            case FLOAT:
                value = new BigDecimal(recordValueEntryBean.toString()).floatValue();
                break;
            case DOUBLE:
                value = new BigDecimal(recordValueEntryBean.toString()).doubleValue();
                break;
            case INTEGER:
                value = new BigDecimal(recordValueEntryBean.toString()).intValue();
                break;
            case LONG:
                value = new BigDecimal(recordValueEntryBean.toString()).longValue();
                break;
            case BOOLEAN:
                value = Boolean.parseBoolean(recordValueEntryBean.toString());
                break;
            case STRING:
                value = recordValueEntryBean.toString();
                break;
            default:
                throw new AnalyticsWebServiceException("Value cannot be mapped to the data type given in the schema");
        }
        return value;
    }

    public static AnalyticsSchema getAnalyticsSchema(AnalyticsSchemaBean schemaBean) {
        SchemaColumnBean[] columnBeans = schemaBean.getColumns();
        String[] primaryKeys = schemaBean.getPrimaryKeys();
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
        List<String> primaryKeyList = new ArrayList<>();
        if (primaryKeys != null) {
            primaryKeyList.addAll(Arrays.asList(primaryKeys));
        }
        if (columnBeans != null) {
            for (SchemaColumnBean columnBean : columnBeans) {
                ColumnDefinitionExt columnDefinition = new ColumnDefinitionExt(columnBean.getColumnName(),
                    getColumnType(columnBean.getColumnType()), columnBean.isIndex(), columnBean.isScoreParam(), columnBean.isFacet());
                //To make backward compatible with DAS 3.0.0 and DAS 3.0.1, see DAS-402
                if (columnBean.getColumnType().equalsIgnoreCase(RecordValueEntryBean.FACET)) {
                    columnDefinition.setFacet(true);
                }
                columnDefinitions.add(columnDefinition);
            }
        }
        return new AnalyticsSchema(columnDefinitions, primaryKeyList);
    }

    private static AnalyticsSchema.ColumnType getColumnType(String columnType) {
        switch (columnType) {
            case RecordValueEntryBean.STRING:
                return AnalyticsSchema.ColumnType.STRING;
            case RecordValueEntryBean.INTEGER:
                return AnalyticsSchema.ColumnType.INTEGER;
            case RecordValueEntryBean.LONG:
                return AnalyticsSchema.ColumnType.LONG;
            case RecordValueEntryBean.FLOAT:
                return AnalyticsSchema.ColumnType.FLOAT;
            case RecordValueEntryBean.DOUBLE:
                return AnalyticsSchema.ColumnType.DOUBLE;
            case RecordValueEntryBean.BOOLEAN:
                return AnalyticsSchema.ColumnType.BOOLEAN;
            case RecordValueEntryBean.BINARY:
                return AnalyticsSchema.ColumnType.BINARY;
            default:
                return AnalyticsSchema.ColumnType.STRING;
        }
    }

    public static AggregateRequest getAggregateRequest(AnalyticsAggregateRequest request) {
        AggregateRequest aggregateRequest = new AggregateRequest();
        aggregateRequest.setTableName(request.getTableName());
        aggregateRequest.setGroupByField(request.getGroupByField());
        aggregateRequest.setQuery(request.getQuery());
        aggregateRequest.setFields(createAggregateFields(request.getFields()));
        aggregateRequest.setAggregateLevel(request.getAggregateLevel());
        List<String> parentPath = new ArrayList<>();
        if (request.getParentPath() != null && request.getParentPath()[0]!= null) {
            parentPath.addAll(Arrays.asList(request.getParentPath()));
        }
        aggregateRequest.setParentPath(parentPath);
        aggregateRequest.setNoOfRecords(request.getNoOfRecords());
        return aggregateRequest;
    }

    private static List<AggregateField> createAggregateFields(AnalyticsAggregateField[] fields) {
        List<AggregateField> aggregateFields = new ArrayList<>();
        if (fields != null) {
            for (AnalyticsAggregateField field : fields) {
                AggregateField aggregateField;
                // this is only to make backward compatible with older versions of aggregate apis
                if (field.getFieldName() != null && !field.getFieldName().isEmpty()) {
                    aggregateField = new AggregateField(new String[]{field.getFieldName()}, field.getAggregate(), field.getAlias());
                } else {
                    aggregateField = new AggregateField(field.getFields(), field.getAggregate(), field.getAlias());
                }
                aggregateFields.add(aggregateField);
            }
        }
        return  aggregateFields;
    }

    @SuppressWarnings("unchecked")
    public static List<Record> createList(AnalyticsIterator<Record> iterator) {
        List<Record> records = new ArrayList<>();
        records.addAll(IteratorUtils.toList(iterator));
        return records;
    }

    public static AggregateRequest[] getAggregateRequests(AnalyticsAggregateRequest[] requests) {
        List<AggregateRequest> aggregateRequests = new ArrayList<>();
        if (requests != null) {
           for (AnalyticsAggregateRequest request : requests) {
               aggregateRequests.add(getAggregateRequest(request));
           }
        }
        return aggregateRequests.toArray(new AggregateRequest[aggregateRequests.size()]);
    }

    public static AggregateResponse[] createAggregateResponses(
            List<AnalyticsIterator<Record>> iterators) {
        List<AggregateResponse> responses = new ArrayList<>();
        for (AnalyticsIterator<Record> iterator : iterators) {
            AggregateResponse response;
            List<Record> records = Utils.createList(iterator);
            if (!records.isEmpty()) {
                response = new AggregateResponse();
                response.setTableName(records.get(0).getTableName());
                response.setRecordBeans(records.toArray(new RecordBean[records.size()]));
                responses.add(response);
            }
        }
        return responses.toArray(new AggregateResponse[responses.size()]);
    }

    public static List<SortByField> getSortByFields(SortByFieldBean[] sortByFieldBeans)
            throws AnalyticsException {
        List<SortByField> sortByFields = new ArrayList<>();
        if (sortByFieldBeans != null) {
            for (SortByFieldBean sortByFieldBean : sortByFieldBeans) {
                SortByField sortByField = new SortByField(sortByFieldBean.getFieldName(),
                                                          getSortType(sortByFieldBean.getFieldName(),
                                                          sortByFieldBean.getSortType()));
                sortByFields.add(sortByField);
            }
        }
        return sortByFields;
    }

    private static SortType getSortType(String field, String sortBy) throws AnalyticsException {
        SortType sortType;
        if (sortBy != null) {
            switch (sortBy) {
                case "ASC":
                    sortType = SortType.ASC;
                    break;
                case "DESC":
                    sortType = SortType.DESC;
                    break;
                default:
                    throw new AnalyticsException("Unknown SORT order: " + sortBy + "for field: " + field);
            }
        }  else {
            throw new AnalyticsException("sortType cannot be null for field: " + field);
        }
        return sortType;
    }

    public static List<RecordBean> getSortedRecordBeans(Map<String, RecordBean> recordBeanMap,
                                                        List<SearchResultEntry> searchResults) {
        List<RecordBean> sortedRecords = new ArrayList<>();
        for (SearchResultEntry searchResultEntry : searchResults) {
            sortedRecords.add(recordBeanMap.get(searchResultEntry.getId()));
        }
        return sortedRecords;
    }
}
