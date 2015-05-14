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
package org.wso2.carbon.analytics.datasink;

import com.google.gson.Gson;
import org.wso2.carbon.analytics.datasink.internal.util.AnalyticsDatasinkConstants;
import org.wso2.carbon.analytics.datasink.internal.util.ServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.core.exception.StreamDefinitionStoreException;

import java.util.*;

/**
 * Analytics data service connector which actually makes the interacts with DS.
 */
public class AnalyticsDSConnector {
    private Gson gson;

    public AnalyticsDSConnector() {
        gson = new Gson();
    }

    public void addStream(int tenantId, StreamDefinition streamDefinition) throws AnalyticsException {
        String tableName = generateTableName(streamDefinition);
        ServiceHolder.getAnalyticsDataService().createTable(tenantId, tableName);
        AnalyticsSchema currentTableSchema = ServiceHolder.getAnalyticsDataService().getTableSchema(tenantId, tableName);
        AnalyticsSchema streamSchema = getSchema(streamDefinition);
        ServiceHolder.getAnalyticsDataService().setTableSchema(tenantId, tableName, getUpdatedSchemaForStream(currentTableSchema, streamSchema));
    }

    public void insertEvents(int tenantId, List<Event> events) throws StreamDefinitionStoreException,
            AnalyticsException {
        ServiceHolder.getAnalyticsDataService().put(convertEventsToRecord(tenantId, events));
    }

    private AnalyticsSchema getUpdatedSchemaForStream(AnalyticsSchema currentTableSchema, AnalyticsSchema newStreamSchema) {
        Set<Map.Entry<String, ColumnDefinition>> streamColumns = newStreamSchema.getColumns().entrySet();
        Map<String, ColumnDefinition> currentSchemaColumns = currentTableSchema.getColumns();
        if (currentSchemaColumns == null) {
            List<String> primaryKeys;
            if (currentTableSchema.getPrimaryKeys() == null) primaryKeys = new ArrayList<>();
            else primaryKeys = currentTableSchema.getPrimaryKeys();
            currentTableSchema = new AnalyticsSchema(new ArrayList<ColumnDefinition>(), primaryKeys);
            currentSchemaColumns = currentTableSchema.getColumns();
        }
        for (Map.Entry<String, ColumnDefinition> aColumn : streamColumns) {
            if (currentSchemaColumns.get(aColumn.getKey()) == null) {
                currentSchemaColumns.put(aColumn.getKey(), aColumn.getValue());
            }
        }
        return currentTableSchema;
    }

    private AnalyticsSchema getSchema(StreamDefinition streamDefinition) {
        List<ColumnDefinition> columns = new ArrayList<>();
        ColumnDefinition keyColumnDef = new ColumnDefinition(AnalyticsDatasinkConstants.STREAM_VERSION_KEY,
                AnalyticsSchema.ColumnType.STRING);
        columns.add(keyColumnDef);
        populateColumnSchema(AnalyticsDatasinkConstants.EVENT_META_DATA_TYPE,
                streamDefinition.getMetaData(), columns);
        populateColumnSchema(AnalyticsDatasinkConstants.EVENT_CORRELATION_DATA_TYPE,
                streamDefinition.getCorrelationData(), columns);
        populateColumnSchema(null, streamDefinition.getPayloadData(), columns);
        return new AnalyticsSchema(columns, new ArrayList<String>());
    }

    private String generateTableName(StreamDefinition streamDefinition) {
        String tableName = streamDefinition.getName();
        if (tableName != null && !tableName.isEmpty()) {
            tableName = tableName.replace('.', '_');
        }
        return tableName;
    }

    private List<Record> convertEventsToRecord(int tenantId, List<Event> events)
            throws StreamDefinitionStoreException, AnalyticsException {
        List<Record> records = new ArrayList<>();
        for (Event event : events) {
            long timestamp;
            StreamDefinition streamDefinition = ServiceHolder.getStreamDefinitionStoreService().
                    getStreamDefinition(event.getStreamId(), tenantId);
            String tableName = generateTableName(streamDefinition);
            AnalyticsSchema analyticsSchema = ServiceHolder.getAnalyticsDataService().getTableSchema(tenantId, tableName);
            Map<String, Object> eventAttributes = new HashMap<>();
            populateCommonAttributes(streamDefinition, analyticsSchema, eventAttributes);
            populateTypedAttributes(analyticsSchema, AnalyticsDatasinkConstants.EVENT_META_DATA_TYPE,
                    streamDefinition.getMetaData(),
                    event.getMetaData(), eventAttributes);
            populateTypedAttributes(analyticsSchema, AnalyticsDatasinkConstants.EVENT_CORRELATION_DATA_TYPE,
                    streamDefinition.getCorrelationData(),
                    event.getCorrelationData(), eventAttributes);
            populateTypedAttributes(analyticsSchema, null,
                    streamDefinition.getPayloadData(),
                    event.getPayloadData(), eventAttributes);

            if (event.getArbitraryDataMap() != null && !event.getArbitraryDataMap().isEmpty()) {
                for (String attributeName : event.getArbitraryDataMap().keySet()) {
                    String attributeKey = "_" + attributeName;
                    eventAttributes.put(attributeKey, getRecordValue(analyticsSchema, attributeKey,
                            event.getArbitraryDataMap().get(attributeName)));
                }
            }
            if (event.getTimeStamp() != 0L) {
                timestamp = event.getTimeStamp();
            } else {
                timestamp = System.currentTimeMillis();
            }

            Record record = new Record(tenantId, tableName, eventAttributes, timestamp);
            records.add(record);
        }
        return records;
    }

    private void populateTypedAttributes(AnalyticsSchema schema, String type, List<Attribute> attributes, Object[] values,
                                         Map<String, Object> eventAttribute) {
        if (attributes == null) {
            return;
        }
        int iteration = 0;
        for (Attribute attribute : attributes) {
            String attributeKey = getAttributeKey(type, attribute.getName());
            eventAttribute.put(attributeKey, getRecordValue(schema, attributeKey, values[iteration]));
            iteration++;
        }
    }

    private String getAttributeKey(String type, String attributeName) {
        if (type == null) {
            return attributeName;
        } else {
            return type + "_" + attributeName;
        }
    }

    private void populateColumnSchema(String type, List<Attribute> attributes, List<ColumnDefinition> schema) {
        if (attributes == null) {
            return;
        }
        AnalyticsSchema.ColumnType columnType;
        String columnName;
        for (Attribute attribute : attributes) {
            columnName = getAttributeKey(type, attribute.getName());
            switch (attribute.getType()) {
                case STRING:
                    columnType = AnalyticsSchema.ColumnType.STRING;
                    break;
                case BOOL:
                    columnType = AnalyticsSchema.ColumnType.BOOLEAN;
                    break;
                case DOUBLE:
                    columnType = AnalyticsSchema.ColumnType.DOUBLE;
                    break;
                case FLOAT:
                    columnType = AnalyticsSchema.ColumnType.FLOAT;
                    break;
                case INT:
                    columnType = AnalyticsSchema.ColumnType.INTEGER;
                    break;
                case LONG:
                    columnType = AnalyticsSchema.ColumnType.LONG;
                    break;
                default:
                    columnType = AnalyticsSchema.ColumnType.STRING;
            }
            schema.add(new ColumnDefinition(columnName, columnType));
        }
    }

    private void populateCommonAttributes(StreamDefinition streamDefinition, AnalyticsSchema schema,
                                          Map<String, Object> eventAttributes) {
        eventAttributes.put(AnalyticsDatasinkConstants.STREAM_VERSION_KEY, getRecordValue(schema,
                AnalyticsDatasinkConstants.STREAM_VERSION_KEY, streamDefinition.getVersion()));
    }

    public void deleteStream(int tenantId, StreamDefinition streamDefinition) throws AnalyticsException {
        ServiceHolder.getAnalyticsDataService().deleteTable(tenantId, generateTableName(streamDefinition));
    }

    private Object getRecordValue(AnalyticsSchema schema, String fieldName, Object fieldValue) {
        if (fieldValue instanceof String) {
            String fieldStrValue = (String) fieldValue;
            ColumnDefinition columnDefinition = schema.getColumns().get(fieldName);
            if (columnDefinition != null) {
                switch (columnDefinition.getType()) {
                    case FACET:
                        return gson.fromJson(fieldStrValue, List.class);
                    case STRING:
                        return fieldStrValue;
                    case BINARY:
                        return GenericUtils.serializeObject(fieldStrValue);
                    case BOOLEAN:
                        return Boolean.parseBoolean(fieldStrValue);
                    case DOUBLE:
                        return Double.parseDouble(fieldStrValue);
                    case FLOAT:
                        return Float.parseFloat(fieldStrValue);
                    case INTEGER:
                        return Integer.parseInt(fieldStrValue);
                    case LONG:
                        return Long.parseLong(fieldStrValue);
                }
            }
        }
        return fieldValue;
    }
}
