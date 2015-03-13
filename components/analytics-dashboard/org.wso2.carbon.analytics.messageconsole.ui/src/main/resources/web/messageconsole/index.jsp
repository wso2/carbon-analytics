<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.MessageConsoleConnector" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    MessageConsoleConnector connector = new MessageConsoleConnector(configContext, serverURL, cookie);
    pageContext.setAttribute("connector", connector, PageContext.PAGE_SCOPE);
%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script src="js/jquery-1.11.2.min-messageconsole.js" type="text/javascript"></script>
    <script src="js/jquery-ui.min-messageconsole.js" type="text/javascript"></script>
    <script src="js/jquery.jtable.min.js" type="text/javascript"></script>
    <script src="js/messageconsole.js" type="text/javascript"></script>
    <link href="themes/metro/blue/jtable.min.css" rel="stylesheet" type="text/css"/>

    <script type="text/javascript">
        $(document).ready(function () {
            var tableNames = "";
            <c:forEach var='tableName' items='${connector.getTableList()}'>
            tableNames += "<option value='${tableName}'>" + '${tableName}' + "</option>";
            </c:forEach>
            $("#tableSelect").append(tableNames);
            $("#DeleteAllButton").hide();
        });
    </script>

</head>
<body>
<label> Table Name:
    <select id="tableSelect" onchange="if (this.selectedIndex) createJTable(this.value);">
        <option value="-1">Select a table</option>
    </select>
</label>

<div id="AnalyticsTableContainer"></div>
<button id="DeleteAllButton"> Delete all selected records</button>

</body>
</html>