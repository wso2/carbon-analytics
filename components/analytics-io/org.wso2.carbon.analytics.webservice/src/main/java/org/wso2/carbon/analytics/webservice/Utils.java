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

import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsCategoryPathBean;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.beans.EventBean;
import org.wso2.carbon.analytics.webservice.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.beans.SchemaColumnBean;
import org.wso2.carbon.analytics.webservice.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.webservice.exception.AnalyticsWebServiceException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a set of utility functionalities for the analytics REST API.
 */
public class Utils {
    /**
     * Gets the records from record beans.
     *
     * @param recordBeans the record beans
     * @return the records from record beans
     * @throws AnalyticsException if the tableName is not specified
     *//*
    public static List<Record> getRecords(String username, List<RecordBean> recordBeans) throws AnalyticsException {
        List<Record> records = new ArrayList<>();
        int tenantId = getTenantId(username);
        if (recordBeans != null) {
            for (RecordBean recordBean : recordBeans) {
                if (recordBean != null) {
                    if (recordBean.getTableName().isEmpty()) {
                        throw new AnalyticsException("TableName cannot be empty!");
                    }
                    records.add(new Record(recordBean.getId(), tenantId, recordBean.getTableName(), validateAndReturn(recordBean.getValues())));
                }
            }
        }
        return records;
    }*/

    /*private static Map<String, Object> validateAndReturn(RecordValueEntryBean[] values) {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        if (values != null) {
            for (RecordValueEntryBean recordEntry : values) {
                valueMap.put(recordEntry.getFieldName(), getValue(recordEntry));
            }
        }
        return valueMap;
    }*/

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
                List<String> analyticsCategoryPath = (List<String>) entry.getValue();
                AnalyticsCategoryPathBean categoryPathBean = new AnalyticsCategoryPathBean();
                categoryPathBean.setPath(analyticsCategoryPath.toArray(new String[analyticsCategoryPath.size()]));
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
    public static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        if (searchResults != null) {
            for (SearchResultEntry searchResult : searchResults) {
                ids.add(searchResult.getId());
            }
        }
        return ids;
    }

    /**
     * Create a Analytics schema from a bean class
     *
     * @param analyticsSchemaBean bean table schema to be converted to Analytics Schema.
     * @return Analytics schema
     *//*
    public static AnalyticsSchema createAnalyticsSchema(AnalyticsSchemaBean analyticsSchemaBean) {
        List<ColumnDefinition> columnTypes = new ArrayList<>();
        if (analyticsSchemaBean == null) {
            return null;
        }
        if (analyticsSchemaBean.getColumns() != null) {
            for (SchemaColumnBean columnBean : analyticsSchemaBean.getColumns()) {
                columnTypes.add(getColumnDefinition(columnBean.getColumnName(), columnBean.getColumnType(), 
                        columnBean.isIndex(), columnBean.isScoreParam()));
            }
        }
        List<String> primaryKeys = null;
        if (analyticsSchemaBean.getPrimaryKeys() != null) {
            primaryKeys = Arrays.asList(analyticsSchemaBean.getPrimaryKeys());
        }
        return new AnalyticsSchema(columnTypes, primaryKeys);
    }*/

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
     * Converts a column type bean to ColumnType.
     *//*
    private static ColumnDefinition getColumnDefinition(String name, String type, boolean isIndex,
                                                        boolean isScoreParam) {
        ColumnDefinition columnDefinition = new ColumnDefinition();
        switch (type) {
            case RecordValueEntryBean.STRING:
                columnDefinition.setType(ColumnType.STRING);
                break;
            case RecordValueEntryBean.INTEGER:
                columnDefinition.setType(ColumnType.INTEGER);
                break;
            case RecordValueEntryBean.LONG:
                columnDefinition.setType(ColumnType.LONG);
                break;
            case RecordValueEntryBean.FLOAT:
                columnDefinition.setType(ColumnType.FLOAT);
                break;
            case RecordValueEntryBean.DOUBLE:
                columnDefinition.setType(ColumnType.DOUBLE);
                break;
            case RecordValueEntryBean.BOOLEAN:
                columnDefinition.setType(ColumnType.BOOLEAN);
                break;
            case RecordValueEntryBean.BINARY:
                columnDefinition.setType(ColumnType.BINARY);
                break;
            case RecordValueEntryBean.FACET:
                columnDefinition.setType(ColumnType.FACET);
                break;
            default:
                columnDefinition.setType(ColumnType.STRING);
        }
        columnDefinition.setName(name);
        columnDefinition.setIndexed(isIndex);
        columnDefinition.setScoreParam(isScoreParam);
        return columnDefinition;
    }*/

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
            case FACET:
                return RecordValueEntryBean.FACET;
            default:
                return RecordValueEntryBean.STRING;
        }
    }

    /*private static int getTenantId(String username) throws AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            return AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }*/

    /*private static Object getValue(RecordValueEntryBean recordValueEntryBean) {
        Object resultObj;
        switch (recordValueEntryBean.getType()) {
            case RecordValueEntryBean.STRING: {
                resultObj = recordValueEntryBean.getStringValue();
                break;
            }
            case RecordValueEntryBean.INTEGER: {
                resultObj = recordValueEntryBean.getIntValue();
                break;
            }
            case RecordValueEntryBean.LONG: {
                resultObj = recordValueEntryBean.getLongValue();
                break;
            }
            case RecordValueEntryBean.BOOLEAN: {
                resultObj = recordValueEntryBean.getBooleanValue();
                break;
            }
            case RecordValueEntryBean.FLOAT: {
                resultObj = recordValueEntryBean.getFloatValue();
                break;
            }
            case RecordValueEntryBean.DOUBLE: {
                resultObj = recordValueEntryBean.getDoubleValue();
                break;
            }
            case RecordValueEntryBean.FACET: {
                List<String> analyticsCategoryPath = new ArrayList<>();
                if (recordValueEntryBean.getAnalyticsCategoryPathBeanValue() != null) {
                    analyticsCategoryPath.addAll(Arrays.asList(recordValueEntryBean.getAnalyticsCategoryPathBeanValue().getPath()));
                }
                resultObj = analyticsCategoryPath;
                break;
            }
            default: {
                resultObj = recordValueEntryBean.getStringValue();
            }
        }
        return resultObj;
    }*/

    private static RecordValueEntryBean getRecordValueEntryBean(String fieldName, Object value) {
        RecordValueEntryBean recordValueEntryBean = new RecordValueEntryBean();
        recordValueEntryBean.setFieldName(fieldName);
        if (value != null) {
            switch (value.getClass().toString().toUpperCase()) {
                case RecordValueEntryBean.STRING: {
                    recordValueEntryBean.setStringValue(String.valueOf(value));
                    recordValueEntryBean.setType(RecordValueEntryBean.STRING);
                    break;
                }
                case RecordValueEntryBean.INTEGER: {
                    recordValueEntryBean.setIntValue(Integer.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.INTEGER);
                    break;
                }
                case RecordValueEntryBean.LONG: {
                    recordValueEntryBean.setLongValue(Long.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.LONG);
                    break;
                }
                case RecordValueEntryBean.BOOLEAN: {
                    recordValueEntryBean.setBooleanValue(Boolean.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.BOOLEAN);
                    break;
                }
                case RecordValueEntryBean.FLOAT: {
                    recordValueEntryBean.setFloatValue(Float.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.FLOAT);
                    break;
                }
                case RecordValueEntryBean.DOUBLE: {
                    recordValueEntryBean.setDoubleValue(Double.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.DOUBLE);
                    break;
                }
                default: {
                    recordValueEntryBean.setStringValue(String.valueOf(value));
                    recordValueEntryBean.setType(RecordValueEntryBean.STRING);
                }
            }
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

    public static Event getEvent(EventBean eventBean)
            throws AnalyticsWebServiceException {
        Event event = new Event();
        event.setStreamId(eventBean.getStreamId());
        event.setTimeStamp(eventBean.getTimeStamp());
        event.setMetaData(getEventData(eventBean.getMetaData()));
        event.setCorrelationData(getEventData(eventBean.getCorrelationData()));
        event.setPayloadData(getEventData(eventBean.getPayloadData()));
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

    private static Object[] getEventData(RecordValueEntryBean[] valueEntryBeans)
            throws AnalyticsWebServiceException {
        List<Object> values = new ArrayList<>();
        if (valueEntryBeans != null) {
            for (RecordValueEntryBean bean : valueEntryBeans) {
                if (bean == null) {
                    values.add(Constants.STR_NULL);
                } else {
                    switch (bean.getType()) {
                        case RecordValueEntryBean.INTEGER:
                            values.add(bean.getIntValue());
                            break;
                        case RecordValueEntryBean.DOUBLE:
                            values.add(bean.getDoubleValue());
                            break;
                        case RecordValueEntryBean.FLOAT:
                            values.add(bean.getFloatValue());
                            break;
                        case RecordValueEntryBean.BOOLEAN:
                            values.add(bean.getBooleanValue());
                            break;
                        case RecordValueEntryBean.LONG:
                            values.add(bean.getLongValue());
                            break;
                        case RecordValueEntryBean.STRING:
                            values.add(bean.getStringValue());
                            break;
                        default:
                            throw new AnalyticsWebServiceException("Unkown data type found while reading event bean");
                    }
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
        if (tags != null) bean.setTags(tags.toArray(new String[tags.size()]));
        if (metaData != null) bean.setMetaData(createStreamDefAttributes(metaData));
        if (correlationData != null) bean.setCorrelationData(createStreamDefAttributes(correlationData));
        if (payloadData != null) bean.setPayloadData(createStreamDefAttributes(payloadData));
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
}
