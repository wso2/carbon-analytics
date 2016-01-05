var tableLoaded = false;
var facetAvailable = false;
var timeFromObj;
var timeToObj;

function getArbitraryFields(rowData) {
    var $img =
            $('<img src="/carbon/dataexplorer/themes/metro/list_metro.png" title="Show Arbitrary Fields / Extended Fields"/>');
    $img.click(function () {
        $('#AnalyticsTableContainer').jtable('openChildTable', $img.closest('tr'), //Parent row
                {
                    title: 'Arbitrary Fields / Extended fields',
                    selecting: true,
                    actions: {
                        // For Details: http://jtable.org/Demo/FunctionsAsActions
                        listAction: function (postData, jtParams) {
                            var postData = {};
                            postData['tableName'] = $("#tableSelect").val();
                            postData['_unique_rec_id'] = rowData.record._unique_rec_id;
                            return arbitraryFieldListActionMethod(postData, jtParams);
                        }
                    },
                    fields: {
                        _unique_rec_id: {
                            type: 'hidden',
                            key: true,
                            list: false,
                            defaultValue: rowData.record._unique_rec_id
                        },
                        Name: {
                            title: 'Name'
                        },
                        Value: {
                            title: 'Value',
                            type: 'STRING'
                        }
                    }
                }, function (data) { //opened handler
                    data.childTable.jtable('load');
                }
        );
    });
    return $img;
}

var entityMap = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
    '/': '&#x2F;',
    '`': '&#x60;',
    '=': '&#x3D;'
};

function escapeHtml (string) {
    return String(string).replace(/[&<>"'`=\/]/g, function fromEntityMap (s) {
        return entityMap[s];
    });
}

function arbitraryFieldListActionMethod(postData, jtParams) {
    return $.Deferred(function ($dfd) {
        $.ajax({
                    url: '/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeListArbitraryRecord,
                    type: 'POST',
                    dataType: 'json',
                    data: postData,
                    success: function (data) {
                        console.log(escapeHtml(data));
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
    postData["resultCountLimit"] = $("#resultCountSelect").val();
    var searchOption = $('input[name=group1]:radio:checked').val();
    if ('time' == searchOption) {
        postData["timeFrom"] = fromTimeStamp;
        postData["timeTo"] = toTimeStamp;
    } else if ('query' == searchOption) {
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
    } else if ('primary' == searchOption) {
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
    }

    return $.Deferred(function ($dfd) {
        $.ajax({
                    url: '/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeListRecord,
                    type: 'POST',
                    dataType: 'json',
                    data: postData,
                    success: function (data) {
                        $dfd.resolve(data);
                        if (data.ActualRecordCount != -1 && data.SearchTime != -1) {
                            $('#searchStat').text('Total Records: ' + data.ActualRecordCount + ' (' + data.SearchTime +
                                                  ' ms)');
                        } else if (data.ActualRecordCount != -1) {
                            $('#searchStat').text('Total Records: ' + data.ActualRecordCount);
                        }
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
        document.getElementById('searchControl').style.display = '';
        $("#purgeRecordButton").show();
        loadPrimaryKeys();
        loadFacetNames();
        checkTotalCountSupport();
    } else {
        $("#purgeRecordButton").hide();
        $('#facetListSelect').find('option:gt(0)').remove();
        $('#query').val('');
        document.getElementById('searchControl').style.display = 'none';
        document.getElementById('resultCount').style.display = 'none';
        document.getElementById('countLabel').style.display = 'none';
    }
    $('#searchStat').text('');
    document.getElementById('countLabel').style.display = 'none';
    document.getElementById('dataRangeSearch').style.display = 'none';
    document.getElementById('primaryKeySearch').style.display = 'none';
    document.getElementById('querySearch').style.display = 'none';
    document.getElementById('facetSearchCombo').style.display = 'none';
    document.getElementById('facetSearchTableRow').style.display = 'none';

    try {
        $('input[name=group1]').attr('checked',false);
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
    $.post('/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeSavePurgingTask,
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
}

function updateSearchOption(ele) {
    switch (ele.value) {
        case 'time':
            document.getElementById('dataRangeSearch').style.display = '';
            document.getElementById('primaryKeySearch').style.display = 'none';
            document.getElementById('querySearch').style.display = 'none';
            document.getElementById('facetSearchCombo').style.display = 'none';
            document.getElementById('facetSearchTableRow').style.display = 'none';
            $('#searchStat').text('');
            break;
        case 'primary':
            document.getElementById('primaryKeySearch').style.display = '';
            document.getElementById('dataRangeSearch').style.display = 'none';
            document.getElementById('querySearch').style.display = 'none';
            document.getElementById('facetSearchTableRow').style.display = 'none';
            document.getElementById('facetSearchCombo').style.display = 'none';
            document.getElementById('countLabel').style.display = 'none';
            break;
        case 'query':
            document.getElementById('querySearch').style.display = '';
            if (facetAvailable) {
                document.getElementById('facetSearchCombo').style.display = '';
                document.getElementById('facetSearchTableRow').style.display = '';
            }
            document.getElementById('primaryKeySearch').style.display = 'none';
            document.getElementById('dataRangeSearch').style.display = 'none';
            document.getElementById('countLabel').style.display = 'none';
            break;
    }
}

function loadFacetNames() {
    $('#facetSearchTable tr').remove();
    $('#facetListSelect').find('option:gt(0)').remove();
    document.getElementById('radioQuery').style.display = '';
    $.get('/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeGetFacetNameList + '&tableName=' + $("#tableSelect").val(),
          function (result) {
              var resultObj = jQuery.parseJSON(result);
              var facetNames = "";
              $(resultObj).each(function (key, tableName) {
                  facetNames += "<option value=" + tableName + ">" + tableName + "</option>";
              });
              facetAvailable = resultObj.length > 0;
              $("#facetListSelect").append(facetNames);
          }
    );
}

function checkTotalCountSupport() {
    $('#facetSearchTable tr').remove();
    $('#facetListSelect').find('option:gt(0)').remove();
    document.getElementById('radioQuery').style.display = '';
    $.get('/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeCheckTotalCount + '&tableName=' + $("#tableSelect").val(),
          function (result) {
              if (result.trim() != 'true') {
                  document.getElementById('resultCount').style.display = '';
                  document.getElementById('countLabel').style.display = '';
              }
          }
    );
}

function loadPrimaryKeys() {
    $('#primaryKeyTable tr').remove();
    $.get('/carbon/dataexplorer/dataexplorer_ajaxprocessor.jsp?type=' + typeGetPrymaryKeyList + '&tableName=' + $("#tableSelect").val(),
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
                  document.getElementById('radioPrimary').style.display = '';
                  document.getElementById('radioLabelPrimary').style.display = '';
              } else {
                  document.getElementById('radioPrimary').style.display = 'none';
                  document.getElementById('radioLabelPrimary').style.display = 'none';
              }
          }
    );
}

function reset() {
    $('input[name=group1]').attr('checked',false);
    tableLoaded = false;
    facetAvailable = false;
    $('#primaryKeyTable tr').remove();
    loadTableSelect();
    document.getElementById('searchControl').style.display = 'none';
    document.getElementById('resultCount').style.display = 'none';
    document.getElementById('countLabel').style.display = 'none';
    document.getElementById('dataRangeSearch').style.display = 'none';
    document.getElementById('querySearch').style.display = 'none';
    document.getElementById('primaryKeySearch').style.display = 'none';
    document.getElementById('facetSearchCombo').style.display = 'none';
    document.getElementById('facetSearchTableRow').style.display = 'none';
    document.getElementById('resultsTable').style.display = 'none';
    timeFromObj.datetimepicker('setDate', '');
    timeToObj.datetimepicker('setDate', '');
    $('#searchStat').text('');
}
