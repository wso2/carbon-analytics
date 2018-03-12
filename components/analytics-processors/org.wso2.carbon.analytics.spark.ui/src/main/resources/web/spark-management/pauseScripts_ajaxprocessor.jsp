<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.analytics.spark.ui.client.AnalyticsExecutionClient" %>
<%@ page import="org.owasp.encoder.Encode" %>

<!--
~ Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%
    if (!"post".equalsIgnoreCase(request.getMethod())) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    try {
        AnalyticsExecutionClient client = new AnalyticsExecutionClient(cookie, serverURL, configContext);
        client.pauseScripts();

%>

<%
} catch (Exception e) {
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,e.getMessage());
 }
%>
