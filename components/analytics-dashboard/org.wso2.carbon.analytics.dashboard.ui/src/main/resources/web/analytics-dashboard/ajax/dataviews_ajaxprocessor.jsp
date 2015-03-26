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
<%@ page import="org.wso2.carbon.analytics.dashboard.stub.data.DataView" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.DashboardAdminClient" %>
<%@ page import="org.wso2.carbon.analytics.dashboard.ui.dto.DataViewDTO" %>

<%
    String responseText = "";
    String action = request.getParameter("action");
    DashboardAdminServiceStub stub =   DashboardAdminClient.getDashboardAdminService(config, session, request);
    response.setContentType("application/json");
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
            responseText = gson.toJson(dataviewsResponse);
        } else {
            responseText = "[]";
        }
    } else if(action.equals("getDataViewById")) {
        String dataViewId = request.getParameter("dataViewId");
        DataView dataView = stub.getDataView(dataViewId);
        System.out.println("+++ " + dataView.getDisplayName());
        if (dataView != null) {
            responseText = gson.toJson(DashboardAdminClient.toDataViewDTO(dataView));
        } else {
            responseText = gson.toJson("{}");
        }
        
    }  else if(action.equals("addDataView")) {
        
    }
%>
<%=responseText%>
