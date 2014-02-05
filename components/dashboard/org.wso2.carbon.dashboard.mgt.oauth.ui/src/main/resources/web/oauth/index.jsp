<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.oauth.ui.OAuthMgtServiceClient" %>
<%@ page import="org.wso2.carbon.dashboard.mgt.oauth.stub.types.carbon.ConsumerEntry" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<jsp:include page="../dialog/display_messages.jsp"/>
<%
    String loggeduser = (String) request.getSession().getAttribute("logged-user");
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

    OAuthMgtServiceClient client;

    client = new OAuthMgtServiceClient(cookie, backendServerURL, configContext, request.getLocale());

    ConsumerEntry[] entries = client.getConsumerPagedEntries(selectPage);

    int numberOfPages = client.getNumberOfPages();

%>
<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.oauth.ui.resource.i18n.Resources">
    <carbon:breadcrumb label="page.title"
                       resourceBundle="org.wso2.carbon.dashboard.mgt.oauth.ui.resource.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <script type="text/javascript" src="javascript/jquery-1.3.2.js"></script>
    <script type="text/javascript">
        function addEntry(service, func) {
            if (func == 'update') {
                location.href = 'add_keys.jsp?mode=' + func + '&service=' + service;
            } else {
                location.href = 'add_keys.jsp?mode=' + func;
            }
        }

        function deleteEntry(service, id) {
            var resp = jQuery.ajax({
                url: 'oauthmgt-ajaxprocessor.jsp?func=delete&service=' + service,
                async: false
            }).responseText;

            jQuery('#cons-' + id).hide('slow');
        }
    </script>
    <div id="middle">
        <h2><fmt:message key="page.title"/></h2>

        <div id="workArea">
            <div style="float:right;"><carbon:paginator pageNumber="<%=selectPage%>"
                                                        numberOfPages="<%=numberOfPages%>"
                                                        page="index.jsp"
                                                        pageNumberParameterName="selectPage"/></div>
            <a href="javascript:addEntry('', 'new')" name="add_new_key" class="icon-link"
               accesskey=""
               style="float:left; background-image: url(../admin/images/add.gif);">Add new
                consumer</a><br/><br/>
            <%if (entries.length != 0) {%>
            <form action="_self" method="get">
                <table class="styledLeft">
                    <thead>
                    <tr>
                        <th colspan="2">Available OAuth Consumer Information</th>
                    </tr>
                    </thead>
                    <% for (int i = 0; i < entries.length; i++) { %>
                    <tr id="cons-<%=i%>">
                        <td>
                            <table width="100%" cellspacing="0" cellpadding="0" border="0"
                                   class="normal">
                                <tr class="tableOddRow">
                                    <td nowrap="nowrap">Service name:</td>
                                    <td width="100%"><%=entries[i].getService()%>
                                    </td>
                                </tr>
                                <tr class="tableEvenRow">
                                    <td nowrap="nowrap">Consumer Key:</td>
                                    <td><%=entries[i].getConsumerKey()%>
                                    </td>
                                </tr>
                                <tr class="tableOddRow">
                                    <td nowrap="nowrap">Consumer Secret:</td>
                                    <td><%=entries[i].getConsumerSecret()%>
                                    </td>
                                </tr>
                                <tr class="tableEvenRow">
                                    <td nowrap="nowrap">Key Type:</td>
                                    <td><%=entries[i].getKeyType()%>
                                    </td>
                                </tr>
                                <tr>
                                    <td colspan="2" style="padding-top: 10px;"><input type="button"
                                                                                      value="Edit"
                                                                                      id="but-<%=i%>"
                                                                                      class="button selec"
                                                                                      onclick="javascript:addEntry('<%=entries[i].getService()%>', 'update')">
                                        <input type="button" value="Delete"
                                               id="but-<%=i%>"
                                               class="button selec"
                                               onclick="javascript:deleteEntry('<%=entries[i].getService()%>', '<%=i%>')">
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    <% } %>
                </table>
            </form>
            <%} %>
        </div>
    </div>
</fmt:bundle>