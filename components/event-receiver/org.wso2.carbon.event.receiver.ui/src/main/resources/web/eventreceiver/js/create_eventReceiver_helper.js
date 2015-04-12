/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var advancedMappingCounter = 0;

function clearTextIn(obj) {
    if (YAHOO.util.Dom.hasClass(obj, 'initE')) {
        YAHOO.util.Dom.removeClass(obj, 'initE');
        YAHOO.util.Dom.addClass(obj, 'normalE');
        textValue = obj.value;
        obj.value = "";
    }
}
function fillTextIn(obj) {
    if (obj.value == "") {
        obj.value = textValue;
        if (YAHOO.util.Dom.hasClass(obj, 'normalE')) {
            YAHOO.util.Dom.removeClass(obj, 'normalE');
            YAHOO.util.Dom.addClass(obj, 'initE');
        }
    }
}

function loadEventAdapterProperty(inputAdapterProperty, eventReceiverInputTable, propertyLoop) {

    var property = inputAdapterProperty.localDisplayName.trim();
    var tableRow = eventReceiverInputTable.insertRow(4);
    var textLabel = tableRow.insertCell(0);
    var displayName = inputAdapterProperty.localDisplayName.trim();
    textLabel.innerHTML = displayName;
    var requiredElementId = "property_";
    var textPasswordType = "text";
    var hint = ""
    var defaultValue = "";

    if (inputAdapterProperty.localRequired) {
        textLabel.innerHTML = displayName + '<span class="required">*</span>';
        requiredElementId = "property_Required_";
    }

    if (inputAdapterProperty.localSecured) {
        textPasswordType = "password";
    }

    if (inputAdapterProperty.localHint != undefined && inputAdapterProperty.localHint != "") {
        hint = inputAdapterProperty.localHint;
    }

    if (inputAdapterProperty.localDefaultValue != undefined && inputAdapterProperty.localDefaultValue != "") {
        defaultValue = inputAdapterProperty.localDefaultValue;
    }

    requiredElementId

    var inputField = tableRow.insertCell(1);
    var classType = 'outputFields';

    if (inputAdapterProperty.localOptions == '') {

        if (hint != undefined) {
            inputField.innerHTML = '<div class="' + classType + '"> <input style="width:75%" type="' + textPasswordType + '" id="' + requiredElementId + propertyLoop + '" name="' + inputAdapterProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> <br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
        else {
            inputField.innerHTML = '<div class="' + classType + '"> <input style="width:75%" type="' + textPasswordType + '" id="' + requiredElementId + propertyLoop + '" name="' + inputAdapterProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> </div>';
        }
    }

    else {

        var option = '';
        jQuery.each(inputAdapterProperty.localOptions, function (index, localOption) {
            if (localOption == inputAdapterProperty.localDefaultValue) {
                option = option + '<option selected=selected>' + localOption + '</option>';
            }
            else {
                option = option + '<option>' + localOption + '</option>';
            }

        });


        if (hint != undefined) {
            inputField.innerHTML = '<div class="' + classType + '"> <select   id="' + requiredElementId + propertyLoop + '" name="' + inputAdapterProperty.localKey + '">' + option + '</select><br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
        else {
            inputField.innerHTML = '<div class="' + classType + '"> <select  id="' + requiredElementId + propertyLoop + '" name="' + inputAdapterProperty.localKey + '"  />' + option + ' </div>';
        }


    }


}

function loadEventAdapterProperties(adapterSchema, propertiesHeading) {

    jQuery('#mappingTypeFilter').empty();
    for (var i = 0; i < adapterSchema.localSupportedMessageFormats.length; i++) {
        // for each property, add a text and input field in a row
        jQuery('#mappingTypeFilter').append('<option>' + adapterSchema.localSupportedMessageFormats[i].trim() + '</option>');
    }

    var eventReceiverInputTable = document.getElementById("eventReceiverInputTable");
// delete message properties related fields
    for (i = eventReceiverInputTable.rows.length - 7; i > 2; i--) {
        eventReceiverInputTable.deleteRow(i);
    }
    if(adapterSchema.localInputEventAdapterProperties.length > 0){
        var tableRow = eventReceiverInputTable.insertRow(3);
        var inputField = tableRow.insertCell(0);
        inputField.innerHTML = '<b><i><span style="color: #666666; ">'+propertiesHeading+'</span></i></b>';
    }
    for (var i = 0; i < adapterSchema.localInputEventAdapterProperties.length; i++) {
        // for each property, add a text and input field in a row
        loadEventAdapterProperty(adapterSchema.localInputEventAdapterProperties[i], eventReceiverInputTable, i);
    }

}
function loadEventAdapterRelatedProperties(toPropertyHeader, propertiesHeading) {

    var selectedIndex = document.getElementById("eventAdapterTypeFilter").selectedIndex;
    var selected_text = document.getElementById("eventAdapterTypeFilter").options[selectedIndex].text;

    jQuery.ajax({
        type: "POST",
        url: "../eventreceiver/get_adapter_properties_ajaxprocessor.jsp?eventAdapterType=" + selected_text + "",
        data: {},
        contentType: "application/json; charset=utf-8",
        dataType: "text",
        async: false,
        success: function (propertiesString) {

            if (propertiesString != null) {
                var jsonObject = JSON.parse(propertiesString);
                loadEventAdapterProperties(jsonObject, propertiesHeading);

                //if (jsonObject != undefined) {
                //    var propertyLoop = 0;
                //    jQuery.each(jsonObject, function (index, messageProperty) {
                //        loadEventAdapterMessageProperties(messageProperty, eventReceiverInputTable, propertyLoop, inputProperty, inputRequiredProperty);
                //        propertyLoop = propertyLoop + 1;
                //    });
                //}

            }
        }
    });

    loadMappingUiElements();
}


function handleAdvancedMapping() {
    var outerDiv = document.getElementById("outerDiv");

    if ((advancedMappingCounter % 2) == 0) {
        outerDiv.style.display = "";
    } else {
        outerDiv.style.display = "none";
    }
    advancedMappingCounter = advancedMappingCounter + 1;

}

function loadMappingUiElements() {

    var selectedIndex = document.getElementById("streamIdFilter").selectedIndex;
    var streamNameWithVersion = document.getElementById("streamIdFilter").options[selectedIndex].text;

    selectedIndex = document.getElementById("mappingTypeFilter").selectedIndex;
    var inputMappingType = document.getElementById("mappingTypeFilter").options[selectedIndex].text;

    var outerDiv = document.getElementById("outerDiv");
    outerDiv.innerHTML = "";

    jQuery.ajax({
        type: "POST",
        url: "../eventreceiver/get_mapping_ui_ajaxprocessor.jsp?mappingType=" + inputMappingType + "&streamNameWithVersion=" + streamNameWithVersion,
        data: {},
        contentType: "text/html; charset=utf-8",
        dataType: "text",
        success: function (ui_content) {
            if (ui_content != null) {
                outerDiv.innerHTML = ui_content;
                if (inputMappingType == "text") {
                    updateAttributeType();
                }
            }
        }
    });

}


function enable_disable_Registry(obj) {

    if (jQuery(obj).attr('id') == "registry_text") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputTextMappingInline");
            var innerRegistryRow = document.getElementById("outputTextMappingRegistry");
            var inlineRadio = document.getElementById("inline_text");
            inlineRadio.checked = false;
            innerInlineRow.style.display = "none";
            innerRegistryRow.style.display = "";

        }
    }

    else if (jQuery(obj).attr('id') == "inline_text") {
        if ((jQuery(obj).is(':checked'))) {

            var innerInlineRow = document.getElementById("outputTextMappingInline");
            var innerRegistryRow = document.getElementById("outputTextMappingRegistry");
            var registryRadio = document.getElementById("registry_text");
            registryRadio.checked = false;
            innerInlineRow.style.display = "";
            innerRegistryRow.style.display = "none";
        }

    }

    else if (jQuery(obj).attr('id') == "registry_xml") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputXMLMappingInline");
            var innerRegistryRow = document.getElementById("outputXMLMappingRegistry");
            var inlineRadio = document.getElementById("inline_xml");
            inlineRadio.checked = false;
            innerInlineRow.style.display = "none";
            innerRegistryRow.style.display = "";
        }
    }

    else if (jQuery(obj).attr('id') == "inline_xml") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputXMLMappingInline");
            var innerRegistryRow = document.getElementById("outputXMLMappingRegistry");
            var registryRadio = document.getElementById("registry_xml");
            registryRadio.checked = false;
            innerInlineRow.style.display = "";
            innerRegistryRow.style.display = "none";
        }

    }

    else if (jQuery(obj).attr('id') == "registry_json") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputJSONMappingInline");
            var innerRegistryRow = document.getElementById("outputJSONMappingRegistry");
            var inlineRadio = document.getElementById("inline_json");
            inlineRadio.checked = false;
            innerInlineRow.style.display = "none";
            innerRegistryRow.style.display = "";
        }
    }

    else if (jQuery(obj).attr('id') == "inline_json") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputJSONMappingInline");
            var innerRegistryRow = document.getElementById("outputJSONMappingRegistry");
            var registryRadio = document.getElementById("registry_json");
            registryRadio.checked = false;
            innerInlineRow.style.display = "";
            innerRegistryRow.style.display = "none";
        }

    }

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

    for (var i = 0; i < tableTBody.rows.length; i++) {

        var row = tableTBody.rows[i];
        var column1 = row.cells[1].innerHTML;

        if (propName.value == column1) {
            error = propName.value + " already defined.\n";
            break;
        }
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

    propDefault.value = "";
    noPropertyDiv.style.display = "none";
}


var advancedMappingCounter = 0;

//Method that used in jsp files

function getWso2EventDataValues(dataTable, inputDataType) {

    var wso2EventDataTable = document.getElementById(dataTable);

    var wso2EventData = "";
    for (var i = 0; i < wso2EventDataTable.rows.length; i++) {

        var column0 = document.getElementById(inputDataType + "EventPropertyName_" + i).value;
        var column2 = document.getElementById(inputDataType + "EventMappedValue_" + i).value;
        var column3 = document.getElementById(inputDataType + "EventType_" + i).value;

        if (column0.trim() == "") {
            return "invalid";
        }

        wso2EventData = wso2EventData + column0 + "^=" + column2 + "^=" + column3 + "$=";
    }
    return wso2EventData;
}

function getJsonDataValues(dataTable) {

    var jsonData = "";
    for (var i = 0; i < dataTable.rows.length; i++) {

        var column0 = document.getElementById("inputPropertyValue_" + i).value;
        var column1 = document.getElementById("inputPropertyName_" + i).value;
        var column2 = document.getElementById("inputPropertyType_" + i).value;
        var column3 = document.getElementById("inputPropertyDefault_" + i).value;

        if (column0.trim() == "") {
            return "invalid";
        }

        // For JSON we use a different terminator (*) since $ is already used in JSONPath
        jsonData = jsonData + column0 + "^=" + column1 + "^=" + column2 + "^=" + column3 + "*=";
    }
    return jsonData;
}

function getXpathDataValues(dataTable) {

    var xpathData = "";
    for (var i = 0; i < dataTable.rows.length; i++) {

        var column0 = document.getElementById("inputPropertyValue_" + i).value;
        var column1 = document.getElementById("inputPropertyName_" + i).value;
        var column2 = document.getElementById("inputPropertyType_" + i).value;
        var column3 = document.getElementById("inputPropertyDefault_" + i).value;


        if (column0 == "") {
            return "invalid";
        }

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
    for (var i = 0; i < dataTable.rows.length; i++) {

        var column0 = document.getElementById("inputMapPropName_" + i).value;
        var column1 = document.getElementById("inputMapPropValueOf_" + i).value;
        var column2 = document.getElementById("inputMapPropType_" + i).value;

        if (column0.trim() == "") {
            return "invalid";
        }

        mapEventData = mapEventData + column0 + "^=" + column1 + "^=" + column2 + "^=" + "$=";
    }
    return mapEventData;
}


function addEventReceiverViaPopup(form, toStreamId, redirectPage) {

    var isFieldEmpty = false;

    var eventReceiverName = document.getElementById("eventReceiverId").value.trim();
    var eventAdapterInfo = document.getElementById("eventAdapterTypeFilter")[document.getElementById("eventAdapterTypeFilter").selectedIndex].value;

    var toStreamNameAndVersion = toStreamId.split(":");
    if (toStreamNameAndVersion.length != 2) {
        CARBON.showErrorDialog("Could not find a valid To Stream Id, Please check.");
        return;
    }

    var toStreamName = toStreamNameAndVersion[0];
    var toStreamVersion = toStreamNameAndVersion[1];

    var customMappingValue = "disable";

    if (((advancedMappingCounter % 2) != 0)) {
        customMappingValue = "enable";
    }

    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_\.]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventReceiverName)) {
        CARBON.showErrorDialog("Invalid character found in event receiver name.");
        return;
    }
    if (isFieldEmpty || (eventReceiverName == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }
    if (toStreamName != "" && toStreamVersion == "") {
        toStreamVersion = "1.0.0";
    }

    var propertyCount = 0;
    var propertyString = "";

    // all properties, not required and required are checked
    while (document.getElementById("property_Required_" + propertyCount) != null || document.getElementById("property_" + propertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("property_Required_" + propertyCount) != null) {
            if (document.getElementById("property_Required_" + propertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                propertyString = "";
                break;
            }
            else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("property_Required_" + propertyCount).value.trim();
                var propertyName = document.getElementById("property_Required_" + propertyCount).name;
                if (propertyString != "") {
                    propertyString = propertyString + "|=";
                }
                propertyString = propertyString + propertyName + "$=" + propertyValue;
            }
        } else if (document.getElementById("property_" + propertyCount) != null) {
            var notRequiredPropertyName = document.getElementById("property_" + propertyCount).name;
            var notRequiredPropertyValue = document.getElementById("property_" + propertyCount).value.trim();
            if (notRequiredPropertyValue == "") {
                notRequiredPropertyValue = "  ";
            }
            if (propertyString != "") {
                propertyString = propertyString + "|=";
            }
            propertyString = propertyString + notRequiredPropertyName + "$=" + notRequiredPropertyValue;
        }
        propertyCount++;
    }

    var mappingType = "";
    if (isFieldEmpty) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'wso2event') {

        mappingType = "wso2event";
        var metaData = "";
        var correlationData = "";
        var payloadData = "";

        if (customMappingValue == "enable") {
            metaData = getWso2EventDataValues("addMetaEventDataTable", 'meta');
            correlationData = getWso2EventDataValues("addCorrelationEventDataTable", 'correlation');
            payloadData = getWso2EventDataValues("addPayloadEventDataTable", 'payload');
        }

        if ((metaData == "" && correlationData == "" && payloadData == "" && customMappingValue == "enable") || correlationData == "invalid" || payloadData == "invalid" || metaData == "invalid") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
                method: 'POST',
                asynchronous: false,
                parameters: {
                    eventReceiverName: eventReceiverName, toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion, eventAdapterInfo: eventAdapterInfo, mappingType: mappingType,
                    propertySet: propertyString, customMappingValue: customMappingValue,
                    metaData: metaData, correlationData: correlationData, payloadData: payloadData
                },
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventreceiver/index.jsp?ordinal=1";
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'text') {

        mappingType = "text";
        var textData = "";

        if (customMappingValue == "enable") {
            var textDataTable = document.getElementById("inputTextMappingTable");
            if (textDataTable.rows.length > 1) {
                textData = getTextDataValues(textDataTable);
            }
        }

        if (textData == "" && customMappingValue == "enable") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
                method: 'POST',
                asynchronous: false,
                parameters: {
                    eventReceiverName: eventReceiverName,
                    toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion,
                    eventAdapterInfo: eventAdapterInfo,
                    mappingType: mappingType,
                    propertySet: propertyString,
                    textData: textData,
                    customMappingValue: customMappingValue
                },
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventreceiver/index.jsp?ordinal=1";
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'xml') {
        var parentSelectorXpath = document.getElementById("parentSelectorXpath").value;

        mappingType = "xml";
        var prefixData = "";
        var xpathData = "";


        if (customMappingValue == "enable") {
            var xpathPrefixTable = document.getElementById("inputXpathPrefixTable");
            if (xpathPrefixTable.rows.length > 1) {
                prefixData = getXpathPrefixValues(xpathPrefixTable);
            }

            var xpathExprTable = document.getElementById("addXpathExprTable");
            if (xpathExprTable.rows.length > 0) {
                xpathData = getXpathDataValues(xpathExprTable);
            }
        }

        if ((prefixData == "" && xpathData == "" && customMappingValue == "enable") || xpathData == "invalid") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
                method: 'POST',
                asynchronous: false,
                parameters: {
                    eventReceiverName: eventReceiverName,
                    toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion,
                    eventAdapterInfo: eventAdapterInfo,
                    mappingType: mappingType,
                    propertySet: propertyString,
                    parentSelectorXpath: parentSelectorXpath,
                    prefixData: prefixData,
                    xpathData: xpathData,
                    customMappingValue: customMappingValue
                },
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventreceiver/index.jsp?ordinal=1";
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'map') {

        mappingType = "map";
        var mapData = "";

        var mapDataTable = document.getElementById("addMapDataTable");
        if (mapDataTable.rows.length > 0 && customMappingValue == "enable") {
            mapData = getMapDataValues(mapDataTable);
        }

        if ((mapData == "" && customMappingValue == "enable") || mapData == "invalid") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
        else {
            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
                method: 'POST',
                asynchronous: false,
                parameters: {
                    eventReceiverName: eventReceiverName,
                    toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion,
                    eventAdapterInfo: eventAdapterInfo,
                    mappingType: mappingType,
                    propertySet: propertyString,
                    mapData: mapData,
                    customMappingValue: customMappingValue
                },
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventreceiver/index.jsp?ordinal=1";
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'json') {

        mappingType = "json";
        var jsonData = "";

        var jsonDataTable = document.getElementById("addJsonpathExprTable");
        if (customMappingValue == "enable" && jsonDataTable.rows.length > 0) {
            jsonData = getJsonDataValues(jsonDataTable);
        }

        if ((customMappingValue == "enable" && jsonData == "") || jsonData == "invalid") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
                method: 'POST',
                asynchronous: false,
                parameters: {
                    eventReceiverName: eventReceiverName,
                    toStreamName: toStreamName,
                    toStreamVersion: toStreamVersion,
                    eventAdapterInfo: eventAdapterInfo,
                    mappingType: mappingType,
                    propertySet: propertyString,
                    jsonData: jsonData,
                    customMappingValue: customMappingValue
                },
                onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
                            if (redirectPage != "none") {
                                window.location.href = "../eventreceiver/index.jsp?ordinal=1";
                            }
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + event.responseText.trim());
                    }
                }
            });
        }
    }
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
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    YAHOO.util.Dom.addClass(newCell2, "property-names");
    newCell2.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeInputProperty(this,\'' + 'xml' + '\')">Delete</a>';

    prefixName.value = "";
    xpathNs.value = "";
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

function updateAttributeType() {
    var typeMap = JSON.parse( document.getElementById("streamMapping").getAttribute("mapping"));
    var selectedIndex = document.getElementById("inputPropertyName").selectedIndex;
    document.getElementById("inputPropertyType").value = typeMap[selectedIndex];
}





