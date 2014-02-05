<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@
        page import="org.apache.axis2.context.ConfigurationContext" %>
<%@
        page import="org.wso2.carbon.CarbonConstants" %>
<%@
        page import="org.wso2.carbon.utils.ServerConstants" %>
<%@
        page import="org.wso2.carbon.dashboard.mgt.oauth.ui.OAuthMgtServiceClient" %>
<%@
        page import="org.wso2.carbon.dashboard.mgt.oauth.stub.types.carbon.ConsumerEntry" %>
<%@
        page import="org.wso2.carbon.dashboard.common.DashboardConstants" %>
<%@
        page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@
        taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@
        taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
               prefix="carbon" %>
<%
    String addConsumerMode = request.getParameter("mode");
    String consumerServiceName = request.getParameter("service");

    String backendServerURL = CarbonUIUtil.getServerURL(config
            .getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config
            .getServletContext().getAttribute(
                    CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session
            .getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    OAuthMgtServiceClient client;

    client = new OAuthMgtServiceClient(cookie, backendServerURL, configContext, request.getLocale());
    ConsumerEntry entry = null;
    if ("update".equals(addConsumerMode) && consumerServiceName != null) {
        entry = client.getConsumerEntry(consumerServiceName);
    }

    String pageAction = request.getParameter("action");

    if ((pageAction != null) && ("submit".equals(pageAction))) {
        ConsumerEntry newEntry = new ConsumerEntry();
        newEntry.setConsumerKey(request.getParameter(DashboardConstants.CONSUMER_KEY_KEY));
        newEntry.setConsumerSecret(request.getParameter(DashboardConstants.CONSUMER_SECRET_KEY));
        newEntry.setKeyType(request.getParameter(DashboardConstants.KEY_TYPE_KEY));
        newEntry.setService(request.getParameter(DashboardConstants.CONSUMER_SERVICE));

        boolean ret = client.addConsumerEntry(newEntry, request.getParameter("mode"));

        if (ret == false) {
            CarbonUIMessage uiMsg = new CarbonUIMessage("Failed to add consumer key data. " +
                    "Please refer to error logs for more details", CarbonUIMessage.ERROR);
            session.setAttribute(CarbonUIMessage.ID, uiMsg);
%>
<jsp:include page="../admin/error.jsp"/>
<%
        return;
    }
%>
<script type="text/javascript">location.href = 'index.jsp?region=region1&item=gadget_server_oauth_mgt_menu';</script>
<%
    }
    // response.sendRedirect("http://www.google.com");
%>
<jsp:include page="../dialog/display_messages.jsp"/>
<fmt:bundle
        basename="org.wso2.carbon.dashboard.mgt.oauth.ui.resource.i18n.Resources">
    <carbon:breadcrumb label="page.title.add.keys"
                       resourceBundle="org.wso2.carbon.dashboard.mgt.oauth.ui.resource.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <script type="text/javascript" src="javascript/jquery-1.3.2.js"></script>
    <script type="text/javascript" src="../ajax/js/prototype.js"></script>
    <script type="text/javascript">
        sessionAwareFunction();

        function addEntry(resPath, func) {
            location.href = 'add_keys.jsp?mode=' + func + '&resPath=' + resPath;
        }

    </script>

    <div id="middle">
        <h2><fmt:message key="page.title.add.keys"/></h2>

        <div id="workArea">

            <table class="styledLeft">
                <thead>
                <tr>
                    <th colspan="2">Add new consumer key information</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td class="formRow">
                        <form onsubmit="true" target="_self" action="add_keys.jsp" id="keyDataForm" name="keyDataForm"
                              method="post">
                                    <% if (("update").equals(addConsumerMode) && entry != null) {%>
                            <input type="hidden" name="action" value="submit"/>
                            <input type="hidden" name="mode" value="update"/>
                            <table class="normal">
                                <tr>
                                    <td>Consumer Service</td>
                                    <td><input id="consumer_service" type="text" name="consumer_service"
                                               value="<%=entry.getService()%>" readonly="true" style="width:400px;"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td>Consumer Key</td>
                                    <td><input id="consumer_key" type="text" name="consumer_key"
                                               value="<%=entry.getConsumerKey()%>" style="width:400px;"/></td>
                                </tr>
                                <tr>
                                    <td style="vertical-align:top !important;">Consumer Secret</td>
                                    <td><textarea id="consumer_secret" type="" name="consumer_secret"
                                                  style="width:400px; height:250px;"><%=entry.getConsumerSecret()%>
                                    </textarea></td>
                                </tr>
                                <tr>
                                    <td>Consumer Key Type</td>
                                    <td><input id="key_type" type="text" name="key_type"
                                               value="<%=entry.getKeyType()%>" style="width:400px;"/></td>
                                </tr>
                            </table>
                                    <% } else { %>
                            <input type="hidden" name="action" value="submit"/>
                            <input type="hidden" name="mode" value="new"/>
                            <table class="normal">
                                <tr>
                                    <td>Consumer Service</td>
                                    <td><input id="consumer_service" type="text" name="consumer_service"
                                               style="width:400px;"/></td>
                                </tr>
                                <tr>
                                    <td>Consumer Key</td>
                                    <td><input id="consumer_key" type="text" name="consumer_key" style="width:400px;"/>
                                    </td>
                                </tr>
                                <tr>
                                    <td style="vertical-align:top !important;">Consumer Secret</td>
                                    <td><textarea id="consumer_secret" type="" name="consumer_secret"
                                                  style="width:400px; height:250px;"></textarea></td>
                                </tr>
                                <tr>
                                    <td>Consumer Key Type</td>
                                    <td><input id="key_type" type="text" name="key_type" style="width:400px;"/></td>
                                </tr>
                            </table>
                                    <% } %>
                    </td>
                </tr>
                <tr>
                    <td class="buttonRow"><input id="Save" class="button" type="submit" name="save_but" value="Save"/>
                    </td>
                </tr>
                </form>
                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>