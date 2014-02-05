<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.analytics.hive.ui.client.HiveScriptStoreClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
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
    <script src="../editarea/edit_area_full.js" type="text/javascript"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <link rel="stylesheet" type="text/css" href="css/hive-explorer-styles.css">

    <%
        String scriptName = "";
        String scriptContent = "";
        scriptName = request.getParameter("scriptName");

        try {
            String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
            ConfigurationContext configContext =
                    (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
            HiveScriptStoreClient client = new HiveScriptStoreClient(cookie, serverURL, configContext);
//
        } catch (Exception e) {
            String errorString = e.getMessage();
            CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request, e);
    %>
    <script type="text/javascript">
        location.href = "../admin/error.jsp";
        alert('<%=errorString%>');
    </script>
    <%
        }
    %>
    <script type="text/javascript">
        jQuery(document).ready(function() {
            executeQuery();
        });
    </script>


    <script type="text/javascript">

        function executeQuery() {
                var execScriptName = '<%=scriptName%>';
                document.getElementById('middle').style.cursor = 'wait';
                openProgressBar();
                new Ajax.Request('../hive-explorer/executeQuery.jsp', {
                            method: 'post',
                            parameters: {scriptName:execScriptName},
                            onSuccess: function(transport) {
                                closeProgrsssBar();
                                document.getElementById('middle').style.cursor = '';
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
        }

        function openProgressBar() {
          var content = '<div id="overlay"><div id="box"><div class="ui-dialog-title-bar">'+
                  'Executing Hive Queries<a href="#" title="Close" class="ui-dialog-titlebar-close" onclick="closeProgrsssBar();">'+
                    '<span style="display: none">x</span></a>'+
                  '</div><div class="dialog-content"><img src="../resources/images/ajax-loader.gif" />'+
                  ' Executing Hive Queries...</div></div></div>';
        document.getElementById('dynamic').innerHTML = content;
        }

        function closeProgrsssBar() {
            document.getElementById('dynamic').innerHTML = '';
        }

    </script>



    <div id="middle">
       <div id="dynamic"></div>
        <h2>Script Editor<%=" - " + scriptName%>
        </h2>

        <div id="workArea">

            <form id="commandForm" name="commandForm" action="" method="POST">
                <table class="styledLeft noBorders">
                    <tbody>
                    <tr>
                        <td class="middle-header">
                            <fmt:message key="script.results"/>
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
                </table>
            </form>
        </div>
    </div>

</fmt:bundle>