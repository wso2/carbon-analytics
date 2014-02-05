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
        import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    GadgetRepoServiceClient gadgetRepoServiceClient = null;

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

    if (pages > 1) {
        for (int i = 0; i < pages; i++) {
            out.print("<a href=\"javascript:paginate(" + (Math.random() * 9999) + "," + (i) + ");\" class=\"pageLinks\" >" + (i + 1) + "</a>&nbsp;");
        }
    }
%>