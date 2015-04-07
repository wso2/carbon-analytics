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
import org.wso2.carbon.analytics.dataservice.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.dataservice.servlet.internal.ServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;

public class AnalyticsRecordProcessor extends HttpServlet {

    /**
     * Get record count
     *
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_RECORD_COUNT_OPERATION)) {
                int tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                long timeFrom = Long.parseLong(req.getParameter(AnalyticsAPIConstants.TIME_FROM_PARAM));
                long timeTo = Long.parseLong(req.getParameter(AnalyticsAPIConstants.TIME_TO_PARAM));
                try {
                    long recordCount = ServiceHolder.getAnalyticsDataService().getRecordCount(tenantIdParam, tableName,
                            timeFrom, timeTo);
                    PrintWriter outputWriter = resp.getWriter();
                    outputWriter.append(AnalyticsAPIConstants.RECORD_COUNT).append(AnalyticsAPIConstants.SEPARATOR).
                            append(String.valueOf(recordCount));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            }else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }

    /**
     * Put records
     *
     * @param req
     * @param resp
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.PUT_RECORD_OPERATION)) {
                String recordsJson = req.getParameter(AnalyticsAPIConstants.RECORDS_PARAM);
                Type recordListType = new TypeToken<List<Record>>() {
                }.getType();
                List<Record> records = new Gson().fromJson(recordsJson, recordListType);
                try {
                    ServiceHolder.getAnalyticsDataService().put(records);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            }else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }

    /**
     * delete records for range and given ids..
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.DELETE_RECORDS_RANGE_OPERATION)) {
                int tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                long timeFrom = Long.parseLong(req.getParameter(AnalyticsAPIConstants.TIME_FROM_PARAM));
                long timeTo = Long.parseLong(req.getParameter(AnalyticsAPIConstants.TIME_TO_PARAM));
                try {
                     ServiceHolder.getAnalyticsDataService().delete(tenantIdParam, tableName,
                            timeFrom, timeTo);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            }else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.DELETE_RECORDS_IDS_OPERATION)){
                int tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                String jsonRecordIds = req.getParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM);
                Type recordIdListType = new TypeToken<List<String>>() {
                }.getType();
                List<String> recordIds = new Gson().fromJson(jsonRecordIds, recordIdListType);
                try {
                    ServiceHolder.getAnalyticsDataService().delete(tenantIdParam, tableName, recordIds);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            }else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }

}
