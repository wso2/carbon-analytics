<%--<!--
 ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 -->--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Gadget" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Comment" %>
<%@ page import="org.wso2.carbon.dashboard.common.DashboardConstants" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", -1);

    String loggeduser = (String) request.getSession().getAttribute("logged-user");

    if ((request.getSession().getAttribute("logged-user") == null)) {
        // We need to log in this user
        CarbonUIMessage.sendCarbonUIMessage("Timed out", CarbonUIMessage.ERROR, request);
%>
<script type="text/javascript">
    CARBON.showErrorDialog(jsi18n["time.out"], null, null);
</script>
<%
        return;
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetRepoServiceClient gadgetRepoServiceClient = new GadgetRepoServiceClient(cookie,
            backendServerURL,
            configContext,
            request.getLocale());

    String gadgetPath = request.getParameter("gadgetPath");

    String userRating = gadgetRepoServiceClient.getUserRating(gadgetPath, loggeduser);

    Gadget gadget = null;

    if (gadgetPath != null) {
        gadget = gadgetRepoServiceClient.getGadget(gadgetPath);
    }

    String activeTab = request.getParameter("tab");
    if (activeTab == null) {
        activeTab = "0";
    }

    String pageNumComStr = request.getParameter("pageNumCom");

    int pages = 0;
    Integer commentCount = gadgetRepoServiceClient.getCommentsCount(gadgetPath);
    if (commentCount != null) {
        pages = (commentCount / DashboardConstants.COMMENTS_PER_PAGE);
        if ((commentCount % DashboardConstants.COMMENTS_PER_PAGE) != 0) {
            pages++;
        }
    }


    int pageNumCom = Integer.parseInt(pageNumComStr);
    int start = 0;

    if (pageNumCom != 0) {
        start = pageNumCom * DashboardConstants.COMMENTS_PER_PAGE;
    }

    int end = start + DashboardConstants.COMMENTS_PER_PAGE;

    Comment[] comments = gadgetRepoServiceClient.getCommentSet(gadgetPath, start, DashboardConstants.COMMENTS_PER_PAGE);

    SimpleDateFormat formatter = new SimpleDateFormat("MMM-dd-yyyy");
%>
<HEAD>
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="Expires" CONTENT="-1">
</HEAD>
<fmt:bundle basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
<%

    if ((comments != null) && (comments.length > 0)) {
        for (int i = 0; i < comments.length; i++) {
            Comment tempCom = comments[i];
            if (tempCom != null && comments.length > 0) {
%>
<input class="commentCls" type="hidden" id="comment-<%=i%>"
       value="<%=tempCom.getCommentPath()%>"/>

<div <%if ((i % 2) > 0) { %>class="tableOddRow"<%} else {%> class="tableEvenRow" <%}%>>
    <table style="width:98%">
        <tr>
            <td class="delete-comment-td">
                <div class="delete-comment-text"><strong><%=tempCom.getAuthorUserName()%></strong> <%=tempCom.getCommentText()%></div>
                <div class="delete-comment-date"><%=formatter.format(tempCom.getCreateTime())%></div>
            </td>

                <%
                    if ((gadgetRepoServiceClient.isAdmin(loggeduser)) ||
                            (loggeduser.equals(tempCom.getAuthorUserName()))) {
                %>
            <td style="width:80px;vertical-align:middle !important;">
                    <a class="icon-link" onclick="deleteComment(<%=i%>)" style="background-image:url(../admin/images/delete.gif);color:#386698">Delete</a>
                </td>
                <%} %>

        </tr>
    </table>



    
</div>
<%} else {%>

<fmt:message key="no.comments.available"/>

<%
            }
        }
    }
    if (commentCount != null && commentCount == 0) {
            %><label><fmt:message key="no.comments.available"/></label><%
        }
    
%>
</fmt:bundle>