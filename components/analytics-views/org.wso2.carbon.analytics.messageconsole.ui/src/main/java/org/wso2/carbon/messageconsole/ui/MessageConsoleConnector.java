package org.wso2.carbon.messageconsole.ui;

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.messageconsole.stub.MessageConsoleStub;
import org.wso2.carbon.analytics.messageconsole.stub.beans.PermissionBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.ScheduleTaskInfo;
import org.wso2.carbon.analytics.webservice.stub.AnalyticsWebServiceAnalyticsWebServiceExceptionException;
import org.wso2.carbon.analytics.webservice.stub.AnalyticsWebServiceStub;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsCategoryPathBean;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.stub.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.stub.beans.CategoryPathBean;
import org.wso2.carbon.analytics.webservice.stub.beans.CategorySearchResultEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SchemaColumnBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SubCategoriesBean;
import org.wso2.carbon.analytics.webservice.stub.beans.ValuesBatchBean;
import org.wso2.carbon.messageconsole.ui.beans.Column;
import org.wso2.carbon.messageconsole.ui.beans.FacetBean;
import org.wso2.carbon.messageconsole.ui.beans.Permissions;
import org.wso2.carbon.messageconsole.ui.beans.Record;
import org.wso2.carbon.messageconsole.ui.beans.ResponseArbitraryField;
import org.wso2.carbon.messageconsole.ui.beans.ResponseResult;
import org.wso2.carbon.messageconsole.ui.beans.ResponseTable;
import org.wso2.carbon.messageconsole.ui.beans.ScheduleTask;
import org.wso2.carbon.messageconsole.ui.exception.MessageConsoleException;
import org.wso2.carbon.messageconsole.ui.serializers.ResponseArbitraryFieldsSerializer;
import org.wso2.carbon.messageconsole.ui.serializers.ResponseResultSerializer;

import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class will expose all the MessageConsoleService stub operations.
 */
public class MessageConsoleConnector {

    private static final Log log = LogFactory.getLog(MessageConsoleConnector.class);
    private static final String MESSAGE_CONSOLE = "MessageConsole";
    private static final String ANALYTICS_WEB_SERVICE = "AnalyticsWebService";
    private static final String OK = "OK";
    private static final String ERROR = "ERROR";

    public static final String RECORD_ID = "_unique_rec_id";
    public static final String TIMESTAMP = "_timestamp";
    public static final int TYPE_LIST_RECORD = 1;
    public static final int TYPE_TABLE_INFO = 5;
    public static final int TYPE_LIST_ARBITRARY_RECORD = 6;
    public static final int TYPE_GET_TABLE_INFO = 12;
    public static final int TYPE_GET_PURGING_TASK_INFO = 13;
    public static final int TYPE_SAVE_PURGING_TASK_INFO = 14;
    public static final int TYPE_LIST_TABLE = 15;
    public static final int TYPE_GET_FACET_NAME_LIST = 16;
    public static final int TYPE_GET_FACET_CATEGORIES = 17;
    public static final int TYPE_GET_PRIMARY_KEY_LIST = 18;
    public static final int TYPE_CHECK_TOTAL_COUNT_SUPPORT = 19;

    private static final GsonBuilder RESPONSE_RESULT_BUILDER = new GsonBuilder().registerTypeAdapter(ResponseResult.class,
                                                                                                     new ResponseResultSerializer());
    private static final GsonBuilder RESPONSE_ARBITRARY_FIELD_BUILDER = new GsonBuilder().
            registerTypeAdapter(ResponseArbitraryField.class, new ResponseArbitraryFieldsSerializer());
    public static final Type STRING_ARRAY_TYPE = new TypeToken<String[]>() {
    }.getType();
    public static final Type FACET_LIST_TYPE = new TypeToken<List<FacetBean>>() {
    }.getType();
    public static final Type PRIMARY_KEYS_TYPE = new TypeToken<List<Column>>() {
    }.getType();
    private static final int MAX_CELL_LENGTH = 100;

    private MessageConsoleStub messageConsoleStub;
    private AnalyticsWebServiceStub analyticsWebServiceStub;


    public MessageConsoleConnector(ConfigurationContext configCtx, String backendServerURL, String cookie) {
        try {
            String messageConsoleServiceUrl = backendServerURL + MESSAGE_CONSOLE;
            messageConsoleStub = new MessageConsoleStub(configCtx, messageConsoleServiceUrl);
            ServiceClient messageConsoleServiceClient = messageConsoleStub._getServiceClient();
            Options options = messageConsoleServiceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

            String analyticsWebServiceUrl = backendServerURL + ANALYTICS_WEB_SERVICE;
            analyticsWebServiceStub = new AnalyticsWebServiceStub(configCtx, analyticsWebServiceUrl);
            ServiceClient analyticsServiceClient = analyticsWebServiceStub._getServiceClient();
            options = analyticsServiceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault axisFault) {
            log.error("Unable to create MessageConsoleStub.", axisFault);
        }
    }

    public Permissions getAvailablePermissionForUser() throws MessageConsoleException {
        Permissions permissions = new Permissions();
        try {
            PermissionBean permissionBean = messageConsoleStub.getAvailablePermissions();
            permissions.setListTable(permissionBean.getListTable());
            permissions.setListRecord(permissionBean.getListRecord());
            permissions.setSearchRecord(permissionBean.getSearchRecord());
            permissions.setDeleteRecord(permissionBean.getDeleteRecord());
        } catch (Exception e) {
            throw new MessageConsoleException("Unable to check granted message console permissions due to: " + e
                    .getMessage(), e);
        }
        return permissions;
    }

    public String getTableList() {
        String[] tableList = null;
        try {
            tableList = analyticsWebServiceStub.listTables();
        } catch (Exception e) {
            log.error("Unable to get table list:" + e.getMessage(), e);
        }
        if (tableList == null) {
            if (log.isDebugEnabled()) {
                log.debug("Received an empty table name list!");
            }
            tableList = new String[0];
        } else {
            Arrays.sort(tableList);
        }
        return new Gson().toJson(tableList);
    }

    public String getRecords(String tableName, long timeFrom, long timeTo, int startIndex, int pageSize,
                             String searchQuery, String facetsJsonString, String primarySearchString,
                             int resultCountLimit) {
        if (log.isDebugEnabled()) {
            log.debug("Search Query: " + searchQuery);
            log.debug("timeFrom: " + timeFrom);
            log.debug("timeTo: " + timeTo);
            log.debug("Start Index: " + startIndex);
            log.debug("Page Size: " + pageSize);
            log.debug("Facet String: " + facetsJsonString);
            log.debug("Primary key String: " + primarySearchString);
        }
        ResponseResult responseResult = new ResponseResult();
        try {
            RecordBean[] resultRecordBeans;
            List<Column> primaryKeys = new Gson().fromJson(primarySearchString, PRIMARY_KEYS_TYPE);
            List<FacetBean> facetsList = new Gson().fromJson(facetsJsonString, FACET_LIST_TYPE);
            responseResult.setSearchTime(-1);
            if (primaryKeys != null && !primaryKeys.isEmpty()) {
                ValuesBatchBean[] batchBeans = new ValuesBatchBean[1];
                ValuesBatchBean batchBean = new ValuesBatchBean();
                batchBeans[0] = batchBean;
                RecordValueEntryBean[] valueEntryBeans = new RecordValueEntryBean[primaryKeys.size()];
                int i = 0;
                for (Column primaryKey : primaryKeys) {
                    RecordValueEntryBean entryBean = new RecordValueEntryBean();
                    entryBean.setFieldName(primaryKey.getKey());
                    entryBean.setStringValue(primaryKey.getValue());
                    valueEntryBeans[i++] = entryBean;
                }
                batchBean.setKeyValues(valueEntryBeans);
                resultRecordBeans = analyticsWebServiceStub.getWithKeyValues(tableName, 1, null, batchBeans);
            } else if (facetsList != null && !facetsList.isEmpty()) {
                long startingTime = System.currentTimeMillis();
                AnalyticsDrillDownRequestBean requestBean = getAnalyticsDrillDownRequestBean(tableName, startIndex, pageSize, searchQuery, facetsList);
                resultRecordBeans = analyticsWebServiceStub.drillDownSearch(requestBean);
                long searchCount = (long) analyticsWebServiceStub.drillDownSearchCount(requestBean);
                responseResult.setSearchTime(System.currentTimeMillis() - startingTime);
                responseResult.setActualRecordCount(searchCount);
                if (responseResult.getActualRecordCount() > resultCountLimit) {
                    responseResult.setTotalRecordCount(resultCountLimit);
                } else {
                    responseResult.setTotalRecordCount(searchCount);
                }
            } else if (searchQuery != null && !searchQuery.isEmpty()) {
                long startingTime = System.currentTimeMillis();
                resultRecordBeans = analyticsWebServiceStub.search(tableName, searchQuery, startIndex, pageSize);
                long searchCount = analyticsWebServiceStub.searchCount(tableName, searchQuery);
                responseResult.setSearchTime(System.currentTimeMillis() - startingTime);
                responseResult.setActualRecordCount(searchCount);
                if (responseResult.getActualRecordCount() > resultCountLimit) {
                    responseResult.setTotalRecordCount(resultCountLimit);
                } else {
                    responseResult.setTotalRecordCount(searchCount);
                }
            } else {
                String recordStoreName = analyticsWebServiceStub.getRecordStoreNameByTable(tableName);
                boolean isRecordCountSupported = analyticsWebServiceStub.isRecordCountSupported(recordStoreName);
                long totalRecordCount = -1;
                if (isRecordCountSupported) {
                    totalRecordCount = analyticsWebServiceStub.getRecordCount(tableName, timeFrom, timeTo);
                    responseResult.setActualRecordCount(totalRecordCount);
                    responseResult.setTotalRecordCount(totalRecordCount);
                } else {
                    responseResult.setActualRecordCount(totalRecordCount);
                    responseResult.setTotalRecordCount(resultCountLimit);
                }
                resultRecordBeans = analyticsWebServiceStub.getByRange(tableName, 1, null, timeFrom, timeTo, startIndex, pageSize);
            }
            List<Record> records = new ArrayList<>();
            if (resultRecordBeans != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Result size: " + resultRecordBeans.length);
                }
                for (RecordBean recordBean : resultRecordBeans) {
                    if (recordBean != null) {
                        Record record = getRecord(recordBean, true);
                        if (record.getColumns() != null && !record.getColumns().isEmpty()) {
                            for (Column column : record.getColumns()) {
                                if ("STRING".equals(column.getType())) {
                                    if (column.getValue() != null && !column.getValue().isEmpty() &&
                                        column.getValue().length() > MAX_CELL_LENGTH) {
                                        column.setValue(column.getValue().substring(0, MAX_CELL_LENGTH).concat("..."));
                                    }
                                }
                            }
                        }
                        records.add(record);
                    }
                }
                responseResult.setRecords(records);
            }
            responseResult.setResult(OK);
        } catch (Exception e) {
            String errorMsg = "Unable to get records for table:" + tableName;
            log.error(errorMsg, e);
            responseResult.setResult(ERROR);
            responseResult.setMessage(errorMsg);
        }
        return RESPONSE_RESULT_BUILDER.serializeNulls().create().toJson(responseResult);
    }

    private AnalyticsDrillDownRequestBean getAnalyticsDrillDownRequestBean(String tableName, int startIndex,
                                                                           int pageSize, String searchQuery,
                                                                           List<FacetBean> facetsList) {
        AnalyticsDrillDownRequestBean requestBean = new AnalyticsDrillDownRequestBean();
        requestBean.setTableName(tableName);
        requestBean.setRecordStart(startIndex);
        requestBean.setRecordCount(pageSize);
        if (searchQuery != null && searchQuery.isEmpty()) {
            searchQuery = null;
        }
        requestBean.setQuery(searchQuery);
        CategoryPathBean[] pathBeans = new CategoryPathBean[facetsList.size()];
        for (int i = 0; i < facetsList.size(); i++) {
            FacetBean facetBean = facetsList.get(i);
            CategoryPathBean categoryPathBean = new CategoryPathBean();
            categoryPathBean.setFieldName(facetBean.getField());
            categoryPathBean.setPath(facetBean.getPath().toArray(new String[facetBean.getPath().size()]));
            pathBeans[i] = categoryPathBean;
        }
        requestBean.setCategoryPaths(pathBeans);
        return requestBean;
    }

    private Record getRecord(RecordBean recordBean, boolean withDefaultColumn) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
        Record record = new Record();
        if (recordBean != null) {
            List<Column> columns = new ArrayList<>();
            if (withDefaultColumn) {
                columns.add(new Column(RECORD_ID, recordBean.getId()));
                columns.add(new Column(TIMESTAMP, dateFormat.format(new Date(recordBean.getTimestamp()))));
            }
            if (recordBean.getValues() != null) {
                for (RecordValueEntryBean entityBean : recordBean.getValues()) {
                    columns.add(getColumn(entityBean));
                }
            }
            record.setColumns(columns);
        }
        return record;
    }

    private Column getColumn(RecordValueEntryBean entityBean) {
        Column column;
        String value = null;
        switch (entityBean.getType()) {
            case "STRING": {
                value = entityBean.getStringValue();
                break;
            }
            case "INTEGER": {
                value = String.valueOf(entityBean.getIntValue());
                break;
            }
            case "LONG": {
                value = String.valueOf(entityBean.getLongValue());
                break;
            }
            case "FLOAT": {
                value = String.valueOf(entityBean.getFloatValue());
                break;
            }
            case "DOUBLE": {
                value = String.valueOf(entityBean.getDoubleValue());
                break;
            }
            case "BOOLEAN": {
                value = String.valueOf(entityBean.getBooleanValue());
                break;
            }
            case "FACET": {
                AnalyticsCategoryPathBean analyticsCategoryPathBeanValue = entityBean.getAnalyticsCategoryPathBeanValue();
                FacetBean facetBean = new FacetBean();
                if (analyticsCategoryPathBeanValue != null && analyticsCategoryPathBeanValue.getPath() != null) {
                    facetBean.setPath(Arrays.asList(analyticsCategoryPathBeanValue.getPath()));
                } else {
                    facetBean.setPath(new ArrayList<String>(0));
                }
                value = String.valueOf(new Gson().toJson(facetBean));
                break;
            }
        }
        column = new Column(entityBean.getFieldName(), value, entityBean.getType());
        return column;
    }

    public String getTableInfo(String tableName) {
        ResponseTable table = new ResponseTable();
        table.setName(tableName);
        try {
            AnalyticsSchemaBean tableSchema = analyticsWebServiceStub.getTableSchema(tableName);
            List<ResponseTable.Column> columns = new ArrayList<>();
            if (tableSchema.getColumns() != null) {
                List<String> primaryKeys = null;
                if (tableSchema.getPrimaryKeys() != null) {
                    primaryKeys = Arrays.asList(tableSchema.getPrimaryKeys());
                }
                for (SchemaColumnBean resultColumn : tableSchema.getColumns()) {
                    ResponseTable.Column column = new ResponseTable.Column();
                    column.setName(resultColumn.getColumnName());
                    column.setType(resultColumn.getColumnType());
                    column.setDisplay(true);
                    if (primaryKeys != null && primaryKeys.contains(resultColumn.getColumnName())) {
                        column.setPrimary(true);
                    }
                    columns.add(column);
                }
            }
            // Adding recordId column as a hidden field
            ResponseTable.Column recordId = new ResponseTable.Column();
            recordId.setName(RECORD_ID);
            recordId.setPrimary(false);
            recordId.setType("STRING");
            recordId.setDisplay(false);
            recordId.setKey(true);
            columns.add(recordId);
            ResponseTable.Column timestamp = new ResponseTable.Column();
            timestamp.setName(TIMESTAMP);
            timestamp.setPrimary(false);
            timestamp.setType("STRING");
            timestamp.setDisplay(true);
            columns.add(timestamp);
            table.setColumns(columns);
            table.setPaginationSupport(analyticsWebServiceStub.isPaginationSupported(analyticsWebServiceStub
                                                                                               .getRecordStoreNameByTable(tableName)));
        } catch (Exception e) {
            log.error("Unable to get table information for table:" + tableName, e);
        }
        return new GsonBuilder().serializeNulls().create().toJson(table);
    }

    private Record getRecord(String table, String recordId, boolean withDefaultColumns)
            throws RemoteException, AnalyticsWebServiceAnalyticsWebServiceExceptionException {
        String[] ids = new String[]{recordId};
        RecordBean[] recordBeans = analyticsWebServiceStub.getById(table, 1, null, ids);
        if (recordBeans != null && recordBeans.length > 0) {
            return getRecord(recordBeans[0], withDefaultColumns);
        }
        return null;
    }

    public String getArbitraryFields(String table, String recordId) {
        if (log.isDebugEnabled()) {
            log.debug("Get arbitrary field values for recordId [" + recordId + "] in table[" + table + "]");
        }
        ResponseArbitraryField responseArbitraryField = new ResponseArbitraryField();
        responseArbitraryField.setResult(OK);
        Set<Column> resultColumns = new HashSet<>();
        try {
            Record originalRecord = getRecord(table, recordId, false);
            AnalyticsSchemaBean schema = analyticsWebServiceStub.getTableSchema(table);
            if (originalRecord != null) {
                List<Column> columns = originalRecord.getColumns();
                if (schema != null) {
                    if (log.isDebugEnabled()) {
                        log.debug("Table schema[" + table + "] is not null");
                    }
                    SchemaColumnBean[] schemaColumns = schema.getColumns();
                    if (schemaColumns != null && schemaColumns.length > 0) {
                        for (Column column : columns) {
                            boolean arbitraryField = true;
                            for (SchemaColumnBean schemaColumnBean : schemaColumns) {
                                if (column.getKey().equals(schemaColumnBean.getColumnName())) {
                                    arbitraryField = false;
                                    break;
                                }
                            }
                            if (arbitraryField) {
                                resultColumns.add(column);
                            }
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Either table schema[" + table + "] null or empty. Adding all records to " +
                                      "arbitrary list");
                        }
                        for (Column column : columns) {
                            resultColumns.add(column);
                        }
                    }
                }
                if (columns != null && !columns.isEmpty()) {
                    for (Column column : columns) {
                        if ("STRING".equals(column.getType())) {
                            if (column.getValue() != null && !column.getValue().isEmpty() &&
                                column.getValue().length() > MAX_CELL_LENGTH) {
                                resultColumns.add(column);
                            }
                        }
                    }
                }
            }
            responseArbitraryField.setColumns(new ArrayList<>(resultColumns));
        } catch (Exception e) {
            String errorMsg = "Unable to get arbitrary fields for record[" + recordId + "] in table[" + table + "]";
            log.error(errorMsg, e);
            responseArbitraryField.setResult(ERROR);
            responseArbitraryField.setMessage(errorMsg);
        }
        return RESPONSE_ARBITRARY_FIELD_BUILDER.serializeNulls().create().toJson(responseArbitraryField);
    }

    public String scheduleDataPurging(String table, String time, String retentionPeriod, boolean enable) {
        String cron = null;
        String msg;
        if (enable) {
            if (time != null && time.length() == 5) {
                int hour = Integer.parseInt(time.substring(0, 2));
                int min = Integer.parseInt(time.substring(3));
                cron = "0 " + min + " " + hour + " * * ?";
            } else {
                cron = time;
            }
        }

        if (!retentionPeriod.matches("^-?\\d+$")) {
            msg = "Please enter valid number for \"Purge Record Older Than \" field.";
        } else {
            try {
                messageConsoleStub.scheduleDataPurging(table, cron, Integer.parseInt(retentionPeriod));
                if (enable) {
                    msg = "Data purging task for " + table + " scheduled successfully.";
                } else {
                    msg = "Data purging task for " + table + " removed successfully.";
                }
            } catch (Exception e) {
                msg = "Unable to schedule task due to " + e.getMessage();
                log.error("Unable to schedule data puring task for " + table, e);
            }
        }
        return msg;
    }

    public String getDataPurgingDetails(String table) {
        ScheduleTask scheduleTask = new ScheduleTask();
        try {
            ScheduleTaskInfo dataPurgingDetails = messageConsoleStub.getDataPurgingDetails(table);
            if (dataPurgingDetails != null) {
                scheduleTask.setCronString(dataPurgingDetails.getCronString());
                scheduleTask.setRetentionPeriod(dataPurgingDetails.getRetentionPeriod());
            }
        } catch (Exception e) {
            log.error("Unable to get schedule task information for table:" + table, e);
        }
        return new Gson().toJson(scheduleTask);
    }

    public String getFacetColumnNameList(String table) {
        List<String> facetNameList = new ArrayList<>();
        try {
            AnalyticsSchemaBean tableSchema = analyticsWebServiceStub.getTableSchema(table);
            if (tableSchema != null && tableSchema.getColumns() != null) {
                for (SchemaColumnBean schemaColumnBean : tableSchema.getColumns()) {
                    if (schemaColumnBean.getFacet()) {
                        facetNameList.add(schemaColumnBean.getColumnName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to get facet column list for table:" + table, e);
        }
        return new Gson().toJson(facetNameList.toArray(new String[facetNameList.size()]));
    }

    public String getPrimaryKeys(String table) {
        try {
            AnalyticsSchemaBean tableSchema = analyticsWebServiceStub.getTableSchema(table);
            if (tableSchema != null && tableSchema.getPrimaryKeys() != null) {
                return new Gson().toJson(tableSchema.getPrimaryKeys());
            }
        } catch (Exception e) {
            log.error("Unable to get facet column list for table:" + table, e);
        }
        return new Gson().toJson(new String[0]);
    }

    public String getFacetCategoryList(String table, String fieldName, String categoryPaths) {
        CategoryDrillDownRequestBean drillDownRequestBean = new CategoryDrillDownRequestBean();
        drillDownRequestBean.setTableName(table);
        drillDownRequestBean.setFieldName(fieldName);
        if (categoryPaths != null && !categoryPaths.isEmpty()) {
            String[] path = new Gson().fromJson(categoryPaths, STRING_ARRAY_TYPE);
            drillDownRequestBean.setPath(path);
        } else {
            drillDownRequestBean.setPath(null);
        }
        List<String> categories = new ArrayList<>();
        try {
            SubCategoriesBean subCategoriesBean = analyticsWebServiceStub.drillDownCategories(drillDownRequestBean);
            if (subCategoriesBean != null && subCategoriesBean.getCategories() != null) {
                for (CategorySearchResultEntryBean categorySearchResultEntryBean : subCategoriesBean.getCategories()) {
                    categories.add(categorySearchResultEntryBean.getCategoryName());
                }
            }
        } catch (Exception e) {
            log.error("Unable to get facet sub category for table[" + table + "], field [" + fieldName + "] and " +
                      "path[" + categoryPaths + "]", e);
        }
        return new Gson().toJson(categories);
    }

    public boolean isRecordCountSupported(String tableName) {
        try {
            return analyticsWebServiceStub.isRecordCountSupported(analyticsWebServiceStub.getRecordStoreNameByTable(tableName));
        } catch (Exception e) {
            log.error("Unable to check record count support status for table " + tableName, e);
            return false;
        }
    }
}
