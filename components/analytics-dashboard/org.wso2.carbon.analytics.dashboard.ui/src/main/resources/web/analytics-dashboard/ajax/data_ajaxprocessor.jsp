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
<%@ page import="org.wso2.carbon.analytics.dashboard.stub.data.Table" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.DashboardAdminClient" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.TableDTO" %>

<%

    String responseText = "";
    String tableName = request.getParameter("table");
    DashboardAdminServiceStub stub =   DashboardAdminClient.getDashboardAdminService(config, session, request);
    response.setContentType("application/json");
    Gson gson = new Gson();

    Table table = stub.getRecords(tableName,DashboardAdminClient.timestampFrom("01/09/1985"),DashboardAdminClient.timestampFrom("01/09/2100"),0,10,null);
    // System.out.println("+++ Num rows:  " + table.
    TableDTO dto = DashboardAdminClient.toTableDTO(table);
    
    System.out.println("+++ Received by FE");
    responseText = gson.toJson(dto);

%>
<%=responseText%>

