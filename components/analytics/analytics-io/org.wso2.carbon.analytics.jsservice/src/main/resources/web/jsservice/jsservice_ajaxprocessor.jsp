<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.jsservice.AnalyticsWebServiceConnector" %>
<%@ page import="org.wso2.carbon.analytics.jsservice.Utils" %>
<%@ page import="org.wso2.carbon.analytics.jsservice.UnauthenticatedUserException" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    AnalyticsWebServiceConnector connector;
    String authParam = request.getHeader("Authorization");

    if (authParam != null) {
        try {
            String[] credentials = Utils.authenticate(authParam);
            connector = new AnalyticsWebServiceConnector(configContext, serverURL, credentials[0], credentials[1]);
        } catch (UnauthenticatedUserException e) {
            out.print("{ \"Result\": \"ERROR\", \"Message\": \"" + e.getMessage() + "\" }");
        }
    } else {
        connector = new AnalyticsWebServiceConnector(configContext, serverURL, cookie);
    }

    String typeParam = request.getParameter("type");
    String type = "";
    if (typeParam != null && !typeParam.isEmpty()) {
        type = typeParam;
    }

    String tableName = request.getParameter("tableName");
    if (!type.equals(AnalyticsWebServiceConnector.TYPE_LIST_TABLES)) {
        if (tableName == null || tableName.isEmpty()) {
            out.print("{ \"Result\": \"ERROR\", \"Message\": \"Table name param is empty\" }");
        }
    }

    switch (type) {
        case AnalyticsWebServiceConnector.TYPE_LIST_TABLES: {
            String jtStartIndex = request.getParameter("jtStartIndex");
            if (jtStartIndex == null || jtStartIndex.isEmpty()) {
                jtStartIndex = "0";
            }
            int startIndex = Integer.parseInt(jtStartIndex);
            String jtPageSize = request.getParameter("jtPageSize");
            if (jtPageSize == null || jtPageSize.isEmpty()) {
                jtPageSize = "500";
            }
            int pageSize = Integer.parseInt(jtPageSize);
            String query = request.getParameter("query");
            long from = 0;
            String timeFrom = request.getParameter("timeFrom");
            if (timeFrom != null && !timeFrom.isEmpty()) {
                timeFrom = timeFrom.concat("000");
                from = Long.parseLong(timeFrom);
            }
            long to = Long.MAX_VALUE;
            String timeTo = request.getParameter("timeTo");
            if (timeTo != null && !timeTo.isEmpty()) {
                timeTo = timeTo.concat("000");
                to = Long.parseLong(timeTo);
            }
            out.print(connector.getRecords(tableName, from, to, startIndex, pageSize, query));
            break;
        }
        case MessageConsoleConnector.TYPE_UPDATE_RECORD: {
            Map<String, String[]> parameters = request.getParameterMap();
            Properties properties = new Properties(parameters).invoke(UPDATE_RECORD_ACTION);
            String[] columns = properties.getColumns();
            String[] values = properties.getValues();

            String recordID = request.getParameter(MessageConsoleConnector.RECORD_ID);
            out.print(connector.updateRecord(tableName, columns, values, recordID));
            break;
        }
        case MessageConsoleConnector.TYPE_CREATE_RECORD: {
            Map<String, String[]> parameters = request.getParameterMap();
            Properties properties = new Properties(parameters).invoke(CREATE_RECORD_ACTION);
            String[] columns = properties.getColumns();
            String[] values = properties.getValues();

            out.print(connector.addRecord(tableName, columns, values));
            break;
        }
        case MessageConsoleConnector.TYPE_DELETE_RECORD: {
            String recordsIdString = request.getParameter(MessageConsoleConnector.RECORD_ID);
            String[] recordsIds = new String[]{recordsIdString};
            out.print(connector.deleteRecords(tableName, recordsIds));
            break;
        }
        case MessageConsoleConnector.TYPE_TABLE_INFO: {
            out.print(connector.getTableInfo(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_LIST_ARBITRARY_RECORD: {
            String recordId = request.getParameter("bam_unique_rec_id");
            out.print(connector.getArbitraryFields(tableName, recordId));
            break;
        }
        case MessageConsoleConnector.TYPE_CRATE_ARBITRARY_RECORD: {
            String recordId = request.getParameter("bam_unique_rec_id");
            String fieldName = request.getParameter("Name");
            String fieldValue = request.getParameter("Value");
            String fieldType = request.getParameter("Type");
            out.print(connector.putArbitraryField(tableName, recordId, fieldName, fieldValue, fieldType));
            break;
        }
        case MessageConsoleConnector.TYPE_UPDATE_ARBITRARY_RECORD: {
            String recordId = request.getParameter("bam_unique_rec_id");
            String fieldName = request.getParameter("Name");
            String fieldValue = request.getParameter("Value");
            String fieldType = request.getParameter("Type");
            out.print(connector.putArbitraryField(tableName, recordId, fieldName, fieldValue, fieldType));
            break;
        }
        case MessageConsoleConnector.TYPE_DELETE_ARBITRARY_RECORD: {
            String recordId = request.getParameter("bam_unique_rec_id");
            String fieldName = request.getParameter("Name");
            out.print(connector.deleteArbitraryField(tableName, recordId, fieldName));
            break;
        }
        case MessageConsoleConnector.TYPE_CREATE_TABLE: {
            String tableInfo = request.getParameter("tableInfo");
            String action = request.getParameter("action");
            if ("add".equals(action)) {
                out.print(connector.putTable(tableName, tableInfo, true));
            } else if ("edit".equals(action)) {
                out.print(connector.putTable(tableName, tableInfo, false));
            }
            break;
        }
        case MessageConsoleConnector.TYPE_DELETE_TABLE: {
            out.print(connector.deleteTable(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_GET_TABLE_INFO: {
            out.print(connector.getTableInfoWithIndexInfo(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_GET_PURGING_TASK_INFO: {
            out.print(connector.getDataPurgingDetails(tableName));
            break;
        }
        case MessageConsoleConnector.TYPE_SAVE_PURGING_TASK_INFO: {
            out.print(connector.scheduleDataPurging(tableName, request.getParameter("cron"),
                                                    request.getParameter("retention"),
                                                    Boolean.parseBoolean(request.getParameter("enable"))));
            break;
        }
        case MessageConsoleConnector.TYPE_LIST_TABLE: {
            out.print(connector.getTableList());
            break;
        }
    }
%>