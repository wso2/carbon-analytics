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

import com.google.gson.GsonBuilder;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.DrillDownResultEntry;
import org.wso2.carbon.analytics.dataservice.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.io.commons.RemoteRecordGroup;
import org.wso2.carbon.analytics.dataservice.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.dataservice.servlet.internal.ServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

public class AnalyticsSearchProcessor extends HttpServlet {

    /**
     * Search the table
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.SEARCH_OPERATION)) {
                int tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                String language = req.getParameter(AnalyticsAPIConstants.LANGUAGE_PARAM);
                String query = req.getParameter(AnalyticsAPIConstants.QUERY);
                int start = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.START_PARAM));
                int count = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.COUNT_PARAM));
                try {
                    List<SearchResultEntry> searchResult = ServiceHolder.getAnalyticsDataService().search(tenantIdParam,
                            tableName, language, query, start, count);
                    PrintWriter output = resp.getWriter();
                    output.append(new GsonBuilder().create().toJson(searchResult));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.SEARCH_COUNT_OPERATION)) {
                int tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                String language = req.getParameter(AnalyticsAPIConstants.LANGUAGE_PARAM);
                String query = req.getParameter(AnalyticsAPIConstants.QUERY);
                try {
                    int count = ServiceHolder.getAnalyticsDataService().searchCount(tenantIdParam,
                            tableName, language, query);
                    PrintWriter output = resp.getWriter();
                    output.append(AnalyticsAPIConstants.SEARCH_COUNT).append(AnalyticsAPIConstants.SEPARATOR).append(String.valueOf(count));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }

    /**
     * Send a drill down request and get the results.
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.DRILL_DOWN_OPERATION)) {
                int tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                ServletInputStream servletInputStream = req.getInputStream();
                ObjectInputStream inputStream = new ObjectInputStream(servletInputStream);
                ObjectOutputStream objectOutputStream = null;
                try {
                    AnalyticsDrillDownRequest drillDownRequest = (AnalyticsDrillDownRequest) inputStream.readObject();
                    Map<String, List<DrillDownResultEntry>> drillDownResult = ServiceHolder.getAnalyticsDataService().
                            drillDown(tenantIdParam, drillDownRequest);
                    objectOutputStream = new ObjectOutputStream(resp.getOutputStream());
                    objectOutputStream.writeObject(drillDownResult);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException | ClassNotFoundException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                } finally {
                    if (servletInputStream != null) {
                        servletInputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (objectOutputStream != null) {
                        objectOutputStream.close();
                    }
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : " + operation
                        + " with get request!");
            }
        }
    }
}
