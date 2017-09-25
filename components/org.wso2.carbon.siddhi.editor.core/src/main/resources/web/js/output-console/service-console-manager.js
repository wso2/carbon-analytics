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
define(['require', 'log', 'jquery', 'lodash', 'console','workspace','toolEditor'],
    function (require, log, jquery, _, Console, Workspace,ToolEditor) {
        var  ServiceConsole;

        ServiceConsole = Console.extend({
            initialize: function (options) {
                Console.prototype.initialize.call(this, options);
                this.app = options.application;
            },
            getFile: function(){
                return this._file;
            },

            getTitle: function(){
                return this._title;
            },

            getContentContainer: function(){
                return this.$el;
            },

            updateHeader: function(){
//                if (this._file.isDirty()) {
//                    this.getHeader().setText('* ' + this.getTitle());
//                } else {
//                    this.getHeader().setText(this.getTitle());
//                }
            },

//            render: function () {
//                var self = this;
//                Console.prototype.render.call(this);
//                var getContentContainer = this.getContentContainer();
//                getContentContainer.append('<span class="ERROR">Started<span>');
//                getContentContainer.append("<br />");
//
//                getContentContainer.scrollTop(100000);
//                this.getHeader().addClass('inverse');
////                var serviceEditorOpts = _.get(this.options, 'das_editor');
////                _.set(serviceEditorOpts, 'toolPalette', this.getParent().options.toolPalette);
////                _.set(serviceEditorOpts, 'container', this.$el.get(0));
////                _.set(serviceEditorOpts, 'tabs_container', _.get(this.options, 'tabs_container'));
////                _.set(serviceEditorOpts, 'file', self._file);
////                _.set(serviceEditorOpts, 'application', self.app);
////                var toolEditor = new ToolEditor.Views.ToolEditor(serviceEditorOpts);
////                this._fileEditor = toolEditor;
//
//
////                toolEditor.on("content-modified", function(){
////                    var updatedContent = toolEditor.getContent();
////                    this._file.setContent(updatedContent);
////                    this._file.setDirty(true);
////                    this._file.save();
////                    this.app.workspaceManager.updateMenuItems();
////                    this.trigger("tab-content-modified");
////                }, this);
//
////                toolEditor.on("dispatch-command", function (id) {
////                    this.app.commandManager.dispatch(id);
////                }, this);
//
////                this._file.on("dirty-state-change", function () {
////                    this.app.workspaceManager.updateSaveMenuItem();
////                    this.app.workspaceManager.updateExportMenuItem();
////                    this.updateHeader();
////                }, this);
////
////                toolEditor.render();
////
////                // bind app commands to source editor commands
////                this.app.commandManager.getCommands().forEach(function(command){
////                    toolEditor.getSourceView().bindCommand(command);
////                });
//            },
            showInitialStartingMessage: function(message){
                this.$el.append('<span class="INFO">' + message + '<span>');
                this.$el.append("<br />");
                this.$el.scrollTop(100000);
            },

            println: function(message){
                this.$el.append('<span class="' + message.type + '">' + message.message + '<span>');
                this.$el.append("<br />");
                this.$el.scrollTop(100000);
                $(".nano").nanoScroller();
            },
            addRunningPlan: function(executionPlan){
                this.addRunningPlanToList(executionPlan);
            }

        });

        return ServiceConsole;
    });