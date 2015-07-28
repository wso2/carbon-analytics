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

var ENABLE = "enable";
var DISABLE = "disable";
var STAT = "statistics";
var TRACE = "Tracing";


function deleteEventReceiver(eventReceiverName) {
    CARBON.showConfirmationDialog(
        "Are you sure want to delete event receiver: " + eventReceiverName + "?", function () {
            var theform = document.getElementById('deleteForm');
            theform.eventReceiver.value = eventReceiverName;
            theform.submit();
        }, null, null);
}

function disableReceiverStat(eventReceiverName) {
    jQuery.ajax({
        type: 'POST',
        url: '../eventreceiver/stat_tracing-ajaxprocessor.jsp',
        data: 'eventReceiverName=' + eventReceiverName + '&action=disableStat',
        async: false,
        success: function (msg) {
            handleCallback(eventReceiverName, DISABLE, STAT);
        },
        error: function (msg) {
            CARBON.showErrorDialog('<fmt:message key="stat.disable.error"/>' +
                ' ' + eventReceiverName);
        }
    });
}

function enableReceiverStat(eventReceiverName) {
    jQuery.ajax({
        type: 'POST',
        url: '../eventreceiver/stat_tracing-ajaxprocessor.jsp',
        data: 'eventReceiverName=' + eventReceiverName + '&action=enableStat',
        async: false,
        success: function (msg) {
            handleCallback(eventReceiverName, ENABLE, STAT);
        },
        error: function (msg) {
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
        type: 'POST',
        url: '../eventreceiver/stat_tracing-ajaxprocessor.jsp',
        data: 'eventReceiverName=' + eventReceiverName + '&action=enableTracing',
        async: false,
        success: function (msg) {
            handleCallback(eventReceiverName, ENABLE, TRACE);
        },
        error: function (msg) {
            CARBON.showErrorDialog('<fmt:message key="trace.enable.error"/>' +
                ' ' + eventReceiverName);
        }
    });
}

function disableReceiverTracing(eventReceiverName) {
    jQuery.ajax({
        type: 'POST',
        url: '../eventreceiver/stat_tracing-ajaxprocessor.jsp',
        data: 'eventReceiverName=' + eventReceiverName + '&action=disableTracing',
        async: false,
        success: function (msg) {
            handleCallback(eventReceiverName, DISABLE, TRACE);
        },
        error: function (msg) {
            CARBON.showErrorDialog('<fmt:message key="trace.disable.error"/>' +
                ' ' + eventReceiverName);
        }
    });
}

function createImportedStreamDefinition() {
    new Ajax.Request('../eventstream/popup_create_event_stream_ajaxprocessor.jsp', {
        method: 'POST',
        asynchronous: false,
        parameters: {callback: "inflow"},
        onSuccess: function (data) {
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
                close: function () {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#custom_dcontainer").empty();
                    return false;
                },
                buttons: {
                    "OK": function () {
                        if (callback && typeof callback == "function") {
                            callback();
                        }
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
                    if (callback && typeof callback == "function") {
                        callback();
                    }
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

}

/* The functions removeInputProperty(link) and removeRegexInputProperty(link) are set to be called
 within a String constant */
/**
 *
 * @param link the row object to be removed
 */
function removeInputProperty(link) {
    var rowToRemove = link.parentNode.parentNode;
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Input Property removed successfully!!");
}

/**
 *
 * @param link the row object to be removed
 */
function removeRegexInputProperty(link) {
    var rowToRemove = link.parentNode.parentNode;
    var propertyToRemove = rowToRemove.cells[0].innerHTML.trim();
    propertyToRemove = propertyToRemove.replace(/(\\)/g, '\\$1');
    propertyToRemove = propertyToRemove.replace(/(:|\.|\[|\])/g, '\\$1');
    rowToRemove.parentNode.removeChild(rowToRemove);
    CARBON.showInfoDialog("Regex Property removed successfully!!");

    var regexSelect = document.getElementById("inputPropertyValue");
    jQuery("#inputPropertyValue option[value='" + propertyToRemove + "']").remove();
    if (regexSelect.length == 0) {
        var newRegexOption = document.createElement("option");
        newRegexOption.value = 'No regular expression defined';
        newRegexOption.text = 'No regular expression defined';
        regexSelect.add(newRegexOption, null);
    }

}



