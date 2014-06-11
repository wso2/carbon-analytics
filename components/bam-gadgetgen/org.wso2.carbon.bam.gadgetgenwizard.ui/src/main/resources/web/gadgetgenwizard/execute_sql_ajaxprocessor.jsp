<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GadgetGenAdminClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GGWUIUtils" %>
<%@ page import="org.json.JSONObject" %>
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    GadgetGenAdminClient gadgetGenAdminClient = new GadgetGenAdminClient(cookie, serverURL, configContext);

    String responseHTML;
    String sqlFromUI = GGWUIUtils.getSQL(session);
    String sql = (sqlFromUI != null) ? sqlFromUI : request.getParameter("sql");
    try {
        JSONObject jsonObject = gadgetGenAdminClient.executeQuery(GGWUIUtils.constructDBConnInfo(session), sql);
        responseHTML = jsonObject.toString();
    } catch (Exception e) {
        responseHTML = "Error executing query. " + e.getMessage();
    }
%>
<%=responseHTML%>