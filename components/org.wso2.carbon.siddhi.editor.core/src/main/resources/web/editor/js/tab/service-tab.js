/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', './tab','workspace','toolEditor','workspace/file', 'operator_finder',
        'etlWizard'],
    function (require, log, jquery, _, Tab, Workspace, ToolEditor, File, OperatorFinder, ETLWizard) {
        var  ServiceTab;

        ServiceTab = Tab.extend({
            initialize: function (options) {
                Tab.prototype.initialize.call(this, options);
                if(!_.has(options, 'file')){
                    if (options.view !== undefined && this.options.view == this.options.application.utils.
                        getGlobalConstnts().VIEW_ETL_FLOW_WIZARD) {
                        this._file = new File({isTemp: true, isDirty: false, view: options.view},
                            {storage: this.getParent().getBrowserStorage()});
                    } else {
                        this._file = new File({isTemp: true, isDirty: false}, {storage: this.getParent().getBrowserStorage()});
                    }
                } else {
                    this._file = _.get(options, 'file');
                }
                this.app = options.application;
                this.options = options;
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
                var initOpts = _.get(this.options, 'editor');
                _.set(initOpts, 'container', this.$el.get(0));
                _.set(initOpts, 'tabs_container', _.get(this.options, 'tabs_container'));
                _.set(initOpts, 'file', self._file);
                _.set(initOpts, 'application', self.app);
                _.set(initOpts, 'isETLTask', _.get(this.options, 'isETLTask') | false);

                if (this.getFile().getView() === this.app.utils.getGlobalConstnts().VIEW_ETL_FLOW_WIZARD) {
                    var etlWizard = new ETLWizard(initOpts);
                    etlWizard.render();
                } else if (this.getFile().getView() === undefined) {
                    var toolEditor = new ToolEditor.Views.ToolEditor(initOpts);
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
                }

            },

            getSiddhiFileEditor: function () {
                return this._fileEditor;
            }
        });

        return ServiceTab;
    });