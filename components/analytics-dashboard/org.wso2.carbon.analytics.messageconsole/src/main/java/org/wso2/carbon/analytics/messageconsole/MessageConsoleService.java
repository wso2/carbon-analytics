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
package org.wso2.carbon.analytics.messageconsole;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.dataservice.AuthorizationUtils;
import org.wso2.carbon.analytics.dataservice.Constants;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.messageconsole.beans.ColumnBean;
import org.wso2.carbon.analytics.messageconsole.beans.EntityBean;
import org.wso2.carbon.analytics.messageconsole.beans.PermissionBean;
import org.wso2.carbon.analytics.messageconsole.beans.RecordBean;
import org.wso2.carbon.analytics.messageconsole.beans.RecordResultBean;
import org.wso2.carbon.analytics.messageconsole.beans.ScheduleTaskInfo;
import org.wso2.carbon.analytics.messageconsole.beans.TableBean;
import org.wso2.carbon.analytics.messageconsole.exception.MessageConsoleException;
import org.wso2.carbon.analytics.messageconsole.internal.ServiceHolder;
import org.wso2.carbon.analytics.messageconsole.purging.AnalyticsDataPurgingTask;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.user.api.AuthorizationManager;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is the service class for message console. This represent all the message console backend operation.
 */

public class MessageConsoleService extends AbstractAdmin {

    private static final Log logger = LogFactory.getLog(MessageConsoleService.class);
    private static final String LUCENE = "lucene";
    private static final String STRING = "STRING";
    private static final String INTEGER = "INTEGER";
    private static final String LONG = "LONG";
    private static final String FLOAT = "FLOAT";
    private static final String DOUBLE = "DOUBLE";
    private static final String BOOLEAN = "BOOLEAN";
    public static final String AT_SIGN = "@";

    private SecureAnalyticsDataService analyticsDataService;

    public MessageConsoleService() {
        this.analyticsDataService = ServiceHolder.getSecureAnalyticsDataService();
    }

    /**
     * This method will return PermissionBean that contains permissions regarding message console operations for
     * logged in user.
     *
     * @return PermissionBean instance
     * @throws MessageConsoleException
     */
    public PermissionBean getAvailablePermissions() throws MessageConsoleException {
        PermissionBean permission = new PermissionBean();
        String username = super.getUsername();
        try {
            AuthorizationManager authorizationManager = getUserRealm().getAuthorizationManager();
            permission.setCreateTable(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_CREATE_TABLE,
                                                                            CarbonConstants.UI_PERMISSION_ACTION));
            permission.setListTable(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_LIST_TABLE,
                                                                          CarbonConstants.UI_PERMISSION_ACTION));
            permission.setDropTable(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_DROP_TABLE,
                                                                          CarbonConstants.UI_PERMISSION_ACTION));
            permission.setSearchRecord(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_SEARCH_RECORD,
                                                                             CarbonConstants.UI_PERMISSION_ACTION));
            permission.setListRecord(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_LIST_RECORD,
                                                                           CarbonConstants.UI_PERMISSION_ACTION));
            permission.setPutRecord(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_PUT_RECORD,
                                                                          CarbonConstants.UI_PERMISSION_ACTION));
            permission.setDeleteRecord(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_DELETE_RECORD,
                                                                             CarbonConstants.UI_PERMISSION_ACTION));
            permission.setSetIndex(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_SET_INDEXING,
                                                                         CarbonConstants.UI_PERMISSION_ACTION));
            permission.setGetIndex(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_GET_INDEXING,
                                                                         CarbonConstants.UI_PERMISSION_ACTION));
            permission.setDeleteIndex(authorizationManager.isUserAuthorized(username, Constants.PERMISSION_DELETE_INDEXING,
                                                                            CarbonConstants.UI_PERMISSION_ACTION));
        } catch (UserStoreException e) {
            throw new MessageConsoleException("Unable to get user permission details due to " + e.getMessage(), e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Granted analytics permission for user[" + username + "] :" + permission.toString());
        }

        return permission;
    }

    /**
     * This will list of name of all the tables.
     *
     * @return String list that contains table names.
     * @throws MessageConsoleException
     */
    public List<String> listTables() throws MessageConsoleException {
        String username = getUsername();
        if (logger.isDebugEnabled()) {
            logger.debug("Getting table list from data layer for user:" + username);
        }
        try {
            return analyticsDataService.listTables(username);
        } catch (Exception e) {
            logger.error("Unable to get table list from Analytics data layer for user: " + username, e);
            throw new MessageConsoleException("Unable to get table list from Analytics data layer for user: " +
                                              username, e);
        }
    }

    /**
     * This method will use to get search result from analytics data service. Search query can be either time range
     * search or lucene based search.
     *
     * @param tableName   Table name
     * @param timeFrom    Starting time that require to search begin
     * @param timeTo      End time that require to search end
     * @param startIndex  Staring index of search records
     * @param recordCount Requested record count
     * @param searchQuery Lucene search query
     * @return RecordResultBean instance that contains total count of records that satisfied the search criteria and
     * the array of RecordBean that less than or equal to record count
     * @throws MessageConsoleException
     */
    public RecordResultBean getRecords(String tableName, long timeFrom, long timeTo, int startIndex, int recordCount,
                                       String searchQuery)
            throws MessageConsoleException {
        if (logger.isDebugEnabled()) {
            logger.debug("Search Query: " + searchQuery);
            logger.debug("timeFrom: " + timeFrom);
            logger.debug("timeTo: " + timeTo);
            logger.debug("Start Index: " + startIndex);
            logger.debug("Page Size: " + recordCount);
        }

        String username = getUsername();
        RecordResultBean recordResult = new RecordResultBean();
        RecordGroup[] results;
        long searchCount = 0;
        if (searchQuery != null && !searchQuery.isEmpty()) {
            try {
                List<SearchResultEntry> searchResults = analyticsDataService.search(username, tableName, LUCENE,
                                                                                    searchQuery, startIndex, recordCount);
                List<String> ids = getRecordIds(searchResults);
                results = analyticsDataService.get(username, tableName, 1, null, ids);
                searchCount = analyticsDataService.searchCount(username, tableName, LUCENE, searchQuery);
                if (logger.isDebugEnabled()) {
                    logger.debug("Query satisfied result count: " + searchResults.size());
                }
            } catch (Exception e) {
                logger.error("Unable to get search indices from Analytics data layer for tenant: " + username +
                             " and for table:" + tableName, e);
                throw new MessageConsoleException("Unable to get indices from Analytics data layer for tenant: " + username +
                                                  " and for table:" + tableName, e);
            }
        } else {
            try {
                results = analyticsDataService.get(username, tableName, 1, null, timeFrom, timeTo, startIndex, recordCount);
                searchCount = analyticsDataService.getRecordCount(username, tableName, timeFrom, timeTo);
            } catch (Exception e) {
                logger.error("Unable to get records from Analytics data layer for tenant: " + username +
                             " and for table:" + tableName, e);
                throw new MessageConsoleException("Unable to get records from Analytics data layer for tenant: " + username +
                                                  " and for table:" + tableName, e);
            }
        }
        if (results != null) {
            List<RecordBean> recordBeanList = new ArrayList<>();
            List<Record> records;
            try {
                records = GenericUtils.listRecords(analyticsDataService, results);
            } catch (Exception e) {
                logger.error("Unable to convert result to record for tenant: " + username +
                             " and for table:" + tableName, e);
                throw new MessageConsoleException("Unable to convert result to record for tenant: " + username +
                                                  " and for table:" + tableName, e);
            }
            if (records != null && !records.isEmpty()) {
                for (Record record : records) {
                    recordBeanList.add(createRecordBean(record));
                }
            }
            RecordBean[] recordBeans = new RecordBean[recordBeanList.size()];
            recordResult.setRecords(recordBeanList.toArray(recordBeans));
            recordResult.setTotalResultCount(searchCount);
        }
        return recordResult;
    }

    private RecordBean createRecordBean(Record record) {
        RecordBean recordBean = new RecordBean();
        recordBean.setRecordId(record.getId());
        recordBean.setTimestamp(record.getTimestamp());
        EntityBean[] entityBeans = new EntityBean[record.getValues().size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : record.getValues().entrySet()) {
            EntityBean entityBean = new EntityBean(entry.getKey(), String.valueOf(entry.getValue()));
            entityBeans[i++] = entityBean;
        }
        recordBean.setEntityBeans(entityBeans);
        return recordBean;
    }

    private List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<String>(searchResults.size());
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

    /**
     * Return table meta information for given table name
     *
     * @param tableName The table name
     * @return TableBean instance that contains array of ColumnBean
     * @throws MessageConsoleException
     */
    public TableBean getTableInfo(String tableName) throws MessageConsoleException {
        String username = getUsername();
        TableBean table = new TableBean();
        table.setName(tableName);
        try {
            AnalyticsSchema schema = analyticsDataService.getTableSchema(username, tableName);
            if (schema != null && schema.getColumns() != null) {
                ColumnBean[] columns = new ColumnBean[schema.getColumns().size()];

                Map<String, AnalyticsSchema.ColumnType> columnTypeMap = schema.getColumns();
                int i = 0;
                for (Map.Entry<String, AnalyticsSchema.ColumnType> stringColumnTypeEntry : columnTypeMap.entrySet()) {
                    ColumnBean column = new ColumnBean();
                    column.setName(stringColumnTypeEntry.getKey());
                    column.setType(stringColumnTypeEntry.getValue().name());
                    column.setPrimary(schema.getPrimaryKeys().contains(stringColumnTypeEntry.getKey()));
                    columns[i++] = column;
                }
                table.setColumns(columns);
            }
        } catch (Exception e) {
            logger.error("Unable to get schema information for table :" + tableName, e);
            throw new MessageConsoleException("Unable to get schema information for table :" + tableName, e);
        }
        return table;
    }

    /**
     * This method will return table meta information with indices information.
     *
     * @param tableName The table name
     * @return TableBean instance that contains array of ColumnBean
     * @throws MessageConsoleException
     */
    public TableBean getTableInfoWithIndicesInfo(String tableName) throws MessageConsoleException {
        TableBean tableBean = getTableInfo(tableName);
        String username = getUsername();
        try {
            if (AuthorizationUtils.isUserAuthorized(getTenantId(), username, Constants.PERMISSION_GET_INDEXING)) {
                Map<String, IndexType> indices = analyticsDataService.getIndices(username, tableName);
                for (ColumnBean columnBean : tableBean.getColumns()) {
                    if (indices.containsKey(columnBean.getName())) {
                        columnBean.setIndex(true);
                    }
                }
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to get indices information for table :" + tableName, e);
            throw new MessageConsoleException("Unable to get indices information for table :" + tableName, e);
        }
        return tableBean;
    }

    /**
     * This operation will remove records from given table with provided record ids.
     *
     * @param table     The table name
     * @param recordIds List of record ids
     * @throws MessageConsoleException
     */
    public void deleteRecords(String table, String[] recordIds) throws MessageConsoleException {
        String username = getUsername();
        String ids = Arrays.toString(recordIds);
        if (logger.isDebugEnabled()) {
            logger.debug(ids + " are going to delete from " + table + " in tenant:" + username);
        }
        try {
            analyticsDataService.delete(username, table, Arrays.asList(recordIds));
        } catch (Exception e) {
            logger.error("Unable to delete records" + ids + " from table :" + table, e);
            throw new MessageConsoleException("Unable to delete records" + ids + " from table :" + table, e);
        }
    }

    /**
     * This method  will use to add record to given table.
     *
     * @param table   The table name
     * @param columns String array that consists of column name
     * @param values  String array that consists of values
     * @return This return RecordBean instance that contains generated record id + values that persisted
     * @throws MessageConsoleException
     */
    public RecordBean addRecord(String table, String[] columns, String[] values) throws MessageConsoleException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String username = getUsername();
        if (logger.isDebugEnabled()) {
            logger.debug("New record {column: " + Arrays.toString(columns) + ", values: " + Arrays.toString(values) +
                         "} going to add to" + table);
        }
        RecordBean recordBean;
        try {
            Map<String, Object> objectMap = getRecordPropertyMap(table, columns, values, username);
            Record record = new Record(tenantId, table, objectMap, System.currentTimeMillis());
            recordBean = createRecordBean(record);
            List<Record> records = new ArrayList<>(1);
            records.add(record);
            analyticsDataService.put(username, records);
            recordBean.setRecordId(records.get(0).getId());
        } catch (Exception e) {
            logger.error("Unable to add record {column: " + Arrays.toString(columns) + ", values: " + Arrays.toString(values) + " } to table :" + table, e);
            throw new MessageConsoleException("Unable to add record {column: " + Arrays.toString(columns) + ", values: " + Arrays.toString(values) + " } to table :" + table, e);
        }
        return recordBean;
    }

    /**
     * This method will use to update single record in given table.
     *
     * @param table    The table name
     * @param recordId RecordId that going to update
     * @param columns  String array that consists of column name
     * @param values   String array that consists of values
     * @return RecordBean instance that contains updated values
     * @throws MessageConsoleException
     */
    public RecordBean updateRecord(String table, String recordId, String[] columns, String[] values)
            throws MessageConsoleException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String username = getUsername();
        if (logger.isDebugEnabled()) {
            logger.debug("Record {id: " + recordId + ", column: " + Arrays.toString(columns) + ", values: " + Arrays.toString
                    (values) + "} going to update to" + table);
        }
        RecordBean recordBean = new RecordBean();
        try {
            Record originalRecord = getRecord(table, recordId, username);
            if (originalRecord != null) {
                Map<String, Object> objectMap = getRecordPropertyMap(table, columns, values, username);
                for (Map.Entry<String, Object> newEntry : objectMap.entrySet()) {
                    if (originalRecord.getValues().containsKey(newEntry.getKey())) {
                        originalRecord.getValues().put(newEntry.getKey(), newEntry.getValue());
                    }
                }
                Record record = new Record(recordId, tenantId, table, originalRecord.getValues(), System.currentTimeMillis());
                recordBean = createRecordBean(record);
                List<Record> records = new ArrayList<>(1);
                records.add(record);
                analyticsDataService.put(username, records);
            }
        } catch (Exception e) {
            logger.error("Unable to update record {id: " + recordId + ", column: " + Arrays.toString(columns) + ", " +
                         "values: " + Arrays.toString(values) + " } to table :" + table, e);
            throw new MessageConsoleException("Unable to update record {id: " + recordId + ", column: " + Arrays
                    .toString(columns) + ", values: " + Arrays.toString(values) + " } to table :" + table, e);
        }
        return recordBean;
    }

    private Map<String, Object> getRecordPropertyMap(String table, String[] columns, String[] values, String username)
            throws AnalyticsException {
        Map<String, Object> objectMap = new HashMap<>();
        AnalyticsSchema schema = analyticsDataService.getTableSchema(username, table);
        if (schema != null && schema.getColumns() != null) {
            Map<String, AnalyticsSchema.ColumnType> columnsMetaInfo = schema.getColumns();
            if (columns != null) {
                for (int i = 0; i < columns.length; i++) {
                    String columnName = columns[i];
                    String stringValue = values[i];
                    if (columnName != null) {
                        AnalyticsSchema.ColumnType columnType = columnsMetaInfo.get(columnName);
                        Object value = getObject(stringValue, columnType.name());
                        objectMap.put(columnName, value);
                    }
                }
            }
        }
        return objectMap;
    }

    /**
     * This method will use to get all the arbitrary values for given record. This will return all the columns that
     * not mention in table schema
     *
     * @param table    The table name
     * @param recordId RecordId that need to retrieve arbitrary fields.
     * @return Array of EntityBean that contains values of arbitrary fields.
     * @throws MessageConsoleException
     */
    public EntityBean[] getArbitraryList(String table, String recordId) throws MessageConsoleException {
        String username = getUsername();
        List<EntityBean> entityBeansList = new ArrayList<>();
        try {
            Record originalRecord = getRecord(table, recordId, username);
            AnalyticsSchema schema = analyticsDataService.getTableSchema(username, table);
            if (originalRecord != null) {
                Map<String, Object> recordValues = originalRecord.getValues();
                if (schema != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Table schema[" + table + "] is not null");
                    }
                    Map<String, AnalyticsSchema.ColumnType> schemaColumns = schema.getColumns();
                    if (schemaColumns != null && !schemaColumns.isEmpty()) {
                        for (Map.Entry<String, Object> objectEntry : recordValues.entrySet()) {
                            if (!schemaColumns.containsKey(objectEntry.getKey())) {
                                entityBeansList.add(getEntityBean(objectEntry));
                            }
                        }
                    }
                }
                if (entityBeansList.isEmpty()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Either table schema[" + table + "] null or empty. Adding all records to " +
                                     "arbitrary list");
                    }
                    for (Map.Entry<String, Object> objectEntry : recordValues.entrySet()) {
                        entityBeansList.add(getEntityBean(objectEntry));
                    }
                }
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to get arbitrary fields for id [" + recordId + "] from table :" + table, e);
            throw new MessageConsoleException("Unable to get arbitrary fields for id [" + recordId + "] from table :" + table, e);
        }
        EntityBean[] entityBeans = new EntityBean[entityBeansList.size()];
        return entityBeansList.toArray(entityBeans);
    }

    private EntityBean getEntityBean(Map.Entry<String, Object> objectEntry) {
        EntityBean entityBean;
        if (objectEntry.getValue() != null) {
            entityBean = new EntityBean(objectEntry.getKey(), String.valueOf(objectEntry.getValue()), objectEntry
                    .getValue().getClass().getSimpleName().toUpperCase());

        } else {
            entityBean = new EntityBean(objectEntry.getKey(), "NULL", STRING);
        }
        return entityBean;
    }

    private Record getRecord(String table, String recordId, String username) throws AnalyticsException {
        List<String> ids = new ArrayList<>(1);
        ids.add(recordId);
        RecordGroup[] results = analyticsDataService.get(username, table, 1, null, ids);
        List<Record> records = GenericUtils.listRecords(analyticsDataService, results);
        if (!records.isEmpty()) {
            return records.get(0);
        }
        return null;
    }

    /**
     * This method will remove given arbitrary field from given record
     *
     * @param table     The table name
     * @param recordId  Record Id of the record
     * @param fieldName Arbitrary field name
     * @throws MessageConsoleException
     */
    public void deleteArbitraryField(String table, String recordId, String fieldName) throws MessageConsoleException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String username = getUsername();
        try {
            Record record = getRecord(table, recordId, username);
            if (record != null) {
                Map<String, Object> recordValues = record.getValues();
                recordValues.remove(fieldName);
                Record editedRecord = new Record(recordId, tenantId, table, recordValues, System.currentTimeMillis());
                List<Record> records = new ArrayList<>(1);
                records.add(editedRecord);
                analyticsDataService.put(username, records);
                if (logger.isDebugEnabled()) {
                    logger.debug("Arbitrary field[" + fieldName + "] removed from record[" + recordId + "] in the" +
                                 table + " successfully.");
                }
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to delete arbitrary field[" + fieldName + "] for id [" + recordId + "] from table :" + table, e);
            throw new MessageConsoleException("Unable to arbitrary arbitrary field[" + fieldName + "] for id [" + recordId + "] from table :" + table, e);
        }
    }

    /**
     * This method will add or update arbitrary values in given table in give record.
     *
     * @param table     The table name
     * @param recordId  Record Id of the record
     * @param fieldName Arbitrary field name
     * @param value     Arbitrary field value
     * @param type      Arbitrary field type
     * @throws MessageConsoleException
     */
    public void putArbitraryField(String table, String recordId, String fieldName, String value, String type)
            throws MessageConsoleException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        String username = getUsername();
        try {
            Record record = getRecord(table, recordId, username);
            if (record != null) {
                Map<String, Object> recordValues = record.getValues();
                recordValues.remove(fieldName);
                Object convertedValue = getObject(value, type);
                recordValues.put(fieldName, convertedValue);
                Record editedRecord = new Record(recordId, tenantId, table, recordValues, System.currentTimeMillis());
                List<Record> records = new ArrayList<>(1);
                records.add(editedRecord);
                analyticsDataService.put(username, records);
                if (logger.isDebugEnabled()) {
                    logger.debug("Updated arbitrary field[" + fieldName + "] in record[" + recordId + "] in the  " +
                                 table + " successfully.");
                }
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to update arbitrary field[" + fieldName + "] for id [" + recordId + "] from table :" + table, e);
            throw new MessageConsoleException("Unable to update arbitrary field[" + fieldName + "] for id [" + recordId + "] from table :" + table, e);
        }
    }

    private Object getObject(String value, String type) {
        Object convertedValue;
        switch (type) {
            case STRING: {
                convertedValue = String.valueOf(value);
                break;
            }
            case INTEGER: {
                if (value == null || value.isEmpty()) {
                    value = "0";
                }
                convertedValue = Integer.valueOf(value);
                break;
            }
            case LONG: {
                if (value == null || value.isEmpty()) {
                    value = "0";
                }
                convertedValue = Integer.valueOf(value);
                break;
            }
            case BOOLEAN: {
                convertedValue = Boolean.valueOf(value);
                break;
            }
            case FLOAT: {
                if (value == null || value.isEmpty()) {
                    value = "0.0";
                }
                convertedValue = Float.valueOf(value);
                break;
            }
            case DOUBLE: {
                if (value == null || value.isEmpty()) {
                    value = "0.0";
                }
                convertedValue = Double.valueOf(value);
                break;
            }
            default: {
                convertedValue = value;
            }
        }
        return convertedValue;
    }

    /**
     * This method will use to create a new table. This will create new table and a table schema if schema
     * information available. During the creation user can specify the indices and primary keys.
     *
     * @param tableInfo TableBean instance that contains table name and column meta information
     * @throws MessageConsoleException
     */
    public void createTable(TableBean tableInfo) throws MessageConsoleException {
        String username = getUsername();
        try {
            analyticsDataService.createTable(username, tableInfo.getName());
        } catch (AnalyticsException e) {
            logger.error("Unable to create table: " + e.getMessage(), e);
            throw new MessageConsoleException("Unable to create table: " + e.getMessage(), e);
        }
        List<String> primaryKeys = new ArrayList<>();
        Map<String, AnalyticsSchema.ColumnType> columns = new HashMap<>();
        Map<String, IndexType> indexColumns = new HashMap<>();
        if (tableInfo.getColumns() != null) {
            for (ColumnBean column : tableInfo.getColumns()) {
                if (column != null) {
                    if (column.isPrimary()) {
                        primaryKeys.add(column.getName());
                    }
                    if (column.isIndex()) {
                        indexColumns.put(column.getName(), createIndexType(column.getType()));
                    }
                    columns.put(column.getName(), getColumnType(column.getType()));
                }
            }
        }
        try {
            AnalyticsSchema schema = new AnalyticsSchema(columns, primaryKeys);
            analyticsDataService.setTableSchema(username, tableInfo.getName(), schema);
        } catch (AnalyticsException e) {
            logger.error("Unable to save table schema information: " + e.getMessage(), e);
            throw new MessageConsoleException("Unable to save table schema information: " + e.getMessage(), e);
        }
        if (!indexColumns.isEmpty()) {
            try {
                if (AuthorizationUtils.isUserAuthorized(getTenantId(), username, Constants
                        .PERMISSION_SET_INDEXING)) {
                    analyticsDataService.setIndices(username, tableInfo.getName(), indexColumns);
                }
            } catch (AnalyticsException e) {
                logger.error("Unable to save table index information: " + e.getMessage(), e);
                throw new MessageConsoleException("Unable to save table index information: " + e.getMessage(), e);
            }
        }
    }

    /**
     * This method will use to edit the table. During the edit, it can be add or remove existing columns and indices.
     *
     * @param tableInfo TableBean instance that contains table name and column meta information
     * @throws MessageConsoleException
     */
    public void editTable(TableBean tableInfo) throws MessageConsoleException {
        String username = getUsername();
        List<String> primaryKeys = new ArrayList<>();
        Map<String, AnalyticsSchema.ColumnType> columns = new HashMap<>();
        Map<String, IndexType> indexColumns = new HashMap<>();
        if (tableInfo.getColumns() != null) {
            for (ColumnBean column : tableInfo.getColumns()) {
                if (column != null) {
                    if (column.isPrimary()) {
                        primaryKeys.add(column.getName());
                    }
                    if (column.isIndex()) {
                        indexColumns.put(column.getName(), createIndexType(column.getType()));
                    }
                    columns.put(column.getName(), getColumnType(column.getType()));
                }
            }
        }
        try {
            AnalyticsSchema schema = new AnalyticsSchema(columns, primaryKeys);
            analyticsDataService.setTableSchema(username, tableInfo.getName(), schema);
        } catch (AnalyticsException e) {
            logger.error("Unable to save table schema information: " + e.getMessage(), e);
            throw new MessageConsoleException("Unable to save table schema information: " + e.getMessage(), e);
        }
        try {
            if (AuthorizationUtils.isUserAuthorized(getTenantId(), username, Constants
                    .PERMISSION_DELETE_INDEXING)) {
                analyticsDataService.clearIndices(username, tableInfo.getName());
                if (!indexColumns.isEmpty()) {
                    if (AuthorizationUtils.isUserAuthorized(getTenantId(), username, Constants
                            .PERMISSION_SET_INDEXING)) {
                        analyticsDataService.setIndices(username, tableInfo.getName(), indexColumns);
                    }
                }
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to save table index information: " + e.getMessage(), e);
            throw new MessageConsoleException("Unable to save table index information: " + e.getMessage(), e);
        }
    }

    private AnalyticsSchema.ColumnType getColumnType(String columnType) {
        switch (columnType) {
            case STRING:
                return AnalyticsSchema.ColumnType.STRING;
            case INTEGER:
                return AnalyticsSchema.ColumnType.INTEGER;
            case LONG:
                return AnalyticsSchema.ColumnType.LONG;
            case FLOAT:
                return AnalyticsSchema.ColumnType.FLOAT;
            case DOUBLE:
                return AnalyticsSchema.ColumnType.DOUBLE;
            case BOOLEAN:
                return AnalyticsSchema.ColumnType.BOOLEAN;
            default:
                return AnalyticsSchema.ColumnType.STRING;
        }
    }

    private IndexType createIndexType(String indexType) {
        switch (indexType) {
            case BOOLEAN:
                return IndexType.BOOLEAN;
            case FLOAT:
                return IndexType.FLOAT;
            case DOUBLE:
                return IndexType.DOUBLE;
            case INTEGER:
                return IndexType.INTEGER;
            case LONG:
                return IndexType.LONG;
            case STRING:
                return IndexType.STRING;
            default:
                return IndexType.STRING;
        }
    }

    /**
     * This method is use to get logged in username with tenant domain
     *
     * @return Username with tenant domain
     */
    @Override
    protected String getUsername() {
        return super.getUsername() + AT_SIGN + super.getTenantDomain();
    }

    /**
     * This method use to remove table.
     *
     * @param table The table name
     * @throws MessageConsoleException
     */
    public void deleteTable(String table) throws MessageConsoleException {
        try {
            String username = getUsername();
            analyticsDataService.deleteTable(username, table);
            try {
                TaskManager taskManager = ServiceHolder.getTaskService().getTaskManager(org.wso2.carbon.analytics.messageconsole.Constants
                                                                                                .ANALYTICS_DATA_PURGING);
                if (taskManager.isTaskScheduled(getDataPurgingTaskName(table))) {
                    taskManager.deleteTask(getDataPurgingTaskName(table));
                }
            } catch (TaskException e) {
                logger.error(e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(table + " deleted successfully");
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to delete table: " + table, e);
            throw new MessageConsoleException("Unable to delete table: " + table, e);
        }
    }

    private int getTenantId() {
        return CarbonContext.getThreadLocalCarbonContext().getTenantId();
    }

    /**
     * To check whether underneath data layer support pagination or not
     *
     * @return boolean true or false
     */
    public boolean isPaginationSupported() {
        return analyticsDataService.isPaginationSupported();
    }

    /**
     * Scheduling data purging task for given table
     *
     * @param table           Table name that need to purge
     * @param cronString      Task cron schedule  information
     * @param retentionPeriod Data retention period
     * @throws MessageConsoleException
     */
    public void scheduleDataPurging(String table, String cronString, int retentionPeriod)
            throws MessageConsoleException {
        try {
            TaskManager taskManager = ServiceHolder.getTaskService().getTaskManager(org.wso2.carbon.analytics.messageconsole.Constants
                                                                                            .ANALYTICS_DATA_PURGING);
            TaskInfo taskInfo = createDataPurgingTask(table, cronString, retentionPeriod);
            taskManager.deleteTask(taskInfo.getName());
            if (cronString != null) {
                taskManager.registerTask(taskInfo);
                taskManager.scheduleTask(taskInfo.getName());
            }
        } catch (TaskException e) {
            throw new MessageConsoleException("Unable to schedule a purging task for " + table + " with corn " +
                                              "schedule[" + cronString + "] due to " + e.getMessage(), e);
        }
    }

    public ScheduleTaskInfo getDataPurgingDetails(String table) throws MessageConsoleException {
        ScheduleTaskInfo taskInfo = new ScheduleTaskInfo();
        try {
            TaskManager taskManager = ServiceHolder.getTaskService().getTaskManager(org.wso2.carbon.analytics.messageconsole.Constants
                                                                                            .ANALYTICS_DATA_PURGING);
            if (taskManager.isTaskScheduled(getDataPurgingTaskName(table))) {
                TaskInfo task = taskManager.getTask(getDataPurgingTaskName(table));
                if (task != null) {
                    taskInfo.setCronString(task.getProperties().get(org.wso2.carbon.analytics.messageconsole.Constants.CRON_STRING));
                    taskInfo.setRetentionPeriod(Integer.parseInt(task.getProperties().get(org.wso2.carbon.analytics.messageconsole.Constants
                                                                                                  .RETENTION_PERIOD)));
                }
            }
        } catch (TaskException e) {
            throw new MessageConsoleException("Unable to get schedule details for " + table + " due to " + e
                    .getMessage(), e);
        }

        return taskInfo;
    }

    private TaskInfo createDataPurgingTask(String table, String cronString, int retentionPeriod) {
        String taskName = getDataPurgingTaskName(table);
        TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(cronString);
        Map<String, String> taskProperties = new HashMap<>(4);
        taskProperties.put(org.wso2.carbon.analytics.messageconsole.Constants.RETENTION_PERIOD, String
                .valueOf(retentionPeriod));
        taskProperties.put(org.wso2.carbon.analytics.messageconsole.Constants.TABLE, table);
        taskProperties.put(org.wso2.carbon.analytics.messageconsole.Constants.TENANT_ID, String.valueOf(CarbonContext.getThreadLocalCarbonContext().getTenantId()));
        taskProperties.put(org.wso2.carbon.analytics.messageconsole.Constants.CRON_STRING, cronString);
        return new TaskInfo(taskName, AnalyticsDataPurgingTask.class.getName(), taskProperties, triggerInfo);
    }

    private String getDataPurgingTaskName(String table) {
        return getTenantDomain() + "_" + table + "_" + "data_purging_task";
    }
}
