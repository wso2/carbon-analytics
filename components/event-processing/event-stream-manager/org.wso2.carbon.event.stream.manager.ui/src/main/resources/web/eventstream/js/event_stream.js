//Method that used in jsp files

function addEventStream(form) {


    var eventStreamName = document.getElementById("eventStreamNameId").value.trim();
    var eventStreamVersion = document.getElementById("eventStreamVersionId").value.trim();

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
        var correlationDataTable = document.getElementById("outputCorrelationDataTable");
        if (correlationDataTable.rows.length > 1) {
            correlationData = getWSO2EventDataValues(correlationDataTable);
        }
        var payloadDataTable = document.getElementById("outputPayloadDataTable");
        if (payloadDataTable.rows.length > 1) {
            payloadData = getWSO2EventDataValues(payloadDataTable);
        }

        if (metaData == "" && correlationData == "" && payloadData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventstream/add_event_stream_ajaxprocessor.jsp', {
                method:'post',
                asynchronous:false,
                parameters:{eventStreamName:eventStreamName, eventStreamVersion:eventStreamVersion, metaData:metaData,correlationData:correlationData,payloadData:payloadData,eventStreamDescription:eventStreamDescription,eventStreamNickName:eventStreamNickName},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        form.submit();
                    } else {
                        CARBON.showErrorDialog("Failed to add event stream, Exception: " + event.responseText.trim());
                    }
                }
            })
        }
    }
}

function addEventStreamViaPopup(form) {


    var eventStreamName = document.getElementById("eventStreamNameId").value.trim();
    var eventStreamVersion = document.getElementById("eventStreamVersionId").value.trim();

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
        var correlationDataTable = document.getElementById("outputCorrelationDataTable");
        if (correlationDataTable.rows.length > 1) {
            correlationData = getWSO2EventDataValues(correlationDataTable);
        }
        var payloadDataTable = document.getElementById("outputPayloadDataTable");
        if (payloadDataTable.rows.length > 1) {
            payloadData = getWSO2EventDataValues(payloadDataTable);
        }

        if (metaData == "" && correlationData == "" && payloadData == "") {
            CARBON.showErrorDialog("Mapping parameters cannot be empty.");
            return;
        } else {
            new Ajax.Request('../eventstream/add_event_stream_ajaxprocessor.jsp', {
                method:'post',
                asynchronous:false,
                parameters:{eventStreamName:eventStreamName, eventStreamVersion:eventStreamVersion, metaData:metaData,correlationData:correlationData,payloadData:payloadData,eventStreamDescription:eventStreamDescription,eventStreamNickName:eventStreamNickName},
                onSuccess:function (event) {
                    if ("true" == event.responseText.trim()) {
                        CARBON.showInfoDialog("Stream definition added successfully!!");
                        onSuccessCreateStreamDefinition();
                        customCarbonWindowClose();
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

 function generateEvent(eventStreamId){

     var selectedIndex = document.getElementById("sampleEventTypeFilter").selectedIndex;
     var eventType = document.getElementById("sampleEventTypeFilter").options[selectedIndex].text;

     jQuery.ajax({
                     type:"POST",
                     url:"../eventstream/getSampleEvent_ajaxprocessor.jsp?streamId=" + eventStreamId + "&eventType="+eventType+"",
                     data:{},
                     dataType:"text",
                     async:false,
                     success:function (sampleEvent) {
                         jQuery('#sampleEventText').val(sampleEvent.trim());
                     }
                 });

 }




