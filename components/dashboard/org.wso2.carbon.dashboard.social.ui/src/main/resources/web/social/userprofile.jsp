<!--
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
 -->
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dashboard.social.GadgetServerSocialDataMgtServiceContext" %>
<%@ page import="org.wso2.carbon.dashboard.social.common.utils.SocialUtils" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.GadgetServerSocialDataMgtServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.SocialUiUtils" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page import="org.wso2.carbon.registry.social.api.activity.Activity" %>
<%@ page import="org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager" %>
<%@ page import="org.wso2.carbon.registry.social.impl.activity.ActivityManagerImpl" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.userprofile.PersonManagerImpl" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.userprofile.model.PersonImpl" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.user.core.claim.Claim" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%--
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
--%>

<%
    if ((request.getSession().getAttribute("logged-user") == null)) {
        // We need to log in this user
        response.sendRedirect("../gsusermgt/login.jsp");
        return;
    }
    String loggedUser = CharacterEncoder.getSafeText((String) request.getSession().getAttribute("logged-user"));
    String loggedUserDisplayName = loggedUser;
    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    // Code to handle getting https url for privacy related actions
    String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
    int startIndex = adminConsoleURL.indexOf("carbon") + 6;
    String baseAbsUrl = adminConsoleURL.substring(0, startIndex);
    //
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String portalCSS = "";
    PersonImpl person = null;
    PersonManagerImpl personManger = null;
    RelationshipManager relationshipManager = null;
    Claim[] claimInfo = null;
    String[] friendList = null;
    String[] pendingRequests = null;
    GadgetServerSocialDataMgtServiceClient socialDataMgtClient = null;

    String profileImagePath = SocialUtils.USER_PROFILE_REGISTRY_ROOT + loggedUser + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + SocialUtils.DEFAULT_PROFILE + SocialUtils.USER_PROFILE_IMAGE;
    ActivityManagerImpl manager = null;
    Activity[] activities = null;
    try {
        personManger = new PersonManagerImpl();
        person = (PersonImpl) personManger.getPerson(loggedUser);
        loggedUserDisplayName = personManger.getDisplayName(loggedUser);
        claimInfo = personManger.getOrderedUserClaimInfo();
        relationshipManager = new RelationshipManagerImpl();
        friendList = relationshipManager.getRelationshipList(loggedUser);
        pendingRequests = relationshipManager.getPendingRelationshipRequests(loggedUser);
        Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
        if (!registry.resourceExists(SocialUtils.USER_ROOT + loggedUser + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + SocialUtils.DEFAULT_PROFILE + SocialUtils.USER_PROFILE_IMAGE)) {
            profileImagePath = SocialUtils.USER_PROFILE_REGISTRY_ROOT + SocialUtils.PROFILE_IMAGE_NAME;
        }

        manager = new ActivityManagerImpl();
        String[] users = new String[1];
        users[0] = loggedUser;
        activities = manager.getSortedActivities(users, "friends", "gs", null, null);
        socialDataMgtClient = new GadgetServerSocialDataMgtServiceClient
                (cookie, backendServerURL, configContext, request.getLocale());
    } catch (Exception e) {

    }
    portalCSS = SocialUiUtils.getPortalCss(cookie, backendServerURL, configContext, request.getLocale(), request, config);
    String portalURL = "../../carbon/dashboard/index.jsp";
%>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
    <title>WSO2 Gadget Server</title>
    <link href="../admin/css/global.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="../admin/css/main.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="../dialog/css/jqueryui/jqueryui-themeroller.css" rel="stylesheet" type="text/css"
          media="all"/>
    <link href="../dialog/css/dialog.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="<%=portalCSS%>" rel="stylesheet"
          type="text/css" media="all"/>

    <link href="../admin/images/favicon.ico" rel="icon" type="image/x-icon"/>
    <link href="../admin/images/favicon.ico" rel="shortcut icon" type="image/x-icon"/>

    <script type="text/javascript" src="../admin/js/jquery.js"></script>
    <script type="text/javascript" src="../admin/js/jquery.form.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/jquery-ui.min.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>
    <script type="text/javascript" src="../admin/js/WSRequest.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>

    <script type="text/javascript" src="global-params.js"></script>
    <script type="text/javascript" src="../admin/js/dhtmlHistory.js"></script>
    <script type="text/javascript" src="../admin/js/WSRequest.js"></script>
    <script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
    <script type="text/javascript" src="javascript/util.js"></script>
    <script type="text/javascript" src="../dialog/js/dialog.js"></script>
    <script type="text/javascript">
        function returnToDashboard() {
            window.location = '<%=portalURL%>?tab=0';
        }
    </script>

</head>

<body>
<div id="dcontainer"></div>
<div id="link-panel">
    <div class="right-links">
        <ul>
            <li><strong>Signed-in as&nbsp;<%=loggedUserDisplayName%>
            </strong></li>
            <li>|</li>
            <li>
                <a href="javaScript:redirectToHttpsUrl('../admin/logout_action.jsp?IndexPageURL=/carbon/gsusermgt/middle.jsp', '<%=SocialUiUtils.getHttpsPort(backendServerURL)%>')">Sign-out</a>
            </li>
        </ul>
    </div>
    <div class="left-logo"><a class="header-home" href="<%=portalURL%>">
        <img width="179" height="28" src="images/1px.gif"/> </a></div>
</div>


<%--<jsp:include page="../dialog/display_messages.jsp"/>--%>
<fmt:bundle
        basename="org.wso2.carbon.dashboard.social.ui.resource.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.dashboard.social.ui.resource.i18n.JSResources"
        request="<%=request%>"/>
<%-- <carbon:breadcrumb label="User Profile"
                 resourceBundle="org.wso2.carbon.dashboard.social.ui.resource.i18n.Resources"
                 topPage="false" request="<%=request%>"/>
<script type="text/javascript" src="../carbon/admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../carbon/admin/js/cookies.js"></script>
<script type="text/javascript" src="../carbon/admin/js/main.js"></script>--%>
<div id="middle">


<div id="workArea">
<table width="100%" height="100%" cellspacing="0">
<tr>
    <td>
        <ul class="gadgets-top-links">
            <li><a class="icon-link"
                   style="background-image: url(images/dashboard.gif);"
                   onclick="returnToDashboard()"><fmt:message
                    key="return.to.dashboard"/></a></li>
        </ul>
    </td>
</tr>
<tr>
    <td><h2 style="padding-left:15px; padding-top:15px;">User Profile</h2></td>
</tr>
<tr>
    <td height="100%" style="background-color: #FFFFFF;">
        <div class="gadgetInfo">
            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                <tr>
                    <td width="75%">
                        <table cellspacing="0">
                            <tr>
                                <td>
                                    <% if (socialDataMgtClient.isProfileImageExists(loggedUser)) { %>
                                    <img src="profile-image-ajaxprocessor.jsp?userId=<%=loggedUser%>"
                                         class="gadgetImage" width="150px" height="150px"/>
                                    <% } else { %>
                                    <img src="images/defaultprofileimage.jpg"
                                         class="gadgetImage" width="150px" height="150px"/>
                                    <% } %>
                                </td>
                                <td style="background-image: url(images/gadgetDescriptionBg.jpg); background-color: #fcfafd; background-position: left top; background-repeat: repeat-x; height: 450px; padding-left: 12px; padding-right: 12px;"
                                    width="100%">
                                    <div class="GadgetHeading">&nbsp;<%=person.getDisplayName()%>
                                    </div>
                                    <div class="GadgetDescription">
                                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                            <tr>
                                                <td colspan="5"><img src="images/1px.gif" width="1"
                                                                     height="8"/></td>
                                            </tr>
                                            <%

                                                for (int index = 0; index < claimInfo.length; index++) {
                                                    if (person.getUserField(claimInfo[index].getClaimUri()) != null) {

                                            %>
                                            <tr>
                                                <td><img src="images/1px.gif" width="8" height="1"/>
                                                </td>
                                                <td nowrap="nowrap"
                                                    height="30"><%=claimInfo[index].getDisplayTag()%>
                                                </td>
                                                <td><img src="images/1px.gif" width="20" height="1"/>
                                                </td>
                                                <td width="100%"><%=person.getUserField(claimInfo[index].getClaimUri())%>
                                                </td>
                                                <td><img src="images/1px.gif" width="5" height="1"/>
                                                </td>
                                            </tr>
                                            <%
                                                    }
                                                }
                                            %>
                                        </table>
                                        <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                            <tr>
                                                <td colspan="3"><img src="images/1px.gif" width="1"
                                                                     height="11"/></td>
                                            </tr>
                                            <tr>
                                                <td><img src="images/1px.gif" width="8" height="1"/>
                                                </td>
                                                <td><img src="images/my-prof.gif" width="16"
                                                         height="16"/></td>
                                                <td width="100%"><a
                                                        href="<%=baseAbsUrl+"/social/updateprofile.jsp"%>"
                                                        class="icon-link">Update Profile</a></td>
                                            </tr>
                                            <tr>
                                                <td>&nbsp;</td>
                                                <td><img src="images/my-prof.gif" width="16"
                                                         height="16"/></td>
                                                <td>
                                                    <a href="<%=baseAbsUrl+"/social/updateprofileimage.jsp"%>"
                                                       class="icon-link">Change Profile Picture</a></td>
                                            </tr>
                                            <tr>
                                                <td>&nbsp;</td>
                                                <td><img src="images/user-store.gif" width="16"
                                                         height="16"/></td>
                                                <td><a href="showuserlist.jsp" class="icon-link">Show
                                                    All Users</a></td>
                                            </tr>
                                        </table>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </td>
                    <td><img src="images/1px.gif" width="50" height="1"/></td>
                    <td width="25%" style="padding:20px; border:1px solid #D7D7D7;">
                        <%if (friendList != null && friendList.length > 0) { %>
                        <table width="100%" cellspacing="0" cellpadding="0" border="0">
                            <tr>
                                <td colspan="3"><strong>Friends</strong></td>
                            </tr>
                            <tr>
                                <td colspan="3">&nbsp;</td>
                            </tr>
                            <%for (int id = 0; id < friendList.length; id++) { %>
                            <tr>
                                <td><a href="showprofile.jsp?owner=<%=friendList[id]%>">
                                    <% if (socialDataMgtClient.isProfileImageExists(friendList[id])) { %>
                                    <img src="profile-image-ajaxprocessor.jsp?userId=<%=friendList[id]%>"
                                         class="gadgetImage" width="50px" height="50px"/>
                                    <% } else { %>
                                    <img src="images/defaultprofileimage.jpg"
                                         class="gadgetImage" width="50px" height="50px"/>
                                    <% } %>


                                </a></td>
                                <td><img src="images/1px.gif" width="15" height="1"/></td>
                                <td width="100%"><a
                                        href="showprofile.jsp?owner=<%=friendList[id]%>"><%=personManger.getDisplayName(friendList[id])%>
                                </a>
                                </td>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>
                            <% }%>
                            <tr>
                                <td colspan="3" height="1" style="background-color: #D7D7D7;"><img
                                        src="images/1px.gif" width="1" height="1"/></td>
                            </tr>
                        </table>
                        <%}%>
                        <%if (pendingRequests != null && pendingRequests.length > 0) { %>
                        <table width="100%" cellspacing="0" cellpadding="0" border="0">
                            <tr>
                                <td colspan="3">&nbsp;</td>
                            </tr>
                            <tr>
                                <td colspan="3"><strong>Pending Requests</strong></td>
                            </tr>
                            <tr>
                                <td colspan="3">&nbsp;</td>
                            </tr>
                            <%for (int id = 0; id < pendingRequests.length; id++) { %>
                            <tr>
                                <td><a href="showprofile.jsp?owner=<%=pendingRequests[id]%>">

                                    <% if (socialDataMgtClient.isProfileImageExists(pendingRequests[id])) { %>
                                    <img src="profile-image-ajaxprocessor.jsp?userId=<%=pendingRequests[id]%>"
                                         class="gadgetImage" width="50px" height="50px"/>
                                    <% } else { %>
                                    <img src="images/defaultprofileimage.jpg"
                                         class="gadgetImage" width="50px" height="50px"/>
                                    <% } %>

                                </a></td>
                                <td><img src="images/1px.gif" width="15" height="1"/></td>
                                <td width="100%"><a
                                        href="showprofile.jsp?owner=<%=pendingRequests[id]%>"><%=personManger.getDisplayName(pendingRequests[id])%>
                                </a></td>
                            </tr>
                            <tr>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                                <td>&nbsp;</td>
                            </tr>
                            <%}%>
                        </table>
                        <%}%>
                    </td>
                </tr>
            </table>
        </div>
        <% if (activities != null && activities.length > 0) {%>
        <table class="styledLeft">
            <thead>
            <tr>
                <th>User Activities</th>
            </tr>
            </thead>
            <tbody>
            <% for (int id = 0; id < activities.length; id++) {%>
            <tr>
                <td><% String displayName = personManger.getDisplayName(activities[id].getUserId()); %>
                    <%=displayName + " has " + activities[id].getTitle()%>
                    <a href="<%=activities[id].getUrl()%>"> &nbsp;view </a></td>
            </tr>
            <%}%>

            </tbody>
        </table>
        <%}%>
    </td>
</tr>

<tr>
    <td height="20">
        <div class="footer-content">
            <div class="copyright">&copy; 2008 - 2010 WSO2 Inc. All Rights Reserved.</div>
        </div>
    </td>
</tr>
</table>
</div>
</div>
</body>
</html>
</fmt:bundle>
