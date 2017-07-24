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
package org.wso2.carbon.analytics.eventsink.internal.util;


import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.eventsink.AnalyticsTableSchema;
import org.wso2.carbon.analytics.eventsink.exception.AnalyticsEventStoreException;
import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the class which holds the util methods to be used in the event sink operations.
 */
public class AnalyticsEventSinkUtil {

    public static String generateAnalyticsTableName(String streamName) {
        String tableName = "";
        if (streamName != null && !streamName.isEmpty()) {
            tableName = streamName.replace('.', '_');
        }
        return tableName;
    }

    public static AnalyticsSchema getAnalyticsSchema(AnalyticsTableSchema tableSchema)
            throws AnalyticsEventStoreException {
        return new AnalyticsSchema(getColumnDefinitions(tableSchema.getColumns()),
                getPrimaryKeys(tableSchema.getColumns()));
    }

    private static List<ColumnDefinition> getColumnDefinitions(List<AnalyticsTableSchema.Column> columns)
            throws AnalyticsEventStoreException {
        List<ColumnDefinition> schemaColumns = new ArrayList<>();
        if (columns != null) {
            for (AnalyticsTableSchema.Column schemaColumn : columns) {
                ColumnDefinitionExt columnDefinition = new ColumnDefinitionExt();
                columnDefinition.setName(schemaColumn.getColumnName());
                columnDefinition.setIndexed(schemaColumn.isIndexed());
                columnDefinition.setScoreParam(schemaColumn.isScoreParam());
                //This is to make backward compatible with DAS 3.0.0 and DAS 3.0.1, see DAS-402
                if (schemaColumn.getType() == AnalyticsSchema.ColumnType.FACET) {
                    columnDefinition.setType(AnalyticsSchema.ColumnType.STRING);
                    columnDefinition.setFacet(true);
                } else if (schemaColumn.getType() == null) {
                    throw new AnalyticsEventStoreException("Invalid type for field: " + schemaColumn.getColumnName() + " in eventStore configuration");
                } else {
                    columnDefinition.setType(schemaColumn.getType());
                    columnDefinition.setFacet(schemaColumn.isFacet());
                }
                schemaColumns.add(columnDefinition);
            }
        }
        return schemaColumns;
    }

    private static List<String> getPrimaryKeys(List<AnalyticsTableSchema.Column> columns) {
        List<String> primaryKeys = new ArrayList<>();
        if (columns != null) {
            for (AnalyticsTableSchema.Column schemaColumn : columns) {
                if (schemaColumn.isPrimaryKey()) {
                    primaryKeys.add(schemaColumn.getColumnName());
                }
            }
        }
        return primaryKeys;
    }

    public static String getAnalyticsEventStoreName(String deploymentFileName) {
        deploymentFileName = GenericUtils.checkAndReturnPath(deploymentFileName);
        if (deploymentFileName.contains(AnalyticsEventSinkConstants.DEPLOYMENT_FILE_EXT)) {
            return deploymentFileName.substring(0, deploymentFileName.length() -
                    AnalyticsEventSinkConstants.DEPLOYMENT_FILE_EXT.length());
        }
        return deploymentFileName;
    }

    public static AnalyticsEventStore getAnalyticsEventStore(String streamName, String version, AnalyticsSchema schema,
                                                             String recordStoreName)
            throws AnalyticsEventStoreException {
        AnalyticsEventStore store = new AnalyticsEventStore();
        AnalyticsEventStore.EventSource eventSource = new AnalyticsEventStore.EventSource();
        List<String> streams = new ArrayList<>();
        streams.add(DataBridgeCommonsUtils.generateStreamId(streamName, version));
        eventSource.setStreamIds(streams);
        store.setEventSource(eventSource);
        store.setAnalyticsTableSchema(getAnalyticsTableSchema(schema));
        store.setRecordStore(recordStoreName);
        return store;
    }

    private static AnalyticsTableSchema getAnalyticsTableSchema(AnalyticsSchema schema)
            throws AnalyticsEventStoreException {
        AnalyticsTableSchema tableSchema = new AnalyticsTableSchema();
        List<AnalyticsTableSchema.Column> columns = new ArrayList<>();
        Set<Map.Entry<String, ColumnDefinition>> columnDefs = schema.getColumns().entrySet();
        for (Map.Entry<String, ColumnDefinition> column : columnDefs) {
            AnalyticsTableSchema.Column analyticsColumn = new AnalyticsTableSchema.Column();
            analyticsColumn.setColumnName(column.getKey());
            analyticsColumn.setIndexed(column.getValue().isIndexed());
            analyticsColumn.setScoreParam(column.getValue().isScoreParam());
            if (column.getValue().getType() != null) {
                analyticsColumn.setType(column.getValue().getType());
            } else {
                throw new AnalyticsEventStoreException("Invalid type for field: " + column.getKey() + " in eventStore configuration");
            }
            analyticsColumn.setFacet(column.getValue().isFacet());
            if (schema.getPrimaryKeys().contains(column.getKey())) {
                analyticsColumn.setPrimaryKey(true);
            }
            columns.add(analyticsColumn);
        }
        tableSchema.setColumns(columns);
        return tableSchema;
    }

    public static AnalyticsEventStore copyAnalyticsEventStore(AnalyticsEventStore analyticsEventStore)
            throws AnalyticsEventStoreException {
        AnalyticsEventStore copyStore = new AnalyticsEventStore();
        copyStore.setEventSource(copyEventSource(analyticsEventStore.getEventSource()));
        copyStore.setAnalyticsTableSchema(copyAnalyticsTableSchema(analyticsEventStore.getAnalyticsTableSchema()));
        return copyStore;
    }

    private static AnalyticsEventStore.EventSource copyEventSource(AnalyticsEventStore.EventSource eventSource) {
        AnalyticsEventStore.EventSource copyEventSource = new AnalyticsEventStore.EventSource();
        List<String> streamIds = new ArrayList<>();
        for (String streamId : eventSource.getStreamIds()) {
            streamIds.add(streamId);
        }
        copyEventSource.setStreamIds(streamIds);
        return copyEventSource;
    }

    private static AnalyticsTableSchema copyAnalyticsTableSchema(AnalyticsTableSchema analyticsTableSchema)
            throws AnalyticsEventStoreException {
        AnalyticsTableSchema copySchema = new AnalyticsTableSchema();
        List<AnalyticsTableSchema.Column> columns = new ArrayList<>();
        for (AnalyticsTableSchema.Column originalCol : analyticsTableSchema.getColumns()) {
            AnalyticsTableSchema.Column column = new AnalyticsTableSchema.Column();
            column.setColumnName(originalCol.getColumnName());
            column.setIndexed(originalCol.isIndexed());
            column.setPrimaryKey(originalCol.isPrimaryKey());
            column.setScoreParam(originalCol.isScoreParam());
            //This is to make backward compatible with DAS 3.0.0 and DAS 3.0.1, see DAS-402
            if (originalCol.getType() == AnalyticsSchema.ColumnType.FACET) {
                column.setType(AnalyticsSchema.ColumnType.STRING);
                column.setFacet(true);
            } else if (originalCol.getType() == null) {
                throw new AnalyticsEventStoreException("Invalid type for field: " + originalCol.getColumnName() + " in eventStore configuration");
            } else {
                column.setType(originalCol.getType());
                column.setFacet(originalCol.isFacet());
            }
            columns.add(column);
        }
        copySchema.setColumns(columns);
        return copySchema;
    }

    /**
     * Converts a List of Stream IDs to a String of comma-separated stream IDs.
     * @param streamIds List of Stream IDs
     * @return stream IDs as a comma separated list.
     */
    public static String getEventSources(List<String> streamIds) {
        if (streamIds != null && !streamIds.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder("");
            for (String streamId : streamIds) {
                stringBuilder.append(streamId).append(", ");
            }
            if (stringBuilder.length() - 2 ==  stringBuilder.lastIndexOf(", ")) {
                stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length() - 1);
            }
            if (stringBuilder.length() > 0) {
                return stringBuilder.toString();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
