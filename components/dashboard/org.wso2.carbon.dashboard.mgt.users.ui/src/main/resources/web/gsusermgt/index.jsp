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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.users.ui.GadgetServerUserManagementServiceClient" %>

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


    String pageAction = request.getParameter("action");
    {
        if ((pageAction != null) && ("submit".equals(pageAction))) {
            // Submit data to the backend
            String selfRegFlag = request.getParameter("allowSelfReg");
            if (selfRegFlag != null) {
                gsUserMgtServiceClient.setUserSelfRegistration(true);
            } else {
                gsUserMgtServiceClient.setUserSelfRegistration(false);
            }

            String allowExternalGadgetsFlag = request.getParameter("allowExternalGadgets");
            if (allowExternalGadgetsFlag != null) {
                gsUserMgtServiceClient.setUserExternalGadgetAddition(true);
            } else {
                gsUserMgtServiceClient.setUserExternalGadgetAddition(false);
            }

        }
    }


%>
<script type="text/javascript" src="javascript/check-session.js"></script>

<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.users.ui.resource.i18n.Resources">
    <%--<carbon:jsi18n
   resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
   request="<%=request%>" />--%>
    <carbon:breadcrumb label="page.title"
                       resourceBundle="org.wso2.carbon.dashboard.mgt.users.ui.resource.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <div id="middle">
        <h2><fmt:message key="page.title"/></h2>

        <div id="workArea">
            <form action="index.jsp" method="get">

                <input type="hidden" name="action" value="submit"/>

                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2">Add/Remove Portal Permissions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="formRow">
                            <table class="normal">
                                <tbody>
                                <tr>
                                    <td>Allow user self registration</td>
                                    <td><input type="checkbox"
                                               name="allowSelfReg" onclick="checkSession()"
                                               <% if(gsUserMgtServiceClient.isSelfRegistration(null)){%>checked<%}%>/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Allow users to add external Gadgets</td>
                                    <td><input type="checkbox"
                                               name="allowExternalGadgets" onclick="checkSession()"
                                               <%if(gsUserMgtServiceClient.isExternalGadgetAddition()){%>checked<%}%>/>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                    <tr>
                        <td class="buttonRow">
                            <input type="submit" value="Update" class="button"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</fmt:bundle>