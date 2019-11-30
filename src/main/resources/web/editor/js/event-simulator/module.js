/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['jquery', 'backbone', 'lodash', 'log', 'dialogs', './simulator', './feed_simulator', './simulator-rest-client'],
    function ($, Backbone, _, log, Dialogs, singleEventSimulator, feedSimulator, Simulator) {
        var EventSimulator = Backbone.View.extend({
            initialize: function (config) {
                var errMsg;
                var self = this;
                this._breakPoints = {};
                if (!_.has(config, 'container')) {
                    errMsg = 'unable to find configuration for container';
                    log.error(errMsg);
                    throw errMsg;
                }
                var container = $(_.get(config, 'container'));
                // check whether container element exists in dom
                if (!container.length > 0) {
                    errMsg = 'unable to find container for event-simulator with selector: ' + _.get(config, 'container');
                    log.error(errMsg);
                    throw errMsg;
                }
                this._$parent_el = container;

                if (!_.has(config, 'application')) {
                    log.error('Cannot init event-simulator. config: application not found.')
                }

                this.application = _.get(config, 'application');
                this.launchManager = _.get(config, 'launchManager');
                this._options = config;
                this.eventSimulatorServiceUrl = _.get(this._options, 'application.config.services.debugger.endpoint');
                this._lastWidth = undefined;
                this._verticalSeparator = $(_.get(this._options, 'separator'));
                this._containerToAdjust = $(_.get(this._options, 'containerToAdjust'));

                // register command
                this.application.commandManager.registerCommand(config.command.id, {shortcuts: config.command.shortcuts});
                this.application.commandManager.registerHandler(config.command.id, this.toggleEventSimulator, this);
                this.application.commandManager.registerHandler(config.commandAddSingleSimulatorForm.id,
                    this.addSingleEventConfigForm, this);
                this.application.commandManager.registerHandler("change-app-status-single-simulation",
                    this.changeSiddhiAppStatusInSingleSimulation, this);
                this.application.commandManager.registerHandler("stop-running-simulation-on-app-stop",
                    this.stopRunningSimulationOnSiddhiAppStop, this);
                this.application.commandManager.registerHandler("remove-unwanted-streams-single-simulation",
                    this.changeSiddhiAppsAndStreamOptionsInSingleSimulationOnSave, this);
                this.application.commandManager.registerHandler("remove-siddhi-apps-on-delete",
                    this.removeSiddhiAppsOnDelete, this);

            },
            isActive: function () {
                return this._activateBtn.parent('li').hasClass('active');
            },
            toggleEventSimulator: function () {
                if (this.isActive()) {
                    this._$parent_el.parent().width('0px');
                    this._containerToAdjust.css('padding-left', _.get(this._options, 'leftOffset'));
                    this._verticalSeparator.css('left', _.get(this._options, 'leftOffset') -
                        _.get(this._options, 'separatorOffset'));
                    this._activateBtn.parent('li').removeClass('active');

                } else {
                    this._activateBtn.tab('show');
                    var width = this._lastWidth || _.get(this._options, 'defaultWidth');
                    this._$parent_el.parent().width(width);
                    this._containerToAdjust.css('padding-left', width);
                    this._verticalSeparator.css('left', width - _.get(this._options, 'separatorOffset'));
                    feedSimulator.updateFeedCreationButtonAndNotification();
                }
            },
            getFeedSimulator: function () {
                return feedSimulator;
            },

            render: function () {
                var self = this;
                var activateBtn = $(_.get(this._options, 'activateBtn'));
                this._activateBtn = activateBtn;
                this.renderContent();
                activateBtn.on('show.bs.tab', function (e) {
                    self._isActive = true;
                    var width = self._lastWidth || _.get(self._options, 'defaultWidth');
                    self._$parent_el.parent().width(width);
                    self._containerToAdjust.css('padding-left', width + _.get(self._options, 'leftOffset'));
                    self._verticalSeparator.css('left', width + _.get(self._options, 'leftOffset') -
                        _.get(self._options, 'separatorOffset'));
                });

                activateBtn.on('hide.bs.tab', function (e) {
                    self._isActive = false;
                });

                activateBtn.on('click', function (e) {
                    //$(this).tooltip('hide');
                    if ($(this).hasClass("disabled")) {
                        return false;
                    }
                    ;
                    e.preventDefault();
                    e.stopPropagation();
                    self.application.commandManager.dispatch(_.get(self._options, 'command.id'));
                    if (self.application.tabController.activeTab._title != "welcome-page") {
                        if (self.application.tabController.activeTab.getSiddhiFileEditor().isInSourceView()) {
                            self.application.tabController.activeTab.getSiddhiFileEditor().getSourceView().editorResize();
                        }
                    }
                });

                activateBtn.attr("data-placement", "right").attr("data-container", "body");

                if (this.application.isRunningOnMacOS()) {
                    activateBtn.attr("title", "Event Simulator (" + _.get(self._options,
                            'command.shortcuts.mac.label') + ") ").tooltip();
                } else {
                    activateBtn.attr("title", "Event Simulator  (" + _.get(self._options,
                            'command.shortcuts.other.label') + ") ").tooltip();
                }

                this._verticalSeparator.on('drag', function (event) {
                    if (event.originalEvent.clientX >= _.get(self._options, 'resizeLimits.minX')
                        && event.originalEvent.clientX <= _.get(self._options, 'resizeLimits.maxX')) {
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

                return this;

            },

            renderContent: function () {
                var eventSimulatorContainer = $('#simulation-index').clone();
                eventSimulatorContainer.addClass(_.get(this._options, 'cssClass.container'));
                eventSimulatorContainer.attr('id', _.get(this._options, ('containerId')));
                this._$parent_el.append(eventSimulatorContainer);
                singleEventSimulator.init(this._options);
                feedSimulator.init(this._options);
                this._eventSimulatorContainer = eventSimulatorContainer;
            },

            addSingleEventConfigForm: function () {
                singleEventSimulator.addSingleEventConfigForm(null, $('#add-single-event-form'));
            },

            stopRunningSimulations: function () {
                feedSimulator.stopRunningSimulationOnStartup();
            },

            changeSiddhiAppStatusInSingleSimulation: function (options) {
                singleEventSimulator.changeSiddhiAppStatusInSingleSimulation(options.siddhiAppName, options.status);
            },

            stopRunningSimulationOnSiddhiAppStop: function (siddhiAppName) {
                feedSimulator.stopRunningSimulationOnSiddhiAppStop(siddhiAppName);
            },

            changeSiddhiAppsAndStreamOptionsInSingleSimulationOnSave: function (siddhiAppName) {
                singleEventSimulator.changeSiddhiAppsAndStreamOptionsInSingleSimulationOnSave(siddhiAppName, 12);
            },

            removeSiddhiAppsOnDelete: function (siddhiAppName) {
                singleEventSimulator.changeSiddhiAppsAndStreamOptionsInSingleSimulationOnDelete(siddhiAppName, 12);
            }

        });

        return EventSimulator;
    });

