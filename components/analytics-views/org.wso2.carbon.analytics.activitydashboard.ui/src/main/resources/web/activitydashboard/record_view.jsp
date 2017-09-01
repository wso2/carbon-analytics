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
<%@ page import="org.wso2.carbon.analytics.activitydashboard.stub.bean.RecordBean" %>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.stub.bean.ColumnEntry" %>
<%@ page
        import="org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceActivityDashboardExceptionException" %>
<%@ page import="org.owasp.encoder.Encode" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources">
    <carbon:breadcrumb
            label="activitydashboard"
            resourceBundle="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>

    <script type="text/javascript">
        function backButton() {
            window.history.back();
        }
    </script>
    <%
        String recordId = request.getParameter("recordId");
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ActivityDashboardClient client = new ActivityDashboardClient(cookie, serverURL, configContext);
        RecordBean recordBean = null;
        try {
            recordBean = client.getRecord(recordId);
        } catch (ActivityDashboardAdminServiceActivityDashboardExceptionException e) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog(<%=e.getFaultMessage().getActivityDashboardException().getErrorMessage()%>, backButton,
                backButton);
    </script>
    <% }
    %>
    <div id="middle">
        <h2>Record: <%=recordId%>
        </h2>

        <div id="workArea">
            <table class="carbonFormTable">
                <%
                    ColumnEntry[] columnEntries = recordBean.getColumnEntries();
                    for (ColumnEntry columnEntry : columnEntries) {
                %>
                <tr>
                    <td class="leftCol-med labelField">
                        <%=Encode.forHtmlContent(columnEntry.getName())%>
                    </td>
                    <td>
                        <%=Encode.forHtmlContent(columnEntry.getValue())%>
                    </td>
                </tr>
                <%
                    }

                %>
            </table>
            <div class="buttonRow">
                <input class="button" type="button" onclick="backButton()"
                       value="< Back"/>
            </div>
        </div>
    </div>

</fmt:bundle>