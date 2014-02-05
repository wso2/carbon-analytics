<%--<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 ~
 ~ WSO2 Inc. licenses this file to you under the Apache License,
 ~ Version 2.0 (the "License"); you may not use this file except
 ~ in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~    http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing,
 ~ software distributed under the License is distributed on an
 ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 ~ KIND, either express or implied.  See the License for the
 ~ specific language governing permissions and limitations
 ~ under the License.
 -->--%>
<%@page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%

    String loggeduser = (String) request.getSession().getAttribute("logged-user");

    if (loggeduser == null) {
        out.print("timeOut");
        return;
    }


    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetRepoServiceClient gadgetRepoServiceClient = new GadgetRepoServiceClient(cookie,
            backendServerURL,
            configContext,
            request.getLocale());

// Getting the function name
    String funcName = request.getParameter("func");

    if ("addGadget".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String url = request.getParameter("url");
        String dashboardName = request.getParameter("dashboardName");
        String gadgetPath = request.getParameter("gadgetPath");
        String gadgetGroup = request.getParameter("group");

        gadgetRepoServiceClient.incrementUserCount(gadgetPath);
        if (gadgetRepoServiceClient.addGadget(userId, tabId, url, dashboardName, gadgetGroup,gadgetPath)) {
            out.print(true);
        }
    }


%>