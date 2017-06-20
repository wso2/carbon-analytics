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
define(['log', 'jquery', 'lodash', 'output_console_list',  'workspace','service_console'],

    function (log, $, _, ConsoleList, Workspace,ServiceConsole) {

        var OutputConsoleList = ConsoleList.extend(
            /** @lends ConsoleList.prototype */
            {
                initialize: function (options) {
                    _.set(options, 'consoleModel', ServiceConsole);
                    ConsoleList.prototype.initialize.call(this, options);
//                    var lastWorkedFiles = this.getBrowserStorage().get('workingFileSet');
//                    this._workingFileSet = [];
//                    var self = this;
//                    if(!_.isNil(lastWorkedFiles)){
//                        lastWorkedFiles.forEach(function(fileID){
//                            self._workingFileSet.push(fileID);
//                        });
//                    }

                    //var commandManager = _.get(this, 'options.application.commandManager');
//                    var optionsNextTab = {
//                        shortcuts: {
//                            mac: {
//                                key: "command+right",
//                                label: "\u2318\u2192"
//                            },
//                            other: {
//                                key: "ctrl+right",
//                                label: "Ctrl+Right"
//                            }
//                        }
//                    };
//                    commandManager.registerCommand("next-tab", optionsNextTab);
//                    commandManager.registerHandler("next-tab", this.goToNextTab, this);
//                    var optionsPrevTab = {
//                        shortcuts: {
//                            mac: {
//                                key: "command+left",
//                                label: "\u2318\u2190"
//                            },
//                            other: {
//                                key: "ctrl+left",
//                                label: "Ctrl+Left"
//                            }
//                        }
//                    };
//                    commandManager.registerCommand("previous-tab", optionsPrevTab);
//                    commandManager.registerHandler("previous-tab", this.goToPreviousTab, this);
                },
                render: function() {
                    ConsoleList.prototype.render.call(this);
//                    if(!_.isEmpty(this._workingFileSet)){
//                        var self = this;
//                        this._workingFileSet.forEach(function(fileID){
//                            var fileData = self.getBrowserStorage().get(fileID);
//                            var file = new File(fileData, {storage:self.getBrowserStorage()});
//                            var tab = self.newTab(_.set({}, 'tabOptions.file', file));
//                            tab.updateHeader();
//                        });
//                    }
                },
                setActiveConsole: function(console) {
                    ConsoleList.prototype.setActiveConsole.call(this, console);
                },
                addConsole: function(console) {
                    ConsoleList.prototype.addConsole.call(this, console);
//                    if(tab instanceof ServiceTab && !_.includes(this._workingFileSet, tab.getFile().id)){
//                        tab.getFile().save();
//                        this._workingFileSet.push(tab.getFile().id);
//                        this.getBrowserStorage().put('workingFileSet', this._workingFileSet);
//                    }
//                    $('[data-toggle="tooltip"]').tooltip();
                },
                removeConsole: function (console) {
                    var commandManager = _.get(this, 'options.application.commandManager');
                    var self = this;
                    var remove = function() {
                        ConsoleList.prototype.removeConsole.call(self, console);
                        if(console instanceof ServiceConsole) {
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


//                    if(!_.isFunction(tab.getFile)){
//                        remove();
//                        return;
//                    }
//
//                    var file = tab.getFile();
//                    if(file.isPersisted() && !file.isDirty()){
//                        // if file is not dirty no need to ask for confirmation
//                        remove();
//                        return;
//                    }
//
//                    if(!file.isPersisted() && _.isEmpty(file.getContent())){
//                        // if file is not dirty no need to ask for confirmation
//                        remove();
//                        return;
//                    }
//
//                    var handleConfirm = function(shouldSave) {
//                        if(shouldSave) {
//                            var done = function(saved) {
//                                if(saved) {
//                                    remove();
//                                }
//                                // saved is false if cancelled. Then don't close the tab
//                            }
//                            commandManager.dispatch('save', {callback: done});
//                        } else {
//                            remove();
//                        }
//                    }
//
//                    commandManager.dispatch('open-close-file-confirm-dialog', {
//                        file: file,
//                        handleConfirm: handleConfirm
//                    });
                },
                newConsole: function(opts) {
                    var options = opts || {};
//                    if(_.has(options, 'tabOptions.file')){
//                        var file = _.get(options, 'tabOptions.file');
//                        file.setStorage(this.getBrowserStorage());
//                    }
                    var console = ConsoleList.prototype.newConsole.call(this, options);
//                    if(tab instanceof ServiceTab){
//                        tab.updateHeader();
//                    }
//                    $('[data-toggle="tooltip"]').tooltip();
                    return console;
                },
                getBrowserStorage: function(){
                    //return _.get(this, 'options.application.browserStorage');
                },
                hasFilesInWorkingSet: function(){
                    return !_.isEmpty(this._workingFileSet);
                },
                getConsoleForType: function(type,uniqueId){
                    return _.find(this._consoles, function(console){
                        if(type == "DEBUG"){
                            if(console._uniqueId == uniqueId){
                                return console;
                            }
                        } else {
                            if(console.getType() == type){
                                return console;
                            }
                        }

                    });
                }
            });

        return OutputConsoleList;
    });
