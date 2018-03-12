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

define(['require', 'log', 'jquery', 'lodash', 'backbone', 'menu_bar', 'tool_bar', 'command', 'workspace', 
        'app/tab/service-tab-list', 'app/tool-palette/tool-palette', 'event_simulator', 
        'app/output-console/service-console-list-manager', 'nano_scroller'],

    function (require, log, $, _, Backbone, MenuBar, ToolBar, CommandManager, Workspace, TabController, ToolPalette, 
              EventSimulator, OutputController) {

        var Application = Backbone.View.extend(
            /** @lends Application.prototype */
            {
                /**
                 * @augments Backbone.View
                 * @constructs
                 * @class Application wraps all the application logic and it is the main starting point.
                 * @param {Object} config configuration options for the application
                 */
                initialize: function (config) {
                    this.config = config;
                    var self = this;
                    this.initComponents();
                    $(".nano").nanoScroller();
                    $( "#service-tabs-wrapper" ).on( "resize", function( event, ui ) {
                          if(self.tabController.activeTab._title != "welcome-page"){
                              if (self.tabController.activeTab.getSiddhiFileEditor().isInSourceView()) {
                                  self.tabController.activeTab.getSiddhiFileEditor().getSourceView().editorResize();
                              } else {
                                  self.tabController.activeTab.getSiddhiFileEditor().getEventFlow().graphResize();
                              }
                          }
                    } );
                },

                initComponents: function(){

                    // init command manager
                    this.commandManager = new CommandManager(this);

                    //init menu bar
                    var menuBarOpts = _.get(this.config, "menu_bar");
                    _.set(menuBarOpts, 'application', this);
                    this.menuBar = new MenuBar(menuBarOpts);

                    //init tool bar
                    var toolBarOpts = _.get(this.config, "tool_bar");
                    _.set(toolBarOpts, 'application', this);
                    this.toolBar = new ToolBar(toolBarOpts);

                    //init workspace manager
                    this.workspaceManager = new Workspace.Manager(this);

                    this.browserStorage = new Workspace.BrowserStorage('spToolingTempStorage');

                    //init tool palette
                    var toolPaletteOpts = _.get(this.config, "tab_controller.tool_palette");
                    _.set(toolPaletteOpts, 'application', this);


                    this.toolPalette = new ToolPalette(toolPaletteOpts);

                    //init tab controller
                    var tabControlOpts = _.get(this.config, "tab_controller");
                    _.set(tabControlOpts, 'application', this);

                    var outputConsoleControlOpts = _.get(this.config, "output_controller");
                    _.set(outputConsoleControlOpts, 'application', this);
                    this.outputController = new OutputController(outputConsoleControlOpts);

                    // tab controller will take care of rendering tool palette
                    _.set(tabControlOpts, 'toolPalette', this.toolPalette);
                    this.tabController = new TabController(tabControlOpts);
                    this.workspaceManager.listenToTabController();

                    //init workspace explorer
                    var workspaceExplorerOpts = _.get(this.config, "workspace_explorer");
                    _.set(workspaceExplorerOpts, 'application', this);
                    this.workspaceExplorer = new Workspace.Explorer(workspaceExplorerOpts);

                    var eventSimulatorOpts = _.get(this.config, "event_simulator");
                    _.set(eventSimulatorOpts, 'application', this);
                    this.eventSimulator = new EventSimulator(eventSimulatorOpts);
                    this.eventSimulator.stopRunningSimulations();
                },

                render: function () {
                    log.debug("start: rendering menu_bar control");
                    this.menuBar.render();
                    log.debug("end: rendering menu_bar control");

                    log.debug("start: rendering workspace explorer control");
                    this.workspaceExplorer.render();
                    log.debug("end: rendering workspace explorer control");

                    log.debug("start: rendering output consoles");
                    this.outputController.render();
                    log.debug("end: rendering output consoles");

                    log.debug("start: rendering tab controller");
                    this.tabController.render();
                    log.debug("end: rendering tab controller");

                    log.debug("start: rendering event simulator control");
                    this.eventSimulator.render();
                    log.debug("end: rendering event simulator control");
                },

                getOperatingSystem: function(){
                    var operatingSystem = "Unknown OS";
                    if (navigator.appVersion.indexOf("Win") != -1) {
                        operatingSystem = "Windows";
                    }
                    else if (navigator.appVersion.indexOf("Mac") != -1) {
                        operatingSystem = "MacOS";
                    }
                    else if (navigator.appVersion.indexOf("X11") != -1) {
                        operatingSystem = "UNIX";
                    }
                    else if (navigator.appVersion.indexOf("Linux") != -1) {
                        operatingSystem = "Linux";
                    }
                    return operatingSystem;
                },

                displayInitialView: function () {
                    this.workspaceManager.displayInitialTab();
                },

                isRunningOnMacOS: function(){
                    return _.isEqual(this.getOperatingSystem(), 'MacOS');
                },

                getPathSeperator: function(){
                    return _.isEqual(this.getOperatingSystem(), 'Windows') ? '\\' : '/' ;
                },

                applicationConstants: function() {
                    var constants = {
                        messageLinkType: {
                            OutOnly : 1,
                            InOut : 2
                        }
                    };

                    return constants;
                }

            });

        return Application;
    });