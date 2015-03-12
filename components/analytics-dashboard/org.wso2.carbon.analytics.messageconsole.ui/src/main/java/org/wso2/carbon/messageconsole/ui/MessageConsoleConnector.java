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
import org.wso2.carbon.analytics.messageconsole.stub.beans.TableBean;
import org.wso2.carbon.messageconsole.ui.beans.ResponseColumn;
import org.wso2.carbon.messageconsole.ui.beans.ResponseRecord;
import org.wso2.carbon.messageconsole.ui.beans.ResponseResult;
import org.wso2.carbon.messageconsole.ui.beans.ResponseTable;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class MessageConsoleConnector {

    private static final Log log = LogFactory.getLog(MessageConsoleConnector.class);
    private static final String MESSAGE_CONSOLE = "MessageConsole";
    private static final String RECORD_ID = "recordId";
    private static final String TIMESTAMP = "timestamp";
    private static final String OK = "OK";
    private static final String ERROR = "ERROR";

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

    public String getRecords(String tableName) {

        ResponseResult responseResult = new ResponseResult();
        Gson gson = RESPONSE_RESULT_BUILDER.serializeNulls().create();

        try {
            RecordBean[] recordBeans = stub.getRecords(tableName);
            responseResult.setResult(OK);
            if (recordBeans != null) {
                List<ResponseRecord> records = new ArrayList<>(recordBeans.length);
                for (RecordBean recordBean : recordBeans) {
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
                responseResult.setTotalRecordCount(records.size());
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
                columns.add(column);
            }
            table.setColumns(columns);

        } catch (RemoteException e) {
            log.error("Unable to get table information for table:" + tableName, e);
        }

        return gson.toJson(table);
    }
}
