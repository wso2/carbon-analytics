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
define(['log', 'jquery', 'lodash', 'output_console_list', 'workspace', 'service_console'],

    function (log, $, _, ConsoleList, Workspace, ServiceConsole) {

        var OutputConsoleList = ConsoleList.extend(
            /** @lends ConsoleList.prototype */
            {
                initialize: function (options) {
                    _.set(options, 'consoleModel', ServiceConsole);
                    ConsoleList.prototype.initialize.call(this, options);
                    this._activateBtn = $(_.get(options, 'activateBtn'));
                    this._clearConsoleBtn = $(_.get(options, 'cleanConsoleBtn'));
                    this.application = _.get(options, 'application');
                    this._options = options;
                    var self = this;
                    this._activateBtn.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
                    });
                    this._clearConsoleBtn.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.application.commandManager.dispatch(_.get(self._options, 'commandClearConsole.id'));
                    });

                    this._activateBtn.attr("data-placement", "right").attr("data-container", "body");
                    if (this.application.isRunningOnMacOS()) {
                        this._activateBtn.attr("title", "Output Console (" + _.get(self._options, 'command.shortcuts.mac.label') + ") ").tooltip();
                    } else {
                        this._activateBtn.attr("title", "Output Console  (" + _.get(self._options, 'command.shortcuts.other.label') + ") ").tooltip();
                    }
                    // register command
                    this.application.commandManager.registerCommand(options.command.id, {shortcuts: options.command.shortcuts});
                    this.application.commandManager.registerHandler(options.command.id, this.toggleOutputConsole, this);
                    this.application.commandManager.registerCommand(options.commandClearConsole.id, {shortcuts: options.commandClearConsole.shortcuts});
                    this.application.commandManager.registerHandler(options.commandClearConsole.id, this.clearConsole, this);
                },
                isActive: function () {
                    return this._activateBtn.parent('li').hasClass('active');
                },
                toggleOutputConsole: function () {
                    var activeTab = this.application.tabController.getActiveTab();
                    var file = undefined;
                    var console = this.getGlobalConsole();
                    var serviceWrapper =  $('#service-tabs-wrapper');
                    if (console !== undefined) {
                        if (this.isActive()) {
                            this._activateBtn.parent('li').removeClass('active');
                            this.hideAllConsoles();
                            if (serviceWrapper.is('.ui-resizable')){
                                serviceWrapper.resizable( "destroy" );
                            }
                            if(activeTab._title != "welcome-page"){
                                if (activeTab.getSiddhiFileEditor().isInSourceView()) {
                                    activeTab.getSiddhiFileEditor().getSourceView().editorResize();
                                } else {
                                    activeTab.getSiddhiFileEditor().getEventFlow().graphResize();
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
                    var commandManager = _.get(this, 'options.application.commandManager');
                    var self = this;
                    var remove = function () {
                        ConsoleList.prototype.removeConsole.call(self, console);
                        if (console instanceof ServiceConsole) {
//                          _.remove(self._workingFileSet, function(fileID){
//                              return _.isEqual(fileID, tab.getFile().id);
//                          });
                            console.trigger('console-removed');
//                          self.getBrowserStorage().destroy(tab.getFile());
//                          self.getBrowserStorage().put('workingFileSet', self._workingFileSet);
//                          // open welcome page upon last tab close
//                          if(_.isEmpty(self.getTabList())){
//                              var commandManager = _.get(self, 'options.application.commandManager');
//                              commandManager.dispatch("go-to-welcome-page");
//                          }
                        }
                    }

                    remove();
                },
                newConsole: function (opts) {
                    var options = opts || {};
                    return ConsoleList.prototype.newConsole.call(this, options);
                },
                getBrowserStorage: function () {
                    //return _.get(this, 'options.application.browserStorage');
                },
                hasFilesInWorkingSet: function () {
                    return !_.isEmpty(this._workingFileSet);
                },
                getConsoleForType: function (type, uniqueId) {
                    return _.find(this._consoles, function (console) {
                        if (type == "DEBUG") {
                            if (console._uniqueId == uniqueId) {
                                return console;
                            }
                        } else {
                            if (console.getType() == type) {
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
                showConsoleByTitle: function (title,type) {
                    ConsoleList.prototype.enableConsoleByTitle.call(this, title,type);
                },
                getConsoleActivateBtn: function () {
                    return this._activateBtn;
                },
                clearConsole: function(){
                    var console = this._options.application.outputController.getGlobalConsole();
                    console.clear();
                },
                initiateLogReader: function (opts) {
                    var url = "ws://" + opts.application.config.baseUrlHost + "/console";
                    var ws = new WebSocket(url);
                    var lineNumber;
                    ws.onmessage = function(msg) {
                        var console = opts.application.outputController.getGlobalConsole();
                        if(console == undefined){
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
                        if(loggerObj.level == "INFO" || loggerObj.level == "WARN"){
                            type = "INFO";
                            colorDIffType = "INFO-LOGGER";
                        } else if(loggerObj.level == "ERROR"){
                            type = "ERROR";
                            colorDIffType = "ERROR";
                        }
                        var stacktrace = "";
                        if(loggerObj.stacktrace != null){
                            stacktrace = "<pre>" + loggerObj.stacktrace + "</pre>";
                        }
                        var logMessage = "[" + loggerObj.timeStamp + "] " + type + " " + "{" + loggerObj.fqcn + "} - " +
                            loggerObj.message + " " + stacktrace ;
                        var message = {
                            "type" : colorDIffType,
                            "message": logMessage
                        };
                        console.println(message);
                    };
                    ws.onerror = function(error) {
                        console.log(error);
                    };
                    ws.onclose = function() {

                    };
                }
            });

        return OutputConsoleList;
    });
