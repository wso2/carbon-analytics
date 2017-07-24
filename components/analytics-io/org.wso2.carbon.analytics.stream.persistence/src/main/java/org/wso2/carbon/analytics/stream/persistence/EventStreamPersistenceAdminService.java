/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.stream.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsTableNotAvailableException;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.stream.persistence.dto.AnalyticsTable;
import org.wso2.carbon.analytics.stream.persistence.dto.AnalyticsTableRecord;
import org.wso2.carbon.analytics.stream.persistence.exception.EventStreamPersistenceAdminServiceException;
import org.wso2.carbon.analytics.stream.persistence.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represent Event Stream Persistence admin operations
 */
public class EventStreamPersistenceAdminService extends AbstractAdmin {

    private static Log log = LogFactory.getLog(EventStreamPersistenceAdminService.class);

    /**
     * This method use to check whether back-end is available for the UI to show analytics table related operation
     *
     * @return true if back-end service available
     */
    public boolean isBackendServicePresent() {
        return true;
    }

    /**
     * This method is use to get Analytics table information for given stream name without considering the version
     *
     * @param streamName stream name
     * @param version stream version
     * @return AnalyticsTable instance with column details
     */
    public AnalyticsTable getAnalyticsTable(String streamName, String version)
            throws EventStreamPersistenceAdminServiceException {
        if (log.isDebugEnabled()) {
            log.debug("Getting analytics schema for stream: " + streamName);
        }
        AnalyticsTable analyticsTable = new AnalyticsTable();
        try {
            // Check whether stream is already available in the file system. There can be situation that we have a
            // analytics schema but we don't have actual stream available now (Previously we had a stream and due to
            // that we have schema. But we removed the stream and still we have orphan schema). If we do have a orphan
            // schema then we are ignoring that.
            boolean streamAlreadyExist = isStreamExist(streamName);
            if (streamAlreadyExist) {
                AnalyticsDataService analyticsDataService = ServiceHolder.getAnalyticsDataService();
                String tableName = getTableName(streamName);
                if (analyticsDataService.tableExists(getTenantId(), tableName)) {
                    AnalyticsSchema tableSchema = analyticsDataService.getTableSchema(getTenantId(), tableName);
                    analyticsTable.setTableName(tableName);
                    if (tableSchema != null && tableSchema.getColumns() != null) {
                        AnalyticsTableRecord[] tableColumns = new AnalyticsTableRecord[tableSchema.getColumns().size()];
                        List<String> primaryKeys = tableSchema.getPrimaryKeys();
                        int i = 0;
                        for (Map.Entry<String, ColumnDefinition> columnDefinitionEntry : tableSchema.getColumns().entrySet()) {
                            AnalyticsTableRecord analyticsTableRecord = new AnalyticsTableRecord();
                            analyticsTableRecord.setColumnName(columnDefinitionEntry.getValue().getName());
                            analyticsTableRecord.setColumnType(columnDefinitionEntry.getValue().getType().name());
                            analyticsTableRecord.setIndexed(columnDefinitionEntry.getValue().isIndexed());
                            analyticsTableRecord.setPrimaryKey(primaryKeys.contains(columnDefinitionEntry.getKey()));
                            analyticsTableRecord.setFacet(columnDefinitionEntry.getValue().isFacet());
                            analyticsTableRecord.setScoreParam(columnDefinitionEntry.getValue().isScoreParam());
                            tableColumns[i++] = analyticsTableRecord;
                        }
                        analyticsTable.setAnalyticsTableRecords(tableColumns);
                        AnalyticsEventStore eventStore = ServiceHolder.getAnalyticsEventSinkService().
                                getEventStore(getTenantId(), streamName);
                        if (eventStore != null && eventStore.getEventSource() != null) {
                            analyticsTable.setMergeSchema(eventStore.isMergeSchema());
                            List<String> streamIds = eventStore.getEventSource().getStreamIds();
                            if (streamIds != null && streamIds.contains(streamName + ":" + version)) {
                                analyticsTable.setPersist(true);
                            }
                        }
                        analyticsTable.setRecordStoreName(analyticsDataService.getRecordStoreNameByTable(getTenantId(),
                                                                                                         tableName));
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Analytics table does not exist for stream[" + streamName + "]");
                    }
                }
            }
            if (!streamAlreadyExist && log.isDebugEnabled()) {
                log.debug("Stream[" + streamName + "] not existing in file system. Retuning empty AnalyticsTable " +
                          "object.");
            }
        } catch (Exception e) {
            log.error("Unable to get analytics schema[" + streamName + "]: " + e.getMessage(), e);
            throw new EventStreamPersistenceAdminServiceException("Unable to get analytics schema", e);
        }
        return analyticsTable;
    }

    private boolean isStreamExist(String streamName) throws EventStreamConfigurationException {
        EventStreamService eventStreamService = ServiceHolder.getEventStreamService();
        List<String> streamIds = eventStreamService.getStreamIds();
        boolean streamAlreadyExist = false;
        for (String streamId : streamIds) {
            if (streamName.equals(streamId.substring(0, streamId.lastIndexOf(':')))) {
                streamAlreadyExist = true;
                if (log.isDebugEnabled()) {
                    log.debug("Existing stream[" + streamId + "] matching with given stream[" + streamName + "]");
                }
                break;
            }
        }
        return streamAlreadyExist;
    }

    /**
     * This method will create a deployable artifact for given AnalyticsTable information.
     *
     * @param analyticsTable AnalyticsTable object with index and type information
     */
    public void addAnalyticsTable(AnalyticsTable analyticsTable) throws EventStreamPersistenceAdminServiceException {
        if (analyticsTable != null) {
            if (log.isDebugEnabled()) {
                log.debug("Saving analytics schema: " + analyticsTable.getTableName());
            }
            if (analyticsTable.getAnalyticsTableRecords() != null) {
                if (analyticsTable.isPersist()) {
                    try {
                        String tableName = getTableName(analyticsTable.getTableName());
                        AnalyticsDataService analyticsDataService = ServiceHolder.getAnalyticsDataService();
                        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
                        List<String> primaryKeys = new ArrayList<>();
                        try {
                            AnalyticsSchema tableSchema = analyticsDataService.getTableSchema(getTenantId(), tableName);
                            if ((isStreamExist(analyticsTable.getTableName()) || analyticsTable.isMergeSchema()) && tableSchema != null) {
                                Map<String, ColumnDefinition> columns = tableSchema.getColumns();
                                removeArbitraryField(columns);
                                AnalyticsTableRecord[] analyticsTableRecords = analyticsTable.getAnalyticsTableRecords();
                                for (AnalyticsTableRecord analyticsTableRecord : analyticsTableRecords) {
                                    if (columns != null && columns.containsKey(analyticsTableRecord.getColumnName())) {
                                        columns.remove(analyticsTableRecord.getColumnName());
                                    }
                                    if (analyticsTableRecord.isPersist()) {
                                        ColumnDefinition columnDefinition = getColumnDefinition(analyticsTableRecord);
                                        columnDefinitions.add(columnDefinition);
                                        if (analyticsTableRecord.isPrimaryKey()) {
                                            primaryKeys.add(analyticsTableRecord.getColumnName());
                                        }
                                    }
                                }
                                if (columns != null) {
                                    columnDefinitions.addAll(columns.values());
                                }
                            }
                        } catch (AnalyticsTableNotAvailableException ex) {
                            for (AnalyticsTableRecord analyticsTableRecord : analyticsTable.getAnalyticsTableRecords()) {
                                if (analyticsTableRecord.isPersist()) {
                                    ColumnDefinition columnDefinition = getColumnDefinition(analyticsTableRecord);
                                    columnDefinitions.add(columnDefinition);
                                    if (analyticsTableRecord.isPrimaryKey()) {
                                        primaryKeys.add(analyticsTableRecord.getColumnName());
                                    }
                                }
                            }
                        }
                        AnalyticsSchema schema = new AnalyticsSchema(columnDefinitions, primaryKeys);
                        ServiceHolder.getAnalyticsEventSinkService().putEventSinkWithSchemaMergeInfo(getTenantId(), analyticsTable
                                        .getTableName(), analyticsTable.getStreamVersion(), schema,
                                analyticsTable.getRecordStoreName(), analyticsTable.isMergeSchema());
                    } catch (Exception e) {
                        log.error("Unable to save analytics schema[" + analyticsTable.getTableName() + "]: " + e.getMessage(), e);
                        throw new EventStreamPersistenceAdminServiceException("Unable to save analytics schema", e);
                    }
                } else {
                    removeExistingEventSink(analyticsTable);
                }
            }
        }
    }

    private void removeExistingEventSink(AnalyticsTable analyticsTable)
            throws EventStreamPersistenceAdminServiceException {
        try {
            AnalyticsEventStore eventStore = ServiceHolder.getAnalyticsEventSinkService().getEventStore(getTenantId(), analyticsTable.getTableName());
            if (eventStore != null && eventStore.getEventSource() != null) {
                ServiceHolder.getAnalyticsEventSinkService().removeEventSink(getTenantId(),
                                                                             analyticsTable.getTableName(),
                                                                             analyticsTable.getStreamVersion());
            }
        } catch (Exception e) {
            log.error("Unable to save analytics schema[" + analyticsTable.getTableName() + "]: " + e.getMessage(), e);
            throw new EventStreamPersistenceAdminServiceException("Unable to save analytics schema", e);
        }
    }

    private void removeArbitraryField(Map<String, ColumnDefinition> columns) {
        if (columns != null) {
            // Removing all arbitrary fields
            Iterator<String> iterator = columns.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                if (key.startsWith("_")) {
                    iterator.remove();
                }
            }
        }
    }

    private ColumnDefinition getColumnDefinition(AnalyticsTableRecord analyticsTableRecord) {
        ColumnDefinitionExt columnDefinition = new ColumnDefinitionExt();
        columnDefinition.setName(analyticsTableRecord.getColumnName());
        if ("FACET".equals(analyticsTableRecord.getColumnType())) {
            columnDefinition.setFacet(true);
            columnDefinition.setType(AnalyticsSchema.ColumnType.STRING);
        } else {
            columnDefinition.setFacet(analyticsTableRecord.isFacet());
            columnDefinition.setType(getColumnType(analyticsTableRecord.getColumnType()));
        }
        columnDefinition.setIndexed(analyticsTableRecord.isIndexed());
        columnDefinition.setScoreParam(analyticsTableRecord.isScoreParam());
        return columnDefinition;
    }

    private int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    private AnalyticsSchema.ColumnType getColumnType(String type) {
        switch (type) {
            case "STRING":
                return AnalyticsSchema.ColumnType.STRING;
            case "INTEGER":
                return AnalyticsSchema.ColumnType.INTEGER;
            case "LONG":
                return AnalyticsSchema.ColumnType.LONG;
            case "BOOLEAN":
                return AnalyticsSchema.ColumnType.BOOLEAN;
            case "FLOAT":
                return AnalyticsSchema.ColumnType.FLOAT;
            case "DOUBLE":
                return AnalyticsSchema.ColumnType.DOUBLE;
            default:
                return AnalyticsSchema.ColumnType.STRING;
        }
    }

    private String getTableName(String streamName) {
        String tableName = streamName;
        if (tableName != null && !tableName.isEmpty()) {
            tableName = tableName.replace('.', '_');
        }
        return tableName;
    }

    /**
     * This method will return all the available records store names;
     *
     * @return Array of String that contains the record store names
     */
    public String[] listRecordStoreNames() {
        List<String> recordStoreNames = ServiceHolder.getAnalyticsDataService().listRecordStoreNames();
        return ServiceHolder.getAnalyticsDataService().listRecordStoreNames().toArray(new String[recordStoreNames
                .size()]);
    }
}
