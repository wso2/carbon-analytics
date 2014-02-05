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
<%@ page import="org.wso2.carbon.dashboard.common.DashboardConstants" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.GadgetRepoServiceClient" %>
<%@ page
        import="org.wso2.carbon.dashboard.ui.DashboardUiUtils" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%

	String selectPageString = (String) request.getParameter("selectPage");
	if (selectPageString == null || selectPageString.equals("")) {
	    selectPageString = "0";
	}
	
	int selectPage;
	try {
	    selectPage = Integer.parseInt(selectPageString);
	} catch (Exception e) {
	    selectPage = 0;
	}

    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetRepoServiceClient gadgetRepoServiceClient;

    String pageNumStr = null;
    int pages = 0;

    try {
	    gadgetRepoServiceClient = new GadgetRepoServiceClient(cookie,
                    backendServerURL,
                    configContext,
                    request.getLocale());

        pageNumStr = request.getParameter("pageNum");
        pages = 0;
        if (gadgetRepoServiceClient.getCollectionSize() != null) {
            pages = (gadgetRepoServiceClient.getCollectionSize() - 1) / DashboardConstants.GADGETS_PER_PAGE;


            if (((gadgetRepoServiceClient.getCollectionSize() - 1) % DashboardConstants.GADGETS_PER_PAGE) != 0) {
                pages++;
            }
        }
    } catch (Exception e) {
        CarbonUIMessage uiMsg = new CarbonUIMessage(CarbonUIMessage.ERROR, e.getMessage(), e);
        session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
    if ( !gadgetRepoServiceClient.isSessionValid())  {
       response.sendRedirect(DashboardUiUtils.getLoginUrl("../gsusermgt/login.jsp", DashboardUiUtils.getHttpsPort(backendServerURL), request));
    }

%>
<script type="text/javascript" src="javascript/util.js"></script>
<script type="text/javascript"
        src="javascript/gadgetrepo-service-stub.js"></script>
<script type="text/javascript"
        src="javascript/gadgetrepo-server-utils.js"></script>

<script type="text/javascript">
    <%if(pageNumStr == null){%>
    $(window).load(function() {

        // load home page when the page loads
        $("#gadget-table").load("GadgetRepoItems-ajaxprocessor.jsp?pageNum=" + <%=selectPage%>);
    });
    <%}%>

    function deleteGadget(gadgetId) {
	confirmDialog(jsi18n["confirm.delete"], deleteGadgetAction, gadgetId);
    }
    
    function deleteGadgetAction(gadgetId){
        var gadgetPath = document.getElementById('gadgetPath-' + gadgetId).value;
        GadgetRepoService.deleteGadget(gadgetPath);
        $('#table-' + gadgetId).fadeOut('slow');
    }

    function modifyGadget(gadgetId) {
        var gadgetPath = document.getElementById('gadgetPath-' + gadgetId).value;
        location.href = 'add-gadget.jsp?mode=mod&gadgetPath=' + gadgetPath;

    }
    

    function paginate(rand, num) {
       // $(document).ready(function() {
            // load home page when the page loads
            $("#gadget-table").load("GadgetRepoItems-ajaxprocessor.jsp?pageNum=" + num + "&random=" + rand );
       // });
    }

    function makeDefault(gadgetId) {
        var isChecked = document.getElementById('makeDefault-' + gadgetId).checked;
        var gadgetPath = document.getElementById('gadgetPath-' + gadgetId).value;
        GadgetRepoService.makeDefault(gadgetPath, isChecked);

    }

    function makeGadgetForUnsignedUser(gadgetId) {
        var isChecked = document.getElementById('unsignedUserGadget-' + gadgetId).checked;
        var gadgetPath = document.getElementById('gadgetPath-' + gadgetId).value;
        GadgetRepoService.unsignedUserGadget(gadgetPath, isChecked);

    }

</script>

<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.Resources">
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.dashboard.mgt.gadgetrepo.ui.resource.i18n.JSResources"
            request="<%=request%>"/>
    <carbon:breadcrumb label="view"
                       resourceBundle="org.wso2.carbon.policyeditor.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <div id="middle">
        <h2><fmt:message key="gadget.repository"/></h2>

        <div id="workArea">
            <table width="100%">
                <tr>
                    <td>
                        <ul class="gadgets-top-links">
                            <li><a class="icon-link"
                                   style="background-image: url(../admin/images/add.gif);"
                                   href="add-gadget.jsp?mode=add"><fmt:message
                                    key="add.new.gadget"/></a></li>
                        </ul>
                    </td>
                </tr>
                <tr>
                    <td>
                    	<table style="float: right;">
                    		<tr>
	                    		<td>
	                    			<div id="pag" class="paginator"><carbon:paginator pageNumber="<%=selectPage%>" numberOfPages="<%=pages%>"
                                                        page="index.jsp" pageNumberParameterName="selectPage"/></div>
	                    		</td>
                    		</tr>
							
                        </table>
                    </td>
                </tr>
            </table>

            <div id="gadget-table"><!-- takes from GadgetRepoItems-ajaxprocessor.jsp --></div>

            <table style="float: right;">
                <tr>
                    <td>
                        <div id="pag1" class="paginator"><carbon:paginator pageNumber="<%=selectPage%>" numberOfPages="<%=pages%>"
                                                        page="index.jsp" pageNumberParameterName="selectPage"/></div>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</fmt:bundle>
