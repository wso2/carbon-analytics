<%@ page contentType="text/html; charset=iso-8859-1" language="java" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub" %>
<%@ page import="org.wso2.carbon.analytics.spark.ui.client.AnalyticsExecutionClient" %>
<%@ page import="org.owasp.encoder.Encode" %>

<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

    String scriptName = Encode.forHtmlContent(request.getParameter("scriptName"));
    String scriptContent = Encode.forHtmlContent(request.getParameter("scriptContent"));
    AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto[] results = null;
    try {
        AnalyticsExecutionClient client = new AnalyticsExecutionClient(cookie, serverURL, configContext);
        if (null != scriptName && !scriptName.trim().isEmpty() && !scriptName.equals("null")) {
            if (client.isAnalyticsTaskExecuting(scriptName)) {
        %>
            <div id="returnedResults">
                <span class="errorView"> <b>WARNING: </b> Scheduled task for the script : <%=scriptName%> is already running.
                    Please try again after the scheduled task is completed.</span>
            </div>
        <%
            } else if (client.isAnalyticsScriptExecuting(scriptName)) {
        %>
            <div id="returnedResults">
                <span class="errorView"> <b>WARNING: </b> Script : <%=scriptName%> is already running in background.
                    Please try again after the background execution is completed.</span>
            </div>
        <%
            } else {
                results = client.executeScript(scriptName);
            }
        } else if (null != scriptContent && !scriptContent.trim().isEmpty() && !scriptContent.equals("null")) { // checks string "null" since owasp encode null values to "null"
            results = client.executeScriptContent(scriptContent);
        }
%>
<div id="returnedResults">
    <% if (null != results) {
        for (AnalyticsProcessorAdminServiceStub.AnalyticsQueryResultDto result : results) {
    %>
    <br/>
    &nbsp;&nbsp;Query: <span class="queryView"><%=result.getQuery()%></span>
    <%
        AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto[] rows = result.getRowsResults();
        if (null != rows && rows.length > 0 && !(rows.length == 1 && rows[0] == null)) {
            String[] columnNames = result.getColumnNames();
    %>
    <br/>
    <br/>
    <b>Results:</b>
    <br/><br/>
        <table class="result">
            <tbody>

            <tr>
                <% for (String aColumnName : columnNames) {
                %>

                <th class="resultCol"><b><%=aColumnName%>
                </b>
                </th>

                <% }

                %>
            </tr>
            <%
                for (AnalyticsProcessorAdminServiceStub.AnalyticsRowResultDto aRow : rows) {

            %>
            <tr>
                <%
                    String[] colValues = aRow.getColumnValues();
                    for (String aValue : colValues) {
                %>
                <td><%=aValue%>
                </td>

                <% }
                %>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
        <br/>
        <span class="queryInfo"><%=rows.length%> rows returned.</span><br/>
            <% } else {
        %>
        <br/>
        <br/>
        <span class="queryInfo">Query Executed</span>
        <br/>
            <%
                }  %>
        <hr color="#888888"/>
            <%
            }
            }
        %>
</div>
<%--<div id="returnedResults">--%>
<%--<span class="errorView"> <b>WARNING: </b> Scheduled task for the script : <%=scriptName%> is already running.--%>
<%--Please try again after the scheduled task is completed.</span>--%>
<%--</div>--%>
<% //}
} catch (Exception e) {
    e.printStackTrace();
%>
<div id="returnedResults">
    <span class="errorView"> <b>ERROR: </b><%=e.getMessage()%> </span>
</div>

<% }
%>
