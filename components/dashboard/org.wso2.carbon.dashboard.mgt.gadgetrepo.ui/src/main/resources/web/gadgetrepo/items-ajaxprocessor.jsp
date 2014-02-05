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
 -->--%><%@ page import="org.wso2.carbon.ui.CarbonUIUtil"%><%@
page
	import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient"%><%@
page
	import="org.apache.axis2.context.ConfigurationContext"%><%@
page
	import="org.wso2.carbon.CarbonConstants"%><%@
page
	import="org.wso2.carbon.utils.ServerConstants"%><%@
taglib prefix="fmt"
	uri="http://java.sun.com/jsp/jstl/fmt"%><%@
taglib
	uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
	prefix="carbon"%><%@page
import="org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Gadget"%>
<%
     String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
            CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetRepoServiceClient gadgetRepoServiceClient;

    try{
    	gadgetRepoServiceClient = new GadgetRepoServiceClient(cookie,
                    backendServerURL,
                    configContext,
                    request.getLocale());
    }catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp" />
<%
        return;
    }

    String activeTab = request.getParameter("tab");
        if (activeTab == null) {
            activeTab = "0";
        }

    String gadgetGrp = request.getParameter("grp");
    String pageNumStr = request.getParameter("pageNum");
    String searchWord = request.getParameter("search");


    int pageNum = 0;

    if(pageNumStr != null) {
    	try {
    		pageNum = Integer.parseInt(pageNumStr);
    	} catch(Exception e) {

    	}
    }

    int nextGadget = 0;

    if(pageNum != 0){
    	nextGadget = (pageNum * DashboardConstants.GADGETS_PER_PAGE);
    }

    Gadget[] result;

    if (searchWord != null) {
        result = gadgetRepoServiceClient.getGadgetByName(searchWord);

    } else {
        result = gadgetRepoServiceClient.getGadgetDataPag(nextGadget, DashboardConstants.GADGETS_PER_PAGE);
    }


%>
<!--// rating plugin-specific resources //-->

<%@page import="org.wso2.carbon.dashboard.common.DashboardConstants"%>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage"%>
<script src="javascript/rating/jquery.form.js" type="text/javascript"
	language="javascript"></script>
<script src="javascript/rating/jquery.MetaData.js"
	type="text/javascript" language="javascript"></script>
<script src="javascript/rating/jquery.rating.js" type="text/javascript"
	language="javascript"></script>
<link href="javascript/rating/jquery.rating.css" type="text/css"
	rel="stylesheet" />
<fmt:bundle
	basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
	<carbon:jsi18n
		resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
		request="<%=request%>" />
    <table class="gadgetList">
				<%

                    if (result == null) { %>

        <tr> <br/>  <P>&nbsp;&nbsp;&nbsp;No matches could be found for the search word:<b><%=searchWord%></b> from Gadget Repository.
        </p></tr>
        <%}else{
                        for (int i = 0; i < result.length; i++) {



						 Gadget gadget = result[i];
                         if(gadget != null){
                            if (i % 3 == 0) {
                                //print tr
                                %><tr><%
                            }

                            //print td and data
                            %>
                            <td>

                                <div class="gadgetBox">
                                    <div class="users-strip">
                                            <div class="user-strip-inside">
                                                usage <%
                                                if (gadget.getUserCount() != null) {
                                                    out.print(gadget.getUserCount());
                                                } else {
                                                    out.print("0");
                                                }
                                            %></div>
                                    </div>
                                    <div class="gadget-name">
                                        <a href="gadget-page.jsp?gadgetPath=<%=gadget.getGadgetPath()%>&tab=<%=activeTab%>&grp=<%=gadgetGrp%>">
                                            <strong><%=gadget.getGadgetName()%></strong>
                                        </a>
                                    </div>
                                    <div class="gadget-image">
                                        <%
                                            if (gadget != null && gadget.getThumbUrl() != null
                                                && !gadget.getThumbUrl().equals("")) {
                                                String rootContext = configContext.getContextRoot();
                                                if ("/".equals(rootContext)) {
                                                    rootContext = "";
                                                }

                                                if (searchWord != null) {

                                            %>
                                        <a href="gadget-page.jsp?gadgetPath=<%=gadget.getGadgetPath()%>&tab=<%=activeTab%>&grp=<%=gadgetGrp%>">
                                            <div id="imageIco"
                                                 style="float:left;padding-right:15px;"
                                                 align="center">
                                                <img style="" id="imageIcon"
                                                     src="<%=rootContext + gadget.getThumbUrl()%>"
                                                     width="100px" height="100px"
                                                     class="gadgetImage"/>
                                            </div>
                                            </a>
                                        <%
                                                } else {
                                            %>
                                            <a href="gadget-page.jsp?gadgetPath=<%=gadget.getGadgetPath()%>&tab=<%=activeTab%>&grp=<%=gadgetGrp%>">
                                                <div id="imageIco"
                                                     align="center">
                                                    <img style="" id="imageIcon"
                                                         src="<%=rootContext + gadget.getThumbUrl()%>"
                                                         width="100px" height="100px"
                                                         class="gadgetImage"/>
                                                </div>
                                                </a>

                                            <%
                                                }
                                            } else {%>
                                                <img src="images/no-image.jpg" width="100px" class="gadgetImage" height="100px"/>
                                            <% }

                                        %>
                                        <%=gadget.getGadgetDesc()%>
                                    </div>
                                    <div style="clear:both"></div>
                                    <%--Rating box--%>
                                    <div>
                                        <input type="radio" class="star" name="avRating-<%=i %>"
                                           disabled="disabled" value="1"
                                            <%if ((gadget.getRating() >= 1) && (gadget.getRating() < 2)) {%>
                                           checked="checked" <%} %> /> <input type="radio" class="star"
                                                                              name="avRating-<%=i %>"
                                                                              disabled="disabled" value="2"
                                        <%if ((gadget.getRating() >= 2) && (gadget.getRating() < 3)) {%>
                                                                              checked="checked" <%} %> /> <input
                                        type="radio" class="star"
                                        name="avRating-<%=i %>" disabled="disabled" value="3"
                                        <%if ((gadget.getRating() >= 3) && (gadget.getRating() < 4)) {%>
                                        checked="checked" <%} %> /> <input type="radio" class="star"
                                                                           name="avRating-<%=i %>" disabled="disabled"
                                                                           value="4"
                                        <%if ((gadget.getRating() >= 4) && (gadget.getRating() < 5)) {%>
                                                                           checked="checked" <%} %> /> <input
                                        type="radio" class="star"
                                        name="avRating-<%=i %>" disabled="disabled" value="5"
                                        <%if (gadget.getRating() == 5) {%> checked="checked" <%} %> />
                                    </div>
                                    <div style="clear:both"></div>

                                    <%--Add it box--%>
                                    <%
                                        if (gadgetRepoServiceClient.userHasGadget(gadget.getGadgetPath())) {
                                    %>
                                    <div class="add-it-box">

                                        <img alt="" src="images/tick.png" align="top"/><b> Added</b>

                                    </div>

                                    <% } %>
                                    <input type="hidden" id="gadgetPath-<%=i%>"
                                           value="<%=gadget.getGadgetPath()%>"/>
                                    <input type="hidden" id="gadgetUrlFromRepo-<%=i%>"
                                           value="<%=gadget.getGadgetUrl()%>"/>
                                    <%--Add it button--%>
                                    <div style="margin-top:10px;margin-bottom:10px;">
                                            <input type="button" onclick="addGadgetToDashboard(<%=i%>)"
                                                   value="<fmt:message key="add.to.dashboard"/>" class="button"
                                                   id="addGadgetToDash"/>
                                            <div style="clear:both"></div>

                                    </div>

                                </div>

                            </td>
                            <%
                            if (i % 3 == 0 && i == (result.length - 1)) {
                                    //print two empty tds
                                    %><td></td><td></td><%
                            } else if (i % 3 == 1 && i == (result.length - 1)) {
                                    //print one empty tds
                                    %><td></td><%
                            }


                            if (i % 3 == 2 || i == (result.length - 1)) {
                                //end tr
                                %></tr><%
                            }
}

                                }

                            } %>
         </table>
			
</fmt:bundle>