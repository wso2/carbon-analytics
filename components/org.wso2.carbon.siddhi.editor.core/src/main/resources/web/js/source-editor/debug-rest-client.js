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
define(["jquery"], function (jQuery) {

    "use strict";   // JS strict mode

    var self = {};
    self.HTTP_GET = "GET";
    self.HTTP_POST = "POST";
    self.DATA_TYPE_JzSON = "json";
    self.CONTENT_TYPE_JSON = "application/json";
    self.RESPONSE_ELEMENT = "responseJSON";
    self.serverUrl = window.location.protocol + "//" + window.location.host + "/editor";

    self.start = function (siddhiAppName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + siddhiAppName + "/start",
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    self.debug = function (siddhiAppName, callback, error, async) {
        if (null == async) {
            async = true;
        }
        jQuery.ajax({
            async: async,
            url: self.serverUrl + "/" + siddhiAppName + "/debug",
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    self.stop = function (siddhiAppName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + siddhiAppName + "/stop",
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    self.acquireBreakPoint = function (siddhiAppName, queryIndex, queryTerminal, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + siddhiAppName + "/acquire",
            type: self.HTTP_GET,
            data: {queryIndex: queryIndex, queryTerminal: queryTerminal},
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    self.releaseBreakPoint = function (siddhiAppName, queryIndex, queryTerminal, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + siddhiAppName + "/release",
            type: self.HTTP_GET,
            data: {queryIndex: queryIndex, queryTerminal: queryTerminal},
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    self.next = function (siddhiAppName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + siddhiAppName + "/next",
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    self.play = function (siddhiAppName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + siddhiAppName + "/play",
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    self.state = function (siddhiAppName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + siddhiAppName + "/state",
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    self.sendEvent = function (siddhiAppName, streamName, eventData, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + siddhiAppName + "/" + streamName + "/send",
            type: self.HTTP_POST,
            data: JSON.stringify(eventData),
            success: function (data) {
                if (typeof callback === 'function')
                    callback(data)
            },
            error: function (msg) {
                if (typeof error === 'function')
                    error(msg)
            }
        });
    };

    return self;
});
