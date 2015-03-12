<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.MessageConsoleConnector" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    MessageConsoleConnector connector = new MessageConsoleConnector(configContext, serverURL, cookie);

    String tableName = request.getParameter("tableName");
    String type = request.getParameter("type");

    if ("tableInfo".equals(type) && tableName != null && !tableName.isEmpty()) {
        out.print(connector.getTableInfo(tableName));
    } else if ("getRecords".equals(type) && tableName != null && !tableName.isEmpty()) {
        out.print(connector.getRecords(tableName));
    }
%>