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
package org.wso2.carbon.analytics.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.io.commons.RemoteRecordGroup;
import org.wso2.carbon.analytics.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.servlet.internal.ServiceHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This servlet processes the RecordGroup and streams the records into the wire.
 */
public class AnalyticsRecordReadProcessor extends HttpServlet {

    private static final long serialVersionUID = -3656252576106717848L;

    /**
     * Read the record record count
     *
     * @param req  HttpRequest which has the required parameters to do the operation.
     * @param resp HttpResponse which returns the result of the intended operation.
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
            boolean securityEnabled = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
            Gson gson = new Gson();
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_RANGE_RECORD_GROUP_OPERATION)) {
                int tenantId = MultitenantConstants.INVALID_TENANT_ID;
                if (!securityEnabled)
                    tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
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
                    AnalyticsDataResponse analyticsDataResponse;
                    if (!securityEnabled) {
                        analyticsDataResponse = ServiceHolder.getAnalyticsDataService().get(tenantId, tableName, partitionHint,
                                list, timeFrom, timeTo, recordFrom, recordsCount);
                    } else {
                        analyticsDataResponse = ServiceHolder.getSecureAnalyticsDataService().get(userName, tableName,
                                partitionHint, list, timeFrom, timeTo, recordFrom, recordsCount);
                    }
                    RemoteRecordGroup[] remoteRecordGroup = new RemoteRecordGroup[analyticsDataResponse.getRecordGroups().length];
                    for (int i = 0; i < analyticsDataResponse.getRecordGroups().length; i++) {
                        remoteRecordGroup[i] = new RemoteRecordGroup();
                        remoteRecordGroup[i].setBinaryRecordGroup(GenericUtils.serializeObject(analyticsDataResponse.
                                getRecordGroups()[i]));
                        remoteRecordGroup[i].setLocations(analyticsDataResponse.getRecordGroups()[i].getLocations());
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                    GenericUtils.serializeObject(new AnalyticsDataResponse(analyticsDataResponse.getRecordStoreName(),
                            remoteRecordGroup), resp.getOutputStream());
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_IDS_RECORD_GROUP_OPERATION)) {
                int tenantId = MultitenantConstants.INVALID_TENANT_ID;
                if (!securityEnabled)
                    tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                int partitionHint = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM));
                Type columnsList = new TypeToken<List<String>>() {
                }.getType();
                List<String> columns = gson.fromJson(req.getParameter(AnalyticsAPIConstants.COLUMNS_PARAM), columnsList);
                List<String> ids = gson.fromJson(req.getParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM), columnsList);
                try {
                    AnalyticsDataResponse analyticsDataResponse;
                    if (!securityEnabled) {
                        analyticsDataResponse = ServiceHolder.getAnalyticsDataService().get(tenantId, tableName,
                                partitionHint, columns, ids);
                    } else {
                        analyticsDataResponse = ServiceHolder.getSecureAnalyticsDataService().get(userName, tableName,
                                partitionHint, columns, ids);
                    }
                    RemoteRecordGroup[] remoteRecordGroup = new RemoteRecordGroup[analyticsDataResponse.getRecordGroups().length];
                    for (int i = 0; i < analyticsDataResponse.getRecordGroups().length; i++) {
                        remoteRecordGroup[i] = new RemoteRecordGroup();
                        remoteRecordGroup[i].setBinaryRecordGroup(GenericUtils.serializeObject(analyticsDataResponse.
                                getRecordGroups()[i]));
                        remoteRecordGroup[i].setLocations(analyticsDataResponse.getRecordGroups()[i].getLocations());
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                    GenericUtils.serializeObject(new AnalyticsDataResponse(analyticsDataResponse.getRecordStoreName(),
                            remoteRecordGroup), resp.getOutputStream());
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_RECORDS_WITH_KEY_VALUES_OPERATION)) {
                int tenantId = MultitenantConstants.INVALID_TENANT_ID;
                if (!securityEnabled)
                    tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                int partitionHint = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM));
                Type columnsList = new TypeToken<List<String>>() {
                }.getType();
                Type keyValueList = new TypeToken<List<Map<String, Object>>>() {
                }.getType();
                List<String> columns = gson.fromJson(req.getParameter(AnalyticsAPIConstants.COLUMNS_PARAM), columnsList);
                List<Map<String, Object>> valuesBatch = gson.fromJson(req.getParameter(AnalyticsAPIConstants.KEY_VALUE_PARAM), keyValueList);
                try {
                    AnalyticsDataResponse analyticsDataResponse;
                    if (!securityEnabled) {
                        analyticsDataResponse = ServiceHolder.getAnalyticsDataService().getWithKeyValues(tenantId, tableName,
                                partitionHint, columns, valuesBatch);
                    } else {
                        analyticsDataResponse = ServiceHolder.getSecureAnalyticsDataService().getWithKeyValues(userName, tableName,
                                partitionHint, columns, valuesBatch);
                    }
                    RemoteRecordGroup[] remoteRecordGroup = new RemoteRecordGroup[analyticsDataResponse.getRecordGroups().length];
                    for (int i = 0; i < analyticsDataResponse.getRecordGroups().length; i++) {
                        remoteRecordGroup[i] = new RemoteRecordGroup();
                        remoteRecordGroup[i].setBinaryRecordGroup(GenericUtils.serializeObject(analyticsDataResponse.
                                getRecordGroups()[i]));
                        remoteRecordGroup[i].setLocations(analyticsDataResponse.getRecordGroups()[i].getLocations());
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                    GenericUtils.serializeObject(new AnalyticsDataResponse(analyticsDataResponse.getRecordStoreName(),
                            remoteRecordGroup), resp.getOutputStream());
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.READ_RECORD_OPERATION)) {
                String recordStoreName = req.getParameter(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM);
                ServletInputStream servletInputStream = req.getInputStream();
                try {
                    RemoteRecordGroup remoteRecordGroupObj = (RemoteRecordGroup) GenericUtils.deserializeObject(servletInputStream);
                    Iterator<Record> records = ServiceHolder.getAnalyticsDataService().readRecords(recordStoreName, remoteRecordGroupObj.getRecordGroupFromBinary());
                    while (records.hasNext()) {
                        Record record = records.next();
                        GenericUtils.serializeObject(record, resp.getOutputStream());
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }

    /**
     * Stream the records in a given record group.
     *
     * @param req  HttpRequest which has the required parameters to do the operation.
     * @param resp HttpResponse which returns the result of the intended operation.
     * @throws ServletException
     * @throws IOException
     */
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
                String recordStoreName = req.getParameter(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM);
                ServletInputStream servletInputStream = req.getInputStream();
                try {
                    RemoteRecordGroup remoteRecordGroupObj = (RemoteRecordGroup) GenericUtils.deserializeObject(servletInputStream);
                    Iterator<Record> records = ServiceHolder.getAnalyticsDataService().readRecords(recordStoreName, remoteRecordGroupObj.getRecordGroupFromBinary());
                    while (records.hasNext()) {
                        Record record = records.next();
                        GenericUtils.serializeObject(record, resp.getOutputStream());
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }

}
