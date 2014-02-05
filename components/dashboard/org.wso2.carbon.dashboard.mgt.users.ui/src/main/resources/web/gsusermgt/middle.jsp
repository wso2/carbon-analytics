<!--
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
 -->
<%@ page import="java.net.URLConnection" %>
<%@ page import="java.net.URL" %>
<%@ page import="java.io.*" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardUiContext" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardUiUtils" %>
<%@ page
        import="org.wso2.carbon.dashboard.mgt.users.ui.GadgetServerUserManagementUiServiceContext" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    Cookie[] cookies = request.getCookies();
    String sessionId = null;
    if (cookies != null) {
        for (Cookie c : cookies) {
            if (c.getName().equals("JSESSIONID")) {
                sessionId = c.getValue();
            }
        }
    }

    Cookie k = new Cookie("JSESSIONID", sessionId);
    String context = GadgetServerUserManagementUiServiceContext.getRootContext();
//    System.out.println(context);
    k.setPath(context);
    //k.setPath("/");
    response.addCookie(k);
//    System.out.println("request context path : " + request.getContextPath());
    String tenantDomain = "";
    if (request.getSession().getAttribute(RegistryConstants.TENANT_DOMAIN) != null) {
        tenantDomain = (String) request.getSession().getAttribute(RegistryConstants.TENANT_DOMAIN);
    }

    String tDomainFromRequest = (String) request.getAttribute(RegistryConstants.TENANT_DOMAIN);

    //System.out.println("tenant domain from request : " + request.getAttribute(RegistryConstants.TENANT_DOMAIN));
    if (tenantDomain != null && !"".equals(tenantDomain)) {
        //    System.out.println("if t domain : " + CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(context, tenantDomain)) + "dashboard/index.jsp");
        response.sendRedirect(CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(context, tenantDomain)) + "dashboard/index.jsp");
    } else if (tDomainFromRequest != null && !"".equals(tDomainFromRequest)) {
        response.sendRedirect(CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(context, tDomainFromRequest)) + "dashboard/index.jsp");
    } else if (request.getContextPath().equals("/carbon") || request.getContextPath().equals("//carbon")) {
        //    System.out.println("if context path = carbon : " + CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(request)) + "dashboard/index.jsp");
        response.sendRedirect(CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(request)) + "dashboard/index.jsp");
    } else {
        //    System.out.println("if context path is not /carbon : " + CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(request)) + "../dashboard/index.jsp");
        response.sendRedirect(CarbonUIUtil.https2httpURL(DashboardUiUtils.getAdminConsoleURL(request)) + "../dashboard/index.jsp");
    }
%>