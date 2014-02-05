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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.theme.stub.GSThemeMgtService" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.theme.ui.GSThemeMgtClient" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.theme.stub.types.carbon.Theme" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
    String loggeduser = (String) request.getSession().getAttribute("logged-user");

    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GSThemeMgtClient client;

    client = new GSThemeMgtClient(cookie, backendServerURL, configContext, request.getLocale());

    String rootContext = configContext.getContextRoot();
    if ("/".equals(rootContext)) {
        rootContext = "";
    }

    Theme[] themes = client.getThemes(loggeduser);

    String defaultThemePath = client.getDefaultThemeForUser(loggeduser);
    if (defaultThemePath == null) {
        defaultThemePath = "";
    }
%>
<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.theme.ui.resource.i18n.Resources">
    <carbon:breadcrumb label="page.title"
                       resourceBundle="org.wso2.carbon.dashboard.mgt.theme.ui.resource.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <script type="text/javascript" src="javascript/jquery-1.3.2.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <script src="js/ajaxsbmt.js" type="text/javascript"></script>
    <script type="text/javascript">
        sessionAwareFunction();

        function rollbackTodef() {
            var resp = jQuery.ajax({
                url: 'thememgt-ajaxprocessor.jsp?func=rollback',
                async: false
            }).responseText;
            for (var i = 0; i < '<%=themes.length%>'; i++) {
                    jQuery('#but-' + i).attr("disabled", false);
                    jQuery('#theme-box-activeORnot-' + i).hide();
                    jQuery('#theme-' + i).removeClass("active");
            }
            jQuery('#but-def').attr("disabled", true);
            jQuery('#theme-def').addClass("active");
            jQuery('#theme-box-activeORnot-def').show();
        }

        function activateTheme(path, id, length) {
            var resp = jQuery.ajax({
                url: 'thememgt-ajaxprocessor.jsp?func=actTheme&themePath=' + path,
                async: false
            }).responseText;
            for (var i = 0; i < length; i++) {
                if (i == id) {
                    jQuery('#but-' + i).attr("disabled", true);
                    jQuery('#theme-box-activeORnot-' + i).show();
                } else {
                    jQuery('#but-' + i).attr("disabled", false);
                    jQuery('#theme-box-activeORnot-' + i).hide();
                    jQuery('#theme-' + i).removeClass("active");
                }
            }
            jQuery('#but-def').attr("disabled", false);
            jQuery('#theme-box-activeORnot-def').hide();

            jQuery('#theme-' + id).addClass("active");
            jQuery('#theme-def').removeClass("active");
        }

        function showAddTheme() {
            jQuery('#addTheme').attr("style", "display:block");
        }
        jQuery(document).ready(function() {

            jQuery(".toggle_container").show();
            /*Hide (Collapse) the toggle containers on load use show() insted of hide() 	in the above code if you want to keep the content section expanded. */

            jQuery("h2.trigger").click(function() {
                jQuery(this).toggleClass("active").next().slideToggle("fast");
                return false; //Prevent the browser jump to the link anchor
            });
        });
    </script>
    <link href="../theme/css/thememgt.css" rel="stylesheet" type="text/css" media="all"/>
    <!--[if IE]>
     <style>
        .theme-box-activeORnot span{
            margin-top:0px !important;
            margin-left:0px !important;
        }
    </style>
    <![endif]-->


    <div id="middle">
        <h2><fmt:message key="page.title"/></h2>

        <div id="workArea">
            <h2 class="trigger"><a href="#">Upload New Theme</a></h2>

            <div class="toggle_container" style="padding:10px;">
                <form target="_self" enctype="multipart/form-data"
                      action="../../fileupload/themeupload" id="resourceUploadForm"
                      name="resourceUploadForm" method="post"
                      onsubmit="xmlhttpPost('index.jsp', 'resourceUploadForm', 'addThemeResult', 'images/please_wait.gif'); return false;">
                    <input type="hidden" id="uResourceName" name="filename" value=""/>
                    <input type="hidden" id="uPath" name="path" value="/user-themes"/>
                    <input id="uResourceMediaType" type="hidden" name="mediaType"
                           value="application/vnd.wso2.gs.theme"/>
                    <input type="hidden" id="redirect" name="redirect" value="theme/index.jsp"/>
                    <input id="uResourceFile" type="file" name="upload"
                           onclick="uploadButton.disabled=false;"><input type="submit"
                                                                         disabled="true"
                                                                         name="uploadButton"
                                                                         value="Upload"
                                                                         id="uploadButton">
                </form>
            </div>

            <div id="addThemeResult"></div>
            <br/><br/>


            <form action="_self" method="get">
                <h3 class="default-theme-subtitle">Select Default Theme</h3>

                <div class="theme-box-title"><fmt:message key="gs.classic"/></div>
                <%if (defaultThemePath == null || "".equals(defaultThemePath)) {%>
                <div id="theme-box-activeORnot-def" class="theme-box-activeORnot"><span>active</span></div>
                <% } else { %>
                <div id="theme-box-activeORnot-def" class="theme-box-activeORnot" style="display:none;"><span>active</span></div>
                <%} %>
                <div class="theme-box <%if(defaultThemePath == null || "".equals(defaultThemePath)) {%> active<% } %>"
                     id="theme-def">
                    <table style="width:100%" class="theme-box-content">
                        <tr>
                            <td class="left-cell"><img src="images/gs-def.png" alt="Thumb"></td>
                            <td><fmt:message key="def.theme.txt"/></td>
                            <td style="width:70px"><input type="button" value="Activate" <%if (defaultThemePath == null || "".equals(defaultThemePath)) {%> disabled="disabled" <% } %>
                                       class="button" id="but-def"
                                       onclick="javascript:rollbackTodef()"></td>
                        </tr>
                    </table>
                    <div class="theme-box-comment-col">Created by: WSO2</div>
                </div>
                <% if (themes.length != 0) {
                    for (int i = 0; i < themes.length; i++) {%>
                <div class="theme-box-title"><%=themes[i].getThemeName()%>
                </div>
                <%if (defaultThemePath.contains(themes[i].getCssUrl())) {%>
                <div id="theme-box-activeORnot-<%=i%>" class="theme-box-activeORnot"><span>active</span></div>
                <% } else { %>
                <div id="theme-box-activeORnot-<%=i%>" class="theme-box-activeORnot" style="display:none;"><span>active</span></div>
                <%} %>
                <div class="theme-box <%if(defaultThemePath.contains(themes[i].getCssUrl())) {%> active<% } %>"
                     id="theme-<%=i%>">
                    <table style="width:100%" class="theme-box-content">
                        <tr>
                            <td class="left-cell"><img
                                    src="<%=rootContext%>/gs/resource/_system/config<%=themes[i].getThumbUrl()%>"
                                    alt="Thumb"></td>
                            <td><%=themes[i].getThemeDesc()%>
                            </td>
                            <td style="width:70px"><input type="button" value="Activate"
                                       class="button" id="but-<%=i%>" <%if (defaultThemePath.contains(themes[i].getCssUrl())) {%> disabled="disabled"<% } %>
                                       onclick="javascript:activateTheme('<%=themes[i].getCssUrl()%>', '<%=i%>', '<%=themes.length%>')">
                            </td>
                        </tr>
                    </table>
                    <div class="theme-box-comment-col">Created by: <%=themes[i].getThemeAuthor()%>
                    </div>
                </div>
                <%
                        }
                    }
                %>
            </form>
        </div>
    </div>
</fmt:bundle>