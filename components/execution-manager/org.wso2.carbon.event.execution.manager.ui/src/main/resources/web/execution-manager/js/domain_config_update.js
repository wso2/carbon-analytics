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

function saveConfiguration(domainName, templateType, configurationName, description, redirectURL, parameters, executionParameters) {

    if (hasWhiteSpace(configurationName) | configurationName == "") {
        showErrorDialog("Configuration name cannot be empty or consist of white spaces");
    } else {

        $.ajax({
            type: "POST",
            url: "manage_configurations_ajaxprocessor.jsp",
            data: "domainName=" + domainName + "&configurationName=" + configurationName + "&templateType="
                + templateType + "&description=" + description + "&saveType=save" + "&parameters=" + parameters + "&executionParameters=" + executionParameters
        })
            .error(function () {
                showErrorDialog("Error occurred when saving configurations");
            })
            .then(function () {
                showInfoDialog("Configurations saved successfully",
                    function () {
                        document.location.href = redirectURL;
                    });
            });
    }
}

//Redirect to Stream Mapping logic
function mapStreamConfiguration(redirectURL) {
    document.location.href = redirectURL;
}

//Save Stream Mapping Configuration
function saveStreamConfiguration(passToStreamID, passFromStreamID, numberOfRows, redirectURL) {

    console.log(passToStreamID);
    console.log(passFromStreamID);
    console.log(numberOfRows);

    if (passFromStreamID == "Choose from here") {
        showErrorDialog("Select a stream to map");
    } else {

        $.ajax({
            type: "POST",
            url: "manage_stream_configurations_ajaxprocessor.jsp",
            data: "toStreamID=" + passToStreamID + "&fromStreamID=" + passFromStreamID
            /*        success: function (data) {
             if (data != undefined) {
             console.log(data);
             }
             else
             console.log("null");
             }*/
        })
            .error(function () {
                showErrorDialog("Error occurred when saving configurations");
            })
            .then(function () {
                showInfoDialog("Stream mapping saved successfully",
                    function () {
                        console.log("Success");
                        document.location.href = redirectURL;
                    });
            });

    }
}

//Load Mapping Stream Attributes
function loadMappingFromStreamAttributes() {
    var selectedIndex = document.getElementById("fromStreamID").selectedIndex;
    var fromStreamNameWithVersion = document.getElementById("fromStreamID").options[selectedIndex].text;
    var toStreamNameWithVersion = document.getElementById("toStreamID").value;

    var outerDiv = document.getElementById("outerDiv");
    outerDiv.innerHTML = "";

    jQuery.ajax({
        type: "POST",
        url: "../execution-manager/get_mapping_ui_ajaxprocessor.jsp?toStreamNameWithVersion=" + toStreamNameWithVersion + "&fromStreamNameWithVersion=" + fromStreamNameWithVersion,
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

