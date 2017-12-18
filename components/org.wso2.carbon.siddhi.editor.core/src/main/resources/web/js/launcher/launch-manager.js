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
        this.baseurl = window.location.protocol + "//" + window.location.host + "/editor/";
    };

    LaunchManager.prototype = Object.create(EventChannel.prototype);
    LaunchManager.prototype.constructor = LaunchManager;

    LaunchManager.prototype.runApplication = function(siddhiAppName,consoleListManager,activeTab,workspace, async){
        var consoleOptions = {};
        var options = {};
        _.set(options, '_type', "CONSOLE");
        _.set(options, 'title', "Console");
        _.set(options, 'currentFocusedFile', siddhiAppName);
        if (null == async) {
            async = true;
        }
        var console;
        $.ajax({
            async: async,
            url: this.baseurl+ siddhiAppName + "/start",
            type: "GET",
            success: function (data) {
                _.set(options, 'statusForCurrentFocusedFile', data.status);
                _.set(options, 'message', " Started Successfully!");
                _.set(consoleOptions, 'consoleOptions', options);
                activeTab.getFile().setRunStatus(true);
                activeTab.getFile().save();
                console = consoleListManager.newConsole(consoleOptions);
                console.addRunningPlan(siddhiAppName);
                workspace.updateRunMenuItem();
                activeTab.setRunMode();
            },
            error: function (msg) {
                _.set(options, 'statusForCurrentFocusedFile', (JSON.parse(msg.responseText)).status);
                _.set(options, 'message', (JSON.parse(msg.responseText)).message);
                _.set(consoleOptions, 'consoleOptions', options);
                activeTab.getFile().setRunStatus(false);
                activeTab.getFile().save();
                console = consoleListManager.newConsole(consoleOptions);
                workspace.updateRunMenuItem();
            }
        });
    };

    LaunchManager.prototype.stopApplication = function(siddhiAppName,consoleListManager,activeTab,workspace,initialLoad, async){
        if(activeTab.getFile().getRunStatus() || initialLoad){
            var console = undefined;
            if(!initialLoad){
                console = consoleListManager.getGlobalConsole();
            }
            if (null == async) {
                async = true;
            }
            $.ajax({
                async: async,
                url: this.baseurl + siddhiAppName + "/stop",
                type: "GET",
                success: function (data) {
                    if(console != undefined){
                        var msg = "";
                        activeTab.getFile().setRunStatus(false);
                        activeTab.getFile().save();
                        msg = "" + siddhiAppName + ".siddhi - Stopped Successfully!"
                        var message = {
                            "type" : "INFO",
                            "message": msg
                        }
                        console.println(message);
                        workspace.updateRunMenuItem();
                        activeTab.setNonRunningMode();
                    }else if(initialLoad){
                        activeTab.getFile().setRunStatus(false);
                        activeTab.getFile().setDebugStatus(false);
                        activeTab.getFile().save();
                    }

                },
                error: function (msg) {
                    if(console != undefined){
                        msg = ""+siddhiAppName+".siddhi - Error in Stopping."
                        var message = {
                            "type" : "ERROR",
                            "message": msg
                        }
                        console.println(message);
                        workspace.updateRunMenuItem();
                    }

                }
            });
        } else{
            activeTab.getSiddhiFileEditor().getDebuggerWrapper().stop();
        }
    };

    LaunchManager.prototype.debugApplication = function(siddhiAppName,consoleListManager,uniqueTabId,
        debuggerWrapperInstance,activeTab,workspace, async){
        var consoleOptions = {};
        var options = {};
        _.set(options, '_type', "DEBUG");
        _.set(options, 'title', "Debug");
        _.set(options, 'statusForCurrentFocusedFile', "SUCCESS");
        _.set(options, 'uniqueTabId', uniqueTabId);
        _.set(options, 'appName', siddhiAppName);

        debuggerWrapperInstance.debug(
            function (runtimeId, streams, queries) {
                // debug successfully started
                debuggerWrapperInstance.setDebuggerStarted(true);
                debuggerWrapperInstance.acquireDebugPoints();
                var console = consoleListManager.getGlobalConsole();
                if(console == undefined){
                    var globalConsoleOptions = {};
                    var opts = {};
                    _.set(opts, '_type', "CONSOLE");
                    _.set(opts, 'title', "Console");
                    _.set(opts, 'currentFocusedFile', siddhiAppName);
                    _.set(opts, 'statusForCurrentFocusedFile', "SUCCESS");
                    _.set(opts, 'message', " - Started in Debug mode Successfully!");
                    _.set(globalConsoleOptions, 'consoleOptions', opts);
                    console = consoleListManager.newConsole(globalConsoleOptions);
                }else {
                    var message = {
                        "type" : "INFO",
                        "message": "" + siddhiAppName + ".siddhi - Started in Debug mode Successfully!"
                    };
                    console.println(message);
                }
                activeTab.getFile().setDebugStatus(true);
                activeTab.getFile().save();
                workspace.updateRunMenuItem();
                console.addRunningPlan(siddhiAppName);
                _.set(options, 'consoleObj', console);
                _.set(consoleOptions, 'consoleOptions', options);
                consoleListManager.newConsole(consoleOptions);
                activeTab.setDebugMode();
            }, function (msg) {
                // debug not started (possible error)
                debuggerWrapperInstance.setDebuggerStarted(false);
                var console = consoleListManager.getGlobalConsole();
                if(console == undefined){
                var globalConsoleOptions = {};
                    var opts = {};
                    _.set(opts, '_type', "CONSOLE");
                    _.set(opts, 'title', "Console");
                    _.set(opts, 'currentFocusedFile', siddhiAppName);
                    _.set(opts, 'statusForCurrentFocusedFile', (JSON.parse(msg.responseText)).status);
                    _.set(opts, 'message', (JSON.parse(msg.responseText)).message);
                    _.set(globalConsoleOptions, 'consoleOptions', opts);
                    consoleListManager.newConsole(globalConsoleOptions);
                }else {
                    var message = {
                        "type" : "ERROR",
                        "message": "" + siddhiAppName + ".siddhi - Could not start in debug mode.Siddhi App is in" +
                         " faulty state."
                    };
                    console.println(message);
                }
                activeTab.getFile().setDebugStatus(false);
                activeTab.getFile().save();
                workspace.updateRunMenuItem();
            },
            async
        );
    };

    return (instance = (instance || new LaunchManager()));
});