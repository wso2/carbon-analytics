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
        YAHOO.util.Dom.addClass(persistCell, "property-names");
        var persistCheckElement = document.createElement('input');
        persistCheckElement.type = "checkbox";
        persistCheckElement.className = type;
        persistCheckElement.addEventListener('change',
                                             function () {
                                                 uncheckedRoot(type);
                                             }, false);
        persistCell.appendChild(persistCheckElement);

        var columnCell = row.insertCell(cellNo++);
        YAHOO.util.Dom.addClass(columnCell, "property-names");
        var columnInputElement = document.createElement('label');
        columnInputElement.name = "column";
        columnInputElement.innerHTML = column0;
        columnCell.appendChild(columnInputElement);

        var typeCell = row.insertCell(cellNo++);
        YAHOO.util.Dom.addClass(typeCell, "property-names");
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
        YAHOO.util.Dom.addClass(primaryCell, "property-names");
        var primaryCheckElement = document.createElement('input');
        primaryCheckElement.type = "checkbox";
        primaryCell.appendChild(primaryCheckElement);

        var indexCell = row.insertCell(cellNo++);
        YAHOO.util.Dom.addClass(indexCell, "property-names");
        var indexCheckElement = document.createElement('input');
        indexCheckElement.type = "checkbox";
        indexCell.appendChild(indexCheckElement);

        var scoreParamCell = row.insertCell(cellNo++);
        YAHOO.util.Dom.addClass(scoreParamCell, "property-names");
        var scoreParamCheckElement = document.createElement('input');
        scoreParamCheckElement.type = "checkbox";
        if (column1 == 'string' || column1 == 'bool') {
            scoreParamCheckElement.disabled = true;
        }
        scoreParamCell.appendChild(scoreParamCheckElement);
    }
}

function createAnalyticsIndexTable() {
    var metaIndexTable = document.getElementById('metaIndexTable');
    var metaDataTable = document.getElementById("outputMetaDataTable");
    for (var i = metaIndexTable.rows.length; i > 1; i--) {
        metaIndexTable.deleteRow(i - 1);
    }
    if (metaDataTable.rows.length > 1) {
        populateAnalyticsTable(metaIndexTable, metaDataTable, 'meta');
        document.getElementById("noOutputMetaIndexData").style.display = 'none';
    } else {
        document.getElementById("metaData").style.display = 'none';
    }

    var correlationIndexTable = document.getElementById('correlationIndexTable');
    var correlationDataTable = document.getElementById("outputCorrelationDataTable");
    for (var i = correlationIndexTable.rows.length; i > 1; i--) {
        correlationIndexTable.deleteRow(i - 1);
    }
    if (correlationDataTable.rows.length > 1) {
        populateAnalyticsTable(correlationIndexTable, correlationDataTable, 'correlation');
        document.getElementById("noOutputCorrelationIndexData").style.display = 'none';
    } else {
        document.getElementById("correlationData").style.display = 'none';
    }

    var payloadIndexTable = document.getElementById('payloadIndexTable');
    var payloadDataTable = document.getElementById("outputPayloadDataTable");
    for (var i = payloadIndexTable.rows.length; i > 1; i--) {
        payloadIndexTable.deleteRow(i - 1);
    }
    if (payloadDataTable.rows.length > 1) {
        populateAnalyticsTable(payloadIndexTable, payloadDataTable, 'payload');
        document.getElementById("noOutputPayloadIndexData").style.display = 'none';
    } else {
        document.getElementById("payloadData").style.display = 'none';
    }
    var arbitraryIndexTable = document.getElementById('arbitraryIndexTable');
    for (var i = arbitraryIndexTable.rows.length; i > 1; i--) {
        arbitraryIndexTable.deleteRow(i - 1);
    }
}

function setRowValues(currentCount, indexTable, element, isPayload) {
    for (var j = 1; j < currentCount; j++) {
        var row = indexTable.rows[j];
        var columnName = row.cells[1].textContent;
        var jsonColumnName
        if (isPayload) {
            jsonColumnName = element.columnName;
        } else {
            jsonColumnName = element.columnName.slice(element.columnName.indexOf("_") + 1, element.columnName.length);
        }
        if (columnName.trim() == jsonColumnName) {
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
}

function setRowValuesForArbitrary(indexTable, element) {
    indexTable.style.display = "";
    var newTableRow = indexTable.insertRow(indexTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = element.columnName.slice(element.columnName.indexOf("_") + 1, element.columnName.length);
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var typeCell = newTableRow.insertCell(1);
    YAHOO.util.Dom.addClass(typeCell, "property-names");
    var selectElement = document.createElement('select');
    selectElement.options[0] = new Option('INTEGER', 'INTEGER');
    selectElement.options[1] = new Option('STRING', 'STRING');
    selectElement.options[2] = new Option('LONG', 'LONG');
    selectElement.options[3] = new Option('BOOLEAN', 'BOOLEAN');
    selectElement.options[4] = new Option('FLOAT', 'FLOAT');
    selectElement.options[5] = new Option('DOUBLE', 'DOUBLE');
    selectElement.options[6] = new Option('FACET', 'FACET');
    selectElement.disabled = true;
    selectElement.value = element.columnType;
    typeCell.appendChild(selectElement);

    var primaryCell = newTableRow.insertCell(2);
    YAHOO.util.Dom.addClass(primaryCell, "property-names");
    var primaryCheckElement = document.createElement('input');
    primaryCheckElement.type = "checkbox";
    primaryCheckElement.checked = element.primaryKey;
    primaryCheckElement.disabled = true;
    primaryCell.appendChild(primaryCheckElement);

    var indexCell = newTableRow.insertCell(3);
    YAHOO.util.Dom.addClass(indexCell, "property-names");
    var indexCheckElement = document.createElement('input');
    indexCheckElement.type = "checkbox";
    indexCheckElement.checked = element.indexed;
    indexCheckElement.disabled = true;
    indexCell.appendChild(indexCheckElement);

    var scoreParamCell = newTableRow.insertCell(4);
    YAHOO.util.Dom.addClass(scoreParamCell, "property-names");
    var scoreParamCheckElement = document.createElement('input');
    scoreParamCheckElement.type = "checkbox";
    scoreParamCheckElement.checked = element.scoreParam;
    scoreParamCheckElement.disabled = true;
    scoreParamCell.appendChild(scoreParamCheckElement);
    var newCel3 = newTableRow.insertCell(5);
    newCel3.innerHTML = '<a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeStreamAttribute(this)">Delete</a>';
    YAHOO.util.Dom.addClass(newCel3, "property-names");
    document.getElementById("noOutputArbitraryData").style.display = "none";

}
function populateAnalyticsIndexTable(eventStreamName, eventStreamVersion) {
    createAnalyticsIndexTable();
    document.getElementById('eventPersistCheckbox').checked = false;
    document.getElementById('metaPersistCheckbox').checked = false;
    document.getElementById('correlationPersistCheckbox').checked = false;
    document.getElementById('payloadPersistCheckbox').checked = false;
    jQuery.ajax({
        type: "GET",
        url: "../eventstream/get_analytics_index_definitions_ajaxprocessor.jsp?eventStreamName=" + eventStreamName + "&eventStreamVersion=" + eventStreamVersion,
        data: {},
        dataType: "text",
        async: false,
        success: function (result) {
            var IS_JSON = true;
            try {
                document.getElementById('attributeFieldSet').disabled = true;
                var resultJson = JSON.parse(result.trim());
            } catch (err) {
                IS_JSON = false;
            }
            if (IS_JSON) {
                var metaIndexTable = document.getElementById('metaIndexTable');
                var correlationIndexTable = document.getElementById('correlationIndexTable');
                var payloadIndexTable = document.getElementById('payloadIndexTable');
                var arbitraryIndexTable = document.getElementById('arbitraryIndexTable');

                var currentMetaCount = metaIndexTable.rows.length;
                var currentCorrelationCount = correlationIndexTable.rows.length;
                var currentPayloadCount = payloadIndexTable.rows.length;

                document.getElementById('eventPersistCheckbox').checked = resultJson.persist;
                document.getElementById('schemaReplaceCheckbox').checked = resultJson.mergeSchema;
                document.getElementById('recordStoreSelect').value = resultJson.recordStoreName;
                document.getElementById('attributeFieldSet').disabled = !resultJson.persist;
                document.getElementById('recordStoreSelect').disabled = true;
                if (resultJson.analyticsTableRecords != null) {
                    jQuery.each(resultJson.analyticsTableRecords, function (index, element) {
                        if (element.columnName.startsWith("meta_")) {
                            setRowValues(currentMetaCount, metaIndexTable, element, false);
                            uncheckedRoot('meta');
                        } else if (element.columnName.startsWith("correlation_")) {
                            setRowValues(currentCorrelationCount, correlationIndexTable, element, false);
                            uncheckedRoot('correlation');
                        } else if (element.columnName.startsWith("_")) {
                            setRowValuesForArbitrary(arbitraryIndexTable, element);
                        } else {
                            setRowValues(currentPayloadCount, payloadIndexTable, element, true);
                            uncheckedRoot('payload');
                        }
                    });
                }
            } else {
                CARBON.showErrorDialog("Failed to get index information, Exception: " + result);
            }
        }
    });
}

function getAnalyticsIndexDataValues(dataTable) {
    var wso2EventData = "";
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
    return wso2EventData;
}

function getArbitraryIndexDataValues(dataTable) {
    var wso2EventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {
        var row = dataTable.rows[i];
        var columnName = row.cells[0].textContent.trim();
        var type = row.cells[1].childNodes[0].options[row.cells[1].childNodes[0].selectedIndex].text;
        var primary = row.cells[2].childNodes[0].checked;
        var index = row.cells[3].childNodes[0].checked;
        var scoreParam = row.cells[4].childNodes[0].checked;
        wso2EventData = wso2EventData + columnName + "^=" + type + "^=" + primary + "^=" + index + "^=" + scoreParam + "^=" + "$=";
    }
    return wso2EventData;
}

function addArbitraryAttribute() {
    var attributeName = document.getElementById("outputArbitraryDataPropName");
    var attributeType = document.getElementById("outputArbitraryDataPropType");
    var error = "";

    if (attributeName.value == "") {
        error = "Attribute name field is empty.\n";
    }
    if (attributeType.value == "") {
        error = "Attribute type field is empty. \n";
    }
    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    var primary = document.getElementById("eventPersistPrimaryKeyCheckbox");
    var index = document.getElementById("eventPersistIndexCheckbox");
    var scoreParam = document.getElementById("eventPersistScoreParamCheckbox");
    var arbitraryTable = document.getElementById("arbitraryIndexTable");

    arbitraryTable.style.display = "";

    var newTableRow = arbitraryTable.insertRow(arbitraryTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = attributeName.value.trim();
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var typeCell = newTableRow.insertCell(1);
    YAHOO.util.Dom.addClass(typeCell, "property-names");
    var selectElement = document.createElement('select');
    selectElement.options[0] = new Option('INTEGER', 'INTEGER');
    selectElement.options[1] = new Option('STRING', 'STRING');
    selectElement.options[2] = new Option('LONG', 'LONG');
    selectElement.options[3] = new Option('BOOLEAN', 'BOOLEAN');
    selectElement.options[4] = new Option('FLOAT', 'FLOAT');
    selectElement.options[5] = new Option('DOUBLE', 'DOUBLE');
    selectElement.options[6] = new Option('FACET', 'FACET');
    selectElement.disabled = true;
    selectElement.value = attributeType.value;
    typeCell.appendChild(selectElement);

    var primaryCell = newTableRow.insertCell(2);
    YAHOO.util.Dom.addClass(primaryCell, "property-names");
    var primaryCheckElement = document.createElement('input');
    primaryCheckElement.type = "checkbox";
    primaryCheckElement.checked = primary.checked;
    primaryCheckElement.disabled = true;
    primaryCell.appendChild(primaryCheckElement);

    var indexCell = newTableRow.insertCell(3);
    YAHOO.util.Dom.addClass(indexCell, "property-names");
    var indexCheckElement = document.createElement('input');
    indexCheckElement.type = "checkbox";
    indexCheckElement.checked = index.checked;
    indexCheckElement.disabled = true;
    indexCell.appendChild(indexCheckElement);

    var scoreParamCell = newTableRow.insertCell(4);
    YAHOO.util.Dom.addClass(scoreParamCell, "property-names");
    var scoreParamCheckElement = document.createElement('input');
    scoreParamCheckElement.type = "checkbox";
    scoreParamCheckElement.checked = scoreParam.checked;
    scoreParamCheckElement.disabled = true;
    scoreParamCell.appendChild(scoreParamCheckElement);
    var newCel3 = newTableRow.insertCell(5);
    newCel3.innerHTML = '<a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeStreamAttribute(this)">Delete</a>';
    YAHOO.util.Dom.addClass(newCel3, "property-names");
    document.getElementById("noOutputArbitraryData").style.display = "none";
    attributeName.value = "";
    primary.checked = false;
    index.checked = false;
    scoreParam.checked = false;
    document.getElementById("eventPersistScoreParamCheckbox").disabled = false;
}

function changeScoreParam(ele) {
    var value = ele.value;
    if (value == 'STRING' || value == 'BOOLEAN' || value == 'FACET') {
        document.getElementById("eventPersistScoreParamCheckbox").disabled = true;
    } else {
        document.getElementById("eventPersistScoreParamCheckbox").disabled = false;
    }
}

function checkAllMeta(ele, type) {
    var checkboxes = document.getElementsByClassName(type);
    if (ele.checked) {
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].type == 'checkbox') {
                checkboxes[i].checked = true;
            }
        }
    } else {
        for (var i = 0; i < checkboxes.length; i++) {
            if (checkboxes[i].type == 'checkbox') {
                checkboxes[i].checked = false;
            }
        }
    }
}

function uncheckedRoot(type) {
    var checkboxes = document.getElementsByClassName(type);
    var checkedCount = 0;
    for (var i = 0; i < checkboxes.length; i++) {
        if (checkboxes[i].checked) {
            checkedCount++;
        }
    }
    var element = document.getElementById(type + 'PersistCheckbox');
    if (checkedCount == checkboxes.length) {
        element.checked = true;
    } else {
        element.checked = false;
    }
}

function enableAttribute(ele) {
    document.getElementById('attributeFieldSet').disabled = !ele.checked;
}

function showHideAttribute() {
    var advancedOptionsDiv = document.getElementById('advancedOptions');
    if (advancedOptionsDiv.style.display === 'block' || advancedOptionsDiv.style.display === '')
        advancedOptionsDiv.style.display = 'none';
    else
        advancedOptionsDiv.style.display = 'block'
}