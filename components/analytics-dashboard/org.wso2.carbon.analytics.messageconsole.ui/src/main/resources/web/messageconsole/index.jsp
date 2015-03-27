<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.MessageConsoleConnector" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.beans.Permissions" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.exception.MessageConsoleException" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);
    ConfigurationContext configContext = (ConfigurationContext) config.getServletContext().
            getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    MessageConsoleConnector connector = new MessageConsoleConnector(configContext, serverURL, cookie);
    pageContext.setAttribute("connector", connector, PageContext.PAGE_SCOPE);

    try {
        Permissions permissions = connector.getAvailablePermissionForUser();
        pageContext.setAttribute("permissions", permissions, PageContext.PAGE_SCOPE);
    } catch (MessageConsoleException e) {
        pageContext.setAttribute("permissionError", e, PageContext.PAGE_SCOPE);
    }
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
        var typeTableInfo = '<%= MessageConsoleConnector.TYPE_TABLE_INFO%>';

        var typeListArbitraryRecord = '<%= MessageConsoleConnector.TYPE_LIST_ARBITRARY_RECORD%>';
        var typeCreateArbitraryRecord = '<%= MessageConsoleConnector.TYPE_CRATE_ARBITRARY_RECORD%>';
        var typeUpdateArbitraryRecord = '<%= MessageConsoleConnector.TYPE_UPDATE_ARBITRARY_RECORD%>';
        var typeDeleteArbitraryRecord = '<%= MessageConsoleConnector.TYPE_DELETE_ARBITRARY_RECORD%>';
        var typeCreateTable = '<%= MessageConsoleConnector.TYPE_CREATE_TABLE%>';
        var typeDeleteTable = '<%= MessageConsoleConnector.TYPE_DELETE_TABLE%>';
        var typeGetTableInfo = '<%= MessageConsoleConnector.TYPE_GET_TABLE_INFO%>';

        var tablePopupAction;

        $(document).ready(function () {
            <c:if test="${permissions != null && permissions.isListTable()}">
            var tableNames = "";
            <c:forEach var='tableName' items='${connector.getTableList()}'>
            tableNames += "<option value='${tableName}'>" + '${tableName}' + "</option>";
            </c:forEach>
            $("#tableSelect").append(tableNames);
            </c:if>
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

            $("#createTableDialog").dialog({
                                               autoOpen: false
                                           });
            $("#addNewTable").on("click", function () {
                $("#column-details").find('tr').slice(1, document.getElementById('column-details').rows.length - 1).remove();
                $('#createTableDialog').dialog('option', 'title', 'Create a new table');
                $("#createTableDialog").dialog("open");
                document.getElementById("tableName").readOnly = false;
                document.getElementById("tableName").value = "";
                tablePopupAction = 'add';
                document.getElementById('createTablePopup').style.display = 'block';
                document.getElementById('msgLabel').style.display = 'none';
            });

            $("#column-details tbody").on("click", ".del", function () {
                $(this).parent().parent().remove();
            });

            $("#column-details tbody").on("click", ".add", function () {
                $(this).val('Delete');
                $(this).attr('class', 'del');
                var appendTxt =
                        "<tr><td><input type='text' name='column'/></td><td><select><option value='STRING'>STRING</option><option value='INTEGER'>INTEGER</option><option value='LONG'>LONG</option><option value='BOOLEAN'>BOOLEAN</option><option value='FLOAT'>FLOAT</option><option value='DOUBLE'>DOUBLE</option></select></td><td><input type='checkbox' name='primary'/></td><td><input type='checkbox' name='index'/></td><td><input class='add' type='button' value='Add More'/></td></tr>";
                $("tr:last").after(appendTxt);
            });

            $("#deleteTableButton").on("click", function () {
                $("#table-delete-confirm").dialog({
                                                      resizable: false,
                                                      height: 140,
                                                      modal: true,
                                                      buttons: {
                                                          "Delete Table": function () {
                                                              $.post('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeDeleteTable, {tableName: $("#tableSelect").val()},
                                                                     function (result) {
                                                                         $("deleteTableMessage").innerHTML = result;
                                                                     });
                                                              $(this).dialog("close");
                                                          },
                                                          Cancel: function () {
                                                              $(this).dialog("close");
                                                          }
                                                      }
                                                  });
                $("#table-delete-confirm").dialog("open");
                return false;
            });

            $("#editTableButton").on("click", function () {
                $.post('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeGetTableInfo, {tableName: $("#tableSelect").val()},
                       function (result) {
                           var resultArray = jQuery.parseJSON(result);
                           var arrayLength = resultArray.length;
                           $("#column-details").find('tr').slice(1, document.getElementById('column-details').rows.length - 1).remove();
                           var table = document.getElementById('column-details');
                           $('#createTableDialog').dialog('option', 'title', 'Edit table');
                           document.getElementById('createTablePopup').style.display = 'block';
                           document.getElementById('msgLabel').style.display = 'none';

                           document.getElementById("tableName").value = $("#tableSelect").val();
                           document.getElementById("tableName").readOnly = true;

                           var tbody = table.getElementsByTagName('tbody')[0];

                           for (var i = 0; i < arrayLength; i++) {
                               var cellNo = 0;
                               var rowCount = table.rows.length - 2;
                               var row = tbody.insertRow(rowCount);

                               var columnCell = row.insertCell(cellNo++);
                               var columnInputElement = document.createElement('input');
                               columnInputElement.name = "column";
                               columnInputElement.type = "text";
                               columnInputElement.value = resultArray[i].column;
                               columnCell.appendChild(columnInputElement);

                               var typeCell = row.insertCell(cellNo++);
                               var selectElement = document.createElement('select');
                               selectElement.options[0] = new Option('STRING', 'STRING');
                               selectElement.options[1] = new Option('INTEGER', 'INTEGER');
                               selectElement.options[2] = new Option('LONG', 'LONG');
                               selectElement.options[3] = new Option('BOOLEAN', 'BOOLEAN');
                               selectElement.options[4] = new Option('FLOAT', 'FLOAT');
                               selectElement.options[5] = new Option('DOUBLE', 'DOUBLE');
                               selectElement.value = resultArray[i].type;
                               typeCell.appendChild(selectElement);

                               var primaryCell = row.insertCell(cellNo++);
                               var primaryCheckElement = document.createElement('input');
                               primaryCheckElement.type = "checkbox";
                               primaryCheckElement.checked = resultArray[i].primary;
                               primaryCell.appendChild(primaryCheckElement);

                               var indexCell = row.insertCell(cellNo++);
                               var indexCheckElement = document.createElement('input');
                               indexCheckElement.type = "checkbox";
                               indexCheckElement.checked = resultArray[i].index;
                               indexCell.appendChild(indexCheckElement);

                               var buttonCell = row.insertCell(cellNo++);
                               var indexCheckElement = document.createElement('input');
                               indexCheckElement.type = "button";
                               indexCheckElement.value = "Delete";
                               indexCheckElement.className = "del";
                               buttonCell.appendChild(indexCheckElement);
                           }
                           tablePopupAction = 'edit';
                           $("#createTableDialog").dialog("open");
                       });
                return true;
            });
        });

        function createTable() {
            var result;
            var tableName = document.getElementById('tableName').value;
            var jsonObj = [];
            var table = document.getElementById('column-details');
            for (var r = 1, n = table.rows.length; r < n; r++) {
                var item = {};
                for (var c = 0, m = table.rows[r].cells.length; c < m; c++) {
                    if (table.rows[r].cells[c].childNodes[0].value != '') {
                        if (c == 0) {
                            item ["column"] = table.rows[r].cells[c].childNodes[0].value;
                        } else if (c == 1) {
                            item ["type"] = table.rows[r].cells[c].childNodes[0].value;
                        } else if (c == 2) {
                            item ["primary"] = table.rows[r].cells[c].childNodes[0].checked;
                        } else if (c == 3) {
                            item ["index"] = table.rows[r].cells[c].childNodes[0].checked;
                        }
                    }
                }
                jsonObj.push(item);
            }
            var values = {};
            values.tableInfo = JSON.stringify(jsonObj);
            $.ajax({
                       type: 'POST',
                       url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' +
                            typeCreateTable + "&tableName=" + tableName + "&action=" + tablePopupAction,
                       data: values,
                       success: function (data) {
                           result = true;
                           var label = document.getElementById('msgLabel');
                           label.style.display = 'block';
                           label.innerHTML = data;
                           document.getElementById('createTablePopup').style.display = 'none';
                       },
                       error: function (data) {
                           result = false;
                           var label = document.getElementById('msgLabel');
                           label.style.display = 'block';
                           label.innerHTML = data;
                       }
                   });

            return result;
        }

        function createMainJTable(fields) {
            $('#AnalyticsTableContainer').jtable({
                                                     title: $("#tableSelect").val(),
                                                     paging: true,
                                                     pageSize: 25,
                                                     selecting: true,
                                                     multiselect: true,
                                                     selectingCheckboxes: true,
                                                     actions: {
                                                         // For Details: http://jtable.org/Demo/FunctionsAsActions
                                                         listAction: function (postData, jtParams) {
                                                             return listActionMethod(jtParams);
                                                         }
                                                         <c:if test="${permissions != null && permissions.isPutRecord()}">
                                                         // Don't remove this leading comma
                                                         , createAction: function (postData) {
                                                             return createActionMethod(postData);
                                                         },
                                                         updateAction: function (postData) {
                                                             return updateActionMethod(postData);
                                                         }
                                                         </c:if>
                                                         <c:if test="${permissions != null && permissions.isDeleteRecord()}">
                                                         // Don't remove this leading comma
                                                         , deleteAction: function (postData) {
                                                             return deleteActionMethod(postData);
                                                         }
                                                         </c:if>
                                                     },
                                                     formCreated: function (event, data) {
                                                         timestamp = data.record.bam_rec_timestamp;
                                                     },
                                                     fields: fields

                                                 });
            $('#AnalyticsTableContainer').jtable('load');
            $("#DeleteAllButton").show();
            $("#DeleteAllButton").on("click", function () {
                var $selectedRows = $('#AnalyticsTableContainer').jtable('selectedRows');
                $('#AnalyticsTableContainer').jtable('deleteRows', $selectedRows);
            });
            tableLoaded = true;
        }
    </script>

</head>
<body>
<c:if test="${permissionError != null}">
    <div>
        <p><c:out value="${permissionError.message}"/></p>
    </div>
</c:if>
<div>
    <label id="deleteTableMessage"></label>
</div>

<div id="table-delete-confirm" title="Remove entire table?" style="display: none">
    <p><span class="ui-icon ui-icon-alert" style="float:left; margin:0 7px 20px 0;"></span>
        This will be permanently delete entire table. Are you sure? </p>
</div>

<fieldset>
    <legend>Search:</legend>
    <c:if test="${permissions != null && permissions.isListTable() && permissions.isListRecord()}">
        <label> Table Name*:
            <select id="tableSelect" onchange="tableSelectChange()">
                <option value="-1">Select a table</option>
            </select>
        </label>
        <c:if test="${permissions != null && permissions.isDropTable()}">
            <input type="button" id="deleteTableButton" value="Delete Table" style="display: none">
        </c:if>
        <c:if test="${permissions != null && permissions.isCreateTable()}">
            <input type="button" id="editTableButton" value="Edit Table" style="display: none">
        </c:if>
        <fieldset>
            <legend>By Date Range:</legend>
            <label> From: <input id="timeFrom" type="text"> </label>
            <label> To: <input id="timeTo" type="text"> </label>
        </fieldset>
        <c:if test="${permissions != null && permissions.isSearchRecord()}">
            <fieldset>
                <legend>By Query:</legend>
                <label> Search Query:
                    <textarea id="query" rows="4" cols="50"></textarea>
                </label>
            </fieldset>
        </c:if>
        <input id="search" type="submit" value="Search" onclick="createJTable();">
    </c:if>
</fieldset>

<div id="AnalyticsTableContainer"></div>
<input type="button" id="DeleteAllButton" value="Delete all selected records">
<c:if test="${permissions != null && permissions.isCreateTable()}">
    <input type="button" id="addNewTable" value="Add New Table">
</c:if>

<div id="createTableDialog" title="Create a new table">
    <div id="msg">
        <label id="msgLabel" style="display: none"></label>
    </div>
    <div id="createTablePopup">
        <form class="noteform" id="notesmodal" action="javascript:createTable()">
            <label> Table Name:
                <input type="text" id="tableName">
            </label>
            <table id="column-details">
                <thead>
                <tr>
                    <th>Column</th>
                    <th>Type</th>
                    <th>Primary key</th>
                    <th>Index</th>
                    <th></th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td><input type="text" name="column"/></td>
                    <td><select>
                        <option value="STRING">STRING</option>
                        <option value="INTEGER">INTEGER</option>
                        <option value="LONG">LONG</option>
                        <option value="BOOLEAN">BOOLEAN</option>
                        <option value="FLOAT">FLOAT</option>
                        <option value="DOUBLE">DOUBLE</option>
                    </select></td>
                    <td><input type="checkbox" name="primary"/></td>
                    <td><input type="checkbox" name="index"/></td>
                    <td><input class="add" type="button" value="Add More"/></td>
                </tr>
                </tbody>
            </table>
            <input type="submit" value="Submit">
        </form>
    </div>
</div>
</body>
</html>