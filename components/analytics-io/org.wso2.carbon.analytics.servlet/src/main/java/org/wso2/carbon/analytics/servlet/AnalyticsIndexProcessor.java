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
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
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
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * Process the index related operations for the remote API, and the remote http clients can use servlet
 * to do index operations by being in a separate node.
 */

public class AnalyticsIndexProcessor extends HttpServlet {
    /**
     * set indices
     *
     * @param req HttpRequest which has the required parameters to do the operation.
     * @param resp HttpResponse which returns the result of the intended operation.
     * @throws javax.servlet.ServletException
     * @throws java.io.IOException
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
            boolean enableSecurity = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.SET_INDICES_OPERATION)) {
                Gson gson = new Gson();
                int tenantId = MultitenantConstants.INVALID_TENANT_ID;
                if (!enableSecurity)
                    tenantId = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                String username = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
                String indicesJson = req.getParameter(AnalyticsAPIConstants.INDEX_PARAM);
                Type stringStringMap = new TypeToken<Map<String, IndexType>>() {
                }.getType();
                Map<String, IndexType> indexTypeMap = gson.fromJson(indicesJson, stringStringMap);
                String scoreJson = req.getParameter(AnalyticsAPIConstants.SCORE_PARAM);
                if (scoreJson != null) {
                    Type scoreLisType = new TypeToken<List<String>>() {
                    }.getType();
                    List<String> scoreParams = gson.fromJson(scoreJson, scoreLisType);
                    try {
                        if (!enableSecurity) ServiceHolder.getAnalyticsDataService().setIndices(tenantId, tableName,
                                indexTypeMap, scoreParams);
                        else ServiceHolder.getSecureAnalyticsDataService().setIndices(username, tableName, indexTypeMap,
                                scoreParams);
                        resp.setStatus(HttpServletResponse.SC_OK);
                    } catch (AnalyticsException e) {
                        resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                    }
                } else {
                    try {
                        if (!enableSecurity)
                            ServiceHolder.getAnalyticsDataService().setIndices(tenantId, tableName, indexTypeMap);
                        else
                            ServiceHolder.getSecureAnalyticsDataService().setIndices(username, tableName, indexTypeMap);
                        resp.setStatus(HttpServletResponse.SC_OK);
                    } catch (AnalyticsException e) {
                        resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                    }
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.WAIT_FOR_INDEXING_OPERATION)) {
                long maxWait = Long.parseLong(req.getParameter(AnalyticsAPIConstants.MAX_WAIT_PARAM));
                try {
                    ServiceHolder.getAnalyticsDataService().waitForIndexing(maxWait);
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

    /**
     * Get the indices
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
            String username = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_INDICES_OPERATION)) {
                try {
                    Map<String, IndexType> indexTypeMap;
                    if (!securityEnabled)
                        indexTypeMap = ServiceHolder.getAnalyticsDataService().getIndices(tenantIdParam, tableName);
                    else indexTypeMap = ServiceHolder.getSecureAnalyticsDataService().getIndices(username, tableName);
                    PrintWriter output = resp.getWriter();
                    output.append(new GsonBuilder().create().toJson(indexTypeMap));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.GET_SCORE_PARAMS_OPERATION)) {
                try {
                    List<String> scoreParams;
                    if (!securityEnabled)
                        scoreParams = ServiceHolder.getAnalyticsDataService().getScoreParams(tenantIdParam, tableName);
                    else
                        scoreParams = ServiceHolder.getSecureAnalyticsDataService().getScoreParams(username, tableName);
                    PrintWriter output = resp.getWriter();
                    output.append(new GsonBuilder().create().toJson(scoreParams));
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
     * delete the index
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
            int tenantIdParam = MultitenantConstants.INVALID_TENANT_ID;
            if (!securityEnabled)
                tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
            String username = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.DELETE_INDICES_OPERATION)) {
                String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
                try {
                    if (!securityEnabled) {
                        ServiceHolder.getAnalyticsDataService().clearIndices(tenantIdParam, tableName);
                    } else {
                        ServiceHolder.getSecureAnalyticsDataService().clearIndices(username, tableName);
                    }
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
}
