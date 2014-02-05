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
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.registry.common.utils.RegistryUtil" %>


<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<!--  script type="text/javascript" src="javascript/jquery-1.3.2.min.js"></script -->
<script type="text/javascript">
    jQuery(document).ready(function() {
        if (jQuery("#permissionsDiv")) {
            showHideCommon('perIconExpanded');
            showHideCommon('perIconMinimized');
            showHideCommon('perExpanded');
        }

        jQuery('#stdViewLink').hide();
    });
</script>

<%

    //set the tree view session
    session.setAttribute("viewType", "std");

    String contStatus = request.getParameter("cstatus");

    if (("yes".equals(contStatus))) {
%>
<div id="contentDiv" style="display:block;width:100%;">

    <jsp:include page="../resources/content_ajaxprocessor.jsp"/>

</div>
<%
} else {
%>
<div id="permissionsDiv" style="display:block;width:100%;">
    <jsp:include page="../resources/permissions_ajaxprocessor.jsp"/>
</div>

<%
    }
%>