/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
function deleteConfiguration(domainName, configurationName, row, tableId) {

    showConfirmationDialog("Are you sure want to delete", function () {
        $.ajax({
            type: "POST",
            url: "manage_configurations_ajaxprocessor.jsp",
            data: "domainName=" + domainName + "&configurationName=" + configurationName + "&saveType=delete"
        })
            .error(function () {
                showErrorDialog("Error occurred when deleting configurations");
            })
            .then(function () {
                document.getElementById(tableId).deleteRow(row.parentNode.parentNode.rowIndex);
                showInfoDialog("Configurations deleted successfully");
            });
    });
}

function saveConfiguration(domainName, templateType, configurationName, description, redirectURL, parameters ,isStreamMappingUpdate) {

    if (hasWhiteSpace(configurationName) | configurationName == "") {
        showErrorDialog("Configuration name cannot be empty or consist of white spaces");
    } else {
        var postURL="";
        var streamMappingDivID = document.getElementById("streamMappingDivID");
        streamMappingDivID.innerHTML = "";

        if(isStreamMappingUpdate){
            postURL="manage_update_configurations_ajaxprocessor.jsp";
        } else {
            postURL="manage_configurations_ajaxprocessor.jsp";
        }

        $.ajax({
            type: "POST",
            url: postURL,
            data: "domainName=" + domainName + "&configurationName=" + configurationName + "&templateType="
            + templateType + "&description=" + description + "&saveType=save" + "&parameters=" + parameters
        })
            .error(function () {
                showErrorDialog("Error occurred when saving configurations");
            })
            .then(function (ui_content) {
                if (ui_content == null) {
                    showInfoDialog("Configurations saved successfully",
                        function () {
                            document.location.href = redirectURL;
                        });
                } else {
                    streamMappingDivID.innerHTML = ui_content;
                    $('#parameterMappingDivID').hide();
                    $('#streamMappingDivID').show();
                }
            });
    }
}

//Save Stream Mapping Configuration
function saveStreamConfiguration(streamMappingArrayLength, domainName, configurationName) {

    var streamMappingObjectArray = getStreamMappingObjectArray(streamMappingArrayLength);

    //todo: should we redirect to template-configuration_ajaxprocessor.jsp page on error?
    if (streamMappingObjectArray != undefined) {
        $.ajax({
            type: "POST",
            url: "manage_stream_configurations_ajaxprocessor.jsp",
            data: "streamMappingObjectArray=" + JSON.stringify(streamMappingObjectArray) + "&domainName=" + domainName + "&configurationName=" + configurationName
        })
            .error(function () {
                showErrorDialog("Error occurred when saving stream configurations");
            })
            .then(function () {
                showInfoDialog("Stream mapping configuration saved successfully",
                    function () {
                        document.location.href = "domain_configurations_ajaxprocessor.jsp?domainName=" + domainName;
                    });
            });
    }
}

//Load Mapping Stream Attributes
function loadMappingFromStreamAttributes(index) {
    var outerDiv = document.getElementById("outerDiv_" + index);
    outerDiv.innerHTML = "";
        var selectedIndex = document.getElementById("fromStreamID_" + index).selectedIndex;
        var fromStreamNameWithVersion = document.getElementById("fromStreamID_" + index).options[selectedIndex].text;
        var toStreamNameWithVersion = document.getElementById("toStreamID_" + index).value;

        jQuery.ajax({
            type: "POST",
            url: "../execution-manager/get_mapping_ui_ajaxprocessor.jsp?toStreamNameWithVersion=" + toStreamNameWithVersion + "&fromStreamNameWithVersion=" + fromStreamNameWithVersion + "&index=" + index,
            data: {},
            contentType: "text/html; charset=utf-8",
            dataType: "text",
            success: function (ui_content) {
                if (ui_content != null) {
                    outerDiv.innerHTML = ui_content;
                }
            }
        });
}

//Get Stream Mapping Values
function getStreamMappingValues(dataTable, inputDataType, index , numOfRows) {
    var eventStreamMappingTable = document.getElementById(dataTable);

    var eventStreamAttributeMap = "";
    for (var colIndex = 0; colIndex < numOfRows; colIndex++) {
        var column0 = document.getElementById(inputDataType + "EventMappingValue_" + index + colIndex).value;
        var column1 = document.getElementById(inputDataType + "EventMappedValue_" + index + colIndex).value;
        var column2 = document.getElementById(inputDataType + "EventType_" + index + colIndex).value;

        if (column0.localeCompare("No matching attribute type to map") == 0) {
            showErrorDialog("Invalid stream mapping");
            return error;
        } else {
            eventStreamAttributeMap = eventStreamAttributeMap + column0 + "^=" + column1 + "^=" + column2 + "$=";
        }
    }
    return eventStreamAttributeMap;
}

//Get Stream Mapping Object Array
function getStreamMappingObjectArray(streamMappingArrayLength) {
    var streamMappingObjectArray = [];
    var streamMappingObject = {};

    for (var i = 0; i < streamMappingArrayLength; i++) {
        var toStreamID = document.getElementById("toStreamID_" + i).value;
        var fromStreamIDIndex = document.getElementById("fromStreamID_" + i);
        var fromStreamID = fromStreamIDIndex.options[fromStreamIDIndex.selectedIndex].text;

        if (fromStreamID.localeCompare("Choose from here") == 0) {
            showErrorDialog("Empty input event stream detail fields are not allowed");
            return;
        } else {
            if (fromStreamID.localeCompare(toStreamID) != 0) {
                var metaRows = document.getElementById("metaRows").value;
                var correlationRows = document.getElementById("correlationRows").value;
                var payloadRows = document.getElementById("payloadRows").value;

                var metaData = getStreamMappingValues("addMetaEventDataTable_" + i, 'meta', i, metaRows);
                var correlationData = getStreamMappingValues("addCorrelationEventDataTable_" + i, 'correlation', i, correlationRows);
                var payloadData = getStreamMappingValues("addPayloadEventDataTable_" + i, 'payload', i, payloadRows);
                streamMappingObject = {
                    "toStreamID": toStreamID,
                    "fromStreamID": fromStreamID,
                    "metaData": metaData,
                    "correlationData": correlationData,
                    "payloadData": payloadData
                };
            } else {
                showErrorDialog("Invalid stream mapping");
                return;
            }
        }
        streamMappingObjectArray.push(streamMappingObject);
    }
    return streamMappingObjectArray;
}

function hasWhiteSpace(s) {
    return s.indexOf(' ') >= 0;


}

function showInfoDialog(message) {
    showInfoDialog(message, undefined);
}


function showInfoDialog(message, postFunction) {

    var divTag = '<div class="modal fade" id="info" tabindex="-1" role="dialog" aria-labelledby="myModalLabel"> <div class="modal-dialog"> <div class="modal-content"><div class="modal-header alert-info"><button id="closeButton" type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button><h4 class="modal-title">Information</h4></div><div class="modal-body"><p>'
        + message
        + ' </p></div></div><!-- /.modal-content --></div><!-- /.modal-dialog --></div><!-- /.modal -->';

    $('#dialogBox').append(divTag);
    $('#info').modal({ keyboard: false });
    if (postFunction != undefined) {
        $('#closeButton').click(postFunction);
    }
}

function showErrorDialog(message) {

    var divTag = '<div class="modal fade" id="error" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true"><div class="modal-dialog"><div class="modal-content"><div class="modal-header alert-danger"><button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button><h4 class="modal-title">Error</h4><div class="modal-body"><p>'
        + message
        + ' </p></div></div><div class="modal-footer"><button type="button" class="btn btn-danger" data-dismiss="modal">Close</button></div></div><!-- /.modal-content --></div><!-- /.modal-dialog --></div><!-- /.modal -->';

    $('#dialogBox').append(divTag);
    $('#error').modal({ keyboard: false })
}

function showConfirmationDialog(message, confirmFunction) {

    var divTag = '<div class="modal fade" id="confirmation" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true"><div class="modal-dialog"><div class="modal-content"><div class="modal-header alert-warning"><button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button><h4 class="modal-title">Are you sure?</h4></div><div class="modal-body"><p>' + message + '</p></div><div class="modal-footer"><button type="button" class="btn btn-default" data-dismiss="modal">No</button><button id="confirmButton" type="button" class="btn btn-default" data-dismiss="modal">Yes</button></div></div><!-- /.modal-content --></div><!-- /.modal-dialog --></div><!-- /.modal -->';


    $('#dialogBox').append(divTag);
    $('#confirmation').modal({ keyboard: false });
    $('#confirmButton').click(confirmFunction);
}


function createCookie(name, value, days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = "; expires=" + date.toGMTString();
    }
    else var expires = "";
    document.cookie = name + "=" + value + expires + "; path=/";
}

function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ') c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length, c.length);
    }
    return null;
}

function eraseCookie(name) {
    createCookie(name, "", -1);
}

