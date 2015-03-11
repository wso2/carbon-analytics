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
import org.wso2.carbon.analytics.messageconsole.stub.beans.EntityBean;
import org.wso2.carbon.analytics.messageconsole.stub.beans.RecordBean;
import org.wso2.carbon.messageconsole.ui.beans.Entity;
import org.wso2.carbon.messageconsole.ui.beans.Record;

import java.util.ArrayList;
import java.util.List;

public class MessageConsoleConnector {

    private static final Log log = LogFactory.getLog(MessageConsoleConnector.class);
    private static final String MESSAGE_CONSOLE = "MessageConsole";

    private MessageConsoleStub stub;

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
        List<Record> records = new ArrayList<>();
        Gson gson = new GsonBuilder().serializeNulls().create();
        try {
            RecordBean[] recordBeans = stub.getRecords(tableName);
            if (recordBeans != null) {
                for (RecordBean recordBean : recordBeans) {
                    Record record = new Record();
                    record.setRecordId(recordBean.getRecordId());
                    record.setTimestamp(recordBean.getTimestamp());
                    for (EntityBean entityBean : recordBean.getEntityBeans()) {
                        record.getEntities().add(new Entity(entityBean.getColumnName(), entityBean.getValue()));
                    }
                    records.add(record);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return gson.toJson(records);
    }
}
