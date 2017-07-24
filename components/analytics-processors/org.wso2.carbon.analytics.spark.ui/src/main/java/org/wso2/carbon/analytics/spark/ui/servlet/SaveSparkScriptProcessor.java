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
package org.wso2.carbon.analytics.spark.ui.servlet;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException;
import org.wso2.carbon.analytics.spark.ui.client.AnalyticsExecutionClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SaveSparkScriptProcessor extends HttpServlet {
    
    private static final long serialVersionUID = 5936476293277591602L;
    
    private static Log log = LogFactory.getLog(SaveSparkScriptProcessor.class);

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String serverURL = CarbonUIUtil.getServerURL(getServletContext(), request.getSession());
        ConfigurationContext configContext =
                (ConfigurationContext) getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) request.getSession().getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


        String scriptName = request.getParameter("name");
        String scriptContent = request.getParameter("queries");
        String operation = request.getParameter("operation");
        String cron = request.getParameter("cronExp");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            try {
                AnalyticsExecutionClient client = new AnalyticsExecutionClient(cookie, serverURL, configContext);
                if (operation != null && operation.equals("update")) {
                    client.updateScript(scriptName, scriptContent, cron);
                } else {
                    client.saveScript(scriptName, scriptContent, cron);
                }
                out.println("Successfully saved the spark sql script " + scriptName);
            } catch (AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException e) {
                out.println("Error while saving the script. " + e.getFaultMessage().getAnalyticsProcessorAdminException().getMessage());
            }
        } catch (IOException e) {
            log.error("Error while writing to the response..", e);
        }
    }


    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SaveSparkScriptProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SaveSparkScriptProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getServletInfo() {
        return "used to save the spark script";
    }
}

