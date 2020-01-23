/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'jquery', 'backbone', 'lodash', 'event_channel', 'console', 'utils'],
    function (require, $, Backbone, _, EventChannel, Console, Utils) {
        var instance;

        var LaunchManager = function (args) {
            var self = this;
            this.enable = false;
            this.channel = undefined;
            this.active = false;
            this.baseurl = window.location.protocol + "//" + window.location.host + "/editor/";
        };

        LaunchManager.prototype = Object.create(EventChannel.prototype);
        LaunchManager.prototype.constructor = LaunchManager;

        LaunchManager.prototype.runApplication = function (siddhiAppName, consoleListManager, activeTab, workspace, async) {
            if (activeTab.getFile().getRunStatus() !== undefined && !activeTab.getFile().getRunStatus() &&
                !activeTab.getFile().getDebugStatus()) {
                var consoleOptions = {};
                var options = {};
                _.set(options, '_type', "CONSOLE");
                _.set(options, 'title', "Console");
                _.set(options, 'currentFocusedFile', siddhiAppName);
                if (null == async) {
                    async = true;
                }
                var console;
                var variableMap = Utils.prototype.retrieveEnvVariables();
                var data = {
                    siddhiAppName: siddhiAppName,
                    variables: variableMap
                };
                $.ajax({
                    async: async,
                    type: "POST",
                    url: this.baseurl + "start",
                    data: JSON.stringify(data),
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
                        try {
                            _.set(options, 'statusForCurrentFocusedFile', (JSON.parse(msg.responseText)).status);
                            _.set(options, 'message', (JSON.parse(msg.responseText)).message);
                            _.set(consoleOptions, 'consoleOptions', options);
                            activeTab.getFile().setRunStatus(false);
                            activeTab.getFile().save();
                            console = consoleListManager.newConsole(consoleOptions);
                            workspace.updateRunMenuItem();
                        } catch (err) {
                            console.log("Error while parsing the Error response from the " +
                                "while starting Siddhi app." + err);
                        }
                    }
                });
            }
        };

        LaunchManager.prototype.stopApplication = function (siddhiAppName, consoleListManager, activeTab, workspace, initialLoad, async) {
            if (activeTab.getFile().getRunStatus() || initialLoad) {
                var console = undefined;
                if (!initialLoad) {
                    console = consoleListManager.getGlobalConsole();
                }
                if (null == async) {
                    async = true;
                }
                consoleListManager.application.toolBar.enableStopButtonLoading();
                $.ajax({
                    async: async,
                    url: this.baseurl + siddhiAppName + "/stop",
                    type: "GET",
                    success: function (data) {
                        if (console != undefined) {
                            activeTab.getFile().setStopProcessRunning(false);
                            activeTab.getSiddhiFileEditor().getSourceView().onEnvironmentChange();
                            var msg = "";
                            activeTab.getFile().setRunStatus(false);
                            activeTab.getFile().save();
                            msg = "" + siddhiAppName + ".siddhi - Stopped Successfully!"
                            var message = {
                                "type": "INFO",
                                "message": msg
                            }
                            console.println(message);
                            workspace.updateRunMenuItem();
                            activeTab.setNonRunningMode();
                            consoleListManager.application.toolBar.disableStopButtonLoading();
                        } else if (initialLoad) {
                            activeTab.getFile().setRunStatus(false);
                            activeTab.getFile().setStopProcessRunning(false);
                            activeTab.getFile().setDebugStatus(false);
                            activeTab.getFile().save();
                            consoleListManager.application.toolBar.disableStopButtonLoading();
                        }

                    },
                    error: function (msg) {
                        if (console != undefined) {
                            msg = "" + siddhiAppName + ".siddhi - Error in Stopping."
                            var message = {
                                "type": "ERROR",
                                "message": msg
                            }
                            console.println(message);
                            workspace.updateRunMenuItem();
                            consoleListManager.application.toolBar.disableStopButtonLoading();
                        }

                    }
                });
            } else {
                activeTab.getSiddhiFileEditor().getDebuggerWrapper().stop();
            }
        };

        LaunchManager.prototype.debugApplication = function (siddhiAppName, consoleListManager, uniqueTabId,
                                                             debuggerWrapperInstance, activeTab, workspace, async) {
            if (!activeTab.getFile().getRunStatus()) {
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
                        if (console == undefined) {
                            var globalConsoleOptions = {};
                            var opts = {};
                            _.set(opts, '_type', "CONSOLE");
                            _.set(opts, 'title', "Console");
                            _.set(opts, 'currentFocusedFile', siddhiAppName);
                            _.set(opts, 'statusForCurrentFocusedFile', "SUCCESS");
                            _.set(opts, 'message', " - Started in Debug mode Successfully!");
                            _.set(globalConsoleOptions, 'consoleOptions', opts);
                            console = consoleListManager.newConsole(globalConsoleOptions);
                        } else {
                            var message = {
                                "type": "INFO",
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
                        if (console == undefined) {
                            var globalConsoleOptions = {};
                            var opts = {};
                            _.set(opts, '_type', "CONSOLE");
                            _.set(opts, 'title', "Console");
                            _.set(opts, 'currentFocusedFile', siddhiAppName);
                            _.set(opts, 'statusForCurrentFocusedFile', (JSON.parse(msg.responseText)).status);
                            _.set(opts, 'message', (JSON.parse(msg.responseText)).message);
                            _.set(globalConsoleOptions, 'consoleOptions', opts);
                            consoleListManager.newConsole(globalConsoleOptions);
                        } else {
                            var message = {
                                "type": "ERROR",
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
            }
        };

        return (instance = (instance || new LaunchManager()));
    });
