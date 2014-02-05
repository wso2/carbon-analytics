<%--<!--
    ~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
    ~ KIND, either express or implied. See the License for the
    ~ specific language governing permissions and limitations
    ~ under the License.
    -->--%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardUiContext" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardUiUtils" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardServiceClient" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.dashboard.stub.types.bean.DashboardContentBean" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardUtilServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.stub.types.bean.DashboardUtilBean" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.wso2.carbon.utils.CarbonUtils" %>
<%--<%@ page import="org.wso2.carbon.dashboard.mgt.users.ui.GadgetServerUserManagementServiceClient" %>--%>
<%
    String loggeduser = (String) request.getSession().getAttribute("logged-user");
    String contextRoot = DashboardUiContext.getConfigContext().getContextRoot();
    boolean userLogedIn = true;

    // Check whether we are loading in debug mode
    String debugMode = request.getParameter("debug");
    if ((debugMode == null) | ("null".equals(debugMode))) {
        debugMode = "0";
    }

    // Getting the Dashboard name. Will be null in case of portal mode.
    String dashboardName = request.getParameter("name");
    if ((dashboardName == null) | ("null".equals(dashboardName))) {
        dashboardName = null;
    }

    if (dashboardName == null && !DashboardUiUtils.isGadgetServer()) {
        return;
    }

    //In some products the add gadget / add tabs etc buttons should be hidden by default
    String hideOptions = request.getParameter("hideOpt");
    if ((hideOptions == null) || ("null".equals(hideOptions))) {
        hideOptions = null;
    }

    // Adding tenant domain if available.
    String tenantDomain = (String) request.getSession().getAttribute(RegistryConstants.TENANT_DOMAIN);
    String tenantDomainWithAt = "";
    if (tenantDomain != null) {
        tenantDomainWithAt = "@" + tenantDomain;
    }

    if ((request.getSession().getAttribute("logged-user") == null) && (dashboardName == null)) {
        userLogedIn = false;
        response.sendRedirect(request.getContextPath() + "/admin/login.jsp");
        return;
    }
    DashboardServiceClient dashboardServiceClient = null;
    DashboardUtilServiceClient dashboardUtilServiceClient = null;
    String backendServerURL = null;
    DashboardContentBean dashboardContentBean = null;
    DashboardUtilBean dashboardUtilBean = null;

    boolean isReadOnlyMode = false;


    backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext()
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    try {
        dashboardUtilServiceClient = new DashboardUtilServiceClient(cookie,
                backendServerURL,
                configContext,
                request.getLocale());
        dashboardUtilBean = dashboardUtilServiceClient.getDashboardUtils(tenantDomain);

        if (userLogedIn) {
            dashboardServiceClient = new DashboardServiceClient(cookie,
                    backendServerURL,
                    configContext,
                    request.getLocale());
            dashboardContentBean = dashboardServiceClient.getDashboardContentBean(loggeduser, dashboardName, tenantDomain, backendServerURL);
            // Check the rendering mode
            isReadOnlyMode = dashboardContentBean.getReadOnlyMode();
        }
    } catch (Exception e) {
        response.sendRedirect(request.getContextPath() + "/admin/login.jsp");
//        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
//        session.setAttribute(CarbonUIMessage.ID, uiMsg);
//        e.printStackTrace();

%>


<%
        return;
    }
    String tenantdomainFromReq = (String) request.getAttribute(CarbonConstants.TENANT_DOMAIN);
    if ((null == loggeduser || !dashboardUtilBean.isSessionValidSpecified()) && !dashboardUtilBean.isAnonModeActiveSpecified()) {
        response.sendRedirect(DashboardUiUtils.getLoginUrl("../gsusermgt/login.jsp", DashboardUiUtils.getHttpsPort(adminConsoleURL), request));
    }

    DashboardUiUtils.currentContext = request.getRequestURL();
    String portalCSS = DashboardUiUtils.getPortalCss(request, config, dashboardServiceClient);

    String currentActiveTab = request.getParameter("tab");
    if (currentActiveTab == null) {
        // Default to Home
        currentActiveTab = "0";
    } else if (userLogedIn) {
        // Check if this is a valid tab
        String[] userTabs = dashboardContentBean.getTabLayout().split(",");
        boolean validTab = false;
        for (String userTab : userTabs) {
            if (userTab.equals(currentActiveTab)) {
                validTab = true;
                break;
            }
        }

        if (!validTab) {
            // Revert to home
            currentActiveTab = "0";
        }

    }

    String[] gadgetUrls = null;

    try {
        if (dashboardName == null) {
            // We are in portal mode
            //Checking for the home tab, the default gadgets will be only for a NEW user (New user has only 1 tab)
            if ("0".equals(currentActiveTab)) {
                if (userLogedIn) {
                    gadgetUrls = dashboardContentBean.getDefaultGadgetUrlSet();
                } else {
                    gadgetUrls = dashboardUtilBean.getGadgetUrlSetForUnSignedUser();
                }
            }
        } else {
            // We are in embedded mode
            //gadgetUrls = dashboardServiceClient.getGadgetUrlsToLayout(loggeduser, currentActiveTab, dashboardName, backendServerURL);
            gadgetUrls = DashboardUiUtils.getGadgetUrlsToLayout(currentActiveTab, dashboardContentBean.getTabs());

            // We need to populate gadgets for this tab (On demand population)
            //dashboardServiceClient.populateDashboardTab(currentActiveTab);
        }


    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
        e.printStackTrace();
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
    if (dashboardName == null) {
        // Adding HTML wrapper tags
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
    <title>WSO2 Gadget Server</title>
    <link href="../admin/css/global.css" rel="stylesheet" type="text/css"
          media="all"/>
    <link href="../dialog/css/jqueryui/jqueryui-themeroller.css"
          rel="stylesheet" type="text/css" media="all"/>
    <link href="../dialog/css/dialog.css" rel="stylesheet" type="text/css"
          media="all"/>

    <link href="<%=portalCSS%>" rel="stylesheet"
          type="text/css" media="all"/>

    <link rel="icon" href="../admin/images/favicon.ico" type="image/x-icon"/>
    <link rel="shortcut icon" href="../admin/images/favicon.ico"
          type="image/x-icon"/>

    <script type="text/javascript" src="../admin/js/main.js"></script>

    <script type="text/javascript" src="../admin/js/WSRequest.js"></script>
    <script type="text/javascript" src="../admin/js/cookies.js"></script>
</head>
<body>
<div id="dcontainer"></div>
<!--This is the link panel of the portal page-->
<div id="link-panel">
    <%if (!userLogedIn) { %>
    <ul>
        <%
            /*            //This is to check whether user can self register to the portal or not.
                   GadgetServerUserManagementServiceClient gsUserMgtServiceClient = null;
                   gsUserMgtServiceClient = new GadgetServerUserManagementServiceClient(cookie,
                           backendServerURL,
                           configContext,
                           request.getLocale());
                   if (gsUserMgtServiceClient.isSelfRegistration(tenantdomainFromReq)) {*/
        %>
        <%--        <li>
                  <a href="javaScript:redirectToHttpsUrl('../user-registration/index.jsp?css=<%=URLEncoder.encode(portalCSS,"UTF-8")%>&title=WSO2 Gadget Server&forwardPage=<%=URLEncoder.encode("../../portal","UTF-8")%>','8443')">Create
                      your account now!</a>
              </li>--%>
        <%
            /*} else {*/%>
        <li><strong>Create your account now!</strong></li>
        <% /*}*/
        %>
        <li>|</li>
        <!--li><a href="../gsusermgt/login.jsp">Sign-in</a></li-->
        <li>
            <a href="javaScript:redirectToHttpsUrl('../gsusermgt/login.jsp', '<%=DashboardUiUtils.getBackendPort("https")%>')">Sign-in</a>
        </li>
        <li>|</li>
        <li><a href="http://wso2.org/project/gadget-server/1.4.0/docs/index.html" target="_blank">Help</a></li>
    </ul>
    <%} else if (userLogedIn) { %>
    <ul>
        <li><strong>Signed-in as&nbsp;<%=loggeduser + tenantDomainWithAt%>
        </strong></li>
        <li>|</li>
        <!--li><a href="javaScript:redirectToHttpsUrl('../gsusermgt/login.jsp?action=signOut', '<%=DashboardUiUtils.getBackendPort("https")%>')">Sign-out</a></li-->
        <li>
            <a href="../admin/logout_action.jsp">Sign-out</a>
        </li>

        <li>|</li>
        <li><a href="http://wso2.org/project/gadget-server/1.4.0/docs/index.html" target="_blank">Help</a></li>
    </ul>
    <%} %>
    <div class="left-logo"><a class="header-home" href="index.jsp">
        <img width="179" height="28" src="images/1px.gif"/> </a></div>
</div>
<%}%>
<!-- default container look and feel -->
<%
    if (dashboardName == null) {
%>
<link rel="stylesheet" href="localstyles/gadgets-gs.css">
<%} else {%>
<link rel="stylesheet" href="localstyles/gadgets.css">
<%}%>
<link href="localstyles/menu-style.css" rel="stylesheet"
      type="text/css" media="all"/>

<script type="text/javascript" src="javascript/jquery.min.js"></script>
<script type="text/javascript" src="javascript/jquery.menu.js"></script>

<script type="text/javascript">
    // Setting the Dashboard name to be used in Javascript : setting this
    // here for it is used in gadgets.js
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
    var isReadOnly = <%=isReadOnlyMode%>;

    // Setting debug mode. If ON, JS compression for gadgets will be OFF.
    <%
         if("0".equals(debugMode)){
         %>
    var debugMode = false;
    <%
         }else{
         %>
    var debugMode = true;
    <%
         }
         %>

</script>

<%
    if ("0".equals(debugMode)) {
%>
<!--script type="text/javascript" src="../../gadgets/js/core:rpc:pubsub?c=1"></script-->
<script type="text/javascript" src="../../gadgets/js/core?c=1"></script>
<script type="text/javascript" src="../../gadgets/js/rpc?c=1"></script>
<script type="text/javascript" src="../../gadgets/js/pubsub?c=1"></script>
<%
} else {
%>
<!--script type="text/javascript" src="../../gadgets/js/core:rpc:pubsub?c=1&debug=1"></script-->
<script type="text/javascript" src="../../gadgets/js/core?c=1&debug=1"></script>
<script type="text/javascript" src="../../gadgets/js/rpc?c=1&debug=1"></script>
<script type="text/javascript" src="../../gadgets/js/pubsub?c=1&debug=1"></script>
<%
    }
%>

<script type="text/javascript" src="javascript/cookies.js"></script>
<script type="text/javascript" src="javascript/json2.js"></script>
<script type="text/javascript" src="javascript/dojo.xd.js"></script>
<script type="text/javascript" src="javascript/util.js"></script>
<script type="text/javascript" src="javascript/gadgets.js"></script>
<script type="text/javascript" src="javascript/wso2-template-engine.js"></script>
<%if (userLogedIn) { %>
<script type="text/javascript"
        src="javascript/userpref-service-stub.js"></script>
<%} else if (!userLogedIn) { %>
<script type="text/javascript"
        src="javascript/userpref-service-cookie-stub.js"></script>
<%} %>
<script type="text/javascript" src="javascript/encoder-decoder.js"></script>
<script type="text/javascript"
        src="javascript/registry-based-userpref-store.js"></script>
<script type="text/javascript" src="javascript/gadget-server-utils.js"></script>
<script type="text/javascript" src="javascript/shortcuts.js"></script>
<script type="text/javascript" src="javascript/tabs.js"></script>

<script type="text/javascript" src="javascript/portlet-config.js"></script>

<script type="text/javascript" src="javascript/jquery-ui.min.js"></script>

<script type="text/javascript" src="../dialog/js/dialog.js"></script>
<%@include file="../dialog/display_messages.jsp" %>
<script type="text/javascript" src="javascript/gadgetsever.js"></script>
<script type="text/javascript">
    var backendHttpsPort = '<%=DashboardUiUtils.getHttpsPort(adminConsoleURL)%>';
    var tDomain = '<%=tenantDomain%>';
    //Number of columns in the portal
    var userId = '<%=loggeduser%>';

    // Setting browser language
    var localLanguage = '<%=request.getLocale().getLanguage()%>';

    // Setting country
    var localCountry = '<%=request.getLocale().getCountry()%>';

    // Storing the currently active tab
    var currentActiveTab = '<%=currentActiveTab%>';

    // Setting the HTTP server root to be used in Javascript
    var httpServerRoot = '<%=DashboardUiUtils.getHttpServerRoot(adminConsoleURL, dashboardUtilBean.getBackendHttpPort())%>';

    gadgetserver.gadgetSpecUrls = new Array();
    <%
         if(gadgetUrls != null) {
         for(int i=0; i<gadgetUrls.length; i++) {
             if(userLogedIn) {
         %>
    gadgetserver.gadgetSpecUrls[<%=i%>] = '<%=gadgetUrls[i].trim()%>';
    <%	} else { %>
    gadgetserver.gadgetSpecUrls[<%=i%>] = '<%=gadgetUrls[i].split(",")[1]%>';
    <%	}
             }
         }%>

    //If the tab is not valid, make it the default tab 0
    if (!dashboardService.isValidTab(userId, currentActiveTab, dashboardName)) {
        currentActiveTab = '0';
    }

    <%if(userLogedIn) {%>
    var gadgetLayoutNoSplit = '<%=DashboardUiUtils.getGadgetLayout(currentActiveTab, dashboardContentBean.getTabs())%>';
    gadgetserver.gadgetLayout = gadgetLayoutNoSplit.split(",");
    <%}else{%>
    gadgetserver.gadgetLayout = dashboardService.getGadgetLayout(userId, currentActiveTab, dashboardName).split(",");
    <%}%>

    if (gadgetserver.gadgetLayout == "NA" && dashboardService.isNewUser()) {
        // We have to populate default gadgets using the default 3-column layout
        gadgetserver.gadgetLayout = dashboardService.populateDefaultThreeColumnLayout(userId, currentActiveTab);
    }

    <%if(userLogedIn) {%>
    var tabLayoutNoSplit = '<%=dashboardContentBean.getTabLayout()%>';
    gadgetserver.tabLayout = tabLayoutNoSplit.split(",");

    var tabIdandNames = '<%=DashboardUiUtils.getTabIdsWithTitles(dashboardContentBean.getTabs())%>';
    gadgetserver.tabIdandNames = tabIdandNames.split(",");

    var gadgetIdsandPrefs = '<%=DashboardUiUtils.getGadgetIdsWithPrefs(currentActiveTab, dashboardContentBean.getTabs())%>';
    gadgetserver.gadgetIdandPrefs = gadgetIdsandPrefs.split("#");

    <%}else{%>
    gadgetserver.tabLayout = dashboardService.getTabLayout(userId, dashboardName).split(",");
    <%}%>


    function optHandler() {
        if ($('.gadgets-top-links').is(':visible')) {
            $('.gadgets-top-links').hide();

            $('#handlerBut').html('Show Options');
            $('#handlerBut').css("background-image", "url(images/show.gif)");
        } else {
            $('.gadgets-top-links').show();
            $('#handlerBut').html('Hide Options');
            $('#handlerBut').css("background-image", "url(images/hide.gif)");
        }
    }

</script>

<fmt:bundle
        basename="org.wso2.carbon.dashboard.ui.resource.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.dashboard.ui.resource.i18n.JSResources"
            request="<%=request%>"/>
    <%
        if (dashboardName != null) {
    %>
    <carbon:breadcrumb
            label="Dashboard"
            resourceBundle="org.wso2.carbon.dashboard.ui.resource.i18n.Resource"
            topPage="false"
            request="<%=request%>"/>
    <%
        }
    %>
    <div id="middle">

        <%
            if (dashboardName != null) {
        %>
        <h2><fmt:message key="dashboard"/></h2>
        <%
            }
        %>

        <div id="workArea">
            <table width="100%" height="100%" cellspacing="0">
                <tr>
                    <td style="background-color: #FFFFFF;">
                        <%if (!userLogedIn) {%>
                        <%@include file="includes/gs-gadget-top-links-anon.html" %>
                        <%} else if (userLogedIn && (dashboardName == null)) { %>
                        <%@include file="includes/gs-gadget-top-links.html" %>
                        <%} else if (userLogedIn && (dashboardName != null) && !isReadOnlyMode) { %>
                        <%if ("true".equals(hideOptions)) {%>
                        <a id="handlerBut" href="javascript:optHandler();"
                           style="background-image:url(images/show.gif);">Show Options</a>
                        <ul class="gadgets-top-links" style="display:none;">
                                    <%} else{%>
                            <ul class="gadgets-top-links">
                                <%}%>
                                <%@include file="includes/gadget-top-links-product.html" %>
                            </ul>
                                    <% } %>
                                    <%if (!userLogedIn) { %>
                            <div id="newGadgetsPane" style="display: none;">
                                <table class="defaultGadgets" width="100%">
                                    <tr>
                                        <form id="newGadgetForm" name="newGadgetForm">
                                            <%
                                                if (gadgetUrls != null) {

                                                    for (int x = 0; x < gadgetUrls.length; x = x + 4) {
                                                        out.print("<td width='250' nowrap='nowrap'>");
                                                        for (int i = x; i < 4 * ((x / 4) + 1); i++) {
                                                            if (i >= gadgetUrls.length) break;
                                                            String[] nameUrlPair = gadgetUrls[i].split(",");
                                            %>

                                            <input type="checkbox" name="checkgroup"
                                                   onclick="gadgetserver.activateAddButton()"
                                                   value="<%=nameUrlPair[1]%>"><%=nameUrlPair[0] %>
                                            </input><br/>

                                            <%

                                                        }
                                                        out.print("</td>");
                                                    }
                                                    out.print("<td width='100%'>&nbsp;</td>");
                                                }
                                            %>

                                        </form>
                                    </tr>
                                    <tr>
                                        <td colspan="4"><br/>&nbsp;<input type="button"
                                                                          id="addButton"
                                                                          name="addButton"
                                                                          class="button"
                                                                          disabled
                                                                          onclick="gadgetserver.saveNewUserGadgets()"
                                                                          value="Add">&nbsp;<input
                                                type="button"
                                                class="button"
                                                onclick="gadgetserver.cancelPane()"
                                                value="Cancel">
                                        </td>
                                    </tr>
                                </table>
                            </div>
                                    <%} %>

                                    <% if(userLogedIn){
	                            %>
                            <ul id="tabmenu">
                                <%
                                    String[] tabLayout = dashboardContentBean.getTabLayout().split(",");
                                    for (String tab : tabLayout) {
                                        String tabTitle = DashboardUiUtils.getTabTitle(tab, dashboardContentBean.getTabs());
                                        if (currentActiveTab.equals(tab)) {
                                %>
                                <li onclick="makeActive('<%=tab%>')"><a class="active"
                                                                        id="tab<%=tab%>"><%=tabTitle%>
                                </a></li>
                                <% } else {
                                %>
                                <li onclick="makeActive('<%=tab%>')"><a class=""
                                                                        id="tab<%=tab%>"><%=tabTitle%>
                                </a></li>
                                <%
                                        }
                                    }
                                %>
                            </ul>
                                    <%
	                            }else {
	                            %>
                            <script type="text/javascript">
                                document.write("<ul id=\"tabmenu\">");
                                var tabLayout = dashboardService.getTabLayout(userId, dashboardName).split(",");
                                var x = 0;
                                for (x = 0; x < tabLayout.length; x++) {
                                    var tabTitle = dashboardService.getTabTitle(userId, tabLayout[x], dashboardName);
                                    if (currentActiveTab == tabLayout[x]) {
                                        document.write("<li onclick=\"makeActive(" + tabLayout[x] + ")\"><a class=\"active\" id=\"tab" + tabLayout[x] + "\">" + tabTitle + "</a></li>");
                                    } else {
                                        document.write("<li onclick=\"makeActive(" + tabLayout[x] + ")\"><a class=\"\" id=\"tab" + tabLayout[x] + "\">" + tabTitle + "</a></li>");
                                    }
                                }
                                document.write("</ul>");
                            </script>
                                    <%}%>
                            <div id="tabContent"></div>

                    </td>
                </tr>
                <tr>
                    <td height="100%" style="background-color: #FFFFFF;">
                        <div id="maximizedGadget"
                             style="width: 100%; min-height: 100%; display: none;"></div>
                    </td>
                </tr>
                <tr>
                    <td style="background-color: #FFFFFF;"><img width="1" height="1"
                                                                src="images/1px.gif"/></td>
                </tr>
                <%
                    if (dashboardName == null) {
                        // Adding HTML wrapper tags
                %>
                <tr>
                    <td height="20">
                        <div class="footer-content">
                            <div class="copyright">&copy; 2008 - 2011 WSO2 Inc. All Rights
                                Reserved.
                            </div>
                        </div>
                    </td>
                </tr>
                <%
                    }
                %>
            </table>
        </div>
        <%
            if (dashboardName != null) {
        %>
        <script type="text/javascript">
            // This is needed in product mode to fix the bread crumb not having the correct link.
            fixBreadCrumb('<%=dashboardName%>');
        </script>
        <%
            }
        %>
    </div>
</fmt:bundle>
<%
    if (dashboardName == null) {
        // Adding HTML wrapper tags
%>
</body>
</html>
<%
    }
%>
