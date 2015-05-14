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

<%@ page import="org.wso2.carbon.analytics.activitydashboard.commons.SearchExpressionTree" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.ui.ActivityDashboardClient" %>
<%@ page import="org.wso2.carbon.analytics.activitydashboard.stub.bean.RecordId" %>
<%@ page
        import="org.wso2.carbon.analytics.activitydashboard.stub.ActivityDashboardAdminServiceActivityDashboardExceptionException" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<fmt:bundle basename="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources">
    <carbon:breadcrumb
            label="activitydashboard"
            resourceBundle="org.wso2.carbon.analytics.activitydashboard.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <link href="../admin/jsp/registry_styles_ajaxprocessor.jsp" rel="stylesheet" type="text/css" media="all"/>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <link type="text/css" rel="stylesheet" href="../resources/css/registry.css"/>
    <script type="text/javascript" src="../registry_common/js/registry_validation.js"></script>
    <script type="text/javascript" src="../registry_common/js/registry_common.js"></script>
    <script type="text/javascript" src="../resources/js/resource_util.js"></script>
    <script type="text/javascript" src="../generic/js/genericpagi.js"></script>
    <script type="text/javascript" src="../generic/js/generic.js"></script>
    <script type="text/javascript" src="js/artifacts-list.js"></script>

    <%
        String requestPageNo = request.getParameter("pageNo");
        int pageNo = 1;
        if (requestPageNo != null) {
            pageNo = Integer.parseInt(requestPageNo);
        }
        String activityId = request.getParameter("activityId");
        Object recordsSearchResult = request.getSession().getAttribute("recordsSearchResult");
        RecordId[] recordIds = new RecordId[0];
        if (recordsSearchResult == null) {
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
            try {
                recordIds = client.getRecordIds(activityId, tableNames);
            } catch (ActivityDashboardAdminServiceActivityDashboardExceptionException e) {
    %>
    <script type="text/javascript">
        CARBON.showErrorDialog(<%=e.getFaultMessage().getActivityDashboardException().getErrorMessage()%>,
                gotoActivitySearch, gotoActivitySearch);

    </script>
    <% }
    } else {
        recordIds = (RecordId[]) recordsSearchResult;
    }
        int numberPages = (int) (Math.ceil(((double) recordIds.length) / 10.0));
    %>

    <script type="text/javascript">
        function pageLoad(pageNo) {
            var activityId = '<%=activityId%>';
            window.location = "records_list_view.jsp?pageNo=" + pageNo + "&activityId=" + activityId;
        }

        function gotoActivitySearch() {
            window.location = "activities_list_page.jsp?page=1";
        }
    </script>
    <div id="middle">
        <h2>Records for activityId : <%=activityId%>
        </h2>

        <div id="workArea">
            <table class="carbonFormTable">
                <%
                    int startRecordIndex = (pageNo - 1) * 10;
                    int endRecordIndex = startRecordIndex + 10;
                    if (endRecordIndex > recordIds.length) {
                        endRecordIndex = recordIds.length;
                    }
                    for (int i = startRecordIndex; i < endRecordIndex; i++) {
                        RecordId recordId = recordIds[i];
                %>
                <tr>
                    <td class="leftCol-med labelField">
                        <i><a href="record_view.jsp?recordId=<%=recordId.getFullQualifiedId()%>"><%=recordId.getFullQualifiedId()%>
                        </a></i>
                    </td>
                </tr>
                <%
                    }
                %>
            </table>

            <div class="buttonRow">
                <input class="button" type="button" onclick="gotoActivitySearch()"
                       value="Go back to Activity Search Result"/>
            </div>
            <br/>
            <%=startRecordIndex + 1%> - <%=endRecordIndex%> of <%=recordIds.length%>

            <table width="100%" style="text-align:center; padding-top: 10px; margin-bottom: -10px">
                <carbon:resourcePaginator pageNumber="<%=pageNo%>" numberOfPages="<%=numberPages%>"
                                          nextKey="next" prevKey="prev"
                                          paginationFunction="pageLoad({0})"/>
            </table>
        </div>
    </div>

</fmt:bundle>