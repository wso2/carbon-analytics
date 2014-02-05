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
<%@ page import="org.wso2.carbon.registry.social.impl.people.userprofile.PersonManagerImpl" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
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
    String loggedUser = CharacterEncoder.getSafeText((String) request.getSession().getAttribute("logged-user"));

    PersonManagerImpl personManger = null;
    String[][] userList = null;
    String[][] userListDetails = null;
    String portalCSS = "";
    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetServerSocialDataMgtServiceClient socialDataMgtClient = null;
    try {
        personManger = new PersonManagerImpl();
        userListDetails = personManger.getUserList(SocialUtils.USER_LIST_FILTER_STRING,
                SocialUtils.USER_LIST_SIZE);
        if (userListDetails != null) {
            userList = new String[3][userListDetails[0].length - 1];       // -1 because the logged in user won't be displayed
            int index = 0, idIndex = 0;
            for (String id : userListDetails[0]) {
                if (!(loggedUser.equals(id))) {
                    userList[0][index] = id;
                    userList[1][index] = userListDetails[1][idIndex];
                    Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
                    if (!registry.resourceExists(SocialUtils.USER_ROOT + userListDetails[0][idIndex] + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + SocialUtils.DEFAULT_PROFILE + SocialUtils.USER_PROFILE_IMAGE)) {
                        userList[2][index] = SocialUtils.USER_PROFILE_REGISTRY_ROOT + SocialUtils.PROFILE_IMAGE_NAME;
                    } else {
                        userList[2][index] = SocialUtils.USER_PROFILE_REGISTRY_ROOT + userListDetails[0][idIndex] + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + SocialUtils.DEFAULT_PROFILE + SocialUtils.USER_PROFILE_IMAGE;
                    }
                    index++;
                }
                idIndex++;
            }
        }

        portalCSS = SocialUiUtils.getPortalCss(cookie, backendServerURL, configContext, request.getLocale(), request, config);
        socialDataMgtClient = new GadgetServerSocialDataMgtServiceClient
                (cookie, backendServerURL, configContext, request.getLocale());
    } catch (Exception e) {

    }
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
    <%--<carbon:breadcrumb label="Users List"
                       resourceBundle="org.wso2.carbon.dashboard.social.ui.resource.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
--%>
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
                    <td><h2 style="padding-left:15px; padding-top:15px;">Users List</h2></td>
                </tr>
                <tr>
                    <td height="100%" style="background-color: #FFFFFF;">
                        <div class="gadgetInfo">
                                    <% if(userList!=null && userList[1].length>0) {%>
                            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                                <% for (int id = 0; id < userList[1].length;) {%>
                                <tr>
                                    <% for (int i = 0; i < 3 && id < userList[1].length; i++) {%>
                                    <td width="33%" style="padding:20px; border:0px solid #D7D7D7;">
                                        <table width="100%" cellspacing="0" cellpadding="0" border="0">
                                            <tr>
                                                <td><a href="showprofile.jsp?owner=<%=userList[0][id]%>">
                                                    <% if (socialDataMgtClient.isProfileImageExists(userList[0][id])) { %>
                                                    <img src="profile-image-ajaxprocessor.jsp?userId=<%=userList[0][id]%>"
                                                         class="gadgetImage" width="100px" height="100px"/>
                                                    <% } else { %>
                                                    <img src="images/defaultprofileimage.jpg"
                                                         class="gadgetImage" width="100px" height="100px"/>
                                                    <% } %>

                                                </a></td>
                                                <td><img src="images/1px.gif" width="15" height="1"/></td>
                                                <td width="100%"><a
                                                        href="showprofile.jsp?owner=<%=userList[0][id]%>"><%=userList[1][id++]%>
                                                </a>
                                                </td>
                                            </tr>
                                        </table>
                                    </td>
                                    <%} %>
                                </tr>
                                <%} %>
                            </table>
                                    <%}%>
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