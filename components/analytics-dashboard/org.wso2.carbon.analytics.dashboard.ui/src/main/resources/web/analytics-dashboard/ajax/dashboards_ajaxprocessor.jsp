<%--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
--%>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceStub" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.stub.data.Dashboard" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.DashboardAdminClient" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.DashboardDTO" %>

<%
    String responseText = "";
    String action = request.getParameter("action");
    DashboardAdminServiceStub stub =   DashboardAdminClient.getDashboardAdminService(config, session, request);
    response.setContentType("application/json");
    Gson gson = new Gson();

    if(action == null) {
        Dashboard[] dashboards = stub.getDashboards();
        DashboardDTO[] dashboardResponse = null;
        int i=0;
        if(dashboards != null && dashboards.length > 0) {
            dashboardResponse = new DashboardDTO[dashboards.length];
            for(Dashboard dashboard : dashboards) {
                dashboardResponse[i++] = new DashboardDTO(dashboard.getId(),dashboard.getTitle(),dashboard.getGroup());
            }
            responseText = gson.toJson(dashboardResponse);
        } else {
            responseText = "[]";
        }
    } else if(action.equals("getDashboardById")) {
        String dashboardId = request.getParameter("dashboardId");
        Dashboard dashboard = stub.getDashboard(dashboardId);
        responseText = gson.toJson(new DashboardDTO(dashboard.getId(),dashboard.getTitle(),dashboard.getGroup()));
    }  else if(action.equals("addDashboard")) {
        Dashboard dashboard = new Dashboard();
        dashboard.setId(String.valueOf(System.currentTimeMillis()));
        dashboard.setTitle(request.getParameter("title"));
        dashboard.setGroup(request.getParameter("group"));
        stub.addDashboard(dashboard);
    } 
%>
<%=responseText%>
