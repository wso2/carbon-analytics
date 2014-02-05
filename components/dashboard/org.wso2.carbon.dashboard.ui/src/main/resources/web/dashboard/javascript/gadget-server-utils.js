/*
 * Copyright 2005-2009 WSO2, Inc. http://wso2.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function getGadgetsJSON(urls, callback) {
    if (!urls || !urls.length)
        return;

    var jsonObj = {};
    jsonObj["context"] = {};
    jsonObj["context"]["language"] = "en";
    jsonObj["context"]["country"] = "US";
    jsonObj["context"]["ignoreCache"] = "true";
    jsonObj["context"]["hostDomain"] = document.domain;
    jsonObj["context"]["cookies"] = "JSESSIONID=" + readCookie("JSESSIONID");
    jsonObj["gadgets"] = new Array();

    /*    for (var i = 0; i < urls.length; i++) {
     if (i == 0) {
     json += '{"moduleId" : 0, "prefs":{}, "url": "' + urls[i] + '"}';
     } else {
     json += ',{"moduleId" : 0, "prefs":{}, "url": "' + urls[i] + '"}';
     }
     }
     json += ']}';    */
    var ix;
    for (ix=0; ix<urls.length; ix++) {
        var gadget = {};
        gadget["moduleId"] = 0;
        gadget["prefs"] = {};
        gadget["url"] = urls[ix];

        jsonObj["gadgets"].push(gadget);
    }
    var json = JSON.stringify(jsonObj);
    var metaDataService = '../../gadgets/metadata';

    var xmlHttpReq = createXmlHttpRequest();

    //Make sure the XMLHttpRequest object was instantiated
    if (xmlHttpReq) {
        // making an async call
        xmlHttpReq.open("POST", metaDataService, true);

        //Send the proper header information along with the request
        xmlHttpReq.setRequestHeader("Content-type", "multipart/form-data");
        xmlHttpReq.setRequestHeader("Content-length", json.length);
        xmlHttpReq.setRequestHeader("Connection", "close");

        xmlHttpReq.send(json);
        xmlHttpReq.onreadystatechange = function () {
            if (xmlHttpReq.readyState == 4) {
                if (xmlHttpReq.status == 200) {
                    var response = "";
                    try {
                        response = JSON.parse(xmlHttpReq.responseText);
                    } catch(ex) {
                    }
                    gadgetserver.renderGadgetsCallBack(response);
                }
            }
        };


    }
}



function createXmlHttpRequest() {
    var request;

    // Lets try using ActiveX to instantiate the XMLHttpRequest object
    try {
        request = new ActiveXObject("Microsoft.XMLHTTP");
    } catch(ex1) {
        try {
            request = new ActiveXObject("Msxml2.XMLHTTP");
        } catch(ex2) {
            request = null;
        }
    }

    // If the previous didn't work, lets check if the browser natively support XMLHttpRequest
    if (!request && typeof XMLHttpRequest != "undefined") {
        //The browser does, so lets instantiate the object
        request = new XMLHttpRequest();
    }

    return request;
}

function generateUniqueId() {
    var d = new Date();
    return d.getTime();
}

// Execute showAddGadgetsPage method in session aware manner
function sessionAwareShowAddGadgetsPage(gadgetGrp){
    // if signed user
    if (userId != "null") {
        // Run the task (showAddGadgetsPage) if the session is valid (Success case)
        sessionAwareFunction(function() { showAddGadgetsPage(gadgetGrp); },
            'Session timed out. Please login again',
            sessionFailureFunction );
    }
    // if unsigned user
    else {
        showAddGadgetsPage(gadgetGrp);
    }

}

function showAddGadgetsPage(gadgetGrp) {
    var grp = '';
    if (gadgetserver.gadgetLayout != null && gadgetserver.gadgetLayout != 'NA') {
        if (gadgetserver.gadgetLayout.length % 3 == 0) {
            grp = 'G1#';
        } else if (gadgetserver.gadgetLayout.length % 3 == 1) {
            grp = 'G2#';
        } else if (gadgetserver.gadgetLayout.length % 3 == 2) {
            grp = 'G3#';
        }
    } else {
        // This is an empty tab. Add gadgets to group 1
        grp = 'G1#';
    }
    window.location = "add-gadgets.jsp?tab=" + currentActiveTab + "&name=" + dashboardName + "&grp=" + grp;
}

/**
 * Goes through a given array of URLs and prefixes the relative URLs with the server's HTTP root
 * @param urls
 */
function sanitizeUrls(urls) {
    var sanitizedUrls = new Array();

    for (var x = 0; x < urls.length; x++) {
        var currentUrl = urls[x];

        // remove local transport related string from first url
        // This method generates wrong urls for local transport without this
        if(x == 0 && currentUrl != null && currentUrl.length > 4 && currentUrl.substring(0,5)== "local") {
            var cleanUrl = currentUrl.substring(18, currentUrl.length);
            currentUrl = cleanUrl;
        }
        if (currentUrl.charAt(0) == "/") {
            currentUrl = httpServerRoot + currentUrl;
        }
        sanitizedUrls.push(currentUrl);
    }

    return sanitizedUrls;
}

function removeCarriageReturns(string) {
    return string.replace(/\n/g, "");
}

/**
 * This is a silly implementation that will need to be overriden by almost all
 * real containers.
 * TODO: Find a better default for this function
 *
 * @param view The view name to get the url for
 */
function getUrlForView(view) {
    if (view === 'canvas') {
        return '/canvas';
    } else if (view === 'profile') {
        return '/profile';
    } else {
        return null;
    }
}
function disableButton(){
    var inputVal = document.getElementById("carbon-ui-dialog-input").value;
    if (inputVal == "") {
        jQuery("#button-ok").hide();

    } else {
        jQuery("#button-ok").show();
    }
}
function onKeyPressed(ev) {
    var enterKey = 13;
    var e = ev || event;
    if (e.keyCode ==enterKey) {
        jQuery("#button-ok").click();
    }
}

function getGadgetIdFromModuleId(moduleId) {
    // Quick hack to extract the gadget id from module id
    return parseInt(moduleId.match(/_([0-9]+)$/)[1], 10);
}
function showInputWizard(message, handleNext, handleCancel, closeCallback,txtMsg,wizardTitle) {
    var gheight = 250;
    if(getInternetExplorerVersion() != -1){//IE
        gheight = 420;
    }
    var strInput = "<div style='margin:20px;'><p>" + message + "</p><br/>" +
        "<input type='text' id='carbon-ui-dialog-input' size='40' onchange='disableButton()' onkeyup='onKeyPressed(event)' value='"+txtMsg+"' name='carbon-dialog-inputval'>" +
        "<br>" +
        "<table class='tablayout'><tr><td><input type='radio' name='layout' value='1' /></td>" +
        "<td><input type='radio' name='layout' value='2' /></td>" +
        "<td><input type='radio' name='layout' value='3' checked/></td>" +
        "<td><input type='radio' name='layout' value='4' /></td>" +
        "<td><input type='radio' name='layout' value='5' /></td>" +
        "<td><input type='radio' name='layout' value='6' /></td></tr>" +
        "<tr>" +
        "<td><img name='img1'  title='One vertical columns tab' src='images/1.png' style='margin-right:20px;'/></td>" +
        "<td><img name='img2' title='Two vertical columns tab' src='images/2.png' style='margin-right:20px;'/></td>" +
        "<td><img name='img3' title='Three vertical columns tab' src='images/3.png' style='margin-right:20px;'/></td>" +
        "<td><img name='img4' title='One column Two rows (33/67) tab' src='images/4.png' style='margin-right:20px;'/></td>" +
        "<td><img name='img5' title='Two vertical columns (33/67) tab' src='images/5.png' style='margin-right:20px;'/></td>" +
        "<td><img name='img6' title='Two vertical columns (67/33) tab' src='images/6.png' style='margin-right:20px;'/></td>" +
        "</table>" +
        "</div>";
    var strDialog = "<div id='dialog' title='"+wizardTitle+"'>" + strInput + "</div>";
    jQuery.noConflict();
    jQuery("#dcontainer").html(strDialog);
    jQuery("#dialog").dialog({
        close:function() {
            jQuery(this).dialog('destroy').remove();
            jQuery("#dcontainer").empty();
            if (closeCallback && typeof closeCallback == "function") {
                closeCallback();
            }
            return false;
        },
        buttons:[{
            id: "button-ok",
            text: "OK",
            click: function() {
                var inputVal = document.getElementById("carbon-ui-dialog-input").value;
                var layout = jQuery('input[type=radio]:checked').val();
                handleNext(inputVal, layout);
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                return false;
            }
        },
            {
                id: "button-cancel",
                text: "Cancel",
                click: function() {
                    jQuery(this).dialog("destroy").remove();
                    jQuery("#dcontainer").empty();
                    handleCancel();
                }
            }
        ],

        height:gheight,
        width:500,
        minHeight:gheight,
        minWidth:500,
        modal:true
    });
}
function getInternetExplorerVersion()
    // Returns the version of Internet Explorer or a -1
    // (indicating the use of another browser).
{
    var rv = -1; // Return value assumes failure.
    if (navigator.appName == 'Microsoft Internet Explorer')
    {
        var ua = navigator.userAgent;
        var re = new RegExp("MSIE ([0-9]{1,}[\.0-9]{0,})");
        if (re.exec(ua) != null)
            rv = parseFloat(RegExp.$1);
    }
    return rv;
}
function showInputDialog(message, handleOk, handleCancel,closeCallback,txtMsg,wizardTitle) {
    var gheight = 160;
    if(getInternetExplorerVersion() != -1){//IE
        gheight = 270;
    }
    var strInput = "<div style='margin:20px;'><p>" + message + "</p><br/>" +
        "<input type='text' id='carbon-ui-dialog-input' onchange='disableButton()' size='40' onkeyup='onKeyPressed(event)'  name='carbon-dialog-inputval' value='"+txtMsg+"' </div>";
    var strDialog = "<div id='dialog' title='"+wizardTitle+"'>" + strInput + "</div>";

    jQuery("#dcontainer").html(strDialog);
    jQuery("#dialog").dialog({
        close:function() {
            jQuery(this).dialog('destroy').remove();
            jQuery("#dcontainer").empty();
            if (closeCallback && typeof closeCallback == "function") {
                closeCallback();
            }
            return false;
        },
        buttons:[{
            id: "button-ok",
            text: "OK",
            click: function() {
                var inputVal = document.getElementById("carbon-ui-dialog-input").value;
                handleOk(inputVal);
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                return false;
            }
        },
            {
                id: "button-cancel",
                text: "Cancel",
                click: function() {
                    jQuery(this).dialog("destroy").remove();
                    jQuery("#dcontainer").empty();
                    handleCancel();
                }
            }
        ],

        height:gheight,
        width:450,
        minHeight:160,
        minWidth:330,
        modal:true
    });
}

function confirmDialog(message, handleYes, gId,wizardTitle) {
    /* This function always assume that your second parameter is handleYes function and third parameter is handleNo function.
     * If you are not going to provide handleYes function and want to give handleNo callback please pass null as the second
     * parameter.
     */
    var strDialog = "<div id='dialog' title='"+wizardTitle+"'><div id='messagebox-confirm'><p>" +
        message + "</p></div></div>";

    handleYes = handleYes || function() {
        return true
    };

    jQuery("#dcontainer").html(strDialog);

    jQuery("#dialog").dialog({
        close:function() {
            jQuery(this).dialog('destroy').remove();
            jQuery("#dcontainer").empty();
            return false;
        },
        buttons:{
            "Yes":function() {
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                handleYes(gId);
            },
            "No":function() {
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                return false;
            }
        },
        height:160,
        width:450,
        minHeight:160,
        minWidth:330,
        modal:true
    });
    return false;

}

function removeHTMLTags(inputItem) {
    inputItem = inputItem.replace(/&(lt|gt);/g, function (strMatch, p1) {
        return (p1 == "lt") ? "<" : ">";
    });
    var inputItemText = inputItem.replace(/<\/?[^>]+(>|$)/g, "");
    return inputItemText;
}

function fixBreadCrumb(dashboardName) {
    var breadCrumbDiv = document.getElementById("breadcrumb-div");
    var breadCrumbLinks = breadCrumbDiv.getElementsByTagName("a");

    for (var x = 0; x < breadCrumbLinks.length; x++) {
        var linkContent = breadCrumbLinks[x].href;
        if (linkContent.search(/dashboard/i) > 0) {
            breadCrumbLinks[x].href = linkContent + "&name=" + dashboardName;
        }
    }
}

function getServerBaseUrl() {
    var currentUrl = document.location.href;

    // We need to break the URL upto the last /carbon
    return currentUrl.substring(0, currentUrl.lastIndexOf("carbon"));
}

function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ')
            c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0)
            return c.substring(nameEQ.length, c.length);
    }
    return "NA";
}


