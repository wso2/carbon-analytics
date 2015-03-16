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
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.messageconsole.stub.MessageConsoleStub;
import org.wso2.carbon.analytics.messageconsole.stub.beans.ColumnBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.EntityBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.RecordBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.RecordResultBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.TableBean;
import org.wso2.carbon.messageconsole.ui.beans.ResponseColumn;
import org.wso2.carbon.messageconsole.ui.beans.ResponseRecord;
import org.wso2.carbon.messageconsole.ui.beans.ResponseResult;
import org.wso2.carbon.messageconsole.ui.beans.ResponseTable;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MessageConsoleConnector {

    private static final Log log = LogFactory.getLog(MessageConsoleConnector.class);
    private static final String MESSAGE_CONSOLE = "MessageConsole";
    private static final String TIMESTAMP = "timestamp";
    private static final String OK = "OK";
    private static final String ERROR = "ERROR";
    public static final String RECORD_ID = "bam_unique_rec_id";

    public static final int TYPE_LIST_RECORD = 1;
    public static final int TYPE_CREATE_RECORD = 2;
    public static final int TYPE_UPDATE_RECORD = 3;
    public static final int TYPE_DELETE_RECORD = 4;
    public static final int TYPE_TABLE_INFO = 5;

    private MessageConsoleStub stub;
    private static GsonBuilder RESPONSE_RESULT_BUILDER = new GsonBuilder().registerTypeAdapter(ResponseResult.class,
                                                                                               new ResponseResultSerializer());

    public MessageConsoleConnector(ConfigurationContext configCtx, String backendServerURL, String cookie) {
        String serviceURL = backendServerURL + MESSAGE_CONSOLE;
        try {
            stub = new MessageConsoleStub(configCtx, serviceURL);
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        } catch (AxisFault axisFault) {
            log.error("Unable to create MessageConsoleStub.", axisFault);
        }
    }

    public String[] getTableList() {
        String[] tableList = null;
        try {
            tableList = stub.listTables();
        } catch (Exception e) {
            log.error("Unable to get table list:" + e.getMessage(), e);
        }

        if (tableList == null) {
            if (log.isDebugEnabled()) {
                log.debug("Receiving a empty table name list!");
            }
            tableList = new String[0];
        }

        return tableList;
    }

    public String getRecords(String tableName, long timeFrom, long timeTo, int startIndex, int pageSize, String
            searchQuery) {

        ResponseResult responseResult = new ResponseResult();
        Gson gson = RESPONSE_RESULT_BUILDER.serializeNulls().create();

        try {
            RecordResultBean recordBeans = stub.getRecords(tableName, timeFrom, timeTo, startIndex, pageSize, searchQuery);
            responseResult.setResult(OK);
            List<ResponseRecord> records = new ArrayList<>();
            if (recordBeans != null) {
                if (recordBeans.getRecords() != null) {
                    for (RecordBean recordBean : recordBeans.getRecords()) {
                        ResponseRecord record = new ResponseRecord();
                        if (recordBean != null) {
                            List<ResponseColumn> columns = new ArrayList<>(recordBean.getEntityBeans().length + 2);
                            columns.add(new ResponseColumn(RECORD_ID, recordBean.getRecordId()));
                            columns.add(new ResponseColumn(TIMESTAMP, String.valueOf(recordBean.getTimestamp())));
                            for (EntityBean entityBean : recordBean.getEntityBeans()) {
                                columns.add(new ResponseColumn(entityBean.getColumnName(), entityBean.getValue()));
                            }
                            record.setColumns(columns);
                        }
                        records.add(record);
                    }
                    responseResult.setRecords(records);
                }
                responseResult.setTotalRecordCount(recordBeans.getTotalResultCount());
            }
        } catch (Exception e) {
            log.error("Unable to get records for table:" + tableName, e);
            responseResult.setResult(ERROR);
            responseResult.setMessage(e.getMessage());
        }

        return gson.toJson(responseResult);
    }

    public String getTableInfo(String tableName) {

        Gson gson = new GsonBuilder().serializeNulls().create();
        TableBean tableInfo = null;
        ResponseTable table = new ResponseTable();
        try {
            tableInfo = stub.getTableInfo(tableName);

            table.setName(tableInfo.getName());
            List<ResponseTable.Column> columns = new ArrayList<>();
            for (ColumnBean resultColumn : tableInfo.getColumns()) {
                ResponseTable.Column column = new ResponseTable().new Column();
                column.setName(resultColumn.getName());
                column.setPrimary(resultColumn.getPrimary());
                column.setType(resultColumn.getType());
                column.setDisplay(resultColumn.getDisplay());
                if (RECORD_ID.equals(resultColumn.getName())) {
                    column.setKey(true);
                    column.setDisplay(false);
                }
                columns.add(column);
            }
            table.setColumns(columns);

        } catch (RemoteException e) {
            log.error("Unable to get table information for table:" + tableName, e);
        }

        return gson.toJson(table);
    }

    public String deleteRecords(String table, String[] recordIds) {

        ResponseResult responseResult = new ResponseResult();
        Gson gson = RESPONSE_RESULT_BUILDER.serializeNulls().create();
        responseResult.setResult(OK);
        try {
            stub.deleteRecords(table, recordIds);
        } catch (Exception e) {
            log.error("Unable to delete records" + Arrays.toString(recordIds) + " from table :" + table, e);
            responseResult.setResult(ERROR);
            responseResult.setMessage(e.getMessage());
        }

        return gson.toJson(responseResult);
    }

    public String addRecord(String table, String[] columns, String[] values) {
        ResponseResult responseResult = new ResponseResult();
        Gson gson = RESPONSE_RESULT_BUILDER.serializeNulls().create();
        responseResult.setResult(OK);
        try {
            stub.addRecord(table, columns, values);
        } catch (Exception e) {
            log.error("Unable to add record to table :" + table, e);
            responseResult.setResult(ERROR);
            responseResult.setMessage(e.getMessage());
        }
        return gson.toJson(responseResult);
    }
}
