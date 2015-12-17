<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ page import="com.google.gson.Gson" %>
<%@ page
        import="org.wso2.carbon.analytics.stream.persistence.stub.EventStreamPersistenceAdminServiceStub" %>
<%@ page import="org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTable" %>
<%@ page import="org.wso2.carbon.analytics.stream.persistence.stub.dto.AnalyticsTableRecord" %>
<%@ page import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils" %>

<%
    EventStreamPersistenceAdminServiceStub
            streamPersistenceAdminServiceStub = EventStreamUIUtils.getEventStreamPersistenceAdminService(config, session, request);

    String responseText = "";
    if (EventStreamUIUtils.isEventStreamPersistenceAdminServiceAvailable(streamPersistenceAdminServiceStub)) {
        try {
            AnalyticsTable schema =
                    streamPersistenceAdminServiceStub.getAnalyticsTable(request.getParameter("eventStreamName"), request.getParameter("eventStreamVersion"));
            if (schema != null) {
                AnalyticsTableRecord[] analyticsTableColumns = schema.getAnalyticsTableRecords();
                org.wso2.carbon.event.stream.ui.beans.AnalyticsTable table = new
                        org.wso2.carbon.event.stream.ui.beans.AnalyticsTable();
                if (analyticsTableColumns != null) {
                    org.wso2.carbon.event.stream.ui.beans.AnalyticsTableRecord[] tableColumns = new
                            org.wso2.carbon.event.stream.ui.beans.AnalyticsTableRecord[analyticsTableColumns.length];
                    int i = 0;
                    for (AnalyticsTableRecord analyticsTableColumn : analyticsTableColumns) {
                        org.wso2.carbon.event.stream.ui.beans.AnalyticsTableRecord column = new
                                org.wso2.carbon.event.stream.ui.beans.AnalyticsTableRecord();
                        column.setColumnName(analyticsTableColumn.getColumnName());
                        column.setColumnType(analyticsTableColumn.getColumnType());
                        column.setPrimaryKey(analyticsTableColumn.getPrimaryKey());
                        column.setIndexed(analyticsTableColumn.getIndexed());
                        column.setScoreParam(analyticsTableColumn.getScoreParam());
                        tableColumns[i++] = column;
                    }
                    table.setAnalyticsTableRecords(tableColumns);
                }
                table.setPersist(schema.getPersist());
                table.setRecordStoreName(schema.getRecordStoreName());
                table.setMergeSchema(schema.getMergeSchema());
                responseText = new Gson().toJson(table);
            }
        } catch (Exception e) {
            responseText = e.getMessage();
        }
    }
%>
<%=responseText%>
