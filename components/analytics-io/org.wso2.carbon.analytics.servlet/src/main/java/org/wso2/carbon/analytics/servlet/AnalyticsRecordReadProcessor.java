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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse.Entry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This servlet processes the RecordGroup and streams the records into the wire.
 */
public class AnalyticsRecordReadProcessor extends HttpServlet {

    private static final long serialVersionUID = -3656252576106717848L;
    private static final Log log = LogFactory.getLog(AnalyticsRecordReadProcessor.class);
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_RANGE_RECORD_GROUP_OPERATION)) {
                doGetRangeRecordGroupOperation(securityEnabled, req, resp);
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_IDS_RECORD_GROUP_OPERATION)) {
                doIdsRecordGroup(securityEnabled, req, resp);
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_RECORDS_WITH_KEY_VALUES_OPERATION)) {
                doGetRecordWithKeyValues(securityEnabled, req, resp);
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.READ_RECORD_OPERATION)) {
                doReadRecords(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with get request!");
                log.error("unsupported operation performed : "+ operation + " with post request!");
            }
        }
    }

    private void doGetRangeRecordGroupOperation(boolean securityEnabled, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        Gson gson = new Gson();
        if (!securityEnabled) {
            tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
        }
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
            resp.setStatus(HttpServletResponse.SC_OK);
            GenericUtils.serializeObject(this.localToRemoteAnalyticsDataResponse(analyticsDataResponse), 
                    resp.getOutputStream());
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
    
    private RecordGroup localToRemoteRecordGroup(RecordGroup rg) throws AnalyticsException {
        RemoteRecordGroup result = new RemoteRecordGroup();
        result.setBinaryRecordGroup(GenericUtils.serializeObject(rg));
        result.setLocations(rg.getLocations());
        return result;
    }
    
    private AnalyticsDataResponse localToRemoteAnalyticsDataResponse(AnalyticsDataResponse resp) throws AnalyticsException {
        List<Entry> localEntries = resp.getEntries();
        List<Entry> remoteEntries = new ArrayList<>(localEntries.size());
        for (Entry entry : localEntries) {
            remoteEntries.add(new Entry(entry.getRecordStoreName(), this.localToRemoteRecordGroup(entry.getRecordGroup())));
        }
        return new AnalyticsDataResponse(remoteEntries);
    }

    private void doIdsRecordGroup(boolean securityEnabled, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        Gson gson = new Gson();
        if (!securityEnabled) {
            tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
        }
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
            resp.setStatus(HttpServletResponse.SC_OK);
            GenericUtils.serializeObject(this.localToRemoteAnalyticsDataResponse(analyticsDataResponse), 
                    resp.getOutputStream());
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void doGetRecordWithKeyValues(boolean securityEnabled, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        int tenantId = MultitenantConstants.INVALID_TENANT_ID;
        Gson gson = new Gson();
        if (!securityEnabled) {
            tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
        }
        String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
        String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
        int partitionHint = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM));
        Type columnsList = new TypeToken<List<String>>() {
        }.getType();
        Type keyValueList = new TypeToken<List<Map<String, Object>>>() {
        }.getType();
        List<String> columns = gson.fromJson(req.getParameter(AnalyticsAPIConstants.COLUMNS_PARAM), columnsList);
        List<Map<String, Object>> valuesBatch = gson.fromJson(req.getParameter(AnalyticsAPIConstants.KEY_VALUE_PARAM),
                keyValueList);
        try {
            AnalyticsDataResponse analyticsDataResponse;
            if (!securityEnabled) {
                analyticsDataResponse = ServiceHolder.getAnalyticsDataService().getWithKeyValues(tenantId, tableName,
                        partitionHint, columns, valuesBatch);
            } else {
                analyticsDataResponse = ServiceHolder.getSecureAnalyticsDataService().getWithKeyValues(userName, tableName,
                        partitionHint, columns, valuesBatch);
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            GenericUtils.serializeObject(this.localToRemoteAnalyticsDataResponse(analyticsDataResponse),
                    resp.getOutputStream());
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private void doReadRecords(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String recordStoreName = req.getParameter(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM);
        ServletInputStream servletInputStream = req.getInputStream();
        try {
            RemoteRecordGroup remoteRecordGroupObj = (RemoteRecordGroup) GenericUtils.deserializeObject(servletInputStream);
            AnalyticsIterator<Record> records = ServiceHolder.getAnalyticsDataService().readRecords(recordStoreName,
                    remoteRecordGroupObj.getRecordGroupFromBinary());
            while (records.hasNext()) {
                Record record = records.next();
                GenericUtils.serializeObject(record, resp.getOutputStream());
            }
            records.close();
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
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
                doReadRecords(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with get request!");
                log.error("unsupported operation performed : "+ operation + " with get request!");
            }
        }
    }

}
