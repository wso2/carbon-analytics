/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
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

    self.submitToParse = function (data, callback, errorCallback) {
        if (data.siddhiApp === "") {
            return false;
        }
        $.ajax({
            async: false,
            type: "POST",
            url: self.editorUrl + "/validator",
            data: JSON.stringify(data),
            success: callback,
            error: errorCallback
        });
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

    return self;
});