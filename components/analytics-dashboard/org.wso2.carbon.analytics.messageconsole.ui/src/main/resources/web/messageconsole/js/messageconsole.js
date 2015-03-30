var tableLoaded = false;
var arbitraryColumnName;

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

function arbitraryFieldCreateActionMethod(postData) {
    var recordId;
    var $selectedRows = $('#AnalyticsTableContainer').jtable('selectedRows');
    if ($selectedRows.length > 0) {
        $selectedRows.each(function () {
            var record = $(this).data('record');
            recordId = record.bam_unique_rec_id;
        });
    }
    return $.Deferred(function ($dfd) {
        $.ajax({
                   url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeCreateArbitraryRecord + "&tableName=" + $("#tableSelect").val() + "&bam_unique_rec_id=" + recordId,
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

function arbitraryFieldUpdateActionMethod(postData) {
    var recordId;
    var $selectedRows = $('#AnalyticsTableContainer').jtable('selectedRows');
    if ($selectedRows.length > 0) {
        $selectedRows.each(function () {
            var record = $(this).data('record');
            recordId = record.bam_unique_rec_id;
        });
    }
    return $.Deferred(function ($dfd) {
        $.ajax({
                   url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeUpdateArbitraryRecord + "&tableName=" + $("#tableSelect").val() + "&bam_unique_rec_id=" + recordId,
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

function arbitraryFieldDeleteActionMethod(postData) {
    postData["Name"] = arbitraryColumnName;
    return $.Deferred(function ($dfd) {
        $.ajax({
                   url: '/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?type=' + typeDeleteArbitraryRecord,
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
                              } else if (val.type == 'INTEGER') {
                                  fields[val.name].inputClass = 'validate[custom[integer]]';
                              } else if (val.type == 'FLOAT') {
                                  fields[val.name].inputClass = 'validate[custom[number]]';
                              } else if (val.type == 'DOUBLE') {
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
                        'type=' + typeUpdateRecord + '&tableName=' + $("#tableSelect").val(),
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

function tableSelectChange() {
    var table = $("#tableSelect").val();
    if (table != '-1') {
        $("#deleteTableButton").show();
        $("#editTableButton").show();
    } else {
        $("#deleteTableButton").hide();
        $("#editTableButton").hide();
    }
}