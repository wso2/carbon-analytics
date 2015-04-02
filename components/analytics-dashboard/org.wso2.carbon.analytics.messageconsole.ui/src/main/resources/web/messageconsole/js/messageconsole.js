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
        $("#purgeRecordButton").show();
    } else {
        $("#deleteTableButton").hide();
        $("#editTableButton").hide();
        $("#purgeRecordButton").hide();
    }
}

function scheduleDataPurge() {
    if ($('#dataPurgingCheckBox').is(":checked")) {
        alert('test');
    }
}