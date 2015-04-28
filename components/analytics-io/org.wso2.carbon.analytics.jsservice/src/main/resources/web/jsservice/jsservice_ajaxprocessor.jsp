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

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.jsservice.AnalyticsWebServiceConnector" %>
<%@ page import="org.wso2.carbon.analytics.jsservice.UnauthenticatedUserException" %>
<%@ page import="org.wso2.carbon.analytics.jsservice.Utils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.io.BufferedReader" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    AnalyticsWebServiceConnector connector = null;
    String authParam = request.getHeader("Authorization");
    if (authParam != null) {
        try {
            String[] credentials = Utils.authenticate(authParam);
            connector = new AnalyticsWebServiceConnector(configContext, serverURL, credentials[0], credentials[1]);
        } catch (UnauthenticatedUserException e) {
            out.print("{ \"status\": \"Unauthenticated\", \"message\": \"Error while authenticating: " +
                      e.getMessage() + "\" }");
            return;
        }
    } else {
        if (cookie != null && !cookie.isEmpty()) {
            connector = new AnalyticsWebServiceConnector(configContext, serverURL, cookie);
        } else {
            out.print("{ \"status\": \"Unauthenticated\", \"message\": \"Cookie is not set: }");
            return;
        }
    }
    int type = 0;
    String typeParam = request.getParameter("type");
    if (typeParam != null && !typeParam.isEmpty()) {
        type = Integer.parseInt(typeParam);
    }

    String tableName = request.getParameter("tableName");
    if (type != AnalyticsWebServiceConnector.TYPE_LIST_TABLES &&
        type != AnalyticsWebServiceConnector.TYPE_PUT_RECORDS &&
            type != AnalyticsWebServiceConnector.TYPE_PAGINATION_SUPPORTED) {
        if (tableName == null || tableName.isEmpty()) {
            out.print("{ \"status\": \"Failed\", \"message\": \"Table name param is empty\" }");
            return;
        }
    }

    if (connector != null) {
        switch (type) {
            case AnalyticsWebServiceConnector.TYPE_LIST_TABLES: {
                out.print(connector.getTableList());
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_CREATE_TABLE: {
                out.print(connector.createTable(tableName));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_DELETE_TABLE: {
                out.print(connector.deleteTable(tableName));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_TABLE_EXISTS: {
                out.print(connector.tableExists(tableName));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_GET_BY_RANGE: {
                String from = request.getParameter("timeFrom");
                String to = request.getParameter("timeTo");
                String start = request.getParameter("start");
                String count = request.getParameter("count");
                out.print(connector.getRecordsByRange(tableName, from, to, start, count));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_GET_BY_ID: {
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String recordIdsAsString = buffer.toString();
                out.print(connector.getRecordsByIds(tableName, recordIdsAsString));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_GET_RECORD_COUNT: {
                out.print(connector.getRecordCount(tableName));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_DELETE_BY_RANGE: {
                long timeFrom = Long.parseLong(request.getParameter("timeFrom"));
                long timeTo = Long.parseLong(request.getParameter("timeTo"));
                out.print(connector.deleteRecordsByRange(tableName, timeFrom, timeTo));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_DELETE_BY_ID: {
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String recordIdsAsString = buffer.toString();
                out.print(connector.deleteRecordsByIds(tableName, recordIdsAsString));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_CLEAR_INDICES: {
                out.print(connector.clearIndexData(tableName));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_PUT_RECORDS: {
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String recordsAsString = buffer.toString();
                out.print(connector.insertRecords(recordsAsString));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_SEARCH_COUNT: {
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String queryAsString = buffer.toString();
                out.print(connector.searchCount(tableName, queryAsString));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_SEARCH: {
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String queryAsString = buffer.toString();
                out.print(connector.search(tableName, queryAsString));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_SET_SCHEMA: {
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String schemaAsString = buffer.toString();
                out.print(connector.setTableSchema(tableName, schemaAsString));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_GET_SCHEMA: {
                out.print(connector.getTableSchema(tableName));
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_PAGINATION_SUPPORTED: {
                out.print(connector.isPaginationSupported());
                break;
            }
            case AnalyticsWebServiceConnector.TYPE_WAIT_FOR_INDEXING: {
                long waitTime = Long.parseLong(request.getParameter("waitTime"));
                out.print(connector.waitForIndexing(waitTime));
                break;
            }
            default:
                out.print("{ \"status\": \"Failed\", \"message\": \"Unidentified operation\" }");
                return;
        }
    } else {
        out.print("{ \"status\": \"Failed\", \"message\": \"AnalyticsWebServiceConnector is unavailable\" }");
        return;
    }
%>