/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'jquery', 'backbone', 'lodash', 'context_menu', 'launch_manager', 'alerts'],

    function (log, $, Backbone, _, ContextMenu, LaunchManager, alerts) {

        var Launcher = Backbone.View.extend({

            initialize: function (config) {
                var errMsg;
                this._items = [];
                this.application = _.get(config, 'application');
            },

            debugApplication: function (workspace, async) {
                var activeTab = this.application.tabController.getActiveTab();
                if (this.isReadyToRun(activeTab)) {
                    var siddhiAppName = "";
                    if(activeTab.getTitle().lastIndexOf(".siddhi") != -1){
                        siddhiAppName = activeTab.getTitle().substring(0, activeTab.getTitle().lastIndexOf(".siddhi"));
                    } else{
                        siddhiAppName = activeTab.getTitle();
                    }

                    var debuggerWrapperInstance = activeTab.getSiddhiFileEditor().getDebuggerWrapper();
                    debuggerWrapperInstance.setAppName(siddhiAppName);
                    LaunchManager.debugApplication(siddhiAppName, this.application.outputController, activeTab.cid,
                        debuggerWrapperInstance, activeTab, workspace, async);
                    var options = {
                        siddhiAppName: siddhiAppName,
                        status: "DEBUG"
                    };
                    this.application.commandManager.dispatch('change-app-status-single-simulation', options);
                    return true;
                } else {
                    alerts.error("Save file before start debugging application");
                    return false;
                }
            },

            stopApplication: function(workspace,initialLoad){
                var activeTab = this.application.tabController.getActiveTab();

                if (undefined == activeTab.getFile().isStopProcessRunning() ||
                    !activeTab.getFile().isStopProcessRunning()) {
                    activeTab.getFile().setStopProcessRunning(true);
                    var siddhiAppName = "";
                    if(activeTab.getTitle().lastIndexOf(".siddhi") != -1){
                        siddhiAppName = activeTab.getTitle().substring(0, activeTab.getTitle().lastIndexOf(".siddhi"));
                    } else{
                        siddhiAppName = activeTab.getTitle();
                    }
                    this.application.commandManager.dispatch('stop-running-simulation-on-app-stop', siddhiAppName);
                    LaunchManager.stopApplication(siddhiAppName,this.application.outputController,activeTab,
                        workspace,initialLoad);
                    var options = {
                        siddhiAppName: siddhiAppName,
                        status: "STOP"
                    };
                    this.application.commandManager.dispatch('change-app-status-single-simulation', options);
                }

            },

            runApplication: function (workspace, async) {
                var activeTab = this.application.tabController.getActiveTab();
                // only saved files can be run as application
                if (this.isReadyToRun(activeTab)) {
                    var siddhiAppName = "";
                    if(activeTab.getTitle().lastIndexOf(".siddhi") != -1){
                        siddhiAppName = activeTab.getTitle().substring(0, activeTab.getTitle().lastIndexOf(".siddhi"));
                    } else{
                        siddhiAppName = activeTab.getTitle();
                    }
                    LaunchManager.runApplication(siddhiAppName, this.application.outputController, activeTab, workspace,
                        async);
                    var options = {
                        siddhiAppName: siddhiAppName,
                        status: "RUN"
                    };
                    this.application.commandManager.dispatch('change-app-status-single-simulation', options);
                    return true;
                } else {
                    alerts.error("Save file before running application");
                    return false;
                }
            },

            isReadyToRun: function(tab) {
                if (!typeof tab.getFile === "function") {
                    return false;
                }

                var file = tab.getFile();
                // file is not saved give an error and avoid running
                if(file.isDirty()) {
                    return false;
                }

                return true;
            },

            stopProgram: function(){
                LaunchManager.stopProgram();
            },

            isActive: function(){
                return this._activateBtn.parent('li').hasClass('active');
            },

            render: function () {
                var self = this;
                var activateBtn = $(_.get(this._options, 'activateBtn'));
                this._activateBtn = activateBtn;

                var launcherContainer = $('<div role="tabpanel"></div>');
                launcherContainer.addClass(_.get(this._options, 'cssClass.container'));
                launcherContainer.attr('id', _.get(this._options, ('containerId')));
                this._$parent_el.append(launcherContainer);

                activateBtn.on('click', function(e){
                    $(this).tooltip('hide');
                    e.preventDefault();
                    e.stopPropagation();
                    self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
                });

                activateBtn.attr("data-placement", "bottom").attr("data-container", "body");

                if (this.application.isRunningOnMacOS()) {
                    activateBtn.attr("title", "Run (" + _.get(self._options, 'command.shortcuts.mac.label') + ") ").tooltip();
                } else {
                    activateBtn.attr("title", "Run  (" + _.get(self._options, 'command.shortcuts.other.label') + ") ").tooltip();
                }

                this._verticalSeparator.on('drag', function(event){
                    if( event.originalEvent.clientX >= _.get(self._options, 'resizeLimits.minX')
                        && event.originalEvent.clientX <= _.get(self._options, 'resizeLimits.maxX')){
                        self._verticalSeparator.css('left', event.originalEvent.clientX);
                        self._verticalSeparator.css('cursor', 'ew-resize');
                        var newWidth = event.originalEvent.clientX;
                        self._$parent_el.parent().width(newWidth);
                        self._containerToAdjust.css('padding-left', event.originalEvent.clientX);
                        self._lastWidth = newWidth;
                        self._isActive = true;
                    }
                    event.preventDefault();
                    event.stopPropagation();
                });
                this._launcherContainer = launcherContainer;

                if(!_.isEmpty(this._openedFolders)){
                    this._openedFolders.forEach(function(folder){
                        self.createExplorerItem(folder);
                    });
                }
                this.renderBody();
                return this;
            },


            renderBody : function(){
                this._launcherContainer.html(this.compiled(LaunchManager));
            },

            showConsole : function(){
                $("#tab-content-wrapper").css("height:70%");
                $("#console-container").css("height:30%");
            }
        });

        return Launcher;

    });

