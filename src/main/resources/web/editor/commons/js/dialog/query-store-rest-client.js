/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(["jquery"], function (jQuery) {
    var self = {};
    self.siddhiStoreUrl = window.location.protocol + "//" + window.location.host + "/editor/query";
    self.retrieveStoresQuery = function (appName, query, successCallback, errorCallback) {
        
        var request = {
          "appName" : appName,
          "query" : query,
          "details" : true
        };

        jQuery.ajax({
            async: true,
            type: "POST",
            contentType: "application/json",
            url: self.siddhiStoreUrl,
            data: JSON.stringify(request),
            success: function (data) {
                if (typeof successCallback === 'function') {
                    successCallback(data);
                }
            },
            error: function ($xhr) {
                if (typeof errorCallback === 'function') {
                    errorCallback($xhr);
                }
            }
        });
    };
    return self;
});
