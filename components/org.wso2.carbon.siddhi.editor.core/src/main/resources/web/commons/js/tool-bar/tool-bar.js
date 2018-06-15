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

define(['log', 'jquery', 'lodash', 'workspace', 'backbone'],
    function (log, $, _, Workspace, Backbone) {
        var ToolBar = Backbone.View.extend(
            /** @lends ConsoleList.prototype */
            {
                initialize: function (options) {
                    this._runButn = $(_.get(options, 'runIconBtn'));
                    this._debugBtn = $(_.get(options, 'debugIconBtn'));
                    this._stopBtn = $(_.get(options, 'stopIconBtn'));
                    this._revertBtn = $(_.get(options, 'revertIconBtn'));
                    this.application = _.get(options, 'application');
                    this._$parent_el = $(_.get(options, 'container'));
                    this._options = options;
                    var self = this;
                    this._runButn.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.application.commandManager.dispatch(_.get(self._options, 'commandRun.id'));
                    });
                    this._debugBtn.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.application.commandManager.dispatch(_.get(self._options, 'commandDebug.id'));
                    });
                    this._stopBtn.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.application.commandManager.dispatch(_.get(self._options, 'commandStop.id'));
                    });
                    this._revertBtn.on('click', function (e) {
                        e.preventDefault();
                        e.stopPropagation();
                        self.application.commandManager.dispatch(_.get(self._options, 'commandRevert.id'));
                    });

                    // register command
                    this.application.commandManager.registerCommand(options.commandRun.id);
                    this.application.commandManager.registerHandler(options.commandRun.id, this.runApp, this);
                    this.application.commandManager.registerCommand(options.commandDebug.id);
                    this.application.commandManager.registerHandler(options.commandDebug.id, this.debugApp, this);
                    this.application.commandManager.registerCommand(options.commandStop.id);
                    this.application.commandManager.registerHandler(options.commandStop.id, this.stopApp, this);
                    this.application.commandManager.registerCommand(options.commandRevert.id);
                    this.application.commandManager.registerHandler(options.commandRevert.id, this.revertAppContent, this);
                },
                render: function () {
                    ConsoleList.prototype.render.call(this);
                    this.initiateLogReader(this._options);
                },
                runApp: function(){
                    var launcher = this.application.tabController.getActiveTab().getSiddhiFileEditor().getLauncher();
                    launcher.runApplication(this.application.workspaceManager, false);
                },
                debugApp: function(){
                    var launcher = this.application.tabController.getActiveTab().getSiddhiFileEditor().getLauncher();
                    launcher.debugApplication(this.application.workspaceManager, false);
                },
                stopApp: function(){
                    var launcher = this.application.tabController.getActiveTab().getSiddhiFileEditor().getLauncher();
                    launcher.stopApplication(this.application.workspaceManager, false);
                },
                revertAppContent: function(){
                    this.application.workspaceManager.revertAppContent();
                },
                disableRunButton: function(){
                    this._runButn.addClass("disabled");
                    this._runButn.removeClass("active");
                },
                disableDebugButton: function(){
                    this._debugBtn.addClass("disabled");
                    this._debugBtn.removeClass("active");
                },
                disableStopButton: function(){
                    this._stopBtn.addClass("disabled");
                    this._stopBtn.removeClass("active");
                },
                disableRevertButton: function(){
                    this._revertBtn.addClass("disabled");
                    this._revertBtn.removeClass("active");
                },
                enableRunButton: function(){
                    this._runButn.removeClass("disabled");
                    this._runButn.addClass("active");
                },
                enableDebugButton: function(){
                    this._debugBtn.removeClass("disabled");
                    this._debugBtn.addClass("active");
                },
                enableStopButton: function(){
                    this._stopBtn.removeClass("disabled");
                    this._stopBtn.addClass("active");
                },
                enableRevertButton: function(){
                    this._revertBtn.removeClass("disabled");
                    this._revertBtn.addClass("active");
                }
            });
        return ToolBar;
    });
