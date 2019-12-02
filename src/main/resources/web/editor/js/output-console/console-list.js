/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'jquery', 'lodash', 'backbone', 'console'], function (log, $, _, Backbone, Console) {

    var ConsoleList = Backbone.View.extend(
        /** @lends ConsoleList.prototype */
        {
            initialize: function (options) {
                var errMsg;
                // check whether a custom Console type is set
                if (_.has(options, 'consoleModel')) {
                    this.ConsoleModel = _.get(options, 'consoleModel');
                    // check whether the custom type is of type Console
                    if (!this.ConsoleModel instanceof Console) {
                        errMsg = 'custom console model is not a sub type of Console: ' + Console;
                        log.error(errMsg);
                        throw errMsg;
                    }
                } else {
                    this.ConsoleModel = Console;
                }
                this._consoles = [];

                if (!_.has(options, 'container')) {
                    errMsg = 'unable to find configuration for container';
                    log.error(errMsg);
                    throw errMsg;
                }
                var container = $(_.get(options, 'container'));
                //container.on('click', '.closeConsole', _.bindKey(this, 'hide'));
                // check whether container element exists in dom
                if (!container.length > 0) {
                    errMsg = 'unable to find container for console list with selector: ' + _.get(options, 'container');
                    log.error(errMsg);
                    throw errMsg;
                }
                this._$parent_el = container;
                if (!_.has(options, 'consoles.container')) {
                    errMsg = 'unable to find configuration for container';
                    log.error(errMsg);
                    throw errMsg;
                }
                var consoleContainer = this._$parent_el.find(_.get(options, 'consoles.container'));
                // check whether container element exists in dom
                if (!consoleContainer.length > 0) {
                    errMsg = 'unable to find container for console list with selector: ' + _.get(options, 'consoles.container');
                    log.error(errMsg);
                    throw errMsg;
                }
                this._$console_container = consoleContainer;
                this.options = options;
            },

            render: function () {
                var consoleHeaderContainer = this._$parent_el.children(_.get(this.options, 'headers.container'));
                var consoleList = $('<ul></ul>');
                consoleHeaderContainer.append(consoleList);

                var consoleListClass = _.get(this.options, 'headers.cssClass.list');
                consoleList.addClass(consoleListClass);
                this._$consoleList = consoleList;
                this.el = consoleList.get();
            },

            hideConsoleComponents: function () {
                var self = this;
                var consoleHeaderContainer = self._$parent_el;
                var serviceWrapper =  $('#service-tabs-wrapper');
                consoleHeaderContainer.addClass('hide');
                serviceWrapper.css('height', '100%');
                if (serviceWrapper.is('.ui-resizable')){
                    serviceWrapper.resizable( 'destroy');
                    /*
                    * when resizable method in #service-tabs-wrapper's resizable method is destroyed
                    * it affects to it's child elements as well. So partitions in the design view also gets affected by
                    * this and partition's resize handles are also disappears. In order to add them back initialise them
                    * again.
                    * */
                    var partitions = $('.partitionDrop');
                    partitions.resizable( "destroy" );
                    partitions.resizable();
                }
                var activeTab = self.options.application.tabController.activeTab;
                if (activeTab !== undefined && activeTab.getTitle() != 'welcome-page') {
                    if (activeTab.getSiddhiFileEditor().isInSourceView()) {
                        activeTab.getSiddhiFileEditor().getSourceView().editorResize();
                    }
                }
            },

            showConsoleComponents: function () {
                var self = this;
                var consoleHeaderContainer = self._$parent_el;
                var serviceWrapper =  $('#service-tabs-wrapper');
                serviceWrapper.css('height', '65%');
                consoleHeaderContainer.removeClass('hide');
                serviceWrapper.resizable({
                    handleSelector: '.splitter-horizontal',
                    handles: "s",
                    resize: function( event, ui ) {
                    }
                });
                /*
                * when resizable method in #service-tabs-wrapper's resizable method is given a custom implementation
                * it affects to it's child elements as well. So partitions in the design view also gets affected by
                * this and partition's resize handles are also disappears. In order to add them back initialise them
                * again.
                * */
                $('.partitionDrop').resizable();
                var activeTab = self.options.application.tabController.activeTab;
                if (activeTab.getTitle() != 'welcome-page') {
                    if (activeTab.getSiddhiFileEditor().isInSourceView() !== undefined
                        && activeTab.getSiddhiFileEditor().isInSourceView()) {
                        activeTab.getSiddhiFileEditor().getSourceView().editorResize();
                    }
                }
            },

            showActiveDebugConsole: function () {

            },

            createHeaderForConsole: function (console) {
                var consoleHeader = $('<li></li>');
                this._$consoleList.append(consoleHeader);

                var consoleHeaderClass = _.get(this.options, 'headers.cssClass.item');
                consoleHeader.addClass(consoleHeaderClass);

                var consoleHeaderLink = $('<a></a>');
                consoleHeader.append(consoleHeaderLink);
                consoleHeader.link = consoleHeaderLink;
                consoleHeader.attr('href', '#' + console.cid);
                consoleHeaderLink.text(console.getTitle());

                consoleHeader.setText = function (text) {
                    consoleHeaderLink.text(text);
                };

                var self = this;
                consoleHeaderLink.click(function (e) {
                    consoleHeaderLink.tab('show');
                    self.setActiveConsole(console);
                    e.preventDefault();
                    e.stopPropagation();
                });

//                var consoleCloseBtn = $('<button type="button" >×</button>');
//                consoleHeader.append(consoleCloseBtn);
//                consoleCloseBtn.addClass( _.get(this.options, 'consoles.console.cssClass.console_close_btn'));
//                consoleCloseBtn.click(function(e){
//                    self.removeConsole(console);
//                    e.preventDefault();
//                    e.stopPropagation();
//                });

                console.on('title-changed', function (title) {
                    consoleHeaderLink.text(title);
                });
                console.setHeader(consoleHeader);
            },

            getConsoleContainer: function () {
                return this._$console_container;
            },
            /**
             * add a console to the console list.
             *
             * @param {Console} console an object of console
             * @fires ConsoleList#console-added
             */
            addConsole: function (console) {
                console.setParent(this);
                this.createHeaderForConsole(console);
                this._consoles.push(console);
                this.showConsoleComponents();
                /**
                 * console added event.
                 * @event ConsoleList#console-added
                 * @type {Console}
                 */
                this.trigger("console-added", console);
            },
            /**
             * gets Console
             * @param {string} consoleId id of the console
             * @returns {*}
             */
            getConsole: function (consoleId) {
                return _.find(this._consoles, ['id', consoleId]);
            },
            /**
             * removes a console
             * @param {Console} console the console instance
             * @fires ConsoleList#console-removed
             */
            removeConsole: function (console) {
                if (!_.includes(this._consoles, console)) {
                    var errMsg = 'console : ' + console.id + 'is not part of this console list.';
                    log.error(errMsg);
                    throw errMsg;
                }
                var consoleIndex = _.findIndex(this._consoles, console);

                _.remove(this._consoles, console);
                console.getHeader().remove();
                console.remove();
                /**
                 * console removed event.
                 * @event ConsoleList#console-removed
                 * @type {Console}
                 */
                this.trigger("console-removed", console);

                //switch to console at last or next index
                //make sure there are remaining consoles
                if (this._consoles.length > 0 && !_.isEqual(consoleIndex, -1)) {
                    // if removing console is 0th tab, next console is also the 0th
                    var nextConsoleIndex = 0;
                    if (!_.isEqual(consoleIndex, 0)) {
                        nextConsoleIndex = consoleIndex - 1;
                    }
                    var nextConsole = this._consoles[nextConsoleIndex];
                    this.setActiveConsole(nextConsole);
                } else {
                    this.hideConsoleComponents();
                }
            },
            /**
             * removes a form console
             * @param {Console} console the form console instance
             * @fires ConsoleList#console-removed
             */
            removeFormConsole: function (console) {
                if (!_.includes(this._consoles, console)) {
                    var errMsg = 'console : ' + console.id + 'is not part of this console list.';
                    log.error(errMsg);
                    throw errMsg;
                }
                _.remove(this._consoles, console);
                console.getHeader().remove();
                console.remove();
                this.trigger("console-removed", console);
                this.removePoppedUpElement();

                // setting the global console as the next active console
                var nextConsole = this.getGlobalConsole();
                this.setActiveConsole(nextConsole);
            },
            /**
             * removes all the console instances
             */
            removeAllConsoles: function () {
                this.hideAllConsoles();
                this.removePoppedUpElement();
            },
            /**
             * removes the classes added to pop-up the element
             */
            removePoppedUpElement: function () {
                $(".selected-element").removeClass("selected-element");
                $(".overlayed-container").fadeOut(200);
                $(".disableContainer").removeClass("disableContainer");
            },
            /**
             * set selected console
             * @param {Console} console the console instance
             * @fires ConsoleList#active-console-changed
             */
            setActiveConsole: function (console) {

                //set the corresponding active console for Tab
                if (console._type === "CONSOLE") {
                    $(".consoleToolbar").removeClass("hidden");
                    this.options.application.tabController.getActiveTab()._lastActiveConsole = "CONSOLE";
                } else if (console._type === "FORM"){
                    $(".consoleToolbar").addClass("hidden");
                    this.options.application.tabController.getActiveTab()._lastActiveConsole = "FORM";
                } else {
                    $(".consoleToolbar").addClass("hidden");
                    this.options.application.tabController.getActiveTab()._lastActiveConsole = "DEBUG";
                }

                if (!_.isEqual(this.activeConsole, console)) {
                    if (!_.includes(this._consoles, console)) {
                        var errMsg = 'console : ' + console.cid + 'is not part of this console list.';
                        log.error(errMsg);
                        throw errMsg;
                    }
                    var lastActiveConsole = this.activeConsole;
                    this.activeConsole = console;
                    var activeConsoleHeaderClass = _.get(this.options, 'headers.cssClass.active');

                    if (!_.isUndefined(lastActiveConsole)) {
                        lastActiveConsole.getHeader().removeClass(activeConsoleHeaderClass);
                        lastActiveConsole.setActive(false);
                    }
                    this.activeConsole.getHeader().addClass(activeConsoleHeaderClass);
                    this.activeConsole.setActive(true);
                    /**
                     * Active console changed event.
                     * @event ConsoleList#active-tab-changed
                     * @type {object}
                     * @property {Console} lastActiveConsole - last active console.
                     * @property {Console} newActiveConsole - new active console.
                     */
                    var evt = {lastActiveConsole: lastActiveConsole, newActiveConsole: console};
                    this.trigger("active-console-changed", evt);
                }
            },
            /**
             * active console
             * @returns {Console}
             */
            getActiveConsole: function () {
                return this.activeConsole;
            },
            getConsoleList: function () {
                return this._consoles;
            },
            getGlobalConsole: function () {
                return _.find(this._consoles, function (console) {
                    return console._type == "CONSOLE"
                });
            },
            showActiveConsole: function (activeConsole) {
                activeConsole.show(true);
                _.each(this._consoles, function (console) {
                    if (console._type !== "CONSOLE" && console._uniqueId !== activeConsole._uniqueId) {
                        console.hide();
                    }
                });
            },
            hideConsoles: function () {
                _.each(this._consoles, function (console) {
                    console.hide();
                });
            },
            enableConsoleByTitle: function (title,type) {
                var exist = false;
                var self = this;
                var globalConsole = this._consoles[_.findIndex(this._consoles, function(o) { return o._type ===
                    'CONSOLE'; })];
                _.each(this._consoles, function (console) {
                    if(console._type === type){
                        if (console._appName === title) {
                            /*
                            * If the user has closed the output console in a previous tab and switches back to the
                            * design view of a tab(earlier a form was kept opened in the output console in this tab and
                            * now it is hidden because user has closed the output console in the previous tab) now we
                            * need to enable the output console.
                            * */
                            if (type === 'FORM') {
                                self.showConsoleComponents();
                            }
                            console.show(true);
                            self.setActiveConsole(console);
                            globalConsole._isActive = false;
                        } else{
                            console.hide();
                            if(console._isActive){
                                self.setActiveConsole(globalConsole);
                            }
                        }
                    }
                });
            },
            /**
             * Creates a new console.
             * @param opts
             *          switchToNewConsole: indicate whether to switch to new console of type after creation
             *          consoleOptions: constructor args for the console
             * @returns {Console} created console instance
             * @event ConsoleList#console-added
             * @fires ConsoleList#active-console-changed
             */
            newConsole: function (opts) {
                var consoleOptions = _.get(opts, 'consoleOptions') || {};
                _.set(consoleOptions, 'application', this.options.application);
                _.assign(consoleOptions, _.get(this.options, 'consoles.console'));
                _.set(consoleOptions, 'consoles_container', _.get(this.options, 'consoles.container'));
                _.set(consoleOptions, 'parent', this);
                var consoleType = _.get(consoleOptions, '_type');
                var uniqueTabId = _.get(consoleOptions, 'uniqueTabId');
                var message = _.get(consoleOptions, 'message');
                var newConsole = this.getConsoleForType(consoleType, uniqueTabId);
                var currentFocusedFile = _.get(opts, 'consoleOptions.currentFocusedFile');
                var statusForCurrentFocusedFile = _.get(opts, 'consoleOptions.statusForCurrentFocusedFile');

                if (newConsole == undefined) {
                    newConsole = new this.ConsoleModel(consoleOptions);
                    if (consoleType == "CONSOLE") {
                        _.set(newConsole, '_title', _.get(consoleOptions, 'title'));
                        this.addConsole(newConsole);
                        _.set(newConsole, '_runStatus', true);
                        this.options.application.tabController.getActiveTab()._lastActiveConsole = "CONSOLE";

                        if (statusForCurrentFocusedFile == "SUCCESS") {
                            newConsole.showInitialStartingMessage(currentFocusedFile + ".siddhi " + message);
                        } else if(statusForCurrentFocusedFile != "LOGGER"){
                            var message = {
                                "type": "ERROR",
                                "message": "" + currentFocusedFile + ".siddhi - " + message + ""
                            };
                            newConsole.println(message);
                        }
                    } else {
                        _.set(newConsole, '_debugStatus', true);
                        _.set(newConsole, '_title', _.get(consoleOptions, 'title') + " - " + _.get(consoleOptions, 'appName'));
                        this.addConsole(newConsole);
                        this.options.application.tabController.getActiveTab()._lastActiveConsole = "DEBUG";
                    }
                } else if (newConsole !== undefined) {
                    if (consoleType == "CONSOLE") {
                        this.options.application.tabController.getActiveTab()._lastActiveConsole = "CONSOLE";
                        var consoleMessage;
                        if (statusForCurrentFocusedFile == "SUCCESS") {
                            consoleMessage = {
                                "type": "INFO",
                                "message": "" + currentFocusedFile + ".siddhi - " + message + ""
                            };
                        } else if (statusForCurrentFocusedFile != "LOGGER") {
                            consoleMessage = {
                                "type": "ERROR",
                                "message": "" + currentFocusedFile + ".siddhi - " + message + ""
                            };
                        }
                        newConsole.println(consoleMessage);
                    }
                }

                // check whether switch to new console set to false
                if (_.has(opts, 'switchToNewConsole')) {
                    if (_.isBoolean(_.get(opts, 'switchToNewConsole')) && _.get(opts, 'switchToNewConsole')) {
                        this.setActiveConsole(newConsole);
                    }
                } else {
                    // activate by default
                    this.setActiveConsole(newConsole);
                }
                this.showActiveConsole(newConsole);
                this.showConsoleComponents();
                this.getConsoleActivateBtn().parent('li').addClass('active');
                return newConsole;
            },

            /**
             * Creates a new console tab for a form.
             * @param opts
             *          switchToNewConsole: indicate whether to switch to new console of type after creation
             *          consoleOptions: constructor args for the console
             * @returns {Console} created console instance
             * @event ConsoleList#console-added
             * @fires ConsoleList#active-console-changed
             */
            newFormConsole: function (opts) {
                var consoleOptions = _.get(opts, 'consoleOptions') || {};
                _.set(consoleOptions, 'application', this.options.application);
                _.assign(consoleOptions, _.get(this.options, 'consoles.console'));
                _.set(consoleOptions, 'consoles_container', _.get(this.options, 'consoles.container'));
                _.set(consoleOptions, 'parent', this);
                var consoleType = _.get(consoleOptions, '_type');
                var uniqueTabId = _.get(consoleOptions, 'uniqueTabId');
                var newConsole = this.getConsoleForType(consoleType, uniqueTabId);
                var currentFocusedFile = _.get(opts, 'consoleOptions.currentFocusedFile');

                if (newConsole === undefined) {
                    newConsole = new this.ConsoleModel(consoleOptions);
                    if (consoleType === "FORM") {
                        _.set(newConsole, '_title', _.get(consoleOptions, 'appName'));
                        this.addConsole(newConsole);
                        this.options.application.tabController.getActiveTab()._lastActiveConsole = "FORM";

                    }
                }
                this.setActiveConsole(newConsole);
                this.showActiveConsole(newConsole);
                this.showConsoleComponents();
                this.getConsoleActivateBtn().parent('li').addClass('active');
                return newConsole;
            }
        });

    return ConsoleList;
});
