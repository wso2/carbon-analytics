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
        pageContext.setAttribute("isPaginationSupported", connector.isPaginationSupported(), PageContext.APPLICATION_SCOPE);
    } catch (MessageConsoleException e) {
        pageContext.setAttribute("permissionError", e, PageContext.PAGE_SCOPE);
    }
%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate" />
    <meta http-equiv="Pragma" content="no-cache" />
    <meta http-equiv="Expires" content="0" />

    <link href="js/jquery-ui.min.css" rel="stylesheet" type="text/css"/>
    <link href="../dialog/css/jqueryui/jqueryui-themeroller.css" rel="stylesheet" type="text/css"/>
    <link href="../dialog/css/dialog.css" rel="stylesheet" type="text/css"/>
    <link href="js/jquery.datetimepicker.css" rel="stylesheet" type="text/css"/>
    <link href="themes/metro/blue/jtable.css" rel="stylesheet" type="text/css"/>
    <link href="js/validationEngine.jquery.css" rel="stylesheet" type="text/css"/>

    <script src="js/jquery-1.11.2.min.js" type="text/javascript"></script>
    <script src="js/jquery-ui.min.js" type="text/javascript"></script>
    <script src="js/jquery.validationEngine.js" type="text/javascript"></script>
    <script src="js/jquery.validationEngine-en.js" type="text/javascript"></script>
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
        var typeGetPurgingTask = '<%= MessageConsoleConnector.TYPE_GET_PURGING_TASK_INFO%>';
        var typeSavePurgingTask = '<%= MessageConsoleConnector.TYPE_SAVE_PURGING_TASK_INFO%>';
        var typeListTable = '<%= MessageConsoleConnector.TYPE_LIST_TABLE%>';

        var tablePopupAction;

        function loadTableSelect() {
            $('#tableSelect').find('option:gt(0)').remove();
            <c:if test="${permissions != null && permissions.isListTable()}">
            $.get('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeListTable,
                  function (result) {
                      var resultObj = jQuery.parseJSON(result);
                      var tableNames = "";
                      $(resultObj).each(function (key, tableName) {
                          tableNames += "<option value=" + tableName + ">" + tableName + "</option>";
                      });
                      $("#tableSelect").append(tableNames);
                  });
            </c:if>
            $("#deleteTableButton").hide();
            $("#editTableButton").hide();
            $("#purgeRecordButton").hide();
            try {
                $('#DeleteAllButton').hide();
                if (tableLoaded == true) {
                    $('#AnalyticsTableContainer').jtable('destroy');
                    tableLoaded = false;
                }
            } catch (err) {
            }
        }

        $(document).ready(function () {
            $.ajaxSetup({cache: false});
            loadTableSelect();
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
            jQuery('#dataPurgingScheudleTime').datetimepicker({
                datepicker: false,
                format: 'H:i'
            });

            $("#createTableDialog").dialog({
                autoOpen: false
            });
            $("#purgeRecordDialog").dialog({
                autoOpen: false
            });

            $("#tableForm").validationEngine();
            $("#dataPurgingForm").validationEngine();

            $("#addNewTable").on("click", function () {
                $("#column-details").find('tr').slice(1, document.getElementById('column-details').rows.length - 1).remove();
                $("#column-details").find("input[type=text]").val("");
                $('#tableForm')[0].reset();
                $('#createTableDialog').dialog({
                    title: 'Create a new table...',
                    height: 300,
                    width: 500,
                    modal: true,
                    resizable: true
                });
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
                        "<tr><td><input type='text' name='column'/></td><td><select><option value='STRING'>STRING</option><option value='INTEGER'>INTEGER</option><option value='LONG'>LONG</option><option value='BOOLEAN'>BOOLEAN</option><option value='FLOAT'>FLOAT</option><option value='DOUBLE'>DOUBLE</option><c:if test="${permissions != null && permissions.isSetIndex()}"><option value='FACET'>FACET</option></c:if></select></td><td><input type='checkbox' name='primary'/></td><c:if test="${permissions != null && permissions.isSetIndex()}"><td><input type='checkbox' name='index'/></td><td><input type='checkbox' name='scoreParam'/></td></c:if><td><input class='add' type='button' value='Add More'/></td></tr>";
                $("#column-details tbody tr:last").after(appendTxt);
            });

            $("#deleteTableButton").on("click", function () {
                $("#table-delete-confirm").dialog({
                                                      height: 300,
                                                      width: 500,
                                                      modal: true,
                                                      resizable: true,
                                                      buttons: {
                                                          "Delete Table": function () {
                                                              $.post('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeDeleteTable, {tableName: $("#tableSelect").val()},
                                                                     function (result) {
                                                                         var label = document.getElementById('deleteTableMsgLabel');
                                                                         label.style.display = 'block';
                                                                         label.innerHTML = result;
                                                                         loadTableSelect();
                                                                         $('#deleteTableMsg').fadeOut(10000);
                                                                         $('#DeleteAllButton').hide();
                                                                         try {
                                                                             if (tableLoaded == true) {
                                                                                 $('#AnalyticsTableContainer').jtable('destroy');
                                                                                 tableLoaded = false;
                                                                             }
                                                                         } catch (err) {
                                                                         }

                                                                     });
                                                              $(this).dialog("close");
                                                          },
                                                          Cancel: function () {
                                                              $(this).dialog("close");
                                                          }
                                                      }
                                                  });
                $("#table-delete-confirm").dialog("open");
                return true;
            });

            $("#purgeRecordButton").on("click", function () {
                var label = document.getElementById('dataPurgingMsgLabel');
                label.style.display = 'none';
                $.post('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeGetPurgingTask,
                        {tableName: $("#tableSelect").val()},
                       function (result) {
                           var resultObj = jQuery.parseJSON(result);
                           $("#purgeRecordDialog").dialog("open");
                           $('#dataPurgingScheudleTime').val(resultObj.cronString);
                           $('#dataPurgingScheudleTime').prop('disabled', false);
                           $('#dataPurgingDay').val(resultObj.retentionPeriod);
                           $('#dataPurgingDay').prop('disabled', false);
                           $('#dataPurgingCheckBox').prop("checked", true);
                           $('#dataPurgingScheudleTime').datepicker("destroy");
                       });
                return true;
            });

            $("#editTableButton").on("click", function () {
                $.post('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeGetTableInfo, {tableName: $("#tableSelect").val()},
                       function (result) {
                           var resultArray = jQuery.parseJSON(result);
                           var arrayLength = resultArray.length;
                           $("#column-details").find('tr').slice(1, document.getElementById('column-details').rows.length - 1).remove();
                           $('#tableForm')[0].reset();
                           var table = document.getElementById('column-details');
                           $('#createTableDialog').dialog({
                                 title: 'Edit table',
                                 height: 300,
                                 width: 500,
                                 modal: true,
                                 resizable: true
                             });
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
                               <c:if test="${permissions != null && permissions.isSetIndex()}">
                               selectElement.options[5] = new Option('FACET', 'FACET');
                               </c:if>
                               selectElement.value = resultArray[i].type;
                               typeCell.appendChild(selectElement);

                               var primaryCell = row.insertCell(cellNo++);
                               var primaryCheckElement = document.createElement('input');
                               primaryCheckElement.type = "checkbox";
                               primaryCheckElement.checked = resultArray[i].primary;
                               primaryCell.appendChild(primaryCheckElement);

                               <c:if test="${permissions != null && permissions.isSetIndex()}">
                               var indexCell = row.insertCell(cellNo++);
                               var indexCheckElement = document.createElement('input');
                               indexCheckElement.type = "checkbox";
                               indexCheckElement.checked = resultArray[i].index;
                               indexCell.appendChild(indexCheckElement);

                               var scoreParamCell = row.insertCell(cellNo++);
                               var scoreParamCheckElement = document.createElement('input');
                               scoreParamCheckElement.type = "checkbox";
                               scoreParamCheckElement.checked = resultArray[i].scoreParam;
                               scoreParamCell.appendChild(scoreParamCheckElement);
                               </c:if>

                               var buttonCell = row.insertCell(cellNo++);
                               var delButtonElement = document.createElement('input');
                               delButtonElement.type = "button";
                               delButtonElement.value = "Delete";
                               delButtonElement.className = "del";
                               buttonCell.appendChild(delButtonElement);
                           }
                           tablePopupAction = 'edit';
                           $("#createTableDialog").dialog("open");
                       });
                return true;
            });

            $('#dataPurgingCheckBox').change(function () {
                if ($(this).is(":checked")) {
                    $("#dataPurgingScheudleTime").prop('disabled', false);
                    $("#dataPurgingDay").prop('disabled', false);
                } else {
                    $("#dataPurgingScheudleTime").prop('disabled', true);
                    $("#dataPurgingDay").prop('disabled', true);
                }
            });
        });

        function createTable() {
            if (!$("#tableForm").validationEngine('validate')) {
                return false;
            }
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
                        } else if (c == 4) {
                            item ["scoreParam"] = table.rows[r].cells[c].childNodes[0].checked;
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
                    loadTableSelect();
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
                <c:choose>
                <c:when test="${isPaginationSupported}">
                paging: true,
                pageSize: 25,
                </c:when>
                <c:otherwise>
                paging: false,
                pageSize: 500,
                </c:otherwise>
                </c:choose>
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
                    data.form.validationEngine();
                },
                //Validate form when it is being submitted
                formSubmitting: function (event, data) {
                    return data.form.validationEngine('validate');
                },
                //Dispose validation logic when form is closed
                formClosed: function (event, data) {
                    data.form.validationEngine('hide');
                    data.form.validationEngine('detach');
                },
                fields: fields

            });
            $('#AnalyticsTableContainer').jtable('load');
            $("#DeleteAllButton").show();
            $("#DeleteAllButton").on("click", function () {
                var $selectedRows = $('#AnalyticsTableContainer').jtable('selectedRows');
                $('#AnalyticsTableContainer').jtable('deleteRows', $selectedRows);
                $('#AnalyticsTableContainer').jtable('reload');
            });
            tableLoaded = true;
             $("#resultsTable").show();
        }

        function getArbitraryFields(rowData) {
            var $img =
                    $('<img src="/carbon/messageconsole/themes/metro/list_metro.png" title="Show Arbitrary Fields"/>');
            $img.click(function () {
                $('#AnalyticsTableContainer').jtable('openChildTable',
                                                     $img.closest('tr'), //Parent row
                        {
                            title: 'Arbitrary Fields',
                            paging: false,
                            selecting: true,
                            messages: {
                                addNewRecord: 'Add new arbitrary field'
                            },
                            actions: {
                                // For Details: http://jtable.org/Demo/FunctionsAsActions
                                listAction: function (postData, jtParams) {
                                    var postData = {};
                                    postData['tableName'] = $("#tableSelect").val();
                                    postData['bam_unique_rec_id'] = rowData.record.bam_unique_rec_id;
                                    return arbitraryFieldListActionMethod(postData, jtParams);
                                }
                                <c:if test="${permissions != null && permissions.isPutRecord()}">
                                ,
                                createAction: function (postData) {
                                    return arbitraryFieldCreateActionMethod(postData);
                                },
                                updateAction: function (postData) {
                                    return arbitraryFieldUpdateActionMethod(postData);
                                }
                                </c:if>
                                <c:if test="${permissions != null && permissions.isDeleteRecord()}">,
                                deleteAction: function (postData) {
                                    postData['tableName'] = $("#tableSelect").val();
                                    postData['bam_unique_rec_id'] = rowData.record.bam_unique_rec_id;
                                    return arbitraryFieldDeleteActionMethod(postData);
                                }
                                </c:if>
                            },
                            deleteConfirmation: function (data) {
                                arbitraryColumnName = data.record.Name;
                            },
                            rowsRemoved: function (event, data) {
                                arbitraryColumnName = "";
                            },
                            formCreated: function (event, data) {
                                data.form.validationEngine();
                            },
                            //Validate form when it is being submitted
                            formSubmitting: function (event, data) {
                                return data.form.validationEngine('validate');
                            },
                            //Dispose validation logic when form is closed
                            formClosed: function (event, data) {
                                data.form.validationEngine('hide');
                                data.form.validationEngine('detach');
                            },
                            fields: {
                                bam_unique_rec_id: {
                                    type: 'hidden',
                                    key: true,
                                    list: false,
                                    defaultValue: rowData.record.bam_unique_rec_id
                                },
                                Name: {
                                    title: 'Name',
                                    inputClass: 'validate[required]'
                                },
                                Value: {
                                    title: 'Value'
                                },
                                Type: {
                                    title: 'Type',
                                    options: ["STRING", "INTEGER", "LONG", "BOOLEAN", "FLOAT", "DOUBLE", "FACET"],
                                    list: false
                                }
                            }
                        },
                                                     function (data) { //opened handler
                                                         data.childTable.jtable('load');
                                                     }
                );
            });
            return $img;
        }

        function createJTable() {
            var table = $("#tableSelect").val();
            if (table != '-1') {
                $.getJSON("/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=" + typeTableInfo + "&tableName=" + table,
                          function (data, status) {
                              var fields = {
                                  ArbitraryFields: {
                                      title: '',
                                      width: '2%',
                                      sorting: false,
                                      edit: false,
                                      create: false,
                                      display: function (rowData) {
                                          return getArbitraryFields(rowData);
                                      }
                                  }
                              };
                              $.each(data.columns, function (key, val) {
                                  if (val.type == 'BOOLEAN') {
                                      fields[val.name] = {
                                          title: val.name,
                                          list: val.display,
                                          key: val.key,
                                          type: 'checkbox',
                                          defaultValue: 'false',
                                          values: {'false': 'False', 'true': 'True'}
                                      };
                                  } else {
                                      fields[val.name] = {
                                          title: val.name,
                                          list: val.display,
                                          key: val.key
                                      };
                                      if (val.type == 'STRING') {
                                          fields[val.name].type = 'textarea';
                                      } else if (val.type == 'INTEGER' || val.type == 'LONG') {
                                          fields[val.name].inputClass = 'validate[custom[integer]]';
                                      } else if (val.type == 'FLOAT' || val.type == 'DOUBLE') {
                                          fields[val.name].inputClass = 'validate[custom[number]]';
                                      }
                                  }
                                  if (val.name == 'bam_unique_rec_id' || val.name == 'bam_rec_timestamp') {
                                      fields[val.name].edit = false;
                                      fields[val.name].create = false;
                                  }
                              });
                              if (data) {
                                  if (tableLoaded == true) {
                                      $('#AnalyticsTableContainer').jtable('destroy');
                                      tableLoaded = false;
                                  }
                                  createMainJTable(fields);
                              }
                          }
                );
            }
        }
    </script>

</head>
<body>
<fmt:bundle basename="org.wso2.carbon.analytics.messageconsole.ui.i18n.Resources">
<carbon:breadcrumb label="MenuName"
                   resourceBundle="org.wso2.carbon.analytics.messageconsole.ui.i18n.Resources"
                   topPage="true" request="<%=request%>"/>
<div id="middle">
    <h2>Message Console</h2>

    <div id="workArea">
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
        <c:if test="${permissions != null && permissions.isCreateTable()}">
            <table class="normal" width="100%">
                <tbody>
                <tr>
                    <td><input type="button" id="addNewTable" value="Add New Table" class="button"></td>
                    <td>&nbsp;</td>
                </tr>
                </tbody>
            </table>
            <br/>
        </c:if>
        <div id="deleteTableMsg">
            <label id="deleteTableMsgLabel" style="display: none"></label>
        </div>
        <table class="styledLeft">
            <thead>
            <tr>
                <th>Search</th>
            </tr>
            </thead>
            <tbody>

            <c:if test="${permissions != null && permissions.isListTable()}">
                <tr>
                    <td class="formRow">
                        <table class="normal" width="100%">
                            <tbody>
                            <tr>
                                <td width="10%">Table Name*</td>
                                <td>
                                    <select id="tableSelect" onchange="tableSelectChange()">
                                        <option value="-1">Select a Table</option>
                                    </select>
                                    <c:if test="${permissions != null && permissions.isDropTable()}">
                                        <input type="button" id="deleteTableButton" value="Delete Table" class="button"
                                               style="display: none">
                                    </c:if>
                                    <c:if test="${permissions != null && permissions.isCreateTable()}">
                                        <input type="button" id="editTableButton" value="Edit Table" class="button"
                                               style="display: none">
                                    </c:if>
                                    <c:if test="${permissions != null && permissions.isDeleteRecord()}">
                                        <input type="button" id="purgeRecordButton" value="Schedule Data Purging"
                                               style="display: none">
                                    </c:if>
                                </td>
                            </tr>
                            <c:if test="${permissions != null && permissions.isListRecord()}">
                                <tr>
                                    <td>By Date Range</td>
                                    <td>
                                        <label> From: <input id="timeFrom" type="text"> </label>
                                        <label> To: <input id="timeTo" type="text"> </label>
                                    </td>
                                </tr>
                            </c:if>
                            <c:if test="${permissions != null && permissions.isSearchRecord()}">
                                <tr>
                                    <td>By Query</td>
                                    <td>
                                        Search Query: <textarea id="query" rows="1" cols="100"></textarea>
                                    </td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <c:if test="${permissions != null && permissions.isListRecord()}">
                    <tr>
                        <td class="buttonRow">
                            <input id="search" type="button" value="Search" onclick="createJTable();" class="button">
                        </td>
                    </tr>
                </c:if>
            </c:if>
            </tbody>
        </table>
        <br/>
        <table id="resultsTable" class="styledLeft">
            <thead>
            <tr>
                <th>Results</th>
            </tr>
            </thead>
            <tbody>
            <tr>
                <td>
                    <div id="AnalyticsTableContainer" class="overflowAuto"></div>
                </td>
            </tr>
            <c:if test="${permissions != null && permissions.isDeleteRecord()}">
                <tr>
                    <td class="buttonRow">
                        <input type="button" id="DeleteAllButton" value="Delete all selected records" class="button">
                    </td>
                </tr>
            </c:if>
            </tbody>
        </table>
        <br/>
    </div>
    <br/>

    <div id="createTableDialog" title="Create a new table">
        <div id="msg">
            <label id="msgLabel" style="display: none"></label>
        </div>
        <div id="createTablePopup">
            <form class="noteform" id="tableForm" action="javascript:createTable()">
                <table class="normal">
                    <tbody>
                    <tr>
                        <td>Table Name</td>
                        <td>
                            <input type="text" id="tableName" class="validate[required]">
                        </td>
                    </tr>
                    </tbody>
                </table>
                <br/>
                <table id="column-details" class="styledLeft">
                    <thead>
                    <tr>
                        <th>Column</th>
                        <th>Type</th>
                        <th>Primary key</th>
                        <c:if test="${permissions != null && permissions.isSetIndex()}">
                            <th>Index</th>
                            <th>Score Param</th>
                        </c:if>
                        <th>&nbsp;</th>
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
                            <c:if test="${permissions != null && permissions.isSetIndex()}">
                                <option value="FACET">FACET</option>
                            </c:if>
                        </select></td>
                        <td><input type="checkbox" name="primary"/></td>
                        <c:if test="${permissions != null && permissions.isSetIndex()}">
                            <td><input type="checkbox" name="index"/></td>
                            <td><input type="checkbox" name="scoreParam"/></td>
                        </c:if>
                        <td><input class="add" type="button" value="Add More"/></td>
                    </tr>
                    </tbody>
                </table>
                <table class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="buttonRow">
                            <input type="submit" value="Submit" class="button">
                        </td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
    <div id="purgeRecordDialog" title="Schedule Data Purging">
        <div id="dataPurgingMsg">
            <label id="dataPurgingMsgLabel" style="display: none"></label>
        </div>
        <div id="purgeRecordPopup">
            <form id="dataPurgingForm" action="javascript:scheduleDataPurge()">
                <label> Enable Data Purging:
                    <input type="checkbox" id="dataPurgingCheckBox">
                </label>
                <br/>
                <label> Schedule Time:
                    <input type="text" id="dataPurgingScheudleTime" disabled="disabled" class="validate[required]">
                </label>
                <br/>
                <label> Purge Record Older Than (Days):
                    <input type="text" id="dataPurgingDay" disabled="disabled"
                           class="validate[custom[integer],min[1]]" value="1">
                </label>
                <input type="submit" value="Save">
            </form>
        </div>
    </div>
    </fmt:bundle>
</body>
</html>