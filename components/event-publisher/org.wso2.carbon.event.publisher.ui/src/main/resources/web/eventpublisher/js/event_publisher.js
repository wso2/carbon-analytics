/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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

var ENABLE = "enable";
var DISABLE = "disable";
var STAT = "statistics";
var TRACE = "Tracing";
var PROCESS="processing";

function deleteEventPublisher(eventPublisherName) {
    CARBON.showConfirmationDialog(
        "Are you sure want to delete event publisher: " + eventPublisherName + "?", function () {
            var theform = document.getElementById('deleteForm');
            theform.eventPublisher.value = eventPublisherName;
            theform.submit();
        }, null, null);
}

function disablePublisherStat(eventPublisherName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventpublisher/stat_tracing-ajaxprocessor.jsp',
                    data:'eventPublisherName=' + eventPublisherName + '&action=disableStat',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventPublisherName, DISABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                               ' ' + eventPublisherName);
                    }
                });
}

function enablePublisherStat(eventPublisherName) {

    jQuery.ajax({
                    type:'POST',
                    url:'../eventpublisher/stat_tracing-ajaxprocessor.jsp',
                    data:'eventPublisherName=' + eventPublisherName + '&action=enableStat',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventPublisherName, ENABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                               ' ' + eventPublisherName);
                    }
          });
}


function enablePublisherProcessing(eventPublisherName){

    jQuery.ajax({

                 type:'POST',
                 url:'../eventpublisher/stat_tracing-ajaxprocessor.jsp',
                 data:'eventPublisherName=' + eventPublisherName + '&action=enableProcessing',
                 async:false,
                 success:function (msg) {

                        handleCallback(eventPublisherName, ENABLE, PROCESS);
                 }
                });
}


function disablePublisherProcessing(eventPublisherName){

    jQuery.ajax({

                    type:'POST',
                    url:'../eventpublisher/stat_tracing-ajaxprocessor.jsp',
                    data:'eventPublisherName=' + eventPublisherName + '&action=disableProcessing',
                    async:false,
                    success:function (msg) {

                        handleCallback(eventPublisherName, DISABLE, PROCESS);
                    }
                });
}


function handleCallback(eventPublisherName, action, type) {
    var element;
    if (action == "enable") {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventPublisherName);
            element.style.display = "";
            element = document.getElementById("enableStat" + eventPublisherName);
            element.style.display = "none";
        } else if (type=="Tracing"){
            element = document.getElementById("disableTracing" + eventPublisherName);
            element.style.display = "";
            element = document.getElementById("enableTracing" + eventPublisherName);
            element.style.display = "none";
        }else if(type=="processing"){
            element=document.getElementById("disableProcessing"+ eventPublisherName);
            element.style.display="";
            element=document.getElementById("enableProcessing"+eventPublisherName);
            element.style.display="none";
        }
    } else {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventPublisherName);
            element.style.display = "none";
            element = document.getElementById("enableStat" + eventPublisherName);
            element.style.display = "";
        } else if (type=="Tracing"){
            element = document.getElementById("disableTracing" + eventPublisherName);
            element.style.display = "none";
            element = document.getElementById("enableTracing" + eventPublisherName);
            element.style.display = "";
        }else if(type=="processing"){
            element=document.getElementById("disableProcessing"+ eventPublisherName);
            element.style.display="none";
            element=document.getElementById("enableProcessing"+eventPublisherName);
            element.style.display="";
    }
}
}

function enablePublisherTracing(eventPublisherName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventpublisher/stat_tracing-ajaxprocessor.jsp',
                    data:'eventPublisherName=' + eventPublisherName + '&action=enableTracing',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventPublisherName, ENABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                               ' ' + eventPublisherName);
                    }
                });
}

function disablePublisherTracing(eventPublisherName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventpublisher/stat_tracing-ajaxprocessor.jsp',
                    data:'eventPublisherName=' + eventPublisherName + '&action=disableTracing',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventPublisherName, DISABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                               ' ' + eventPublisherName);
                    }
                });
}

function addEventPublisher(form, streamNameWithVersion) {

    var isFieldEmpty = false;
    var inline = "inline";
    var registry = "registry";
    var dataFrom = "";

    var eventPublisherName = document.getElementById("eventPublisherId").value.trim();
    var eventAdapterInfo = document.getElementById("eventAdapterTypeFilter")[document.getElementById("eventAdapterTypeFilter").selectedIndex].value;


    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_\.]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventPublisherName)) {
        CARBON.showErrorDialog("Invalid character found in event publisher name.");
        return;
    }
    if (isFieldEmpty || (eventPublisherName == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }


    var propertyCount = 0;
    var outputPropertyParameterString = "";

    // all properties, not required and required are checked
    while (document.getElementById("property_Required_" + propertyCount) != null ||
           document.getElementById("property_" + propertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("property_Required_" + propertyCount) != null) {
            if (document.getElementById("property_Required_" + propertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                outputPropertyParameterString = "";
                break;
            } else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("property_Required_" + propertyCount).value.trim();
                var propertyName = document.getElementById("property_Required_" + propertyCount).name;
                outputPropertyParameterString = outputPropertyParameterString + propertyName + "$=" + propertyValue + "|=";

            }
        } else if (document.getElementById("property_" + propertyCount) != null) {
            var notRequriedPropertyValue = document.getElementById("property_" + propertyCount).value.trim();
            var notRequiredPropertyName = document.getElementById("property_" + propertyCount).name;
            if (notRequriedPropertyValue == "") {
                notRequriedPropertyValue = "  ";
            }
            outputPropertyParameterString = outputPropertyParameterString + notRequiredPropertyName + "$=" + notRequriedPropertyValue + "|=";


        }
        propertyCount++;
    }

    if (isFieldEmpty) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'wso2event') {


        var metaData = "";
        var correlationData = "";
        var payloadData = "";
        var customMappingEnabled = "disable";

        if (((advancedMappingCounter % 2) != 0)) {

            var toStreamName = document.getElementById("property_Required_stream_name").value;
            var toStreamVersion = document.getElementById("property_Required_stream_version").value;

            if (toStreamName.localeCompare("") == 0 || toStreamVersion.localeCompare("") == 0) {
                CARBON.showErrorDialog("Empty output event stream detail fields are not allowed.");
                return;
            }

            var metaDataTable = document.getElementById("outputMetaDataTable");
            if (metaDataTable.rows.length > 1) {
                metaData = getWSO2EventDataValues(metaDataTable);
            }
            var correlationDataTable = document.getElementById("outputCorrelationDataTable");
            if (correlationDataTable.rows.length > 1) {
                correlationData = getWSO2EventDataValues(correlationDataTable);
            }
            var payloadDataTable = document.getElementById("outputPayloadDataTable");
            if (payloadDataTable.rows.length > 1) {
                payloadData = getWSO2EventDataValues(payloadDataTable);
            }
            customMappingEnabled = "enable";
        }

        if ((metaData == "" && correlationData == "" && payloadData == "") && ((advancedMappingCounter % 2) != 0)) {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventpublisher/add_event_publisher_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{
                    eventPublisher:eventPublisherName,
                    streamNameWithVersion:streamNameWithVersion,
                    eventAdapterInfo:eventAdapterInfo,
                    mappingType:"wso2event",
                    outputParameters:outputPropertyParameterString,
                    metaData:metaData,
                    correlationData:correlationData,
                    payloadData:payloadData,
                    customMappingValue:customMappingEnabled,
                    toStreamName:toStreamName,
                    toStreamVersion:toStreamVersion},
                onSuccess:function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event publisher added successfully!!", function () {
                            window.location.href = "../eventpublisher/index.jsp?ordinal=1";
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event publisher, Exception: " + response.responseText.trim());
                    }
                }
            })
        }

    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'text') {

        var textData = "";
        var customMappingEnabled = "disable";
        var cacheTimeoutDuration = 0;

        if (((advancedMappingCounter % 2) != 0)) {
            if ((document.getElementById("inline_text")).checked) {
                textData = document.getElementById("textSourceText").value;
                dataFrom = inline;
            } else if ((document.getElementById("registry_text")).checked) {
                textData = document.getElementById("textSourceRegistry").value;
                cacheTimeoutDuration = document.getElementById("textCacheTimeout").value;
                dataFrom = registry;
            }
            customMappingEnabled = "enable";
        }

        if (textData == "" && ((advancedMappingCounter % 2) != 0)) {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else if(!isInt(cacheTimeoutDuration)) {
           CARBON.showErrorDialog(org_wso2_carbon_event_publisher_ui_jsi18n["registry.resource.cache.timeout.nan"] + ' ' + document.getElementById("textCacheTimeout").value);
           return;
        } else {
            cacheTimeoutDuration = parseInt(cacheTimeoutDuration, 10);
            if (cacheTimeoutDuration < 0) {
                CARBON.showErrorDialog(org_wso2_carbon_event_publisher_ui_jsi18n["registry.resource.cache.timeout.negative"] + ' ' + document.getElementById("textCacheTimeout").value);
                return;
            }

            new Ajax.Request('../eventpublisher/add_event_publisher_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventPublisher:eventPublisherName, streamNameWithVersion:streamNameWithVersion,
                    eventAdapterInfo:eventAdapterInfo, mappingType:"text", outputParameters:outputPropertyParameterString,
                    textData:textData, dataFrom:dataFrom, cacheTimeoutDuration:cacheTimeoutDuration, customMappingValue:customMappingEnabled},
                onSuccess:function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event publisher added successfully!!", function () {
                            window.location.href = "../eventpublisher/index.jsp?ordinal=1";
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event publisher, Exception: " + response.responseText.trim());
                    }
                }
            })

        }

    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'xml') {

        var textData = "";
        var customMappingEnabled = "disable";
        var cacheTimeoutDuration = 0;

        if (((advancedMappingCounter % 2) != 0)) {
            if ((document.getElementById("inline_xml")).checked) {
                textData = document.getElementById("xmlSourceText").value;
                dataFrom = inline;
            } else if ((document.getElementById("registry_xml")).checked) {
                textData = document.getElementById("xmlSourceRegistry").value;
                cacheTimeoutDuration = parseInt(document.getElementById("textCacheTimeout").value, 10);
                dataFrom = registry;
            }

            customMappingEnabled = "enable";
        }

        if (textData == "" && ((advancedMappingCounter % 2) != 0)) {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else if(!isInt(cacheTimeoutDuration)) {
           CARBON.showErrorDialog(org_wso2_carbon_event_publisher_ui_jsi18n["registry.resource.cache.timeout.nan"] + ' ' + document.getElementById("textCacheTimeout").value);
           return;
        } else {
            cacheTimeoutDuration = parseInt(cacheTimeoutDuration, 10);
            if (cacheTimeoutDuration < 0) {
                CARBON.showErrorDialog(org_wso2_carbon_event_publisher_ui_jsi18n["registry.resource.cache.timeout.negative"] + ' ' + document.getElementById("textCacheTimeout").value);
                return;
            }
            new Ajax.Request('../eventpublisher/add_event_publisher_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventPublisher:eventPublisherName, streamNameWithVersion:streamNameWithVersion,
                    eventAdapterInfo:eventAdapterInfo, mappingType:"xml", outputParameters:outputPropertyParameterString,
                    textData:textData, dataFrom:dataFrom, cacheTimeoutDuration:cacheTimeoutDuration, customMappingValue:customMappingEnabled},
                onSuccess:function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event publisher added successfully!!", function () {
                            window.location.href = "../eventpublisher/index.jsp?ordinal=1";
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event publisher, Exception: " + response.responseText.trim());
                    }
                }
            })

        }

    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'map') {

        var mapData = "";
        var customMappingEnabled = "disable";

        var mapDataTable = document.getElementById("outputMapPropertiesTable");
        if (mapDataTable.rows.length > 1 && ((advancedMappingCounter % 2) != 0)) {
            mapData = getMapDataValues(mapDataTable);
            customMappingEnabled = "enable";
        }

        if (mapData == "" && ((advancedMappingCounter % 2) != 0)) {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventpublisher/add_event_publisher_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventPublisher:eventPublisherName, streamNameWithVersion:streamNameWithVersion,
                    eventAdapterInfo:eventAdapterInfo, mappingType:"map", outputParameters:outputPropertyParameterString,
                    mapData:mapData, customMappingValue:customMappingEnabled},
                    onSuccess:function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event publisher added successfully!!", function () {
                            window.location.href = "../eventpublisher/index.jsp?ordinal=1";
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event publisher, Exception: " + response.responseText.trim());
                    }
                }
            })
        }

    } else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'json') {

        var jsonData = "";
        var customMappingEnabled = "disable";
        var cacheTimeoutDuration = 0;

        if (advancedMappingCounter % 2 != 0) {
            if ((document.getElementById("inline_json")).checked) {
                jsonData = document.getElementById("jsonSourceText").value;
                dataFrom = inline;
            } else if ((document.getElementById("registry_json")).checked) {
                jsonData = document.getElementById("jsonSourceRegistry").value;
                parameters = parameters + "&jsonData=" + jsonData;
                cacheTimeoutDuration = parseInt(document.getElementById("textCacheTimeout").value, 10);
                dataFrom = registry;
            }
            customMappingEnabled = "enable";
        }

        if (jsonData == "" && ((advancedMappingCounter % 2) != 0)) {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else if(!isInt(cacheTimeoutDuration)) {
           CARBON.showErrorDialog(org_wso2_carbon_event_publisher_ui_jsi18n["registry.resource.cache.timeout.nan"] + ' ' + document.getElementById("textCacheTimeout").value);
           return;
        } else {
            cacheTimeoutDuration = parseInt(cacheTimeoutDuration, 10);
            if (cacheTimeoutDuration < 0) {
                CARBON.showErrorDialog(org_wso2_carbon_event_publisher_ui_jsi18n["registry.resource.cache.timeout.negative"] + ' ' + document.getElementById("textCacheTimeout").value);
                return;
            }
            new Ajax.Request('../eventpublisher/add_event_publisher_ajaxprocessor.jsp', {
                method:'POST',
                asynchronous:false,
                parameters:{eventPublisher:eventPublisherName, streamNameWithVersion:streamNameWithVersion,
                    eventAdapterInfo:eventAdapterInfo, mappingType:"json", outputParameters:outputPropertyParameterString,
                    dataFrom:dataFrom, jsonData:jsonData, cacheTimeoutDuration:cacheTimeoutDuration, customMappingValue:customMappingEnabled},
                    onSuccess:function (response) {
                    if ("true" == response.responseText.trim()) {
                        CARBON.showInfoDialog("Event publisher added successfully!!", function () {
                            window.location.href = "../eventpublisher/index.jsp?ordinal=1";
                        }, null);
                        customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event publisher, Exception: " + response.responseText.trim());
                    }
                }
            })
        }
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

function testPublisherConnection() {

    var eventPublisherName = document.getElementById("eventPublisherId").value.trim();
    var eventAdapterInfo = document.getElementById("eventAdapterTypeFilter")[document.getElementById("eventAdapterTypeFilter").selectedIndex].value;
    var propertyCount = 0;
    var outputPropertyParameterString = "";
    var isFieldEmpty = false;
    var messageFormat = [document.getElementById("mappingTypeFilter").selectedIndex].text;

    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_\.]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventPublisherName)) {
        CARBON.showErrorDialog("Invalid character found in event publisher name.");
        return;
    }
    if (isFieldEmpty || (eventPublisherName == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }

    // all properties, not required and required are checked
    while (document.getElementById("property_Required_" + propertyCount) != null ||
        document.getElementById("property_" + propertyCount) != null) {
        // if required fields are empty
        if (document.getElementById("property_Required_" + propertyCount) != null) {
            if (document.getElementById("property_Required_" + propertyCount).value.trim() == "") {
                // values are empty in fields
                isFieldEmpty = true;
                outputPropertyParameterString = "";
                break;
            } else {
                // values are stored in parameter string to send to backend
                var propertyValue = document.getElementById("property_Required_" + propertyCount).value.trim();
                var propertyName = document.getElementById("property_Required_" + propertyCount).name;
                outputPropertyParameterString = outputPropertyParameterString + propertyName + "$=" + propertyValue + "|=";

            }
        } else if (document.getElementById("property_" + propertyCount) != null) {
            var notRequriedPropertyValue = document.getElementById("property_" + propertyCount).value.trim();
            var notRequiredPropertyName = document.getElementById("property_" + propertyCount).name;
            if (notRequriedPropertyValue == "") {
                notRequriedPropertyValue = "  ";
            }
            outputPropertyParameterString = outputPropertyParameterString + notRequiredPropertyName + "$=" + notRequriedPropertyValue + "|=";


        }
        propertyCount++;
    }

    if (isFieldEmpty) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }

    new Ajax.Request('../eventpublisher/test_event_publisher_ajaxprocessor.jsp', {
        method:'POST',
        asynchronous:false,
        parameters:{
            eventPublisher:eventPublisherName,
            eventAdapterInfo:eventAdapterInfo,
            messageFormat:messageFormat,
            outputParameters:outputPropertyParameterString
        },
        onSuccess:function (response) {
            if ("true" == response.responseText.trim()) {
                CARBON.showInfoDialog("Testing publisher connection was successful", function () {
                }, null);
            } else {
                CARBON.showErrorDialog(response.responseText.trim());
            }
        }
    })
}

function removeOutputProperty(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToERemove = rowToRemove.cells[0].innerHTML.trim();
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Mapped Property removed successfully!!");
    return;
}

function isInt(val) {
   var int_val = parseInt(val, 10);
   return !isNaN(int_val) && val == int_val && val.toString() == int_val.toString();
}