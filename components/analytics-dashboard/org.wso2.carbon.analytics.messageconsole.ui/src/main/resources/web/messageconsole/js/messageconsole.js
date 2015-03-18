var tableLoaded = false;
var timestamp;
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
                                                 },
                                                 createAction: function (postData) {
                                                     return createActionMethod(postData);
                                                 },
                                                 updateAction: function (postData) {
                                                     return updateActionMethod(postData);
                                                 },
                                                 deleteAction: function (postData) {
                                                     return deleteActionMethod(postData);
                                                 }
                                             },
                                             formCreated: function (event, data) {
                                                 timestamp = data.record.bam_rec_timestamp;
                                             },
                                             fields: fields

                                         });
    $('#AnalyticsTableContainer').jtable('load');
    $("#DeleteAllButton").show();
    $('#DeleteAllButton').button().click(function () {
        var $selectedRows = $('#AnalyticsTableContainer').jtable('selectedRows');
        $('#AnalyticsTableContainer').jtable('deleteRows', $selectedRows);
    });
    tableLoaded = true;
}

function getArbitraryFields(rowData) {
    var $img =
            $('<img src="/carbon/messageconsole/themes/metro/list_metro.png" title="Show Arbitrary Fields"/>');
    $img.click(function () {
        $('#AnalyticsTableContainer').jtable('openChildTable',
                                             $img.closest('tr'), //Parent row
                                             {
                                                 title: 'Arbitrary Fields',
                                                 messages: {
                                                     addNewRecord: 'Add new arbitrary field'
                                                 },
                                                 actions: {
                                                     listAction: function (postData, jtParams) {
                                                         return listActionMethod(jtParams);
                                                     },
                                                     deleteAction: '/Demo/DeleteExam',
                                                     updateAction: '/Demo/UpdateExam',
                                                     createAction: '/Demo/CreateExam'
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
                          fields[val.name] = {
                              title: val.name,
                              list: val.display,
                              key: val.key
                          };
                          if (val.type == 'STRING') {
                              fields[val.name].type = 'textarea';
                          }
                          if (val.name == 'bam_unique_rec_id' || val.name == 'bam_rec_timestamp') {
                              fields[val.name].edit = false;
                              fields[val.name].create = false;
                          }
                      });

                      console.log(fields);

                      if (data) {
                          if (tableLoaded == true) {
                              $('#AnalyticsTableContainer').jtable('destroy');
                          }
                          createMainJTable(fields);
                      }
                  }
        );
    }
}

function listActionMethod(jtParams) {
    var postData = {};
    postData["jtStartIndex"] = jtParams.jtStartIndex;
    postData["jtPageSize"] = jtParams.jtPageSize;
    postData["tableName"] = $("#tableSelect").val();
    postData["timeFrom"] = $("#timeFrom").val();
    postData["timeTo"] = $("#timeTo").val();
    postData["query"] = $("#query").val();
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

function createActionMethod(postData) {
    return $.Deferred(function ($dfd) {
        $.ajax({
                   url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeCreateRecord + '&tableName=' + $("#tableSelect").val(),
                   type: 'POST',
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

function updateActionMethod(postData) {
    return $.Deferred(function ($dfd) {
        $.ajax({
                   url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?' +
                        'type=' + typeUpdateRecord + '&tableName=' + $("#tableSelect").val() + '&bam_rec_timestamp=' + timestamp,
                   type: 'POST',
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

function deleteRecords(postData) {
    postData["tableName"] = $("#tableSelect").val();
    return $.Deferred(function ($dfd) {
        $.ajax({
                   url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeDeleteRecord,
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

function deleteActionMethod(postData) {
    return deleteRecords(postData);
}