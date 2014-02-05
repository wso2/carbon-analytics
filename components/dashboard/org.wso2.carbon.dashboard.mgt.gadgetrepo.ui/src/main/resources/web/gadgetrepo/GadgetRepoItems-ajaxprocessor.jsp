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
<%@ page import="org.wso2.carbon.dashboard.common.DashboardConstants" %>
<%
    response.setHeader("Cache-Control", "no-cache");
    response.setHeader("Pragma", "no-cache");
    response.setDateHeader("Expires", -1);

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

    String pageNumStr = request.getParameter("pageNum");
    int pageNum = 0;

    if (pageNumStr != null) {
        try {
            pageNum = Integer.parseInt(pageNumStr);
        } catch (Exception e) {

        }
    }

    int nextGadget = 0;

    if (pageNum != 0) {
        nextGadget = (pageNum * DashboardConstants.GADGETS_PER_PAGE);
    }

    Gadget[] result = gadgetRepoServiceClient.getGadgetDataPag(nextGadget, DashboardConstants.GADGETS_PER_PAGE);
%>
<HEAD>
    <META HTTP-EQUIV="PRAGMA" CONTENT="NO-CACHE">
    <META HTTP-EQUIV="Expires" CONTENT="-1">
</HEAD>
<fmt:bundle basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
            request="<%=request%>"/>
    <table class="styledLeft">
        <thead>
        <tr>
            <th><fmt:message key="gadgets"/></th>
        </tr>
        <tbody>
        <tr>
            <td>
                <%
                    if (result != null) {
                        for (int i = 0; i < result.length; i++) {
                            Gadget gadget = result[i];

                            if (gadget != null) {
                                String registryURL =
                                        "../../carbon/gadgetrepo/resource_editor.jsp?viewType=std&resourcePath=" +
                                        gadget.getGadgetUrl() + "&path=";

                                String resourcePath = registryURL;
                                String gadgetUrl = gadget.getGadgetUrl();
                                String permissionDisabled = "";

                                // if url is a registry resource then permissions can edit
                                if (gadgetUrl != null && !gadgetUrl.contains("http") && gadgetUrl.length() > 18) {
                                    resourcePath += gadgetUrl.substring(18);
                                    permissionDisabled = "";
                                } else {
                                    // if http url, then disable the permission button
                                    resourcePath="";
                                    permissionDisabled = "disabled";
                                }

                %>
                <table width="100%" class="normal gadgetList" id="table-<%=i%>">
                    <tbody>
                    <tr>
                        <td>
                            <%
                                if (gadget != null && gadget.getThumbUrl() != null
                                    && !gadget.getThumbUrl().equals("")) {
                                    String rootContext = configContext.getContextRoot();
                                    if ("/".equals(rootContext)) {
                                        rootContext = "";
                                    }
                            %>
                            <div id="imageIco" align="center">
                                <img style="" id="imageIcon"
                                     src="<%=rootContext + gadget.getThumbUrl()%>"
                                     width="100px" height="100px"
                                     class="gadgetImage"/>
                            </div>
                            <%
                            } else {
                            %> <img src="images/no-image.jpg" width="100px" height="100px"
                                    class="gadgetImage"/>
                            <%
                                }
                            %>
                        </td>
                        <td width="100%">
                            <p><label><strong><%=gadget.getGadgetName()%>
                            </strong></label><br/>
                                <label><%=gadget.getGadgetUrl()%>
                                </label><br/>
                                <label><%=gadget.getGadgetDesc()%>
                                </label><br/>
                            </p>

                            <input type="hidden" id="gadgetPath-<%=i%>"
                                   value="<%=gadget.getGadgetPath()%>"/></td>
                        <td nowrap="nowrap">
                            <table>
                                <tr>
                                    <td>
                                        <a style="background-image: url(../admin/images/edit.gif);margin-left:0px;" class="icon-link" id="modifyGadget"
                                               onclick="modifyGadget(<%=i%>)"><fmt:message key="modify" /></a>

                                    </td>
                                    <td>
                                        <a style="background-image: url(../admin/images/delete.gif);" class="icon-link" id="deleteGadget"
                                               onclick="deleteGadget(<%=i%>)"><fmt:message key="delete"  /></a>
                                        
                                    </td>
                                    <td>
                                        <a <% if(permissionDisabled.equals("disabled")){%>style="background-image: url(../gadgetrepo/images/permissions-disabled.png);"<% } else { %>style="background-image: url(../gadgetrepo/images/permissions.png);" onclick=" window.location = '<%=resourcePath%>'"<% } %> 
                                           class="icon-link" id="permissions"><fmt:message key="permissions" /></a>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="3"><input type="checkbox" id="makeDefault-<%=i%>"
                                   onclick="makeDefault(<%=i%>)"
                                   <%if("true".equals(gadget.getDefaultGadget())){%>checked="checked"<%} %>><fmt:message
                                    key="make.default"/></input></td>
                                </tr>
                                <tr>
                                    <td colspan="3"><input type="checkbox" id="unsignedUserGadget-<%=i%>"
                                   onclick="makeGadgetForUnsignedUser(<%=i%>)"
                                   <%if("true".equals(gadget.getUnsignedUserGadget())){%>checked="checked"<%} %>><fmt:message
                                    key="make.unsignedUser"/></input></td>
                                </tr>
                            </table>







                        </td>
                    </tr>

                    </tbody>
                </table>
                <%
                            }
                        }

                    }
                %>
            </td>
        </tr>
        </tbody>
        </thead>
    </table>
</fmt:bundle>
