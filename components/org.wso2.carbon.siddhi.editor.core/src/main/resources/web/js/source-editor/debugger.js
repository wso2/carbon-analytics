/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * This Javascript module exposes Siddhi debug API as Javascript methods.
 */

function DebugClient() {
    var HTTP_GET = "GET";
    var HTTP_POST = "POST";
    var DATA_TYPE_JzSON = "json";
    var CONTENT_TYPE_JSON = "application/json";
    var RESPONSE_ELEMENT = "responseJSON";
    this.serverUrl = "";

    this.startDebug = function (executionPlan, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/debug",
            type: HTTP_POST,
            data: executionPlan,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.stopDebug = function (runtimeId, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/stop",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.acquireBreakPoint = function (runtimeId, queryName, queryTerminal, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/acquire",
            type: HTTP_GET,
            data: { queryName: queryName, queryTerminal: queryTerminal },
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.releaseBreakPoint = function (runtimeId, queryName, queryTerminal, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/release",
            type: HTTP_GET,
            data: { queryName: queryName, queryTerminal: queryTerminal },
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.pollState = function (runtimeId, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/poll",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.next = function (runtimeId, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/next",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.play = function (runtimeId, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/play",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.getQueryState = function (runtimeId, queryName, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/" + queryName + "/state",
            type: HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    this.sendEvent = function (runtimeId, streamName, eventData, callback, error) {
        jQuery.ajax({
            async: true,
            url: this.serverUrl + "/" + runtimeId + "/" + streamName + "/send",
            type: HTTP_POST,
            data: JSON.stringify(eventData),
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                console.error(JSON.stringify(msg));
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };
}

/**
 * Construct an DebugClient object by given the serverUrl.
 * @param svrUrl the server url
 * @returns {DebugClient} DebugClient object
 */
DebugClient.prototype.init = function (svrUrl) {
    if (svrUrl) {
        this.serverUrl = "http://localhost:9090/editor";
    } else {
        this.serverUrl = svrUrl;
    }
    return this;
};