/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

//Method that used in jsp files

function getWso2EventDataValues(dataTable, inputDataType) {

    var wso2EventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {
        var row = dataTable.rows[i];
        var column1 = row.cells[1].innerHTML;
        if (column1 == inputDataType) {
            var column0 = row.cells[0].innerHTML;
            var column2 = row.cells[2].innerHTML;
            var column3 = row.cells[3].innerHTML;

            wso2EventData = wso2EventData + column0 + "^=" + column2 + "^=" + column3 + "$=";
        }
    }
    return wso2EventData;
}

function getJsonDataValues(dataTable) {

    var jsonData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;
        var column3 = row.cells[3].innerHTML;

        // For JSON we use a different terminator (*) since $ is already used in JSONPath
        jsonData = jsonData + column0 + "^=" + column1 + "^=" + column2 + "^=" + column3 + "*=";
    }
    return jsonData;
}

function getXpathDataValues(dataTable) {

    var xpathData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;
        var column3 = row.cells[3].innerHTML;

        xpathData = xpathData + column0 + "^=" + column1 + "^=" + column2 + "^=" + column3 + "$=";
    }
    return xpathData;
}

function getXpathPrefixValues(dataTable) {
    var xpathPrefixes = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;

        xpathPrefixes = xpathPrefixes + column0 + "^=" + column1 + "$=";
    }

    return xpathPrefixes;
}

function getTextDataValues(dataTable) {

    var textData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;
        var column3 = row.cells[3].innerHTML;

        textData = textData + column1 + "^=" + column0 + "^=" + column2 + "^=" + column3 + "$=";
    }
    return textData;
}

function getMapDataValues(dataTable) {

    var mapEventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;

        mapEventData = mapEventData + column0 + "^=" + column1 + "^=" + column2 + "^=" + "$=";
    }
    return mapEventData;
}

function addEventBuilder(form) {

    var isFieldEmpty = false;

    var eventBuilderName = document.getElementById("eventBuilderNameId").value.trim();
    var toStreamId = document.getElementById("streamNameFilter").value.trim();
    var toStreamNameAndVersion = toStreamId.split(":");
    if(toStreamNameAndVersion.length != 2) {
        CARBON.showErrorDialog("Could not find a valid To Stream Id, Please check.");
        return;
    }

    var toStreamName = toStreamNameAndVersion[0];
    var toStreamVersion = toStreamNameAndVersion[1];
    var eventAdaptorInfo = document.getElementById("eventAdaptorNameSelect")[document.getElementById("eventAdaptorNameSelect").selectedIndex].value.trim();


    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventBuilderName)) {
        CARBON.showErrorDialog("Invalid character found in event builder name.");
        return;
    }
    if (isFieldEmpty || (eventBuilderName == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }
    if(toStreamName != "" && toStreamVersion == "") {
        toStreamVersion = "1.0.0";
    }

    var propertyCount = 0;
    var msgConfigPropertyString = "";

    // all properties, not required and required are checked
    while (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null ||
        document.getElementById("msgConfigProperty_" + propertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null) {
            if (document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                msgConfigPropertyString = "";
                break;
            }
            else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim();
                var propertyName = document.getElementById("msgConfigProperty_Required_" + propertyCount).name;
                msgConfigPropertyString = msgConfigPropertyString + propertyName + "$=" + propertyValue + "|=";

            }
        } else if (document.getElementById("msgConfigProperty_" + propertyCount) != null) {
            var notRequiredPropertyName = document.getElementById("msgConfigProperty_" + propertyCount).name;
            var notRequiredPropertyValue = document.getElementById("msgConfigProperty_" + propertyCount).value.trim();
            if (notRequiredPropertyValue == "") {
                notRequiredPropertyValue = "  ";
            }
            msgConfigPropertyString = msgConfigPropertyString + notRequiredPropertyName + "$=" + notRequiredPropertyValue + "|=";
        }
        propertyCount++;
    }

    if (isFieldEmpty) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    } else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'wso2event') {

        var mappingType = "wso2event";
        var metaData = "";
        var correlationData = "";
        var payloadData = "";
        var customMappingValue = "disable";
        var checkedRB = document.querySelectorAll('input[type="radio"][name="customMapping"]:checked');
        if(checkedRB.length != 0) {
            customMappingValue = checkedRB[0].value;
        }

        if(customMappingValue == "enable") {
        var wso2EventDataTable = document.getElementById("inputWso2EventDataTable");
        if (wso2EventDataTable.rows.length > 1) {
            metaData = getWso2EventDataValues(wso2EventDataTable, 'meta');
            correlationData = getWso2EventDataValues(wso2EventDataTable, 'correlation');
            payloadData = getWso2EventDataValues(wso2EventDataTable, 'payload');
        }
        }
        if (metaData == "" && correlationData == "" && payloadData == "" && customMappingValue == "enable") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType,
                    msgConfigPropertySet: msgConfigPropertyString, customMappingValue: customMappingValue,
                    metaData: metaData, correlationData: correlationData, payloadData: payloadData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'text') {

        var mappingType = "text";
        var textData = "";

        var textDataTable = document.getElementById("inputTextMappingTable");
        if (textDataTable.rows.length > 1) {
            textData = getTextDataValues(textDataTable);
        }

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    textData: textData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'xml') {
        var parentSelectorXpath = document.getElementById("parentSelectorXpath").value;

        var mappingType = "xml";
        var prefixData = "";
        var xpathData = "";

        var xpathPrefixTable = document.getElementById("inputXpathPrefixTable");
        if (xpathPrefixTable.rows.length > 1) {
            prefixData = getXpathPrefixValues(xpathPrefixTable);
        }

        var xpathExprTable = document.getElementById("inputXpathExprTable");
        if (xpathExprTable.rows.length > 1) {
            xpathData = getXpathDataValues(xpathExprTable);
        }

        if (prefixData == "" && xpathData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    parentSelectorXpath: parentSelectorXpath, prefixData: prefixData, xpathData: xpathData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'map') {

        var mappingType = "map";
        var mapData = "";

        var mapDataTable = document.getElementById("inputMapPropertiesTable");
        if (mapDataTable.rows.length > 1) {
            mapData = getMapDataValues(mapDataTable);
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    mapData: mapData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        } else {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
    }
    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'json') {

        var mappingType = "json";
        var jsonData = "";

        var jsonDataTable = document.getElementById("inputJsonpathExprTable");
        if (jsonDataTable.rows.length > 1) {
            jsonData = getJsonDataValues(jsonDataTable);
        }

        if (jsonData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    jsonData: jsonData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }


}

function addEventBuilderViaPopup(form) {

    var isFieldEmpty = false;

    var eventBuilderName = document.getElementById("eventBuilderNameId").value.trim();
    var toStreamId = document.getElementById("streamNameFilter").value.trim();
    var toStreamNameAndVersion = toStreamId.split(":");
    if(toStreamNameAndVersion.length != 2) {
        CARBON.showErrorDialog("Could not find a valid To Stream Id, Please check.");
        return;
    }

    var toStreamName = toStreamNameAndVersion[0];
    var toStreamVersion = toStreamNameAndVersion[1];
    var eventAdaptorInfo = document.getElementById("eventAdaptorNameSelect")[document.getElementById("eventAdaptorNameSelect").selectedIndex].value.trim();


    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventBuilderName)) {
        CARBON.showErrorDialog("Invalid character found in event builder name.");
        return;
    }
    if (isFieldEmpty || (eventBuilderName == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }
    if(toStreamName != "" && toStreamVersion == "") {
        toStreamVersion = "1.0.0";
    }

    var propertyCount = 0;
    var msgConfigPropertyString = "";

    // all properties, not required and required are checked
    while (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null ||
        document.getElementById("msgConfigProperty_" + propertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("msgConfigProperty_Required_" + propertyCount) != null) {
            if (document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                msgConfigPropertyString = "";
                break;
            }
            else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("msgConfigProperty_Required_" + propertyCount).value.trim();
                var propertyName = document.getElementById("msgConfigProperty_Required_" + propertyCount).name;
                msgConfigPropertyString = msgConfigPropertyString + propertyName + "$=" + propertyValue + "|=";

            }
        } else if (document.getElementById("msgConfigProperty_" + propertyCount) != null) {
            var notRequiredPropertyName = document.getElementById("msgConfigProperty_" + propertyCount).name;
            var notRequiredPropertyValue = document.getElementById("msgConfigProperty_" + propertyCount).value.trim();
            if (notRequiredPropertyValue == "") {
                notRequiredPropertyValue = "  ";
            }
            msgConfigPropertyString = msgConfigPropertyString + notRequiredPropertyName + "$=" + notRequiredPropertyValue + "|=";
        }
        propertyCount++;
    }

    var mappingType = "";
    if (isFieldEmpty) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    } else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'wso2event') {

        mappingType = "wso2event";
        var metaData = "";
        var correlationData = "";
        var payloadData = "";
        var customMappingValue = "disable";
        var checkedRB = document.querySelectorAll('input[type="radio"][name="customMapping"]:checked');
        if(checkedRB.length != 0) {
            customMappingValue = checkedRB[0].value;
        }

        if(customMappingValue == "enable") {
        var wso2EventDataTable = document.getElementById("inputWso2EventDataTable");
        if (wso2EventDataTable.rows.length > 1) {
            metaData = getWso2EventDataValues(wso2EventDataTable, 'meta');
            correlationData = getWso2EventDataValues(wso2EventDataTable, 'correlation');
            payloadData = getWso2EventDataValues(wso2EventDataTable, 'payload');
        }
        }
        if (metaData == "" && correlationData == "" && payloadData == "" && customMappingValue == "enable") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType,
                    msgConfigPropertySet: msgConfigPropertyString, customMappingValue: customMappingValue,
                    metaData: metaData, correlationData: correlationData, payloadData: payloadData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'text') {

        mappingType = "text";
        var textData = "";

        var textDataTable = document.getElementById("inputTextMappingTable");
        if (textDataTable.rows.length > 1) {
            textData = getTextDataValues(textDataTable);
        }

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    textData: textData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'xml') {
        var parentSelectorXpath = document.getElementById("parentSelectorXpath").value;

        mappingType = "xml";
        var prefixData = "";
        var xpathData = "";

        var xpathPrefixTable = document.getElementById("inputXpathPrefixTable");
        if (xpathPrefixTable.rows.length > 1) {
            prefixData = getXpathPrefixValues(xpathPrefixTable);
        }

        var xpathExprTable = document.getElementById("inputXpathExprTable");
        if (xpathExprTable.rows.length > 1) {
            xpathData = getXpathDataValues(xpathExprTable);
        }

        if (prefixData == "" && xpathData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    parentSelectorXpath: parentSelectorXpath, prefixData: prefixData, xpathData: xpathData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'map') {

        mappingType = "map";
        var mapData = "";

        var mapDataTable = document.getElementById("inputMapPropertiesTable");
        if (mapDataTable.rows.length > 1) {
            mapData = getMapDataValues(mapDataTable);
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    mapData: mapData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        } else {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
    }
    else if (document.getElementById("inputMappingTypeSelect")[document.getElementById("inputMappingTypeSelect").selectedIndex].text == 'json') {

        mappingType = "json";
        var jsonData = "";

        var jsonDataTable = document.getElementById("inputJsonpathExprTable");
        if (jsonDataTable.rows.length > 1) {
            jsonData = getJsonDataValues(jsonDataTable);
        }

        if (jsonData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventbuilder/add_eventbuilder_ajaxprocessor.jsp', {
                method: 'post',
                asynchronous: false,
                parameters: {eventBuilderName: eventBuilderName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdaptorInfo: eventAdaptorInfo, mappingType: mappingType, msgConfigPropertySet: msgConfigPropertyString,
                    jsonData: jsonData},
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event builder added successfully!!");
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event builder, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
}

function addInputMapProperty() {
    var propName = document.getElementById("inputMapPropName");
    var propValueOf = document.getElementById("inputMapPropValueOf");
    var propertyTable = document.getElementById("inputMapPropertiesTable");
    var propertyTableTBody = document.getElementById("inputMapPropertiesTBody");
    var propType = document.getElementById("inputMapPropType");
    var noPropertyDiv = document.getElementById("noInputMapProperties");

    var error = "";
    if (propName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propName.value)) {
        error = "Invalid character found in Input Attribute name field.";
    }

    if (propValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = propertyTableTBody.insertRow(propertyTableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propValueOf.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    propValueOf.value = "";
    noPropertyDiv.style.display = "none";
    // propType.value = "";
    // showAddProperty();
}

function addInputXpathDef() {
    var prefixName = document.getElementById("inputPrefixName");
    var xpathNs = document.getElementById("inputXpathNs");
    var propertyTable = document.getElementById("inputXpathPrefixTable");
    var tableTBody = document.getElementById("inputXpathPrefixTBody");
    var noPropertyDiv = document.getElementById("noInputPrefixes");

    var error = "";

    if (prefixName.value == "") {
        error = "Prefix field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(prefixName.value)) {
        error = "Invalid character found in prefix field.";
    }

    if (xpathNs.value == "") {
        error = "Namespace field is empty.\n";
    }
//    // Check for white space
//    if (!reWhiteSpace.test(xpathNs.value)) {
//        error = "Invalid character found in XPath namespace.";
//    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = prefixName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = xpathNs.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'xml' + '\')">Delete</a>';

    prefixName.value = "";
    xpathNs.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputXmlProperty() {
    var propName = document.getElementById("inputPropertyName");
    var xpathExpr = document.getElementById("inputPropertyValue");
    var propDefault = document.getElementById("inputPropertyDefault");
    var propertyTable = document.getElementById("inputXpathExprTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputXpathExprTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propName.value)) {
        error = "Invalid character found in Mapped To field.";
    }

    if (xpathExpr.value == "") {
        error = "XPath field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = xpathExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    xpathExpr.value = "";
    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputRegexDef() {
    var regex = document.getElementById("inputRegexDef");
    var propertyTable = document.getElementById("inputRegexDefTable");
    var regexSelect = document.getElementById("inputPropertyValue");
    var tableTBody = document.getElementById("inputRegexDefTBody");
    var noPropertyDiv = document.getElementById("noInputRegex");

    var error = "";


    if (regex.value == "") {
        error = "Regular expression field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = regex.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeRegexInputProperty(this,\'' + 'xml' + '\')">Delete</a>';


    if (regexSelect.value == "") {
        regexSelect.remove(regexSelect.selectedIndex);
    }
    var newRegexOption = document.createElement("option");
    newRegexOption.value = regex.value;
    newRegexOption.text = regex.value;
    regexSelect.add(newRegexOption, null);

    regex.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputTextProperty() {
    var propName = document.getElementById("inputPropertyName");
    var regexExpr = document.getElementById("inputPropertyValue");
    var propDefault = document.getElementById("inputPropertyDefault");
    var propertyTable = document.getElementById("inputTextMappingTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputTextMappingTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propName.value)) {
        error = "Invalid character found in Mapped To field.";
    }


    if (regexExpr.value == "") {
        error = "Regular expression field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = regexExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    regexExpr.value = "";
    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputJsonProperty() {
    var propName = document.getElementById("inputPropertyName");
    var propDefault = document.getElementById("inputPropertyDefault");
    var jsonpathExpr = document.getElementById("inputPropertyValue");
    var propertyTable = document.getElementById("inputJsonpathExprTable");
    var propertyType = document.getElementById("inputPropertyType");
    var tableTBody = document.getElementById("inputJsonpathExprTBody");
    var noPropertyDiv = document.getElementById("noInputProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propName.value)) {
        error = "Invalid character found in Mapped To field.";
    }

    if (jsonpathExpr.value == "") {
        error = "JSONPath field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //Check for duplications
//    var topicNamesArr = YAHOO.util.Dom.getElementsByClassName("property-names");
//    var foundDuplication = false;
//    for (var i = 0; i < topicNamesArr.length; i++) {
//        if (topicNamesArr[i].innerHTML == propName.value) {
//            foundDuplication = true;
//            CARBON.showErrorDialog("Duplicated Entry");
//            return;
//        }
//    }

    //add new row
    var newTableRow = tableTBody.insertRow(tableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = jsonpathExpr.value;

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyType.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propDefault.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propName.value = "";
    jsonpathExpr.value = "";
    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}

function addInputWso2EventProperty() {
    var propertyName = document.getElementById("inputWso2EventPropertyName");
    var inputPropertyType = document.getElementById("inputWso2EventPropertyInputType");
    var propertyValueOf = document.getElementById("inputWso2EventPropertyValue");
    var propertyType = document.getElementById("inputWso2EventPropertyType");
    var propertyTable = document.getElementById("inputWso2EventDataTable");
    var propertyTableTBody = document.getElementById("inputWso2EventTBody");
    var noPropertyDiv = document.getElementById("noInputWso2EventData");

    var error = "";

    if (propertyName.value == "") {
        error = "Name field is empty.\n";
    }
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(propertyName.value)) {
        error = "Invalid character found in Input Attribute Name field.";
    }

    if (propertyValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";

    //add new row
    var newTableRow = propertyTableTBody.insertRow(propertyTableTBody.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propertyName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = inputPropertyType.value;

    var newCell2 = newTableRow.insertCell(2);
    newCell2.innerHTML = propertyValueOf.value;

    var newCell3 = newTableRow.insertCell(3);
    newCell3.innerHTML = propertyType.value;

    var newCell4 = newTableRow.insertCell(4);
    newCell4.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'map' + '\')">Delete</a>';

    propertyName.value = "";
    propertyValueOf.value = "";
    noPropertyDiv.style.display = "none";

}

function clearInputFields() {
    document.getElementById("queryName").value = "";
    document.getElementById("newTopic").value = "";
    document.getElementById("xmlSourceText").value = "";
    document.getElementById("textSourceText").value = "";
    document.getElementById("querySource").value = "";

    clearDataInTable("inputMetaDataTable");
    clearDataInTable("inputCorrelationDataTable");
    clearDataInTable("inputPayloadDataTable");

    document.getElementById("noInputMetaData").style.display = "";
    document.getElementById("noInputCorrelationData").style.display = "";
    document.getElementById("noInputPayloadData").style.display = "";
}

function removeInputProperty(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToRemove = rowToRemove.cells[0].innerHTML.trim();
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Input Property removed successfully!!");
}

function removeRegexInputProperty(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToRemove = rowToRemove.cells[0].innerHTML.trim();
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Input Property removed successfully!!");

    var regexSelect = document.getElementById("inputPropertyValue");
    jQuery("#inputPropertyValue option[value='"+propertyToRemove+"']").remove();
    if(regexSelect.length == 0){
        var newRegexOption = document.createElement("option");
        newRegexOption.value = 'No regular expression defined';
        newRegexOption.text = 'No regular expression defined';
        regexSelect.add(newRegexOption, null);
    }

}

/**
 * Utils
 */
function clearDataInTable(tableName) {
    deleteTableRows(tableName, true);
}

function deleteTableRows(tl, keepHeader) {
    if (typeof(tl) != "object") {
        tl = document.getElementById(tl);

    }
    //debugger;
    for (var i = tl.rows.length; tl.rows.length > 0; i--) {
        if (tl.rows.length > 1) {
            tl.deleteRow(tl.rows.length - 1);
        }
        if (tl.rows.length == 1) {
            if (!keepHeader) {
                tl.deleteRow(0);
            }
            return;
        }
    }

}

function enableMapping(isEnabled) {
    var mappingRow = document.getElementById("mappingRow");
    if(isEnabled) {
        mappingRow.style.display = "";
    } else {
        mappingRow.style.display = "none";
    }
}

function customCarbonWindowClose(){
    jQuery("#custom_dialog").dialog("destroy").remove();
}


var ENABLE = "enable";
var DISABLE = "disable";
var STAT = "statistics";
var TRACE = "Tracing";

function deleteEventBuilder(eventBuilderName) {
    var theform = document.getElementById('deleteForm');
    theform.eventBuilder.value = eventBuilderName;
    theform.submit();
}

function disableStat(eventBuilderName) {
    jQuery.ajax({
                    type: 'POST',
                    url: 'update_property_ajaxprocessor.jsp',
                    data: 'eventBuilderName=' + eventBuilderName + '&attribute=stat' + '&value=false',
                    async:false,
                    success: function (msg) {
                        handleCallback(eventBuilderName, DISABLE, STAT);
                    },
                    error: function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                               ' ' + eventBuilderName);
                    }
                });
}

function enableStat(eventBuilderName) {
    jQuery.ajax({
                    type: 'POST',
                    url: 'update_property_ajaxprocessor.jsp',
                    data: 'eventBuilderName=' + eventBuilderName + '&attribute=stat' + '&value=true',
                    async:false,
                    success: function (msg) {
                        handleCallback(eventBuilderName, ENABLE, STAT);
                    },
                    error: function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                               ' ' + eventBuilderName);
                    }
                });
}

function handleCallback(eventBuilderName, action, type) {
    var element;
    if (action == "enable") {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventBuilderName);
            element.style.display = "";
            element = document.getElementById("enableStat" + eventBuilderName);
            element.style.display = "none";
        } else {
            element = document.getElementById("disableTracing" + eventBuilderName);
            element.style.display = "";
            element = document.getElementById("enableTracing" + eventBuilderName);
            element.style.display = "none";
        }
    } else {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventBuilderName);
            element.style.display = "none";
            element = document.getElementById("enableStat" + eventBuilderName);
            element.style.display = "";
        } else {
            element = document.getElementById("disableTracing" + eventBuilderName);
            element.style.display = "none";
            element = document.getElementById("enableTracing" + eventBuilderName);
            element.style.display = "";
        }
    }
}

function enableTracing(eventBuilderName) {
    jQuery.ajax({
                    type: 'POST',
                    url: 'update_property_ajaxprocessor.jsp',
                    data: 'eventBuilderName=' + eventBuilderName + '&attribute=trace' + '&value=true',
                    async:false,
                    success: function (msg) {
                        handleCallback(eventBuilderName, ENABLE, TRACE);
                    },
                    error: function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                               ' ' + eventBuilderName);
                    }
                });
}

function disableTracing(eventBuilderName) {
    jQuery.ajax({
                    type: 'POST',
                    url: 'update_property_ajaxprocessor.jsp',
                    data: 'eventBuilderName=' + eventBuilderName + '&attribute=trace' + '&value=false',
                    async:false,
                    success: function (msg) {
                        handleCallback(eventBuilderName, DISABLE, TRACE);
                    },
                    error: function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                               ' ' + eventBuilderName);
                    }
                });
}