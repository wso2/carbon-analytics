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

import org.apache.axiom.om.util.Base64;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.servlet.internal.ServiceHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This servlet intend to process the request which controls overall analytics data api operations.
 *
 */
public class AnalyticsManagementProcessor extends HttpServlet {

    /**
     * Login operation for remote analytics api servlet.
     *
     * @param req HttpRequest which has the required parameters to do the operation.
     * @param resp HttpResponse which returns the result of the intended operation.
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String operation = req.getParameter(AnalyticsAPIConstants.OPERATION);
        if (operation != null && operation.equalsIgnoreCase(AnalyticsAPIConstants.LOGIN_OPERATION)) {
            String[] credentials = getUserPassword(req.getHeader(AnalyticsAPIConstants.AUTHORIZATION_HEADER));
            if (credentials == null) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid authentication!");
            } else {
                String userName = credentials[0];
                String password = credentials[1];
                try {
                    String sessionId = ServiceHolder.getAuthenticator().authenticate(userName, password);
                    PrintWriter writer = resp.getWriter();
                    writer.print(AnalyticsAPIConstants.SESSION_ID + AnalyticsAPIConstants.SEPARATOR + sessionId);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsAPIAuthenticationException e) {
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized user: " + userName);
                }
            }
        } else if (operation != null && operation.equalsIgnoreCase(AnalyticsAPIConstants.IS_PAGINATION_SUPPORTED_OPERATION)) {
            String sessionId = req.getHeader(AnalyticsAPIConstants.SESSION_ID);
            if (sessionId == null || sessionId.trim().isEmpty()) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
            } else {
                try {
                    ServiceHolder.getAuthenticator().validateSessionId(sessionId);
                } catch (AnalyticsAPIAuthenticationException e) {
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
                }
            }
            boolean isSupported = ServiceHolder.getAnalyticsDataService().isPaginationSupported();
            PrintWriter writer = resp.getWriter();
            writer.print(AnalyticsAPIConstants.PAGINATION_SUPPORT + AnalyticsAPIConstants.SEPARATOR + isSupported);
            resp.setStatus(HttpServletResponse.SC_OK);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "Unavailable operation - " + operation + " provided!");
        }
    }

    private String[] getUserPassword(String authHeader) {
        if (authHeader == null) {
            return null;
        }
        if (!authHeader.startsWith("Basic ")) {
            return null;
        }
        String[] userPassword = new String(Base64.decode(authHeader.substring(6))).split(":");
        if (userPassword.length != 2) {
            return null;
        }
        return userPassword;
    }


}
