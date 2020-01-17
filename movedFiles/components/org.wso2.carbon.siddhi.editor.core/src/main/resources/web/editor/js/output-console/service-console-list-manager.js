/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'jquery', 'lodash', 'output_console_list', 'workspace', 'service_console'],

    function (log, $, _, ConsoleList, Workspace, ServiceConsole) {

        const CONSOLE_TYPE_FORM = "FORM";
        const CONSOLE_TYPE_DEBUG = "DEBUG";
        var OutputConsoleList = ConsoleList.extend(
            /** @lends ConsoleList.prototype */
            {
                initialize: function (options) {
                    _.set(options, 'consoleModel', ServiceConsole);
                    ConsoleList.prototype.initialize.call(this, options);
                    this._activateBtn = $(_.get(options, 'activateBtn'));
                    this._openConsoleBtn = $(_.get(options, 'openConsoleBtn'));
                    this._closeConsoleBtn = $(_.get(options, 'closeConsoleBtn'));
                    this._clearConsoleBtn = $(_.get(options, 'cleanConsoleBtn'));
                    this._reloadConsoleBtn = $(_.get(options, 'reloadConsoleBtn'));
                    this.application = _.get(options, 'application');
                    this._options = options;
                    var self = this;
                    this._activateBtn.on('click', function (e) {
                        if (self.activeConsole.options._type === "FORM") {
                            $(self.activeConsole).trigger('close-button-in-form-clicked');
                        } else {
                            e.preventDefault();
                            e.stopPropagation();
                            self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
                        }
                    });
                    this._clearConsoleBtn.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.application.commandManager.dispatch(_.get(self._options, 'commandClearConsole.id'));
                    });
                    this._reloadConsoleBtn.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.initiateLogReader(self._options);
                    });
                    if (this.application.isRunningOnMacOS()) {
                        this._closeConsoleBtn.attr("title", "Close (" + _.get(self._options,
                            'command.shortcuts.mac.label') + ") ").tooltip();
                        this._openConsoleBtn.attr("title", "Open Console (" + _.get(self._options,
                            'command.shortcuts.mac.label') + ") ").tooltip();
                    } else {
                        this._closeConsoleBtn.attr("title", "Close  (" + _.get(self._options,
                            'command.shortcuts.other.label') + ") ").tooltip();
                        this._openConsoleBtn.attr("title", "Open Console  (" + _.get(self._options,
                            'command.shortcuts.other.label') + ") ").tooltip();
                    }
                    // register command
                    this.application.commandManager.registerCommand(options.command.id,
                        {shortcuts: options.command.shortcuts});
                    this.application.commandManager.registerHandler(options.command.id, this.toggleOutputConsole, this);
                    this.application.commandManager.registerCommand(options.commandClearConsole.id,
                        {shortcuts: options.commandClearConsole.shortcuts});
                    this.application.commandManager.registerHandler(options.commandClearConsole.id,
                        this.clearConsole, this);
                },
                isActive: function () {
                    return this._activateBtn.parent('li').hasClass('active');
                },
                toggleOutputConsole: function () {
                    var activeTab = this.application.tabController.getActiveTab();
                    var file = undefined;
                    var console = this.getGlobalConsole();
                    var serviceWrapper = $('#service-tabs-wrapper');
                    if (console !== undefined) {
                        if (this.isActive()) {
                            this._activateBtn.parent('li').removeClass('active');
                            this.hideAllConsoles();
                            if (serviceWrapper.is('.ui-resizable')) {
                                serviceWrapper.resizable('destroy');
                            }
                            if (activeTab._title != "welcome-page") {
                                if (activeTab.getSiddhiFileEditor().isInSourceView()) {
                                    activeTab.getSiddhiFileEditor().getSourceView().editorResize();
                                } else {
                                    ConsoleList.prototype.removePoppedUpElement();
                                }
                            }
                        } else {
                            this._activateBtn.parent('li').addClass('active');
                            this.showAllConsoles();
                        }
                    }
                },
                makeInactiveActivateButton: function () {
                    if (this.isActive()) {
                        this._activateBtn.parent('li').removeClass('active');
                    }
                },
                render: function () {
                    ConsoleList.prototype.render.call(this);
                    this.initiateLogReader(this._options);
                },
                setActiveConsole: function (console) {
                    ConsoleList.prototype.setActiveConsole.call(this, console);
                },
                addConsole: function (console) {
                    ConsoleList.prototype.addConsole.call(this, console);
                },
                removeConsole: function (console) {
                    if (self.activeConsole.options._type === CONSOLE_TYPE_FORM) {
                        $(self.activeConsole).trigger('close-button-in-form-clicked');
                    } else {
                        var commandManager = _.get(this, 'options.application.commandManager');
                        var self = this;
                        var remove = function () {
                            ConsoleList.prototype.removeConsole.call(self, console);
                            if (console instanceof ServiceConsole) {
                                console.trigger('console-removed');
                            }
                        };

                        remove();
                    }
                },
                newConsole: function (opts) {
                    var options = opts || {};
                    return ConsoleList.prototype.newConsole.call(this, options);
                },
                getBrowserStorage: function () {
                },
                hasFilesInWorkingSet: function () {
                    return !_.isEmpty(this._workingFileSet);
                },
                getConsoleForType: function (type, uniqueId) {
                    return _.find(this._consoles, function (console) {
                        if (type === CONSOLE_TYPE_DEBUG) {
                            if (console._uniqueId === uniqueId) {
                                return console;
                            }
                        } else if (type === CONSOLE_TYPE_FORM) {
                            if (console._uniqueId === uniqueId) {
                                return console;
                            }
                        } else {
                            if (console.getType() === type) {
                                return console;
                            }
                        }

                    });
                },
                hideAllConsoles: function () {
                    ConsoleList.prototype.hideConsoleComponents.call(this);
                    this._activateBtn.parent('li').removeClass('active');
                },
                showAllConsoles: function () {
                    ConsoleList.prototype.showConsoleComponents.call(this);
                },
                showConsoleByTitle: function (title, type) {
                    ConsoleList.prototype.enableConsoleByTitle.call(this, title, type);
                },
                getConsoleActivateBtn: function () {
                    return this._activateBtn;
                },
                clearConsole: function () {
                    var console = this._options.application.outputController.getGlobalConsole();
                    console.clear();
                },
                initiateLogReader: function (opts) {
                    var url = "ws://" + opts.application.config.baseUrlHost + "/console";
                    var ws = new WebSocket(url);
                    var lineNumber;
                    ws.onmessage = function (msg) {
                        var console = opts.application.outputController.getGlobalConsole();
                        if (console == undefined) {
                            var consoleOptions = {};
                            var options = {};
                            _.set(options, '_type', "CONSOLE");
                            _.set(options, 'title', "Console");
                            _.set(options, 'statusForCurrentFocusedFile', "LOGGER");
                            _.set(options, 'currentFocusedFile', undefined);
                            _.set(consoleOptions, 'consoleOptions', options);
                            console = opts.application.outputController.newConsole(consoleOptions);
                            // opts.application.outputController.toggleOutputConsole();
                            opts.application.outputController.hideAllConsoles();
                        }
                        var loggerObj = JSON.parse(msg.data);
                        var type = "";
                        var colorDIffType = "";
                        if (loggerObj.level == "INFO" || loggerObj.level == "WARN") {
                            type = "INFO";
                            colorDIffType = "INFO-LOGGER";
                        } else if (loggerObj.level == "ERROR") {
                            type = "ERROR";
                            colorDIffType = "ERROR";
                        }
                        var stacktrace = "";
                        if (loggerObj.stacktrace != null) {
                            stacktrace = "<pre>" + loggerObj.stacktrace + "</pre>";
                        }
                        var logMessage = "[" + loggerObj.timeStamp + "] " + type + " " + "{" + loggerObj.fqcn + "} - " +
                            loggerObj.message + " " + stacktrace;
                        var message = {
                            "type": colorDIffType,
                            "message": logMessage
                        };
                        disableLink($(_.get(opts, 'reloadConsoleBtn'))[0]);
                        console.println(message);
                    };
                    ws.onerror = function (error) {
                        console.error('Editor console service encountered an error, ', error, 'Hence, ' +
                            'closing connection.');
                        ws.close();
                    };
                    ws.onclose = function (error) {
                        var console = opts.application.outputController.getGlobalConsole();
                        if (console == undefined) {
                            var consoleOptions = {};
                            var options = {};
                            _.set(options, '_type', "CONSOLE");
                            _.set(options, 'title', "Console");
                            _.set(options, 'statusForCurrentFocusedFile', "LOGGER");
                            _.set(options, 'currentFocusedFile', undefined);
                            _.set(consoleOptions, 'consoleOptions', options);
                            console = opts.application.outputController.newConsole(consoleOptions);
                            // opts.application.outputController.toggleOutputConsole();
                            opts.application.outputController.hideAllConsoles();
                        }
                        var logMessage = "Connection closed, backend is unavailable! " +
                            "Please try reconnecting the console "+
                            "by pressing the reload button "+
                            "when backend is available.";
                        var message = {
                            "type": "ERROR",
                            "message": logMessage
                        };
                        enableLink($(_.get(opts, 'reloadConsoleBtn'))[0]);
                        console.println(message);
                    };
                }
            });
        return OutputConsoleList;
    });

/**
 *enable the reconnect button when  backend is unavailable.
 * @param link
 */
var enableLink = function (link) {
    link.removeAttribute('style');
    link.classList.remove('console-sync-btn-disable');
    link.href = link.getAttribute('data-href');
    link.removeAttribute('aria-disabled');
}

/**
 *disable the reconnect button when backend is available.
 * @param link
 */
var disableLink = function (link) {
    link.classList.add('console-sync-btn-disable');
    link.setAttribute('style', 'color: rgb(80, 80, 80)');
    link.setAttribute('data-href', link.href);
    link.href = '';
    link.setAttribute('aria-disabled', 'true');
}
