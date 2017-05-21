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
    self.serverUrl = "http://localhost:9090/editor";

    self.start = function (executionPlanName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/start",
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

    self.debug = function (executionPlanName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/debug",
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

    self.stop = function (executionPlanName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/stop",
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

    self.acquireBreakPoint = function (executionPlanName, queryIndex, queryTerminal, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/acquire",
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

    self.releaseBreakPoint = function (executionPlanName, queryIndex, queryTerminal, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/release",
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

    self.next = function (executionPlanName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/next",
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

    self.play = function (executionPlanName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/play",
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

    self.state = function (executionPlanName, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/state",
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

    self.sendEvent = function (executionPlanName, streamName, eventData, callback, error) {
        jQuery.ajax({
            async: true,
            url: self.serverUrl + "/" + executionPlanName + "/" + streamName + "/send",
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
