/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

function populateAnalyticsTable(analyticsTable, columnInformation, type) {
    var tbody = analyticsTable.getElementsByTagName('tbody')[0];
    for (var i = 1; i < columnInformation.rows.length; i++) {
        var cellNo = 0;
        var meta = columnInformation.rows[i];
        var column0 = meta.cells[0].textContent.trim();
        var column1 = meta.cells[1].textContent.trim();
        var row = tbody.insertRow(analyticsTable.rows.length - 1);

        var persistCell = row.insertCell(cellNo++);
        var persistCheckElement = document.createElement('input');
        persistCheckElement.type = "checkbox";
        persistCheckElement.name = "persist";
        persistCell.appendChild(persistCheckElement);

        var columnCell = row.insertCell(cellNo++);
        var columnInputElement = document.createElement('label');
        columnInputElement.name = "column";
        columnInputElement.innerHTML = type + column0;
        columnCell.appendChild(columnInputElement);

        var typeCell = row.insertCell(cellNo++);
        var selectElement = document.createElement('select');

        if (column1 == 'string') {
            selectElement.options[0] = new Option('STRING', 'string');
            selectElement.options[1] = new Option('FACET', 'FACET');
        } else {
            selectElement.options[0] = new Option('STRING', 'string');
            selectElement.options[1] = new Option('INTEGER', 'int');
            selectElement.options[2] = new Option('LONG', 'long');
            selectElement.options[3] = new Option('BOOLEAN', 'bool');
            selectElement.options[4] = new Option('FLOAT', 'float');
            selectElement.options[5] = new Option('DOUBLE', 'double');
            selectElement.options[6] = new Option('FACET', 'FACET');
            selectElement.disabled = true;
        }
        selectElement.value = column1;
        typeCell.appendChild(selectElement);

        var primaryCell = row.insertCell(cellNo++);
        var primaryCheckElement = document.createElement('input');
        primaryCheckElement.type = "checkbox";
        primaryCell.appendChild(primaryCheckElement);

        var indexCell = row.insertCell(cellNo++);
        var indexCheckElement = document.createElement('input');
        indexCheckElement.type = "checkbox";
        indexCell.appendChild(indexCheckElement);

        var scoreParamCell = row.insertCell(cellNo++);
        var scoreParamCheckElement = document.createElement('input');
        scoreParamCheckElement.type = "checkbox";
        if (column1 == 'string' || column1 == 'bool') {
            scoreParamCheckElement.disabled = true;
        }
        scoreParamCell.appendChild(scoreParamCheckElement);
    }
}

function createAnalyticsIndexTable() {
    var table = document.getElementById('analyticsIndexTable');
    for (var i = table.rows.length; i > 1; i--) {
        table.deleteRow(i - 1);
    }
    var metaDataTable = document.getElementById("outputMetaDataTable");
    if (metaDataTable.rows.length > 1) {
        populateAnalyticsTable(table, metaDataTable, 'meta_');
    }
    var correlationDataTable = document.getElementById("outputCorrelationDataTable");
    if (correlationDataTable.rows.length > 1) {
        populateAnalyticsTable(table, correlationDataTable, 'correlation_');
    }
    var payloadDataTable = document.getElementById("outputPayloadDataTable");
    if (payloadDataTable.rows.length > 1) {
        populateAnalyticsTable(table, payloadDataTable, '');
    }
}

function populateAnalyticsIndexTable(eventStreamName) {
    createAnalyticsIndexTable();
    jQuery.ajax({
        type: "GET",
        url: "../eventstream/get_analytics_index_definitions_ajaxprocessor.jsp?eventStreamName=" + eventStreamName,
        data: {},
        dataType: "text",
        async: false,
        success: function (result) {
            var IS_JSON = true;
            try {
                var resultJson = JSON.parse(result.trim());
            } catch (err) {
                IS_JSON = false;
            }
            if (IS_JSON) {
                var table = document.getElementById('analyticsIndexTable');
                var currentRowCount = table.rows.length;
                //var nextRowPosition = currentRowCount - 1;
                var tbody = table.getElementsByTagName('tbody')[0];
                jQuery.each(resultJson, function (index, element) {
                    //var columnRecordNotContain = true;
                    for (var j = 1; j < currentRowCount; j++) {
                        var row = table.rows[j];
                        var columnName = row.cells[1].textContent;
                        if (columnName.trim() == element.columnName) {
                            //columnRecordNotContain = false;
                            row.cells[0].childNodes[0].checked = true;
                            var select = row.cells[2].childNodes[0];
                            if (select.value == 'string') {
                                if (element.columnType == 'STRING') {
                                    select.selectedIndex = 0;
                                } else {
                                    select.selectedIndex = 1;
                                }
                            }
                            row.cells[3].childNodes[0].checked = element.primaryKey;
                            row.cells[4].childNodes[0].checked = element.indexed;
                            row.cells[5].childNodes[0].checked = element.scoreParam;
                            break;
                        }
                    }
                    /*// This record not in this current stream, but available in analytics schema
                     if (columnRecordNotContain) {
                     var row = tbody.insertRow(nextRowPosition);
                     var cellNo = 0;
                     var persistCell = row.insertCell(cellNo++);
                     var persistCheckElement = document.createElement('input');
                     persistCheckElement.type = "checkbox";
                     persistCheckElement.name = "persist";
                     persistCheckElement.checked = true;
                     persistCheckElement.disabled = true;
                     persistCell.appendChild(persistCheckElement);

                     var columnCell = row.insertCell(cellNo++);
                     var columnInputElement = document.createElement('label');
                     columnInputElement.name = "column";
                     columnInputElement.innerHTML = element.columnName;
                     columnCell.appendChild(columnInputElement);

                     var typeCell = row.insertCell(cellNo++);
                     var selectElement = document.createElement('select');
                     selectElement.options[0] = new Option('STRING', 'string');
                     selectElement.options[1] = new Option('INTEGER', 'int');
                     selectElement.options[2] = new Option('LONG', 'long');
                     selectElement.options[3] = new Option('BOOLEAN', 'bool');
                     selectElement.options[4] = new Option('FLOAT', 'float');
                     selectElement.options[5] = new Option('DOUBLE', 'double');
                     selectElement.options[6] = new Option('FACET', 'FACET');
                     selectElement.value = element.columnType;
                     selectElement.disabled = true;
                     typeCell.appendChild(selectElement);

                     var primaryCell = row.insertCell(cellNo++);
                     var primaryCheckElement = document.createElement('input');
                     primaryCheckElement.type = "checkbox";
                     primaryCheckElement.checked = element.primaryKey;
                     primaryCheckElement.disabled = true;
                     primaryCell.appendChild(primaryCheckElement);

                     var indexCell = row.insertCell(cellNo++);
                     var indexCheckElement = document.createElement('input');
                     indexCheckElement.type = "checkbox";
                     indexCheckElement.checked = element.indexed;
                     indexCheckElement.disabled = true;
                     indexCell.appendChild(indexCheckElement);

                     var scoreParamCell = row.insertCell(cellNo++);
                     var scoreParamCheckElement = document.createElement('input');
                     scoreParamCheckElement.type = "checkbox";
                     scoreParamCheckElement.checked = element.scoreParam;
                     scoreParamCheckElement.disabled = true;
                     scoreParamCell.appendChild(scoreParamCheckElement);
                     nextRowPosition++;
                     }*/
                });
            } else {
                CARBON.showErrorDialog("Failed to get index information, Exception: " + result);
            }
        }
    });
}

function getAnalyticsIndexDataValues(dataTable) {
    var wso2EventData = "";
    if (document.getElementById("eventPersistCheckbox").checked) {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var persist = row.cells[0].childNodes[0].checked;
            var columnName = row.cells[1].textContent.trim();
            var type = row.cells[2].childNodes[0].options[row.cells[2].childNodes[0].selectedIndex].text;
            var primary = row.cells[3].childNodes[0].checked;
            var index = row.cells[4].childNodes[0].checked;
            var scoreParam = row.cells[5].childNodes[0].checked;
            wso2EventData = wso2EventData + persist + "^=" + columnName + "^=" + type + "^=" + primary + "^=" + index + "^=" + scoreParam + "^=" + "$=";
        }
    }
    return wso2EventData;
}