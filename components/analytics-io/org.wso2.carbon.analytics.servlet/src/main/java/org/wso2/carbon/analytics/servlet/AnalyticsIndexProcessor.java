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

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.servlet.internal.ServiceHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet process the Indexing related operations for remote clients.
 */
public class AnalyticsIndexProcessor extends HttpServlet {
    /**
     * This focuses on changing the wait time for indexing for the given table.
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.WAIT_FOR_INDEXING_OPERATION)) {
                long maxWait = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.MAX_WAIT_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                if (tableName != null) {
                    int tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                    boolean securityEnabled = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
                    String username = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
                    try {
                        if (securityEnabled) {
                            ServiceHolder.getAnalyticsDataService().waitForIndexing(tenantId, tableName, maxWait);
                        }else {
                            ServiceHolder.getSecureAnalyticsDataService().waitForIndexing(username, tableName, maxWait);
                        }
                        resp.setStatus(HttpServletResponse.SC_OK);
                    } catch (AnalyticsException e) {
                        resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                    }
                } else {
                    try {
                        ServiceHolder.getAnalyticsDataService().waitForIndexing(maxWait);
                        resp.setStatus(HttpServletResponse.SC_OK);
                    } catch (AnalyticsException e) {
                        resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * This focuses on deleting the index data for the given table.
     *
     * @param req  HttpRequest which has the required parameters to do the operation.
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.DELETE_INDICES_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                try {
                    if (!securityEnabled) ServiceHolder.getAnalyticsDataService().clearIndexData(tenantId, tableName);
                    else ServiceHolder.getSecureAnalyticsDataService().clearIndexData(userName, tableName);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed : "
                        + operation + " with post request!");
            }
        }
    }
}
