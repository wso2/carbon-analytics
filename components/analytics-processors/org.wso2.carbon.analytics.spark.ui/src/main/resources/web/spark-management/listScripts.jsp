<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.spark.ui.client.AnalyticsExecutionClient" %>
<%@ page import="org.wso2.carbon.analytics.spark.admin.stub.AnalyticsProcessorAdminServiceStub" %>
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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib uri="http://www.owasp.org/index.php/Category:OWASP_CSRFGuard_Project/Owasp.CsrfGuard.tld" prefix="csrf" %>

<fmt:bundle basename="org.wso2.carbon.analytics.spark.ui.i18n.Resources">
    <carbon:breadcrumb label="analytics_list.menu"
                       resourceBundle="org.wso2.carbon.analytics.hive.ui.i18n.Resources"
                       topPage="true" request="<%=request%>"/>
    <script type="text/javascript">
        function deleteRow(name, msg) {
            CARBON.showConfirmationDialog(msg + "' " + name + " ' ?", function () {
                document.location.href = "deleteScript.jsp?" + "scriptName=" + name;
            });
        }
    </script>
    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        AnalyticsExecutionClient client = new AnalyticsExecutionClient(cookie, serverURL, configContext);
        AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto[] scriptNames = null;

        try {
            scriptNames = client.getAllScripts();
        } catch (Exception e) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog("Error while getting the list of scripts");
    </script>
    <%
        }
    %>
    <div id="middle">
        <h2>Available Analytics Scripts</h2>

        <div id="workArea">

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th class="leftCol-med">
                            <fmt:message key="spark.scripts"/>
                        </th>
                        <th class="leftCol-med">Actions</th>
                    </tr>
                    </thead>
                    <tbody>

                    <% if (null != scriptNames && scriptNames.length != 0) {
                        for (AnalyticsProcessorAdminServiceStub.AnalyticsScriptDto aScript : scriptNames) {
                    %>
                    <tr>
                        <td><label>
                            <%=aScript.getName()%>
                        </label>
                        </td>
                        <td>
                            <form name='executeScript_<%=aScript.getName()%>' action="executeScript.jsp" method='post'>
                                <input type="hidden" name="scriptName" value="<%=aScript.getName()%>"/>
                                <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>"/>
                            </form>
                            <form name='executeInBackground_<%=aScript.getName()%>' action="executeScriptInBackground.jsp" method='post'>
                                <input type="hidden" name="scriptName" value="<%=aScript.getName()%>"/>
                                <input type="hidden" name="<csrf:tokenname/>" value="<csrf:tokenvalue/>"/>
                            </form>
                            <% if (aScript.getEditable()) {%>
                            <a class="icon-link"
                               style="background: url('../spark-management/images/edit.gif') no-repeat;"
                               href="addOrEditScript.jsp?scriptName=<%=aScript.getName()%>">Edit</a>
                            <%if (client.isAnalyticsExecutionEnabled()) {%>
                            <a class="icon-link"
                               onclick="document.forms['executeScript_<%=aScript.getName()%>'].submit();"
                               href="#"
                               style="background: url('../spark-management/images/execute.gif') no-repeat;">
                                Execute</a>
                            <a class="icon-link"
                               onclick="document.forms['executeInBackground_<%=aScript.getName()%>'].submit();"
                               href="#"
                               style="background: url('../spark-management/images/execute.gif') no-repeat;">
                                Execute in Background</a>
                            <%}%>
                                <%--<a class="icon-link" style="background: url('images/tasks-icon.gif') no-repeat;"--%>
                                <%--href="">Schedule--%>
                                <%--Script--%>
                                <%--</a>--%>
                            <a onclick="deleteRow('<%=aScript.getName()%>','Do you want to delete')"
                               class="delete-icon-link" href="#">Delete</a>
                            <%
                            } else {
                            %>
                            <a class="icon-link"
                               style="background: url('../spark-management/images/edit.gif') no-repeat;"
                               href="addOrEditScript.jsp?scriptName=<%=aScript.getName()%>&editable=false">View</a>
                            <% if (client.isAnalyticsExecutionEnabled()) {%>
                            <a class="icon-link"
                               onclick="document.forms['executeScript_<%=aScript.getName()%>'].submit();"
                               href="#"
                               style="background: url('../spark-management/images/execute.gif') no-repeat;">
                                Execute</a>
                            <a class="icon-link"
                               onclick="document.forms['executeInBackground_<%=aScript.getName()%>'].submit();"
                               href="#"
                               style="background: url('../spark-management/images/execute.gif') no-repeat;">
                                Execute in Background</a>
                            <%
                                }
                            }
                            %>
                        </td>

                    </tr>

                    <%
                        }
                    } else { %>
                    <tr>
                        <td colspan="2">No analytics scripts found</td>
                    </tr>

                    <% }
                    %>
                    </tbody>
                </table>
            <table>
                <tbody>
                <tr>
                    <td></td>
                </tr>
                <tr>
                    <td><a class="icon-link" style="background-image:url(images/add.gif);"
                           href="addOrEditScript.jsp"><fmt:message
                            key="spark.script.add"/></a></td>
                </tr>
                </tbody>
            </table>

        </div>
    </div>


</fmt:bundle>
