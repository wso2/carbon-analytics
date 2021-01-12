/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

define(["jquery", "utils"], function (jQuery, Utils) {

    "use strict";   // JS strict mode

    var self = {};


    self.HTTP_GET = "GET";
    self.HTTP_POST = "POST";
    self.HTTP_PUT = "PUT";
    self.HTTP_DELETE = "DELETE";
    self.simulatorUrl = window.location.protocol + "//" + window.location.host + "/simulation";
    self.editorUrl = window.location.protocol + "//" + window.location.host + '/editor';

    self.retrieveSiddhiAppNames = function (successCallback, errorCallback) {
        var retrieveEnvVariables = Utils.prototype.retrieveEnvVariables();
        jQuery.ajax({
            async: true,
            url: self.editorUrl + "/artifact/listSiddhiApps?envVar=" +
                                    Utils.prototype.base64EncodeUnicode(JSON.stringify(retrieveEnvVariables)),
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof successCallback === 'function')
                    successCallback(data)
            },
            error: function (msg) {
                if (typeof errorCallback === 'function')
                    errorCallback(msg)
            }
        });
    };

    self.submitToParse = function (data) {
        if (data.siddhiApp === "") {
            return false;
        }
        var result = {};
        $.ajax({
            async: false,
            type: "POST",
            url: self.editorUrl + "/validator",
            data: JSON.stringify(data),
            success: function (response) {
                result = {status: "success", response: response};
            },
            error: function (error) {
                if (error.responseText) {
                    result = {status: "fail", errorMessage: error.responseText};
                } else {
                    result = {status: "fail", errorMessage: "Error Occurred while processing your request"};
                }
            }
        });
        return result;
    }

    self.getSourcesAndSinks = function (siddhiAppName, callback, errorCallback) {
        $.ajax({
            async: false,
            type: "GET",
            url: self.editorUrl + "/" +  siddhiAppName + "/sources-sinks",
            success: callback,
            error: errorCallback
        });
    }

    self.getCodeView = function (designViewJSON) {
        var self = this;
        var result = {};
        self.designToCodeURL = window.location.protocol + "//" + window.location.host + "/editor/code-view";
        $.ajax({
            type: "POST",
            url: self.designToCodeURL,
            data: window.btoa(designViewJSON),
            async: false,
            success: function (response) {
                result = {status: "success", responseString: response};
            },
            error: function (error) {
                if (error.responseText) {
                    result = {status: "fail", errorMessage: error.responseText};
                } else {
                    result = {status: "fail", errorMessage: "Error Occurred while processing your request"};
                }
            }
        });
        return result;
    }

    self.getSiddhiElements = function (code) {
        var self = this;
        var result = {};
        // Remove Single Line Comments
        var regexStr = code.replace(/--.*/g, '');
        // Remove Multi-line Comments
        regexStr = regexStr.replace(/\/\*(.|\s)*?\*\//g, '');
        var regex = /^\s*@\s*app\s*:\s*name\s*\(\s*["|'](.*)["|']\s*\)\s*@\s*app\s*:\s*description\s*\(\s*["|'](.*)["|']\s*\)\s*$/gi;
        var match = regex.exec(regexStr);

        // check whether Siddhi app is in initial mode(initial Siddhi app template) and if yes then go to the
        // design view with the no content
        if (match !== null) {
            var defaultResponse = {
                "siddhiAppConfig": {
                    "siddhiAppName": match[1],
                    "siddhiAppDescription": match[2],
                    "appAnnotationList": [],
                    "streamList": [],
                    "tableList": [],
                    "windowList": [],
                    "triggerList": [],
                    "aggregationList": [],
                    "functionList": [],
                    "partitionList": [],
                    "sourceList": [],
                    "sinkList": [],
                    "queryLists": {
                        "WINDOW_FILTER_PROJECTION": [],
                        "PATTERN": [],
                        "SEQUENCE": [],
                        "JOIN": []
                    },
                    "finalElementCount": 0
                },
                "edgeList": []
            };
            var defaultString = JSON.stringify(defaultResponse);
            result = {
                status: "success",
                responseString: defaultString
            };
        } else {
            self.codeToDesignURL = window.location.protocol + "//" + window.location.host + "/editor/design-view";
            $.ajax({
                type: "POST",
                url: self.codeToDesignURL,
                data: btoa(unescape(encodeURIComponent(code))),
                async: false,
                success: function (response) {
                    result = {status: "success", response: response};
                },
                error: function (error) {
                    if (error.responseText) {
                        result = {status: "fail", errorMessage: error.responseText};
                    } else {
                        result = {status: "fail", errorMessage: "Error Occurred while processing your request"};
                    }
                }
            });
        }
        return result;
    }

    return self;
});