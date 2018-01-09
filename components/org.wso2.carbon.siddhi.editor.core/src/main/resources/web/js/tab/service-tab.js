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
define(['require', 'log', 'jquery', 'lodash', './tab','workspace','toolEditor','workspace/file'],
    function (require, log, jquery, _, Tab, Workspace,ToolEditor,File) {
        var  ServiceTab;

        ServiceTab = Tab.extend({
            initialize: function (options) {
                Tab.prototype.initialize.call(this, options);
                if(!_.has(options, 'file')){
                    this._file = new File({isTemp: true, isDirty: false}, {storage: this.getParent().getBrowserStorage()});
                } else {
                    this._file = _.get(options, 'file');
                }
                this.app = options.application;
            },
            getFile: function(){
                return this._file;
            },

            getTitle: function(){
                return _.isNil(this._file) ? "untitled" :  this._file.getName();
            },

            updateHeader: function(){
                if (this._file.isDirty()) {
                    this.getHeader().setText('* ' + this.getTitle());
                } else {
                    this.getHeader().setText(this.getTitle());
                }
            },

            render: function () {
                var self = this;
                Tab.prototype.render.call(this);
                var serviceEditorOpts = _.get(this.options, 'das_editor');
                _.set(serviceEditorOpts, 'toolPalette', this.getParent().options.toolPalette);
                _.set(serviceEditorOpts, 'container', this.$el.get(0));
                _.set(serviceEditorOpts, 'tabs_container', _.get(this.options, 'tabs_container'));
                _.set(serviceEditorOpts, 'file', self._file);
                _.set(serviceEditorOpts, 'application', self.app);
                var toolEditor = new ToolEditor.Views.ToolEditor(serviceEditorOpts);
                this._fileEditor = toolEditor;
                this.getHeader().addClass('inverse');

                toolEditor.on("content-modified", function(){
                    var updatedContent = toolEditor.getContent();
                    this._file.setContent(updatedContent);
                    this._file.setDirty(true);
                    this._file.save();
                    this.app.workspaceManager.updateMenuItems();
                    var activeTab = this.app.tabController.getActiveTab();
                    var file = activeTab.getFile();
                    if(file.getRunStatus() || file.getDebugStatus()){
                        var launcher = activeTab.getSiddhiFileEditor().getLauncher();
                        launcher.stopApplication(this.app.workspaceManager, false);
                    }
                    this.trigger("tab-content-modified");
                }, this);

                toolEditor.on("dispatch-command", function (id) {
                    this.app.commandManager.dispatch(id);
                }, this);

                this._file.on("dirty-state-change", function () {
                    this.app.workspaceManager.updateSaveMenuItem();
                    this.app.workspaceManager.updateExportMenuItem();
                    this.updateHeader();
                }, this);

                toolEditor.render();
                this.app.workspaceManager.updateUndoRedoMenus();

                // bind app commands to source editor commands
                this.app.commandManager.getCommands().forEach(function(command){
                    toolEditor.getSourceView().bindCommand(command);
                });
            },

            getSiddhiFileEditor: function () {
                return this._fileEditor;
            }
        });

        return ServiceTab;
    });