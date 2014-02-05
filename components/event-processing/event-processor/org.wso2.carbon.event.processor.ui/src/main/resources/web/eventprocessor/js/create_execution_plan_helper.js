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

function validateQueries() {
    var queryExpressions = window.queryEditor.getValue();

    if (queryExpressions == "") {
        CARBON.showErrorDialog("Query expressions cannot be empty.");
        return;
    }

    var importedStreams = "";
    var importedStreamTable = document.getElementById("streamDefinitionsTable");

    if (importedStreamTable.rows.length > 0) {
        for (var i = 0; i < importedStreamTable.rows.length; i++) {
            var row = importedStreamTable.rows[i];
            var query = row.getAttribute("siddhiStreamDefinition");
            importedStreams = importedStreams + query + ";";
        }
    }
    else {
        CARBON.showErrorDialog("Imported streams cannot be empty.");
        return;
    }

    new Ajax.Request('../eventprocessor/validate_siddhi_queries_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        parameters: {siddhiStreamDefinitions: importedStreams, queries: queryExpressions },
        onSuccess: function (transport) {
            var resultText = transport.responseText.trim();
            if (resultText == "success") {
                CARBON.showInfoDialog("Queries are valid!");
                return;
            } else {
                CARBON.showErrorDialog(resultText);
                return;
            }
        }
    });
}

function createImportedStreamDefinition(element) {
    var selectedVal = element.options[element.selectedIndex].value;
    if (selectedVal == 'createStreamDef') {
        new Ajax.Request('../eventstream/popup_create_event_stream_ajaxprocessor.jsp', {
            method: 'post',
            asynchronous: false,
            onSuccess: function (data) {
                showCustomPopupDialog(data.responseText, "Create Stream Definition", "80%", "", onSuccessCreateStreamDefinition, "90%");
            }
        });
    }
}

function importedStreamDefSelectClick(element) {
    if (element.length <= 1) {
        createImportedStreamDefinition(element);
    }
}

function exportedStreamDefSelectClick(element) {
    if (element.length <= 1) {
        createExportedStreamDefinition(element);
    }
}

function createExportedStreamDefinition(element) {

    var selectedVal = element.options[element.selectedIndex].value;
    if (selectedVal == 'createStreamDef') {

        var queryExpressions = window.queryEditor.getValue();

        if (queryExpressions == "") {
            CARBON.showErrorDialog("Query expressions cannot be empty.");
            return;
        }

        var importedStreams = "";
        var importedStreamTable = document.getElementById("streamDefinitionsTable");

        if (importedStreamTable.rows.length > 0) {
            for (var i = 0; i < importedStreamTable.rows.length; i++) {
                var row = importedStreamTable.rows[i];
                var query = row.getAttribute("siddhiStreamDefinition");
                importedStreams = importedStreams + query + ";";
            }
        }
        else {
            CARBON.showErrorDialog("Imported streams cannot be empty.");
            return;
        }

        var valueOf = document.getElementById("exportedStreamValueOf").value;
        var jsonStreamDefinition = "";

        new Ajax.Request('../eventprocessor/export_siddhi_stream_ajaxprocessor.jsp', {
            method: 'post',
            asynchronous: false,
            parameters: {siddhiStreamDefinitions: importedStreams, queries: queryExpressions, targetStream: valueOf },
            onSuccess: function (data) {
                jsonStreamDefinition = data.responseText;
//                showCustomPopupDialog(data.responseText, "Create Stream Definition", "80%", "", onSuccessCreateStreamDefinition, "90%");
            }
        });

        if (jsonStreamDefinition) {
            jsonStreamDefinition = jsonStreamDefinition.replace(/^\s+|\s+$/g, '');
            if (jsonStreamDefinition == "") {
                CARBON.showErrorDialog("No matching stream definition can be found for: " + valueOf + ". " +
                    "Please fill the 'Value of' field with a valid stream name.");
                return;
            }
        } else {
            CARBON.showErrorDialog("No matching stream definition can be found for: " + valueOf + ". " +
                "Please fill the 'Value of' field with a valid stream name.");
            return;
        }


        new Ajax.Request('../eventstream/popup_create_event_stream_ajaxprocessor.jsp', {
            method: 'post',
            asynchronous: false,
            parameters: {streamDef: jsonStreamDefinition},
            onSuccess: function (data) {
                showCustomPopupDialog(data.responseText, "Create Stream Definition", "80%", "", onSuccessCreateStreamDefinition, "90%");
            }
        });
    }
}


function addImportedStreamDefinition() {
    var propStreamId = document.getElementById("importedStreamId");
    var propAs = document.getElementById("importedStreamAs");

    var error = "";
    if (propStreamId.value == "" || propStreamId.value == "createStreamDef") {
        error = "Invalid Stream ID selected.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }

    if (propAs.value.trim() == "") {
        propAs.value = propStreamId.value.trim().split(':')[0];
        propAs.value = propAs.value.replace(/\./g, '_');
    }

// checking for duplicate stream definitions.
    var streamDefinitionsTable = document.getElementById("streamDefinitionsTable");
    for (var i = 0, row; row = streamDefinitionsTable.rows[i]; i++) {
//        if (row.cells.length > 1) { // leaving out headers.
        var existingStreamId = row.getAttribute("streamId");
        var existingAs = row.getAttribute("as");
        if ((propStreamId.value == existingStreamId) && (propAs.value == existingAs)) {
            CARBON.showErrorDialog("An identical imported stream already exists.");
            return;
        }
//        }
    }

    new Ajax.Request('../eventprocessor/get_stream_definition_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        parameters: {streamId: propStreamId.value, streamAs: propAs.value },
        onSuccess: function (transport) {
            var definitions = transport.responseText.trim().split("|=");
            var streamId = definitions[0].trim();
            var streamAs = definitions[1].trim();

            var streamDefinitionString = "define stream <b>" + streamAs + "</b> (<div class=\"siddhiStreamDefParams\"> " + definitions[2].trim() + ")</div> ";
            var siddhiStreamDefinition = "define stream " + streamAs + " (" + definitions[3].trim() + ")";

            var streamDefinitionsTable = document.getElementById("streamDefinitionsTable");
            var newStreamDefTableRow = streamDefinitionsTable.insertRow(streamDefinitionsTable.rows.length);
            newStreamDefTableRow.setAttribute("id", "def:imported:" + streamId);
            newStreamDefTableRow.setAttribute("streamId", streamId);
            newStreamDefTableRow.setAttribute("as", streamAs);
            newStreamDefTableRow.setAttribute("siddhiStreamDefinition", siddhiStreamDefinition);

            var newStreamDefCell0 = newStreamDefTableRow.insertCell(0);
            newStreamDefCell0.innerHTML = "//  <i> Imported from " + streamId + "</i><br>" + streamDefinitionString;
            YAHOO.util.Dom.addClass(newStreamDefCell0, "property-names");
            YAHOO.util.Dom.setStyle(newStreamDefCell0, "padding-left", "45px !important");

            var newStreamDefCell1 = newStreamDefTableRow.insertCell(1);
            newStreamDefCell1.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeImportedStreamDefinition(this)">Delete</a>';
            YAHOO.util.Dom.addClass(newStreamDefCell1, "property-names");
        }
    });
    propAs.value = "";


}


function addExportedStreamDefinition() {
    var propStreamId = document.getElementById("exportedStreamId");
    var propValueOf = document.getElementById("exportedStreamValueOf");
    var propertyTable = document.getElementById("exportedStreamsTable");
    var noExportedStreamData = document.getElementById("noExportedStreamData");
    var exportedStreamMapping = document.getElementById("exportedStreamMapping");
    var eventFormatterSelect = document.getElementById("eventFormatter");

    var error = "";

    if (propStreamId.value == "" || propStreamId.value == "createStreamDef") {
        error = "Invalid Stream ID selected.\n";
    }

    if (propValueOf.value == "") {
        error = "Value Of field is empty.\n";
    }

    if (error != "") {
        CARBON.showErrorDialog(error);
        return;
    }
    exportedStreamMapping.style.display = "";


// checking for duplicate stream definitions.
    for (var i = 0, row; row = propertyTable.rows[i]; i++) {
        if (i > 0) {  // leaving out the headers.
            var existingValueOf = row.cells[0].innerHTML;
            var existingStreamId = row.cells[1].innerHTML;
            if ((propStreamId.value == existingStreamId) && (propValueOf.value == existingValueOf)) {
                CARBON.showErrorDialog("An identical exported stream already exists.");
                return;
            }
        }
    }

    var newTableRow = propertyTable.insertRow(propertyTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = propValueOf.value;
    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = propStreamId.value;
    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCell2 = newTableRow.insertCell(2);
    if (eventFormatterSelect.value == "createEventFormatter") {
        newCell2.innerHTML = "false";
    } else {
        newCell2.innerHTML = "true";
    }
    YAHOO.util.Dom.addClass(newCell2, "property-names");

    var newCel3 = newTableRow.insertCell(3);
    newCel3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeExportedStream(this)">Delete</a>';

//    propStreamId.value = "";
    propValueOf.value = "";
    noExportedStreamData.style.display = "none";
    eventFormatterSelect.checked = "true";

//    noPropertyDiv.style.display = "none";
//    propType.value = "";
//    showAddProperty();
}


function addExecutionPlan(form) {
    var isFieldEmpty = false;
    var execPlanName = document.getElementById("executionPlanId").value.trim();
    var description = document.getElementById("executionPlanDescId").value.trim();
    var snapshotInterval = document.getElementById("siddhiSnapshotTime").value.trim();
    var distributedProcessing = document.getElementById("distributedProcessing").value.trim();

    // query expressions can be empty for pass thru execution plans...?
    var queryExpressions = window.queryEditor.getValue();
    var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");

    if (!reWhiteSpace.test(execPlanName)) {
        CARBON.showErrorDialog("Invalid character found in execution plan name.");
        return;
    }
    if ((execPlanName == "")) {
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
    } else {

        var importedStreams = "";

        var importedStreamTable = document.getElementById("streamDefinitionsTable");
        if (importedStreamTable.rows.length > 0) {
            importedStreams = getImportedStreamDataValues(importedStreamTable);
        }
        else {
            CARBON.showErrorDialog("Imported streams cannot be empty.");
            return;
        }

        var exportedStreams = "";
        var exportedStreamsTable = document.getElementById("exportedStreamsTable");
        if (exportedStreamsTable.rows.length > 1) {
            exportedStreams = getExportedStreamDataValues(exportedStreamsTable);
            makeAsyncCallToCreateEventFormatter(form,execPlanName,description,snapshotInterval,distributedProcessing,importedStreams,exportedStreams,queryExpressions);
        }else{
            CARBON.showConfirmationDialog("There is no any exported streams defined. Are you sure want to continue.",function(){
                makeAsyncCallToCreateEventFormatter(form,execPlanName,description,snapshotInterval,distributedProcessing,importedStreams,exportedStreams,queryExpressions);
            },null);

        }
    }


}

function makeAsyncCallToCreateEventFormatter(form,execPlanName,description,snapshotInterval,distributedProcessing,importedStreams,exportedStreams,queryExpressions) {
    new Ajax.Request('../eventprocessor/add_execution_plan_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        parameters: {execPlanName: execPlanName, description: description, snapshotInterval: snapshotInterval, distributedProcessing: distributedProcessing,
            importedStreams: importedStreams, exportedStreams: exportedStreams, queryExpressions: queryExpressions },
        onSuccess: function (transport) {
            if ("true" == transport.responseText.trim()) {
                form.submit();
            } else {
                CARBON.showErrorDialog("Failed to add execution plan, Exception: " + transport.responseText.trim());
            }
        }
    });
}


function getExportedStreamDataValues(dataTable) {

    var streamData = "";
    for (var i = 1; i < dataTable.rows.length; i++) {

        var row = dataTable.rows[i];
        var column0 = row.cells[0].innerHTML;
        var column1 = row.cells[1].innerHTML;
        var column2 = row.cells[2].innerHTML;

        streamData = streamData + column0 + "^=" + column1 + "^=" + column2 + "^=" + "$=";
    }
    return streamData;
}

function getImportedStreamDataValues(dataTable) {

    var streamData = "";
    for (var i = 0; i < dataTable.rows.length; i++) {
        var row = dataTable.rows[i];
        var column0 = row.getAttribute("streamId");
        var column1 = row.getAttribute("as");
        streamData = streamData + column0 + "^=" + column1 + "^=" + "$=";
    }
    return streamData;
}


function loadUIElements(uiId) {
    var uiElementTd = document.getElementById("uiElement");
    uiElementTd.innerHTML = "";

    jQuery.ajax({
        type: "POST",
        url: "../eventexecutionplanwizard/get_ui_ajaxprocessor.jsp?uiId=" + uiId + "",
        data: {},
        contentType: "text/html; charset=utf-8",
        dataType: "text",
        async: false,
        success: function (ui_content) {
            if (ui_content != null) {
                jQuery(uiElementTd).html(ui_content);

                if (uiId == 'builder') {
                    var innertTD = document.getElementById('addEventAdaptorTD');
                    jQuery(innertTD).html('<a onclick=\'loadUIElements("inputAdaptor") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Input Event Adaptor </a>')
                }

                else if (uiId == 'formatter') {
                    var innertTD = document.getElementById('addOutputEventAdaptorTD');
                    jQuery(innertTD).html('<a onclick=\'loadUIElements("outputAdaptor") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Out Event Adaptor </a>')
                }

                else if (uiId == 'processor') {
                    var innertTD = document.getElementById('addEventStreamTD');
                    jQuery(innertTD).html('<a onclick=\'loadUIElements("builder") \'style=\'background-image:url(images/add.gif);\' class="icon-link" href="#" \'> Add New Event Stream </a>')
                }

            }
        }
    });


}


