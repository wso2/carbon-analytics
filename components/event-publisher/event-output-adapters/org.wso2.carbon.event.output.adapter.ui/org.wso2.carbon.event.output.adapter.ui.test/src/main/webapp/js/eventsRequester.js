/*
 *
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 *
 */

var CONSTANTS = {
    webAppName: 'outputui',
    urlSeperator: '/',
    tenantUrlAttribute: 't',
    urlUnsecureTransportHttp : 'http://',
    urlGetParameter : '?lastUpdatedTime=',
    urlUnsecureTransportWebsocket : 'ws://',
    urlSecureTransportWebsocket : 'wss://',
    urlSecureTransportHttp : 'https://',
    colon : ':',
    defaultMode : 'AUTO',
    processModeHTTP : 'HTTP',
    processModeWebSocket : 'WEBSOCKET',
    processModeAuto : 'AUTO',
    superTenantId : 'carbon.super',
    numThousand : 1000,
    websocketTimeAppender : 400,
    secureMode : 'SECURED'
};

var websocket = null;
var webSocketUrl;
var httpUrl;
var cepHostName;
var cepPortNumber;
var isErrorOccured = false;
var lastUpdatedtime = -1;
var polingInterval;
var stream;
var streamVersion;
var firstPollingAttempt;
var processMode;
var userDomainUrl = "";
var terminateWebsocketInstance = false;
var transportToBeUsedHttp;
var transportToBeUsedWebsocket;

//select default dropDownValue
$("#idMode").val("auto");
$("#idSecureLevel").val("unsecured");

/**
 * Publishing events for data views
 */
function retrieveEvents(streamName,version,cepHost,cepPort,intervalTime,mode,domain,secureMode){

    $("textarea#idRecievedEvents").val("");
    $("textarea#idConsole").val("");

    if(websocket != null){
        terminateWebsocketInstance = true;
        websocket.onclose;
    }

    firstPollingAttempt = true;
    stream = streamName;
    streamVersion = version;
    polingInterval = intervalTime * CONSTANTS.numThousand;
    cepHostName = cepHost;
    cepPortNumber = cepPort;
    processMode = mode;

    if(secureMode == CONSTANTS.secureMode){
        transportToBeUsedHttp = CONSTANTS.urlSecureTransportHttp;
        transportToBeUsedWebsocket = CONSTANTS.urlSecureTransportWebsocket;
    } else {
        transportToBeUsedHttp = CONSTANTS.urlUnsecureTransportHttp;
        transportToBeUsedWebsocket = CONSTANTS.urlUnsecureTransportWebsocket;
    }

    if(domain != CONSTANTS.superTenantId){
        userDomainUrl = CONSTANTS.tenantUrlAttribute + CONSTANTS.urlSeperator + domain + CONSTANTS.urlSeperator;

    }
    webSocketUrl = transportToBeUsedWebsocket + cepHostName + CONSTANTS.colon + cepPortNumber +
        CONSTANTS.urlSeperator + CONSTANTS.webAppName + CONSTANTS.urlSeperator + userDomainUrl + stream +
        CONSTANTS.urlSeperator + streamVersion;

    if(processMode == CONSTANTS.processModeHTTP){
        firstPollingAttempt = true;
        startPoll();
    } else{
        initializeWebSocket(webSocketUrl);
    }
}

/**
 * Polling to retrieve events from http request periodically
 */
function startPoll(){

    (function poll(){
        setTimeout(function(){
            httpUrl = transportToBeUsedHttp + cepHostName + CONSTANTS.colon + cepPortNumber + CONSTANTS.urlSeperator+
                CONSTANTS.webAppName + CONSTANTS.urlSeperator + userDomainUrl + stream + CONSTANTS.urlSeperator +
                streamVersion + CONSTANTS.urlGetParameter + lastUpdatedtime;

            $.getJSON(httpUrl, function(responseText) {
                if(firstPollingAttempt){
                    var data = $("textarea#idConsole").val();
                    $("textarea#idConsole").val(data + "Successfully connected to HTTP.");
                    firstPollingAttempt = false;
                }
                var eventList = $.parseJSON(responseText.events);
                $("textarea#idRecievedEvents").val("");
                if(eventList.length != 0){
                    lastUpdatedtime = responseText.lastEventTime;
                    var streamId = stream + ":" + streamVersion + " =";
                    for(var i=0;i<eventList.length;i++){
                        var data = $("textarea#idRecievedEvents").val();
                        $("textarea#idRecievedEvents").val(data + streamId + " [" +eventList[i]+ "]\n");
                    }
                }
                startPoll();
            })
                .fail(function(errorData) {
                    var data = $("textarea#idConsole").val();
                    var errorData = JSON.parse(errorData.responseText);
                    $("textarea#idConsole").val(data + errorData.error + "\n");
                });
        }, polingInterval);
    })()
}

/**
 * Initializing Web Socket
 */
function initializeWebSocket(webSocketUrl){
    websocket = new WebSocket(webSocketUrl);
    websocket.onopen = webSocketOnOpen;
    websocket.onmessage = webSocketOnMessage;
    websocket.onclose = webSocketOnClose;
    websocket.onerror = webSocketOnError;
}

/**
 * Web socket On Open
 */

var webSocketOnOpen = function () {
    $("textarea#idConsole").val("Successfully connected to URL:" + webSocketUrl + "\n");
};


/**
 * On server sends a message
 */
var webSocketOnMessage = function (evt) {
    var data = $("textarea#idRecievedEvents").val();
    var event = stream + ":" + streamVersion + " =" + evt.data;
    $("textarea#idRecievedEvents").val(data + event + "\n");
};

/**
 * On server close
 */
var webSocketOnClose =function (e) {

    if(isErrorOccured){
        if(processMode != CONSTANTS.processModeWebSocket){
            firstPollingAttempt = true;
            var data = $("textarea#idConsole").val();
            $("textarea#idConsole").val( data + "Starting to poll from http endpoint... \n");
            startPoll();
        }
    } else{
        var data = $("textarea#idConsole").val();
        $("textarea#idConsole").val(data + "Closing the connection... \n");

        if(!terminateWebsocketInstance){
            data = $("textarea#idConsole").val();
            $("textarea#idConsole").val(data + "Starting to retry connection... \n");
            waitForSocketConnection(websocket);
        } else{
            terminateWebsocketInstance = false;
        }


    }
};

/**
 * On server Error
 */
var webSocketOnError = function (err) {
    isErrorOccured = true;
    var data = $("textarea#idConsole").val();
    $("textarea#idConsole").val(data +"Error: Cannot connect to Websocket URL:" + webSocketUrl +
        " .Hence closing the connection! \n");

};

/**
 * Gracefully increments the connection retry
 */
var waitTime = CONSTANTS.numThousand;
function waitForSocketConnection(socket, callback){
    setTimeout(
        function () {
            if (socket.readyState === 1) {
                initializeWebSocket(webSocketUrl);
                var data = $("textarea#idConsole").val();
                $("textarea#idConsole").val(data + "Connection is made... \n");
                if(callback != null){
                    callback();
                }
                return;
            } else {
                websocket = new WebSocket(webSocketUrl);
                waitTime += CONSTANTS.websocketTimeAppender;
                waitForSocketConnection(websocket, callback);
            }
        }, waitTime);
}

function clearTextArea(){
    $("textarea#idRecievedEvents").val("");
}

function connectToSource() {

    var streamName = $("#idStreamName").val();
    var version = $("#idVersion").val();
    var hostName = $("#idHost").val();
    var port = $("#idPort").val();
    var timeToPoll = $("#idPollingInterval").val();
    var mode = $( "#idMode option:selected" ).text();
    var secureLevel = $( "#idSecureLevel option:selected" ).text();
    var tenantDomain = $("#idDomain").val();

    if(hostName == ""){
        hostName = $("#idHost").attr("placeholder");
    }
    if(port == ""){
        port = $("#idPort").attr("placeholder");
    }
    if(timeToPoll == ""){
        timeToPoll = $("#idPollingInterval").attr("placeholder");
    }
    if(tenantDomain == ""){
        tenantDomain = $("#idDomain").attr("placeholder");
    }
    retrieveEvents(streamName,version,hostName,port,timeToPoll,mode,tenantDomain,secureLevel);
}
