function createMainJTable(fields) {
    $('#AnalyticsTableContainer').jtable({
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
    $('#AnalyticsTableContainer').jtable('load');
}

function getArbitraryFields(rowData) {
    var $img =
            $('<img src="/carbon/messageconsole/themes/metro/list_metro.png" title="Show Arbitrary Fields" />');
    //Open child table when user clicks the image
    $img.click(function () {
        $('#AnalyticsTableContainer').jtable('openChildTable',
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
    return $img;
}

function createJTable(table) {
    if (table != '-1') {
        /*$.getJSON("/analytics/getSchema/" + table, function (data, status) {
            var fields = {
                ArbitraryFields: {
                    title: '',
                    width: '1%',
                    sorting: false,
                    edit: false,
                    create: false,
                    display: function (rowData) {
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
                $('#AnalyticsTableContainer').jtable('destroy');
                createMainJTable(fields);
            }
        });*/

        $.getJSON("/carbon/messageconsole/messageconsole_ajaxprocessor.jsp?tableName=" + table, function (data, status) {
            console.log(data);
        });

    }
}

function listActionMethod(postData) {
    return $.Deferred(function ($dfd) {
        $.ajax({
                   url: '/analytics/getppl',
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