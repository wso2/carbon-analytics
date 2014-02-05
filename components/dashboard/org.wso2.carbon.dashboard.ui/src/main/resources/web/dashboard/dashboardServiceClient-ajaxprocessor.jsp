<%--
 /*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 WSO2 Inc. licenses this file to you under the Apache License,
 Version 2.0 (the "License"); you may not use this file except
 in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.*/--%>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);

    String context = request.getContextPath();

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    DashboardServiceClient dashboardServiceClient = new DashboardServiceClient(cookie,
            backendServerURL,
            configContext,
            request.getLocale());

    // Getting the function name
    String funcName = request.getParameter("func");
    if ("sessionValid".equals(funcName)) {
        out.print(dashboardServiceClient.isSessionValid());

    } else if ("addGadget".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String url = request.getParameter("url");
        String dashboardName = request.getParameter("dashboardName");
        String group = request.getParameter("group");

        out.print(dashboardServiceClient.addGadgetToUser(userId, tabId, url, dashboardName, group));

    } else if ("addNewTab".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabTitle = request.getParameter("tabTitle");
        String dashboardName = request.getParameter("dashboardName");

        out.print(dashboardServiceClient.addNewTab(userId, tabTitle, dashboardName));

    } else if ("getGadgetLayout".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String dashboardName = request.getParameter("dashboardName");

        out.print(dashboardServiceClient.getGadgetLayout(userId, tabId, dashboardName));

    } else if ("getGadgetPrefs".equals(funcName)) {
        String userId = request.getParameter("userId");
        String gadgetId = request.getParameter("gadgetId");
        String prefId = request.getParameter("prefId");
        String dashboardName = request.getParameter("dashboardName");

        out.print(dashboardServiceClient.getGadgetPrefs(userId, gadgetId, prefId, dashboardName));

    } else if ("getGadgetUrlsToLayout".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String dashboardName = request.getParameter("dashboardName");

        String[] urlsToLayout =
                dashboardServiceClient.getGadgetUrlsToLayout(userId, tabId, dashboardName, adminConsoleURL);
        String jsArray = "";
        if (urlsToLayout != null) {
            for (int x = 0; x < urlsToLayout.length; x++) {
                if (x == 0) {
                    jsArray = jsArray + urlsToLayout[x];
                } else {
                    jsArray = jsArray + "," + urlsToLayout[x];
                }
            }
        }

        out.print(jsArray);

    } else if ("getTabLayout".equals(funcName)) {
        String userId = request.getParameter("userId");
        String dashboardName = request.getParameter("dashboardName");

        out.print(dashboardServiceClient.getTabLayout(userId, dashboardName));

    } else if ("getTabTitle".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String dashboardName = request.getParameter("dashboardName");

        out.print(dashboardServiceClient.getTabTitle(userId, tabId, dashboardName));

    } else if ("isReadOnlyMode".equals(funcName)) {
        String userId = request.getParameter("userId");

        out.print(dashboardServiceClient.isReadOnlyMode(userId));

    } else if ("removeGadget".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String dashboardName = request.getParameter("dashboardName");
        String gadgetId = request.getParameter("gadgetId");

        out.print(dashboardServiceClient.removeGadget(userId, tabId, gadgetId, dashboardName));

    } else if ("removeTab".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String dashboardName = request.getParameter("dashboardName");

        out.print(dashboardServiceClient.removeTab(userId, tabId, dashboardName));

    } else if ("setGadgetLayout".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String dashboardName = request.getParameter("dashboardName");
        String newLayout = request.getParameter("newLayout");

        out.print(dashboardServiceClient.setGadgetLayout(userId, tabId, newLayout, dashboardName));

    } else if ("setGadgetPrefs".equals(funcName)) {
        String userId = request.getParameter("userId");
        String gadgetId = request.getParameter("gadgetId");
        String prefId = request.getParameter("prefId");
        String value = request.getParameter("value");
        String dashboardName = request.getParameter("dashboardName");

        out.print(dashboardServiceClient.setGadgetPrefs(userId, gadgetId, prefId, value,
                dashboardName));

    } else if ("duplicateTab".equals(funcName)) {
        String userId = request.getParameter("userId");
        String dashboardName = request.getParameter("dashboardName");
        String sourceTabId = request.getParameter("sourceTabId");
        String newTabName = request.getParameter("newTabName");

        out.print(dashboardServiceClient.duplicateTab(userId, dashboardName, sourceTabId, newTabName));
    } else if ("copyGadget".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tab");
        String dashboardName = request.getParameter("dashboardName");
        String sourceGadgetId = request.getParameter("sourceGadgetId");

        out.print(dashboardServiceClient.copyGadget(userId, tabId, dashboardName, sourceGadgetId, "G1#"));
    } else if ("moveGadget".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tab");
        String dashboardName = request.getParameter("dashboardName");
        String sourceGadgetId = request.getParameter("sourceGadgetId");

        out.print(dashboardServiceClient.moveGadgetToTab(userId, dashboardName, sourceGadgetId, tabId));
    } else if ("populateDefaultThreeColumnLayout".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");

        out.print(dashboardServiceClient.populateDefaultThreeColumnLayout(userId, tabId));
    } else if ("populateCustomLayouts".equals(funcName)) {
        String userId = request.getParameter("userId");
        String tabId = request.getParameter("tabId");
        String layout = request.getParameter("layout");
        String dashboardName = request.getParameter("dashboardName");

        out.print(dashboardServiceClient.populateCustomLayouts(userId, tabId, layout, dashboardName));
    } else if ("getTabContentAsJson".equals(funcName)) {
        String userId = request.getParameter("userId");
        String dashboardName = request.getParameter("dashboardName");
        String tDomain = request.getParameter("tDomain");
        String tabId = request.getParameter("tabId");
        out.print(dashboardServiceClient.getTabContentBeanAsJson(userId, dashboardName, tDomain, backendServerURL, tabId));
    } else if ("getTabLayoutWithNames".equals(funcName)) {
        String userId = request.getParameter("userId");
        String dashboardName = request.getParameter("dashboardName");
        out.print(dashboardServiceClient.getTabLayoutWithNames(userId, dashboardName));
    } else if ("populateDashboardTab".equals(funcName)) {
        String tabId = request.getParameter("tabId");
        out.print(dashboardServiceClient.populateDashboardTab(tabId));
    }

%>