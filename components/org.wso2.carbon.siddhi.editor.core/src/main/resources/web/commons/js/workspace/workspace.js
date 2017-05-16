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
define(['ace/ace', 'jquery', 'lodash', 'backbone', 'log','dialogs','./service-client','welcome-page'],
    function (ace, $, _, Backbone, log,Dialogs,ServiceClient,WelcomePages) {

        // workspace manager constructor
        /**
         * Arg: application instance
         */
        return function (app) {
            var self = this;
            const plan_regex = /@Plan:name\(['|"](.*?)['|"]\)/g;

            this._serviceClient = new ServiceClient({application: app});

            if (_.isUndefined(app.commandManager)) {
                var error = "CommandManager is not initialized.";
                log.error(error);
                throw error;
            }

            this.getServiceClient = function(){
                return this._serviceClient;
            };

            this.listenToTabController = function(){
                app.tabController.on("active-tab-changed", this.onTabChange, this);
            };

            this.onTabChange = function(evt){
                this.updateMenuItems();
            };

            this.createNewTab = function createNewTab(options) {
                //var welcomeContainerId = app.config.welcome.container;
                //$(welcomeContainerId).css("display", "none");
                var editorId = app.config.container;
                $(editorId).css("display", "block");
                //Showing menu bar
                app.tabController.newTab(options);
            };

            this.saveFileBrowserBased = function saveFile() {
                var editor = ace.edit('siddhi-editor');
                var code = editor.getValue();
                var filename = "untitled";
                var match = plan_regex.exec(code);
                if (match && match[1]) {
                    filename = match[1].replace(/ /g, "_");
                }
                var blob = new Blob([code], {type: "text/plain;charset=utf-8"});
                saveAs(blob, filename + ".siddhi");
            };

            this.saveFile = function saveFile() {
                var editor = ace.edit('siddhi-editor');
                var code = editor.getValue();
                var filename = "untitled";
                var match = plan_regex.exec(code);
                if (match && match[1]) {
                    filename = match[1].replace(/ /g, "_") + ".siddhi";
                }
                var filePath = prompt("Enter a directory (absolute path) to save the execution plan : ");
                filePath = (filePath.slice(-1) === '/') ? filePath + filename : filePath + '/' + filename;
                $.ajax({
                    type: "POST",
                    url: "http://localhost:9090/editor/save",
                    data: JSON.stringify({
                        executionPlan: code,
                        filePath: filePath
                    }),
                    success: function (e) {
                        alert("file successfully saved.");
                    },
                    error: function (e) {
                        alert("failed to save file.");
                    }
                });
            };

            this.handleSave = function(options) {
                var activeTab = app.tabController.getActiveTab();
                var file = undefined;
                var siddhiFileEditor;
                var config = "";

                if(activeTab.getTitle() != "welcome-page"){
                    file = activeTab.getFile();
                    siddhiFileEditor = activeTab.getSiddhiFileEditor();
                    config = siddhiFileEditor.getContent();
                }

                if(file !== undefined){
                    if(file.isPersisted()){
                        if(file.isDirty()){
                            var response = self._serviceClient.writeFile(file,config);
                            if(response.error){
                                alerts.error(response.message);
                                return;
                            }
                        }
                        if(!_.isNil(options) && _.isFunction(options.callback)){
                            options.callback(true);
                        }
                    } else {
                        app.commandManager.dispatch('open-file-save-dialog', options);
                    }
                }
            };

            this.openReplaceFileConfirmDialog = function(options) {
                if(_.isNil(this._replaceFileConfirmDialog)){
                    this._replaceFileConfirmDialog = new Dialogs.ReplaceConfirmDialog();
                }
                // This dialog need to be re-rendered so that it comes on top of save file dialog.
                this._replaceFileConfirmDialog.render();

                this._replaceFileConfirmDialog.askConfirmation(options);
            };

            this.updateMenuItems = function(){
                //this.updateUndoRedoMenus();
                this.updateSaveMenuItem();
                this.updateExportMenuItem();
                //this.updateCodeFormatMenu();
            };

            this.updateExportMenuItem = function(){
                var activeTab = app.tabController.getActiveTab(),
                    exportMenuItem = app.menuBar.getMenuItemByID('file.export'),
                    file = undefined;

                if(activeTab.getTitle() != "welcome-page"){
                    file = activeTab.getFile();
                }

                if(file !== undefined){
                    file = activeTab.getFile();
                    if(file.isDirty()){
                        exportMenuItem.disable();
                    } else if(file.isPersisted()){
                        exportMenuItem.enable();
                    } else {
                        exportMenuItem.disable();
                    }
                } else {
                    exportMenuItem.disable();
                }
            };

            this.updateSaveMenuItem = function(){
                var activeTab = app.tabController.getActiveTab(),
                    saveMenuItem = app.menuBar.getMenuItemByID('file.save'),
                    saveAsMenuItem = app.menuBar.getMenuItemByID('file.saveAs'),
                    file = undefined;

                if(activeTab.getTitle() != "welcome-page"){
                    file = activeTab.getFile();
                }

                if(file !== undefined){
                    file = activeTab.getFile();
                    if(file.isDirty()){
                        saveMenuItem.enable();
                        saveAsMenuItem.enable();
                    } else {
                        saveMenuItem.disable();
                        saveAsMenuItem.enable();
                    }
                } else {
                    saveMenuItem.disable();
                    saveAsMenuItem.disable();
                }
            };

            this.openFileSaveDialog = function openFileSaveDialog(options) {
                if(_.isNil(this._saveFileDialog)){
                    this._saveFileDialog = new Dialogs.save_to_file_dialog(app);
                }
                this._saveFileDialog.render();

                if(!_.isNil(options) && _.isFunction(options.callback)){
                    var isSaved = false;
                    this._saveFileDialog.once('save-completed', function(success){
                        isSaved = success;
                    }, this);
                    this._saveFileDialog.once('unloaded', function(){
                        options.callback(isSaved);
                    }, this);
                }

                this._saveFileDialog.show();
                var activeTab = app.tabController.getActiveTab();
//                if(!_.isNil(activeTab) && _.isFunction(activeTab.getFile)){
//                    var activeFile = activeTab.getFile();
//                    if(activeFile.isPersisted()){
//                        this._saveFileDialog.once('loaded', function(){
//                            this._saveFileDialog.setSelectedFile(activeFile.getPath(), activeFile.getName());
//                        }, this);
//                    }
//                }

            };

            this.displayInitialTab = function () {
                //TODO : remove this if else condition
                // display first launch welcome page tab
                if (!this.passedFirstLaunch()) {
                    // create a generic tab - without ballerina editor components
                    var tab = app.tabController.newTab({
                        tabModel: {},
                        tabOptions:{title: 'welcome-page'}
                    });
                    var opts = _.get(app.config, 'welcome');
                    _.set(opts, 'application', app);
                    _.set(opts, 'tab', tab);
                    this.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                    this.welcomePage.render();
                } else {
                    // user has no active tabs from last session
                    if (!app.tabController.hasFilesInWorkingSet()) {
                        // create a generic tab - without ballerina editor components
                        var tab = app.tabController.newTab({
                            tabModel: {},
                            tabOptions:{title: 'welcome-page'}
                        });
                        // Showing FirstLaunchWelcomePage instead of regularWelcomePage
                        var opts = _.get(app.config, 'welcome');
                        _.set(opts, 'application', app);
                        _.set(opts, 'tab', tab);
                        this.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                        this.welcomePage.render();
                    }
                }
            };

            this.passedFirstLaunch = function(){
                return app.browserStorage.get("pref:passedFirstLaunch") || false;
            };

            this.importFileImportDialog = function importFileImportDialog() {
                if(_.isNil(this._importFileDialog)){
                    this._importFileDialog = new Dialogs.import_file_dialog(app);
                }
                this._importFileDialog.render();
                this._importFileDialog.show();
            };

            this.exportFileExportDialog = function exportFileExportDialog() {
                if(_.isNil(this._exportFileDialog)){
                    this._exportFileDialog = new Dialogs.export_file_dialog(app);
                }
                this._exportFileDialog.render();
                this._exportFileDialog.show();
            };

            this.handleExport = function(options) {
                var activeTab = app.tabController.getActiveTab();
                var file = activeTab.getFile();
                if(file.isPersisted()){
                    app.commandManager.dispatch('export-file-export-dialog', options);
                }
            };

            this.openFileOpenDialog = function openFileOpenDialog() {
                if(_.isNil(this._openFileDialog)){
                    this._openFileDialog = new Dialogs.open_file_dialog(app);
                }
                this._openFileDialog.render();
                this._openFileDialog.show();
            };

            this.openCloseFileConfirmDialog = function(options) {
                if(_.isNil(this._closeFileConfirmDialog)){
                    this._closeFileConfirmDialog = new Dialogs.CloseConfirmDialog();
                    this._closeFileConfirmDialog.render();
                }

                this._closeFileConfirmDialog.askConfirmation(options);
            };


            app.commandManager.registerHandler('create-new-tab', this.createNewTab);

            app.commandManager.registerHandler('save', this.handleSave);

            app.commandManager.registerHandler('export', this.handleExport, this);

            // Open file open dialog
            app.commandManager.registerHandler('open-file-open-dialog', this.openFileOpenDialog, this);

            // Open file save dialog
            app.commandManager.registerHandler('open-file-save-dialog', this.openFileSaveDialog, this);

            // Import file import dialog
            app.commandManager.registerHandler('import-file-import-dialog', this.importFileImportDialog, this);

            // Export file export dialog
            app.commandManager.registerHandler('export-file-export-dialog', this.exportFileExportDialog, this);

            app.commandManager.registerHandler('open-replace-file-confirm-dialog', this.openReplaceFileConfirmDialog, this);

            app.commandManager.registerHandler('open-close-file-confirm-dialog', this.openCloseFileConfirmDialog, this);


        }


    });

