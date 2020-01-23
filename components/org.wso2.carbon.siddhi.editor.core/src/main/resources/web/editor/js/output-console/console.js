/**
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org)  Apache License, Version 2.0  http://www.apache.org/licenses/LICENSE-2.0
 */
define(['log', 'backbone', 'lodash', 'jquery'], function (log, Backbone, _, $) {

    var Console = Backbone.View.extend(
        /** @lends Console.prototype */
        {
            /**
             * @augments Backbone.View
             * @constructs
             * @class ConsoleView represents the view for console.
             */
            initialize: function (options) {

                _.set(this, 'id', this.cid);
                _.set(this, '_title', _.get(options, 'title'));
                if (!_.has(options, 'template')) {
                    errMsg = 'unable to find config template ' + _.toString(options);
                    log.error(errMsg);
                    throw errMsg;
                }
                template = $(_.get(options, 'template'));
                if (!template.length > 0) {
                    errMsg = 'unable to find template with id ' + _.get(options, 'template');
                    log.error(errMsg);
                    throw errMsg;
                }
                this._type = _.get(options, '_type');
                this._uniqueId = _.get(options, 'uniqueTabId');
                this._template = template;
                this.options = options;
                this._isActive = false;
                this._startedExecutionPlans = [];
                this.app = _.get(options, 'application');
                this._appName = _.get(options, 'appName') + ".siddhi";

                if (_.has(options, 'parent')) {
                    this.setParent(_.get(options, 'parent'));
                }

                // create the console template
                var console;
                _.set(options, 'parent-container', this.getParent().getConsoleContainer());
                _.set(options, 'cid', this.cid);

                if (this._type === "CONSOLE") {
                    console = this._template.children('div').clone();
                } else if (this._type === "DEBUG") {
                    var debugManager = this.app.tabController.activeTab.getSiddhiFileEditor().getDebuggerWrapper();
                    debugManager.initContainerOpts(options);
                    debugManager.render();
                    console = debugManager.getConsole();
                } else if (this._type === "FORM") {
                    var formTemplate = $("#form-template");
                    console = formTemplate.children('div').clone();
                }

                this.getParent().getConsoleContainer().append(console);
                var consoleClass = _.get(this.options, 'cssClass.console');
                console.addClass(consoleClass);
                console.attr('id', this.cid);
                this.$el = console;
                this._contentContainer = console;
            },
            setActive: function (isActive) {
                if (_.isBoolean(isActive)) {
                    this._isActive = isActive;
                    if (isActive) {
                        this.$el.addClass(_.get(this.options, 'cssClass.console_active'));
                    } else {
                        this.$el.removeClass(_.get(this.options, 'cssClass.console_active'));
                    }
                }
            },
            isActive: function () {
                return this._isActive;
            },
            setHeader: function (header) {
                this._consoleHeader = header;
            },
            getHeader: function () {
                return this._consoleHeader;
            },
            getContentContainer: function () {
                return this.$el.get(0);
            },
            getParent: function () {
                return this._parentConsoleList;
            },
            setParent: function (parentConsoleList) {
                this._parentConsoleList = parentConsoleList;
            },
            getTitle: function () {
                return _.isNil(this._title) ? "untitled" : this._title;
            },
            setTitle: function (title) {
                this._title = title;
                this.trigger('title-changed', title);
            },
            getType: function () {
                return _.isNil(this._type) ? "untitled" : this._type;
            },
            setType: function (type) {
                this._type = type;
            },
            addRunningPlanToList: function (executionPlan) {
                this._startedExecutionPlans.push(executionPlan);
            },
            hide: function () {
                if (this._consoleHeader.hasClass('active')) {
                    this._consoleHeader.removeClass('active');
                }

                this.$el.removeClass('active');
                this._consoleHeader.css('display', 'none');
            },
            show: function (makeActive) {

                if (makeActive) {
                    this._consoleHeader.addClass('active');
                    this.$el.addClass('active');
                } else {
                    if (this._consoleHeader.hasClass('active')) {
                        this._consoleHeader.removeClass('active');
                        this.$el.removeClass('active');
                    }
                }

                this._consoleHeader.css('display', 'block');
            }
        });

    return Console;
});
