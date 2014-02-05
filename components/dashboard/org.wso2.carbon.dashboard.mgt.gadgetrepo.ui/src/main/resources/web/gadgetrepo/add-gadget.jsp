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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>

<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Gadget" %>
<%@ page import="org.apache.axiom.om.util.Base64" %>
<%@ page import="java.util.ResourceBundle" %>

<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config
                                                                .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetRepoServiceClient gadgetRepoServiceClient = new
            GadgetRepoServiceClient(cookie, backendServerURL,configContext,request.getLocale());

    String param = request.getParameter("mode");
    String gadgetPath = request.getParameter("gadgetPath");
    String uploadedGadgetPath = request.getParameter("ugPath");

    String errMsg = request.getParameter("msg");

    Gadget result = null;
    if ("mod".equals(param)) {
        result = gadgetRepoServiceClient.getGadget(gadgetPath);
    }

    if (uploadedGadgetPath == null || "".equals(uploadedGadgetPath)) {
        uploadedGadgetPath = "";
    }

    String registryURL =
            "../../carbon/gadgetrepo/resource_editor.jsp?cstatus=yes&viewType=std&resourcePath="
            + gadgetPath + "&path=";

%>

<script type="text/javascript" src="../yui/build/yahoo/yahoo-min.js"></script>
<script type="text/javascript" src="../yui/build/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="../yui/build/connection/connection-min.js"></script>
<script type="text/javascript" src="javascript/gadgetrepo-server-utils.js"></script>
<script type="text/javascript" src="javascript/gadgetrepo-service-stub.js"></script>
<script type="text/javascript" src="javascript/jquery-1.3.2.min.js"></script>
<script type="text/javascript" src="javascript/jquery-ui-1.6.custom.min.js"></script>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<script type="text/javascript" src="../resources/js/registry-browser.js"></script>
<script type="text/javascript" src="../admin/js/main.js"></script>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>

<script type="text/javascript">
    <%if(errMsg != null){%>
    CARBON.showErrorDialog('<%=errMsg%>');
    <% } %>

    function validateForm(opt) {
        var gadgetURLType = document.getElementById("gadgetURLType" + opt);
        var type = gadgetURLType.value;

        var gadgetURL = document.getElementById("gadgetUrl");
        var gadgetURLValue = gadgetURL.value;

        if ("RegistryBrowser" == type) {
            //set gadgetUrl
            // this val.replace is simply doing the trim() operation (trim not support IE)
            jQuery('#gadgetUrl').val(jQuery('#gadgetRegistryFile').val().replace(/^\s+|\s+$/g, ''));
        }
        //else do nothing, gadgetUrl already set with required values

        if (jQuery('#gadgetName').val().replace(/^\s+|\s+$/g, '') == "") {
            CARBON.showErrorDialog('Please insert a name for the gadget');
        } else if (jQuery('#gadgetUrl').val().replace(/^\s+|\s+$/g, '') == "" &&
                   jQuery('#gadget').val().replace(/^\s+|\s+$/g, '') == "" &&
                   jQuery('#gadgetRegistryFile').val().replace(/^\s+|\s+$/g, '') == "") {
            CARBON.showErrorDialog('Please insert the Gadget URL/ Gadget File for the gadget');
        } else if (jQuery('#gadgetDesc').val().replace(/^\s+|\s+$/g, '') == "") {
            CARBON.showErrorDialog('Please insert a description for the gadget');
        } else if ('GadgetURL' == type && gadgetURLValue.substring(0, 4) != "http") {
            CARBON.showErrorDialog('Please insert a valid URL for the gadget URL');
        } else {
            jQuery('#gadgetForm').submit();
        }
    }

    function cancelForm() {
        location.href = 'index.jsp?region=region1&item=gadgetrepo_menu&name=governance';
    }

    function setType(opt) {
        var gadgetUrlTR = document.getElementById("gadgetUrlTR");
        var fileBrowserTR = document.getElementById("fileBrowserTR");
        var registryBrowserTR = document.getElementById("registryBrowserTR");
        var gadgetScreenTR = document.getElementById("gadgetScreenTR");
        var gadgetURLType = document.getElementById("gadgetURLType" + opt);
        var type = null;
        if (gadgetUrlTR != null && fileBrowserTR != null && registryBrowserTR != null && gadgetScreenTR != null) {
            type = gadgetURLType.value;

            if ('GadgetURL' == type) {
                gadgetURLType.value = "GadgetURL";
                jQuery('#gadgetUrlTR').show();
                jQuery('#fileBrowserTR').hide();
                jQuery('#registryBrowserTR').hide();
                jQuery('#gadgetScreenTR').show();

                jQuery('#gadget').val("");
                jQuery('#gadgetRegistryFile').val("");

            } else if ("FileBrowser" == type) {
                gadgetURLType.value = "FileBrowser";
                jQuery('#gadgetUrlTR').hide();
                jQuery('#fileBrowserTR').show();
                jQuery('#registryBrowserTR').hide();
                jQuery('#gadgetScreenTR').show();

                jQuery('#gadgetUrl').val("");
                jQuery('#gadgetRegistryFile').val("");

            } else if ("RegistryBrowser" == type) {
                gadgetURLType.value = "RegistryBrowser";
                jQuery('#gadgetUrlTR').hide();
                jQuery('#fileBrowserTR').hide();
                jQuery('#registryBrowserTR').show();
                jQuery('#gadgetScreenTR').show();

                jQuery('#gadgetUrl').val("");
                jQuery('#gadget').val("");
            }
            else if("ZipFileBrowser" == type){
                gadgetURLType.value = "FileBrowser";
                jQuery('#gadgetUrlTR').hide();
                jQuery('#fileBrowserTR').show();
                jQuery('#registryBrowserTR').hide();
                jQuery('#gadgetScreenTR').hide();

                jQuery('#gadgetUrl').val("");
                jQuery('#gadgetRegistryFile').val("");

            }
        }
        return true;
    }

</script>

<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
<carbon:jsi18n
        resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
        request="<%=request%>"/>
<carbon:breadcrumb label="Add / Modify Gadget"
                   resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources"
                   topPage="false" request="<%=request%>"/>

<%
    if ("add".equals(param)) {
%>
<div id="middle">
    <h2>
        <fmt:message key="add.gadget.header"/>
    </h2>

    <div id="workArea">

        <form action="../../fileupload/gadgetupload" enctype="multipart/form-data"
              method="post" id="gadgetForm">

            <input type="hidden" id="uMode" name="mode" value="<%=param%>"/>
            <input id="uResourceMediaType" type="hidden" name="mediaType"
                   value="application/vnd.wso2.gs.gadget"/>
            <input type="hidden" id="redirect" name="redirect" value="gadgetrepo/index.jsp"/>
            <input type="hidden" id="errorRedirect" name="errorRedirect"
                   value="gadgetrepo/add-gadget.jsp"/>

            <table cellpadding="0" cellspacing="0" border="0" class="styledLeft noBorders">
                <thead>
                <tr>
                    <th colspan="2"><fmt:message key="new.gadget.info"/></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td style="padding-bottom:10px;">

                        <table style="width:100%;">
                            <tr>
                                <td width="150px"><label><fmt:message key="gadget.name"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td width="650px"><input type="text" name="gadgetName"
                                                         id="gadgetName"/></td>
                            </tr>

                            <tr>
                                <td><fmt:message key="gadget.url.type.add"/></td>
                                <td>
                                    <select id="gadgetURLType1" name="gadgetURLType1" onchange="setType(1)">
                                        <option value="GadgetURL">Gadget URL</option>
                                        <option value="FileBrowser">File Path</option>
                                        <option value="RegistryBrowser">Registry Path</option>
                                        <option value="ZipFileBrowser">Zip File Path</option>
                                    </select>
                                </td>
                            </tr>

                            <tr id="gadgetUrlTR">
                                <td><label><fmt:message key="gadget.url.address"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td><input type="text" size="50" name="gadgetUrl" id="gadgetUrl"
                                           value="<%=uploadedGadgetPath%>"/></td>
                            </tr>

                            <tr style="display:none;" id="fileBrowserTR">
                                <td><label><fmt:message key="gadget.url.file"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td><input type="file" size="50" name="gadget"
                                           id="gadget"/></td>
                            </tr>

                            <tr style="display:none;" id="registryBrowserTR">
                                <td><label><fmt:message key="gadget.url.registry"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td>
                                    <input type="text" size="50" id="gadgetRegistryFile"
                                           name="gadgetRegistryFile"
                                           readonly="true" style="float:left;"/>
                                    <a href="#" style="padding-left:20px; float:left;"
                                       class="registry-picker-icon-link"
                                       onclick="showRegistryBrowser('gadgetRegistryFile', '/_system/config');">
                                        <fmt:message key="conf.registry.keys"/>
                                    </a>
                                </td>
                            </tr>

                            <tr>
                                <td><label><fmt:message key="gadget.description"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td><textarea rows="5" name="gadgetDesc" cols="57" id="gadgetDesc"></textarea><%=""%></td>
                            </tr>

                            <tr id="gadgetScreenTR">
                                <td colspan="2">
                                <jsp:include page="inc/gadget-screen.jsp"/>   </td>
                            </tr>

                        </table>

                    </td>
                </tr>

                <tr>
                    <td class="buttonRow"><input type="button" onclick="validateForm(1)"
                                                     value="<fmt:message key="add.gadget.button"/>"
                                                     class="button"
                                                     id="addGadget"/>
                            <input type="button" onclick="cancelForm()"
                                   value="<fmt:message key="cancel.gadget.button"/>" class="button"
                                   id="cancelGadget"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </form>


    </div>
</div>
<%
} else { // Modify Gadget
    String gadgetUrlValue = result.getGadgetUrl();
    int validationOperator = 1;

    boolean isUrlAddress = false;
    if (gadgetUrlValue != null || !"".equals(gadgetUrlValue.trim())) {    
        if (gadgetUrlValue.contains("http://")) {
            isUrlAddress = true;
            validationOperator = 2;
        } else if (gadgetUrlValue.contains("/registry/resource/")) {
            isUrlAddress = false;
            validationOperator = 3;
        }
    }

    String resourcePath = registryURL;
    String displayEditSourceLink = "";

    // if url is a registry resource then gadget xml can edit
    if (gadgetUrlValue != null && !gadgetUrlValue.contains("http") && gadgetUrlValue.length() > 18) {
        resourcePath += gadgetUrlValue.substring(18);
        displayEditSourceLink = "";
    } else {
        // otherwise disable the edit source link
        displayEditSourceLink = "display:none";
    }
%>
<div id="middle">
    <h2>
        <fmt:message key="add.gadget.header"/>
    </h2>

    <div id="workArea">


        <form action="../../fileupload/gadgetupload"
              enctype="multipart/form-data" method="post" id="gadgetForm">
            <input type="hidden" id="uMode" name="mode" value="<%=param%>"/>
            <input id="uResourceMediaType" type="hidden" name="mediaType"
                   value="application/vnd.wso2.gs.gadget"/>
            <input type="hidden" id="redirect" name="redirect" value="gadgetrepo/index.jsp"/>
            <input type="hidden" id="errorRedirect" name="errorRedirect"
                   value="gadgetrepo/add-gadget.jsp"/>
            <input type="hidden" id="gadgetPath" name="gadgetPath" value="<%=gadgetPath%>"/>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th>
                        <span style="float:left; position:relative; margin-top:2px;"><fmt:message
                                key="modify.gadget.info"/></span>
                        <a href="#"
                           onclick="window.location = '<%=resourcePath%>'"
                           class="icon-link" style="background-image:url(images/source-view.gif);<%=displayEditSourceLink%> ">
                            <fmt:message key="gadget.switchto.source.text"/>
                        </a>

                    </th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">

                        <table class="normal" style="width:100%;">
                            <tbody>
                            <tr>
                                <td width="150px"><label><fmt:message key="gadget.name"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td width="650px"><input type="text" name="gadgetName"
                                           id="gadgetName" value="<%=result.getGadgetName()%>"/>
                                </td>
                            </tr>

                            <tr <%=!isUrlAddress ? "style=\"display:none\";" : ""%>>
                                <td><fmt:message key="gadget.url.type.modify"/></td>
                                <td>
                                    <select id="gadgetURLType2" name="gadgetURLType2" onchange="setType(2)">
                                        <option value="GadgetURL">Gadget URL</option>
                                        <option value="FileBrowser">File Path</option>
                                        <option value="RegistryBrowser">Registry Path</option>
                                    </select>
                                </td>
                            </tr>

                            <tr <%=isUrlAddress ? "style=\"display:none\";" : ""%>>
                                <td><fmt:message key="gadget.url.type.modify"/></td>
                                <td>
                                    <select id="gadgetURLType3" name="gadgetURLType3" onchange="setType(3)">
                                        <option value="RegistryBrowser">Registry Path</option>
                                        <option value="GadgetURL">Gadget URL</option>
                                        <option value="FileBrowser">File Path</option>
                                    </select>
                                </td>
                            </tr>

                            <tr id="gadgetUrlTR" <%=!isUrlAddress ? "style=\"display:none\";" : ""%>>
                                <td><label><fmt:message key="gadget.url.address"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td><input type="text" size="50" name="gadgetUrl"
                                           id="gadgetUrl" value="<%=gadgetUrlValue%>"/></td>
                            </tr>
                            <tr style="display:none;" id="fileBrowserTR">
                                <td><label><fmt:message key="gadget.url.file"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td><input type="file" size="50" name="gadget" value="<%=gadgetUrlValue%>"
                                           id="gadget"/></td>
                            </tr>

                            <tr id="registryBrowserTR" <%=isUrlAddress ? "style=\"display:none\";" : ""%>>
                                <td><label><fmt:message key="gadget.url.registry"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td>
                                    <input type="text" size="50" id="gadgetRegistryFile"
                                           name="gadgetRegistryFile" value="<%=gadgetUrlValue%>"
                                           readonly="true" style="float:left;"/>
                                    <a href="#" style="padding-left:20px; float:left;"
                                       class="registry-picker-icon-link"
                                       onclick="showRegistryBrowser('gadgetRegistryFile', '/_system/config');"><fmt:message
                                            key="conf.registry.keys"/>
                                    </a>
                                </td>
                            </tr>

                            <tr>
                                <td><label><fmt:message key="gadget.description"/></label><span
                                        class="required">&nbsp;* </span></td>
                                <td><textarea rows="5" cols="57" name="gadgetDesc" id="gadgetDesc"><%=result.getGadgetDesc()%></textarea></td>
                            </tr>

                            <tr id="gadgetScreenTR">
                                <td colspan="2">
                                    <jsp:include page="inc/gadget-screen.jsp"/>
                                </td>
                            </tr>

                            </tbody>
                        </table>

                    </td>
                </tr>

                <tr>
                    <td class="buttonRow">
                        <button class="button"
                                onclick="validateForm(<%=validationOperator %>);removeThumbnail('<%=gadgetPath%>'); return false;">
                            <fmt:message key="modify.gadget.button"/></button>
                        <button class="button" onclick="cancelForm(); return false;"><fmt:message
                                key="cancel.gadget.button"/></button>

                    </td>
                </tr>

                </tbody>

            </table>
        </form>

    </div>

</div>
<%
    }
%>


</fmt:bundle>
