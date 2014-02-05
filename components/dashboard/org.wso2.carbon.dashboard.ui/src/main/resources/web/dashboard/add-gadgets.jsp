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
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardUiUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // Getting the Dashboard name. Will be null in case of portal mode.
    String dashboardName = request.getParameter("name");
    if ((dashboardName == null) | ("null".equals(dashboardName))) {
        dashboardName = null;
    }

    if (dashboardName == null && !DashboardUiUtils.isGadgetServer()) {
        return;
    }

    String activeTab = request.getParameter("tab");
    if (activeTab == null) {
        activeTab = "0";
    }

    //
    String gadgetGrp = request.getParameter("grp");

    // Check the availability of Gadge Repo. If available re-direct
    boolean gadgetRepoFound = CarbonUIUtil.isContextRegistered(config, "/gadgetrepo/");
    if (gadgetRepoFound) {
%>

<c:redirect url="../gadgetrepo/gadget-browser.jsp">
    <c:param name="tab" value="<%=activeTab%>"/>
    <c:param name="grp" value="<%=gadgetGrp%>"/>
</c:redirect>

<%
    }

    String loggeduser = (String) request.getSession().getAttribute("logged-user");


    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


    DashboardServiceClient dashboardServiceClient = new DashboardServiceClient(cookie,
            backendServerURL,
            configContext,
            request.getLocale());
    // Check the rendering mode
    boolean isReadOnlyMode = dashboardServiceClient.isReadOnlyMode(loggeduser);

    String portalCSS = DashboardUiUtils.getPortalCss(request, config, dashboardServiceClient);

    if (isReadOnlyMode) {
        // This user can't add Gadgets redirect to whatever page they came from
        out.print("Sorry! You are not authorized to add Gadgets.");

    } else {

        if (dashboardName == null) {
            // Adding HTML wrapper tags
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

    <link rel="icon" href="../admin/images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="../admin/images/favicon.ico" type="image/x-icon"/>

    <script type="text/javascript" src="../admin/js/jquery.js"></script>
    <script type="text/javascript" src="../admin/js/jquery.form.js"></script>
    <script type="text/javascript" src="../dialog/js/jqueryui/jquery-ui.min.js"></script>
    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript" src="../admin/js/WSRequest.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>

    <script type="text/javascript" src="../dialog/js/dialog.js"></script>

    <script type="text/javascript" src="javascript/util.js"></script>
</head>

<body>
<div id="dcontainer"></div>
<div id="link-panel">
    <div class="right-links">
        <ul>
            <li>
                <strong>Signed-in as&nbsp;<%=loggeduser%>
                </strong>
            </li>
            <li>|</li>
<%--            <li>
                <a href="javaScript:redirectToHttpsUrl('../admin/logout_action.jsp?IndexPageURL=/carbon/gsusermgt/middle.jsp', '<%=DashboardUiUtils.getHttpsPort(backendServerURL)%>')">Sign-out</a>
            </li>--%>
            <li>
                <a href="../admin/logout_action.jsp">Sign-out</a>
            </li>
        </ul>
    </div>
    <div class="left-logo">
        <a class="header-home" href="index.jsp">
            <img width="179" height="28" src="images/1px.gif"/>
        </a>
    </div>
</div>
<%
    }
%>


<link rel="stylesheet" type="text/css" href="../admin/css/main.css"/>
<link rel="stylesheet" type="text/css" href="../admin/css/gadgets.css"/>

<script type="text/javascript" src="global-params.js"></script>
<script type="text/javascript" src="../admin/js/dhtmlHistory.js"></script>
<script type="text/javascript" src="../admin/js/WSRequest.js"></script>
<script type="text/javascript" src="../admin/js/breadcrumbs.js"></script>
<script type="text/javascript" src="../admin/js/cookies.js"></script>

<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>

<script type="text/javascript" src="javascript/jquery-1.2.6.js"></script>
<script type="text/javascript" src="javascript/jquery-ui.min.js"></script>

<script type="text/javascript" src="javascript/gadget-server-utils.js"></script>
<script type="text/javascript" src="javascript/userpref-service-stub.js"></script>

<script type="text/javascript" src="javascript/util.js"></script>

<script type="text/javascript">
    var userId = '<%=loggeduser%>';

    // Setting the Dashboard name to be used in Javascript
    <%
    if(dashboardName == null){
    %>
    var dashboardName = null;
    <%
    }else{
    %>
    var dashboardName = '<%=dashboardName%>';
    <%
    }
    %>

    function addGadget() {
        var gadgetUrl = document.getElementById('gadgetUrl').value;

        if ((gadgetUrl == "") || (gadgetUrl.length == 0)) {
            CARBON.showErrorDialog(jsi18n["please.enter.location"]);
        } else if (gadgetUrl.indexOf("https://") == 0) {
            CARBON.showErrorDialog(jsi18n["block.https"]);
        } else {
            dashboardService.addGadget(userId, <%=activeTab%>, gadgetUrl, dashboardName, '<%=gadgetGrp%>#');
            window.location = 'index.jsp?tab=<%=activeTab%>&name=' + dashboardName;
        }
    }


    function returnToDashboard() {
        window.location = 'index.jsp?tab=<%=activeTab%>&name=' + dashboardName;
    }

</script>

<fmt:bundle basename="org.wso2.carbon.dashboard.ui.resource.i18n.Resources">
    <carbon:jsi18n resourceBundle="org.wso2.carbon.dashboard.ui.resource.i18n.JSResources"
                   request="<%=request%>"/>
    <carbon:breadcrumb
            label="Add Gadgets"
            resourceBundle="org.wso2.carbon.dashboard.ui.resource.i18n.Resources"
            topPage="false"
            request="<%=request%>"/>
    <div id="middle">

        <%
            if (dashboardName != null) {
        %>
        <h2><fmt:message key="ading.new.gadgets"/></h2>
        <%
            }
        %>
        <div id="workArea">

            <table width="100%" cellspacing="0">
                <tr>
                    <td class="gadgets-top-links">
                        <a class="icon-link"
                           style="background-image: url(../dashboard/images/return-to-dashboard.png);padding-left:30px"
                           onclick="returnToDashboard()"><fmt:message
                                key="return.to.dashboard"/></a>
                    </td>
                </tr>

                <%
                    if ((dashboardServiceClient.isExternalGadgetAdditionEnabled()) || ((dashboardName != null)) && (!"null".equals(dashboardName))) {
                %>
                <tr>
                    <table class="styledLeft">
                        <thead>
                        <tr>
                            <th><fmt:message key="enter.gadget.location"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td class="formRow">
                                <table class="normal">
                                    <tbody>
                                    <tr>
                                        <td>
                                            <label><fmt:message key="enter.url"/></label>
                                        </td>
                                        <td>
                                            <input type="text" size="50" name="gadgetUrl"
                                                   id="gadgetUrl"/>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td class="buttonRow">
                                <input type="button" onclick="addGadget()"
                                       value="<fmt:message key="add.gadget.button"/>"
                                       class="button" id="addGadget"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </tr>
                <%
                } else {
                %>
                <tr>
                    <table class="styledLeft">
                        <thead>
                        <tr>
                            <th><fmt:message key="enter.gadget.location"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                            <td>
                                Sorry! This feature was disabled by the Administrator.
                            </td>
                        </tr>

                        </tbody>
                    </table>
                </tr>
                <%
                    }%>

            </table>
        </div>

    </div>
    <%
        if (dashboardName == null) {
            // Closing HTML wrapper tags
    %>
    <div class="footer-content">
        <div class="copyright">&copy; 2008 - 2011 WSO2 Inc. All Rights Reserved.</div>
    </div>

    </body>
    </html>
    <%
    } else {
    %>
    <script type="text/javascript">
        // This is needed in product mode to fix the bread crumb not having the correct link.
        fixBreadCrumb('<%=dashboardName%>');
    </script>
    <%
        }
    %>
</fmt:bundle>
<%
    }
%>
