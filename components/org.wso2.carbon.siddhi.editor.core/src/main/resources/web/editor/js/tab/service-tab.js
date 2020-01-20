/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', './tab','workspace','toolEditor','workspace/file', 'operator_finder'],
    function (require, log, jquery, _, Tab, Workspace, ToolEditor, File, OperatorFinder) {
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

                toolEditor.on("view-switch", function (e) {
                    this.app.workspaceManager.updateMenuItems();
                    OperatorFinder.Utils.toggleAddToSource(e.view === 'design');
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