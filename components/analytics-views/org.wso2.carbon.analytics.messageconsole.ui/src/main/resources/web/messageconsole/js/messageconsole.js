var tableLoaded = false;
var arbitraryColumnName;

function getArbitraryFields(rowData) {
    var $img =
            $('<img src="/carbon/messageconsole/themes/metro/list_metro.png" title="Show Arbitrary Fields"/>');
    $img.click(function () {
        $('#AnalyticsTableContainer').jtable('openChildTable',
                                             $img.closest('tr'), //Parent row
                {
                    title: 'Arbitrary Fields',
                    selecting: true,
                    messages: {
                        addNewRecord: 'Add new arbitrary field'
                    },
                    actions: {
                        // For Details: http://jtable.org/Demo/FunctionsAsActions
                        listAction: function (postData, jtParams) {
                            var postData = {};
                            postData['tableName'] = $("#tableSelect").val();
                            postData['_unique_rec_id'] = rowData.record._unique_rec_id;
                            return arbitraryFieldListActionMethod(postData, jtParams);
                        }
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
                        _unique_rec_id: {
                            type: 'hidden',
                            key: true,
                            list: false,
                            defaultValue: rowData.record._unique_rec_id
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
                            options: ["STRING", "INTEGER", "LONG", "BOOLEAN", "FLOAT", "DOUBLE"],
                            list: false
                        }
                    }
                }, function (data) { //opened handler
                    data.childTable.jtable('load');
                }
        );
    });
    return $img;
}

function arbitraryFieldListActionMethod(postData, jtParams) {
    return $.Deferred(function ($dfd) {
        $.ajax({
                    url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeListArbitraryRecord,
                    type: 'POST',
                    dataType: 'json',
                    data: postData,
                    success: function (data) {
                        $dfd.resolve(data);
                    },
                    error: function () {
                        $dfd.reject();
                    }
                }
        );
    });
}

function listActionMethod(jtParams) {
    var postData = {};
    var fromTime = document.getElementById('timeFrom').value;
    var toTime = document.getElementById('timeTo').value;
    var fromTimeStamp, toTimeStamp;
    if (fromTime != '') {
        fromTimeStamp = jQuery('#timeFrom').datetimepicker("getDate").getTime();
    }
    if (toTime != '') {
        toTimeStamp = jQuery('#timeTo').datetimepicker("getDate").getTime();
    }
    postData["jtStartIndex"] = jtParams.jtStartIndex;
    postData["jtPageSize"] = jtParams.jtPageSize;
    postData["tableName"] = $("#tableSelect").val();
    postData["timeFrom"] = fromTimeStamp;
    postData["timeTo"] = toTimeStamp;
    postData["query"] = $("#query").val();
    var facets = [];
    $('#facetSearchTable > tbody  > tr').each(function () {
        var facet = {};
        var row = $(this);
        facet.field = row.find("label").text();
        facet.path = [];
        row.find("select").each(function (index, node) {
            if ($(node).val() != '-1') {
                facet.path.push($(node).val());
            }
        });
        facets.push(facet);
    });
    postData["facets"] = JSON.stringify(facets);
    var primaryKeys = [];
    $('#primaryKeyTable > tbody  > tr').each(function () {
        var primary = {};
        var row = $(this);
        primary.key = row.find("label").text();
        primary.value = row.find("input").val();
        if (primary.value != '') {
            primaryKeys.push(primary);
        }
    });
    postData["primary"] = JSON.stringify(primaryKeys);
    return $.Deferred(function ($dfd) {
        $.ajax({
                    url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeListRecord,
                    type: 'POST',
                    dataType: 'json',
                    data: postData,
                    success: function (data) {
                        $dfd.resolve(data);
                    },
                    error: function () {
                        $dfd.reject();
                    }
                }
        );
    });
}

function tableSelectChange() {
    var table = $("#tableSelect").val();
    if (table != '-1') {
        $("#purgeRecordButton").show();
        loadFacetNames();
        loadPrimaryKeys();
    } else {
        $("#purgeRecordButton").hide();
        $('#facetListSelect').find('option:gt(0)').remove();
        $('#query').val('');
        document.getElementById('facetSearchCombo').style.display = 'none';
        document.getElementById('facetSearchTableRow').style.display = 'none';
    }
    try {
        $('#DeleteAllButton').hide();
        if (tableLoaded == true) {
            $('#AnalyticsTableContainer').jtable('destroy');
            tableLoaded = false;
        }
    } catch (err) {
    }
}

function scheduleDataPurge() {
    if ($('#dataPurgingCheckBox').is(":checked")) {
        if (!$("#dataPurgingForm").validationEngine('validate')) {
            return false;
        }
    }
    $.post('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeSavePurgingTask,
            {
                tableName: $("#tableSelect").val(),
                cron: $('#dataPurgingScheudleTime').val(),
                retention: $('#dataPurgingDay').val(),
                enable: $('#dataPurgingCheckBox').is(":checked")
            },
           function (result) {
               var label = document.getElementById('dataPurgingMsgLabel');
               label.style.display = 'block';
               label.innerHTML = result;
           }
    );
    return true;
}

function loadFacetNames() {
    $('#facetSearchTable tr').remove();
    $('#facetListSelect').find('option:gt(0)').remove();
    $.get('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeGetFacetNameList + '&tableName=' + $("#tableSelect").val(),
          function (result) {
              var resultObj = jQuery.parseJSON(result);
              var facetNames = "";
              $(resultObj).each(function (key, tableName) {
                  facetNames += "<option value=" + tableName + ">" + tableName + "</option>";
              });
              if (resultObj.length > 0) {
                  document.getElementById('facetSearchCombo').style.display = '';
                  document.getElementById('facetSearchTableRow').style.display = '';
              } else {
                  document.getElementById('facetSearchCombo').style.display = 'none';
                  document.getElementById('facetSearchTableRow').style.display = 'none';
              }
              $("#facetListSelect").append(facetNames);
          }
    );
}

function loadPrimaryKeys() {
    $('#primaryKeyTable tr').remove();
    $.get('/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeGetPrymaryKeyList + '&tableName=' + $("#tableSelect").val(),
          function (result) {
              var resultObj = jQuery.parseJSON(result);
              $(resultObj).each(function (key, columnName) {
                  $("#primaryKeyTable").find('tbody').
                          append($('<tr>').
                                         append($('<td>').append($('<label>').text(columnName))).
                                         append($('<td>').append('<input id="' + columnName + '" type="text">'))
                  );
              });
              if (resultObj.length > 0) {
                  document.getElementById('primaryKeySearch').style.display = '';
              } else {
                  document.getElementById('primaryKeySearch').style.display = 'none';
              }
          }
    );
}

function reset() {
    $('#primaryKeyTable tr').remove();
    document.getElementById('primaryKeySearch').style.display = 'none';
    $('#facetSearchTable tr').remove();
    $('#facetListSelect').find('option:gt(0)').remove();
    $('#query').val('');
    document.getElementById('facetSearchCombo').style.display = 'none';
    document.getElementById('facetSearchTableRow').style.display = 'none';
}
