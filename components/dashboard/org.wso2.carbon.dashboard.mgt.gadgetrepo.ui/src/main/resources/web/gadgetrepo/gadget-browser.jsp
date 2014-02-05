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
<%@ page import="org.wso2.carbon.dashboard.common.DashboardConstants" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoUiUtils" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.dashboard.ui.DashboardServiceClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%
	String selectPageString = (String) request.getParameter("selectPage");
	if (selectPageString == null || selectPageString.equals("")) {
	    selectPageString = "0";
	}

	int selectPage;
	try {
	    selectPage = Integer.parseInt(selectPageString);
	} catch (Exception e) {
	    selectPage = 0;
	}

    String activeTab = request.getParameter("tab");
    if (activeTab == null) {
        activeTab = "0";
    }
    // Adding tenant domain if available.
    String tenantDomain = (String) request.getSession().getAttribute(RegistryConstants.TENANT_DOMAIN);
    String tenantDomainWithAt = "";
    if (tenantDomain != null) {
        tenantDomainWithAt = "@" + tenantDomain;
    }

    String gadgetGrp = request.getParameter("grp");

    String loggeduser = (String) request.getSession().getAttribute("logged-user");

    if ((request.getSession().getAttribute("logged-user") == null)) {
        // We need to log in this user
        response.sendRedirect("../gsusermgt/login.jsp?from=" +
                URLEncoder.encode("../gadgetrepo/gadget-browser.jsp?tab=" + activeTab, "UTF-8"));
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

    DashboardServiceClient dashboardServiceClient = new DashboardServiceClient(cookie,
            backendServerURL,
            configContext,
            request.getLocale());

    if (!dashboardServiceClient.isSessionValid()) {
        // We need to log in this user
        response.sendRedirect("../gsusermgt/login.jsp?from=" +
                URLEncoder.encode("../gadgetrepo/gadget-browser.jsp?tab=" + activeTab, "UTF-8"));
        return;
    }

    String pageNumStr = request.getParameter("pageNum");

    int pages = 0;

    if (gadgetRepoServiceClient.getCollectionSize() != null) {

        pages = (gadgetRepoServiceClient.getCollectionSize() - 1) / DashboardConstants.GADGETS_PER_PAGE;


        if (((gadgetRepoServiceClient.getCollectionSize() - 1) % DashboardConstants.GADGETS_PER_PAGE) != 0) {
            pages++;
        }
    }

    String portalCSS = "";
    try {
        portalCSS = GadgetRepoUiUtils.getPortalCss(cookie, backendServerURL, configContext, request.getLocale(), request, config);
    } catch (Exception ignore) {

    }

    // to obtain the portal url with the correct context
    /*String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
    int index = adminConsoleURL.lastIndexOf("carbon");
    String portalURL = adminConsoleURL.substring(0, index);
    while (portalURL.lastIndexOf("carbon") > 0) {
        portalURL = portalURL.substring(0, portalURL.lastIndexOf("carbon"));
    }
    portalURL = portalURL + "carbon/dashboard/index.jsp";*/

    String portalURL = "../../carbon/dashboard/index.jsp";

%>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
    <meta http-equiv="content-type" content="text/html;charset=utf-8"/>
    <title>WSO2 Gadget Server</title>
    <link href="../admin/css/global.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="../admin/css/main.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="../admin/css/gadgets.css" rel="stylesheet" type="text/css"/>
    <link href="css/gadget-browser.css" rel="stylesheet" type="text/css"/>
    <link href="../dialog/css/jqueryui/jqueryui-themeroller.css" rel="stylesheet" type="text/css"
          media="all"/>
    <link href="../dialog/css/dialog.css" rel="stylesheet" type="text/css" media="all"/>
    <link href="<%=portalCSS%>" rel="stylesheet"
          type="text/css" media="all"/>
    <link href="../dashboard/localstyles/gadgets.css" rel="stylesheet" type="text/css"/>
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

    <script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
    <script type="text/javascript" src="javascript/gadgetrepo-server-utils.js"></script>
    <script type="text/javascript" src="javascript/gadgetrepo-service-stub.js"></script>
    <script type="text/javascript" src="javascript/util.js"></script>

    <!--// rating plugin-specific resources //-->
    <script src="javascript/rating/jquery.form.js" type="text/javascript"
            language="javascript"></script>
    <script src="javascript/rating/jquery.MetaData.js" type="text/javascript"
            language="javascript"></script>
    <script src="javascript/rating/jquery.rating.js" type="text/javascript"
            language="javascript"></script>
    <link href="javascript/rating/jquery.rating.css" type="text/css" rel="stylesheet"/>

    <script type="text/javascript" src="../dialog/js/dialog.js"></script>
    <%--Includes for default text on text box--%>
     <script src="../yui/build/yahoo-dom-event/yahoo-dom-event.js" type="text/javascript"></script>
    <script type="text/javascript" src="../admin/js/widgets.js"></script>

    <script type="text/javascript">
        var userId = '<%=loggeduser%>';
        <%if(pageNumStr == null){%>
        $(document).ready(function() {
            // load home page when the page loads
            $("#repo-browser").load("items-ajaxprocessor.jsp?grp=<%=gadgetGrp%>&tab=<%=activeTab%>&pageNum=" + <%=selectPage%>);
        });
        <%}%>

        function addGadgetToDashboard(id) {
            var elementId = "gadgetUrlFromRepo-" + id;
            var gadgetUrl = document.getElementById(elementId).value;

            var pathElementId = "gadgetPath-" + id;
            var gadgetPath = document.getElementById(pathElementId).value;

            if ((gadgetUrl == "") || (gadgetUrl.length == 0)) {
                CARBON.showErrorDialog(jsi18n["please.enter.location"]);
            } else if (gadgetUrl.indexOf("https://") == 0) {
                CARBON.showErrorDialog(jsi18n["block.https"]);
            } else {
                if (checkSessionInPortal()) {
                    var resp = GadgetRepoService.addGadget(userId, <%=activeTab%>, gadgetUrl, null, gadgetPath, '<%=gadgetGrp%>#');
                    if (!resp) {
                        CARBON.showErrorDialog(jsi18n["no.permission"]);
                    } else {
                        window.location = '<%=portalURL%>?tab=<%=activeTab%>';
                    }
                }

            }
        }

        function addGadget() {

            var gadgetUrl = document.getElementById('gadgetUrl').value;

            if ((gadgetUrl == "") || (gadgetUrl.length == 0)) {

                CARBON.showErrorDialog(jsi18n["please.enter.location"]);

            } else if (gadgetUrl.indexOf("https://") == 0) {
                CARBON.showErrorDialog(jsi18n["block.https"]);
            } else {
                 if(checkSessionInPortal) {
                GadgetRepoService.addGadget(userId, <%=activeTab%>, gadgetUrl, null, null, '<%=gadgetGrp%>#');
                window.location = '<%=portalURL%>?tab=<%=activeTab%>';
            }}
        }


        function returnToDashboard() {

            window.location = '<%=portalURL%>?tab=<%=activeTab%>';

        }

        function paginate(num) {
            $(document).ready(function() {
                // load home page when the page loads
                $("#repo-browser").load("items-ajaxprocessor.jsp?grp=<%=gadgetGrp%>&pageNum=" + num);
            });
        }

        function searchGad() {
            var search=document.getElementById("gadgetSearch").value;
            if(search=='')  {
                window.location.reload();
            }
           jQuery('#pag').attr("style", "display:none");
           jQuery('#pag1').attr("style", "display:none");
           jQuery('#repoLink').attr("style", "display:block");
            $(document).ready(function() {
             $("#repo-browser").load("items-ajaxprocessor.jsp?grp=<%=gadgetGrp%>&pageNum=" + 0 +"&search="+search);
              });

        }

    function clear(){
        var theDiv = document.getElementById(gadgetSearch);
        theDiv.value = '';
        history.go();

    }

        jQuery(document).ready(function() {
            //Hide (Collapse) the toggle containers on load
            jQuery(".toggle_container").hide();
            jQuery("h2.trigger").click(function() {
                jQuery(this).toggleClass("active").next().slideToggle("fast");
                return false; //Prevent the browser jump to the link anchor
            });
        });

        enableDefaultText("gadgetSearch");
        enableDefaultText("gadgetUrl");
    </script>

</head>

<body>
<div id="dcontainer"></div>
<div id="link-panel">
    <div class="right-links">
        <ul>
            <li><strong>Signed-in as&nbsp;<%=loggeduser+tenantDomainWithAt%>
            </strong></li>
            <li>|</li>
            <!--li>
                <a href="javaScript:redirectToHttpsUrl('../admin/logout_action.jsp?IndexPageURL=/carbon/gsusermgt/middle.jsp', '<%=GadgetRepoUiUtils.getHttpsPort(backendServerURL)%>')">Sign-out</a>
            </li-->
            <li>
                <a href="../admin/logout_action.jsp">Sign-out</a>
            </li>
        </ul>
    </div>
    <div class="left-logo"><a class="header-home" href="<%=portalURL%>">
        <img width="179" height="28" src="images/1px.gif"/> </a></div>
</div>

<fmt:bundle basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
            request="<%=request%>"/>
    <div id="middle">
        <div id="workArea">

            <table width="100%" height="100%" cellspacing="0">
                <tr>
                    <td class="gadgets-top-links">
                        <a href="#" onclick="returnToDashboard()" class="icon-link" style="background-image:url(../dashboard/images/return-to-dashboard.png)"><fmt:message
                                    key="return.to.dashboard"/></a>
                    </td>
                </tr>


                <tr>
                    <td style="padding:10px;">
                        <h2>Add Gadget to Dashboard</h2>
                        <table class="gadget-repo-table">
                            <tr>
                                <td class="gadget-ropo-cell-1">
                                    <h3 class="gadget-subtitle"><fmt:message key="gadget.repository" /></h3>
                                    <input type="text" size="20" name="gadgetSearch"
                                           id="gadgetSearch" alt="Search Gadget Repository"
                                            onkeydown="if (event.keyCode == 13) document.getElementById('searchGadget').click()" />  <input type="button" class="button" onclick="searchGad()" value="Search" id="searchGadget"  />

                                </td>
                                <td class="gadget-ropo-cell-2">
                                    <%
                                        if (gadgetRepoServiceClient.isExternalGadgetAdditionEnabled()) {
                                    %>
                                    <div class="gadget-ropo-cell-2-inside">
                                        <h3 class="gadget-subtitle">Add gadget from URL</h3>

                                        <h2 class="active trigger"><a href="#"><fmt:message
                                                key="enter.gadget.location"/></a></h2>

                                        <div class="toggle_container" style="margin-bottom:10px;padding:10px;">

                                            <input type="text" size="30" name="gadgetUrl"
                                                   onclick="checkSessionInPortal()" alt="<fmt:message key="enter.url"/>"
                                                   id="gadgetUrl" value=""/>

                                            <input type="button" onclick="addGadget()"
                                                   value="<fmt:message key="add.gadget.button"/>"
                                                   class="button" id="addGadget"/>

                                        </div>
                                    </div>
                                    <%
                                        }
                                    %>
                                </td>
                            </tr>
                            <tr><td colspan="2" class="gadget-ropo-cell-3">
								<% String selectedTab = "tab=" + activeTab;%>

                                <div id="pag" class="paginator"><carbon:paginator pageNumber="<%=selectPage%>" numberOfPages="<%=pages%>"
                                                        page="gadget-browser.jsp" parameters="<%=selectedTab%>"
                                                        pageNumberParameterName="selectPage"/></div>

                                <div id="repoLink" align="right" style="display:none;"> <a href="javascript: window.location.reload()">Back To Full View Of Gadget Repository </a></div> <br/>
                                 <div style="clear:both"></div>

                                 <div id="repo-browser">
                            <!-- Loads data useing ajax (items-ajaxprocessor.jsp) --></div>

                                <div id="pag1" class="paginator"><carbon:paginator pageNumber="<%=selectPage%>" numberOfPages="<%=pages%>"
                                                        page="gadget-browser.jsp" parameters="<%=selectedTab%>"
                                                        pageNumberParameterName="selectPage"/></div>

                            </td>
                            </tr>
                        </table>




                    </td>
                </tr>
                <tr>
                    <td height="20">
                        <div class="footer-content">
                            <div class="copyright">&copy; 2008 - 2011 WSO2 Inc. All Rights
                                Reserved.
                            </div>
                        </div>
                    </td>
                </tr>

            </table>
        </div>

    </div>
    </body>
    </html>
</fmt:bundle>
