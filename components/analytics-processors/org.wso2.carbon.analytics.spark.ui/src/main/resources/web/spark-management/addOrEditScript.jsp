<!--
~ Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.spark.ui.client.AnalyticsExecutionClient" %>
<%@ page import="org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub" %>
<%@ page
        import="org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException" %>
<%@page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.analytics.spark.ui.i18n.Resources">
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <link rel="stylesheet" type="text/css" href="css/spark-explorer-styles.css">

    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        String scriptName = Encode.forHtmlContent(request.getParameter("scriptName"));
        String editableParam = Encode.forHtmlContent(request.getParameter("editable"));
        boolean editable = true;
        if (editableParam != null && !editableParam.equals("null")) { // checks string "null" since owasp encode null values to "null"
            editable = Boolean.parseBoolean(editableParam);
        }

        // checks string "null" since owasp encode null values to "null"
        boolean isExistingScript = scriptName != null && !scriptName.trim().isEmpty() && !scriptName.equals("null");
        String scriptContent = "";
        String cronExp = "";
        AnalyticsExecutionClient client = new AnalyticsExecutionClient(cookie, serverURL, configContext);
        if (isExistingScript) {
            try {
                AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto analyticsScript = client.getScriptContent(scriptName);
                scriptContent = analyticsScript.getScriptContent();
                cronExp = analyticsScript.getCronExpression();
                if (cronExp == null) cronExp = "";
            } catch (AnalyticsProcessorAdminServiceAnalyticsProcessorAdminExceptionException e) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog(<%=e.getFaultMessage().getAnalyticsProcessorAdminException().getMessage()%>);
    </script>

    <% }
    }
    %>

    <script type="text/javascript">
        var editable = <%=editable%>;
        YAHOO.util.Event.onDOMReady(function () {
            editAreaLoader.init({
                id: "allcommands"
                , syntax: "sql"
                , start_highlight: true,
                is_editable: editable
            });
        });
        //        editAreaLoader.set_editable(false);
        //        alert("edi")

        function executeQuery() {
            var scriptContent = editAreaLoader.getValue("allcommands");
            var scriptName = '<%=scriptName%>';
            if (scriptContent == null || scriptContent == '') {
                CARBON.showErrorDialog("No queries entered in the window! Please enter some queries and try again!");
            } else {
                document.getElementById('middle').style.cursor = 'wait';
                openProgressBar();
                new Ajax.Request('executeScript_ajaxprocessor.jsp', {
                    method: 'post',
                    parameters: {scriptName: scriptName, scriptContent: scriptContent},
                    onSuccess: function (transport) {
                        closeProgrsssBar();
                        document.getElementById('middle').style.cursor = '';
                        var allPage = transport.responseText;
                        var divText = '<div id="returnedResults">';
                        var closeDivText = '</div>';
                        var temp = allPage.indexOf(divText, 0);
                        var startIndex = temp + divText.length;
                        var endIndex = allPage.indexOf(closeDivText, temp);
                        var queryResults = allPage.substring(startIndex, endIndex);
                        document.getElementById('analyticsResult').innerHTML = queryResults;
                    },
                    onFailure: function (transport) {
                        closeProgrsssBar();
                        document.getElementById('middle').style.cursor = '';
                        CARBON.showErrorDialog(transport.responseText);
                    }
                });
            }
        }

        function executeQueryInBackground() {
            var scriptContent = editAreaLoader.getValue("allcommands");
            var scriptName = '<%=scriptName%>';
            if (scriptContent == null || scriptContent == '') {
                CARBON.showErrorDialog("No queries entered in the window! Please enter some queries and try again!");
            } else {
                document.getElementById('middle').style.cursor = 'wait';
                openProgressBar();
                new Ajax.Request('executeScriptInBackground_ajaxprocessor.jsp', {
                    method: 'post',
                    parameters: {scriptName: scriptName, scriptContent: scriptContent},
                    onSuccess: function (transport) {
                        closeProgrsssBar();
                        document.getElementById('middle').style.cursor = '';
                        var allPage = transport.responseText;
                        var divText = '<div id="returnedResults">';
                        var closeDivText = '</div>';
                        var temp = allPage.indexOf(divText, 0);
                        var startIndex = temp + divText.length;
                        var endIndex = allPage.indexOf(closeDivText, temp);
                        var queryResults = allPage.substring(startIndex, endIndex);
                        document.getElementById('analyticsResult').innerHTML = queryResults;
                    },
                    onFailure: function (transport) {
                        closeProgrsssBar();
                        document.getElementById('middle').style.cursor = '';
                        CARBON.showErrorDialog(transport.responseText);
                    }
                });
            }
        }

        function openProgressBar() {
            var content = '<div id="overlay"><div id="box"><div class="ui-dialog-title-bar">' +
                    'Executing Spark Queries<a href="#" title="Close" class="ui-dialog-titlebar-close" onclick="closeProgrsssBar();">' +
                    '<span style="display: none">x</span></a>' +
                    '</div><div class="dialog-content"><img src="../resources/images/ajax-loader.gif" />' +
                    ' Executing Spark Queries...</div></div></div>';
            document.getElementById('dynamic').innerHTML = content;
        }

        function closeProgrsssBar() {
            document.getElementById('dynamic').innerHTML = '';
        }


        function saveScript() {
            var scriptName = '';
            var operation = '';
            <%
            if (!isExistingScript){
            %>
            scriptName = document.getElementById('scriptName').value;
            operation = 'add';
            <%
            }else{
            %>
            scriptName = '<%=scriptName%>';
            operation = 'update';
            <%
            }
            %>
            var scriptContent = editAreaLoader.getValue("allcommands");
            var cronExp = document.getElementById('cronExpr').value;

            if (scriptName == null || scriptName == "") {
                CARBON.showErrorDialog("No script name provided. Please enter a name fo the script and then save");
            } else if (scriptContent == null || scriptContent == "") {
                CARBON.showErrorDialog("No spark script provided to execute! Please enter some spark SQL queries and try again!");
            } else {
                new Ajax.Request('../spark-management/SaveSparkScriptProcessor', {
                    method: 'post',
                    parameters: {
                        queries: scriptContent,
                        name: scriptName,
                        operation: operation,
                        cronExp: cronExp
                    },
                    onSuccess: function (transport) {
                        var result = transport.responseText;
                        if (result.indexOf('Success') == -1) {
                            if (result == '') result = "Error while adding the script : " + scriptName;
                            CARBON.showErrorDialog(result);
                        } else {
                            CARBON.showInfoDialog(result, function () {
                                location.href = "listScripts.jsp";
                            }, function () {
                                location.href = "listScripts.jsp";
                            });
                        }
                    },
                    onFailure: function (transport) {
                        CARBON.showErrorDialog(transport.responseText);
                    }
                });
            }
        }

        function cancelScript() {
            history.go(-1);
        }

    </script>


    <div id="middle">
        <div id="dynamic"></div>
                <%
            if (isExistingScript) {
        %>
        <h2><fmt:message key="spark.script.edit"/> <%=" - " + scriptName%>
            <%
            } else {
            %>
            <h2><fmt:message key="spark.script.add"/></h2>
            <%
                }
            %>
        </h2>

        <div id="workArea">
            <% if (!isExistingScript) { %>
            <div class="sectionSeperator togglebleTitle">New Analytics Script Information</div>
            <% } else { %>
            <div class="sectionSeperator togglebleTitle">Analytics Script Information</div>
            <% } %>
            <div class="sectionSub">
                <table class="carbonFormTable">
                    <%
                        if (!isExistingScript) {
                    %>
                    <tr>
                        <td class="leftCol-med labelField">
                            <fmt:message key="spark.script.name"/> <span
                                class="required">*</span>
                        </td>
                        <td>
                            <input type="text" id="scriptName" name="scriptName" size="60"
                                   class="required" autofocus/>

                            <div class="sectionHelp">
                                The name of the actual analytics script.
                            </div>

                        </td>
                    </tr>
                    <%
                        }
                    %>
                    <tr>
                        <td class="leftCol-med labelField">
                            <fmt:message key="spark.script.queries"/> <span
                                class="required">*</span>
                        </td>
                        <td>
                                <%--<%--%>
                                <%--if (!editable) {--%>
                                <%--%>--%>
                                <%--<script type="application/javascript">--%>
                                <%--editAreaLoader._setDisabled()--%>
                                <%--</script>--%>
                                <%--<%--%>
                                <%--}--%>
                                <%--%>--%>
                            <textarea id="allcommands" name="allcommands" rows="20"
                                      style="width:99%" class="required"><%=scriptContent%>
                            </textarea>
                        </td>
                    </tr>
                    <tr>
                    </tr>
                </table>
                <div class="sectionSeperator togglebleTitle">Schedule Script</div>

                <table class="carbonFormTable">
                    <tr>
                        <td colspan="2">
                        </td>
                    </tr>
                    <tr>
                        <td class="leftCol-med labelField">Cron Expression</td>
                        <td>

                            <input name="cronExpr" type="text" id="cronExpr" placeholder="0 0/5 * * * ?"
                                   value="<%=cronExp.trim()%>" <% if (!editable) {%> disabled <% }%>/>

                            <div class="sectionHelp">
                                The cron expression for the rate of analytics script to be executed
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
            <div class="buttonRow">
                <% if (editable) {%>
                <input class="button" type="button" onclick="saveScript()"
                       value="<%if (!isExistingScript){ %><fmt:message key="spark.script.save"/><% }else{ %> <fmt:message key="spark.script.update"/><%}%>"/>
                <%
                    }
                    if (client.isAnalyticsExecutionEnabled()) {
                %>
                <input class="button" type="button" onclick="executeQuery()"
                       value="<fmt:message key="spark.script.execute"/>"/>
                <input class="button" type="button" onclick="executeQueryInBackground()"
                       value="<fmt:message key="spark.script.execute.in.background"/>"/>
                <%
                    }
                %>
                <input type="button"
                       value="<fmt:message key="spark.script.cancel"/>"
                       onclick="cancelScript()"
                       class="button"/>
            </div>

            <br/><br/>

            <div><b><i><fmt:message key="spark.script.exec.results"/></i></b></div>
            <br/>

            <div id="analyticsResult" class="scrollable" style="width:99%">
                    <%--the results goes here...--%>
            </div>
        </div>
</fmt:bundle>