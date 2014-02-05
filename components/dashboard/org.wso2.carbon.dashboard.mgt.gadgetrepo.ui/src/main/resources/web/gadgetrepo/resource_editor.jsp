<!--
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
-->
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.resource.ui.clients.ResourceServiceClient" %>


<!-- other includes -->

<script type="text/javascript"
        src="../../carbon/registry_common/js/registry_validation.js"></script>
<script type="text/javascript" src="../../carbon/registry_common/js/registry_common.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>

<script type="text/javascript"
        src="../../carbon/resources/resource/js/resource_media_type_loader.js"></script>
<!-- including the JS for properties, since JS can't be loaded via async calls. -->

<script type="text/javascript" src="../../carbon/properties/js/properties.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<%
    ResourceServiceClient client;
    boolean resourceExists = false;
    boolean isRoot = false;
    String location = "";
    try {
        client = new ResourceServiceClient(config, session);
        String path = request.getParameter("path");
        if (path == null || path.equals("")) {
            path = "/";
        } else {
            location = path;
        }

        if ("/".equals(path)) {
            isRoot = true;
        }
        if (client.getResourceTreeEntry(path) != null) {
            resourceExists = true;
        }

    } catch (Exception e) {
        resourceExists = false;
    }
%>
<fmt:bundle basename="org.wso2.carbon.registry.resource.ui.i18n.Resources">
    <script type="text/javascript">
        <!--
        sessionAwareFunction(function() {
            <% if (!resourceExists) {
            //TODO: We should be able to distinguish the two scenarios below. An authorization failure
            //generates a AuthorizationFailedException which doesn't seem to arrive at this page.
            %>
            CARBON.showErrorDialog("<fmt:message key="unable.to.browse"/>", function() {
                location.href = "../admin/index.jsp";
                return;
            });
            <% } else { %>
            loadMediaTypes();
            jQuery(document).ready(loadMediaTypes);
            <% } %>
        }, "<fmt:message key="session.timed.out"/>");
        // -->

        function returnToPreviousPage(gadgetPath, cStatus) {
            if (cStatus == 'yes') {
                window.location = '../../carbon/gadgetrepo/add-gadget.jsp?mode=mod&gadgetPath=' + gadgetPath;
            }
            else {
                window.location = '../../carbon/gadgetrepo/index.jsp?region=region1&item=gadgetrepo_menu&name=governance';
            }
        }
    </script>
</fmt:bundle>
<%
    if (!resourceExists) {
        return;
    }
%>
<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
            request="<%=request%>"/>
    <%
        if ("yes".equals(request.getParameter("cstatus"))) {
    %>
    <carbon:breadcrumb label="Gadget Source"
                       resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <%
    } else {
    %>
    <carbon:breadcrumb label="Gadget Permissions"
                       resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources"
                       topPage="false" request="<%=request%>"/>

    <%
        }
    %>

    <%

        String viewType = "tree";

        if (!isRoot || "std".equals(request.getParameter("viewType"))) {
            viewType = "std";
        }

        String gadgetPath = request.getParameter("resourcePath");
        String cStatus = request.getParameter("cstatus");
    %>


    <div id="middle">

        <h2><%if ("yes".equals(request.getParameter("cstatus"))) {%><fmt:message
                key="gadget.source.editor"/><%} else {%><fmt:message key="gadget.perm.editor"/><%}%></h2>

        <div id="workArea">

            <table width="100%">

                <tr>
                    <td class="gadgets-top-links" colspan="3">
                        <a href="#" onclick="returnToPreviousPage('<%=gadgetPath%>','<%=cStatus%>')" class="icon-link"
                           style="background-image:url(../dashboard/images/return-to-dashboard.png);padding-left:30px">
                            <fmt:message key="return.to.previous.page"/></a>
                    </td>
                </tr>

                <tr>
                    <td id="resourceMain" <% if (resourceExists){ %>style="width:100%"<% } %>>

                        <div id="viewPanel">
                            <% if (viewType.equals("std")) { %>

                            <jsp:include page="StandardView_ajaxprocessor.jsp"/>

                            <% } %>
                        </div>
                    </td>

                    <td class="contraction-spacer"></td>
                    <td id="pointTds">

                    </td>

                </tr>

            </table>

        </div>
    </div>


</fmt:bundle>
