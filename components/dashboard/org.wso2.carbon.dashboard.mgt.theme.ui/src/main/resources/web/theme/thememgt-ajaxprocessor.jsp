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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.theme.stub.GSThemeMgtService" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.theme.ui.GSThemeMgtClient" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.theme.stub.types.carbon.Theme" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GSThemeMgtClient client;

    client = new GSThemeMgtClient(cookie, backendServerURL, configContext, request.getLocale());

    String loggeduser = (String) request.getSession().getAttribute("logged-user");


    String funcName = request.getParameter("func");

    if ("actTheme".equals(funcName)) {
        String themePath = request.getParameter("themePath");
        out.print(client.setThemeForUser(themePath, loggeduser));
    } else if ("rollback".equals(funcName)) {
        client.getRollbackToDefTheme(loggeduser);
    }
%>