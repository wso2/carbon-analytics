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
    propValueOf.value = "";
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

function addEventFormatter(form) {

    var isFieldEmpty = false;
    var inline = "inline";
    var registry = "registry";
    var dataFrom = "";

    var eventFormatterName = document.getElementById("eventFormatterId").value.trim();
    var streamNameWithVersion = document.getElementById("streamNameFilter")[document.getElementById("streamNameFilter").selectedIndex].text;
    var eventAdaptorInfo = document.getElementById("eventAdaptorNameFilter")[document.getElementById("eventAdaptorNameFilter").selectedIndex].value;


    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
    // Check for white space
    if (!reWhiteSpace.test(eventFormatterName)) {
        CARBON.showErrorDialog("Invalid character found in Event Formatter name.");
        return;
    }
    if (isFieldEmpty || (eventFormatterName == "")) {
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
            }
            else {
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

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'wso2event') {

        var metaData = "";
        var correlationData = "";
        var payloadData = "";

        var customMappingValue = "disable";
        var checkedRB = document.querySelectorAll('input[type="radio"][name="customMapping"]:checked');
        if (checkedRB.length != 0) {
            customMappingValue = checkedRB[0].value;
        }

        if (customMappingValue == "enable") {
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
        }

        if (metaData == "" && correlationData == "" && payloadData == "" && customMappingValue == "enable") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method:'post',
                asynchronous:false,
                parameters:{eventFormatter:eventFormatterName, streamNameWithVersion:streamNameWithVersion,
                    eventAdaptorInfo:eventAdaptorInfo, mappingType:"wso2event", outputParameters:outputPropertyParameterString,
                    metaData:metaData, correlationData:correlationData, payloadData:payloadData ,customMappingValue: customMappingValue},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'text') {

        var textData = "";
        if ((document.getElementById("inline_text")).checked) {
            textData = document.getElementById("textSourceText").value;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_text")).checked) {
            textData = document.getElementById("textSourceRegistry").value;
            dataFrom = registry;
        }

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method:'post',
                asynchronous:false,
                parameters:{eventFormatter:eventFormatterName, streamNameWithVersion:streamNameWithVersion,
                    eventAdaptorInfo:eventAdaptorInfo, mappingType:"text", outputParameters:outputPropertyParameterString,
                    textData:textData, dataFrom:dataFrom},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + event.responseText.trim());
                    }
                }
            })

        }

    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'xml') {

        var textData = "";
        if ((document.getElementById("inline_xml")).checked) {
            textData = document.getElementById("xmlSourceText").value;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_xml")).checked) {
            textData = document.getElementById("xmlSourceRegistry").value;
            dataFrom = registry;
        }

        if (textData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method:'post',
                asynchronous:false,
                parameters:{eventFormatter:eventFormatterName, streamNameWithVersion:streamNameWithVersion,
                    eventAdaptorInfo:eventAdaptorInfo, mappingType:"xml", outputParameters:outputPropertyParameterString,
                    textData:textData, dataFrom:dataFrom},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + event.responseText.trim());
                    }
                }
            })

        }

    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'map') {

        var mapData = "";

        var mapDataTable = document.getElementById("outputMapPropertiesTable");
        if (mapDataTable.rows.length > 1) {
            mapData = getMapDataValues(mapDataTable);
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method:'post',
                asynchronous:false,
                parameters:{eventFormatter:eventFormatterName, streamNameWithVersion:streamNameWithVersion,
                    eventAdaptorInfo:eventAdaptorInfo, mappingType:"map", outputParameters:outputPropertyParameterString,
                    mapData:mapData},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + event.responseText.trim());
                    }
                }
            })
        }

        else {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        }
    }

    else if (document.getElementById("mappingTypeFilter")[document.getElementById("mappingTypeFilter").selectedIndex].text == 'json') {

        var jsonData = "";
        if ((document.getElementById("inline_json")).checked) {
            jsonData = document.getElementById("jsonSourceText").value;
            dataFrom = inline;
        }
        else if ((document.getElementById("registry_json")).checked) {
            jsonData = document.getElementById("jsonSourceRegistry").value;
            dataFrom = registry;
        }

        if (jsonData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventformatter/add_event_formatter_ajaxprocessor.jsp', {
                method:'post',
                asynchronous:false,
                parameters:{eventFormatter:eventFormatterName, streamNameWithVersion:streamNameWithVersion,
                    eventAdaptorInfo:eventAdaptorInfo, mappingType:"json", outputParameters:outputPropertyParameterString,
                    dataFrom:dataFrom, jsonData:jsonData},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event formatter, Exception: " + event.responseText.trim());
                    }
                }
            })

        }

    }

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
    propValueOf.value = "";
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

function doDelete(theform,eventFormatterName) {
    theform.eventFormatter.value = eventFormatterName;
    theform.submit();
}

function disableStat(eventFormatterName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventformatter/stat_tracing-ajaxprocessor.jsp',
                    data:'eventFormatterName=' + eventFormatterName + '&action=disableStat',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventFormatterName, DISABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                                               ' ' + eventFormatterName);
                    }
                });
}

function enableStat(eventFormatterName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventformatter/stat_tracing-ajaxprocessor.jsp',
                    data:'eventFormatterName=' + eventFormatterName + '&action=enableStat',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventFormatterName, ENABLE, STAT);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="stat.enable.error"/>' +
                                               ' ' + eventFormatterName);
                    }
                });
}

function handleCallback(eventFormatterName, action, type) {
    var element;
    if (action == "enable") {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventFormatterName);
            element.style.display = "";
            element = document.getElementById("enableStat" + eventFormatterName);
            element.style.display = "none";
        } else {
            element = document.getElementById("disableTracing" + eventFormatterName);
            element.style.display = "";
            element = document.getElementById("enableTracing" + eventFormatterName);
            element.style.display = "none";
        }
    } else {
        if (type == "statistics") {
            element = document.getElementById("disableStat" + eventFormatterName);
            element.style.display = "none";
            element = document.getElementById("enableStat" + eventFormatterName);
            element.style.display = "";
        } else {
            element = document.getElementById("disableTracing" + eventFormatterName);
            element.style.display = "none";
            element = document.getElementById("enableTracing" + eventFormatterName);
            element.style.display = "";
        }
    }
}

function enableTracing(eventFormatterName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventformatter/stat_tracing-ajaxprocessor.jsp',
                    data:'eventFormatterName=' + eventFormatterName + '&action=enableTracing',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventFormatterName, ENABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                                               ' ' + eventFormatterName);
                    }
                });
}

function disableTracing(eventFormatterName) {
    jQuery.ajax({
                    type:'POST',
                    url:'../eventformatter/stat_tracing-ajaxprocessor.jsp',
                    data:'eventFormatterName=' + eventFormatterName + '&action=disableTracing',
                    async:false,
                    success:function (msg) {
                        handleCallback(eventFormatterName, DISABLE, TRACE);
                    },
                    error:function (msg) {
                        CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                                               ' ' + eventFormatterName);
                    }
                });
}


