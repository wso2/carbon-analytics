/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.hive.ui.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.ui.cron.CronBuilderConstants;
import org.wso2.carbon.analytics.hive.ui.cron.CronExpressionBuilder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SaveCronExpression extends HttpServlet {
    private static Log log = LogFactory.getLog(SaveCronExpression.class);

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String cronExpression = "";
        String message = "";
        if (request.getParameter("optionCron").equalsIgnoreCase("selectUI")) {
            HashMap<String, String> cronVals = new HashMap<String, String>();
            cronVals.put(CronBuilderConstants.YEAR, request.getParameter("yearSelected"));
            cronVals.put(CronBuilderConstants.MONTH, request.getParameter("monthSelected"));
            if (request.getParameter("selectDay").equalsIgnoreCase("selectDayMonth")) {
                cronVals.put(CronBuilderConstants.DAY_OF_MONTH, request.getParameter("dayMonthSelected"));
            } else {
                cronVals.put(CronBuilderConstants.DAY_OF_WEEK, request.getParameter("dayWeekSelected"));
            }
            cronVals.put(CronBuilderConstants.HOURS, request.getParameter("hoursSelected"));
            cronVals.put(CronBuilderConstants.MINUTES, request.getParameter("minutesSelected"));

            CronExpressionBuilder cronBuilder = CronExpressionBuilder.getInstance();
            cronExpression = cronBuilder.getCronExpression(cronVals);
            message = "Successfully updated the script scheduling.# cron expression: #" + cronExpression;
        } else if (request.getParameter("optionCron").equalsIgnoreCase("customCron")) {
            cronExpression = request.getParameter("customCron");
            message = "Successfully updated the script scheduling.# cron expression: #" + cronExpression;
        } else {
            message = "Interval wise scheduling is not supported yet";
        }
        log.info(cronExpression);

        PrintWriter writer = null;
        try {
            writer = response.getWriter();
            writer.print(message);
        } catch (IOException e) {
            log.error("Error while setting the cronExpression");
            writer.print("Error while scheduling the script");
        }

    }


    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException            if an I/O error occurs
     */

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SaveScriptProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            processRequest(request, response);
        } catch (Exception ex) {
            Logger.getLogger(SaveScriptProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */

    public String getServletInfo() {
        return "used to save the Hive script";
    }// </editor-fold>
}
