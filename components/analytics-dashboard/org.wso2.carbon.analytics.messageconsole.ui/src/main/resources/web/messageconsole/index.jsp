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
            $('#AnalyticsTableContainer').jtable({
                                                  title: 'Table of people',
                                                  paging: true, //Enable paging
                                                  pageSize: 10, //Set page size (default: 10)
//                                                  sorting: true, //Enable sorting
                                                  actions: {
                                                      // For Details: http://jtable.org/Demo/FunctionsAsActions
                                                      listAction: function (postData, jtParams) {
                                                          return listActionMethod(postData);
                                                      },
//                                                      listAction: 'https://192.168.1.5:9443/analytics/getppl',
//                                                      createAction: '/GettingStarted/CreatePerson',
//                                                      updateAction: '/GettingStarted/UpdatePerson',
                                                      deleteAction: '/GettingStarted/DeletePerson'
                                                  },
                                                  fields: {
                                                      "PersonId": {
                                                          "key": true,
                                                          "list": false
                                                      },
                                                      "Name": {
                                                          "title": "Author Name",
                                                          "width": "40%"
                                                      },
                                                      Age: {
                                                          title: 'Age',
                                                          width: '20%'
                                                      },
                                                      RecordDate: {
                                                          title: 'Record date',
                                                          width: '30%',
                                                          type: 'date',
                                                          create: false,
                                                          edit: false
                                                      },
                                                      ArbitraryFields: {
                                                          title: '',
                                                          width: '1%',
                                                          sorting: false,
                                                          edit: false,
                                                          create: false,
                                                          display: function (studentData) {
                                                              //Create an image that will be used to open child table
                                                              var $img =
                                                                      $('<img src="/carbon/messageconsole/themes/metro/list_metro.png" title="Edit exam results" />');
                                                              //Open child table when user clicks the image
                                                              $img.click(function () {
                                                                  $('#AnalyticsTableContainer').jtable('openChildTable',
                                                                                                    $img.closest('tr'), //Parent row
                                                                                                    {
                                                                                                        title: 'Arbitrary Fields',
                                                                                                        actions: {
                                                                                                            listAction: function (postData,
                                                                                                                                  jtParams) {
                                                                                                                return listActionMethod(postData);
                                                                                                            },
                                                                                                            deleteAction: '/Demo/DeleteExam'
//                                                                                                updateAction: '/Demo/UpdateExam',
//                                                                                                createAction: '/Demo/CreateExam'
                                                                                                        },
                                                                                                        fields: {
                                                                                                            PersonId: {
                                                                                                                key: true,
                                                                                                                list: false
                                                                                                            },
                                                                                                            Name: {
                                                                                                                title: 'Name'
                                                                                                            },
                                                                                                            Value: {
                                                                                                                title: 'Value'
                                                                                                            },
                                                                                                            Type: {
                                                                                                                title: 'Type',
                                                                                                                options: ["String", "boolean", "int", "long"]
                                                                                                            }
                                                                                                        }
                                                                                                    }, function (data) { //opened handler
                                                                              data.childTable.jtable('load');
                                                                          });
                                                              });
                                                              //Return image to show on the person row
                                                              return $img;
                                                          }
                                                      }
                                                  }
                                              });

            $('#AnalyticsTableContainer').jtable('load');

            var tableNames = "";
            <c:forEach var='tableName' items='${connector.getTableList()}'>
                tableNames += "<option value='${tableName}'>" + '${tableName}' +"</option>";
            </c:forEach>
            $("#tableSelect").append(tableNames);
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

</body>
</html>