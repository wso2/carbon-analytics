/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(["jquery"], function (jQuery) {
    "use strict";   // JS strict mode
    var self = {};
    self.HTTP_GET = "GET";
    self.HTTP_POST = "POST";
    self.HTTP_PUT = "PUT";
    self.HTTP_DELETE = "DELETE";
    self.siddhiAppsUrl = window.location.protocol + "//" + window.location.host + "/editor";

    self.retrieveSampleEvent = function (appName, streamName, eventFormat, successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            url: self.siddhiAppsUrl + "/siddhi-apps/" + appName + "/streams/" + streamName + "/event/" + eventFormat,
            type: self.HTTP_GET,
            success: function (data) {
                if (typeof successCallback === 'function') {
                    successCallback(data)
                }
            },
            error: function ($xhr) {
                if (typeof errorCallback === 'function') {
                    errorCallback($xhr)
                }
            }
        });
    };

    return self;
});
