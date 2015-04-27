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

//Method that used in jsp files

function addOutputWSO2EventProperty(dataType) {
    var propName = document.getElementById("output" + dataType + "DataPropName");
    var propValueOf = document.getElementById("output" + dataType + "DataPropValueOf");
    var propertyTable = document.getElementById("output" + dataType + "DataTable");
    var noPropertyDiv = document.getElementById("noOutput" + dataType + "Data");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }

    if (propValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";


    var newTableRow = propertyTable.insertRow(propertyTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propName.value;

    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propValueOf.value;

    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCel3 = newTableRow.insertCell(2);
    newCel3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeOutputProperty(this,\'' + dataType + '\')">Delete</a>';

    YAHOO.util.Dom.addClass(newCel3, "property-names");

    propName.value = "";
    noPropertyDiv.style.display = "none";

}

function getWSO2EventDataValues(dataTable) {

    var wso2EventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].textContent;
        var column1 = row.cells[1].textContent;

        wso2EventData = wso2EventData + column0 + "^=" + column1 + "^=" + "$=";
    }
    return wso2EventData;
}

function getMapDataValues(dataTable) {

    var mapEventData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].textContent;
        var column1 = row.cells[1].textContent;

        mapEventData = mapEventData + column0 + "^=" + column1 + "^=" + "$=";
    }
    return mapEventData;
}


function removeOutputProperty(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToERemove = rowToRemove.cells[0].innerHTML.trim();
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Output Property removed successfully!!");
    return;
}


function addOutputMapProperty() {
    var propName = document.getElementById("outputMapPropName");
    var propValueOf = document.getElementById("outputMapPropValueOf");
    var propertyTable = document.getElementById("outputMapPropertiesTable");
    var noPropertyDiv = document.getElementById("noOutputMapProperties");

    var error = "";

    if (propName.value == "") {
        error = "Name field is empty.\n";
    }

    if (propValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    propertyTable.style.display = "";


    var newTableRow = propertyTable.insertRow(propertyTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propName.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propValueOf.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCel2 = newTableRow.insertCell(2);
    newCel2.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeOutputProperty(this,\'' + 'map' + '\')">Delete</a>';
    YAHOO.util.Dom.addClass(newCel2, "property-names");

    propName.value = "";
    noPropertyDiv.style.display = "none";

}

function enableMapping(isEnabled) {
    var mappingRow1 = document.getElementById("outputWSO2EventMappingMeta");
    var mappingRow2 = document.getElementById("outputWSO2EventMappingCorrelation");
    var mappingRow3 = document.getElementById("outputWSO2EventMappingPayload");
    if (isEnabled) {
        mappingRow1.style.display = "";
        mappingRow2.style.display = "";
        mappingRow3.style.display = "";
    } else {
        mappingRow1.style.display = "none";
        mappingRow2.style.display = "none";
        mappingRow3.style.display = "none";
    }
}


var ENABLE = "enable";
var DISABLE = "disable";
var STAT = "statistics";
var TRACE = "Tracing";

function deleteOutFlowEventReceiver(eventStreamWithVersion, eventReceiverName) {
    var theform = document.getElementById('deleteForm1');
    theform.eventReceiver.value = eventReceiverName;
    theform.eventStreamWithVersion.value = eventStreamWithVersion;
    theform.submit();
}


function deleteEventReceiver(eventReceiverName) {
    var theform = document.getElementById('deleteForm');
    theform.eventReceiver.value = eventReceiverName;
    theform.submit();
}

function disableReceiverStat(eventReceiverName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventreceiver/stat_tracing-ajaxprocessor.jsp',
                    data:'eventReceiverName=' + eventReceiverName + '&action=disableStat',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventReceiverName, DISABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                               ' ' + eventReceiverName);
                    }
                });
}

function enableReceiverStat(eventReceiverName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventreceiver/stat_tracing-ajaxprocessor.jsp',
                    data:'eventReceiverName=' + eventReceiverName + '&action=enableStat',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventReceiverName, ENABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                               ' ' + eventReceiverName);
                    }
                });
}

function handleCallback(eventReceiverName, action, type) {
    var element;
    if (action == "enable") {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventReceiverName);
            element.style.display = "";
            element = document.getElementById("enableStat" + eventReceiverName);
            element.style.display = "none";
        } else {
            element = document.getElementById("disableTracing" + eventReceiverName);
            element.style.display = "";
            element = document.getElementById("enableTracing" + eventReceiverName);
            element.style.display = "none";
        }
    } else {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventReceiverName);
            element.style.display = "none";
            element = document.getElementById("enableStat" + eventReceiverName);
            element.style.display = "";
        } else {
            element = document.getElementById("disableTracing" + eventReceiverName);
            element.style.display = "none";
            element = document.getElementById("enableTracing" + eventReceiverName);
            element.style.display = "";
        }
    }
}

function enableReceiverTracing(eventReceiverName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventreceiver/stat_tracing-ajaxprocessor.jsp',
                    data:'eventReceiverName=' + eventReceiverName + '&action=enableTracing',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventReceiverName, ENABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                               ' ' + eventReceiverName);
                    }
                });
}

function disableReceiverTracing(eventReceiverName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventreceiver/stat_tracing-ajaxprocessor.jsp',
                    data:'eventReceiverName=' + eventReceiverName + '&action=disableTracing',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventReceiverName, DISABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                               ' ' + eventReceiverName);
                    }
                });
}

function createPopupEventReceiverUI(streamId) {

    new Ajax.Request('../eventreceiver/create_event_receiver.jsp', {
        method:'POST',
        parameters:{streamId:streamId},
        asynchronous:false,
        onSuccess:function (data) {
            showCustomEventReceiverPopupDialog(data.responseText, "Create Event Receiver", "80%", "", onSuccessCreateEventReceiver, "90%");
        }
    });
}


function showCustomEventReceiverPopupDialog(message, title, windowHight, okButton, callback,
                                             windowWidth) {
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
                                                close:function () {
                                                    jQuery(this).dialog('destroy').remove();
                                                    jQuery("#custom_dcontainer").empty();
                                                    return false;
                                                },
                                                buttons:{
                                                    "OK":function () {
                                                        if (callback && typeof callback == "function") {
                                                            callback();
                                                        }
                                                        jQuery(this).dialog("destroy").remove();
                                                        jQuery("#custom_dcontainer").empty();
                                                        return false;
                                                    }
                                                },
                                                autoOpen:false,
                                                height:windowHight,
                                                width:requiredWidth,
                                                minHeight:windowHight,
                                                minWidth:requiredWidth,
                                                modal:true
                                            });
        } else {
            jQuery("#custom_dialog").dialog({
                                                close:function () {
                                                    if (callback && typeof callback == "function") {
                                                        callback();
                                                    }
                                                    jQuery(this).dialog('destroy').remove();
                                                    jQuery("#custom_dcontainer").empty();
                                                    return false;
                                                },
                                                autoOpen:false,
                                                height:windowHight,
                                                width:requiredWidth,
                                                minHeight:windowHight,
                                                minWidth:requiredWidth,
                                                modal:true
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

}

function createImportedStreamDefinition() {
    new Ajax.Request('../eventstream/popup_create_event_stream_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{callback:"inflow"},
        onSuccess:function (data) {
            showCustomPopupDialog(data.responseText, "Create Stream Definition", "80%", "", "", "90%");
        }
    });
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
                close:function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#custom_dcontainer").empty();
                    return false;
                },
                buttons:{
                    "OK":function () {
                        if (callback && typeof callback == "function") {
                            callback();
                        }
                        jQuery(this).dialog("destroy").remove();
                        jQuery("#custom_dcontainer").empty();
                        return false;
                    }
                },
                autoOpen:false,
                height:windowHight,
                width:requiredWidth,
                minHeight:windowHight,
                minWidth:requiredWidth,
                modal:true
            });
        } else {
            jQuery("#custom_dialog").dialog({
                close:function () {
                    if (callback && typeof callback == "function") {
                        callback();
                    }
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#custom_dcontainer").empty();
                    return false;
                },
                autoOpen:false,
                height:windowHight,
                width:requiredWidth,
                minHeight:windowHight,
                minWidth:requiredWidth,
                modal:true
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

}



//
//function addEventReceiverViaPopup(form, streamNameWithVersion) {
//
//    var isFieldEmpty = false;
//    var inline = "inline";
//    var registry = "registry";
//    var dataFrom = "";
//
//    var eventReceiverName = document.getElementById("eventReceiverId").value.trim();
//    var eventAdapterInfo = document.getElementById("eventAdapterTypeFilter")[document.getElementById("eventAdapterTypeFilter").selectedIndex].value;
//
//
//    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_\.]+$");
//    // Check for white space
//    if (!reWhiteSpace.test(eventReceiverName)) {
//        CARBON.showErrorDialog("Invalid character found in event receiver name.");
//        return;
//    }
//    if (isFieldEmpty || (eventReceiverName == "")) {
//        // empty fields are encountered.
//        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
//        return;
//    }
//
//
//    var propertyCount = 0;
//    var outputPropertyParameterString = "";
//
//    // all properties, not required and required are checked
//    while (document.getElementById("property_Required_" + propertyCount) != null ||
//           document.getElementById("property_" + propertyCount) != null) {
//        // if required fields are empty
//        if (document.getElementById("property_Required_" + propertyCount) != null) {
//            if (document.getElementById("property_Required_" + propertyCount).value.trim() == "") {
//                // values are empty in fields
//                isFieldEmpty = true;
//                outputPropertyParameterString = "";
//                break;
//            } else {
//                // values are stored in parameter string to send to backend
//                var propertyValue = document.getElementById("property_Required_" + propertyCount).value.trim();
//                var propertyName = document.getElementById("property_Required_" + propertyCount).name;
//                outputPropertyParameterString = outputPropertyParameterString + propertyName + "$=" + propertyValue + "|=";
//
//            }
//        } else if (document.getElementById("property_" + propertyCount) != null) {
//            var notRequriedPropertyValue = document.getElementById("property_" + propertyCount).value.trim();
//            var notRequiredPropertyName = document.getElementById("property_" + propertyCount).name;
//            if (notRequriedPropertyValue == "") {
//                notRequriedPropertyValue = "  ";
//            }
//            outputPropertyParameterString = outputPropertyParameterString + notRequiredPropertyName + "$=" + notRequriedPropertyValue + "|=";
//
//
//        }
//        propertyCount++;
//    }
//
//    if (isFieldEmpty) {
//        // empty fields are encountered.
//        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
//        return;
//    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'wso2event') {
//
//
//        var metaData = "";
//        var correlationData = "";
//        var payloadData = "";
//        var customMappingEnabled = "disable";
//
//        if (((advancedMappingCounter % 2) != 0)) {
//            var metaDataTable = document.getElementById("outputMetaDataTable");
//            if (metaDataTable.rows.length > 1) {
//                metaData = getWSO2EventDataValues(metaDataTable);
//            }
//            var correlationDataTable = document.getElementById("outputCorrelationDataTable");
//            if (correlationDataTable.rows.length > 1) {
//                correlationData = getWSO2EventDataValues(correlationDataTable);
//            }
//            var payloadDataTable = document.getElementById("outputPayloadDataTable");
//            if (payloadDataTable.rows.length > 1) {
//                payloadData = getWSO2EventDataValues(payloadDataTable);
//            }
//            customMappingEnabled = "enable";
//        }
//
//        if ((metaData == "" && correlationData == "" && payloadData == "") && ((advancedMappingCounter % 2) != 0)) {
//            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
//            return;
//        } else {
//            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
//                method:'POST',
//                asynchronous:false,
//                parameters:{eventReceiver:eventReceiverName, streamNameWithVersion:streamNameWithVersion,
//                    eventAdapterInfo:eventAdapterInfo, mappingType:"wso2event", outputParameters:outputPropertyParameterString,
//                    metaData:metaData, correlationData:correlationData, payloadData:payloadData, customMappingValue:customMappingEnabled},
//                onSuccess:function (response) {
//                    if ("true" == response.responseText.trim()) {
//                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
//                            window.location.href = "../eventreceiver/index.jsp?ordinal=1";
//                        }, null);
//                        customCarbonWindowClose();
//                    } else {
//                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + response.responseText.trim());
//                    }
//                }
//            })
//        }
//
//    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'text') {
//
//        var textData = "";
//        var customMappingEnabled = "disable";
//
//        if (((advancedMappingCounter % 2) != 0)) {
//            if ((document.getElementById("inline_text")).checked) {
//                textData = document.getElementById("textSourceText").value;
//                dataFrom = inline;
//            } else if ((document.getElementById("registry_text")).checked) {
//                textData = document.getElementById("textSourceRegistry").value;
//                dataFrom = registry;
//            }
//            customMappingEnabled = "enable";
//        }
//
//        if (textData == "" && ((advancedMappingCounter % 2) != 0)) {
//            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
//            return;
//        } else {
//            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
//                method:'POST',
//                asynchronous:false,
//                parameters:{eventReceiver:eventReceiverName, streamNameWithVersion:streamNameWithVersion,
//                    eventAdapterInfo:eventAdapterInfo, mappingType:"text", outputParameters:outputPropertyParameterString,
//                    textData:textData, dataFrom:dataFrom, customMappingValue:customMappingEnabled},
//                onSuccess:function (response) {
//                    if ("true" == response.responseText.trim()) {
//                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
//                            window.location.href = "../eventreceiver/index.jsp?ordinal=1";
//                        }, null);
//                        customCarbonWindowClose();
//                    } else {
//                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + response.responseText.trim());
//                    }
//                }
//            })
//
//        }
//
//    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'xml') {
//
//        var textData = "";
//        var customMappingEnabled = "disable";
//
//        if (((advancedMappingCounter % 2) != 0)) {
//            if ((document.getElementById("inline_xml")).checked) {
//                textData = document.getElementById("xmlSourceText").value;
//                dataFrom = inline;
//            } else if ((document.getElementById("registry_xml")).checked) {
//                textData = document.getElementById("xmlSourceRegistry").value;
//                dataFrom = registry;
//            }
//
//            customMappingEnabled = "enable";
//        }
//
//        if (textData == "" && ((advancedMappingCounter % 2) != 0)) {
//            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
//            return;
//        } else {
//            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
//                method:'POST',
//                asynchronous:false,
//                parameters:{eventReceiver:eventReceiverName, streamNameWithVersion:streamNameWithVersion,
//                    eventAdapterInfo:eventAdapterInfo, mappingType:"xml", outputParameters:outputPropertyParameterString,
//                    textData:textData, dataFrom:dataFrom, customMappingValue:customMappingEnabled},
//                onSuccess:function (response) {
//                    if ("true" == response.responseText.trim()) {
//                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
//                            window.location.href = "../eventreceiver/index.jsp?ordinal=1";
//                        }, null);
//                        customCarbonWindowClose();
//                    } else {
//                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + response.responseText.trim());
//                    }
//                }
//            })
//
//        }
//
//    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'map') {
//
//        var mapData = "";
//        var customMappingEnabled = "disable";
//
//        var mapDataTable = document.getElementById("outputMapPropertiesTable");
//        if (mapDataTable.rows.length > 1 && ((advancedMappingCounter % 2) != 0)) {
//            mapData = getMapDataValues(mapDataTable);
//            customMappingEnabled = "enable";
//        }
//
//        if (mapData == "" && ((advancedMappingCounter % 2) != 0)) {
//            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
//            return;
//        } else {
//            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
//                method:'POST',
//                asynchronous:false,
//                parameters:{eventReceiver:eventReceiverName, streamNameWithVersion:streamNameWithVersion,
//                    eventAdapterInfo:eventAdapterInfo, mappingType:"map", outputParameters:outputPropertyParameterString,
//                    mapData:mapData, customMappingValue:customMappingEnabled},
//                    onSuccess:function (response) {
//                    if ("true" == response.responseText.trim()) {
//                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
//                            window.location.href = "../eventreceiver/index.jsp?ordinal=1";
//                        }, null);
//                        customCarbonWindowClose();
//                    } else {
//                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + response.responseText.trim());
//                    }
//                }
//            })
//        }
//
//    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'json') {
//
//        var jsonData = ""
//        var customMappingEnabled = "disable";
//
//        if (advancedMappingCounter % 2 != 0) {
//            if ((document.getElementById("inline_json")).checked) {
//                jsonData = document.getElementById("jsonSourceText").value;
//                dataFrom = inline;
//            } else if ((document.getElementById("registry_json")).checked) {
//                jsonData = document.getElementById("jsonSourceRegistry").value;
//                parameters = parameters + "&jsonData=" + jsonData;
//                dataFrom = registry;
//            }
//            customMappingEnabled = "enable";
//        }
//
//        if (jsonData == "" && ((advancedMappingCounter % 2) != 0)) {
//            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
//            return;
//        } else {
//            new Ajax.Request('../eventreceiver/add_event_receiver_ajaxprocessor.jsp', {
//                method:'POST',
//                asynchronous:false,
//                parameters:{eventReceiver:eventReceiverName, streamNameWithVersion:streamNameWithVersion,
//                    eventAdapterInfo:eventAdapterInfo, mappingType:"json", outputParameters:outputPropertyParameterString,
//                    dataFrom:dataFrom, jsonData:jsonData, customMappingValue:customMappingEnabled},
//                    onSuccess:function (response) {
//                    if ("true" == response.responseText.trim()) {
//                        CARBON.showInfoDialog("Event receiver added successfully!!", function () {
//                            window.location.href = "../eventreceiver/index.jsp?ordinal=1";
//                        }, null);
//                        customCarbonWindowClose();
//                    } else {
//                        CARBON.showErrorDialog("Failed to add event receiver, Exception: " + response.responseText.trim());
//                    }     advancedMappingCounter
//                }
//            })
//        }
//    }
//}


