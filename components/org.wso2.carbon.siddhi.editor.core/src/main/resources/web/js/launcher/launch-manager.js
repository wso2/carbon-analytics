/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'jquery', 'backbone', 'lodash', 'event_channel', 'console' ],
    function (require, $, Backbone, _ ,EventChannel, Console) {
	var instance;

    var LaunchManager = function(args) {
        var self = this;
        this.enable = false;
        this.channel = undefined;
        this.active = false;
    };

    LaunchManager.prototype = Object.create(EventChannel.prototype);
    LaunchManager.prototype.constructor = LaunchManager;

    LaunchManager.prototype.runApplication = function(siddhiAppName,consoleListManager){
        var consoleOptions = {};
        var options = {};
        _.set(options, '_type', "RUN");
        _.set(options, 'title', "Run");
        _.set(options, 'currentFocusedFile', siddhiAppName);
        $.ajax({
            async: true,
            url: "http://localhost:9090/editor/" + siddhiAppName + "/start",
            type: "GET",
            success: function (data) {
                _.set(options, 'statusForCurrentFocusedFile', data.status);
                _.set(options, 'message', "Started Successfully!");
                _.set(consoleOptions, 'consoleOptions', options);
                consoleListManager.newConsole(consoleOptions);
            },
            error: function (msg) {
                _.set(options, 'statusForCurrentFocusedFile', (JSON.parse(msg.responseText)).status);
                _.set(options, 'message', (JSON.parse(msg.responseText)).message);
                _.set(consoleOptions, 'consoleOptions', options);
                consoleListManager.newConsole(consoleOptions);
            }
        });
    };

    LaunchManager.prototype.debugApplication = function(siddhiAppName,consoleListManager,uniqueTabId){
        var consoleOptions = {};
        var options = {};
        _.set(options, '_type', "DEBUG");
        _.set(options, 'title', "Debug");
        _.set(options, 'statusForCurrentFocusedFile', "SUCCESS");
        _.set(options, 'uniqueTabId', uniqueTabId);
        _.set(consoleOptions, 'consoleOptions', options);
        consoleListManager.newConsole(consoleOptions);
    };

    return (instance = (instance || new LaunchManager()));
});