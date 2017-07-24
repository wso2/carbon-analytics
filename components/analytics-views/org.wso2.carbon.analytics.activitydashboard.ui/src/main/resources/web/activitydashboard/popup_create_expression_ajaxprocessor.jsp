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

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.ui.ActivityDashboardClient" %>
<%@ page
        import="org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceActivityDashboardExceptionException" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources">

    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ActivityDashboardClient client = new ActivityDashboardClient(cookie, serverURL, configContext);
        String[] listOfTables = new String[0];
        try {
            listOfTables = client.getAllTables();
        } catch (ActivityDashboardAdminServiceActivityDashboardExceptionException e) {
            response.getWriter().write(e.getFaultMessage().getActivityDashboardException().getErrorMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    %>
    <script type="text/javascript">

        var nodeId = '<%=request.getParameter("nodeId")%>';

        function toggelExpressionSelection(type) {
            if (type == 'query') {
                document.getElementById('tableName').disabled = false;
                document.getElementById('searchQuery').disabled = false;
                document.getElementById('operationType').disabled = true;
            } else {
                document.getElementById('tableName').disabled = true;
                document.getElementById('searchQuery').disabled = true;
                document.getElementById('operationType').disabled = false;
            }
        }

        function addExpression() {
            var expressionType = getExpressionType();
            var isQueryType = false;
            if (expressionType == 'query') {
                isQueryType = true;
                var tableName = document.getElementById('tableName').value;
                var searchQuery = document.getElementById('searchQuery').value;
                if (!tableName) {
                    CARBON.showErrorDialog("Table Name field is not filled, please fill that and save the expression");
                    return;
                }
                if (!searchQuery) {
                    CARBON.showErrorDialog("Search query is not provided! Please enter the intended query to be executed!");
                    return;
                }
                var paramsvalues = {
                    nodeId: nodeId,
                    isQueryType: isQueryType,
                    tableName: tableName,
                    searchQuery: searchQuery
                };
                jQuery.ajax({
                    async: false,
                    type: 'POST',
                    url: '../activitydashboard/add_expression_ajaxprocessor.jsp',
                    data: paramsvalues,
                    success: function (data) {
                        customCarbonWindowClose();
                    },
                    error: function (data) {
                        CARBON.showErrorDialog("Unable to proceed with the request. " + data);
                    }
                });
            } else {
                isQueryType = false;
                var operationType = document.getElementById('operationType').value;
                var values = {
                    nodeId: nodeId,
                    isQueryType: isQueryType,
                    operationType: operationType
                };
                jQuery.ajax({
                    async: false,
                    type: 'POST',
                    url: '../activitydashboard/add_expression_ajaxprocessor.jsp',
                    data: values,
                    success: function (data) {
                        customCarbonWindowClose();
                    },
                    error: function (data) {
                        CARBON.showErrorDialog("Unable to proceed with the request. " + data);
                    }
                });
            }
        }

        function customCarbonWindowClose() {
            jQuery("#dialog").dialog("destroy").remove();
            jQuery("#dcontainer").empty();
            return false;
        }

        function getExpressionType() {
            if (document.getElementById('queryExpression').checked) {
                return document.getElementById('queryExpression').value;
            } else {
                return document.getElementById('operationExpression').value;
            }
        }
    </script>
    <div id="middle">
        <div id="workArea">

            <form name="inputForm" method="post" id="searchExpForm">
                <table style="width:100%" id="addNewExpression" class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="leftCol-med">
                            <div class="sectionInputs">
                                <ul class="items">
                                    <li>
                                        <input type="radio" checked="true" value="query"
                                               name="expressionType" id="queryExpression"
                                               onclick="toggelExpressionSelection('query')">
                                        <label for="queryExpression">Search Query</label>
                                        <table id="queryExpressionTable"
                                               class="normal-nopadding smallTextInput"
                                               style="width:100%">
                                            <tbody>
                                            <tr>
                                                <td class="leftcol-med">
                                                    Table Name <span
                                                        class="required">*</span>
                                                </td>
                                                <td>
                                                    <% if (listOfTables != null && listOfTables.length != 0) { %>
                                                    <select id="tableName" name="tableName"
                                                            class="required">
                                                        <%
                                                            for (String aTableName : listOfTables) {
                                                        %>
                                                        <option value="<%=aTableName%>"><%=aTableName%>
                                                        </option>
                                                        <%
                                                            }
                                                        %>
                                                    </select>

                                                    <div class="sectionHelp">
                                                        The name of the table against the intended query is going to be
                                                        executed.
                                                    </div>
                                                    <%
                                                    } else {
                                                    %>
                                                    <script type="text/javascript">
                                                        CARBON.showErrorDialog("No Tables exists in the analytics data store.",customCarbonWindowClose,
                                                                customCarbonWindowClose );
                                                    </script>
                                                    <%
                                                        }
                                                    %>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="leftcol-med">
                                                    Search Query <span
                                                        class="required">*</span>
                                                </td>
                                                <td><input type="text" id="searchQuery" name="searchQuery"
                                                           class="required" size="60"/>

                                                    <div class="sectionHelp">
                                                        The actual set query that needs to be executed.
                                                    </div>
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </li>
                                    <li>
                                        <input type="radio" value="operation" name="expressionType"
                                               id="operationExpression"
                                               onclick="toggelExpressionSelection('operation')">
                                        <label for="operationExpression">Operation</label>
                                        <table id="operationExpressionTable"
                                               class="normal-nopadding smallTextInput"
                                               style="width:100%">
                                            <tbody>
                                            <tr>
                                                <td class="leftcol-med">
                                                    Type <span
                                                        class="required">*</span>
                                                </td>
                                                <td>
                                                    <select id="operationType" name="operationType"
                                                            class="required" disabled="">
                                                        <option value="and">AND</option>
                                                        <option value="or">OR</option>
                                                    </select>

                                                    <div class="sectionHelp">
                                                        The type of the operation to be performed between the
                                                        expressions.
                                                    </div>
                                                </td>
                                            </tr>
                                            </tbody>
                                        </table>
                                    </li>
                                </ul>
                            </div>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>