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
<%@ page
        import="org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceActivityDashboardExceptionException" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%
    if (!"post".equalsIgnoreCase(request.getMethod())) {
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        return;
    }

    Object searchTreeObj = request.getSession().getAttribute("SearchExpression");
    String fromTimeString = request.getParameter("fromTime");
    String toTimeString = request.getParameter("toTime");
    long fromTime, toTime;
    SearchExpressionTree searchExpressionTree;
    if (searchTreeObj == null) {
        searchExpressionTree = new SearchExpressionTree();
    } else {
        searchExpressionTree = (SearchExpressionTree) searchTreeObj;
        if (!searchExpressionTree.isCompleted()) {
            response.sendError(HttpServletResponse.SC_PARTIAL_CONTENT, "Search query populated is not completed, please complete the search query and submit again!");
        }
    }
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    ActivityDashboardClient client = new ActivityDashboardClient(cookie, serverURL, configContext);
    if (fromTimeString == null || fromTimeString.isEmpty()) {
        fromTime = Long.MIN_VALUE;
    } else {
        fromTime = Long.parseLong(fromTimeString);
    }
    if (toTimeString == null || toTimeString.isEmpty()) {
        toTime = Long.MAX_VALUE;
    } else {
        toTime = Long.parseLong(toTimeString);
    }
    try {
        String[] activities = client.searchActivities(fromTime, toTime, searchExpressionTree);
        request.getSession().setAttribute("ActivitiesSearchResult", activities);
    } catch (ActivityDashboardAdminServiceActivityDashboardExceptionException e) {
        response.getWriter().print("Cannot complete the search! " +
                e.getFaultMessage().getActivityDashboardException().getErrorMessage());
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }
%>