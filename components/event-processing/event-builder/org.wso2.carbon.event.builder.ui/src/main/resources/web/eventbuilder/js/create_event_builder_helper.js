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

// event adaptor msg config properties are taken from back-end and rendered according to fields
function showMessageConfigProperties() {
    var eventBuilderInputTable = document.getElementById("eventBuilderInputTable");
    var taSelect = document.getElementById("eventAdaptorNameSelect");
    var selectedIndex = taSelect.selectedIndex;
    var selected_text = taSelect.options[selectedIndex].text;
    var insertIndex = document.getElementById("eventAdaptorSelectTr").rowIndex + 1;

    var inputPropertyIdPrefix = "msgConfigProperty_";
    var requiredInputPropertyIdPrefix = "msgConfigProperty_Required_";

    jQuery.ajax({
        type: "POST",
        url: "../eventbuilder/get_mappings_ajaxprocessor.jsp?eventAdaptorName=" + selected_text + "",
        data: {},
        contentType: "application/json; charset=utf-8",
        dataType: "text",
        async: false,
        success: function (mappingTypes) {

            if (mappingTypes != null) {
                mappingTypes = mappingTypes.trim();
                var mappings = JSON.parse(mappingTypes);
                var inputMappingSelect = document.getElementById('inputMappingTypeSelect');
                inputMappingSelect.length = 0;
                // for each property, add a text and input field in a row
                for (i = 0; i < mappings.length; i++) {
                    var mappingName = mappings[i];
                    if (mappingName != undefined && mappingName != "") {
                        mappingName = mappingName.trim();
                        inputMappingSelect.add(new Option(mappingName, mappingName), null);
                    }
                }

            }
        }
    });


    // delete all msg config property rows
    for (i = eventBuilderInputTable.rows.length - 1; i > 1; i--) {
        var tableRow = eventBuilderInputTable.rows[i];
        var inputCell = tableRow.cells[1];
        if (inputCell != undefined) {
            var cellElements = inputCell.getElementsByTagName('input');
            for (var j = 0; j < cellElements.length; j++) {
                if (cellElements[j].id.substring(0, inputPropertyIdPrefix.length) == inputPropertyIdPrefix) {
                    eventBuilderInputTable.deleteRow(i);
                    break;
                }
            }
        }
    }


    jQuery.ajax({
        type: "POST",
        url: "../eventbuilder/get_properties_ajaxprocessor.jsp?eventName=" + selected_text + "",
        data: {},
        contentType: "application/json; charset=utf-8",
        dataType: "text",
        async: false,
        success: function (msg) {
            if (msg != null) {
                var inputTransportProperties = JSON.parse(msg);
                if (inputTransportProperties != '') {
                    var propertyIndex = 0;
                    jQuery.each(inputTransportProperties, function (index, messageProperty) {
                        loadTransportMessageProperty(messageProperty, eventBuilderInputTable, propertyIndex, insertIndex, inputPropertyIdPrefix, requiredInputPropertyIdPrefix);
                        propertyIndex = propertyIndex + 1;
                        insertIndex = insertIndex + 1;
                    });
                }
                loadMappingUiElements();
            }
        }
    });
}

function clearInputPropertyTable() {
    var inputPropertyTable = document.getElementById("wso2EventMappingPropertyTable");
    var noPropDiv = document.getElementById("noInputProperty");
    clearDataInTable(inputPropertyTable.getName());
    noPropDiv.style.display = "block";

}

function loadMappingUiElements() {
    var taSelect = document.getElementById("inputMappingTypeSelect");
    var inputMappingType = taSelect.options[taSelect.selectedIndex].text;
    var mappingUiTd = document.getElementById("mappingUiTd");
    mappingUiTd.innerHTML = "";

    jQuery.ajax({
        type: "POST",
        url: "../eventbuilder/get_mapping_ui_ajaxprocessor.jsp?mappingType=" + inputMappingType + "",
        data: {},
        contentType: "text/html; charset=utf-8",
        dataType: "text",
        success: function (ui_content) {
            if (ui_content != null) {
                mappingUiTd.innerHTML = ui_content;
            }
        }
    });

}

function loadTransportMessageProperty(messageProperty, eventBuilderInputTable, propertyIndex, insertIndex, optionalPropertyIdPrefix, requiredPropertyIdPrefix) {
    var tableRow = eventBuilderInputTable.insertRow(insertIndex);
    var textLabel = tableRow.insertCell(0);
    var displayName = messageProperty.localDisplayName.trim();
    textLabel.innerHTML = displayName;
    var elementIdPrefix = optionalPropertyIdPrefix;
    var inputElementType = "text";
    var hint = "";
    var defaultValue = "";
    var htmlForHint = "";

    if (messageProperty.localRequired) {
        textLabel.innerHTML = displayName + '<span class="required">*</span>';
        elementIdPrefix = requiredPropertyIdPrefix;
    }

    if (messageProperty.localSecured) {
        inputElementType = "password";
    }

    if (messageProperty.localHint != undefined && messageProperty.localHint != "") {
        hint = messageProperty.localHint;
        htmlForHint = '<div class="sectionHelp">' + hint + '</div>'
    }

    if (messageProperty.localDefaultValue != undefined && messageProperty.localDefaultValue != "") {
        defaultValue = messageProperty.localDefaultValue;
    }

    var inputField = tableRow.insertCell(1);
    inputField.innerHTML = '<input type="' + inputElementType + '" id="' + elementIdPrefix + propertyIndex + '" name="' + messageProperty.localKey + '" value="' + defaultValue + '" class="initE" /> <br/> ' + htmlForHint;
}

function createStreamDefinition(element) {
    var selectedVal = element.options[element.selectedIndex].value;
    if (selectedVal == 'createStreamDef') {
        var streamDef = getOutStreamDefinitionAsJson();
        new Ajax.Request('../eventstream/popup_create_event_stream_ajaxprocessor.jsp', {
            method: 'post',
            parameters: {streamDef: streamDef},
            asynchronous: false,
            onSuccess: function (data) {
                showCustomPopupDialog(data.responseText, "Create Stream Definition", "80%", "", onSuccessCreateStreamDefinition, "90%");
            }
        });
    }
}

/**
 * Display the Info Message inside a jQuery UI's dialog widget.
 * @method showPopupDialog
 * @param {String} message to display
 * @return {Boolean}
 */
function showCustomPopupDialog(message, title, windowHight, okButton, callback, windowWidth) {
    var strDialog = "<div id='custom_dialog' title='" + title + "'><div id='popupDialog'></div>" + message + "</div>";
    var requiredWidth = 750;
    if (windowWidth) {
        requiredWidth = windowWidth;
    }
    var func = function () {
        jQuery("#custom_dcontainer").hide();
        jQuery("#custom_dcontainer").html(strDialog);
        if (okButton) {
            jQuery("#custom_dialog").dialog({
                close: function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#custom_dcontainer").empty();
                    return false;
                },
                buttons: {
                    "OK": function () {
                        if (callback && typeof callback == "function")
                            callback();
                        jQuery(this).dialog("destroy").remove();
                        jQuery("#custom_dcontainer").empty();
                        return false;
                    }
                },
                autoOpen: false,
                height: windowHight,
                width: requiredWidth,
                minHeight: windowHight,
                minWidth: requiredWidth,
                modal: true
            });
        } else {
            jQuery("#custom_dialog").dialog({
                close: function () {
                    if (callback && typeof callback == "function")
                        callback();
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#custom_dcontainer").empty();
                    return false;
                },
                autoOpen: false,
                height: windowHight,
                width: requiredWidth,
                minHeight: windowHight,
                minWidth: requiredWidth,
                modal: true
            });
        }

        jQuery('.ui-dialog-titlebar-close').click(function () {
            jQuery('#custom_dialog').dialog("destroy").remove();
            jQuery("#custom_dcontainer").empty();
            jQuery("#custom_dcontainer").html('');
        });
        jQuery("#custom_dcontainer").show();
        jQuery("#custom_dialog").dialog("open");
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }

};


function onSuccessCreateStreamDefinition() {
    refreshStreamDefInfo("streamNameFilter");
}

function refreshStreamDefInfo(streamDefSelectId) {
    var streamDefSelect = document.getElementById(streamDefSelectId);
    new Ajax.Request('../eventstream/get_stream_definitions_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        onSuccess: function (event) {
            streamDefSelect.length = 0;
            // for each property, add a text and input field in a row
            var jsonArrStreamDefIds = JSON.parse(event.responseText);
            for (i = 0; i < jsonArrStreamDefIds.length; i++) {
                var streamDefId = jsonArrStreamDefIds[i];
                if (streamDefId != undefined && streamDefId != "") {
                    streamDefId = streamDefId.trim();
                    streamDefSelect.add(new Option(streamDefId, streamDefId), null);
                }
            }
            streamDefSelect.add(new Option("-- Create Stream Definition --", "createStreamDef"), null);
        }
    });
}

function getOutStreamDefinitionAsJson() {
    var toStreamName = "org.wso2.default.stream.name";
    var toStreamVersion = "1.0.0";
    var toStreamId = toStreamName + ":" + toStreamVersion;

    var mappingTypeSelect = document.getElementById("inputMappingTypeSelect");
    var inputMappingType = mappingTypeSelect.options[mappingTypeSelect.selectedIndex].text;

    var outputStreamAsString = "";
    var dataTable = null;
    if (inputMappingType == 'wso2event') {
        dataTable = document.getElementById("inputWso2EventDataTable");
        var metaData = getStreamAttributesForWso2Event(dataTable, "meta_");
        var correlationData = getStreamAttributesForWso2Event(dataTable, "correlation_");
        var payloadData = getStreamAttributesForWso2Event(dataTable, "");
        outputStreamAsString = "{\"streamId\":\"" + toStreamId + "\"," +
            "\"name\":" + toStreamName + "," +
            "\"version\":" + toStreamVersion + "," +
            "\"metaData\":" + metaData + "," +
            "\"correlationData\":" + correlationData + "," +
            "\"payloadData\":" + payloadData +
            "}";
    } else if (inputMappingType == 'xml') {
        dataTable = document.getElementById("inputXpathExprTable");
        var xpathMetaData = getStreamAttributesForXml(dataTable, "meta_");
        var xpathCorrelationData = getStreamAttributesForXml(dataTable, "correlation_");
        var xpathPayloadData = getStreamAttributesForXml(dataTable, "");
        outputStreamAsString = "{\"streamId\":\"" + toStreamId + "\"," +
            "\"name\":" + toStreamName + "," +
            "\"version\":" + toStreamVersion + "," +
            "\"metaData\":" + xpathMetaData + "," +
            "\"correlationData\":" + xpathCorrelationData + "," +
            "\"payloadData\":" + xpathPayloadData +
            "}";
    } else if (inputMappingType == 'json') {
        dataTable = document.getElementById("inputJsonpathExprTable");
        var jsonpathMetaData = getStreamAttributesForJson(dataTable, "meta_");
        var jsonpathCorrelationData = getStreamAttributesForJson(dataTable, "correlation_");
        var jsonpathPayloadData = getStreamAttributesForJson(dataTable, "");
        outputStreamAsString = "{\"streamId\":\"" + toStreamId + "\"," +
            "\"name\":" + toStreamName + "," +
            "\"version\":" + toStreamVersion + "," +
            "\"metaData\":" + jsonpathMetaData + "," +
            "\"correlationData\":" + jsonpathCorrelationData + "," +
            "\"payloadData\":" + jsonpathPayloadData +
            "}";
    } else if (inputMappingType == 'map') {
        dataTable = document.getElementById("inputMapPropertiesTable");
        var mapMetaData = getStreamAttributesForMap(dataTable, "meta_");
        var mapCorrelationData = getStreamAttributesForMap(dataTable, "correlation_");
        var mapPayloadData = getStreamAttributesForMap(dataTable, "");
        outputStreamAsString = "{\"streamId\":\"" + toStreamId + "\"," +
            "\"name\":" + toStreamName + "," +
            "\"version\":" + toStreamVersion + "," +
            "\"metaData\":" + mapMetaData + "," +
            "\"correlationData\":" + mapCorrelationData + "," +
            "\"payloadData\":" + mapPayloadData +
            "}";
    } else if (inputMappingType == 'text') {
        dataTable = document.getElementById("inputTextMappingTable");
        var textMetaData = getStreamAttributesForText(dataTable, "meta_");
        var textCorrelationData = getStreamAttributesForText(dataTable, "correlation_");
        var textPayloadData = getStreamAttributesForText(dataTable, "");
        outputStreamAsString = "{\"streamId\":\"" + toStreamId + "\"," +
            "\"name\":" + toStreamName + "," +
            "\"version\":" + toStreamVersion + "," +
            "\"metaData\":" + textMetaData + "," +
            "\"correlationData\":" + textCorrelationData + "," +
            "\"payloadData\":" + textPayloadData +
            "}";
    }

    return outputStreamAsString;
}

function getStreamAttributesForWso2Event(dataTable, typePrefix) {

    var wso2EventStreamAttributes = "[";
    if (document.getElementById("customMappingCheckBox").checked) {
        if (typePrefix == "") {
            for (var i = 1; i < dataTable.rows.length; i++) {
                var row = dataTable.rows[i];
                var column2 = row.cells[2].innerHTML;
                if (!(column2.substring(0,"meta_".length) == "meta_") && !(column2.substring(0,"correlation_".length) == "correlation_")) {
                    var column3 = row.cells[3].innerHTML;
                    if (wso2EventStreamAttributes.charAt(wso2EventStreamAttributes.length - 1) == '}') {
                        wso2EventStreamAttributes = wso2EventStreamAttributes + ","
                    }
                    wso2EventStreamAttributes = wso2EventStreamAttributes +
                        "{\"name\":" + column2 + ",\"type\":" + column3 + "}";
                }
            }
        } else {
            for (var i = 1; i < dataTable.rows.length; i++) {
                var row = dataTable.rows[i];
                var column2 = row.cells[2].innerHTML;
                if (column2.substring(0,typePrefix.length) == typePrefix) {
                    var column3 = row.cells[3].innerHTML;
                    if (wso2EventStreamAttributes.charAt(wso2EventStreamAttributes.length - 1) == '}') {
                        wso2EventStreamAttributes = wso2EventStreamAttributes + ","
                    }
                    wso2EventStreamAttributes = wso2EventStreamAttributes +
                        "{\"name\":" + column2.substring(typePrefix.length) + ",\"type\":" + column3 + "}";
                }
            }
        }
    }
    wso2EventStreamAttributes = wso2EventStreamAttributes + "]";
    return wso2EventStreamAttributes;
}

function getStreamAttributesForXml(dataTable, typePrefix) {
    var xmlStreamAttributes = "[";
    if (typePrefix == "") {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var column1 = row.cells[1].innerHTML;
            if (!(column1.substring(0,"meta_".length) == "meta_") && !(column1.substring(0,"correlation_".length) == "correlation_")) {
                var column2 = row.cells[2].innerHTML;
                if (xmlStreamAttributes.charAt(xmlStreamAttributes.length - 1) == '}') {
                    xmlStreamAttributes = xmlStreamAttributes + ","
                }
                xmlStreamAttributes = xmlStreamAttributes +
                    "{\"name\":" + column1 + ",\"type\":" + column2 + "}";
            }
        }
    } else {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var column1 = row.cells[1].innerHTML;
            if (column1.substring(0,typePrefix.length) == typePrefix) {
                var column2 = row.cells[2].innerHTML;
                if (xmlStreamAttributes.charAt(xmlStreamAttributes.length - 1) == '}') {
                    xmlStreamAttributes = xmlStreamAttributes + ","
                }
                xmlStreamAttributes = xmlStreamAttributes +
                    "{\"name\":" + column1.substring(typePrefix.length) + ",\"type\":" + column2 + "}";
            }
        }
    }
    xmlStreamAttributes = xmlStreamAttributes + "]";
    return xmlStreamAttributes;

}

function getStreamAttributesForJson(dataTable, typePrefix) {
    var jsonStreamAttributes = "[";
    if (typePrefix == "") {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var column1 = row.cells[1].innerHTML;
            if (!(column1.substring(0,"meta_".length) == "meta_") && !(column1.substring(0,"correlation_".length) == "correlation_")) {
                var column2 = row.cells[2].innerHTML;
                if (jsonStreamAttributes.charAt(jsonStreamAttributes.length - 1) == '}') {
                    jsonStreamAttributes = jsonStreamAttributes + ","
                }
                jsonStreamAttributes = jsonStreamAttributes +
                    "{\"name\":" + column1 + ",\"type\":" + column2 + "}";
            }
        }
    } else {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var column1 = row.cells[1].innerHTML;
            if (column1.substring(0,typePrefix.length) == typePrefix) {
                var column2 = row.cells[2].innerHTML;
                if (jsonStreamAttributes.charAt(jsonStreamAttributes.length - 1) == '}') {
                    jsonStreamAttributes = jsonStreamAttributes + ","
                }
                jsonStreamAttributes = jsonStreamAttributes +
                    "{\"name\":" + column1.substring(typePrefix.length) + ",\"type\":" + column2 + "}";
            }
        }
    }
    jsonStreamAttributes = jsonStreamAttributes + "]";
    return jsonStreamAttributes;

}

function getStreamAttributesForMap(dataTable, typePrefix) {
    var mapStreamAttributes = "[";
    if (typePrefix == "") {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var column1 = row.cells[1].innerHTML;
            if (!(column1.substring(0,"meta_".length) == "meta_") && !(column1.substring(0,"correlation_".length) == "correlation_")) {
                var column2 = row.cells[2].innerHTML;
                if (mapStreamAttributes.charAt(mapStreamAttributes.length - 1) == '}') {
                    mapStreamAttributes = mapStreamAttributes + ","
                }
                mapStreamAttributes = mapStreamAttributes +
                    "{\"name\":" + column1 + ",\"type\":" + column2 + "}";
            }
        }
    } else {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var column1 = row.cells[1].innerHTML;
            if (column1.substring(0,typePrefix.length) == typePrefix) {
                var column2 = row.cells[2].innerHTML;
                if (mapStreamAttributes.charAt(mapStreamAttributes.length - 1) == '}') {
                    mapStreamAttributes = mapStreamAttributes + ","
                }
                mapStreamAttributes = mapStreamAttributes +
                    "{\"name\":" + column1.substring(typePrefix.length) + ",\"type\":" + column2 + "}";
            }
        }
    }
    mapStreamAttributes = mapStreamAttributes + "]";
    return mapStreamAttributes;
}

function getStreamAttributesForText(dataTable, typePrefix) {
    var textStreamAttributes = "[";
    if (typePrefix == "") {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var column1 = row.cells[1].innerHTML;
            if (!(column1.substring(0,"meta_".length) == "meta_") && !(column1.substring(0,"correlation_".length) == "correlation_")) {
                var column2 = row.cells[2].innerHTML;
                if (textStreamAttributes.charAt(textStreamAttributes.length - 1) == '}') {
                    textStreamAttributes = textStreamAttributes + ","
                }
                textStreamAttributes = textStreamAttributes +
                    "{\"name\":" + column1 + ",\"type\":" + column2 + "}";
            }
        }
    } else {
        for (var i = 1; i < dataTable.rows.length; i++) {
            var row = dataTable.rows[i];
            var column1 = row.cells[1].innerHTML;
            if (column1.substring(0,typePrefix.length) == typePrefix) {
                var column2 = row.cells[2].innerHTML;
                if (textStreamAttributes.charAt(textStreamAttributes.length - 1) == '}') {
                    textStreamAttributes = textStreamAttributes + ","
                }
                textStreamAttributes = textStreamAttributes +
                    "{\"name\":" + column1.substring(typePrefix.length) + ",\"type\":" + column2 + "}";
            }
        }
    }
    textStreamAttributes = textStreamAttributes + "]";
    return textStreamAttributes;
}
