<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.hive.ui.client.HiveScriptStoreClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.util.regex.Pattern" %>
<%@ page import="java.util.regex.Matcher" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<fmt:bundle basename="org.wso2.carbon.analytics.hive.ui.i18n.Resources">
<carbon:breadcrumb label="hive.explorer"
                   resourceBundle="org.wso2.carbon.analytics.hive.ui.i18n.Resources"
                   topPage="false" request="<%=request%>"/>
<script src="../editarea/edit_area_full.js" type="text/javascript"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<link rel="stylesheet" type="text/css" href="css/hive-explorer-styles.css">
<script type="text/javascript">
    YAHOO.util.Event.onDOMReady(function() {
        editAreaLoader.init({
                    id : "allcommands"
                    ,syntax: "sql"
                    ,start_highlight: true
                });
    });
</script>

<%
    String scriptName = "";
    String scriptContent = "";
    String cron = "";
    String mode = request.getParameter("mode");

    String isSchedulingCanceled = "false";
    if("true".equals(request.getParameter("schedulingCanceled"))){
        isSchedulingCanceled  = "true";
    }
    int max = 40;
    boolean scriptNameExists = false;
    if (request.getParameter("scriptName") != null && !request.getParameter("scriptName").equals("")) {
        scriptName = request.getParameter("scriptName");
    }
    if (null != mode && mode.equalsIgnoreCase("edit")) {
        scriptNameExists = true;
    } else {
        scriptNameExists = false;
        mode = "";
    }
    String requestUrl = request.getHeader("Referer");
    boolean isFromScheduling = false;
    if (requestUrl != null && requestUrl.contains("scheduletask.jsp")) {
        isFromScheduling = true;
    }
    if (scriptNameExists && !isFromScheduling) {
        try {
            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            HiveScriptStoreClient client = new HiveScriptStoreClient(cookie, serverURL, configContext);
            scriptContent = client.getScript(scriptName);
            cron = client.getCronExpression(scriptName);
            if (scriptContent != null && !scriptContent.equals("")) {
                scriptContent = scriptContent.replace("'", "\'");
//                Pattern regex = Pattern.compile("[^\"']+|\"([^\"]*)\"|'([^']*)'");
//                Matcher regexMatcher = regex.matcher(scriptContent);
//                String formattedScript = "";
//                while (regexMatcher.find()) {
//                    String temp = "";
//                    if (regexMatcher.group(1) != null) {
//                        // Add double-quoted string without the quotes
//                        temp = regexMatcher.group(1);
//                        temp = "\"" + temp + "\"";
//                    } else if (regexMatcher.group(2) != null) {
//                        // Add single-quoted string without the quotes
//                        temp = regexMatcher.group(2);
//                        temp = "\'" + temp + "\'";
//                    } else {
//                        temp = regexMatcher.group();
//                    }
//                    formattedScript += temp + " ";
//                }

//                String[] queries = formattedScript.split(";");
//                scriptContent = "";
//                for (String aquery : queries) {
//                    aquery = aquery.trim();
//                    if (!aquery.equals("")) {
//                        aquery = aquery.replaceAll("%%\n", ";");
//                        aquery = aquery.replaceAll("%%", ";");
//                        aquery = wrapTextInVisibleWidth(aquery);
//                        String[] temp = aquery.split(",");
//
//                        if (null != temp) {
//                            aquery = "";
//                            int count = 0;
//                            for (String aSubQuery : temp) {
//                                aSubQuery = aSubQuery.trim();
//                                if (!aSubQuery.equals("")) {
//                                    count += aSubQuery.length() + 1;
//                                    if (count > max) {
//                                        aquery += aSubQuery + "," + "\n\t";
//                                        count = 0;
//                                    } else {
//                                        aquery += aSubQuery + ",";
//                                    }
//                                }
//                            }
//                            aquery = aquery.trim();
//                            if (aquery.endsWith(",")) aquery = aquery.substring(0, aquery.length() - 1);
//                            scriptContent = scriptContent + aquery + ";" + "\n";
//                        }
//                    }
//                }
            }
        } catch (Exception e) {
            String errorString = e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
%>
<script type="text/javascript">
    location.href = "../admin/error.jsp";
    alert('<%=errorString%>');
</script>
<%
            return;
        }
    }
    if (isFromScheduling) {
        Object content = session.getAttribute("scriptContent" + scriptName);
        if (null != content) {
            scriptContent = content.toString();
        } else {
            scriptContent = "";
        }
        if (null != request.getParameter("cron")) {
            cron = request.getParameter("cron").toString();
        }
        if (scriptContent != null && !scriptContent.equals("")) {
            scriptContent = scriptContent.replace("'", "\'");

//            Pattern regex = Pattern.compile("[^\"']+|\"([^\"]*)\"|'([^']*)'");
//            Matcher regexMatcher = regex.matcher(scriptContent);
//            String formattedScript = "";
//            while (regexMatcher.find()) {
//                String temp = "";
//                if (regexMatcher.group(1) != null) {
//                    // Add double-quoted string without the quotes
//                    temp = regexMatcher.group(1);
//                    temp = "\"" + temp + "\"";
//                } else if (regexMatcher.group(2) != null) {
//                    // Add single-quoted string without the quotes
//                    temp = regexMatcher.group(2);
//                    temp = "\'" + temp + "\'";
//                } else {
//                    temp = regexMatcher.group();
//                }
//                formattedScript += temp + " ";
//            }
//            scriptContent = formattedScript;
//            System.out.println("After formatting Script content: ");
//            System.out.println(scriptContent);
//            String[] queries = formattedScript.split(";");
//            scriptContent = "";
//            for (String aquery : queries) {
//                aquery = aquery.trim();
//                if (!aquery.equals("")) {
//                    aquery = aquery.replaceAll("%%\n", ";");
//                    aquery = aquery.replaceAll("%%", ";");
//                    aquery = wrapTextInVisibleWidth(aquery);
//                    String[] temp = aquery.split(",");
//
//                    if (null != temp) {
//                        aquery = "";
//                        int count = 0;
//                        for (String aSubQuery : temp) {
//                            aSubQuery = aSubQuery.trim();
//                            if (!aSubQuery.equals("")) {
//                                count += aSubQuery.length() + 1;
//                                if (count > max) {
//                                    aquery += aSubQuery + "," + "\n\t";
//                                    count = 0;
//                                } else {
//                                    aquery += aSubQuery + ",";
//                                }
//                            }
//                        }
//                        aquery = aquery.trim();
//                        if (aquery.endsWith(",")) aquery = aquery.substring(0, aquery.length() - 1);
//                        scriptContent = scriptContent + aquery + ";" + "\n";
//                    }
//                }
//            }
        }
    }


%>
<%!
    private String wrapTextInVisibleWidth(String line) {
        int max = 100;
        if (null != line) {
            line = line.trim();
            if (line.length() <= max) {
                return line;
            } else {
                String newLine = "";
                String[] spaceSplit = line.split(" ");
                int count = 0;
                for (String word : spaceSplit) {
                    if (count + word.length() <= max) {
                        newLine += word + " ";
                        count += word.length() + 1;
                    } else {
                        newLine += "\n\t" + word + " ";
                        count = (word + " ").length();
                    }
                }
                return newLine;
            }
        } else {
            return null;
        }


    }
%>
<script type="text/javascript">
    var cron = '<%=cron%>';
    var scriptName = '<%=scriptName%>';
    var saveWithoutPrompt = '<%=isSchedulingCanceled%>';
    var allQueries = '';
    function executeQuery() {
        document.getElementById('hiveResult').innerHTML = '';
        var allQueries = editAreaLoader.getValue("allcommands");
        allQueries = trim(allQueries);
        if (allQueries != '') {
            document.getElementById('middle').style.cursor = 'wait';
            openProgressBar();
            window.location.hash = "scriptResults";
            new Ajax.Request('../hive-explorer/queryresults.jsp', {
                        method: 'post',
                        parameters: {queries:allQueries,
                            scriptName: scriptName},
                        onSuccess: function(transport) {
                            document.getElementById('middle').style.cursor = '';
                            closeProgrsssBar();
                            var allPage = transport.responseText;
                            var divText = '<div id="returnedResults">';
                            var closeDivText = '</div>';
                            var temp = allPage.indexOf(divText, 0);
                            var startIndex = temp + divText.length;
                            var endIndex = allPage.indexOf(closeDivText, temp);
                            var queryResults = allPage.substring(startIndex, endIndex);
                            document.getElementById('hiveResult').innerHTML = queryResults;
                        },
                        onFailure: function(transport) {
                            closeProgrsssBar();
                            document.getElementById('middle').style.cursor = '';
                            CARBON.showErrorDialog(transport.responseText);
                        }
                    });

        } else {
            document.getElementById('middle').style.cursor = '';
            var message = "Empty query can not be executed";
            CARBON.showErrorDialog(message);
        }
    }

    function trim(text) {
        return text.replace(/^\s+|\s+$/g, "");
    }

    function saveScript() {
        allQueries = editAreaLoader.getValue("allcommands");
        scriptName = document.getElementById('scriptName').value;
        allQueries = trim(allQueries);
        if (allQueries != "") {
            if (scriptName != "") {
                if (cron != "" || saveWithoutPrompt == "true") {
                    checkExistingNameAndSaveScript();
                }
                else {
                    document.getElementById('saveWithCron').value = 'true';
                    CARBON.showConfirmationDialog("Do you want to schedule the script?", function() {
                        scheduleTask();
                    }, function() {
                        checkExistingNameAndSaveScript();
                    }, function() {

                    });
                }

            } else {
                var message = "Please enter script name to save";
                CARBON.showErrorDialog(message);
            }

        } else {
            var message = "Empty query can not be saved";
            CARBON.showErrorDialog(message);
        }
    }

    function cancelScript() {
        location.href = "../hive-explorer/listscripts.jsp";
    }

    function scheduleTask() {
        var allQueries = editAreaLoader.getValue("allcommands");
        document.getElementById('scriptContent').value = allQueries;
        document.getElementById('commandForm').action = "../hive-explorer/scheduletask.jsp?mode=" + '<%=mode%>' + '&cron=' + '<%=cron%>';
        document.getElementById('commandForm').submit();
    }

    function checkExistingNameAndSaveScript() {
        var mode = '<%=mode%>';
        if (mode != 'edit') {
            new Ajax.Request('../hive-explorer/ScriptNameChecker', {
                        method: 'post',
                        parameters: {scriptName:scriptName},
                        onSuccess: function(transport) {
                            var result = transport.responseText;
                            if (result.indexOf('true') != -1) {
                                var message = "The script name: " + scriptName + 'already exists in the database. Please enter a different script name.';
                                CARBON.showErrorDialog(message);
                            } else {
                                sendRequestToSaveScript();
                            }
                        },
                        onFailure: function(transport) {
                            return true;
                        }
                    });
        } else {
            sendRequestToSaveScript();
        }
    }

    function sendRequestToSaveScript() {
        new Ajax.Request('../hive-explorer/SaveScriptProcessor', {
                    method: 'post',
                    parameters: {queries:allQueries, scriptName:scriptName,
                        cronExp:cron},
                    onSuccess: function(transport) {
                        var result = transport.responseText;
                        if (result.indexOf('Success') != -1) {
                            CARBON.showInfoDialog(result, function() {
                                location.href = "../hive-explorer/listscripts.jsp";
                            }, function() {
                                location.href = "../hive-explorer/listscripts.jsp";
                            });

                        } else {
                            CARBON.showErrorDialog(result);
                        }
                    },
                    onFailure: function(transport) {
                        CARBON.showErrorDialog(result);
                    }
                });
    }

    function openProgressBar() {
        var content = '<div id="overlay"><div id="box"><div class="ui-dialog-title-bar">' +
                'Executing Hive Queries<a href="#" title="Close" class="ui-dialog-titlebar-close" onclick="closeProgrsssBar();">' +
                '<span style="display: none">x</span></a>' +
                '</div><div class="dialog-content"><img src="../resources/images/ajax-loader.gif" />' +
                ' Executing Hive Queries...</div></div></div>';
        document.getElementById('dynamic').innerHTML = content;
    }

    function closeProgrsssBar() {
        document.getElementById('dynamic').innerHTML = '';
    }

</script>


<script type="text/javascript">
    $(document).ready(function() {
        document.getElementById('allcommands').focus();
    });
</script>


<div id="middle">
    <div id="dynamic"></div>
    <%
        if (scriptNameExists) {
    %>
    <h2>Script Editor<%=" - " + scriptName%>
        <%
        } else {
        %>
        <h2>Script Editor</h2>
        <%
            }
        %>
    </h2>

    <div id="workArea">

        <form id="commandForm" name="commandForm" action="" method="POST">
            <table class="styledLeft noBorders">
                <thead>
                <tr>
                    <th><span style="float: left; position: relative; margin-top: 2px;">
<fmt:message key="script"/></span>
                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td style="padding: 10px 0 0 20px;">
                        <a class="icon-link" style="background: url('images/tasks-icon.gif') no-repeat;float:right;"
                           href="javascript: scheduleTask();">Schedule
                            Script</a>
                    </td>
                </tr>
                <%
                    if (!scriptNameExists) {
                %>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td class="leftCol-small">
                                    <fmt:message key="script.name"/> <span
                                        class="required">*</span>
                                </td>
                                <td>
                                    <input type="text" id="scriptName" name="scriptName" size="60"
                                           value="<%=scriptName%>"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <%
                } else { %>
                <input type="hidden" value="<%=scriptName%>" name="scriptName" id="scriptName">
                <% }
                %>
                    <%--<tr>--%>
                    <%--<td>--%>
                    <%--<table class="normal-nopadding">--%>
                    <%--<tbody>--%>
                    <%--<tr>--%>
                    <%--<td class="leftCol-small">--%>
                    <%--<fmt:message key="script.type"/>--%>
                    <%--</td>--%>
                    <%--<td>--%>
                    <%--<select style="width:100px">--%>
                    <%--<option value="hive">Hive</option>--%>
                    <%--</select>--%>
                    <%--</td>--%>
                    <%--</tr>--%>
                    <%--</tbody>--%>
                    <%--</table>--%>
                    <%--</td>--%>
                    <%--</tr>--%>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td>
                                    <textarea id="allcommands" name="allcommands" rows="25"
                                              style="width:99%"><%=scriptContent%>
                                    </textarea>
                                </td>
                                    <%--<td>--%>
                                    <%--<input class="button" type="button" onclick="scheduleTask()" value="Schedule"/>--%>
                                    <%--</td>--%>
                            </tr>

                            <tr>
                                <td>
                                    <input class="button" type="button" onclick="executeQuery()"
                                           value="<fmt:message key="execute"/>" />
                                    <input class="button" type="button" onclick="saveScript()"
                                           value="<fmt:message key="save"/>"/>
                                    <input type="button" value="<fmt:message key="cancel"/>" onclick="cancelScript()"
                                           class="button"/>
                                </td>
                            </tr>

                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td>
                        <table class="normal-nopadding">
                            <tbody>
                            <tr>
                                <td>
                                </td>
                                <td>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="middle-header">
                        <a name="scriptResults"> <fmt:message key="script.results"/> </a>
                    </td>
                </tr>
                <tr>
                    <td>

                    </td>
                </tr>
                <tr>
                    <td>
                        <div id="hiveResult" class="scrollable" style="width:99%">
                                <%--the results goes here...--%>
                        </div>
                    </td>
                </tr>
                </tbody>
                <input type="hidden" name="scriptContent" id="scriptContent"/>
                <input type="hidden" name="saveWithCron" id="saveWithCron"/>
            </table>
            </td>
            </tr>
            </tbody>
            </table>

        </form>


    </div>
</div>

<script type="text/javascript">
    <%--var commands = '<%=scriptContent%>';--%>
    //  editAreaLoader.setValue('allcommands', commands.replace(/#*#/g, '\'').replace(/$*$/g, '\"'));
    <%--editAreaLoader.setValue('allcommands', '<%=scriptContent%>');--%>
</script>

<%
    String saveWithCron = request.getParameter("saveWithCron");
    if (null != saveWithCron && !saveWithCron.equals("")) {
%>

<script type="text/javascript">
    saveScript();
</script>
<%
    }
%>

</fmt:bundle>
