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
import java.util.List;

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
//    public static final int TYPE_CREATE_RECORD = 2;
//    public static final int TYPE_UPDATE_RECORD = 3;
//    public static final int TYPE_DELETE_RECORD = 4;
    public static final int TYPE_TABLE_INFO = 5;
    public static final int TYPE_LIST_ARBITRARY_RECORD = 6;
//    public static final int TYPE_CRATE_ARBITRARY_RECORD = 7;
//    public static final int TYPE_UPDATE_ARBITRARY_RECORD = 8;
//    public static final int TYPE_DELETE_ARBITRARY_RECORD = 9;
//    public static final int TYPE_CREATE_TABLE = 10;
//    public static final int TYPE_DELETE_TABLE = 11;
    public static final int TYPE_GET_TABLE_INFO = 12;
    public static final int TYPE_GET_PURGING_TASK_INFO = 13;
    public static final int TYPE_SAVE_PURGING_TASK_INFO = 14;
    public static final int TYPE_LIST_TABLE = 15;
    public static final int TYPE_GET_FACET_NAME_LIST = 16;
    public static final int TYPE_GET_FACET_CATEGORIES = 17;

    private static final GsonBuilder RESPONSE_RESULT_BUILDER = new GsonBuilder().registerTypeAdapter(ResponseResult.class,
                                                                                                     new ResponseResultSerializer());
    /*private static final GsonBuilder RESPONSE_RECORD_BUILDER = new GsonBuilder().registerTypeAdapter(ResponseRecord.class,
                                                                                                     new ResponseRecordSerializer());*/
    private static final GsonBuilder RESPONSE_ARBITRARY_FIELD_BUILDER = new GsonBuilder().
            registerTypeAdapter(ResponseArbitraryField.class, new ResponseArbitraryFieldsSerializer());
    /*private static final GsonBuilder RESPONSE_ARBITRARY_FIELD_COLUMN_BUILDER = new GsonBuilder().
            registerTypeAdapter(ResponseArbitraryFieldColumn.class, new ResponseArbitraryFieldSerializer());
    private static final Type TABLE_SCHEMA_TYPE = new TypeToken<List<TableSchemaColumn>>() {
    }.getType();
    private static final Type FACET_BEAN_TYPE = new TypeToken<FacetBean>() {
    }.getType();*/
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
    public static final Type STRING_ARRAY_TYPE = new TypeToken<String[]>() {
    }.getType();
    public static final Type FACET_LIST_TYPE = new TypeToken<List<FacetBean>>() {
    }.getType();

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
            permissions.setCreateTable(permissionBean.getCreateTable());
            permissions.setListTable(permissionBean.getListTable());
            permissions.setDropTable(permissionBean.getDropTable());
            permissions.setListRecord(permissionBean.getListRecord());
            permissions.setPutRecord(permissionBean.getPutRecord());
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
        }
        return new Gson().toJson(tableList);
    }

    public String getRecords(String tableName, long timeFrom, long timeTo, int startIndex, int pageSize,
                             String searchQuery, String facetsJsonString) {
        if (log.isDebugEnabled()) {
            log.debug("Search Query: " + searchQuery);
            log.debug("timeFrom: " + timeFrom);
            log.debug("timeTo: " + timeTo);
            log.debug("Start Index: " + startIndex);
            log.debug("Page Size: " + pageSize);
            log.debug("Facet String: " + facetsJsonString);
        }
        ResponseResult responseResult = new ResponseResult();
        try {
            RecordBean[] resultRecordBeans;
            List<FacetBean> facetsList = new Gson().fromJson(facetsJsonString, FACET_LIST_TYPE);
            if (!facetsList.isEmpty()) {
                AnalyticsDrillDownRequestBean requestBean = getAnalyticsDrillDownRequestBean(tableName, startIndex, pageSize, searchQuery, facetsList);
                resultRecordBeans = analyticsWebServiceStub.drillDownSearch(requestBean);
                responseResult.setTotalRecordCount(new Double(analyticsWebServiceStub.drillDownSearchCount(requestBean)).intValue());
            } else if (searchQuery != null && !searchQuery.isEmpty()) {
                resultRecordBeans = analyticsWebServiceStub.search(tableName, searchQuery, startIndex, pageSize);
                responseResult.setTotalRecordCount(analyticsWebServiceStub.searchCount(tableName, searchQuery));
            } else {
                resultRecordBeans = analyticsWebServiceStub.getByRange(tableName, 1, null, timeFrom, timeTo, startIndex, pageSize);
                responseResult.setTotalRecordCount(analyticsWebServiceStub.getRecordCount(tableName, timeFrom, timeTo));
            }
            List<Record> records = new ArrayList<>();
            if (resultRecordBeans != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Result size: " + resultRecordBeans.length);
                }
                for (RecordBean recordBean : resultRecordBeans) {
                    Record record = getRecord(recordBean, true);
                    records.add(record);
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
        Record record = new Record();
        if (recordBean != null) {
            List<Column> columns = new ArrayList<>();
            if (withDefaultColumn) {
                columns.add(new Column(RECORD_ID, recordBean.getId()));
                columns.add(new Column(TIMESTAMP, DATE_FORMAT.format(new Date(recordBean.getTimestamp()))));
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
                    ResponseTable.Column column = new ResponseTable().new Column();
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
            ResponseTable.Column recordId = new ResponseTable().new Column();
            recordId.setName(RECORD_ID);
            recordId.setPrimary(false);
            recordId.setType("STRING");
            recordId.setDisplay(false);
            recordId.setKey(true);
            columns.add(recordId);
            ResponseTable.Column timestamp = new ResponseTable().new Column();
            timestamp.setName(TIMESTAMP);
            timestamp.setPrimary(false);
            timestamp.setType("STRING");
            timestamp.setDisplay(true);
            columns.add(timestamp);
            table.setColumns(columns);
        } catch (Exception e) {
            log.error("Unable to get table information for table:" + tableName, e);
        }
        return new GsonBuilder().serializeNulls().create().toJson(table);
    }

    /*public String deleteRecords(String table, String[] recordIds) {
        if (log.isDebugEnabled()) {
            log.debug("Records[" + Arrays.toString(recordIds) + "] going to delete from" + table);
        }
        ResponseResult responseResult = new ResponseResult();
        try {
            analyticsWebServiceStub.deleteByIds(table, recordIds);
            responseResult.setResult(OK);
        } catch (Exception e) {
            String errorMsg = "Unable to delete records" + Arrays.toString(recordIds) + " from table :" + table;
            log.error(errorMsg, e);
            responseResult.setResult(ERROR);
            responseResult.setMessage(errorMsg);
        }
        return RESPONSE_RESULT_BUILDER.serializeNulls().create().toJson(responseResult);
    }*/

    /*public String addRecord(String table, String[] columns, String[] values) {
        if (log.isDebugEnabled()) {
            log.debug("New record {column: " + Arrays.toString(columns) + ", values: " + Arrays.toString(values) +
                      "} going to add to" + table);
        }
        ResponseRecord responseRecord = new ResponseRecord();
        try {
            RecordBean[] recordBeans = putRecord(table, columns, values, null);
            responseRecord.setRecord(getRecord(recordBeans[0], true));
            responseRecord.setResult(OK);
        } catch (Exception e) {
            String errorMsg;
            if (e instanceof JsonSyntaxException) {
                errorMsg = "One of the Facet column has an invalid JSON format.";
            } else {
                errorMsg = "Unable to add record {column: " + Arrays.toString(columns) + ", values: " + Arrays
                        .toString(values) + " } to table :" + table;
            }
            log.error(errorMsg, e);
            responseRecord.setResult(ERROR);
            responseRecord.setMessage(errorMsg);
        }
        return RESPONSE_RECORD_BUILDER.serializeNulls().create().toJson(responseRecord);
    }*/

    /*private RecordValueEntryBean[] getRecordValueEntryBeans(String[] columns, String[] values,
                                                            AnalyticsSchemaBean tableSchema) {
        int position = 0;
        RecordValueEntryBean[] recordValueEntryBeans = new RecordValueEntryBean[columns.length];
        List<SchemaColumnBean> schemaColumnBeans = Arrays.asList(tableSchema.getColumns());
        for (int i = 0; i < columns.length; i++) {
            String columnName = columns[i];
            RecordValueEntryBean recordValueEntryBean = new RecordValueEntryBean();
            recordValueEntryBean.setFieldName(columnName);
            for (SchemaColumnBean schemaColumnBean : schemaColumnBeans) {
                if (columnName.equals(schemaColumnBean.getColumnName())) {
                    setRecordValueEntryBeanType(values[i], recordValueEntryBean, schemaColumnBean.getColumnType());
                    break;
                }
            }
            recordValueEntryBeans[position++] = recordValueEntryBean;
        }
        return recordValueEntryBeans;
    }*/

    /*private void setRecordValueEntryBeanType(String value, RecordValueEntryBean recordValueEntryBean, String type) {
        switch (type) {
            case "STRING": {
                recordValueEntryBean.setStringValue(value);
                recordValueEntryBean.setType("STRING");
                break;
            }
            case "INTEGER": {
                if (value == null || value.isEmpty()) {
                    value = "0";
                }
                recordValueEntryBean.setIntValue(Integer.valueOf(value));
                recordValueEntryBean.setType("INTEGER");
                break;
            }
            case "LONG": {
                if (value == null || value.isEmpty()) {
                    value = "0";
                }
                recordValueEntryBean.setLongValue(Long.valueOf(value));
                recordValueEntryBean.setType("LONG");
                break;
            }
            case "BOOLEAN": {
                if (value == null || value.isEmpty()) {
                    value = "false";
                }
                recordValueEntryBean.setBooleanValue(Boolean.valueOf(value));
                recordValueEntryBean.setType("BOOLEAN");
                break;
            }
            case "FLOAT": {
                if (value == null || value.isEmpty()) {
                    value = "0";
                }
                recordValueEntryBean.setFloatValue(Float.valueOf(value));
                recordValueEntryBean.setType("FLOAT");
                break;
            }
            case "DOUBLE": {
                if (value == null || value.isEmpty()) {
                    value = "0";
                }
                recordValueEntryBean.setDoubleValue(Double.valueOf(value));
                recordValueEntryBean.setType("DOUBLE");
                break;
            }
            case "FACET": {
                if (value != null && !value.isEmpty()) {
                    FacetBean facetBean = new Gson().fromJson(value, FACET_BEAN_TYPE);
                    if (facetBean != null) {
                        AnalyticsCategoryPathBean categoryPathBean = new AnalyticsCategoryPathBean();
                        if (facetBean.getPath() != null) {
                            String[] paths = new String[facetBean.getPath().size()];
                            categoryPathBean.setPath(facetBean.getPath().toArray(paths));
                        }
                        recordValueEntryBean.setAnalyticsCategoryPathBeanValue(categoryPathBean);
                    }
                }
                recordValueEntryBean.setType("FACET");
                break;
            }
            default: {
                recordValueEntryBean.setStringValue(value);
            }
        }
    }*/

    /*public String updateRecord(String table, String[] columns, String[] values, String recordId) {
        if (log.isDebugEnabled()) {
            log.debug("Record {id: " + recordId + ", column: " + Arrays.toString(columns) + ", values: " + Arrays.toString(values) + "} going to update to" + table);
        }
        ResponseRecord responseRecord = new ResponseRecord();
        responseRecord.setResult(OK);
        try {
            RecordBean[] recordBeans = putRecord(table, columns, values, recordId);
            responseRecord.setRecord(getRecord(recordBeans[0], true));
            responseRecord.setResult(OK);
        } catch (Exception e) {
            String errorMsg;
            if (e instanceof JsonSyntaxException) {
                errorMsg = "One of the Facet column has an invalid JSON format.";
            } else {
                errorMsg = "Unable to add record {column: " + Arrays.toString(columns) + ", values: " + Arrays
                        .toString(values) + " } to table :" + table;
            }
            log.error(errorMsg, e);
            responseRecord.setResult(ERROR);
            responseRecord.setMessage(errorMsg);
        }
        return RESPONSE_RECORD_BUILDER.serializeNulls().create().toJson(responseRecord);
    }*/

    /*private RecordBean[] putRecord(String table, String[] columns, String[] values, String recordId)
            throws RemoteException, AnalyticsWebServiceAnalyticsWebServiceExceptionException {
        AnalyticsSchemaBean tableSchema = analyticsWebServiceStub.getTableSchema(table);
        RecordBean recordBean = new RecordBean();
        recordBean.setId(recordId);
        recordBean.setTableName(table);
        recordBean.setTimestamp(System.currentTimeMillis());
        if (columns != null && tableSchema != null && tableSchema.getColumns() != null) {
            RecordValueEntryBean[] recordValueEntryBeans = getRecordValueEntryBeans(columns, values, tableSchema);
            recordBean.setValues(recordValueEntryBeans);
        }
        RecordBean[] recordBeans = new RecordBean[]{recordBean};
        return analyticsWebServiceStub.put(recordBeans);
    }*/

    private Record getRecord(String table, String recordId, boolean withDefaultColumns)
            throws RemoteException, AnalyticsWebServiceAnalyticsWebServiceExceptionException {
        String[] ids = new String[]{recordId};
        RecordBean[] recordBeans = analyticsWebServiceStub.getById(table, 1, null, ids);
        if (recordBeans != null && recordBeans.length > 0) {
            return getRecord(recordBeans[0], withDefaultColumns);
        }
        return null;
    }

    /*private RecordBean getRecordBean(String table, String recordId)
            throws RemoteException, AnalyticsWebServiceAnalyticsWebServiceExceptionException {
        String[] ids = new String[]{recordId};
        RecordBean[] recordBeans = analyticsWebServiceStub.getById(table, 1, null, ids);
        if (recordBeans != null && recordBeans.length > 0) {
            return recordBeans[0];
        }
        return null;
    }*/

    public String getArbitraryFields(String table, String recordId) {
        if (log.isDebugEnabled()) {
            log.debug("Get arbitrary field values for recordId [" + recordId + "] in table[" + table + "]");
        }
        ResponseArbitraryField responseArbitraryField = new ResponseArbitraryField();
        responseArbitraryField.setResult(OK);
        List<Column> resultColumns = new ArrayList<>();
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
            }
            responseArbitraryField.setColumns(resultColumns);
        } catch (Exception e) {
            String errorMsg = "Unable to get arbitrary fields for record[" + recordId + "] in table[" + table + "]";
            log.error(errorMsg, e);
            responseArbitraryField.setResult(ERROR);
            responseArbitraryField.setMessage(errorMsg);
        }
        return RESPONSE_ARBITRARY_FIELD_BUILDER.serializeNulls().create().toJson(responseArbitraryField);
    }

    /*public String deleteArbitraryField(String table, String recordId, String fieldName) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting arbitrary field[" + fieldName + "] in record[" + recordId + "] in table[" + table + "]");
        }
        ResponseArbitraryField responseArbitraryField = new ResponseArbitraryField();
        responseArbitraryField.setResult(OK);
        try {
            RecordBean originalRecord = getRecordBean(table, recordId);
            if (originalRecord != null) {
                RecordValueEntryBean[] columns = originalRecord.getValues();
                RecordValueEntryBean[] newColumnsList = new RecordValueEntryBean[columns.length - 1];
                int i = 0;
                for (RecordValueEntryBean column : columns) {
                    if (!fieldName.equalsIgnoreCase(column.getFieldName())) {
                        newColumnsList[i++] = column;
                    }
                }
                originalRecord.setValues(newColumnsList);
                originalRecord.setTimestamp(System.currentTimeMillis());
                analyticsWebServiceStub.put(new RecordBean[]{originalRecord});
            }
        } catch (Exception e) {
            String errorMsg = "Unable to delete arbitrary field with record[" + recordId + "] in table[" + table + "]";
            log.error(errorMsg, e);
            responseArbitraryField.setResult(ERROR);
            responseArbitraryField.setMessage(errorMsg);
        }
        return RESPONSE_ARBITRARY_FIELD_BUILDER.serializeNulls().create().toJson(responseArbitraryField);
    }*/

    /*public String putArbitraryField(String table, String recordId, String fieldName, String value, String type) {
        if (log.isDebugEnabled()) {
            log.debug("Saving arbitrary field[" + fieldName + "] with value[" + value + "] and type [" + type +
                      "] in record[" + recordId + "] in  table[" + table + "]");
        }
        ResponseArbitraryFieldColumn responseArbitraryFieldColumn = new ResponseArbitraryFieldColumn();
        try {
            RecordBean originalRecord = getRecordBean(table, recordId);
            if (originalRecord != null) {
                int i = 0;
                RecordValueEntryBean[] newColumnsList;
                RecordValueEntryBean[] columns = originalRecord.getValues();
                if (columns != null) {
                    newColumnsList = new RecordValueEntryBean[columns.length + 1];
                    for (RecordValueEntryBean column : columns) {
                        newColumnsList[i++] = column;
                    }
                } else {
                    newColumnsList = new RecordValueEntryBean[1];
                }
                RecordValueEntryBean newArbitraryField = new RecordValueEntryBean();
                newArbitraryField.setFieldName(fieldName);
                newArbitraryField.setFieldName(fieldName);
                setRecordValueEntryBeanType(value, newArbitraryField, type);
                newColumnsList[i] = newArbitraryField;
                originalRecord.setValues(newColumnsList);
                originalRecord.setTimestamp(System.currentTimeMillis());
                analyticsWebServiceStub.put(new RecordBean[]{originalRecord});
                responseArbitraryFieldColumn.setColumn(new Column(fieldName, value, type));
                responseArbitraryFieldColumn.setResult(OK);
            }
        } catch (Exception e) {
            String errorMsg = "Unable to save arbitrary field[" + fieldName + "] with record[" + recordId + "] in " +
                              "table[" + table + "]";
            log.error(errorMsg, e);
            responseArbitraryFieldColumn.setResult(ERROR);
            responseArbitraryFieldColumn.setMessage(errorMsg);
        }
        return RESPONSE_ARBITRARY_FIELD_COLUMN_BUILDER.serializeNulls().create().toJson(responseArbitraryFieldColumn);
    }*/

    /*public String putTable(String table, String detailsJsonString, boolean isCreating) {
        if (log.isDebugEnabled()) {
            log.debug("Put table[" + table + "] with values [" + detailsJsonString + "]");
        }
        String msg;
        List<TableSchemaColumn> columnList = new Gson().fromJson(detailsJsonString, TABLE_SCHEMA_TYPE);
        List<String> primaryKeys = new ArrayList<>();
        List<SchemaColumnBean> schemaColumnBeans = new ArrayList<>();
        for (TableSchemaColumn schemaColumn : columnList) {
            if (schemaColumn.getColumn() != null && !schemaColumn.getColumn().isEmpty()) {
                if (schemaColumn.isPrimary()) {
                    primaryKeys.add(schemaColumn.getColumn());
                }
                SchemaColumnBean schemaColumnBean = new SchemaColumnBean();
                schemaColumnBean.setColumnName(schemaColumn.getColumn());
                schemaColumnBean.setColumnType(schemaColumn.getType());
                if (schemaColumn.isScoreParam()) {
                    schemaColumnBean.setColumnType("INTEGER");
                    schemaColumnBean.setScoreParam(true);
                }
                if (schemaColumn.isIndex() || "FACET".equals(schemaColumn.getType())) {
                    schemaColumnBean.setIndex(true);
                }
                schemaColumnBeans.add(schemaColumnBean);
            }
        }
        try {
            if (isCreating) {
                analyticsWebServiceStub.createTable(table);
            }
            AnalyticsSchemaBean analyticsSchemaBean = new AnalyticsSchemaBean();
            SchemaColumnBean[] columnBeans = new SchemaColumnBean[schemaColumnBeans.size()];
            analyticsSchemaBean.setColumns(schemaColumnBeans.toArray(columnBeans));
            String[] primaryKeyArray = new String[primaryKeys.size()];
            analyticsSchemaBean.setPrimaryKeys(primaryKeys.toArray(primaryKeyArray));
            analyticsWebServiceStub.setTableSchema(table, analyticsSchemaBean);
            msg = "Successfully saved table information";
        } catch (Exception e) {
            log.error("Unable to save table information: " + e.getMessage(), e);
            msg = "Unable to save table information: " + e.getMessage();
        }
        return msg;
    }*/

    /*public String deleteTable(String table) {
        if (log.isDebugEnabled()) {
            log.debug("Deleting table[" + table + "]");
        }
        String msg;
        try {
            analyticsWebServiceStub.deleteTable(table);
            msg = "Table " + table + " deleted successfully";
        } catch (Exception e) {
            log.error("Unable to delete table due to " + e.getMessage(), e);
            msg = "Unable to delete table due to " + e.getMessage();
        }
        return msg;
    }*/

    /*public String getTableInfoWithIndexInfo(String table) {
        Map<String, TableSchemaColumn> schemaColumnMap = new HashMap<>();
        try {
            AnalyticsSchemaBean tableSchema = analyticsWebServiceStub.getTableSchema(table);
            if (tableSchema.getColumns() != null) {
                List<String> primaryKeys = null;
                if (tableSchema.getPrimaryKeys() != null) {
                    primaryKeys = Arrays.asList(tableSchema.getPrimaryKeys());
                }
                for (SchemaColumnBean resultColumn : tableSchema.getColumns()) {
                    TableSchemaColumn schemaColumn = new TableSchemaColumn();
                    schemaColumn.setColumn(resultColumn.getColumnName());
                    schemaColumn.setType(resultColumn.getColumnType());
                    schemaColumn.setIndex(resultColumn.getIndex());
                    schemaColumn.setScoreParam(resultColumn.getScoreParam());
                    if (primaryKeys != null && primaryKeys.contains(resultColumn.getColumnName())) {
                        schemaColumn.setPrimary(true);
                    }
                    schemaColumnMap.put(resultColumn.getColumnName(), schemaColumn);
                }
            }
        } catch (Exception e) {
            log.error("Unable to get table information for table:" + table, e);
        }
        return new GsonBuilder().serializeNulls().create().toJson(new ArrayList<>(schemaColumnMap.values()));
    }*/


    public boolean isPaginationSupported() {
        try {
            return analyticsWebServiceStub.isPaginationSupported();
        } catch (RemoteException e) {
            log.error("Unable to check whether pagination support available or not.");
        }
        return false;
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
                    if ("FACET".equalsIgnoreCase(schemaColumnBean.getColumnType())) {
                        facetNameList.add(schemaColumnBean.getColumnName());
                    }
                }
            }
        } catch (Exception e) {
            log.error("Unable to get facet column list for table:" + table, e);
        }
        return new Gson().toJson(facetNameList.toArray(new String[facetNameList.size()]));
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
}
