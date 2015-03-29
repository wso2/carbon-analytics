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
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.DashboardAdminClient" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.TableDTO" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.ColumnDTO" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.batch.stub.BatchAnalyticsDashboardAdminServiceStub" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.batch.stub.data.Table" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.batch.stub.data.Column" %>

<%

    String responseText = "";
    String action = request.getParameter("action");
    BatchAnalyticsDashboardAdminServiceStub stub =   DashboardAdminClient.getDashboardBatchAnalyticsAdminService(config,
            session, request);
    response.setContentType("application/json");
    Gson gson = new Gson();

    if(action == null) {
        String tableName = request.getParameter("table");
        Table table = stub.getRecords(tableName,DashboardAdminClient.timestampFrom("01/09/1985"),DashboardAdminClient.timestampFrom("01/09/2100"),0,10,null);
        TableDTO dto = DashboardAdminClient.toTableDTO(table);
        System.out.println("+++ Received by FE");
        responseText = gson.toJson(dto);
    } else if(action.equals("getTables")) {
        String[] tables = stub.getTableNames();
        ColumnDTO[] dtos = new ColumnDTO[tables.length];
        for(int i=0;i<tables.length;i++) {
            dtos[i] = new ColumnDTO(tables[i],"batch");
        }
        responseText = gson.toJson(dtos);
    } else if(action.equals("getSchema")) {
        String tableName = request.getParameter("table");
        Column[] columns = stub.getTableSchema(tableName);
        if(columns != null && columns.length > 0) {
            ColumnDTO[] dtos = new ColumnDTO[columns.length];
            for(int i=0;i< columns.length;i++) {
              ColumnDTO dto = new ColumnDTO(columns[i].getName(),columns[i].getType());
              dtos[i] = dto;  
            }
            responseText = gson.toJson(dtos);
        }
    }
%>
<%=responseText%>

