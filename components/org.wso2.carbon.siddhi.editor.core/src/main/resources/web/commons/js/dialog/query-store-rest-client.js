/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
define(["jquery"], function (jQuery) {
    var self = {};
    self.siddhiStoreUrl = window.location.protocol + "//" + window.location.host + "/editor/stores/query";
    self.retrieveStoresQuery = function (appName, query, successCallback, errorCallback) {
        jQuery.ajax({
            async: true,
            type: "POST",
            contentType: "application/json",
            url: self.siddhiStoreUrl,
            data: "{\"appName\": \"" + appName + "\", \"query\": \"" + query + "\", \"details\": true }",
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
