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

    String defaultThemePath = client.getDefaultThemeForUser(loggeduser);
    if (defaultThemePath == null) {
        defaultThemePath = "";
    }
%>
<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.theme.ui.resource.i18n.Resources">
    <carbon:breadcrumb label="page.title.upload"
                       resourceBundle="org.wso2.carbon.dashboard.mgt.theme.ui.resource.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <script type="text/javascript" src="javascript/jquery-1.3.2.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <script type="text/javascript">
        sessionAwareFunction();
    </script>

    <div id="middle">
        <h2><fmt:message key="page.title.upload"/></h2>

        <div id="workArea">

            <table class="styledLeft">
                <thead>
                <tr>
                    <th colspan="2">Upload New Theme</th>
                </tr>
                </thead>
                <tbody>
                <form onsubmit="true" target="_self" enctype="multipart/form-data" action="../../fileupload/themeupload" id="resourceUploadForm" name="resourceUploadForm" method="post">
                    <input type="hidden" id="uResourceName" name="filename" value=""/>    
                    <input type="hidden" id="uPath" name="path" value="/user-themes"/>
                        <input id="uResourceMediaType" type="hidden" name="mediaType" value="application/vnd.wso2.gs.theme"/>
                        <input type="hidden" id="redirect" name="redirect" value="theme/index.jsp"/>
                    <tr>
                        <td><input id="uResourceFile" type="file" name="upload" onclick="uploadButton.disabled=false;"><input type="submit" disabled="true" name="uploadButton" value="Upload" id="uploadButton"></td>
                    </tr>
                </form>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>