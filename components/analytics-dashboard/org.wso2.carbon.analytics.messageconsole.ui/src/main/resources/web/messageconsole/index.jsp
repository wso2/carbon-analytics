<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.MessageConsoleConnector" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <script src="js/jquery-1.11.2.min-messageconsole.js" type="text/javascript"></script>
    <script src="js/jquery-ui.min-messageconsole.js" type="text/javascript"></script>
    <script src="js/jquery.jtable.min.js" type="text/javascript"></script>
    <link href="themes/metro/blue/jtable.min.css" rel="stylesheet" type="text/css"/>

    <%
        String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
                getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        MessageConsoleConnector connector = new MessageConsoleConnector(configContext, serverURL, cookie);

    %>

    <script type="text/javascript">
        function listActionMethod(postData) {
            return $.Deferred(function ($dfd) {
                $.ajax({
                           url: '/analytics/getppl',
                           type: 'POST',
                           headers: {
                               'Authorization': 'some value'
                           },
                           dataType: 'json',
                           data: postData,
                           success: function (data) {
                               $dfd.resolve(data);
                           },
                           error: function () {
                               $dfd.reject();
                           }
                       });
            });
        }

        $(document).ready(function () {
            $('#PersonTableContainer').jtable({
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
                                                                  $('#PersonTableContainer').jtable('openChildTable',
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

            $('#PersonTableContainer').jtable('load');

            var myList = "";
            <%
            for(String tableName: connector.getTableList()) {
            %>
                myList += "<option value='<%= tableName%>'>" + <%= tableName%> +"</option>";
            <%
            }
            %>
            $("#tableSelect").append(myList);
        });

        function createMainJTable(fields) {
            $('#PersonTableContainer').jtable({
                                                  title: 'Table of BAM',
                                                  paging: true,
                                                  pageSize: 10,
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
                                                  fields: fields
                                              });
            $('#PersonTableContainer').jtable('load');
        }

        function getArbitraryFields(rowData) {
            var $img =
                    $('<img src="/carbon/messageconsole/themes/metro/list_metro.png" title="Edit exam results" />');
            //Open child table when user clicks the image
            $img.click(function () {
                $('#PersonTableContainer').jtable('openChildTable',
                                                  $img.closest('tr'), //Parent row
                                                  {
                                                      title: 'Arbitrary Fields',
                                                      actions: {
                                                          listAction: function (postData, jtParams) {
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
        function createJTable(table) {
            $.getJSON("/analytics/getSchema/" + table, function (data, status) {
                var fields = {
                    ArbitraryFields: {
                        title: '',
                        width: '1%',
                        sorting: false,
                        edit: false,
                        create: false,
                        display: function (rowData) {
                            //Create an image that will be used to open child table
                            return getArbitraryFields(rowData);
                        }
                    }
                };
                $.each(data, function (key, val) {
                    fields[val.column] = {
                        title: val.column
                    };
                });

                if (data) {
                    $('#PersonTableContainer').jtable('destroy');
                    createMainJTable(fields);
                }
            });
        }
    </script>

</head>
<body>
<label> Select:
    <%--<select onchange="if (this.selectedIndex) createJTable(this.value);">--%>
    <select id="tableSelect">
        <option value="-1">Please select a table</option>
    </select>
</label>

<div id="PersonTableContainer"></div>

</body>
</html>