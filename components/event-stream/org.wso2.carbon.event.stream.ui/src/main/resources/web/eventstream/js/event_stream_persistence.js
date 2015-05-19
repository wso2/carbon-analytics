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

function populateAnalyticsTable(analyticsTable, columnInformation) {
    var tbody = analyticsTable.getElementsByTagName('tbody')[0];
    for (var i = 1; i < columnInformation.rows.length; i++) {
        var cellNo = 0;
        var meta = columnInformation.rows[i];
        var column0 = meta.cells[0].textContent;
        var column1 = meta.cells[1].textContent;
        var row = tbody.insertRow(analyticsTable.rows.length - 1);

        var persistCell = row.insertCell(cellNo++);
        var persistCheckElement = document.createElement('input');
        persistCheckElement.type = "checkbox";
        persistCheckElement.checked = true;
        persistCell.appendChild(persistCheckElement);

        var columnCell = row.insertCell(cellNo++);
        var columnInputElement = document.createElement('label');
        columnInputElement.name = "column";
        columnInputElement.innerHTML = column0;
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
        if (column1 == 'string') {
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
        populateAnalyticsTable(table, metaDataTable);
    }
    var correlationDataTable = document.getElementById("outputCorrelationDataTable");
    if (correlationDataTable.rows.length > 1) {
        populateAnalyticsTable(table, correlationDataTable);
    }
    var payloadDataTable = document.getElementById("outputPayloadDataTable");
    if (payloadDataTable.rows.length > 1) {
        populateAnalyticsTable(table, payloadDataTable);
    }
}

function getAnalyticsIndexDataValues(dataTable) {
    var wso2EventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {
        var row = dataTable.rows[i];
        var persis = row.cells[0].childNodes[0].checked;
        var columnName = row.cells[1].textContent;
        var type = row.cells[2].childNodes[0].options[row.cells[2].childNodes[0].selectedIndex].text;
        var primary = row.cells[3].childNodes[0].checked;
        var index = row.cells[4].childNodes[0].checked;
        var scoreParam = row.cells[5].childNodes[0].checked;
        wso2EventData = wso2EventData + persis + "^=" + columnName + "^=" + type + "^=" + primary + "^=" + index + "^=" + scoreParam + "^=" + "$=";
    }

    return wso2EventData;
}