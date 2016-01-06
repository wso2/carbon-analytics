<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.MessageConsoleConnector" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.beans.Permissions" %>
<%@ page import="org.wso2.carbon.messageconsole.ui.exception.MessageConsoleException" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
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
    <meta http-equiv="Cache-Control" content="no-cache, no-store, must-revalidate">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Expires" content="0">

    <link href="js/jquery-ui.min.css" rel="stylesheet" type="text/css"/>
    <link href="../dialog/css/jqueryui/jqueryui-themeroller.css" rel="stylesheet" type="text/css"/>
    <link href="../dialog/css/dialog.css" rel="stylesheet" type="text/css"/>
    <link href="themes/metro/blue/jtable.css" rel="stylesheet" type="text/css"/>
    <link href="js/validationEngine.jquery.css" rel="stylesheet" type="text/css"/>

    <script src="js/jquery-1.11.2.min.js" type="text/javascript"></script>
    <script src="js/jquery-ui.min.js" type="text/javascript"></script>
    <script src="js/jquery.validationEngine.js" type="text/javascript"></script>
    <script src="js/jquery.validationEngine-en.js" type="text/javascript"></script>
    <script src="js/jquery.jtable.min.js" type="text/javascript"></script>
    <script src="js/messageconsole.js?version=<%= java.lang.Math.round(java.lang.Math.random() * 2) %>"
            type="text/javascript"></script>
    <script type="text/javascript" src="js/jquery-ui-timepicker-addon.min.js"></script>
    <script type="text/javascript" src="js/jquery-ui-timepicker-addon.js"></script>
    <link href="js/jquery-ui-timepicker-addon.min.css" type="text/css" rel="stylesheet"/>
    <link href="js/jquery-ui-timepicker-addon.css" type="text/css" rel="stylesheet"/>

    <script type="text/javascript">
        var typeListRecord = '<%= MessageConsoleConnector.TYPE_LIST_RECORD%>';
        var typeTableInfo = '<%= MessageConsoleConnector.TYPE_TABLE_INFO%>';
        var typeListArbitraryRecord = '<%= MessageConsoleConnector.TYPE_LIST_ARBITRARY_RECORD%>';
        var typeGetTableInfo = '<%= MessageConsoleConnector.TYPE_GET_TABLE_INFO%>';
        var typeGetPurgingTask = '<%= MessageConsoleConnector.TYPE_GET_PURGING_TASK_INFO%>';
        var typeSavePurgingTask = '<%= MessageConsoleConnector.TYPE_SAVE_PURGING_TASK_INFO%>';
        var typeListTable = '<%= MessageConsoleConnector.TYPE_LIST_TABLE%>';
        var typeGetFacetNameList = '<%= MessageConsoleConnector.TYPE_GET_FACET_NAME_LIST%>';
        var typeGetFacetCategories = '<%= MessageConsoleConnector.TYPE_GET_FACET_CATEGORIES%>';
        var typeGetPrymaryKeyList = '<%= MessageConsoleConnector.TYPE_GET_PRIMARY_KEY_LIST%>';
        var typeCheckTotalCount = '<%= MessageConsoleConnector.TYPE_CHECK_TOTAL_COUNT_SUPPORT%>';

        var tablePopupAction;

        function loadTableSelect() {
            $('#tableSelect').find('option:gt(0)').remove();
            <c:if test="${permissions != null && permissions.isListTable()}">
            $.get('/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeListTable,
                  function (result) {
                      var resultObj = jQuery.parseJSON(result);
                      var tableNames = "";
                      $(resultObj).each(function (key, tableName) {
                          tableNames += "<option value=" + tableName + ">" + tableName + "</option>";
                      });
                      $("#tableSelect").append(tableNames);
                  });
            </c:if>
            $("#purgeRecordButton").hide();
            $('#facetListSelect').find('option:gt(0)').remove();
            $('#facetSearchTable tr').remove();
            $('#query').val('');
            try {
                if (tableLoaded == true) {
                    $('#AnalyticsTableContainer').jtable('destroy');
                    tableLoaded = false;
                }
            } catch (err) {
            }
        }

        $(document).ready(function () {
            loadTableSelect();
            timeFromObj = jQuery('#timeFrom').datetimepicker({
                controlType: 'select',
                oneLine: true,
                timeFormat: 'HH:mm:ss'
            });
            timeToObj = jQuery('#timeTo').datetimepicker({
                controlType: 'select',
                oneLine: true,
                timeFormat: 'HH:mm:ss'
            });
            $("#purgeRecordDialog").dialog({
                autoOpen: false
            });
            $("#dataPurgingForm").validationEngine();
            $("#purgeRecordButton").on("click", function () {
                var label = document.getElementById('dataPurgingMsgLabel');
                label.style.display = 'none';
                $.post('/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeGetPurgingTask,
                        {tableName: $("#tableSelect").val()},
                       function (result) {
                           var resultObj = jQuery.parseJSON(result);
                           if (resultObj.retentionPeriod > 0) {
                               $('#dataPurgingScheudleTime').val(resultObj.cronString);
                               $('#dataPurgingScheudleTime').prop('disabled', false);
                               $('#dataPurgingDay').val(resultObj.retentionPeriod);
                               $('#dataPurgingDay').prop('disabled', false);
                               $('#dataPurgingCheckBox').prop("checked", true);
                           }
                           $("#purgeRecordDialog").dialog("open");
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

            $('#facetListSelect').on('change', function () {
                var facetName = $(this).val();
                var exist = false;
                if (facetName != -1) {
                    $('#facetSearchTable > tbody  > tr').each(function () {
                        var row = $(this);
                        if (facetName == row.find("label").text()) {
                            exist = true;
                        }
                    });
                    if (!exist) {
                        $("#facetSearchTable").find('tbody').
                                append($('<tr>').
                                               append($('<td>').append($('<label>').text(facetName))).
                                               append($('<td>').append(function () {
                                                          var $container =
                                                                  $('<select class="facetSelect1"></select>');
                                                          $container.append($('<option>').val('-1').text('Select a category'));
                                                          $.post('/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeGetFacetCategories,
                                                                  {
                                                                      tableName: $("#tableSelect").val(),
                                                                      categoryPath: "",
                                                                      fieldName: facetName
                                                                  },
                                                                 function (result) {
                                                                     var categories = JSON.parse(result);
                                                                     $.each(categories,
                                                                            function (index,
                                                                                      val) {
                                                                                $container.append($('<option>').val(val).text(val));
                                                                            });
                                                                 }
                                                          );
                                                          return $container;
                                                      })).
                                               append($('<td>').append($('<input type="button" value="Remove" class="del">'))
                                       ));
                    }
                }
            });

            $('#facetSearchTable').on('change', '.facetSelect1', function (event) {
                var facetName = $(this).val();
                var tdLength = $(this).closest('tr').find('td').size();
                if (tdLength > 2) {
                    $(this).closest('tr').find('td').slice($(this).parents('td')[0].cellIndex + 1, tdLength - 1).remove();
                }
                if (facetName != '-1') {
                    var path = [];
                    $(this).closest('td').prevAll().find("select").each(function (index, node) {
                        path.push($(node).val());
                    });
                    path.push(facetName);
                    var field = $(this).closest('td').prevAll().find("label").text();
                    $(this).closest("tr").find('td:last').before($('<td>').append(function () {
                        var $container = $('<select class="facetSelect1"></select>');
                        $container.append($('<option>').val('-1').text('Select a category'));
                        $.post('/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeGetFacetCategories,
                                {
                                    tableName: $("#tableSelect").val(),
                                    categoryPath: JSON.stringify(path),
                                    fieldName: field
                                },
                               function (result) {
                                   var categories = JSON.parse(result);
                                   $.each(categories, function (index, val) {
                                       $container.append($('<option>').val(val).text(val));
                                   });
                               }
                        );
                        return $container;
                    }));
                }
            });

            $("#facetSearchTable tbody").on("click", ".del", function () {
                $(this).parent().parent().remove();
            });
        });
        function createMainJTable(fields) {
            $('#AnalyticsTableContainer').jtable({
                title: $("#tableSelect").val(),
                paging: true,
                pageSize: 10,
                actions: {
                    // For Details: http://jtable.org/Demo/FunctionsAsActions
                    listAction: function (postData, jtParams) {
                        return listActionMethod(jtParams);
                    }
                },
                fields: fields
            });
            $('#AnalyticsTableContainer').jtable('load');
            tableLoaded = true;
            $("#resultsTable").show();
        }

        function createJTable() {
            var table = $("#tableSelect").val();
            if (table == '-1') {
                CARBON.showErrorDialog("Please select a table", null, null);
                return;
            }
            $("#resultsTable").show();
            var workAreaWidth = $(".styledLeft").width();
            $("#AnalyticsTableContainer").width(workAreaWidth - 20);
            if (table != '-1') {
                $.getJSON("/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=" + typeTableInfo + "&tableName=" + table,
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
                                      if (val.type == 'STRING' || val.type == 'FACET') {
                                          fields[val.name].type = 'textarea';
                                      }
                                  }
                                  if (val.name == '_unique_rec_id' || val.name == '_timestamp') {
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
    <h2>Data Explorer</h2>

    <div id="workArea">
        <c:if test="${permissionError != null}">
            <div>
                <p><c:out value="${permissionError.message}"/></p>
            </div>
        </c:if>
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
                                    <c:if test="${permissions != null && permissions.isDeleteRecord()}">
                                        <input type="button" id="purgeRecordButton" class="button"
                                               value="Schedule Data Purging"
                                               style="display: none">
                                    </c:if>
                                </td>
                            </tr>
                            <tr id="resultCount" style="display: none">
                                <td width="10%">Maximum Result Count</td>
                                <td>
                                    <select id="resultCountSelect">
                                        <option value="50">50</option>
                                        <option value="100">100</option>
                                        <option value="1000" selected="selected">1000</option>
                                        <option value="10000">10000</option>
                                        <option value="100000">100000</option>
                                    </select>
                                </td>
                            </tr>
                            <tr id="searchControl" style="display: none">
                                <td>Search</td>
                                <td>
                                    <c:if test="${permissions != null && permissions.isListRecord()}">
                                        <input type="radio" name="group1" value="time" id="radioDateRange"
                                               onchange="updateSearchOption(this)" title="">By Date Range
                                        <input type="radio" name="group1" value="primary" id="radioPrimary"
                                               onchange="updateSearchOption(this)">
                                        <label for="radioPrimary" id="radioLabelPrimary">By Primary Key</label>
                                    </c:if>
                                    <c:if test="${permissions != null && permissions.isSearchRecord()}">
                                        <input type="radio" name="group1" value="query" id="radioQuery"
                                               onchange="updateSearchOption(this)">By Query
                                    </c:if>
                                </td>
                            </tr>
                            <c:if test="${permissions != null && permissions.isListRecord()}">
                                <tr id="dataRangeSearch" style="display: none">
                                    <td></td>
                                    <td>
                                        <label> From: <input id="timeFrom" type="text"> </label>
                                        <label> To: <input id="timeTo" type="text"> </label>
                                    </td>
                                </tr>
                            </c:if>
                            <c:if test="${permissions != null && permissions.isSearchRecord()}">
                                <tr id="querySearch" style="display: none">
                                    <td></td>
                                    <td>
                                        <textarea id="query" rows="4" cols="100" placeholder="Search Query"></textarea>
                                    </td>
                                </tr>
                                <tr id="facetSearchCombo" style="display: none">
                                    <td></td>
                                    <td>
                                        <select id="facetListSelect">
                                            <option value="-1">Select a Facet</option>
                                        </select>
                                    </td>
                                </tr>
                                <tr id="facetSearchTableRow" style="display: none">
                                    <td></td>
                                    <td>
                                        <table id="facetSearchTable" class="normal">
                                            <tbody></tbody>
                                        </table>
                                    </td>
                                </tr>
                            </c:if>
                            <c:if test="${permissions != null && permissions.isListRecord()}">
                                <tr id="primaryKeySearch" style="display: none">
                                    <td></td>
                                    <td>
                                        <table id="primaryKeyTable" class="normal">
                                            <tbody></tbody>
                                        </table>
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
                            <input id="reset" type="button" value="Reset" onclick="reset();" class="button">
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
                    <div>
                        <label id="searchStat"></label>
                        <label id="countLabel"
                               style="display: none; font-size: 11px !important; color: #aaaaaa; font-style: italic;">
                            Note: Total record count for the table is not available.
                        </label>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                <div id="AnalyticsTableContainer" class="overflowAuto"></div>
                </td>
            </tr>
            </tbody>
        </table>
        <br/>
    </div>
    <br/>
    <div id="purgeRecordDialog" title="Schedule Data Purging">
        <div id="dataPurgingMsg">
            <label id="dataPurgingMsgLabel" style="display: none"></label>
        </div>
        <div id="purgeRecordPopup">
            <form id="dataPurgingForm" action="javascript:scheduleDataPurge()">
                <table class="normal">
                    <tbody>
                    <tr>
                        <td>Enable Data Purging</td>
                        <td><input type="checkbox" id="dataPurgingCheckBox"></td>
                    </tr>
                    <tr>
                        <td>Schedule Time (Either cron string or HH:MM)*:</td>
                        <td><input type="text" id="dataPurgingScheudleTime" disabled="disabled"
                                   class="validate[required]" data-prompt-position="bottomLeft"></td>
                    </tr>
                    <tr>
                        <td>Purge Record Older Than (Days)*:</td>
                        <td><input type="text" id="dataPurgingDay" disabled="disabled"
                                   class="validate[required,custom[integer],min[1]]" data-prompt-position="bottomLeft"></td>
                    </tr>
                    </tbody>
                </table>
                <table class="styledLeft">
                    <tbody>
                    <tr>
                        <td class="buttonRow"><input type="submit" value="Save"></td>
                    </tr>
                    </tbody>
                </table>
            </form>
        </div>
    </div>
</div>
</fmt:bundle>
</body>
</html>