define(["jquery"], function (jQuery) {

    "use strict";   // JS strict mode

    var self = {};

    self.HTTP_GET = "GET";
    self.HTTP_POST = "POST";
    self.HTTP_PUT = "PUT";
    self.HTTP_DELETE = "DELETE";
    self.simulatorUrl = "http://localhost:9090/simulation";
    self.editorUrl = "http://localhost:9090/editor";


    self.retrieveExecutionPlanNames = function (successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.editorUrl + "/artifact/listExecutionPlans",
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

    self.retrieveStreamNames = function (executionPlanName, successCallback, errorCallback) {
        if (executionPlanName === null || executionPlanName.length === 0) {
            console.error("Execution plan name is required to retrieve stream names.")
        }
        if (executionPlanName !== null && executionPlanName.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.editorUrl + "/artifact/listStreams/" + executionPlanName,
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
        }
    };

    self.retrieveStreamAttributes = function (executionPlanName, streamName, successCallback, errorCallback) {
        if (executionPlanName === null || executionPlanName.length === 0) {
            console.error("Execution plan name is required to retrieve stream attributes.")
        }
        if (streamName === null || streamName.length === 0) {
            console.error("Stream name is required to retrieve stream attributes.")
        }
        if (executionPlanName !== null && executionPlanName.length > 0
            && streamName !== null && streamName.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.editorUrl + "/artifact/listAttributes/" + executionPlanName + "/" + streamName,
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
        }
    };

    self.singleEvent = function (singleEventConfig, successCallback, errorCallback) {
        if (singleEventConfig === null || singleEventConfig.length === 0) {
            console.error("Single event configuration is required.");
        }
        if (singleEventConfig !== null && singleEventConfig.length > 0) {
            jQuery.ajax({
                async: true,
                url: self.simulatorUrl + "/single",
                type: self.HTTP_POST,
                data: singleEventConfig,
                success: function (data) {
                    if (typeof successCallback === 'function')
                        successCallback(data)
                },
                error: function (msg) {
                    if (typeof errorCallback === 'function')
                        errorCallback(msg)
                }
            });
        }
    };

    return self;
});