/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['require', 'log', 'jquery', 'lodash', 'backbone', 'menu_bar', 'tool_bar', 'command', 'workspace',
        'app/tab/service-tab-list', 'event_simulator', 'app/output-console/service-console-list-manager',
        'nano_scroller','guide','workspace/file', 'operator_finder', 'template_deploy', 'utils'],

    function (require, log, $, _, Backbone, MenuBar, ToolBar, CommandManager, Workspace, TabController,
              EventSimulator, OutputController,NanoScroller, Guide, File, OperatorFinder, TemplateDeploy ,Utils) {

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
                    var pathSeparator;
                    this.initComponents();
                    $(".nano").nanoScroller();
                    $( "#service-tabs-wrapper" ).on( "resize", function( event, ui ) {
                        if(self.tabController.activeTab._title != "welcome-page"){
                            if (self.tabController.activeTab.getSiddhiFileEditor().isInSourceView()) {
                                self.tabController.activeTab.getSiddhiFileEditor().getSourceView().editorResize();
                            }
                        }
                    });
                },

                initComponents: function(){

                    // init command manager
                    this.commandManager = new CommandManager(this);

                    this.utils = new Utils();

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

                    //init tab controller
                    var tabControlOpts = _.get(this.config, "tab_controller");
                    _.set(tabControlOpts, 'application', this);

                    var outputConsoleControlOpts = _.get(this.config, "output_controller");
                    _.set(outputConsoleControlOpts, 'application', this);
                    this.outputController = new OutputController(outputConsoleControlOpts);

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

                    //init Hint Guide
                    this.guide = new Guide(this);

                    // Initialize operator finder.
                    var operatorFinderOpts = _.get(this.config, 'operator_finder');
                    _.set(operatorFinderOpts, 'application', this);
                    this.operatorFinder = new OperatorFinder.OperatorFinder(operatorFinderOpts);

                    var templateDeploymentOpts = _.get(this.config, 'template_deploy');
                    _.set(templateDeploymentOpts, 'application', this);
                    this.templateDeploy = new TemplateDeploy.TemplateDeploy(templateDeploymentOpts);
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

                    log.debug("start: rendering operator finder");
                    this.operatorFinder.render();
                    log.debug("end: rendering operator finder");

                    log.debug("start: rendering template deploy feature");
                    this.templateDeploy.render();
                    log.debug("end: rendering template deploy feature");
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

                runInitialGuide: function (){
                    var isFreshUser = (this.browserStorage.get('guideFileNameIncrement') === null);
                    if(isFreshUser) {
                        this.guide.startGuide();
                        this.browserStorage.put("guideFileNameIncrement", 1);
                    }
                },

                isRunningOnMacOS: function(){
                    return _.isEqual(this.getOperatingSystem(), 'MacOS');
                },

                getPathSeperator: function(){
                    if(this.pathSeparator != undefined) {
                        return this.pathSeparator;
                    } else {
                        this.pathSeparator = this.workspaceExplorer._serviceClient.readPathSeparator();
                        return this.pathSeparator;
                    }
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