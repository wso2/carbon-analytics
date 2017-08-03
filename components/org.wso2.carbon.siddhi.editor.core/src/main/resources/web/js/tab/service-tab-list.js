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
define(['log', 'jquery', 'lodash', './tab-list', './service-tab',  'workspace','workspace/file'],

    function (log, $, _, TabList, ServiceTab, Workspace,File) {

        var ServiceTabList = TabList.extend(
            /** @lends ServiceTabList.prototype */
            {
                /**
                 * @augments ServiceTabList
                 * @constructs
                 * @class ServiceTabList represents service tab list.
                 */
                initialize: function (options) {
                    _.set(options, 'tabModel', ServiceTab);
                    TabList.prototype.initialize.call(this, options);
                    var lastWorkedFiles = this.getBrowserStorage().get('workingFileSet');
                    this.outputController = _.get(this, 'options.application.outputController');
                    this._workingFileSet = [];
                    var self = this;
                    if(!_.isNil(lastWorkedFiles)){
                        lastWorkedFiles.forEach(function(fileID){
                            self._workingFileSet.push(fileID);
                        });
                    }

                    this.commandManager = _.get(this, 'options.application.commandManager');
                    var optionsNextTab = {
                        shortcuts: {
                            mac: {
                                key: "command+right",
                                label: "\u2318\u2192"
                            },
                            other: {
                                key: "ctrl+right",
                                label: "Ctrl+Right"
                            }
                        }
                    };
                    this.commandManager.registerCommand("next-tab", optionsNextTab);
                    this.commandManager.registerHandler("next-tab", this.goToNextTab, this);
                    var optionsPrevTab = {
                        shortcuts: {
                            mac: {
                                key: "command+left",
                                label: "\u2318\u2190"
                            },
                            other: {
                                key: "ctrl+left",
                                label: "Ctrl+Left"
                            }
                        }
                    };
                    this.commandManager.registerCommand("previous-tab", optionsPrevTab);
                    this.commandManager.registerHandler("previous-tab", this.goToPreviousTab, this);
                },
                render: function() {
                    TabList.prototype.render.call(this);
                    if(!_.isEmpty(this._workingFileSet)){
                        var self = this;
                        this._workingFileSet.forEach(function(fileID){
                            var fileData = self.getBrowserStorage().get(fileID);
                            var file = new File(fileData, {storage:self.getBrowserStorage()});
                            var tab = self.newTab(_.set({}, 'tabOptions.file', file));
                            tab.updateHeader();
                        });
                    }
                },
                setActiveTab: function(tab) {
                    TabList.prototype.setActiveTab.call(this, tab);
                },
                addTab: function(tab) {
                    TabList.prototype.addTab.call(this, tab);
                    if(tab instanceof ServiceTab && !_.includes(this._workingFileSet, tab.getFile().id)){
                        tab.getFile().save();
                        this._workingFileSet.push(tab.getFile().id);
                        this.getBrowserStorage().put('workingFileSet', this._workingFileSet);
                    }
                    $('[data-toggle="tooltip"]').tooltip();
                },
                removeTab: function (tab) {
                    this.commandManager = _.get(this, 'options.application.commandManager');
                    var self = this;
                    var remove = function() {
                        TabList.prototype.removeTab.call(self, tab);
                        if(tab instanceof ServiceTab) {
                          _.remove(self._workingFileSet, function(fileID){
                              return _.isEqual(fileID, tab.getFile().id);
                          });
                          tab.trigger('tab-removed');
                          self.getBrowserStorage().destroy(tab.getFile());
                          self.getBrowserStorage().put('workingFileSet', self._workingFileSet);
                          // open welcome page upon last tab close
                          if(_.isEmpty(self.getTabList())){
                              self.commandManager.dispatch("go-to-welcome-page");
                          }
                        }

                    }

                    if(!_.isFunction(tab.getFile)){
                        remove();
                        return;
                    }

                    var file = tab.getFile();

                    if(file.getRunStatus() || file.getDebugStatus()){
                        this.commandManager.dispatch('stop',{initialLoad: false});
                    }

                    if(file.isPersisted() && !file.isDirty()){
                        // if file is not dirty no need to ask for confirmation
                        remove();
                        return;
                    }

                    if(!file.isPersisted() && _.isEmpty(file.getContent())){
                        // if file is not dirty no need to ask for confirmation
                        remove();
                        return;
                    }

                    var handleConfirm = function(shouldSave) {
                        if(shouldSave) {
                            var done = function(saved) {
                                if(saved) {
                                    remove();
                                }
                                // saved is false if cancelled. Then don't close the tab
                            }
                            self.commandManager.dispatch('save', {callback: done});
                        } else {
                            remove();
                        }
                    }

                    self.commandManager.dispatch('open-close-file-confirm-dialog', {
                        file: file,
                        handleConfirm: handleConfirm
                    });
                },
                newTab: function(opts) {
                    var options = opts || {};
                    var file = undefined;
                    var self = this;
                    if(_.has(options, 'tabOptions.file')){
                        file = _.get(options, 'tabOptions.file');
                        file.setStorage(this.getBrowserStorage());
                    }
                    var tab = TabList.prototype.newTab.call(this, options);
                    if(tab instanceof ServiceTab){
                        tab.updateHeader();
                    }
                    $('[data-toggle="tooltip"]').tooltip();
                    this.outputController.hideAllConsoles();
                    if(file !== undefined){
                        if(file.getRunStatus() || file.getDebugStatus()){
                            self.commandManager.dispatch('stop',{initialLoad: true});
                        }
                    }
                    return tab;
                },
                goToNextTab: function(){
                    if(!_.isEmpty(this._tabs)){
                        var nextTabIndex = 0,
                            currentActiveIndex = _.findIndex(this._tabs, this.activeTab);
                        if(currentActiveIndex >= 0){
                            if(currentActiveIndex < (this._tabs.length - 1)){
                                nextTabIndex = currentActiveIndex + 1;
                            }
                        }
                        var nextTab = _.nth(this._tabs, nextTabIndex);
                        this.setActiveTab(nextTab);
                    }
                },
                goToPreviousTab: function(){
                    if(!_.isEmpty(this._tabs)){
                        var currentActiveIndex = _.findIndex(this._tabs, this.activeTab),
                            prevTabIndex = 0;
                        if(currentActiveIndex == 0){
                            prevTabIndex = this._tabs.length - 1;
                        } else{
                            prevTabIndex = currentActiveIndex - 1;
                        }
                        var previousTab = _.nth(this._tabs, prevTabIndex);
                        this.setActiveTab(previousTab);
                    }
                },
                getTabForFile: function(file){
                    return _.find(this._tabs, function(tab){
                        if(tab instanceof ServiceTab){
                            var tabFile = tab.getFile();
                            return _.isEqual(tabFile.getPath(), file.getPath()) &&  _.isEqual(tabFile.getName(), file.getName());
                        }
                    });
                },
                getTabFromTitle: function(appName){
                    return _.find(this._tabs, function(tab){ return tab._title == appName + ".siddhi" });
                },
                getBrowserStorage: function(){
                    return _.get(this, 'options.application.browserStorage');
                },
                hasFilesInWorkingSet: function(){
                    return !_.isEmpty(this._workingFileSet);
                }
            });

        return ServiceTabList;
    });
