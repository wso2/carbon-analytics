<%@ page import="org.wso2.carbon.analytics.spark.ui.client.AnalyticsExecutionClient" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
    AnalyticsExecutionClient client = new AnalyticsExecutionClient(cookie, serverURL, configContext);
    String query = request.getParameter("query");
    String jsonResult = "";

    try {
        jsonResult = client.execute(query.trim());
        response.setStatus(HttpServletResponse.SC_OK);
    }catch (Exception e){
        throw new RuntimeException("Exception occured while executing query: "+e.getMessage(),e);
    }
%>
<%=jsonResult%>
