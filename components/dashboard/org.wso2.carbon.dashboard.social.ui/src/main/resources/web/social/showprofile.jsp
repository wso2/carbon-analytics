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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dashboard.social.GadgetServerSocialDataMgtServiceContext" %>
<%@ page import="org.wso2.carbon.dashboard.social.common.PrivacyFieldDTO" %>
<%@ page import="org.wso2.carbon.dashboard.social.common.utils.SocialUtils" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.GadgetServerSocialDataMgtServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.SocialUiUtils" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page import="org.wso2.carbon.registry.social.api.activity.Activity" %>
<%@ page import="org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager" %>
<%@ page import="org.wso2.carbon.registry.social.impl.SocialImplConstants" %>
<%@ page import="org.wso2.carbon.registry.social.impl.activity.ActivityManagerImpl" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.userprofile.PersonManagerImpl" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.userprofile.model.PersonImpl" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.user.core.claim.Claim" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
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

    String owner = CharacterEncoder.getSafeText((String) request.getParameter("owner"));
    GadgetServerSocialDataMgtServiceClient socialDataMgtClient = null;
    PersonImpl person = null;
    PersonManagerImpl personManger = null;
    RelationshipManager relationshipManager = null;
    String relationshipStatus = null;
    Claim[] claimInfo = null;
    PrivacyFieldDTO[] profileData = null;
    ;
    String profileImagePath = SocialUtils.USER_PROFILE_REGISTRY_ROOT + owner + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + "/default/" + SocialUtils.USER_PROFILE_IMAGE;
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String portalCSS = "";

    String portalURL = "../../carbon/dashboard/index.jsp";

    ActivityManagerImpl manager = null;
    Activity[] activities = null;
    try {
        personManger = new PersonManagerImpl();
        person = (PersonImpl) personManger.getPerson(owner);
        if (person == null) { // no person exists with this user name
            response.sendRedirect("../admin/error.jsp");
            return;
        }
        claimInfo = personManger.getOrderedUserClaimInfo();
        relationshipManager = new RelationshipManagerImpl();
        relationshipStatus = relationshipManager.getRelationshipStatus(loggedUser, owner);


        socialDataMgtClient = new GadgetServerSocialDataMgtServiceClient
                (cookie, backendServerURL, configContext, request.getLocale());
        String[] claimUris = new String[claimInfo.length];
        int index = 0;
        for (Claim claim : claimInfo) {
            claimUris[index++] = claim.getClaimUri();
        }
        profileData = socialDataMgtClient.getUserProfile(owner, null, claimUris);
        Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
        if (!registry.resourceExists("/users/" + owner + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + "/default/" + SocialUtils.USER_PROFILE_IMAGE)) {
            profileImagePath = SocialUtils.USER_PROFILE_REGISTRY_ROOT + "profileimage.jpg";
        }
        portalCSS = SocialUiUtils.getPortalCss(cookie, backendServerURL, configContext, request.getLocale(), request, config);
        manager = new ActivityManagerImpl();
        String[] users = new String[1];
        users[0] = owner;
        activities = manager.getSortedActivities(users, "self", "gs", null, null);
    } catch (Exception e) {

        CarbonUIMessage.sendCarbonUIMessage(e.getMessage(), CarbonUIMessage.ERROR, request);   //TODO: Display alert
        String forwardTo = "../admin/error.jsp";
        response.sendRedirect(forwardTo);
    }


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
        function doAccept() {
            document.friendRequestForm.action = "friendrequest-finish.jsp?requestStatus=accept";
            document.friendRequestForm.submit();
        }

        function doIgnore() {
            document.friendRequestForm.action = "friendrequest-finish.jsp?requestStatus=ignore";
            document.friendRequestForm.submit();
        }
    </script>

</head>

<body>
<div id="dcontainer"></div>
<div id="link-panel">
    <div class="right-links">
        <ul>
            <li><strong>Signed-in as&nbsp;<a href="userprofile.jsp"><%=personManger.getDisplayName(loggedUser)%>
            </a>
            </strong></li>
            <li>|</li>
<%--            <li>
                <a href="javaScript:redirectToHttpsUrl('../admin/logout_action.jsp?IndexPageURL=/carbon/gsusermgt/middle.jsp', '<%=SocialUiUtils.getHttpsPort(backendServerURL)%>')">Sign-out</a>
            </li>--%>
            <li>
                <a href="../admin/logout_action.jsp">Sign-out</a>
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
topPage="false" request="<%=request%>"/>--%>

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
                    <form action="friendrequest-finish.jsp" method="POST"
                          name="friendRequestForm">
                        <input type="hidden" name="owner" value="<%=owner%>"/>

                        <div class="gadgetInfo">
                            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                                <tr>
                                    <td width="75%">
                                        <table cellspacing="0">
                                            <tr>
                                                <td>
                                                    <% if (socialDataMgtClient.isProfileImageExists(owner)) { %>
                                                    <img src="profile-image-ajaxprocessor.jsp?userId=<%=owner%>"
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
                                                            <tr>
                                                                <td><img src="images/1px.gif" width="8" height="1"/>
                                                                </td>
                                                                <td nowrap="nowrap"
                                                                    height="30"><%=claimInfo[0].getDisplayTag()%>
                                                                </td>
                                                                <td><img src="images/1px.gif" width="20" height="1"/>
                                                                </td>
                                                                <td width="100%"><%=person.getUserField(claimInfo[0].getClaimUri())%>
                                                                </td>
                                                                <td><img src="images/1px.gif" width="5" height="1"/>
                                                                </td>
                                                            </tr>
                                                            <tr>
                                                                <td><img src="images/1px.gif" width="8" height="1"/>
                                                                </td>
                                                                <td nowrap="nowrap"
                                                                    height="30"><%=claimInfo[1].getDisplayTag()%>
                                                                </td>
                                                                <td>&nbsp;</td>
                                                                <td width="100%"><%=person.getUserField(claimInfo[1].getClaimUri())%>
                                                                </td>
                                                                <td><img src="images/1px.gif" width="5" height="1"/>
                                                                </td>
                                                            </tr>
                                                            <%
                                                                for (int index = SocialUtils.DEFAULT_PROFILE_FIELDS_COUNT; index < claimInfo.length; index++) {
                                                                    String visibility = profileData[index].getVisibilityValue();
                                                                    if (person.getUserField(claimInfo[index].getClaimUri()) != null &&
                                                                            visibility != null &&
                                                                            SocialUtils.isViewable(relationshipStatus, visibility)) {
                                                            %>
                                                            <tr>
                                                                <td><img src="images/1px.gif" width="8" height="1"/>
                                                                </td>
                                                                <td nowrap="nowrap"
                                                                    height="30"><%=claimInfo[index].getDisplayTag()%>
                                                                </td>
                                                                <td>&nbsp;</td>
                                                                <td width="100%"><%=person.getUserField(claimInfo[index].getClaimUri())%>
                                                                </td>
                                                                <td><img src="images/1px.gif" width="5" height="1"/>
                                                                </td>
                                                            </tr>
                                                            <%
                                                                    }
                                                                }
                                                            %>
                                                            <tr>
                                                                <td colspan="5"><img src="images/1px.gif" width="1"
                                                                                     height="8"/></td>
                                                            </tr>
                                                            <% if (!(loggedUser.equals(owner))) { %>
                                                            <% if (relationshipStatus.equals(SocialImplConstants.RELATIONSHIP_STATUS_FRIEND)) {%>
                                                            <tr>
                                                                <td colspan="5" nowrap="nowrap" class="buttonRow">
                                                                    <input type="hidden" id="requestStatus2"
                                                                           name="requestStatus" value="remove">
                                                                    <input type="submit" class="button"
                                                                        <%-- value="<fmt:message key='remove.as.friend'/>"/>--%>
                                                                           value="Remove from Friends"/>
                                                                </td>


                                                            </tr>
                                                            <% } else if (relationshipStatus.equals(SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_PENDING)) {%>
                                                            <tr>
                                                                <td colspan="5" nowrap="nowrap" class="buttonRow">
                                                                    <input type="text" class="button"
                                                                           value=" <fmt:message key='friend.request.pending'/>"
                                                                           disabled="true"/>

                                                                </td>


                                                            </tr>
                                                            <% } else if (relationshipStatus.equals(SocialImplConstants.RELATIONSHIP_STATUS_REQUEST_RECEIVED)) { %>
                                                            <tr>
                                                                <td colspan="5" nowrap="nowrap" class="buttonRow">
                                                                    <input type="submit" class="button"
                                                                           value="<fmt:message key='accept.request'/>"
                                                                           onclick="doAccept();"/>&nbsp;
                                                                    <input type="submit" class="button"
                                                                           value="<fmt:message key='ignore.request'/>"
                                                                           onclick="doIgnore();"/>

                                                                </td>


                                                            </tr>
                                                            <% } else {%>
                                                            <tr>
                                                                <td colspan="5" nowrap="nowrap" class="buttonRow">
                                                                    <input type="hidden" id="requestStatus3"
                                                                           name="requestStatus" value="add">&nbsp;
                                                                    <input type="submit" class="button"
                                                                           value="<fmt:message key='add.as.friend'/>"/>
                                                                </td>


                                                            </tr>
                                                            <% } %>

                                                            <% } %>

                                                        </table>
                                                    </div>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <td><img src="images/1px.gif" width="50" height="1"/></td>
                                    <td width="25%">&nbsp;</td>
                                </tr>
                            </table>
                        </div>
                    </form>
                    <% if (activities != null && activities.length > 0) { %>
                    <table class="styledLeft">
                        <thead>
                        <tr>
                            <th>User Activities</th>
                        </tr>
                        </thead>
                        <tbody>
                        <%for (int id = 0; id < activities.length; id++) {%>
                        <tr>
                            <td class="formRow">
                                <%
                                    String displayName = personManger.getDisplayName(owner) + " has ";
                                    if (loggedUser.equals(owner)) {
                                        displayName = "You have ";
                                    }
                                %>
                                <%=displayName + activities[id].getTitle()%>
                                <a href="<%=activities[id].getUrl()%>"> &nbsp;view</a>
                            </td>
                        </tr>
                        <%} %>
                        </tbody>
                    </table>
                    <% } %>
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
</fmt:bundle>
</body>
</html>