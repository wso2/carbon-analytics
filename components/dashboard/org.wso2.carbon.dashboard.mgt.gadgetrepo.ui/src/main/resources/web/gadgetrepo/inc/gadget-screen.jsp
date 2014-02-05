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
<jsp:include page="../../resources/resources-i18n-ajaxprocessor.jsp"/>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>

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


<fmt:bundle
basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
<carbon:jsi18n
resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
request="<%=request%>"/>

<script type="text/javascript">
    var thumbnailDeleted = false;

    function setThumbRemoveParam() {
        thumbnailDeleted = true;
        var gadgetThumb = document.getElementById("imageIcon");
        gadgetThumb.src = "images/no-image.jpg";
    }

    // Actual method which removes thumbnail
    function removeThumbnail(pathToGadget) {
        if (thumbnailDeleted == true) {
            GadgetRepoService.deleteGadgetImage(pathToGadget);
        } //else do nothing
    }
    jQuery(document).ready(function() {

        jQuery(".toggle_container").hide();
        /*Hide (Collapse) the toggle containers on load use show() insted of hide() 	in the 			above code if you want to keep the content section expanded. */

        jQuery("h2.trigger").click(function() {
            if (jQuery(this).next().is(":visible")) {
                this.className = "active trigger";
            } else {
                this.className = "trigger";
            }

            jQuery(this).next().slideToggle("fast");
            return false; //Prevent the browser jump to the link anchor
        });
    });
</script>


<%
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

    String param = request.getParameter("mode");
    String gadgetPath = request.getParameter("gadgetPath");

    //String errMsg = request.getParameter("msg");

    Gadget result = null;
    if ("mod".equals(param)) {
        result = gadgetRepoServiceClient.getGadget(gadgetPath);
    }

%>

<div id="screenContent">

        <h2 class="trigger active"><a href="#"><fmt:message key="gadget.screen.modify"/></a></h2>

        <div class="toggle_container">
            <input name="availableScreensList" id="availableScreensList" type="hidden" value=""/>
            <table id="availableScreens" class="normal-nopadding"  style="width: auto">
                                <tr>
                                    <td style="vertical-align: top ! important;">
                                        <%
                                            if (result != null && result.getThumbUrl() != null
                                                && !result.getThumbUrl().equals("")) {

                                                String rootContext = configContext.getContextRoot();
                                                if ("/".equals(rootContext)) {
                                                    rootContext = "";
                                                }
                                        %>

                                        <img style="" id="imageIcon"
                                             src="<%=rootContext + result.getThumbUrl()%>"
                                             width="100px" height="100px"
                                             class="gadgetImage"/><br/><br/>

                                        <div style="padding-left:10px">
                                            <a href="javascript:setThumbRemoveParam()"
                                               accesskey=""
                                               id="rmLink">Remove
                                                Image</a>
                                        </div>
                                       
                                        </td>
                                        <td>
                                        <input type="file" size="50"
                                               name="gadgetScreen"
                                               id="gadgetScreen"/>
                                        </td>
                                        <%
                                        } else {
                                        %>
                                        <td>
                                            <%--<div><fmt:message key="gadget.sceenshot"/></div>--%>
                                            <img id="imageIcon" src="images/no-image.jpg"
                                                 width="100px" height="100px" class="gadgetImage" align="bottom"/>

                                        </td>
                                        <td>
                                            <input type="file" size="50" name="gadgetScreen"
                                               id="gadgetScreen"/>
                                        </td>
                                        <% } %>
                                   
                                </tr>

                            </table>
        </div>

</div>
</fmt:bundle>
