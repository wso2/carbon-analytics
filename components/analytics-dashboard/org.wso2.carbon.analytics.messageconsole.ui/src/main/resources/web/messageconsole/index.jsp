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
    <link href="js/jquery-ui.min.css" rel="stylesheet" type="text/css"/>
    <link href="js/jquery.datetimepicker.css" rel="stylesheet" type="text/css"/>
    <link href="themes/metro/blue/jtable.css" rel="stylesheet" type="text/css"/>

    <script src="js/jquery-1.11.2.min.js" type="text/javascript"></script>
    <script src="js/jquery-ui.min.js" type="text/javascript"></script>
    <script src="js/jquery.jtable.min.js" type="text/javascript"></script>
    <script src="js/jquery.datetimepicker.js" type="text/javascript"></script>
    <script src="js/messageconsole.js" type="text/javascript"></script>

    <script type="text/javascript">
        var typeCreateRecord = '<%= MessageConsoleConnector.TYPE_CREATE_RECORD%>';
        var typeListRecord = '<%= MessageConsoleConnector.TYPE_LIST_RECORD%>';
        var typeDeleteRecord = '<%= MessageConsoleConnector.TYPE_DELETE_RECORD%>';
        var typeUpdateRecord = '<%= MessageConsoleConnector.TYPE_UPDATE_RECORD%>';
        var typeUpdateRecord = '<%= MessageConsoleConnector.TYPE_UPDATE_RECORD%>';
        var typeTableInfo = '<%= MessageConsoleConnector.TYPE_TABLE_INFO%>';

        var typeListArbitraryRecord = '<%= MessageConsoleConnector.TYPE_LIST_ARBITRARY_RECORD%>';
        var typeCreateArbitraryRecord = '<%= MessageConsoleConnector.TYPE_CRATE_ARBITRARY_RECORD%>';
        var typeUpdateArbitraryRecord = '<%= MessageConsoleConnector.TYPE_UPDATE_ARBITRARY_RECORD%>';
        var typeDeleteArbitraryRecord = '<%= MessageConsoleConnector.TYPE_DELETE_ARBITRARY_RECORD%>';

        $(document).ready(function () {
            var tableNames = "";
            <c:forEach var='tableName' items='${connector.getTableList()}'>
            tableNames += "<option value='${tableName}'>" + '${tableName}' + "</option>";
            </c:forEach>
            $("#tableSelect").append(tableNames);
            $("#DeleteAllButton").hide();
            jQuery('#timeFrom').datetimepicker({
                                                   format: 'unixtime',
                                                   onShow: function (ct) {
                                                       this.setOptions({
                                                                           maxDate: jQuery('#timeTo').val() ? jQuery('#timeTo').val() : false
                                                                       })
                                                   }
                                               });
            jQuery('#timeTo').datetimepicker({
                                                 format: 'unixtime',
                                                 onShow: function (ct) {
                                                     this.setOptions({
                                                                         minDate: jQuery('#timeFrom').val() ? jQuery('#timeFrom').val() : false
                                                                     })
                                                 }
                                             });
        });
    </script>

</head>
<body>
<fieldset>
    <legend>Search:</legend>
    <label> Table Name*:
        <select id="tableSelect">
            <option value="-1">Select a table</option>
        </select>
    </label>
    <label> From: <input id="timeFrom" type="text"> </label>
    <label> To: <input id="timeTo" type="text"> </label>
    <br>
    <label> Search Query:
        <textarea id="query" rows="4" cols="50"></textarea>
    </label>
    <input id="search" type="submit" value="Search" onclick="createJTable();">
</fieldset>

</body>
<div id="AnalyticsTableContainer"></div>
<button id="DeleteAllButton"> Delete all selected records</button>

</body>
</html>