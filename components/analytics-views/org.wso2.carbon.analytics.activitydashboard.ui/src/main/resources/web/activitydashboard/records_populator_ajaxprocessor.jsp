<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
  ~ KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.commons.*" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.ui.ActivityDashboardClient" %>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.stub.bean.RecordId" %>
<%@ page
        import="org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceActivityDashboardExceptionException" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

<table class="carbonFormTable">
    <%
        if (!"post".equalsIgnoreCase(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String activityId = request.getParameter("activityId");
        Object searchTreeObj = request.getSession().getAttribute("SearchExpression");
        String[] tableNames = new String[0];
        if (searchTreeObj != null) {
            SearchExpressionTree searchExpressionTree = (SearchExpressionTree) searchTreeObj;
            tableNames = searchExpressionTree.getUniqueTableNameInvolved();
        }
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ActivityDashboardClient client = new ActivityDashboardClient(cookie, serverURL, configContext);
        RecordId[] recordIds = new RecordId[0];
        try {
            recordIds = client.getRecordIds(activityId, tableNames);
        } catch (ActivityDashboardAdminServiceActivityDashboardExceptionException e) {
            response.getWriter().write(e.getFaultMessage().getActivityDashboardException().getErrorMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        request.getSession().setAttribute("recordsSearchResult", recordIds);
        int currentRecordCount = 0;
        for (RecordId recordId : recordIds) {
    %>
    <tr>
        <td class="leftCol-med labelField">
            <i><a href="record_view.jsp?recordId=<%=recordId.getFullQualifiedId()%>"><%=recordId.getFullQualifiedId()%>
            </a></i>
        </td>
    </tr>

    <%
            currentRecordCount++;
            if (currentRecordCount == 10) {
                break;
            }
        }
        if (recordIds.length > 10) {
    %>
    <tr>
        <td class="leftCol-med labelField">
            <i><a href="records_list_view.jsp?activityId=<%=activityId%>">more..
            </a></i>
        </td>
    </tr>
    <%
        }
    %>
</table>