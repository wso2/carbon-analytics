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

//Method that used in jsp files
function addEventStream(form, option, eventStreamId) {

    var eventStreamName = document.getElementById("eventStreamNameId").value.trim();
    var eventStreamVersion = document.getElementById("eventStreamVersionId").value.trim();

    var eventStreamDescription = document.getElementById("eventStreamDescription").value.trim();
    var eventStreamNickName = document.getElementById("eventStreamNickName").value.trim();

    if ((eventStreamName == "") || (eventStreamVersion == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    } else {
        var metaData = "";
        var correlationData = "";
        var payloadData = "";
        var indexData = "";
        var newEventStreamId = eventStreamName + ":" + eventStreamVersion;

        var metaDataTable = document.getElementById("outputMetaDataTable");
        if (metaDataTable != null && metaDataTable.rows.length > 1) {
            metaData = getWSO2EventDataValues(metaDataTable);
        }
        var correlationDataTable = document.getElementById("outputCorrelationDataTable");
        if (correlationDataTable != null
            && correlationDataTable.rows.length > 1) {
            correlationData = getWSO2EventDataValues(correlationDataTable);
        }
        var payloadDataTable = document.getElementById("outputPayloadDataTable");
        if (payloadDataTable != null && payloadDataTable.rows.length > 1) {
            payloadData = getWSO2EventDataValues(payloadDataTable);
        }

        var indexDataTable = document.getElementById("analyticsIndexTable");
        if (indexDataTable != null && indexDataTable.rows.length > 1) {
            indexData = getAnalyticsIndexDataValues(indexDataTable);
        }

        if (metaData == "" && correlationData == "" && payloadData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else if (option == "add") {
            new Ajax.Request(
                '../eventstream/add_event_stream_ajaxprocessor.jsp',
                {
                    method: 'POST',
                    asynchronous: false,
                    parameters: {
                        eventStreamName: eventStreamName,
                        eventStreamVersion: eventStreamVersion,
                        metaData: metaData,
                        correlationData: correlationData,
                        payloadData: payloadData,
                        indexData: indexData,
                        eventStreamDescription: eventStreamDescription,
                        eventStreamNickName: eventStreamNickName
                    },
                    onSuccess: function (event) {
                        if ("true" == event.responseText.trim()) {
                            CARBON.showInfoDialog("Stream definition added successfully!!",function () {
                                form.submit();
                            });
                        } else {
                            CARBON.showErrorDialog("Failed to add event stream, Exception: " + event.responseText.trim());
                        }
                    }
                })
        } else if (option == "edit") {

            CARBON.showConfirmationDialog("If event stream is edited then related configuration files will be also affected! Are you sure want to edit?",
            function () {
                new Ajax.Request('../eventstream/edit_event_stream_ajaxprocessor.jsp',{
                        method: 'POST',
                        asynchronous: false,
                        parameters: {
                            oldStreamId: eventStreamId,
                            eventStreamName: eventStreamName,
                            eventStreamVersion: eventStreamVersion,
                            metaData: metaData,
                            correlationData: correlationData,
                            payloadData: payloadData,
                            indexData: indexData,
                            eventStreamDescription: eventStreamDescription,
                            eventStreamNickName: eventStreamNickName
                        },onSuccess: function (event) {
                            if ("true" == event.responseText.trim()) {
                                form.submit();
                            } else {
                                CARBON.showErrorDialog("Failed to edit event stream, Exception: " + event.responseText.trim());
                            }
                        }
                    })
            }, null, null);

        }
    }
}

function ignore() {

}

function addEventStreamViaPopup(form, callback) {

    var eventStreamName = document.getElementById("eventStreamNameId").value.trim();
    var eventStreamVersion = document.getElementById("eventStreamVersionId").value.trim();

    var streamId = eventStreamName + ":" + eventStreamVersion;
    var eventStreamDescription = document.getElementById("eventStreamDescription").value.trim();
    var eventStreamNickName = document.getElementById("eventStreamNickName").value.trim();

    if ((eventStreamName == "") || (eventStreamVersion == "")) {
        // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
    }

    else {
        var metaData = "";
        var correlationData = "";
        var payloadData = "";

        var metaDataTable = document.getElementById("outputMetaDataTable");
        if (metaDataTable.rows.length > 1) {
            metaData = getWSO2EventDataValues(metaDataTable);
        }
        var correlationDataTable = document
            .getElementById("outputCorrelationDataTable");
        if (correlationDataTable.rows.length > 1) {
            correlationData = getWSO2EventDataValues(correlationDataTable);
        }
        var payloadDataTable = document
            .getElementById("outputPayloadDataTable");
        if (payloadDataTable.rows.length > 1) {
            payloadData = getWSO2EventDataValues(payloadDataTable);
        }

        if (metaData == "" && correlationData == "" && payloadData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventstream/add_event_stream_ajaxprocessor.jsp',{
                method: 'POST',
                asynchronous: false,
                parameters: {
                    eventStreamName: eventStreamName,
                    eventStreamVersion: eventStreamVersion,
                    metaData: metaData,
                    correlationData: correlationData,
                    payloadData: payloadData,
                    eventStreamDescription: eventStreamDescription,
                    eventStreamNickName: eventStreamNickName
                },onSuccess: function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Stream definition added successfully!!");
                        //CARBON.showInfoDialog("Stream definition added successfully!!",
                        //    function () {
                        //        if (callback == "inflow") {
                        //            onSuccessCreateInflowStreamDefinition(streamId);
                        //        } else if (callback == "outflow") {
                        //            onSuccessCreateOutflowStreamDefinition(streamId);
                        //        }
                        //    },function () {
                        //        if (callback == "inflow") {
                        //            onSuccessCreateInflowStreamDefinition(streamId);
                        //        } else if (callback == "outflow") {
                        //            onSuccessCreateOutflowStreamDefinition(streamId);
                        //        }
                        //    });customCarbonWindowClose();
                    } else {
                        CARBON.showErrorDialog("Failed to add event stream, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }
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

function addStreamAttribute(dataType) {
    var attributeName = document.getElementById("output" + dataType + "DataPropName");
    var attributeType = document.getElementById("output" + dataType + "DataPropType");
    var streamAttributeTable = document.getElementById("output" + dataType + "DataTable");
    var noStreamAttributesDiv = document.getElementById("noOutput" + dataType + "Data");

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
    streamAttributeTable.style.display = "";

    var newTableRow = streamAttributeTable.insertRow(streamAttributeTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = attributeName.value;

    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = attributeType.value;

    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCel3 = newTableRow.insertCell(2);
    newCel3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeStreamAttribute(this,\'' + dataType + '\')">Delete</a>';

    YAHOO.util.Dom.addClass(newCel3, "property-names");

    attributeName.value = "";
    noStreamAttributesDiv.style.display = "none";

}

function removeStreamAttribute(link, format) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToERemove = rowToRemove.cells[0].innerHTML.trim();
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Stream Attribute removed successfully!!");
    return;
}

function generateEvent(eventStreamId) {

    var selectedIndex = document.getElementById("sampleEventTypeFilter").selectedIndex;
    var eventType = document.getElementById("sampleEventTypeFilter").options[selectedIndex].text;

    jQuery.ajax({
        type: "POST",
        url: "../eventstream/getSampleEvent_ajaxprocessor.jsp?streamId="
            + eventStreamId + "&eventType=" + eventType + "",
        data: {},
        dataType: "text",
        async: false,
        success: function (sampleEvent) {
            if (eventType == "xml") {
                jQuery('#sampleEventText').val(
                    vkbeautify.xml(sampleEvent.trim()));
            } else if (eventType == "json") {
                jQuery('#sampleEventText').val(
                    vkbeautify.json(sampleEvent.trim()));
            } else {
                jQuery('#sampleEventText').val(sampleEvent.trim());
            }
        }
    });

}

CARBON.customConfirmDialogBox = function (message, option1, option2, callback, closeCallback) {
    var strDialog = "<div id='dialog' title='WSO2 Carbon'><div id='messagebox-info' style='height:90px'><p>"
        + message
        + "</p> <br/><input id='dialogRadio1' name='dialogRadio' type='radio' value='default' checked />"
        + option1
        + "<br/> <input id='dialogRadio2' name='dialogRadio' type='radio' value='custom' />"
        + option2 + "</div></div>";
    var func = function () {
        jQuery("#dcontainer").html(strDialog);

        jQuery("#dialog").dialog({ close: function () {
                jQuery(this).dialog('destroy').remove();
                jQuery("#dcontainer").empty();
                if (closeCallback
                    && typeof closeCallback == "function") {
                    closeCallback();
                }
                return false;
            },

            buttons: { "OK": function () {
                    var value = jQuery('input[name=dialogRadio]:checked').val();
                    jQuery(this).dialog("destroy").remove();
                    jQuery("#dcontainer").empty();
                    if (callback && typeof callback == "function") {
                        callback(value);
                    }
                    return false;
                },"Create Later": function () {
                        jQuery(this).dialog('destroy').remove();
                        jQuery("#dcontainer").empty();
                        if (closeCallback && typeof closeCallback == "function") {
                            closeCallback();
                        }
                        return false;
                    }
            },

                height: 200,
                width: 500,
                minHeight: 200,
                minWidth: 330,
                modal: true
        });
    };
    if (!pageLoaded) {
        jQuery(document).ready(func);
    } else {
        func();
    }

};

function convertEventStreamInfoDtoToString() {

    var eventStreamName = document.getElementById("eventStreamNameId").value.trim();
    var eventStreamVersion = document.getElementById("eventStreamVersionId").value.trim();

    var eventStreamDescription = document.getElementById("eventStreamDescription").value.trim();
    var eventStreamNickName = document.getElementById("eventStreamNickName").value.trim();

    if (eventStreamVersion == "") {
        eventStreamVersion = "1.0.0";
    }

    var metaData = "";
    var correlationData = "";
    var payloadData = "";
    var newEventStreamId = eventStreamName + ":" + eventStreamVersion;

    var metaDataTable = document.getElementById("outputMetaDataTable");
    if (metaDataTable != null && metaDataTable.rows.length > 1) {
        metaData = getWSO2EventDataValues(metaDataTable);
    }
    var correlationDataTable = document.getElementById("outputCorrelationDataTable");
    if (correlationDataTable != null && correlationDataTable.rows.length > 1) {
        correlationData = getWSO2EventDataValues(correlationDataTable);
    }
    var payloadDataTable = document.getElementById("outputPayloadDataTable");
    if (payloadDataTable != null && payloadDataTable.rows.length > 1) {
        payloadData = getWSO2EventDataValues(payloadDataTable);
    }

    new Ajax.Request('../eventstream/transform_to_string_ajaxprocessor.jsp',{
        method: 'POST',
        asynchronous: false,
        dataType: "text",
        parameters: {
            eventStreamName: eventStreamName,
            eventStreamVersion: eventStreamVersion,
            metaData: metaData,
            correlationData: correlationData,
            payloadData: payloadData,
            eventStreamDescription: eventStreamDescription,
            eventStreamNickName: eventStreamNickName
        },onSuccess: function (data) {
                var eventStreamDefinitionString = JSON.parse(data.responseText.trim());

                if (eventStreamDefinitionString.success.localeCompare("fail") == 0) {
                    CARBON.showErrorDialog(eventStreamDefinitionString.message);
                }else {
                    document.getElementById("streamDefinitionText").value = eventStreamDefinitionString.message;
                    document.getElementById("designWorkArea").style.display = "none";
                    document.getElementById("sourceWorkArea").style.display = "inline";
                }
            }
        });

}

function convertStringToEventStreamInfoDto() {
    var eventStreamDefinitionString = document
        .getElementById("streamDefinitionText").value.trim();



    new Ajax.Request('../eventstream/transform_to_dto_ajaxprocessor.jsp',{
        method: 'POST',
        asynchronous: false,
        dataType: "text",
        parameters: {
            eventStreamDefinitionString: eventStreamDefinitionString
        },
        onSuccess: function (data) {

            var eventStreamDefinitionDtoJSON = JSON.parse(data.responseText.trim());

            if (eventStreamDefinitionDtoJSON.success.localeCompare("fail") == 0) {
                CARBON.showErrorDialog(eventStreamDefinitionDtoJSON.message);
            }
            else {

                document.getElementById("eventStreamNameId").value = eventStreamDefinitionDtoJSON.message.name;
                document.getElementById("eventStreamVersionId").value = eventStreamDefinitionDtoJSON.message.version;
                document.getElementById("eventStreamDescription").value = eventStreamDefinitionDtoJSON.message.description;
                document.getElementById("eventStreamNickName").value = eventStreamDefinitionDtoJSON.message.nickName;

                //var source =  document.getElementById("sourceWorkArea");
                //var design =  document.getElementById("designWorkArea");



                if (0 == eventStreamDefinitionDtoJSON.message.metaAttributes.length) {
                    var streamAttributeTable = document.getElementById("outputMetaDataTable");

                    while (streamAttributeTable.rows.length > 1) {
                        streamAttributeTable.deleteRow(1);
                    }

                    document.getElementById("noOutputMetaData").style.display = "";
                    streamAttributeTable.style.display = "none";

                } else {
                    var streamAttributeTable = document.getElementById("outputMetaDataTable");
                    while (streamAttributeTable.rows.length > 1) {
                        streamAttributeTable.deleteRow(1);
                    }
                    for (i = 0; i < eventStreamDefinitionDtoJSON.message.metaAttributes.length; i++) {
                        addStreamAttribute2("Meta", eventStreamDefinitionDtoJSON.message.metaAttributes[i].attributeName, eventStreamDefinitionDtoJSON.message.metaAttributes[i].attributeType);
                    }
                    document.getElementById("noOutputMetaData").style.display = "none";
                    streamAttributeTable.style.display = "";
                }
                if (0 == eventStreamDefinitionDtoJSON.message.correlationAttributes.length) {

                    var streamAttributeTable = document.getElementById("outputCorrelationDataTable");
                    while (streamAttributeTable.rows.length > 1) {
                        streamAttributeTable.deleteRow(1);
                    }
                    document.getElementById("noOutputCorrelationData").style.display = "";
                    streamAttributeTable.style.display = "none";
                } else {
                    var streamAttributeTable = document.getElementById("outputCorrelationDataTable");
                    while (streamAttributeTable.rows.length > 1) {
                        streamAttributeTable.deleteRow(1);
                    }
                    for (i = 0; i < eventStreamDefinitionDtoJSON.message.correlationAttributes.length; i++) {
                        addStreamAttribute2("Correlation", eventStreamDefinitionDtoJSON.message.correlationAttributes[i].attributeName, eventStreamDefinitionDtoJSON.message.correlationAttributes[i].attributeType);
                    }
                    document.getElementById("noOutputCorrelationData").style.display = "none";
                    streamAttributeTable.style.display = "";
                }
                if (0 == eventStreamDefinitionDtoJSON.message.payloadAttributes.length) {
                    var streamAttributeTable = document.getElementById("outputPayloadDataTable");
                    while (streamAttributeTable.rows.length > 1) {
                        streamAttributeTable.deleteRow(1);
                    }
                    document.getElementById("noOutputPayloadData").style.display = "";
                    streamAttributeTable.style.display = "none";
                } else {
                    var streamAttributeTable = document.getElementById("outputPayloadDataTable");
                    while (streamAttributeTable.rows.length > 1) {
                        streamAttributeTable.deleteRow(1);
                    }
                    for (i = 0; i < eventStreamDefinitionDtoJSON.message.payloadAttributes.length; i++) {
                        addStreamAttribute2("Payload", eventStreamDefinitionDtoJSON.message.payloadAttributes[i].attributeName, eventStreamDefinitionDtoJSON.message.payloadAttributes[i].attributeType);
                    }
                    document.getElementById("noOutputPayloadData").style.display = "none";
                    streamAttributeTable.style.display = "";
                }

                document.getElementById("sourceWorkArea").style.display = "none";
                document.getElementById("designWorkArea").style.display = "inline";


            }


        }
    });
}

function addStreamAttribute2(dataType, name, type) {

    var streamAttributeTable = document.getElementById("output" + dataType + "DataTable");
    var newTableRow = streamAttributeTable.insertRow(streamAttributeTable.rows.length);
    var newCell0 = newTableRow.insertCell(0);
    newCell0.innerHTML = name;

    YAHOO.util.Dom.addClass(newCell0, "property-names");

    var newCell1 = newTableRow.insertCell(1);
    newCell1.innerHTML = type;

    YAHOO.util.Dom.addClass(newCell1, "property-names");

    var newCel3 = newTableRow.insertCell(2);
    newCel3.innerHTML = ' <a class="icon-link" style="background-image:url(../admin/images/delete.gif)" onclick="removeStreamAttribute(this,\'' + dataType + '\')">Delete</a>';

    YAHOO.util.Dom.addClass(newCel3, "property-names");

}

function addEventStreamByString(form) {
    var eventStreamDefinitionString = document.getElementById("streamDefinitionText").value.trim();


    new Ajax.Request('../eventstream/add_event_stream_by_string_ajaxprocessor.jsp',{
        method: 'POST',
        asynchronous: false,
        parameters: {
            eventStreamDefinitionString: eventStreamDefinitionString
        },onSuccess: function (event) {

            var addStreamResposeJSON = JSON.parse(event.responseText.trim());
            if (addStreamResposeJSON.success.localeCompare("fail") == 0) {
                CARBON.showErrorDialog("Failed to add event stream, Exception: " + addStreamResposeJSON.message);
            } else {
                CARBON.showInfoDialog("Stream definition added successfully!!",function () {
                    form.submit();
                });
            }


        }
    })
}

function editEventStreamByString(form, eventStreamId) {

    CARBON.showConfirmationDialog("If event stream is edited then related configuration files will be also affected! Are you sure want to edit?",

        function () {

            var eventStreamDefinitionString = document.getElementById("streamDefinitionText").value.trim();
            new Ajax.Request('../eventstream/edit_event_stream_by_string_ajaxprocessor.jsp',{
                method: 'POST',
                asynchronous: false,
                parameters: {
                    eventStreamDefinitionString: eventStreamDefinitionString,
                    oldEventStreamId: eventStreamId
                },onSuccess: function (event) {

                    var addStreamResposeJSON = JSON.parse(event.responseText.trim());
                    if (addStreamResposeJSON.success.localeCompare("fail") == 0) {
                        CARBON.showErrorDialog("Failed to edit event stream, Exception: " + addStreamResposeJSON.message);
                    } else {
                        form.submit();
                    }


                }
            })
        }, null, null);

}