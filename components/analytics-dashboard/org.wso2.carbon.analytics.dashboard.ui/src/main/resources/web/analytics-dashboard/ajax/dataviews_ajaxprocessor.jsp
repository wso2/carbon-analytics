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
<%@ page import="com.google.gson.JsonSyntaxException" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.stub.DashboardAdminServiceStub" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.stub.data.DataView" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.stub.data.Widget" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.DashboardAdminClient" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.WidgetDTO" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.DataViewDTO" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.WidgetAndDataViewDTO" %>

<%
    String responseText = "";

    String action = request.getParameter("action");
    DashboardAdminServiceStub stub =   DashboardAdminClient.getDashboardAdminService(config, session, request);
    Gson gson = new Gson();

    if(action == null) {
        DataView[] dataviews = stub.getDataViewsInfo();
        DataViewDTO[] dataviewsResponse = null;
        int i=0;
        if(dataviews != null && dataviews.length > 0) {
            dataviewsResponse = new DataViewDTO[dataviews.length];
            for(DataView dataview : dataviews) {
                dataviewsResponse[i++] = DashboardAdminClient.toDataViewDTO(dataview);
            }
            response.setContentType("application/json");
            responseText = gson.toJson(dataviewsResponse);
        } else {
            responseText = "[]";
        }
    } else if(action.equals("getDataViewById")) {
        String dataViewId = request.getParameter("dataViewId");
        DataView dataView = stub.getDataView(dataViewId);
        //System.out.println("+++ " + dataView.getDisplayName());
        response.setContentType("application/json");
        if (dataView != null) {
            responseText = gson.toJson(DashboardAdminClient.toDataViewDTO(dataView));
        } else {
            responseText = gson.toJson("{}");
        }
        
    }  else if(action.equals("addWidget")) {
        String dataview = request.getParameter("dataview");
        String widgetDefinition = request.getParameter("widgetDefinition");

        WidgetDTO dto = null;
        try {
            dto = gson.fromJson(widgetDefinition,WidgetDTO.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        if(dto != null) {
            Widget widget = new Widget();
            widget.setId(dto.getId());
            widget.setTitle(dto.getTitle());
            widget.setConfig(dto.getConfig());
            stub.addWidget(dataview,widget);
            responseText = "OK";
        }
    }   else if(action.equals("getWidget")) {
        String dataview = request.getParameter("dataview");
        String widgetId = request.getParameter("widgetId");

        DataView dataView = stub.getWidgetWithDataViewInfo(dataview,widgetId);
        WidgetAndDataViewDTO dto = DashboardAdminClient.toWidgetAndDVDTO(dataView);
        if(dto != null) {
            response.setContentType("application/json");
           responseText = gson.toJson(dto); 
        }

    } else if(action.equals("addDataView")) {
        String definition = request.getParameter("definition");
        DataViewDTO dto = null;
        try {
            dto = gson.fromJson(definition,DataViewDTO.class);
            if(dto != null) {
                DataView dv = DashboardAdminClient.toDataView(dto);
                stub.addDataView(dv);
                responseText = "OK"; 
            } 
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            responseText = e.getMessage();
            response.setStatus(500);
        }
    }  
%>
<%=responseText%>
