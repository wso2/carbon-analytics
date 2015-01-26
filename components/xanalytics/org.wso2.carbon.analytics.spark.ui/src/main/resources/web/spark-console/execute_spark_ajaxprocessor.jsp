<%@ page import="org.wso2.carbon.analytics.spark.ui.client.SparkExecutionClient" %>
<%

    String query = request.getParameter("query");
    SparkExecutionClient client = new SparkExecutionClient();

    String jsonResult = client.execute(-1234, query.);

String msg="{\"response\": 200}";


%>
<%=msg%>
