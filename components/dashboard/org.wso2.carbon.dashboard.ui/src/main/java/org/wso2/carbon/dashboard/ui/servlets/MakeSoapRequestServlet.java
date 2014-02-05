/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.dashboard.ui.servlets;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.dashboard.ui.DashboardUiContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This servlet is used to provide SOAP request support for gadgets
 */
public class MakeSoapRequestServlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(MakeSoapRequestServlet.class);

    public void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse)
            throws ServletException, IOException {

        //Gathering request parameters
        String endpoint = servletRequest.getParameter("endpoint");
        String payload = servletRequest.getParameter("payload");
        String operation = servletRequest.getParameter("operation");

        if (endpoint == null) {
            servletResponse.getWriter().println("<error><description>An end point URL was not specified</description></error>");
        } else {
            try {
                MakeSoapRequestServiceClient serviceClient = new MakeSoapRequestServiceClient(DashboardUiContext.getConfigContext());
                OMElement response = serviceClient.makeRequest(endpoint, operation, payload);
                servletResponse.getWriter().println(response.toString());
            } catch (Exception e) {
                String errorMessage = "An error occurred while relaying a SOAP payload, " + payload + " to end point " + endpoint;
                log.error(errorMessage, e);
                servletResponse.getWriter().println("<error><description>" + errorMessage + "</description></error>");
            }
        }
    }
}
