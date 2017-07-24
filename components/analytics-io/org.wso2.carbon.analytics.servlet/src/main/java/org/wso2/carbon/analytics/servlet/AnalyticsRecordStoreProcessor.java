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
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.servlet.internal.ServiceHolder;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class AnalyticsRecordStoreProcessor extends HttpServlet {

    private static final long serialVersionUID = 5655324228428424513L;
    private static final Log log = LogFactory.getLog(AnalyticsRecordStoreProcessor.class);

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.
                    GET_RECORD_STORE_OF_TABLE_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                if (!securityEnabled) {
                    int tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                    try {
                        String recordStore = ServiceHolder.getAnalyticsDataService().getRecordStoreNameByTable(tenantId,
                                tableName);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        PrintWriter responseWriter = resp.getWriter();
                        responseWriter.print(AnalyticsAPIConstants.RECORD_STORE_NAME + AnalyticsAPIConstants.SEPARATOR
                                + recordStore);
                    } catch (AnalyticsException e) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    }
                } else {
                    String username = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
                    try {
                        String recordStore = ServiceHolder.getSecureAnalyticsDataService().
                                getRecordStoreNameByTable(username, tableName);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        PrintWriter responseWriter = resp.getWriter();
                        responseWriter.print(AnalyticsAPIConstants.RECORD_STORE_NAME + AnalyticsAPIConstants.SEPARATOR
                                + recordStore);
                    } catch (AnalyticsException e) {
                        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                    }
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.LIST_RECORD_STORES_OPERATION)) {
                List<String> recordStores = ServiceHolder.getAnalyticsDataService().listRecordStoreNames();
                GenericUtils.serializeObject(recordStores, resp.getOutputStream());
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with get request!");
                log.error("unsupported operation performed : "+ operation + " with get request!");
            }
        }
    }
}
