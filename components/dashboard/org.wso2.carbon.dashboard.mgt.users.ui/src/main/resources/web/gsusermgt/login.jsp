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
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.users.ui.GadgetServerUserManagementServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoUiUtils" %>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


    GadgetServerUserManagementServiceClient gsUserMgtServiceClient = new GadgetServerUserManagementServiceClient(cookie,
            backendServerURL,
            configContext,
            request.getLocale());
    try {
        session.invalidate();
    } catch (Exception ignored) {
        // Ignore exception when invalidating and invalidated session
    }

    String tenantdomain = (String) request.getAttribute(CarbonConstants.TENANT_DOMAIN);
    String tDomainWithAt = "";
    if (tenantdomain != null) {
        tDomainWithAt = "@" + tenantdomain;
    }

    String portalCSS = "";
    try {
        portalCSS = GadgetRepoUiUtils.getPortalCss(cookie, backendServerURL, configContext, request.getLocale(), request, config);
    } catch (Exception ignore) {

    }

    // We are not displaying self-registration + Infocard etc for MT mode.
    boolean multiTenantMode = CarbonUIUtil.isContextRegistered(config, "/tenant-login/");%>

<script type="text/javascript" src="../dialog/js/dialog.js"></script>
<script type="text/javascript" src="../admin/js/jquery.js"></script>

<script type="text/javascript" src="../admin/js/jquery.form.js"></script>
<script type="text/javascript" src="../dialog/js/jqueryui/jquery-ui.min.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.users.ui.resource.i18n.Resources">

<%

    String loginStatus = request.getParameter("loginStatus");

    if (loginStatus != null) {
      if( "false".equalsIgnoreCase(loginStatus)) {

%>
    <script type="text/javascript">
            jQuery(document).ready(function() {
            CARBON.showWarningDialog('<fmt:message key="login.fail.message"/>');
            });
    </script>
    <%
        }
        }
    %>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
    <title>WSO2 Gadget Server</title>

    <link href="../admin/css/global.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="../dialog/css/jqueryui/jqueryui-themeroller.css" rel="stylesheet" type="text/css"
          media="all"/>
    <link href="../dialog/css/dialog.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="../gsusermgt/css/login.css" rel="stylesheet" type="text/css" media="all"/>

    <link href="<%=portalCSS%>" rel="stylesheet"
          type="text/css" media="all"/>

    <link rel="icon" href="../admin/images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="../admin/images/favicon.ico" type="image/x-icon"/>


    <script type="text/javascript" src="../admin/js/WSRequest.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
</head>

<body>
<div id="dcontainer"></div>
<script type="text/javascript" src="../dialog/js/dialog.js"></script>

<!--This is the link panel of the portal page-->
<div id="link-panel">
    <div class="left-logo">
        <a class="header-home" href="../../portal">
            <img width="179" height="28" src="images/1px.gif"/>

        </a>
    </div>
</div>

<div id="middle">
<div id="workArea">
<div>

<div id="loginContent">
     <table style="width:100%">
         <tr>
             <td style="text-align:left">
                 <table class="tip-table">
                     <tbody>
                     <tr>
                         <td class="tip-top enterprice-info"></td>
                         <td class="tip-empty"></td>
                         <td class="tip-top easyuser"></td>
                     </tr>
                     <tr>
                         <td class="tip-content">
                             <div class="tip-content-lifter">
                                 <h3 class="tip-title">Enterprise Information Portal</h3> <br>
                                 <p>Users can organize gadgets in a familiar portal interface. Sets of gadgets can be
                                     organized
                                     using tabs.</p>

                             </div>
                         </td>
                         <td class="tip-empty"></td>
                         <td class="tip-content">
                             <div class="tip-content-lifter">
                                 <h3 class="tip-title">Easy User Options</h3><br>
                                 <p>Add gadgets from the enterprise repository or from an external URL. Drag-and-drop
                                     gadgets
                                     into
                                     new arrangements.</p>

                             </div>
                         </td>
                     </tr>
                     <tr>
                         <td class="tip-bottom"></td>
                         <td class="tip-empty"></td>
                         <td class="tip-bottom"></td>
                     </tr>
                     </tbody>
                 </table>
                 <div style="height:10px;"></div>
                 <table class="tip-table">
                     <tbody>
                     <tr>
                         <td class="tip-top author-gadgets"></td>
                         <td class="tip-empty "></td>
                         <td class="tip-top client-side-gadgets"></td>
                     </tr>
                     <tr>
                         <td class="tip-content">
                             <div class="tip-content-lifter">
                                 <h3 class="tip-title">Author Gadgets</h3> <br>

                                 <p>Suports XML, HTML, and Javascript and use 3rd party Javascript libraries. Include
                                     Flash or
                                     other
                                     embeddable formats.</p>

                             </div>
                         </td>
                         <td class="tip-empty"></td>
                         <td class="tip-content">
                             <div class="tip-content-lifter">
                                 <h3 class="tip-title">Client-side Gadgets</h3> <br>


                                 <p>Any gadget adhering to the Google Gadget specification can be added to the
                                     portal. </p>

                             </div>
                         </td>
                     </tr>
                     <tr>
                         <td class="tip-bottom"></td>
                         <td class="tip-empty"></td>
                         <td class="tip-bottom"></td>
                     </tr>
                     </tbody>
                 </table>
             </td>
             <td class="page-login-box">
                 <table class="page-header-links-table" cellspacing="0"
                        style="margin-bottom:5px;">
                     <tr>
                         <td class="page-header-help"><a target="_blank"
                                                         href="/docs/user_reg.html">Help</a>
                         </td>
                     </tr>
                 </table>
                 <!-- login box -->
                 <div id="login" class="gsLoginBox">
                     <form method="POST" action="../admin/login_action.jsp"
                           id="login-form">
                         <input type="hidden" name="backendURL"
                                value="<%=backendServerURL%>"/>
                         <input type="hidden" name="IndexPageURL"
                                value="/carbon/gsusermgt/middle.jsp"/>
                         <input type="hidden" name="gsHttpRequest"
                   value="/carbon/gsusermgt/login.jsp"/>

                         <div id="mainLogin">
                             <table width="100%" cellspacing="3" cellpadding="5"
                                    border="0">
                                 <tbody>
                                 <tr>
                                     <td valign="top" nowrap="nowrap"
                                         style="text-align: center;">

                                         <div class="loginBox">
                                             <table cellspacing="0" cellpadding="1"
                                                    border="0"
                                                    align="center">
                                                 <tbody>
                                                 <tr>
                                                     <td align="center" colspan="2">
                                                         <font size="-1">
                                                             <b>Sign-in to WSO2
                                                                 Gadget Server</b>
                                                         </font>
                                                     </td>
                                                 </tr>
                                                 <tr>
                                                     <td align="center" colspan="2"
                                                         height="20">
                                                     </td>
                                                 </tr>
                                                 <tr>
                                                     <td nowrap="nowrap">
                                                         <div align="right"
                                                              style="margin-top:3px;">
                                                             <span>Username: </span>
                                                         </div>
                                                     </td>
                                                     <td>
                                                         <input type="text" size="18"
                                                                id="username"
                                                                name="username"/><span
                                                             id="tenantDomain"><%=tDomainWithAt%></span> <span
                                                             id="busyCheck"></span>
                                                     </td>
                                                 </tr>
                                                 <tr>
                                                     <td/>
                                                     <td align="left">
                                                     </td>
                                                 </tr>
                                                 <tr>
                                                     <td nowrap="nowrap">
                                                         <div align="right"
                                                              style="margin-top:3px;">
                                                             <span>Password: </span>
                                                         </div>
                                                     </td>
                                                     <td>
                                                         <input type="password"
                                                                size="18"
                                                                id="password"
                                                                name="password"/>
                                                     </td>
                                                 </tr>
                                                 <!--tr>
                                                     <td valign="top" align="right">
                                                     </td>
                                                     <td>
                                                         <input type="checkbox"
                                                                id="PersistentCookie"
                                                                name="PersistentCookie"/>
                                                         <label for="PersistentCookie">Stay
                                                             Signed-in</label>
                                                     </td>
                                                 </tr-->
                                                 <!--tr>
                                                     <td></td>
                                                     <td>
                                                         <input type="checkbox" name="rememberMe"
                                                                value="rememberMe" tabindex="3"/>
                                                         <label>Remember Me</label>
                                                     </td>

                                                 </tr-->
                                                 <tr>
                                                     <td>
                                                     </td>
                                                     <td align="left">
                                                         <input type="submit"
                                                                class="button"
                                                                value="Sign-in"
                                                                name="action"/>
                                                     </td>
                                                 </tr>
                                                 </tbody>
                                             </table>
                                         </div>
                                     </td>
                                 </tr>
                                 </tbody>
                             </table>
                         </div>
                     </form>
                 </div>
                 <!-- end login box -->
                 <!-- links box (below login box) -->
                 <table width="100%" cellpadding="0" bgcolor="#BCBEC0" id="links">
                     <tbody>
                     <tr bgcolor="#F2F2F2">
                         <td valign="top">
                             <div align="center" style="margin: 10px 0pt;">
                                 <%
                                     if (multiTenantMode) {
                                 %>
                                 <font size="-1">New to WSO2 Gadget Server as a Service? <a
                                         href="../carbon/">Sign-up</a>.</font>
                                 <%

                                 } else {
                                 %>
                                 <font size="-1">You can use <a
                                         href="../relyingparty/index.jsp?css=<%=URLEncoder.encode(portalCSS,"UTF-8")%>&title=WSO2 Gadget Server&forwardPage=<%=URLEncoder.encode("../../portal","UTF-8")%>">OpenId
                                     or Infocard </a>to Sign-in.</font>
                                 <br/><br/>
                                 <%
                                     if (gsUserMgtServiceClient.isSelfRegistration(tenantdomain)) {
                                 %>
                                 <font size="-1">New to WSO2 Gadget Server? <a
                                         href="../user-registration/index.jsp?css=<%=URLEncoder.encode(portalCSS,"UTF-8")%>&title=WSO2 Gadget Server&forwardPage=<%=URLEncoder.encode("../../portal","UTF-8")%>">Sign-up</a>.</font>
                                 <br/><br/>
                                 <%
                                     }%>
                                 <font size="-1">
                                     <a href="http://wso2.org/projects/gadget-server"
                                        target="_blank">About
                                         WSO2 Gadget Server</a>
                                     <br>
                                 </font>
                                 <br/>
                                 <%
                                     }
                                 %>
                             </div>
                         </td>
                     </tr>
                     </tbody>
                 </table>
    <!-- end links box (below login box) -->
             </td>
         </tr>
     </table>

</div>

<div class="footer-content">
    <div class="copyright">&copy; 2008 - 2010 WSO2 Inc. All Rights Reserved.</div>
</div>
</div>
</div>
</div>

</body>

</html>
</fmt:bundle>
