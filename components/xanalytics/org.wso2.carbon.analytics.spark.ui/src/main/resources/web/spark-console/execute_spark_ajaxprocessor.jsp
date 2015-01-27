<%@ page import="org.wso2.carbon.analytics.spark.ui.client.SparkExecutionClient" %>
<%@ page import="org.wso2.carbon.analytics.spark.core.AnalyticsExecutionException" %>
<%

    String query = request.getParameter("query");
    SparkExecutionClient client = new SparkExecutionClient();
    String jsonResult ="";
    try {
        jsonResult = client.execute(-1234, query.trim());

        response.setStatus(HttpServletResponse.SC_OK);
    } catch (AnalyticsExecutionException e) {
//        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (RuntimeException e) {
//        e.printStackTrace();
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    }

%>
<%=jsonResult%>
