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
<%@ page import="org.wso2.carbon.dashboard.social.common.PrivacyFieldDTO" %>
<%@ page import="org.wso2.carbon.dashboard.social.common.utils.SocialUtils" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.GadgetServerSocialDataMgtServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.social.ui.SocialUiUtils" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page import="org.wso2.carbon.registry.social.api.people.relationship.RelationshipManager" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.relationship.RelationshipManagerImpl" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.userprofile.PersonManagerImpl" %>
<%@ page import="org.wso2.carbon.registry.social.impl.people.userprofile.model.PersonImpl" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.ui.util.CharacterEncoder" %>
<%@ page import="org.wso2.carbon.user.core.claim.Claim" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>


<%
    if ((request.getSession().getAttribute("logged-user") == null)) {
        // We need to log in this user
        response.sendRedirect("../gsusermgt/login.jsp");
        return;
    }
    String loggedUser = CharacterEncoder.getSafeText((String) request.getSession().getAttribute("logged-user"));
    GadgetServerSocialDataMgtServiceClient socialDataMgtClient = null;
    PersonImpl person = null;
    PersonManagerImpl personManger = null;
    RelationshipManager relationshipManager = null;
    Claim[] claimInfo = null;
    String[] friendList = null;
    String[] pendingRequests = null;
    PrivacyFieldDTO[] profileData = null;
    String profileImagePath = SocialUtils.USER_PROFILE_REGISTRY_ROOT + loggedUser + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + SocialUtils.DEFAULT_PROFILE + SocialUtils.USER_PROFILE_IMAGE;
    String portalCSS = "";
    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    String portalURL = "../../carbon/dashboard/index.jsp";
    try {
        personManger = new PersonManagerImpl();
        person = (PersonImpl) personManger.getPerson(loggedUser);
        claimInfo = personManger.getOrderedUserClaimInfo();
        relationshipManager = new RelationshipManagerImpl();
        String cookie = (String) session
                .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        ConfigurationContext configContext = (ConfigurationContext) config
                .getServletContext().getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        socialDataMgtClient =
                new GadgetServerSocialDataMgtServiceClient
                        (cookie, backendServerURL, configContext, request.getLocale());

        String[] claimUris = new String[claimInfo.length];
        int index = 0;
        for (Claim claim : claimInfo) {
            claimUris[index++] = claim.getClaimUri();
        }
        profileData = socialDataMgtClient.getUserProfile(loggedUser, null, claimUris);
        Registry registry = GadgetServerSocialDataMgtServiceContext.getRegistry();
        if (!registry.resourceExists(SocialUtils.USER_ROOT + loggedUser + SocialUtils.USER_PROFILE_DASHBOARD_REGISTRY_ROOT + SocialUtils.DEFAULT_PROFILE + SocialUtils.USER_PROFILE_IMAGE)) {
            profileImagePath = SocialUtils.USER_PROFILE_REGISTRY_ROOT + SocialUtils.PROFILE_IMAGE_NAME;
        }
        portalCSS = SocialUiUtils.getPortalCss(cookie, backendServerURL, configContext, request.getLocale(), request, config);

    } catch (Exception e) {

    }


%>
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
    <%--<carbon:breadcrumb label="Update Profile"
   resourceBundle="org.wso2.carbon.dashboard.social.ui.resource.i18n.Resources"
   topPage="false" request="<%=request%>"/>--%>
    <script type="text/javascript">
        function validate() {
        <% if (claimInfo != null) {
        for (int i = 0; i < claimInfo.length; i++) {

        %>
            var value = document.getElementsByName("<%=claimInfo[i].getClaimUri()%>")[0].value;
        <%if (claimInfo[i].isRequired() && claimInfo[i].getDisplayTag()!=null) {%>
            if (validateEmpty("<%=claimInfo[i].getClaimUri()%>").length > 0) {
                CARBON.showWarningDialog("<%=CharacterEncoder.getSafeText(claimInfo[i].getDisplayTag())%>" + " <fmt:message key='is.required'/>");
                return false;
            }
        <%}
     if(claimInfo[i].getRegEx() != null){ %>
            var reg = new RegExp("<%=claimInfo[i].getRegEx()%>");
            var valid = reg.test(value);
            if (value != '' && !valid) {
                CARBON.showWarningDialog("<%=CharacterEncoder.getSafeText(claimInfo[i].getDisplayTag())%>" + " <fmt:message key='is.not.valid'/>");
                return false;
            }
        <%}
             }
        }%>

            document.updateProfileForm.submit();
        }
    </script>
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
                    <td><h2 style="padding-left:15px; padding-top:15px;">Update Profile</h2></td>
                </tr>
                <tr>
                    <td height="100%" style="background-color: #FFFFFF;">
                        <div class="gadgetInfo">
                            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                                <form name="updateProfileForm" action="updateprofile-finish.jsp" method="POST">
                                    <tr>
                                        <td width="75%">
                                            <table cellspacing="0">
                                                <tr>
                                                    <td><% if (socialDataMgtClient.isProfileImageExists(loggedUser)) { %>
                                                        <img src="profile-image-ajaxprocessor.jsp?userId=<%=loggedUser%>"
                                                             class="gadgetImage" width="150px" height="150px"/>
                                                        <% } else { %>
                                                        <img src="images/defaultprofileimage.jpg"
                                                             class="gadgetImage" width="150px" height="150px"/>
                                                        <% } %></td>
                                                    <td style="background-image: url(images/gadgetDescriptionBg.jpg); background-color: #fcfafd; background-position: left top; background-repeat: repeat-x; height: 450px; padding-left: 12px; padding-right: 12px;"
                                                        width="100%">
                                                        <div class="GadgetHeading">&nbsp;User Profile</div>
                                                        <div class="GadgetDescription">
                                                            <table width="100%" border="0" cellspacing="0"
                                                                   cellpadding="0">
                                                                <tr>
                                                                    <td colspan="11"><img src="images/1px.gif" width="1"
                                                                                          height="8"/></td>
                                                                </tr>
                                                                <%
                                                                    for (int index = 0; index < claimInfo.length; index++) {
                                                                        String value = "";
                                                                        if (person.getUserField(claimInfo[index].getClaimUri()) != null) {
                                                                            value = person.getUserField(claimInfo[index].getClaimUri());
                                                                        } else {
                                                                            value = "";
                                                                        }
                                                                %>
                                                                <tr>
                                                                    <td><img src="images/1px.gif" width="8" height="1"/>
                                                                    </td>
                                                                    <td nowrap="nowrap"
                                                                        height="30"><%=claimInfo[index].getDisplayTag()%>
                                                                        <% if (claimInfo[index].isRequired()) {
                                                                        %>
                                                                        <font class="required">*</font>
                                                                        <%
                                                                            }
                                                                        %></td>
                                                                    <td><img src="images/1px.gif" width="20"
                                                                             height="1"/>
                                                                    </td>
                                                                    <td><input type="text"
                                                                               value="<%=value%>"
                                                                               id="<%=claimInfo[index].getClaimUri()%>"
                                                                               name="<%=claimInfo[index].getClaimUri()%>"
                                                                               size="35"></td>
                                                                    <td><img src="images/1px.gif" width="5" height="1"/>
                                                                    </td>
                                                                    <td>&nbsp;</td>
                                                                    <td><img src="images/1px.gif" width="5" height="1"/>
                                                                    </td>
                                                                    <%
                                                                        String visibility = SocialUtils.VISIBILITY_NONE;  // default value is NONE
                                                                        if (profileData != null && profileData.length > 0 && (index - SocialUtils.DEFAULT_PROFILE_FIELDS_COUNT) >= 0) {
                                                                            visibility = profileData[index].getVisibilityValue();

                                                                    %>
                                                                    <td nowrap="nowrap">
                                                                        Display to:
                                                                    </td>
                                                                    <td>&nbsp;</td>
                                                                    <td><select
                                                                            name="show-<%=claimInfo[index].getClaimUri()%>">

                                                                        <option <% if(visibility.equals(SocialUtils.VISIBILITY_NONE)){%>selected="selected"<%}%>
                                                                                value="<%=SocialUtils.VISIBILITY_NONE%>">
                                                                            None
                                                                        </option>
                                                                        <option <% if(visibility.equals(SocialUtils.VISIBILITY_ONLYFRIENDS)){%>selected="selected"
                                                                                <%}%>value="<%=SocialUtils.VISIBILITY_ONLYFRIENDS%>">
                                                                            Only to Friends
                                                                        </option>
                                                                        <option <% if(visibility.equals(SocialUtils.VISIBILITY_EVERYONE)){%>selected="selected"
                                                                                <%}%>value="<%=SocialUtils.VISIBILITY_EVERYONE%>">
                                                                            Every One
                                                                        </option>
                                                                    </select></td>
                                                                    <td width="100%"></td>

                                                                    <%} %>
                                                                </tr>
                                                                <%} %>
                                                                <tr>
                                                                    <td colspan="11"><img src="images/1px.gif" width="1"
                                                                                          height="8"/></td>
                                                                </tr>
                                                                <tr>
                                                                    <td colspan="11" nowrap="nowrap">&nbsp;&nbsp;<input
                                                                            type="button"
                                                                            value="<fmt:message key="update.profile"/>"
                                                                            class="button"
                                                                            onclick="validate();">&nbsp;<input
                                                                            type="reset"
                                                                            value="<fmt:message key="reset"/>"
                                                                            class="button">
                                                                    </td>
                                                                </tr>
                                                            </table>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                        <td><img src="images/1px.gif" width="50" height="1"/></td>
                                        <td width="25%">&nbsp;</td>
                                    </tr>
                                </form>
                            </table>
                        </div>

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