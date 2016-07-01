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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.servlet.internal.ServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * The servlet does the analytics table related operations for the analytics service.
 */
public class AnalyticsTableProcessor extends HttpServlet {

    private static final long serialVersionUID = -8592513244152763351L;
    private static final Log log = LogFactory.getLog(AnalyticsTableProcessor.class);

    /**
     * Get the all tables for tenant or check table exists
     *
     * @param req HttpRequest which has the required parameters to do the operation.
     * @param resp HttpResponse which returns the result of the intended operation.
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
            boolean securityEnabled = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
            int tenantIdParam = MultitenantConstants.INVALID_TENANT_ID;
            if (!securityEnabled)
                tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
            String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.TABLE_EXISTS_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                try {
                    boolean tableExists;
                    if (!securityEnabled)
                        tableExists = ServiceHolder.getAnalyticsDataService().tableExists(tenantIdParam, tableName);
                    else
                        tableExists = ServiceHolder.getSecureAnalyticsDataService().tableExists(userName, tableName);
                    PrintWriter output = resp.getWriter();
                    output.append(AnalyticsAPIConstants.TABLE_EXISTS).append(AnalyticsAPIConstants.SEPARATOR).
                            append(String.valueOf(tableExists));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.LIST_TABLES_OPERATION)) {
                try {
                    List<String> tableNames;
                    if (!securityEnabled)
                        tableNames = ServiceHolder.getAnalyticsDataService().listTables(tenantIdParam);
                    else tableNames = ServiceHolder.getSecureAnalyticsDataService().listTables(userName);
                    resp.getOutputStream().write(GenericUtils.serializeObject(tableNames));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with get request!");
                log.error("unsupported operation performed : "+ operation + " with get request!");
            }
        }
    }

    /**
     * create table
     *
     * @param req HttpRequest which has the required parameters to do the operation.
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
            boolean securityEnabled = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            if (!securityEnabled)
                tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
            String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.CREATE_TABLE_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                String recordStoreName = req.getParameter(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM);
                try {
                    if (!securityEnabled) {
                        if (recordStoreName == null) {
                            ServiceHolder.getAnalyticsDataService().createTable(tenantId, tableName);
                        } else {
                            ServiceHolder.getAnalyticsDataService().createTable(tenantId, recordStoreName, tableName);
                        }
                    } else {
                        if (recordStoreName == null) {
                            ServiceHolder.getSecureAnalyticsDataService().createTable(userName, tableName);
                        } else {
                            ServiceHolder.getSecureAnalyticsDataService().createTable(userName, recordStoreName, tableName);
                        }
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.CREATE_IF_NOT_EXISTS_TABLE_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                String recordStoreName = req.getParameter(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM);
                try {
                    if (!securityEnabled){
                        ServiceHolder.getAnalyticsDataService().createTableIfNotExists(tenantId, recordStoreName, tableName);
                    } else {
                        ServiceHolder.getSecureAnalyticsDataService().createTableIfNotExists(userName, recordStoreName, tableName);
                    }
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with post request!");
                log.error("unsupported operation performed : "+ operation + " with post request!");
            }
        }
    }

    /**
     * delete the table
     *
     * @param req HttpRequest which has the required parameters to do the operation.
     * @param resp HttpResponse which returns the result of the intended operation.
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
            boolean securityEnabled = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            if (!securityEnabled)
                tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
            String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.DELETE_TABLE_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                try {
                    if (!securityEnabled) ServiceHolder.getAnalyticsDataService().deleteTable(tenantId, tableName);
                    else ServiceHolder.getSecureAnalyticsDataService().deleteTable(userName, tableName);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with post request!");
                log.error("unsupported operation performed : "+ operation + " with post request!");
            }
        }
    }
}
