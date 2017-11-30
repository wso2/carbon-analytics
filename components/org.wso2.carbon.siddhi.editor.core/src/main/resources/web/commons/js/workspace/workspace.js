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
define(['ace/ace', 'jquery', 'lodash', 'log','dialogs','./service-client','welcome-page'],
    function (ace, $, _,log,Dialogs,ServiceClient,WelcomePages) {

        // workspace manager constructor
        /**
         * Arg: application instance
         */
        return function (app) {
            var self = this;

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
                var tab;
                if(app.tabController !== undefined){
                    tab = app.tabController.getTabFromTitle((evt.newActiveTab._title).split(".")[0]);
                }
                if(evt.newActiveTab._title != "welcome-page" && evt.newActiveTab._title != "untitled"){
                    if(tab.getSiddhiFileEditor() !== undefined){
                        tab.getSiddhiFileEditor().getSourceView().editorResize();
                    }
                }
                this.manageConsoles(evt);
            };

            this.createNewTab = function createNewTab(options) {
                var editorId = app.config.container;
                $(editorId).css("display", "block");
                //Showing menu bar
                app.tabController.newTab(options);
                app.outputController.makeInactiveActivateButton();
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
                var filePath = prompt("Enter a directory (absolute path) to save the siddhi app : ");
                filePath = (filePath.slice(-1) === '/') ? filePath + filename : filePath + '/' + filename;
                $.ajax({
                    type: "POST",
                    url: window.location.protocol + "//" + window.location.host + "/editor/save",
                    data: JSON.stringify({
                        siddhiApp: code,
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

            this.handleSave = function() {
                var activeTab = app.tabController.getActiveTab();
                var file = undefined;
                var siddhiFileEditor;
                var config = "";
                var providedAppName = "";
                var fileName = "";
                var options = {};

                if(activeTab.getTitle() != "welcome-page"){
                    file = activeTab.getFile();
                }

                if(file !== undefined){
                    if(file.isPersisted()){
                        if(file.isDirty()){
                            var activeTab = app.tabController.activeTab;
                            var siddhiFileEditor= activeTab.getSiddhiFileEditor();
                            config = siddhiFileEditor.getContent();
                            var response = self._serviceClient.writeFile(file,config);
                            if(response.error){
                                alerts.error(response.message);
                                self.updateRunMenuItem();
                                return;
                            }
                            if(file.getRunStatus() || file.getDebugStatus()){
                                var launcher = activeTab.getSiddhiFileEditor().getLauncher();
                                launcher.stopApplication(self, false);
                            }
                        }
                        if(!_.isNil(options) && _.isFunction(options.callback)){
                            options.callback(true);
                        }
                    } else {
                        app.commandManager.dispatch('open-file-save-dialog', options);
                    }
                }
                self.updateRunMenuItem();
            };

            this.handleUndo = function() {

                // undo manager for current tab
                var undoManager = app.tabController.getActiveTab().getSiddhiFileEditor().getUndoManager();
                if (undoManager.hasUndo()) {
                    undoManager.undo();
                }
                self.updateUndoRedoMenus();
            };

            this.handleRedo = function() {
                // undo manager for current tab
                var undoManager = app.tabController.getActiveTab().getSiddhiFileEditor().getUndoManager();
                if (undoManager.hasRedo()) {
                    undoManager.redo();
                }
                self.updateUndoRedoMenus();
            };

            this.handleFind = function() {
                var activeTab = app.tabController.getActiveTab();

                if(activeTab.getTitle() != "welcome-page"){
                    var aceEditor = app.tabController.getActiveTab().getSiddhiFileEditor().getSourceView().getEditor();
                    aceEditor.execCommand("find");
                }
            };

            this.handleRun = function(options) {
                var launcher = app.tabController.getActiveTab().getSiddhiFileEditor().getLauncher();
                launcher.runApplication(self);
            };

            this.handleStop = function(options) {
                var launcher = app.tabController.getActiveTab().getSiddhiFileEditor().getLauncher();
                if(options === undefined){
                    launcher.stopApplication(self, false);
                } else{
                    launcher.stopApplication(self, options.initialLoad);
                }
            };

            this.handleDebug = function(options) {
                var launcher = app.tabController.getActiveTab().getSiddhiFileEditor().getLauncher();
                launcher.debugApplication(self);
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
                this.updateUndoRedoMenus();
                this.updateSaveMenuItem();
                this.updateExportMenuItem();
                this.updateRunMenuItem();
                this.updateSettingsMenuItem();
                //this.updateCodeFormatMenu();
            };

            this.manageConsoles = function(evt){
                if(app.outputController !== undefined){
                    app.outputController.showConsoleByTitle(evt.newActiveTab._title,"DEBUG");
                    //app.outputController.toggleOutputConsole();
                }
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

            this.updateUndoRedoMenus = function(){
                // undo manager for current tab
                var activeTab = app.tabController.getActiveTab(),
                    undoMenuItem = app.menuBar.getMenuItemByID('edit.undo'),
                    redoMenuItem = app.menuBar.getMenuItemByID('edit.redo'),
                    file = undefined;

                if(activeTab.getTitle() != "welcome-page"){
                    file = activeTab.getFile();
                }

                if(file !== undefined){
                    var fileEditor = activeTab.getSiddhiFileEditor();
                    if(!_.isUndefined(fileEditor)){
                        var undoManager = fileEditor.getUndoManager();
                        if (undoManager.hasUndo() && undoManager.undoStackTop().canUndo()) {
                            undoMenuItem.enable();
                            undoMenuItem.addLabelSuffix(
                                undoManager.undoStackTop().getTitle());
                        } else {
                            undoMenuItem.disable();
                            undoMenuItem.clearLabelSuffix();
                        }
                        if (undoManager.hasRedo() && undoManager.redoStackTop().canRedo()) {
                            redoMenuItem.enable();
                            redoMenuItem.addLabelSuffix(
                            undoManager.redoStackTop().getTitle());
                        } else {
                            redoMenuItem.disable();
                            redoMenuItem.clearLabelSuffix();
                        }
                    }
                } else {
                    undoMenuItem.disable();
                    undoMenuItem.clearLabelSuffix();
                    redoMenuItem.disable();
                    redoMenuItem.clearLabelSuffix();
                }
            };

            this.updateSaveMenuItem = function(){
                var activeTab = app.tabController.getActiveTab(),
                    saveMenuItem = app.menuBar.getMenuItemByID('file.save'),
                    saveAsMenuItem = app.menuBar.getMenuItemByID('file.saveAs'),
                    deletesMenuItem = app.menuBar.getMenuItemByID('file.delete'),
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
                    deletesMenuItem.enable();
                } else {
                    saveMenuItem.disable();
                    saveAsMenuItem.disable();
                    deletesMenuItem.disable();
                }
            };

            this.updateSettingsMenuItem = function(){
                var activeTab = app.tabController.getActiveTab(),
                    settingMenuItem = app.menuBar.getMenuItemByID('file.settings'),
                    file = undefined;

                if(activeTab.getTitle() != "welcome-page"){
                    file = activeTab.getFile();
                }

                if(file !== undefined){
                    settingMenuItem.enable();
                } else {
                    settingMenuItem.disable();
                }
            };

            this.updateRunMenuItem = function(){
                var activeTab = app.tabController.getActiveTab(),
                    runMenuItem = app.menuBar.getMenuItemByID('run.run'),
                    debugMenuItem = app.menuBar.getMenuItemByID('run.debug'),
                    stopMenuItem = app.menuBar.getMenuItemByID('run.stop'),
                    file = undefined;

                var toolBar = app.toolBar;

                if(activeTab.getTitle() != "welcome-page" && activeTab.getTitle() != "untitled"){
                    file = activeTab.getFile();
                }

                if(file !== undefined){
                    file = activeTab.getFile();
                    if(file.isDirty()){
                        runMenuItem.disable();
                        debugMenuItem.disable();
                        stopMenuItem.disable();
                        toolBar.disableRunButton();
                        toolBar.disableDebugButton();
                        toolBar.disableStopButton();
                    } else {
                        if(activeTab.getFile().getRunStatus() || activeTab.getFile().getDebugStatus()){
                            runMenuItem.disable();
                            debugMenuItem.disable();
                            stopMenuItem.enable();
                            toolBar.disableRunButton();
                            toolBar.disableDebugButton();
                            toolBar.enableStopButton();
                        } else if(!activeTab.getFile().getRunStatus()){
                            if(!activeTab.getFile().getDebugStatus()){
                                runMenuItem.enable();
                                debugMenuItem.enable();
                                stopMenuItem.disable();
                                toolBar.enableRunButton();
                                toolBar.enableDebugButton();
                                toolBar.disableStopButton();
                            } else{
                                stopMenuItem.enable();
                                toolBar.enableStopButton();
                            }
                        } else if(!activeTab.getFile().getDebugStatus()){
                            if(!activeTab.getFile().getRunStatus()){
                                runMenuItem.enable();
                                debugMenuItem.enable();
                                stopMenuItem.disable();
                                toolBar.enableRunButton();
                                toolBar.enableDebugButton();
                                toolBar.disableStopButton();
                            } else{
                                stopMenuItem.enable();
                                toolBar.enableStopButton();
                            }
                        }
                    }
                } else {
                    runMenuItem.disable();
                    debugMenuItem.disable();
                    stopMenuItem.disable();
                    toolBar.disableRunButton();
                    toolBar.disableDebugButton();
                    toolBar.disableStopButton();
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

            this.openDeleteFileConfirmDialog = function openDeleteFileConfirmDialog(options) {
                if(_.isNil(this._deleteFileDialog)){
                    this._deleteFileDialog = new Dialogs.DeleteConfirmDialog(app);
                }
                this._deleteFileDialog.render();

                if(!_.isNil(options) && _.isFunction(options.callback)){
                    var isSaved = false;
                    this._deleteFileDialog.once('save-completed', function(success){
                        isSaved = success;
                    }, this);
                    this._deleteFileDialog.once('unloaded', function(){
                        options.callback(isSaved);
                    }, this);
                }
                this._deleteFileDialog.show();
            };

            this.displayInitialTab = function () {
                // display first launch welcome page tab
                var tab = app.tabController.newTab({
                    tabModel: {},
                    tabOptions:{title: 'welcome-page'}
                });
                // Showing FirstLaunchWelcomePage instead of regularWelcomePage
                var opts = _.get(app.config, 'welcome');
                _.set(opts, 'application', app);
                _.set(opts, 'tab', tab);
                tab.getHeader().addClass('inverse')
                this.welcomePage = new WelcomePages.FirstLaunchWelcomePage(opts);
                this.welcomePage.render();
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
                if(!file.isDirty() && file.isPersisted()){
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

            this.openSettingsDialog = function openSettingsDialog(options){
                if(_.isNil(this._openFileDialog)){
                    var opts = _.cloneDeep(_.get(app.config, 'settings_dialog'));
                    _.set(opts, "application", app);
                    this._openSettingsDialog = new Dialogs.settings_dialog(opts);
                }
                this._openSettingsDialog.render();
                this._openSettingsDialog.show();
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

            app.commandManager.registerHandler('run', this.handleRun);

            app.commandManager.registerHandler('debug', this.handleDebug);

            app.commandManager.registerHandler('stop', this.handleStop, this);

            app.commandManager.registerHandler('undo', this.handleUndo);

            app.commandManager.registerHandler('redo', this.handleRedo);

            // Open settings dialog
            app.commandManager.registerHandler('open-settings-dialog', this.openSettingsDialog, this);

            // Delete file delete dialog
            app.commandManager.registerHandler('delete-file-delete-dialog', this.openDeleteFileConfirmDialog, this);


        }


    });

