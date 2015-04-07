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
package org.wso2.carbon.analytics.dataservice.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.wso2.carbon.analytics.dataservice.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.dataservice.io.commons.RemoteRecordGroup;
import org.wso2.carbon.analytics.dataservice.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.dataservice.servlet.internal.ServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class AnalyticsRecordReadProcessor extends HttpServlet {

    /**
     * Read the record record count
     *
     * @param req
     * @param resp
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
     */

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getHeader(AnalyticsAPIConstants.SESSION_ID);
        if (sessionId == null || sessionId.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
        } else {
            try {
                ServiceHolder.getAuthenticator().validateSessionId(sessionId);
            } catch (AnalyticsAPIAuthenticationException e) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
            }
            String operation = req.getParameter(AnalyticsAPIConstants.OPERATION);
            Gson gson = new Gson();
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_RANGE_RECORD_GROUP_OPERATION)) {
                int tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                int partitionHint = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM));
                Type columnsList = new TypeToken<List<String>>() {
                }.getType();
                List<String> list = gson.fromJson(req.getParameter(AnalyticsAPIConstants.COLUMNS_PARAM), columnsList);
                long timeFrom = Long.parseLong(req.getParameter(AnalyticsAPIConstants.TIME_FROM_PARAM));
                long timeTo = Long.parseLong(req.getParameter(AnalyticsAPIConstants.TIME_TO_PARAM));
                int recordFrom = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.RECORD_FROM_PARAM));
                int recordsCount = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.COUNT_PARAM));
                try {
                    RecordGroup[] recordGroups = ServiceHolder.getAnalyticsDataService().get(tenantId, tableName, partitionHint, list, timeFrom, timeTo,
                            recordFrom, recordsCount);
                    RemoteRecordGroup[] remoteRecordGroup = new RemoteRecordGroup[recordGroups.length];
                    for (int i = 0; i < recordGroups.length; i++) {
                        remoteRecordGroup[i] = new RemoteRecordGroup();
                        remoteRecordGroup[i].setBinaryRecordGroup(getBinaryRecordGroup(recordGroups[i]));
                        remoteRecordGroup[i].setLocations(recordGroups[i].getLocations());
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(resp.getOutputStream());
                    objectOutputStream.writeObject(remoteRecordGroup);
                    objectOutputStream.close();
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }

            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_IDS_RECORD_GROUP_OPERATION)) {
                int tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                int partitionHint = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM));
                Type columnsList = new TypeToken<List<String>>() {
                }.getType();
                List<String> columns = gson.fromJson(req.getParameter(AnalyticsAPIConstants.COLUMNS_PARAM), columnsList);
                List<String> ids = gson.fromJson(req.getParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM), columnsList);
                try {
                    RecordGroup[] recordGroups = ServiceHolder.getAnalyticsDataService().get(tenantId, tableName,
                            partitionHint, columns, ids);
                    RemoteRecordGroup[] remoteRecordGroup = new RemoteRecordGroup[recordGroups.length];
                    for (int i = 0; i < recordGroups.length; i++) {
                        remoteRecordGroup[i] = new RemoteRecordGroup();
                        remoteRecordGroup[i].setBinaryRecordGroup(getBinaryRecordGroup(recordGroups[i]));
                        remoteRecordGroup[i].setLocations(recordGroups[i].getLocations());
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                    ObjectOutputStream objectOutputStream = new ObjectOutputStream(resp.getOutputStream());
                    objectOutputStream.writeObject(remoteRecordGroup);
                    objectOutputStream.close();
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }

            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.READ_RECORD_OPERATION)) {
                ServletInputStream servletInputStream = req.getInputStream();
                ObjectInputStream inputStream = new ObjectInputStream(servletInputStream);
                try {
                    RemoteRecordGroup remoteRecordGroupObj = (RemoteRecordGroup) inputStream.readObject();
                    Iterator<Record> records = ServiceHolder.getAnalyticsDataService().readRecords(remoteRecordGroupObj.getRecordGroupFromBinary());
                    resp.setStatus(HttpServletResponse.SC_OK);
                    ObjectOutputStream outputStream = new ObjectOutputStream(resp.getOutputStream());
                    while (records.hasNext()) {
                        Record record = records.next();
                        outputStream.writeObject(record);
                    }
                    outputStream.close();
                } catch (ClassNotFoundException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No class found for the " +
                            "record group implementation : " + ". " + e.getMessage());
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getHeader(AnalyticsAPIConstants.SESSION_ID);
        if (sessionId == null || sessionId.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
        } else {
            try {
                ServiceHolder.getAuthenticator().validateSessionId(sessionId);
            } catch (AnalyticsAPIAuthenticationException e) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
            }
            String operation = req.getParameter(AnalyticsAPIConstants.OPERATION);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.READ_RECORD_OPERATION)) {
                ServletInputStream servletInputStream = req.getInputStream();
                ObjectInputStream inputStream = new ObjectInputStream(servletInputStream);
                try {
                    RemoteRecordGroup remoteRecordGroupObj = (RemoteRecordGroup) inputStream.readObject();
                    Iterator<Record> records = ServiceHolder.getAnalyticsDataService().readRecords(remoteRecordGroupObj.getRecordGroupFromBinary());
                    resp.setStatus(HttpServletResponse.SC_OK);
                    ObjectOutputStream outputStream = new ObjectOutputStream(resp.getOutputStream());
                    while (records.hasNext()) {
                        Record record = records.next();
                        outputStream.writeObject(record);
                    }
                    outputStream.close();
                } catch (ClassNotFoundException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "No class found for the " +
                            "record group implementation : " + ". " + e.getMessage());
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }


    private byte[] getBinaryRecordGroup(RecordGroup recordGroup) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(recordGroup);
        return out.toByteArray();
    }
}
