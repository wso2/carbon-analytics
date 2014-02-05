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
<%@page
        import="org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Gadget" %>
<%@ page import="org.wso2.carbon.dashboard.common.DashboardConstants" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoUiUtils" %>

<%

    String loggeduser = (String) request.getSession().getAttribute("logged-user");

    String gadgetGrp = request.getParameter("grp");

    if ((request.getSession().getAttribute("logged-user") == null)) {
        // We need to log in this user
        response.sendRedirect("../gsusermgt/login.jsp");
        return;
    }

    String backendServerURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().getAttribute(
            CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetRepoServiceClient gadgetRepoServiceClient = new GadgetRepoServiceClient(cookie,
            backendServerURL,
            configContext,
            request.getLocale());

    String gadgetPath = request.getParameter("gadgetPath");

    if (gadgetPath == null) {
        response.sendRedirect("../gsusermgt/login.jsp");
        return;
    }

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

    // to obtain the portal url with the correct context
    /*String adminConsoleURL = CarbonUIUtil.getAdminConsoleURL(request);
    int index = adminConsoleURL.lastIndexOf("carbon");
    String portalURL = adminConsoleURL.substring(0, index);
    while (portalURL.lastIndexOf("carbon") > 0) {
        portalURL = portalURL.substring(0, portalURL.lastIndexOf("carbon"));
    }
    portalURL = portalURL + "carbon/dashboard/index.jsp";*/

    String portalURL = "../../carbon/dashboard/index.jsp";

    if (gadget == null || commentCount == null) {
        response.sendRedirect("../gsusermgt/login.jsp");
        return;
    }

    String portalCSS = "";
    try {
        portalCSS = GadgetRepoUiUtils.getPortalCss(cookie, backendServerURL, configContext, request.getLocale(), request, config);
    } catch (Exception ignore) {

    }

%>
<html xmlns="http://www.w3.org/1999/xhtml">

<head>
<meta http-equiv="content-type" content="text/html;charset=utf-8"/>
<title>WSO2 Gadget Server</title>
<link href="../admin/css/global.css" rel="stylesheet" type="text/css" media="all"/>
<link href="../admin/css/main.css" rel="stylesheet" type="text/css" media="all"/>
<link rel="stylesheet" type="text/css" href="../admin/css/gadgets.css"/>
<link rel="stylesheet" type="text/css" href="css/gadget-browser.css"/>
<link href="../dialog/css/jqueryui/jqueryui-themeroller.css" rel="stylesheet" type="text/css"
      media="all"/>
<link href="../dialog/css/dialog.css" rel="stylesheet" type="text/css" media="all"/>
<link href="<%=portalCSS%>" rel="stylesheet"
      type="text/css" media="all"/>
<link rel="stylesheet" type="text/css" href="../dashboard/localstyles/gadgets.css"/>
<link rel="icon" href="../admin/images/favicon.ico" type="image/x-icon"/>
<link rel="shortcut icon" href="../admin/images/favicon.ico" type="image/x-icon"/>

<script type="text/javascript" src="javascript/jquery-latest.js"></script>
<script src="javascript/jquery-1.2.6.min.js" type="text/javascript"></script>
<script src="javascript/jquery-ui-1.6.custom.min.js" type="text/javascript"></script>
<script src="javascript/gadgetrepo-service-stub.js" type="text/javascript"></script>
<script src="javascript/gadgetrepo-server-utils.js" type="text/javascript"></script>

<!--// rating plugin-specific resources //-->
<script src="javascript/rating/jquery.form.js" type="text/javascript"
        language="javascript"></script>
<script src="javascript/rating/jquery.MetaData.js" type="text/javascript"
        language="javascript"></script>
<script src="javascript/rating/jquery.rating.js" type="text/javascript"
        language="javascript"></script>
<link href="javascript/rating/jquery.rating.css" type="text/css" rel="stylesheet"/>
<script src="javascript/jquery.counter.min.js" type="text/javascript"
        language="javascript"></script>
<script type="text/javascript" src="../dialog/js/dialog.js"></script>
<script type="text/javascript" src="javascript/util.js"></script>

<fmt:bundle basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
        request="<%=request%>"/>

<script type="text/javascript">

    var userId = '<%=loggeduser%>';
    var curPage = 0;

    $(function() {
        $('.auto-submit-star').rating({
            callback: function(value, link) {
                if (value == null || value == '') {
                    value = '0'
                }
                rateGadget(value);
            }
        });
    });

    function rateGadget(rateValue) {
        if (checkSessionInPortal()) {
            var res = GadgetRepoService.rateGadget('<%=gadgetPath%>', rateValue, '<%=activeTab%>', '<%=gadgetGrp%>');
            if (!res) {
                CARBON.showErrorDialog(jsi18n["time.out"], null, null);
            }
        }
    }
    function doComment() {
        if (checkSessionInPortal()) {
            $('#writeComment').fadeIn('slow');
        }
    }

    function saveComment() {
        var commentText = document.getElementById('commentTextArea').value;

        if (commentText == '') {
            CARBON.showErrorDialog(jsi18n["enter.comment"], null, null);
            return;
        }

        $('#writeComment').fadeOut('slow');

        if (commentText != null && commentText != "") {

            GadgetRepoService.addComment('<%=gadgetPath%>', commentText, '<%=loggeduser%>', '<%=activeTab%>', '<%=gadgetGrp%>');

        }

        document.getElementById('commentTextArea').value = '';

        $('#charlimitinfo').html('<fmt:message key="textArea.comment.limit"/>');

        $("#pagNums").load(location.href + " #pagNums>*", "");
        $("#comments-table").load("GadgetPage-ajaxprocessor.jsp?pageNumCom=" + curPage + "&gadgetPath=" + '<%=gadgetPath%>');
    }

    function deleteComment(id) {
        var commentId = document.getElementById('comment-' + id).value;
        if (commentId != null && commentId != "") {
            GadgetRepoService.deleteComment(commentId);
        }

        $("#pagNums").load(location.href + " #pagNums>*", "");
        if ($('.commentCls').length == 1) {
            curPage = curPage - 1;
        }
        $("#comments-table").load("GadgetPage-ajaxprocessor.jsp?pageNumCom=" + curPage + "&gadgetPath=" + '<%=gadgetPath%>');
    }

    function getCheckedValue(radioObj) {
        if (!radioObj)
            return "";
        var radioLength = radioObj.length;
        if (radioLength == undefined)
            if (radioObj.checked)
                return radioObj.value;
            else
                return "";
        for (var i = 0; i < radioLength; i++) {
            if (radioObj[i].checked) {
                return radioObj[i].value;
            }
        }
        return "";
    }

    function addGadget() {
        var gadgetUrl = document.getElementById('gadgetUrl').value;

        if ((gadgetUrl == "") || (gadgetUrl.length == 0)) {
            CARBON.showErrorDialog(jsi18n["please.enter.location"]);
        } else {
            if (checkSessionInPortal()) {
                var res = GadgetRepoService.addGadget(userId, <%=activeTab%>, gadgetUrl, null, '<%=gadgetPath%>', '<%=gadgetGrp%>#');
                if (!res) {
                    CARBON.showErrorDialog(jsi18n["time.out"], null, null);
                } else {
                    window.location = '<%=portalURL%>?tab=<%=activeTab%>';
                }
            }
        }
    }

    function returnToDashboard() {
        window.location = '<%=portalURL%>?tab=<%=activeTab%>';
    }

    function getItem(id) {
        var itm = false;
        if (document.getElementById)
            itm = document.getElementById(id);
        else if (document.all)
            itm = document.all[id];
        else if (document.layers)
            itm = document.layers[id];

        return itm;
    }

    function toggleItem(id) {
        if (checkSessionInPortal()) {
            $('#commentTextArea')[0].value = '';
            itm = getItem(id);

            if (!itm)
                return false;

            if (itm.style.display == 'none')
                itm.style.display = '';
            else
                itm.style.display = 'none';

            return false;
        }
    }

    function paginate(num) {
        $(document).ready(function() {
            curPage = num;
            // load home page when the page loads
            $("#comments-table").load("GadgetPage-ajaxprocessor.jsp?pageNumCom=" + num + "&gadgetPath=" + '<%=gadgetPath%>');
        });
    }

    <%if(pageNumComStr == null){%>
    $(document).ready(function() {
        // load home page when the page loads
        $("#comments-table").load("GadgetPage-ajaxprocessor.jsp?pageNumCom=" + 0 + "&gadgetPath=" + '<%=gadgetPath%>');
    });
    <%}%>

    $(function() {
        $('#commentTextArea').keyup(function() {
            limitChars('commentTextArea', 200, 'charlimitinfo');
        })

        $('#commentTextArea').bind('paste', function() {
            setTimeout('limitChars(\'commentTextArea\', 200, \'charlimitinfo\')', 100);
        })
    });
    jQuery(document).ready(function() {
            //Hide (Collapse) the toggle containers on load
            jQuery(".toggle_container").show();
            jQuery("h2.trigger").click(function() {
                jQuery(this).toggleClass("active").next().slideToggle("fast");
                return false; //Prevent the browser jump to the link anchor
            });
        });

</script>
</head>

<body>
<div id="dcontainer"></div>
<div id="link-panel">
    <div class="right-links">
        <ul>
            <li><strong>Signed-in as&nbsp;<%=loggeduser%>
            </strong></li>
            <li>|</li>
<%--            <li>
                <a href="javaScript:redirectToHttpsUrl('../admin/logout_action.jsp?IndexPageURL=/carbon/gsusermgt/middle.jsp', '<%=GadgetRepoUiUtils.getHttpsPort(backendServerURL)%>')">Sign-out</a>
            </li>--%>
            <li>
                <a href="../admin/logout_action.jsp">Sign-out</a>
            </li>
        </ul>
    </div>
    <div class="left-logo"><a class="header-home" href="<%=portalURL%>">
        <img width="179" height="28" src="images/1px.gif"/> </a></div>
</div>

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
                <td height="100%" style="background-color: #FFFFFF;padding:0px 10px;">
                    <div class="gadgetInfo">
                        <table cellspacing="0">
                            <tr>
                                <td class="gadgetImage">
                                    <%
                                        if (gadget != null && gadget.getThumbUrl() != null
                                            && !gadget.getThumbUrl().equals("")) {
                                            String rootContext = configContext.getContextRoot();
                                            if ("/".equals(rootContext)) {
                                                rootContext = "";
                                            }
                                    %>

                                    <div id="imageIco"
                                         align="center">
                                        <img style="" id="imageIcon"
                                             src="<%=rootContext + gadget.getThumbUrl()%>"
                                             width="200px" height="200px"/>
                                    </div>
                                    <%
                                    } else {
                                    %> <img src="images/no-image-large.jpg" width="200px"
                                            height="200px"/>
                                    <%
                                        }
                                    %>

                                </td>
                                <td class="gadgetDescriptionArea" width="100%">
                                    <div class="GadgetHeading"><%=gadget.getGadgetName()%></div>
                                    <div class="GadgetDescription"><%=gadget.getGadgetDesc()%>
                                        <br/><br/>

                                        <div class="rating-text"><%
                                            if (gadget.getUserCount() != null) {
                                                out.print(gadget.getUserCount());
                                            } else {
                                                out.print("0");
                                            }
                                        %> users |
                                        </div>

                                        <input type="radio" class="star" name="avRating"
                                               disabled="disabled"
                                               value="1"
                                               <%if((gadget.getRating() >= 1) && (gadget.getRating() < 2)){%>checked="checked"<%} %>/>
                                        <input type="radio" class="star" name="avRating"
                                               disabled="disabled"
                                               value="2"
                                               <%if((gadget.getRating() >= 2) && (gadget.getRating() < 3)){%>checked="checked"<%} %> />
                                        <input type="radio" class="star" name="avRating"
                                               disabled="disabled"
                                               value="3"
                                               <%if((gadget.getRating() >= 3) && (gadget.getRating() < 4)){%>checked="checked"<%} %> />
                                        <input type="radio" class="star" name="avRating"
                                               disabled="disabled"
                                               value="4"
                                               <%if((gadget.getRating() >= 4) && (gadget.getRating() < 5)){%>checked="checked"<%} %> />
                                        <input type="radio" class="star" name="avRating"
                                               disabled="disabled"
                                               value="5"
                                               <%if(gadget.getRating() == 5){%>checked="checked"<%} %> />

                                        <br/>

                                        <div class="rating-text">My Rating:</div>
                                        <input type="radio" id="1" class="auto-submit-star"
                                               name="rating" value="1"
                                               <%if(userRating.equals("1")){%>checked="checked"<%} %> />
                                        <input type="radio" id="2" class="auto-submit-star"
                                               name="rating" value="2"
                                               <%if(userRating.equals("2")){%>checked="checked"<%} %> />
                                        <input type="radio" id="3" class="auto-submit-star"
                                               name="rating" value="3"
                                               <%if(userRating.equals("3")){%>checked="checked"<%} %> />
                                        <input type="radio" id="4" class="auto-submit-star"
                                               name="rating" value="4"
                                               <%if(userRating.equals("4")){%>checked="checked"<%} %> />
                                        <input type="radio" id="5" class="auto-submit-star"
                                               name="rating" value="5"
                                               <%if(userRating.equals("5")){%>checked="checked"<%} %> />

                                        <input type="hidden" id="gadgetUrl"
                                               value="<%=gadget.getGadgetUrl()%>"/>

                                        <br/><br/>
                                        <label>
                                            <input type="button" onclick="addGadget()"
                                                   value="<fmt:message key="add.gadget.button"/>"
                                                   class="button"
                                                   id="addGadget"/>
                                        </label>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>
                    <a class="icon-link" onclick="doComment()"
                                   style="background-image: url(../admin/images/add.gif);margin:0px 0px 10px 0px">Write a
                                Comment</a>
                    <div style="clear:both"></div>
                    <div id="writeComment" style="display:none;margin-bottom:10px;">
                        <h2 class="active trigger"><a href="#">You may complete the below information.</a></h2>

                        <div class="toggle_container" style="margin-bottom:10px;padding:10px;">
                            <div class="form-item">
                                <label for="comments"><fmt:message
                                        key="textArea.comment"/></label>
                                <textarea cols="60" rows="5" id="commentTextArea"
                                          onclick="checkSessionInPortal()"></textarea>
                                                    <div id="charlimitinfo"><fmt:message
                                                            key="textArea.comment.limit"/></div>
                            </div>
                            <br/>
                            <input type="button" id="commentButton" class="button"
                                   value="<fmt:message key="post.comment" />"
                                   onclick="saveComment()"/>
                            <input id="cancel" class="button" type="button"
                                   onclick="toggleItem('writeComment')"
                                   value="Cancel"/>
                        </div>
                    </div>
                    <h2 class="active trigger"><a href="#">Comments</a></h2>
                    <div class="toggle_container" style="margin-bottom:10px;padding:10px;">
                        <div id="comments-table"></div>
                        <div id="pagNums" class="pagination"><% if (pages > 1) {
                                    for (int i = 0; i < pages; i++) {
                                        out.print("<a href=\"javascript:paginate(" + (i) + ");\" class=\"pageLinks\" >" + (i + 1) + "</a> ");
                                    }
                                } %></div>
                    </div>


                </td>
            </tr>
            <tr>
                <td height="20">
                    <div class="footer-content">
                        <div class="copyright">&copy; 2008 - 2010 WSO2 Inc. All Rights Reserved.
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