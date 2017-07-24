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
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet processes the request to operate the analytics table schema.
 */
public class AnalyticsTableSchemaProcessor extends HttpServlet {

    private static final long serialVersionUID = 2072033492230159580L;
    private static final Log log = LogFactory.getLog(AnalyticsTableSchemaProcessor.class);
    /**
     * set schema
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
            boolean securityEnabled = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            if (!securityEnabled)
                tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
            String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.SET_SCHEMA_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                Object analyticsSchemeObj = GenericUtils.deserializeObject(req.getInputStream());
                if (analyticsSchemeObj != null && analyticsSchemeObj instanceof AnalyticsSchema) {
                    AnalyticsSchema schema = (AnalyticsSchema) analyticsSchemeObj;
                    try {
                        if (!securityEnabled)
                            ServiceHolder.getAnalyticsDataService().setTableSchema(tenantId, tableName, schema);
                        else
                            ServiceHolder.getSecureAnalyticsDataService().setTableSchema(userName, tableName, schema);
                        resp.setStatus(HttpServletResponse.SC_OK);
                    } catch (AnalyticsException e) {
                        resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                    }
                } else {
                    resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unexpected content passed with the request! " +
                            "Expected analytics schema but found " + analyticsSchemeObj);
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with get request!");
                log.error("unsupported operation performed : "+ operation + " with get request!");
            }
        }
    }

    /**
     * Get table schema.
     *
     * @param req  HttpRequest which has the required parameters to do the operation.
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
            int tenantId = MultitenantConstants.INVALID_TENANT_ID;
            if (!securityEnabled)
                tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
            String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_SCHEMA_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                try {
                    AnalyticsSchema schema;
                    if (!securityEnabled)
                        schema = ServiceHolder.getAnalyticsDataService().getTableSchema(tenantId, tableName);
                    else
                        schema = ServiceHolder.getSecureAnalyticsDataService().getTableSchema(userName, tableName);
                    resp.getOutputStream().write(GenericUtils.serializeObject(schema));
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
}
