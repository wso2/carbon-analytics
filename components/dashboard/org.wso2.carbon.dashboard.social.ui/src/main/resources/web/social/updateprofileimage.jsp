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
<%@ page import="org.wso2.carbon.dashboard.social.ui.SocialUiUtils" %>
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
    if ((request.getSession().getAttribute("logged-user") == null)) {
        // We need to log in this user
        response.sendRedirect("../gsusermgt/login.jsp");
        return;
    }

    String loggedUser = CharacterEncoder.getSafeText((String) request.getSession().getAttribute("logged-user"));
    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    String portalCSS = "";

    String portalURL = "../../carbon/dashboard/index.jsp";
    try {
        portalCSS = SocialUiUtils.getPortalCss(cookie, backendServerURL, configContext, request.getLocale(), request, config);

    } catch (Exception e) {


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
        function cancelUpload() {
            location.href = "userprofile.jsp";
        }
    </script>
</head>

<body>
<div id="dcontainer"></div>
<div id="link-panel">
    <div class="right-links">
        <ul>
            <li><strong>Signed-in as&nbsp;<a
                    href="userprofile.jsp"><%=new PersonManagerImpl().getDisplayName(loggedUser)%>
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
    <%--<carbon:breadcrumb label="Change Profile Picture"
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
                    <td><h2 style="padding-left:15px; padding-top:15px;">Upload Profile Picture</h2></td>
                </tr>
                <tr>
                    <td height="100%" style="background-color: #FFFFFF;">
                        <table class="styledLeft">
                            <thead>
                            <tr>
                                <th>Upload Profile Picture</th>
                            </tr>
                            </thead>
                            <tbody>
                            <form target="_self" enctype="multipart/form-data" action="updateprofileimagefinish.jsp"
                                  id="resourceUploadForm" name="resourceUploadForm" method="post">
                                <tr>
                                    <td class="formRow">
                                        <table class="normal" width="100%" border="0" cellspacing="0" cellpadding="0">
                                            <tr>
                                                <td><img src="images/1px.gif" width="8" height="1"/></td>
                                                <td nowrap="nowrap" height="30">Profile Picture</td>
                                                <td width="100%"><input type="file" id="browsePic" size="20"
                                                                        name="browsePic"/></td>
                                                <td><img src="images/1px.gif" width="5" height="1"/></td>
                                            </tr>
                                        </table>
                                    </td>
                                </tr>
                                <tr>
                                    <td class="buttonRow">
                                        <input type="submit" class="button" value="<fmt:message key='upload.image'/>"/>&nbsp;
                                        <input type="button" value="<fmt:message key='cancel.upload'/>" class="button"
                                               onclick="cancelUpload();">

                                    </td>
                                </tr>
                            </form>
                            </tbody>
                        </table>
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